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

package com.naryx.tagfusion.cfm.tag;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.naryx.tagfusion.cfm.engine.Javacast;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfFixedArrayData;
import com.naryx.tagfusion.cfm.engine.cfJavaArrayData;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfJavaStructData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.cfm.xml.ws.encoding.ser.QueryBean;

public final class tagUtils extends Object {

	/*
	 * Returns a List of sprted page numbers that have selected. If all pages are
	 * selected, then null is returned
	 * 
	 * 1, 2, 3-5, 7-12
	 */
	public static List<Integer> getNumberListSorted(String pagesStr) throws NumberFormatException {
		Set<Integer>	set	= getNumberSet( pagesStr );
		List<Integer>	list	= new ArrayList<Integer>( set.size() );
		Iterator<Integer>	it	= set.iterator();
		while ( it.hasNext() )
			list.add( it.next() );
		
		Collections.sort( list );
		return list;
	}
		
	/*
	 * Returns a Set of page numbers that have selected. If all pages are
	 * selected, then null is returned
	 * 
	 * 1, 2, 3-5, 7-12
	 */
	public static Set<Integer> getNumberSet(String pagesStr) throws NumberFormatException {
		if (pagesStr.length() == 0) {
			return null;
		} else {
			HashSet<Integer> pages = new HashSet<Integer>();
			String[] pageArr = pagesStr.split(",");
			for (int i = 0; i < pageArr.length; i++) {
				String nextPage = pageArr[i].trim();
				int rangeIndx = pageArr[i].indexOf('-');
				if (rangeIndx < 0) {
					pages.add(Integer.parseInt(nextPage));
				} else {
					int start = Integer.parseInt(nextPage.substring(0, rangeIndx));
					int end = Integer.parseInt(nextPage.substring(rangeIndx + 1));
					for (int j = start; j <= end; j++) {
						pages.add(j);
					}
				}
			}
			return pages;
		}
	}
	

	public static String getLastToken(String rhs) {
		return getLastToken(rhs, ".");
	}

	public static String getLastToken(String rhs, String tok) {
		int c1 = rhs.lastIndexOf(tok);
		if (c1 == -1)
			return rhs;
		else
			return rhs.substring(c1 + 1);
	}

	public static String trimError(String error) {
		if (error == null)
			return "null";

		int c1 = error.indexOf("java.sql.SQLException");
		if (c1 != -1)
			return error.substring(c1 + 21).trim();
		else
			return error;
	}

	public static String removeExtra(String rhs) {
		if (rhs.length() >= 2 && rhs.charAt(1) == '#' && ((rhs.charAt(0) == '\"' && rhs.charAt(rhs.length() - 1) == '\"') || (rhs.charAt(0) == '\'' && rhs.charAt(rhs.length() - 1) == '\'')))
			rhs = rhs.substring(1, rhs.length() - 1);

		return com.nary.util.string.replaceString(rhs, "#", "");
	}

	public static String arrayIndexClean(String indx) {
		if (indx.length() >= 2 && indx.charAt(0) == '\"' && indx.charAt(indx.length() - 1) == '\"')
			return indx.substring(1, indx.length() - 1);
		else
			return "#" + indx + "#";
	}

	private static String BODMAS[] = { "(", "+", "-", "*", "/", "!", "%", "&", "[", "]", " eq ", " equal ", " is ", " is not ", " not equal ", " neq ", " not ", " contains ", " does not contain ", " greater than ", " less than ", " gt", " lt ", " gte ", " ge ", " lte ", " le ", " greater than or equal to ", " less than or equal to " };

	public static boolean isExpression(String RHS) {
		RHS = RHS.toLowerCase();
		for (int x = 0; x < BODMAS.length; x++)
			if (RHS.indexOf(BODMAS[x]) != -1)
				return true;

		return false;
	}

	private static String NAMES[] = { "variables.", "form.", "url.", "cookie.", "cgi.", "client.", "session.", "server", "application.", "caller.", "request." };

