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
 *  $Id: MongoDatabaseList.java 2325 2013-02-09 19:07:29Z alan $
 */
package com.bluedragon.mongo;

import java.util.Iterator;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoException;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;
import com.naryx.tagfusion.expression.function.string.serializejson;

public class MongoDatabaseList extends functionBase {
	private static final long serialVersionUID = 1L;

	public MongoDatabaseList(){  min = 1; max = 1; setNamedParams( new String[]{ "datasource" } ); }
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Name previously created using MongoRegister"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"mongo", 
				"Lists all the available databases", 
				ReturnType.ARRAY );
	}
	
	protected DB getDataSource(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String datasource = getNamedStringParam(argStruct, "datasource", null );
		if ( datasource == null )
			throwException(_session, "please specify a datasource");

		try{
			DB	db	= (DB)MongoExtension.get( datasource.toLowerCase() );
			if ( db == null )
				throwException(_session, "Mongo datasource [" + datasource + "] could not be found.  Did you register it?");

			return db;
		}catch(Exception e){
			throwException(_session, "Mongo datasource [" + datasource + "] " + e.getMessage() );
			return null;
		}
	}
	
	
	
	/**
	 * Converts the java object into a BasicDBObject; recursive
	 * @throws cfmRunTimeException 
	 */
	protected BasicDBObject	convertToDBObject(cfData d) throws cfmRunTimeException{
		if ( d.getDataType() == cfData.CFSTRUCTDATA ){
			StringBuilder buffer = new StringBuilder(1024);
			new serializejson().encodeJSON(buffer, d, false, serializejson.CaseType.MAINTAIN, serializejson.DateType.MONGO );
			return (BasicDBObject)com.mongodb.util.JSON.parse(buffer.toString());
		}else
			return (BasicDBObject)com.mongodb.util.JSON.parse(d.getString());
	}
	
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		DB	db	= getDataSource( _session, argStruct );
		
		try{
			List<String> names = db.getMongo().getDatabaseNames();
			
			cfArrayData	arr	= cfArrayData.createArray(1);
			
			Iterator<String>	it	= names.iterator();
			while ( it.hasNext() ){
				arr.addElement( new cfStringData( it.next() ) );
			}
			
			return arr;
		} catch (MongoException me){
			throwException(_session, me.getMessage());
			return null;
		}
	}
}
