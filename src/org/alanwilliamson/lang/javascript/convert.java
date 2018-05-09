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
 *  $Id: convert.java 2374 2013-06-10 22:14:24Z alan $
 *  
 *  dump() added by Marcus Fernstrom on May 9, 2018.
 */
package org.alanwilliamson.lang.javascript;


import java.util.HashMap;

import org.m0zilla.javascript.Context;
import org.m0zilla.javascript.Function;
import org.m0zilla.javascript.FunctionObject;
import org.m0zilla.javascript.IdScriptableObject;
import org.m0zilla.javascript.NativeArray;
import org.m0zilla.javascript.NativeObject;
import org.m0zilla.javascript.Scriptable;
import org.m0zilla.javascript.ScriptableObject;
import org.m0zilla.javascript.Synchronizer;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.tag.cfDUMP;
import com.naryx.tagfusion.cfm.tag.tagUtils;

public class convert extends Object {

	public static Context getJavaScriptContextForSession(cfSession _Session, HashMap<String, JavascriptDefinedFunction>	functions ) throws Exception {
		Context cx = (Context) _Session.getDataBin("jscontext");
		
		if (cx == null) {
			cx = Context.enter();
			cx.setOptimizationLevel(-1);
			cx.getWrapFactory().setJavaPrimitiveWrap(false);

			Scriptable scope = cx.initStandardObjects();

			/* Setup the properties that we will let the JS block get at */
			ScriptableObject.putProperty(scope, "$cf", Context.javaToJS(new cfmlBridge(_Session, functions), scope));

			ScriptableObject.putProperty(scope, "variables", 		convert.cfDataConvert(_Session.getData("variables")));
			ScriptableObject.putProperty(scope, "cgi", 					convert.cfDataConvert(_Session.getData("cgi")));
			ScriptableObject.putProperty(scope, "form", 				convert.cfDataConvert(_Session.getData("form")));
			ScriptableObject.putProperty(scope, "url", 					convert.cfDataConvert(_Session.getData("url")));

			ScriptableObject.putProperty(scope, "application", 	convert.cfDataConvert(_Session.getData("application")));
			ScriptableObject.putProperty(scope, "session", 			convert.cfDataConvert(_Session.getData("session")));
			ScriptableObject.putProperty(scope, "client", 			convert.cfDataConvert(_Session.getData("client")));

			
			/* Add in the Global methods load, sync, print, dump */
			Class[] signature = new Class[] {Context.class,Scriptable.class,Object[].class,Function.class};
			FunctionObject f = new FunctionObject( "print", convert.class.getMethod("print", signature ), scope );
			ScriptableObject.defineProperty(scope, "print", f, ScriptableObject.DONTENUM);
			
			f = new FunctionObject( "dump", convert.class.getMethod("dump", signature ), scope );
			ScriptableObject.defineProperty(scope, "dump", f, ScriptableObject.DONTENUM);
			
			f = new FunctionObject( "console", convert.class.getMethod("console", signature ), scope );
			ScriptableObject.defineProperty(scope, "console", f, ScriptableObject.DONTENUM);
			
			f = new FunctionObject( "load", convert.class.getMethod("load", signature ), scope );
			ScriptableObject.defineProperty(scope, "load", f, ScriptableObject.DONTENUM);
			
			f = new FunctionObject( "sync", convert.class.getMethod("sync", signature ), scope );
			ScriptableObject.defineProperty(scope, "sync", f, ScriptableObject.DONTENUM);

			/* Execute the script block */
			cx.putThreadLocal("scope", scope);
			cx.putThreadLocal("cfsession", _Session);

			_Session.setDataBin("jscontext", cx);
		}

		return cx;
	}

	
	public static Object sync(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws Exception {
		if (args.length == 1 && args[0] instanceof Function) {
			return new Synchronizer((Function) args[0]);
		} else {
			throw new Exception("msg.sync.args");
		}
	}
	
	public static Object load( Context cx, Scriptable thisObj, Object[] args, Function funObj) throws Exception {
    for (int i = 0; i < args.length; i++) {
    	String filePath = Context.toString(args[i]);
    	cfSession thisSession = (cfSession)cx.getThreadLocal("cfsession");
    	cfFile file = thisSession.getRealFile( filePath );
    	ScriptFactory.getScript( filePath, file.getFileBody()).exec(cx, (Scriptable) cx.getThreadLocal("scope"));
    }
		
		return Context.getUndefinedValue();
	}

	public static Object print( Context cx, Scriptable thisObj, Object[] args, Function funObj){
		cfSession thisSession = (cfSession)cx.getThreadLocal("cfsession");
		thisSession.forceWrite( String.valueOf(args[0]) );
		return Context.getUndefinedValue();
	}
	
	
	/*
	 * The output follows cfml writeDump for complex values and js print() for simple values
	 */
	public static Object dump( Context cx, Scriptable thisObj, Object[] args, Function funObj) throws Exception{
		cfSession thisSession = (cfSession)cx.getThreadLocal("cfsession");
		
		if ( args[0] instanceof IdScriptableObject ) {
			cfData dat = convert.jsConvert2cfData( (IdScriptableObject) args[0] );
			String out = cfDUMP.dumpSession(thisSession, dat, null, "", 9999, false, false );
			thisSession.forceWrite( out );
			
		} else {
			thisSession.forceWrite( (String) "" + args[0] );
		}
		
		return Context.getUndefinedValue();
	}
	

	public static Object console( Context cx, Scriptable thisObj, Object[] args, Function funObj){
		System.out.println( args[0] );
		return Context.getUndefinedValue();
	}
	
	/*
	 * Converts Javascript NativeObject into CFML; recursive call
	 */
	public static cfData jsConvert2cfData(IdScriptableObject obj) throws Exception {
		
		if (obj instanceof NativeObject) {
			cfStructData struct = new cfStructData();

			NativeObject nobj = (NativeObject) obj;
			Object[] elements = nobj.getAllIds();
			cfData cfdata;

			for (int x = 0; x < elements.length; x++) {
				Object jsObj = nobj.get(elements[x]);

				if (jsObj == null)
					cfdata = cfNullData.NULL;
				else if (jsObj instanceof NativeObject || jsObj instanceof NativeArray)
					cfdata = jsConvert2cfData((IdScriptableObject) jsObj);
				else if (jsObj instanceof cfData)
					cfdata = (cfData) jsObj;
				else
					cfdata = tagUtils.convertToCfData(jsObj);

				/* check for null; it may be intentional */
				if (cfdata == null)
					cfdata = cfNullData.NULL;

				struct.setData((String) elements[x], cfdata);
			}

			return struct;
		} else if (obj instanceof NativeArray) {
			cfArrayData array = cfArrayData.createArray(1);

			NativeArray nobj = (NativeArray) obj;
			cfData cfdata;
			int len = (int) nobj.getLength();

			for (int x = 0; x < len; x++) {
				Object jsObj = nobj.get(x);

				if (jsObj == null)
					cfdata = cfNullData.NULL;
				else if (jsObj instanceof NativeObject || jsObj instanceof NativeArray)
					cfdata = jsConvert2cfData((IdScriptableObject) jsObj);
				else if (jsObj instanceof cfData)
					cfdata = (cfData) jsObj;
				else
					cfdata = tagUtils.convertToCfData(jsObj);

				/* check for null; it may be intentional */
				if (cfdata == null)
					cfdata = cfNullData.NULL;

				array.addElement(cfdata);
			}

			return array;
		} else {
			return null;
		}
	}

	/*
	 * Takes a CFML object -> Java
	 */
	public static Object cfDataConvert(cfData data) throws Exception {
		if (data == null) {
			return null;
		} else if (data.getDataType() == cfData.CFSTRINGDATA)
			return data.getString();
		else if (data.getDataType() == cfData.CFBOOLEANDATA)
			return data.getBoolean();
		else if (data.getDataType() == cfData.CFSTRUCTDATA)
			return new cfStructDataScriptableObject((cfStructData) data);
		else if (data.getDataType() == cfData.CFARRAYDATA)
			return new cfArrayDataScriptableObject((cfArrayData) data);
		else if (data.getDataType() == cfData.CFCOMPONENTOBJECTDATA)
			return new cfComponentDataScriptableObject((cfComponentData) data);
		else if (data.getDataType() == cfData.CFQUERYRESULTDATA)
			return new cfQueryDataScriptableObject((cfQueryResultData) data);
		else if (data.getDataType() == cfData.CFNUMBERDATA) {
			String tmp = data.getString();
			if (tmp.lastIndexOf(".") == -1)
				return Long.valueOf(tmp);
			else
				return Double.valueOf(tmp);
		} else
			return data.getString();
	}
}