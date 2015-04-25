/* 
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
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
 *  http://openbd.org/
 */

package com.naryx.tagfusion.expression.function;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;

public class getMetaData extends functionBase {
	private static final long serialVersionUID = 1L;

	public getMetaData() {
		min = 0;
		max = 1;
	}

	public String[] getParamInfo() {
		return new String[] { "object" };
	}

	public java.util.Map<String,String> getInfo() {
		return makeInfo("engine", "Returns back meta information depending on the object passed in", ReturnType.STRUCTURE);
	}

	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		if (parameters.size() == 0) {
			cfComponentData activeComponent = _session.getActiveComponentData();
			if (activeComponent != null) {
				return activeComponent.getMetaData();
			} else {
				return new cfStructData(); // CFMX returns an empty struct
			}
		}

		cfData data = parameters.get(0);
		if (data instanceof cfComponentData) {
			return ((cfComponentData) data).getMetaData();
		} else if (data instanceof userDefinedFunction) {
			return ((userDefinedFunction) data).getMetaData();
		} else if (data instanceof cfQueryResultData) {
			try {
				ResultSetMetaData metaData = ((cfQueryResultData) data).getMetaData();
				int cols = metaData.getColumnCount();
				cfArrayData returnData = cfArrayData.createArray(1);
				for (int i = 1; i <= cols; i++) {
					cfStructData nextColInfo = new cfStructData();

					nextColInfo.setData("IsCaseSensitive", cfBooleanData.getcfBooleanData(metaData.isCaseSensitive(i)));
					nextColInfo.setData("Name", new cfStringData(metaData.getColumnName(i)));

					// if the type wasn't set in QueryNew then the type name won't be set so don't include it
					String colTypeName = metaData.getColumnTypeName(i);
					if (colTypeName != null) {
						nextColInfo.setData("TypeName", new cfStringData(colTypeName));
					}
					returnData.addElement(nextColInfo);
				}

				return returnData;
			} catch (SQLException e) {
				throwException(_session, "Failed to obtain query metadata due to SQLException: " + e.getMessage());
			}

		} else if (data instanceof cfJavaObjectData) { // undocumented handling of other types
			return new cfJavaObjectData(((cfJavaObjectData) data).getInstanceClass());
		}

		// CFMX doesn't throw an exception when the passed in element isn't a
		// component. Instead it returns a java.lang.Class object. We don't see how this object
		// can be useful so we return a null object instead. Refer to bug #2297.
		return cfNullData.NULL;
	}
}
