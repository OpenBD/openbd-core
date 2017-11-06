/* 
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
 *
 *  This file is part of OpenBD CFML Server Engine.
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
 *  
 *  http://openbd.org/
 *  https://github.com/OpenBD/openbd-core/blob/master/LICENSE
 */
package com.naryx.tagfusion.cfm.application.sessionstorage;

import java.util.Date;

import org.aw20.io.ByteArrayOutputStreamRaw;
import org.aw20.io.FileUtil;
import org.aw20.security.MD5;
import org.bson.Document;

import com.bluedragon.mongo.MongoDSN;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.application.cfSessionData;
import com.naryx.tagfusion.cfm.application.sessionUtility;
import com.naryx.tagfusion.cfm.application.sessionstorage.SessionStorageFactory.SessionEngine;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.variableStore;

public class SessionStorageMongoImpl extends SessionStorageBase implements SessionStorageInterface {
	private MongoClient	mongo = null;
	private MongoDatabase mdb = null;
	private MongoCollection<Document> col = null;
	private boolean indexes = false;
	
	private final String uri;
	
	/**
	 * mongodb://[a@b:]server1:port1,server2:port2/db
	 * 
	 * @param appName
	 * @param connectionUri
	 */
	public SessionStorageMongoImpl(String appName, String _connectionUri) throws Exception {
		super(appName);
		
		this.uri	= _connectionUri;
		
		if ( _connectionUri.startsWith("mongodb://") ){
			
			MongoClientURI clientURI = new MongoClientURI(_connectionUri);
			mongo	= new MongoClient( clientURI );
			mdb		= mongo.getDatabase( clientURI.getDatabase() == null ? "openbd" : clientURI.getDatabase() );

		}else{
		
			// Parse the URL
			String user = null, pass = null, db = "openbd";
	
			// Are they using a user/pass
			String connectionUri	= _connectionUri.substring( _connectionUri.indexOf("//")+2 );
			if ( connectionUri.indexOf("@") != -1 ){
				int c1 = connectionUri.indexOf("@");
				user	= connectionUri.substring( 0, c1 );
				
				int c2	= connectionUri.indexOf(":", c1 + 1 );
				if ( c2 == -1 )
					throw new Exception("invalid connection uri: " + _connectionUri );
				
				pass	= connectionUri.substring( c1 + 1, c2 );
				connectionUri	= connectionUri.substring( c2+1 );
			}
			
			// is there a database at the end
			int c1	= connectionUri.indexOf("/");
			if ( c1 != -1 ){
				db	=	connectionUri.substring( c1 + 1 );
				connectionUri	= connectionUri.substring(0,c1);
			}
	
			mongo	= MongoDSN.newClient( connectionUri, user, pass, db );
			
			mdb	= mongo.getDatabase( db );
		}

		
		// Create the collection
		col	= mdb.getCollection( "sessions" );
		setIndexes();
		cfEngine.log( appName + "; SessionStorageMongo: Created " + _connectionUri );
	}
	
	
	private void setIndexes(){
		if ( !indexes ){
			try{
				col.createIndex( new Document("et",true) );
				indexes = true;
			}catch(Exception e){
				cfEngine.log( appName + "; Exception: " + e );
			}
		}
	}
	
	
	public void shutdown(){
		if ( mongo != null ){
			mongo.close();
			mongo	= null;
			mdb		= null;
			col		= null;
			cfEngine.log("SessionStorageMongo: Closed");
		}
	}
	
	
	@Override
	public boolean onRequestStart(cfSession Session, long sessionTimeOut, sessionUtility sessionInfo) {
		boolean sessionStart = false;
		cfSessionData sessData = null;
		
		try{
		
			// If the timeout is greater than 0, then lookup it from Mongo
			if ( sessionTimeOut > 0 ){
				setIndexes();
				Document	keys	= new Document( "_id", appName + sessionInfo.getTokenShort() );
				Document	doc		= col.find(keys).first();
				
				if ( doc != null  ){
					Date et	= (Date)doc.get("et");
					if ( et.getTime() > System.currentTimeMillis() ){
						// Found a live one that we can use!!!
						
						Object bufObj	= doc.get("d");
						byte[]	buf;
						if ( bufObj instanceof org.bson.types.Binary	){
							buf = ( (org.bson.types.Binary) bufObj ).getData();
						}else{ // should be byte []. Keep for backwards compatibility
							buf = (byte[]) bufObj;
						}
						
						sessData	= (cfSessionData)FileUtil.loadClass(buf, true);
						sessData.setMD5( MD5.getDigest(buf) );
					}
				}
			}
		
		} catch (Exception e) {
			cfEngine.log( appName + " MongoDBException _id:" + sessionInfo.getTokenShort() + "; Exception: " + e );
		}
			
		// We don't have a session so create a new one
		if ( sessData == null ){
			sessData = new cfSessionData( appName );
			sessionStart = true;
		}

		sessData.setSessionID( appName, sessionInfo.CFID, sessionInfo.CFTOKEN );
		sessData.setTimeOut( sessionTimeOut );
		Session.setQualifiedData( variableStore.SESSION_SCOPE, sessData );
		
		return sessionStart;
	}

	
	
	/**
	 * Need to page the session out to Mongo
	 * @param session
	 */
	public void onRequestEnd(cfSession Session) {
		cfSessionData	sessData	= getSessionData( Session );
		if ( sessData == null )
			return;
	
		try{
			Document	keys	= new Document("_id", appName + sessData.getStorageID() );
			Document	vals	= new Document("et", new Date(System.currentTimeMillis() + sessData.getTimeOut() ) );
			
			// Serialize the object
			ByteArrayOutputStreamRaw	bos	= new ByteArrayOutputStreamRaw( 32000 );
			FileUtil.saveClass(bos, sessData, true);

			byte[]	buf	= bos.toByteArray();

			// Has it really changed; we only update the last used date if it has
			if ( !MD5.getDigest(buf).equals( sessData.getMD5() ) )
				vals.append("d", buf);
			
			col.updateOne( keys, new Document("$set", vals), new UpdateOptions().upsert(true) );

		}catch(Exception e){
			cfEngine.log( appName + "MongoDBException: " + e );
		}
	}


	public void onExpireAll(cfApplicationData applicationData) {
		col.deleteMany( Filters.lte("et", new Date() ) );
	}
	
	
	public void onApplicationEnd(cfApplicationData applicationData) {
		onExpireAll( applicationData );
	}
	
	
	public int size() {	
		return (int)col.count(); 
	}
	
	
	@Override
	public SessionEngine getType() {
		return SessionStorageFactory.SessionEngine.MONGO;
	}
	
	public String getURI(){
		return uri;
	}
}