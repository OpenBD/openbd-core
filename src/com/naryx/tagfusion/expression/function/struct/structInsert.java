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
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.CFUndefinedValue;
import com.naryx.tagfusion.expression.function.functionBase;

public class structInsert extends functionBase {
	private static final long serialVersionUID = 1L;

	public structInsert() {
		min = 3;
		max = 4;
		setNamedParams( new String[]{ "struct", "key", "value", "overwrite" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"struct1",
			"key",
			"value",
			"overwrite - default false"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"structure", 
				"Inserts the key and value into the given structure, overwriting if already exists", 
				ReturnType.BOOLEAN );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		cfData struct;
		cfData valueData;
		cfData key;
		boolean overwrite = false;

		struct = getNamedParam(argStruct, "struct");
		if (!struct.isStruct())
				throwException(_session, "Parameter isn't of type STRUCTURE");

		key = getNamedParam(argStruct, "key");
		valueData = getNamedParam(argStruct, "value");
		overwrite = getNamedBooleanParam(argStruct, "overwrite", false);

		// --[ Do the actual insertion
		// throw an exception if the key exists and overwrite is not enabled
		// except if the struct is an argument struct and where the key exists but
		// the value is undefined
		if (!overwrite && ((cfStructData) struct).containsKey(key) && !(struct instanceof cfArgStructData && ((cfArgStructData) struct).getData(key) instanceof CFUndefinedValue))
			throwException(_session, "Key already exists within the structure");
		else
			((cfStructData) struct).setData(key, valueData);

		return cfBooleanData.TRUE;
	}
}
