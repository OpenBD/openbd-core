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
 * Created on Dec 28, 2004
 *
 */
package com.naryx.tagfusion.cfm.xml.ws.dynws;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import com.nary.io.FileUtils;
import com.nary.util.FastMap;

public class DynamicCacheClassLoader extends ClassLoader {
	public static final Object STUB_MUTEX = new Object();

	public static final Object SKEL_MUTEX = new Object();
	public static final int STUB_CLASSES = 0;
	public static final int SKEL_CLASSES = 1;

	private static Map stubLoaders = new FastMap();
	private static Map skelLoaders = new FastMap();

	private int category = STUB_CLASSES;
	private String cacheDir = null;
	private boolean validating = false;
	private boolean invalidating = false;
	private boolean registeringClasses = false;
	private Map allClasses = null;
	private Map associatedCFCs = null;
	private Map associatedCLs = null;
	private Map iComplexObjects = null;
	private String iQueryBean = null;
	private String iStructMap = null;

	/**
	 * Constructs a new DynamicCacheClassLoader instance with the specified
	 * directory as the directory it can load class definitions from and with the
	 * specified category (either STUB_CLASSES or SKEL_CLASSES).
	 * 
	 * NOTE: as this constructor alters the static list of STUB and SKEL
	 * DynamicCacheClassLoaders, it should be called from within a block that's
	 * synchronized on either the DynamicCacheClassLoader.STUB_MUTEX or the
	 * DynamicCacheClassLoader.SKEL_MUTEX.
	 * 
	 * @param dir
	 * @param cat
	 */
	public DynamicCacheClassLoader(String dir, int cat) {
		this(Thread.currentThread().getContextClassLoader(), dir, cat);
	}

	/**
	 * Constructs a new DynamicCacheClassLoader instance with the specified
	 * directory as the directory it can load class definitions from and with the
	 * specified category (either STUB_CLASSES or SKEL_CLASSES).
	 * 
	 * NOTE: as this constructor alters the static list of STUB and SKEL
	 * DynamicCacheClassLoaders, it should be called from within a block that's
	 * synchronized on either the DynamicCacheClassLoader.STUB_MUTEX or the
	 * DynamicCacheClassLoader.SKEL_MUTEX.
	 * 
	 * @param cl
	 * @param dir
	 * @param cat
	 */
	public DynamicCacheClassLoader(ClassLoader cl, String dir, int cat) {
		super(cl);
		init(dir, cat);
	}

	/**
	 * Performs initialization. Loads all the class definitions in this
	 * DynamicCacheClassLoader's directory. Adds this instance to one of the
	 * static DynamicCacheClassLoader lists (either the STUB or SKEL list).
	 * 
	 * @param dir
	 * @param cat
	 */
	protected void init(String dir, int cat) {
		if (!dir.endsWith(File.separator))
			dir += File.separator;
		this.category = cat;
		this.cacheDir = dir;

		// Add to the appropriate global list
		addClassLoader(this);

		// Populate the allClasses list
		this.allClasses = new FastMap();
		this.associatedCFCs = new FastMap();
		this.associatedCLs = new FastMap();
		this.iComplexObjects = new FastMap(FastMap.CASE_INSENSITIVE);
		addClassFromDir(new File(this.cacheDir));
	}

	/**
	 * Provides a meaningful description of this DynamicCacheClassLoader.
	 */
	public String toString() {
		return super.toString() + " " + (this.category == STUB_CLASSES ? "stubs" : "skels") + " " + this.cacheDir;
	}

	/**
	 * Returns the cache directory this DynamicCacheClassLoader loads from.
	 * 
	 * @return
	 */
	public String getCacheDir() {
		return this.cacheDir;
	}

	/**
	 * Returns the category this DynamicCacheClassLoader belongs to (either
	 * STUB_CLASSES or SKEL_CLASSES).
	 * 
	 * @return
	 */
	public int getCategory() {
		return this.category;
	}

