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

import java.sql.Types;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class queryNew extends functionBase {

	private static final long serialVersionUID = 1L;

	public queryNew() {
		min = 1;
		max = 2;
		setNamedParams( new String[]{"namelist", "typelist"} );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"column name list",
			"column type list"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"query", 
				"Creates a new query object with the columns past in of the optional types", 
				ReturnType.QUERY  );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String columnListStr 	= getNamedStringParam(argStruct, "namelist", null);
		String[] columnList 	= com.nary.util.string.convertToList(columnListStr.toLowerCase(), ',');

		// if the column type list is specified
		String typeListStr = getNamedStringParam(argStruct, "typelist", null);
		int[] columnTypeList = null;
		if (typeListStr != null) {
			String[] columnTypeListStr = com.nary.util.string.convertToList(typeListStr.toLowerCase(), ',');

			if (columnTypeListStr.length != columnList.length) {
				throwException(_session, "The number of items in the column list and column type list do not match.");
			}

			// convert the column types to their int equivalents
			columnTypeList = new int[columnTypeListStr.length];
			for (int i = 0; i < columnTypeListStr.length; i++) {
				columnTypeList[i] = getColumnType(_session, columnTypeListStr[i]);
			}
		}

		return new cfQueryResultData(columnList, columnTypeList, "QueryNew()");
	}

	public static int getColumnType(cfSession _session, String _type) throws cfmRunTimeException {
		String tempType = _type;

		// If the type begins with cf_sql_ then remove it for the comparisons.
		if (_type.startsWith("cf_sql_"))
			tempType = _type.substring("cf_sql_".length());

		if (tempType.length() == 7) {
			if (tempType.equals("integer")) {
				return Types.INTEGER;
			} else if (tempType.equals("decimal")) {
				return Types.DECIMAL;
			} else if (tempType.equals("varchar")) {
				return Types.VARCHAR;
			}
		} else if (tempType.length() == 6) {
			if (tempType.equals("bigint")) {
				return Types.BIGINT;
			} else if (tempType.equals("double")) {
				return Types.DOUBLE;
			} else if (tempType.equals("binary")) {
				return Types.BINARY;
			}
		} else if (tempType.length() == 4) {
			if (tempType.equals("date")) {
				return Types.DATE;
			} else if (tempType.equals("time")) {
				return Types.TIME;
			}
		} else if (tempType.equals("bit")) {
			return Types.BIT;
		} else if (tempType.equals("timestamp")) {
			return Types.TIMESTAMP;
		}

		cfCatchData catchData = new cfCatchData(_session);
		catchData.setType("Expression");
		catchData.setMessage("Unsupported column type '" + _type + "'");
		throw new cfmRunTimeException(catchData);

	}
}
