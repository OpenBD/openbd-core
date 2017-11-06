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

import org.bson.Document;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class MongoCollectionCount extends MongoDatabaseList {
	private static final long serialVersionUID = 1L;

	public MongoCollectionCount(){  min = 2; max = 3; setNamedParams( new String[]{ "datasource", "collection", "query" } ); }
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Name previously created using MongoRegister",
			"collection name",
			"Query to narrow your count to.  Can be a structure of elements, or a raw string that will be passed to Mongo"
		};
	}
	
	
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"mongo", 
				"Counts the number of documents in a collection, filtering with the optional query", 
				ReturnType.NUMERIC );
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		MongoDatabase	db	= getMongoDatabase( _session, argStruct );
		
		String collection	= getNamedStringParam(argStruct, "collection", null);
		if ( collection == null )
			throwException(_session, "please specify a collection");
		
		cfData	queryData	= getNamedParam(argStruct, "query", null );
		
		try{
			Document qry = null;
			int count = 0;
			long start = System.currentTimeMillis();
			MongoCollection<Document> col = db.getCollection(collection);
			
			if ( queryData != null ){
				qry  	= getDocument(queryData);
				count	= (int)col.count( qry ); 
			}else
				count = (int)col.count();
			
			_session.getDebugRecorder().execMongo(col, "count", qry, System.currentTimeMillis()-start);

			return new cfNumberData( count );

		} catch (MongoException me){
			throwException(_session, me.getMessage());
			return null;
		}
	}
}