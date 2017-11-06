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
 *  $Id: cfQUERY.java 2363 2013-05-01 12:33:11Z andy $
 */

package com.naryx.tagfusion.cfm.sql;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.aw20.util.StringUtil;

import com.naryx.tagfusion.cfm.cache.CacheFactory;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.xmlConfig.xmlCFML;


/**
 * cfQuery implements the CFQUERY tag of ColdFusion. This class
 * implements a number of extra features with respect to the CACHE.
 */
public class cfQUERY extends cfTag implements Serializable {

	static final long serialVersionUID = 1;

	public static final String DATA_BIN_KEY = "CFQUERY_DATA";
	private static final String DEFAULT_CACHE = "CFQUERY";

	// transient public static queryCache activeQueryCache = null;

	transient private static Map<String, cfQueryImplInterface> queryImplementations = new HashMap<String, cfQueryImplInterface>();
	transient private static cfQueryImplInterface defaultImplementation;


	public boolean doesTagHaveEmbeddedPoundSigns() {
		return true;
	}


	public java.util.Map<String, String> getInfo() {
		return createInfo(
				"database",
				"Ability to execute SQL statements against databases, query's or Amazon" );
	}


	@SuppressWarnings( "rawtypes" )
	public java.util.Map[] getAttInfo() {
		return new java.util.Map[] {
				createAttInfo( "DATASOURCE", "The datasource to which the database is tied to.  If not available, defaults to the 'this.datasource' from the Application.cfc", "", false ),

				createAttInfo( "DBTYPE=", "The type of database; default is the SQL, so the DATASOURCE points to a SQL server", "", false ),
				createAttInfo( "DBTYPE=QUERY", "Querying one or more result sets; the DATASOURCE should be blank", "", false ),
				createAttInfo( "DBTYPE=AMAZON", "Querying Amazon SimpleDB; the DATASOURCE is a previously setup Amazon datasource", "", false ),

				createAttInfo( "USERNAME", "The database USERNAME if using an SQL database", "", true ),
				createAttInfo( "PASSWORD", "The database PASSWORD if using an SQL database", "", false ),

				createAttInfo( "BACKGROUND", "If set, will throw this query to the background spooling agent for running later", "", false ),

				createAttInfo( "NAME", "The name of the variable that will receive the query result", "", false ),
				createAttInfo( "PRESERVESINGLEQUOTES", "A flag to preserve the quotes within the CFQUERY body", "false", false ),
				createAttInfo( "MAXROWS", "The maximum number of rows to return; default is to return them all", "", false ),

				createAttInfo( "RESULT", "The variable that will hold the RESULT variable", "", false ),
				createAttInfo( "DEBUG", "Flag to control whether this query is included in the debugging output", "false", false ),

				createAttInfo( "CACHEDWITHIN", "The time span to which this query result will be cached for before it is reexecuted. Use CreateTimeSpan() to get a unit of a day to manage", "", false ),
				createAttInfo( "CACHEDAFTER", "The time when the cache will be expired", "", false ),
				createAttInfo( "CACHEID", "The name of the cache you have given this query. If omitted, then the id will be calculated from the SQL statement plus any arguments passed in", "", false ),
				createAttInfo( "REGION", "The cache region to use", "CFQUERY", false ),
		};
	}


	/*
	 * This method is called once at the start of the engine
	 */
	public static void init( xmlCFML configFile ) {

		// Setup the QUERY implementations
		queryImplementations.put( "query", new cfQueryImplQOQ() );

		cfEngine.thisPlatform.initialiseQuerySystem( configFile, queryImplementations );
		defaultImplementation = cfEngine.thisPlatform.getDefaultQuerySystem( queryImplementations );


		// Setup the Cache size; Default to 200 queries
		int cacheCount = StringUtil.toInteger( configFile.getString( "server.cfquery.cachecount" ), 200 );
		if ( cacheCount > 0 )
			CacheFactory.setMemoryDiskCache( DEFAULT_CACHE, cacheCount, true, 5 );
		else
			cfEngine.log( "QueryCache: Disabled" );
	}


