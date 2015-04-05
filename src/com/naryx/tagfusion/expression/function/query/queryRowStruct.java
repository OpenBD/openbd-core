/* 
 *  Copyright (C)  2000-2010 TagServlet Ltd
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
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class queryRowStruct extends functionBase {

	private static final long serialVersionUID = 1L;

	public queryRowStruct() {
		min = 2;
		max = 2;
		setNamedParams( new String[] { "query", "row" });
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"query",
			"rowNo"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"query", 
				"Returns all the data in a query for a given row but as a structure", 
				ReturnType.STRUCTURE );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		cfData data	= getNamedParam(argStruct, "query");
		if ( data.getDataType() != cfData.CFQUERYRESULTDATA ){
			throwException(_session, "The parameter must be a query result");
		}

		cfQueryResultData queryData = (cfQueryResultData)data;
		
		// Make sure the row exists
		int rowNo	= getNamedIntParam(argStruct, "row", 0);
		if ( rowNo <= 0 || rowNo > queryData.getSize() ){
			throwException(_session, "The row '" + rowNo + "' does not exist");
		}
		
		// Build up the struct
		cfStructData	struct	= new cfStructData();
		String[]	columns = queryData.getColumnList();
		
		for ( int c=0; c < columns.length; c++ ){
			struct.setData( columns[c], queryData.getCell(rowNo, queryData.getColumnIndexCF( columns[c] )) ); 
		}

		return struct;
	}
}
