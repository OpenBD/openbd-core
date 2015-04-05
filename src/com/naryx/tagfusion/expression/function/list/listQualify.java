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

import java.util.List;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class listQualify extends functionBase {
	private static final long serialVersionUID = 1L;

	public listQualify() {
		min = 2;
		max = 4;
		setNamedParams( new String[]{ "list","qualifier","delimiter","type"} );
	}

	public String[] getParamInfo(){
		return new String[]{
			"list",
			"qualifier",
			"delimiter - default comma (,)",
			"element type - ('ALL' default or 'CHAR')"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"list", 
				"Returns the list, of only that match the type, prefixed and postfixed with the qualifier", 
				ReturnType.STRING );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String list = getNamedStringParam( argStruct, "list" , "" );
		String qualifier = getNamedStringParam( argStruct, "qualifier" , "" );
		String delimiter = getNamedStringParam( argStruct, "delimiter" , "," );
		boolean isAll = getNamedStringParam( argStruct, "type" , "ALL" ).equals("ALL"); // represents the "ALL" or "CHAR" option. Default is
													// ALL

		if (delimiter.length() == 0) {
			throwException(_session, "Invalid delimiter specified. The delimiter value must contain at least one character.");
		}

		List<String> elements = string.split(list, delimiter);
		char newDelim = delimiter.charAt(0); // only use the first char of the
																					// delimiter in the new list

		String result;
		if (elements.size() == 0) {
			result = "";
		} else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < elements.size(); i++) {
				String nextToken = elements.get(i);
				if (isAll || !isNumber(nextToken)) {
					sb.append(qualifier);
					sb.append(nextToken);
					sb.append(qualifier);
					sb.append(newDelim);
				} else {
					sb.append(nextToken);
					sb.append(newDelim);
				}
			}
			// remove extraneous delimiter
			sb = sb.deleteCharAt(sb.length() - 1);
			result = sb.toString();
		}

		_session.setEscapeSingleQuotes(false);
		return new cfStringData(result);
	}

	private static boolean isNumber(String _token) {
		for (int x = 0; x < _token.length(); x++) {
			if (!com.nary.util.string.isANumber(_token.charAt(x)))
				return false;
		}
		return true;
	}
}
