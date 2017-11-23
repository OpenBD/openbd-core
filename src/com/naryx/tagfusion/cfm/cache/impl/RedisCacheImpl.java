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
 *  $Id:$
 */
package com.naryx.tagfusion.cfm.cache.impl;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aw20.util.StringUtil;
import org.joda.time.Instant;

import com.naryx.tagfusion.cfm.cache.CacheInterface;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.util.Transcoder;

import io.lettuce.core.Range;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.StreamScanCursor;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.ZAddArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.output.KeyValueStreamingChannel;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.DirContextDnsResolver;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;


public class RedisCacheImpl implements CacheInterface {

	// One day in milliseconds, used as TTL if one within one day is not provided
	private static long DAY_MS = 24 * 60 * 60 * 1000;

	// The redis client
	private RedisClient redisClient = null;

	// A connection to the redis client
	private StatefulRedisConnection<String, String> connection = null;

	// Async commands for the connection in this cache instance
	private RedisAsyncCommands<String, String> asyncCommands = null;

	// A structure containing all the properties
	private cfStructData props;

	// The region for this cache instance
	private String region;

	// The server to which this cache instance connects
	private String server;

	// The ttls for the keys in this region
	private String ttls;

	// The default operation timeout in seconds for this cache instance
	private int waitTimeSeconds = 0;

	// Reactive commands for the connection in this cache instance
	RedisReactiveCommands<String, String> reactiveCommands;

	// A collection of mappings from a cache keys to cache instances.
	private static HashMap<String, RedisCacheImpl> instances = new HashMap<>();


	/* Returns a cache key representing a mapping from the given region to the given server */
	private static String getCacheKey( String _region, String _server ) {
		return _region + "|||" + _server;
	}


	/**
	 * Returns a cache instance for the given region and server, when one as such exists.
	 * Otherwise creates a cache instance for the given region and server.
	 * 
	 * @param region
	 *          the given region.
	 * @param server
	 *          the given server.
	 * @return a RedisCacheImpl object.
	 */
	public static RedisCacheImpl getInstance( String region, String server ) {
		// Search the cache instance
		String cacheKey = getCacheKey( region, server );
		RedisCacheImpl redisCache = instances.get( cacheKey );

		// When not found, create a new one (thread-safe)
		if ( redisCache == null ) {
			synchronized ( instances ) {
				redisCache = instances.get( cacheKey );
				if ( redisCache == null ) {
					redisCache = new RedisCacheImpl();
					instances.put( cacheKey, redisCache );
				}
			}
		}

		return redisCache;
	}


	/*
	 * Auxiliary method used to build a cfArrayData object,
	 * populating it with the keys in the given list.
	 */
	private cfArrayData buildCfArrayData( List<String> keys ) {
		cfArrayData data = cfArrayData.createArray( 1 );

		for ( String key : keys ) {
			try {
				data.addElement( new cfStringData( key ) );
			} catch ( cfmRunTimeException e ) {
				if ( cfEngine.thisPlatform != null ) {
					cfEngine.log( getName() + " buildCfArrayData Failed: " + e.getMessage() );
				}
			}
		}

		return data;
	}


	/**
	 * Delete the given key, or all the keys starting with 'key'.
	 * 
	 * @param key
	 *          the given key, or key prefix.
	 * @param exact
	 *          true to delete the given key, false to delete all keys starting with 'key'.
	 */
	@Override
	public void delete( String key, boolean exact ) {
		if ( exact ) {
			deleteExactTrue( key );
		} else {
			deleteExactFalse( key );
		}
	}


	/**
	 * Delete all the keys in the region data store, and its respective TTLs.
	 * 
	 * Time Complexity is O(2N), where N is the number of keys in the region.
	 * This N is multiplied by 2 since there are N ttls for N keys.
	 */
	@Override
	public void deleteAll() {

		RedisFuture<TransactionResult> future = null;
		try {

			/*
			 * Atomic transaction (MULTI/EXEC) to delete the data store for the region,
			 * and the respective ttls data store.
			 * 
			 * (see https://redis.io/commands/multi)
			 */
			asyncCommands.multi();
			asyncCommands.del( region );
			asyncCommands.del( ttls );
			future = asyncCommands.exec();
			future.get( waitTimeSeconds, TimeUnit.SECONDS );


		} catch ( Exception e ) {
			if ( cfEngine.thisPlatform != null ) {
				cfEngine.log( getName() + " deleteAllFailed: " + e.getMessage() );
			}
			if ( future != null ) {
				future.cancel( false );
			}
		}
	}


