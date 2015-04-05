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
 * This class represents a SQL select statement.
 * You can define the columns that will be selected using addSelectColumn()
 * and set the tables the select is from using setFromList()
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.runTime;

public class selectStatement{

	private List<selectColumn> selectList;
	private List<String> fromList;
	private List<columnRef> groupByList;
	protected List<orderByCol> orderByList;
	private List<String> referencedColumns; // used to validate referenced columns in the statement
	private List<String> aliases; // used for validation
		
	//private tableSource sourceTable; 
	private boolean distinct;
	private condition whereClause = null;
	private condition havingClause = null;
		
		
	selectStatement( List<selectColumn> _selectList, boolean _distinct ){
		selectList = _selectList;
		distinct = _distinct;
	}// selectStatement()
	
	private selectStatement( boolean _distinct, List<selectColumn> _selectList, List<String> _fromList, List<columnRef> _groupByList,	
									List<orderByCol> _orderByList, List<String> _referencedColumns, List<String> _aliases, 
									condition _where, condition _having ){
		distinct = _distinct;
		selectList = _selectList;
		fromList =  _fromList;
		groupByList = _groupByList;
		orderByList = _orderByList;
		referencedColumns = _referencedColumns;
		aliases = _aliases;
		whereClause = _where;
		havingClause = _having;
	}
	
	void addSelectColumn( selectColumn _selectCol ){
		selectList.add( _selectCol );
	}// setSelect()
	
	
	void setFromList( List<String> _fromList ){
		fromList = _fromList;
	}// setFromList()
	
	
	// sets the where condition that will filter table rows
	// within the tableSource
	void setWhereCondition( condition _whereClause ){
		whereClause = _whereClause;
	}// setWhereClause
	
	
	// sets the HAVING clause. Assumes the condition is valid.
	// i.e. 
	void setHavingCondition( condition _havingClause ){
		havingClause = _havingClause;
	}// setWhereClause
	
	
	void setGroupByList( List<columnRef> _groupByList ){
		groupByList = _groupByList;
	}// setGroupByList()
	
	
	void setOrderByList( List<orderByCol> _orderBy ){
		orderByList = _orderBy;	
	}// setOrderByList()
	
	
	void setReferencedColumns( List<String> _referencedColumns ){
		referencedColumns = _referencedColumns;	
	}// setReferencedColumns()

	public void setAliases( List<String> _aliases ){
		aliases = _aliases;	
	}
	