	public static String getQualifiedName(String name) {
		name = name.toLowerCase();
		for (int x = 0; x < NAMES.length; x++) {
			if (name.indexOf(NAMES[x]) == 0)
				return name;
		}

		return "variables." + name;
	}

	/**
	 * The conversion from a Java object class to a CFML type is based on the
	 * following:
	 * 
	 * http://livedocs.macromedia.com/coldfusion/6.1/htmldocs/java35.htm
	 * 
	 * Note the following errors in the above-referenced documentation:
	 * 
	 * 1. A Java Long/long is converted to a CFML Real Number (not Integer). 2. A
	 * java.util.List is converted to a CFML Array (not comma delimited list). 3.
	 * A Java byte[] is converted to a CFML Binary (not Array).
	 */
	public static cfData convertToCfData(Object obj) {

		if (obj instanceof cfData) {
			return (cfData) obj;
		} else if (obj == null) {
			return cfNullData.JAVA_NULL;
		} else if (obj instanceof java.lang.String) {
			return new cfStringData((String) obj);
		} else if (obj instanceof java.lang.Boolean) {
			return cfBooleanData.getcfBooleanData((Boolean) obj);
		} else if (obj instanceof java.lang.Character) {
			return new cfStringData(((Character) obj).toString());
		} else if (obj instanceof java.lang.Byte) {
			return new cfStringData(((Byte) obj).toString());
		} else if (obj instanceof java.lang.Short) {
			return new cfNumberData((Short) obj);
		} else if (obj instanceof java.lang.Integer) {
			return new cfNumberData(((Integer) obj));
		} else if (obj instanceof java.lang.Long) {
			return new cfNumberData((Long) obj);
		} else if (obj instanceof java.lang.Double) {
			return new cfNumberData((Double) obj);
		} else if (obj instanceof java.lang.Float) {
			return new cfNumberData((Float) obj);
		} else if (obj instanceof java.util.Date) {
			return new cfDateData(((java.util.Date) obj).getTime());
		} else if (obj instanceof java.util.Map) {
			java.util.Map<?, ?> map = (java.util.Map<?, ?>) obj;
			return new cfJavaStructData(map);
		} else if (obj instanceof java.util.List) {
			java.util.List<Object> v = (java.util.List<Object>) obj;
			return new cfJavaArrayData(v);
		} else if (obj.getClass().isArray()) {
			if (obj.getClass().getName().equals("[B")) {
				return new cfBinaryData((byte[]) obj);
			} else {
				cfData cfdata = tagUtilsJava.convertToCfData(obj);
				if (cfdata != null)
					return cfdata;

				return cfArrayData.createArray(obj);
			}

		} else if (obj instanceof QueryBean) {
			return convertToCFQuery((QueryBean) obj);
		} else if (cfXmlData.isXmlObject(obj)) {
			return cfXmlData.newInstance(obj, true);
		}

		return new cfJavaObjectData(obj);

	}// convertToCfData()

	/**
	 * converts a QueryBean to a CFQUERY
	 */
	private static cfQueryResultData convertToCFQuery(QueryBean qb) {
		cfQueryResultData rtn = new cfQueryResultData(qb.getColumnList(), "QueryNew()");

		// get the column names
		int noCols = qb.getColumnList().length;

		// populate tablerows
		// get the no. of row by getting the no. of rows in the data set
		int noRows = qb.getData().length;
		rtn.addRow(noRows);
		for (int i = 1; i <= noRows; i++) {
			for (int j = 1; j <= noCols; j++)
				rtn.setCell(i, j, convertToCfData(qb.getData()[i - 1][j - 1]));
		}

		return rtn;
	}

	/**
	 * converts the given cfData to an instance of the specified Class. Returns
	 * null if the conversion is not possible
	 */
	public static Object convertCFtoJava(cfData _cfdata, Class<?> _class ) {
		return convertCFtoJava( _cfdata, _class, true );
	}
	
