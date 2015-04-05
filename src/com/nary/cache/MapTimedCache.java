/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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

/*
 * MapTimedCache used for supporting:
 * 
 *  Caching using a map
 *  Sliding expiration
 *  No absolute expiration
 * 
 */
package com.nary.cache;

import com.nary.util.HashMapTimed;

public class MapTimedCache extends CacheBase {

	protected HashMapTimed<String, Object> cache;

	public MapTimedCache() {
		cache = new HashMapTimed<String, Object>(600);
	}

	public MapTimedCache(int timeOutSeconds) {
		cache = new HashMapTimed<String, Object>(timeOutSeconds);
	}

	public Object getFromCache(String key) {
		return cache.get(key);
	}

	public void setInCache(String key, Object obj) {
		cache.put(key, obj);
	}

	public void flushAll() {
		cache.clear();
	}

	public void flushEntry(String key) {
		cache.remove(key);
	}

	public int size() {
		return cache.size();
	}

	public Object[] getEntries() {
		return cache.values().toArray(new Object[cache.size()]);
	}
}