	/**
	 * Returns the DynamicCacheClassLoader that corresponds to the specified
	 * directory. If no DynamicCacheClassLoader exists that is responsible for
	 * that directory, null is returned.
	 * 
	 * @param dir
	 * @param cat
	 * @return
	 */
	public static DynamicCacheClassLoader findClassLoader(String dir, int cat) {
		if (!dir.endsWith(File.separator))
			dir += File.separator;
		if (cat == STUB_CLASSES)
			return (DynamicCacheClassLoader) stubLoaders.get(dir);
		else
			return (DynamicCacheClassLoader) skelLoaders.get(dir);
	}

	/**
	 * Returns a Class that corresponds to the fully qualified name. If the class
	 * was loaded by a DynamicCacheClassLoader, that class is returned. Otherwise,
	 * null.
	 * 
	 * @param clsName
	 * @param cat
	 * @return
	 */
	public static Class findLoadedClass(String clsName, int cat) {
		Iterator itr = null;
		if (cat == STUB_CLASSES)
			itr = stubLoaders.values().iterator();
		else
			itr = skelLoaders.values().iterator();

		if (itr != null) {
			DynamicCacheClassLoader dcl = null;
			while (itr.hasNext()) {
				dcl = (DynamicCacheClassLoader) itr.next();
				Class klass = (Class) dcl.allClasses.get(clsName);
				if (klass != null)
					return klass;
			}
		}

		// Not loaded
		return null;
	}

	/**
	 * Adds the specified DynamicCacheClassLoader to the global list of
	 * DynamicCacheClassLoader instances. This list is used to locate
	 * DynamicCacheClassLoader instances when searching for class definitions.
	 * 
	 * @param cl
	 */
	private static void addClassLoader(DynamicCacheClassLoader cl) {
		if (cl.getCategory() == STUB_CLASSES) {
			if (!stubLoaders.containsKey(cl.getCacheDir()))
				stubLoaders.put(cl.getCacheDir(), cl);
		} else {
			if (!skelLoaders.containsKey(cl.getCacheDir()))
				skelLoaders.put(cl.getCacheDir(), cl);
		}
	}

	/**
	 * Locates the specified class by name (if available). Returns the Class
	 * instance or null if not found.
	 * 
	 * @param name
	 * @return
	 */
	protected Class findClass(String name) {
		Class cls = null;

		// Try any loaded classes first
		cls = DynamicCacheClassLoader.findLoadedClass(name, this.category);
		if (cls != null)
			return cls;

		// OK, no luck. Look for the class on the f/s
		cls = findClassOnDisk(name);
		return cls;
	}

	/**
	 * Looks for a class definition in a file on the filesystem. Returns a Class
	 * instance representing that class if found. Returns null otherwise.
	 * 
	 * @param name
	 * @return
	 */
	private Class findClassOnDisk(String name) {
		// Locate the file
		String pkg = name.substring(0, name.lastIndexOf('.'));
		String n = name.substring(name.lastIndexOf('.') + 1);
		File dir = new File(new File(this.cacheDir), pkg.replace('.', File.separatorChar));
		File file = null;

		if (!dir.exists())
			return null;
		
		File[] pFiles = dir.listFiles();
		if (pFiles != null) {
			for (int i = 0; i < pFiles.length; i++) {
				String t = pFiles[i].getName();
				int dotPos = t.indexOf(".");
				int tLength = t.length();
				
				if ( dotPos != -1 && dotPos == (tLength-6) && t.substring(tLength-6).equalsIgnoreCase(".class")) {
					if (t.substring(0, dotPos).equals(n)) {
						file = pFiles[i];
						break;
					}
				}
			}
		}

		if (file == null)
			return null;

		// OK load up the file data
		byte[] b = loadClassData(file);
		if (b != null) {
			Class rtn = defineClass(name, b, 0, b.length);
			this.allClasses.put(rtn.getName(), rtn);
			return rtn;
		} else {
			return null;
		}
	}

