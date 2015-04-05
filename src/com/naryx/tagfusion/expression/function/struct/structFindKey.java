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
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.expression.function.functionBase;

public class structFindKey extends functionBase {
	private static final long serialVersionUID = 1L;

	public structFindKey() {
		min = 2;
		max = 3;
		setNamedParams( new String[]{ "struct", "key", "returnall" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"struct or array",
			"key",
			"returnallflag - default to false; returns all the keys found"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"structure", 
				"Returns the data (or an array of all the values) at the given key, throwing an exception if it does not exist", 
				ReturnType.ARRAY );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		boolean returnall = false; // default scope. ONE -> false, ALL -> true
		String searchKey = "";
		cfData top;

		String returnAllParam = getNamedStringParam(argStruct, "returnall", "ONE");		
		returnall = returnAllParam.equalsIgnoreCase("ALL");
		top = getNamedParam(argStruct, "struct");		
		try {
			searchKey = getNamedParam(argStruct, "key").getString();
		} catch (dataNotSupportedException e) {
			throwException(_session, "Invalid argument. The KEY must be a valid string argument.");
		}

		if (!top.isStruct()) {
			throwException(_session, "Invalid argument. This function takes a struct as the first argument.");
		}

		cfArrayData result = cfArrayData.createArray(1);
		return findKey(top, top, searchKey, "", "", result, returnall);
	}

	private cfArrayData findKey(cfData _parent, cfData _top, String _keySearch, String _path, String _key, cfArrayData _result, boolean _returnall) throws cfmRunTimeException {
		if (_top.getDataType() == cfData.CFARRAYDATA) {
			cfArrayData array = (cfArrayData) _top;
			int arraylen = array.size();
			for (int i = 1; i <= arraylen; i++) {
				String nextPathStep = "[" + i + "]";
				cfArrayData result = findKey(_top, array.getElement(i), _keySearch, _path + nextPathStep, nextPathStep, _result, _returnall);
				if (result.size() > 0 && !_returnall) {
					return result;
				}
			}

		} else if (_top.isStruct()) {
			cfStructData struct = (cfStructData) _top;
			Object[] keys = struct.keys();
			String nextKey;
			String nextKeyU;
			String pathStart = _path + ".";
			for (int i = 0; i < keys.length; i++) {
				nextKey = ((String) keys[i]);
				nextKeyU = nextKey.toUpperCase();
				if (nextKey.equalsIgnoreCase(_keySearch)) {
					cfStructData result = new cfStructData();
					result.setData("owner", _top); //
					result.setData("path", new cfStringData(pathStart + nextKeyU));
					result.setData("value", struct.getData(nextKey));
					_result.addElement(result);
					if (!_returnall) {
						return _result;
					}
				} else {
					cfArrayData result = findKey(_top, struct.getData(nextKey), _keySearch, pathStart + nextKeyU, nextKeyU, _result, _returnall);
					if (result.size() > 0 && !_returnall) {
						return result;
					}
				}

			}
		}

		return _result;
	}

}
