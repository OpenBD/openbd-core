/*
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: runTime.java 2486 2015-01-22 03:22:37Z alan $
 */

package com.naryx.tagfusion.cfm.parser;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.script.CFScriptStatement;
import com.naryx.tagfusion.cfm.parser.script.CFStatementResult;

/**
 * The <code>runTime</code> is a class to facilitate the interface to the expression engine.
 */

public class runTime extends Object {

	public static cfData runExpression(String variable) throws cfmRunTimeException {
		CFContext emptyContext = new EmptyCFContext();
		CFExpression expression = CFMLCache.getExpression(variable);

		cfData temp = expression.Eval(emptyContext);
		if (temp.getDataType() == cfData.CFLDATA) {
			temp = ((cfLData) temp).Get(emptyContext);
		}
		return temp;
	}

	public static cfData runExpression(cfSession Session, String variable, boolean _fullyEvaluate) throws cfmRunTimeException {
		// Some preliminary checks to stop it going through the main engine
		if (variable == null) {
			return null;
		}
		return runExpression(Session, CFMLCache.getExpression(variable), _fullyEvaluate);
	}

	public static cfData runExpression(cfSession Session, CFExpression expression, boolean _fullyEvaluate) throws cfmRunTimeException {
		if (expression == null) {
			return null;
		}

		if (_fullyEvaluate) {
			return run(Session, expression);
		} else {
			return runTime.runNonEval(Session, expression);
		}
	}

	public static cfData runExpression(cfSession Session, CFExpression expression) throws cfmRunTimeException {
		return runExpression(Session, expression, true);
	}

	private static cfData run(cfSession Session, CFExpression expression) throws cfmRunTimeException {
		cfData temp = expression.Eval(Session.getCFContext());
		if (temp.getDataType() == cfData.CFLDATA) {
			temp = ((cfLData) temp).Get(Session.getCFContext());
		}
		return temp;
	}

	public static cfData runExpression(cfSession Session, String variable) throws cfmRunTimeException {
		return runExpression(Session, variable, true);
	}

	/**
	 * This version of run() will return the cfData resulting from the evaluation of the given expression. It will not fully evaluate the cfData if it an instance of cfLData. This is used in the functions.setVariable class.
	 */

	private static cfData runNonEval(cfSession Session, CFExpression expression) throws cfmRunTimeException {
		return expression.Eval(Session.getCFContext());
	}

	// ----------------------------------------------------------------------

	// this method is only invoked from CFSCRIPT.render()
	public static CFStatementResult run(cfSession Session, CFScriptStatement statement) throws cfmRunTimeException {
		return statement.Exec(Session.getCFContext());
	}

	public static CFStatementResult run(cfSession Session, CFStatement statement) throws cfmRunTimeException {
		return statement.Exec(Session.getCFContext());
	}

}
