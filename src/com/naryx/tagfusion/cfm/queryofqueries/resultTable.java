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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class resultTable{
 
 	protected List<ResultRow> resultRows;
 	
 	protected selectColumn[] selCols;
 	protected orderByCol[]   orderCols;
 	
	protected int [] orderByIndx; // the orderByIndx is used when there are ORDER BY columns specified. 

 	protected boolean distinct;
	
 	public resultTable( selectColumn[] _selCols, boolean _distinct, orderByCol [] _orderBy ){
	  distinct = _distinct;
		resultRows = new ArrayList<ResultRow>();
		selCols = _selCols;
		orderCols = _orderBy;
 	}
	
  public selectColumn [] getSelCols(){
  	return this.selCols;
  }
  
	public void processRow( rowContext _rowcontext, List<cfData> _pData, Map<String, String> _lookup ) throws cfmRunTimeException{
		cfData [] result;
		List<cfData> nextSelectedRow = new ArrayList<cfData>();
		List<cfData> nextOrderByRow = null;
		cfData nextItem;
		
		for ( int i = 0; i < selCols.length; i++ ){ // each selectColumn
			// execute 
			result = selCols[i].execute( _rowcontext, _pData );
			for ( int j = 0; j < result.length; j++ ){
				nextItem = result[j].duplicate();
				nextSelectedRow.add( nextItem ); 
			}
		}
		
		if ( orderCols != null ){
			initOrderByIndex( _rowcontext );
			nextOrderByRow = new ArrayList<cfData>();
			for ( int i = 0; i < orderCols.length; i++ ){
				if ( orderByIndx[i] != -1 ){ 
					nextOrderByRow.add( nextSelectedRow.get( orderByIndx[i] ) );
				}else{
					int tablenameIndx = orderCols[i].getColName().indexOf( '.' );
					cfData val = null;
					if ( tablenameIndx != -1 ){
						val = _rowcontext.get( orderCols[i].getColName().substring( 0, tablenameIndx ), orderCols[i].getColName().substring( tablenameIndx+1 ) );
					}else{
						val = _rowcontext.get( orderCols[i].getColName() );
					}
					nextOrderByRow.add( val );
				}
			}

		}
		
		ResultRow resultRow = new ResultRow( nextSelectedRow, nextOrderByRow );
		addRow( resultRow );
 	}

	
	/**
	 * returns an List of ResultRows 
	 * this is the data from the result table
	 */
	 
	public List<ResultRow> getResultData( Map<String, Integer> _indxLookup ) throws cfmRunTimeException{
		return resultRows;
	}
	
	// Used in implementing DISTINCT, this method returns true the List contains 
	// the given ResultRow. Note that the values of the order columns are considered 
	// in the comparison.
	
	protected boolean listContains( List<ResultRow> _mainList, ResultRow _listItem ) {
		boolean found = false;
		List<cfData> selected = _listItem.getSelectedColumns();
		
		for ( int i = 0; i < _mainList.size(); i++ ){
			// assumes each item in the list has same number of elements	
			List<cfData> nextItem = _mainList.get( i ).getSelectedColumns();
			found = true; 

			for ( int j = 0; j < nextItem.size(); j++ ){
				try {
					if ( !nextItem.get(j).equals( selected.get(j) ) ){
						found = false;
						break;
					}
				} catch ( cfmRunTimeException e ) {
					// should not happen
				}
			}
			
			if ( found ) return true;
		}
		return found;
	}
	
	private void addRow( ResultRow _row ){
		if ( distinct ){
			if ( !listContains( resultRows, _row ) ){
				resultRows.add( _row );
			}
		}else{
			resultRows.add( _row );
		}
	}
	
	/*
	 * When there are ORDER BY columns specified, in order to be most efficient we create an 
	 * array - the orderByIndx - that contains the index into the SELECT columns that should
	 * be used to get the value for that ORDER BY column. Note that it's possible that the 
	 * ORDER BY column may not be in the SELECT column list in which case the index will
	 * be set to -1
	 */
	protected void initOrderByIndex( rowContext _rowcontext ) throws cfmRunTimeException{
		if ( orderByIndx == null ){
			orderByIndx = new int[ orderCols.length ];
			for ( int i = 0; i < orderCols.length; i++ ){
				orderByCol nextCol = orderCols[i]; 
				if ( nextCol.isIndex() ){
					orderByIndx[i] = nextCol.getIndex(); //TODO: test
				}else{
					boolean found = false;
					for ( int j = 0; j < selCols.length; j++ ){
						if( nextCol.getColName().equalsIgnoreCase( selCols[j].getShortName() ) ||
								nextCol.getColName().equalsIgnoreCase( selCols[j].getFullName( _rowcontext ) ) ||
								nextCol.getColName().equalsIgnoreCase( selCols[j].getAlias() ) ){
							orderByIndx[i] = j;
							found = true;
							break;
						}
					}

					if ( !found ){
						int tablenameIndx = nextCol.getColName().indexOf( '.' );
						cfData val = null;
						if ( tablenameIndx != -1 ){
							val = _rowcontext.get( nextCol.getColName().substring( 0, tablenameIndx ), nextCol.getColName().substring( tablenameIndx+1 ) );
						}else{
							val = _rowcontext.get( nextCol.getColName() );
						}
						
						if ( val == null ){
							throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.expressionError", 
								"queryofqueries.invalidIndex", 
								new String[]{nextCol.getColName()} ) );
						}else{
							orderByIndx[i] = -1; // indicate that it should be looked up
						}
					}
				}					
			}
		}

	}
}
