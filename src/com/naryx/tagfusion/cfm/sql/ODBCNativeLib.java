/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: ODBCNativeLib.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.sql;

import java.io.IOException;

import com.naryx.tagfusion.cfm.engine.cfEngine;

public class ODBCNativeLib {
	public static final String URL_PREFIX = "jdbc:odbc:";

	public static final String DRIVER_CLASS = "sun.jdbc.odbc.JdbcOdbcDriver";

	static {
		// this has only been tested on Windows so far
		String nativeLibName = (cfEngine.WINDOWS ? "ODBCNativeLib.dll" : "ODBCNativeLib.so");
		String nativeLibPath = cfEngine.getNativeLibDirectory();

		try {

			if (nativeLibPath != null) {
				nativeLibPath = nativeLibPath + nativeLibName;
				System.load(com.nary.io.FileUtils.resolveNativeLibPath(nativeLibPath));
				cfEngine.log("ODBCNativeLib: Loaded [" + nativeLibPath + "]");
			}

		} catch (IOException ioe) {

			cfEngine.log("ODBCNativeLib: Failed to resolve path [" + nativeLibPath + "] ODBC auto-configuration disabled");
			cfEngine.log(" " + ioe.getMessage());

		} catch (UnsatisfiedLinkError ule) {

			cfEngine.log("ODBCNativeLib Failed to load [" + nativeLibPath + "] ODBC auto-configuration disabled");
			cfEngine.log(" " + ule.getMessage());

		}
	}

	public native static String[] getOdbcDataSources() throws Exception;
}
