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

import java.util.Iterator;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class CFCall implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private CFScopeStack _scopeStack;

	public CFCall(cfStructData _args) {
		_scopeStack = new CFScopeStack();
		// The scope for local variables and parameters
		_scopeStack.push( new CFCallScope( _args ) );
	}

	public CFCall() {
		_scopeStack = new CFScopeStack();
		// The scope for local variables and parameters
		_scopeStack.push( new CFCallScope() );
	}

	// Add a new variable/member to the innermost scope?
	public void put( String name, cfData val, CFContext context )
	    throws cfmRunTimeException {
		_scopeStack.peek().put( name, val, context );
	}

	public void putAll( cfStructData val, CFContext context )
	    throws cfmRunTimeException {
		CFScope scope = _scopeStack.peek();
		Iterator<String> keys = val.keySet().iterator();
		while ( keys.hasNext() ) {
			String nextKey = keys.next();
			scope.put( nextKey, val.getData( nextKey ), context );
		}
	}

	// Add a variable to the outermost scope, for local variable declarations
	public void putLocal( String name, cfData val, CFContext context )
	    throws cfmRunTimeException {
		_scopeStack.peekFirst().put( name, val, context );
	}

	// Find a local-scope identifier
	public cfData get( String name, CFContext context )
	    throws cfmRunTimeException {
		return _scopeStack.get( name, context );
	}

	// Push a new ("with") scope
	public void push( CFScope scope ) {
		_scopeStack.push( scope );
	}

	// Pop the innermost ("with") scope
	public void pop() {
		_scopeStack.pop();
	}

	public CFScopeStack scopeStack() {
		return _scopeStack;
	}
}
