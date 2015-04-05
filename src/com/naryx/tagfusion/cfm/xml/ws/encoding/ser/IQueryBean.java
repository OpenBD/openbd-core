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

/**
 * Interface for complex QueryBean like generated types to implement. Will make type conversion
 * faster by avoiding per field reflection.
 */
public interface IQueryBean
{
	/**
	 * Returns the columnList for this IQueryBean.
	 * 
	 * @return   The columnList for this IQueryBean.
	 */
    public String[] getColumnList();

    /**
     * Sets the columnList for this IQueryBean.
     * 
     * @param columnList   The columnList for this IQueryBean.
     */
    public void setColumnList(String[] columnList);

    /**
     * Returns the query data for this IQueryBean.
     * 
     * @return   The query data for this IQueryBean.
     */
    public Object[][] getData();

    /**
     * Sets the query data for this IQueryBean.
     * 
     * @param data   The query data for this IQueryBean.
     */
    public void setData(Object[][] data);

}
