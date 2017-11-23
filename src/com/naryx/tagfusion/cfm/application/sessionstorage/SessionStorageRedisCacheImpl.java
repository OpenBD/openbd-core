/* 
 *  Copyright (C) 2017 AW 2.0 Ltd
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
 *  $Id: $
 */
package com.naryx.tagfusion.cfm.application.sessionstorage;

import java.util.concurrent.TimeUnit;

import com.naryx.tagfusion.cfm.application.cfSessionData;
import com.naryx.tagfusion.cfm.application.sessionUtility;
import com.naryx.tagfusion.cfm.application.sessionstorage.SessionStorageFactory.SessionEngine;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.util.Transcoder;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.DirContextDnsResolver;


public class SessionStorageRedisCacheImpl extends SessionStorageBase implements SessionStorageInterface {

	private RedisClient rediscache = null;
	private StatefulRedisConnection<String, String> connection = null;
	private RedisAsyncCommands<String, String> asyncCommands = null;
	private final String uri;


	public SessionStorageRedisCacheImpl( String appName, String _connectionUri ) throws Exception {
		super( "session:" + appName + ":" );

		this.uri = _connectionUri;

		DefaultClientResources clientResources = DefaultClientResources.builder() //
				.dnsResolver( new DirContextDnsResolver() ) // Does not cache DNS lookups (needed for ElasticCache)
				.build();

		rediscache = RedisClient.create( clientResources, _connectionUri );

		connection = rediscache.connect();
		asyncCommands = connection.async();

		cfEngine.log( "SessionStorageRedisCache: Created " + _connectionUri );
	}


	@Override
	public boolean onRequestStart( cfSession Session, long sessionTimeOut, sessionUtility sessionInfo ) {
		boolean sessionStart = false;
		cfSessionData sessData = null;

		// If the timeout is greater than 0, then lookup it from Redis
		if ( sessionTimeOut > 0 ) {
			RedisFuture<String> future = null;

			try {

				future = asyncCommands.get( appName + sessionInfo.getTokenShort() );

				String obj = future.get( 3, TimeUnit.SECONDS );

				sessData = (cfSessionData) Transcoder.fromString( obj );

			} catch ( Exception e ) {
				cfEngine.log( "SessionStorageRedisCache get Failed: " + e.getMessage() );
				future.cancel( false );
			}

		}

		if ( sessData == null ) {
			sessData = new cfSessionData( appName );
			sessionStart = true;
		}

		sessData.setSessionID( appName, sessionInfo.CFID, sessionInfo.CFTOKEN );
		sessData.setTimeOut( sessionTimeOut );
		Session.setQualifiedData( variableStore.SESSION_SCOPE, sessData );


		return sessionStart;
	}


	public void onRequestEnd( cfSession Session ) {
		cfSessionData sessData = getSessionData( Session );
		if ( sessData == null )
			return;

		RedisFuture<String> status = null;

		try {

			String sessDataStr = Transcoder.toString( sessData );

			status = asyncCommands.setex( appName + sessData.getStorageID(), sessData.getTimeOut() / 1000, sessDataStr );
			status.get( 3, TimeUnit.SECONDS );

		} catch ( Exception e ) {
			cfEngine.log( "SessionStorageRedisCache set Failed: " + e.getMessage() );
			status.cancel( false );
		}
	}


	/**
	 * We never really know how many items we have that are not expired in a Redis cache
	 * server, so we'll always return 1 to stop other parts from closing this down
	 */
	public int size() {
		return 1;
	}


	@Override
	public SessionEngine getType() {
		return SessionStorageFactory.SessionEngine.REDIS;
	}


	public void shutdown() {
		if ( asyncCommands != null ) {
			asyncCommands = null;
		}
		if ( connection != null ) {
			connection.close();
			connection = null;
		}
		if ( rediscache != null ) {
			rediscache.shutdown();
			rediscache = null;
		}

	}


	public String getURI() {
		return uri;
	}
}