	/**
	 * Returns the contents of a class definition from the specified File. Returns
	 * null if there was a problem reading the File.
	 * 
	 * @param classFile
	 * @return
	 */
	private byte[] loadClassData(File classFile) {
		BufferedInputStream bin = null;
		FileInputStream fin = null;
		ByteArrayOutputStream bout = null;
		try {
			bout = new ByteArrayOutputStream();
			fin = new FileInputStream(classFile);
			bin = new BufferedInputStream(fin);
			int read = -1;
			byte[] buf = new byte[1024];
			while ((read = bin.read(buf, 0, buf.length)) != -1)
				bout.write(buf, 0, read);
			return bout.toByteArray();
		} catch (IOException xpn) {
			com.nary.Debug.printStackTrace(xpn);
			return null;
		} finally {
			try {
				if (fin != null)
					fin.close();
				if (bin != null)
					bin.close();
				if (bout != null)
					bout.close();
			} catch (IOException ex) {
				// Just eat it;
			}
		}
	}

	/**
	 * Returns a URL for the requested resource if such a resource exists in the
	 * directory this DynamicCacheClassLoader loads from.
	 * 
	 * @param name
	 * @return
	 */
	@SuppressWarnings("deprecation")
	protected URL findResource(String name) {
		File f = new File(new File(this.cacheDir), name);
		try {
			if (f.exists())
				return f.toURL();
			else
				return null;
		} catch (MalformedURLException ex) {
			return null;
		}
	}

	/**
	 * Returns an array of Class instances that this DynamicCacheClassLoader
	 * manages (is responsible for). If the DynamicCacheClassLoader has been
	 * invalidated, an empty array is returned.
	 * 
	 * @return
	 */
	public Class[] findAllClasses() {
		// Return what we have
		return (Class[]) this.allClasses.values().toArray(new Class[this.allClasses.size()]);
	}

	/**
	 * Associates a CFC File with this DynamicCacheClassLoader instance. This
	 * allows the DynamicCacheClassLoader to check for changes in CFC files. If
	 * changes have been made to the CFC File, this DynamicCacheClassLoader's
	 * classes should be invalidated.
	 * 
	 * @param cfcFile
	 *          File representation of the CFC on disk
	 */
	public void associateCFC(File cfcFile) {
		this.associatedCFCs.put(cfcFile, new Long(cfcFile.lastModified()));
	}

	/**
	 * Associates a CFC name and IComplexObject implementation Class name with
	 * this DynamicCacheClassLoader instance. This allows the
	 * DynamicCacheClassLoader to keep track of the IComplexObject implementations
	 * generated for the CFC.
	 * 
	 * @param cfcName
	 *          name of the CFC
	 * @param impl
	 *          name of the generated IComplexObject impl Class for this CFC
	 */
	public void setIComplexObject(String cfcName, String impl) {
		this.iComplexObjects.put(cfcName, impl);
	}

	/**
	 * Sets the IQueryBean implementation Class name for the set of classes
	 * managed by this DynamicCacheClassLoader.
	 * 
	 * @param impl
	 *          IQueryBean implementation Class name
	 */
	public void setIQueryBean(String impl) {
		this.iQueryBean = impl;
	}

	/**
	 * Sets the IStructMap implementation Class name for the set of classes
	 * managed by this DynamicCacheClassLoader.
	 * 
	 * @param impl
	 *          IStructMap implementation Class name
	 */
	public void setIStructMap(String impl) {
		this.iStructMap = impl;
	}

	/**
	 * Returns the IComplexObject implementation Class corresponding to the
	 * specified CFC name if it exists in this DynamicCacheClassLoader.
	 * 
	 * @param cfcName
	 *          CFC name corresponding to a IComplexObject implementation Class
	 * @return IComplexObject implementation Class corresponding to the specified
	 *         CFC name
	 */
	public Class findIComplexObject(String cfcName) {
		String cls = (String) this.iComplexObjects.get(cfcName);
		if (cls != null)
			return (Class) this.allClasses.get(cls);
		else
			return null;
	}

