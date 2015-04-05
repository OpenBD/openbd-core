/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
 *
 *  This file is part of Open BlueDragon (OpenBD) CFML Server Engine.
 *  
 *  OpenBD is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  Free Software Foundation,version 3.
 *  
 *  OpenBD is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with OpenBD.  If not, see http://www.gnu.org/licenses/
 *  
 *  Additional permission under GNU GPL version 3 section 7
 *  
 *  If you modify this Program, or any covered work, by linking or combining 
 *  it with any of the JARS listed in the README.txt (or a modified version of 
 *  (that library), containing parts covered by the terms of that JAR, the 
 *  licensors of this Program grant you additional permission to convey the 
 *  resulting work. 
 *  README.txt @ http://www.openbluedragon.org/license/README.txt
 *  
 *  http://www.openbluedragon.org/
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.cfLData;
import com.naryx.tagfusion.cfm.parser.runTime;

 
public class cfLOCK extends cfTag implements Serializable {

	static final long serialVersionUID = 1;
  
	public static final String TYPE_EXCLUSIVE = "exclusive";
	public static final String TYPE_READONLY = "readonly";

  public java.util.Map getInfo(){
  	return createInfo("system", "Places a lock around the block to ensure only one thread of execution can enter");
  }
  
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   			createAttInfo("ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
  			createAttInfo("NAME", 					"The name of the lock.  Should not be used with SCOPE.  All locks with the same name are locked", "", false ),
   			createAttInfo("SCOPE", 					"The scope of the lock.  Should not be used with NAME.  Values are: server, application, or session", "", false ),
   			createAttInfo("TYPE", 					"The type of lock to grant; values exclusive or readonly", "exculsive", false ),
   			createAttInfo("TIMEOUT", 				"The time in seconds that the process will wait for the lock to become available", "", true ),
   			createAttInfo("THROWONTIMEOUT", "If the timeout happens while waiting on a lock to become availabe, then throw an exception", "true", false ),
  	};
  }
  
	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute( "THROWONTIMEOUT", "YES" );
		defaultAttribute( "TYPE", TYPE_EXCLUSIVE );
		
		parseTagHeader( _tag );
		
		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;

		if ( !containsAttribute("TIMEOUT") )
			throw newBadFileException( "Missing Attribute", " CFLOCK requires the TIMEOUT attribute" );
			
		if ( containsAttribute("NAME") && containsAttribute("SCOPE") )
			throw newBadFileException( "Invalid Attributes", " specify either SCOPE or NAME, but not both" );
	}
  
	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if ( !containsAttribute(attributes,"TIMEOUT") )
			throw newBadFileException( "Missing Attribute", " CFLOCK requires the TIMEOUT attribute" );
			
		if ( containsAttribute(attributes,"NAME") && containsAttribute(attributes,"SCOPE") )
			throw newBadFileException( "Invalid Attributes", " specify either SCOPE or NAME, but not both" );
    
    return	attributes;
	}

	public String getEndMarker(){	return "</CFLOCK>";  }
	
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);

		String nameOfLock;
		if (containsAttribute(attributes,"SCOPE")) {
			String scope = getDynamic(attributes,_Session, "SCOPE").getString().toLowerCase();
			if (!scope.equals("application") && !scope.equals("server") && !scope.equals("session")) {
				throw newRunTimeException("Invalid attribute value. The SCOPE must be one of Server/Application/Session.");
			}
			nameOfLock = scope + ".lockingobject";
			if (_Session.getData(scope) == null) {
				throw newRunTimeException( "Cannot initiate lock on " + scope.toUpperCase() + " scope. Make sure the scope has been enabled via the CFAPPLICATION tag." );
			}
		} else if (containsAttribute(attributes,"NAME")) {
			nameOfLock = getDynamic(attributes,_Session, "NAME").getString().toLowerCase().trim();
			nameOfLock = "server." + cleanupLockName(nameOfLock);
		} else {
			nameOfLock = "server.lockingobject";
		}

		String type = getDynamic(attributes,_Session, "TYPE").getString().toLowerCase().trim();
		if (!type.equals(TYPE_EXCLUSIVE) && !type.equals(TYPE_READONLY)) {
			throw newRunTimeException("Invalid attribute value. The TYPE must be either EXCLUSIVE or READONLY");
		}

		int timeOut = getDynamic(attributes, _Session, "TIMEOUT" ).getInt() * 1000;
		if ( timeOut < 0 ) {
			throw newRunTimeException( "Invalid attribute value. The TIMEOUT must be greater than or equal to 0 (zero)");
		}

		cfLockingObject lock = getLock(_Session, nameOfLock);
		try {
			if (lock.lock(type, timeOut)) {
				try {
					cfTagReturnType rt = super.render(_Session);
					if ( !rt.isNormal() ) {
						return rt;
					}
				} finally {
					lock.unlock(type);
				}
			} else {
				cfEngine.log( "CFLOCK.TimedOut, " + getFile().getPath() + ", line " + this.posLine );
				if ( getConstant("THROWONTIMEOUT").toLowerCase().indexOf("yes") != -1 )
					throw newRunTimeException("CFLOCK lock timed out");
			}
		} finally {
			freeLock( _Session, nameOfLock, lock );
		}
		
		return cfTagReturnType.NORMAL;
	}
	
	public static synchronized cfLockingObject getLock( cfSession _Session, String _name ) throws cfmRunTimeException {
		cfLockingObject lockOb = getLockingObject( _Session, _name );
		if ( lockOb == null ) {
			lockOb = new cfLockingObject();
			_Session.setData( _name, lockOb );
		}
		lockOb.incrRefCount();
		return lockOb;
	}
	
	public static synchronized void freeLock( cfSession _Session, String _name, cfLockingObject lockOb ) throws cfmRunTimeException {
		lockOb.decrRefCount();
		if ( lockOb.isFree() ) {
			_Session.deleteData( _name );
		}
	}

	public static cfLockingObject getLockingObject( cfSession _Session, String _name ) {
		cfData lockOb = null;
		
		try {
			lockOb = runTime.runExpression( _Session, _name, false );
			if ( lockOb instanceof cfLData ) {
				if ( ((cfLData)lockOb).exists() ) {
					lockOb = ((cfLData)lockOb).Get( _Session.getCFContext() );
				} else {
					lockOb = null;
				}
			}
		} catch( cfmRunTimeException ignored ) {
		} // lockOb doesn't exist
		
		if ( lockOb instanceof cfLockingObject ) {
			return (cfLockingObject)lockOb;
		}
		
		return null;
	}

  public static String cleanupLockName( String _s ){
    String returnStr = "A" + _s;
    for ( int i = 0; i < returnStr.length(); i++ ){
      char nextCh = returnStr.charAt(i);
      // if not a valid char
      if ( !( Character.isDigit( nextCh ) || Character.isLetter( nextCh ) || ( nextCh == '_' ) ) ) {
        returnStr = returnStr.replace( nextCh, '_' );        
      }
    }
    return returnStr;
  }

}