	tableSource createTableSource( cfSession _Session, List<cfData> _pData ) throws cfmRunTimeException{
		Map<String, cfQueryResultData> tabs = new FastMap<String, cfQueryResultData>();
		
		// convert the Arraylist of tables to a Hashtable with table name as the key
		Iterator<String> froms = fromList.iterator();
		while ( froms.hasNext() ){
			String tableName = froms.next();
			cfData table = runTime.runExpression( _Session, tableName );
			if ( table == null ){
				throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.expressionError", 
																																				"queryofqueries.badTable", 
																																				new String[]{tableName} ) );
			}
			tabs.put( tableName, (cfQueryResultData)table );
		}
		
		// initialise the table source
		if ( whereClause == null ){
			return new tableSource( tabs, _pData );
		}else{
			return new filteredTableSource( tabs, whereClause, _pData );
		}
	}// createTableSource()
	
	public selectStatement copy(){
		List<selectColumn> copiedSelectCols = new ArrayList<selectColumn>();
		selectColumn nextCol;
		for ( int i = 0; i < this.selectList.size(); i++ ){
			nextCol = selectList.get(i);
			copiedSelectCols.add( nextCol.shallowCopy() );
		}
		return new selectStatement( distinct, copiedSelectCols, fromList, groupByList,	
				orderByList, referencedColumns, aliases, whereClause, havingClause );
    }
	
	cfQueryResultData execute( cfSession _Session, List<cfData> _pData ) throws cfmRunTimeException {
		resultTable resTable;
		tableSource source;
		
		// create the source table from the FROM elements
		source = createTableSource( _Session, _pData );
		
		// at this point some validation has already been done in validateGroupBy(), validateHaving()
		validateStatement( source );
		
		// initialise the select columns 
		selectColumn[] selCols = getSelectColumns();
		int totalNoCols = 0;
		rowContext rctxt = source.getInitRow();
		Map<String, String> lookup = new FastMap<String, String>();
		Map<String, Integer> indxLookup = new FastMap<String, Integer>();
		
		int zIndx = 0;
		for ( int j = 0; j < selCols.length; j++ ){
			selCols[j].initResultData( rctxt );
			String [] colNames = selCols[j].getColumnNames();
			totalNoCols += colNames.length;
			zIndx = createLookupEntries( rctxt, lookup, indxLookup, selCols[j], zIndx );
		}
		
		orderByCol [] obCols = null;
		if ( orderByList != null ){
			obCols = new orderByCol[ orderByList.size() ];
			for ( int i = 0; i < obCols.length; i++ ){
				obCols[i] = orderByList.get( i );
			}
		}
		
		if ( groupByList != null ){
			resTable = new groupByResultTable( selCols, obCols, groupByList, havingClause, distinct );
		}else if ( selCols[0].isAggregateFunction() ){ // this differs cos there is only one returned row
			resTable = new expressionResultTable( selCols, obCols );
		}else{
			resTable = new resultTable( selCols, distinct, obCols );
		}
		
		while ( source.hasNext() ){
			resTable.processRow( source.nextRow(), _pData, lookup );
		}
		
		selCols = resTable.getSelCols();
		
		// get Column names
		// need a String[] in the end so need something from selectColumns
		String [] allColNames = new String [ totalNoCols ];
		int [] allColTypes = new int [ totalNoCols ];
		//List allExprStrs
		int index = 0;
		for ( int n = 0; n < selCols.length; n++ ){
			String [] colNames = selCols[n].getColumnNames();
			int [] colTypes = selCols[n].getColumnTypes();
			for ( int m = 0; m < colNames.length; m++ ){
				allColNames[index] = colNames[m];
				if ( colTypes != null ) {
					allColTypes[index] = colTypes[m];
				}
				index++;
			}
		}
    
		if ( allColTypes.length == 0 ) {
			allColTypes = null;
		}
		
		// create a new query & return it.
		cfQueryResultData qoqRes = new cfQueryResultData( allColNames, allColTypes, "Query of Queries" );
		
		List<ResultRow> results = resTable.getResultData( indxLookup );
    if ( results.size() > 0 ){
      qoqRes.setQueryData( doOrderBy( results, lookup, qoqRes ) );
    }
		return qoqRes;
	}//execute()

	
  private static int createLookupEntries( rowContext _rc, Map<String, String> _lookup, Map<String, Integer> _ilookup, selectColumn _col, int _index ){
    int index = _index;
  	switch( _col.getColumnType() ){
      case selectColumn.TABLEANDCOLUMN:
      case selectColumn.COLUMN:
        String colName = _col.getColumnNames()[0];
        _lookup.put( _col.getShortName().toLowerCase(), colName );
        _lookup.put( _col.getFullName( _rc ).toLowerCase(), colName );
        _ilookup.put( _col.getShortName().toLowerCase(), new Integer( index ) );
        _ilookup.put( _col.getFullName( _rc ).toLowerCase(), new Integer( index ) );
        if ( _col.getAlias() != null ){
          _lookup.put( _col.getAlias().toLowerCase(), _col.getShortName() );
					_ilookup.put(_col.getAlias().toLowerCase(), new Integer(index));
        }
        index++;
        break;
        
      case selectColumn.TABLEANDASTERISK:
        String tablename = _col.getTable();
        String [] colNames = _rc.getTableColumnNames( tablename );
        
        for ( int i = 0; i < colNames.length; i++ ){
          _lookup.put( colNames[i].toLowerCase(), colNames[i] );
          _lookup.put( (tablename.toLowerCase() + "." + colNames[i]).toLowerCase(), colNames[i] );
					_ilookup.put(colNames[i].toLowerCase(), new Integer(index));
					_ilookup.put((tablename.toLowerCase() + "." + colNames[i]).toLowerCase(), new Integer(index));
          index++;
        }
        break;

      case selectColumn.ASTERISK:
        String [] tablenames = _rc.getTableNames();
        for ( int j = 0; j < tablenames.length; j++ ){
          String [] columnNames = _rc.getTableColumnNames( tablenames[j] );
      
          for ( int i = 0; i < columnNames.length; i++ ){
            _lookup.put( columnNames[i].toLowerCase(), columnNames[i] );
            _lookup.put( (tablenames[j].toLowerCase() + "." + columnNames[i]).toLowerCase(), columnNames[i] );
						_ilookup.put(columnNames[i].toLowerCase(), new Integer(index));
						_ilookup.put((tablenames[j].toLowerCase() + "." + columnNames[i]).toLowerCase(), new Integer(index));
            index++;
          }
        }
        break;
      case selectColumn.AGGREGATEFUNCTION:
				_ilookup.put(_col.toString(), new Integer(index));
      	if ( _col.getAlias() != null ){
      		_lookup.put( _col.getAlias(), _col.getAlias() );
					_ilookup.put(_col.getAlias(), new Integer(index));
      	}
      	index++;
	  default:
	  	break;
    }
  	return index;
  }
	
	protected List<List<cfData>> doOrderBy( List<ResultRow> _data, Map<String, String> _lookup, cfQueryResultData _query )
			throws cfmRunTimeException {

		// do sort if necessary
		if ( orderByList != null ) {
			// make a copy of the ORDER BY list
			List<orderByCol> orderByCopy = new ArrayList<orderByCol>( orderByList.size() );
			for ( int j = 0; j < orderByList.size(); j++ ) {
				orderByCopy.add( orderByList.get( j ).copy() );
			}

			// do the sort
			Collections.sort( _data, new MultiColumnComparator( orderByCopy ) );

		} 

		// create the cfData result rows from the ResultRows
		List<List<cfData>> returnData = new ArrayList<List<cfData>>( _data.size() );
		
		for ( int i = 0; i < _data.size(); i++ ) {
			ResultRow nextRow = (ResultRow)_data.get( i );
			List<cfData> rowData = nextRow.getSelectedColumns();
			for ( int j = 0; j < rowData.size(); j++ ){
				rowData.get(j).setQueryTableData( returnData, j+1 );
			}
			returnData.add( rowData );
		}

		return returnData;
	}// doOrderBy()
	
	
	class MultiColumnComparator implements Comparator<ResultRow> {
		orderByCol [] colList;
		
		MultiColumnComparator( List<orderByCol> _cols  ) {
			colList = new orderByCol[ _cols.size() ];
			for ( int i = 0; i < colList.length; i++ ){
				colList[i] = _cols.get( i );
			}
		}// MultiColumnComparator()
		
		
		public int compare( ResultRow _resultRow1, ResultRow _resultRow2 ){
     	orderByCol obCol;
			cfData dataItem1, dataItem2; // the components of the 2 rows to compare

			List<cfData> orderData1 = _resultRow1.getOrderByColumns();
			List<cfData> orderData2 = _resultRow2.getOrderByColumns();

			// loop thru the colList returning if o1 and o2 are not equal on a column
			for ( int i = 0; i < colList.length; i++ ){
				obCol = colList[i];
					
				dataItem1 = orderData1.get( i );
				dataItem2 = orderData2.get( i );
					
				int compResult = cfData.compare( dataItem1, dataItem2 );
					
				if ( compResult != 0 ){ // if items not equal
					if ( obCol.isAscending() ){
						return compResult; 
					}else{
						return -1 * compResult; // return the reverse of the comparison result
					}
				}
			}
			return 0;				
		}//compare()
		
		
	}// MultiColumnComparator

	
	// converts the selectList VectorArrayList to an array of selectColumns
	private selectColumn[] getSelectColumns(){
 		
		if ( groupByList != null ){
	 		for ( int i = 0; i < groupByList.size(); i++ ){
	 			expression groupByCol = groupByList.get(i);
	 			if ( groupByCol instanceof columnRef ){
	 				columnRef col = (columnRef) groupByCol;
	 				boolean found = false;
	 				for ( int j = 0; j < selectList.size(); j++ ){
	 					selectColumn selCol = selectList.get(j);
	 					switch( selCol.getColumnType() ){
	 						case selectColumn.COLUMN:
	 							if ( selCol.getShortName().equalsIgnoreCase( col.colName ) ){
	 								found = true;
	 							}else if ( selCol.getAlias() != null && selCol.getAlias().equalsIgnoreCase( col.colName ) ){
	 								found = true;
	 							}
	 							break;
	 						case selectColumn.ASTERISK:
	 							found = true;
	 							break;
	 						case selectColumn.TABLEANDASTERISK:
	 							if ( col.tableNamed && selCol.getTable().equalsIgnoreCase( col.table ) ){
	 								found = true;
	 							}
	 							break;
	 						case selectColumn.TABLEANDCOLUMN:
	 							if ( col.tableNamed && selCol.getTable().equalsIgnoreCase( col.table ) && selCol.getShortName().equalsIgnoreCase( col.colName ) ){
	 								found = true;
	 							}
	 							
	 					}
	 					if ( found )
	 						break;

	 				}
	 				
	 				if ( !found ){
	 					selectList.add( 0, col.tableNamed ? new selectColumn( col.table, col.colName ) : new selectColumn( col.colName ) ); 
	 				}
	 			}
	 		}
		}
		
		Object [] objArray = selectList.toArray();
		selectColumn [] selArray = new selectColumn[ objArray.length ];
		
		for ( int i = 0; i < objArray.length; i++ ){
			selArray[i] = (selectColumn) objArray[i];
			if ( selArray[i].isAggregateFunction() ){ //Expression() ){
				selArray[i].getExpression().reset();
			}
		}
		
		return selArray;
	}// getSelectColumns()
	
	
	/**
	 * returns true if all the columns specified in the select are available
	 * in the group by.
	 */
	 
	boolean validateGroupBy(){
		selectColumn selectCol;
		String selectColStr;
		boolean fullRef = false;
		boolean found = false;
		
		String [] groupByStrs = getGroupByStrs();
			
		// loop thru' the columns in the select list 
		Iterator<selectColumn> selectByCols = selectList.iterator();
		
		while ( selectByCols.hasNext() ){
			selectCol =  selectByCols.next();
			switch ( selectCol.getColumnType() ){
				case selectColumn.EXPRESSION:
					break;		
				case selectColumn.TABLEANDCOLUMN:
				case selectColumn.COLUMN:
		
					selectColStr = ( selectCol.table.equals( "" ) ? selectCol.columnName : selectCol.table + "." + selectCol.columnName ).toLowerCase();
					
					fullRef = selectColStr.indexOf( '.' ) != -1;
					found = false;

					for ( int i = 0; i < groupByStrs.length; i++ ){
						if ( groupByStrs[i].equals( selectColStr ) 
							|| ( !fullRef && groupByStrs[i].endsWith( selectColStr ) )
							|| ( fullRef && selectColStr.endsWith( groupByStrs[i] ) ) 
							|| ( selectCol.alias != null && selectCol.alias.equalsIgnoreCase( groupByStrs[i] ) ) ){
							found = true;
							break;
						}
					}
					
					// might want to throw an exception instead saying which column not found
					// in the group by
					if ( !found ) return false;
					
					break;
				case selectColumn.TABLEANDASTERISK:
				case selectColumn.ASTERISK:	
					return false;
	
				default:
					break;
			
			}// switch
		}
		return true;
		
	}// validateGroupBy()
	
	
	/**
	 * returns true if all the columns specified in the having condition
	 * are specified in the group by
	 */
	 
	boolean validateHaving( List<String> _refdCols ){
		if ( groupByList == null ){
			return true;
		}
		
		// create string [] for better performance 
		String [] groupByStrs = getGroupByStrs(); 
		
		for ( int i = 0; i < _refdCols.size(); i++ ){
			String col = _refdCols.get( i );
			boolean found = false;
			boolean fullRef = col.indexOf( '.' ) != -1;
			
			for ( int j = 0; j < groupByList.size(); j++ ){
				// handles people.id 
				if ( groupByStrs[j].equals( col ) 
					|| ( !fullRef && groupByStrs[j].endsWith( col ) )
					|| ( fullRef && col.endsWith( groupByStrs[j] ) ) ){
					found = true;
					break;
				}
			}
			
			if ( !found ){
				// could throw an exception that this column is not specified in the group by
				return false;
			}
		}
		
		return true;
	}// validateHaving()
	
	
	private String [] getGroupByStrs(){
		String [] groupByStrs = new String[ groupByList.size() ];
		for ( int i = 0; i < groupByList.size(); i++ ){
			groupByStrs[i] = groupByList.get(i).toString();//.toLowerCase();
		}
		return groupByStrs;
	}// getGroupByStrs()
	
	
	private void validateStatement( tableSource _source ) throws cfmRunTimeException{
		
		if ( referencedColumns != null ){
			// check select columns occur in table
			// if occur in more than one table throw ambiguity exception
			// if don't occur in any table then throw invalid column in select exception
			
			for ( int i = 0; i < referencedColumns.size(); i++ ){
				String refdCol = referencedColumns.get(i);
				int dotIndex = refdCol.indexOf( '.' ); 
				if ( dotIndex == -1 ){ // unqualified col name
					// could throw an exception for ambiguity in this case
					if ( !_source.existsColumn( refdCol ) ) { //&& !aliases.contains( refdCol.toLowerCase() ) ){
						throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.expressionError", 
																																						"queryofqueries.invalidSelect", 
																																						new String[]{refdCol} ) );
					}
				}else{ // fully qualified name
					if ( !_source.existsTableColumn( refdCol.substring( 0, dotIndex ), refdCol.substring( dotIndex+1 ) ) ){

						throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.expressionError", 
																																						"queryofqueries.invalidSelect", 
																																						new String[]{refdCol} ) );
					}
				}
			}
		}
	}// validateStatement
	
	
	public String toString(){
		StringBuilder selectStr = new StringBuilder(40);
		selectStr.append( "SELECT " );
		
		Iterator<selectColumn> selects = selectList.iterator();
		
		while ( selects.hasNext() ){
			selectStr.append( selects.next().toString() );
			selectStr.append( ',' );
		}
	
		// delete the trailing comma
		selectStr.setLength( selectStr.length() - 1 );
		selectStr.append( " FROM " );
				
		Iterator<String> froms = fromList.iterator();
		
		while ( froms.hasNext() ){
			selectStr.append( froms.next() );
			selectStr.append( ',' );
		}
	
		// delete the trailing comma
		selectStr.setLength( selectStr.length() - 1 );
		
		return selectStr.toString();
		
	}// toString()
	

}// selectStatement
