/* 
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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
 */


package org.alanwilliamson.amazon.simpledb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aw20.amazon.SimpleDB;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.sql.cfSQLQueryData;
import com.naryx.tagfusion.cfm.sql.preparedData;

public class cfSimpleDBResultData extends cfSQLQueryData implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	private SimpleDB sdb;
	private String tokenPassed;
	
	public cfSimpleDBResultData(SimpleDB	sdb, String tokenPassed){
		super( "AmazonSimpleDb" );
		this.sdb 					= sdb;
		this.tokenPassed	= tokenPassed;
	}

	
	/*
	 * Override the getData() so we can give access to other parameters
	 * that the amazon implementation returns
	 * @see com.naryx.tagfusion.cfm.sql.cfSQLQueryData#getData(java.lang.String)
	 */
	public cfData getData( String _key ) {
		if ( _key.equalsIgnoreCase("token") ){
			return new cfStringData( this.sdb.getLastToken() );
		}else	if ( _key.equalsIgnoreCase("boxusage") ){
			return new cfStringData( this.sdb.getLastBoxUsage() );
		}else if ( _key.equalsIgnoreCase("requestid") ){
			return new cfStringData( this.sdb.getLastRequestId() );
		}else
			return super.getData( _key );
	}
	
	protected String getExtraInfo( boolean _isLong ){
		return "";
	}
	
	public void execute( cfSession _Session ) throws cfmRunTimeException {
		if ( getQueryType() == cfSQLQueryData.SQL_UPDATE ){
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "UPDATE statement not supported", null, queryString) );
		} else if ( getQueryType() == cfSQLQueryData.SQL_DELETE ){
			executeDelete( _Session );
		} else if ( getQueryType() == cfSQLQueryData.SQL_INSERT ){
			executeInsert( _Session );
		} else if ( getQueryType() == cfSQLQueryData.SQL_SELECT ){
			executeSelect( _Session );
		} else {
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "Statement not supported", null, queryString) );
		}
	}


	private preparedData currentPreparedData = null;
	private int	preparedIndex = 0, preparedDataIndex = 0;
	
	private String getNextParam()  throws cfmRunTimeException  {
		if ( preparedDataList == null || preparedDataList.size() == 0 )
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "No CFQUERYPARAM", null, queryString) );			
		
		if ( currentPreparedData == null )
			currentPreparedData	= preparedDataList.get( preparedIndex++ );

		if ( preparedDataIndex == currentPreparedData.getSize() ){
			preparedDataIndex = 0;
			
			if ( preparedIndex == preparedDataList.size() )
				throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "CFQUERYPARAM out of sync", null, queryString) );
			
			currentPreparedData	= preparedDataList.get( preparedIndex++ );
		}
			
		cfData	thisData	= currentPreparedData.getData( preparedDataIndex++ );
		
		String data;
		
		//Pad out the data
		if ( thisData.getDataType() == cfData.CFNUMBERDATA && currentPreparedData.getPadding() > 0 ){
			data = thisData.getString();
			int padLength = currentPreparedData.getPadding() - data.length();
			for ( int x=0; x < padLength; x++ ){
				data = "0" + data;
			}
		}else
			data = thisData.getString();

		
		//Final check to make sure the string isn't too big
		if ( data.length() > 1024 )
			return data.substring( 0, 1024 );
		else
			return data;
	}

	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void executeInsert( cfSession _Session ) throws cfmRunTimeException {
		/*
		 * Pull out the DOMAIN name ----------------------------------------------
		 */
		int c1 = queryString.toLowerCase().indexOf("insert into");
		int c2 = queryString.indexOf("(");
		if ( c1 == -1 || c2 == -1 )
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "SQL Parse Error should be [insert into myDomainName () values ()]", null, queryString) );
		

		String domain	= queryString.substring( c1 + 11, c2 ).trim();
		if ( domain.length() == 0 )
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "SQL Parse Error invalid domain", null, queryString) );
	
		c1 = queryString.indexOf(")", c2 + 1);
		

		/*
		 * Pull out the column names ----------------------------------------------
		 */
		String columnNames[] = queryString.substring( c2 + 1, c1 ).trim().split( ",(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))" );
		
		//Clean up and validate the columns
		boolean bFound_ItemName = false;
		for ( int x=0; x < columnNames.length; x++ ){
			columnNames[x] = columnNames[x].trim();
			
			if ( columnNames[x].length() == 0 )
				throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "SQL Parse Error invalid column name", null, queryString) );

			if ( columnNames[x].length() >= 2 && columnNames[x].charAt(0) == '"' && columnNames[x].charAt(columnNames[x].length()-1) == '"' )
				columnNames[x] = columnNames[x].substring( 1, columnNames[x].length()-1 );
			
			if ( columnNames[x].equals("ItemName") && bFound_ItemName )
				throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "You cannot include the column name 'ItemName' more than once", null, queryString) );
			
			if ( columnNames[x].equals("ItemName") )
				bFound_ItemName = true;
			
		}
		
		if ( !bFound_ItemName )
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "You must include the column name 'ItemName' in the column list", null, queryString) );
		
		
		/*
		 * Pull out the values names ----------------------------------------------
		 */
		c2	= queryString.indexOf("(", c1 + 1);
		c1	= queryString.lastIndexOf(")" );
		if ( c2 == -1 || c1 == -1 )
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "SQL Parse Error invalid values (..)", null, queryString) );
		
		String values[] = queryString.substring( c2 + 1, c1 ).trim().split( ",(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))" );
		if ( values.length != columnNames.length )
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "SQL Parse Error number of columns do not match values", null, queryString) );
		
		for ( int x=0; x < values.length; x++ ){
			values[x] = values[x].trim();
			
			if ( values[x].length() == 0 )
				throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "SQL Parse Error invalid value", null, queryString) );

			if ( values[x].length() >= 2 && values[x].charAt(0) == '"' && values[x].charAt(values[x].length()-1) == '"' )
				values[x] = values[x].substring( 1, values[x].length()-1 );
		}
		
		
		/*
		 * Create the Amazon DB call ----------------------------------------------
		 */
		HashMap	attributes	= new HashMap();
		String ItemName	= null;
		for ( int x=0; x < columnNames.length; x++ ){
			if ( values[x].equals("?") )
				values[x]	= getNextParam();
			
			if ( !columnNames[x].equals("ItemName") ){
				attributes.put( columnNames[x], values[x] );
			}else{
				ItemName = values[x];
			}
		}
		
		try {
			executeTime = System.currentTimeMillis();
			sdb.putAttributes(domain, ItemName, attributes, attributes.keySet() );
		} catch (Exception e) {
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "AmazonError: " + e.getMessage(), null, queryString) );
		} finally {
			executeTime	= System.currentTimeMillis() - executeTime;
		}
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void executeDelete( cfSession _Session ) throws cfmRunTimeException {
		/*
		 * Pull out the domain ----------------------------------------------
		 */
		int c1 = queryString.toLowerCase().indexOf("delete from");
		int c2 = queryString.toLowerCase().indexOf("where");
		if ( c1 == -1 || c2 == -1 )
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "SQL Parse Error should be [delete from myDomainName where ItemName='' {AND ItemAttribute=''}]", null, queryString) );
				
		String domain	= queryString.substring( c1 + 11, c2 ).trim();
		if ( domain.length() == 0 )
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "SQL Parse Error invalid domain", null, queryString) );
		
		
		/*
		 * Pull out the ItemName ----------------------------------------------
		 */
		c1 = queryString.indexOf("ItemName");
		if ( c1 == -1 )
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "SQL Parse Error: 'ItemName' was not found in WHERE clause", null, queryString) );

		c2 = queryString.indexOf("=", c1+8);
		if ( c2 == -1 )
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "SQL Parse Error: 'ItemName' value not found in WHERE clause", null, queryString) );
		
		String strleft = queryString.substring( c2 + 1 ).trim();
		String ItemName = null;
		
		if ( strleft.charAt(0) == '\'' ){
			c1 = strleft.indexOf( "'", 1 );
			if ( c1 == -1 )
				throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "SQL Parse Error: 'ItemName' value not found in WHERE clause", null, queryString) );
			
			ItemName	= strleft.substring( 1, c1 );
			if ( c1 == strleft.length() )
				strleft = "";
			else
				strleft	= strleft.substring( c1 + 1 );
		} else if ( strleft.charAt(0) == '"' ){
			c1 = strleft.indexOf( "\"", 1 );
			if ( c1 == -1 )
				throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "SQL Parse Error: 'ItemName' value not found in WHERE clause", null, queryString) );
			
			ItemName	= strleft.substring( 1, c1 );
			if ( c1 == strleft.length() )
				strleft = "";
			else
				strleft	= strleft.substring( c1 + 1 );
		} else if ( strleft.charAt(0) == '?' ) {
			
			ItemName = getNextParam();
			strleft = strleft.substring( 1 );

		} else if ( ItemName == null )
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "SQL Parse Error: 'ItemName' value not found in WHERE clause, use quotes or CFQUERYPARAM", null, queryString) );

		strleft = strleft.trim();
		
		
		/*
		 * Pull out the ItemAttribute ----------------------------------------------
		 */
		String ItemAttribute = null;
		c1 = strleft.indexOf("ItemAttribute");
		if ( c1 != -1 ){
			strleft	= strleft.substring( c1 + 13 ).trim();
			c2 = strleft.indexOf("=");
			if ( c2 == -1 )
				throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "SQL Parse Error: 'ItemAttribute' value not found in WHERE clause", null, queryString) );
			
			strleft = strleft.substring( c2 + 1 ).trim();
			if ( strleft.charAt(0) == '\'' ){
				c1 = strleft.indexOf( "'", 1 );
				if ( c1 == -1 )
					throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "SQL Parse Error: 'ItemAttribute' value not found in WHERE clause", null, queryString) );
				
				ItemAttribute	= strleft.substring( 1, c1 );
			} else if ( strleft.charAt(0) == '"' ){
				c1 = strleft.indexOf( "\"", 1 );
				if ( c1 == -1 )
					throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "SQL Parse Error: 'ItemAttribute' value not found in WHERE clause", null, queryString) );
				
				ItemAttribute	= strleft.substring( 1, c1 );
			} else if ( strleft.charAt(0) == '?' ) {
				ItemAttribute = getNextParam();
			}
		}
		
		/*
		 * Now Prepare the call for amazon ----------------------------------------------
		 */
		try {
			executeTime = System.currentTimeMillis();
			
			if ( ItemAttribute == null ){
				sdb.deleteAttributes(domain, ItemName);
			}else{
				HashMap attributes = new HashMap();
				attributes.put( ItemAttribute, null );
				sdb.deleteAttributes(domain, ItemName, attributes );
			}
									
		} catch (Exception e) {
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "AmazonError: " + e.getMessage(), null, queryString) );
		} finally {
			executeTime	= System.currentTimeMillis() - executeTime;
		}
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void executeSelect( cfSession _Session ) throws cfmRunTimeException {
		/*
		 * Run the full query against amazon
		 */
		try {
			executeTime = System.currentTimeMillis();
			resultSet		= true;
			
			List<HashMap> results = sdb.select( queryString, tokenPassed ); 

			if ( results.size() > 0 ){
				Set columns = getUnqiueColumns( results );
				init( (String[])columns.toArray(new String[0]), null, "AmazonSimpleDB" );

				for ( Iterator<HashMap> it = results.iterator(); it.hasNext(); ){
					HashMap item	= it.next();
					
					addRow(1);
					setCurrentRow( getSize() );
					
					for( Iterator<String> kit = item.keySet().iterator(); kit.hasNext(); ){
						String key = kit.next();
						String val = (String)((String[])item.get( key ))[0];
						setCell( key, new cfStringData(val) );
					}

				}
			}else{
				init( new String[]{"itemname"}, null, "AmazonSimpleDB" );
			}
			
		} catch (Exception e) {
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_DATABASE,	"errorCode.sqlError", "AmazonError: " + e.getMessage(), null, queryString) );
		} finally {
			executeTime	= System.currentTimeMillis() - executeTime;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Set getUnqiueColumns( List<HashMap> results ){
		HashSet columns = new HashSet();
		for ( Iterator<HashMap> it = results.iterator(); it.hasNext(); ){
			HashMap item	= it.next();
			columns.addAll( item.keySet() );
		}
		return columns;
	}
}