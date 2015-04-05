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

public class listInsertAt extends functionBase {
	private static final long serialVersionUID = 1L;

	public listInsertAt() {
		min = 3;
		max = 4;
		setNamedParams( new String[]{ "list","position","value","delimiter"} );
	}

	public String[] getParamInfo(){
		return new String[]{
			"list",
			"position",
			"value",
			"delimiter - default comma (,)"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"list", 
				"Inserts a new element into the list at the given position, returning the newly created list", 
				ReturnType.STRING );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String list = getNamedStringParam( argStruct, "list" , "" );
		String delimiter = getNamedStringParam( argStruct, "delimiter" , "," );
		int position = getNamedIntParam( argStruct, "position" ,0 );
		String value = getNamedStringParam( argStruct, "value" , "" );

		if (delimiter.length() == 0) {
			throwException(_session, "Invalid delimiter specified. The delimiter value must contain at least one character.");
		}

		List<String> elements = string.split(list, delimiter);
		char newDelim = delimiter.charAt(0); // only use the first char of the
																					// delimiter in the new list

		if (position > elements.size() || position < 1) {
			throwException(_session, "Invalid list position specified " + position);
		}

		position--; // adjust position to real position in list

		StringBuilder sb = new StringBuilder();
		int index = 0;
		for (int i = 0; i <= elements.size(); i++) {
			String nextToken;
			if (i == position) {
				nextToken = value;
			} else {
				nextToken = elements.get(index);
				index++;
			}
			sb.append(nextToken);
			sb.append(newDelim);
		}

		// remove extraneous delimiter
		sb = sb.deleteCharAt(sb.length() - 1);

		return new cfStringData(sb.toString());
	}
}