	/*
	 * This method allows PlugIn's to register different implementations for the dbtype
	 */
	public static void registerImplementation( String dbType, cfQueryImplInterface implementation ) {
		queryImplementations.put( dbType.toLowerCase(), implementation );
	}


	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute( "PRESERVESINGLEQUOTES", "NO" );

		parseTagHeader( _tag );

		if ( containsAttribute( "SQL" ) )
			throw invalidAttributeException( "cfquery.unsupportedSQL", null );

		if ( containsAttribute( "CACHEDUNTILCHANGE" ) ) {
			throw invalidAttributeException( "cfquery.invalidCachedUntilChange", null );
		}
	}


	public String getEndMarker() {
		return "</CFQUERY>";
	}


	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		// Determine the DBTYPE type of this tag and switch accordingly
		if ( containsAttribute( "DBTYPE" ) ) {
			String dbType = getDynamic( _Session, "DBTYPE" ).getString().toLowerCase();
			if ( queryImplementations.containsKey( dbType ) )
				return queryImplementations.get( dbType ).render( this, _Session );
			else
				throw newRunTimeException( "You must specify a recognizable DBTYPE" );
		} else if ( defaultImplementation != null ) {
			return defaultImplementation.render( this, _Session );
		} else
			throw newRunTimeException( "You must specify a recognizable DBTYPE" );
	}


	/*
	 * This is the method that will actually execute the statement; it will determine if the cache can be used
	 * and will return back the results accordingly
	 */
	public void executeStatement( cfSQLQueryData queryData, cfSession _Session ) throws cfmRunTimeException {
		// Set the MAX rows
		if ( containsAttribute( "MAXROWS" ) )
			queryData.setMaxRows( getDynamic( _Session, "MAXROWS" ).getInt() );


		// keep a ref to query params for debug recording (the reference queryData holds is set to null once the query has been run.
		List<preparedData> queryParams = queryData.getParams();


		// Run the query
		queryData.runQuery( _Session );


		// Insert result into the session object
		String name = "";
		if ( queryData.hasResultSet() && containsAttribute( "NAME" ) ) {
			name = getDynamic( _Session, "NAME" ).getString();
			_Session.setData( name, queryData );
		}

		// trace recording
		if ( !queryData.getCacheUsed() )
			querySlowLog.record( this, queryData );

		// debug recording
		debugRecord( _Session, name, queryData, queryParams );
		_Session.metricQueryTimeAdd( queryData.getExecuteTime() );

		if ( containsAttribute( "RESULT" ) ) {
			_Session.setData( getDynamic( _Session, "RESULT" ).getString(), createResultVar( queryData ) );
		} else {
			// only create the cfquery.executiontime variable if RESULT isn't used
			cfData d = runTime.runExpression( _Session, "variables['cfquery.executiontime']", false );
			if ( d instanceof com.naryx.tagfusion.cfm.parser.cfLData ) {
				( (com.naryx.tagfusion.cfm.parser.cfLData) d ).Set( new cfNumberData( queryData.getExecuteTime() ), _Session.getCFContext() );
			}
		}
	}


	/*
	 * This determines the inner body
	 */
	@SuppressWarnings( "unchecked" )
	public void renderInnerBody( cfQueryResultData queryData, cfSession _Session ) throws cfmRunTimeException {
		boolean escapeSingleQuotes = !getDynamic( _Session, "PRESERVESINGLEQUOTES" ).getBoolean();

		Stack<cfQueryResultData> queryStack = (Stack<cfQueryResultData>) _Session.getDataBin( DATA_BIN_KEY );
		if ( queryStack == null ) {
			queryStack = new Stack<cfQueryResultData>();
			_Session.setDataBin( DATA_BIN_KEY, queryStack );

		}
		queryStack.push( queryData );
		queryData.setQueryString( renderToString( _Session, ( escapeSingleQuotes ? cfTag.ESCAPE_SINGLE_QUOTES : cfTag.DEFAULT_OPTIONS ) ).getOutput() );
		queryStack.pop();
	}


	/*
	 * Setup the parameters associated with the Query Cache
	 */
	public void setupCache( cfSQLQueryData queryData, cfSession _Session ) throws cfmRunTimeException {

		// Only do SELECT queries for the cache
		if ( queryData.getQueryType() != cfSQLQueryData.SQL_SELECT )
			return;

		// set up query caching
		long expireTime = -1;
		String cacheName = null;
		boolean bUsingCache = false;

		if ( containsAttribute( "CACHENAME" ) ) {
			cacheName = getDynamic( _Session, "CACHENAME" ).getString().toLowerCase();
			bUsingCache = true;
		} else if ( containsAttribute( "CACHEID" ) ) {
			cacheName = getDynamic( _Session, "CACHEID" ).getString().toLowerCase();
			bUsingCache = true;
		}

		if ( containsAttribute( "CACHEDWITHIN" ) ) {
			bUsingCache = true;

			double timeOut = getDynamic( _Session, "CACHEDWITHIN" ).getDouble();
			if ( timeOut > 0 )
				expireTime = (long) ( timeOut * 86400000 );
			else {
				// If they have specified a 0 or a negative then lets assume they are clearing the cache
				expireTime = 0;
			}

		} else if ( containsAttribute( "CACHEDAFTER" ) ) {
			bUsingCache = true;
			expireTime = getDynamic( _Session, "CACHEDAFTER" ).getLong() - System.currentTimeMillis();
		}


		/* Check to see if the cache type has been specified */
		String region = DEFAULT_CACHE;
		if ( containsAttribute( "REGION" ) || containsAttribute( "CACHEREGION" ) ) {

			if ( containsAttribute( "REGION" ) )
				region = getDynamic( _Session, "REGION" ).getString();
			else
				region = getDynamic( _Session, "CACHEREGION" ).getString();

			if ( !CacheFactory.isCacheEnabled( region ) )
				throw newRunTimeException( "Invalid REGION='" + region + "' specified for CFQUERY" );
		}

		if ( bUsingCache )
			queryData.setCacheData( region, expireTime, cacheName );
	}


	public static cfData createResultVar( cfSQLQueryData _query ) throws cfmRunTimeException {
		cfStructData resultStruct = new cfStructData();
		resultStruct.setData( "sql", _query.getQueryString() );
		resultStruct.setData( "recordcount", _query.getUpdatedCount() > 0 ? _query.getUpdatedCount() : _query.getNoRows() );
		resultStruct.setData( "cached", new cfStringData( _query.getCacheUsed() ? "true" : "false" ) );

		if ( _query.hasGeneratedKeys() )
			resultStruct.setData( _query.getGeneratedKeysName(), new cfStringData( _query.getGeneratedKeys() ) );


		List<preparedData> params = _query.getParams();
		if ( params != null ) {
			cfArrayData paramData = cfArrayData.createArray( 1 );
			for ( int i = 0; i < params.size(); i++ )
				paramData.addElement( new cfStringData( params.get( i ).getDataAsString() ) );

			resultStruct.setData( "sqlparameters", paramData );
		}
		resultStruct.setData( "columnlist", _query.getColumns() );
		resultStruct.setData( "executiontime", _query.getExecuteTime() );

		return resultStruct;
	}


	private void debugRecord( cfSession _Session, String _name, cfSQLQueryData _query, List<preparedData> _qryParams ) throws cfmRunTimeException {
		if ( _Session.getShowDBActivity() ) {
			if ( !containsAttribute( "DEBUG" )
					|| getDynamic( _Session, "DEBUG" ).getString().equals( "" )
					|| getDynamic( _Session, "DEBUG" ).getBoolean() ) {
				_Session.recordQuery( getFile(), _name, _query, _qryParams );
			}

		} else if ( containsAttribute( "DEBUG" )
				&& ( getDynamic( _Session, "DEBUG" ).getString().equals( "" )
				|| getDynamic( _Session, "DEBUG" ).getBoolean() ) ) {
			_Session.recordQuery( getFile(), _name, _query, _qryParams );
		}
	}
}