	/*
	 * Auxiliary method used in com.naryx.tagfusion.cfm.cache.impl.RedisCacheImpl.delete(String, boolean),
	 * called when the boolean parameter 'exact' is 'false'.
	 * 
	 * Deletes all keys starting with 'key'.
	 * 
	 * Scans the region data store and looks for keys starting with 'key',
	 * on each key sets each ttl to 0 seconds.
	 * 
	 * Since 'ttl' is zero the key will no longer be returned,
	 * and eventually will be deleted in the background by the clean up scheduler.
	 * 
	 * Scanning is a non-blocking operation,
	 * which means Redis will continue to serve any client requests while the scan is running.
	 */
	private void deleteExactFalse( String key ) {

		RedisFuture<StreamScanCursor> futureScan = null;
		try {

			/*
			 * A key-value streaming channel that on each key sets a ttl of 0 seconds for that key.
			 * 
			 */
			KeyValueStreamingChannel<String, String> adapter = new KeyValueStreamingChannel<String, String>() {

				@Override
				public void onKeyValue( String key, String value ) {
					/*
					 * Set TTL to the given key for 0 seconds
					 * Time complexity: O(log(N)) for each item added, where N is the number of elements in the sorted set.
					 */
					reactiveCommands.zadd( ttls, ZAddArgs.Builder.xx(), 0, key ).subscribe();
				}
			};

			/*
			 * The scan uses a cursor, similar to an iterative variable in a for-loop.
			 * A new the cursor is returned after each iteration, which can be used to resume scanning.
			 * The keys itself are stream to the channel defined above.
			 * This scan is defined to return 500 keys on each iteration.
			 * Increasing the number of keys returned, reduces the round-trip time, but uses more memory.
			 * 
			 * Time complexity: O(1) for every call. O(N) for a complete iteration,
			 * including enough command calls for the cursor to return back to 0. N is the number of elements inside the collection.
			 */
			futureScan = asyncCommands.hscan( adapter, region, ScanCursor.INITIAL, ScanArgs.Builder.matches( key + "*" ).limit( 500 ) );
			StreamScanCursor cursor = futureScan.get( waitTimeSeconds, TimeUnit.SECONDS );

			while ( !cursor.isFinished() ) {

				futureScan = asyncCommands.hscan( adapter, region, cursor, ScanArgs.Builder.matches( key + "*" ).limit( 500 ) );
				cursor = futureScan.get( waitTimeSeconds, TimeUnit.SECONDS );

			}
		} catch ( Exception e ) {
			if ( cfEngine.thisPlatform != null ) {
				cfEngine.log( getName() + " deleteAllFailed: " + e.getMessage() );
			}
			if ( futureScan != null ) {
				futureScan.cancel( false );
			}
		}

	}


	/*
	 * Auxiliary method used in com.naryx.tagfusion.cfm.cache.impl.RedisCacheImpl.delete(String, boolean),
	 * called when the boolean parameter 'exact' is 'true'.
	 * 
	 * Deletes the given key, and its respective ttl.
	 * 
	 * Time Complexity is O(log(N)) with N being the number of ttls in the ttls data store for this region,
	 * and O(1) to delete the key from the key data store for this region.
	 */
	private void deleteExactTrue( String key ) {

		RedisFuture<TransactionResult> futureDel = null;

		try {

			/*
			 * Atomic transaction (MULTI/EXEC) to delete the key from the region data store,
			 * and the respective ttl from the ttls data store for this region.
			 * 
			 * (see https://redis.io/commands/multi)
			 */
			asyncCommands.multi();
			asyncCommands.zrem( ttls, key );
			asyncCommands.hdel( region, key );
			futureDel = asyncCommands.exec();
			futureDel.get( waitTimeSeconds, TimeUnit.SECONDS );

		} catch ( Exception e ) {
			if ( cfEngine.thisPlatform != null ) {
				cfEngine.log( getName() + " deleteExactFalse: " + e.getMessage() );
			}
			if ( futureDel != null ) {
				futureDel.cancel( false );
			}
		}

	}


