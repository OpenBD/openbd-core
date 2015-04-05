/* 
 * Copyright (C) 2010 TagServlet Ltd
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
 *  
 *  $Id: dbInfo.java 1590 2011-06-08 14:25:37Z alan $
 */

package com.naryx.tagfusion.cfm.sql.platform.java;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.sql.cfDataSource;
import com.naryx.tagfusion.expression.function.functionBase;

public class dbInfo extends functionBase {
	private static final long serialVersionUID = 1L;

	public dbInfo() {
		min = 2;
		max = 7;
		
		setNamedParams( new String[]{ "datasource", "type", "dbname", "table", "pattern", "username", "password" } );
	}

	public String[] getParamInfo() {
		return new String[] { 
				"name of the datasource", 
				"Type of query to make. Values are: dbnames, tables, columns, version, procedures, foreignkeys, index", 
				"database name",
				"table name",
				"pattern to filter results",
				"username for the database",
				"password for the database"};
	}

	public java.util.Map<String,String> getInfo() {
		return makeInfo("query", "Retrieves information about the underlying database/tables", ReturnType.QUERY);
	}

	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		String datasource = getNamedStringParam( argStruct, "datasource", null );
		String type 			= getNamedStringParam( argStruct, "type", null );
		if ( type == null || datasource == null )
			throwException(_session,"provide both datasource and type parameters");
		
		// Get the datasource
		cfDataSource dataSource 	= new cfDataSource(datasource, _session);
		dataSource.setUsername( getNamedStringParam( argStruct, "username", null ) );
		dataSource.setPassword( getNamedStringParam( argStruct, "password", null ) );
		
		type	= type.toLowerCase();
		if ( type.equals("dbnames") ){
			return typeDbnames( _session, dataSource );
		}else	if ( type.equals("tables") ){
			String dbname		= getNamedStringParam( argStruct, "dbname", null );
			String pattern	= getNamedStringParam( argStruct, "pattern", null );
			return typeTables( _session, dataSource, dbname, pattern );
		}else	if ( type.equals("version") ){
			return typeVersion( _session, dataSource );
		}else	if ( type.equals("columns") ){
			String dbname		= getNamedStringParam( argStruct, "dbname", null );
			String pattern	= getNamedStringParam( argStruct, "pattern", null );
			String table		= getNamedStringParam( argStruct, "table", null );
			
			if ( table == null )
				throwException(_session,"provide table parameter");
			
			return typeColumns( _session, dataSource, dbname, pattern, table );
		}else	if ( type.equals("foreignkeys") ){
			String dbname		= getNamedStringParam( argStruct, "dbname", null );
			String table		= getNamedStringParam( argStruct, "table", null );
			
			if ( table == null )
				throwException(_session,"provide table parameter");
			
			return typeForeignKeys( _session, dataSource, dbname, table );
		}else	if ( type.equals("procedures") ){
			String dbname		= getNamedStringParam( argStruct, "dbname", null );
			String pattern	= getNamedStringParam( argStruct, "pattern", null );
			
			return typeProcedures( _session, dataSource, dbname, pattern );
		}else	if ( type.equals("index") ){
			String dbname		= getNamedStringParam( argStruct, "dbname", null );
			String table		= getNamedStringParam( argStruct, "table", null );
			if ( table == null )
				throwException(_session,"provide table parameter");
			
			return typeIndex( _session, dataSource, dbname, table );
		}else
			throwException(_session,"Invalid TYPE; valid values: dbnames, tables, columns, version, procedures, foreignkeys, index");
		
