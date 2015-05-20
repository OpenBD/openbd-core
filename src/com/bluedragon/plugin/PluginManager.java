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
 *  $Id: PluginManager.java 2147 2012-07-02 01:57:34Z alan $
 */

/*
 * Created on 03-Feb-2005, 22-Sep-2009
 *
 * This is the main Plugin Manager class that is used as a bridge for managing
 * new plugins in the BlueDragon tree.
 * 
 * To create a plugin, you must:
 * 
 * - Implement the PlugIn interface, naming your class xyxPlugIn
 * - Optionally, package up your Plugin in a JAR file named: openbdplugin-xyz.jar
 * 
 * It will be auto-discovered on startup
 */
package com.bluedragon.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.engineListener;
import com.naryx.tagfusion.cfm.tag.cfSCRIPT;
import com.naryx.tagfusion.expression.compile.expressionEngine;
import com.naryx.tagfusion.util.dummyServletRequest;
import com.naryx.tagfusion.util.dummyServletResponse;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class PluginManager implements PluginManagerInterface, RequestListener {
	private static PluginManagerInterface thisInst;

	private List<Plugin> listOfPlugins;
	private List<RequestListener> listOfRequestListeners;
	private Set<String>	filesSet;
	private boolean bOverrideCore = true;

	public PluginManager(xmlCFML systemParameters) {
		
		thisInst = this;
		listOfPlugins = new ArrayList<Plugin>();
		listOfRequestListeners = new ArrayList<RequestListener>();
		filesSet	= new HashSet<String>();

    // Load in the standard plugins
		log("PlugInManager: Loading Standard Plugins" );
    loadPlugIn("org.alanwilliamson.openbd.plugin.spreadsheet.SpreadSheetExtension", systemParameters);
    loadPlugIn("org.alanwilliamson.openbd.plugin.cfsmtp.SmtpExtension", systemParameters);
    loadPlugIn("org.aw20.plugin.login.LoginExtension", systemParameters);
    loadPlugIn("org.alanwilliamson.openbd.plugin.crontab.CronExtension", systemParameters);
    loadPlugIn("com.bluedragon.mongo.MongoExtension", systemParameters);
    loadPlugIn("org.alanwilliamson.openbd.plugin.salesforce.SalesForceExtension", systemParameters);
    loadPlugIn("com.bluedragon.vision.VisionExtension", systemParameters);
    loadPlugIn("com.bluedragon.profiler.ProfilerExtension", systemParameters);

    int standardCount = listOfPlugins.size();
    log("PlugInManager: Standard Plugins=" + standardCount );
		

    // Look at the flag if we can override the function 
		bOverrideCore = systemParameters.getBoolean("server.system.pluginoverride", true);
    log("PluginManager: Auto Discovery [server.system.pluginoverride]=" + bOverrideCore );

    
    // Look at the dynamic
    log("PluginManager: Auto Discovery (openbdplugin-XXXX.jar)");
		try {
			ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
			URL[] urls = ((URLClassLoader)systemLoader).getURLs();
			inspectPaths( urls, systemParameters );
		} catch (Exception e) {
			log( "PluginManager.Exception: " + e.getMessage() );
		}

		try {
			ClassLoader systemLoader = Thread.currentThread().getContextClassLoader();
			URL[] urls = ((URLClassLoader)systemLoader).getURLs();
			inspectPaths( urls, systemParameters );
		} catch (Throwable e) {
			log( "PluginManager.Exception: " + e.getMessage() );
		}

		
		String	jarList			= systemParameters.getString("server.system.pluginjarpath");
		log("PluginManager: [server.system.pluginjarpath]=" + jarList );
		
		if ( jarList != null && jarList.length() > 0 ){
			
			String[]	jars	= jarList.split(",");
			for ( int x=0; x < jars.length; x++ ){
				try{
					if ( !jars[x].startsWith("file://") )
						jars[x]	= "file://" + jars[x];

					inspectJAR( new URL( jars[x] ), systemParameters );
				}catch(Exception e){
					log( "PluginManager.Exception: " + e.getMessage() );
				}
			}
			
		}

		// Finally look at the alternative lib path
		String	altlibpath 	= cfEngine.getAltLibPath();
		if ( altlibpath != null ){
			try {
				File p	= new File(altlibpath);
				inspectDIR( "", p, p.getPath(), systemParameters );
			} catch (Exception e) {
				log( "PluginManager.Exception: " + e.getMessage() );
			}
		}
		
		log("PlugInManager: Custom Plugins=" + (listOfPlugins.size()-standardCount) );
		
		// We don't need this anymore
		filesSet	= null;
	}
	
	
	private void inspectPaths( URL[] urls, xmlCFML systemParameters ) throws IOException{
		for (int x = 0; x < urls.length; x++) {
			String fullName = urls[x].toString();
			if (fullName.toLowerCase().indexOf("openbdplugin") != -1 && fullName.endsWith(".jar")) {
				inspectJAR( urls[x], systemParameters );
			}else{
				File file = new File(urls[x].getFile());
				inspectDIR( "", file, file.getPath(), systemParameters );
			}
		}
	}

	
	private void inspectDIR( String packageDir, File file, String topLevelDir, xmlCFML systemParameters) throws IOException {
		if ( filesSet.contains( file.getCanonicalFile().toString() ) )
			return;
		else
			filesSet.add( file.getCanonicalFile().toString() );
		
		if ( file.isFile() && file.getName().toLowerCase().endsWith("plugin.class") ){
			String classF = file.getName();
			classF = classF.substring(0, classF.lastIndexOf(".class") );
			if ( packageDir.length() != 0 )
				classF = packageDir + "." + classF;
		
			loadPlugIn( classF, systemParameters );
		} else if ( file.isDirectory() ){

			String curPath = file.getPath();
			if ( !curPath.equals(topLevelDir) ){
				if ( packageDir.length() == 0 )
					packageDir = file.getName();
				else
					packageDir = packageDir + "." + file.getName();
			}

			String childFiles[] = file.list();
			for ( int x=0; x < childFiles.length; x++ ){
				inspectDIR( packageDir, new File( file, childFiles[x] ), topLevelDir, systemParameters );
			}
		}
	}
	
	private void inspectJAR(URL jarFile, xmlCFML systemParameters) {
		if ( filesSet.contains( jarFile.toString() ) )
			return;
		else
			filesSet.add( jarFile.toString() );
		
		JarInputStream jarInputStream = null;
		
		try {
			jarInputStream = new JarInputStream(new FileInputStream( new File(jarFile.getFile()) ), false);
			JarEntry jarEntry = jarInputStream.getNextJarEntry();
			
			while (jarEntry != null) {
				if ( (!jarEntry.isDirectory()) && jarEntry.getName().toLowerCase().endsWith("plugin.class") ) {
					String classF = jarEntry.getName();
					classF = classF.substring(0, classF.lastIndexOf(".class") );
					classF = classF.replace('/', '.');
					
					loadPlugIn( classF, systemParameters );
				}
				jarEntry = jarInputStream.getNextJarEntry();
			}
		} catch (Throwable t) {
			if (!(t instanceof RuntimeException)) {
				throw new RuntimeException(t.toString(), t);
			} else {
				throw (RuntimeException) t;
			}
		} finally {
			org.aw20.io.StreamUtil.closeStream( jarInputStream );
		}
	}

	
	public void loadPlugIn( String pluginClass, xmlCFML systemParameters ){
		if ( "com.bluedragon.plugin.Plugin".equals(pluginClass) )
			return;
		
		try{ 
			Class<?> C = Class.forName( pluginClass ); 
			Plugin plugin = (Plugin)C.newInstance(); 
			plugin.pluginStart( this, systemParameters );
			listOfPlugins.add( plugin );
			log("PlugIn.Load.Installed: " + pluginClass + "; " + plugin.getPluginName() + "; Version=" + plugin.getPluginVersion() );
		}catch(ClassCastException ce){
			//ignore this because we may collide with other libraries/projects using xxxPlugin.java as their convention
		}catch(InstantiationException ce){
			//ignore this because we may collide with other libraries/projects using xxxPlugin.java as their convention
		}catch(IllegalAccessException ce){
			//ignore this because we may collide with other libraries/projects using xxxPlugin.java as their convention
		}catch(Throwable e){
			log("PlugIn.Load.Failed: " + pluginClass + "; Error:" + e.getMessage() );
		}
	}
	
	
	public void shutdown() {
		Iterator<Plugin> it = listOfPlugins.iterator();
		while (it.hasNext())
			it.next().pluginStop(this);
	}

	public void registerLangauge( String lang, String cfscriptClass ){
		cfSCRIPT.registerLanguage(lang, cfscriptClass);
	}

	public void registerTag(String tagName, String tagClass) {
		cfEngine.thisInstance.TagChecker.addTag(tagName, tagClass);
	}

	public void registerFunction(String functionName, String functionClass) {
		if ( bOverrideCore ){
			if ( expressionEngine.isFunction(functionName) )
				log("PluginManager: Core Function Replaced; Name=" + functionName + "; Class=" + functionClass );

			expressionEngine.addFunction(functionName, functionClass);
		}else if ( (!bOverrideCore && !expressionEngine.isFunction(functionName)) )
			expressionEngine.addFunction(functionName, functionClass);
	}

	public void addEngineListener(engineListener _new) {
		cfEngine.registerEngineListener(_new);
	}

	public void removeEngineListener(engineListener _new) {
		cfEngine.registerEngineListener(_new);
	}

	public void startRequestStats() {
		cfEngine.thisInstance.startRequestStats();
	}

	public void stopRequestStats() {
		cfEngine.thisInstance.stopRequestStats();
	}

	public long getStatsTotalRequests() {
		if (cfEngine.thisInstance.avgTracker != null)
			return cfEngine.thisInstance.avgTracker.getCount();
		else
			return -1;
	}

	public long getStatsAverageRequestTimeMS() {
		if (cfEngine.thisInstance.avgTracker != null)
			return cfEngine.thisInstance.avgTracker.getAverage();
		else
			return -1;
	}

	public long getStatsShortestRequestTimeMS() {
		if (cfEngine.thisInstance.avgTracker != null)
			return cfEngine.thisInstance.avgTracker.getMin();
		else
			return -1;
	}

	public long getStatsLongestRequestTimeMS() {
		if (cfEngine.thisInstance.avgTracker != null)
			return cfEngine.thisInstance.avgTracker.getMax();
		else
			return -1;
	}

	public long getStatsActiveRequests() {
		if (cfEngine.thisInstance.avgTracker != null)
			return cfEngine.thisInstance.avgTracker.getActiveCount();
		else
			return -1;
	}

	public void log(String log) {
		cfEngine.log(log);
	}

	public void addRequestListener(RequestListener _new) {
		listOfRequestListeners.remove(_new);
		listOfRequestListeners.add(_new);
		if (listOfRequestListeners.size() > 0) {
			cfEngine.thisInstance.registerRequestListener(this);
		}
	}

	public void removeRequestListener(RequestListener _new) {
		listOfRequestListeners.remove(_new);
		if (listOfRequestListeners.size() == 0) {
			cfEngine.thisInstance.removeRequestListener();
		}
	}

	public void requestBadFileException(cfmBadFileException bfException, cfSession session) {
		Iterator<RequestListener> it = listOfRequestListeners.iterator();
		while (it.hasNext())
			it.next().requestBadFileException(bfException, session);
	}

	public void requestEnd(cfSession session) {
		Iterator<RequestListener> it = listOfRequestListeners.iterator();
		while (it.hasNext())
			it.next().requestEnd(session);
	}

	public void requestRuntimeException(cfmRunTimeException cfException, cfSession session) {
		Iterator<RequestListener> it = listOfRequestListeners.iterator();
		while (it.hasNext())
			it.next().requestRuntimeException(cfException, session);
	}

	public void requestStart(cfSession session) {
		Iterator<RequestListener> it = listOfRequestListeners.iterator();
		while (it.hasNext())
			it.next().requestStart(session);
	}

	public static PluginManagerInterface getPlugInManager() {
		return thisInst;
	}

	public ObjectCFC createCFC(cfSession session, cfData cfcObject) throws Exception {
		if (cfcObject.getDataType() == cfData.CFCOMPONENTOBJECTDATA)
			return new ObjectCFCImpl((cfComponentData) cfcObject);
		else
			return createCFC(session, cfcObject.getString());
	}

	public ObjectCFC createCFC(cfSession session, String cfcName) throws Exception {
		return new ObjectCFCImpl(new cfComponentData(session, cfcName));
	}

	public cfSession createBlankSession() {
		return new cfSession(new dummyServletRequest(cfEngine.thisServletContext.getRealPath("/")), new dummyServletResponse(), cfEngine.thisServletContext);
	}
}