	public static Object convertCFtoJava(cfData _cfdata, Class<?> _class, boolean _applyJavacast ) {

		String className = _class.getName();

		try {
			if (className.equals("java.lang.Object") || (className.equals("java.io.Serializable") && (_cfdata.getDataType() != cfData.CFJAVAOBJECTDATA))) {
				if ( _applyJavacast && _cfdata.getJavaCast() != null ){
					Javacast javacast = _cfdata.getJavaCast();
					_class = javacast.getCastClass();
					return convertCFtoJava( _cfdata, _class, false );
				}else{
					return getNatural(_cfdata);
				}
			} else if (className.equals("java.lang.Comparable") && ((_cfdata.getDataType() == cfData.CFNUMBERDATA) || (_cfdata.getDataType() == cfData.CFSTRINGDATA) || (_cfdata.getDataType() == cfData.CFDATEDATA) || (_cfdata.getDataType() == cfData.CFNULLDATA))) {
				return getNatural(_cfdata);
			} else if (className.equals("java.lang.Cloneable") && _cfdata.getDataType() == cfData.CFDATEDATA) {
				return getNatural(_cfdata);
			} else if (className.equals("java.lang.Number") && (_cfdata.getDataType() == cfData.CFNUMBERDATA)) {
				return getNatural(_cfdata);
			} else if (className.equals("boolean") || className.equals("java.lang.Boolean")) {
				return new Boolean(_cfdata.getBoolean());
			} else if (className.equals("java.lang.String")) {
				return _cfdata.getString();
			} else if (className.equals("char") || className.equals("java.lang.Character")) {
				String chStr = _cfdata.getString();
				return new Character(chStr.charAt(0));
			} else if (className.equals("int") || className.equals("java.lang.Integer")) {
				return new Integer(_cfdata.getInt());
			} else if (className.equals("double") || className.equals("java.lang.Double")) {
				return new Double(_cfdata.getDouble());
			} else if (className.equals("float") || className.equals("java.lang.Float")) {
				return new Float(_cfdata.getDouble());
			} else if (className.equals("long") || className.equals("java.lang.Long")) {
				return new Long(_cfdata.getLong());
			} else if (className.equals("short") || className.equals("java.lang.Short")) {
				return new Short((short) _cfdata.getInt());
			} else if (className.equals("byte") || className.equals("java.lang.Byte")) {
				return new Byte((byte) _cfdata.getInt());

			} else if (_cfdata.getDataType() != cfData.CFJAVAOBJECTDATA && className.equals("java.util.Date")) {
				return new java.util.Date(_cfdata.getDateData().getLong());

				// if the object is a java.util.Vector wrapped up by a cfJavaObjectData
				// we don't want to catch it here
			} else if (_cfdata.getDataType() != cfData.CFJAVAOBJECTDATA && !(_cfdata instanceof cfJavaArrayData) && _class.isAssignableFrom(Class.forName("java.util.Vector"))) {
				return convertToVector(_cfdata);

			} else if (_cfdata.getDataType() != cfData.CFJAVAOBJECTDATA && _class.isArray()) {
				if (_cfdata.getDataType() == cfData.CFBINARYDATA && _class.getName().equals("[B")) {
					return ((cfBinaryData) _cfdata).getByteArray();
				} else if (_cfdata.getDataType() == cfData.CFARRAYDATA) {
					return getArray((cfArrayData) _cfdata, _class);
				} else {
					return null;
				}

			} else if (_class.isInstance(_cfdata)) {
				// cfArrayData implements java.util.List/System.Collections.IList
				// cfStructData implements Map
				// ...
				return _cfdata;
			}

			if (_cfdata instanceof cfJavaObjectData) {
				Object javaObj = ((cfJavaObjectData) _cfdata).getInstance();
				if ((javaObj != null) && (_class.isInstance(javaObj))) {
					return javaObj;
				}
			}
		} catch (Exception ignored) {
		} // allow to return null.

		return null;

	}// convertCFtoJava()

