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

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class val extends functionBase {

	private static final long serialVersionUID = 1L;

	public val() {
		min = max = 1;
	}

  public String[] getParamInfo(){
		return new String[]{
			"string"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"math", 
				"Attempts to convert the string to a number", 
				ReturnType.NUMERIC );
	}
  
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		// --[ get parameters
		cfData data = parameters.get(0);

		if (data.getDataType() == cfData.CFNUMBERDATA) {
			return data;
		}

		String number = data.getString();

		if (number.length() == 0) {
			return new cfNumberData(0);
		} else {
			return new cfNumberData(getNumberFromString(number));
		}
	}

	// Returns a number that the beginning of a string can be converted to.
	// Returns 0 if conversion is not possible
	private static double getNumberFromString(String _str) {
		int index = 0;
		int startIndex = 0;
		boolean dotUsed = false;
		char firstChar;

		if (_str.length() == 0)
			return 0;

		firstChar = _str.charAt(0);
		if (firstChar == '-' || Character.isDigit(firstChar)) {
			index++;
		} else if (firstChar == '+') {
			index++;
			startIndex = 1;
		} else if (firstChar == '.') { // handles strings like '.52' (refer to bug
																		// #1902)
			index++;
			dotUsed = true;
		} else {
			return 0;
		}

		for (int i = index; i < _str.length(); i++) {
			if (_str.charAt(index) == '.') {
				if (dotUsed) {
					break;
				} else {
					dotUsed = true;
					index++;
				}
			} else if (Character.isDigit(_str.charAt(index))) {
				index++;
			} else {
				break;
			}
		}

		return com.nary.util.string.convertToDouble(_str.substring(startIndex, index), 0);
	}
}
