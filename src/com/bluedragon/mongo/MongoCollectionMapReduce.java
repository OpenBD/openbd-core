/* 
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
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

import org.bson.Document;

import com.mongodb.MongoException;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.MapReduceAction;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class MongoCollectionMapReduce extends MongoCollectionInsert {
	private static final long serialVersionUID = 1L;

	public MongoCollectionMapReduce(){  min = 5; max = 8; setNamedParams( new String[]{ "datasource", "collection", "map", "reduce", "outputcollection", "type", "query", "finalize" } ); }
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Name previously created using MongoRegister",
			"collection name",
			"map function in javascript code",
			"reduce function in javascript code",
			"name of the collection to put the results",
			"the type of output; MERGE (Merge the job output with the existing contents of outputTarget collection), REDUCE (Save the job output to a collection, replacing its previous content), REPLACE (Reduce the job output with the existing contents of outputTarget collection) (default REPLACE)",
			"query to use on input",
			"finalize function in javascript code"
		};
	}
	
	public java.util.Map<String, String> getInfo(){
		return makeInfo(
				"mongo", 
				"Runs a Map Reduce command", 
				ReturnType.BOOLEAN );
	}
		
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		MongoDatabase	db	= getMongoDatabase( _session, argStruct );
		
		String collection	= getNamedStringParam(argStruct, "collection", null);
		if ( collection == null )
			throwException(_session, "please specify a collection");
		
		String map	= getNamedStringParam(argStruct, "map", null );
		if ( map == null )
			throwException(_session, "please specify a map");
		
		String reduce	= getNamedStringParam(argStruct, "reduce", null );
		if ( reduce == null )
			throwException(_session, "please specify a reduce");
		
		String outputcollection	= getNamedStringParam(argStruct, "outputcollection", null );
		if ( outputcollection == null )
			throwException(_session, "please specify a outputcollection");
		
		String action		= getNamedStringParam(argStruct, "type", "replace" ).toLowerCase();
		String finalize	= getNamedStringParam(argStruct, "finalize", null );
		cfData	query		= getNamedParam(argStruct, "query", null );
		
		try{
			MapReduceIterable<Document>	mi	= db.getCollection( collection ).mapReduce( map, reduce );
			
			if ( query != null )
				mi.filter( getDocument( query ) );
			
			if ( finalize != null )
				mi.finalizeFunction( finalize );
			
			mi.collectionName( outputcollection );
			mi.action( MapReduceAction.valueOf( action ) );
			
			
			// Kick start the map reduce
			mi.first();
			
			return cfBooleanData.TRUE;

		} catch (MongoException me){
			throwException(_session, me.getMessage());
			return null;
		}
	}
}