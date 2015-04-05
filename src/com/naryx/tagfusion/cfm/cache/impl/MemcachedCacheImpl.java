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
 *  $Id: MemcachedCacheImpl.java 2431 2014-03-30 20:00:05Z alan $
 */
package com.naryx.tagfusion.cfm.cache.impl;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;

import org.aw20.util.StringUtil;

import com.naryx.tagfusion.cfm.cache.CacheInterface;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;

public class MemcachedCacheImpl implements CacheInterface {
	private static long MONTH_MS = 30l * 24l * 60l * 60l * 1000l;
	
	private MemcachedClient memcache	= null;
	private int	waitTimeSeconds	= 0;
	private cfStructData props;
	

	@Override
	public void setProperties(String region, cfStructData _props) throws Exception {
		this.props	= _props;
		
		if ( memcache != null )
			shutdown();

		if ( !props.containsKey("server") )
			throw new Exception("'server' does not exist. in format: server1:port1 server2:port2");
		
		if ( !props.containsKey("waittimeseconds") )
			props.setData("waittimeseconds", 5);
		
		waitTimeSeconds	= StringUtil.toInteger( props.getData("waittimeseconds").getInt(), 5 );
	
		String server	= props.getData("server").getString();
		memcache			= new MemcachedClient( new BinaryConnectionFactory(), AddrUtil.getAddresses(server) );
		
		cfEngine.log( getName() + " server: " + server + "; WaitTimeSeconds: " + waitTimeSeconds );
	}

	
	@Override
	public void set(String id, cfData data, long ageMS, long idleTime) {
		int ageSecs = 0;
		
		if ( ageMS > MONTH_MS ){
			ageSecs	= (int)(MONTH_MS / 1000) - 60;
		}else if ( ageMS < MONTH_MS && ageMS > 0 ){
			ageSecs	= (int)(ageMS / 1000);
		}else if ( ageMS < 0 )
			ageSecs = (int)(MONTH_MS / 1000) - 60;
		
		Future<Boolean> status = null;

		try{
		
			if ( data instanceof cfStringData )
				status = memcache.set( id, ageSecs, ((cfStringData)data).getString() );
			else
				status = memcache.set( id, ageSecs, data );

			status.get(waitTimeSeconds, TimeUnit.SECONDS);
		}catch(Exception e){
			cfEngine.log( getName() + " set Failed: " + e.getMessage() );
			status.cancel(false);
		}
	}

	
	@Override
	public cfData get(String id) {
		Future<Object> future = memcache.asyncGet(id);

		try {
			Object obj = future.get(waitTimeSeconds, TimeUnit.SECONDS);
			
			if ( obj == null )
				return null;
			else if ( obj instanceof String )
				return new cfStringData( (String)obj );
			else if ( obj instanceof cfData )
				return (cfData)obj;
			else
				return new cfJavaObjectData( obj );

		} catch (Exception e) {
			cfEngine.log( getName() + " get Failed: " + e.getMessage() );
			future.cancel(false);
		}

		return null;
	}

	
	@Override
	public void delete(String id, boolean exact) {
		Future<Boolean> success = memcache.delete(id);

		try {
			success.get();
		} catch (Exception e) {
			cfEngine.log( getName() + " deleteFailed: " + e.getMessage() );
			success.cancel(false);
		}
	}

	@Override
	public void deleteAll() {
		Future<Boolean> success = memcache.flush();
		try {
			success.get();
		} catch (Exception e) {
			cfEngine.log( getName() + " flushFailed: " + e.getMessage() );
			success.cancel(false);
		}
	}

	@Override
	public String getName() {
		return "memcached";
	}

	@Override
	public cfArrayData getAllIds() {
		return cfArrayData.createArray(1);
	}

	@Override
	public cfStructData getStats() {
		cfStructData	stats	= new cfStructData();
		
		Map<SocketAddress, Map<String, String>> ms = memcache.getStats();
		
		Iterator<SocketAddress>	it	= ms.keySet().iterator();
		while ( it.hasNext() ){
			SocketAddress sa	= it.next();
			
			cfStructData	sd	= new cfStructData();
			
			Map<String,String>	innerMap	= ms.get(sa);
			Iterator<String>	innerMapIt	= innerMap.keySet().iterator();
			String key;
			
			while ( innerMapIt.hasNext() ){
				key	= innerMapIt.next();
				sd.setData( key, innerMap.get(key) );
			}

			stats.setData( sa.toString(), sd );
		}
		
		return stats;
	}

	@Override
	public cfStructData getProperties() {
		return props;
	}

	@Override
	public void shutdown() {
		memcache.shutdown();
		memcache	= null;
	}

}
