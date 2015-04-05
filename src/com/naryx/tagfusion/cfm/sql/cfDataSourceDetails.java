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
 *  $Id: cfDataSourceDetails.java 2327 2013-02-10 22:26:44Z alan $
 */

/*
 * This class holds the actual DATASOURCE details as per the XML file.
 */
package com.naryx.tagfusion.cfm.sql;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.sql.pool.J2EEDataSourceFactory;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class cfDataSourceDetails extends Object {

	protected String DataSourceName;
	protected String drivername;
	protected String catalog;
  
	String hoststring="";
	String username="", password="";
	
	String initString=null;
  
	boolean sql_select, sql_insert, sql_delete, sql_update, sql_storedprocedures, perRequestConnections;
	int logintimeout, connectiontimeout, limitconnections, maxUsage, maxLiveTime, connectionRetries;
	DataSource j2eeDataSource = null;
	
	private boolean bUnlimitedPool = false;
	
	private static boolean limitDrivers = false;
	public static void setLimitDrivers( boolean limit ) {limitDrivers = limit;}
	public static boolean isLimitDrivers() {return limitDrivers;}
	

	// This constructor is only used by a dynamic data source
	public cfDataSourceDetails(){}

	public cfDataSourceDetails( String _DataSource, cfStructData	sdata ) throws cfmRunTimeException {
		DataSourceName 	= _DataSource.toUpperCase();
		
		hoststring 			= sdata.getData("hoststring").getString();
		drivername			= sdata.getData("drivername").getString();
		
		
		/* Register the driver */
		try{
	  	cfEngine.dataSourcePoolFactory.registerDriver( drivername );
		}catch(SQLException E){
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE, "errorCode.sqlError", 
																			   "sql.invalidJDBCDriver", 
																			   new String[]{com.naryx.tagfusion.cfm.tag.tagUtils.trimError(E.getMessage())},
																			   drivername) );
		}
		
		
		catalog 					= sdata.getData("databasename").getString();
		username 					=	sdata.getData("username").getString();
		password 					= sdata.getData("password").getString();

		logintimeout 			= sdata.getData("logintimeout").getInt();
		connectiontimeout = sdata.getData("connectiontimeout").getInt();
		limitconnections 	= sdata.getData("maxconnections").getInt();
		
		maxLiveTime   		= sdata.getData("maxlivetime").getInt();
		maxUsage  				= sdata.getData("maxusage").getInt();
		
		initString 				= sdata.getData("initstring").getString();
		connectionRetries = sdata.getData("connectionretries").getInt();
		
		bUnlimitedPool		= sdata.getData("connectionperpage").getBoolean();
		
		// Default
		sql_select 						= true;
		sql_insert 						= true;
		sql_delete 						= true;
		sql_update 						= true;
		sql_storedprocedures 	= true;
		perRequestConnections = false;
	}
	
	public cfDataSourceDetails( String _DataSource ) throws cfmRunTimeException {
		DataSourceName	= "server.cfquery.datasource[" + _DataSource.toLowerCase() + "]";
		xmlCFML config 	= cfEngine.getConfig();
		
		hoststring 	= config.getString( DataSourceName + ".hoststring" );
		drivername 	= config.getString( DataSourceName + ".drivername" );
		catalog 		= config.getString( DataSourceName + ".databasename" );
    	
		if ( hoststring == null || drivername == null || catalog == null ){
			// check for a configured J2EE datasource
			sql_select = true;
			sql_insert = true;
			sql_delete = true;
			sql_update = true;
			sql_storedprocedures = true;
			perRequestConnections = false;
							
			j2eeDataSource = J2EEDataSourceFactory.getJ2eeDataSource( _DataSource );			
			username = null;
			password = null;

		}else{
			if ( ( limitDrivers )
					&& !drivername.equals( "sun.jdbc.odbc.JdbcOdbcDriver" )
					&& !drivername.equals( "org.gjt.mm.mysql.Driver" )
				  && !drivername.equals( "com.mysql.jdbc.Driver" )
				  && !drivername.equals( "org.postgresql.Driver" ) ) {
				throw new cfmRunTimeException( catchDataFactory.generalException(	cfCatchData.TYPE_DATABASE, 
																				"errorCode.sqlError", 
																				"sql.invalidDatasource", 
																				new String[]{_DataSource} ) );
			}
    	
			username 		= config.getString( DataSourceName + ".username", "" );
			password 		= config.getString( DataSourceName + ".password", "" );
	    
			sql_select 	= config.getBoolean( DataSourceName + ".sqlselect", true );
			sql_insert 	= config.getBoolean( DataSourceName + ".sqlinsert", true );
			sql_delete 	= config.getBoolean( DataSourceName + ".sqldelete", true );
			sql_update 	= config.getBoolean( DataSourceName + ".sqlupdate", true );
			sql_storedprocedures = config.getBoolean( DataSourceName + ".sqlstoredprocedures", true );
	   
			bUnlimitedPool		= config.getBoolean( DataSourceName + ".connectionperpage", false );
			
			logintimeout 			= config.getInt( DataSourceName + ".logintimeout", 30 );
			connectiontimeout = config.getInt( DataSourceName + ".connectiontimeout", 30 );
			limitconnections 	= config.getInt( DataSourceName + ".maxconnections", 3 );
			
			// default 30minutes
			maxLiveTime   		= config.getInt( DataSourceName + ".maxlivetime", 60 * 30 );
			maxUsage  				= config.getInt( DataSourceName + ".maxusage", 1000 );
			
			initString 				= config.getString( DataSourceName + ".initstring" );
			
			connectionRetries = config.getInt( DataSourceName + ".connectionretries", 0 );
			
			// for MySQL, reuse the same connection per datasource throughout the request by default (see bug #2670);
			// for all other databases don't reuse the same connection by default
			boolean perRequestConnectionsDefault = ( drivername.equals( "org.gjt.mm.mysql.Driver" ) || drivername.equals( "com.mysql.jdbc.Driver" ) );
			perRequestConnections = config.getBoolean(DataSourceName + ".perrequestconnections", perRequestConnectionsDefault);

			
			// Register the driver
			try{
				cfEngine.dataSourcePoolFactory.registerDriver( drivername );
			}catch(Exception E){
				throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE, "errorCode.sqlError", 
																				   "sql.invalidJDBCDriver", 
																				   new String[]{com.naryx.tagfusion.cfm.tag.tagUtils.trimError(E.getMessage())},
																				   drivername) );
			}
		}

		DataSourceName = _DataSource.toUpperCase();	
	}
	
	public String getKey(){
		if ( username != null && password != null )
			return DataSourceName	+ ":" + username + "@" + password;
		else
			return DataSourceName;
	}

	public DataSource getJ2EEDataSource(){
		return j2eeDataSource;
	}

	public String getCatalog() {
		return catalog;
	}

	public int getConnectiontimeout() {
		return connectiontimeout;
	}

	public String getDataSourceName() {
		return DataSourceName;
	}

	public String getDrivername() {
		return drivername;
	}

	public String getHoststring() {
		return hoststring;
	}

	public String getInitString() {
		return initString;
	}

	public int getLimitconnections() {
		return limitconnections;
	}

	public int getLogintimeout() {
		return logintimeout;
	}

	public String getPassword() {
		return password;
	}

	public boolean isSql_delete() {
		return sql_delete;
	}

	public boolean isSql_insert() {
		return sql_insert;
	}

	public boolean isSql_select() {
		return sql_select;
	}

	public boolean isSql_storedprocedures() {
		return sql_storedprocedures;
	}

	public boolean isSql_update() {
		return sql_update;
	}

	public String getUsername() {
		return username;
	}

	public int getMaxLiveTime(){
		return maxLiveTime;
	}
	
	public int getMaxUsage(){
		return maxUsage;
	}
	
	public int getConnectionRetries(){
		return connectionRetries;
	}

	public String toString(){
		if ( j2eeDataSource == null ){
			return 	"DatabaseName        = " + catalog + "\r\n" + 
					"hoststring          = " + hoststring + "\r\n" +
					"Driver Name         = " + drivername + "\r\n" +
					"sql_select          = " + sql_select + "\r\n" +
					"sql_insert          = " + sql_insert + "\r\n" +
					"sql_delete          = " + sql_delete + "\r\n" +
					"sql_update          = " + sql_update + "\r\n" +
					"sql_storedprocedures= " + sql_storedprocedures + "\r\n" +
					"logintimeout        = " + logintimeout + "\r\n" +
					"connectiontimeout   = " + connectiontimeout + "\r\n" +
					"maxUsage            = " + maxUsage + "\r\n" +
					"maxLiveTime         = " + maxLiveTime + "\r\n" +
					"limitconnections    = " + limitconnections + "\r\n" +
					"connectionretries   = " + connectionRetries + "\r\n" +
					"connectionperpage   = " + bUnlimitedPool + "\r\n" +
					"initString          = " + initString + "\r\n";
		}else{
			return 	"J2EE DataSource\r\n" + 
						  "sql_select          = " + sql_select + "\r\n" +
							"sql_insert          = " + sql_insert + "\r\n" +
							"sql_delete          = " + sql_delete + "\r\n" +
							"sql_update          = " + sql_update + "\r\n" +
							"sql_storedprocedures= " + sql_storedprocedures + "\r\n";
		}
	}
	
	
	public boolean isUnlimitedPool() {
		return bUnlimitedPool;
	}
}
