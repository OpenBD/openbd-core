/* 
 * Copyright (C) 2000 - 2008 TagServlet Ltd
 *
 * This file is part of the BlueDragon Java Open Source Project.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.naryx.tagfusion.cfm.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.aw20.security.MD5;

import com.naryx.tagfusion.cfm.cache.impl.MemcachedCacheImpl;
import com.naryx.tagfusion.cfm.cache.impl.MemoryDiskCacheImpl;
import com.naryx.tagfusion.cfm.cache.impl.MongoCacheImpl;
import com.naryx.tagfusion.cfm.cache.impl.NullCacheImpl;
import com.naryx.tagfusion.cfm.cache.impl.RedisCacheImpl;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfStructData;


public class CacheFactory extends Object {

	public static String CFFUNCTION = "function";
	public static CacheFactory thisInst;

	static {
		new CacheFactory();
	}

	private HashMap<String, CacheInterface> cacheEngines;
	private Map<String, lockSemaphore> cacheLocks;


	private CacheFactory() {
		thisInst = this;
		cacheEngines = new HashMap<String, CacheInterface>();
		cacheLocks = new HashMap<String, lockSemaphore>();


		cfStructData props = new cfStructData();
		props.setData( "type", "null" );
		try {
			createCacheEngine( "nullcache", props );
		} catch ( Exception e ) {}

		setMemoryDiskCache( CFFUNCTION, 100, false, 0 );
		setMemoryDiskCache( "default", 25, false, 0 );
	}


	public static void shutdown() {
		Iterator<CacheInterface> it = thisInst.cacheEngines.values().iterator();
		while ( it.hasNext() ) {
			it.next().shutdown();
			it.remove();
		}

		cfEngine.log( "CacheFactory: All Cache Regions shutdown" );
	}


	public static String[] getAllRegionsNames() {
		return thisInst.cacheEngines.keySet().toArray( new String[0] );
	}


	public static boolean isCacheEnabled( String type ) {
		return thisInst.cacheEngines.containsKey( type.toLowerCase() );
	}


	public static CacheInterface getCacheEngine( String type ) {
		CacheInterface e = thisInst.cacheEngines.get( type.toLowerCase() );
		return ( e != null ) ? e : thisInst.cacheEngines.get( "nullcache" );
	}


	public static void removeCacheEngine( String type ) {
		synchronized ( thisInst ) {
			CacheInterface ci = thisInst.cacheEngines.remove( type.toLowerCase() );
			if ( ci != null ) {
				ci.shutdown();
				cfEngine.log( ci.getName() + "." + type + ": removed" );
			}
		}
	}


	public static void createCacheEngine( String region, cfStructData props ) throws Exception {

		if ( !props.containsKey( "type" ) )
			throw new Exception( "missing 'type' parameter" );

		// Pull out the type
		CacheInterface cacheinterface = null;
		String type = props.getData( "type" ).getString().toLowerCase();

		if ( type.equals( "null" ) )
			cacheinterface = new NullCacheImpl();
		else if ( type.equals( "memorydisk" ) )
			cacheinterface = new MemoryDiskCacheImpl();
		else if ( type.equals( "memcached" ) )
			cacheinterface = MemcachedCacheImpl.getInstance( region.toLowerCase(), props.getData( "server" ).getString() );
		else if ( type.equals( "mongo" ) )
			cacheinterface = new MongoCacheImpl();
		else if ( type.equals( "redis" ) )
			cacheinterface = RedisCacheImpl.getInstance( region.toLowerCase(), props.getData( "server" ).getString() );


		// Make sure we have one
		if ( cacheinterface == null )
			throw new Exception( "unknown 'type' parameter [" + type + "]" );

		synchronized ( thisInst ) {
			cacheinterface.setProperties( region.toLowerCase(), props );
			thisInst.cacheEngines.put( region.toLowerCase(), cacheinterface );
		}
	}


	/*
	 * Creates a normalized cache name from a potentially long string
	 */
	public static String createCacheKey( String data ) {
		return MD5.getDigest( data + "crc" );
	}


	/*
	 * ------------------------------------------------------------------
	 * We use a simple semaphore to track the number of waiting threads
	 * waiting on a lock. We only remove from the list of locks when
	 * there are no more thread waitings. Since the getLock/removeLock
	 * functions are synchronized there is no need to synchronize the
	 * lockSemaphore classes.
	 * ------------------------------------------------------------------
	 */

	public static Object getLock( String key ) {
		synchronized ( thisInst.cacheLocks ) {
			lockSemaphore lock = thisInst.cacheLocks.get( key );
			if ( lock == null ) {
				lock = thisInst.new lockSemaphore();
				thisInst.cacheLocks.put( key, lock );
			}
			lock.lock();
			return lock;
		}
	}


	public static void removeLock( String key ) {
		synchronized ( thisInst.cacheLocks ) {
			lockSemaphore lock = thisInst.cacheLocks.get( key );

			if ( lock == null ) {
				return;
			}

			lock.unlock();
			if ( lock.isFree() ) {
				thisInst.cacheLocks.remove( key );
			}
		}
	}

	class lockSemaphore {

		int inUse = 0;


		public void lock() {
			inUse = inUse + 1;
		}


		public void unlock() {
			inUse = inUse - 1;
		}


		public boolean isFree() {
			return ( inUse == 0 );
		}
	}


	/**
	 * Helper method for creating the default engines for the core engine
	 * 
	 * @param region
	 * @param cacheCount
	 * @param diskPersit
	 * @param diskMaxMB
	 */
	public static void setMemoryDiskCache( String region, int cacheCount, boolean diskPersit, int diskMaxMB ) {
		cfStructData props = new cfStructData();
		props.setData( "type", "memorydisk" );
		props.setData( "diskpersistent", cfBooleanData.TRUE );
		props.setData( "diskcleanonstart", cfBooleanData.TRUE );
		props.setData( "diskmaxsizemb", diskMaxMB );
		props.setData( "size", cacheCount );

		try {
			createCacheEngine( region, props );
		} catch ( Exception e ) {}
	}
}
