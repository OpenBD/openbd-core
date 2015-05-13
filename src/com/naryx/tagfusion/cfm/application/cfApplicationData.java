/* 
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
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
 *  README.txt @ http://openbd.org/license/README.txt
 *  
 *  http://openbd.org/ 
 */

package com.naryx.tagfusion.cfm.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ucar.unidata.util.DateUtil;

import com.naryx.tagfusion.cfm.application.sessionstorage.SessionStorageFactory;
import com.naryx.tagfusion.cfm.application.sessionstorage.SessionStorageInterface;
import com.naryx.tagfusion.cfm.cookie.cfCookieData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfCGIData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfFormData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfUrlData;
import com.naryx.tagfusion.cfm.engine.cfcMethodData;
import com.naryx.tagfusion.cfm.engine.cfmAbortException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;

/**
 * This class manages an individual application. Sets up the necessary SESSION, CLIENT and APPLICATION scopes.
 */
public class cfApplicationData extends cfStructExpireData implements java.io.Serializable {
	private static final long serialVersionUID = 1;

	private SessionStorageInterface SessionStorage = null;

	private String appName, loginStorage, clientStorage, scriptProtect;

	private long applicationTimeOut, sessionTimeOut;

	private boolean bClient, bCookiesWorking = false, bDomainCookies = false;

	private boolean OnApplicationStartNeedToBeCalled;

	private String applicationCfcPath; // path to Application.cfc for this application

	private String webroot;

	private String datasource;

	public cfApplicationData(cfSession Session, String _appName) {
		super();
		appName = _appName;

		if (Session != null)
			this.webroot = com.nary.io.FileUtils.getRealPath(Session.REQ, "/");
	}

	public final String getAppName() {
		return appName;
	}

	public final String getLoginStorage() {
		return loginStorage;
	}

	public final int getTotalSessions() {
		return SessionStorage.size();
	}

	public final boolean isSessionEnabled() {
		return SessionStorage.getType() != SessionStorageFactory.SessionEngine.NONE;
	}

	public final boolean isJ2EESessionEnabled() {
		return SessionStorage.getType() == SessionStorageFactory.SessionEngine.J2EE;
	}

	public final boolean isClientEnabled() {
		return bClient;
	}

	public final boolean isCookiesWorking() {
		return bCookiesWorking;
	}

	public final String getDataSource() {
		return datasource;
	}

	public cfStructData getMetaData(cfSession session) {
		cfStructData s = new cfStructData();

		s.setData(cfAPPLICATION.SESSIONMANAGEMENT, cfBooleanData.getcfBooleanData(SessionStorage.getType() != SessionStorageFactory.SessionEngine.NONE));
		s.setData("J2EESESSIONENABLED", cfBooleanData.getcfBooleanData(SessionStorage.getType() == SessionStorageFactory.SessionEngine.J2EE));
		s.setData(cfAPPLICATION.SETDOMAINCOOKIES, cfBooleanData.getcfBooleanData(bDomainCookies));

		s.setData(cfAPPLICATION.CLIENTMANAGEMENT, cfBooleanData.getcfBooleanData(bClient));
		s.setData(cfAPPLICATION.CLIENTSTORAGE, new cfStringData(clientStorage));
		s.setData(cfAPPLICATION.LOGINSTORAGE, new cfStringData(loginStorage));
		s.setData(cfAPPLICATION.APPLICATIONTIMEOUT, new cfNumberData(applicationTimeOut));
		s.setData(cfAPPLICATION.SESSIONTIMEOUT, new cfNumberData(sessionTimeOut));
		s.setData("NAME", new cfStringData(appName));
		s.setData(cfAPPLICATION.DATASOURCE, new cfStringData(datasource));
		s.setData(cfAPPLICATION.SECUREJSON, getData(cfAPPLICATION.SECUREJSON));
		s.setData(cfAPPLICATION.SECUREJSONPREFIX, getData(cfAPPLICATION.SECUREJSONPREFIX));
		s.setData(cfAPPLICATION.SCRIPTPROTECT, new cfStringData(scriptProtect));

		if (session.getDataBin(cfAPPLICATION.MAPPINGS) != null)
			s.setData(cfAPPLICATION.MAPPINGS, (cfData) session.getDataBin(cfAPPLICATION.MAPPINGS));

		if (session.getDataBin(cfAPPLICATION.CUSTOMTAGPATHS) != null)
			s.setData(cfAPPLICATION.CUSTOMTAGPATHS, new cfStringData((String) session.getDataBin(cfAPPLICATION.CUSTOMTAGPATHS)));

		return s;
	}

