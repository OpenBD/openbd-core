/*
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: CollectionFactory.java 2523 2015-02-22 16:23:11Z alan $
 */

package com.bluedragon.search.collection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.aw20.util.SystemClockEvent;

import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.xmlConfig.xmlCFML;


public class CollectionFactory extends Object implements SystemClockEvent {
	private static CollectionFactory	thisInst = new CollectionFactory();
	
	private Map<String, Collection>	collectionMap = new HashMap<String, Collection>();
	
	
	/**
	 * Reads the underlying bluedragon.xml file populates the collections
	 * 
	 * @param xmlconfig
	 */
	public static void init(xmlCFML xmlconfig){
		thisInst.loadCollections(xmlconfig);
	}
	
	public static void close(){
		thisInst._close();
	}

	public static String[]	getCollectionNames(){
		return thisInst._getCollectionNames();
	}
	
	private String[] _getCollectionNames() {
		return (String[]) collectionMap.keySet().toArray( new String[]{} );
	}

	public static Collection	getCollection( String name ){
		return thisInst._getCollection(name);
	}
	
	public static void addCollection( Collection col ){
		thisInst._addCollection( col );
	}
	
	public static void deleteCollection( String name ) throws IOException{
		thisInst._deleteCollection( name );
	}
	
	private void _deleteCollection(String name) throws IOException {
		Collection col = collectionMap.get( name.toLowerCase() );
		if ( col != null ){
			col.delete();
			collectionMap.remove( name.toLowerCase() );
		}
	}
	
	public static boolean isCollection( String name ){
		return thisInst._isCollection( name );
	}
	
	private boolean _isCollection(String name){
		return collectionMap.containsKey( name.toLowerCase() );
	}
	
	private void _addCollection(Collection col) {
		collectionMap.put( col.getName().toLowerCase(), col );
	}

	private Collection _getCollection(String name) {
		return collectionMap.get( name.toLowerCase() );
	}


	private CollectionFactory(){
		cfEngine.thisPlatform.timerSetListenerMinute(this, 7);
	}
	
	/**
	 * Called at the start of the engine, and it runs through the collections stored and loads them
	 * 
	 * The Lucene index is _NOT_ opened yet.  Only the first person calling this will do that.
	 * 
	 * @param xmlconfig
	 */
	private void loadCollections(xmlCFML xmlconfig){
		Vector<String> keys = xmlconfig.getKeys("server.cfcollection.collection[]");
		if ( keys != null ){
			Iterator<String>	it	= keys.iterator();
			while ( it.hasNext() ){
				String xmlkey	= it.next();
				
				try {
					Collection	collection	= new Collection();
					
					collection.setName( xmlconfig.getString(xmlkey + ".name") );
					collection.setLanguage( xmlconfig.getString(xmlkey + ".language", "english") );
					collection.setStoreBody( xmlconfig.getBoolean(xmlkey + ".storebody", false ) );
					
					String path	= xmlconfig.getString(xmlkey + ".path");
					if ( path == null || path.length() == 0 )
						throw  new Exception("invalid cfcollection reference: " + xmlkey );
					
					if ( xmlconfig.getBoolean(xmlkey + ".relative", false ) ){
						collection.setDirectory( cfEngine.getResolvedFile(path).getCanonicalPath() );
					}else{
						collection.setDirectory( path );	
					}

					collectionMap.put( collection.getName().toLowerCase(), collection );
					cfEngine.log("Collection Registered: " + collection.getName() + " @ " + collection.getPath() );
				} catch (Exception e) {
					cfEngine.log("CollectionFactory.init(" + xmlkey + "): " + e.getMessage() );
				}
				
			}
			
		}else{
			cfEngine.log("CollectionFactory.init(): no <cfcollection> found in configuration");
		}
	}
	
	
	private void _close(){
		Iterator<Collection>	it	= collectionMap.values().iterator();
		while ( it.hasNext() )
			it.next().close();
		
		collectionMap.clear();
	}

	@Override
	public void clockEvent(int type) { 
		
		Iterator<Collection>	it	= collectionMap.values().iterator();
		while ( it.hasNext() ){
			Collection collection	= it.next();
			
			if ( collection.getTimeSinceLastUsed() > 300000 ){
				collection.closeReader();
			}
			
		}
		
	}
}
