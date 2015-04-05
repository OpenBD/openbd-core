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
package org.alanwilliamson.lang.java.inline;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.tag.tagUtils;

public class cfStruct extends Object implements Map {

	private cfStructData cfdata;
	
	public cfStruct( cfStructData data ){
		this.cfdata	= data;
	}
	
	public cfData getCFData(){
		return this.cfdata;
	}
	
	public String getName() {
		return "cfStruct";
	}
	
	public java.lang.Object get(String name){
		try {
			return ContextImpl.getForJava( cfdata.getData(name) );
		} catch (Exception e) {
			return null;
		}
	}
	
	public void put(String name, Object value) {
		cfdata.setData( name, tagUtils.convertToCfData( value ) );
	}

	public void clear() {
		cfdata.clear();
	}

	public boolean containsKey(Object key) {
		return cfdata.containsKey(key);
	}

	public boolean containsValue(Object arg0) {
		return cfdata.containsValue(arg0);
	}

	public Set entrySet() {
		return cfdata.entrySet();
	}

	public Object get(Object arg0) {
		return get( arg0.toString() );
	}

	public boolean isEmpty() {
		return cfdata.isEmpty();
	}

	public Set keySet() {
		return cfdata.keySet();
	}

	public Object put(Object name, Object value) {
		return cfdata.put( name, value );
	}

	public void putAll(Map arg0) {
		cfdata.putAll( arg0 );
	}

	public Object remove(Object arg0) {
		return cfdata.remove( arg0 );
	}

	public int size() {
		return cfdata.size();
	}

	public Collection values() {
		return cfdata.values();
	}
}