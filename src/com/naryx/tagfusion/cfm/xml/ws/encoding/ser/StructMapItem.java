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

/*
 * Created on Dec 29, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.naryx.tagfusion.cfm.xml.ws.encoding.ser;

import java.io.Serializable;

/**
 * Holds the key=value pairs for a StructMap.
 */
public class StructMapItem implements Serializable {
	private static final long serialVersionUID = 1L;

	public Object key = null;

	public Object val = null;

	/**
	 * Default constructor.
	 */
	public StructMapItem() {
	}

	/**
	 * Alternate constructor. Takes the key and value.
	 * 
	 * @param k
	 *          key for this StructMapItem
	 * @param v
	 *          value for this StructMapItem
	 */
	public StructMapItem(Object k, Object v) {
		this.key = k;
		this.val = v;
	}

	/**
	 * Returns the key for this StructMapItem.
	 * 
	 * @return key for this StructMapItem.
	 */
	public Object getKey() {
		return this.key;
	}

	/**
	 * Sets the key for this StructMapItem.
	 * 
	 * @param k
	 *          key for this StructMapItem.
	 */
	public void setKey(Object k) {
		this.key = k;
	}

	/**
	 * Returns the value for this StructMapItem.
	 * 
	 * @return value for this StructMapItem.
	 */
	public Object getVal() {
		return this.val;
	}

	/**
	 * Sets the value for this StructMapItem.
	 * 
	 * @param v
	 *          value for thist StructMapItem.
	 */
	public void setVal(Object v) {
		this.val = v;
	}

	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(StructMapItem.class);

	/*
	 * It's necessary to set the xml type directly here because there'll be a
	 * class conflict for dynamic classes generated on BD Java for the client and
	 * this class (which would be used to generate the WSDL on the server side ---
	 * also on BD Java). Yes this problem only exists for BD Java clients that are
	 * running in the same BD Java instance as the server.
	 */
	static {
		typeDesc.setXmlType(new javax.xml.namespace.QName("http://wstypes.newatlanta.com", "StructMapItem"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("key");
		elemField.setXmlName(new javax.xml.namespace.QName("", "key"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("val");
		elemField.setXmlName(new javax.xml.namespace.QName("", "val"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
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