		return null; //keep compiler happy
	}

	
	
	private cfData typeVersion(cfSession _session, cfDataSource datasource) throws cfmRunTimeException {
		Connection c = null;
		try {
			c = datasource.getPooledConnection();
			
			DatabaseMetaData metaData = c.getMetaData();
			
			cfQueryResultData queryResult = new cfQueryResultData(new String[] { "database_version", "database_productname", "driver_version", "driver_name", "jdbc_major_version", "jdbc_minor_version" }, "DBINFO");
			
			queryResult.addRow(1);
			queryResult.setCell(1, 1, new cfStringData( metaData.getDatabaseProductVersion() ) );
			queryResult.setCell(1, 2, new cfStringData( metaData.getDatabaseProductName() ) );
			queryResult.setCell(1, 3, new cfStringData( metaData.getDriverVersion() ) );
			queryResult.setCell(1, 4, new cfStringData( metaData.getDriverName() ) );
			queryResult.setCell(1, 5, new cfNumberData( metaData.getDriverMajorVersion() )  );
			queryResult.setCell(1, 6, new cfNumberData( metaData.getDriverMinorVersion() ) );
			
			return queryResult;
			
		} catch (SQLException e) {
			throwException(_session,  e.getMessage() );
		} finally {
			datasource.close(c);
		}
		
		return null;
	}

	
	
	private cfData typeIndex(cfSession _session, cfDataSource datasource, String dbname, String table ) throws cfmRunTimeException{
		Connection c = null;
		try {
			c = datasource.getPooledConnection();
			
			DatabaseMetaData metaData = c.getMetaData();
			
			cfQueryResultData queryResult = new cfQueryResultData(new String[] { "index_name", "column_name", "ordinal_position", "cardinality", "type", "pages", "non_unique" }, "DBINFO");
			
			ResultSet rset = metaData.getIndexInfo(dbname, null, table, false, false);
			int row=1;
			while ( rset.next() ){
				queryResult.addRow(1);

				queryResult.setCell(row, 1, new cfStringData(rset.getString(6)) );
				queryResult.setCell(row, 2, new cfStringData(rset.getString(9)) );
				queryResult.setCell(row, 3, new cfStringData(rset.getString(8)) );
				queryResult.setCell(row, 3, new cfStringData(rset.getString(11)) );
				queryResult.setCell(row, 3, new cfStringData(rset.getString(7)) );
				queryResult.setCell(row, 3, new cfStringData(rset.getString(12)) );

				row++;
			}
			rset.close();

			return queryResult;
			
		} catch (SQLException e) {
			throwException(_session,  e.getMessage() );
		} finally {
			datasource.close(c);
		}
		
		return null;
	}
	
	
	private cfData typeProcedures(cfSession _session, cfDataSource datasource, String dbname, String pattern ) throws cfmRunTimeException{
		Connection c = null;
		try {
			c = datasource.getPooledConnection();
			
			DatabaseMetaData metaData = c.getMetaData();
			
			cfQueryResultData queryResult = new cfQueryResultData(new String[] { "procedure_name", "procedure_type", "remarks" }, "DBINFO");
			
			ResultSet rset = metaData.getProcedures( dbname, null, pattern );
			int row=1;
			while ( rset.next() ){
				queryResult.addRow(1);

				queryResult.setCell(row, 1, new cfStringData(rset.getString(3)) );
				queryResult.setCell(row, 2, new cfStringData(rset.getString(8)) );
				queryResult.setCell(row, 3, new cfStringData(rset.getString(7)) );
				
				row++;
			}
			rset.close();

			return queryResult;
			
		} catch (SQLException e) {
			throwException(_session,  e.getMessage() );
		} finally {
			datasource.close(c);
		}
		
		return null;
	}
	
	private cfData typeForeignKeys(cfSession _session, cfDataSource datasource, String dbname, String table ) throws cfmRunTimeException{
		Connection c = null;
		try {
			c = datasource.getPooledConnection();
			
			DatabaseMetaData metaData = c.getMetaData();
			
			cfQueryResultData queryResult = new cfQueryResultData(new String[] { 
					"fkcolumn_name", "fktable_name", "pkcolumn_name", 
					"delete_rule", "update_rule" }, "DBINFO");
			
			
			ResultSet rset = metaData.getImportedKeys(dbname, null, table);
			int row=1;
			while ( rset.next() ){
				queryResult.addRow(1);

				queryResult.setCell(row, 1, new cfStringData(rset.getString(8)) );
				queryResult.setCell(row, 2, new cfStringData(rset.getString(3)) );
				queryResult.setCell(row, 3, new cfStringData(rset.getString(4)) );

				queryResult.setCell(row, 3, new cfStringData(rset.getString(11)) );
				queryResult.setCell(row, 3, new cfStringData(rset.getString(10)) );

				row++;
			}
			rset.close();
			
			return queryResult;
			
		} catch (SQLException e) {
			throwException(_session,  e.getMessage() );
		} finally {
			datasource.close(c);
		}
		
		return null;
	}
	
	
	private cfData typeColumns(cfSession _session, cfDataSource datasource, String dbname, String pattern, String table ) throws cfmRunTimeException{
		Connection c = null;
		try {
			c = datasource.getPooledConnection();
			
			DatabaseMetaData metaData = c.getMetaData();
			
			cfQueryResultData queryResult = new cfQueryResultData(new String[] { 
					"column_name", "type_name", "is_nullable", 
					"is_primarykey", "is_foreignkey", "referenced_primarykey", 
					"referenced_primarykey_table", "column_size", "decimal_digits", 
					"column_default_value", "char_octet_length", "ordinal_position", 
					"remarks" }, "DBINFO");
			
			
			ResultSet rset = metaData.getColumns(dbname, null, table, pattern);
			int row=1;
			while ( rset.next() ){
				queryResult.addRow(1);

				queryResult.setCell(row, 1, new cfStringData(rset.getString(4)) );
				queryResult.setCell(row, 2, new cfStringData(rset.getString(6)) );
				queryResult.setCell(row, 3, cfBooleanData.getcfBooleanData(rset.getString(18)) );
				
				queryResult.setCell(row, 4,	cfBooleanData.FALSE );
				queryResult.setCell(row, 5, cfBooleanData.FALSE );
				queryResult.setCell(row, 6, new cfStringData("") );
				
				queryResult.setCell(row, 7, new cfStringData("") );
				queryResult.setCell(row, 8, new cfStringData(rset.getString(7)) );
				queryResult.setCell(row, 9, new cfStringData(rset.getString(9)) );

				queryResult.setCell(row, 10, new cfStringData(rset.getString(13)) );
				queryResult.setCell(row, 11, new cfStringData(rset.getString(16)) );
				queryResult.setCell(row, 12, new cfNumberData( Integer.valueOf(rset.getString(17)) ) );
				
				queryResult.setCell(row, 13, new cfStringData(rset.getString(12)) );
				
				row++;
			}
			rset.close();

			
			// Manage the primary keys ---------------------------------
			rset = metaData.getPrimaryKeys(dbname, null, table);
			while ( rset.next() ){
				String columnName	= rset.getString(4);
				
				// Find the row in the query
				for ( int r=1; r <= row; r++ ){
					String cellColumn	= queryResult.getCell(r, 1).getString();
					if ( cellColumn.equals(columnName) ){
						queryResult.setCell(r, 4,	cfBooleanData.TRUE );
						break;
					}
				}
			}

			// Manage the foreign keys ---------------------------------
			rset = metaData.getImportedKeys(dbname, null, table);
			while ( rset.next() ){
				String columnName	= rset.getString(4);

				// 	Find the row in the query
				for ( int r=1; r <= row; r++ ){
					String cellColumn	= queryResult.getCell(r, 1).getString();
					if ( cellColumn.equals(columnName) ){
						queryResult.setCell(r, 5,	cfBooleanData.TRUE );
						queryResult.setCell(r, 6,	new cfStringData(rset.getString(8)) );
						queryResult.setCell(r, 7,	new cfStringData(rset.getString(7)) );
						break;
					}
				}

			}
			
			return queryResult;
			
		} catch (SQLException e) {
			throwException(_session,  e.getMessage() );
		} finally {
			datasource.close(c);
		}
		
		return null;
	}
	
	
	
	private cfData typeTables(cfSession _session, cfDataSource datasource, String dbname, String pattern ) throws cfmRunTimeException{
		Connection c = null;
		try {
			c = datasource.getPooledConnection();
			
			DatabaseMetaData metaData = c.getMetaData();
			
			cfQueryResultData queryResult = new cfQueryResultData(new String[] { "table_name", "table_type", "remarks" }, "DBINFO");
			
			
			ResultSet rset = metaData.getTables( dbname, null, pattern, null);
			int row=1;
			while ( rset.next() ){
				queryResult.addRow(1);

				queryResult.setCell(row, 1, new cfStringData(rset.getString(3)) );
				queryResult.setCell(row, 2, new cfStringData(rset.getString(4)) );
				queryResult.setCell(row, 3, new cfStringData(rset.getString(5)) );
				
				row++;
			}
			rset.close();

			return queryResult;
			
		} catch (SQLException e) {
			throwException(_session,  e.getMessage() );
		} finally {
			datasource.close(c);
		}
		
		return null;
	}
	
	
	
	private cfData typeDbnames(cfSession _session, cfDataSource datasource) throws cfmRunTimeException{
		Connection c = null;
		try {
			c = datasource.getPooledConnection();
			
			DatabaseMetaData metaData = c.getMetaData();
			
			cfQueryResultData queryResult = new cfQueryResultData(new String[] { "database_name", "type" }, "DBINFO");
			
			cfStringData	catalogSD = new cfStringData("catalog");
			cfStringData	schemaSD = new cfStringData("schema");
			
			ResultSet rset = metaData.getCatalogs();
			int row=1;
			while ( rset.next() ){
				queryResult.addRow(1);
				queryResult.setCell(row, 1, new cfStringData(rset.getString(1)) );
				queryResult.setCell(row, 2, catalogSD );
				row++;
			}
			rset.close();
			
			
			rset = metaData.getSchemas();
			while ( rset.next() ){
				queryResult.addRow(1);
				queryResult.setCell(row, 1, new cfStringData(rset.getString(1)) );
				queryResult.setCell(row, 2, schemaSD );
				row++;
			}
			rset.close();
			
			return queryResult;
			
		} catch (SQLException e) {
			throwException(_session,  e.getMessage() );
		} finally {
			datasource.close(c);
		}
		
		return null;
	}
	
}
