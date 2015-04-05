/* 
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmExitException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfEXIT extends cfTag implements Serializable 
{

	static final long serialVersionUID = 1;
  
	public java.util.Map getInfo(){
		return createInfo("control", "Used to exit a currently executing CFML custom tag, or exit the page within a currently executing CFML custom tag or re-executes a section of a CFML custom tag.");
	}
  
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
				createAttInfo("METHOD", "The type of CFEXIT function to perform. Valid options include \"EXITTAG\", \"EXITTEMPLATE\", \"LOOP\" or \"REQUEST\".", "EXITTAG", false )
		};

	}

	public static final String METHOD_EXITTAG      = "EXITTAG";
	public static final String METHOD_EXITTEMPLATE = "EXITTEMPLATE";
	public static final String METHOD_LOOP         = "LOOP";
	public static final String METHOD_REQUEST      = "REQUEST";

	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute( "METHOD", METHOD_EXITTAG );
		parseTagHeader( _tag );
	}
  
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		String method = getDynamic( _Session, "METHOD" ).getString();
		if ( !isValidMethod( method ) )
		{
			throw new cfmRunTimeException( catchDataFactory.invalidAttributeException( this, "cfexit.invalidMethod", null ) );
		}
    
		/*
		 * BlueDragon addition; method="request".  This will effectively be like a
		 * CFABORT but it suppresses the output to the browser if inside a CFSILENT
		 */
		if ( method.equalsIgnoreCase( METHOD_REQUEST ) ) {
			_Session.abortPageProcessing( false );
		}
    
    
		cfStructData thisTag = (cfStructData)_Session.getData( cfMODULE.THISTAG_SCOPE );
    	
		if ( thisTag == null ) { // executing within base template, not custom tag
			if ( method.equalsIgnoreCase( METHOD_LOOP ) ) {
				throw newRunTimeException( "CFEXIT METHOD=LOOP can only be used within custom tags" );
			} else {
				// from the CFMX docs: If this tag is encountered outside the context of a custom tag,
				// for example in the base page or an included page, it executes in the same way as cfabort;
				// there are three exceptions to this: within Application.cfm, cfinclude, and cffunction
				throw new cfmExitException();
			}
		}
	  
		// only get session output if executing within a custom tag end mode
		if ( thisTag.getData( cfMODULE.EXECUTION_MODE ).equals( cfMODULE.END_MODE ) ) {
			return cfTagReturnType.newExit( method, _Session.getOutputAsString() );
		}
	  
		return cfTagReturnType.newExit( method, "" );
	}
  
  public static boolean isValidMethod( String _method ){
  	return _method.equalsIgnoreCase( METHOD_EXITTAG ) ||
  			_method.equalsIgnoreCase( METHOD_EXITTEMPLATE ) ||
  			_method.equalsIgnoreCase( METHOD_REQUEST ) ||
  			_method.equalsIgnoreCase( METHOD_LOOP );
  }
  
}