	/**
	 * Return the given key, if it is not expired.
	 * 
	 * Time Complexity is 2*O(1).
	 */
	@Override
	public cfData get( String key ) {

		// Calculate the time now, represented as a decimal
		long nowSecs = Instant.now().getMillis() / 1000;
		double nowTtl = getDecimalTtl( nowSecs );

		RedisFuture<TransactionResult> future = null;
		TransactionResult transactionResult = null;
		try {
			if ( key == null ) {
				throw new Exception( "'key' cannot be null" );
			}

			/*
			 * Atomic transaction (MULTI/EXEC) to check get the key and its ttl in one round.
			 * 
			 * (see https://redis.io/commands/multi)
			 */
			asyncCommands.multi();
			asyncCommands.zscore( ttls, key ); // Time complexity: O(1)
			asyncCommands.hget( region, key ); // Time complexity: O(1)
			future = asyncCommands.exec();

			transactionResult = future.get( waitTimeSeconds, TimeUnit.SECONDS );

			// Get the result of the 'ZSCORE' command, the TTL for the given key
			Double keyTtl = transactionResult.get( 0 );
			String base64value = null;
			if ( keyTtl > nowTtl ) {
				// Set the value to be returned, when the TTL is not expired
				base64value = transactionResult.get( 1 );
			}

			return base64value == null ? null : (cfData) Transcoder.fromString( base64value );

		} catch ( Exception e ) {
			if ( cfEngine.thisPlatform != null ) {
				cfEngine.log( getName() + " get Failed: " + e.getMessage() );
			}
			if ( future != null ) {
				future.cancel( true );
			}
		}
		return null;
	}


	/**
	 * Returns all the keys belonging to the region of this cache instance.
	 * 
	 * Time Complexity is O(N) where N is the number of keys in this region.
	 */
	@Override
	public cfArrayData getAllIds() {

		try {

			List<String> keys = asyncCommands.hkeys( region ).get( 5, TimeUnit.SECONDS ); // Time complexity: O(N) where N is the size of the hash.
			return buildCfArrayData( keys );

		} catch ( Exception e ) {
			if ( cfEngine.thisPlatform != null ) {
				cfEngine.log( getName() + " get Failed: " + e.getMessage() );
			}
		}

		return null;

	}


	/**
	 * Return the type of caching technology used by this cache instance, i.e. "redis".
	 */
	@Override
	public String getName() {
		String type = null;
		try {
			cfData data = props.getData( "type" );
			type = data != null ? data.getString() : null;
		} catch ( dataNotSupportedException e ) {
			if ( cfEngine.thisPlatform != null ) {
				cfEngine.log( getName() + " getName: " + e.getMessage() );
			}
		}
		return type;
	}


	/**
	 * Returns the properties for this cache instance.
	 */
	@Override
	public cfStructData getProperties() {
		return props;
	}


	/**
	 * Returns general statistics about the server.
	 * This is the content of section 'stats' in Redis command 'INFO'.
	 * 
	 * (see https://redis.io/commands/info)
	 */
	@Override
	public cfStructData getStats() {
		cfStructData stats = new cfStructData();

		RedisFuture<String> future = null;

		try {
			// Get the INFO stats information
			future = asyncCommands.info( "stats" );
			Object obj = future.get( waitTimeSeconds, TimeUnit.SECONDS );

			if ( obj == null )
				return null;
			else if ( obj instanceof String ) {
				// Parse the result of INFO stats in order to build a cfStructData carrying the results
				BufferedReader reader = new BufferedReader( new StringReader( obj.toString() ) );
				String line;
				while ( ( line = reader.readLine() ) != null ) {
					String[] keyValue = line.split( ":" );
					if ( keyValue.length == 2 ) {
						stats.setData( keyValue[0], new cfStringData( keyValue[1] ) );
					}
				}

			}

		} catch ( Exception e ) {
			if ( cfEngine.thisPlatform != null ) {
				cfEngine.log( getName() + " getStats Failed: " + e.getMessage() );
			}
			if ( future != null ) {
				future.cancel( false );
			}
		}

		return stats;
	}


