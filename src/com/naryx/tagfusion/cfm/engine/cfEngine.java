/*
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: cfEngine.java 2486 2015-01-22 03:22:37Z alan $
 */

package com.naryx.tagfusion.cfm.engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.vfs.FileSystemManager;
import org.aw20.io.StreamUtil;
import org.aw20.net.HttpGet;

import com.bluedragon.journal.JournalManager;
import com.bluedragon.journal.JournalSession;
import com.bluedragon.platform.Platform;
import com.bluedragon.plugin.PluginManager;
import com.bluedragon.plugin.RequestListener;
import com.nary.io.FileUtils;
import com.nary.io.StreamUtils;
import com.nary.util.AverageTracker;
import com.nary.util.FastMap;
import com.nary.util.Localization;
import com.naryx.tagfusion.cfm.cache.CacheFactory;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.file.cfmlFileCache;
import com.naryx.tagfusion.cfm.sql.ODBCNativeLib;
import com.naryx.tagfusion.cfm.sql.cfDataSourceStatus;
import com.naryx.tagfusion.cfm.sql.pool.DataSourcePoolFactory;
import com.naryx.tagfusion.cfm.tag.tagChecker;
import com.naryx.tagfusion.cfm.tag.ext.thread.cfThreadRunner;
import com.naryx.tagfusion.expression.compile.expressionEngine;
import com.naryx.tagfusion.expression.function.string.deserializejson;
import com.naryx.tagfusion.xmlConfig.xmlCFML;
import com.naryx.tagfusion.xmlConfig.xmlConfigManagerFactory;

public class cfEngine extends Object implements cfEngineMBean {
	public static final String DEFAULT_BUFFERSIZE = "0";
	public static final String DEFAULT_SUPPRESS_WHITESPACE = "false";
	public static final String DEFAULT_ERROR_HANDLER = "";
	public static final String DEFAULT_MISSING_TEMPLATE_HANDLER = "";
	public static final String DEFAULT_DEBUG = "true";
	public static final String DEFAULT_CHARSET = "UTF-8";
	public static final String DEFAULT_SCRIPTPROTECT = "false";
	public static final String DEFAULT_SCRIPTSRC = "/bluedragon/scripts/";
	public static final String DEFAULT_ASSERT = "false";
	public static final String DEFAULT_STRICT_FUNCTIONSCOPEDVARS = "false";

	public static final String DEFAULT_STRICT_ARRAYBYREFERENCE = "false";

	public static final String 	PRODUCT_NAME = "BlueDragon";

	public static String PRODUCT_VERSION;
	public static String PRODUCT_RELEASEDATE;
	public static String PRODUCT_STATE;
	public static String BUILD_ISSUE;
	public static boolean WINDOWS;

	public static cfEngine thisInstance = null;
	public static ServletContext thisServletContext;

	public tagChecker TagChecker = null;
	public long startTime = 0;
	public long totalBytesSent = 0, totalRequests = 0;

	private String nativeLibDirectory;
	private File xmlFileLocation = null;

	private final Map<String, cfDataSourceStatus> dataSourceStatus = new FastMap<String, cfDataSourceStatus>();

	private xmlCFML systemParameters; // this is the in-memory representation of bluedragon.xml

	private boolean bDebugOutputEnabled = true,
									bAssertionsEnabled = false,
									bSuppressWhitespace = false,
									bLegacyFormValidation = true,
									bCombinedFormUrlScope = false,
									bFunctionScopedVariables = false,
									bStrictArrayPassByReference = false,
									bMainFormUrlCase = false,
									bDebugerOutputEnabled = false,
									bCFoutputShorthand = false;

	private Vector<engineListener> engineListeners;
	private ResourceBundle runtimeMessages;

	public AverageTracker avgTracker;
	public JournalManager	journalManager;

	private String defaultCharset, webResourcePath;

	private final com.bluedragon.plugin.PluginManager pluginManager;
	private com.bluedragon.plugin.RequestListener requestListener = null;

	private int[] debugIPsAsInts;

	public static Platform	thisPlatform;
	public static FileSystemManager vfsManager;
	public static DataSourcePoolFactory	dataSourcePoolFactory;

	public static boolean bEngineActive = true;
	public static String	OpenBDLogoDataUri = null;
	public static String 	DefaultJSONReturnCase = "maintain";
	public static String 	DefaultJSONReturnDate = "long";

	public static AtomicInteger	sessionCounter	= new AtomicInteger();

	static {
		WINDOWS = System.getProperty("os.name").startsWith("Windows");
		PRODUCT_VERSION = "Unknown";
		PRODUCT_RELEASEDATE = "Unknown";
		PRODUCT_STATE= "Unknown";

		try{
			InputStream in = cfEngine.class.getResourceAsStream("/openbd.properties");
			if ( in != null ){
				Properties props = new Properties();
				props.load( in );
				PRODUCT_VERSION = props.getProperty( "version", PRODUCT_VERSION );
				BUILD_ISSUE = props.getProperty( "builddate", "" );
				PRODUCT_STATE = props.getProperty( "state", PRODUCT_STATE );
				PRODUCT_RELEASEDATE = props.getProperty( "releasedate", PRODUCT_RELEASEDATE );
			}
		}catch( Exception e ){}
	}



