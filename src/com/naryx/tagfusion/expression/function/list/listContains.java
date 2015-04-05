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

/*
 * Extra BlueDragon feature added; new parameter to this function to flag if you want
 * an exact match.  CFMX returns only if the substring is inside the list!
 */

import java.util.List;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class listContains extends functionBase {
	private static final long serialVersionUID = 1L;

	public listContains() {
		min = 2;
		max = 4;
		setNamedParams( new String[]{ "list","substring","delimiter","exact"} );
	}

	public String[] getParamInfo(){
		return new String[]{
			"list",
			"substring",
			"delimiter - default comma (,)",
			"exact match flag - default false"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"list", 
				"Determines if the substring is within the list, returning the list index of where the item was found; 0 if not found", 
				ReturnType.NUMERIC );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		String list = getNamedStringParam( argStruct, "list" ,"" );
		String substring = getNamedStringParam( argStruct, "substring" ,"" );
		String delimiter = getNamedStringParam( argStruct, "delimiter" ,"," );
		boolean exactMatch = getNamedBooleanParam( argStruct, "exact" , false);

		if (substring.length() != 0) {
			List<String> valist = string.split(list, delimiter);
			for (int index = 0; index < valist.size(); index++) {
				String token = valist.get(index);
				if ((exactMatch && token.equals(substring)) || (!exactMatch && token.indexOf(substring) != -1))
					return new cfNumberData(index + 1);
			}
		}
		return new cfNumberData(0);
	}
}
