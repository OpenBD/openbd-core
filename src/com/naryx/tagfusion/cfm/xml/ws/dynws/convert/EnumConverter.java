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

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.ws.encoding.ser.IEnum;

/**
 * Converts enum types to/from their BD equivalents.
 */
public class EnumConverter {

	public EnumConverter() {}

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
		// Must be a cfData object
		if (!(wrapper.value instanceof cfData))
			return false;

		// The typeHint must be a IEnum
		if (typeHint == null || !IEnum.class.isAssignableFrom(typeHint))
			return false;

		// Now convert it
		IEnum e = null;
		try {
			// Create an instance of the specified type
			e = (IEnum) typeHint.newInstance();
			wrapper.value = e.enumFromString(((cfData) wrapper.value).getString());
			return true;
		} catch (Exception ex) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Cannot convert type: " + TypeConverter.getTypeDesc(wrapper.value.getClass()) + ": " + Utils.getExceptionMessage(ex)));
		}
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
		// Must be an Axis Enum type
		if (!(wrapper.value instanceof IEnum))
			return false;

		// Now convert it.
		wrapper.value = TypeConverter.toBDType(((IEnum) wrapper.value).getValueAsObject(), session);
		return true;
	}

}
