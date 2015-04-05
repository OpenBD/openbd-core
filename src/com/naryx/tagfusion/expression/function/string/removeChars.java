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

package com.naryx.tagfusion.expression.function.string;


import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class removeChars extends functionBase {

	private static final long serialVersionUID = 1L;

	public removeChars() {
		min = max = 3;
		setNamedParams( new String[]{ "string","start","count"} );
	}

	public String[] getParamInfo(){
		return new String[]{
			"string",
			"start position",
			"count"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"string", 
				"Removes the substring from the given string, at the position for the characters count", 
				ReturnType.STRING );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String str = getNamedStringParam(argStruct,"string","");
		int start = getNamedIntParam(argStruct,"start",0);
		int count = getNamedIntParam(argStruct,"count",0);

		start -= 1;

		if (start > str.length() - 1 || start < 0)
			throwException(_session, "invalid start position");

		if (count < 0)
			throwException(_session, "invalid count. The value given must be a positive integer.");

		if ((start + count) > str.length()) {
			return new cfStringData(str.substring(0, start));
		} else {
			return new cfStringData(str.substring(0, start) + str.substring(start + count));
		}
	}
}
