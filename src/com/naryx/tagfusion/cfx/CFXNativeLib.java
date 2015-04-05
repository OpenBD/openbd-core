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

package com.naryx.tagfusion.cfx;

import java.io.IOException;

import com.naryx.tagfusion.cfm.engine.cfEngine;

public class CFXNativeLib {
	static {
		// Select platform-specific library; filenames must be used to distinguish
		// Unix versions in order to support functionality in J2EE version.
		String nativeLibName = null;
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Windows"))
			nativeLibName = "CFXNativeLib.dll";
		else if (osName.startsWith("Linux"))
			nativeLibName = "CFXNativeLib-linux.so";
		else if (osName.startsWith("SunOS"))
			nativeLibName = "CFXNativeLib-solaris.so";
		else if (osName.startsWith("Mac"))
			nativeLibName = "CFXNativeLib-macosx.so";

		if (nativeLibName != null) {

			String nativeLibPath = cfEngine.getNativeLibDirectory();

			try {

				if (nativeLibPath != null) {
					nativeLibPath = nativeLibPath + nativeLibName;
					System.load(com.nary.io.FileUtils.resolveNativeLibPath(nativeLibPath));
					cfEngine.log("-] Loaded CFXNativeLib >> [" + nativeLibPath + "]");
				}

			} catch (IOException ioe) {

				cfEngine.log("-] Failed to resolve CFXNativeLib path [" + nativeLibPath + "] C++ CFXs disabled");
				cfEngine.log("-] " + ioe.getMessage());

			} catch (UnsatisfiedLinkError ule) {

				cfEngine.log("-] Failed to load CFXNativeLib [" + nativeLibPath + "] C++ CFXs disabled");
				cfEngine.log("-] " + ule.getMessage());
			}

		} else {
			cfEngine.log("-] Unknown operating system - C++ CFXs disabled");
		}
	}

	public native static void processRequest(String moduleName, String functionName, sessionRequest sRequest, sessionResponse sResponse, boolean keepLibraryLoaded) throws Exception;
}
