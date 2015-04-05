/* 
 *  Copyright (C) 2000 - 2014 TagServlet Ltd
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
 *  $Id: MongoCollectionIndexEnsure.java 2426 2014-03-30 18:53:18Z alan $
 */
package com.bluedragon.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class MongoCollectionIndexEnsure extends MongoDatabaseList {
	private static final long serialVersionUID = 1L;

	public MongoCollectionIndexEnsure(){ min = 4; max = 5; setNamedParams( new String[]{ "datasource", "collection", "keys", "name", "unique" } ); }
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Name previously created using MongoRegister",
			"collection",
			"object with a key set of the fields desired for the index",
			"name of the index",
			"flag to determine if this represents a unique, defaults to false"
		};
	}
	
	
	public java.util.Map getInfo(){
		return makeInfo(
				"mongo", 
				"Ensures the given index for the collection exists, or creates it if not",
				ReturnType.BOOLEAN );
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		DB	db	= getDataSource( _session, argStruct );
		String collection	= getNamedStringParam(argStruct, "collection", null);
		if ( collection == null )
			throwException(_session, "please specify a 'collection' parameter");
		
		cfData	keys	= getNamedParam(argStruct, "keys", null );
		if ( keys == null )
			throwException(_session, "please specify 'keys' parameter");
		
		String index	= getNamedStringParam(argStruct, "name", null );
		if ( index == null )
			throwException(_session, "please specify 'index' parameter");
		
		DBObject	options	= new BasicDBObject()
			.append( "name", index )
			.append( "unique", getNamedBooleanParam(argStruct, "unique", false) );
		
		try{
			db.getCollection(collection).createIndex( convertToDBObject(keys), options );
			return cfBooleanData.TRUE;
		} catch (Exception me){
			throwException(_session, me.getMessage());
			return null;
		}
	}
}