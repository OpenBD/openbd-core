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

/**
 * Interface for complex generated types to implement. Will make type conversion
 * faster by avoiding per field reflection.
 */
public interface IStructMap
{
	/**
	 * Populates the specified IList with all the keys corresponding
	 * to each entry in this IStructMap.
	 * 
	 * @param keys   List into which the entries' keys should be added.
	 */
    public void getKeys(List keys);

    /**
     * Returns the object associated with the specified key or null.
     * 
     * @param key   Key for the value to retrieve.
     * @return   The object associated with the specified key or null.
     */
    public Object getItem(Object key);

    /**
     * Sets the key=value pair for this IStructMap.
     * 
     * @param key   Key for the value to insert/overwrite.
     * @param val   Value to insert/overwrite.
     */
    public void setItem(Object key, Object val);

}
