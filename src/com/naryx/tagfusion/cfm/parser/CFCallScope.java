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

import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class CFCallScope implements CFScope, java.io.Serializable{

	private static final long serialVersionUID = 1L;
	
	private static final String ARGUMENTS_SCOPE_NAME = "arguments";
	
	private Map<String, cfData> _ht;

	public CFCallScope( cfStructData _args ) {
		this();
		_ht.put( ARGUMENTS_SCOPE_NAME, ( _args != null ? _args : new cfStructData() ) );
	}

	public CFCallScope() {
		_ht = new FastMap<String, cfData>( FastMap.CASE_INSENSITIVE );
	}
	
	// for use by CFDUMP
	public Map<String, cfData> getScopeMap() {
		return _ht;
	}

	public final cfLData get( String name, CFContext context ) throws cfmRunTimeException {
		return get( name, false, context );
	}

	public final cfLData get( String _name, boolean create, CFContext context ) throws cfmRunTimeException {
		cfData val = _ht.get( _name );
		if ( !create && ( ( val == null ) || ( val == CFUndefinedValue.UNDEFINED ) ) ) {
			throw new CFException( "Local variable \"" + _name + "\" does not exist.", context );
		}
		if ( val instanceof cfLData ) { // true for indirectReference to argument in function local scope
			return (cfLData)val;
		}
		return new cfLData( this, _name, val, true );
	}

	public boolean containsVar( String name ) {
		cfData val = _ht.get( name );
		return ( ( val != null ) && ( val != CFUndefinedValue.UNDEFINED ) );
	}

	// create/overwrite a member value
	public final void put( String name, cfData val, CFContext context ) {
		_ht.put( name, val );
	}

	public cfData getVal( String name, CFContext context, boolean _doQuerySearch ) throws cfmRunTimeException {
		return getVal( name, context ); // ignores the _doQuerySearch
	}

	// resolve the member's value
	public cfData getVal( String name, CFContext context ) throws cfmRunTimeException {
		cfData val = _ht.get( name );

		if ( val != null ) {
			return val;
		} else {
			throw new CFException( "Can't find member \"" + name + "\" in object.", context );
		}
	}

	// remove a member
	public void remove( String name, CFContext context ) throws cfmRunTimeException {
		if ( _ht.containsKey( name ) ) {
			_ht.remove( name );
		} else {
			throw new CFException( "Can't find member \"" + name + "\" in object.", context );
		}
	}
}


