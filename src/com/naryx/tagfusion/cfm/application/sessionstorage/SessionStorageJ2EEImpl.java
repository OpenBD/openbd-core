/* 
 *  Copyright (C) 2012 TagServlet Ltd
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
 *  
 *  $Id: SessionStorageJ2EEImpl.java 2534 2015-03-20 20:47:55Z alan $
 */
package com.naryx.tagfusion.cfm.application.sessionstorage;

import javax.servlet.http.HttpSession;

import com.naryx.tagfusion.cfm.application.cfSessionData;
import com.naryx.tagfusion.cfm.application.sessionUtility;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.variableStore;

public class SessionStorageJ2EEImpl extends SessionStorageBase implements SessionStorageInterface {

	public SessionStorageJ2EEImpl(String appName) {
		super(appName);
	}

	public SessionStorageFactory.SessionEngine	getType(){
		return SessionStorageFactory.SessionEngine.J2EE;
	}

	public boolean onRequestStart(cfSession Session, long sessionTimeOut, sessionUtility sessionInfo) {
		boolean sessionStart = false;
		
		// This will look for the HttpSession object and then get the necessary cfJ2EESessionData into action
		HttpSession	httpSess = Session.REQ.getSession( true );
		if ( httpSess == null )
			return false;
		
		cfSessionData sessionData = (cfSessionData)httpSess.getAttribute( appName );
		if ( sessionData == null || sessionTimeOut == 0 ){
			// Create a new instance, if none was found, or the timeout was 0 (which means delete it)
			sessionData	= new cfSessionData( appName );
			sessionStart = true;
			httpSess.setAttribute( appName, sessionData );
		} 

		// If sessionTimeout is -1 then we want to default to the session timeout value configured
		// in the J2EE web app's web.xml file.
		// If sessionTimeout is 0 then we don't want to set the session timeout.
		if ( sessionTimeOut > 0 ) {
			httpSess.setMaxInactiveInterval( (int)(sessionTimeOut/1000) );
		}
			
		sessionData.setSessionID( httpSess.getId() );
		Session.setQualifiedData( variableStore.SESSION_SCOPE, sessionData );

		return sessionStart;
	}

}