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
import com.naryx.tagfusion.cfm.engine.cfQueryColumnData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.expression.function.functionBase;

public class arrayInsertAt extends functionBase {
	private static final long serialVersionUID = 1L;

	public arrayInsertAt() {
		min = max = 3;
		setNamedParams( new String[]{ "array", "index", "data" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"Array Object or XML object",
			"Position index to add",
			"Data to add"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"array", 
				"Adds an element to this array/xml object at the given index.  Throws exception is out of bounds", 
				ReturnType.BOOLEAN );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		
		cfData array = getNamedParam(argStruct, "array");
		int indx = getNamedIntParam(argStruct, "index", 0);

		if (array instanceof cfXmlData) {
			if (indx != 1)
				throwException(_session, "the index is out of range.  This array has 1 element");

			cfData toAdd = getNamedParam(argStruct, "data");
			if (!(toAdd instanceof cfXmlData))
				throwException(_session, "the parameter is not a XML object");
			if (!((cfXmlData) toAdd).get("XmlType").toString().equals(((cfXmlData) array).get("XmlType").toString()))
				throwException(_session, "the parameter is not the same type of XML object");
			if (!((cfXmlData) toAdd).get("XmlName").toString().equals(((cfXmlData) array).get("XmlName").toString()))
				throwException(_session, "the XML object parameter does not have the same name");
			((cfXmlData) array).getParent().insertBefore((cfXmlData) toAdd, (cfXmlData) array);
			return cfBooleanData.TRUE;
			
		}else	if (array.getDataType() == cfData.CFARRAYDATA) {

			if (array instanceof cfFixedArrayData)
				throwException(_session, "Cannot perform this function on an unmodifiable array.");
			else if (array instanceof cfQueryColumnData)
				throwException(_session, "Cannot perform this function on an query column.");

			if (indx > ((cfArrayData) array).size())
				throwException(_session, "the index is out of range.  This array has " + ((cfArrayData) array).size() + " elements");

			cfData value = getNamedParam(argStruct, "data");
			if (value.getDataType() == cfData.CFARRAYDATA) {
				value = value.duplicate();
			}
			((cfArrayData) array).addElementAt(value, indx);
			return cfBooleanData.TRUE;

		} else
			throwException(_session, "the parameter is not an Array");

		return null;
	}
}
