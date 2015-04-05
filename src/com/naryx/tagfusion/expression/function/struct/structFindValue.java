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

public class structFindValue extends functionBase {

	private static final long serialVersionUID = 1L;

	public structFindValue() {
		min = 2;
		max = 3;
		setNamedParams( new String[]{ "struct", "value", "returnall" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"struct",
			"value",
			"return - ('ONE' default or 'ALL')"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"structure", 
				"Returns the data (or an array of all the values) that matches the value, throwing an exception if it does not exist", 
				ReturnType.ARRAY );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		boolean returnall = false; // default scope. ONE -> false, ALL -> true
		String searchVal = "";
		cfData top;

		String returnAllParam = getNamedStringParam(argStruct, "returnall", "ONE");		
		returnall = returnAllParam.equalsIgnoreCase("ALL");
		top = getNamedParam(argStruct, "struct");

		try {
			searchVal = getNamedParam(argStruct, "value").getString();
		} catch (dataNotSupportedException e) {
			throwException(_session, "Invalid argument. The VALUE must be a valid string argument.");
		}

		if (!top.isStruct()) {
			throwException(_session, "Invalid argument. This function takes a struct as the first argument.");
		}

		cfArrayData result = cfArrayData.createArray(1);
		cfData finalResult = findVal(top, top, searchVal, "", "", result, returnall);
		if (finalResult != null) {
			return finalResult;
		} else {
			return result;
		}
	}

	private cfData findVal(cfData _parent, cfData _top, String _val, String _path, String _key, cfArrayData _result, boolean _returnall) throws cfmRunTimeException {
		if (_top.getDataType() == cfData.CFARRAYDATA) {
			cfArrayData array = (cfArrayData) _top;
			int arraylen = array.size();
			for (int i = 1; i <= arraylen; i++) {
				String nextPathStep = "[" + i + "]";
				cfData result = findVal(_top, array.getElement(i), _val, _path + nextPathStep, nextPathStep, _result, _returnall);
				if (result != null && !_returnall) {
					return result;
				}
			}

			if (_result.size() > 0) {
				return _result;
			}// else let it drop thru to the default return null

		} else if (_top.isStruct()) {
			cfStructData struct = (cfStructData) _top;
			Object[] keys = struct.keys();
			String nextKey;
			String pathStart = _path + ".";
			for (int i = 0; i < keys.length; i++) {
				cfData nextVal = struct.getData((String) keys[i]);
				nextKey = ((String) keys[i]).toUpperCase();
				cfData result = findVal(_top, nextVal, _val, pathStart + nextKey, nextKey, _result, _returnall);
				if (result != null && !_returnall) {
					return result;
				}
			}

			if (_result.size() > 0) {
				return _result;
			}// else let it drop thru to the default return null

		} else {
			try {
				String thisVal = _top.getString();
				if (thisVal.equalsIgnoreCase(_val)) {
					cfStructData result = new cfStructData();
					result.setData("owner", _parent);
					result.setData("path", new cfStringData(_path));
					result.setData("key", new cfStringData(_key));
					_result.addElement(result);
					return _result;
				}
			} catch (dataNotSupportedException d) {}
		}
		return null;
	}

}
