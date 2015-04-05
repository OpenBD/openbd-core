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
 *  http://openbd.org/
 *  $Id: cfLData.java 2374 2013-06-10 22:14:24Z alan $
 */
package com.naryx.tagfusion.cfm.parser;

/**
 * Provides Get() and Set() methods representing variables
 * held within a CFScope.
 */
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfLData extends cfData implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	protected boolean exists;
	protected cfData data;

	private CFScope scope;
	private boolean isGlobalScope;
	private String name;
	private boolean doQuerySearch;

	cfLData(cfSession _session) {
	}

	public cfLData(CFScope _scope, String _name, boolean _exists, boolean _d) {
		scope = _scope;
		isGlobalScope = (scope instanceof CFGlobalScope);
		name = _name;
		exists = _exists;
		doQuerySearch = _d;
		data = null;
	}

	// use this constructor when the variable exists
	public cfLData(CFScope _scope, String _name, cfData _data, boolean _d) {
		this(_scope, _name, true, _d);
		data = _data;
	}

	public byte getDataType() {
		return cfData.CFLDATA;
	}

	public boolean isGlobalScope() {
		return isGlobalScope;
	}

	public String getName() {
		return name;
	}

	public cfQueryResultData getQueryResult() {
		return null;
	}

	public cfData Get(CFContext context) throws cfmRunTimeException {
		if (!exists) 
			throw new CFException("Variable [" + name.toUpperCase() + "] does not exist", context);
		
		if (data == null) 
			data = scope.getVal(name, context, doQuerySearch);
		
		if (data == CFUndefinedValue.UNDEFINED)
			throw new CFException("Variable [" + name.toUpperCase() + "] is not defined", context);
		
		return data;
	}

	public void Set(cfData val, CFContext context) throws cfmRunTimeException {

		if (isGlobalScope) {

			/**
			 * By default if a variable is declared within a function it is added to the "variables" scope of the CFC.
			 * This flag turns off this feature, and instead pushes it to the local scope of the function, mirroring
			 * what other programming languages offer
			 */
			if (cfEngine.isFunctionScopedVariables()) {

				if (context.isCallEmpty())
					((CFGlobalScope) scope).putGlobal(name, val);
				else
					context.getLocal(name).Set(val, context);

			} else
				((CFGlobalScope) scope).putGlobal(name, val);

		} else
			scope.put(name, val, context);
		
		exists = true;
		
		if (data != null)
			data.invalidateLoopIndex(); // invalidate cfLoopIndex
		
		data = val;
	}

	public void Delete(CFContext context) throws cfmRunTimeException {
		scope.remove(name, context);
		exists = false;
		data = null;
	}

	// returns whether or not the variable really exists
	public boolean exists() {
		return exists;
	}

}