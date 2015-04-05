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

package com.naryx.tagfusion.expression.function.query;

import java.util.HashMap;
import java.util.Iterator;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class queryAddRow extends functionBase {
	private static final long serialVersionUID = 1L;

	public queryAddRow() {
		min = 1;
		max = 3;
		setNamedParams( new String[]{ "query", "rows", "data" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"query",
			"rows to add",
			"data can be either a structure, or an array of structures; ignored if 'rows' is present"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"query", 
				"Adds the specified the number of rows to the end of the query, or transposes a structure into a query, creating additional columns if needed to support the keys in the structure.  Only simple fields of the structure will be mapped (no inner structures for example)", 
				ReturnType.BOOLEAN );
	}


	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		cfData data = getNamedParam(argStruct, "query");
		cfQueryResultData thisQuery;
		int noRows = getNamedIntParam(argStruct, "rows", -1);

		// check the arg is a query
		if (!(data instanceof cfQueryResultData)) 
			throwException(_session, "The provided argument was not a query type.");

		thisQuery = (cfQueryResultData) data;

		// Now add the number of rows in
		if ( noRows > 0 ){
			thisQuery.addRow(noRows);
			return thisQuery.getData("recordcount");
		}
		
		
		// Structure/Array
		data	= getNamedParam(argStruct, "data", null);
		if ( data == null ){
			thisQuery.addRow(1);
			return thisQuery.getData("recordcount");
		}
		
		if ( data.getDataType() != cfData.CFSTRUCTDATA && data.getDataType() != cfData.CFARRAYDATA ){
			throwException(_session, "The parameter must be a structure/array");
		}

		
		// Now we can add it into the query
		HashMap<String,Integer> activeColumns	= new HashMap<String,Integer>();
		String[] QUERY_COLUMNS	= thisQuery.getColumnList();
		for ( int x=0; x<QUERY_COLUMNS.length; x++ )
			activeColumns.put(QUERY_COLUMNS[x].toLowerCase(), x+1 );

		if ( data.getDataType() == cfData.CFSTRUCTDATA )
			addRow(thisQuery, activeColumns, (cfStructData)data );
		else{
			
			cfArrayData	array	= (cfArrayData)data;
			for ( int x=0; x < array.size(); x++ ){
				cfData	el	= array.getData(x+1);
				
				if ( el.getDataType() == cfData.CFSTRUCTDATA )
					addRow(thisQuery, activeColumns, (cfStructData)el );
			}
		}

		return thisQuery.getData("recordcount");
	}
	
	
	
	private void addRow(cfQueryResultData queryData, HashMap<String,Integer> activeColumns, cfStructData structData) throws cfmRunTimeException{

		queryData.addRow(1);
		queryData.setCurrentRow( queryData.getSize() );

		Iterator<String>	it	= structData.keySet().iterator();
		while ( it.hasNext() ){
			String key	=	it.next().toLowerCase();
			
			if ( !activeColumns.containsKey( key ) ){
				int newcolumn = queryData.addColumnData( key, cfArrayData.createArray(1), null );
				activeColumns.put( key, newcolumn );
			}
			
			queryData.setCell( activeColumns.get(key), structData.getData(key) );
		}

		
	}
}
