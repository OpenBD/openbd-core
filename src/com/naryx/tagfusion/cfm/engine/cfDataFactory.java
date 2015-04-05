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

package com.naryx.tagfusion.cfm.engine;

import java.util.Enumeration;
import java.util.Hashtable;

import org.w3c.dom.Document;

import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.cfm.xml.ws.encoding.ser.QueryBean;
import com.naryx.tagfusion.cfm.xml.ws.encoding.ser.StructMap;

public class cfDataFactory
{

	private static Hashtable<String, Byte> typesAndTypeNames = new Hashtable<String, Byte>();
	private static Hashtable<String, Class<?>> typesAndTypeClasses = new Hashtable<String, Class<?>>();
	private static Hashtable<String, Class<?>> typesAndJavaClasses = new Hashtable<String, Class<?>>();
	
	// static constructor
	static 
	{	
		// standard types
		typesAndTypeNames.put("ARRAY", new Byte(cfData.CFARRAYDATA));
		typesAndTypeNames.put("BINARY", new Byte(cfData.CFBINARYDATA));
		typesAndTypeNames.put("BOOLEAN", new Byte(cfData.CFBOOLEANDATA));
		typesAndTypeNames.put("DATE", new Byte(cfData.CFDATEDATA));
		typesAndTypeNames.put("NUMERIC", new Byte(cfData.CFNUMBERDATA));
		typesAndTypeNames.put("QUERY", new Byte(cfData.CFQUERYRESULTDATA));
		typesAndTypeNames.put("STRING", new Byte(cfData.CFSTRINGDATA));
		typesAndTypeNames.put("STRUCT", new Byte(cfData.CFSTRUCTDATA));

		// new types
		typesAndTypeNames.put("GUID", new Byte(cfData.CFSTRINGDATA));
		typesAndTypeNames.put("UUID", new Byte(cfData.CFSTRINGDATA));
		
		// non-standard types
		typesAndTypeNames.put("UNKNOWN", new Byte(cfData.UNKNOWN));
		typesAndTypeNames.put("COMPONENT", new Byte(cfData.CFCOMPONENTOBJECTDATA));
		typesAndTypeNames.put("JAVA", new Byte(cfData.CFJAVAOBJECTDATA));
		typesAndTypeNames.put("LDATA", new Byte(cfData.CFLDATA));
		typesAndTypeNames.put("NULL", new Byte(cfData.CFNULLDATA));
		typesAndTypeNames.put("OTHER", new Byte(cfData.OTHER));
		typesAndTypeNames.put("ANY", new Byte(cfData.OTHER));
		typesAndTypeNames.put("VOID", new Byte(cfData.CFNULLDATA));		 	 
		typesAndTypeNames.put("QUERY", new Byte(cfData.CFQUERYRESULTDATA));
		typesAndTypeNames.put("VARIABLENAME", new Byte(cfData.OTHER));

		// classes
		typesAndTypeClasses.put("ARRAY", cfArrayData.class);
		typesAndTypeClasses.put("BINARY", cfBinaryData.class);
		typesAndTypeClasses.put("BOOLEAN", cfBooleanData.class);
		typesAndTypeClasses.put("DATE", cfDateData.class);
		typesAndTypeClasses.put("NUMERIC", cfNumberData.class);
		typesAndTypeClasses.put("QUERY", cfQueryInterface.class);
		typesAndTypeClasses.put("STRING", cfStringData.class);
		typesAndTypeClasses.put("STRUCT", cfStructData.class);
		typesAndTypeClasses.put("GUID", cfStringData.class);
		typesAndTypeClasses.put("UUID", cfStringData.class);
		typesAndTypeClasses.put("QUERY", cfQueryResultData.class);
		typesAndTypeClasses.put("XML", cfXmlData.class);
		typesAndTypeClasses.put("UNKNOWN", cfData.class);
		typesAndTypeClasses.put("COMPONENT", cfComponentData.class);
		typesAndTypeClasses.put("JAVA", cfJavaObjectData.class);
		typesAndTypeClasses.put("NULL", cfNullData.class);
		typesAndTypeClasses.put("OTHER", cfData.class);
		typesAndTypeClasses.put("ANY", cfData.class);

		// java types
		typesAndJavaClasses.put("ARRAY", Object[].class);
		typesAndJavaClasses.put("BINARY", byte[].class);
		typesAndJavaClasses.put("BOOLEAN", Boolean.class);
		typesAndJavaClasses.put("DATE", java.util.Date.class);
		typesAndJavaClasses.put("NUMERIC", Double.class);
		typesAndJavaClasses.put("STRING", String.class);
		typesAndJavaClasses.put("STRUCT", StructMap.class);
		typesAndJavaClasses.put("GUID", String.class);
		typesAndJavaClasses.put("UUID", String.class);
		typesAndJavaClasses.put("QUERY", QueryBean.class);
		typesAndJavaClasses.put("XML", Document.class);
		typesAndJavaClasses.put("UNKNOWN", Object.class);
		typesAndJavaClasses.put("COMPONENT", StructMap.class);
		typesAndJavaClasses.put("JAVA", Object.class);
		typesAndJavaClasses.put("OTHER", Object.class);
		typesAndJavaClasses.put("ANY", Object.class);
	}

	public static byte getDatatypeByteValue(String name)
	{
		Byte type = null;
		
		type = typesAndTypeNames.get(name.toUpperCase());
		if(type == null)
		{
			type = new Byte(cfData.UNKNOWN);
		}
		
		return type.byteValue();
	}
	
	
	public static String getDatatypeString( cfData data )
	{	
		if ( data.getDataType() == cfData.OTHER ) {
			return new String( "any" );
		} else if ( data.getDataType() == cfData.CFCOMPONENTOBJECTDATA ) {
			cfComponentData componentData = (cfComponentData)data;
			return componentData.getComponentName() + ", " + componentData.getComponentPath();
		}
		
		return data.getDataTypeName();
	}
	
	public static Class<?> getDatatypeJavaClass(String type)
	{
		String name = null;
		Enumeration<?> enumer = typesAndJavaClasses.keys();
		while (enumer.hasMoreElements())
		{
			name = (String)enumer.nextElement();
			if (name.equalsIgnoreCase(type.trim()))
				return typesAndJavaClasses.get(name);
		}
		return null;
	}
}
