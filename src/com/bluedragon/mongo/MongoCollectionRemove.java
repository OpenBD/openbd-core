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
 *  $Id: MongoCollectionRemove.java 1770 2011-11-05 11:50:08Z alan $
 */
package com.bluedragon.mongo;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoException;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class MongoCollectionRemove extends MongoCollectionInsert {
	private static final long serialVersionUID = 1L;

	public MongoCollectionRemove(){  min = 3; max = 4; setNamedParams( new String[]{ "datasource", "collection", "query", "writeconcern" } ); }
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Name previously created using MongoRegister",
			"collection name",
			"query to run. Can be a single structure, or a JSON string (which will be converted to a structure via Mongo)",
			"the mode to save the data: FSYNC_SAFE, JOURNAL_SAFE, MAJORITY, NONE, NORMAL (default), REPLICAS_SAFE, SAFE"
		};
	}
	
	
	public java.util.Map getInfo(){
		return makeInfo(
				"mongo", 
				"Removes the objects that this query will find", 
				ReturnType.BOOLEAN );
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		DB	db	= getDataSource( _session, argStruct );
		
		String collection	= getNamedStringParam(argStruct, "collection", null);
		if ( collection == null )
			throwException(_session, "please specify a collection");
		
		cfData	query	= getNamedParam(argStruct, "query", null );
		if ( query == null )
			throwException(_session, "please specify query to remove");
		
		String writeconcern	= getNamedStringParam(argStruct, "writeconcern", null );
		
		try{
			
			DBCollection col = db.getCollection(collection);
				
			if ( writeconcern == null )
				col.remove( convertToDBObject(query) );
			else
				col.remove( convertToDBObject(query), getConcern(writeconcern) );

			return cfBooleanData.TRUE;

		} catch (MongoException me){
			throwException(_session, me.getMessage());
			return null;
		}
	}
}