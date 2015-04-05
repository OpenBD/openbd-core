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

package com.naryx.tagfusion.expression.function.struct;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.cfLData;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.expression.function.functionBase;

public class structGet extends functionBase {
	private static final long serialVersionUID = 1L;

	public structGet() {
		min = max = 1;
		setNamedParams( new String[]{ "keypath" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"key path"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"structure", 
				"Returns a new structure, or heirarchy of structures to match the given key path", 
				ReturnType.STRUCTURE );
	}
	

	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {

		String structpath = "";
		cfData structPathRaw = getNamedParam(argStruct, "keypath");
		if (structPathRaw.getDataType() != 3)
			throwException(_session, "Argument is not a valid variable pathname.");

		structpath = structPathRaw.getString();
		cfLData varref = null;
		cfData result1 = runTime.runExpression(_session, structpath, false);
		if (result1 instanceof cfLData)
			varref = (cfLData) result1;
		else
			throwException(_session, "Invalid function argument. The path provided does not evaluate as a possible struct path.");

		cfStructData theStruct = null;

		CFContext context = _session.getCFContext();
		try {
			cfData data = varref.Get(context);
			if (data != null && data.isStruct()) {
				theStruct = (cfStructData) data;
			} else {
				theStruct = new cfStructData();
			}
		} catch (cfmRunTimeException e) {
			theStruct = new cfStructData();
		}

		varref.Set(theStruct, context);
		return theStruct;
	}

}