	/** ------------------------------------------------------
	 * Initialisation method
	 *
	 * Run once and initialises all the sub-components of the main engine
	 */
	private cfEngine(ServletConfig config) throws ServletException {
		thisInstance = this;
		thisServletContext = config.getServletContext();

		// Setup the main marker class for the platform flag
		String bluedragonXmlParm = config.getInitParameter("BLUEDRAGON_XML");

		try{

			thisPlatform	= (Platform)Class.forName("com.bluedragon.platform.java.JavaPlatform").newInstance();

			if ((bluedragonXmlParm != null) && (bluedragonXmlParm.length() > 0))
				xmlFileLocation	= getOpenBDXmlFile( bluedragonXmlParm );

		}catch(Exception e){
			throw new ServletException(PRODUCT_NAME + ": " + e );
		}


		// Load in the main XML configuration file
		try {
			setSystemParameters(xmlConfigManagerFactory.createXmlConfigManager(xmlFileLocation).getXMLCFML());
			engineListeners = new Vector<engineListener>();
		} catch (Exception E) {
			thisInstance = null;
			System.out.println(PRODUCT_NAME + ": Error " + E + " loading: BLUEDRAGON_XML=" + bluedragonXmlParm);
			throw new ServletException(PRODUCT_NAME + ": Error occurred loading: BLUEDRAGON_XML=" + bluedragonXmlParm + ", " + E);
		}


		// Load in the image
		try {
			InputStream in = this.getClass().getResourceAsStream("openbdlogo.txt");
			if ( in != null)
				OpenBDLogoDataUri = StreamUtils.readToString( in );
		} catch (NullPointerException e1) {
			OpenBDLogoDataUri = null;
		} catch (IOException e1) {
			OpenBDLogoDataUri = null;
		}


		// Initialise the Platform
		thisPlatform.init(config);

		// Get a handle to the underlying FileSystem manager
		vfsManager = thisPlatform.getFileIO().vfsManager();

		// Initialize the Datasource
		dataSourcePoolFactory	= new DataSourcePoolFactory();

		try {
			// Load up the runtimeErrorMessages
			runtimeMessages = ResourceBundle.getBundle("com.naryx.tagfusion.cfm.engine.exceptionMessage");

			log(runtimeMessages.getString("cfEngine.welcomeMessage"));
			log("Product Version: " + PRODUCT_VERSION);
			log("Build date: " + BUILD_ISSUE);

			// TagChecker
			TagChecker = new tagChecker();

			//fileCache initialisation
			cfmlFileCache.init(thisServletContext, getSystemParameters());

			// CFX initalization
			com.naryx.tagfusion.cfx.cfCFX.init(getSystemParameters());

			// CFCs
			ComponentFactory.init(getSystemParameters());

			startTime = System.currentTimeMillis();

		} catch (Exception E) {
			log(PRODUCT_NAME + " Engine Failed to load:" + E.getMessage());
			E.printStackTrace();
			thisInstance = null;
			throw new ServletException("Failed to initialise the cfEngine: " + E.getMessage());
		}

		// Create the plugin Manager and load up any extensions
		pluginManager = new PluginManager(getSystemParameters());

		// Set the Default Character set
		setDefaultCharset();

		// Set the Web Resource path
		setWebResourcePath();

		// Set the native library path; must be done before tags are initialised
		setNativeLibDirectory();


		// Initialise any of the tags
		try {
	    // initialize operators and expressions
			TagChecker.initialiseTags(getSystemParameters());
	    log("expressionEngine loaded: " + expressionEngine.getTotalFunctions() + " functions");
		} catch (Throwable t) {
			t.printStackTrace();
			thisInstance = null;
			log( PRODUCT_NAME + " Engine Failed to initialise tags:" + t.getMessage());
			com.nary.Debug.printStackTrace(t);
			throw new ServletException(PRODUCT_NAME + " Engine Failed to initialise tags: " + t.getMessage());
		}

		// Set the startup engine flags
		String ipList	= getSystemParameters().getString("server.debugoutput.ipaddresses");
		if ( (ipList == null) || (ipList.length() == 0) )
			debugIPsAsInts	=	new int[0];
		else
			debugIPsAsInts	= cfSession.DecodeIPs(ipList);


		setDebugOutputFlag();
		setStrictFlags();
		setAssertionsFlag();
		setCombinedFormUrlFlag();
		setLegacyFormValidation();
		setSuppressWhiteSpace();
		setDefaultBufferSize();
		setDefaultJSONFlags();
		setFormUrlCaseMaintainedFlag();
		setCFOutpuShorthand();

		// Only update the bluedragon.xml file if flagged to do so
		if ( getSystemParameters().getBoolean("server.system.rewritebluedragonxml", true ) && !bluedragonXmlParm.startsWith( "http" ) ) {
			try {
				writeXmlFile(getSystemParameters(), false);
			} catch (cfmRunTimeException e) {
				log( PRODUCT_NAME + "  " + e.getMessage());
			}
		}

		// Auto-configure ODBC datasources (notifyListeners=false, autoConfig=true)
		autoConfigOdbcDataSources(false, true);

		// Setup/clean the dynamic web service cache (and any other resources)
		try {
			cfWebServices.initialize( config );
		} catch (Exception e) {
			throw new ServletException(PRODUCT_NAME + " Engine Failed to initialise Web Services: " + e.getMessage());
		}

		startRequestStats();

		// Setup the Journal Manager
		journalManager	= new JournalManager();

		log(runtimeMessages.getString("cfEngine.serverStarted"));

		// The Engine is ready for requests, so lets call the ServerCFC handling
		new ServerCFC().onServerStart( getSystemParameters() );
	}


