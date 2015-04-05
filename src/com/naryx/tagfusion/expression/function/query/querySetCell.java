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

import java.sql.SQLException;
import java.sql.Types;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultSetMetaData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.expression.function.functionBase;

public class querySetCell extends functionBase {

	private static final long serialVersionUID = 1L;

	public querySetCell() {
		min = 3;
		max = 4;
		setNamedParams( new String[] {"query", "column", "value", "row"} );
	}

	public String[] getParamInfo(){
		return new String[]{
			"query",
			"column",
			"value",
			"row number - defaults to the last one"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"query", 
				"Sets the given column within a query with the value at the given row, or the last row if not specified", 
				ReturnType.BOOLEAN );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		cfQueryResultData thisQuery = (cfQueryResultData) getNamedParam(argStruct, "query");
		String columnName = getNamedStringParam(argStruct, "column", null);
		cfData value = getNamedParam(argStruct, "value");
		int rowNo = getNamedIntParam(argStruct, "row", thisQuery.getSize());

		if (rowNo <= 0)
			throwException(_session, "You must provide a value greater than 0, for the row you are setting");

		if (rowNo > thisQuery.getSize())
			throwException(_session, "The row you specified (" + rowNo + ") is outside the available rows");

		if (value.getQueryTableData() != null)
			value = value.duplicate();

		// need an error message indicating the expected type
		try {
			cfQueryResultSetMetaData metaData = (cfQueryResultSetMetaData) thisQuery.getMetaData();
			// only check the type if the type has been set and the value isn't an
			// empty string
			if (metaData.isColumnTypesSet() && !(value.getDataType() == cfData.CFSTRINGDATA && value.getString().length() == 0)) {
				int colIndex = metaData.getColumnIndex(columnName);
				int colType = metaData.getColumnType(colIndex);
				boolean typeMisMatch = false;
				try {
					switch (colType) {
					case Types.VARCHAR:
						value.getString();
						break;
					case Types.INTEGER:
					case Types.BIGINT:
						cfNumberData number = value.getNumber();
						if (!number.isInt()) {
							if (number.getDouble() > Integer.MAX_VALUE) {
								typeMisMatch = true;
							}
						}
						break;
					case Types.DOUBLE:
					case Types.DECIMAL:
						value.getDouble();
						break;

					case Types.BIT:
						value.getBoolean();
						break;
					case Types.BINARY:
						if (value.getDataType() != cfData.CFBINARYDATA) {
							typeMisMatch = true;
						}
						break;
					case Types.DATE:
					case Types.TIME:
						value.getDateData();
						break;
					default:
						break;
					}
				} catch (dataNotSupportedException e) {
					typeMisMatch = true;
				} catch (NumberFormatException e) {
					typeMisMatch = true;
				}
				if (typeMisMatch) {
					throwException(_session, "Invalid data for column of type " + metaData.getColumnTypeName(colIndex));
				}
			}

		} catch (SQLException e) { // shouldn't happen
			throwException(_session, "Failed to set cell due to unexpected SQLException: " + e.getMessage());
		}

		if (!thisQuery.setCell(rowNo, columnName, value))
			throwException(_session, "The column you specified (" + columnName + ") does not exist");

		return cfBooleanData.TRUE;
	}
}
