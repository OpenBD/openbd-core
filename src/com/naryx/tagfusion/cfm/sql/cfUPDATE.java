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

package com.naryx.tagfusion.cfm.sql;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nary.db.metaColumn;
import com.nary.db.metaDatabase;
import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfUPDATE extends cfTag implements Serializable{
  
  static final long serialVersionUID = 1;

  protected void defaultParameters( String _tag ) throws cfmBadFileException {

	  defaultAttribute( "FORMFIELDS", "" );
	  parseTagHeader( _tag );

	  //--[ Do a preliminary check
	  if ( !containsAttribute("TABLENAME") )
		  throw missingAttributeException( "cfupdate.missingTablename", null );

	  if ( containsAttribute("DBTYPE") )
	  {
		  if ( containsAttribute("DATASOURCE") )
			  throw newBadFileException( "Invalid Attributes", "You cannot provide a DATASOURCE and DBTYPE" );
		  if ( !getConstant( "DBTYPE" ).equalsIgnoreCase( "dynamic" ) )
			  throw newBadFileException( "Invalid Attribute", "Only a DBTYPE of dynamic is supported" );
		  if ( !containsAttribute( "CONNECTSTRING" ) )
			  throw newBadFileException( "Missing Attribute", "When a DBTYPE of dynamic is specified, you must provide a CONNECTSTRING" );
	  }
	  else
	  {
		  if ( !containsAttribute("DATASOURCE") )
			  throw newBadFileException( "Missing Attribute", "You need to provide a DATASOURCE or DBTYPE" );
		  if ( containsAttribute( "CONNECTSTRING" ) )
			  throw newBadFileException( "Invalid Attribute", "CONNECTSTRING is only valid when DBTYPE is dynamic" );
	  }
  }
  

  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
  
  	//--[ Create the cfDataSource
	String sDATASOURCE;
	cfDataSource dataSource = null;
	if ( containsAttribute("DATASOURCE") )
	{
		sDATASOURCE = getDynamic(_Session, "DATASOURCE").getString();
		dataSource = new cfDataSource(sDATASOURCE, _Session);
	}
	else
	{
		sDATASOURCE = "DYNAMIC";
		dataSource = new cfDynamicDataSource(sDATASOURCE, _Session, getDynamic(_Session, "CONNECTSTRING").getString());
	}
  
	//--[ Set any additional parameters
	//--[ Get the data connection
    if ( containsAttribute("USERNAME") )
			dataSource.setUsername( getDynamic( _Session, "USERNAME" ).getString() );
    
    if ( containsAttribute("PASSWORD") )
			dataSource.setPassword( getDynamic( _Session, "PASSWORD" ).getString() );
	 
	Connection dataConnection	= setupDataConnection( dataSource );

	try
	{
		//--[ Get the information regarding the database table
		String tableName	 = getDynamic( _Session, "TABLENAME" ).getString();
		
		Map<String, metaColumn> HT = metaDatabase.getColumns( dataConnection, dataSource.getCatalog(), tableName );
		if ( HT == null || HT.size() == 0 ){
			if ( !metaDatabase.tableExist( dataConnection, dataSource.getCatalog(), tableName ) ){
				throw newRunTimeException( "Database table [" + tableName + "] does not exist." );
			}else{
				throw newRunTimeException( sDATASOURCE + " database does not fully support JDBC" );
			}
		}
		
		//--[ Build and validate the list of parameters
		List<metaColumn> primaryKeys = getPrimaryKeys( HT );
		String fieldList[] = buildFieldList( HT, getDynamic( _Session, "FORMFIELDS" ).getString() );
		HT = populateData( _Session, HT, fieldList, false );
		List<metaColumn> columnList	= metaDatabase.sortColumns( HT );
		primaryKeyCheck( _Session, primaryKeys, columnList );

		//--[ Build up Query String
		try{
			String updateString	= prepareUpdateQueryString( columnList, tableName );
			PreparedStatement	pStatmt	= prepareStatement( dataConnection, updateString, columnList );
			pStatmt.executeUpdate();
			pStatmt.close();
			//--[ debug reporting
			_Session.recordUpdate( getFile(), sDATASOURCE, updateString);
		}catch(Exception E){
			throw newRunTimeException( sDATASOURCE + ", caused an error: " + E );		
		}
	}
	finally
	{
		//--[ Push the connection back to the database
		dataSource.returnConnection( dataConnection );		  
	}
	
	return cfTagReturnType.NORMAL;
  }


  private static List<metaColumn> getPrimaryKeys( Map<String, metaColumn> _ht ){
  	List<metaColumn> primaryKeys = new ArrayList<metaColumn>();
  	Iterator<String> keys = _ht.keySet().iterator();
  	while ( keys.hasNext() ) {
  		String nextKey = keys.next();
  		metaColumn nextCol = _ht.get( nextKey );
    	if ( nextCol.PRIMARYKEY ){
    		primaryKeys.add( nextCol );
    	}
  	}
  	
  	return primaryKeys;
  }
  
  private void primaryKeyCheck( cfSession _Session, List<metaColumn> _keys, List<metaColumn> _updatefields ) throws cfmRunTimeException {
  	//	if ( !( (metaColumn) columnList.lastElement() ).PRIMARYKEY )
  	if ( !_updatefields.containsAll( _keys ) ){
  		StringBuilder primaryKeyList = new StringBuilder();
  		for ( int i = 0; i < _keys.size(); i++ ){
  			primaryKeyList.append( ((metaColumn) _keys.get(i)).NAME );
  			primaryKeyList.append( "," );
  		}
  		throw newRunTimeException( "The FORMFIELDS provided must include the primary key [" 
  				+  primaryKeyList.toString().substring( 0, primaryKeyList.length()-1 ) +  "]." );
  	}
  }

	protected PreparedStatement prepareStatement( Connection Con, String updateString, List<metaColumn> columnList ) throws Exception {
		PreparedStatement	pStatmt	= Con.prepareStatement( updateString );
		
		int x=1;
		metaColumn MC;
		Iterator<metaColumn> iter = columnList.iterator();
		
		// Determine if the parameters should be set using only setObject() or
		// using setObject() and setString().
		DatabaseMetaData dbmd = Con.getMetaData();
		String driverName = dbmd.getDriverName();
		boolean useOnlySetObject = true;
		if ( ( com.nary.db.metaDatabase.isOracleDatabase( dbmd ) ) ||
		     ( ( driverName != null ) && ( driverName.startsWith( "JDBC-ODBC Bridge" ) ) ) )
		{
			// 1. With Oracle, calling setObject for certain types will cause a ClassCastException.
			// 2. With the JDBC-ODBC bridge, calling setObject for certain types will cause an exception.
			useOnlySetObject = false;
		}

		while ( iter.hasNext() ){
			MC	= iter.next();
			
			if ( useOnlySetObject )
			{
				// For Sybase using the BEA driver calling setString() for an INTEGER column
				// will cause a failed to cast CHAR to INT exception so use setObject instead.
				pStatmt.setObject( x++, MC.VALUE, MC.SQLTYPE );
			}
			else 
			{
				if ( ( MC.SQLTYPE == Types.VARCHAR ) ||
					 ( MC.SQLTYPE == Types.LONGVARCHAR ) ||
					 ((MC.SQLTYPE == Types.BIGINT) && (com.nary.db.metaDatabase.isMySQLDatabase(dbmd))))
					pStatmt.setObject( x++, MC.VALUE, MC.SQLTYPE );
				else
					pStatmt.setString( x++, MC.VALUE );
			}
		}
				
		return pStatmt;
	}



	protected String prepareUpdateQueryString( List<metaColumn> fieldList, String tableName ){
		StringBuilder SB	= new StringBuilder( 32 );
		SB.append( "UPDATE " );
		SB.append( tableName );
		SB.append( " SET " );
		
		metaColumn MC;
		String SET = "", WHERE = "";
		
		Iterator<metaColumn> iter = fieldList.iterator();
		while ( iter.hasNext() ){
			MC	= iter.next();
			if ( MC.PRIMARYKEY )
				WHERE += MC.NAME + "=? AND ";
			else
				SET += MC.NAME + "=?,";
		}
		
		if ( SET.length() > 0 )
			SET	= SET.substring( 0, SET.length()-1 );
			
		if ( WHERE.length() > 0 )
			WHERE	= WHERE.substring( 0, WHERE.length()-4 );

		SB.append( SET );
		if ( WHERE.length() > 0 ){
			SB.append( " WHERE " );
			SB.append( WHERE );
		}
			
		return SB.toString();
	}
	
	protected Map<String, metaColumn> populateData( cfSession _Session, Map<String, metaColumn> HT, String _fieldList[], boolean _ignoreEmptyFields ) throws cfmRunTimeException  {
		Map<String, metaColumn> newHT = new FastMap<String, metaColumn>();
		metaColumn MC;
		cfData formData;
		
		for( int x=0; x < _fieldList.length; x++ ){
			MC = (metaColumn)HT.get( _fieldList[x] );
			if ( MC == null )
				throw newRunTimeException( "Invalid column name, " + _fieldList[x] + ", was specified." );
			
			//--[ Determine if the form value is there
			formData = _Session.getQualifiedData( variableStore.FORM_SCOPE ).getData( MC.NAME );
			if ( formData == null )
				continue;
			
			MC.VALUE = formData.getString();
			if ( MC.VALUE == null || ( MC.VALUE.length() == 0 && _ignoreEmptyFields ) ){
				if ( (MC.SQLTYPE == java.sql.Types.VARCHAR || MC.SQLTYPE == java.sql.Types.LONGVARCHAR) && MC.DEFAULTVALUE == null )
					continue;
			}
			
			newHT.put( _fieldList[x], MC );
		}
				
		return newHT;
	}

  protected String[] buildFieldList( Map<String, metaColumn> HT, String _fieldList ) {
		// --[ Build list of fields
		String fieldList = "";
		String[] fields = null; // we return this

		if ( _fieldList.length() == 0 ) {
			Iterator<metaColumn> iter = HT.values().iterator();
			while ( iter.hasNext() )
				fieldList += iter.next().NAME + ",";

			if ( fieldList.length() > 0 )
				fieldList = fieldList.substring( 0, fieldList.length() - 1 );

			fields = com.nary.util.string.convertToList( fieldList, ',' );
		} else {

			fieldList = _fieldList;
			fields = com.nary.util.string.convertToList( fieldList, ',' );
			for ( int i = 0; i < fields.length; i++ ) {
				// if there's no key found that matches then try a case insensitive match
				// - would be nicer if we had a case-insensitive hashtable to use
				if ( !HT.containsKey( fields[ i ] ) ) {
					Iterator<String> keys = HT.keySet().iterator();
					while ( keys.hasNext() ) {
						String nextKey = keys.next();
						if ( nextKey.equalsIgnoreCase( fields[ i ] ) ) {
							fields[ i ] = nextKey;
							break;
						}
					}
				}
			}
		}

		return fields;
	}
	
  protected Connection setupDataConnection( cfDataSource thisDataSource) throws cfmRunTimeException {
  	try{
			return thisDataSource.takeConnection();
  	}catch( SQLException e ){
     	throw new cfmRunTimeException( catchDataFactory.databaseException( thisDataSource.getDataSourceName(), "sql.connecting", 
																		   new String[]{com.naryx.tagfusion.cfm.tag.tagUtils.trimError(e.getMessage())},
                                                                           "", e ) );
   	}
  }
	
}