	private File getOpenBDXmlFile( String bluedragonXmlParm ) throws IOException {
		if ( bluedragonXmlParm.startsWith( "http://" ) || bluedragonXmlParm.startsWith( "https://" ) ){
			File locationXMLFile = File.createTempFile( "openbd", ".xml" );

			System.out.println( "Loading Remote bluedragon.xml from: " + bluedragonXmlParm );
			String remoteXML = HttpGet.doGet( bluedragonXmlParm ).getBodyAsString();

			FileWriter	fos = null;
			try{
				fos	= new FileWriter( locationXMLFile );
				fos.write( remoteXML );
				fos.flush();
			}finally{
				StreamUtil.closeStream( fos );
			}

			return locationXMLFile;
		}else{
			return getResolvedFile(bluedragonXmlParm);
		}
	}


	public static synchronized void init(ServletConfig config) throws ServletException {
		if (cfEngine.thisInstance == null)
			new cfEngine(config);
	}


	private void setSystemParameters(xmlCFML xmlcfml) {
		systemParameters = xmlcfml;
	}

	public xmlCFML getSystemParameters() {
		return systemParameters;
	}


	public static void destroy() {
		// If the BlueDragon engine has already been destroyed then just return.
		if (cfEngine.thisInstance == null)
			return;

		log( PRODUCT_NAME + " is being shut down ... ");
		bEngineActive	= false;

		cfThreadRunner.stopAllThreads();

		notifyAllListenersShutdown();

		thisInstance.pluginManager.shutdown();

		com.naryx.tagfusion.cfm.engine.variableStore.shutdown();

		cfEngine.thisPlatform.destroy();

		// Close off all the logging around the system
		com.nary.util.LogFile.closeAll();
		log("All logging has been shutdown");

		dataSourcePoolFactory.close();

		CacheFactory.shutdown();

		log( PRODUCT_NAME + " has been successfully shutdown");
		com.nary.Debug.setFilename(null);

		thisInstance = null;
	}

	public static Map<String, cfDataSourceStatus> getDataSourceStatus() {
		return cfEngine.thisInstance.dataSourceStatus;
	}

	public static int[]	getDebugIPs(){
		return cfEngine.thisInstance.debugIPsAsInts;
	}

	public static xmlCFML getConfig() {
		return cfEngine.thisInstance.getSystemParameters();
	}

	// --------------------------------------------------------
	// --] Engine Listener Methods
	// --------------------------------------------------------

	public static final void registerEngineListener(engineListener _new) {
		cfEngine.thisInstance.engineListeners.addElement(_new);
	}

	private static final void notifyAllListenersShutdown() {

		// This method may be called before the static instance variable has been initialized; for example
		// when a shutdown occurs before any requests have been processed. To avoid the NPE, test the
		// state of thisInstance first.
		if (cfEngine.thisInstance != null) {
			Enumeration<engineListener> E = cfEngine.thisInstance.engineListeners.elements();
			while (E.hasMoreElements())
				E.nextElement().engineShutdown();
		}
	}

	public static final void notifyAllListenersAdmin(xmlCFML newConfig) {
		cfEngine.thisInstance.setSystemParameters(newConfig);

		Enumeration<engineListener> E = cfEngine.thisInstance.engineListeners.elements();
		while (E.hasMoreElements())
			E.nextElement().engineAdminUpdate(newConfig);

		thisPlatform.engineAdminUpdate();

		cfEngine.thisInstance.setDebugOutputFlag();
		cfEngine.thisInstance.setSuppressWhiteSpace();
		cfEngine.thisInstance.setLegacyFormValidation();
		cfEngine.thisInstance.setDefaultCharset();
		cfEngine.thisInstance.setDefaultBufferSize();
		cfmlFileCache.flushCache();
	}

	private void setDefaultBufferSize() {
		// Set the response buffer size from the configuration. The default
		// is 0 which means to buffer the entire page.
		try {
			int defaultBufferSize = getSystemParameters().getInt("server.system.buffersize", 0) * 1024;
			if (defaultBufferSize == 0)
				defaultBufferSize = cfHttpServletResponse.UNLIMITED_SIZE;

			cfHttpServletResponse.setUserSize(defaultBufferSize);

		} catch (Exception e) {
			getSystemParameters().setData("server.system.buffersize", DEFAULT_BUFFERSIZE);
			cfHttpServletResponse.setUserSize(cfHttpServletResponse.UNLIMITED_SIZE);
			log("response buffer set to entire page.");
		}
	}

	private void setDefaultJSONFlags(){
		cfEngine.DefaultJSONReturnCase	= getSystemParameters().getString("server.system.jsoncase", "maintain").toLowerCase().trim();
		if ( !"maintain".equals(cfEngine.DefaultJSONReturnCase) &&
				!"lower".equals(cfEngine.DefaultJSONReturnCase) &&
				!"upper".equals(cfEngine.DefaultJSONReturnCase) &&
				!"true".equals(cfEngine.DefaultJSONReturnCase) &&
				!"false".equals(cfEngine.DefaultJSONReturnCase)
				)
			cfEngine.DefaultJSONReturnCase = "maintain";

		cfEngine.DefaultJSONReturnDate	= getSystemParameters().getString("server.system.jsondate", "long").toLowerCase().trim();
		if ( !"long".equals(cfEngine.DefaultJSONReturnDate) &&
				!"http".equals(cfEngine.DefaultJSONReturnDate) &&
				!"json".equals(cfEngine.DefaultJSONReturnDate) &&
				!"mongo".equals(cfEngine.DefaultJSONReturnDate) &&
				!"cfml".equals(cfEngine.DefaultJSONReturnDate)
				)
			cfEngine.DefaultJSONReturnDate = "long";

		log( "cfEngine: JSON Encoding Defaults: server.system.jsoncase=" + cfEngine.DefaultJSONReturnCase + "; server.system.jsondate=" + cfEngine.DefaultJSONReturnDate );
	}

