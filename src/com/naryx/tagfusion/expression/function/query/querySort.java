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

/**
 * Author: Alan Williamson
 * Created on 02-Sep-2004
 * 
 * BlueDragon only Expression
 * 
 * QuerySort( query, sort_column, sort_type, [, sort_order ] )
 */

package com.naryx.tagfusion.expression.function.query;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class querySort extends functionBase {

	private static final long serialVersionUID = 1L;

	public querySort() {
		min = 3;
		max = 4;
		
		setNamedParams(new String[] { "query", "column", "type", "direction" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"query",
			"column name",
			"type - ('text', 'textnocase', 'numeric')",
			"direction - ('asc' or 'desc')"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"query", 
				"Sorts the query based on the column specified and the order criteria given.  Modifies the original query object", 
				ReturnType.QUERY );
	}
 
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		cfData query = getNamedParam(argStruct, "query");
		String columnName = getNamedStringParam(argStruct, "column", null);
		String sortDirection = getNamedStringParam(argStruct, "direction", "asc");
		String sortType = getNamedStringParam(argStruct, "type", "text");

		if (!(sortDirection.equalsIgnoreCase("asc") || sortDirection.equalsIgnoreCase("desc"))) {
			throwException(_session, "\"" + sortDirection + "\" is an invalid sort order. A valid sort order is \"asc\" or \"desc\".");
		}

		if (!(sortType.equalsIgnoreCase("numeric") || sortType.equalsIgnoreCase("text") || sortType.equalsIgnoreCase("textnocase"))) {
			throwException(_session, "\"" + sortType + "\" is an invalid sort type. A valid sort type is \"numeric\", \"text\", or \"textnocase\".");
		}

		if (query.getDataType() == cfData.CFQUERYRESULTDATA) {
			try {
				((cfQueryResultData) query).sort(columnName, sortType, sortDirection);
			} catch (cfmRunTimeException e) {
				throwException(_session, e.getMessage());
			}

			return query;
		} else
			throwException(_session, "the parameter is not an Query");

		return null;

	}
}
