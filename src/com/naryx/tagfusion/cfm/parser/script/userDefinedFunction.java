/*
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: userDefinedFunction.java 2496 2015-02-01 02:19:29Z alan $
 */


package com.naryx.tagfusion.cfm.parser.script;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDataFactory;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.parser.CFCall;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.CFUndefinedValue;
import com.naryx.tagfusion.cfm.parser.cfLData;
import com.naryx.tagfusion.cfm.parser.indirectReferenceData;
import com.naryx.tagfusion.cfm.tag.cfFUNCTION;
import com.naryx.tagfusion.cfm.tag.cfFUNCTION.ReturnFormat;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.tagUtils;
import com.naryx.tagfusion.expression.function.functionBase;

public class userDefinedFunction extends functionBase implements java.io.Serializable {
	private static final long serialVersionUID = 1;

	public static final int ACCESS_PRIVATE = 0;
	public static final int ACCESS_PACKAGE = 1;
	public static final int ACCESS_PUBLIC = 2;
	public static final int ACCESS_REMOTE = 3;

	protected String name;
	protected cfStructData metaData;
	protected int access = -1;

	private String returnType;
	private Map<String, String> attributes; // the function attributes

	// the following attribute is only used for CFFUNCTION-based UDFs
	protected cfFUNCTION parentFunction;

	// the following two attributes are only used for CFSCRIPT-based UDFs
	private CFScriptStatement body;

	protected List<CFFunctionParameter> formals; // a list of argument names in Strings

	// for UDFs within a CFC; needs to be per-CFC instance
	protected cfComponentData superScope;

	// for cfScript:Java
	protected JavaBlock javaBlock;

	protected Method javaMethod;

	// for creating CFSCRIPT-based UDFs
	public userDefinedFunction(String _name, byte _access, String _returnType, List<CFFunctionParameter> _formals, Map<String, String> _attr, CFScriptStatement _body) {
		name = _name;
		access = _access;
		formals = _formals;
		attributes = _attr;
		body = _body;
		returnType = _returnType;
	}

	// for creating CFFUNCTION-based UDFs
	public userDefinedFunction(String _name, cfFUNCTION _parent) {
		name = _name;
		parentFunction = _parent;
	}

	public userDefinedFunction(userDefinedFunction udf, cfComponentData _superScope) {
		name = udf.name;
		metaData = udf.metaData;
		access = udf.access;
		attributes = udf.attributes;
		returnType = udf.returnType;
		parentFunction = udf.parentFunction;
		body = udf.body;
		formals = udf.formals;

		javaBlock = udf.javaBlock;
		javaMethod = udf.javaMethod;

		superScope = _superScope;
	}

	public userDefinedFunction(Method method, JavaBlock javaBlock) {
		name = method.getName();
		this.javaBlock = javaBlock;
		this.javaMethod = method;
		access = ACCESS_PUBLIC;
		formals = new ArrayList<CFFunctionParameter>(2);
	}

	/*
	 * This allows for the JavascriptDefinedFunction to properly handle the inheritance stack
	 */
	public userDefinedFunction duplicateAndInherit(cfComponentData _superScope) {
		return new userDefinedFunction(this, _superScope);
	}

	public cfData duplicate() {
		// once constructed, the UDF attributes never change, so no need to create a
		// duplicate, which just would be exactly the same as the original
		return this;
	}

	public byte getDataType() {
		return cfData.CFUDFDATA;
	}

	public String getTypeString() {
		return "UDF";
	}

	// for CFFUNCTION-based UDFs only
	public List<cfStructData> getFormalArguments() {
		if (parentFunction != null) {
			return parentFunction.getFormalArguments();
		}
		return null;
	}

	public List<CFFunctionParameter> getUDFFormals() {
		return formals;
	}

	public String getName() {
		return name;
	}

	public String getString() {
		return name;
	}

	public cfFUNCTION getParentFunction() {
		return parentFunction;
	}

	public String getParentComponentName() {
		String parentComponentName = null;
		if (parentFunction != null) {
			parentComponentName = parentFunction.getParentComponentName();
		}
		return (parentComponentName != null ? parentComponentName : "");
	}

	public boolean isAbstract() {
		if (parentFunction != null) {
			return parentFunction.isAbstract();
		}
		return false;
	}

	public boolean isJavaBlock() {
		return (javaBlock != null);
	}

	public JavaBlock getJavaBlock() {
		return javaBlock;
	}

	public void setJavaBlock(JavaBlock jb) {
		javaBlock = jb;
	}

