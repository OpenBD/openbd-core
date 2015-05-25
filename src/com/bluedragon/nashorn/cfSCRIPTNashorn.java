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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.parser.script.CFFunctionParameter;
import com.naryx.tagfusion.cfm.tag.ContentTypeStaticInterface;
import com.naryx.tagfusion.cfm.tag.cfCOMPONENT;
import com.naryx.tagfusion.cfm.tag.cfSCRIPT;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfSCRIPTNashorn extends cfSCRIPT implements ContentTypeStaticInterface, Serializable {
	private static final long serialVersionUID = 1L;
	
	private CompiledScript compiledScript = null;
	private Bindings bindings = null;
	
	private HashMap<String, NashornJSUserDefinedFunction>	functions	= new HashMap<>();
	
	protected void tagLoadingComplete() throws cfmBadFileException {
		ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
    try {
      compiledScript = ((Compilable) nashorn).compile(new String(this.getStaticBody()));
   
      
      
      bindings = nashorn.getBindings( ScriptContext.ENGINE_SCOPE );

    } catch (ScriptException e) {
      throw new RuntimeException("Unable to compile script", e);
    }
	}

	
	
  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {

  	try {
			Object o = compiledScript.eval( bindings );
			
			Iterator<Entry<String,Object>> it = bindings.entrySet().iterator();
			while ( it.hasNext() ){
				Entry<String,Object> entry = it.next();
				
				if ( entry.getValue() instanceof ScriptObjectMirror ){
					ScriptObjectMirror som = (ScriptObjectMirror)entry.getValue();
					if ( som.isFunction() && !functions.containsKey( entry.getKey() ) )
						createFunctionWrapper( entry.getKey(), som );
				}
			}
			
			
		} catch ( ScriptException e ) {
			e.printStackTrace();
		}
  	
  	return cfTagReturnType.NORMAL;
  }

	private void createFunctionWrapper( String functionName, ScriptObjectMirror som ) throws cfmBadFileException {
		NashornJSUserDefinedFunction	udf	= new NashornJSUserDefinedFunction( functionName, new ArrayList<CFFunctionParameter>(), som );
  	functions.put( functionName, udf );
  	
  	cfFile parentFile = this.getFile();
  	parentFile.addUDF( this, udf );
  	if ( this.isSubordinate( "CFCOMPONENT" ) ) {
  		((cfCOMPONENT)parentTag).addFunctionMetaData( udf.getMetaData() );
  	}
	}

}
