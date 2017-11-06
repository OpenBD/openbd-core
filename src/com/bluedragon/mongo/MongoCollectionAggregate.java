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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;

public class MongoCollectionAggregate extends MongoCollectionInsert {
	private static final long serialVersionUID = 1L;

	public MongoCollectionAggregate(){  min = 3; max = 3; setNamedParams( new String[]{ "datasource", "collection", "pipeline" } ); }
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Name previously created using MongoRegister",
			"collection name",
			"an array or single document of pipeline operations"
		};
	}

	public java.util.Map<String, String> getInfo(){
		return makeInfo(
				"mongo", 
				"Performs an aggregated function against the given collection", 
				ReturnType.ARRAY );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		MongoDatabase	db	= getMongoDatabase( _session, argStruct );
		
		String collection	= getNamedStringParam(argStruct, "collection", null);
		if ( collection == null )
			throwException(_session, "please specify a collection");
		
		cfData	data	= getNamedParam(argStruct, "pipeline", null );
		if ( data == null )
			throwException(_session, "please specify a pipeline");

		List<Bson>	pipelineList	= new ArrayList<Bson>();
		
		
		if ( data.getDataType() == cfData.CFARRAYDATA ){
			cfArrayData	arrData	= (cfArrayData)data;
			
			if ( arrData.size() == 0 )
				throwException(_session, "please specify at least one pipeline");

			for ( int x=0; x < arrData.size(); x++ )
				pipelineList.add( getDocument(arrData.getData(x+1)) );

		}else{
			pipelineList.add( getDocument(data) );
		}
		
		
		try{
			MongoCollection<Document> col = db.getCollection(collection);
			long start = System.currentTimeMillis();
			
			// Now we can run the query
			cfArrayData	results	= cfArrayData.createArray(1);
			
			col.aggregate( pipelineList ).forEach( new Block<Document>(){

				@SuppressWarnings( "rawtypes" )
				@Override
				public void apply( Document doc ) {
					try {
						results.addElement( tagUtils.convertToCfData( (Map)doc ) );
					} catch ( cfmRunTimeException e ) {}					
				}
				
			});
	
			_session.getDebugRecorder().execMongo(col, "aggregate", null, System.currentTimeMillis()-start);
			
			return results;
		} catch (Exception e){
			throwException(_session, e.getMessage());
			return null;
		}
	}
}