	private void setSuppressWhiteSpace() {
		bSuppressWhitespace = getSystemParameters().getBoolean("server.system.whitespacecomp", Boolean.valueOf(DEFAULT_SUPPRESS_WHITESPACE).booleanValue());
	}


	// get suppress whitespace setting as configured in bluedragon.xml
	public static boolean getSuppressWhiteSpaceDefault() {
		return thisInstance.bSuppressWhitespace;
	}


	/**
	 * Returns back the alternative path for the JAR files
	 *
	 * @return
	 */
	public static String getAltLibPath(){
		String altpath	= thisInstance.getSystemParameters().getString("server.system.libpath");
		if ( altpath != null ){
			File	f	= new File( altpath );
			if ( f.isDirectory() && f.exists() ){
				String t	= f.getAbsolutePath();
				if ( !t.endsWith(File.separator) )
					t	= t + File.separator;

				return t;
			}
		}

		return altpath;
	}

	public static File getXmlFileName() {
		return cfEngine.thisInstance.xmlFileLocation;
	}

	/*
	 * getResolvedFile
	 *
	 * This method uses context.getRealPath() so it should only be called at init
	 * time and not at request time. You can call it at request time if you know
	 * that with BD Server and JX you want the file to be resolved relative to the
	 * built-in web server's wwwroot directory. An example of this is the
	 * CFXClassLoader.loadTagsFolderClass().
	 */
	public static File getResolvedFile(String directory) {
		if ( ( directory == null) || ( directory.length() == 0 ) )
			return null;

		if (directory.length() > 0 && directory.charAt(0) == '/') {
			// --[ Relative path
			String realPath = FileUtils.getRealPath(directory);
			return (realPath == null ? null : new File(realPath));
		} else if (directory.length() > 0 && directory.charAt(0) == '$') {
			// --[ Real path
			return new File(directory.substring(1));
		} else {
			// --[ Real path
			return new File(directory);
		}
	}

	public static final tagChecker getTagChecker() {
		return cfEngine.thisInstance.TagChecker;
	}


	public static final String getNativeLibDirectory() {
		return cfEngine.thisInstance.nativeLibDirectory;
	}

	public static final String getMessage(String id) {
		try {
			return cfEngine.thisInstance.runtimeMessages.getString(id);
		} catch (MissingResourceException e) {
			return "Unrecognized error code: " + id;
		}
	}

	public static final String getMessage(String id, String values[]) {
		try {
			String message = cfEngine.thisInstance.runtimeMessages.getString(id);
			if (values == null || message == null)
				return message;

			for (int x = 0; x < values.length; x++)
				message = com.nary.util.string.replaceString(message, "%" + (x + 1), values[x]);

			return message;
		} catch (MissingResourceException e) {
			return "Unrecognized error code: " + id;
		}
	}


	public final static boolean isFormUrlScopeCombined() {
		return cfEngine.thisInstance.bCombinedFormUrlScope;
	}

	public final static boolean isCFOutputShorthand() {
		return cfEngine.thisInstance.bCFoutputShorthand;
	}

	public final static boolean isFormUrlCaseMaintained() {
		return cfEngine.thisInstance.bMainFormUrlCase;
	}

	public final static boolean isDebugOutputEnabled() {
		return cfEngine.thisInstance.bDebugOutputEnabled;
	}

	public final static boolean isDebuggerOutputEnabled() {
		return cfEngine.thisInstance.bDebugerOutputEnabled;
	}

	public final static boolean isAssertionsEnabled() {
		return cfEngine.thisInstance.bAssertionsEnabled;
	}

	public final static boolean isStrictPassArrayByReference(){
		return cfEngine.thisInstance.bStrictArrayPassByReference;
	}

	public final static boolean isFunctionScopedVariables(){
		return cfEngine.thisInstance.bFunctionScopedVariables;
	}

	/**
	 * The encoding value should be used internally.
	 */
	public static String getDefaultEncoding() {
		return Localization.convertCharSetToCharEncoding(cfEngine.getDefaultCharset());
	}

	public static String getDefaultCharset() {
		return cfEngine.thisInstance.defaultCharset;
	}

	/**
	 * The charset value should be used in the request/response content-type
	 * header.
	 */
	private void setDefaultCharset() {
		defaultCharset = getSystemParameters().getString("server.system.defaultcharset", DEFAULT_CHARSET);
		log("cfEngine: Using default character encoding " + defaultCharset);
	}

	private void setWebResourcePath() {
		webResourcePath = getSystemParameters().getString("server.system.resourcepath", "/WEB-INF/webresources" );
		if ( webResourcePath.endsWith("/") )
			webResourcePath = webResourcePath.substring( 0, webResourcePath.length()-1 );

		webResourcePath = getResolvedFile(webResourcePath).toString();

		log("cfEngine: WebResourcePath " + webResourcePath );
	}

