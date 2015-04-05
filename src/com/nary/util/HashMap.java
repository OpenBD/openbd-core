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

package com.nary.util;

/**
 * A subclass of java.util.HashMap that adds support for case-insensitive keys.
 * If case-insensitive, then keys are treated as Strings; if case-sensitive,
 * keys are treated as Objects (if case-sensitive, this class works exactly
 * the same as java.util.HashMap).
 *
 * DEPRECATION WARNING! This class is deprecated in favor of com.nary.util.FastMap.
 * This class should only be used when there's no other way to achieve backwards-
 * compatibility.
 *
 * @deprecated  Use com.nary.util.FastMap
 */
@Deprecated public class HashMap extends java.util.HashMap implements CaseSensitiveMap, java.io.Serializable
{
	static final long serialVersionUID = 1;

	public static final boolean CASE_SENSITIVE = true;
	public static final boolean CASE_INSENSITIVE = false;

	// since keyMap is not synchronized, make sure all references are
	// explicitly wrapped in synchronization blocks
	private HashMap keyMap;	// case-sensitive if ( keyMap == null )

	// HashMaps are case-sensitive by default (keyMap is initialized to null)
	@Deprecated public HashMap() {
	}

	@Deprecated public HashMap( java.util.Map map ) {
		super( map );
	}

	// can be used to create a case-insensitive HashMap
	// (this constructor is not part of the standard java.util.Map interface)
	@Deprecated public HashMap( boolean caseSensitive ) {
		if ( !caseSensitive ) {
			keyMap = new HashMap();
		}
	}

	// always creates a case-sensitive HashMap (keyMap is initialized to null)
	@Deprecated public HashMap( int initialCapacity ) {
		super( initialCapacity );
	}

	// this method is not part of the standard java.util.Map interface
	@Deprecated public HashMap( java.util.Map m, boolean _caseSensitive ) {
		this( _caseSensitive );
		putAll( m );
	}

	// this method is not part of the standard java.util.Map interface
	public boolean isCaseSensitive() {
		return ( keyMap == null );
	}

	// overrides java.util.HashMap
	public boolean containsKey( Object key ) {
		return super.containsKey( getMappedKey( key ) );
	}

	// overrides java.util.HashMap
	public Object get( Object key ) {
		return super.get( getMappedKey( key ) );
	}

	// overrides java.util.HashMap
	public Object put( Object key, Object value ) {
		return super.put( setMappedKey( key ), value );
	}

	// overrides java.util.HashMap
	public Object remove( Object key ) {
		return super.remove( getMappedKey( key ) );
	}

	private Object setMappedKey( Object key ) {
		if ( keyMap != null ) {
    		String lowerKey = key.toString().toLowerCase();
			synchronized ( keyMap ) {
				Object gotKey = keyMap.get( lowerKey );
				if ( gotKey != null ) return gotKey;
				keyMap.put( lowerKey, key );
			}
		}
		return key;
    }

	// don't ever return null from this method
    private Object getMappedKey( Object key ) {
    	if ( keyMap == null ) {
    		return key;
    	}
    	synchronized ( keyMap ) {
    		String realKey = (String)keyMap.get( key.toString().toLowerCase() );
    		return ( realKey != null ? realKey : key );
    	}
    }
}