/* 
 *  Copyright (C) 2000-2010 TagServlet Ltd
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

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class queryColumnArray extends functionBase {

	private static final long serialVersionUID = 1L;

	public queryColumnArray() {
		min = 2;
		max = 2;
		setNamedParams( new String[]{ "query", "column" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"query",
			"column name"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"query", 
				"Returns all the data in a query for a given column", 
				ReturnType.ARRAY );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		cfData data	= getNamedParam(argStruct, "query");
		if ( data.getDataType() != cfData.CFQUERYRESULTDATA ){
			throwException(_session, "The parameter must be a query result");
		}

		return getColumnArray( _session, (cfQueryResultData)data, getNamedStringParam(argStruct, "column", null) );
	}
	
	
	protected cfArrayData	getColumnArray(cfSession _session, cfQueryResultData queryData, String column ) throws cfmRunTimeException {
		cfArrayData array = cfArrayData.createArray(1);
		
		int columnIndex = queryData.getColumnIndexCF( column );
		if ( columnIndex == 0 ){
			throwException(_session, "The column '" + column + "' does not exist in this query");
		}
		
		int rowCount = queryData.getSize();
		for ( int row=0; row < rowCount; row++ ){
			array.addElement( queryData.getCell(row+1, columnIndex) );
		}

		return array;
	}
}
