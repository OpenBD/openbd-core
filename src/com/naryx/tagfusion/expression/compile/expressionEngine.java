/* 
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
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
 *  http://openbd.org/
 */

package com.naryx.tagfusion.expression.compile;

import java.util.Iterator;
import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.registerTagsExpressions;
import com.naryx.tagfusion.expression.function.functionBase;

public class expressionEngine extends Object {

	private static Map<String, String> functions = new FastMap<String, String>(600);
	private static Map<String, functionBase> functionCache = new FastMap<String, functionBase>();

	public static void init(){
		registerTagsExpressions.registerFunctions(functions);

		Iterator<String> iter = functions.keySet().iterator();
		while (iter.hasNext()) {
			loadFunction(iter.next().toString());
		}
		
		cfEngine.log( "Core Expressions loaded: " + functions.size() );
	}

	public static void addFunction(String function, String functionclass) {
		functions.put(function.toLowerCase(), functionclass);
		functionCache.remove(function.toLowerCase());
	}

	public static Map<String, String> getFunctions() {
		return functions;
	}

	public static int getTotalFunctions() {
		return functions.size();
	}

	public static String getFunctionName(String packageFunction) {
		Iterator<String> iter = functions.keySet().iterator();
		while (iter.hasNext()) {
			String k = iter.next().toString();
			if (functions.get(k).equals(packageFunction))
				return k;
		}

		return null;
	}

	public static Object[] getFunctionKeys() {
		return functions.keySet().toArray();
	}

	public static functionBase getFunction(String _a) throws cfmRunTimeException {
		// function name in "_a" is lowercase at this point
		try {
			functionBase function = functionCache.get(_a);
			if (function != null) {
				return function;
			} else {
				return loadFunction(_a);
			}
		} catch (Exception CE) {
			// just fall through to throw exception
		}

		throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.expressionError", "function.invalidFunction", new String[] { _a }));
	}

	private static synchronized functionBase loadFunction(String _a) {
		functionBase function = functionCache.get(_a);
		if (function != null) {
			return function;
		}

		String functionClassName = functions.get(_a);
		if (functionClassName == null) {
			throw new RuntimeException("failed to find function class name - " + _a);
		}

		try {
			Class<?> functionClass = Class.forName(functionClassName);
			if (functionClass == null) {
				throw new RuntimeException("failed to load function class - " + functionClassName);
			}

			function = (functionBase) functionClass.newInstance();
			if (function == null) {
				throw new RuntimeException("failed to create function class instance - " + functionClassName);
			}
		} catch (Exception e) {
			com.nary.Debug.printStackTrace(e);
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e.toString());
		}

		functionCache.put(_a, function);
		return function;
	}

	public static boolean isFunction(String _a) {
		return functions.containsKey(_a.toLowerCase());
	}
}