	public int getAccessType() {
		if (access < 0) {
			access = ACCESS_PUBLIC; // default access type
			cfData accessData = getMetaData().getData(cfFUNCTION.ACCESS);
			if (accessData != null) {
				String accessString = accessData.toString();
				if (accessString.equalsIgnoreCase("PRIVATE")) {
					access = ACCESS_PRIVATE;
				} else if (accessString.equalsIgnoreCase("PACKAGE")) {
					access = ACCESS_PACKAGE;
				} else if (accessString.equalsIgnoreCase("REMOTE")) {
					access = ACCESS_REMOTE;
				}
			}
		}
		return access;
	}

	public String getAccessTypeString() {
		int access = getAccessType();
		switch (access) {
			case ACCESS_PUBLIC:
				return "PUBLIC";
			case ACCESS_PRIVATE:
				return "PRIVATE";
			case ACCESS_PACKAGE:
				return "PACKAGE";
			case ACCESS_REMOTE:
				return "REMOTE";
			default:
				return "";
		}
	}

	public ReturnFormat	getReturnFormat(){
		return cfFUNCTION.getReturnFormat( attributes.get("returnformat") );				
	}

	public String	getReturnJSONDate(){
		if (attributes.containsKey("jsondate")) {
			return ((String)attributes.get("jsondate")).toLowerCase();
		} else {
			return cfEngine.DefaultJSONReturnDate;
		}
	}	
	
	public String	getReturnJSONCase(){
		if (attributes.containsKey("jsoncase")) {
			return ((String)attributes.get("jsoncase")).toLowerCase();
		} else {
			return cfEngine.DefaultJSONReturnCase;
		}
	}
	
	public String getReturnType() {
		return returnType;
	}

	// metadata is needed by CFC function calls, also by GetMetaData()
	public cfStructData getMetaData() {
		if (parentFunction != null) {
			return parentFunction.getMetaData();
		}
		if (metaData == null) {
			initMetaData();
		}
		return metaData;
	}

	private synchronized void initMetaData() {
		metaData = new cfStructData();
		metaData.setData("NAME", new cfStringData(name));
		metaData.setData(cfFUNCTION.ACCESS, new cfStringData(getAccessTypeString()));
		if (this.returnType != null) {
			metaData.setData(cfFUNCTION.RETURNTYPE, new cfStringData(returnType));
		}

		Iterator<String> keyIterator = attributes.keySet().iterator();
		while (keyIterator.hasNext()) {
			String nextKey = keyIterator.next();
			metaData.put(nextKey, attributes.get(nextKey));
		}

		cfArrayData argArray = cfArrayData.createArray(1);

		for (int i = 0; i < formals.size(); i++) {
			CFFunctionParameter nextParam = formals.get(i);
			
			cfStructData argStruct = new cfStructData();
			argStruct.setData("NAME", new cfStringData(nextParam.getName().toUpperCase()));
			argStruct.setData("REQUIRED", new cfStringData(nextParam.isRequired() ? "true" : "false"));
			
			if (nextParam.isDefaulted())
				argStruct.setData("DEFAULT", new cfStringData(nextParam.getDefaultAsString()));
			
			if (nextParam.isFormallyTyped())
				argStruct.setData("TYPE", new cfStringData(nextParam.getType()));
			
			try {
				argArray.addElement(argStruct);
			} catch (cfmRunTimeException ignore) {
				// will never happen
			}
		}

		metaData.setData("PARAMETERS", argArray);
	}

	public cfData execute(cfSession _session, List<cfData> _actuals) throws cfmRunTimeException {
		return execute(_session, _actuals, true);
	}

