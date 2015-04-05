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

/**
 * Object to hold QueryBean data.
 * 
 */
public class QueryBean implements Serializable, IQueryBean {
	private static final long serialVersionUID = 1L;

	public String[] columnList = null;

	public Object[][] data = null;

	/**
	 * Default constructor.
	 */
	public QueryBean() {
		setColumnList(new String[0]);
		setData(new Object[0][]);
	}

	/**
	 * Alternate constructor. Takes the columnList and data.
	 * 
	 * @param columns
	 *          columnList for this QueryBean
	 * @param data
	 *          data for this QueryBean
	 */
	public QueryBean(String[] columns, Object[][] data) {
		setColumnList(columns);
		setData(data);
	}

	/**
	 * Returns the columnList for this IQueryBean.
	 * 
	 * @return The columnList for this IQueryBean.
	 */
	public String[] getColumnList() {
		return columnList;
	}

	/**
	 * Sets the columnList for this IQueryBean.
	 * 
	 * @param columnList
	 *          The columnList for this IQueryBean.
	 */
	public void setColumnList(String[] columnList) {
		this.columnList = columnList;
	}

	/**
	 * Returns the query data for this IQueryBean.
	 * 
	 * @return The query data for this IQueryBean.
	 */
	public Object[][] getData() {
		return data;
	}

	/**
	 * Sets the query data for this IQueryBean.
	 * 
	 * @param data
	 *          The query data for this IQueryBean.
	 */
	public void setData(Object[][] data) {
		this.data = data;
	}

	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(QueryBean.class);

	/*
	 * It's necessary to set the xml type directly here because there'll be a
	 * class conflict for dynamic classes generated on BD Java for the client and
	 * this class (which would be used to generate the WSDL on the server side ---
	 * also on BD Java). Yes this problem only exists for BD Java clients that are
	 * running in the same BD Java instance as the server.
	 */
	static {
		typeDesc.setXmlType(new javax.xml.namespace.QName("http://wstypes.newatlanta.com", "QueryBean"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("data");
		elemField.setArrayType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
		elemField.setXmlName(new javax.xml.namespace.QName("", "data"));
		// elemField.setXmlType(new
		// javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
		// "anyType"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("columnList");
		elemField.setArrayType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setXmlName(new javax.xml.namespace.QName("", "columnList"));
		// elemField.setXmlType(new
		// javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
