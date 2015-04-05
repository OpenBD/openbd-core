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

/*
 * MapCache used for supporting:
 * 
 *  Caching using a map
 *  No sliding or absolute expiration
 * 
 */
package com.nary.cache;

import java.util.Map;

import com.nary.util.FastMap;

public class MapCache extends CacheBase {

	// all operations on the cache must be synchronized; in subclasses, too
	protected Map<String, Object> cache;

	private long statsHits=0, statsMisses=0;
	
	public MapCache(int size) {
		cache = new FastMap<String, Object>(size);
	}

	public MapCache() {
		cache = new FastMap<String, Object>();
	}

	public Object getFromCache(String key) {
		Object obj;
		synchronized (cache) {
			obj = cache.get(key);
			if (obj == null )
				statsMisses++;
			else
				statsHits++;
		}
		return obj;
	}

	public void setInCache(String key, Object obj) {
		synchronized (cache) {
			cache.put(key, obj);
		}
	}

	public void flushAll() {
		synchronized (cache) {
			cache.clear();
		}
		statsHits		= 0;
		statsMisses	= 0;
	}

	public void flushEntry(String key) {
		synchronized (cache) {
			cache.remove(key);
		}
	}

	public int size() {
		synchronized (cache) {
			return cache.size();
		}
	}

	public long getStatsHits(){
		return statsHits;
	}
	
	public long getStatsMisses(){
		return statsMisses;
	}
	
	public Object[] getEntries() {
		synchronized (cache) {
			return cache.values().toArray(new Object[cache.size()]);
		}
	}
}
