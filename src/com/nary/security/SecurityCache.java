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

package com.nary.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfEngine;

/**
 * This class stores security roles information for users logged-in via the
 * &lt;cfloginuser> tag. It's shutdown method will clean out expired data.
 * 
 * This class facilitates the storing and reading in of security roles for
 * logged in users, to/from a back-end store (i.e. across restarts). Currently
 * the only supported type of back-end store is a file on the hard-drive.
 * 
 * This cache will: 1. attempt to read in previously persisted entries from
 * disk, during startup 2. persist all its non-expired entries to disk at
 * shutdown
 * 
 * This cache does not: 1. impose a maximum number of entries to manage memory
 * usage 2. do any sort of runtime swapping of entries between RAM and back-end
 * store 3. do any sort of periodic/Threaded cleanup of old, stale, expired
 * entries during runtime
 * 
 * So it is a potential memory "leak".
 */
public class SecurityCache {
	public static final String STORAGE_TYPE_KEY = "storageType";

	public static final String STORAGE_LOCATION_KEY = "storageFolderLocation";

	public static final String STORAGE_FILENAME = "securityRoles.data";

	String storageType, storageLocation;

	Map<String, SecurityCacheItem> data = null;

	int maxResidents;

	public SecurityCache(int maxResidents, Properties props) {
		this.maxResidents = maxResidents;

		init(props);
	}

	private void init(Properties config) {
		storageType = config.getProperty(STORAGE_TYPE_KEY);
		storageLocation = config.getProperty(STORAGE_LOCATION_KEY);

		// read-in/deserialize any persisted data
		read();

		if (data == null)
			data = new FastMap<String, SecurityCacheItem>();
		else
			removeExpiredEntries();
	}

	public Map<String, String> get(String key) {
		SecurityCacheItem o = null;
		Map<String, String> roles = null;

		o = data.get(key);
		if (o != null) {
			SecurityCacheItem item = o;
			if (!isExpired(item))
				roles = item.getData();
		}

		return roles;
	}

	public void add(String key, Map<String, String> val, long lifespan) {
		data.put(key, new SecurityCacheItem(val, lifespan));
	}

	public void remove(String key) {
		data.remove(key);
	}

	private void read() {
		File f = new File(storageLocation);
		if (f.exists()) {
			FileInputStream fin = null;
			ObjectInputStream in = null;

			try {
				// Create input streams for the data
				fin = new FileInputStream(f);
				in = new ObjectInputStream(fin);

				data = (Map<String, SecurityCacheItem>) in.readObject();
			} catch (Exception e) {
			} finally {
				// make certain that the input streams get closed
				if (in != null) {
					try {
						in.close();
						in = null;
					} catch (IOException ioe) {
					}
				}

				if (fin != null) {
					try {
						fin.close();
						fin = null;
					} catch (IOException ioe) {
					}
				}
			}
		}
	}

	public synchronized void persist() {
		File f = new File(storageLocation);
		if (data != null && data.size() > 0) {
			// make sure that the parent folder exists
			File parentFolder = f.getParentFile();

			if (!parentFolder.exists())
				parentFolder.mkdirs();

			OutputStream fout = null;
			ObjectOutputStream out = null;

			try {
				fout = cfEngine.thisPlatform.getFileIO().getFileOutputStream(f);
				out = new ObjectOutputStream(fout);

				// Write out the data
				out.writeObject(data);

				out.close();
				fout.close();
			} catch (Exception e) {
				// Since it wasn't written out properly we should delete it
				f.delete();
			} finally {
				// make certain that the streams get closed
				if (out != null) {
					try {
						out.close();
						out = null;
					} catch (IOException ioe) {
					}
				}

				if (fout != null) {
					try {
						fout.close();
						fout = null;
					} catch (IOException ioe) {
					}
				}
			}
		}
	}

	private static boolean isExpired(SecurityCacheItem item) {
		boolean isExpired = false;

		long lifespan = item.getLifespan();

		// modified a condition here to fix bug #1775
		isExpired = (lifespan == 0 || (lifespan > 0 && (item.getCreationTime() + lifespan <= System.currentTimeMillis())));

		return isExpired;
	}

	public synchronized void removeExpiredEntries() {
		Iterator<String> it = data.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			SecurityCacheItem item = data.get(key);
			Map<String, String> roles = item.getData();

			if (roles.isEmpty() || isExpired(item))
				it.remove();
		}
	}

	public synchronized void shutdown() {
		// first clean out all expired entries
		removeExpiredEntries();

		// now persist the roles
		persist();

		// now remove all in-memory entries
		data.clear();
	}

}// end SecurityCache class

/*****************/
/** inner class **/
/*****************/

class SecurityCacheItem implements java.io.Serializable {
	static final long serialVersionUID = 1;

	long creationTime = System.currentTimeMillis();

	Map<String, String> data = null;

	long lifespan = -1; // -1 = lives forever, 0 = dies ASAP

	/**
	 * 
	 * @param data
	 *          - a com.naryx.tagfusion.cfm.engine.cfJavaObjectData instance which
	 *          holds a Map whose keys are the roles specified by the roles
	 *          attribute of the &lt;cfloginuser> tag
	 * @param lifespan
	 *          - if cookies are used to track login data then this is the value
	 *          of the idleTimeout attribute of the &lt;cflogin> tag (converted to
	 *          milliseconds) (actually not anymore since fixing bug #2309), else
	 *          the login data is being tracked with the J2EE session, in which
	 *          case this value is -1. -1 is used to signify that the lifespan of
	 *          this cache item is not known at this time (sessions keep
	 *          refreshing as requests continue to come in). In this case, the
	 *          HttpSessionBindingListener interface COULD used by
	 *          SessionLoginToken, in tandem with
	 *          SecurityCache.removeExpiredEntries() to handle the expiring of
	 *          this item, but that's not implemeted at this time.
	 */
	SecurityCacheItem(Map<String, String> data, long lifespan) {
		this.data = data;
		this.lifespan = lifespan;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public long getLifespan() {
		return lifespan;
	}

	public Map<String, String> getData() {
		return data;
	}
}
