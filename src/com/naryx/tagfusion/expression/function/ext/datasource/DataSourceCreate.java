/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  $Id: DataSourceCreate.java 2511 2015-02-10 02:19:11Z alan $
 */
package com.naryx.tagfusion.expression.function.ext.datasource;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.sql.cfDataSourceDetails;
import com.naryx.tagfusion.cfm.sql.cfDataSourceDetailsFactory;
import com.naryx.tagfusion.expression.function.functionBase;


/**
 * Checks to see if a DataSource is valid
 */
public class DataSourceCreate extends functionBase {
	private static final long serialVersionUID = 1L;
	private static String[] requiredKeys = {"hoststring","drivername","databasename"}; 
	
	public DataSourceCreate() {
		min = max = 2;
		setNamedParams( new String[]{ "datasource", "details" } );
	}

	public String[] getParamInfo(){
		return new String[]{
				"datasource name to create",	
				"struct containing datasource information Keys=[hoststring, drivername, databasename, username, password, (optional)(logintimeout [seconds that will wait until a connection from the pool becomes available default=30], connectiontimeout [seconds it will attempting to make a connection default=30], maxconnections [total max connections allowed at any point in time default=3], maxlivetime [seconds that a connection can remain active for before it is removed from the pool default=360],  maxusage [number of times this connection will be reused before manually being closed default=1000], connectionretries [number of times it will attempt to reconnect default=0], initstring, connectionperpage [disables system pool and uses page-pool instead.  new connection per page]) ]"
		};
	}
	
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"query", 
				"Adds a new datasource to the system for use with any database functions.  This does not persist over server restarts", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String datasource = getNamedStringParam(argStruct, "datasource", null);
		cfData data 			= getNamedParam( argStruct, "details" );

		if ( data.getDataType() != cfData.CFSTRUCTDATA )
			throwException(_session, "You must pass in a STRUCTURE with all the elements for the datasource" );
		

		cfEngine.dataSourcePoolFactory.deleteDataSource(datasource);
		cfDataSourceDetailsFactory.add( datasource, getDataSourceDetails( _session, datasource, (cfStructData)data ) );
		return cfBooleanData.TRUE;
	}
	
	private cfDataSourceDetails getDataSourceDetails( cfSession _session, String datasource, cfStructData data ) throws cfmRunTimeException  {
		// Check the keys
		for ( int x=0; x < requiredKeys.length; x++ ){
			if ( !data.containsKey( requiredKeys[x] ) ) {
				throwException(_session, "You are missing one of the core parameters (hoststring, drivername, databasename, username, password)" );
			}
		}


		// Default the ones that are not important
		if ( !data.containsKey("logintimeout") )
			data.setData("logintimeout", new cfNumberData(30) );
		
		if ( !data.containsKey("connectiontimeout") )
			data.setData("connectiontimeout", new cfNumberData(30) );
		
		if ( !data.containsKey("maxconnections") )
			data.setData("maxconnections", new cfNumberData(3) );
		
		if ( !data.containsKey("maxlivetime") )
			data.setData("maxlivetime", new cfNumberData( 60*5 ) );
		
		if ( !data.containsKey("maxusage") )
			data.setData("maxusage", new cfNumberData( 1000 ) );
		
		if ( !data.containsKey("connectionretries") )
			data.setData("connectionretries", new cfNumberData( 0 ) );
		
		if ( !data.containsKey("initstring") )
			data.setData("initstring", new cfStringData("") );
		
		if ( !data.containsKey("connectionperpage") )
			data.setData("connectionperpage", cfBooleanData.FALSE );
		
		if ( !data.containsKey("username") )
			data.setData("username", cfStringData.EMPTY_STRING );
		
		if ( !data.containsKey("password") )
			data.setData("password", cfStringData.EMPTY_STRING );
		
		return new cfDataSourceDetails( datasource, data );
	}
}
