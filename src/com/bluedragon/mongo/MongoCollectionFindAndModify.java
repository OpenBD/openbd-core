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

import java.util.Map;

import org.bson.Document;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;

public class MongoCollectionFindAndModify extends MongoCollectionInsert {
	private static final long serialVersionUID = 1L;

	public MongoCollectionFindAndModify(){  min = 4; max = 8; setNamedParams( new String[]{ "datasource", "collection", "query", "update", "fields", "sort", "returnnew", "upsert" } ); }
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Name previously created using MongoRegister",
			"collection name",
			"the query to which to find the object to update",
			"update object to apply",
			"fields to return",
			"sort to apply before picking first object",
			"if true, the updated document is returned, otherwise the old document is returned (or it would be lost forever) default=false",
			"do upsert (insert if document not present) default=false"
		};
	}
	
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"mongo", 
				"Finds the first document in the query and updates it", 
				ReturnType.STRUCTURE );
	}
	
	
	@SuppressWarnings( "rawtypes" )
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		MongoDatabase	db	= getMongoDatabase( _session, argStruct );
		
		String collection	= getNamedStringParam(argStruct, "collection", null);
		if ( collection == null )
			throwException(_session, "please specify a collection");
		
		cfData	update	= getNamedParam(argStruct, "update", null );
		if ( update == null )
			throwException(_session, "please specify update");
		
		cfData	query	= getNamedParam(argStruct, "query", null );
		if ( query == null )
			throwException(_session, "please specify query to update");

		try{
			
			MongoCollection<Document> col = db.getCollection(collection);
			FindOneAndUpdateOptions	findOneAndUpdateOptions	= new FindOneAndUpdateOptions();
			
			if ( getNamedParam(argStruct, "fields", null ) != null )
				findOneAndUpdateOptions.projection( getDocument( getNamedParam(argStruct, "fields", null ) ) );

			if ( getNamedParam(argStruct, "sort", null ) != null )
				findOneAndUpdateOptions.sort( getDocument( getNamedParam(argStruct, "sort", null ) ) );

			findOneAndUpdateOptions.upsert( getNamedBooleanParam(argStruct, "upsert", false ) );
			
			if ( getNamedBooleanParam(argStruct, "returnnew", false ) )
				findOneAndUpdateOptions.returnDocument( ReturnDocument.AFTER );
			
			Document qry = getDocument(query);
			long start = System.currentTimeMillis();
			
			Document result = col.findOneAndUpdate( qry, getDocument(update), findOneAndUpdateOptions );
			
			_session.getDebugRecorder().execMongo(col, "findandmodify", qry, System.currentTimeMillis()-start);
			
			return tagUtils.convertToCfData( (Map)result );

		} catch (MongoException me){
			throwException(_session, me.getMessage());
			return null;
		}
	}
}