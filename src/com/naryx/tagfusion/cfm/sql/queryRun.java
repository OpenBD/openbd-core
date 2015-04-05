/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: queryRun.java 2210 2012-07-26 23:51:20Z alan $
 */

package com.naryx.tagfusion.cfm.sql;

import java.util.ArrayList;
import java.util.List;

import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.cache.CacheFactory;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.expression.function.functionBase;

public class queryRun extends functionBase {
	private static final long serialVersionUID = 1L;

	public queryRun() {
		min = 1;
		max = 6;
		
		setNamedParams( new String[]{ "datasource", "sql", "params", "cachedwithin", "id", "region" } );
	}

	public String[] getParamInfo() {
		return new String[] { 
				"name of the datasource, if omitted, then it will be pulled from the Application.cfc ('this.datasource').  If omitted, then pleased used named parameters for this function call", 
				"SQL string", 
				"array of structures {value, padding, scale, maxlength, separator, list, defaultlist, nullvalue, cfsqltype} representing the attributes of CFQUERYPARAM; one for each ? within the SQL string",
				"time span to which this query result will be cached for before it is reexecuted. Use CreateTimeSpan() to get a unit of a day to manage",
				"name of the cache you have given this query. If omitted, then the id will be calculated from the SQL statement plus any arguments passed in",
				"cache region to use (defaults to 'cfquery')"
		};
	}

	public java.util.Map<String,String> getInfo() {
		return makeInfo("query", "Executes the given SQL query against the given datasource, optionally passing in paramters.  Function version of CFQUERY", ReturnType.QUERY);
	}

	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		String datasource = getNamedStringParam( argStruct, "datasource", null );
		if ( datasource == null ){
			cfApplicationData appData = _session.getApplicationData();
			if ( appData != null )
				datasource = appData.getDataSource();
		}
		
		String sql = getNamedStringParam( argStruct, "sql", null );
		if ( sql == null || datasource == null )
			throwException(_session,"provide both datasource and sql parameters");
		
		cfData paramData = getNamedParam( argStruct, "params", null );

		cfDataSource dataSource 	= new cfDataSource(datasource, _session);
		cfSQLQueryData queryData 	= new cfSQLQueryData(dataSource);
		
		setupCache( _session, argStruct, queryData );
		
		List<preparedData>	listPdata = null;

		if (paramData != null) {
			if (paramData.getDataType() == cfData.CFARRAYDATA) {
				listPdata = prepareParams(_session, queryData, (cfArrayData) paramData);
				sql	= prepareSQL( sql, listPdata );
			} else
				throwException(_session, "params must be an array of structures");
		}

		// Perform the query execution
		queryData.setQueryString(sql);
		queryData.runQuery(_session);

		if ( _session.getShowDBActivity() )
			_session.getDebugRecorder().queryRan( "queryRun()", "queryRun()", queryData, listPdata );  
		
		return queryData;
	}
	
	
	private void setupCache( cfSession _session, cfArgStructData argStruct, cfSQLQueryData queryData) throws cfmRunTimeException {
		double	cachedwithin	= getNamedDoubleParam(argStruct, "cachedwithin", -1 );
		String	cacheid				= getNamedStringParam(argStruct, "id", null );
		
		// If we are not using caching here just return
		if ( cachedwithin == -1 && cacheid == null )
			return;
		
		String region					= getNamedStringParam(argStruct, "region", "cfquery" );
		if ( !CacheFactory.isCacheEnabled(region) )
			throwException( _session, "Invalid REGION='" + region + "' specified for QueryRun()" );
		
		long expireTime = -1;
		if ( cachedwithin > 0 )
  		expireTime = (long)(cachedwithin * 86400000);
  	
		queryData.setCacheData(region, expireTime, cacheid);
	}
	

	/**
	 * Re-escapes the ? in the list if there is a list
	 * 
	 * @param sql
	 * @param listPdata
	 * @return
	 * @throws dataNotSupportedException 
	 */
	protected String	prepareSQL( String sql, List<preparedData>	listPdata ) throws dataNotSupportedException{
		if ( listPdata.size() == 0 )
			return sql;
		
		int c1	= sql.indexOf("?");
		int qM	= 0;
		while ( c1 != -1 ){
			
			if ( listPdata.get(qM).getSize() > 1 ){
				String newQM	= listPdata.get(qM).getQueryString();
				sql	= sql.substring(0, c1) + newQM + sql.substring(c1+1);
				c1	+= newQM.length();
			}

			qM++;
			c1	= sql.indexOf("?", c1+1);
		}
		
		return sql;
	}
	

	/*
	 * Runs around the query params
	 */
	protected List<preparedData> prepareParams(cfSession _session, cfQueryResultData queryData, cfArrayData paramData) throws cfmRunTimeException  {
		List<preparedData>	listPdata = new ArrayList<preparedData>();
		
		for ( int x=0; x < paramData.size(); x++ ){
			cfData	data	= paramData.getElement(x+1);
			if ( data.getDataType() != cfData.CFSTRUCTDATA )
				throwException( _session, "params must be an array of structures; " + (x+1) + " element was not a structure" );
			
			cfStructData	sdata	= (cfStructData)data;
			
			preparedData pData = new preparedData();
			pData.setIN();
			
			Object[] keys = sdata.keys();
			
			String separator = ",";
			boolean bList = false;
			cfData defaultList = null;
			
			for ( int i = 0; i < keys.length; i++ ) {
				String key = (String)keys[ i ];
				cfData val = sdata.getData( key );
				
				if ( key.equalsIgnoreCase("NULLVALUE") && val.getBoolean() ){
					pData.setPassAsNull(true);
					pData.setData(cfNullData.NULL);
				}else if ( key.equalsIgnoreCase("CFSQLTYPE") ){
					pData.setDataType( val.getString() );
				} else if ( key.equalsIgnoreCase("VALUE") ){
					pData.setData(val);
				} else if ( key.equalsIgnoreCase("PADDING") ){
					pData.setPadding( val.getInt() );
				} else if ( key.equalsIgnoreCase("SCALE") ){
					pData.setScale( val.getInt() );
				} else if ( key.equalsIgnoreCase("MAXLENGTH") ){
					pData.setMaxLength( val.getInt() );
				} else if ( key.equalsIgnoreCase("SEPARATOR") ){
					separator = val.getString();
				} else if ( key.equalsIgnoreCase("LIST") ){
					bList = val.getBoolean();
				} else if ( key.equalsIgnoreCase("DEFAULTLIST") ){
					defaultList = val;
				}
			}

			if ( bList )
				pData.setList(separator, defaultList);

			pData.validateData(_session);
			queryData.addPreparedData( pData );
			listPdata.add( pData );
		}
		return listPdata;
	}
}
