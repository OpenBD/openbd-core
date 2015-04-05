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
 
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultSetMetaData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class unionStatement extends selectStatement{

	private selectStatement statement1, statement2;
	private boolean all;
	
	unionStatement( selectStatement _statement1, selectStatement _statement2, boolean _all ){
		super(null,true);
		statement1 = _statement1;
		statement2 = _statement2;
		all = _all;
	
	}// unionStatement()

	
  public selectStatement copy(){
    unionStatement copy = new unionStatement( statement1.copy(), statement2.copy(), all );
    copy.setOrderByList( this.orderByList );
    return copy;
  }
  
	cfQueryResultData execute( cfSession _session, List<cfData> _pData ) throws cfmRunTimeException {
		cfQueryResultData result1 = statement1.execute( _session, _pData );
		cfQueryResultData result2 = statement2.execute( _session, _pData );
		
		// THROW EXCEPTION IF COL COUNTS DON"T MATCH
		if ( result1.getNoColumns() != result2.getNoColumns() ){
			throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.expressionError", 
																			 "queryofqueries.invalidUnion", 
																			 null ) );
		}
		
		cfQueryResultData resultData; 
		if ( all ){
			resultData = unionAll( result1, result2 );
		}else{
			resultData = unionDistinct( result1, result2 );
		}
		
		doOrderBy( resultData );
		return resultData;
	}// execute()

	
	private void doOrderBy( cfQueryResultData _data ) throws cfmRunTimeException{
		if ( orderByList == null ) {
			return;
		}
		List<orderByCol> orderByCopy = new ArrayList<orderByCol>( orderByList.size() );
		for ( int j = 0; j < orderByList.size(); j++ ){
			orderByCopy.add( orderByList.get(j).copy() ); 
		}
    
		cfQueryResultSetMetaData queryMetaData = null;
		try {
			queryMetaData = ( (cfQueryResultSetMetaData) _data.getMetaData() );
		} catch (SQLException e) { // shouldn't happen
			throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.expressionError", 
					"queryofqueries.badMetadata", 
					new String[]{e.getMessage()} ) );

		}

		String[] colNames = queryMetaData.getColumnNames();

		// validate the order by list first
		int no_cols = colNames.length;

		for ( int i = 0; i < orderByCopy.size(); i++ ){
			orderByCol col = orderByCopy.get( i );
			if ( col.isIndex() ){ // if column is an index then check it's within the number of accessible columns
				if ( col.getIndex() > no_cols || col.getIndex() < 1 ){
					// invalid index
					throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.expressionError", 
																																					"queryofqueries.invalidIndex", 
																																					new String[]{col.getIndex()+""} ) );
				}
				
			}else{
				boolean found = false;
				// loop over the column names looking for a match
				for ( int j = 0; j < colNames.length; j++ ){
					if ( colNames[j].equalsIgnoreCase( col.getColName() ) ){
						found = true;
						break;
					}
				}

				if ( !found ){
					 throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.expressionError", 
								"queryofqueries.invalidIndex", 
								new String[]{col.getColName()} ) );
				}
				
				// if it wasn't found then check if it's in the lookup
				/*if ( !found ){
          String actualColname = (String) _lookup.get( col.getColName() );
          if ( actualColname != null ){
            col.setColName( actualColname );
          }else{
            // invalid column name 
            throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.expressionError", 
																																					"queryofqueries.invalidIndex", 
																																					new String[]{col.getColName()} ) );
          }
        }*/
			}
		}
		
		_data.sort( orderByCopy );
	}
	
	private static cfQueryResultData unionAll( cfQueryResultData _data1, cfQueryResultData _data2 ) {
		List<List<cfData>> tableRows2 = _data2.getTableRows();
		List<List<cfData>> tableRows1 = _data1.getTableRows();
		
		// reset the table rows of the first query to contain the rows of the second query also
		// it's safe to return the first query as it will not be reused since the context is 
		// within a query of queries
		for ( int i = 0; i < tableRows2.size(); i++ ){
			List<cfData> nextRow = tableRows2.get( i );
			// since we're moving these to a different query we need to update the queryTableData for each cfData in the row
			for ( int j = 0; j < nextRow.size(); j++ ){ 
				( (cfData) nextRow.get( j ) ).setQueryTableData( tableRows1, j+1 );
			}
			
		}
		_data1.getTableRows().addAll( tableRows2 );
		return _data1;
	}// unionAll()
	
	
	private cfQueryResultData unionDistinct( cfQueryResultData _data1, cfQueryResultData _data2 ) {
		List<List<cfData>> tableRows1 = _data1.getTableRows();
		List<List<cfData>> tableRows2 = _data2.getTableRows();
		List<List<cfData>> resultRows = new ArrayList<List<cfData>>();

		Comparator<List<cfData>> comp = new RowComparator();
		
		// as cfmx does with a union, 
		Collections.sort( tableRows1, comp );
		addDistinct( resultRows, tableRows1, comp );
		
		addDistinct( resultRows, tableRows2, comp );
		
		// note the reuse of _data1; safe to do so since _data1 is within the context
		// of the qoq and will not be reused elsewhere
		_data1.setQueryData( resultRows );
		return _data1;
	}// unionDistinct()

	
	/*
	 * adds all the objects in the _from list to the _to list that
	 * don't already exist there (equivalence test not instance test)
	 */
	 
	private static void addDistinct( List<List<cfData>> _to, List<List<cfData>> _from, Comparator<List<cfData>> _comp ){
		int fromSize = _from.size();
		
		// DEAL WITH: this could throw an exception on the cfData.equals()
		// catch it here? or throw it up?? or does it throw an exception at all?
		// it might be caught within the _to.contains
		for ( int i = 0; i < fromSize; i++ ){
			List<cfData> next = _from.get( i );
			int find = Collections.binarySearch( _to, next, _comp ); 
			if ( find < 0 ){
				// since we're moving these to a different query we need to update the queryTableData for each cfData in the row
				for ( int j = 0; j < next.size(); j++ ){
					( (cfData) next.get( j ) ).setQueryTableData( _to, j+1 );
				}
				_to.add( -1 * ( find + 1 ), next );
			}
		}
		
	}// addDistinct()
	
	private static class RowComparator implements Comparator<List<cfData>> {

		public int compare( List<cfData> o1, List<cfData> o2 ) {
			return compareRows( o1, o2 );
		}
		
	}
	
	private static int compareRows( List<cfData> _row1, List<cfData> _row2 ){
		// INVARIANT: _row1.size() == _row2.size()
		for ( int i = 0; i < _row2.size(); i++ ){
			int compareResult = cfData.compare( _row1.get(i), _row2.get(i) );
			if ( compareResult != 0 ){
				return compareResult;
			}
		}
		
		return 0;
	}
	
}// unionStatement