	private void setDebugOutputFlag() {
		bDebugOutputEnabled = getSystemParameters().getBoolean("server.system.debug", Boolean.valueOf(DEFAULT_DEBUG).booleanValue());
		log("cfEngine: [server.system.debug] Show Debug output on error? " + bDebugOutputEnabled);

		bDebugerOutputEnabled = getSystemParameters().getBoolean("server.debugoutput.enabled", Boolean.valueOf(false).booleanValue());
		log("cfEngine: [server.debugoutput.enabled] Show Debugger output? " + bDebugerOutputEnabled);
	}

	private void setAssertionsFlag() {
		bAssertionsEnabled = getSystemParameters().getBoolean("server.system.assert", Boolean.valueOf(DEFAULT_ASSERT).booleanValue());
		log("cfEngine: [server.system.assert] Assertions " + (bAssertionsEnabled ? "enabled" : "disabled"));
	}

	private void setCombinedFormUrlFlag() {
		bCombinedFormUrlScope = getSystemParameters().getBoolean("server.system.formurlcombined", false);
		log("cfEngine: [server.system.formurlcombined] Combined Form/Url Scope? " + bCombinedFormUrlScope);
	}
	private void setCFOutpuShorthand() {
		bCFoutputShorthand = getSystemParameters().getBoolean("server.system.cfoutputshorthand", false);
		log("cfEngine: [server.system.cfoutputshorthand] <%= ... %> ? " + bCFoutputShorthand );
	}

	private void setFormUrlCaseMaintainedFlag() {
		bMainFormUrlCase = getSystemParameters().getBoolean("server.system.formurlmaintaincase", false);
		log("cfEngine: [server.system.formurlmaintaincase] Case Form/Url maintained? " + bMainFormUrlCase);
	}

	private void setStrictFlags() {
		bStrictArrayPassByReference = getSystemParameters().getBoolean("server.system.strictarraypassbyreference", Boolean.valueOf(DEFAULT_STRICT_ARRAYBYREFERENCE).booleanValue());
		log("cfEngine: [server.system.strictarraypassbyreference] Strict Mode: ArrayPassByReference? " + bStrictArrayPassByReference);

		bFunctionScopedVariables = getSystemParameters().getBoolean("server.system.functionscopedvariables", Boolean.valueOf(DEFAULT_STRICT_FUNCTIONSCOPEDVARS).booleanValue());
		log("cfEngine: [server.system.functionscopedvariables] Strict Mode: FunctionScopedVariables? " + bFunctionScopedVariables);
	}

	private void setLegacyFormValidation(){
		bLegacyFormValidation = getSystemParameters().getBoolean("server.system.legacyformvalidation", true);
		log("cfEngine: [server.system.legacyformvalidation] Legacy server side form validation? " + bLegacyFormValidation);
	}


	private void setNativeLibDirectory() {
		nativeLibDirectory = getSystemParameters().getString("server.system.nativelibdir");

		if (nativeLibDirectory == null) {
			log(PRODUCT_NAME + " failed to set NativeLibDirectory. Check the nativelibdir is defined in the configuration file.");
		} else if (!nativeLibDirectory.endsWith("/")) {
			nativeLibDirectory = nativeLibDirectory + "/";
		}

		String osArch = System.getProperty("os.arch");
		if ((osArch != null) && (osArch.indexOf("64") != -1)) {
			nativeLibDirectory = nativeLibDirectory + "x64/";
		}
		log("cfEngine: server.system.nativelibdir=[" + nativeLibDirectory + "]");
	}

	public static void autoConfigOdbcDataSources() {
		autoConfigOdbcDataSources(true, false);
	}

	public static void autoConfigOdbcDataSources(boolean notifyListeners, boolean autoConfig) {
		// currently only supported on Windows
		if (!WINDOWS)
			return;

		try {
			xmlCFML config = getConfig();

			// If this is an auto-config instead of an odbc refresh
			// then check if auto-config is disabled in bluedragon.xml
			if (autoConfig) {
				if (!config.getBoolean("server.cfquery.autoconfig-odbc", true)) {
					log("Auto-configure of ODBC datasources is disabled");
					return;
				}
			}

			log("cfEngine: [server.cfquery.autoconfig-odbc] Auto-configure of ODBC datasources is enabled");

			boolean writeXmlFile = false;

			String[] odbcDataSources = ODBCNativeLib.getOdbcDataSources();

			for (int i = 0; i < odbcDataSources.length; i += 2) {
				String dsnName = odbcDataSources[i];
				String description = odbcDataSources[i + 1];
				String dsnKey = "server.cfquery.datasource[" + dsnName.toLowerCase() + "]";

				// test to see if datasource name already configured
				String currentDescr = config.getString(dsnKey + ".description");
				if (currentDescr == null) {
					config.setData(dsnKey + ".name", dsnName);
					config.setData(dsnKey + ".logintimeout", "120");
					config.setData(dsnKey + ".connectiontimeout", "120");
					config.setData(dsnKey + ".username", "");
					config.setData(dsnKey + ".hoststring", ODBCNativeLib.URL_PREFIX + dsnName);
					config.setData(dsnKey + ".databasename", dsnName);
					config.setData(dsnKey + ".drivername", ODBCNativeLib.DRIVER_CLASS);
					config.setData(dsnKey + ".password", "");
					config.setData(dsnKey + ".description", description);
					config.setData(dsnKey + ".maxconnections", "24");
					config.setData(dsnKey + ".sqlselect", "true");
					config.setData(dsnKey + ".sqldelete", "true");
					config.setData(dsnKey + ".sqlinsert", "true");
					config.setData(dsnKey + ".sqlupdate", "true");
					config.setData(dsnKey + ".sqlstoredprocedures", "true");
					config.setData(dsnKey + ".drivertype", "3");
					config.setData(dsnKey + ".servername", "");
					config.setData(dsnKey + ".port", "");
					cfEngine.getDataSourceStatus().put(dsnName.toLowerCase(), new cfDataSourceStatus());

					log("Auto-configured ODBC datasource: " + dsnName);

					writeXmlFile = true;
				}
			}

			if (writeXmlFile)
				cfEngine.writeXmlFile(config, notifyListeners);
		} catch (UnsatisfiedLinkError ignore) {
		} catch (Exception e) {
			log("ODBC auto-configuration error: " + e.getMessage());
			com.nary.Debug.printStackTrace(e);
		}
	}

