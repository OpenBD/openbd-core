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
 *  $Id: SessionStorageInternalImpl.java 2459 2014-12-16 10:18:40Z alan $
 */
package com.naryx.tagfusion.cfm.application.sessionstorage;

import java.util.Iterator;
import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.application.cfSessionData;
import com.naryx.tagfusion.cfm.application.sessionUtility;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.variableStore;

public class SessionStorageInternalImpl extends SessionStorageBase implements SessionStorageInterface {

	private Map<String, cfSessionData>	cfSessionStore;
	
	public SessionStorageInternalImpl(String appName){
		super(appName);
		cfSessionStore	= new FastMap<String, cfSessionData>().shared();
	}
	
	public SessionStorageFactory.SessionEngine	getType(){
		return SessionStorageFactory.SessionEngine.INTERNAL;
	}
	
	public void onExpireAll( cfApplicationData applicationData ) {
		boolean bHasOnSessionEndMethod	= true;
		
		long nowTime = System.currentTimeMillis();
		Iterator<cfSessionData> it = cfSessionStore.values().iterator();
		while ( it.hasNext() ) {
			cfSessionData sess = it.next();
			if ( ( nowTime - sess.getLastUsed() ) > sess.getTimeOut() ) {
				
				/**
				 * The first time we call this method it will let us know if the onSessionEnd() method is actually
				 * available by returning true/false.  We don't need to keep calling this function if there is no method
				 * to actually invoke.  But we must call it the first time around to see.
				 */
				if ( bHasOnSessionEndMethod )
					bHasOnSessionEndMethod = sess.onSessionEnd( applicationData );
				
				it.remove();
			}
		}
	}

	
	@Override
	public void onApplicationEnd(cfApplicationData applicationData) {
		Iterator<cfSessionData> it = cfSessionStore.values().iterator();
		while ( it.hasNext() ) {
			cfSessionData sess = it.next();

			if ( !sess.onSessionEnd( applicationData ) )
				break;
		}
		
		cfSessionStore.clear();
	}

	
	@Override
	public void clearAll() {
		cfSessionStore.clear();
	}

	
	@Override
	public boolean onRequestStart(cfSession Session, long sessionTimeOut, sessionUtility sessionInfo ) {
		boolean sessionStart = false;
		
		cfSessionData sessData = cfSessionStore.get( sessionInfo.urlToken() );
		if ( sessData == null || sessionTimeOut == 0 ){
			sessData = new cfSessionData( appName );
			sessData.setTimeOut( sessionTimeOut );
			sessionStart = true;
			cfSessionStore.put( sessionInfo.urlToken(), sessData );
		}
		
		sessData.setSessionID( appName, sessionInfo.CFID, sessionInfo.CFTOKEN );
		sessData.setLastUsed();
		Session.setQualifiedData( variableStore.SESSION_SCOPE, sessData );
		
		return sessionStart;
	}

	@Override
	public int size() {
		return cfSessionStore.size();
	}

	@Override
	public void shutdown() {
		cfSessionStore.clear();
	}
	
}