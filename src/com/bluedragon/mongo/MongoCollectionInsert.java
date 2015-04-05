/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  $Id: MongoCollectionInsert.java 2426 2014-03-30 18:53:18Z alan $
 */
package com.bluedragon.mongo;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class MongoCollectionInsert extends MongoDatabaseList {
	private static final long serialVersionUID = 1L;

	public MongoCollectionInsert(){  min = 3; max = 4; setNamedParams( new String[]{ "datasource", "collection", "data", "writeconcern" } ); }
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Name previously created using MongoRegister",
			"collection name",
			"data to save into the collection.  Can be a single structure, list of structures, or a JSON string (which will be converted to a structure via Mongo)",
			"the mode to save the data: FSYNC_SAFE, JOURNAL_SAFE, MAJORITY, NORMAL (default), REPLICAS_SAFE, REPLICA_ACKNOWLEDGED, SAFE"
		};
	}
	
	
	public java.util.Map getInfo(){
		return makeInfo(
				"mongo", 
				"Inserts the elements into a collection to Mongo, returning the _id's  of elements that were inserted.  If each structure does not have a _id then one will be automatically assigned and returned", 
				ReturnType.ARRAY );
	}
	
	
	protected WriteConcern	getConcern(String c){
		if ( c == null || c.equalsIgnoreCase("NORMAL") )
			return WriteConcern.NORMAL;
		else if ( c.equalsIgnoreCase("FSYNC_SAFE") )
			return WriteConcern.FSYNC_SAFE;
		else if ( c.equalsIgnoreCase("JOURNAL_SAFE") )
			return WriteConcern.JOURNAL_SAFE;
		else if ( c.equalsIgnoreCase("MAJORITY") )
			return WriteConcern.MAJORITY;
		else if ( c.equalsIgnoreCase("REPLICA_ACKNOWLEDGED") )
			return WriteConcern.REPLICA_ACKNOWLEDGED;
		else if ( c.equalsIgnoreCase("REPLICAS_SAFE") )
			return WriteConcern.REPLICAS_SAFE;
		else if ( c.equalsIgnoreCase("SAFE") )
			return WriteConcern.SAFE;
		else
			return WriteConcern.NORMAL;
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		DB	db	= getDataSource( _session, argStruct );
		
		String collection	= getNamedStringParam(argStruct, "collection", null);
		if ( collection == null )
			throwException(_session, "please specify a collection");
		
		cfData	data	= getNamedParam(argStruct, "data", null );
		if ( data == null )
			throwException(_session, "please specify data to insert");
		
		String writeconcern	= getNamedStringParam(argStruct, "writeconcern", null );
		
		try{
			
			DBCollection col = db.getCollection(collection);
			
			if ( data.getDataType() == cfData.CFARRAYDATA ){

				cfArrayData	idArr	= cfArrayData.createArray(1);
				List<DBObject> list	= new ArrayList<DBObject>();
				cfArrayData	arrdata	= (cfArrayData)data;
				for ( int x=0; x < arrdata.size();  x++ ){
					BasicDBObject dbo = convertToDBObject(arrdata.getData(x+1));
					idArr.addElement( new cfStringData( String.valueOf(dbo.get("_id")) ) );
					list.add( dbo );
				}

				long start = System.currentTimeMillis();
				col.insert( list, getConcern(writeconcern) );
				_session.getDebugRecorder().execMongo(col, "insert", null, System.currentTimeMillis()-start);
				
				return idArr;
				
			} else {
				
				BasicDBObject dbo = convertToDBObject(data);
				
				long start = System.currentTimeMillis();
				col.insert( dbo, getConcern(writeconcern) );
				_session.getDebugRecorder().execMongo(col, "insert", null, System.currentTimeMillis()-start);
				
				return new cfStringData( String.valueOf(dbo.get("_id")) );
			}

		} catch (MongoException me){
			throwException(_session, me.getMessage());
			return null;
		}
	}
}