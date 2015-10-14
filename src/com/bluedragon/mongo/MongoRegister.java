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

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class MongoRegister extends functionBase {
	private static final long serialVersionUID = 1L;

	public MongoRegister(){  min = 3; max = 7; setNamedParams( new String[]{ "name", "server", "port", "db", "username", "password", "mongouri" } ); }
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Used to reference this connection",
			"the ip/network of the server",
			"network port of the server. defaults to 27017",
			"database name on Mongo",
			"username of the server",
			"password of the server",
			"the mongo uri (see below).  If used then server/port/db/username/password are ignored/"
		};
	}
	
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"mongo", 
				"Creates a new datasource for Mongo, to be referenced using the 'name' by the other Mongo functions.  If already exists an exception will be thrown.  If the connection has not been used within 5minutes it will be automatically closed and reopened on the next time it is called upon", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String name		= getNamedStringParam(argStruct, "name", null );
		if ( name == null )
			throwException(_session, "please specify a name for your datasource");

		String mongouri	= getNamedStringParam(argStruct, "mongouri", null );
		if ( mongouri == null ){
		
			String server	= getNamedStringParam(argStruct, "server", null );
			if ( server == null )
				throwException(_session, "please specify a server for your mongo");
	
			String db	= getNamedStringParam(argStruct, "db", null );
			if ( db == null )
				throwException(_session, "please specify a db for your mongo");
	
			int	port			= getNamedIntParam(argStruct, "port", 27017 );
			String user		= getNamedStringParam(argStruct, "username", null );
			String pass		= getNamedStringParam(argStruct, "password", null );

			// Create a new DB
			try {
				MongoExtension.add( name, server, port, user, pass, db );
			} catch (Exception e) {
				throwException(_session, e.getMessage() );
			}
			
		}else{
			
			// Create a new DB
			try {
				MongoExtension.add( name, mongouri );
			} catch (Exception e) {
				throwException(_session, e.getMessage() );
			}

		}
		

		return cfBooleanData.TRUE;
	}
}
