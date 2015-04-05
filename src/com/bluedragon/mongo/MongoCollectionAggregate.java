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
 *  $Id: MongoCollectionAggregate.java 2343 2013-03-12 01:41:47Z alan $
 */
package com.bluedragon.mongo;

import java.lang.reflect.Method;
import java.util.Iterator;

import com.mongodb.AggregationOutput;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
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

	public java.util.Map getInfo(){
		return makeInfo(
				"mongo", 
				"Performs an aggregated function against the given collection", 
				ReturnType.ARRAY );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		DB	db	= getDataSource( _session, argStruct );
		
		String collection	= getNamedStringParam(argStruct, "collection", null);
		if ( collection == null )
			throwException(_session, "please specify a collection");
		
		cfData	data	= getNamedParam(argStruct, "pipeline", null );
		if ( data == null )
			throwException(_session, "please specify a pipeline");

		Object[]	args	= new Object[2];
		
		if ( data.getDataType() == cfData.CFARRAYDATA ){
			cfArrayData	arrData	= (cfArrayData)data;
			
			if ( arrData.size() == 0 )
				throwException(_session, "please specify at least one pipeline");

			args[0]	= convertToDBObject( arrData.getData(1) );
			arrData.removeElementAt(1);
			
			DBObject[] dargs	= new DBObject[ arrData.size() ];
			for ( int x=0; x < dargs.length; x++ )
				dargs[x]	= (DBObject)convertToDBObject( arrData.getData(x+1) );
			
			args[1]	= dargs;
		}else{
			args[0]	= convertToDBObject(data);
			args[1]	= new DBObject[]{};
		}
		
		
		try{
			DBCollection col = db.getCollection(collection);
			long start = System.currentTimeMillis();
			
			Method agg	=	col.getClass().getMethod("aggregate", DBObject.class, DBObject[].class );

			AggregationOutput aggOutput = (AggregationOutput)agg.invoke(col, args);

			// Now we can run the query
			cfArrayData	results	= cfArrayData.createArray(1);
			Iterator<DBObject>	it	= aggOutput.results().iterator();
			
			while ( it.hasNext() )
				results.addElement( tagUtils.convertToCfData(it.next().toMap()) );

			_session.getDebugRecorder().execMongo(col, "aggregate", null, System.currentTimeMillis()-start);
			
			return results;
		} catch ( java.lang.reflect.InvocationTargetException ite ){
			throwException(_session, ite.getTargetException().getMessage());
			return null;
		} catch (Exception e){
			throwException(_session, e.getMessage());
			return null;
		}
	}
}