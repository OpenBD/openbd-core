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

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class findOneOf extends functionBase {

	private static final long serialVersionUID = 1L;

	public findOneOf() {
		min = 2;
		max = 3;
		setNamedParams( new String[]{ "character", "string", "start" } );
	}

	public String[] getParamInfo() {
		return new String[] { 
				"character set", 
				"string", 
				"start index" };
	}

	public java.util.Map getInfo() {
		return makeInfo("string", 
				"Looks for the given characters in the string, starting optionally at the index position", 
				ReturnType.NUMERIC);
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String chars = getNamedStringParam(argStruct, "character", "");
		String stringToSearch = getNamedStringParam(argStruct, "string", "");
		int start = getNamedIntParam( argStruct, "start",0 );
		int numSkippedChars = 0;
		
		if (start > 0) {
			start = start - 1;
			if (start > stringToSearch.length() - 1 || start < 0)
				return new cfNumberData(0);

			numSkippedChars = start;
			stringToSearch = stringToSearch.substring(start);
		}

		int firstOccurancePos = stringToSearch.length(); // assume there is no match

		for (int i = 0; i < chars.length(); i++) {
			int pos = stringToSearch.indexOf(chars.charAt(i));
			if (pos != -1 && pos < firstOccurancePos)
				firstOccurancePos = pos;
		}

		int val = 0;

		if (firstOccurancePos > -1 && firstOccurancePos < stringToSearch.length())
			val = numSkippedChars + firstOccurancePos + 1;

		return new cfNumberData(val);
	}
}