	protected void setApplicationCfcPath(String _applicationCfcPath) {
		this.applicationCfcPath = _applicationCfcPath;
	}

	public String getApplicationCfcPath() {
		return this.applicationCfcPath;
	}

	public String getWebroot() {
		return this.webroot;
	}

	public void setApplicationStart(boolean start) {
		OnApplicationStartNeedToBeCalled = start;
	}

	// -----------------------------------------------------

	public void onRequestStart(cfSession Session, boolean bJ2EESessionManagement, long applicationTimeOut, long sessionTimeOut, boolean setClientCookies, boolean setDomainCookies, boolean bSessionManagement, boolean bClientManagement, String clientStorage, String loginStorage, String applicationCfcPath, String scriptProtect, boolean secureJson, String secureJsonPrefix, String datasource,
			String sessionStorage, cfComponentData applicationCfc) throws cfmRunTimeException {

		// Determine the session storage engine
		synchronized (this) {
			SessionStorageInterface ss = SessionStorageFactory.createStorage(appName, SessionStorage, bSessionManagement, bJ2EESessionManagement, sessionStorage);
			if (ss != null)
				SessionStorage = ss;
		}

		bClient = bClientManagement;
		this.loginStorage = loginStorage;
		this.bDomainCookies = setDomainCookies;
		this.datasource = datasource;
		this.clientStorage = clientStorage;
		this.scriptProtect = scriptProtect;

		setData("securejson", cfBooleanData.getcfBooleanData(secureJson));
		setData("securejsonprefix", new cfStringData(secureJsonPrefix));

		setApplicationCfcPath(applicationCfcPath);

		// Handle the application timeout. If 0 (ZERO) then all the elements are cleared down
		this.applicationTimeOut = applicationTimeOut;
		if (applicationTimeOut == 0) {
			clear();
		}

		// Set the app and apply any script protection
		setData("applicationname", new cfStringData(appName));
		this.sessionTimeOut = sessionTimeOut;
		applyScriptProtection(Session, scriptProtect);

		// Set this application data into the session
		Session.setQualifiedData(variableStore.APPLICATION_SCOPE, this);

		// Run the start application
		if (OnApplicationStartNeedToBeCalled && applicationCfc != null)
			onApplicationStart(Session, applicationCfc);

		// Setup the necessary cookie/uri information to determining this client session
		sessionUtility sessionInfo = null;
		if (bClient || SessionStorage.getType() == SessionStorageFactory.SessionEngine.INTERNAL || SessionStorage.getType() == SessionStorageFactory.SessionEngine.MONGO || SessionStorage.getType() == SessionStorageFactory.SessionEngine.MEMCACHED) {
			sessionInfo = new sessionUtility(Session, setDomainCookies);
			bCookiesWorking = (setClientCookies && sessionInfo.IsSessionFromCookie());
		}

		// Setup the client variables
		if (bClient) {
			if (setupClientData(Session, sessionInfo) && applicationCfc != null)
				onClientStart(Session, applicationCfc);
		}

		// Setup the session variables
		if (SessionStorage.onRequestStart(Session, sessionTimeOut, sessionInfo) && applicationCfc != null)
			onSessionStart(Session, applicationCfc);

		// Set the cookies, only if client or session(cf) is enabled
		if (setClientCookies && sessionInfo != null)
			sessionInfo.setCookie(Session);

		// Let the bottom layer know we just used this class
		setLastUsed();
	}

	// -----------------------------------------------------

	private static final String ON_APPLICATION_START = "onApplicationStart";

	private static Map<String, lockObject> lockMap = new HashMap<String, lockObject>();

