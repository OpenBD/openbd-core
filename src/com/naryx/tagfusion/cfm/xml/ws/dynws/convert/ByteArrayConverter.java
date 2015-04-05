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

/*
 * Created on Dec 29, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.naryx.tagfusion.cfm.xml.ws.dynws.convert;

import java.lang.reflect.Array;

import com.nary.net.Base64;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * Converts binary array data types to/from their BD equivalents.
 */
public class ByteArrayConverter {

	
	public ByteArrayConverter() {}

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
		// Has to be a cfBinaryData object
		if (!(wrapper.value instanceof cfBinaryData))
			return false;

		// Default type is byte[] (even if Object is specified)
		if (typeHint == null || typeHint.equals(Object.class))
			typeHint = byte[].class;

		// Check that we can convert it to the correct type.
		if (!typeHint.isArray())
			return false;

		// Get as a cfBinaryData obj
		cfBinaryData bin = coerceIntocfBinaryData(wrapper.value);
		if (bin == null)
			return false;

		// Convert to the request typeHint
		wrapper.value = bin.getByteArray();
		return true;
	}

	/**
	 * Returns a cfBinaryData if the object is converable to a cfBinaryData or
	 * null.
	 * 
	 * @param val
	 *          Object to convert.
	 * @return A cfBinaryData instance or null.
	 */
	private static cfBinaryData coerceIntocfBinaryData(Object val) throws cfmRunTimeException {
		if (val instanceof cfBinaryData)
			return (cfBinaryData) val;
		else if (val instanceof cfStringData)
			return new cfBinaryData(Base64.base64Decode(((cfStringData) val).toString().getBytes()));
		else
			return null;
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

		// Cast to an array
		int len = Array.getLength(wrapper.value);

		// See if declared element type is a byte.
		Class eType = wrapper.value.getClass().getComponentType();
		boolean shouldConvert = (Byte.class.isAssignableFrom(eType) || Byte.TYPE.isAssignableFrom(eType));

		// If the length is 0 and there's no indication that this should
		// be a cfBinaryData, let the ArrayConverter handle it.
		if (len == 0 && !shouldConvert)
			return false;

		// Get as a byte array
		byte[] buf = new byte[len];
		for (int i = 0; i < len; i++) {
			Object o = Array.get(wrapper.value, i);
			if (o != null && o instanceof Byte) {
				// It's a Byte, cast into a byte
				buf[i] = ((Byte) o).byteValue();
			} else if (!shouldConvert) {
				// It's not declared as a byte[]/Byte[] array so it
				// may not be a cfBinaryData after all.
				return false;
			}
		}
		// Create the binary type
		wrapper.value = new cfBinaryData(buf);
		return true;
	}

}