	/**
	 * Returns the IQueryBean implementation Class corresponding to the web
	 * service for which this DynamicCacheClassLoader was created, or null if no
	 * such implementation Class exists.
	 * 
	 * @return IQueryBean implementation Class corresponding to the web service
	 *         for which this DynamicCacheClassLoader was created
	 */
	public Class getIQueryBean() {
		if (iQueryBean != null)
			return (Class) this.allClasses.get(iQueryBean);
		else
			return null;
	}

	/**
	 * Returns the IStructMap implementation Class corresponding to the web
	 * service for which this DynamicCacheClassLoader was created, or null if no
	 * such implementation Class exists.
	 * 
	 * @return IStructMap implementation Class corresponding to the web service
	 *         for which this DynamicCacheClassLoader was created
	 */
	public Class getIStructMap() {
		if (iStructMap != null)
			return (Class) this.allClasses.get(iStructMap);
		else
			return null;
	}

	/**
	 * Associates a DynamicCacheClassLoader instance with this
	 * DynamicCacheClassLoader instance. The association means this instance
	 * relies on classes maintained by the specified/associated instance. When
	 * checking for class validity, the specified/associated instance (and any
	 * chained instances) will be checked as well.
	 * 
	 * @param cl
	 */
	public void associateDynamicCacheClassLoader(DynamicCacheClassLoader cl) {
		if (this != cl)
			this.associatedCLs.put(cl.getCacheDir(), cl);
	}

	/**
	 * Returns true if the classes loaded by this DynamicCacheClassLoader are
	 * still valid. False otherwise. The classes are considered invalid when any
	 * associated CFC files are modified from the time when the classes were first
	 * generated.
	 * 
	 * NOTE: this method should be called whenever the a class loaded by this
	 * DynamicCacheClassLoader is used (or more specifically when an appropriate
	 * time would be to regenerate the class definition).
	 * 
	 * @return
	 */
	public boolean areClassesValid() {
		// In the case of circular references don't continue recursion
		if (!this.validating) {
			try {
				this.validating = true;
				DynamicCacheClassLoader cl = null;
				File cfcFile = null;
				Iterator itr = null;

				// Check the directly associated CFC files
				itr = this.associatedCFCs.keySet().iterator();
				while (itr.hasNext()) {
					cfcFile = (File) itr.next();
					if (!cfcFile.exists() || cfcFile.lastModified() > ((Long) this.associatedCFCs.get(cfcFile)).longValue())
						return false;
				}

				// Check the dependent DynamicCacheClassLoader instance
				itr = this.associatedCLs.values().iterator();
				while (itr.hasNext()) {
					cl = (DynamicCacheClassLoader) itr.next();
					if (!cl.areClassesValid())
						return false;
				}
			} finally {
				this.validating = false;
			}
		}
		return true;
	}

	/**
	 * Causes this DynamicCacheClassLoader instance to unload all classes from
	 * it's list of known classes and removes the corresponding class files off
	 * the filesystem. This is typically used to invalidate classes that are now
	 * out of date.
	 * 
	 * NOTE: as this method alters the static list of STUB and SKEL
	 * DynamicCacheClassLoaders, it should be called from within a block that's
	 * synchronized on either the DynamicCacheClassLoader.STUB_MUTEX or the
	 * DynamicCacheClassLoader.SKEL_MUTEX.
	 */
	public void invalidate() {
		// In the case of circular references don't continue recursion
		if (!this.invalidating) {
			try {
				this.invalidating = true;

				// Delete the directory reference
				if (getCategory() == STUB_CLASSES)
					stubLoaders.remove(getCacheDir());
				else
					skelLoaders.remove(getCacheDir());

				// Clear the loaded classes
				allClasses.clear();

				// Delete the directory
				try {
					FileUtils.recursiveDelete( new File(getCacheDir()), true );
				} catch (IOException e) {}

				// Invalidate any dependent classes
				Iterator itr = associatedCLs.values().iterator();
				while (itr.hasNext())
					((DynamicCacheClassLoader) itr.next()).invalidate();

				associatedCFCs.clear();
				associatedCLs.clear();
				iComplexObjects.clear();
				iQueryBean = null;
				iStructMap = null;
			} finally {
				this.invalidating = false;
			}
		}
	}

