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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.expression.function.ParseDateTime;

/**
 * Converts simple data types to/from their BD equivalents.
 */
public class SimpleConverter {


	public SimpleConverter() {}

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

		if (typeHint != null && !typeHint.equals(Object.class)) {
			// Try using the typeHint
			if (cfDataToDefined(wrapper, typeHint, cl))
				return true;
		}

		// Try converting to a common type without looking at the typeHint
		if (cfDataToUndefined(wrapper))
			return true;

		// Alas...
		return false;
	}

	/**
	 * Converts the cfData in the ObjectWrapper into the Class specified by
	 * typeHint. Returns true if the cfData was successfully converted/replaced,
	 * false otherwise.
	 * 
	 * @param wrapper
	 *          ObjectWrapper containing the cfData to convert/replace.
	 * @param typeHint
	 *          Class into which we need to convert the Object.
	 * @param cl
	 *          ClassLoader that may contain the specified typeHint.
	 * @return true if the cfData was successfully converted/replaced, false
	 *         otherwise.
	 * @throws cfmRunTimeException
	 */
	private static boolean cfDataToDefined(ObjectWrapper wrapper, Class typeHint, ClassLoader cl) throws cfmRunTimeException {
		cfData data = (cfData) wrapper.value;
		try {
			if (String.class.isAssignableFrom(typeHint)) {
				wrapper.value = data.getString();
				return true;
			} else if (Boolean.class.isAssignableFrom(typeHint) || typeHint.equals(boolean.class)) {
				wrapper.value = new Boolean(data.getBoolean());
				return true;
			} else if (Character.class.isAssignableFrom(typeHint) || typeHint.equals(char.class)) {
				String str = data.getString();
				if (str != null && str.length() > 0) {
					wrapper.value = new Character(str.charAt(0));
					return true;
				}
			} else if (Byte.class.isAssignableFrom(typeHint) || typeHint.equals(byte.class)) {
				wrapper.value = new Byte(data.getString());
				return true;
			} else if (Short.class.isAssignableFrom(typeHint) || typeHint.equals(short.class)) {
				wrapper.value = new Short(data.getString());
				return true;
			} else if (Integer.class.isAssignableFrom(typeHint) || typeHint.equals(int.class)) {
				wrapper.value = new Integer(data.getInt());
				return true;
			} else if (Float.class.isAssignableFrom(typeHint) || typeHint.equals(float.class)) {
				wrapper.value = new Float(data.getDouble());
				return true;
			} else if (Double.class.isAssignableFrom(typeHint) || typeHint.equals(double.class)) {
				wrapper.value = new Double(data.getDouble());
				return true;
			} else if (Long.class.isAssignableFrom(typeHint) || typeHint.equals(long.class)) {
				wrapper.value = new Long(data.getString());
				return true;
			} else if (BigInteger.class.isAssignableFrom(typeHint)) {
				wrapper.value = new BigInteger(data.getString());
				return true;
			} else if (BigDecimal.class.isAssignableFrom(typeHint)) {
				wrapper.value = new BigDecimal(data.getString());
				return true;
			} else if (Date.class.isAssignableFrom(typeHint)) {
				wrapper.value = ParseDateTime.parseDateString(data.getString());
				return true;
			} else if (Calendar.class.isAssignableFrom(typeHint)) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(ParseDateTime.parseDateString(data.getString()));
				wrapper.value = cal;
				return true;
			}
		} catch (dataNotSupportedException ex) {
			// Couldn't convert
		}

		// Cannot convert it.
		return false;
	}

	/**
	 * Converts the cfData in the ObjectWrapper into an appropriate Type that
	 * corresponds to the cfData subclass. Returns true if the cfData was
	 * successfully converted/replaced, false otherwise.
	 * 
	 * @param wrapper
	 *          ObjectWrapper containing the cfData to convert/replace.
	 * @return true if the cfData was successfully converted/replaced, false
	 *         otherwise.
	 * @throws cfmRunTimeException
	 */
	private static boolean cfDataToUndefined(ObjectWrapper wrapper) throws cfmRunTimeException {
		if (wrapper.value instanceof cfNumberData) {
			wrapper.value = (Double) ((cfNumberData) wrapper.value).getDouble();
			return true;
		} else if (wrapper.value instanceof cfBooleanData) {
			wrapper.value = (Boolean) ((cfBooleanData) wrapper.value).getBoolean();
			return true;
		} else if (wrapper.value instanceof cfStringData) {
			wrapper.value = ((cfStringData) wrapper.value).getString();
			return true;
		} else if (wrapper.value instanceof cfDateData) {
			wrapper.value = new Date(((cfDateData) wrapper.value).getLong());
			return true;
		} else if (wrapper.value instanceof cfNullData) {
			wrapper.value = null;
			return true;
		} else {
			return false;
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
		if (wrapper.value instanceof String) {
			wrapper.value = new cfStringData(wrapper.value.toString());
			return true;
		} else if (wrapper.value instanceof Boolean) {
			// Pass in an all lowercase "true" or "false"
			wrapper.value = cfBooleanData.getcfBooleanData(((Boolean) wrapper.value).toString().toLowerCase());
			return true;
		} else if (wrapper.value instanceof Character) {
			wrapper.value = new cfStringData(((Character) wrapper.value).toString());
			return true;
		} else if (wrapper.value instanceof Byte) {
			wrapper.value = new cfStringData(((Byte) wrapper.value).toString());
			return true;
		} else if (wrapper.value instanceof Short) {
			wrapper.value = new cfNumberData(((Short) wrapper.value).shortValue());
			return true;
		} else if (wrapper.value instanceof Integer) {
			wrapper.value = new cfNumberData(((Integer) wrapper.value).intValue());
			return true;
		} else if (wrapper.value instanceof Float) {
			wrapper.value = new cfNumberData(((Float) wrapper.value).floatValue());
			return true;
		} else if (wrapper.value instanceof Double) {
			wrapper.value = new cfNumberData(((Double) wrapper.value).doubleValue());
			return true;
		} else if (wrapper.value instanceof Long) {
			wrapper.value = new cfNumberData(((Long) wrapper.value).longValue());
			return true;
		} else if (wrapper.value instanceof Number) {
			wrapper.value = new cfNumberData(((Number) wrapper.value).doubleValue());
			return true;
		} else if (wrapper.value instanceof BigInteger) {
			wrapper.value = new cfNumberData(((BigInteger) wrapper.value).longValue());
			return true;
		} else if (wrapper.value instanceof BigDecimal) {
			wrapper.value = new cfNumberData(((BigDecimal) wrapper.value).doubleValue());
			return true;
		} else if (wrapper.value instanceof Date) {
			wrapper.value = new cfDateData(((Date) wrapper.value).getTime());
			return true;
		} else if (wrapper.value instanceof Calendar) {
			wrapper.value = new cfDateData(((Calendar) wrapper.value).getTime().getTime());
			return true;
		} else {
			// Cannot convert it.
			return false;
		}
	}

}
