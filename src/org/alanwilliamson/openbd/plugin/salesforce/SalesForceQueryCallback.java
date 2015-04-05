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
 *  http://www.openbluedragon.org/
 *  
 *  $Id: SalesForceQueryCallback.java 2131 2012-06-27 19:02:26Z alan $
 */
package org.alanwilliamson.openbd.plugin.salesforce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.bluedragon.plugin.ObjectCFC;
import com.bluedragon.plugin.PluginManager;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.sql.cfSQLQueryData;
import com.naryx.tagfusion.cfm.tag.tagUtils;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;

public class SalesForceQueryCallback extends SalesForceBaseFunction {
	private static final long serialVersionUID = 1L;

	public SalesForceQueryCallback(){
		min = 3; max = 5;
		setNamedParams( new String[]{ "email", "passwordtoken", "query", "cfc", "timeout" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"SalesFoce Email address",
			"SalesForce password and token concatenated together",
			"SalesForce SOQL statement",
			"The CFC object that will have the callback performed on it on the method: onSalesForceQueryResult( qry, recordsin, recordstotal ).  If this function returns false; then it will stop further processing",
			"the time in milliseconds, that the connection will wait for a response"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"salesforce-plugin", 
				"Executes a query against SalesForce, sending the results to a call back CFC for each page of results that come back", 
				ReturnType.NUMERIC );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		String	query	= getNamedStringParam(argStruct, "query", null );
		if ( query == null )
			throwException( _session, "query was not properly defined" );
		
		cfData	cfcdata	= getNamedParam(argStruct, "cfc", null);
		if ( cfcdata == null || !(cfcdata instanceof cfComponentData) )
			throwException( _session, "cfc callback was not properly defined" );
		
		cfComponentData	cfcCallback	= (cfComponentData)cfcdata;
		
		// Run the query
    try {
    	long	startTime	= System.currentTimeMillis();
    	int total	= getQuery(_session, argStruct, query, cfcCallback );
  		PluginManager.getPlugInManager().log( "SalesForceQueryCallback() Time=" + (System.currentTimeMillis()-startTime) + "ms; rows=" + total + "; " + query );
      return new cfNumberData( total );
    }catch(Exception e){
    	PluginManager.getPlugInManager().log( "SalesForceQueryCallback().Exception:" + e.getMessage() );
    	throwException( _session, e.getMessage() );
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
	private int	getQuery(cfSession _session, cfArgStructData argStruct, String query, cfComponentData	cfcCallback) throws cfmRunTimeException{
		PartnerConnection connection = null;
		
		try{
			connection = getConnection( _session, argStruct );
		
	    long time	= System.currentTimeMillis();
	    int total = runQuery(_session, connection, query, cfcCallback );
	    time	= System.currentTimeMillis() - time;
	    
	    return total;
		} catch (ConnectionException e) {
			throwException(_session, e.getMessage() );
			return -1;
		}finally{
    	if ( connection != null ){
				try {
					connection.logout();
				} catch (ConnectionException e) {}
    	}
    }
	}
	
	
	private cfSQLQueryData	convertToQuery(String query, List<Map<String,Object>>	rows ) throws cfmRunTimeException{
    Map<String,Integer>	activeColumns	= new HashMap<String,Integer>();
    cfSQLQueryData	queryResultData	=  new cfSQLQueryData( "SALESFORCE" );
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
	}
	
	
	/**
	 * Runs the actual SalesForce Query
	 * 
	 * @param connection
	 * @param soql
	 * @return
	 * @throws ConnectionException
	 * @throws cfmRunTimeException 
	 */
	private int	runQuery(cfSession _session,  PartnerConnection connection, String soql, cfComponentData	cfcCallback ) throws ConnectionException, cfmRunTimeException{
		int totalRecords = 0;
		QueryResult queryResults = connection.query(soql);
    List<Map<String,Object>>	rows	= new ArrayList<Map<String,Object>>();
    
    if (queryResults.getSize() > 0) {
    	boolean bWasRowAdded = false;
    	int recordsRxd = 0;
    	totalRecords = queryResults.getSize();
    	
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
        
        // Now we can make the call out to the CFC
        callback( _session, convertToQuery(soql, rows), recordsRxd, totalRecords, cfcCallback );
        rows.clear();

        if ( queryResults.isDone() )
        	break;
        else
        	queryResults = connection.queryMore(queryResults.getQueryLocator());
    	}      
    }

    return totalRecords;
	}


	
	/**
	 * Perform the call back
	 * 
	 * @param convertToQuery
	 * @param cfcCallback
	 */
	private void callback(cfSession _session, cfSQLQueryData sqlQryData, int recordsin, int totalrecords, cfComponentData cfcCallback) throws cfmRunTimeException {
		try {
			
			ObjectCFC	objectcfc = PluginManager.getPlugInManager().createCFC(_session, cfcCallback);
			objectcfc.addArgument("qry", 					sqlQryData );
			objectcfc.addArgument("recordsin", 		new cfNumberData(recordsin) );
			objectcfc.addArgument("recordstotal", new cfNumberData(totalrecords) );
			if ( !objectcfc.runMethodReturnBoolean(_session, "onSalesForceQueryResult" ) )
				throwException(_session, "CFC returned false; cancelling the callback");

		} catch (Exception e) {
			throwException( _session, e.getMessage() );
		}
	}
}