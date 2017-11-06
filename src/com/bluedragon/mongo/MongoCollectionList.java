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

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class MongoCollectionList extends MongoDatabaseList {
	private static final long serialVersionUID = 1L;

	public MongoCollectionList(){  min = 1; max = 1; setNamedParams( new String[]{ "datasource" } ); }
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Name previously created using MongoRegister"
		};
	}
	
	
	public java.util.Map<String, String> getInfo(){
		return makeInfo(
				"mongo", 
				"Lists all the available collections", 
				ReturnType.ARRAY );
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		MongoClient client	= getMongoClient( _session, argStruct );
		
		try{
			cfArrayData	arr	= cfArrayData.createArray(1);
			
			client.listDatabaseNames().forEach( new Block<String>(){

				@Override
				public void apply( String dbName ) {
					try {
						arr.addElement( new cfStringData( dbName ) );
					} catch ( cfmRunTimeException e ) {}
				}
				
			});
			
			
			return arr;
		} catch (MongoException me){
			throwException(_session, me.getMessage());
			return null;
		}
	}
}