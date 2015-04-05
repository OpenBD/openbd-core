/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  $Id: ProfilerExtension.java 2392 2013-07-01 14:09:12Z alan $
 */
package com.bluedragon.profiler;

import java.io.File;
import java.util.Map;

import org.aw20.collections.ProducerConsumer;

import com.bluedragon.plugin.Plugin;
import com.bluedragon.plugin.PluginManagerInterface;
import com.bluedragon.plugin.RequestListener;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.xmlConfig.xmlCFML;


public class ProfilerExtension implements Plugin, RequestListener, Runnable {	
	public static ProfilerExtension thisInst;
	
	private ProducerConsumer	producerConsumer;
	private String mongourl;
	private boolean bRunning = true;
	private MongoClient mongo;
	private DBCollection coll;

	public ProfilerExtension(){
		thisInst = this;
	}
	
	public String getPluginDescription() {
		return "Profiler";
	}

	public String getPluginName() {
		return "Profiler";
	}

	public String getPluginVersion() {
		String r = "$Revision: 2392 $";
		return "1." + r.substring( 10, r.length()-1 ).trim();
	}

	@Override
	public void pluginStart(PluginManagerInterface manager, xmlCFML systemParameters) {
		manager.registerFunction("profileron",		"com.bluedragon.profiler.functions.On" );
		manager.registerFunction("profileroff",		"com.bluedragon.profiler.functions.Off" );
		manager.registerFunction("profileradd",		"com.bluedragon.profiler.functions.Add" );
		
		File pluginFolder	= new File( cfEngine.thisPlatform.getFileIO().getWorkingDirectory(), "plugin-profiler" );
		producerConsumer	= new ProducerConsumer(pluginFolder, 2000);
	}

	public void pluginStop(PluginManagerInterface manager) {
		bRunning = false;
		thisInst.producerConsumer.onClose();
	}

	@Override
	public void requestStart(cfSession session) {
		new ProfileSession(session);
	}

	public void requestEnd(cfSession session) {}
	public void requestBadFileException(cfmBadFileException bfException, cfSession session) {}
	public void requestRuntimeException(cfmRunTimeException cfException, cfSession session) {}
	
	public static void log(Map m){
		m.put( "dt", new java.util.Date() );
		thisInst.producerConsumer.onProduce(m);
	}
	
	public static void setMongo(String mongourl, String database, String collection) throws Exception {
		if ( thisInst.mongourl != null && !thisInst.mongourl.equals(mongourl)) 
			return;
		
		thisInst.mongourl = mongourl;
		MongoClientURI	m	= new MongoClientURI(thisInst.mongourl);
		
		thisInst.mongo	= new MongoClient( m );
		thisInst.coll		= thisInst.mongo.getDB( database ).getCollection(collection);
		
		Thread t = new Thread(thisInst);
		t.setName("ProfilerExtension:MongoThread");
		t.setPriority( Thread.MIN_PRIORITY );
		t.start();
	}
	
	@Override
	public void run() {
		cfEngine.log("ProfileExtension started logging to " + thisInst.mongourl );
					
		while (bRunning){
			
			Map m = (Map)thisInst.producerConsumer.onConsume();
			if ( m == null ){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}
				continue;
			}
			
			BasicDBObject	dbo	= new BasicDBObject(m);

			try{
				coll.save(dbo, WriteConcern.JOURNAL_SAFE);
			}catch(Exception e){
				thisInst.producerConsumer.unConsume(m);

				try {
					Thread.sleep(1000);
				} catch (InterruptedException te) {}
			}

			Thread.yield();
		}
		
	}
}