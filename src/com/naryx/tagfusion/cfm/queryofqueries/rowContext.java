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
 * This class represents a single row from the combination of tables in the FROM line.
 */
 
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
 
class rowContext{
	
	Hashtable<String, tableRow> tables;
		
	rowContext(){
		tables = new Hashtable<String, tableRow>();
	}// rowContext()
	
	
	/**
	 * sets up the row context ready for row data to be added.
	 * @param _tableNames - the list of tables for this rowContext
	 * @param _columnNames - the list of column name lists.
	 */
	 
	void initTable( String _tableName, String [] _columnNames, int [] _columnTypes ){
		// note that this allows for columns to be index
		tableRow row = new tableRow();
		row.setColumnNames( _columnNames );
		row.setColumnTypes( _columnTypes );
		tables.put( _tableName, row );
	}// init()
	
	
	/**
	 * adds a new row that represents a row in the table '_tablename'
	 */
	 
	void addTableRow( String _tablename, List<cfData> _rowData ){
		tables.get( _tablename ).setRowData( _rowData );
	}// addTableRow
	
	
	/**
	 * clears the rowcontext of the current row data.
	 */
	 
	void reset(){
		/*Enumeration elements = tables.elements();
		while ( elements.hasMoreElements() ){
			( (tableRow) elements.nextElement() ).*/
	}// reset()
	
	
	/**
	 * returns the cfData located in table.col for this row.
	 * returns null if it cannot be found.
	 */
	 
	cfData get( String _table, String _col ){
		//cfData val = (cfData) ( (Hashtable) tables.get( _table ) ).get( _col );
		
		return tables.get( _table ).getColumn( _col );
	}// get()
	

	/**
	 * returns the cfData located in col for this row.
	 * returns null if it cannot be found.
	 */
	 
	cfData get( String _col ){
		cfData val = null;
		// loop thru the tables trying to get _col
		Enumeration<tableRow> tabs = tables.elements();
		
		while( tabs.hasMoreElements() ){
			tableRow row = tabs.nextElement(); //tables.get( i );
			val = row.getColumn( _col );
			
      if ( val != null ){
				return val;
			}
		}
		
		return val;
	}// get()
	
  public String getTableName( String _col ){
    Enumeration<String> tableNames = tables.keys();
    while( tableNames.hasMoreElements() ){
      String nextName = tableNames.nextElement();
      tableRow row = tables.get( nextName );
      String [] colNames = row.getColumnNames();
      for ( int i = 0; i < colNames.length; i++ ){
        if ( _col.equalsIgnoreCase( colNames[i] ) ){
          return nextName;
        }
      }
    }
    return "";
  }

  public String [] getTableNames(){
    String [] tableNames = new String [tables.size()];
    Enumeration<String> enumer = tables.keys();
    int i = 0;
    while( enumer.hasMoreElements() ){
      tableNames[i] = enumer.nextElement();
      i++;
    }
    return tableNames;
  }

	/**
	 * returns all the row data for the table with the name _table
	 * in the cfData array. Assumes that _row.length will match
	 * the no. of columns in the table row.
	 */
	 
	void getAllFromTable( String _table, cfData [] _row ){
		List<cfData> tableRow = tables.get( _table ).getRowData();
		
		for ( int i = 0; i < _row.length; i++ ){
			_row[i] = tableRow.get( i );
		}
		
	}// get AllFromTable()
	
	
	/**
	 * returns all the row data from all the tables.
	 */
	 
	void getAll( cfData [] _row ){
		int index = 0;
		Enumeration<String> keys = tables.keys();
		
		while ( keys.hasMoreElements() ){
			List<cfData> tableRow = tables.get( keys.nextElement() ).getRowData();
		
			for ( int i = 0; i < tableRow.size(); i++ ){
				_row[ index ] = tableRow.get( i );
				index++;
			}
		}
		
	}// getAll()
	
	
	/**
	 * returns a String[] containing all the column names for a particular table
	 */
	 
	String [] getTableColumnNames( String _table ){
		return tables.get( _table ).getColumnNames();
		
	}// getTableColumnNames()
	
	
	
	String [] getAllColumnNames(){
		List<String[]> allNamesList = new ArrayList<String[]>();
		int totalCols = 0;
		Enumeration<String> keys = tables.keys();
		
		// loop thru' all the tables getting all the column names
		while ( keys.hasMoreElements() ){
			String [] colNames = getTableColumnNames( keys.nextElement() );
			allNamesList.add( colNames );
			totalCols += colNames.length;
		}
		
		String [] allTableColNames = new String [ totalCols ];
		int allColumnsIndex = 0;
		for ( int i = 0; i < allNamesList.size(); i++ ){
			String [] tableCols = allNamesList.get(i);
			for ( int j = 0; j < tableCols.length; j++ ){
				allTableColNames[ allColumnsIndex ] = tableCols[ j ];
				allColumnsIndex++;
			}
		}
		
		return allTableColNames;
	}// getAllColumnNames()
	
	int [] getAllColumnTypes(){
		List<int[]> allTypesList = new ArrayList<int[]>();
		int totalCols = 0;
		Enumeration<String> keys = tables.keys();
		
		// loop thru' all the tables getting all the column types
		while ( keys.hasMoreElements() ){
			int [] colTypes = getTableColumnTypes( keys.nextElement() );
			allTypesList.add( colTypes );
			totalCols += colTypes.length;
		}
		
		int [] allTableColTypes = new int [ totalCols ];
		int allColumnsIndex = 0;
		for ( int i = 0; i < allTypesList.size(); i++ ){
			int [] tableCols = allTypesList.get(i);
			for ( int j = 0; j < tableCols.length; j++ ){
				allTableColTypes[ allColumnsIndex ] = tableCols[ j ];
				allColumnsIndex++;
			}
		}
		
		return allTableColTypes;
	}
	
	
	int [] getTableColumnTypes( String _table ){
		return tables.get( _table).getColumnTypes();
	}
	
	class tableRow {
		
		String [] colNames;
		int [] colTypes;
		List<cfData> rowData;
		
		String [] getColumnNames(){
			return colNames;
		}
		
		void setColumnNames( String [] _colNames ){
			colNames = _colNames;
		}
		
		int [] getColumnTypes(){
			return colTypes;
		}
		
		void setColumnTypes( int [] _colTypes ){
			colTypes = _colTypes;
		}
		
		List<cfData> getRowData(){
			return rowData;
		}
		
		void setRowData( List<cfData> _rowData ){
			rowData = _rowData;
		}
		
		
		cfData getColumn( String _colName ){
			for ( int i = 0; i < colNames.length; i++ ){
				if ( colNames[i].equalsIgnoreCase( _colName ) ){ // FIX
					return rowData.get( i );
				}
			}
			return null;
		}
		
	}// tableRow
	
}// rowContext
