/*
 *  Copyright (C) 2000 - 2015 TagServlet Ltd
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
 */
package com.bluedragon.mongo;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.spy.memcached.AddrUtil;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

public class MongoDSN extends Object {

	private	static Map<String,MongoClientWrapper>	clientMongoMap = new HashMap<String,MongoClientWrapper>();

	public long lastUsed;
	private MongoDatabase mdb;
	private MongoClient mongoclient;

	private String server, user, pass, db, mongouri = null, ip;
	private int port;

	public MongoDSN( String mongouri ){
		this.mongouri = mongouri;
		this.mdb			= null;
	}

	public MongoDSN( String server, int port, String user, String pass, String db ) throws UnknownHostException{
		this.db				= db;
		this.server		= server;
		this.user			= user;
		this.pass			= pass;
		this.port			= port;
		this.ip				= Inet4Address.getByName( server ).getHostAddress();
		this.mongouri	= null;
		this.mdb			= null;
	}


	public synchronized void open() throws Exception {
		

		if ( mongouri == null ){

			if ( clientMongoMap.containsKey( ip + port ) ){
				mdb	= clientMongoMap.get( ip + port ).open().getDatabase( db );
			}else{
				mongoclient	= newClient( server + ":" + port, user, pass, db );
				MongoClientWrapper mcw = new MongoClientWrapper(mongoclient);
				clientMongoMap.put(  ip + port, mcw );
				mdb	= mcw.open().getDatabase( db );
			}

		}else{
			MongoClientURI clientURI = new MongoClientURI(mongouri);
			mongoclient	= new MongoClient( clientURI );
			mdb		= mongoclient.getDatabase( clientURI.getDatabase() );
		}

		lastUsed	= System.currentTimeMillis();
	}


	public synchronized void close(){
		if ( mdb != null ){

			if ( mongouri == null ){
				clientMongoMap.get( ip + port ).close();

				if ( clientMongoMap.get( ip + port ).getUsageCount() == 0 )
					clientMongoMap.remove( ip + port );

				mdb = null;
				mongoclient = null;
			}else{
				mongoclient.close();
				mongoclient = null;
			}

		}
	}

	public String getDBName(){
		return db;
	}

	public MongoDatabase getDatabase(){
		return mdb;
	}
	
	public MongoClient getClient(){
		return mongoclient;
	}
	
	
	public static MongoClient	newClient(String server, String user, String pass, String db) throws UnknownHostException{
	
		MongoClientOptions options = MongoClientOptions
				.builder()
				.readPreference( ReadPreference.secondaryPreferred() )
				.build();

		List<InetSocketAddress> serverList = AddrUtil.getAddresses(server);
		List<ServerAddress> addrs = new ArrayList<ServerAddress>();
			
		Iterator<InetSocketAddress>	it	= serverList.iterator();
		while ( it.hasNext() ){
			InetSocketAddress	isa	= it.next();
			addrs.add( new ServerAddress( isa.getAddress(), isa.getPort() ) );
		}
		
		
		if ( user != null ) {
			MongoCredential cred = MongoCredential.createCredential( user, db, pass.toCharArray() );
			List<MongoCredential> creds = new ArrayList<MongoCredential>();
			creds.add( cred );

			return new MongoClient( addrs, creds, options );
		} else {
			return new MongoClient( addrs, options );
		}

		
	}
	
}