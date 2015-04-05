/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

package com.naryx.tagfusion.cfm.engine;

import java.io.Serializable;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import com.nary.util.FastMap;

public class cfQueryResultSetMetaData implements ResultSetMetaData, Serializable {

	private static final long serialVersionUID = 1L;

	private ColumnMetaData[] columnList;

	private boolean isColumnTypeSet;

	// map column name to column index for fast lookups; index is 1-based
	private Map<String, Integer> columnNameCache = new FastMap<String, Integer>(FastMap.CASE_INSENSITIVE);

	public cfQueryResultSetMetaData(ResultSetMetaData rsmd) {
		isColumnTypeSet = true;
		try {
			int numCols = rsmd.getColumnCount();
			columnList = new ColumnMetaData[numCols];

			for (int i = 0; i < numCols; i++) {
				ColumnMetaData cmd = new ColumnMetaData();

				cmd.autoIncrement = rsmd.isAutoIncrement(i + 1);
				cmd.caseSensitive = rsmd.isCaseSensitive(i + 1);
				cmd.catalogName = rsmd.getCatalogName(i + 1);
				cmd.columnClassName = rsmd.getColumnClassName(i + 1);
				cmd.columnDisplaySize = rsmd.getColumnDisplaySize(i + 1);
				cmd.columnLabel = rsmd.getColumnLabel(i + 1);
				cmd.columnName = rsmd.getColumnLabel(i + 1);
				if ((cmd.columnName == null) || (cmd.columnName.length() == 0)) {
					// If the columnName is null or an empty string then we won't be able
					// to access it in a CFML page. To work around this set the name to
					// "Computed_Column_X". This particular problem was seen with Informix
					// stored procedures that return return values as a result set. This
					// is
					// also compatible with CF5/MX.
					cmd.columnName = "Computed_Column_" + (i + 1);
				}
				cmd.columnType = rsmd.getColumnType(i + 1);
				cmd.columnTypeName = rsmd.getColumnTypeName(i + 1);
				cmd.currency = rsmd.isCurrency(i + 1);
				cmd.definitelyWriteable = rsmd.isDefinitelyWritable(i + 1);
				cmd.nullable = rsmd.isNullable(i + 1);
				try {
					cmd.precision = rsmd.getPrecision(i + 1);
				} catch (NumberFormatException e) { // Oracle does this for LOB types
					cmd.precision = Integer.MAX_VALUE;
				}
				cmd.readOnly = rsmd.isReadOnly(i + 1);
				cmd.scale = rsmd.getScale(i + 1);
				cmd.schemaName = rsmd.getSchemaName(i + 1);
				cmd.searchable = rsmd.isSearchable(i + 1);
				cmd.signed = rsmd.isSigned(i + 1);
				cmd.tableName = rsmd.getTableName(i + 1);
				cmd.writable = rsmd.isWritable(i + 1);

				columnList[i] = cmd;
				columnNameCache.put(cmd.columnName, new Integer(i + 1));
			}
		} catch (SQLException e) {
			// If you see the MySQL JDBC driver throwing an exception with text like
			// "Can't create/write
			// to file 'C:\WINDOWS\TEMP\#sql_2ddc_0.MYD' (Errcode: 13)" then you may
			// need to disable your
			// antivirus program. I saw this problem with McAfee - Paul.
			cfEngine.log(e.getMessage());
		}
	}

