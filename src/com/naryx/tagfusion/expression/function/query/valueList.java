/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  $Id: valueList.java 1818 2011-11-22 05:52:12Z alan $
 */

package com.naryx.tagfusion.expression.function.query;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class valueList extends functionBase {

	private static final long serialVersionUID = 1L;

	protected boolean quotedValue = false;

	public valueList() {
		min = 1;
		max = 3;
		setNamedParams(new String[] {"column", "delimiter", "defaultreturn"});
	}

	public String[] getParamInfo(){
		return new String[]{
			"query.column",
			"value separator. defaults to comma (,)",
			"if there are no rows, this is the default value return; blank"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"query", 
				"Returns a list of all the values, for a given column within the query, delimited by the value given", 
				ReturnType.STRING );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		cfData colData 			= getNamedParam(argStruct, "column");
		String delimitor 		= getNamedStringParam(argStruct, "delimiter",		 		",");
		String defaultValue = getNamedStringParam(argStruct, "defaultreturn", 	"");

		List<List<cfData>> thisQueryData = colData.getQueryTableData();
		if (thisQueryData != null && cfQueryResultData.getNoRows(thisQueryData) > 0 ) {
			return new cfStringData(listQuery(thisQueryData, colData.getQueryColumn(), delimitor, quotedValue));
		} else {
			return new cfStringData(defaultValue);
		}
	}

	public static String listQuery(List<List<cfData>> _query, int _col, String _delim, boolean _quoted) {
		// Loop through the columns
		int noRows = cfQueryResultData.getNoRows(_query);
		StringBuilder buffer = new StringBuilder(32);
		for (int x = 1; x <= noRows; x++) {

			if (_quoted)
				buffer.append("'");

			try {
				buffer.append(cfQueryResultData.getCellData(_query, x, _col).getString());
			} catch (Exception ignore) {}

			if (_quoted)
				buffer.append("'");

			if (x < noRows)
				buffer.append(_delim);
		}

		return buffer.toString();
	}

}
