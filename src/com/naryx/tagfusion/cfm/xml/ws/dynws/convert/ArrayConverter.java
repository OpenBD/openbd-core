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

package com.naryx.tagfusion.cfm.xml.ws.dynws.convert;

import java.lang.reflect.Array;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * Converts array data types to/from their BD equivalents.
 */
public class ArrayConverter {

	
	public ArrayConverter() {}

	/**
	 * Converts the Object in the ObjectWrapper into the Class specified by
	 * typeHint (or something suitable for SOAP serialization if typeHint is not
	 * specified). Returns true if the Object was successfully converted/replaced,
	 * false otherwise.
	 * 
	 * @param wrapper
	 *          ObjectWrapper containing the Object to convert/replace.
	 * @param typeHint
	 *          Class into which we need to convert the Object.
	 * @param cl
	 *          ClassLoader that may contain the specified typeHint.
	 * @return true if the Object was successfully converted/replaced, false
	 *         otherwise.
	 * @throws cfmRunTimeException
	 */
	public static boolean toWebServiceType(ObjectWrapper wrapper, Class typeHint, ClassLoader cl) throws cfmRunTimeException {
		// Has to be an Array
		if (!(wrapper.value instanceof cfArrayData))
			return false;

		// Default type is Object[] (even if Object is specified)
		if (typeHint == null || typeHint.equals(Object.class))
			typeHint = Object[].class;

		// Check that we can convert it to the correct type.
		if (!typeHint.isArray())
			return false;

		// Cast to an cfArrayData
		cfArrayData arr = (cfArrayData) wrapper.value;

		// This assumes the typeHint array type can be built 1 dimension
		// at a time (sub elements being 1 dimensional arrays and so on).
		Object rtn = Array.newInstance(typeHint.getComponentType(), arr.size());

		for (int i = 0; i < arr.size(); i++) {
			// Convert the item and set it in the new array instance
			Array.set(rtn, i, TypeConverter.toWebServiceType(arr.getElement(i + 1), typeHint.getComponentType(), cl));
		}
		wrapper.value = rtn;
		return true;
	}

	/**
	 * Converts the Object in the ObjectWrapper into a cfData. Returns true if the
	 * Object was successfully converted/replaced, false otherwise.
	 * 
	 * @param wrapper
	 *          ObjectWrapper containing the Object to convert/replace.
	 * @param session
	 *          cfSession class, needed for object creation
	 * @return true if the Object was successfully converted/replaced, false
	 *         otherwise.
	 * @throws cfmRunTimeException
	 */
	public static boolean toBDType(ObjectWrapper wrapper, cfSession session) throws cfmRunTimeException {
		// Has to be an Array
		if (!wrapper.value.getClass().isArray())
			return false;

		// OK then, get the length
		int len = Array.getLength(wrapper.value);

		// Populate the cfArrayData
		cfArrayData rtn = cfArrayData.createArray(1);
		for (int i = 0; i < len; i++)
			rtn.setData(i + 1, TypeConverter.toBDType(Array.get(wrapper.value, i), session));
		wrapper.value = rtn;
		return true;
	}

}
