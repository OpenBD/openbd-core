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
 *  $Id: SessionStorageInterface.java 2131 2012-06-27 19:02:26Z alan $
 */
package com.naryx.tagfusion.cfm.application.sessionstorage;

import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.application.sessionUtility;
import com.naryx.tagfusion.cfm.engine.cfSession;

public interface SessionStorageInterface {
	
	public void onExpireAll( cfApplicationData applicationData );
	public void clearAll();
	
	/**
	 * Get the session associated with this one
	 * 
	 * @param token
	 * @return
	 */
	public boolean onRequestStart(cfSession Session, long sessionTimeOut, sessionUtility sessionInfo );	
	

	/**
	 * Called when the page has ended; opportunity for the engine to 
	 * page this session to an external storage if need be
	 * 
	 * @param token
	 * @param session
	 */
	public void onRequestEnd( cfSession session );
	
	public void onApplicationEnd( cfApplicationData applicationData );
	
	public int size();
	
	public SessionStorageFactory.SessionEngine	getType();
	
	public void shutdown();
}
