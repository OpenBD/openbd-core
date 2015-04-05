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

import java.util.List;
import java.util.Map;

/**
 * Interface for complex generated types to implement. Will make type conversion
 * faster by avoiding per field reflection.
 */
public interface IComplexObject
{
	/**
	 * Helper method that allows a Map of String=Object pairs
	 * to populate the fields of this instance. Populates missingRequiredFieldNames
	 * with the namess of fields that are either non-nullable or non-omittable 
	 * and do not have a value in the data Map.
	 * 
	 * @param data
	 * @param missingRequiredFieldNames
	 */
	public void bd_setFieldValues(Map<String, Object> data, List<String> missingRequiredFieldNames);
	
	/**
	 * Helper method that fills a Map with String=Object pairs
	 * representing the fields of this instance.
	 * 
	 * @param data   Map of String=Object pairs
	 */
	public void bd_getFieldValues(Map<String, Object> data);
	
	/**
	 * Helper method that fills a Map with String=Class pairs
	 * representing the field types of this instance.
	 * 
	 * @param data   Map of String=Class pairs
	 */
	public void bd_getFieldTypes(Map<String, Class<?>> data);
	
	/**
	 * Returns the name of the corresponding CFC if this IComplexObject
	 * were created from a CFC type to begin with. Otherwise, returns null.
	 * 
	 * @return   Name of the corresponding CFC if this IComplexObject were created
	 * 			from a CFC type to begin with, null otherwise.
	 */
	public String bd_getCfcName();
}
