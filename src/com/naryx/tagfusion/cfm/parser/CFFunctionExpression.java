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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfJavaArrayData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.script.CFFunctionParameter;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.expression.compile.expressionEngine;
import com.naryx.tagfusion.expression.function.functionBase;

public class CFFunctionExpression extends CFExpression {
	private static final long serialVersionUID = 1L;

	private String name;
	private Vector<CFExpression> args; // Vector of CFExpression's
	private boolean isUDF = true;
	private boolean isParamExists;

	public CFFunctionExpression(CFIdentifier _name, Vector<CFExpression> _args) throws ParseException {
		super(_name.getToken());
		name 					= _name.getName().toLowerCase();
		args 					= _args;
		isParamExists = name.equals("parameterexists");
		isUDF 				= !expressionEngine.isFunction(name);

		// if it's a predefined function, check the number of params is legal
		if (!isUDF && !(args instanceof ArgumentsVector) ) {
			
			try {
				com.naryx.tagfusion.expression.function.functionBase f = com.naryx.tagfusion.expression.compile.expressionEngine.getFunction(name);
				if (_args.size() < f.getMin())
					throw new ParseException(_name.getToken(), "The function " + name + " requires at least " + f.getMin() + " argument(s).");
				if (_args.size() > f.getMax())
					throw new ParseException(_name.getToken(), "The function " + name + " requires at most " + f.getMax() + " argument(s).");

			} catch (com.naryx.tagfusion.cfm.engine.cfmRunTimeException ee) {} // shouldn't happen
			
		}
	}	
	
	public byte getType() {
		return CFExpression.FUNCTION;
	}

	public String getFunctionName() {
		return name;
	}

	public boolean isUDF() {
		return isUDF;
	}
	
	public boolean isEscapeSingleQuotes() {
		if ( isUDF ){
			return false;
		}else{
			functionBase function;
			try {
				function = (functionBase) expressionEngine.getFunction( getFunctionName() );
				return function.isEscapeSingleQuotes();
			} catch ( cfmRunTimeException e ) { // shouldn't occur since function name should definitely exist at this stage
				com.nary.Debug.println( "Unexpected exception in cfTag.isEscapeSingleQuotes: " + e.getMessage() );
				return true;
			}
		}
	}