	public static void writeXmlFile(xmlCFML newXmlCFML) throws cfmRunTimeException {
		writeXmlFile(newXmlCFML, true);
	}

	public static void reloadXmlFile()  throws cfmRunTimeException {
		File xmlFileName = getXmlFileName();
		if ( xmlFileName != null ){

			try {
				thisInstance.setSystemParameters(xmlConfigManagerFactory.createXmlConfigManager( xmlFileName ).getXMLCFML());
			} catch (Exception E) {
				cfCatchData catchData = new cfCatchData();
				catchData.setMessage("failed to load configuration file");
				catchData.setExtendedInfo( E.getMessage() );
				throw new cfmRunTimeException(catchData);
			}
			notifyAllListenersAdmin( thisInstance.getSystemParameters() );

		}else{
			cfCatchData catchData = new cfCatchData();
			catchData.setMessage("failed to find configuration file");
			throw new cfmRunTimeException(catchData);
		}
	}

	public synchronized static void writeXmlFile(xmlCFML newXmlCFML, boolean notifyListeners) throws cfmRunTimeException {
		File xmlFileName = getXmlFileName();

		if ((xmlFileName != null) && (xmlFileName.canWrite())) {
			// delete the oldest (10th) backup; increment the numbers of the remaining backups
			for (int backupNo = 10; backupNo > 0; backupNo--) {
				File backupFile = new File(xmlFileName + ".bak." + backupNo);
				if (backupFile.exists()) {
					if (backupNo == 10) {
						backupFile.delete();
					} else {
						backupFile.renameTo(new File(xmlFileName + ".bak." + (backupNo + 1)));
					}
				}
			}

			// clean up older backup files
			for (int backupNo = 11; true; backupNo++) {
				File backupFile = new File(xmlFileName + ".bak." + backupNo);
				if (backupFile.exists()) {
					backupFile.delete();
				} else {
					break;
				}
			}

			// write back out the xml file to the file, saving the old one
			File xmlFile = xmlFileName;
			File newFile = new File(xmlFileName + ".bak.1");

			// Put in the last updated
			newXmlCFML.setData("server.system.lastupdated", com.nary.util.Date.formatDate(System.currentTimeMillis(), "dd/MMM/yyyy HH:mm.ss", Locale.US));
			newXmlCFML.setData("server.system.lastfile", newFile.toString());

			// Rename the file
			xmlFile.renameTo(newFile);

			try {
				newXmlCFML.writeTo(xmlFile);
			} catch (IOException e) {
				cfCatchData catchData = new cfCatchData();
				catchData.setMessage("failed to write configuration file, " + e.toString());
				throw new cfmRunTimeException(catchData);
			}
		} else {
			// NOTE: this will happen with BD J2EE when it is running in a WAR file,
			// or when the bluedragon.xml
			// file is marked read-only, or when something goes seriously wrong at BD
			// init time.
			cfCatchData catchData = new cfCatchData();
			catchData.setMessage("failed to write configuration file");
			throw new cfmRunTimeException(catchData);
		}

		if (notifyListeners) {
			notifyAllListenersAdmin(newXmlCFML);
		}
	}

	// --------------------------------------------------------
	// --] Main Service method
	// --------------------------------------------------------

