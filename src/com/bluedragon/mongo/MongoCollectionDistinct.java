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

import com.mongodb.Block;
import com.mongodb.MongoException;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoDatabase;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;


public class MongoCollectionDistinct extends MongoDatabaseList {

	private static final long serialVersionUID = 1L;


	public MongoCollectionDistinct() {
		min = 3;
		max = 4;
		setNamedParams( new String[] { "datasource", "collection", "key", "query" } );
	}


	public String[] getParamInfo() {
		return new String[] {
				"datasource name.  Name previously created using MongoRegister",
				"collection name",
				"key to find",
				"optional query"
		};
	}


	public java.util.Map<String, String> getInfo() {
		return makeInfo(
				"mongo",
				"Finds all the distinct values for the key for the collection against the optional query",
				ReturnType.ARRAY );
	}


	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		MongoDatabase db = getMongoDatabase( _session, argStruct );

		String collection = getNamedStringParam( argStruct, "collection", null );
		if ( collection == null )
			throwException( _session, "please specify a collection" );

		String key = getNamedStringParam( argStruct, "key", null );
		if ( key == null )
			throwException( _session, "please specify a key" );

		cfData query = getNamedParam( argStruct, "query", null );

		try {

			DistinctIterable<String> result;
			if ( query != null )
				result = db.getCollection( collection ).distinct( key, String.class ).filter( getDocument( query ) );
			else
				result = db.getCollection( collection ).distinct( key, String.class );

			cfArrayData arr = cfArrayData.createArray( 1 );

			result.forEach( new Block<String>() {

				@Override
				public void apply( final String st ) {
					try {
						arr.addElement( new cfStringData( st ) );
					} catch ( cfmRunTimeException e ) {}
				}
			} );


			return arr;

		} catch ( MongoException me ) {
			throwException( _session, me.getMessage() );
			return null;
		}
	}
}