	public cfData execute(cfSession _session, List<cfData> _actuals, boolean _isLocalExec) throws cfmRunTimeException {
		if (parentFunction != null) {
			if (parentFunction.isAbstract()) {
				throw new cfmRunTimeException(catchDataFactory.generalException(_session, cfCatchData.TYPE_TEMPLATE, "Abstract Function Invocation", "Abstract functions may not be invoked"));
			}
			return parentFunction.run(_session, name, _actuals, superScope, _isLocalExec);
		}

		// Create the arguments array - this is empty if there are fewer actuals than formals
		int nargs = _actuals.size();
		cfArgStructData args = new cfArgStructData();

		// Instantiate the formals
		for (int i = 0; i < formals.size(); i++) {
			if (i < nargs) {
				// note index increment is down to cfArrayData api that indexes from 1 as opposed to 0.
				args.setData(formals.get(i).getName(), _actuals.get(i));
			}
		}

		// Put the remainder into "arguments"
		for (int i = formals.size(); i < nargs; i = i + 1) {
			// note index increment is down to cfArrayData api that indexes from 1 as opposed to 0.
			args.setData(String.valueOf(i + 1), _actuals.get(i));
		}

		// Invoke the javablock if this function is representing this
		if (javaBlock != null) {

			org.alanwilliamson.lang.java.inline.ContextImpl.putSession(_session);

			if (_actuals.size() == 0) {

				try {

					if (javaMethod.getReturnType() == null) {
						javaMethod.invoke(javaBlock, (Object[]) null);
						return cfBooleanData.TRUE;
					} else {
						Object returnObj = javaMethod.invoke(javaBlock, (Object[]) null);
						return tagUtils.convertToCfData(returnObj);
					}

				} catch (IllegalArgumentException e) {
					throwException(_session, "IllegalArgumentException: " + e.getMessage());
					return null;
				} catch (IllegalAccessException e) {
					throwException(_session, "IllegalAccessException: " + e.getMessage());
					return null;
				} catch (InvocationTargetException e) {
					throwException(_session, "InvocationTargetException: " + e.getMessage());
					return null;
				}

			} else {

				try {

					cfJavaObjectData javaobject = new cfJavaObjectData(javaBlock);
					return javaobject.invokeMethod(name, _actuals, _session);

				} catch (IllegalArgumentException e) {
					throwException(_session, "IllegalArgumentException: " + e.getMessage());
					return null;
				}

			}

		} else {
			return execute(_session, args, _isLocalExec);
		}
	}

	public cfData execute(cfSession _session, cfArgStructData _args, boolean _isLocalExec) throws cfmRunTimeException {
		if (parentFunction != null) { // CFFUNCTION-based UDF call
			return parentFunction.run(_session, name, _args, superScope, _isLocalExec);
		}

		cfComponentData activeComponent	= _session.getActiveComponentData();
		boolean pushFile = false;
		if ( activeComponent != null ){
			cfFile thisFile = activeComponent.getComponentFile();
			if (thisFile != null) {
				pushFile = !thisFile.equals(_session.activeFile());
				if (pushFile)
					_session.pushActiveFile(thisFile);
			}
		}
		
		cfTag thisTag = body.getHostTag();
		boolean pushTag = false;
		if (thisTag != null) {
			if (!thisTag.equals(_session.activeTag())) {
				_session.pushTag(thisTag);
				pushTag = true;
			}
		}

		_session.pushUserDefinedFunction(this);

		cfData retVal = null;
		CFContext context = _session.getCFContext();
		if (context == null) {
			throwException(_session, "Internal error : failed to get context");
		}

		// Create a call environment
		CFCall call = _session.enterUDF(_args, superScope, _isLocalExec);

		// loop thru the formal args inserting their values to the call scope
		for (int i = 0; i < formals.size(); i++) {
			CFFunctionParameter nextParam = formals.get(i);
			String nextKey = nextParam.getName();
			cfData nextData;

			if (_args.isNamedBased()) 
				nextData = _args.getData(nextKey);
			else
				nextData = _args.getData(i);
			
			
			boolean isDefined = (nextData != null && nextData != CFUndefinedValue.UNDEFINED);
			if (!isDefined && nextParam.isDefaulted()) {
				nextData = nextParam.getDefaultValue(context);
				_args.setData(nextKey, nextData);
			} else if (!isDefined && nextParam.isRequired()) {
				throwException(_session, "Value not provided for required argument " + nextKey.toUpperCase() + " of function " + name);
			} else if (nextData != null && nextParam.isFormallyTyped()) {
				// validate type
				try {
					nextData = cfFUNCTION.coerceArgumentType(_session, nextParam.getType(), nextKey, nextData);
				} catch (dataNotSupportedException e) {
					throw new cfmRunTimeException(catchDataFactory.generalException(_session, cfCatchData.TYPE_TEMPLATE, "The argument " + nextKey.toUpperCase() + " passed to function is not of type " + nextParam.getType(), "If the parameter type is a component name, it is possible the component could not be found"));
				}
			}

			if (nextData == null || nextData == CFUndefinedValue.UNDEFINED) {
				continue;
			}

			nextData = new indirectReferenceData(nextKey, _args, new cfStringData(nextKey));
			call.put(nextKey, nextData, context);
		}

		// Execute the body.
		_session.debugger.startScriptFunction( this );
		
		CFStatementResult result;
		boolean output = string.convertToBoolean(this.attributes.get("output"), true);
		if (!output) {
			cfSession currentSession = context.getSession();
			cfSession offlineSession = new cfSession(currentSession, false);
			context.setSession(offlineSession);

			result = body.Exec(context);
			context.setSession(currentSession);
		} else {
			result = body.Exec(context);
		}

		if (result == null) {
			retVal = CFUndefinedValue.UNDEFINED;
		} else if (result.isReturn()) {
			retVal = result.getReturnValue();
			if ((retVal != null) && (retVal.getDataType() == cfData.CFLDATA)) {
				retVal = ((cfLData) retVal).Get(context);
			}

			// check if trying to return a value when the return type is VOID
			if ( "VOID".equalsIgnoreCase( returnType ) && retVal != null && retVal.getDataType() != cfData.CFNULLDATA && !( retVal instanceof CFUndefinedValue ) ){
				cfCatchData catchData = new cfCatchData();
				catchData.setMessage( "A value cannot be returned when the return type is VOID" );
				catchData.setDetail("Function: " + name);
				throw new cfmRunTimeException(catchData);
			}

			try {
				retVal = cfFUNCTION.coerceReturnType(context.getSession(), retVal, returnType);
			} catch (dataNotSupportedException e) {
				// throw error
				cfCatchData catchData = new cfCatchData();
				catchData.setMessage("The value returned from function " + name + "() is not of type " + returnType);
				catchData.setDetail("Function: " + name);
				if (returnType.equalsIgnoreCase("UUID") || returnType.equalsIgnoreCase("GUID")) {
					catchData.setExtendedInfo("Formal type: " + returnType + ", Return value: " + retVal);
				} else {
					catchData.setExtendedInfo("Formal type: " + returnType + ", Actual type: " + cfDataFactory.getDatatypeString(retVal));
				}

				throw new cfmRunTimeException(catchData);
			}
		} else {
			// ### not sure that this should get through here
			retVal = cfBooleanData.TRUE;
		}

		_session.debugger.endScriptFunction( this );

		_session.popUserDefinedFunction();

		if (pushFile)
			_session.popActiveFile();

		if (pushTag)
			_session.popTag();

		_session.leaveUDF();
		
		return retVal;
	}

