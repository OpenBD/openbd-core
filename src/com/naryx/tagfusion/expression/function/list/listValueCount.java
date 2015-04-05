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
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class listValueCount extends functionBase {
	private static final long serialVersionUID = 1L;

	public listValueCount() {
		min = 2;
		max = 3;
		setNamedParams( new String[]{ "list","element","delimiter"} );
	}

	public String[] getParamInfo(){
		return new String[]{
			"list",
			"element",
			"delimiter - default comma (,)"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"list", 
				"Counts the number of times the given element appears in the list", 
				ReturnType.NUMERIC );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		return execute(_session, argStruct, false);
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct, boolean _nocase) throws cfmRunTimeException {

		String list = getNamedStringParam( argStruct, "list" , "" );
		String substring = getNamedStringParam( argStruct, "element" , "" );
		String delimiter = getNamedStringParam( argStruct, "delimiter" , "," );

		if (_nocase) {
			list = list.toLowerCase();
			substring = substring.toLowerCase();
		}

		List<String> elements = string.split(list, delimiter);
		int count = 0;
		for (int i = 0; i < elements.size(); i++) {
			if (elements.get(i).equals(substring))
				count++;
		}
		return new cfNumberData(count);
	}
}