	public cfQueryResultSetMetaData(String[] columnNames, int[] columnTypes) {
		columnList = new ColumnMetaData[columnNames.length];
		isColumnTypeSet = columnTypes != null;

		for (int i = 0; i < columnNames.length; i++) {
			columnList[i] = new ColumnMetaData(columnNames[i]);
			if (columnTypes != null) {
				columnList[i].columnType = columnTypes[i];
				switch (columnTypes[i]) {
				case Types.VARCHAR:
					columnList[i].columnTypeName = "VARCHAR";
					break;
				case Types.BIGINT:
					columnList[i].columnTypeName = "BIGINT";
					break;
				case Types.BINARY:
					columnList[i].columnTypeName = "BINARY";
					break;
				case Types.BIT:
					columnList[i].columnTypeName = "BIT";
					break;
				case Types.DATE:
					columnList[i].columnTypeName = "DATE";
					break;
				case Types.TIME:
					columnList[i].columnTypeName = "TIME";
					break;
				case Types.INTEGER:
					columnList[i].columnTypeName = "INTEGER";
					break;
				case Types.DOUBLE:
					columnList[i].columnTypeName = "DOUBLE";
					break;
				case Types.DECIMAL:
					columnList[i].columnTypeName = "DECIMAL";
					break;
				default:
					columnList[i].columnTypeName = "OTHER";
					break;

				}

			}
			columnNameCache.put(columnNames[i], new Integer(i + 1));
		}
	}

	public boolean isColumnTypesSet() {
		return this.isColumnTypeSet;
	}

	/**
	 * WARNING! The performance of this method is critical to overall system
	 * performance. Do not make any changes to this method without doing
	 * before-and-after timing measurements to make sure you have not decreased
	 * performance.
	 */
	public int getColumnIndex(String columnName) { // return a 1-based index for
																									// the column
		Integer columnIndexObj = columnNameCache.get(columnName);
		if (columnIndexObj != null) {
			return columnIndexObj.intValue();
		}
		return 0;
	}

	public Object clone() {
		return new cfQueryResultSetMetaData(this);
	}

	public String[] getColumnNames() {
		String[] columnNames = new String[columnList.length];

		for (int i = 0; i < columnList.length; i++)
			columnNames[i] = columnList[i].columnName;

		return columnNames;
	}

	public int[] getColumnTypes() {
		int[] columnTypes = new int[columnList.length];

		for (int i = 0; i < columnList.length; i++)
			columnTypes[i] = columnList[i].columnType;

		return columnTypes;
	}

	public synchronized void deleteColumn(String columnName){
		int columnIndex = getColumnIndex( columnName ) - 1;
		if ( columnIndex < 0 )
			return;
		
		// Clear down the cache; we may need to rebuild it
		columnNameCache.clear();
		
		if ( columnList.length == 1 ){
			columnList	= new ColumnMetaData[0];
		}else{
			ColumnMetaData[] columnListTemp	= new ColumnMetaData[ columnList.length - 1 ];
			if ( columnIndex > 0 ){
				System.arraycopy( columnList, 0, columnListTemp, 0, columnIndex );
			}
			System.arraycopy( columnList, columnIndex+1, columnListTemp, columnIndex, columnList.length - columnIndex - 1 );
			columnList = columnListTemp;
			
			// rebuild the columnNameCache
			for ( int x=0; x < columnList.length; x++ ){
				columnNameCache.put( columnList[x].columnName, new Integer(x + 1) );
			}
		}
	}
	
	public synchronized int addColumn(String columnName) {
		return addColumn(new ColumnMetaData(columnName));
	}

	public synchronized int addColumn(String columnName, int type) {
		return addColumn(new ColumnMetaData(columnName, type));
	}

	private synchronized int addColumn(ColumnMetaData col) {
		ColumnMetaData[] temp = new ColumnMetaData[columnList.length + 1];
		System.arraycopy(columnList, 0, temp, 0, columnList.length);
		temp[columnList.length] = col;
		columnNameCache.put(col.columnName, new Integer(columnList.length + 1));
		columnList = temp;

		return columnList.length; // 1-based index of column just added
	}

	
	/*
	 * This is for renaming a column
	 */
	public boolean renameColumn(String oldcolumnName, String newcolumnName) {
		int columnIndex = getColumnIndex( oldcolumnName );
		if ( columnIndex == 0 ) return false;

		// make sure the new column doesn't already exist
		int chkcolumnIndex = getColumnIndex( newcolumnName );
		if ( chkcolumnIndex > 0 ) return false;
		
		// Update the cache
		columnNameCache.remove(oldcolumnName);
		columnNameCache.put(newcolumnName, new Integer(columnIndex) );
		
		columnList[columnIndex-1].columnName	= newcolumnName;
		
		return false;
	}

	
	/**
	 * Takes a 1-based column index, does a range check, and returns a 0-based
	 * (private) index. Throws an exception for index-out-of-range.
	 */
	private int internalColumnIndex(int column) throws SQLException {
		if ((column < 1) || (column > columnList.length))
			throw new SQLException("index out of range: " + column);

		return (column - 1);
	}

