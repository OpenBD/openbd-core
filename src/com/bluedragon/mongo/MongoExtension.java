/*
 *  Copyright (C) 2000 - 2013 AW2.0 Ltd
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
 *  $Id: MongoExtension.java 2416 2013-11-06 16:54:56Z alan $
 */

package com.bluedragon.mongo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.aw20.util.SystemClock;
import org.aw20.util.SystemClockEvent;

import com.bluedragon.plugin.Plugin;
import com.bluedragon.plugin.PluginManager;
import com.bluedragon.plugin.PluginManagerInterface;
import com.bluedragon.plugin.RequestListener;
import com.mongodb.client.MongoDatabase;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.xmlConfig.xmlCFML;


public class MongoExtension implements Plugin, RequestListener, SystemClockEvent {

	@Override public void requestEnd(cfSession session) {}
	@Override public void requestBadFileException(cfmBadFileException bfException, cfSession session) {}
	@Override	public void requestRuntimeException(cfmRunTimeException cfException, cfSession session) {}
	@Override public void requestStart(cfSession session) {}

	@Override	public String getPluginDescription() {
		return getPluginName();
	}

	@Override public String getPluginName() {
		return "MongoExtension";
	}

	@Override public String getPluginVersion() {
		return "1.2015.9.27";
	}

	private static Map<String,MongoDSN>	dbPool	= new HashMap<String,MongoDSN>();
	private static long	DSN_TIMEOUT	= 1000 * 60 * 5;

	@Override	public void pluginStart(PluginManagerInterface manager, xmlCFML systemParameters) {
		manager.addRequestListener( this );

		manager.registerFunction("mongoregister", 				"com.bluedragon.mongo.MongoRegister" );
		manager.registerFunction("mongoderegister", 			"com.bluedragon.mongo.MongoDeRegister" );
		manager.registerFunction("mongoisvalid", 					"com.bluedragon.mongo.MongoIsRegistered" );

		manager.registerFunction("mongodatabaselist", 		"com.bluedragon.mongo.MongoDatabaseList" );
		manager.registerFunction("mongodatabasedrop", 		"com.bluedragon.mongo.MongoDatabaseDrop" );
		manager.registerFunction("mongodatabasestats", 		"com.bluedragon.mongo.MongoDatabaseStats" );
		manager.registerFunction("mongodatabaseruncmd", 	"com.bluedragon.mongo.MongoDatabaseRunCmd" );
		manager.registerFunction("mongoobjectid", 				"com.bluedragon.mongo.MongoObjectId" );

		manager.registerFunction("mongocollectionlist", 		"com.bluedragon.mongo.MongoCollectionList" );
		manager.registerFunction("mongocollectiondrop", 		"com.bluedragon.mongo.MongoCollectionDrop" );
		manager.registerFunction("mongocollectioncount", 		"com.bluedragon.mongo.MongoCollectionCount" );
		manager.registerFunction("mongocollectionrename",		"com.bluedragon.mongo.MongoCollectionRename" );
		manager.registerFunction("mongocollectionstats", 		"com.bluedragon.mongo.MongoCollectionStats" );
		manager.registerFunction("mongocollectiondistinct",	"com.bluedragon.mongo.MongoCollectionDistinct" );

		manager.registerFunction("mongocollectioninsert", 	"com.bluedragon.mongo.MongoCollectionInsert" );
		manager.registerFunction("mongocollectionsave", 		"com.bluedragon.mongo.MongoCollectionSave" );
		manager.registerFunction("mongocollectionupdate", 	"com.bluedragon.mongo.MongoCollectionUpdate" );
		manager.registerFunction("mongocollectionremove", 	"com.bluedragon.mongo.MongoCollectionRemove" );

		manager.registerFunction("mongocollectionfind", 					"com.bluedragon.mongo.MongoCollectionFind" );
		manager.registerFunction("mongocollectionfindone",				"com.bluedragon.mongo.MongoCollectionFindOne" );
		manager.registerFunction("mongocollectionfindandmodify",	"com.bluedragon.mongo.MongoCollectionFindAndModify" );

		manager.registerFunction("mongocollectionindexdrop",			"com.bluedragon.mongo.MongoCollectionIndexDrop" );
		manager.registerFunction("mongocollectionindexensure",		"com.bluedragon.mongo.MongoCollectionIndexEnsure" );

		manager.registerFunction("mongocollectionmapreduce",	"com.bluedragon.mongo.MongoCollectionMapReduce" );
		manager.registerFunction("mongocollectiongroup",			"com.bluedragon.mongo.MongoCollectionGroup" );
		manager.registerFunction("mongocollectionaggregate",	"com.bluedragon.mongo.MongoCollectionAggregate" );

		manager.registerFunction("mongogridfssave", 					"com.bluedragon.mongo.gridfs.Add");
		manager.registerFunction("mongogridfsremove",					"com.bluedragon.mongo.gridfs.Delete");
		manager.registerFunction("mongogridfsfindone",				"com.bluedragon.mongo.gridfs.FindOne");
		manager.registerFunction("mongogridfsfind",						"com.bluedragon.mongo.gridfs.Find");
		manager.registerFunction("mongogridfsget",						"com.bluedragon.mongo.gridfs.Get");

		SystemClock.setListenerMinute(this, 2);
	}


	@Override
	public void pluginStop(PluginManagerInterface manager) {
		Iterator<MongoDSN>	it	= dbPool.values().iterator();
		while (it.hasNext() ){
			MongoDSN db = it.next();
			it.remove();
			db.close();
		}
	}

	public static void remove(String name) {
		synchronized( dbPool ){
			MongoDSN db = dbPool.get(name);
			if ( db != null ){
				db.close();
				dbPool.remove( name );
			}
		}
	}

	public static MongoDSN	getDSN( String name ) throws Exception {
		synchronized( dbPool ){
			MongoDSN	mdsn	= dbPool.get(name.toLowerCase());
			mdsn.lastUsed = System.currentTimeMillis();

			MongoDatabase	db	= mdsn.getDatabase();
			if ( db == null ){
				mdsn.open();
			}

			return mdsn;
		}
	}


	public static boolean isValid(String datasource) {
		synchronized( dbPool ){
			return dbPool.containsKey(datasource.toLowerCase());
		}
	}


	@Override
	public void clockEvent(int arg0) {
		synchronized( dbPool ){
			Iterator<MongoDSN>	it	= dbPool.values().iterator();
			while (it.hasNext() ){
				MongoDSN mdsn = it.next();

				if ( mdsn.getDatabase() != null && (System.currentTimeMillis()-mdsn.lastUsed) > DSN_TIMEOUT ){
					mdsn.close();
					PluginManager.getPlugInManager().log( "MongoDSN Removed: " + mdsn.getDBName() );
				}
			}
		}
	}


	public static void add(String name, String server, int port, String user, String pass, String db) throws Exception {
		synchronized( dbPool ){
			if ( dbPool.containsKey(name.toLowerCase()) )
				throw new Exception( "MongoDSN already exists [" + name.toLowerCase() + "]" );

			dbPool.put( name.toLowerCase(), new MongoDSN(server,port,user,pass,db) );
		}
	}


	public static void add(String name, String mongouri ) throws Exception {
		synchronized( dbPool ){
			if ( dbPool.containsKey(name.toLowerCase()) )
				throw new Exception( "MongoDSN already exists [" + name.toLowerCase() + "]" );

			dbPool.put( name.toLowerCase(), new MongoDSN(mongouri) );
		}
	}

}