	/**
	 * Set the given key and value in the region for this cache instance,
	 * with the given time-to-live, i.e. ageMS.
	 * 
	 * Time Complexity is O(1) to set the key-value pair and O(log(N) to set the tll for the key,
	 * where N is the number of the keys in this region.
	 * 
	 * @param key
	 *          the given key.
	 * @param data
	 *          the value for this key.
	 * @param ageMS
	 *          the ttl for this key-value pair. If ageMS < 1 || ageMs > dayMs, then ageMS = dayMs.
	 * @param idleTime
	 *          not used in this implementation.
	 */
	@Override
	public void set( String key, cfData data, long ageMS, long idleTime ) {

		// Convert the ageMs, tll in milliseconds, to ageSecs, ttl in seconds.
		long ageSecs;
		if ( ageMS < 1 || ageMS > DAY_MS ) {
			ageSecs = DAY_MS / 1000;
		} else {
			ageSecs = ageMS / 1000;
		}

		// Get the time now in seconds plus the ageSecs in seconds
		long nowSecs = Instant.now().getMillis() / 1000 + ageSecs;

		// Convert the time now in seconds to a decimal
		double ttl = getDecimalTtl( nowSecs );

		RedisFuture<TransactionResult> future = null;
		RedisFuture<Boolean> futureHset = null;
		RedisFuture<Long> futureZadd = null;

		try {
			if ( key == null ) {
				throw new Exception( "'key' cannot be null" );
			}

			if ( data == null ) {
				throw new Exception( "'data' cannot be null" );
			}

			String base64value = Transcoder.toString( data );

			/*
			 * Atomic transaction (MULTI/EXEC) to set the key and its ttl.
			 * 
			 * (see https://redis.io/commands/multi)
			 */
			asyncCommands.multi();
			futureHset = asyncCommands.hset( region, key, base64value ); // Time complexity: O(1)
			futureZadd = asyncCommands.zadd( ttls, ttl, key ); // O(log(N)) for each key added, where N is the number of keys in the sorted set.
			future = asyncCommands.exec();
			future.get( waitTimeSeconds, TimeUnit.SECONDS );

		} catch ( Exception e ) {
			if ( cfEngine.thisPlatform != null ) {
				cfEngine.log( getName() + " set Failed: " + e.getMessage() );
			}
			if ( futureHset != null ) {
				futureHset.cancel( true );
			}
			if ( futureZadd != null ) {
				futureZadd.cancel( true );
			}
			if ( future != null ) {
				future.cancel( true );
			}
		}
	}


	/**
	 * Convert the Unix time in seconds to a decimal,
	 * where the most significant digits represent the number of days elapsed since the Unix epoch
	 * and the less significant digits represent the number of seconds elapsed since the start of
	 * the day.
	 * 
	 * @param nowSecs
	 *          the Unix time in seconds.
	 * @return a decimal representing the Unix time.
	 */
	private double getDecimalTtl( long nowSecs ) {

		/*
		 * 1 day = 86400
		 * nowDays is the integer division of the Unix time in seconds by 1 day in seconds,
		 * thus it will give us the number of days elapsed since the Unix epoch
		 */
		long nowDays = nowSecs / 86400;

		/*
		 * The mod division of the Unix time in seconds by the one day in seconds,
		 * gives us the remaining of the integer division of the Unix time in seconds by 1 day in seconds.
		 * Thus this represents the number of seconds elapsed since the start of the day.
		 */
		long nowRemSecs = nowSecs % 86400;

		/*
		 * The remaining in this case is a number between 0 and 86400 thus dividing the remaining by 100k
		 * will reduce the remaining to 0.<number of seconds elapsed since the start of the day>
		 */
		double nowRemSecsDecimal = nowRemSecs / 100000.0;

		/*
		 * Finally add the Unix time in days (times 1.0 to make it a double)
		 * with the remaining decimal representing the number of seconds elapsed since the start of the day
		 */
		return nowDays * 1.0 + nowRemSecsDecimal;
	}


