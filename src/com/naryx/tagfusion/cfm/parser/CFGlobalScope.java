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
package com.naryx.tagfusion.cfm.parser;

/**
 * The global scope for a function invocation. This is just a wrapper around
 * an object, with a different thisValInvoke().
 */

import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;


public class CFGlobalScope implements CFGlobalScopeInterface, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private cfSession session;

	public CFGlobalScope(cfSession _session) {
		session = _session;
	}

	/**
	 * Return an lvalue for the given identifier (null if it doesn't exist) Throws
	 * CFException if the identifier does not exist
	 */

	public cfLData get( String name, CFContext context ) {
		return get( name, context, true );
	}

	public cfLData get( String name, CFContext context, boolean _doQuerysearch ) {
		return get( name, false, context, _doQuerysearch );
	}

	/**
	 * Return an lvalue for the given identifier. If create is true then create it
	 * regardless of current existence, otherwise throws CFException if the
	 * identifier does not exist
	 */

	public cfLData get( String name, boolean create, CFContext context ) {
		return get( name, create, context, true );
	}

	public cfLData get( String name, boolean create, CFContext context,
	    boolean _doQuerysearch ) {
		cfData data = getVal( name, context, _doQuerysearch );
		if ( data == null && create ) {
			return new cfLData( this, name, false, _doQuerysearch );
		} else if ( data == null ) {
			return null;
		}

		return new cfLData( this, name, data, _doQuerysearch );

	}

	/**
	 * Resolve the name, returning the value of the identifier (not the lvalue).
	 */

	public cfData getVal( String name, CFContext context ) {
		return getVal( name, context, true );
	}

	public cfData getVal( String name, CFContext context, boolean _doQuerysearch ) {
		cfData val = session.getData( name, _doQuerysearch );

		// in case someone removed the "this" reference from the variables scope; see bug #2912
		// TODO: references to "this" will probably perform better if we do this check first,
		// before the call to session.getData(); but I'm worried about the negative performance
		// impact that might have on non-"this" references
		if ( ( val == null )
		    && name.equalsIgnoreCase( "this" ) ) {
			val = session.getActiveComponentData();
		}
		return val; // might be null
	}

	// fudge - Not worth implementing
	public boolean containsVar( String name ) {
		return false;
	}

	/**
	 * Set the value of the given identifier. A new one is created if necessary.
	 */

	public void put( String name, cfData val, CFContext context )
	    throws cfmRunTimeException {
		session.setData( name, val );
	}

	/**
	 * Remove the identifier from the dictionary Throws CFException if the
	 * identifier does not exist
	 */

	public void remove( String name, CFContext context )
	    throws cfmRunTimeException {
		session.deleteData( name );
	}

	public void putGlobal( String name, cfData val ) {
		cfData tData = session.getQualifiedData( variableStore.VARIABLES_SCOPE );
		if ( tData != null ) {
			tData.setData( name, val );
		}
	}

  //for use by CFDUMP
  public Map<String, cfData> getScopeMap() {
	  throw new UnsupportedOperationException();
  }

}
