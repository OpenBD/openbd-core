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

package com.naryx.tagfusion.cfm.parser.script;

import org.antlr.runtime.Token;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.parser.CFUndefinedValue;
import com.naryx.tagfusion.cfm.parser.cfLData;

public class CFReturnStatement extends CFParsedStatement implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private CFExpression _ret; // null if no return

	// "ret" is null if there is no return value
	public CFReturnStatement(Token t, CFExpression ret) {
		super(t);
		_ret = ret;
	}

	public CFStatementResult Exec(CFContext context) throws cfmRunTimeException {
		cfData retVal;

		setLineCol(context);
		if (_ret != null) {
			retVal = _ret.Eval(context);
		} else {
			retVal = CFUndefinedValue.UNDEFINED;
		}

		// if it's a cfLData, lets evaluate it since it may not exist
		if (retVal.getDataType() == cfData.CFLDATA) {
			retVal = ((cfLData) retVal).Get(context);
		}

		return new CFStatementResult(retVal);
	}

	public String Decompile(int indent) {
		StringBuilder s = new StringBuilder();
		s.append(Indent(indent));
		s.append("return ");
		if (_ret != null) {
			s.append(_ret.Decompile(indent));
		}
		return s.toString();
	}

}