	public cfData Eval(CFContext context) throws cfmRunTimeException {

		setLineCol(context);
		// definitely an identifier - cf
		functionBase function = null;

		// is it a UDF?
		if (isUDF) {
			function = context.getUDF(name);
		} else {
			// is it a predefined function
			function = expressionEngine.getFunction(name);
		}

		if (function == null) {
			// Throw as type expression to match CFMX
			CFException exc = new CFException("No such function exists - " + name + ".", context);
			exc.getCatchData().setType(cfCatchData.TYPE_EXPRESSION);
			throw exc;
		}

		// evaluate all the arguments - have to send them in reverse order for the predefined functions
		List<cfData> argVals;
		if (isUDF) {
			return context._lastExpr = ((userDefinedFunction) function).execute(context.getSession(), processArguments(context, (userDefinedFunction) function), true);
		} else if (isParamExists) {
			argVals = new ArrayList<cfData>( args.size() );
			
			for (int i = (args.size() - 1); i >= 0; i--) {
				cfData arg = args.get(i).Eval(context);
				if (arg.getDataType() != cfData.CFLDATA) {
					throw new CFException("Invalid argument to ParameterExists().", context);
				}
				argVals.add(arg);
			}
		} else {

			// If this function supports named parameters we can 
			if ( function.supportNamedParams() ) {
				setLineCol(context);
				return context._lastExpr = function.execute(context.getSession(), processArguments(context, function) );
			} else {
				argVals = new ArrayList<cfData>(args.size());
				
				for (int i = (args.size() - 1); i >= 0; i--) {
					cfData arg = args.get(i).Eval(context);
					if (arg instanceof indirectReferenceData) {
						arg = ((indirectReferenceData) arg).Get(context);
					} else if (arg.getDataType() == cfData.CFLDATA) {
						arg = ((cfLData) arg).Get(context);
					}
					
					// copy by value if it's a simple type and not the first argument
					if (((i > 0) && cfData.isSimpleValue(arg)) || arg.isLoopIndex()) {
						arg = arg.duplicate();
					}
					argVals.add(arg);
				}
			}
		}

		setLineCol(context);
		return context._lastExpr = function.execute(context.getSession(), argVals);
	}

	
	/**
	 * For transforming named arguments into
	 * 
	 * @param _context
	 * @param _function
	 * @return
	 * @throws cfmRunTimeException
	 */
	private cfArgStructData processArguments(CFContext _context, functionBase _function) throws cfmRunTimeException {
		cfArgStructData argStruct = new cfArgStructData();

		if (args instanceof ArgumentsVector) { // if named arguments
			argStruct.setNamedBasedMode();
			
			ArgumentsVector argsVtr = (ArgumentsVector) args;
			Iterator<String> keys = argsVtr.keys().iterator();
			String nextKey;
			CFExpression nextArgExpression;
			
			while (keys.hasNext()) {
				nextKey = keys.next();
				nextArgExpression = argsVtr.getNamedArg(nextKey);
				cfData nextArgVal = evaluateArgument(_context, nextArgExpression, isUDF );
				
				if (nextKey.equalsIgnoreCase("argumentcollection")) {
					if (nextArgVal.isStruct()) {
						cfStructData argCollection = (cfStructData) nextArgVal;
						Object[] argKeys = argCollection.keys();
						for (int i = 0; i < argKeys.length; i++) {
							String key = (String) argKeys[i];
							cfData nextArg = argCollection.getData(key);
							if ( nextArg.isLoopIndex() ){
								nextArg = nextArg.duplicate();
							}
							argStruct.setData(key, nextArg);
						}
					} else {
						throw new CFException("argumentCollection must be a struct", _context);
					}
				} else {
					argStruct.setData(nextKey, nextArgVal);
				}
			}
			
			
			//Make sure we have the right params
			if ( !isUDF ){
				List<String> formalParams = _function.getFormals();
				StringBuilder sb = new StringBuilder(32);
				sb.append("Function: ");
				sb.append( name );
				sb.append("(");
				for ( int x=0; x < formalParams.size(); x++ )
					sb.append( formalParams.get(x) + ", " );
				
				if ( sb.length() > 0 ){
					sb.deleteCharAt( sb.length()-1 );
					sb.deleteCharAt( sb.length()-1 );
				}
				
				sb.append( " ); Min=" + _function.getMin() );
				sb.append( "; Max=" + _function.getMax() );
				sb.append( "; Wrong number of arguments" );
				
				if (argStruct.size() < _function.getMin() || argStruct.size() > _function.getMax()){
					throw new CFException( sb.toString(), _context );
				}
			}

		} else {

			// Create the arguments array - this is empty if there are fewer actuals than formals
			int nargs = args.size();
			List<cfStructData> formals = _function.getFormalArguments();
			int nfargs; // no of formal args

			if (formals != null) {
				nfargs = formals.size();
				
				// Instantiate the formals
				for (int i = 0; i < nfargs; i++) {
					cfData arg = CFUndefinedValue.UNDEFINED;
					if (i < args.size()) {
						arg = evaluateArgument(_context, args.elementAt(i), isUDF );
						
						// note index increment is down to cfArrayData api that indexes from 1 as opposed to 0.
						String argName = formals.get(i).getData("name").getString();
						argStruct.setData(argName, arg);
					}
				}

			} else if ( _function instanceof userDefinedFunction ){
				List<CFFunctionParameter> formalList = ( (userDefinedFunction) _function ).getUDFFormals();
				nfargs = formalList.size();
				
				// Instantiate the formals
				for (int i = 0; i < nfargs; i++) {
					cfData arg;
					if (i < args.size()) {
						arg = evaluateArgument(_context, args.get(i));
					} else if ( formalList.get(i).isDefaulted() ){
						arg = formalList.get(i).getDefaultValue(_context);
					} else {
						arg = CFUndefinedValue.UNDEFINED;
					}
					argStruct.setData(formalList.get(i).getName(), arg);
				}
				
			} else {
				List<String> formalList = _function.getFormals();
				nfargs = formalList.size();
				
				// Instantiate the formals
				for (int i = 0; i < nfargs; i++) {
					cfData arg;
					if (i < args.size()) {
						arg = evaluateArgument(_context, args.get(i), isUDF );
					} else {
						arg = CFUndefinedValue.UNDEFINED;
					}
					argStruct.setData(formalList.get(i), arg);
				}
			}

			
			// Put the remainder into "arguments"
			for (int i = nfargs; i < nargs; i = i + 1) {
				// note index increment is down to cfArrayData api that indexes from 1 as opposed to 0.
				argStruct.setData(String.valueOf(i + 1), evaluateArgument(_context, args.elementAt(i), isUDF ) );
			}
		}

		return argStruct;
	}

	// default (package) scope intentionally
	public static cfData evaluateArgument(CFContext _context, CFExpression _argExp) throws cfmRunTimeException {
		return evaluateArgument(_context, _argExp, true);
	}

	public static cfData evaluateArgument(CFContext _context, CFExpression _argExp, boolean cfcMethod) throws cfmRunTimeException {
		cfData arg = _argExp.Eval(_context);

		if (arg.getDataType() == cfData.CFLDATA) {
			arg = ((cfLData) arg).Get(_context);
			if (cfcMethod) {
				// if basic type or array then copy - queries and structs are passed by reference
				if (cfData.isSimpleValue(arg)) {
					arg = arg.duplicate();
				} else if (arg.getDataType() == cfData.CFARRAYDATA && !(arg instanceof cfJavaArrayData) && !cfEngine.isStrictPassArrayByReference() ) {
					arg = ((cfArrayData) arg).copy();
				}
			}else if ( arg.getDataType() == cfData.CFNUMBERDATA || arg.getDataType() == cfData.CFSTRINGDATA ){
				arg.invalidateLoopIndex();
				if ( arg.isLoopIndex() ){
					arg = arg.duplicate();
				}
			}
		}
		if (cfcMethod && (arg.getDataType() == cfData.CFNULLDATA) && ((cfNullData) arg).isJavaNull()) {
			arg = CFUndefinedValue.UNDEFINED;
		}
		
		return arg;
	}

	
	public String Decompile(int indent) {
		String s = name + "(";

		for (int i = 0; i < args.size(); i++) {
			s += args.elementAt(i).Decompile(indent);
			if (i < args.size() - 1) {
				s += ", ";
			}
		}

		s += ")";

		return s;
	}

}
