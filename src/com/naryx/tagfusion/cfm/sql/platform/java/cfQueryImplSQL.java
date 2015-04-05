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
 *  http://www.openbluedragon.org/
 *  $Id: cfQueryImplSQL.java 2327 2013-02-10 22:26:44Z alan $
 */


package com.naryx.tagfusion.cfm.sql.platform.java;

import java.util.Enumeration;
import java.util.Vector;

import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.sql.cfDataSource;
import com.naryx.tagfusion.cfm.sql.cfDataSourceStatus;
import com.naryx.tagfusion.cfm.sql.cfDynamicDataSource;
import com.naryx.tagfusion.cfm.sql.cfQUERY;
import com.naryx.tagfusion.cfm.sql.cfQueryImplInterface;
import com.naryx.tagfusion.cfm.sql.cfSQLQueryData;
import com.naryx.tagfusion.cfm.sql.querySlowLog;
import com.naryx.tagfusion.cfm.sql.pool.WrappedConnection;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.xmlConfig.xmlCFML;


/*
 * This class provides the necessary implementation specific
 * for providing the hook to basic SQL feature set.
 */
public class cfQueryImplSQL extends Object implements cfQueryImplInterface {
	static final long serialVersionUID = 1;
	
	private queryBatchServer queryServer;
	
	public cfQueryImplSQL(){}
	
	public void init( xmlCFML configFile ){
		
		// Setup the Batch Server [will move to the SQLImp]
		queryServer = new queryBatchServer(configFile);
		querySlowLog.init( configFile );

		// Set the DEFAULT Validation
		boolean bValidate = configFile.getBoolean( "server.cfquery.validate", true );
		WrappedConnection.setValidateConnections( bValidate );
		cfEngine.log( "CFQUERY Connection pool validation queries: " + ( bValidate ? "ENABLED" : "DISABLED" ) );
		
		
		// set the status of all connections to "unknown"
		Vector<String> V = configFile.getKeys( "server.cfquery.datasource[]" );
		if ( V != null ){
			Enumeration<String> E = V.elements();
			while ( E.hasMoreElements() ){
				String key = E.nextElement();

				// Remove the status values from their old BD 6.1 and earlier location (if present)
				if (configFile.getString(key + ".success") != null)
					configFile.removeData( key + ".success" );
				
				if (configFile.getString(key + ".errormessage") != null)
					configFile.removeData(key + ".errormessage");

				int begin = key.indexOf( '[' );
				if ( begin != - 1 ){
					int end = key.indexOf( ']', begin );
					if ( end != - 1 ){
						String dsName = key.substring( begin+1, end );
						cfEngine.getDataSourceStatus().put( dsName, new cfDataSourceStatus() );
					}
				}
			}
		}
	}
	
	
	
	public cfTagReturnType render( cfQUERY tag, cfSession _Session ) throws cfmRunTimeException {
	
		//Setup the main datasource and querydata objects
		cfDataSource 	dataSource = getDataSource( tag, _Session );
		cfSQLQueryData	queryData	= new cfSQLQueryData( dataSource );
		queryData.setRetrieveGeneratedKeys( tag.containsAttribute("RESULT") );
		
		//Determine the inner SQL
		tag.renderInnerBody( queryData, _Session );
		
    
		//Determine if this is a BACKGROUND query; we only want to background non SELECT statements, non QoQ queries
    if ( tag.containsAttribute("BACKGROUND") && (queryData.getQueryType() != cfSQLQueryData.SQL_SELECT) && tag.getDynamic(_Session, "BACKGROUND").getBoolean() ){
    	prepareBackgroundExecution( dataSource, queryData );
    	return cfTagReturnType.NORMAL;
    }

		
    //Setup any cache information for this
    tag.setupCache( queryData, _Session );
		
    
		//Execute the query
		tag.executeStatement( queryData, _Session );

    
		return cfTagReturnType.NORMAL;
	}
	
	
	
	/*
	 * This query will be run at a later date, managed by a single process.  We prepare
	 * this for later
	 */
	private void prepareBackgroundExecution(cfDataSource dataSource, cfSQLQueryData	queryData ){
    queryBatchSQL   newSql  = new queryBatchSQL();
    
    newSql.setDatasourceName( dataSource.getDataSourceName() );
    newSql.setDatasourceUser( dataSource.getUsername() );
    newSql.setDatasourcePass( dataSource.getPassword() );
    newSql.setQueryParams( queryData.getParams() );
    newSql.setSqlString( queryData.getQueryString() );

    queryServer.acceptSQL( newSql );
	}
	
	
	
	/*
	 * This method gets the datasource for this query, based on the dbtype; this lets us determine
	 * if the datasource is to be created dynamically or we simply use one that is already part of
	 * the underlying engine 
	 */
	private cfDataSource getDataSource( cfQUERY tag, cfSession _Session ) throws cfmRunTimeException {
		boolean dynamicDatasource = false;
		
		
		if ( tag.containsAttribute( "DBTYPE" ) ){
			String dbType	= tag.getDynamic( _Session, "DBTYPE" ).getString().toLowerCase();
			if ( dbType.equals("dynamic") ) {
				if ( !tag.containsAttribute("CONNECTSTRING") )
					throw tag.newRunTimeException( "You must specify the CONNECTSTRING attribute with DBTYPE=\"DYNAMIC\"" );
				
				dynamicDatasource = true;
			}
		}

		
		cfDataSource 	dataSource = null;
		if ( dynamicDatasource )
			dataSource = new cfDynamicDataSource( "DYNAMIC", _Session, tag.getDynamic(_Session, "CONNECTSTRING").getString());
		else{
			
			String datasource	 = getDataSourceValue( tag, _Session );
			if ( datasource == null )
				throw tag.newRunTimeException( "You must specify either the DATASOURCE attribute or 'this.datasource' in the Application.cfc setup" );
			
			dataSource = new cfDataSource( datasource, _Session );
		}

		// Set any additional parameters
		if ( tag.containsAttribute("USERNAME") )
			dataSource.setUsername( tag.getDynamic(_Session, "USERNAME").getString() );

		if ( tag.containsAttribute("PASSWORD") )
			dataSource.setPassword( tag.getDynamic(_Session, "PASSWORD").getString() );

		return dataSource;
	}
	
	
	
	/**
	 * Gets the datasource value from first the TAG attribute, and if that does not exist, then from the application data
	 * @param tag
	 * @param _Session
	 * @return
	 * @throws cfmRunTimeException
	 */
	private String	getDataSourceValue( cfQUERY tag, cfSession _Session ) throws cfmRunTimeException {
		
		if ( tag.containsAttribute("DATASOURCE") )
			return tag.getDynamic(_Session, "DATASOURCE").getString();
		else{
			cfApplicationData appData = _Session.getApplicationData();
			if ( appData != null )
				return appData.getDataSource();
		}
		
		return null;
	}
}
