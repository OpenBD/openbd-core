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
 *  $Id: metaDatabase.java 2327 2013-02-10 22:26:44Z alan $
 */

package com.nary.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.sql.pool.WrappedConnection;

/**
 * This class is an interface to information regarding a database
 * table or column
 */
 
public class metaDatabase extends Object {

	public static Map<String, metaColumn> getColumns( Connection _Connection, String _dbInst, String _tableName ){
		Map<String, metaColumn> HT	= new FastMap<String, metaColumn>();
	
		try{
			DatabaseMetaData metaData	= _Connection.getMetaData();
			
			// If it's an Oracle database then we need to convert the table name to all uppercase and
			// set _dbInst to null since DatabaseMetaData calls using the Oracle thin driver and the
			// JDBC-ODBC bridge connected to Oracle won't work if the table name isn't all uppercase
			// and if the catalog parameter isn't set to null.
			if ( isOracleDatabase( metaData ) )
			{
				_tableName	= _tableName.toUpperCase();
				_dbInst		= null;
			}
			else if ( isDb2Database( metaData ) || isPointBaseDatabase( metaData ) )
			{
				// If it's a DB2 or PointBase database then we need to convert the table name to all uppercase
				// since DatabaseMetaData calls using the BEA WLS driver won't work if the table name
				// isn't all uppercase.
				_tableName	= _tableName.toUpperCase();
			}
			else if ( isInformixDatabase( metaData ) || isPostgreSQLDatabase( metaData ) )
			{
				// If it's an Informix or PostgreSQL database then we need to convert the table name to all lowercase
				// since DatabaseMetaData calls using the BEA WLS driver or PostgreSQL driver won't work if the table name
				// isn't all lowercase.
				_tableName	= _tableName.toLowerCase();
			}
			
			ResultSet RES	= metaData.getColumns( null, null, _tableName, null );

			while ( RES.next() ){
										
				metaColumn mC		= new metaColumn();
				mC.NAME					= RES.getString( 4 );
				mC.SQLTYPE			= RES.getInt( 5 );
				mC.DATATYPE			= RES.getString( 6 );
				mC.DEFAULTVALUE	= RES.getString( 13 );

				HT.put( mC.NAME, mC );
			}
		
			RES.close();
			
			if ( getPrimaryKeys(HT,metaData,_dbInst,_tableName) == null )
				return getPrimaryKeysAccess(HT,metaData,_dbInst,_tableName);
									
			return HT;
		}catch(Exception E){
			return null;
		}
	}

	
	private static Map<String, metaColumn> getPrimaryKeys( Map<String, metaColumn> HT, DatabaseMetaData metaData, String _dbInst, String _tableName ){
		ResultSet RES;
		
		try{
			RES	= metaData.getPrimaryKeys( null, null, _tableName ); 
			while ( RES.next() ){
				String columnName = RES.getString(4);
				if ( HT.containsKey( columnName ) ){
					metaColumn mC	=	HT.get( columnName );
					mC.PRIMARYKEY	= true;
				}
			}
			RES.close();
						
			return HT;
		}catch(Exception E){
			return null;
		}
	}
		
