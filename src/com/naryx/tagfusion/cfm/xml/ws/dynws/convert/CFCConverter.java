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
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.ws.dynws.DynamicCacheClassLoader;
import com.naryx.tagfusion.cfm.xml.ws.encoding.ser.IComplexObject;

/**
 * Converts complex object data types to/from their BD equivalents.
 */
public class CFCConverter {

	public CFCConverter() {}

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
	public static boolean toWebServiceType(ObjectWrapper wrapper, Class<?> typeHint, ClassLoader cl) throws cfmRunTimeException {
		// Must be a CFC
		if (!(wrapper.value instanceof cfComponentData))
			return false;

		// Default type is a name matching the cfcName in the ClassLoader, or Object
		// if no such Class can be found (even if Object is specified)
		if (typeHint == null || typeHint.equals(Object.class)) {
			typeHint = getIComplexObjectType(((cfComponentData) wrapper.value).getMetaData().getData("NAME").toString(), cl);
		}

		if (IComplexObject.class.isAssignableFrom(typeHint)) {
			// Convert it into a IComplexObject
			return cfComponentDataToIComplexObject(wrapper, typeHint, cl);
		} else {
			// Don't know what this is.
			return false;
		}
	}

	/**
	 * Converts the cfComponentData in the ObjectWrapper into an IComplexObject
	 * implementation. Returns true if the Object was successfully
	 * converted/replaced, false otherwise.
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
	private static boolean cfComponentDataToIComplexObject(ObjectWrapper wrapper, Class<?> typeHint, ClassLoader cl) throws cfmRunTimeException {
		// Cast to a cfComponentObjectData object
		cfComponentData cfc = (cfComponentData) wrapper.value;

		IComplexObject rtn = null;
		try {
			// Create the type
			rtn = (IComplexObject) typeHint.newInstance();

			// Get all the properties from the IComplexObject and set all the
			// cfComponentData
			// fields corresponding to those properties on the IComplexObject
			Map<String, Class<?>> propTypes = new FastMap<String, Class<?>>(FastMap.CASE_INSENSITIVE);
			rtn.bd_getFieldTypes(propTypes);
			Map<String, Object> props = new FastMap<String, Object>(FastMap.CASE_INSENSITIVE);
			Iterator<String> e = propTypes.keySet().iterator();
			while (e.hasNext()) {
				String k = e.next();
				Object v = cfc.getData(k.toString());
				if (v != null)
					props.put(k, TypeConverter.toWebServiceType(v, propTypes.get(k), cl));
			}
			List<String> missing = new LinkedList<String>();
			rtn.bd_setFieldValues(props, missing);
			if (missing.size() > 0) {
				StringBuilder buffy = new StringBuilder();
				Iterator<String> ee = missing.iterator();
				while (ee.hasNext())
					buffy.append(ee.next() + ", ");
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
	 * Returns the type for an IComplexObject that corresponds to the specified
	 * cfcName. If a locally generated type exists, this will return that type. If
	 * not, this will return the default, the Object type.
	 * 
	 * @param cfcName
	 *          name of the corresponding cfComponentData
	 * @param cl
	 *          ClassLoader that may contain the preferred IStructMap
	 *          implementation type
	 * @return IComplexObject implementation type for the specified cfcName
	 */
	private static Class<?> getIComplexObjectType(String cfcName, ClassLoader cl) {
		if (cl != null && cl instanceof DynamicCacheClassLoader) {
			// Use the ClassLoader's specified impl
			DynamicCacheClassLoader dcl = (DynamicCacheClassLoader) cl;
			Class<?> rtn = dcl.findIComplexObject(cfcName);
			if (rtn != null)
				return rtn;
		}
		// Otherwise return the default
		return Object.class;
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
		// If it's an IComplexObject we can handle it
		if (!(wrapper.value instanceof IComplexObject))
			return false;

		// Create the cfComponentData
		IComplexObject co = (IComplexObject) wrapper.value;
		if (co.bd_getCfcName() == null)
			return false;
		cfComponentData rtn = new cfComponentData(session, co.bd_getCfcName());

		// Get all the properties from the IComplexObject and set them as data
		// on the cfComponentData.
		Map<String, Object> props = new FastMap<String, Object>(FastMap.CASE_INSENSITIVE);
		co.bd_getFieldValues(props);
		Iterator<String> e = props.keySet().iterator();
		while (e.hasNext()) {
			String k = e.next();
			Object v = props.get(k);
			if (k != null && v != null)
				rtn.put(k, TypeConverter.toBDType(v, session));
		}

		wrapper.value = rtn;
		return true;
	}

}
