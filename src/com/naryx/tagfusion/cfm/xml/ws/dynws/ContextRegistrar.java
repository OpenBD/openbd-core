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
 * Created on Mar 24, 2005
 *
 */
package com.naryx.tagfusion.cfm.xml.ws.dynws;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.DeserializerFactory;
import javax.xml.rpc.encoding.SerializerFactory;

import org.apache.axis.MessageContext;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;

import com.nary.util.string;

/**
 * Provides a callback interface for registering dynamically generated classes
 * with the current MessageContext's TypeMappingRegistry.
 */
public class ContextRegistrar {
	private MessageContext msgContext = null;

	private String scheme = null;

	/**
	 * Default constructor.
	 * 
	 * @param msgContext
	 */
	public ContextRegistrar(MessageContext msgContext, String scheme) {
		this.msgContext = msgContext;
		this.scheme = scheme;
	}

	/**
	 * Registers the specified Class with the current MessageContext's TypeMapping
	 * for use by Axis. This method will remove any existing entries for the same
	 * class type (QName equivalence) before adding new Bean
	 * serializer/deserializer entries.
	 * 
	 * @param kls
	 */
	public void registerClass(Class<?> kls) {
		// Add ser/deser pairs for these classes
		TypeMapping tm = msgContext.getTypeMapping();
		QName xmlType = convertToQName(kls, this.scheme);
		if (!tm.isRegistered(kls, xmlType)) {
			SerializerFactory sf = new BeanSerializerFactory(kls, xmlType);
			DeserializerFactory dsf = new BeanDeserializerFactory(kls, xmlType);
			tm.register(kls, xmlType, sf, dsf);
		}
	}

	/**
	 * Unregisters the specified Class from the current MessageContext's
	 * TypeMapping for use by Axis. This method will remove all existing entries
	 * for the class type (QName equivalence).
	 * 
	 * @param kls
	 */
	public void unregisterClass(Class<?> kls) {
		TypeMapping tm = msgContext.getTypeMapping();
		QName xmlType = convertToQName(kls, this.scheme);
		if (tm.isRegistered(kls, xmlType)) {
			tm.removeDeserializer(kls, xmlType);
			tm.removeSerializer(kls, xmlType);
		}
	}

	/**
	 * Returns a QName representation of the specified Class.
	 * 
	 * @param kls
	 * @param scm
	 * @return
	 */
	private QName convertToQName(Class<?> kls, String scm) {
		List<String> tokens = string.split(kls.getName(), ".");
		String ns = "";
		String n = "";
		for (int i = 0; i < tokens.size(); i++) {
			if (!n.equals(""))
				ns = n + "." + ns;
			n = tokens.get(i).toString();
		}
		if (ns.length() > 0)
			ns = ns.substring(0, ns.length() - 1);
		if (ns.equals(""))
			return new QName(n);
		else
			return new QName(scm + "://" + ns, n);
	}
}
