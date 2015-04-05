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
 *  $Id: DynamicWebServiceTypeGenerator.java 2147 2012-07-02 01:57:34Z alan $
 */

/*
 * Created on Jan 22, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.naryx.tagfusion.cfm.xml.ws.dynws;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.axis.AxisProperties;
import org.apache.axis.MessageContext;
import org.apache.axis.components.compiler.Compiler;
import org.apache.axis.components.compiler.CompilerError;
import org.apache.axis.components.compiler.CompilerFactory;

import com.nary.util.FastMap;
import com.nary.util.UUID;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfWebServices;
import com.naryx.tagfusion.cfm.xml.ws.encoding.ser.IComplexObject;

public class DynamicWebServiceTypeGenerator {
	public static final String NAMESPACE = "na_svr";

	private static final String EOL = System.getProperty("line.separator");

	private String javaCacheDir;

	private String dynDirName;

	private Map<String, CFCSourceInfo> srcMap;

	private Map<String, String> srcBeanInfoMap;

	private List<DynamicCacheClassLoader> clList;

	private Map<String, String> typeList;

	private CFCDescriptor desc;

	public DynamicWebServiceTypeGenerator(String javaCache) {
		this.javaCacheDir = javaCache;
	}

	public String generateType(CFCDescriptor d, MessageContext msgContext) throws IOException {
		Map<String, CFCSourceInfo> sMap = new FastMap<String, CFCSourceInfo>();
		Map<String, String> sBeanInfoMap = new FastMap<String, String>();
		List<DynamicCacheClassLoader> clList = new ArrayList<DynamicCacheClassLoader>();
		String name = UUID.generateKey();

		// Populate the necessary lists
		String dynClsName = prepareType(d, name, new FastMap<String, String>(false), sMap, sBeanInfoMap, clList);

		// Build the classes (if necessary)
		if (buildClasses(name, sMap, sBeanInfoMap, clList)) {
			// Link the DynamicCacheClassLoaders together (as dependents)
			linkClassLoaders(getClassLoader(dynClsName));
		}

		// Return the class name
		return dynClsName;
	}

	public String prepareType(CFCDescriptor d, String dynName, Map<String, String> typeNames, Map<String, CFCSourceInfo> srcMap, Map<String, String> srcBeanInfoMap, List<DynamicCacheClassLoader> clList) {
		this.dynDirName = dynName;
		this.srcMap = srcMap;
		this.srcBeanInfoMap = srcBeanInfoMap;
		this.clList = clList;
		this.typeList = typeNames;
		String fqName = getFQName(d.getName());
		this.typeList.put(fqName, fqName);
		this.desc = d;
		return buildType();
	}

	public String getKnownType(String fullTypeName) {
		return this.typeList.get(fullTypeName);
	}

	public void addType(CFCDescriptor d) {
		DynamicWebServiceTypeGenerator g = new DynamicWebServiceTypeGenerator(this.javaCacheDir);
		g.prepareType(d, this.dynDirName, this.typeList, this.srcMap, this.srcBeanInfoMap, this.clList);
	}

	public static String getFQName(String cfcName) {
		String rtn = NAMESPACE + "." + cfcName.trim();
		String name = rtn.substring(rtn.lastIndexOf(".") + 1);
		rtn = rtn.substring(0, rtn.lastIndexOf("."));
		name = (Character.isDigit(name.charAt(0)) ? "n" + name : name);
		rtn = prefixNSDigits(rtn);
		return rtn + "." + name;
	}

	private String buildType() {
		// Create the full class name
		String name = getFQName(this.desc.getName());
		name = name.replaceAll("-", "");

		// Look for the generated type first
		DynamicCacheClassLoader cl = null;
		Class<?> cls = DynamicCacheClassLoader.findLoadedClass(name, DynamicCacheClassLoader.SKEL_CLASSES);
		if (cls != null)
			cl = (DynamicCacheClassLoader) cls.getClassLoader();

		// If it's been generated already, get the class path to include.
		// Otherwise, generate the java source code directly.
		if (cl == null)
			genSource();
		else
			this.clList.add(cl);

		// Return the class name
		return name;
	}

	private void genSource() {
		// Determine the package name for the generated source
		String prefix = this.desc.getName().trim();
		String pkgName = NAMESPACE;
		if (prefix.lastIndexOf(".") != -1) {
			prefix = prefix.substring(0, prefix.lastIndexOf("."));
			prefix = prefixNSDigits(prefix);
			pkgName += "." + prefix;
			pkgName = pkgName.replaceAll("-", "");
		}

		// Determine the class name for the generated source
		String className = this.desc.getName().trim().substring(this.desc.getName().trim().lastIndexOf(".") + 1);
		if (Character.isDigit(className.charAt(0)))
			className = "n" + className;

		// Start the java source
		StringBuilder buffy = startClass(pkgName, className);

		// Add WS methods for each CFC function
		addOperations(buffy);

		// Add WS properties for each CFC property
		addProperties(buffy);

		// Add the IComplexObject interface.
		addIComplexObjectInterface(buffy);

		// End the java source
		endClass(buffy);

		// Add the java source
		this.srcMap.put(pkgName + "." + className, new CFCSourceInfo(buffy.toString(), this.desc.getFile(), this.desc.getName(), pkgName + "." + className));

		// Start the BeanInfo source
		buffy = startBeanInfoClass(pkgName, className + "BeanInfo");

		// Add WS properties for each CFC property
		addBeanInfoProperties(pkgName + "." + className, buffy);

		// End the java source
		endClass(buffy);

		// Add the BeanInfo source
		this.srcBeanInfoMap.put(pkgName + "." + className + "BeanInfo", buffy.toString());
	}

	private StringBuilder startClass(String pkgName, String clsName) {
		StringBuilder buffy = new StringBuilder();
		buffy.append("/* This java file was dynamically generated by OpenBlueDragon */" + EOL);
		buffy.append("package " + pkgName + ";" + EOL);
		buffy.append(EOL);
		buffy.append("public class " + clsName + " implements ");
		buffy.append(Serializable.class.getName() + ", " + IComplexObject.class.getName() );
		buffy.append(" {" + EOL);
		return buffy;
	}

	private StringBuilder startBeanInfoClass(String pkgName, String clsName) {
		StringBuilder buffy = new StringBuilder();
		buffy.append("/* This java file was dynamically generated by OpenBlueDragon */" + EOL);
		buffy.append("package " + pkgName + ";" + EOL);
		buffy.append(EOL);
		buffy.append("public class " + clsName + " extends ");
		buffy.append(SimpleBeanInfo.class.getName());
		buffy.append(" {" + EOL);
		return buffy;
	}

	private void endClass(StringBuilder buffy) {
		buffy.append("/* End of dynamically generated class. */" + EOL);
		buffy.append("}" + EOL);
	}

	private void addOperations(StringBuilder buffy) {
		for (int i = 0; i < this.desc.getFunctionCount(); i++) {
			String rtnType = desc.getFunctionReturnType(i, this);
			String rtnTypeKlass = null;
			if (rtnType == null) {
				rtnType = "void";
				rtnTypeKlass = "null";
			} else {
				rtnTypeKlass = rtnType + ".class";
			}

			buffy.append("	public " + rtnType + " " + desc.getFunctionName(i) + "(");
			for (int x = 0; x < this.desc.getFunctionParameterCount(i); x++) {
				if (x > 0)
					buffy.append(", ");
				buffy.append(this.desc.getFunctionParameterType(i, x, this) + " " + this.desc.getFunctionParameterName(i, x));
			}
			buffy.append(") throws java.lang.Exception {" + EOL);

			// Have all operations look for a CFCInvoker in the Request context.
			// Then use it to do the actual invocation.
			buffy.append("		" + CFCInvoker.class.getName() + " cfcInvoker = " + CFCInvoker.class.getName() + ".getCFCInvoker(Thread.currentThread());" + EOL);
			buffy.append("		if (cfcInvoker == null)" + EOL);
			buffy.append("			throw new Exception(\"Cannot find a CFCInvoker for current thread: \" + Thread.currentThread() + \". Perhaps " + desc.getFunctionName(i) + " is being invoked a second time.\");" + EOL);
			buffy.append("		Class rtnType = " + rtnTypeKlass + ";" + EOL);
			buffy.append("		String[] argNames = new String[" + this.desc.getFunctionParameterCount(i) + "];" + EOL);
			buffy.append("		Object[] argValues = new Object[" + this.desc.getFunctionParameterCount(i) + "];" + EOL);
			for (int x = 0; x < this.desc.getFunctionParameterCount(i); x++) {
				buffy.append("		argNames[" + x + "] = \"" + this.desc.getFunctionParameterName(i, x) + "\";" + EOL);
				buffy.append("		argValues[" + x + "] = " + this.desc.getFunctionParameterName(i, x) + ";" + EOL);
			}
			if (rtnType.equals("void"))
				buffy.append("		cfcInvoker.invoke(\"" + this.desc.getFunctionName(i) + "\", argNames, argValues, rtnType, this.getClass().getClassLoader());" + EOL);
			else
				buffy.append("		return (" + rtnType + ")cfcInvoker.invoke(\"" + this.desc.getFunctionName(i) + "\", argNames, argValues, rtnType, this.getClass().getClassLoader());" + EOL);
			buffy.append("	}" + EOL);
			buffy.append(EOL);
		}
	}

	private void addProperties(StringBuilder buffy) {
		for (int i = 0; i < this.desc.getPropertyCount(); i++) {
			String typ = this.desc.getPropertyType(i, this);
			String name = this.desc.getPropertyName(i);
			buffy.append("	private " + typ + " " + name + ";" + EOL);
			buffy.append("	public " + typ + " get" + name + "(){" + EOL);
			buffy.append("		return " + name + ";" + EOL);
			buffy.append("	}" + EOL);
			buffy.append("	public void set" + name + "(" + typ + " pVal){" + EOL);
			buffy.append("		" + name + "=pVal;" + EOL);
			buffy.append("	}" + EOL);
		}
		buffy.append(EOL);
	}

	private void addBeanInfoProperties(String fullClassName, StringBuilder buffy) {
		buffy.append("	public " + PropertyDescriptor.class.getName() + "[] getPropertyDescriptors(){" + EOL);
		if (this.desc.getPropertyCount() > 0) {
			buffy.append("		try{" + EOL);
			buffy.append("			" + PropertyDescriptor.class.getName() + "[] rtn;" + EOL);
			buffy.append("			rtn = new " + PropertyDescriptor.class.getName() + "[" + this.desc.getPropertyCount() + "];" + EOL);
			for (int i = 0; i < this.desc.getPropertyCount(); i++) {
				String name = this.desc.getPropertyName(i);
				String readMethodName = "get" + name;
				String writeMethodName = "set" + name;
				buffy.append("			rtn[" + i + "] = new " + PropertyDescriptor.class.getName() + "(\"" + name + "\", " + fullClassName + ".class, \"" + readMethodName + "\", \"" + writeMethodName + "\");" + EOL);
			}
			buffy.append("			return rtn;" + EOL);
			buffy.append("		}" + EOL);
			buffy.append("		catch (" + IntrospectionException.class.getName() + " ex) {" + EOL);
			buffy.append("			throw new " + RuntimeException.class.getName() + "(\"Cannot create PropertyDescriptor for " + fullClassName + ". \" + ex.getMessage(), ex);" + EOL);
			buffy.append("		}" + EOL);
		} else {
			buffy.append("		return null;" + EOL);
		}
		buffy.append("	}" + EOL);
	}

	private void addIComplexObjectInterface(StringBuilder buffy) {
		// Add the bd_setFieldValues method.
		buffy.append("	public void bd_setFieldValues(");
		buffy.append(Map.class.getName() + " data, " + List.class.getName() + " missingRequiredFieldNames){" + EOL);

		// Now one for each defined property
		for (int i = 0; i < desc.getPropertyCount(); i++) {
			/*
			 * Add some code that looks like this:
			 * 
			 * if (data.containsKey("field1")) this.field1 =
			 * (MyFieldClass)data.get("field1");
			 * 
			 * Notice that this implementation doesn't add any names to the
			 * missingRequiredFieldNames List. That's because this is the server side
			 * IComplexObject. In practice this method should never actually be called
			 * (i.e. the conversion routines should never have a need to populate an
			 * instance of this Type).
			 */
			buffy.append("		if (data.containsKey(\"" + desc.getPropertyName(i) + "\"))" + EOL);
			buffy.append("			this." + desc.getPropertyName(i) + " = (" + this.desc.getPropertyType(i, this) + ")data.get(\"" + desc.getPropertyName(i) + "\");" + EOL);
		}
		buffy.append("	}" + EOL);
		buffy.append(EOL);

		// Add the bd_getFieldValues method.
		buffy.append("	public void bd_getFieldValues(" + Map.class.getName() + " data){" + EOL);

		// Now one for each defined property
		for (int i = 0; i < desc.getPropertyCount(); i++) {
			/*
			 * Add some code that looks like this.
			 * 
			 * data.put("field1", this.field1);
			 */
			buffy.append("		data.put(\"" + desc.getPropertyName(i) + "\", this." + desc.getPropertyName(i) + ");" + EOL);
		}
		buffy.append("	}" + EOL);
		buffy.append(EOL);

		// Add the bd_getFieldTypes method.
		buffy.append("	public void bd_getFieldTypes(" + Map.class.getName() + " data){" + EOL);

		// Now one for each defined property
		for (int i = 0; i < desc.getPropertyCount(); i++) {
			/*
			 * Add some code that looks like this.
			 * 
			 * data.put("field1", MyFieldClass.class);
			 */
			buffy.append("		data.put(\"" + desc.getPropertyName(i) + "\", " + desc.getPropertyType(i, this) + ".class);" + EOL);
		}
		buffy.append("	}" + EOL);
		buffy.append(EOL);

		// Add the bd_getCfcName method.
		buffy.append("	public String bd_getCfcName(){" + EOL);

		/*
		 * Add some code that looks like this.
		 * 
		 * return "MyCfcPackage.MyCfcName";
		 */
		buffy.append("		return \"" + desc.getName() + "\";" + EOL);
		buffy.append("	}" + EOL);
		buffy.append(EOL);
	}

	private boolean buildClasses(String name, Map<String, CFCSourceInfo> sMap, Map<String, String> sBeanInfoMap, List<DynamicCacheClassLoader> clList) throws IOException {
		if (sMap.size() > 0) {
			// Setup the classpath
			StringBuilder buffy = getDefaultClasspath();
			Iterator<DynamicCacheClassLoader> itr = clList.iterator();
			while (itr.hasNext())
				buffy.append(File.pathSeparator + itr.next().getCacheDir());

			// Write the files
			File rootDir = new File(genClassPath(name));
			File[] srcFiles = new File[sMap.size() + sBeanInfoMap.size()];
			CFCSourceInfo[] cfcInfo = new CFCSourceInfo[sMap.size()];
			Iterator<String> keys = sMap.keySet().iterator();
			for (int i = 0; keys.hasNext(); i++) {
				String tmp = keys.next();
				srcFiles[i] = writeFile(rootDir, tmp, sMap.get(tmp).source);

				// Keep the underlying cfc file reference, name, and IComplexObject
				// impl class name handy
				cfcInfo[i] = sMap.get(tmp);
			}

			keys = sBeanInfoMap.keySet().iterator();
			for (int i = sMap.size(); keys.hasNext(); i++) {
				String tmp = keys.next();
				srcFiles[i] = writeFile(rootDir, tmp, sBeanInfoMap.get(tmp));
			}

			// Compile the src
			Compiler compiler = CompilerFactory.getCompiler();
			compiler.setClasspath(buffy.toString());
			compiler.setDestination(rootDir.getAbsolutePath());
			for (int i = 0; i < srcFiles.length; i++) {
				compiler.addFile(srcFiles[i].getAbsolutePath());
			}
			boolean result = compiler.compile();

			// Problem encountered
			if (!result) {
				for (int i = 0; i < srcFiles.length; i++)
					new File(srcFiles[i].getAbsolutePath().substring(0, srcFiles[i].getAbsolutePath().length() - 5) + ".class").delete();

				// Build compile errors
				StringBuilder message = new StringBuilder("Error compiling: " + EOL);
				for (int i = 0; i < srcFiles.length; i++)
					message.append(srcFiles[i].getAbsolutePath() + EOL);
				message.append(":" + EOL);

				List<CompilerError> errors = compiler.getErrors();
				int count = errors.size();
				for (int i = 0; i < count; i++) {
					CompilerError error = errors.get(i);
					if (i > 0)
						message.append(EOL);
					message.append("Line ");
					message.append(error.getStartLine());
					message.append(", column ");
					message.append(error.getStartColumn());
					message.append(": ");
					message.append(error.getMessage());
				}

				message.append(EOL + "Classpath: " + EOL);
				message.append(buffy.toString() + EOL);
				throw new IOException("Server compileError: " + EOL + message.toString());
			}

			// Delete the temporary *.java file and check return code
			// for (int i=0; i<srcFiles.length; i++)
			// srcFiles[i].delete();

			// Add an addition to the class path for this new cache
			DynamicCacheClassLoader dcl = DynamicCacheClassLoaderFactory.newClassLoader(rootDir.getCanonicalPath(), DynamicCacheClassLoader.SKEL_CLASSES);
			clList.add(dcl);

			// Associate the generated classes with their cfc file
			// equivalents for this DynamicCacheClassLoader
			for (int i = 0; i < cfcInfo.length; i++) {
				dcl.associateCFC(cfcInfo[i].file);
				dcl.setIComplexObject(cfcInfo[i].name, cfcInfo[i].impl);
			}

			// Return true, we did create a new DynamicCacheClassLoader
			return true;
		}

		// Return false, we had no source to compile
		return false;
	}

	/**
	 * Writes the specified source out to a physical file in the rootDir. Returns
	 * the File that was written.
	 * 
	 * @param rootDir
	 *          root directory in which to write the files
	 * @param name
	 *          name of the file to write
	 * @param source
	 *          source of the file to write
	 * @return File created on disk
	 * @throws IOException
	 */
	private File writeFile(File rootDir, String name, String source) throws IOException {
		File f = new File(rootDir, name.replace('.', File.separatorChar) + ".java");
		if (!f.getParentFile().exists())
			f.getParentFile().mkdirs();
		f.createNewFile();
		if (!f.exists() && f.getAbsolutePath().length() < 256)
			throw new IOException("Could not create file: " + f.getAbsolutePath());

		// Write out the file
		Writer fw = null;
		try {
			fw = cfEngine.thisPlatform.getFileIO().getFileWriter(f);
			fw.write(source);
			fw.flush();
		} finally {
			if (fw != null)
				fw.close();
		}

		return f;
	}

	/**
	 * Link together the dependent DynamicCacheClassLoaders as associated
	 * instances to the primary DynamicCacheClassLoader (the one responsible for
	 * the type/class being generated). All DynamicCacheClassLoaders in the list
	 * at this point are needed to realize the requested type/class.
	 * 
	 * @param primaryCl
	 */
	private void linkClassLoaders(DynamicCacheClassLoader primaryCl) {
		for (int i = 0; i < this.clList.size(); i++)
			primaryCl.associateDynamicCacheClassLoader(this.clList.get(i));
	}

	public DynamicCacheClassLoader getClassLoader(String clsName) throws IOException {
		Class<?> dynClass = DynamicCacheClassLoader.findLoadedClass(clsName, DynamicCacheClassLoader.SKEL_CLASSES);
		if (dynClass == null)
			throw new IOException("Dynamic class: " + clsName + " not found. Perhaps there was an error compiling it.");
		return (DynamicCacheClassLoader) dynClass.getClassLoader();
	}

	private String genClassPath(String file) {
		File dir = new File(this.javaCacheDir);
		return new File(dir, file).getAbsolutePath();
	}

	private StringBuilder getDefaultClasspath() throws IOException {
		StringBuilder classpath = new StringBuilder();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		while (cl != null) {
			if (cl instanceof URLClassLoader) {
				URL[] urls = ((URLClassLoader) cl).getURLs();
				for (int i = 0; (urls != null) && i < urls.length; i++)
					addPathToBuffer(urls[i], classpath);
			}

			cl = cl.getParent();
		}

		// Add the BD JARs to the java compiler classpath
		String ps = System.getProperty("path.separator");
		String fs = System.getProperty("file.separator");
		String dir = null;
		String libDir = null;
		String bootClassPath = AxisProperties.getProperty("sun.boot.class.path");

		// The JARs are in the WEB-INF lib folder.
		dir = cfWebServices.getDocRootDir();
		if (dir != null && !dir.endsWith(fs))
			dir += fs;
		dir = dir + "WEB-INF" + fs;
		libDir = dir + "lib" + fs;
		
		String altLibDir	= cfEngine.getAltLibPath();

		if (!bootClassPath.contains("webservices.jar") && classpath.indexOf("webservices.jar") < 0) {
			classpath.append( ps );
			classpath.append( getJarPath(libDir, altLibDir, "webservices.jar") );
		}
		
		if (!bootClassPath.contains("wsdl4j.jar") && classpath.indexOf("wsdl4j.jar") < 0) {
			classpath.append( ps );
			classpath.append( getJarPath(libDir, altLibDir, "wsdl4j.jar") );
		}
		
		if (!bootClassPath.contains("saaj.jar") && classpath.indexOf("saaj.jar") < 0) {
			classpath.append( ps );
			classpath.append( getJarPath(libDir, altLibDir, "saaj.jar") );
		}
		
		if (!bootClassPath.contains("jaxrpc.jar") && classpath.indexOf("jaxrpc.jar") < 0) {
			classpath.append( ps );
			classpath.append( getJarPath(libDir, altLibDir, "jaxrpc.jar") );
		}

		classpath.append(ps + dir + "classes");

		// Add the J2EE specific jars (not required as classes, may be in the classes dir)
		classpath.append( ps );
		classpath.append( getJarPath(libDir, altLibDir, "OpenBlueDragon.jar") );

		// boot classpath isn't found in above search
		if (bootClassPath != null)
			classpath.append(ps + bootClassPath);

		return classpath;
	}

	
	public static String	getJarPath(String libDir, String altPath, String jarfile ) throws IOException {
		File f	= new File( libDir + jarfile );
		if ( f.exists() )
			return f.getAbsolutePath();
		
		if ( altPath != null ){
			f	= new File( altPath + jarfile );
			if ( f.exists() )
				return f.getAbsolutePath();
		}
		
		throw new IOException("JAR: " + jarfile + " was not found in either [" + libDir + "] or [" + altPath + "]");
	}
	
	
	@SuppressWarnings("deprecation")
	private void addPathToBuffer(URL url, StringBuilder classpath) {
		String path = url.getPath();

		// The path is URL encoded so we need to URL decode it
		// before adding it to the classpath.
		path = java.net.URLDecoder.decode(path);

		// If it is a drive letter, adjust accordingly.
		if (path.length() >= 3 && path.charAt(0) == '/' && path.charAt(2) == ':')
			path = path.substring(1);
		classpath.append(path);
		classpath.append(File.pathSeparatorChar);

		// if its a jar extract Class-Path entries from manifest
		File file = new File(url.getFile());
		if (file.isFile()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);

				if (isJar(fis)) {
					JarFile jar = new JarFile(file);
					Manifest manifest = jar.getManifest();
					if (manifest != null) {
						Attributes attributes = manifest.getMainAttributes();
						if (attributes != null) {
							String s = attributes.getValue(java.util.jar.Attributes.Name.CLASS_PATH);
							String base = file.getParent();
							if (s != null) {
								List<String> tokens = string.split(s, " ");
								for (int i = 0; i < tokens.size(); i++) {
									String t = tokens.get(i).toString();
									classpath.append(base + File.separatorChar + t);
									classpath.append(File.pathSeparatorChar);
								}
							}
						}
					}
				}
			} catch (IOException ioe) {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException ioe2) {
					}
				}
			}
		}
	}

	// an exception or emptiness signifies not a jar
	public static boolean isJar(InputStream is) {
		JarInputStream jis= null;
		try {
			jis = new JarInputStream(is);
			if (jis.getNextEntry() != null) {
				return true;
			}
		} catch (IOException ioe) {
		}finally{
			org.aw20.io.StreamUtil.closeStream(jis);
		}

		return false;
	}

	public static String prefixNSDigits(String str) {
		if (str != null) {
			str = str.trim();
			StringBuilder buffy = new StringBuilder();
			List<String> tokens = string.split(str, ".");
			for (int i = 0; i < tokens.size(); i++) {
				String t = tokens.get(i).toString();
				if (Character.isDigit(t.charAt(0)))
					t = "ns" + t;
				buffy.append("." + t);
			}
			if (str.endsWith("."))
				buffy.append(".");
			str = (str.startsWith(".") ? buffy.toString() : buffy.toString().substring(1));
		}
		return str;
	}

	private class CFCSourceInfo {
		public String source = null;
		public String name = null;
		public String impl = null;
		public File file = null;

		public CFCSourceInfo(String javaSource, File file, String name, String impl) {
			this.source = javaSource;
			this.file = file;
			this.name = name;
			this.impl = impl;
		}
	}
}
