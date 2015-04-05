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
 *  
 */

/*
 * This is used for times when we want a completely empty HashMap that will
 * never contain any values
 */

package com.nary.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EmptyFastMap<V, K> implements CaseSensitiveMap<K, V>, Serializable {
	private static final long serialVersionUID = 1L;
	final static Set emptySet	= new HashSet();
	
	@Override
	public boolean isCaseSensitive() {
		return false;
	}

	@Override
	public void clear() {
	}

	@Override
	public boolean containsKey(Object arg0) {
		return false;
	}

	@Override
	public boolean containsValue(Object arg0) {
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<String, V>> entrySet() {
		return null;
	}

	@Override
	public V get(Object arg0) {
		return null;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public Set<String> keySet() {
		return emptySet;
	}

	@Override
	public V put(String arg0, V arg1) {
		return null;
	}

	@Override
	public void putAll(Map<? extends String, ? extends V> arg0) {
	}

	@Override
	public V remove(Object arg0) {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public Collection<V> values() {
		return null;
	}
}
