/*
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: queryAddColumn.java 2512 2015-02-13 00:11:54Z alan $
 */

package com.naryx.tagfusion.expression.function.query;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.CFUndefinedValue;
import com.naryx.tagfusion.expression.function.functionBase;

public class queryAddColumn extends functionBase {

	private static final long serialVersionUID = 1L;

	public queryAddColumn() {
		min = 3;
		max = 4;
		setNamedParams(new String[] { "query", "column", "datatype", "valuearray" });
	}

	public String[] getParamInfo() {
		return new String[] { "query", "column", "type of data; integer, decimal, varchar, bigint, double, binary, date, time, bit, timestamp", "array of values" };
	}

	public java.util.Map getInfo() {
		return makeInfo("query", "Adds a new column of data to the exist query object, returning the column number", ReturnType.NUMERIC);
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {

		// Get the Query
		cfData thisQuery = getNamedParam(argStruct, "query");
		if (thisQuery.getDataType() != cfData.CFQUERYRESULTDATA) {
			throwException(_session, "Invalid argument type. The first argument must be a query result.");
		}

		// Get the column name
		String columnName = getNamedStringParam(argStruct, "column", null);
		if (columnName == null) {
			throwException(_session, "Invalid argument type. Please specify a 'column' parameter");
		}

		/**
		 * There is a bizarre use case with some other well known engines where the 3rd parameter is actually optionally. So we need to look at the 3rd type to see what data-type it is before deeming the parameters correct
		 *
		 */
		cfData arrayData = null;
		String dataType = null;

		if (argStruct.size() == 3 && !argStruct.isNamedBased()) {
			cfData param3 = argStruct.getData(2);
			if (param3.getDataType() == cfData.CFARRAYDATA) {
				arrayData = param3;
			}
		} else if (argStruct.isNamedBased()) {
			arrayData = getNamedParam(argStruct, "valuearray");
			dataType = getNamedStringParam(argStruct, "datatype", null);
		} else {
			cfData param2 = argStruct.getData(2);
			cfData param3 = argStruct.getData(3);

			if (param2.getDataType() == cfData.CFARRAYDATA && CFUndefinedValue.UNDEFINED == param3) {
				arrayData = param2;
			} else if (param3.getDataType() == cfData.CFARRAYDATA) {
				arrayData = param3;
				dataType = getNamedStringParam(argStruct, "datatype", null);
			}
		}

		if (arrayData == null) {
			throwException(_session, "Invalid argument type. 'valuearray' must be present");
		} else {
			if (arrayData.getDataType() != cfData.CFARRAYDATA)
				throwException(_session, "Invalid argument type. 'valuearray' must be an array");
		}

		try {
			int columnNo = ((cfQueryResultData) thisQuery).addColumnData(columnName.toLowerCase(), (cfArrayData) arrayData, (dataType == null ? null : new Integer(queryNew.getColumnType(_session, dataType.toLowerCase()))));

			return new cfNumberData(columnNo);
		} catch (cfmRunTimeException E) {
			throwException(_session, E.getMessage());
		}

		return null; // should never be reached--keep compiler happy
	}
}