	private static Vector<Object> convertToVector(cfData _cfdata) {
		if (_cfdata.getDataType() == cfData.CFARRAYDATA) {
			Vector<Object> converted = new Vector<Object>();
			cfArrayData cfarray = (cfArrayData) _cfdata;
			Object nextObj;
			cfData next;
			int arrayLen = cfarray.size();

			for (int i = 1; i <= arrayLen; i++) {
				next = cfarray.getElement(i);
				nextObj = getNatural(next, true);
				if (nextObj == null) { // couldn't convert
					return null;
				}
				converted.addElement(nextObj);
			}

			return converted;
		} else {
			return null;
		}
	}

	/**
	 * If "deep" is set to true, objects contained within cfStructData and
	 * cfArrayData are also converted to natural objects. If "forceDouble" is set
	 * to true, all numeric cfData objects are forced into Double objects
	 * irrespective of their significant digits (or lack thereof). If
	 * "preferObjectArray" is true cfArrayData types are converted to Object[]
	 * instead of a VectorArrayList.
	 */
	public static Object getNatural(cfData _cfdata) {
		return tagUtilsNatural.getNatural(_cfdata, false, false, false);
	}

	public static Object getNatural(cfData _cfdata, boolean deep) {
		return tagUtilsNatural.getNatural(_cfdata, deep, false, false);
	}

	public static Object getNatural(cfData _cfdata, boolean deep, boolean forceDouble) {
		return tagUtilsNatural.getNatural(_cfdata, deep, forceDouble, false);
	}

	public static Object getNatural(cfData _cfdata, boolean deep, boolean forceDouble, boolean preferObjectArray) {
		return tagUtilsNatural.getNatural(_cfdata, deep, forceDouble, preferObjectArray);
	}

	public static Map<String, Object> getNaturalMap(cfStructData _struct) {
		return tagUtilsNatural.getNaturalMap(_struct, true, false, false);
	}

	/**
	 * converts the given cfArrayData to an array matching the given Class type.
	 * Returns null if the match cannot be done.
	 */

	private static Object getArray(cfArrayData _cfarray, Class<?> _class) {
		if (_cfarray instanceof cfFixedArrayData && _cfarray.getInstanceClass().equals(_class)) {
			try {
				return _cfarray.getInstance();
			} catch (cfmRunTimeException e) {
				return null;
			}
		}

		Object nextObj;

		// Determine the number of dimensions in the class we're converting the
		// array to.
		String classname = _class.getName();
		int numToDim = 0;
		while (classname.charAt(numToDim) == '[')
			numToDim++;

		// Get the number of dimensions in the array
		int numDimensions = _cfarray.getDimension();

		// If the dimensions don't match then return null
		if (numDimensions != numToDim)
			return null;

		// Determine the length of each dimension
		int[] dimLens = new int[numDimensions];
		cfArrayData innerArray = _cfarray;
		for (int i = 0; i < _cfarray.getDimension(); i++) {
			dimLens[i] = innerArray.size();
			if (i + 1 < _cfarray.getDimension())
				innerArray = (cfArrayData) innerArray.getElement(i + 1);
		}

		// Remove all but one dimension before calling translateToClass.
		classname = classname.substring(numToDim - 1);
		Class<?> arrayType = translateToClass(classname);

		// Create the array
		Object newArray = Array.newInstance(arrayType, dimLens);

		// Fill in the array
		int[] pos = new int[numDimensions];

		if ( _cfarray.size() > 0 ){
			while (true) {
				// Get the next value
				cfData next = _cfarray.getElement(pos[0] + 1);
				for (int i = 1; i < numDimensions; i++) {
					next = ((cfArrayData) next).getElement(pos[i] + 1);
				}
	
				// Set the value to null if necessary
				if (next instanceof cfNullData && !_class.isPrimitive()) {
					nextObj = null;
				} else {
					nextObj = convertCFtoJava(next, arrayType);
					if (nextObj == null)
						return null; // couldn't convert
				}
	
				// Set the value in the array
				Object innerMostArray = newArray;
				for (int i = 0; i < numDimensions - 1; i++)
					innerMostArray = Array.get(newArray, pos[i]);
				Array.set(innerMostArray, pos[numDimensions - 1], nextObj);
	
				// Increment the position
				boolean done = true;
				for (int i = 0; i < numDimensions; i++) {
					pos[i]++;
					if (pos[i] < dimLens[i]) {
						done = false;
						break;
					}
	
					pos[i] = 0;
				}
	
				// If we've reached the end of all of the dimensions then we are done
				if (done)
					break;
			}
		}
		
		return newArray;
	}// getArray