	private void onApplicationStart(cfSession session, cfComponentData applicationCfc) throws cfmAbortException {

		// Get the locking object; we only want to lock per-application, not global
		lockObject lock;
		synchronized (lockMap) {
			lock = lockMap.get(appName);
			if (lock == null) {
				lock = new lockObject();
				lockMap.put(appName, lock);
			}
			lock.totalWaiting++;
		}

		synchronized (lock) {
			try {

				// Make sure the application hasn't already been loaded due to a previous lock
				if (!OnApplicationStartNeedToBeCalled) {
					return;
				}

				// Invoke the CFC method to load up the application
				cfcMethodData methodData = new cfcMethodData(session, ON_APPLICATION_START);
				applicationCfc.invokeApplicationFunction(session, methodData);
				return;

			} catch (cfmAbortException e) {
				// continue after catch blocks
			} catch (cfmRunTimeException e) {
				try {
					session.invokeOnError(applicationCfc, e, ON_APPLICATION_START);
				} catch (cfmRunTimeException ie) {
					ie.handleException(session);
				}
			} finally {
				OnApplicationStartNeedToBeCalled = false;

				// Remove the lock if this is the only waiting
				lock.totalWaiting--;
				if (lock.totalWaiting <= 0) {
					synchronized (lockMap) {
						lockMap.remove(appName);
					}
				}
			}

			// If this method throws an uncaught exception, CFABORT, or returns false,
			// the application does not start and the request is aborted
			cfEngine.log("onApplicationStart failed: " + applicationCfc.getComponentPath());
			cfEngine.thisServletContext.removeAttribute(appName);
			session.abortPageProcessing();
		}
	}

	// -----------------------------------------------------

	private static final String ON_SESSION_START = "onSessionStart";

	private void onSessionStart(cfSession session, cfComponentData applicationCfc) throws cfmRunTimeException {

		if (applicationCfc.isMethodAvailable(ON_SESSION_START)) {
			try {
				cfcMethodData methodData = new cfcMethodData(session, ON_SESSION_START);
				applicationCfc.invokeApplicationFunction(session, methodData);
			} catch (cfmRunTimeException e) {
				session.invokeOnError(applicationCfc, e, ON_SESSION_START);
				session.abortPageProcessing();
			}
		}

		// Set the session time
		cfData data = session.getQualifiedData(variableStore.SESSION_SCOPE);
		if (data instanceof cfSessionData) {
			cfSessionData sessionData = (cfSessionData) data;
			sessionData.setTimeOut((long) (applicationCfc.getData(cfAPPLICATION.SESSIONTIMEOUT).getDouble() * DateUtil.MILLIS_DAY));
		}
	}

	// -----------------------------------------------------

	private static final String ON_CLIENT_START = "onClientStart";

	private void onClientStart(cfSession session, cfComponentData applicationCfc) throws cfmRunTimeException {
		if (applicationCfc.isMethodAvailable(ON_CLIENT_START)) {
			try {
				cfcMethodData methodData = new cfcMethodData(session, ON_CLIENT_START);
				applicationCfc.invokeApplicationFunction(session, methodData);
			} catch (cfmRunTimeException e) {
				session.invokeOnError(applicationCfc, e, ON_CLIENT_START);
				session.abortPageProcessing();
			}
		}
	}

	// -----------------------------------------------------

	public synchronized boolean expire() {
		expireSessions(); // check for expired sessions

		// note that an application only expires if there are no active sessions, even if the
		// application timeout is exceeded; this seems to make sense, but is different from
		// CFMX, where applications and sessions can timeout independently
		return (SessionStorage.size() == 0) && (System.currentTimeMillis() - getLastUsed()) > applicationTimeOut;
	}

	// -----------------------------------------------------

	private synchronized void expireSessions() {
		SessionStorage.onExpireAll(this);
	}

	public synchronized void onApplicationEnd() {
		SessionStorage.onApplicationEnd(this);
		SessionStorage.shutdown();
		SessionStorage = null;
	}

