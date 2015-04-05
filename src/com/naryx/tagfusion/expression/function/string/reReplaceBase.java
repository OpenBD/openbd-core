/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

package com.naryx.tagfusion.expression.function.string;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public abstract class reReplaceBase extends functionBase {
	private static final long serialVersionUID = 1L;

	private boolean caseSensitiveMatch = true;

	public reReplaceBase() {
		min = 3;
		max = 4;
	}
	
  public String[] getParamInfo(){
		return new String[]{
			"string",
			"regex",
			"string to replace for found matches",
			"flag to limit to scope to just the first ONE [default] or ALL"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"string", 
				"Runs a regular expression replacement against the string", 
				ReturnType.STRING );
	}

	void setCaseSensitive(boolean _bool) {
		caseSensitiveMatch = _bool;
	}

	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		String str; // the main string
		String substr; // the string that will replace occurrences of the RE
		String regexp; // the regular expression
		boolean replaceall = false;

		int index = 0;
		if (parameters.size() == 4) {
			index++; // increment the index cos 4 parameters
			String scope = parameters.get(0).getString();
			if (scope.equalsIgnoreCase("one")) {
				replaceall = false;
			} else if (scope.equalsIgnoreCase("all")) {
				replaceall = true;
			} else {
				cfCatchData catchD = new cfCatchData();
				catchD.setType("Function");
				catchD.setMessage("REReplace - invalid parameter value");
				catchD.setDetail("Invalid SCOPE value. Must be either \"ONE\" or \"ALL\".");
				throw new cfmRunTimeException(catchD);
			}
		}

		substr = parameters.get(index).getString();
		index++;
		regexp = parameters.get(index).getString();
		index++;
		// INVARIANT - index == 2 or index == 3
		str = parameters.get(index).getString();

		return new cfStringData(doRereplace(str, regexp, substr, caseSensitiveMatch, replaceall));

	}

	protected abstract String doRereplace(String _theString, String _theRE, String _theSubstr, boolean _casesensitive, boolean _replaceAll) throws cfmRunTimeException;

}