	private class ColumnMetaData implements java.io.Serializable {
		private static final long serialVersionUID = 1L;

		private boolean autoIncrement;

		private boolean caseSensitive;

		private boolean currency;

		private int nullable;

		private boolean signed;

		private boolean searchable;

		private int columnDisplaySize;

		private String columnLabel;

		private String columnName;

		private String columnClassName;

		private String schemaName;

		private int precision;

		private int scale;

		private String tableName;

		private String catalogName;

		private int columnType = java.sql.Types.NULL; // Default it to null

		private String columnTypeName;

		private boolean readOnly;

		private boolean writable;

		private boolean definitelyWriteable;

		private ColumnMetaData() {
		}

		private ColumnMetaData(String _columnName) {
			columnLabel = _columnName;
			columnName = _columnName;
		}

		private ColumnMetaData(String _columnName, int _type) {
			columnLabel = _columnName;
			columnName = _columnName;
			columnType = _type;
		}
	}

	/**************************************************************************
	 * 
	 * Methods below here implement the java.sql.ResultSetMetaData interface.
	 * 
	 **************************************************************************/

	public int getColumnCount() {
		return columnList.length;
	}

	public boolean isAutoIncrement(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].autoIncrement;
	}

	public boolean isCaseSensitive(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].caseSensitive;
	}

	public boolean isCurrency(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].currency;
	}

	public int isNullable(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].nullable;
	}

	public boolean isSigned(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].signed;
	}

	public int getColumnDisplaySize(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].columnDisplaySize;
	}

	public String getColumnLabel(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].columnLabel;
	}

	public String getColumnName(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].columnName;
	}

	public String getColumnClassName(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].columnClassName;
	}

	public String getSchemaName(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].schemaName;
	}

	public int getPrecision(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].precision;
	}

	public int getScale(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].scale;
	}

	public String getTableName(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].tableName;
	}

	public String getCatalogName(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].catalogName;
	}

	public int getColumnType(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].columnType;
	}

	public String getColumnTypeName(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].columnTypeName;
	}

	public boolean isReadOnly(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].readOnly;
	}

	public boolean isWritable(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].writable;
	}

	public boolean isDefinitelyWritable(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].definitelyWriteable;
	}

	public boolean isSearchable(int column) throws SQLException {
		return columnList[internalColumnIndex(column)].searchable;
	}

	/**************************************************************************
	 * 
	 * Methods below here are used to fill in the column information.
	 * 
	 **************************************************************************/

	public void setAutoIncrement(int column, boolean b) throws SQLException {
		columnList[internalColumnIndex(column)].autoIncrement = b;
	}

	public void setNullable(int column, int i) throws SQLException {
		columnList[internalColumnIndex(column)].nullable = i;
	}

	public void setColumnLabel(int column, String label) throws SQLException {
		columnList[internalColumnIndex(column)].columnLabel = label;
	}

	public void setColumnName(int column, String name) throws SQLException {
		columnList[internalColumnIndex(column)].columnName = name;
		columnNameCache.put(name, new Integer(column));
	}

	public void setColumnType(int column, int type) throws SQLException {
		columnList[internalColumnIndex(column)].columnType = type;
	}

	public void setReadOnly(int column, boolean b) throws SQLException {
		columnList[internalColumnIndex(column)].readOnly = b;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new SQLException("method not supported");
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLException("method not supported");
	}

}
