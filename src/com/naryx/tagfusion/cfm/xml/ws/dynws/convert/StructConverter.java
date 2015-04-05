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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.cfm.xml.cfXmlDataAttributeStruct;
import com.naryx.tagfusion.cfm.xml.ws.dynws.DynamicCacheClassLoader;
import com.naryx.tagfusion.cfm.xml.ws.encoding.ser.IComplexObject;
import com.naryx.tagfusion.cfm.xml.ws.encoding.ser.IStructMap;
import com.naryx.tagfusion.cfm.xml.ws.encoding.ser.StructMap;

/**
 * Converts struct data types to/from their BD equivalents.
 */
public class StructConverter {
	public StructConverter() {
	}

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
		// Must be a struct and not a cfc or xml object
		if (!((wrapper.value instanceof cfStructData) && !(wrapper.value instanceof cfXmlData) && !(wrapper.value instanceof cfXmlDataAttributeStruct) && !(wrapper.value instanceof cfComponentData)))
			return false;

		// Default type is the Assembly's IStructMap implementation or our StructMap
		// if no such implementation exists (even if System.Object is specified)
		if (typeHint == null || typeHint.equals(Object.class))
			typeHint = getIStructMapType(cl);

		if (IStructMap.class.isAssignableFrom(typeHint)) {
			// Convert it into a IStructMap
			return cfStructDataToIStructMap(wrapper, typeHint, cl);
		} else if (IComplexObject.class.isAssignableFrom(typeHint)) {
			// Convert it into a IComplexObject and hope the cfStructData values
			// match up to the fields on the IComplexObject.
			return cfStructDataToIComplexObject(wrapper, typeHint, cl);
		} else if (Map.class.isAssignableFrom(typeHint)) {
			// Convert it into a Map
			return cfStructDataToMap(wrapper, typeHint, cl);
		} else {
			// Don't know what this is.
			return false;
		}
	}

	/**
	 * Converts the cfStructData in the ObjectWrapper into an IStructMap
	 * implementation. Returns true if the cfStructData was successfully
	 * converted/replaced, false otherwise.
	 * 
	 * @param wrapper
	 *          ObjectWrapper containing the cfStructData to convert/replace.
	 * @param typeHint
	 *          Class into which we need to convert the Object.
	 * @param cl
	 *          ClassLoader that may contain the specified typeHint.
	 * @return true if the cfStructData was successfully converted/replaced, false
	 *         otherwise.
	 * @throws cfmRunTimeException
	 */
	private static boolean cfStructDataToIStructMap(ObjectWrapper wrapper, Class typeHint, ClassLoader cl) throws cfmRunTimeException {
		// Cast to a cfStructData object
		cfStructData struct = (cfStructData) wrapper.value;

		IStructMap rtn = null;
		try {
			// Create the type
			rtn = (IStructMap) typeHint.newInstance();

			// Add all the cfStructData entries to the IStructMap
			java.util.Iterator itr = struct.keySet().iterator();
			while (itr.hasNext()) {
				Object k = itr.next();
				Object v = struct.getData(k.toString());
				rtn.setItem(k, TypeConverter.toWebServiceType(v, null, cl));
			}

			wrapper.value = rtn;
			return true;
		} catch (cfmRunTimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Cannot convert type: " + TypeConverter.getTypeDesc(wrapper.value.getClass()) + ": " + Utils.getExceptionMessage(ex)));
		}
	}

	/**
	 * Converts the cfStructData in the ObjectWrapper into an IComplexObject
	 * implementation. Returns true if the cfStructData was successfully
	 * converted/replaced, false otherwise.
	 * 
	 * @param wrapper
	 *          ObjectWrapper containing the cfStructData to convert/replace.
	 * @param typeHint
	 *          Class into which we need to convert the Object.
	 * @param cl
	 *          ClassLoader that may contain the specified typeHint.
	 * @return true if the cfStructData was successfully converted/replaced, false
	 *         otherwise.
	 * @throws cfmRunTimeException
	 */
	private static boolean cfStructDataToIComplexObject(ObjectWrapper wrapper, Class typeHint, ClassLoader cl) throws cfmRunTimeException {
		// Cast to a cfStructData object
		cfStructData struct = (cfStructData) wrapper.value;

		IComplexObject rtn = null;
		try {
			// Create the type
			rtn = (IComplexObject) typeHint.newInstance();

			// Add all the cfStructData entries to the IComplexObject
			Map props = new FastMap(FastMap.CASE_INSENSITIVE);
			Map propTypes = new FastMap(FastMap.CASE_INSENSITIVE);
			rtn.bd_getFieldTypes(propTypes);
			java.util.Iterator itr = struct.keySet().iterator();
			while (itr.hasNext()) {
				Object k = itr.next();
				Object v = struct.getData(k.toString());
				props.put(k, TypeConverter.toWebServiceType(v, (Class) propTypes.get(k), cl));
			}
			List missing = new LinkedList();
			rtn.bd_setFieldValues(props, missing);
			if (missing.size() > 0) {
				StringBuilder buffy = new StringBuilder();
				Iterator e = missing.iterator();
				while (e.hasNext())
					buffy.append((String) e.next() + ", ");
				String names = buffy.toString().trim();
				throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Cannot convert type " + TypeConverter.getTypeDesc(wrapper.value.getClass()) + " to target type " + TypeConverter.getTypeDesc(typeHint) + ". Missing value for field(s) " + names + " which cannot be omitted according to the web service WSDL."));
			}
		} catch (cfmRunTimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Cannot convert type: " + TypeConverter.getTypeDesc(wrapper.value.getClass()) + ": " + Utils.getExceptionMessage(ex)));
		}

		wrapper.value = rtn;
		return true;
	}

	/**
	 * Converts the cfStructData in the ObjectWrapper into a Map implementation.
	 * Returns true if the cfStructData was successfully converted/replaced, false
	 * otherwise.
	 * 
	 * @param wrapper
	 *          ObjectWrapper containing the cfStructData to convert/replace.
	 * @param typeHint
	 *          Class into which we need to convert the Object.
	 * @param cl
	 *          ClassLoader that may contain the specified typeHint.
	 * @return true if the cfStructData was successfully converted/replaced, false
	 *         otherwise.
	 * @throws cfmRunTimeException
	 */
	private static boolean cfStructDataToMap(ObjectWrapper wrapper, Class typeHint, ClassLoader cl) throws cfmRunTimeException {
		// Cast to a cfStructData object
		cfStructData struct = (cfStructData) wrapper.value;

		Map rtn = null;
		try {
			// Create the type
			rtn = (Map) typeHint.newInstance();

			// Add all the cfStructData entries to the IStructMap
			java.util.Iterator itr = struct.keySet().iterator();
			while (itr.hasNext()) {
				Object k = itr.next();
				Object v = struct.getData(k.toString());
				rtn.put(k, TypeConverter.toWebServiceType(v, null, cl));
			}

			wrapper.value = rtn;
			return true;
		} catch (cfmRunTimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Cannot convert type: " + TypeConverter.getTypeDesc(wrapper.value.getClass()) + ": " + Utils.getExceptionMessage(ex)));
		}
	}

	/**
	 * Returns the type for an IStructMap. If a locally generated type exists,
	 * this will return that type. If not, this will return the default StructMap
	 * type.
	 * 
	 * @param cl
	 *          ClassLoader that may contain the preferred IStructMap
	 *          implementation type
	 * @return preferred IStructMap implementation type
	 */
	private static Class getIStructMapType(ClassLoader cl) {
		if (cl != null && cl instanceof DynamicCacheClassLoader) {
			// Use the ClassLoader's specified impl
			DynamicCacheClassLoader dcl = (DynamicCacheClassLoader) cl;
			Class rtn = dcl.getIStructMap();
			if (rtn != null)
				return rtn;
		}
		// Otherwise return the default
		return StructMap.class;
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
		// If it's an IStructMap or Map we can handle it
		if (wrapper.value instanceof IStructMap)
			return iStructMapToBDType(wrapper, session);
		else if (wrapper.value instanceof Map)
			return mapToBDType(wrapper, session);
		else
			return false;
	}

	/**
	 * Converts the IStructMap in the ObjectWrapper into a cfData. Returns true if
	 * the IStructMap was successfully converted/replaced, false otherwise.
	 * 
	 * @param wrapper
	 *          ObjectWrapper containing the IStructMap to convert/replace.
	 * @param session
	 *          cfSession class, needed for object creation
	 * @return true if the IStructMap was successfully converted/replaced, false
	 *         otherwise.
	 * @throws cfmRunTimeException
	 */
	private static boolean iStructMapToBDType(ObjectWrapper wrapper, cfSession session) throws cfmRunTimeException {
		// Create the cfStructData
		cfStructData rtn = new cfStructData();

		// Get each entry out of the IStructMap and add it to the cfStructData
		IStructMap sm = (IStructMap) wrapper.value;
		List keys = new LinkedList();
		sm.getKeys(keys);
		Iterator e = keys.iterator();
		while (e.hasNext()) {
			Object k = e.next();
			Object v = sm.getItem(k);
			if (k != null && v != null)
				rtn.setData(k.toString(), TypeConverter.toBDType(v, session));
		}

		wrapper.value = rtn;
		return true;
	}

	/**
	 * Converts the Map in the ObjectWrapper into a cfData. Returns true if the
	 * Map was successfully converted/replaced, false otherwise.
	 * 
	 * @param wrapper
	 *          ObjectWrapper containing the Map to convert/replace.
	 * @param session
	 *          cfSession class, needed for object creation
	 * @return true if the Map was successfully converted/replaced, false
	 *         otherwise.
	 * @throws cfmRunTimeException
	 */
	private static boolean mapToBDType(ObjectWrapper wrapper, cfSession session) throws cfmRunTimeException {
		// Create the cfStructData
		cfStructData rtn = new cfStructData();

		// Get each entry out of the Map and add it to the cfStructData
		Map sm = (Map) wrapper.value;
		Iterator e = sm.keySet().iterator();
		while (e.hasNext()) {
			Object k = e.next();
			Object v = sm.get(k);
			if (k != null && v != null)
				rtn.setData(k.toString(), TypeConverter.toBDType(v, session));
		}

		wrapper.value = rtn;
		return true;
	}
}
