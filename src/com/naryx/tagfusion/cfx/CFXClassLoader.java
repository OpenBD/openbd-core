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

package com.naryx.tagfusion.cfx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.security.SecureClassLoader;
import java.util.Hashtable;

import com.naryx.tagfusion.cfm.engine.cfEngine;

/**
 * CFXClassLoader
 * 
 * Java CFX tags need to be loaded in a separate classloader so that the class
 * files will get reloaded if they change. Also, this mimics the behavior of
 * CF5/MX regarding static class attributes (creating a new classloader every
 * time the tag is invoked causes the static attributes to be reset every time).
 */
public class CFXClassLoader extends SecureClassLoader {
	private String tagsFolder;

	// Cache classes
	private Hashtable classCache = new Hashtable();

	private Hashtable resolvedCache = new Hashtable();

	public CFXClassLoader(ClassLoader parent, String tagsFolder) {
		super(parent);

		if (tagsFolder.endsWith(File.separator))
			this.tagsFolder = tagsFolder;
		else
			this.tagsFolder = tagsFolder + File.separator;
	}

	public synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class c = null;

		// See if we have already cached this class
		// If so don't return yet because we may need to resolve it.
		c = (Class) classCache.get(name);

		// If it isn't cached then look for it in the tags folder and then delegate
		// to the parent classloader
		if (c == null) {
			// Try loading it from the tags folder if it's not a system class
			if (!name.startsWith("java.") && !name.startsWith("sun.") && !name.startsWith("javax.") && !name.startsWith("com.allaire") && !name.startsWith("com.nary")) {
				c = loadTagsFolderClass(name);
			}

			if (c == null) {
				// Delegate to the parent classloader
				try {
					if (getParent() == null)
						c = findSystemClass(name);
					else
						c = getParent().loadClass(name);
					if (c == null)
						throw new ClassNotFoundException(name + ", tags folder=" + tagsFolder);

					classCache.put(name, c);
				} catch (ClassNotFoundException e) {
					throw new ClassNotFoundException(name + ", tags folder=" + tagsFolder);
				} catch (Exception ex) {
					throw new ClassNotFoundException(name + ", " + ex.toString() + ", tags folder=" + tagsFolder);
				}
			}
		}

		// If resolve is true and we haven't already resolved this class then call
		// resolveClass
		if ((resolve) && (!resolvedCache.containsKey(name))) {
			resolveClass(c);
			resolvedCache.put(name, "true");
		}

		return c;
	}

	private Class loadTagsFolderClass(String name) {
		byte[] cdata = null;
		URL url = null;

		// Convert name from java.util.date to java/util/date
		String pathname = tagsFolder + name.replace('.', File.separatorChar) + ".class";

		// See if the class is in a .class file
		// NOTE: if tagsFolder is a relative path then with BD Server and JX the
		// getResolvedFile() method will resolve the path relative to the
		// built-in web server's wwwroot directory.
		File f = cfEngine.getResolvedFile(pathname);
		if ((f != null) && f.exists()) {
			// Load the class file
			cdata = loadClassFile(name, f);
			try {
				url = new URL("file:" + pathname.replace('\\', '/'));
			} catch (MalformedURLException e) {
				com.nary.Debug.printStackTrace(e);
			}
		} else if (pathname.startsWith("/")) // for packed WARs
		{
			try {
				url = cfEngine.thisServletContext.getResource(pathname.replace('\\', '/'));
				if (url != null) {
					URLConnection conn = url.openConnection();
					cdata = loadClassFile(conn.getInputStream(), conn.getContentLength());
				}
			} catch (Exception e) {
				com.nary.Debug.printStackTrace(e);
			}
		}

		try {
			// Cache and return the class
			if (cdata != null) {
				Class c;
				if (url != null)
					c = defineClass(name, cdata, 0, cdata.length, new CodeSource(url, (java.security.cert.Certificate[]) null));
				else
					c = defineClass(name, cdata, 0, cdata.length);
				classCache.put(name, c);
				return c;
			}
		} catch (ClassFormatError e) {
			com.nary.Debug.printStackTrace(e);
		}

		// Return null if the class isn't in the tags folder
		return null;
	}

	private byte[] loadClassFile(String name, File f) {
		if (f.canRead() && (f.length() > 0)) {
			try {
				return loadClassFile(new FileInputStream(f), (int) f.length());
			} catch (IOException e) {
				com.nary.Debug.printStackTrace(e);
			}
		}

		return null;
	}

	private byte[] loadClassFile(InputStream in, int len) {
		if (len <= 0)
			return null;

		try {
			byte[] cdata = new byte[len];

			int left = len;
			int num = 0;

			while (left > 0) {
				num = in.read(cdata, len - left, left);
				if (num == -1)
					break;

				left -= num;
			}

			in.close();

			return cdata;
		} catch (IOException e) {
			com.nary.Debug.printStackTrace(e);
			return null;
		}
	}
}