	public static void service(HttpServletRequest req, HttpServletResponse res) throws ServletException {
		Thread.currentThread().setName( req.getRequestURI() );

		thisInstance.totalRequests++;

		if (thisInstance.avgTracker != null) {
			thisInstance.avgTracker.begin();
		}

		cfSession _Session 							= new cfSession(req, res, thisServletContext);
		JournalSession	journalSession 	= thisInstance.journalManager.getJournalSession(req);

		cfFile requestFile = null;

		try {

			// If we are instrumenting this request, then we want to use that and disable any other plugin that maybe used
			if ( journalSession != null )
				journalSession.requestStart(_Session);
			else if ( thisInstance.requestListener != null )
				thisInstance.requestListener.requestStart( _Session );


			_Session.setSuppressWhiteSpace(thisInstance.bSuppressWhitespace);
			_Session.checkAndDecodePost(thisInstance.bLegacyFormValidation);


			// Load in the main request file
			try {
				requestFile = _Session.getRequestFile();
			} catch (cfmBadFileException bfe) {
				// attempt to invoke onMissingTemplate handler for file-not-found
				if (bfe.fileNotFound() && _Session.onMissingTemplate()) {
					_Session.pageEnd();
					return;
				}
				throw bfe;
			}

			// Render the Application.cfm or Application.cfc
			_Session.onRequestStart(requestFile);

			// Render the main request file
			_Session.onRequest(requestFile);

			// Render the 'OnRequestEnd.cfm' file
			_Session.onRequestEnd(requestFile.getURI());

			// Send all data, including any that may be in the cache straight to the client
			_Session.pageEnd();

		} catch (cfmAbortException ACF) {
			// Do nothing. Don't print anything out to the Session
			_Session.pageEnd();
		} catch (cfmBadFileException BF) {

			BF.handleException(requestFile, _Session);
			
			// If we are instrumenting this request, then we want to use that and disable any other plugin that maybe used
			if ( journalSession != null )
				journalSession.requestBadFileException( BF, _Session );
			else if ( thisInstance.requestListener != null )
				thisInstance.requestListener.requestBadFileException( BF, _Session );

		} catch (cfmRunTimeException CF) {

			CF.handleException(_Session);
			
			// If a plugin is listening to this request
			if ( journalSession != null )
				journalSession.requestRuntimeException( CF, _Session );
			else if ( thisInstance.requestListener != null )
				thisInstance.requestListener.requestRuntimeException( CF, _Session );

		} catch (Throwable E) {
			new cfmRunTimeException(_Session, E).handleException(_Session);
		} finally {

			// no matter what happens, we must ensure this gets called
			if (thisInstance.avgTracker != null)
				thisInstance.avgTracker.end();


			thisInstance.totalBytesSent += _Session.getBytesSent();

			// If we are instrumenting this request, then we want to use that and disable any other plugin that maybe used
			if ( journalSession != null )
				journalSession.requestEnd(_Session);
			else if  ( thisInstance.requestListener != null )
				thisInstance.requestListener.requestEnd( _Session );

			_Session.close();

			Thread.currentThread().setName( "no-request" );
		}
	}

	/**
	 * serviceCfcMethod - this method is invoked when a url like the following is
	 * received:
	 *
	 * /xxx.cfc?method= <method name>&arg1=value1&arg2=value2
	 *
	 * It invokes the specified method which is defined in the CFC with the
	 * specified arguments.
	 */
	public static void serviceCfcMethod(HttpServletRequest req, HttpServletResponse res) {
		Thread.currentThread().setName( req.getRequestURI() );
		thisInstance.totalRequests++;

		cfSession _Session 							= new cfSession(req, res, thisServletContext);
		JournalSession	journalSession 	= thisInstance.journalManager.getJournalSession(req);

		cfFile svrFile = null;
		boolean	defaultExceptionHandling = true;
		boolean serializeQueryByColumns = false;

		try {
			_Session.setSuppressWhiteSpace(thisInstance.bSuppressWhitespace);

			// If we are instrumenting this request, then we want to use that and disable any other plugin that maybe used
			if ( journalSession != null )
				journalSession.requestStart(_Session);
			else if ( thisInstance.requestListener != null )
				thisInstance.requestListener.requestStart( _Session );

			_Session.checkAndDecodePost( false );

			//  Load in the main request file
			svrFile = _Session.getRequestFile();
			_Session.onRequestStart(svrFile);

			// Extract the component name from the URI (it's the filename minus the extension)
			String compName = svrFile.getURI();
			int pos = compName.lastIndexOf('/');
			if (pos != -1)
				compName = compName.substring(pos + 1);

			pos = compName.lastIndexOf('.');
			if (pos != -1)
				compName = compName.substring(0, pos);

			// Extract the method name and arguments
			String methodName = null;
			String formatMethod = null;
			String jsonpCallback	= null;
			String jsonCase	= null;
			boolean jsonEncodedParams = false;
			cfArgStructData arguments = new cfArgStructData();
			String[]	bdParams	= null;
			Object[] paramNames;

			cfFormData formData = (cfFormData) _Session.getQualifiedData(variableStore.FORM_SCOPE);
			paramNames = formData.keys();
			for (int i = 0; i < paramNames.length; i++) {
				String name = (String) paramNames[i];
				cfData value = formData.getData(name);

				if (name.equals("METHOD"))
					methodName = value.getString();
				else if ( name.equals("__BDNODEBUG") )
					defaultExceptionHandling = false;
				else if ( name.equals("__BDJSONENCODED") )
					jsonEncodedParams = true;
				else if ( name.equals("__BDJSONCASE") )
					jsonCase = value.getString().toLowerCase();
				else if ( name.equals("__BDRETURNFORMAT"))
					formatMethod = value.getString().toUpperCase();
				else if ( name.equals("__BDPARAMS"))
					bdParams = value.getString().split(",");
				else if ( name.equals("__BDQUERYFORMAT"))
					serializeQueryByColumns = ( value.getString().equalsIgnoreCase("column") );
				else if (!name.equals("FIELDNAMES"))
					arguments.setData(name, value);
			}

			cfUrlData urlData = (cfUrlData) _Session.getQualifiedData(variableStore.URL_SCOPE);
			paramNames = urlData.keys();
			for (int i = 0; i < paramNames.length; i++) {
				String name = (String) paramNames[i];
				cfData value = urlData.getData(name);

				if (name.equals("METHOD"))
					methodName = value.getString();
				else if ( name.equals("CALLBACK") )
					jsonpCallback = value.getString();
				else if ( name.equals("__BDNODEBUG") )
					defaultExceptionHandling = false;
				else if ( name.equals("__BDJSONENCODED") )
					jsonEncodedParams = true;
				else if ( name.equals("__BDJSONCASE") )
					jsonCase = value.getString().toLowerCase();
				else if ( name.equals("__BDPARAMS"))
					bdParams = value.getString().split(",");
				else if ( name.equals("__BDRETURNFORMAT") )
					formatMethod = value.getString().toUpperCase();
				else if ( name.equals("__BDQUERYFORMAT") )
					serializeQueryByColumns = ( value.getString().equalsIgnoreCase("column") );
				else
					arguments.setData(name, value);
			}

			// Make sure a method name was specified
			if (methodName == null) {
				cfCatchData catchData = new cfCatchData();
				catchData.setMessage("the request must contain a METHOD parameter");
				throw new cfmRunTimeException(catchData);
			}

			// Make sure this is a valid JSONP call
			if ( formatMethod != null && formatMethod.equals("JSONP") && jsonpCallback == null ){
				cfCatchData catchData = new cfCatchData();
				catchData.setMessage("the request must contain a CALLBACK parameter if using JSONP");
				throw new cfmRunTimeException(catchData);
			}


			// The params are JSON encoded, so we need to unwrap them to their rich counter parts
			if ( jsonEncodedParams )
				arguments = deserializejson.transformStructElements(arguments, bdParams);

			// Invoke the method
			_Session.onCFCRequest( svrFile.getURI(), compName, methodName, arguments, formatMethod, serializeQueryByColumns, jsonpCallback, jsonCase );

			// Render the 'OnRequestEnd.cfm' file
			_Session.onRequestEnd(svrFile.getURI());
			_Session.pageEnd();

		} catch (cfmAbortException ACF) {
			// Do nothing. Don't print anything out to the Session
			_Session.pageEnd();
		} catch (cfmBadFileException BF) {

			try{ _Session.setStatus( 404, "Not Found" ); }catch(Exception ignore){}
			if ( defaultExceptionHandling )
				BF.handleException(svrFile, _Session);
			
			// If we are instrumenting this request, then we want to use that and disable any other plugin that maybe used
			if ( journalSession != null )
				journalSession.requestBadFileException(BF, _Session);
			else if( thisInstance.requestListener != null )
				thisInstance.requestListener.requestBadFileException( BF, _Session );


		} catch (cfmRunTimeException CF) {

			try{ _Session.setStatus( 500, CF.getMessageThenDetail() ); }catch(Exception ignore){}

			if ( defaultExceptionHandling )
				CF.handleException(_Session);

			
			// If we are instrumenting this request, then we want to use that and disable any other plugin that maybe used
			if ( journalSession != null )
				journalSession.requestRuntimeException(CF, _Session);
			else if ( thisInstance.requestListener != null )
				thisInstance.requestListener.requestRuntimeException( CF, _Session );

		} catch (Throwable E) {
			com.nary.Debug.printStackTrace( E );
			try{ _Session.setStatus( 500, E.getMessage() ); }catch(Exception ignore){}
			if ( defaultExceptionHandling )
				new cfmRunTimeException(_Session, E).handleException(_Session);
		} finally {

			// If we are instrumenting this request, then we want to use that and disable any other plugin that maybe used
			if ( journalSession != null )
				journalSession.requestEnd(_Session);
			else if ( thisInstance.requestListener != null )
				thisInstance.requestListener.requestEnd( _Session );

			// no matter what happens, we must ensure this gets called
			_Session.close();
			thisInstance.totalBytesSent += _Session.getBytesSent();

			Thread.currentThread().setName( "no-request" );
		}
	}