	// translates an Array name to a Class that represents the type
	private static Class<?> translateToClass(String _classname) {
		if (_classname.startsWith("[L")) {
			try {
				return Class.forName(_classname.substring(2, _classname.length() - 1));
			} catch (ClassNotFoundException unlikely) {
				return null;
			}
		} else if (_classname.equals("[B")) {
			return byte.class;
		} else if (_classname.equals("[C")) {
			return char.class;
		} else if (_classname.equals("[D")) {
			return double.class;
		} else if (_classname.equals("[F")) {
			return float.class;
		} else if (_classname.equals("[I")) {
			return int.class;
		} else if (_classname.equals("[J")) {
			return long.class;
		} else if (_classname.equals("[S")) {
			return short.class;
		} else if (_classname.equals("[Z")) {
			return boolean.class;
		} else {
			return null;
		}
	}

	// a valid UUID has the form XXXXXXXX-XXXX-XXXX-XXXXXXXXXXXXXXXX (8-4-4-16),
	// where each "X" is a valid hexadecimal digit (0-9 or A-F)
	public static boolean isUUID(cfData data) throws dataNotSupportedException {
		char[] dataChars = getChars(data, 35);
		if (dataChars == null)
			return false;

		for (int i = 19; i < dataChars.length; i++) {
			if (!isHexDigit(dataChars[i]))
				return false;
		}

		return true;
	}

	// a valid "guid" has the form XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
	// (8-4-4-4-12),
	// where each "X" is a valid hexadecimal digit (0-9 or A-F)
	public static boolean isGUID(cfData data) throws dataNotSupportedException {
		char[] dataChars = getChars(data, 36);
		if (dataChars == null)
			return false;

		if (dataChars[23] != '-')
			return false;

		for (int i = 19; i < 23; i++) {
			if (!isHexDigit(dataChars[i]))
				return false;
		}

		for (int i = 24; i < dataChars.length; i++) {
			if (!isHexDigit(dataChars[i]))
				return false;
		}

		return true;
	}

	public static boolean isXmlString(String str) {
		return str.trim().toLowerCase().startsWith("<?xml");
	}

	// check the common portion (8-4-4-) of the guid/UUID
	private static char[] getChars(cfData data, int length) throws dataNotSupportedException {
		if (data.getDataType() != cfData.CFSTRINGDATA)
			return null;

		char[] dataChars = data.getString().toUpperCase().toCharArray();

		if (dataChars.length != length)
			return null;

		if ((dataChars[8] != '-') || (dataChars[13] != '-') || (dataChars[18] != '-'))
			return null;

		for (int i = 0; i < 8; i++) {
			if (!isHexDigit(dataChars[i]))
				return null;
		}

		for (int i = 9; i < 13; i++) {
			if (!isHexDigit(dataChars[i]))
				return null;
		}

		for (int i = 14; i < 18; i++) {
			if (!isHexDigit(dataChars[i]))
				return null;
		}

		return dataChars;
	}

	// only accepts uppercase letters A-F
	private static boolean isHexDigit(char c) {
		return (c >= 48 && c <= 57) || (c >= 65 && c <= 70);
	}
}
