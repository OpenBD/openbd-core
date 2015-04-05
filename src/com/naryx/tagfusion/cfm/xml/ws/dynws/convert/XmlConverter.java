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

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Node;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;
import com.naryx.tagfusion.cfm.xml.cfXmlData;

/**
 * Converts xml data types to/from their BD equivalents.
 */
public class XmlConverter {
	public XmlConverter() {}

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
		// Must be a cfXmlData
		if (!(wrapper.value instanceof cfXmlData))
			return false;

		// Default type is String
		if (typeHint == null || typeHint.equals(Object.class))
			typeHint = String.class;

		// Cast to a cfXmlData object
		cfXmlData xml = (cfXmlData) wrapper.value;

		if (Node.class.isAssignableFrom(typeHint)) {
			// If this is a String type or Document type, we can convert it
			Node n = xml.getXMLNode();
			if (n.getNodeType() != Node.DOCUMENT_NODE)
				n = (cfXmlData.parseXml(xml.toString(), xml.isCaseSensitive(), null)).getXMLNode();
			wrapper.value = n;
			return true;
		} else if (String.class.isAssignableFrom(typeHint)) {
			// Return as a String
			wrapper.value = xml.toString();
			return true;
		} else {
			// Don't know what it is
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
		try {
			// Must be a String, a Node, or a MessageElement
			if (wrapper.value instanceof MessageElement) {
				wrapper.value = cfXmlData.parseXml(cfXmlData.toString(((MessageElement) wrapper.value).getAsDOM()), true, null);
				return true;
			} else if (wrapper.value instanceof Node) {
				wrapper.value = cfXmlData.parseXml(cfXmlData.toString((Node) wrapper.value), true, null);
				return true;
			} else if (wrapper.value instanceof String && tagUtils.isXmlString((String) wrapper.value)) {
				try {
					wrapper.value = cfXmlData.parseXml((String) wrapper.value, true, null);
					return true;
				} catch (cfmRunTimeException ex) {
					return false;
				}
			} else {
				// Cannot be converted to a cfXmlData
				return false;
			}
		} catch (cfmRunTimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Cannot convert type: " + TypeConverter.getTypeDesc(wrapper.value.getClass()) + ": " + Utils.getExceptionMessage(ex)));
		}
	}

}
