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

import java.lang.reflect.Field;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * Converts BD object to/from web service types using converters in this
 * package.
 */
public class TypeConverter {

	public TypeConverter() {
	}

	/**
	 * Returns an object suitable for sending through a web service proxy.
	 * 
	 * @param val
	 *          Object to convert.
	 * @param typeHint
	 *          Class used for hint purposes
	 * @param cl
	 *          ClassLoader that may contain the standard generated complex types
	 * @return Object suitable for sending through a web service proxy.
	 */
	public static Object toWebServiceType(Object val, Class typeHint, ClassLoader cl) throws cfmRunTimeException {
		if (val == null)
			return val;
		else if (typeHint != null && typeHint.equals(val.getClass()))
			return val;

		ObjectWrapper wrapper = new ObjectWrapper(val);
		if (HolderConverter.toWebServiceType(wrapper, typeHint, cl))
			return wrapper.value;
		else if (ByteArrayConverter.toWebServiceType(wrapper, typeHint, cl))
			return wrapper.value;
		else if (ArrayConverter.toWebServiceType(wrapper, typeHint, cl))
			return wrapper.value;
		else if (CFCConverter.toWebServiceType(wrapper, typeHint, cl))
			return wrapper.value;
		else if (QueryBeanConverter.toWebServiceType(wrapper, typeHint, cl))
			return wrapper.value;
		else if (StructConverter.toWebServiceType(wrapper, typeHint, cl))
			return wrapper.value;
		else if (XmlConverter.toWebServiceType(wrapper, typeHint, cl))
			return wrapper.value;
		else if (EnumConverter.toWebServiceType(wrapper, typeHint, cl))
			return wrapper.value;
		else if (SimpleConverter.toWebServiceType(wrapper, typeHint, cl))
			return wrapper.value;
		else if (IComplexConverter.toWebServiceType(wrapper, typeHint, cl) )
			return wrapper.value;
		else if (UnknownConverter.toWebServiceType(wrapper, typeHint, cl))
			return wrapper.value;
		else
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Cannot convert BD type: " + getTypeDesc(val.getClass()) + " to type: " + getTypeDesc(typeHint) + ". The target type may be unspecified or the BD type may be an unknown type."));
	}

	/**
	 * Returns an object suitable for using in the BD runtime.
	 * 
	 * @param val
	 *          Object to convert.
	 * @param session
	 *          cfSession class, needed for object creation
	 * @return Object suitable for using in the BD runtime.
	 */
	public static cfData toBDType(Object val, cfSession session) throws cfmRunTimeException {
		if (val == null)
			return cfNullData.NULL;
		else if (val instanceof cfData)
			return (cfData) val;

		ObjectWrapper wrapper = new ObjectWrapper(val);
		if (HolderConverter.toBDType(wrapper, session))
			return (cfData) wrapper.value;
		else if (ByteArrayConverter.toBDType(wrapper, session))
			return (cfData) wrapper.value;
		else if (ArrayConverter.toBDType(wrapper, session))
			return (cfData) wrapper.value;
		else if (XmlConverter.toBDType(wrapper, session))
			return (cfData) wrapper.value;
		else if (EnumConverter.toBDType(wrapper, session))
			return (cfData) wrapper.value;
		else if (SimpleConverter.toBDType(wrapper, session))
			return (cfData) wrapper.value;
		else if (CFCConverter.toBDType(wrapper, session))
			return (cfData) wrapper.value;
		else if (QueryBeanConverter.toBDType(wrapper, session))
			return (cfData) wrapper.value;
		else if (StructConverter.toBDType(wrapper, session))
			return (cfData) wrapper.value;
		else if (UnknownConverter.toBDType(wrapper, session))
			return (cfData) wrapper.value;
		else
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Cannot convert type: " + getTypeDesc(val.getClass()) + " to BD type. Type is most likely unknown/unspecified."));
	}

	/**
	 * Returns a String representation of the specified Class.
	 * 
	 * @param klass
	 *          Class for which to get a String representation
	 * @return
	 */
	public static String getTypeDesc(Class klass) {
		StringBuilder buffy = new StringBuilder();
		if (klass == null) {
			buffy.append("Unspecified");
		} else {
			if (klass.isAssignableFrom(Object.class)) {
				buffy.append("Unspecified ");
			} else if (!appendClassType(klass, buffy)) {
				buffy.append("{ ");
				buffy.append(" FIELDS: ");
				Field[] fields = klass.getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					appendClassType(fields[i].getType(), buffy);
					buffy.append(" " + fields[i].getName() + "; ");
				}
				/*
				 * WAY TOO MUCH TO DISPLAY buffy.append(" METHODS: "); Method[] methods
				 * = klass.getDeclaredMethods(); for (int i=0; i<methods.length; i++) {
				 * buffy.append(getClassTypeAsString(methods[i].getReturnType()) + " ");
				 * buffy.append(methods[i].getName() + "("); Class[] parmKlasses =
				 * methods[i].getParameterTypes(); for (int x=0; x<parmKlasses.length;
				 * x++) { if (x != 0) buffy.append(", ");
				 * buffy.append(getClassTypeAsString(parmKlasses[x]) + " arg" + x); }
				 * buffy.append("); "); }
				 */
				buffy.append(" }");
			}
			buffy.append(" ");
		}
		return buffy.toString();
	}

	/**
	 * Appends the String representation of the specified Class to the specified
	 * StringBuilder. Handles array types better than the default "[Lxxxxx;"
	 * system representation. Returns true if the type (component type) is a
	 * cfData class, false otherwise.
	 * 
	 * @param klass
	 *          Class for which a String representation is needed
	 * @param buffy
	 *          StringBuilder to which we will append
	 * @return true if the type (component type) is a cfData class, false
	 *         otherwise.
	 */
	private static boolean appendClassType(Class klass, StringBuilder buffy) {
		if (klass == null) {
			buffy.append("void ");
			return false;
		} else {
			StringBuilder brackets = new StringBuilder();
			Class tmp = klass;
			while (tmp.isArray()) {
				brackets.append("[]");
				tmp = tmp.getComponentType();
			}
			if (cfData.class.isAssignableFrom(tmp)) {
				buffy.append(tmp.getName() + brackets.toString());
				return true;
			} else {
				buffy.append(tmp.getName() + brackets.toString());
				return false;
			}
		}
	}
}
