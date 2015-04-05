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

package org.alanwilliamson.lang.java;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.aw20.security.MD5;

import com.nary.io.StreamUtils;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.parser.script.JavaBlock;

public class JavaClassFactory {

	public int SRC_OFFSET_INLINE = 16;
	public String javaBlockTemplate, javaClassTemplate;
	
	private static String PACKAGE = "cfscript.java";
	private ClassLoader classLoader; 
	private Hashtable<String,Object>	attributes = null;
		
	public JavaClassFactory() throws IOException{
		classLoader 			= cfSCRIPTJava.class.getClassLoader();
		javaBlockTemplate	= StreamUtils.readToString( this.getClass().getResourceAsStream("javablock.java.txt") );
		javaClassTemplate	= StreamUtils.readToString( this.getClass().getResourceAsStream("javaclass.java.txt") );
		attributes				= new Hashtable<String,Object>();
	}
	
	public Hashtable<String,Object> getAttributes(){
		return attributes;
	}
	
	public JavaBlock	getCompiledClass( String javaTemplate, String javaSnippet, String jarlist, String importlist ) throws Exception {
		String CLASSNAME	= "cf" + MD5.getDigest(javaSnippet);
		
		importlist		= getImportList( importlist );
		
		JavaImport j	= extractImportList(javaSnippet);
		javaSnippet		= j.code;
		importlist		+= ";" + j.importlist;
		
		
		String javaCode = com.nary.util.string.replaceString(javaTemplate, "%PACKAGE%", PACKAGE, false );
		javaCode = com.nary.util.string.replaceString(javaCode, "%IMPORT%", importlist, false );
		javaCode = com.nary.util.string.replaceString(javaCode, "%CLASSNAME%", CLASSNAME, false );
		javaCode = com.nary.util.string.replaceString(javaCode, "%JAVASNIPPET%", javaSnippet, false );
		
		CharSequenceCompiler<JavaBlock> compiler = new CharSequenceCompiler<JavaBlock>( classLoader, getClasspath(jarlist) );
	  
	  try {
	  	Class<JavaBlock> newClass = compiler.compile( PACKAGE  + "." + CLASSNAME, javaCode, null );	
	  	return newClass.newInstance();
	  } catch (IllegalAccessException e) {
	  	cfEngine.log( e.getMessage() );
	  } catch (InstantiationException e) {
	  	cfEngine.log( e.getMessage() );
		}
		  
	  return null;
	}

	

	/**
	 * Retrieves all the imports for the generated class
	 * 
	 * @param javaSnippet
	 * @param importlist
	 * @return
	 */

	class JavaImport {
		public String code, importlist;
	}
	
	private JavaImport extractImportList(String javaSnippet) {
		StringBuilder sb = new StringBuilder(32);

		// Attempt to extract them from the body
		int c1=0, c2;
		
		while ( c1 != -1 ){
			c1 = javaSnippet.indexOf("import ", c1+1);
			if ( c1 != -1 ){
				c2 = javaSnippet.indexOf(";", c1+1);
				if ( c2 != -1 ){
					sb.append( javaSnippet.substring(c1, c2+1) );
					javaSnippet = javaSnippet.substring(0,c1) + javaSnippet.substring(c2+1);
				}
			}
		}
		
		JavaImport j = new JavaImport();
		j.code = javaSnippet;
		j.importlist = sb.toString();
		
		return j;
	}
	
	private String getImportList(String importlist) {
		StringBuilder sb = new StringBuilder(32);
		
		if ( importlist != null && importlist.length() > 0 ){
			String[] packages = importlist.split(",");
			for ( int x=0; x < packages.length; x++ ){
				if ( packages[x].trim().length() == 0 )
					continue;
				
				if ( packages[x].startsWith("import ") ){
					packages[x] = packages[x].substring( packages[x].indexOf(" ") );
				}
				
				if ( packages[x].endsWith(";"))
					packages[x] = packages[x].substring(0,packages[x].length()-1);
				
				sb.append( "import " + packages[x].trim() + ";" );
			}
		}
		
		return sb.toString();
	}
	

	private List<String> getClasspath(String jarlist) throws Exception{
		StringBuilder sb = new StringBuilder(1024);

		String jar	= findJarPath("classes/");
		if (jar != null ){
			sb.append( jar + File.pathSeparator );
		}
		
		// We need the main OpenBlueDragon.jar file
		String opbdjar	= findJarPath("OpenBlueDragon.jar");
		if (opbdjar == null ){
			if ( jar != null  ){ // final check to see if we are running unexploded
				jar	= jar + "org/alanwilliamson/lang/java/JavaClassFactory.class";
				if ( !new File(jar).exists() )
					throw new Exception("failed to find: OpenBlueDragon.jar in classpath" );
			}else
				throw new Exception("failed to find: OpenBlueDragon.jar in classpath" );
		}
		sb.append( opbdjar + File.pathSeparator );

		if ( jarlist != null ){
			String[] list = jarlist.split(",");
			for ( int x=0; x < list.length; x++ ){
				jar = list[x].trim();

				jar	= findJarPath(jar);
				if (jar == null ){
					throw new Exception("failed to find: " + jar + " in classpath" );
				}

				sb.append( jar + File.pathSeparator );
			}
		}

		return Arrays.asList(new String[] { "-classpath", sb.toString() });
	}

	
	/**
	 * Attempts to do a file search for the file path.  Returns null if not found
	 * 
	 * @param jarfile
	 * @return
	 */
	private	String	findJarPath( String jarfile ){
		jarfile	= jarfile.replace( '\\', '/' );
		
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		String path	= findJarPath( ((URLClassLoader)cl).getURLs(), jarfile );
		if ( path != null )
			return path;
		
		cl 	= getClass().getClassLoader();
		path	= findJarPath( ((URLClassLoader)cl).getURLs(), jarfile );
		if ( path != null )
			return path;
		
		cl	= Thread.currentThread().getContextClassLoader();
		path	= findJarPath( ((URLClassLoader)cl).getURLs(), jarfile );
		if ( path != null )
			return path;
		
		// could not be found
		return null;
	}
	
	
	private String findJarPath( URL[] urls, String jarfile ){
		for ( int x=0; x < urls.length; x++ ){
			String up = urls[x].getFile();
			if ( up.endsWith(jarfile) )
				return up;
		}
		
		return null;
	}
	
	
	/**
	 * Helper method to quickly determine the type of error the compiler threw.  This helps
	 * us "guestimate" if the CFML developer is using a simple inline java or if they have
	 * declared functions
	 * 
	 * @param e
	 * @return
	 */
	public boolean wasExpectingMethods(CharSequenceCompilerException e) {
		DiagnosticCollector<JavaFileObject> dC = e.getDiagnostics();
		
		Iterator<Diagnostic<? extends JavaFileObject>> it = dC.getDiagnostics().iterator();
		while ( it.hasNext() ){
			Diagnostic<? extends JavaFileObject> d = it.next();

			if ( d.getMessage(null).indexOf("class, interface, or enum expected") != -1 
					|| d.getMessage(null).indexOf("<identifier> expected") != -1 )
				return true;
		}
		
		return false;
	}

}
