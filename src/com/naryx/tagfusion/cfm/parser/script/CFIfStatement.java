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

import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.CFException;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.tag.cfTag;

public class CFIfStatement extends CFParsedStatement implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	private CFExpression cond;

	private CFScriptStatement thenStatement;

	private CFScriptStatement elseStatement; // null if there is no else clause

	public CFIfStatement(org.antlr.runtime.Token _t, CFExpression _cond, CFScriptStatement _then, CFScriptStatement _else) {
		super(_t);
		cond = _cond;
		thenStatement = _then;
		elseStatement = _else;
	}

	public void setHostTag(cfTag _parentTag) {
		super.setHostTag(_parentTag);

		if (thenStatement != null)
			thenStatement.setHostTag(_parentTag);

		if (elseStatement != null)
			elseStatement.setHostTag(_parentTag);
	}

	public void checkIndirectAssignments(String[] scriptSource) {
		thenStatement.checkIndirectAssignments(scriptSource);
		if (elseStatement != null) {
			elseStatement.checkIndirectAssignments(scriptSource);
		}
	}

	public CFStatementResult Exec(CFContext context) throws cfmRunTimeException {
		setLineCol(context);
		boolean condValue = false;

		try {
			condValue = fullyEvaluate(cond, context).getBoolean();
		} catch (dataNotSupportedException e) {
			throw new CFException("Invalid condition in IF statement. The condition cannot be evaluated to true/false.", context);
		}

		CFStatementResult result = null;
		if (condValue) {
			result = thenStatement.Exec(context);
		} else if (elseStatement != null) {
			result = elseStatement.Exec(context);
		}

		return result;
	}

	public String Decompile(int indent) {
		StringBuilder s = new StringBuilder();
		s.append(Indent(indent));
		s.append("if(");
		s.append(cond.Decompile(indent));
		s.append(" ) ");
		s.append(thenStatement.Decompile(indent + 2));
		if (elseStatement != null) {
			s.append("\n");
			s.append(Indent(indent));
			s.append("else ");
			s.append(elseStatement.Decompile(indent + 2));
		}
		return s.toString();
	}

}