	/**
	 * Registers this DynamicCacheClassLoader's classes and all dependent
	 * DynamicCacheClassLoader's classes with the specified ContextRegistrar.
	 * 
	 * @param ctxtReg
	 */
	public void registerClasses(ContextRegistrar ctxtReg) {
		// In the case of circular references don't continue recursion
		if (!this.registeringClasses) {
			try {
				this.registeringClasses = true;
				Iterator itr = null;

				// Register the classes
				itr = this.allClasses.values().iterator();
				while (itr.hasNext())
					ctxtReg.registerClass((Class) itr.next());

				// Pass to any associated DynamicCacheClassLoaders
				itr = this.associatedCLs.values().iterator();
				while (itr.hasNext())
					((DynamicCacheClassLoader) itr.next()).registerClasses(ctxtReg);
			} finally {
				this.registeringClasses = false;
			}
		}
	}

	/**
	 * Unregisters this DynamicCacheClassLoader's classes and all dependent
	 * DynamicCacheClassLoader's classes with the specified ContextRegistrar.
	 * 
	 * @param ctxtReg
	 */
	public void unregisterClasses(ContextRegistrar ctxtReg) {
		if (!this.registeringClasses) {
			try {
				this.registeringClasses = true;
				Iterator itr = null;

				// Unregister the classes
				itr = this.allClasses.values().iterator();
				while (itr.hasNext())
					ctxtReg.unregisterClass((Class) itr.next());

				// Pass to any associated DynamicCacheClassLoaders
				itr = this.associatedCLs.values().iterator();
				while (itr.hasNext())
					((DynamicCacheClassLoader) itr.next()).unregisterClasses(ctxtReg);
			} finally {
				this.registeringClasses = false;
			}
		}
	}


	/**
	 * Recursively adds all class files in the specified directory to this
	 * DynamicCacheClassLoader's list of loaded/available classes.
	 * 
	 * @param dir
	 */
	private void addClassFromDir(File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					addClassFromDir(files[i]);
				} else {
					try {						
						File outputDir = new File(this.cacheDir);
						String p = getRelativePath(outputDir.getCanonicalPath(), files[i].getCanonicalPath());
						if (p.startsWith(File.separator))
							p = p.substring(1);
						int ndx = p.lastIndexOf('.');
						// Only look for those ending with '.class'
						if (ndx == p.length() - 6 && p.substring(ndx).equalsIgnoreCase(".class")) {
							p = p.substring(0, p.length() - 6);
							p = p.replace(File.separatorChar, '.');

							// Calling defineClass() on a class will cause classes that it
							// extends
							// or implements to be loaded too so check if this class was
							// already loaded
							// when a previous class was loaded.
							Class klass = findLoadedClass(p);
							if (klass == null) {
								// This class wasn't already loaded so try to load it from disk.
								klass = findClassOnDisk(p);
							}
						}
					} catch (IOException ex) {
						com.nary.Debug.printStackTrace(ex);
						// Just move on.
					}
				}
			}
		}
	}

	/**
	 * Returns a relative path string created using the specified parentDir and
	 * fullPath strings. Specifically, it returns the portion of the fullPath that
	 * is not part of the parentDir.
	 * 
	 * @param parentDir
	 * @param fullPath
	 * @return
	 */
	private String getRelativePath(String parentDir, String fullPath) {
		if (fullPath.length() >= parentDir.length()) {
			if (fullPath.substring(0, parentDir.length()).equalsIgnoreCase(parentDir))
				return fullPath.substring(parentDir.length());
		}

		return fullPath;
	}

}
