/* 
 *  Copyright (C) 2012-2015 TagServlet Ltd
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
 *  server user password db collection
 */
package com.naryx.tagfusion.cfm.cache.impl;

import java.io.IOException;
import java.util.Date;

import org.aw20.io.ByteArrayOutputStreamRaw;
import org.aw20.io.FileUtil;
import org.aw20.util.SystemClock;
import org.aw20.util.SystemClockEvent;
import org.bson.Document;

import ucar.unidata.util.DateUtil;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
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
	private MongoDatabase db = null;
	private MongoCollection<Document> col = null;
	
	@Override
	public cfStructData getProperties() {
		return props;
	}

	@Override
	public void setProperties(String region, cfStructData _props) throws Exception {
		shutdown();
		
		this.props	= _props;

		if ( !props.containsKey("mongoclienturi") )
			throw new Exception("'mongoclienturi' does not exist. in format: mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]");

		if ( !props.containsKey("db") )
			props.setData("db", "openbdcache" );
		
		if ( !props.containsKey("collection") )
			props.setData("collection", region );


		MongoClientURI	mcu	= new MongoClientURI(props.getData("mongoclienturi").getString());
		mongo	= new MongoClient( mcu );
		
		db	= mongo.getDatabase( props.getData("db").getString() );

		createCollection();
		
		SystemClock.setListenerMinute( this, 5 );
	}

	
	private void createCollection() throws dataNotSupportedException{
		col	= db.getCollection( props.getData("collection").getString() );
		col.createIndex( new Document("id",true) );
		col.createIndex( new Document("ct",true) );
	}
	
	
	@Override
	public void set(String id, cfData data, long ageMs, long idleTime) {
		//TODO: idleTime not implemented so idleSpan won't work
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
		Document	vals	= new Document("id", id).append("ct", ct);
		
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
		
		
		Document	keys	= new Document("id", id);
		col.updateOne(keys, new Document("$set", vals), new UpdateOptions().upsert(true));
	}

	
	@Override
	public cfData get(String id) {
		Document doc	= col.find( Filters.eq("id", id) ).first();
		
		if ( doc != null ){

			// Check that the date is correct
			Date ct	= (Date)doc.get("ct");
			if ( ct.getTime() < System.currentTimeMillis() ){
				delete( id, true );
				statsMissAge++;
				return null;
			}
			
			statsHit++;
			if ( doc.containsKey("vs") )
				return new cfStringData( (String)doc.get("vs") );
			else{
				Object bufObj	= doc.get("vb");
				byte[]	buf;
				if ( bufObj instanceof org.bson.types.Binary	){
					buf = ( (org.bson.types.Binary) bufObj ).getData();
				}else{ // should be byte []. Keep for backwards compatibility
					buf = (byte[]) bufObj;
				}
				
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
			col.deleteOne( new Document("id", id) );
		}else{
			col.deleteMany( Filters.regex("id", "" + id + "*") ); 
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
			stats.setData("collection", 	col.getNamespace().getCollectionName() );
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
		col.deleteMany( Filters.lte("ct", new Date() ) );
	}
}