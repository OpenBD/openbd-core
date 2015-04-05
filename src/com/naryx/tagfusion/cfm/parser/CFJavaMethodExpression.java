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

package com.naryx.tagfusion.cfm.parser;

/**
 * This cannot be invoked/evaluated as a normal CFExpression is.
 * It simply acts as a holder of the Java method attributes 
 * (i.e. method name, and arguments being passed to it)
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.antlr.runtime.Token;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.javaMethodDataInterface;

public class CFJavaMethodExpression extends CFExpression implements javaMethodDataInterface {

	private static final long			serialVersionUID	= 1L;

	private CFExpression					name;
	private Vector<CFExpression>	args;											// Vector of CFExpression's
	private boolean								_onMissingMethod	= false;



	public CFJavaMethodExpression(Token _t, CFExpression _name, Vector<CFExpression> _args) {
		super(_t);
		name = _name;
		args = _args;
	}



	public byte getType() {
		return CFExpression.FUNCTION;
	}



	public String getFunctionName() {
		return ((CFIdentifier) name).getName();
	}



	public Vector<CFExpression> getArguments() {
		return args;
	}



	public boolean isOnMethodMissing() {
		return _onMissingMethod;
	}



	public void setOnMethodMissing() {
		_onMissingMethod = true;
	}



	public List<cfData> getEvaluatedArguments(CFContext _context, boolean cfcMethod) throws cfmRunTimeException {
		int noArgs = args.size();
		List<cfData> evaluatedArgs = new ArrayList<cfData>(noArgs);
		for (int i = 0; i < noArgs; i++) {
			evaluatedArgs.add(CFFunctionExpression.evaluateArgument(_context, args.elementAt(i), cfcMethod));
		}
		return evaluatedArgs;
	}// getArguments()



	public cfData Eval(CFContext context) throws cfmRunTimeException {
		if (args instanceof ArgumentsVector) { // named arguments for CFC method
			return prepareArguments(context, (ArgumentsVector) args);
		} else { // Java object method or CFC method with unnamed arguments
			return null;
		}
	}



	public static cfArgStructData prepareArguments(CFContext context, ArgumentsVector _args) throws cfmRunTimeException {
		cfArgStructData argStruct = new cfArgStructData(true);
		Iterator<String> keys = _args.keys().iterator();
		while (keys.hasNext()) {
			String nextKey = keys.next();

			CFExpression nextArgExpression = _args.getNamedArg(nextKey);
			cfData nextEvaluatedArg = CFFunctionExpression.evaluateArgument(context, nextArgExpression);
			// special case if args passed in as argumentCollection
			if (nextKey.equalsIgnoreCase("argumentcollection")) {
				if (nextEvaluatedArg.isStruct()) {
					cfStructData argCollection = (cfStructData) nextEvaluatedArg;
					Object[] argKeys = argCollection.keys();
					for (int i = 0; i < argKeys.length; i++) {
						String key = (String) argKeys[i];
						argStruct.setData(key, argCollection.getData(key));
					}
				} else {
					throw new CFException("argumentCollection must be a struct", context);
				}
			} else {
				argStruct.setData(nextKey, nextEvaluatedArg);
			}
		}
		return argStruct;
	}



	public String Decompile(int indent) {
		StringBuilder sb = new StringBuilder();
		sb.append(name.Decompile(indent));
		sb.append("(");

		for (int i = 0; i < args.size(); i++) {
			sb.append((args.elementAt(i)).toString());
			if (i < args.size() - 1) {
				sb.append(", ");
			}
		}

		sb.append(")");

		return sb.toString();
	}

}
