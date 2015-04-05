/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  
 *  $Id: JavascriptDefinedFunction.java 1747 2011-10-25 15:46:01Z alan $
 */
package org.alanwilliamson.lang.javascript;

import java.util.List;
import java.util.Map;

import org.m0zilla.javascript.Context;
import org.m0zilla.javascript.NativeFunction;
import org.m0zilla.javascript.Script;
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

public class JavascriptDefinedFunction extends userDefinedFunction {
	private static final long serialVersionUID = 1L;

	public JavascriptDefinedFunction( String _name, List<CFFunctionParameter> _formals, Script script ) {
		super( _name, (byte)0, "any", _formals, (Map)null, (CFScriptStatement)null );
	}
	
	public userDefinedFunction	duplicateAndInherit( cfComponentData _superScope ){
		/*
		 * This for CFCOMPONENT in the super-stack; however Javascript will handling the inheritance itself
		 * and we can't have a javascript function extend a CFML function or other way
		 */
		return this;
	}
	
	public cfData execute( cfSession _session, cfArgStructData _args, boolean _isLocalExec ) throws cfmRunTimeException{
		Context cx;
		try {

			cx = convert.getJavaScriptContextForSession(_session, null);
			Scriptable scope = (Scriptable)cx.getThreadLocal("scope");

			// Sort out the parameters
			Object[]	params;
			if ( _args.size() == 0 )
				params	= new Object[0];
			else{
				params	= new Object[_args.size()];
				for ( int x=0; x < params.length; x++ ){
					params[x]	= convert.cfDataConvert( _args.getData(x) );
				}
			}
				
			// Make the call out to the javascript
			Object result = NativeFunction.callMethod( cx, scope, getName(), params );
			return tagUtils.convertToCfData( result );
			
		} catch (Exception e) {
			//e.printStackTrace();
			throwException(_session, "Javascript: " + e.getMessage() );
		}
		return cfBooleanData.TRUE;
	}

	public cfData execute( cfSession _session, List<cfData> _actuals ) throws cfmRunTimeException {
		System.out.println( "JavascriptDefinedFunction.execute" );
		return cfBooleanData.TRUE;
	}
}