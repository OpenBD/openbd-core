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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import javax.xml.rpc.holders.Holder;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.ws.encoding.ser.IHolder;

public class HolderConverter {

	public HolderConverter() {}

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

		// See if the typeHint is specified and is a javax.xml.rpc.holders.Holder
		// or IHolder implementation.
		if (typeHint != null && IHolder.class.isAssignableFrom(typeHint))
			return cfDataToIHolder(wrapper, typeHint, cl);
		if (typeHint != null && Holder.class.isAssignableFrom(typeHint))
			return cfDataToHolder(wrapper, typeHint, cl);
		else
			return false;
	}

	/**
	 * Converts the Object in the ObjectWrapper into an IHolder implementation
	 * Class specified by typeHint. Returns true if the Object was successfully
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
	private static boolean cfDataToIHolder(ObjectWrapper wrapper, Class typeHint, ClassLoader cl) throws cfmRunTimeException {
		IHolder holder = null;
		try {
			// Create an instance of the specified type
			holder = (IHolder) typeHint.newInstance();
			holder.setValueAsObject(TypeConverter.toWebServiceType(wrapper.value, holder.getValueClass(), cl));
			wrapper.value = holder;
			return true;
		} catch (cfmRunTimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Cannot convert type: " + TypeConverter.getTypeDesc(wrapper.value.getClass()) + ": " + Utils.getExceptionMessage(ex)));
		}
	}

	/**
	 * Converts the Object in the ObjectWrapper into a
	 * javax.xml.rpc.holders.Holder Class specified by typeHint. Returns true if
	 * the Object was successfully converted/replaced, false otherwise.
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
	private static boolean cfDataToHolder(ObjectWrapper wrapper, Class typeHint, ClassLoader cl) throws cfmRunTimeException {
		Holder holder = null;
		try {
			// Get the default constructor
			Constructor c = typeHint.getConstructor(new Class[0]);
			// Get the "value" field all Holder's have and convert
			// our val object into the type of the "value" field
			Field f = typeHint.getDeclaredField("value");
			// Create the instance
			holder = (Holder) c.newInstance(new Object[0]);
			f.set(holder, TypeConverter.toWebServiceType(wrapper.value, f.getType(), cl));
		} catch (cfmRunTimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Cannot convert type: " + TypeConverter.getTypeDesc(wrapper.value.getClass()) + ": " + Utils.getExceptionMessage(ex)));
		}

		if (holder == null)
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Cannot convert type: " + TypeConverter.getTypeDesc(wrapper.value.getClass())));

		// Return the holder
		wrapper.value = holder;
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
		// Must be a javax.xml.rpc.holders.Holder or IHolder instance
		if (wrapper.value instanceof IHolder)
			return iHolderToBDType(wrapper, session);
		else if (wrapper.value instanceof Holder)
			return holderToBDType(wrapper, session);
		else
			return false;
	}

	/**
	 * Converts the IHolder in the ObjectWrapper into a cfData. Returns true if
	 * the IHolder was successfully converted/replaced, false otherwise.
	 * 
	 * @param wrapper
	 *          ObjectWrapper containing the IHolder to convert/replace.
	 * @param session
	 *          cfSession class, needed for object creation
	 * @return true if the IHolder was successfully converted/replaced, false
	 *         otherwise.
	 * @throws cfmRunTimeException
	 */
	public static boolean iHolderToBDType(ObjectWrapper wrapper, cfSession session) throws cfmRunTimeException {
		// Cast as a IHolder instance
		IHolder holder = (IHolder) wrapper.value;

		// Get the value
		wrapper.value = TypeConverter.toBDType(holder.getValueAsObject(), session);
		return true;
	}

	/**
	 * Converts the Holder in the ObjectWrapper into a cfData. Returns true if the
	 * Holder was successfully converted/replaced, false otherwise.
	 * 
	 * @param wrapper
	 *          ObjectWrapper containing the Holder to convert/replace.
	 * @param session
	 *          cfSession class, needed for object creation
	 * @return true if the Holder was successfully converted/replaced, false
	 *         otherwise.
	 * @throws cfmRunTimeException
	 */
	public static boolean holderToBDType(ObjectWrapper wrapper, cfSession session) throws cfmRunTimeException {
		// Cast as a Holder instance
		Holder holder = (Holder) wrapper.value;

		// Return the "value" field's value as a cfData object
		try {
			Field f = holder.getClass().getDeclaredField("value");
			wrapper.value = TypeConverter.toBDType(f.get(holder), session);
			return true;
		} catch (cfmRunTimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Cannot convert type: " + TypeConverter.getTypeDesc(wrapper.value.getClass()) + ": " + Utils.getExceptionMessage(ex)));
		}
	}
}
