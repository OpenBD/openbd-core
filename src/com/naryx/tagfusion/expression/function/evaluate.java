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
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.runTime;

/**
 * Implements the Evaluate function
 */

public class evaluate extends functionBase {

	private static final long serialVersionUID = 1L;

	public evaluate() {
		min = 1;
		max = 32;
	}

	public java.util.Map getInfo(){
		return makeInfo(
				"dynamic", 
				"Evaluates the list of expressions, one by one.", 
				ReturnType.OBJECT );
	}
	
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		/*
		 * NOTE: Although designed to evaluate a list of string expressions, CF does
		 * support non-string expressions even though I can't see the point of doing
		 * so.
		 */

		cfData result = null;
		cfData next = null;

		for (int x = parameters.size() - 1; x >= 0; x--) {
			next = parameters.get(x);
			if (next.getDataType() == cfData.CFSTRINGDATA) {
				_session.setEscapeSingleQuotes(false);
				result = runTime.runExpression(_session, next.getString());
			} else {
				result = next;
			}
		}

		return result;
	}// execute()

}
