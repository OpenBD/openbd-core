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

import com.nary.util.NumberUtils;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

public class dollarFormat extends functionBase {

	private static final long serialVersionUID = 1L;

	public dollarFormat() {
		min = max = 1;
	}

	public String[] getParamInfo(){
		return new String[]{
			"number"
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"format", 
				"Returns a formatted string of the number with 2 decimal points and thousand markers $000,000.00", 
				ReturnType.STRING );
	}
	
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		cfData param = parameters.get(0);
		if (param == null || param.toString() == null)
			throwException(_session, "The " + getName() + " function must be passed 1 parameter");

		double val = 0;
		try {
			val = param.getDouble();
		} catch (dataNotSupportedException e) {
			String s = param.getString();
			if (s.trim().length() > 0)
				throwException(_session, "Parameter 1 of function " + getName() + " must be a number. \"" + param.getString() + "\" is not a number.");
		}

		boolean isNegative = val < 0;

		if (isNegative)
			val = -1 * val;

		String formatedVal = NumberUtils.decimalFormat(val);
		String result;

		if (isNegative)
			result = "($" + formatedVal + ')';
		else
			result = '$' + formatedVal;

		return new cfStringData(result);
	}
}
