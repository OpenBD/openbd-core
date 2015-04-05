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
import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

class expressionResultTable extends resultTable{
  
	private boolean init;
	private List<cfData> nextResultRow; // ResultRow
	
 	public expressionResultTable( selectColumn[] _selCols, orderByCol [] _orderBy ){
		super( _selCols, false, _orderBy );
	  init = false;
 	}
	
 
	public void processRow( rowContext _rowcontext, List<cfData> _pData, Map<String, String> _lookup ) throws cfmRunTimeException{
		cfData [] result;
		
		if ( !init ){
			nextResultRow = new ArrayList<cfData>();
			List<cfData> orderByList = null;
			if ( orderCols != null ){
				orderByList = new ArrayList<cfData>();
				for ( int i = 0; i < orderCols.length; i++ ){
					orderByList.add( cfBooleanData.getcfBooleanData( true ) );
				}
				
			}
			
			resultRows.add( new ResultRow( nextResultRow, orderByList ) );		
			init = true;
		}
		
		nextResultRow.clear();
    for ( int i = 0; i < selCols.length; i++ ){ // each selectColumn
			result =  selCols[i].execute( _rowcontext, _pData );
			for ( int j = 0; j < result.length; j++ ){
				// Note we don't need to duplicate() since this cfData will have been created in this QoQ
				nextResultRow.add( result[j] ); 
			}
		}
 	}
	
}
