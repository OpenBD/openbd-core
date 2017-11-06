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
 *  $Id: SessionStorageMemcachedImpl.java 2459 2014-12-16 10:18:40Z alan $
 */
package com.naryx.tagfusion.cfm.application.sessionstorage;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;

import com.naryx.tagfusion.cfm.application.cfSessionData;
import com.naryx.tagfusion.cfm.application.sessionUtility;
import com.naryx.tagfusion.cfm.application.sessionstorage.SessionStorageFactory.SessionEngine;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.variableStore;

public class SessionStorageMemcachedImpl extends SessionStorageBase implements SessionStorageInterface {

	private MemcachedClient memcache	= null;
	private final String uri;
	
	public SessionStorageMemcachedImpl(String appName, String _connectionUri) throws Exception {
		super(appName);
		
		this.uri = _connectionUri;
		
		// Are they using a user/pass
		String connectionUri	= _connectionUri.substring( _connectionUri.indexOf("//")+2 );

		memcache	= new MemcachedClient( new BinaryConnectionFactory(), AddrUtil.getAddresses(connectionUri) );
		
		cfEngine.log( "SessionStorageMemcached: Created " + _connectionUri );
	}

	
	@Override
	public boolean onRequestStart(cfSession Session, long sessionTimeOut, sessionUtility sessionInfo) {
		boolean sessionStart = false;
		cfSessionData sessData = null;
		
		// If the timeout is greater than 0, then lookup it from Mongo
		if ( sessionTimeOut > 0 ){
			Future<Object> future = memcache.asyncGet( appName + sessionInfo.getTokenShort() );
			
			try {
				Object obj = future.get(3, TimeUnit.SECONDS);
				
				if ( obj != null ){
					sessData	= (cfSessionData)obj;
				}
				
			} catch (Exception e) {
				cfEngine.log( "SessionStorageMemcached get Failed: " + e.getMessage() );
				future.cancel(false);
			}
			
		}
		
		if ( sessData == null ){
			sessData = new cfSessionData( appName );
			sessionStart = true;
		}

		sessData.setSessionID( appName, sessionInfo.CFID, sessionInfo.CFTOKEN );
		sessData.setTimeOut( sessionTimeOut );
		Session.setQualifiedData( variableStore.SESSION_SCOPE, sessData );
		
		
		return sessionStart;
	}

	
	public void onRequestEnd(cfSession Session) {
		cfSessionData	sessData	= getSessionData( Session );
		if ( sessData == null )
			return;
		
		Future<Boolean> status = null;
		status = memcache.set( appName + sessData.getStorageID(), (int)(sessData.getTimeOut()/1000), sessData );
		
		try{
			status.get( 3, TimeUnit.SECONDS);
		}catch(Exception e){
			cfEngine.log( "SessionStorageMemcached set Failed: " + e.getMessage() );
			status.cancel(false);
		}
	}
	
	
	
	/**
	 * We never really know how many items we have that are not expired in a memcached
	 * server, so we'll always return 1 to stop other parts from closing this down
	 */
	public int size() {	
		return 1; 
	}
	
	@Override
	public SessionEngine getType() {
		return SessionStorageFactory.SessionEngine.MEMCACHED;
	}

	
	public void shutdown() {
		if ( memcache != null )
			memcache.shutdown();
	}
	
	public String getURI(){
		return uri;
	}
}