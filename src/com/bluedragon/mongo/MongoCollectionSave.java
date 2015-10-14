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

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;


public class MongoCollectionSave extends MongoCollectionList {

	private static final long serialVersionUID = 1L;


	public MongoCollectionSave() {
		min = max = 3;
		setNamedParams( new String[] { "datasource", "collection", "data" } );
	}


	public String[] getParamInfo() {
		return new String[] {
				"datasource name.  Name previously created using MongoRegister",
				"collection name",
				"data to save into the collection.  Can be a single structure, or a JSON string (which will be converted to a structure via Mongo)"
		};
	}


	public java.util.Map<String, String> getInfo() {
		return makeInfo(
				"mongo",
				"Saves the object into mongo, doing an INSERT or an UPDATE depending on the existence of _id",
				ReturnType.BOOLEAN );
	}


	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		MongoDatabase db = getMongoDatabase( _session, argStruct );

		String collection = getNamedStringParam( argStruct, "collection", null );
		if ( collection == null )
			throwException( _session, "please specify a collection" );

		cfData data = getNamedParam( argStruct, "data", null );
		if ( data == null )
			throwException( _session, "please specify data to save" );

		try {
			Document doc = getDocument( data );
			MongoCollection<Document> col = db.getCollection( collection );
			long start = System.currentTimeMillis();

			if ( doc.containsKey( "_id" ) ) {
				col.updateOne( new Document( "_id", doc.get( "_id" ) ), new Document("$set",doc) );
			} else {
				col.insertOne( doc );
			}

			_session.getDebugRecorder().execMongo( col, "save", doc, System.currentTimeMillis() - start );

			return cfBooleanData.TRUE;

		} catch ( Exception me ) {
			throwException( _session, me.getMessage() );
			return null;
		}
	}
}