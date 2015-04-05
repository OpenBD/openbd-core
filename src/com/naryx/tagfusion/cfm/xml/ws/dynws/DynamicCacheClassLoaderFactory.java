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

package com.naryx.tagfusion.cfm.xml.ws.dynws;

import java.util.Enumeration;

public class DynamicCacheClassLoaderFactory {
	public static final String WEBLOGIC_PROP_PREFIX = "weblogic.";

	/**
	 * Returns a DynamicCacheClassLoader instance suitable for handling
	 * dynamically generated web service classes.
	 * 
	 * @param dir
	 *          directory for the DynamicCacheClassLoader
	 * @param cat
	 *          category type for the DynamicCacheClassLoader
	 * @return instance of a DynamicCacheClassLoader
	 */
	public static DynamicCacheClassLoader newClassLoader(String dir, int cat) {
		// Return a WebLogicDynamicCacheClassLoader if
		// we're running inside a WL instance. A normal
		// DynamicCacheClassLoader otherwise.
		if (runningInWebLogic())
			return new WebLogicDynamicCacheClassLoader(dir, cat);
		else
			return new DynamicCacheClassLoader(dir, cat);
	}

	/**
	 * Returns true if the container is BEA WebLogic, false otherwise.
	 * 
	 * @return true if the container is BEA WebLogic, false otherwise
	 */
	public static boolean runningInWebLogic() {
		Enumeration e = System.getProperties().propertyNames();
		while (e.hasMoreElements()) {
			if (e.nextElement().toString().startsWith(WEBLOGIC_PROP_PREFIX))
				return true;
		}
		return false;
	}
}
