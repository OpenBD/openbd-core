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

package com.naryx.tagfusion.cfm.engine;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.tag.cfDUMP;
import com.naryx.tagfusion.cfm.tag.tagUtils;

/**
 * This class extends the regular cfStructData with the purpose of giving
 * access to java.util.Map instances (and subclasses of).
 * This will give us the ability to call methods of a subclass of Map 
 * that aren't in the declared Map methods.
 * 
 * This differs from cfStructData in that it stores its values in Java "natural"
 * form and converts to cfData as needed, the opposite of cfStructData.
 */
public class cfJavaStructData extends cfStructData implements java.io.Serializable{
	
	static final long serialVersionUID = 1;
	
	private Map theMap;
    
  /**
   *  The original case-sensitive keys are stored in "theMap". A mapping from the
   *  lowercased version of the key to the original key is stored in "keyMap". This
   *  allows us to do case-insensitive key lookup via the getMappedKey() method.
   */
  private Map keyMap;
	
	public cfJavaStructData( java.util.Map _map ){
        super( _map );
		theMap = _map;
    setInstance( theMap );
        
        // initialize keyMap for case-insensitive key lookups
        keyMap = new FastMap();
        
        Iterator iter = theMap.keySet().iterator();
        while ( iter.hasNext() ) {
            setMappedKey( iter.next().toString() );
        }
	}
    
  
	public void dump( java.io.PrintWriter out ) {
		dump( out, false, "", cfDUMP.TOP_DEFAULT );
	}

	public void dump( java.io.PrintWriter out, String _lbl, int _top ) {
		dump( out, false, _lbl, _top );
	}
    
	public void dumpLong( java.io.PrintWriter out ) {
		dump( out, true, "", cfDUMP.TOP_DEFAULT );
	}

	public void dumpLong( java.io.PrintWriter out, String _lbl, int _top ) 
	{
		dump( out, true, _lbl, _top );
	}
    
	protected void dump( java.io.PrintWriter out, boolean longVersion, String _lbl, int _top )	{
		out.write( "<table class='cfdump_table_struct'>" );
        
		Object[] keys = keys();
		if ( keys.length > 0 )
		{
			out.write( "<th class='cfdump_th_struct' colspan='2'>" );
			if ( _lbl.length() > 0 ) out.write( _lbl + " - " );        
			out.write( "struct</th>" );
			try{
				Arrays.sort( keys );
			}catch( ClassCastException e ){} // we can't guarantee the key types, but we'll try and sort them
			for ( int i = 0; i < keys.length; i ++ )
			{
				String key = keys[ i ].toString(); // again we can't guarantee the key type so use toString()
				out.write( "<tr><td class='cfdump_td_struct'>" );
				out.write( key );
				out.write( "</td><td class='cfdump_td_value'>" );
				if ( _top > 0 ){
					cfData dd = getForDump( key );
					if ( dd != null ) {
						int newTop = (dd.getDataType() == cfData.CFSTRUCTDATA ? _top - 1 : _top);
						if (longVersion)
							dd.dumpLong( out, "", newTop );
						else
							dd.dump( out, "", newTop );
					} else {
						out.write( "[null]" );
					}
				}
				out.write( "</td></tr>" );
			}
		} else {
			out.write( "<th class='cfdump_th_struct' colspan='2'>struct [empty]</th>" );
		}
        
		out.write( "</table>" );
	}

    
	// don't ever return null from this method
	private String getMappedKey( String key ) {
		Object realKey = keyMap.get( key.toLowerCase() );
		return ( realKey != null ? realKey.toString() : key );
	}
    
	private String setMappedKey( String key ) {
		String lowerKey = key.toLowerCase();
		if ( keyMap.containsKey( lowerKey ) ) {
			return (String)keyMap.get( lowerKey );
		}
		keyMap.put( lowerKey, key );
		return key;
	}
	
	public void clear() {
		theMap.clear();
	}

	public boolean containsKey(String _key) {
		return theMap.containsKey( getMappedKey( _key ) );
	}

	public boolean containsValue(cfData _data) {
		return theMap.containsValue( tagUtils.getNatural( _data ) );
	}

	public boolean containsValue(Object value) {
		return theMap.containsValue(value);
	}

	public Map copy() {
		FastMap copy = new FastMap();
		Object[] keys = keys();
		
		for ( int i = 0; i < keys.length; i ++ )
		{
			String key = (String)keys[ i ];
			Object val = getData( key );
			copy.put( key, val );
		}
		
		return copy;
	}

	public void deleteData(String _key) {
		theMap.remove( getMappedKey( _key ) );
		keyMap.remove( _key.toLowerCase() );
	}	

	public cfData duplicate() {
		java.util.Map duplicateMap = null;
		try{
			duplicateMap = (java.util.Map)theMap.getClass().newInstance();
		}catch( InstantiationException e ){
			duplicateMap = new java.util.HashMap();
		}catch( IllegalAccessException i ){
			duplicateMap = new java.util.HashMap();
		}
		duplicateMap.putAll( theMap );
		return new cfJavaStructData( duplicateMap );
	}
	
	public Set entrySet() {
		return theMap.entrySet();
	}

	// Map interface method, key is case-sensitive
	public Object get(Object key) {
		return theMap.get( key );
	}
	
	public cfData getData(String _key) {
		cfData data = tagUtils.convertToCfData( theMap.get( getMappedKey( _key ) ) );
		return ( data.getDataType() == cfData.CFNULLDATA ? null : data );
	}

	private cfData getForDump(String _key) {
		return tagUtils.convertToCfData( theMap.get( getMappedKey( _key ) ) );
	}

	public boolean equals( Object o ) {
		if ( o instanceof cfJavaStructData ) {
			return theMap.equals( ((cfJavaStructData)o).theMap );
		}
		return false;
	}
    
	public int hashCode() {
		return theMap.hashCode();
	}

	public boolean isEmpty() {
		return theMap.isEmpty();
	}

	public Object[] keys() {
		return theMap.keySet().toArray();
	}

	public Set keySet() {
		return theMap.keySet();
	}
	
	public Object put(Object key, Object value) { 
		return theMap.put( key, value); // key is case-sensitive
	}
	
	public void putAll(Map m) {
		Iterator iter = m.keySet().iterator();
		while ( iter.hasNext() ) {
			Object key = iter.next();
			put( key, m.get( key ) );
		}
	}

	// Map interface method, key is case-sensitive
	public Object remove(Object key) {
		Object obj = theMap.remove(key);
		if ( obj != null ) {
			keyMap.remove( key.toString().toLowerCase() );
		}
		return obj;
	}
	
	public void setData(String _key, cfData _data) {
		theMap.put( setMappedKey( _key ), tagUtils.getNatural(_data ) );
	}

	public int size() {
		return theMap.size();
	}

	public String toString() {
		return theMap.toString();
	}

	public Collection values() {
		return theMap.values();
	}
}
