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

package com.naryx.tagfusion.expression.function.array;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfFixedArrayData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class arraySort extends functionBase {
	private static final long serialVersionUID = 1L;

	public arraySort() {
		min = 2;
		max = 3;
		setNamedParams( new String[]{ "array", "type", "order" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"Array object",
			"Type of search ('numeric', 'text' or 'textnocase')",
			"Order of search ('asc', 'desc')"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"array", 
				"Reorders the elements in the array according to the sort type and direction", 
				ReturnType.BOOLEAN );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {

		cfData array = getNamedParam(argStruct, "array");
		String type = getNamedStringParam(argStruct, "type", null);
		String order = getNamedStringParam(argStruct, "order", "asc");

		type = type.toLowerCase();
		if (!type.equals("numeric") && !type.equals("text") && !type.equals("textnocase")) {
			throwException(_session, "Invalid sort type [" + type + "] - use 'numeric', 'text' or 'textnocase'.");
		}

		order = order.toLowerCase();
		if (!order.equals("asc") && !order.equals("desc")) {
			throwException(_session, "Invalid sort ordering [" + order + "] - use 'asc' or 'desc'.");
		}

		if (array instanceof cfFixedArrayData) {
			throwException(_session, "Cannot perform this function on an unmodifiable array.");
		}

		if (array.getDataType() == cfData.CFARRAYDATA) {
			((cfArrayData) array).sortArray(type, order);
			return cfBooleanData.TRUE;
		} else
			throwException(_session, "the parameter is not an Array");

		return cfBooleanData.FALSE;
	}
}
