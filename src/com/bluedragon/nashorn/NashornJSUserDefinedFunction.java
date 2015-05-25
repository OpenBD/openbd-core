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
package com.bluedragon.nashorn;

import java.util.List;
import java.util.Map;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.m0zilla.javascript.Context;
import org.m0zilla.javascript.NativeFunction;
import org.m0zilla.javascript.Scriptable;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.script.CFFunctionParameter;
import com.naryx.tagfusion.cfm.parser.script.CFScriptStatement;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.cfm.tag.tagUtils;

public class NashornJSUserDefinedFunction extends userDefinedFunction {
	private static final long serialVersionUID = 1L;
	
	private ScriptObjectMirror script;

	public NashornJSUserDefinedFunction( String _name, List<CFFunctionParameter> _formals, ScriptObjectMirror script ) {
		super( _name, (byte)0, "any", _formals, (Map<String,String>)null, (CFScriptStatement)null );
		this.script = script;
	}
	
	public userDefinedFunction	duplicateAndInherit( cfComponentData _superScope ){
		/*
		 * This for CFCOMPONENT in the super-stack; however Javascript will handling the inheritance itself
		 * and we can't have a javascript function extend a CFML function or other way
		 */
		return this;
	}
	
	public cfData execute( cfSession _session, cfArgStructData _args, boolean _isLocalExec ) throws cfmRunTimeException{
		
		System.out.println( "executeNashornJSFunction" );
		
		return cfBooleanData.TRUE;
	}

	public cfData execute( cfSession _session, List<cfData> _actuals ) throws cfmRunTimeException {
		System.out.println( "NashornJSUserDefinedFunction.execute" );
		
		script.call( null );
		
		return cfBooleanData.TRUE;
	}
}