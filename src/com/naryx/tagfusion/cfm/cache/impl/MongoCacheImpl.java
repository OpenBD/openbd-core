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
 *  $Id: MongoCacheImpl.java 2426 2014-03-30 18:53:18Z alan $
 *  
 *  server user password db collection
 */
package com.naryx.tagfusion.cfm.cache.impl;

import java.io.IOException;
import java.util.Date;

import org.aw20.io.ByteArrayOutputStreamRaw;
import org.aw20.io.FileUtil;
import org.aw20.util.SystemClock;
import org.aw20.util.SystemClockEvent;

import ucar.unidata.util.DateUtil;

import com.bluedragon.mongo.MongoDSN;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.naryx.tagfusion.cfm.cache.CacheInterface;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

public class MongoCacheImpl implements CacheInterface, SystemClockEvent {
	private cfStructData props;
	
	private MongoClient	mongo = null;
	private DB db = null;
	private DBCollection col = null;
	
	@Override
	public cfStructData getProperties() {
		return props;
	}

	@Override
	public void setProperties(String region, cfStructData _props) throws Exception {
		shutdown();
		
		this.props	= _props;

		if ( !props.containsKey("server") )
			throw new Exception("'server' does not exist. in format: server1:port1 server2:port2");

		if ( !props.containsKey("db") )
			props.setData("db", "openbdcache" );
		
		if ( !props.containsKey("collection") )
			props.setData("collection", region );

		// Get the server
		String server	= props.getData("server").getString();
		
		String user	= props.containsKey("user") ? props.getData("user").toString() : null;
		String pass	= props.containsKey("password") ? props.getData("password").toString() : null;
		
		mongo	= MongoDSN.newClient( server, user, pass, props.getData("db").getString() );
		
		db	= mongo.getDB( props.getData("db").getString() );

		// Get the database/collection
		createCollection();
		
		SystemClock.setListenerMinute( this, 5 );
	}

	
	private void createCollection() throws dataNotSupportedException{
		col	= db.getCollection( props.getData("collection").getString() );
		col.createIndex( new BasicDBObject("id",true) );
		col.createIndex( new BasicDBObject("ct",true) );
	}
	
	
	@Override
	public void set(String id, cfData data, long ageMs, long idleTime) {
		Date ct	 = null;
		statsSet++;
		
		if ( ageMs == 0 ){
			delete( id, false );
			return;
		} else if ( ageMs < 0 ){
			ct	= new Date( System.currentTimeMillis() + DateUtil.MILLIS_MONTH );
		} else
			ct	= new Date( System.currentTimeMillis() + ageMs );
		
		// Set up the key
		DBObject	keys	= new BasicDBObject("id", id);
		BasicDBObject	vals	= new BasicDBObject("id", id).append("ct", ct);
		
		if ( data instanceof cfStringData )
			vals.append( "vs", ((cfStringData)data).getString() );
		else{
			
			ByteArrayOutputStreamRaw	bos	= new ByteArrayOutputStreamRaw( 32000 );
			try {
				FileUtil.saveClass(bos, data, true);
				vals.append( "vb", bos.toByteArray() );
			} catch (IOException e) {
				cfEngine.log( getName() + " id:" + id + "; Exception: " + e ); 
				return;
			}
			
		}
		
		col.update( keys, new BasicDBObject("$set", vals), true, false, WriteConcern.JOURNAL_SAFE );
	}

	
	@Override
	public cfData get(String id) {
		DBObject	keys	= new BasicDBObject("id", id);
		
		DBObject	dbo	= col.findOne(keys);
		if ( dbo != null ){

			// Check that the date is correct
			Date ct	= (Date)dbo.get("ct");
			if ( ct.getTime() < System.currentTimeMillis() ){
				delete( id, true );
				statsMissAge++;
				return null;
			}
			
			statsHit++;
			if ( dbo.containsField("vs") )
				return new cfStringData( (String)dbo.get("vs") );
			else{
				byte[]	buf	= (byte[])dbo.get("vb");
				try {
					return (cfData)FileUtil.loadClass(buf, true);
				} catch (Exception e) {
					cfEngine.log( getName() + " id:" + id + "; Exception: " + e );
				}
			}
		}else{
			statsMiss++;
		}

		return null;
	}

	@Override
	public void delete(String id, boolean exact) {
		statsDelete++;
		
		if ( exact ){
			col.remove( new BasicDBObject("id", id) );
		}else{
			col.remove( new BasicDBObject("id", new BasicDBObject("$regex","/" + id + "*/") ) );
		}
	}

	@Override
	public void deleteAll() {
		col.drop();
		try {
			createCollection();
		} catch (Exception e) {
			cfEngine.log( getName() + " " + e.getMessage() );
		}
	}

	@Override
	public String getName() {
		return "MongoCache";
	}

	@Override
	public cfArrayData getAllIds() {
		return cfArrayData.createArray(1);
	}
	
	private int	
		statsSet = 0, 
		statsGet = 0,
		statsDelete = 0, 
		statsMiss = 0,
		statsHit = 0,
		statsMissAge = 0;

	
	@Override
	public cfStructData getStats() {
		cfStructData	stats	= new cfStructData();

		stats.setData("set", 			statsSet);
		stats.setData("get", 			statsGet);
		stats.setData("hits", 		statsHit);
		stats.setData("miss", 		statsMiss);
		stats.setData("missage",	statsMissAge);
		stats.setData("delete",		statsDelete);
		
		if ( col != null ){
			stats.setData("size", 				col.count() );
			stats.setData("collection", 	col.getName() );
			stats.setData("db", 					db.getName() );
		}
		
		return stats;
	}

	
	@Override
	public void shutdown() {
		SystemClock.removeListenerMinute( this );
		
		if ( mongo != null ){
			mongo.close();
			mongo	= null;
			db	= null;
			col	= null;
		}
	}

	
	@Override
	public void clockEvent(int eventType) {
		col.remove( new BasicDBObject( "ct", new BasicDBObject("$lte", new Date()) ) );
	}
}