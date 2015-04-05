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
 * This class represents the result table from a select statement
 * where the select is not a group by but contains expressions 
 * in the select columns. Only one row of data is returned in the
 * result. 
 */

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

class groupByResultTable extends resultTable{
	
	// the first groupby column, subsequent groupby cols are hashtables within this one
	private Hashtable groupBy;
	private List<columnRef> groupByList; // arraylist of columnRef's
	private condition havingCondition;
 
 	public groupByResultTable( selectColumn[] _selCols, orderByCol [] _orderBy, List<columnRef> _groupByList, condition _havingCondition, boolean _distinct ){
		super(  _selCols, _distinct, _orderBy );
	  
		havingCondition = _havingCondition;
		
		groupByList = _groupByList;
		groupBy = new Hashtable();
		
 	}// groupByResultTable()
	
	public void processRow( rowContext _rowcontext, List<cfData> _pData, Map<String, String> _lookup ) throws cfmRunTimeException{
		cfData [] result;
		
		groupByResultRow row = getRow( _rowcontext, _pData );
		
		row.rowData.clear();
		cfData nextItem;
    
		for ( int i = 0; i < row.cols.length; i++ ){ // each selectColumn
			result =  row.cols[i].execute( _rowcontext, _pData );
			for ( int j = 0; j < result.length; j++ ){
        nextItem = result[j].duplicate();

        row.rowData.add( nextItem );
			}
		}
		
		if ( orderCols != null && row.orderData.size() == 0 ){
			
			initOrderByIndex( _rowcontext );
			for ( int i = 0; i < orderCols.length; i++ ){
				if ( orderByIndx[i] != -1 ){ 
					row.orderData.add( row.rowData.get( orderByIndx[i] ) );
				}else{
					int tablenameIndx = orderCols[i].getColName().indexOf( '.' );
					cfData val = null;
					if ( tablenameIndx != -1 ){
						val = _rowcontext.get( orderCols[i].getColName().substring( 0, tablenameIndx ), orderCols[i].getColName().substring( tablenameIndx+1 ) );
					}else{
						val = _rowcontext.get( orderCols[i].getColName() );
					}
					row.orderData.add( val );
				}
			}
		}
		
 	}

	
	// get the selectCol[] / arraylist from the hashtable
	// by getting the values of the group by columns from the rowcontext
	private groupByResultRow getRow( rowContext _rowContext, List<cfData> _pData ) throws cfmRunTimeException{
		Hashtable ht = groupBy;
		groupByResultRow rowData = null; // not arraylist
	
		for ( int i = 0; i < groupByList.size(); i++ ){
			String index = ( groupByList.get( i ) ).evaluate( _rowContext, _pData ).getString();
			if ( ht.containsKey( index ) ){
				if ( i == groupByList.size() - 1 ){
					rowData = (groupByResultRow) ht.get( index );
				}else{
					ht = (Hashtable) ht.get( index );
				}
			}else{
				// create it
				if ( i == groupByList.size() - 1 ){
					// create new selectCol/arraylist copying selectCols
					rowData = new groupByResultRow( getSelectColumnsCopy() );
					ht.put( index, rowData );
				}else{
					Hashtable temp = new Hashtable();
					ht.put( index, temp );
					ht = temp;
					//rowData = (datatype) ht.get( index );
				}
			}
		}
		
		
		
		return rowData;
	}// getRow()
	
	
	/**
	 * returns a List of Lists of cfData this is the data from the
	 * result table
	 * 
	 * @throws cfmRunTimeException
	 */
	public List<ResultRow> getResultData( Map<String, Integer> _indxLookup ) throws cfmRunTimeException {

		// iterate thru the hashtable adding all the arraylists
		getArrayListsFromHashtable( groupBy, groupByList.size(), this.resultRows );

		// if DISTINCT, then remove all the duplicate rows
		if ( distinct ) {
			List<ResultRow> distinctRows = new ArrayList<ResultRow>();
			for ( int i = 0; i < this.resultRows.size(); i++ ) {
				ResultRow row = this.resultRows.get( i );
				if ( !listContains( distinctRows, row ) ) {
					distinctRows.add( row );
				}
			}
			this.resultRows = distinctRows;
		}

		if ( this.havingCondition != null ) {
			List<ResultRow> selectedRows = new ArrayList<ResultRow>();
			for ( int i = 0; i < this.resultRows.size(); i++ ) {
				ResultRow row = this.resultRows.get( i );
				if ( this.havingCondition.evaluate( row, null, _indxLookup ) ) {
					selectedRows.add( row );
				}
			}
			this.resultRows = selectedRows;
		}

		return this.resultRows;
	}// getResultData()
	
	
	private void getArrayListsFromHashtable( Hashtable _ht, int _depth, List<ResultRow> _outList ){
		if ( _depth == 1 ){
			Enumeration rows = _ht.elements();
			while( rows.hasMoreElements() ){
				groupByResultRow nextRow = (groupByResultRow) rows.nextElement();
				_outList.add( new ResultRow( nextRow.rowData, nextRow.orderData ) );
			}

		}else{
			Enumeration elements = _ht.elements();
			while ( elements.hasMoreElements() ){
				getArrayListsFromHashtable( (Hashtable) elements.nextElement(), _depth - 1, _outList );
			}
		}
	}
	
	
	/**
	 * This returns shallow copies of the select columns in this result table.
	 * The main reason for this method is that functions will be deep copied,
	 * facilitating the group by.
	 */
	 
	private selectColumn[] getSelectColumnsCopy(){
		int no_cols = selCols.length;
		selectColumn[] colCopies = new selectColumn[ no_cols ];
		
		for ( int i = 0; i < no_cols; i++ ){
			colCopies[i] = selCols[i].shallowCopy();
		}
		
		return colCopies;
	}// getSelectColumnsCopy()
	
	
	class groupByResultRow {
		
		private selectColumn [] cols;
		private List<cfData> rowData;
		private List<cfData> orderData;
		
		public groupByResultRow( selectColumn [] _selCols ){
			cols = _selCols;
			rowData = new ArrayList<cfData>();
			orderData = new ArrayList<cfData>();
		}
		
	}
	
}
