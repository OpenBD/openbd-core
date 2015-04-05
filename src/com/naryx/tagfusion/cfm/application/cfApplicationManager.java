/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  $Id: cfApplicationManager.java 2331 2013-02-25 20:07:43Z alan $
 */

package com.naryx.tagfusion.cfm.application;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.aw20.util.SystemClockEvent;

import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfcMethodData;
import com.naryx.tagfusion.cfm.engine.cfmAbortException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.engineListener;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.util.dummyServletRequest;
import com.naryx.tagfusion.util.dummyServletResponse;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class cfApplicationManager extends Object implements java.io.Serializable, engineListener, SystemClockEvent {
	static final long serialVersionUID = 1;

	public static final String DEFAULT_CLIENT_STORAGE = "COOKIE";
	public static final String DEFAULT_CLIENT_PURGE = "true";
	public static final String DEFAULT_CLIENT_EXPIRY = "90";
	public static final String DEFAULT_SESSION_TIMEOUT = "#CreateTimeSpan(0,0,20,0)#"; // "0.01388";
	public static final String DEFAULT_APPLICATION_TIMEOUT = "#CreateTimeSpan(0,1,0,0)#"; // "1";
	public static final String DEFAULT_J2EE_SESSION = "false";
	public static final String DEFAULT_CF5CLIENTDATA = "false";
	public static final String DEFAULT_GLOBAL_CLIENT_UPDATES_DISABLED = "false";
	public static final String DEFAULT_CFTOKEN_UUID = "true";
	private static final String ON_APPLICATION_END = "onApplicationEnd";

	public static boolean cf5ClientData;
	public static boolean clientGlobalUpdateDisabled;
	public static boolean cftokenUUID;
	
	private String sessionTimeOut, applicationTimeOut, defaultClientStorage;
	private cfData sessionTimeOutData, applicationTimeOutData;
	private boolean bJ2EESessionManagement, bScriptProtect;
	
	// --------------------------------------------------

	public static cfApplicationManager init(xmlCFML config) {
		cfApplicationManager appManager = new cfApplicationManager();
		appManager.engineAdminUpdate(config);
		cfEngine.registerEngineListener(appManager);
		cfEngine.thisPlatform.timerSetListenerMinute(appManager, 5);

		cf5ClientData = config.getBoolean("server.cfapplication.cf5clientdata", false);
		if (cf5ClientData) {
			cfEngine.log("ColdFusion-compatible CLIENT data enabled");
			cftokenUUID = config.getBoolean("server.cfapplication.cftokenuuid", false); // default to false
		} else {
			cftokenUUID = config.getBoolean("server.cfapplication.cftokenuuid", true); // default to true
		}
		
		// Initialise this watchdog that will look for the deletion of the client data
		new cfClientDataManager();
		
		return appManager;
	}

	public void engineShutdown() {
		onApplicationEnd(false); // false == don't check if expired
		cfEngine.log("cfApplicationManager has been shutdown");
	}

	
	public int getApplicationCount() {
		// Run through the Servlet Context counting the applications
		int totalApps = 0;
		Enumeration<String> E = cfEngine.thisServletContext.getAttributeNames();
		while (E.hasMoreElements()) {
			String key = E.nextElement();
			if ((cfEngine.thisServletContext.getAttribute(key)) instanceof cfApplicationData) {
				totalApps++;
			}
		}

		return totalApps;
	}

	public int getSessionCount(String name){
		Object o = cfEngine.thisServletContext.getAttribute(name);
		if ( o != null && o instanceof cfApplicationData ){
			return ((cfApplicationData)o).getTotalSessions();
		}
		return -1;
	}
	
	public int getSessionCount() {
		int totalSessions = 0;
		Enumeration<String> E = cfEngine.thisServletContext.getAttributeNames();
		while (E.hasMoreElements()) {
			String key = E.nextElement();
			if ((cfEngine.thisServletContext.getAttribute(key)) instanceof cfApplicationData) {
				totalSessions += ((cfApplicationData) cfEngine.thisServletContext.getAttribute(key)).getTotalSessions();
			}
		}

		return totalSessions;
	}

	// --------------------------------------------------
	// Methods to support the default values of the Application and Session values
	// --------------------------------------------------

	public void engineAdminUpdate(xmlCFML config) {

		// Defaults to store the client data using COOKIES
		defaultClientStorage = config.getString("server.cfapplication.clientstorage", DEFAULT_CLIENT_STORAGE);
		if (defaultClientStorage.equalsIgnoreCase("REGISTRY")) {
			cfEngine.log("ERROR:  Registry can no longer be used to store client data.  Cookies will be used instead.");
			
			defaultClientStorage = DEFAULT_CLIENT_STORAGE;
			config.setData("server.cfapplication.clientstorage", DEFAULT_CLIENT_STORAGE);
			try {
				cfEngine.writeXmlFile(config, false);
			} catch (cfmRunTimeException rte) {
				cfEngine.log("ERROR:  failed to change registry to cookie in bluedragon.xml.");
			}
		}

		clientGlobalUpdateDisabled = config.getBoolean("server.cfapplication.clientglobalupdatesdisabled", false);
		if (clientGlobalUpdateDisabled) {
			cfEngine.log("Global client variable updates disabled");
		}

		// Defaults to not use the J2EE Session management
		bJ2EESessionManagement = config.getBoolean("server.cfapplication.j2eesession", Boolean.valueOf(DEFAULT_J2EE_SESSION).booleanValue());

		if (bJ2EESessionManagement) {
			/*
			 * Set to -1 so it will default to the session timeout value
			 * configured in the J2EE web app's web.xml file 
			 */
			sessionTimeOut = "-1";
		} else {
			// Defaults to 20 minutes, expressed as a fraction of 1 day
			sessionTimeOut = config.getString("server.cfapplication.sessiontimeout", DEFAULT_SESSION_TIMEOUT);
		}
		sessionTimeOutData = null;

		// Defaults to 2 days
		applicationTimeOut 			= config.getString("server.cfapplication.applicationtimeout", DEFAULT_APPLICATION_TIMEOUT);
		applicationTimeOutData 	= null;

		cfEngine.log("cfApplicationManager.DefaultClientStorage=[" + defaultClientStorage + "]; J2EE Sessions=" + bJ2EESessionManagement );

		bScriptProtect = config.getBoolean("server.system.scriptprotect", false);
	}

	public String getDefaultSessionTimeOut() {
		return sessionTimeOut;
	}

	public cfData getDefaultSessionTimeOutData(cfSession session) throws cfmRunTimeException {
		if (sessionTimeOutData == null)
			sessionTimeOutData = cfTag.getDynamicAttribute(session, sessionTimeOut);

		return sessionTimeOutData;
	}

	public String getDefaultApplicationTimeOut() {
		return applicationTimeOut;
	}

	public cfData getDefaultApplicationTimeOutData(cfSession session) throws cfmRunTimeException {
		if (applicationTimeOutData == null)
			applicationTimeOutData = cfTag.getDynamicAttribute(session, applicationTimeOut);

		return applicationTimeOutData;
	}

	public String getDefaultClientStorage() {
		return defaultClientStorage;
	}

	// --------------------------------------------------

	public synchronized void clockEvent(int type) {
		onApplicationEnd(true); // true == only if expired
	}

	private void onApplicationEnd(boolean checkExpired) {
		List<String> attrNames = getAttributeNames();
		for (int i = 0; i < attrNames.size(); i++) {
			String key = attrNames.get(i);
			Object attr = cfEngine.thisServletContext.getAttribute(key);
			if (attr instanceof cfApplicationData) {
				cfApplicationData appData = (cfApplicationData) attr;
				
				// the expire() method only returns true if there are no active sessions
				if (checkExpired && !appData.expire()) {
					continue;
				}

				// invoke onSessionEnd() for all sessions within the application
				appData.onApplicationEnd();

				cfEngine.thisServletContext.removeAttribute(key);
				onApplicationEnd(appData);
				cfEngine.log("cfApplicationManager.onApplicationEnd: " + key);
			}
		}
		
		if (!checkExpired) { // J2EE applications never expire
			onApplicationEnd(new cfJ2EEApplicationData(cfEngine.thisServletContext));
		}
	}

	private static List<String> getAttributeNames() {
		/*
		 * With WebLogic Server, removing an attribute while enumerating over the
		 * attribute names will result in a ConcurrentModificationException so we need to copy
		 * the attribute names to a Vector and loop over it instead.
		 */
		Enumeration<String> E = cfEngine.thisServletContext.getAttributeNames();
		List<String> attrNames = new ArrayList<String>();
		while (E.hasMoreElements())
			attrNames.add(E.nextElement());
		
		return attrNames;
	}

	// --------------------------------------------------

	public void loadApplication(cfComponentData applicationCfc, cfSession session) throws cfmRunTimeException {
		cfData appName = applicationCfc.getData("NAME");
		cfApplicationData appData = getAppData(session, appName == null ? cfAPPLICATION.UNNAMED_APPNAME : appName.toString());

		String scriptProtect = null;
		if (applicationCfc.containsKey(cfAPPLICATION.SCRIPTPROTECT)) {
			scriptProtect = applicationCfc.getData(cfAPPLICATION.SCRIPTPROTECT).getString();
		} else if (bScriptProtect) { // use global default
			scriptProtect = "all";
		}

		// Set up the specific application mappings
		if (applicationCfc.containsKey(cfAPPLICATION.MAPPINGS)) {
			cfData mappingsData = (cfData) applicationCfc.getData(cfAPPLICATION.MAPPINGS);
			if (mappingsData.getDataType() == cfData.CFSTRUCTDATA) {
				session.setDataBin(cfAPPLICATION.MAPPINGS, mappingsData);
			}
		}
		
		// Set up the specific application customtagpaths
		if (applicationCfc.containsKey(cfAPPLICATION.CUSTOMTAGPATHS)) {
			cfData mappingsData = (cfData) applicationCfc.getData(cfAPPLICATION.CUSTOMTAGPATHS);
			if (mappingsData.getDataType() == cfData.CFSTRINGDATA) {
				session.setDataBin(cfAPPLICATION.CUSTOMTAGPATHS, mappingsData.getString() );
			}
		}

		String datasource	= null;
		if ( applicationCfc.containsKey(cfAPPLICATION.DATASOURCE))
			datasource	= applicationCfc.getData(cfAPPLICATION.DATASOURCE).getString();
		
		String sessionstorage	= null;
		if ( applicationCfc.containsKey(cfAPPLICATION.SESSIONSTORAGE))
			sessionstorage	= applicationCfc.getData(cfAPPLICATION.SESSIONSTORAGE).getString();
		
		appData.onRequestStart(session, bJ2EESessionManagement, 
				(long) (applicationCfc.getData(cfAPPLICATION.APPLICATIONTIMEOUT).getDouble() * DateUtils.MILLIS_PER_DAY), 
				(long) (applicationCfc.getData(cfAPPLICATION.SESSIONTIMEOUT).getDouble() * DateUtils.MILLIS_PER_DAY), 
				applicationCfc.getData(cfAPPLICATION.SETCLIENTCOOKIES).getBoolean(), 
				applicationCfc.getData(cfAPPLICATION.SETDOMAINCOOKIES).getBoolean(), 
				applicationCfc.getData(cfAPPLICATION.SESSIONMANAGEMENT).getBoolean(), 
				applicationCfc.getData(cfAPPLICATION.CLIENTMANAGEMENT).getBoolean(),
				applicationCfc.getData(cfAPPLICATION.CLIENTSTORAGE).getString(), 
				applicationCfc.getData(cfAPPLICATION.LOGINSTORAGE).getString(), 
				applicationCfc.getComponentPath(), 
				scriptProtect,
				applicationCfc.getData(cfAPPLICATION.SECUREJSON).getBoolean(), 
				applicationCfc.getData(cfAPPLICATION.SECUREJSONPREFIX).getString(),
				datasource, sessionstorage, applicationCfc );

	}

	
	public static void onApplicationEnd(cfApplicationData appData) {
		String applicationCfcPath = appData.getApplicationCfcPath();
		if (applicationCfcPath == null)
			return;

		cfSession session = new cfSession(new dummyServletRequest(appData.getWebroot()), new dummyServletResponse(), cfEngine.thisServletContext);
		cfComponentData applicationCfc = null;

		try {
			cfFile applicationFile = session.getRealFile(applicationCfcPath);
			applicationFile.setComponentName("Application");

			applicationCfc = new cfComponentData(session, applicationFile, false); // false = don't allow abstract

			List<cfApplicationData> args = new ArrayList<cfApplicationData>();
			args.add(appData); // ApplicationScope

			cfcMethodData methodData = new cfcMethodData(session, ON_APPLICATION_END, args);
			applicationCfc.invokeApplicationFunction(session, methodData);

		} catch (cfmAbortException ignore) {
			// do nothing, we're finished anyway (catch here so it's not caught as Throwable below)
		} catch (cfmRunTimeException e) {
			try {
				session.invokeOnError(applicationCfc, e, ON_APPLICATION_END);
			} catch (cfmRunTimeException ie) {
				cfEngine.log("RunTimeError in onApplicationEnd: " + applicationCfcPath);
				ie.handleException(session);
			}
		} catch (Throwable t) {
			com.nary.Debug.printStackTrace(t);
			new cfmRunTimeException(session, t).handleException(session);
		} finally {
			// Make sure per request connections are closed (bug #3174)
			session.sessionEnd();
		}
	}

	
	public void loadApplication(cfTag parentTag, cfSession _Session) throws cfmRunTimeException {
		// This method is invoked from the CFAPPLICATION tag and will initialise and setup all the application data storage
		String appName = parentTag.getDynamic(_Session, "NAME").getString();

		// Get the application data instance
		cfApplicationData appData = getAppData(_Session, appName);

		String scriptProtect = null;
		if (parentTag.containsAttribute(cfAPPLICATION.SCRIPTPROTECT)) {
			scriptProtect = parentTag.getDynamic(_Session, cfAPPLICATION.SCRIPTPROTECT).getString();
		} else if (bScriptProtect) { // use global default
			scriptProtect = "all";
		}
		
		String datasource = null;
		if ( parentTag.containsAttribute(cfAPPLICATION.DATASOURCE) )
			datasource = parentTag.getDynamic(_Session, cfAPPLICATION.DATASOURCE).getString();

		String sessionstorage = null;
		if ( parentTag.containsAttribute(cfAPPLICATION.SESSIONSTORAGE) )
			sessionstorage = parentTag.getDynamic(_Session, cfAPPLICATION.SESSIONSTORAGE).getString();

		if ( parentTag.containsAttribute(cfAPPLICATION.CUSTOMTAGPATHS) )
			_Session.setDataBin(cfAPPLICATION.CUSTOMTAGPATHS, parentTag.getDynamic(_Session, cfAPPLICATION.CUSTOMTAGPATHS).getString() );

		// Setup the properties of this application
		appData.onRequestStart(_Session, bJ2EESessionManagement, 
				(long) (parentTag.getDynamic(_Session, cfAPPLICATION.APPLICATIONTIMEOUT).getDouble() * DateUtils.MILLIS_PER_DAY), 
				(long) (parentTag.getDynamic(_Session, cfAPPLICATION.SESSIONTIMEOUT).getDouble() * DateUtils.MILLIS_PER_DAY), 
				parentTag.getDynamic(_Session, cfAPPLICATION.SETCLIENTCOOKIES).getBoolean(), 
				parentTag.getDynamic(_Session, cfAPPLICATION.SETDOMAINCOOKIES).getBoolean(), 
				parentTag.getDynamic(_Session, cfAPPLICATION.SESSIONMANAGEMENT).getBoolean(), 
				parentTag.getDynamic(_Session, cfAPPLICATION.CLIENTMANAGEMENT).getBoolean(), 
				parentTag.getDynamic(_Session, cfAPPLICATION.CLIENTSTORAGE).getString(), 
				parentTag.getDynamic(_Session, cfAPPLICATION.LOGINSTORAGE).getString(), 
				null, 
				scriptProtect, 
				parentTag.getDynamic(_Session, cfAPPLICATION.SECUREJSON).getBoolean(), 
				parentTag.getDynamic(_Session, cfAPPLICATION.SECUREJSONPREFIX).getString(),
				datasource, sessionstorage, null );
	}

	
	public cfApplicationData getAppData(cfSession _Session, String appName) {
		if (bJ2EESessionManagement && appName.equals(cfAPPLICATION.UNNAMED_APPNAME))
			return new cfJ2EEApplicationData(_Session);
		
		cfApplicationData appData = getAppData(appName);
		if (appData == null)
			appData = createAppData(_Session, appName);

		return appData;
	}

	
	//Try to get the application data in its original unwrapped form.
	private cfApplicationData getAppData(String appName) {
		cfApplicationData appData = null;
		
		Object obj = cfEngine.thisServletContext.getAttribute(appName);
		if (obj instanceof cfApplicationData)
			appData = (cfApplicationData) obj;
	
		return appData;
	}

	private synchronized cfApplicationData createAppData(cfSession session, String appName) {
		cfApplicationData appData = getAppData(appName);
		if (appData == null) {
			appData = new cfApplicationData(session, appName);
			appData.setApplicationStart(true);

			// Place the application data in the application scope in its wrapped form
			cfEngine.thisServletContext.setAttribute(appName, appData);
			cfEngine.log("cfApplicationManager.onApplicationStart: " + (appName.equals(cfAPPLICATION.UNNAMED_APPNAME) ? "(unnamed application)" : appName));
		}
		return appData;
	}
}