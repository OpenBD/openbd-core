/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: $
 */
package com.nary.cache;

import java.util.Map;

import com.nary.util.FastMap;

public abstract class CacheBase {

	public abstract int size();

	public abstract Object[] getEntries();

	public abstract Object getFromCache(String key);

	public abstract void setInCache(String key, Object obj);

	public abstract void flushAll();

	public abstract void flushEntry(String key);

	// This HashMap maps a cacheKey to a lock object. This allows us to get a lock
	// for an individual entry instead of the entire cache. This improves
	// performance
	// by allowing multiple entries to be processed at the same time.
	private Map<String, Object> cacheLocks = new FastMap<String, Object>();

	public CacheBase() {
	}

	public Object getLock(String key) {
		synchronized (cacheLocks) {
			Object lock = cacheLocks.get(key);
			if (lock == null) {
				lock = new Object();
				cacheLocks.put(key, lock);
			}
			return lock;
		}
	}

	public void removeLock(String key) {
		synchronized (cacheLocks) {
			cacheLocks.remove(key);
		}
	}
}
