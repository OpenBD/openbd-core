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
 *  http://www.openbluedragon.org/
 *  $Id: $
 */

package com.naryx.tagfusion.cfm.application;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfcMethodData;
import com.naryx.tagfusion.cfm.engine.cfmAbortException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.util.dummyServletRequest;
import com.naryx.tagfusion.util.dummyServletResponse;

/**
 * This class handles the [session] scope. It has been classed out, incase we
 * need to do anything special with this data that sets it aside from a normal
 * structure.
 */
public class cfSessionData extends cfStructExpireData implements java.io.Serializable {
	static final long serialVersionUID = 1;
	private static final String BD_ADMIN_APPNAME = "BLUEDRAGONADMIN";
	public static final String ON_SESSION_END = "onSessionEnd";

	private int defaultElements = 3;
	private long timeout;
	
	transient private	String md5Digest = null;
	transient private String storageid = null;

	public cfSessionData(String appName) {
		super();
		if ((appName != null) && (appName.toUpperCase().equals(BD_ADMIN_APPNAME))) {
			isBDAdminStruct = true;
		}
	}

	/**
	 * This constructor is for subclasses that want to override the private
	 * hashdata attribute to use an alternate data store (see the comment labeled
	 * "SUBCLASSES" in cfStructData).
	 */
	protected cfSessionData(Map<String, cfData> hashdata) {
		super(hashdata);
	}

	public String getStorageID(){
		return storageid;
	}
	
	public void setSessionID(String appName, String CFID, String CFTOKEN) {
		setData("cfid", 			new cfStringData(CFID));
		setData("cftoken", 		new cfStringData(CFTOKEN));
		setData("urltoken", 	new cfStringData("CFID=" + CFID + "&CFTOKEN=" + CFTOKEN));
		setData("sessionid", 	new cfStringData(appName.toUpperCase() + "_" + CFID + "_" + CFTOKEN));
		
		storageid	= CFID + ":" + CFTOKEN;	
		
		defaultElements = 4;
	}

	public void setSessionID(String jsessionid) {
		setData("sessionid", new cfStringData(jsessionid));
		defaultElements = 1;
	}

	/*
	 * This method allows us to determine if this is an empty session; one that
	 * has had no values stored in it. This is performed by looking at the number
	 * of elements it is holding. We use the 'defaultElements' variable to aid in
	 * this since a JSESSION has only 2 elements, where as a CFML session has 3
	 * elements.
	 */
	public boolean isEmpty() {
		return (size() == defaultElements);
	}

	/**
	 * Invoke onSessionEnd() for any cfSessionData instance within the
	 * HttpSession.
	 */
	public static void onSessionEnd(HttpSession httpSession) {
		ServletContext servletContext = httpSession.getServletContext();
		boolean containsSessionId = false;
		boolean containsUrlToken = false;

		Enumeration<String> attrNames = httpSession.getAttributeNames();
		while (attrNames.hasMoreElements()) {
			String key = attrNames.nextElement();
			if (key.equals("sessionid")) {
				containsSessionId = true;
			} else if (key.equals("urltoken")) {
				containsUrlToken = true;
			} else {
				Object sessionAttr = httpSession.getAttribute(key);
				if (sessionAttr instanceof cfSessionData) {
					Object appAttr = servletContext.getAttribute(key);
					if (appAttr instanceof cfApplicationData) {
						((cfSessionData) sessionAttr).onSessionEnd((cfApplicationData) appAttr);
					}
				}
			}
		}

		// check for unnamed cfJ2EESessionData
		if (containsSessionId && containsUrlToken) {
			cfSessionData session = new cfJ2EESessionData(httpSession);
			session.onSessionEnd(new cfJ2EEApplicationData(servletContext));
		}
	}

	
	/**
	 * Invoked the onSessionEnd() method of the Application.cfc
	 * 
	 * If this method is not available (not provided) then it returns false
	 * 
	 * @param appData
	 * @return
	 */
	public boolean onSessionEnd(cfApplicationData appData) {
		String applicationCfcPath = appData.getApplicationCfcPath();
		if (applicationCfcPath == null) {
			return false;
		}

		cfSession session = new cfSession(new dummyServletRequest(appData.getWebroot()), new dummyServletResponse(), cfEngine.thisServletContext);
		cfComponentData applicationCfc = null;

		try {
			cfFile applicationFile = session.getRealFile(applicationCfcPath);
			applicationFile.setComponentName("Application");
			applicationCfc = new cfComponentData(session, applicationFile, false); // false =  don't allow abstract
			
			if ( !applicationCfc.isMethodAvailable(ON_SESSION_END) )
				return false;

			// Set the application/session scope for this one
			session.setQualifiedData( variableStore.APPLICATION_SCOPE, appData );
			session.setQualifiedData( variableStore.SESSION_SCOPE, this );
			
			List<cfStructExpireData> args = new ArrayList<cfStructExpireData>();
			args.add(this); // SessionScope
			args.add(appData); // ApplicationScope

			cfcMethodData methodData = new cfcMethodData(session, ON_SESSION_END, args);
			applicationCfc.invokeApplicationFunction(session, methodData);

		} catch (cfmAbortException ignore) {
			// do nothing, we're finished anyway (catch here so it's not caught as Throwable below)
		} catch (cfmRunTimeException e) {
			try {
				session.invokeOnError(applicationCfc, e, ON_SESSION_END);
			} catch (cfmRunTimeException ie) {
				cfEngine.log("onSessionEnd: " + applicationCfcPath + "; " + ie.getMessage() );
				ie.handleException(session);
			}
		} catch (Throwable t) {
			com.nary.Debug.printStackTrace(t);
			new cfmRunTimeException(session, t).handleException(session);
		} finally {
			// Make sure per request connections are closed (bug #3174)
			session.sessionEnd();
		}
		
		return true;
	}

	public void setTimeOut(long sessionTimeOut) {
		timeout	= sessionTimeOut;
	}
	
	public long getTimeOut(){
		return timeout;
	}

	public void setMD5(String digest) {
		md5Digest	= digest;
	}
	
	public String getMD5(){
		return md5Digest;
	}
}
