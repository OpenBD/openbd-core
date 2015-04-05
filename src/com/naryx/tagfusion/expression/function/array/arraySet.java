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

import java.util.Vector;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfFixedArrayData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.cfm.xml.cfXmlDataArray;
import com.naryx.tagfusion.expression.function.functionBase;

public class arraySet extends functionBase {

	private static final long serialVersionUID = 1L;

	public arraySet() {
		min = max = 4;
		setNamedParams( new String[]{ "array", "start", "end", "value" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"Array/XML Object",
			"Start index",
			"End index",
			"value to set"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"array", 
				"Sets the range of elements to the value given", 
				ReturnType.BOOLEAN );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {

		cfData array = getNamedParam(argStruct, "array");
		if (array instanceof cfXmlData) {
			Vector<cfData> list = new Vector<cfData>(1);
			list.add(array);
			array = new cfXmlDataArray(((cfXmlData) array).getParent(), list);
		}

		if (array instanceof cfFixedArrayData) {
			throwException(_session, "Cannot perform this function on an unmodifiable array.");
		}

		if (array.getDataType() == cfData.CFARRAYDATA) {
			int start = getNamedIntParam(argStruct, "start", 0);
			int end = getNamedIntParam(argStruct, "end", 0);
			int size = ((cfArrayData) array).size();

			if (end < start) {
				throwException(_session, "The start index must be greater than or equal to the end index.");
			} else if (start <= 0) {
				throwException(_session, "The start index must be greater than zero.");
			}

			if (end > size) {
				((cfArrayData) array).setCapacity(end);
			}

			cfData value = getNamedParam(argStruct, "value");
			if (value.getDataType() == cfData.CFARRAYDATA) {
				for (int i = start; i <= end; i++) {
					((cfArrayData) array).setData(i, value.duplicate());
				}
			} else {
				((cfArrayData) array).setElements(start, end, value);
			}
			return cfBooleanData.TRUE;

		} else
			throwException(_session, "the parameter is not an Array");

		return null;
	}
}
