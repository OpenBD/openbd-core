/* 
 *  Copyright (C) 2000 - 20089 TagServlet Ltd
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
import com.naryx.tagfusion.cfm.engine.cfJavaArrayData;
import com.naryx.tagfusion.cfm.engine.cfQueryColumnData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.expression.function.functionBase;

public class arrayAppend extends functionBase {
	private static final long serialVersionUID = 1L;

	public arrayAppend() {
		min = max = 2;
		setNamedParams( new String[]{ "array", "toadd" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"Array Object or XML object",
			"Data to add"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"array", 
				"Adds a new element to the end of the array, or if an XML object, adds a new child element.", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {

		cfData array = getNamedParam(argStruct, "array");

		if (array instanceof cfXmlData) {
			cfData toAdd = getNamedParam(argStruct, "toadd");
			if (!(toAdd instanceof cfXmlData))
				throwException(_session, "the parameter is not a XML object");
			if (!((cfXmlData) toAdd).get("XmlType").toString().equals(((cfXmlData) array).get("XmlType").toString()))
				throwException(_session, "the parameter is not the same type of XML object");
			if (!((cfXmlData) toAdd).get("XmlName").toString().equals(((cfXmlData) array).get("XmlName").toString()))
				throwException(_session, "the XML object parameter does not have the same name");
			((cfXmlData) array).getParent().appendChild((cfXmlData) toAdd);
			return cfBooleanData.TRUE;
			
		} else	if (array.getDataType() == cfData.CFARRAYDATA) {

			if (array instanceof cfFixedArrayData) {
				throwException(_session, "Cannot perform this function on an unmodifiable array.");
			} else if (array instanceof cfQueryColumnData)
				throwException(_session, "Cannot perform this function on an query column.");

			cfData value = getNamedParam(argStruct, "toadd");
			if (value.getDataType() == cfData.CFARRAYDATA && !(value instanceof cfJavaArrayData)) {
				value = value.duplicate();
			}
			((cfArrayData) array).addElement(value);
			return cfBooleanData.TRUE;
		} else {
			throwException(_session, "the parameter is not an Array");
		}

		return null;
	}
}