	private static Map<String, metaColumn> getPrimaryKeysAccess( Map<String, metaColumn> HT, DatabaseMetaData metaData, String _dbInst, String _tableName ){
		ResultSet RES;
		
		try{
			RES	= metaData.getIndexInfo( null, null, _tableName, false, false );
			metaColumn mC;
			while ( RES.next() ){
				String columnName = RES.getString( "COLUMN_NAME" );
				String indexName 	= RES.getString( "INDEX_NAME" );
				
				if ( columnName == null )
					continue;
				
				if ( HT.containsKey( columnName ) ){
					mC =	HT.get( columnName );
				}else{
					mC	= new metaColumn();
					mC.NAME					= columnName;
					mC.DEFAULTVALUE	= "";
					HT.put( columnName, mC );
				}

				// The check for indexName.startsWith( "Index_" ) was added to fix bug 1193 for
				// BD/Java connected to an Access database.
				if ( ( indexName != null ) &&
				     ( indexName.equals( "PrimaryKey" ) || indexName.startsWith( "Index_" ) ) )
					mC.PRIMARYKEY	= true;
			}
			RES.close();
			
			return HT;
		}catch(Exception E){
			com.nary.Debug.printStackTrace( E );
			return null;
		}
	}
		
	
	public static boolean createTable( Connection _Connection, String table ){
		Statement Statmt = null;
		try
		{
			Statmt = _Connection.createStatement();
			if ( _Connection instanceof WrappedConnection )
				((WrappedConnection)_Connection).setLastQuery("STATEMENT: " + table);
			Statmt.executeUpdate(table);
			return true;
		}
		catch (Exception E) { }
		finally
		{
			if (Statmt != null)
			{
				try { Statmt.close(); }
				catch (SQLException sqlExc) { }
			}
		}
		
		return false;
	}
		
	
	public static boolean tableExist( Connection _Connection, String _dbInst, String _tableName ){
		try{
			DatabaseMetaData metaData	= _Connection.getMetaData();

			// If it's an Oracle database then we need to convert the table name to all uppercase and
			// set _dbInst to null since DatabaseMetaData calls using the Oracle thin driver and the
			// JDBC-ODBC bridge connected to Oracle won't work if the table name isn't all uppercase
			// and if the catalog parameter isn't set to null.
			if ( isOracleDatabase( metaData ) )
			{
				_tableName	= _tableName.toUpperCase();
				_dbInst		= null;
			}
			else if ( isDb2Database( metaData ) || isPointBaseDatabase( metaData ) )
			{
				// If it's a DB2 or PointBase database then we need to convert the table name to all uppercase
				// since DatabaseMetaData calls using the BEA WLS driver won't work if the table name
				// isn't all uppercase.
				_tableName	= _tableName.toUpperCase();
			}
			else if ( isInformixDatabase( metaData ) || isPostgreSQLDatabase( metaData ) )
			{
				// If it's an Informix or PostgreSQL database then we need to convert the table name to all lowercase
				// since DatabaseMetaData calls using the BEA WLS driver or PostgreSQL driver won't work if the table name
				// isn't all lowercase.
				_tableName	= _tableName.toLowerCase();
			}

			ResultSet RES	= metaData.getTables( _dbInst, null, _tableName, null );
			boolean bFound = false;
			
			if ( RES.next() )
				bFound = true;
		
			RES.close();
			return bFound;
		}catch(Exception E){}

		return false;
	}
	
	public static List<metaColumn> sortColumns( Map<String, metaColumn> HT ){
		List<metaColumn> V	= new ArrayList<metaColumn>( HT.size() );
		Iterator<metaColumn> iter = HT.values().iterator();
		while ( iter.hasNext() )
		{
			metaColumn mC	= iter.next();
			if ( mC.PRIMARYKEY )
				V.add( mC );
			else
				V.add(0,mC);
		}
		
		return V;
	}

	public static boolean isMySQLDatabase(DatabaseMetaData metaData)
		throws SQLException
	{
		String dbName = metaData.getDatabaseProductName();
		if (dbName.equals("MySQL"))
			return true;

		return false;
	}

	public static boolean isOracleDatabase(DatabaseMetaData metaData)
		throws SQLException
	{
		String dbName = metaData.getDatabaseProductName();
		if (dbName.equals("Oracle"))
			return true;

		return false;
	}
	
	public static boolean isSybaseDatabase( DatabaseMetaData metaData )
		throws SQLException
	{
		String dbName = metaData.getDatabaseProductName();
		if ( dbName.equals( "Sybase" ) )
			return true;
			
		return false;
	}
	
	private static boolean isDb2Database( DatabaseMetaData metaData )
		throws SQLException
	{
		String dbName = metaData.getDatabaseProductName();
		if ( dbName.startsWith( "DB2" ) )
			return true;
			
		return false;
	}
	
	private static boolean isPointBaseDatabase( DatabaseMetaData metaData )
		throws SQLException
	{
		String dbName = metaData.getDatabaseProductName();
		if ( dbName.startsWith( "PointBase" ) )
			return true;
			
		return false;
	}
	
	private static boolean isInformixDatabase( DatabaseMetaData metaData )
		throws SQLException
	{
		String dbName = metaData.getDatabaseProductName();
		if ( dbName.startsWith( "Informix" ) )
			return true;
			
		return false;
	}
	
	private static boolean isPostgreSQLDatabase( DatabaseMetaData metaData )
		throws SQLException
	{
		String dbName = metaData.getDatabaseProductName();
		if ( dbName.startsWith( "PostgreSQL" ) )
			return true;
			
		return false;
	}
}
