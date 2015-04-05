/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  
 *  $Id: MongoCollectionFind.java 2343 2013-03-12 01:41:47Z alan $
 */
package com.bluedragon.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;

public class MongoCollectionFind extends MongoCollectionInsert {
	private static final long serialVersionUID = 1L;

	public MongoCollectionFind(){  min = 3; max = 7; setNamedParams( new String[]{ "datasource", "collection", "query", "fields", "skip", "size", "sort" } ); }
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Name previously created using MongoRegister",
			"collection name",
			"the query to which to find the object to update",
			"fields to which to bring back.  If left blank or not specified, then all fields for the document will be returned",
			"records to skip before starting",
			"number of records to return",
			"sort object"
		};
	}

	public java.util.Map getInfo(){
		return makeInfo(
				"mongo", 
				"Performs a query against Mongo returning back an array of structures of the documents matching the query", 
				ReturnType.ARRAY );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		DB	db	= getDataSource( _session, argStruct );
		
		String collection	= getNamedStringParam(argStruct, "collection", null);
		if ( collection == null )
			throwException(_session, "please specify a collection");
		
		cfData	query	= getNamedParam(argStruct, "query", null );
		if ( query == null )
			throwException(_session, "please specify query");
		
		int	size			= getNamedIntParam(argStruct, "size", -1 );
		int	skip			= getNamedIntParam(argStruct, "skip", -1 );
		cfData sort		= getNamedParam(argStruct, 		"sort", null );
		cfData fields	= getNamedParam(argStruct, 		"fields", null );
		
		try{
			DBCollection col = db.getCollection(collection);

			// Get the initial cursor
			DBCursor	cursor;
			long start = System.currentTimeMillis();
			DBObject qry = convertToDBObject(query);
			
			if ( fields != null )
				cursor = col.find( qry, convertToDBObject(fields) );
			else
				cursor = col.find( qry, new BasicDBObject() );
			
			// Are we sorting?
			if ( sort != null )
				cursor	= cursor.sort( convertToDBObject(sort) );
			
			// Are we limiting
			if ( skip != -1 )
				cursor	= cursor.skip(skip);
			
			// How many we bringing back
			if ( size != -1 )
				cursor = cursor.limit(size);

			// Now we can run the query
			cfArrayData	results	= cfArrayData.createArray(1);
			while ( cursor.hasNext() ){
				results.addElement( tagUtils.convertToCfData(cursor.next().toMap()) );
			}

			_session.getDebugRecorder().execMongo(col, "find", qry, System.currentTimeMillis()-start);
			
			return results;
		} catch (MongoException me){
			throwException(_session, me.getMessage());
			return null;
		}
	}
}