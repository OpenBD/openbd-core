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
 * The java version of the FileCache mechanism.
 * 
 */
package com.nary.cache;

import java.util.Iterator;

import com.naryx.tagfusion.cfm.file.cachedFile;

public class FileCache extends MapCache {

	public FileCache(int maxFiles) {
		super(maxFiles);
	}

	/*
	 * flushAll
	 * 
	 * Override the superclass method so we can check the neverExpires flag.
	 */
	public void flushAll() {
		synchronized (cache) {
			Iterator<String> it = cache.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				cachedFile FC = (cachedFile) cache.get(key);
				if (!FC.neverExpires())
					it.remove();
			}
		}
	}

	/*
	 * getEntries
	 * 
	 * Override the superclass method so we can return an array of the right type.
	 */
	public Object[] getEntries() {
		synchronized (cache) {
			return cache.values().toArray(new cachedFile[cache.size()]);
		}
	}

	public void setInCache(String key, Object obj, String fileDependency) {
		// The java version of FileCache doesn't use fileDependency
		setInCache(key, obj);
	}

	public void _flushFile(String _file) {
		synchronized (cache) {
			Iterator<String> it = cache.keySet().iterator();
			while (it.hasNext()) {
				String nextKey = it.next();
				if (nextKey.endsWith(_file)) {
					it.remove();
				}
			}
		}
	}

	public void deleteOldestFile() {
		// - Runs through the list and removes the oldest file
		String togoKey = null;
		long min = System.currentTimeMillis();

		synchronized (cache) {
			Iterator<String> it = cache.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				cachedFile FC = (cachedFile) getFromCache(key);
				if (FC.lastAccessed <= min && !FC.neverExpires()) {
					togoKey = key;
					min = FC.lastAccessed;
				}
			}

			if (togoKey != null) {
				flushEntry(togoKey);
			}
		}
	}
}