	public void dump(PrintWriter out) {
		dump(out, "");
	}

	public void dump(PrintWriter out, String _label) {
		if (parentFunction != null) {
			parentFunction.dump(out, _label);
			return;
		}

		out.write("<table class='cfdump_table_udf' width=\"100%\">");
		out.write("<th class='cfdump_th_udf'>");
		if (_label.length() > 0)
			out.write(_label + " - ");
		out.write("function ");
		out.write(name);
		// out.write( " (" + this.hashCode() + ")" ); // useful for debugging
		out.write("</th>");
		out.write("<tr><td class='cfdump_td_value'><table cellspacing=2 width=\"100%\">");

		if ((formals == null) || (formals.size() == 0)) {
			out.write("<tr><td  class='cfdump_td_udf'>Arguments:</td>");
			out.write("<td class='cfdump_td_value'>none</td></tr>");
		} else {
			out.write("<tr><td  class='cfdump_td_udf' colspan=2>Arguments:</td></tr>");
			out.write("<tr><td  class='cfdump_td_udf_args' colspan=2>");
			out.write("<table class='cfdump_table_udf_args'>");
			out.write("<th class='cfdump_th_udf_args'>Name</th>");
			out.write("<th class='cfdump_th_udf_args'>Required</th>");
			out.write("<th class='cfdump_th_udf_args'>Type</th>");
			out.write("<th class='cfdump_th_udf_args'>Default</th>");

			for (int i = 0; i < formals.size(); i++) {
				CFFunctionParameter nextFormal = formals.get(i);
				out.write("<tr><td class='cfdump_td_value'>" + nextFormal.getName().toLowerCase() + "</td>");
				out.write("<td class='cfdump_td_value'>");
				out.write(nextFormal.isRequired() ? "Required" : "Optional");
				out.write("</td>");
				out.write("<td class='cfdump_td_value'>");
				out.write(nextFormal.isFormallyTyped() ? nextFormal.getType() : "any");
				out.write("</td>");
				out.write("<td class='cfdump_td_value'>");
				out.write(nextFormal.isDefaulted() ? nextFormal.getDefaultAsString() : "");
				out.write("</td></tr>");
			}

			out.write("</table>");
			out.write("</td></tr>");
		}

		out.write("<tr><td class='cfdump_td_udf' width='50%'>Return Type:</td>");
		out.write("<td class='cfdump_td_value'>" + (returnType != null ? returnType.toLowerCase() : "") + "</td></tr>");
		out.write("<tr><td class='cfdump_td_udf' width='50%'>Roles:</td>");
		out.write("</table></td></tr>");
		out.write("</table>");

	}

	public String toString() {
		StringWriter w = new StringWriter();
		this.dump(new PrintWriter(w));
		return w.toString();
	}

}
