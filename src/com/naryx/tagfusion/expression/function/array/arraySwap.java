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
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.expression.function.functionBase;

public class arraySwap extends functionBase {
	private static final long serialVersionUID = 1L;

	public arraySwap() {
		min = max = 3;
		setNamedParams( new String[]{ "array", "index1", "index2" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"Array object",
			"index1",
			"index2"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"array", 
				"Swaps the elements around at the given index position", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		cfData array = getNamedParam(argStruct, "array");
		if (array instanceof cfXmlData) {
			int start = getNamedIntParam(argStruct, "index1", 0);
			int end = getNamedIntParam(argStruct, "index2", 0);
			if (start != 1 || end != 1)
				throwException(_session, "the index is out of range.  This array has 1 element");
			else
				return cfBooleanData.TRUE;
		}

		if (array instanceof cfFixedArrayData) {
			throwException(_session, "Cannot perform this function on an unmodifiable array.");
		}

		if (array.getDataType() == cfData.CFARRAYDATA) {
			int start = getNamedIntParam(argStruct, "index1", 0);
			int end = getNamedIntParam(argStruct, "index2", 0);
			int size = ((cfArrayData) array).size();

			if (start > size || end > size)
				throwException(_session, "the index is out of range.  This array has " + ((cfArrayData) array).size() + " elements");

			((cfArrayData) array).elementSwap(start, end);
			return cfBooleanData.TRUE;
		} else
			throwException(_session, "the parameter is not an Array");

		return null;
	}
}
