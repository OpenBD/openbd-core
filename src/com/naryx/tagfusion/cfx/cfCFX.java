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

package com.naryx.tagfusion.cfx;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import com.allaire.cfx.CustomTag;
import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class cfCFX extends cfTag implements Serializable{
  
	static final long serialVersionUID = 1;
	
	protected static final int RELOAD_AUTO = 0;		// Automatically reload Java CFX and dependent classes
													// within the classes directory whenever the CFX class
													// file changes.  Does not reload if a dependent class
													// file changes but the CFX class file does not change.
	
	protected static final int RELOAD_ALWAYS = 1;	// Always reload Java CFX and dependent classes within
													// the classes directory.  Ensures a class reload even
													// if a dependent class changes, but the CFX class file
													// does not change.

	protected static final int RELOAD_NEVER	= 2;	// Never reload Java CFX classes.  Load them once per
													// server lifetime.

	protected static boolean disableNativeTags = false;
	protected static cfxMapper	cfxMapperEngine;
	protected long cfxClassLastMod = -1;	// The last modified value for the .class file
	
	private static String	tagsFolder = "";
	
	transient private CFXClassLoader cfxClassLoader  = null;
 	transient private Class<?>		 cfxClass		 = null;	// The java class for the CFX tag
 	
 	public static void init( xmlCFML configFile ) {
 	 	
 		cfxMapperEngine	= new cfxMapper( configFile );
		
		// Determine the location of the tags folder
		tagsFolder = configFile.getString( "server.javacustomtags.classes", "" );
	}

	protected void defaultParameters( String _tag ) throws cfmBadFileException {

		parseTagHeader( _tag );

		//--[ Determine the name of this custom tag
		_tag	= _tag.replace('\t',' ');
		_tag	= _tag.replace('\r',' ');
		_tag	= _tag.replace('\n',' ');
		int c1 = _tag.indexOf(" ");
		if ( c1 == -1 )
			c1 = _tag.length() - 1;

		tagName	= _tag.substring( _tag.toLowerCase().indexOf( "cfx_" ), c1 );

		//--[ Check for the CFX_J notation
		if ( tagName.equalsIgnoreCase("cfx_j") ) {

			if ( !containsAttribute("CLASS") )
				throw missingAttributeException( "cfcfx.missingClass", null );		
		}

		// don't check for tag mapping--let configuration errors be caught during render
	}

	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {

		//--[ Get the debug attribute
		boolean debugOn = containsAttribute( "DEBUG" );

		if ( cfxMapperEngine.isNativeCFX( tagName ) )
			if ( disableNativeTags )
				throw newRunTimeException("Unmanaged C++ CFX tags are disabled. Check bluedragon.log for error messages.");
			else
				renderNativeCFX( _Session, debugOn );
		else
			renderJavaCFX( _Session, debugOn );

		return cfTagReturnType.NORMAL;
	}

	private void renderJavaCFX( cfSession _Session, boolean debugOn ) throws cfmRunTimeException {

		//--[ Get the RELOAD attribute
		int reloadType = RELOAD_AUTO;	// default
		if ( containsAttribute( "RELOAD" ) )
		{
			String reload = getDynamic( _Session, "RELOAD" ).getString();
			if ( reload.equalsIgnoreCase( "ALWAYS" ) )
				reloadType = RELOAD_ALWAYS;
			else if ( reload.equalsIgnoreCase( "NEVER" ) )
				reloadType = RELOAD_NEVER;
		}

		String className;
		if ( containsAttribute("CLASS") ) {
			className = getDynamic( _Session, "CLASS" ).getString();
		} else {
			className = cfxMapperEngine.getJavaTagClass( tagName );
		}

		if ( className == null ) {	// tag not configured as either C++ or Java CFX
			throw newRunTimeException( "Configuration error for Java tag [" + tagName + "]" );
		}

		//--[ Run the class
		runJavaCFX( _Session, debugOn, className, reloadType );
	}

	private void renderNativeCFX( cfSession _Session, boolean debugOn ) throws cfmRunTimeException {

		String moduleName = cfxMapperEngine.getNativeTagModuleName( tagName );
		String functionName = cfxMapperEngine.getNativeTagFunction( tagName );

		if ( ( moduleName == null ) || ( functionName == null ) ) {
			throw newRunTimeException( "Configuration error for C++ tag [ " + tagName + " ]" );
		}

		String modulePath = cfxMapperEngine.getNativeTagModulePath( tagName );
		boolean keepLibraryLoaded = cfxMapperEngine.keepLoaded( tagName );

		try {

			//--[ Create the necessary parameters
			sessionRequest	sRequest  = new sessionRequest( _Session, evaluateAttributes( _Session ), debugOn );
			sessionResponse	sResponse = new sessionResponse( _Session, debugOn );

			// convert moduleName to full physical path before native library call
			if ( modulePath == null ) {
				modulePath = com.nary.io.FileUtils.resolveNativeLibPath( moduleName );
				cfxMapperEngine.setNativeTagModulePath( tagName, modulePath );
			}

			long startTime = 0;	// for debug timings

			if ( debugOn )
				startTime = System.currentTimeMillis();

			runNativeCFX(modulePath, functionName, sRequest, sResponse, keepLibraryLoaded);

			if ( debugOn ) {
				long processingTime = System.currentTimeMillis() - startTime;
				_Session.write( "<hr><b>" + moduleName + "::" + functionName + "</b> Execution Time = " + processingTime + " ms <br><hr>" );
			}

		} catch ( UnsatisfiedLinkError ule ) {

			throw newRunTimeException( "UnsatisfiedLinkError invoking CFXNativeLib method" );

		} catch ( Exception E ) {

			cfCatchData	catchData	= new cfCatchData( _Session );
			catchData.setType( "Application" );
			catchData.setDetail( "CFCFX: " + tagName );
			catchData.setMessage( E.getMessage() );
			throw newRunTimeException( catchData );
		}
	}

	protected Map<String, String> evaluateAttributes( cfSession _Session ) throws cfmRunTimeException {
		//--[ This function runs through the hashtable of parameters calling the expression engine
		Map<String, String> values = new FastMap<String, String>( FastMap.CASE_INSENSITIVE );
		Iterator<String> iter = properties.keySet().iterator();
		while ( iter.hasNext() ) {
			String key = iter.next();
			values.put( key, getDynamic( _Session, key ).getString() );
		}

		return values;
	}

	private void runNativeCFX(String moduleName, String functionName, sessionRequest sRequest,
												sessionResponse sResponse, boolean keepLibraryLoaded) throws Exception
	{
		CFXNativeLib.processRequest(moduleName, functionName, sRequest, sResponse, keepLibraryLoaded);
	}

	private void runJavaCFX( cfSession _Session, boolean debugOn, String className, int reloadType )
		throws cfmRunTimeException
	{
		try 
		{
			CustomTag customJavaClass = getCustomTag( className, reloadType );
			
			//--[ Create the necessary parameters
			sessionRequest	sRequest  = new sessionRequest( _Session, evaluateAttributes( _Session ), debugOn );
			sessionResponse	sResponse = new sessionResponse( _Session, debugOn );
					
			long startTime = System.currentTimeMillis();	// for debug timings
				
			customJavaClass.processRequest( sRequest, sResponse );

			long processingTime = System.currentTimeMillis() - startTime;
					
			if ( debugOn ) 
			{
				_Session.write( "<hr><b>" + className + "</b> Execution Time = " + processingTime + " ms <br><hr>" );
			}	
		} 
		catch( java.lang.Exception E ) 
		{		
			cfCatchData	catchData = new cfCatchData( _Session );
			catchData.setType( "Application" );
			catchData.setMessage( "CFCFX: " + tagName );
			catchData.setDetail( E.toString() );
			throw newRunTimeException( catchData );
		}
	}
 	
	private CustomTag getCustomTag( String className, int reloadType ) throws Exception 
	{
		//--[ Try to find the class
		getJavaCFXClass( className, reloadType );
		//--[ return an instance of the class
		return (CustomTag)cfxClass.newInstance();
	}
	
  	/*
		getJavaCFXClass
		
		This method is synchronized so that two threads don't try to reload the CFX class
		at the same time.  This could happen if two requests were made at the same time for
		the same CFML page.
	*/
	private synchronized void getJavaCFXClass( String name, int reloadType ) throws ClassNotFoundException
	{
		// If reload type is NEVER and we've already loaded the CFX class just return
		if ( ( reloadType == RELOAD_NEVER ) && ( cfxClass != null ) )
			return;
		
		// (convert name from java.util.date to java/util/date)
		File f = new File( tagsFolder + name.replace( '.', File.separatorChar ) + ".class" );
		
		// If reload type is AUTO and we've already loaded the CFX class then see
		// if the CFX class file  has been modified and therefore needs to be reloaded
		if ( ( reloadType == RELOAD_AUTO ) && ( cfxClass != null ) )
		{	
			// If the CFX class hasn't been modified then just return
			if ( f.exists() )
			{
				if ( f.lastModified() == cfxClassLastMod )
					return;
				
				cfxClassLoader = null;	// make sure class is reloaded
			}
		}
		
		// If we hit here then the CFX needs to be loaded for the first time or
		// needs to be reloaded.
		if ( ( reloadType == RELOAD_ALWAYS ) || ( cfxClassLoader == null ) )
		{
			ClassLoader parent = Thread.currentThread().getContextClassLoader();
			cfxClassLoader = new CFXClassLoader( parent, tagsFolder );
		} 
		
		cfxClass = cfxClassLoader.loadClass( name );
		cfxClassLastMod = f.lastModified();
	}
}
