/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: CFContext.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.parser;

/**
 * Execution context for evaluating cfscript statements and expressions.
 *   The execution context comprises the global scope list, the local scope list,
 *   the current function hierarchy, and the current source _line and _col.
 */

import java.util.Map;
import java.util.Stack;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;

public class CFContext extends cfData implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private CFGlobalScopeInterface _globalScope;
	private CFCallStack _callStack;

	private Stack<CFCallStack> callStackStack;

	private int _line;
	private int _col;

	private cfSession cfsession;

	// This is set by every expression evaluation routine - we need it for the
	// "eval()" builtin
	public cfData _lastExpr;

	public CFContext(CFGlobalScopeInterface globalScope,
	    com.naryx.tagfusion.cfm.engine.cfSession _session) {
		_globalScope = globalScope;
		_callStack = new CFCallStack();
		callStackStack = new Stack<CFCallStack>();

		_line = 0;
		_col = 0;

		_lastExpr = CFUndefinedValue.UNDEFINED;
		cfsession = _session;
	}

	public CFCallStack getCallStack() {
		return _callStack;
	}

	public Stack<CFCallStack> getCallStackStack() {
		return callStackStack;
	}

	public cfSession getSession() {
		return cfsession;
	}

	public void setSession( cfSession _session ) {
		cfsession = _session;
	}

	public void enterCustomTag() {
		callStackStack.push( _callStack );
		_callStack = new CFCallStack();
	}

	public void leaveCustomTag() {
		_callStack = callStackStack.pop();
	}

	/** UDF specific functions **/

	public boolean containsFunction( String _function ) {
		return ( getUDF( _function ) != null );
	}

	public userDefinedFunction getUDF( String _function ) {
		try {
			cfData udf = null;

			if ( !_callStack.isEmpty() ) {
				if ( ( (CFCallScope) _callStack.localScope().peek() ).containsVar( _function ) ) {
					udf = _callStack.localScope().get( _function, this );
					udf = ( (cfLData) udf ).Get( this );
				}
			}

			if ( udf == null ) {
				udf = cfsession.getData( _function );
			}

			if ( ( udf != null ) && ( udf instanceof userDefinedFunction ) )
				return (userDefinedFunction) udf;
		} catch ( Exception e ) {
			/*
			 * shouldn't fail, provided udf is added to the session by cfScript and
			 * _function exists in udf
			 */
		}
		return null;
	}

	public void putUDF( String _functionName, userDefinedFunction _function ) {
		try {
			cfsession.setData( _functionName, _function ); // put function into variables scope
		} catch ( Exception e ) { /* shouldn't fail */
		}
	}

	/** getting variables **/

	public cfData get( String name ) throws cfmRunTimeException {
		return get( name, true );
	}

	// Return an lval for the given identifier
	public cfData get( String name, boolean _doquerysearch ) throws cfmRunTimeException {

		cfData val = null;

		// try the local scope
		if ( !_callStack.isEmpty() ) {
			if ( _callStack.localScope().peek().containsVar( name ) ) {
				return _callStack.localScope().get( name, this );
			}
		}

		// then try the global scope
		if ( val == null )
			val = _globalScope.get( name, true, this, _doquerysearch );
	

		return val;
	}

	// Return an lval for a local variable regardless of existence (used for "var"
	// declarations)
	// If there is no local scope then this is done in the outermost global scope.
	public cfLData getLocal( String name ) {
		cfLData val = null;

		// try the local scope
		try {
			if ( !_callStack.isEmpty() )
				val = _callStack.localScope().peekFirst().get( name, true, this );
		} catch ( cfmRunTimeException e ) { /* this should never occur */
		}

		return val;
	}

	// for use by CFDUMP
	public Map<String, cfData> getLocalScope() {
		if ( !_callStack.isEmpty() ) {
			return _callStack.localScope().peekFirst().getScopeMap();
		}
		return null;
	}

	public boolean containsLocal( String name ) {
		// try the local scope
		if ( !_callStack.isEmpty() ) {
			return _callStack.localScope().peekFirst().containsVar( name );
		}

		return false;
	}

	public void pushCall( CFCall call ) {
		_callStack.push( call );
	}

	public void popCall() {
		if ( !_callStack.empty() )
			_callStack.pop();
	} 

	public boolean isCallEmpty() {
		return _callStack.isEmpty();
	}

	public void setLineCol( int line, int col ) {
		_line = line;
		_col = col;
	}

	public int getLine() {
		return _line;
	}

	public int getCol() {
		return _col;
	}

	public String toString() {
		return ""; // might want to put scope print out here
	}

	public void dump( cfSession Session ) {
		Session.write( "&nbsp;" );
	}
}