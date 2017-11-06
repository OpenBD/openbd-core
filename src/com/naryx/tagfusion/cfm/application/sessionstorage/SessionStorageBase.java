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
 *  http://openbd.org/
 *  $Id: SessionStorageBase.java 2459 2014-12-16 10:18:40Z alan $
 */
package com.naryx.tagfusion.cfm.application.sessionstorage;

import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.application.cfSessionData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.variableStore;

/**
 * Abstract class for the SessionStorage Implementations to override and provide the minimal default processing
 */
public abstract class SessionStorageBase {

	public SessionStorageBase(String appName) {
		this.appName = appName;
	}

	protected cfSessionData getSessionData(cfSession session) {
		cfStructData sd = session.getQualifiedData(variableStore.SESSION_SCOPE);
		if (sd instanceof cfSessionData)
			return (cfSessionData) sd;
		else
			return null;
	}

	protected String appName;

	public void onRequestEnd(cfSession session) {
	}

	public void onApplicationEnd(cfApplicationData applicationData) {
	}

	public int size() {
		return 0;
	}

	public void shutdown() {
	}

	public void onExpireAll(cfApplicationData applicationData) {
	}

	public void clearAll() {
	}

	public String getURI(){
		return null;
	}

	
}