	/**
	 * Set properties for this cache instance and starts the connection to the Redis instance.
	 * Properties will only be set the first time this method is called.
	 * Properties that can be set are:
	 * - type: must be 'redis'.
	 * - server: the redis instance url in format 'redis://<host>:<port>'
	 * - region: the region for this cache instance, i.e. a key for this cache instance data store within the redis instance.
	 * - waitTimeSeconds: the operations time out in seconds.
	 */
	@Override
	public void setProperties( String region, cfStructData props ) throws Exception {
		if ( redisClient != null )
			return;

		if ( region == null ) {
			throw new Exception( "'region' can not be null" );
		} else {
			this.region = "cache:region:" + region;
			this.ttls = "cache:ttls:" + region;
		}

		this.props = validateAndParseProps( props );

		start();

	}


	/**
	 * Shuts down the server and all the connections and resources associated with it.
	 */
	@Override
	public void shutdown() {

		if ( connection != null ) {
			connection.close();
		}

		if ( redisClient != null ) {
			redisClient.shutdown();
		}

		redisClient = null;
		connection = null;
		asyncCommands = null;

		region = null;
		props = null;
		waitTimeSeconds = 0;
	}


	/* Auxiliary method to start the server */
	private void start() {
		DefaultClientResources clientResources = DefaultClientResources.builder() //
				.dnsResolver( new DirContextDnsResolver() ) // Does not cache DNS lookups (needed for ElasticCache)
				.build();

		redisClient = RedisClient.create( clientResources, server );
		connection = redisClient.connect();
		asyncCommands = connection.async();
		reactiveCommands = connection.reactive();

		/*
		 * Run the global clean up scheduler,
		 * which cleans expired keys from all regions in the Redis instance in background
		 */
		runGlobalCleanupScheduler();

		if ( cfEngine.thisPlatform != null )
			cfEngine.log( getName() + " server: " + server + "; WaitTimeSeconds: " + waitTimeSeconds );
	}


	/*
	 * Auxiliary method to start a cleanup scheduler in background,
	 * which operates only in the region for this cache instance.
	 * 
	 * For complete documentation on Flux and Lettuce reactive API see:
	 * https://lettuce.io/core/milestone/reference/
	 * 
	 */
	@SuppressWarnings( "unused" )
	private void runRegionCleanupScheduler() {

		// Emit a tick each 6 milliseconds
		Flux
				.interval( Duration.ofMillis( 6000 ), Schedulers.newParallel( "redisScheduler:" + region + server ) )
				.doOnNext( tick -> {
					// System.out.println( "\ntick: " + tick );
					long nowSecs = Instant.now().getMillis() / 1000;
					double nowTtl = getDecimalTtl( nowSecs );
					// On each tick get all the keys with expired ttls
					reactiveCommands.zrangebyscore( ttls, Range.create( 0.0, nowTtl ) )
							.doOnNext( key -> {
								// System.out.println( "\nFound expired key: " + key );
								reactiveCommands.multi()
										.doOnSuccess( s -> {
											// On each expired key atomically delete the key and its ttl
											reactiveCommands.zrem( ttls, key ).subscribe();
											reactiveCommands.hdel( region, key ).subscribe();
											// System.out.println( "\nRemoved expired key from " + ttls + ":" + key );
											// System.out.println( "\nDeleted expired key from " + region + ": " + key );
										} ).flatMap( s -> reactiveCommands.exec() )
										.doOnError( error -> {
											if ( cfEngine.thisPlatform != null ) {
												cfEngine.log( getName() + " runCleanupScheduller Failed: " + error.getMessage() );
											}
										} )
										.subscribe();
							} )
							.doOnError( error -> {
								if ( cfEngine.thisPlatform != null ) {
									cfEngine.log( getName() + " runCleanupScheduller Failed: " + error.getMessage() );
								}
							} )
							.subscribeOn( Schedulers.newParallel( "redisScheduler:" + region + server ) )
							.subscribe();
				} )
				.doOnError( error -> {
					if ( cfEngine.thisPlatform != null ) {
						cfEngine.log( getName() + " runCleanupScheduller Failed: " + error.getMessage() );
					}
				} )
				.subscribe();
	}


