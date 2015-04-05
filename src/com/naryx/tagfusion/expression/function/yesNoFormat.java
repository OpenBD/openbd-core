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
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class yesNoFormat extends functionBase {

	private static final long serialVersionUID = 1L;

	public yesNoFormat() {
		min = max = 1;
	}

  public String[] getParamInfo(){
		return new String[]{
			"string"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"format", 
				"Formats the boolean of the string to be returned as 'Yes' or 'No'", 
				ReturnType.STRING );
	}
  
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		cfData data = parameters.get(0);

		// returns "Yes" in all cases where the data cannot be evaluated to false.
		if (data.getDataType() == cfData.CFSTRINGDATA && data.getString().length() == 0) {
			return new cfStringData("No");
		} else
			return new cfStringData(data.getBoolean() ? "Yes" : "No");
	}
}
