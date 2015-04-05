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

package com.naryx.tagfusion.expression.function.list;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class listRest extends functionBase {
	private static final long serialVersionUID = 1L;

	public listRest() {
		min = 1;
		max = 2;
		setNamedParams( new String[]{ "list","delimiter"} );
	}

	public String[] getParamInfo(){
		return new String[]{
			"list",
			"delimiter - default comma (,)"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"list", 
				"Returns all the elements in the list, minus the first element", 
				ReturnType.STRING );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String list = getNamedStringParam( argStruct, "list" ,"" );
		String delimiter = getNamedStringParam( argStruct, "delimiter" ,"," );

		char[] delims = delimiter.toCharArray();
		int listLen = list.length();
		boolean foundNonDelim = false; // if a list starts with delims these are
																		// ignored

		for (int i = 0; i < listLen; i++) {
			char nextCh = list.charAt(i);
			boolean isDelim = isDelim(nextCh, delims);
			if (foundNonDelim && isDelim) { // found the first delim that isn't at the
																			// start of the list
				// now find the end of the delimiter so we know where to start when
				// creating the sublist
				i++;
				while (i < listLen && isDelim((nextCh = list.charAt(i)), delims)) {
					i++;
				}
				return new cfStringData(list.substring(i));
			} else if (!isDelim) {
				foundNonDelim = true;
			}
		}

		return new cfStringData("");

	}

	private static boolean isDelim(char _ch, char[] delims) {
		for (int j = 0; j < delims.length; j++) {
			if (_ch == delims[j]) {
				return true;
			}
		}
		return false;
	}
}