	private void runScan( ScanCursor cursor ) {

		/*
		 * Scan 500 keys starting from the given cursor,
		 * triggering a clean up region and the next scan in parallel
		 */
		reactiveCommands.scan( cursor, ScanArgs.Builder.matches( "*" ).limit( 500 ) )
				.doOnNext( next -> {
					// System.out.println( "Clean up region" );

					// For each key representing a region, clean up region
					List<String> keys = next.getKeys();
					Flux.fromIterable( keys ).filter( key -> !key.startsWith( ttls ) && key.startsWith( "cache:" ) )
							.doOnNext( region -> {
								// Clean up the given region
								cleanUpRegion( region );
							} )
							.doOnError( error -> {
								// log error
								if ( cfEngine.thisPlatform != null ) {
									cfEngine.log( getName() + " runCleanupScheduller Failed: " + error.getMessage() );
								}
							} )
							.subscribe();

				} )
				.doOnNext( next -> {
					// Trigger next scan
					// System.out.println( "Scan again" );
					if ( !next.isFinished() ) {
						runScan( ScanCursor.of( next.getCursor() ) );
					} else {
						// System.out.println( "Scan completed" );
					}
				} )
				.doOnError( error -> {
					if ( cfEngine.thisPlatform != null ) {
						cfEngine.log( getName() + " runCleanupScheduller Failed: " + error.getMessage() );
					}
				} )
				.subscribeOn( Schedulers.newParallel( "redisScheduler:" + region + server ) )
				.subscribe();

	}


	/*
	 * Auxiliary method to clean up a region.
	 * Gets all the keys that are expired and deletes their ttls and key-value pairs.
	 */
	private void cleanUpRegion( String region ) {

		long nowSecs = Instant.now().getMillis() / 1000;
		double nowTtl = getDecimalTtl( nowSecs );
		reactiveCommands.zrangebyscore( ttls, Range.create( 0.0, nowTtl ) )
				.doOnNext( key -> {
					// System.out.println( "\nFound expired key: " + key );
					reactiveCommands.multi()
							.doOnSuccess( s -> {
								reactiveCommands.zrem( ttls, key ).subscribe();
								reactiveCommands.hdel( region, key ).subscribe();
								// System.out.println( "\nRemoved expired key from " + ttls + ":" + key );
								// System.out.println( "\nDeleted expired key from " + region + ": " + key );
							} )
							.flatMap( s -> reactiveCommands.exec() )
							.doOnError( error -> {
								if ( cfEngine.thisPlatform != null ) {
									cfEngine.log( getName() + " runCleanupScheduller Failed: " + error.getMessage() );
								}
							} )
							.subscribe();
				} )
				.doOnError( error -> {
					if ( cfEngine.thisPlatform != null ) {
						cfEngine.log( getName() + " runCleanupScheduller Failed: " + error.getMessage() );
					}
				} )
				.subscribe();

	}


	/*
	 * Auxiliary method to start a cleanup scheduler in background,
	 * which operates across all regions in the Redis instance.
	 * 
	 * For complete documentation on Flux and Lettuce reactive API see:
	 * https://lettuce.io/core/milestone/reference/
	 * 
	 */
	private void runGlobalCleanupScheduler() {

		// Emit a tick every 6 seconds
		Flux
				.interval( Duration.ofMillis( 6000 ), Schedulers.newParallel( "redisScheduler:" + region + server ) )
				.doOnNext( tick -> {
					// System.out.println( "\ntick: " + tick );
					// On each tick start a scan
					runScan( ScanCursor.INITIAL );
				} )
				.doOnError( error -> {
					if ( cfEngine.thisPlatform != null ) {
						cfEngine.log( getName() + " runCleanupScheduller Failed: " + error.getMessage() );
					}
				} )
				.subscribe();

	}


	/* Auxiliary method to validate and parse the properties for the this RedisCacheImpl */
	private cfStructData validateAndParseProps( cfStructData props ) throws Exception {
		if ( props == null )
			throw new Exception( "'props' can not be null" );

		if ( !props.containsKey( "type" ) )
			throw new Exception( "'type' can not be null" );

		if ( !props.getData( "type" ).getString().equals( "redis" ) )
			throw new Exception( "'type' must be 'redis'" );

		if ( !props.containsKey( "server" ) )
			throw new Exception( "'server' does not exist. in format: server1:port1 server2:port2" );

		if ( !props.containsKey( "waittimeseconds" ) )
			props.setData( "waittimeseconds", 5 );

		waitTimeSeconds = StringUtil.toInteger( props.getData( "waittimeseconds" ).getInt(), 5 );
		server = props.getData( "server" ).getString();

		return props;
	}
}