	private boolean setupClientData(cfSession Session, sessionUtility sessionInfo) throws cfmRunTimeException {
		// Set the URLTOKEN information for this client
		cfData existingData = Session.getQualifiedData(variableStore.CLIENT_SCOPE);

		// added for bug 1528. In the unlikely event that 2 cfapplication tags
		// are used. The client data for the first one must be saved first
		if (existingData != null && existingData instanceof cfClientSessionData) {

			if (!((cfClientSessionData) existingData).appName.equalsIgnoreCase(appName)) {
				((cfClientSessionData) existingData).close(Session);
				cfClientSessionData clientData = new cfClientSessionData(Session, sessionInfo, clientStorage, appName, false);
				Session.setQualifiedData(variableStore.CLIENT_SCOPE, clientData);
				return clientData.isClientStart();
			}// else if the existing data is for the named application then it is not replaced

		} else {
			cfClientSessionData clientData = new cfClientSessionData(Session, sessionInfo, clientStorage, appName, true);
			Session.setQualifiedData(variableStore.CLIENT_SCOPE, clientData);
			return clientData.isClientStart();
		}

		return false;
	}

	/**
	 * Called at the end of every request. This is an opportunity for the client and any session storage engine to page their results out to a back end
	 * 
	 * @param Session
	 * @throws cfmRunTimeException
	 */
	public void onRequestEnd(cfSession Session) throws cfmRunTimeException {
		if (bClient) {
			cfData clientData = Session.getQualifiedData(variableStore.CLIENT_SCOPE);
			if ((clientData != null) && (clientData instanceof cfClientSessionData))
				((cfClientSessionData) clientData).close(Session);
		}

		if (SessionStorage != null)
			SessionStorage.onRequestEnd(Session);
	}

	private void applyScriptProtection(cfSession _Session, String _protect) {
		String scriptProtect = _protect;

		if (scriptProtect != null) {
			scriptProtect = scriptProtect.toLowerCase();
			if (scriptProtect.equals("none")) {
				// do nothing
				return;
			} else if (scriptProtect.equals("all")) {
				applyScriptProtection(_Session, variableStore.CGI_SCOPE);
				applyScriptProtection(_Session, variableStore.FORM_SCOPE);
				applyScriptProtection(_Session, variableStore.URL_SCOPE);
				applyScriptProtection(_Session, variableStore.COOKIE_SCOPE);
			} else {
				List<String> scopeStrs = com.nary.util.string.split(scriptProtect, ",");
				for (int i = 0; i < scopeStrs.size(); i++) {
					String nextScope = ((String) scopeStrs.get(i)).toLowerCase();
					if (nextScope.equals(variableStore.CGI_SCOPE_NAME)) {
						applyScriptProtection(_Session, variableStore.CGI_SCOPE);
					} else if (nextScope.equals(variableStore.FORM_SCOPE_NAME)) {
						applyScriptProtection(_Session, variableStore.FORM_SCOPE);
					} else if (nextScope.equals(variableStore.URL_SCOPE_NAME)) {
						applyScriptProtection(_Session, variableStore.URL_SCOPE);
					} else if (nextScope.equals(variableStore.COOKIE_SCOPE_NAME)) {
						applyScriptProtection(_Session, variableStore.COOKIE_SCOPE);
					}
				}
			}
		}
	}

	private void applyScriptProtection(cfSession _Session, int _scope) {		
		cfData scopeData = _Session.getQualifiedData(_scope);
		
		if ( scopeData != null && _scope == variableStore.CGI_SCOPE )
			((cfCGIData) scopeData).setScriptProtect();
		else if (scopeData != null && scopeData.getDataType() == cfData.CFSTRUCTDATA) {
			cfStructData data = (cfStructData) scopeData;
			Object[] keys = data.keys();
			for (int i = 0; i < keys.length; i++) {
				String nextKey = keys[i].toString();
				cfData valueData = data.getData(nextKey);

				if (valueData.getDataType() == cfData.CFSTRINGDATA) {
					String value = ((cfStringData) valueData).getString();
					int origLen = value.length();
					value = ScriptProtect.sanitize( value );
					
					// only replace the existing cfData if it's changed - note this works because any replaced string will grow the existing string length
					if (value.length() != origLen) {
						if (_scope == variableStore.COOKIE_SCOPE) {
							((cfCookieData) scopeData).overrideData(nextKey, value);
						} else if (_scope == variableStore.FORM_SCOPE) {
							((cfFormData) scopeData).overrideData(nextKey, new cfStringData(value));
						} else if (_scope == variableStore.URL_SCOPE) {
							((cfUrlData) scopeData).overrideData(nextKey, new cfStringData(value));
						}
					}
				}
			}
		}
	}
}
