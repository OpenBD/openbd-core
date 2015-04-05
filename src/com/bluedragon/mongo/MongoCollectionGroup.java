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
 *  $Id: MongoCollectionGroup.java 1770 2011-11-05 11:50:08Z alan $
 */
package com.bluedragon.mongo;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;

public class MongoCollectionGroup extends MongoCollectionInsert {
	private static final long serialVersionUID = 1L;

	public MongoCollectionGroup(){  min = 6; max = 7; setNamedParams( new String[]{ "datasource", "collection", "key", "cond", "reduce", "initial", "finalize" } ); }
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Name previously created using MongoRegister",
			"collection name",
			"key",
			"condition on query",
			"javascript reduce function",
			"initial value for first match on a key",
			"finalize function in javascript code"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"mongo", 
				"Runs a Group command", 
				ReturnType.STRUCTURE );
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		DB	db	= getDataSource( _session, argStruct );
		
		String collection	= getNamedStringParam(argStruct, "collection", null);
		if ( collection == null )
			throwException(_session, "please specify a collection");
		
		cfData key	= getNamedParam(argStruct, "key", null );
		if ( key == null )
			throwException(_session, "please specify a key");
		
		cfData cond	= getNamedParam(argStruct, "cond", null );
		if ( cond == null )
			throwException(_session, "please specify a cond");
		
		cfData initial	= getNamedParam(argStruct, "initial", null );
		if ( initial == null )
			throwException(_session, "please specify a initial");
		
		String reduce	= getNamedStringParam(argStruct, "reduce", null );
		if ( reduce == null )
			throwException(_session, "please specify a reduce");
		
		String finalize	= getNamedStringParam(argStruct, "finalize", null );
		
		try{
			
			DBCollection col = db.getCollection(collection);
			
			DBObject	result;
			
			if ( finalize != null )
				result = col.group( convertToDBObject(key), convertToDBObject(cond), convertToDBObject(initial), reduce, finalize);
			else
				result = col.group( convertToDBObject(key), convertToDBObject(cond), convertToDBObject(initial), reduce );

			return tagUtils.convertToCfData( result );

		} catch (MongoException me){
			throwException(_session, me.getMessage());
			return null;
		}
	}
}