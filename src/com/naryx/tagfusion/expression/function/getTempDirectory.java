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

package com.naryx.tagfusion.expression.function;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class getTempDirectory extends functionBase {

	private static final long serialVersionUID = 1L;

	public getTempDirectory() {}

	public java.util.Map getInfo(){
		return makeInfo(
				"file", 
				"Returns the full path to the currently assigned temporary directory", 
				ReturnType.STRING );
	}
	
	
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		try {
			String tempDir = cfEngine.thisPlatform.getFileIO().getTempDirectory().toString();
			String fileSeparator = System.getProperty("file.separator");

			// If the temporary directory starts with a ./ or .\ then it is relative
			// to the current directory so we need to calculate the absolute path.
			// NOTE: this code was added to fix bug #1321.
			if (tempDir.startsWith("./") || tempDir.startsWith(".\\")) {
				String currentDir = System.getProperty("user.dir");
				if (currentDir != null) {
					// Append the temporary directory to the current directory
					if (currentDir.endsWith("/") || currentDir.endsWith("\\"))
						tempDir = currentDir + tempDir.substring(2);
					else
						tempDir = currentDir + tempDir.substring(1);

					// Convert all file separators to the file separator for this OS
					if (fileSeparator.equals("/"))
						tempDir = tempDir.replace('\\', '/');
					else
						tempDir = tempDir.replace('/', '\\');
				}
			}

			if (!tempDir.endsWith(fileSeparator)) {
				tempDir = tempDir + fileSeparator;
			}
			return new cfStringData(tempDir);
		} catch (Exception E) {
			throwException(_session, "An error occurred with the XML configuration");
		}
		return null;
	}
}
