/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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
package com.naryx.tagfusion.cfm.xml.ws.encoding.ser;

import java.io.Serializable;
import java.util.List;

/**
 * StructMap that holds Dictionary/Hashtable like data.
 */
public class StructMap implements Serializable, IStructMap {
	private static final long serialVersionUID = 1L;

	public StructMapItem[] item = null;

	/**
	 * Default consructor.
	 */
	public StructMap() {
	}

	/**
	 * Gets the StructMapItem array. This accessor is necessary for Axis to
	 * properly recognize this as a JavaBean. Note: removing this accessor or
	 * adding an indexed accessor will cause Axis to generate WSDL that does not
	 * use the ArrayOf_ prefix (see
	 * http://issues.apache.org/jira/browse/AXIS-1673).
	 * 
	 * @return the StructMapItem array.
	 */
	public StructMapItem[] getItem() {
		return item;
	}

	/**
	 * Sets the StructMapItem array. This accessor is necessary for Axis to
	 * properly recognize this as a JavaBean. Note: removing this accessor or
	 * adding an indexed accessor will cause Axis to generate WSDL that does not
	 * use the ArrayOf_ prefix (see
	 * http://issues.apache.org/jira/browse/AXIS-1673).
	 * 
	 * @param val
	 *          StructMapItem array to set
	 */
	public void setItem(StructMapItem[] val) {
		this.item = val;
	}

	/**
	 * Populates the specified IList with all the keys corresponding to each entry
	 * in this IStructMap.
	 * 
	 * @param keys
	 *          List into which the entries' keys should be added.
	 */
	public void getKeys(List keys) {
		if (item != null) {
			for (int i = 0; i < item.length; i++)
				keys.add(item[i].key);
		}
	}

	/**
	 * Returns the object associated with the specified key or null.
	 * 
	 * @param key
	 *          Key for the value to retrieve.
	 * @return The object associated with the specified key or null.
	 */
	public Object getItem(Object key) {
		if (item != null) {
			for (int i = 0; i < item.length; i++) {
				if (item[i].key.equals(key))
					return item[i].val;
			}
		}
		return null;
	}

	/**
	 * Sets the key=value pair for this IStructMap.
	 * 
	 * @param key
	 *          Key for the value to insert/overwrite.
	 * @param val
	 *          Value to insert/overwrite.
	 */
	public void setItem(Object key, Object val) {
		if (item == null)
			item = new StructMapItem[0];

		for (int i = 0; i < item.length; i++) {
			if (item[i].key.equals(key)) {
				item[i].val = val;
				return;
			}
		}
		StructMapItem[] items = new StructMapItem[item.length + 1];
		System.arraycopy(item, 0, items, 0, item.length);
		items[items.length - 1] = new StructMapItem(key, val);
		this.item = items;
	}

	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(StructMap.class);

	/*
	 * It's necessary to set the xml type directly here because there'll be a
	 * class conflict for dynamic classes generated on BD Java for the client and
	 * this class (which would be used to generate the WSDL on the server side ---
	 * also on BD Java). Yes this problem only exists for BD Java clients that are
	 * running in the same BD Java instance as the server.
	 */
	static {
		typeDesc.setXmlType(new javax.xml.namespace.QName("http://wstypes.newatlanta.com", "StructMap"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("item");
		elemField.setArrayType(new javax.xml.namespace.QName("http://wstypes.newatlanta.com", "StructMapItem"));
		elemField.setXmlName(new javax.xml.namespace.QName("", "item"));
		// elemField.setXmlType(new
		// javax.xml.namespace.QName("http://wstypes.newatlanta.com",
		// "StructMapItem"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
	}

	/**
	 * Return type metadata object
	 */
	public static org.apache.axis.description.TypeDesc getTypeDesc() {
		return typeDesc;
	}

	/**
	 * Get Custom Serializer
	 */
	public static org.apache.axis.encoding.Serializer getSerializer(java.lang.String mechType, java.lang.Class _javaType, javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanSerializer(_javaType, _xmlType, typeDesc);
	}

	/**
	 * Get Custom Deserializer
	 */
	public static org.apache.axis.encoding.Deserializer getDeserializer(java.lang.String mechType, java.lang.Class _javaType, javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType, _xmlType, typeDesc);
	}

}
