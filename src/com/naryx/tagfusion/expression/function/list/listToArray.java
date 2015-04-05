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
import com.nary.util.stringtokenizer;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class listToArray extends functionBase {
	private static final long serialVersionUID = 1L;

	public listToArray() {
		min = 1;
		max = 3;
		setNamedParams( new String[]{ "list","delimiter","flag"} );
	}


	public String[] getParamInfo(){
		return new String[]{
			"list",
			"delimiter - default comma (,)",
			"empty flag - indicates whether blank elements should be included in the list. Defaults to false"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"list", 
				"Converts the list into an array, optionally including the empty elements", 
				ReturnType.ARRAY );
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String list = getNamedStringParam( argStruct, "list" , "" );
		String delimiter = getNamedStringParam( argStruct, "delimiter" , "," );
		boolean incEmpty = getNamedBooleanParam( argStruct, "flag" ,false );
		
		cfArrayData array = cfArrayData.createArray(1);
		// if empty list elements are to be included then use our own string
		// tokenizer
		if (incEmpty) {
			stringtokenizer st = new stringtokenizer(list, delimiter);

			while (st.hasMoreTokens()) {
				array.addElement(new cfStringData(st.nextToken()));
			}

		} else {
			List<String> elements = string.split(list, delimiter);
			for (int i = 0; i < elements.size(); i++) {
				array.addElement(new cfStringData(elements.get(i)));
			}
		}

		return array;
	}
}