	/*
	 * Starts the tracking of request stats
	 */
	public void startRequestStats() {
		if ( avgTracker == null )
			avgTracker = new AverageTracker("cfEngine");
	}

	/*
	 * Stops the tracking of request stats
	 */
	public void stopRequestStats() {
		avgTracker = null;
	}

	public AverageTracker getRequestStats() {
		return avgTracker;
	}

	public void registerRequestListener( RequestListener _requestListener){
		requestListener = _requestListener;
	}

	public RequestListener getRequestListener(){
		return requestListener;
	}

	public void removeRequestListener(){
		requestListener = null;
	}

	@Override
	public long getTotalRequests(){
		return totalRequests;
	}

	@Override
	public long getCurrentRequests(){
		return ( avgTracker != null ) ? avgTracker.getActiveCount() : -1;
	}

	@Override
	public long getAverageRequestTime(){
		return ( avgTracker != null ) ? avgTracker.getAverage() : -1;
	}

	@Override
	public long getMaxRequestTime(){
		return ( avgTracker != null ) ? avgTracker.getMax() : -1;
	}

	@Override
	public long getMinRequestTime(){
		return ( avgTracker != null ) ? avgTracker.getMin() : -1;
	}

	@Override
	public long getFreeMemory(){
		return Runtime.getRuntime().freeMemory();
	}

	@Override
	public long getTotalMemory(){
		return Runtime.getRuntime().totalMemory();
	}

	@Override
	public long getTotalBytesSent(){
		return totalBytesSent;
	}

	@Override
	public String getStartTime(){
		return new cfDateData( startTime ).getString();
	}

	@Override
	public int getFunctionCount(){
		return expressionEngine.getTotalFunctions();
	}

	public static String getWebResourcePath(){
		return thisInstance.webResourcePath;
	}

	@Override
	public int getTagCount(){
		return TagChecker.getTotalTags();
	}

	@Override
	public String getProductVersion(){
		return PRODUCT_VERSION;
	}

	@Override
	public String getBuildVersion(){
		return BUILD_ISSUE;
	}


	/**
	 * Single point for logging
	 *
	 * @param l
	 */
	public static void log( String l ){
		thisPlatform.log(l);
	}
}
