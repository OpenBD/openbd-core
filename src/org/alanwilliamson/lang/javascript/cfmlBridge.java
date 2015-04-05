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
 *  $Id: cfmlBridge.java 1747 2011-10-25 15:46:01Z alan $
 */

package org.alanwilliamson.lang.javascript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.m0zilla.javascript.Context;
import org.m0zilla.javascript.Function;
import org.m0zilla.javascript.IdScriptableObject;
import org.m0zilla.javascript.NativeArray;
import org.m0zilla.javascript.NativeFunction;
import org.m0zilla.javascript.NativeJavaObject;
import org.m0zilla.javascript.NativeObject;
import org.m0zilla.javascript.Scriptable;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.cfm.tag.tagUtils;
import com.naryx.tagfusion.expression.compile.expressionEngine;
import com.naryx.tagfusion.expression.function.functionBase;

public class cfmlBridge extends Object {
	static final long serialVersionUID = 1;

	private cfSession thisSession;
	private HashMap<String, JavascriptDefinedFunction>	functions;

	public cfmlBridge(cfSession session, HashMap<String, JavascriptDefinedFunction>	functions) {
		thisSession = session;
		this.functions = functions;
	}
	
	
	/*
	 * Exports the named variable to the Session
	 */
	public void tocfml(String varName) throws Exception{
		if ( varName.equals("$cf") 
				|| varName.equals("cgi") 
				|| varName.equals("form") 
				|| varName.equals("url") 
				|| varName.equals("application") 
				|| varName.equals("session") 
				|| varName.equals("client") )
			return;

		Context cx = Context.getCurrentContext();
		Scriptable scope = (Scriptable)cx.getThreadLocal("scope");
		
		Object jsObj = scope.get( varName, scope );
		
		if ( !(jsObj instanceof Function) ){
			
			if ( jsObj instanceof NativeJavaObject ) {

				NativeJavaObject nativeJSObj = (NativeJavaObject)jsObj;
				saveIntoCfml( thisSession, varName, nativeJSObj.unwrap() );

			}else if ( jsObj instanceof NativeObject || jsObj instanceof NativeArray ){

				cfData	cfdata	= convert.jsConvert2cfData( (IdScriptableObject) jsObj );
				if ( cfdata != null )
					thisSession.setData( varName, cfdata );
				
			}else
				saveIntoCfml( thisSession, varName, jsObj );
			
		} else if ( jsObj instanceof NativeFunction ){
			
			NativeFunction	func	= (NativeFunction)jsObj;
			
			JavascriptDefinedFunction	javascriptDefinedFunction	= functions.get( func.getFunctionName() );
			if ( javascriptDefinedFunction != null ){
				//javascriptDefinedFunction.setScope( scope );
			}
		}
	}

  
  /*
   * Marshalls the data out from the Javascript engine into the CFML world
   */
  private void saveIntoCfml( cfSession _Session, String varName, Object value ) throws cfmRunTimeException {
  	if ( value instanceof cfData )
  		_Session.setData( varName, (cfData)value );
  	else if ( value instanceof cfQueryDataScriptableObject )
  		_Session.setData( varName, ((cfQueryDataScriptableObject)value).getCFData() );
  	else if ( value instanceof cfStructDataScriptableObject )
  		_Session.setData( varName, ((cfStructDataScriptableObject)value).getCFData() );
  	else if ( value instanceof cfArrayDataScriptableObject )
  		_Session.setData( varName, ((cfArrayDataScriptableObject)value).getCFData() );
  	else
  		_Session.setData( varName, tagUtils.convertToCfData(value) );
  }

	
	/*
	 * Loads the Javascript file referenced in the path
	 * This is a URI path
	 */
	public void load(String filePath) throws Exception{
		try {
			cfFile file = thisSession.getUriFile( filePath );

			Context cx = Context.getCurrentContext();
			ScriptFactory.getScript( filePath, file.getFileBody() ).exec( cx, (Scriptable)cx.getThreadLocal("scope") );
			
		}catch (cfmBadFileException e) {
			throw new Exception( "Failed to load:" + filePath );
		}
	}
	
	/*
	 * Get method for retreiving variables from within CFML
	 */
	public Object get(String var) throws Exception {
		cfData data = runTime.runExpression(thisSession, var);

		return convert.cfDataConvert(data);
	}

	public void print(String o) {
		thisSession.forceWrite(o);
	}

	/*
	 * The method was not found, so we attempt to look up our list of installed
	 * functions
	 */
	public Object __noSuchMethod__(String function, NativeArray nativeArray) throws Exception {
		int argCount = (nativeArray != null) ? (int) nativeArray.getLength() : 0;

		try {
			functionBase cfFunc = expressionEngine.getFunction(function.toLowerCase());

			if (cfFunc.getMin() <= argCount && cfFunc.getMax() >= argCount) {

				if (argCount == 0) {
					return convert.cfDataConvert(cfFunc.execute(thisSession, new ArrayList<cfData>()));
				} else {

					if ( cfFunc.supportNamedParams() ){

						cfArgStructData argStruct	= new cfArgStructData();
						for (int x=0; x < argCount; x++ ) {
							argStruct.setData( x+1, tagUtils.convertToCfData(nativeArray.get(x, null)) );
						}
						
						return convert.cfDataConvert(cfFunc.execute(thisSession, argStruct));
					}else{
						
						List<cfData> parameters = new ArrayList<cfData>();
						for (int x = argCount - 1; x >= 0; x--) {
							parameters.add(tagUtils.convertToCfData(nativeArray.get(x, null)));
						}
						
						return convert.cfDataConvert(cfFunc.execute(thisSession, parameters));
					}

				}

			} else {
				throw new Exception("Wrong parameters supplied (" + function + ")");
			}

		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}

	}

}