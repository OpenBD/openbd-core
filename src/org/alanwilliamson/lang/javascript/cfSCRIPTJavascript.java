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
 *  $Id: cfSCRIPTJavascript.java 1929 2012-01-18 11:56:19Z alan $
 */

package org.alanwilliamson.lang.javascript;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.m0zilla.javascript.Context;
import org.m0zilla.javascript.InterpretedFunction;
import org.m0zilla.javascript.InterpreterData;
import org.m0zilla.javascript.RhinoException;
import org.m0zilla.javascript.Script;
import org.m0zilla.javascript.Scriptable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.parser.script.CFFunctionParameter;
import com.naryx.tagfusion.cfm.tag.ContentTypeStaticInterface;
import com.naryx.tagfusion.cfm.tag.cfCOMPONENT;
import com.naryx.tagfusion.cfm.tag.cfSCRIPT;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfSCRIPTJavascript extends cfSCRIPT implements ContentTypeStaticInterface, Serializable {
	private static final long serialVersionUID = 1L;
	
	private Script script = null;
	private HashMap<String, JavascriptDefinedFunction>	functions	= new HashMap<String, JavascriptDefinedFunction>();
	
	protected void tagLoadingComplete() throws cfmBadFileException {
		String jsbody	= new String(this.getStaticBody());
		
		Context cx = Context.enter();
		
		try{
			cx.setLanguageVersion( Context.VERSION_1_8 );
			cx.setOptimizationLevel( -1 );
			script = cx.compileString( jsbody, "javascript@" + posLine + ":" + posColumn, 0, null );

			InterpretedFunction	i	= (InterpretedFunction)script;
			
			InterpreterData[]	functions = i.getNestedFunctions();
			if ( functions != null ){
				for ( int x=0; x < functions.length; x++){
					marshallFunction( functions[x] );
				}
			}
			
		}finally{
			Context.exit();
		}
	}
	
	
	private	void marshallFunction( InterpreterData	func ) throws cfmBadFileException{
  	List<CFFunctionParameter>	params	= new ArrayList( func.getParamCount() );

  	String name	= func.getFunctionName();
  	if ( name == null )
  		return;
  	
  	for ( int p=0; p < func.getParamCount(); p++ ){
  		CFFunctionParameter	fP	= new CFFunctionParameter(func.getParamOrVarName(p));
  		params.add( fP );
  	}

  	JavascriptDefinedFunction	javascriptDefinedFunction	= new JavascriptDefinedFunction( name, params, script );
  	functions.put( name, javascriptDefinedFunction );
  	
  	/* Register the function */
  	cfFile parentFile = this.getFile();
  	parentFile.addUDF( this, javascriptDefinedFunction );
  	if ( this.isSubordinate( "CFCOMPONENT" ) ) {
  		((cfCOMPONENT)parentTag).addFunctionMetaData( javascriptDefinedFunction.getMetaData() );
  	}
  }

	
	
  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {

  	try{
    	Context cx = convert.getJavaScriptContextForSession(_Session, functions);

    	Scriptable scope = (Scriptable)cx.getThreadLocal("scope");
    	script.exec( cx, scope );
      return cfTagReturnType.NORMAL;

  	} catch (RhinoException rE){

  		throw newRunTimeException( "JavaScript: " + rE.getMessage() );

  	} catch (Exception e){
  		
  		e.printStackTrace();
  		throw newRunTimeException( "JavaScript.Exception: " + e.getMessage() );

  	}
  }

}
