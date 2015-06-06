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
 *  $Id: cfAPPLICATION.java 2418 2013-11-19 09:46:13Z andy $
 */

package com.naryx.tagfusion.cfm.application;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

/**
 * This tag is the main entry point for the session management. It this tag that will 
 * give the CFML developer access to application,session and client scopes The application 
 * management, utilises the J2EE Session feature
 */
public class cfAPPLICATION extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

	public static final String UNNAMED_APPNAME 				= "";
	public static final String DEFAULT_LOGIN_STORAGE 	= "COOKIE";
	public static final String ALT_LOGIN_STORAGE_1 		= "SESSION";
	public static final String SESSIONMANAGEMENT 			= "SESSIONMANAGEMENT";
	public static final String SESSIONTIMEOUT 				= "SESSIONTIMEOUT";
	public static final String APPLICATIONTIMEOUT 		= "APPLICATIONTIMEOUT";
	
	public static final String SETCLIENTCOOKIES = "SETCLIENTCOOKIES";
	public static final String SETDOMAINCOOKIES = "SETDOMAINCOOKIES";
	public static final String CLIENTMANAGEMENT = "CLIENTMANAGEMENT";
	public static final String CLIENTSTORAGE 		= "CLIENTSTORAGE";
	public static final String LOGINSTORAGE 		= "LOGINSTORAGE";
	public static final String SCRIPTPROTECT 		= "SCRIPTPROTECT";
	public static final String MAPPINGS 				= "MAPPINGS";
	public static final String SECUREJSON 			= "SECUREJSON";
	public static final String SECUREJSONPREFIX = "SECUREJSONPREFIX";
	public static final String CUSTOMTAGPATHS		= "CUSTOMTAGPATHS";
	public static final String DATASOURCE				= "DATASOURCE";
	public static final String SESSIONSTORAGE		= "SESSIONSTORAGE";

	private static cfApplicationManager appManager;

	public static void init(xmlCFML config) {
		appManager = cfApplicationManager.init(config);
	}

	public static cfApplicationManager getAppManager() {
		return appManager;
	}

	public static int getApplicationCount() {
		return appManager.getApplicationCount();
	}

	public static int getSessionCount() {
		return appManager.getSessionCount();
	}

	// -------------------------------------------------------

	protected synchronized void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute(SESSIONMANAGEMENT, "NO");
		defaultAttribute(SETCLIENTCOOKIES, 	"YES");
		defaultAttribute(SETDOMAINCOOKIES, 	"NO");
		defaultAttribute(CLIENTMANAGEMENT, 	"NO");
		defaultAttribute("NAME", 						UNNAMED_APPNAME);
		defaultAttribute(LOGINSTORAGE, 			DEFAULT_LOGIN_STORAGE);

		defaultAttribute(SECUREJSON, 				"NO");
		defaultAttribute(SECUREJSONPREFIX, 	"//");

		parseTagHeader(_tag);
	}

	protected void tagLoadingComplete() throws cfmBadFileException {
		if (this.isSubordinate("CFTHREAD")) {
			throw newBadFileException("Illegal Nesting", "CFAPPLICATION may not be nested within CFTHREAD");
		}
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		if (!containsAttribute(SESSIONTIMEOUT))
			defaultAttribute(SESSIONTIMEOUT, appManager.getDefaultSessionTimeOut());
		
		if (!containsAttribute(APPLICATIONTIMEOUT))
			defaultAttribute(APPLICATIONTIMEOUT, appManager.getDefaultApplicationTimeOut());
		
		if (!containsAttribute(CLIENTSTORAGE))
			defaultAttribute(CLIENTSTORAGE, appManager.getDefaultClientStorage());

		appManager.loadApplication(this, _Session);
		return cfTagReturnType.NORMAL;
	}

	public static void closeClient(cfSession _Session) throws cfmRunTimeException {
		cfApplicationData application = _Session.getApplicationData();
		if ( application != null )
			application.onRequestEnd(_Session);
	}
	
  public java.util.Map<String,String> getInfo(){
  	return createInfo(
  			"engine", 
  			"Defines a CFML application.  Usually defined in the Application.cfm file in a given directory or parent directory");
  }

  @SuppressWarnings("rawtypes")
	public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
    	createAttInfo("NAME", 					"The name of the CFML application", "", false ),
    	
    	createAttInfo("SESSIONMANAGEMENT", 	"Defines whether or not session management is enabled.  The session scope will be available if enabled", "", false ),
    	createAttInfo("SETCLIENTCOOKIES", 	"Set the client cookies (CFID/CFTOKEN) for session management", "", true ),
    	createAttInfo("SETDOMAINCOOKIES", 	"Should the cookies be limited to the top-level domain or the specific host", "", false ),
  		createAttInfo("CLIENTMANAGEMENT",		"Enables the client management.  If enabled the client scope is available", "", false ),
  		createAttInfo("LOGINSTORAGE",				"Controls what the type of client storage is", "", false ),
  		
  		createAttInfo("DATASOURCE",					"The dafault datasource for CFQUERY/CFSTOREDPROC/QueryRun() methods", "", false ),
  		createAttInfo("SCRIPTPROTECT",			"Whether or not the engine will try and protect FORM submissions from script injection; none, all, cgi, cookie, form, url", "", false ),
  		createAttInfo("APPLICATIONTIMEOUT",	"The timeout of this application when it will be unloaded from memory.  Defaults to 1 hour", "", false ),
  		createAttInfo("SESSIONTIMEOUT",			"The timeout of this session when it will be unloaded from memory.  Defaults to 20 minutes", "", false ),
  		createAttInfo("SECUREJSON",					"Controls if the JSON RPC methods are made secure", "", false ),
  		createAttInfo("SECUREJSONPREFIX",		"The prefix if JSON RPC methods are made secure that is used", "", false ),

  	};
  }
  
}
