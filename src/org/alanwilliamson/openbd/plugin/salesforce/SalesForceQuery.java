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
 *  $Id: SalesForceQuery.java 2137 2012-06-27 22:47:03Z alan $
 */
package org.alanwilliamson.openbd.plugin.salesforce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.cache.CacheFactory;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.sql.cfSQLQueryData;
import com.naryx.tagfusion.cfm.tag.tagUtils;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.fault.LoginFault;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;

public class SalesForceQuery extends SalesForceBaseFunction {
	private static final long serialVersionUID = 1L;

	public SalesForceQuery(){
		min = 3; max = 5;
		setNamedParams( new String[]{ "email", "passwordtoken", "query", "cacheage", "timeout", "region" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"SalesFoce Email address",
			"SalesForce password and token concatenated together",
			"SalesForce SOQL statement",
			"Age of the cache in seconds.  If this is 0 (default) then the query is not cached.  Otherwise, if the query is within the time given then the cached version is returned",
			"the time in milliseconds, that the connection will wait for a response",
			"the cache region to use, defaults to SALESFORCE"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"salesforce-plugin", 
				"Executes a query against SalesForce, caching the result if necessary in the given cache region", 
				ReturnType.QUERY );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		String	query	= getNamedStringParam(argStruct, "query", null );
		if ( query == null )
			throwException( _session, "query was not properly defined" );
		
		int cacheAgeMS	= getNamedIntParam(argStruct, "cacheage", 0 ) * 1000;
		
		// If we are using caching then see if it is there
		String region = null;
		String md5Key = null;
		
		if ( cacheAgeMS > 0 ){
			md5Key	= CacheFactory.createCacheKey(query);
			region	= getNamedStringParam(argStruct, "region", "SALESFORCE" );
			
			if ( !CacheFactory.isCacheEnabled(region) ){
				if ( region.equals("SALESFORCE") )
					CacheFactory.setMemoryDiskCache(region, 10, true, 5 );
				else
					throwException( _session, "Cache Region: " + region + ", was not found" );
			}

			cfQueryResultData	queryResultData	= (cfQueryResultData)CacheFactory.getCacheEngine(region).get(md5Key);
			if ( queryResultData != null ){
				queryResultData.executeTime = -1;
				return queryResultData;
			}
		}
		
		
		// Run the query -------------------------
    try {
    	long	startTime	= System.currentTimeMillis();

    	cfSQLQueryData queryResultData	= getQuery(_session, argStruct, query);

  		cfEngine.log( "SalesForceQuery() Time=" + (System.currentTimeMillis()-startTime) + "ms; rows=" + queryResultData.getSize() + "; " + query );

  		// Cache this query if we are using it
      if ( cacheAgeMS > 0 )
      	CacheFactory.getCacheEngine(region).set( md5Key, queryResultData.duplicate(), cacheAgeMS, cacheAgeMS );

      return queryResultData;
    }catch(LoginFault lf){
    	cfEngine.log( "SalesForceQuery().Exception:" + lf.getExceptionMessage() );
    	throwException( _session, "LoginFault: " +	lf.getExceptionMessage() );
    }catch(Exception e){
    	cfEngine.log( "SalesForceQuery().Exception:" + e.getLocalizedMessage() );
    	throwException( _session, "SalesForceException: " + e.getLocalizedMessage() );
    }

    return null;
	}
	
	
	
	/**
	 * Executes the query
	 * 
	 * @param _session
	 * @param argStruct
	 * @param query
	 * @return
	 * @throws ConnectionException
	 * @throws cfmRunTimeException
	 */
	private cfSQLQueryData	getQuery(cfSession _session, cfArgStructData argStruct, String query) throws ConnectionException, cfmRunTimeException{
		PartnerConnection connection = getConnection( _session, argStruct );
		
		try{
		  long time	= System.currentTimeMillis();
	    List<Map<String,Object>>	rows	= runQuery(connection, query);
	    time	= System.currentTimeMillis() - time;
	
	    Map<String,Integer>	activeColumns	= new HashMap<String,Integer>();
	    cfSQLQueryData	queryResultData	=  new cfSQLQueryData( "SALESFORCE" );
	    queryResultData.executeTime = time;
	    queryResultData.setQueryString(query);
	    
	    Iterator<Map<String,Object>>	itRows	= rows.iterator();
	    while ( itRows.hasNext() ) {
	    	Map<String,Object>	row	= itRows.next();
	    	
	  		queryResultData.addRow(1);
	  		queryResultData.setCurrentRow( queryResultData.getSize() );
	
	  		Iterator<String> it	= row.keySet().iterator();
	  		while ( it.hasNext() ){
	  			String	field	= it.next();
	  			
	  			if ( !activeColumns.containsKey( field ) ){
	  				int newcolumn = queryResultData.addColumnData( field, cfArrayData.createArray(1), null );
	  				activeColumns.put( field, newcolumn );
	  			}
	
	  			int column	= activeColumns.get( field );
	  			queryResultData.setCell( column, tagUtils.convertToCfData( row.get(field) ) );
	  			
	  			it.remove();
	  		}
	  		
	  		itRows.remove();
	    }
	    
	    // Reset the internal counters
	    queryResultData.reset();
	    
	    return queryResultData;
	    
		}finally{
    	if ( connection != null ){
				try {
					connection.logout();
				} catch (ConnectionException e) {}
    	}
    }
	}
	
	
	
	/**
	 * Runs the actual SalesForce Query
	 * 
	 * @param connection
	 * @param soql
	 * @return
	 * @throws ConnectionException
	 */
	private List<Map<String,Object>>	runQuery( PartnerConnection connection, String soql ) throws ConnectionException{
    QueryResult queryResults = connection.query(soql);
    List<Map<String,Object>>	rows	= new ArrayList<Map<String,Object>>();
    
    if (queryResults.getSize() > 0) {
    	boolean bWasRowAdded = false;
    	int recordsRxd = 0;
    	int totalRecords = queryResults.getSize();
    	
    	while ( recordsRxd < totalRecords ){
    		
        for (SObject s: queryResults.getRecords()) {
        	recordsRxd++;
        	
        	Map<String,Object>	rowMap	= new HashMap<String,Object>();
        	bWasRowAdded = false;
        	
        	Iterator<XmlObject> childrenIT = s.getChildren();
        	while (childrenIT.hasNext()) {
        		XmlObject	xmlobject	= childrenIT.next();
        		
        		if ( !xmlobject.getChildren().hasNext()  ){
        			
        			String k	= xmlobject.getName().getLocalPart();
        			Object v	= s.getField(k);
        			if ( v != null ){
        				if ( k.equals("type") )
        					k	= "objectType";

        				rowMap.put( k, v );
        			}
        			
        		}else{

        			String namespace	= xmlobject.getName().getLocalPart() + "__";
        			
        			Iterator<XmlObject> subchildrenIT = xmlobject.getChildren();
        			while ( subchildrenIT.hasNext() ){
        				XmlObject	cxmlobject	= subchildrenIT.next();
        				
        				if ( cxmlobject.getName().getLocalPart().equals("records") ){
        					
        					Map<String,Object>	innerMap	= new HashMap<String,Object>();
        					innerMap.putAll(rowMap);

        					Iterator<XmlObject> recordsIT = cxmlobject.getChildren();
            			while ( recordsIT.hasNext() ){
            				XmlObject	rxmlobject	= recordsIT.next();
            			
            				Object v	= rxmlobject.getValue();
              			if ( v != null ){
              				String k = rxmlobject.getName().getLocalPart();
              				if ( k.equals("type") )
              					k	= "objectType";

              				innerMap.put( namespace + k, v );
              			}
            			}
        					
            			rows.add(innerMap);
            			innerMap.clear();
            			bWasRowAdded = true;
        					
        				}else{

          				Object v = cxmlobject.getValue();
          				if ( v != null ){
          					String k = cxmlobject.getName().getLocalPart();
            				if ( k.equals("type") )
            					k	= "objectType";
          					rowMap.put( namespace + k, v );
          				}
          				
        				}
        			}
        		}
        	}

        	
        	// Add the row to our list
        	if ( !bWasRowAdded ){
        		rows.add(rowMap);
        	}
        }

        if ( queryResults.isDone() )
        	break;
        else
        	queryResults = connection.queryMore(queryResults.getQueryLocator());
    	}      
    }
      
    return rows;
	}
}