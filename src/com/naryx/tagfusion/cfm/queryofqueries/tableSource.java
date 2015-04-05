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

package com.naryx.tagfusion.cfm.queryofqueries;

/**
 * This class represents a table constructed from a list of tables.
 */

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
 
class tableSource{
	 
	tableDesc [] tables; // hold cfQueryResultDatas
	String [] columnNames;
	rowContext currentRow; 
	boolean hasNext; 
	protected List<cfData> pData;
	
	// a current row index for each table, used to ensure that every row combination
	// between tables is covered
	int [] rowIndex; 
	int [] maxRowIndex;   	
	
	tableSource( Map<String, cfQueryResultData> _tables, List<cfData> _pData ){
		hasNext = true;
		pData   = _pData;
		currentRow = new rowContext();
		initTables( _tables );
		
		int numTables = tables.length;
		rowIndex = new int[ numTables ]; // all init'd to 0 
		maxRowIndex = new int[ numTables ];
		for ( int i = 0; i < numTables; i++ ){ 
			maxRowIndex[ i ] = tables[i].data.getNoRows() - 1;
		}
	
	}// tableSource()
	
		
	private void initTables( Map<String, cfQueryResultData> _tables ){
		tables = new tableDesc[ _tables.size() ];
		int index = 0;
		
		Iterator<String> tableNames = _tables.keySet().iterator();
		while ( tableNames.hasNext() ){
			String tableName = tableNames.next();
			cfQueryResultData data = _tables.get( tableName );
			if ( data.getNoRows() == 0 ){
				hasNext = false;
			}
			tables[ index ] = new tableDesc( tableName, data );

			try {
				ResultSetMetaData metaData = data.getMetaData();
				int numCols = metaData.getColumnCount();
				
				String[] colsNameArray = new String[ numCols ];
				int[] colsTypeArray = new int[ numCols ];
				
				for ( int i = 0; i < numCols; i++ ) {
					colsNameArray[ i ] = metaData.getColumnName( i+1 );
					colsTypeArray[ i ] = metaData.getColumnType( i+1 );
				}
				
				currentRow.initTable( tableName, colsNameArray, colsTypeArray ); // init rowContext for current table
			} catch ( SQLException e ) {
				// thrown by data.getMetaData() - should never happen
				com.nary.Debug.printStackTrace( e );
			} finally {
				index++;
			}
		}
		
	}// initTables()
	
	
	rowContext getInitRow(){
		return currentRow;
	}// getInitRow()
	
	
	boolean hasNext() throws cfmRunTimeException{
		return hasNext;
	}// hasNext()
	
	
	/** 
	 * returns a rowContext representing the next row in the table
	 */
	 
	rowContext nextRow() {
		currentRow.reset(); 
		
		for ( int i = 0; i < tables.length; i++ ){
			currentRow.addTableRow( tables[i].name, tables[i].data.getRow( rowIndex[i] ) );
		}
		
		// calculate next row indices so that can determine if there is a next row
		calculateNextRowIndices();
		return currentRow;
	}// nextRow()

	
	// calculates the index into the array list of each of the sub tables
	// so that the rows can be combined to return the nextRow()
	void calculateNextRowIndices(){
		int lastIndex = rowIndex.length - 1;
		
		// loop thru all; breaking out as soon as have incremented successfully
		for ( int i = 0; i < rowIndex.length; i++ ){
			if ( rowIndex[ i ] < maxRowIndex[ i ] ){
				rowIndex[ i ]++;
				break;
			}else{ // equals no rows
				// if all at last
				if ( i == lastIndex /* && rowIndex[i] == maxRowIndex[i] */ ){
					hasNext = false;
				}
				rowIndex[ i ] = 0;
			}
		}
				
	}// calculateNextRowIndices()
	
	
	boolean existsTableColumn( String _table, String _column ){
		boolean exists = false;
		tableDesc table = null;

		// loop thru' all the tables til find req'd one
		for ( int i = 0; i < tables.length; i++ ){
			if ( tables[i].name.equals( _table ) ){
				table = tables[i];
				break;
			}
		}
			
		if ( table != null ){
			// if found in this table
      if ( _column.equals( "*" ) ){
        exists = true;
      }else if ( table.containsColumn( _column ) ){
				exists = true;
			}
		}
		
		return exists;
	}// existsTableColumn()
	
	
	/**
	 * searches thru' all the tables looking for the column specified, returning true
	 * if it is found in a table. 
	 * @throws cfmRunTimeException if the column occurs in more than one table
	 */
	 
	boolean existsColumn( String _column ) throws cfmRunTimeException{
		boolean exists = false;
		// loop thru' all the tables
		for ( int i = 0; i < tables.length; i++ ){
			
			// if found in this table
			if ( tables[i].containsColumn( _column ) ) {
				// if already found and found in this table too then throw an exception
				if ( exists ){
					throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.expressionError", 
																																				"queryofqueries.badColumn", 
																																				new String[]{_column} ) );
				}else{
					exists = true;
				}
			}
		}
		
		return exists;
	}// existsColumn()
	
	
	class tableDesc{
		String name;
		cfQueryResultData data;
		String [] colNames;
		
		tableDesc( String _name, cfQueryResultData _data ){
			name = _name;
			data = _data;
			colNames = data.getColumnList();
		}// tableDesc()
		
		boolean containsColumn( String _col ){
			for ( int i = 0; i < colNames.length; i++ ){
				if ( colNames[i].equalsIgnoreCase( _col ) ){
					return true;	
				}
			}
			return false;
		}
		
	}//tableDesc
	
}// tableSource

