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
 *  $Id: cfComponentData.java 2398 2013-07-28 21:19:27Z alan $
 */

package com.naryx.tagfusion.cfm.engine;

/**
 * cfcInstanceData
 *
 * This class represents a single instance of a CFC class.
 */
import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;

import com.naryx.tagfusion.cfm.application.cfAPPLICATION;
import com.naryx.tagfusion.cfm.application.cfApplicationManager;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.cfm.tag.cfCOMPONENT;
import com.naryx.tagfusion.cfm.tag.cfDUMP;
import com.naryx.tagfusion.cfm.tag.cfFUNCTION;

public class cfComponentData extends cfComponentDataBase implements Serializable {
	static final long serialVersionUID = 1;

	// cfc namespace
	public static final String NS_URI_CFC = "http://openbd.org/cfc";

	// metaData attributes
	private static final String NAME = "NAME";
	private static final String FULLNAME = "FULLNAME";
	private static final String PATH = "PATH";

	// metaData is set within cfCOMPONENT.render()
	private cfStructData metaData;

	// component name
	private String 	_componentName;
	private String 	_componentPath;
	private int	 		_componentHash;
	private int 		_componentType; // one of the constant values defined in cfCOMPONENT

	// superclass
	private cfComponentData _superComponent;

	private cfFile _componentFile;
	
	// variables scopes
	private cfStructData _variablesScope = new cfStructData();

	// for inheritance/implements "is-type-of" checks
	private FastList<String> _polymorphNames;
	private FastList<String> _polymorphPaths;
	
	// simple constructor
	public cfComponentData(cfSession session, String componentName ) throws cfmRunTimeException {
		this(session, ComponentFactory.loadRawComponent(session, componentName, null ), false);
	}

	// simple constructor
	public cfComponentData(cfSession session, String componentName, List<String> importPaths ) throws cfmRunTimeException {
		this(session, ComponentFactory.loadRawComponent(session, componentName, importPaths ), false);
	}

	public cfComponentData(cfSession session, String componentName, boolean allowAbstract) throws cfmRunTimeException {
		this(session, ComponentFactory.loadRawComponent(session, componentName, null ), allowAbstract);
	}

	// simple constructor
	public cfComponentData(cfSession session, String componentName, List<String> importPaths, boolean allowAbstract) throws cfmRunTimeException {
		this(session, ComponentFactory.loadRawComponent(session, componentName, importPaths ), allowAbstract);
	}

	// simple constructor
	public cfComponentData(cfSession session, cfFile componentFile) throws cfmRunTimeException {
		this(session, componentFile, false);
	}

	// real constructor
	public cfComponentData(cfSession session, cfFile componentFile, boolean allowAbstract) throws cfmRunTimeException {
		
		// save the component name
		_componentHash 	= componentFile.hashCode();
		_componentName 	= componentFile.getComponentName();
		_componentPath 	= componentFile.getCfmlURI().getRealPath(session.REQ);
		_componentFile	= componentFile;
		
		// set "this" scope variable defaults for Application .cfc
		if (_componentName.toLowerCase().endsWith("application")) { 
			cfApplicationManager appManager = cfAPPLICATION.getAppManager();
			this.setData(cfAPPLICATION.APPLICATIONTIMEOUT, appManager.getDefaultApplicationTimeOutData(session));
			this.setData(cfAPPLICATION.CLIENTMANAGEMENT, cfBooleanData.FALSE);
			this.setData(cfAPPLICATION.CLIENTSTORAGE, new cfStringData(appManager.getDefaultClientStorage()));
			this.setData(cfAPPLICATION.LOGINSTORAGE, new cfStringData(cfAPPLICATION.DEFAULT_LOGIN_STORAGE));
			this.setData(cfAPPLICATION.SCRIPTPROTECT, new cfStringData(""));
			this.setData(cfAPPLICATION.SESSIONMANAGEMENT, cfBooleanData.FALSE);
			this.setData(cfAPPLICATION.SESSIONTIMEOUT, appManager.getDefaultSessionTimeOutData(session));
			this.setData(cfAPPLICATION.SETCLIENTCOOKIES, cfBooleanData.TRUE);
			this.setData(cfAPPLICATION.SETDOMAINCOOKIES, cfBooleanData.FALSE);
			this.setData(cfAPPLICATION.SECUREJSON, cfBooleanData.FALSE);
			this.setData(cfAPPLICATION.SECUREJSONPREFIX, new cfStringData("//"));
		}

		// "this" scope lives inside the variables scope
		_variablesScope.setData("this", this);

		// render CFC pseudo-constructor (sets metadata and _componentType)
		renderComponentFile(session, componentFile);

		if (!allowAbstract) {
			if (_componentType == cfCOMPONENT.ABSTRACT) {
				throw new cfmRunTimeException(catchDataFactory.generalException(session, cfCatchData.TYPE_TEMPLATE, "Abstract Component Instantiation",	"Components of TYPE=\"abstract\" cannot be instantiated directly"));
			} else if (_componentType == cfCOMPONENT.INTERFACE) {
				throw new cfmRunTimeException(catchDataFactory.generalException(session, cfCatchData.TYPE_TEMPLATE, "Component Interface Instantiation","Components of TYPE=\"interface\" cannot be instantiated directly"));
			}
		}
	}

	public int getComponentHash(){
		return _componentHash;
	}

	public cfFile getComponentFile(){
		return _componentFile;
	}
	
	private void renderComponentFile(cfSession session, cfFile componentFile) throws cfmRunTimeException {
		enterComponent(session);
		session.enterUDF(null, _superComponent, _variablesScope, false);

		//This try-catch block was taken from commercial BD to fix OpenBD #178
		try{
			componentFile.render(session);
		}catch (cfmRunTimeException e){
			session.write(e.getOutput());
			throw e;
		}

		session.leaveUDF();
		leaveComponent(session);
	}

	// for duplicate
	private cfComponentData() {}

	public synchronized cfData duplicate() {
		// when a CFC is instantiated, there's only one "this" scope and one
		// "variables" scope
		// for the entire inheritance hierarchy; the "super" scope contains only
		// UDFs, which do
		// not need to be duplicated; therefore, the "super" scope does not need to
		// be duplicated
		cfComponentData dupComponent = new cfComponentData();
		dupComponent._superComponent = this._superComponent;

		// duplicate the "this" scope
		Object[] keys = this.keys();
		for (int i = 0; i < keys.length; i++) {
			String nextKey = (String) keys[i];
			cfData nextData = this.getData(nextKey);
			if (nextData != null) {
				cfData nextDataCopy = nextData.duplicate();
				if (nextDataCopy == null) { // return null if struct contains
																		// non-duplicatable type
					return null;
				}
				dupComponent.setData(nextKey, nextDataCopy);
			}
		}

		if (this._variablesScope == null) {
			dupComponent._variablesScope = null;
		} else {
			// duplicate the "variables" scopes, ignoring the "this" scope
			keys = this._variablesScope.keys();
			for (int i = 0; i < keys.length; i++) {
				String nextKey = (String) keys[i];
				if (!nextKey.equalsIgnoreCase("this")) { // don't duplicate the "this"
																									// scope
					cfData nextData = this._variablesScope.getData(nextKey);
					if (nextData != null) {
						cfData nextDataCopy = nextData.duplicate();
						if (nextDataCopy == null) { // return null if struct contains
																				// non-duplicatable type
							return null;
						}
						dupComponent._variablesScope.setData(nextKey, nextDataCopy);
					}
				}
			}

			// "this" scope lives inside the variables scope
			dupComponent._variablesScope.setData("this", dupComponent);
		}

		dupComponent.metaData = (cfStructData) this.metaData.duplicate();

		dupComponent._componentName = this._componentName;
		dupComponent._componentPath = this._componentPath;
		dupComponent._componentType = this._componentType;

		return dupComponent;
	}

	public cfComponentData getSuperComponent() {
		return _superComponent;
	}

	public void setSuperComponent(cfComponentData superComponent) {
		_superComponent = superComponent;
	}

	public cfStructData getVariablesScope() {
		return _variablesScope;
	}

	public void setVariablesScope(cfStructData newVariables) {
		_variablesScope = newVariables;
	}

	// identify component by name
	public String getComponentName() {
		return _componentName;
	}

	public String getComponentPath() {
		return _componentPath;
	}

	// _componentType is set within cfCOMPONENT.render() and has one of the
	// constant values defined in cfCOMPONENT
	public void setComponentType(int componentType) {
		_componentType = componentType;
	}

	public int getComponentType() {
		return _componentType;
	}

	// determine component's package path
	public String getComponentPackagePath() {
		int lastDot = _componentName.lastIndexOf(".");
		return (lastDot > 0 ? _componentName.substring(0, lastDot) : "");
	}

	// identify the cfData subclass
	public byte getDataType() {
		return cfData.CFCOMPONENTOBJECTDATA;
	}

	public String getDataTypeName() {
		return "component";
	}

	// metaData is set within cfCOMPONENT.render()
	public void setMetaData(cfStructData meta) {
		metaData = meta;
	}

	// return the component metadata
	public cfStructData getMetaData() {
		metaData.setData(NAME, new cfStringData(_componentName));
		metaData.setData(FULLNAME, new cfStringData(_componentName));
		metaData.setData(PATH, new cfStringData(_componentPath));
		return metaData;
	}

	// check to see if this component inherits from the type specified
	public boolean isTypeOf(cfSession session, String type) {
		if (_polymorphNames == null) {
			buildPolymorphList();
		}

		// first check based on component names
		if (_polymorphNames.contains(type)) {
			return true;
		}

		if (type.indexOf('.') == -1) {
			type = ComponentFactory.getFullComponentName(session, type);
			if (_polymorphNames.contains(type)) {
				_polymorphNames.add(type);
				return true;
			}
		}

		// path-based check because a given CFC can have multiple names due to
		// mappings
		try {
			cfFile typeFile = ComponentFactory.loadRawComponent(session, type, null);
			String typePath = typeFile.getCfmlURI().getRealPath(session.REQ);
			if (_polymorphPaths.contains(typePath)) {
				_polymorphNames.add(type); // save the name for next time
				return true;
			}
		} catch (cfmRunTimeException e) {
			return false;
		}

		return false;
	}

	// build the list of components with which this one is polymorphic; basically
	// the
	// inheritance chain, plus the IMPLEMENTS list, used for "is-type-of" checking
	private synchronized void buildPolymorphList() {
		if (_polymorphNames != null) {
			return;
		}
		_polymorphNames = new FastList<String>();
		_polymorphNames.setValueComparator(com.nary.util.FastMap.stringComparatorIgnoreCase);

		_polymorphPaths = new FastList<String>();
		_polymorphPaths.setValueComparator(com.nary.util.FastMap.stringComparatorIgnoreCase);

		// add this component into list
		_polymorphNames.add(_componentName);
		_polymorphPaths.add(_componentPath);

		// add inheritance chain to the list
		cfComponentData inst = _superComponent;
		while (inst != null) {
			// add type of super class
			_polymorphNames.add(inst.getComponentName());
			_polymorphPaths.add(inst.getComponentPath());
			// check for next super class
			inst = inst.getSuperComponent();
		}

		// add implements to the list
		cfArrayData implementsData = (cfArrayData) metaData.getData(cfCOMPONENT.IMPLEMENTS);
		if (implementsData != null) {
			for (int i = 1; i <= implementsData.size(); i++) {
				cfStructData interfaceData = (cfStructData) implementsData.getElement(i);
				_polymorphNames.add(interfaceData.getData(NAME).toString());
				_polymorphPaths.add(interfaceData.getData(PATH).toString());
			}
		}
	}

	// NOTE:
	// See the warning attached to this method's overload
	private void enterComponent(cfSession session) {
		enterComponent(session, cfFUNCTION.EMPTY_FUNCTION);
	}

	// NOTE:
	// It is important NOT to enter the component until just before the
	// component's
	// method is being called inside of the invokeComponentFunction. Doing so
	// before then will most likely lead to problems evaluating the method's
	// visibility cause the component to think that it is invoking a method on
	// itself
	// rather than from some other component or page.
	private void enterComponent(cfSession session, cfFUNCTION functionTag) {
		session.pushComponentData(this, functionTag);
	}

	// NOTE:
	// It is important to enter the component right after the component's
	// method is being called inside of the invokeComponentFunction. Failing to do
	// will most likely lead to problems evaluating the method's visibility cause
	// the component to think that it is invoking a method on itself
	// rather than from some other component or page.
	private static void leaveComponent(cfSession session) {
		session.popComponentData();
	}

	// retrieve a variable value from the "this" scope
	public cfData getData(cfData name) {
		return getData(name.toString());
	}

	public cfData getData(String name) {
		// check for scope names
		if (isProtectedScope(name)) {
			return cfNullData.NULL;
		}
		return super.getData(name);
	}

	// set a variable and value into the "this" scope
	public void setData(String _key, cfData _data) {
		if (isProtectedScope(_key)) {
			throw new IllegalArgumentException("Cannot set CFC \"" + _key + "\" scope");
		}
		super.setData(_key, _data);
	}

	public void setData(cfData _key, cfData _data) throws cfmRunTimeException {
		String keyStr = _key.toString();
		// check for scope names
		if (isProtectedScope(keyStr)) {
			throw new dataNotSupportedException("Cannot set CFC \"" + keyStr + "\" scope");
		}
		super.setData(keyStr, _data);
	}

	public void deleteData(String _key) throws cfmRunTimeException {
		if (isProtectedScope(_key)) {
			throw new dataNotSupportedException("Cannot delete CFC \"" + _key + "\" scope");
		}
		cfData val = super.getData(_key);
		if (val != null) {
			super.deleteData(_key);
		}
	}

	private userDefinedFunction getFunction(String functionName) {
		cfData cfdata = this.getData(functionName);
		if ((cfdata != null) && (cfdata.getDataType() == cfData.CFUDFDATA)) {
			return (userDefinedFunction) cfdata;
		}
		return null;
	}

	/**
	 * Appliction.cfc functions differ from normal functions in that they don't
	 * throw an exception if the function isn't defined or not accessible.
	 */
	public cfData invokeApplicationFunction(cfSession session, javaMethodDataInterface method) throws cfmRunTimeException {
		String methodName = method.getFunctionName();
		userDefinedFunction udf = getFunction(methodName);
		if (udf == null) {
			return null;
		}

		switch (udf.getAccessType()) {
		case userDefinedFunction.ACCESS_PUBLIC:
		case userDefinedFunction.ACCESS_REMOTE:
			return this.invokeComponentFunction(session, method, udf);

		default:
			return null;
		}
	}

	public boolean isMethodAvailable( String methodName ){
		return ( getFunction(methodName) != null );
	}
	
	/*
	 * For remote callers we want to get the return type of this method
	 */
	public cfFUNCTION.ReturnFormat getReturnFormat(javaMethodDataInterface method) throws cfmRunTimeException {
		String methodName = method.getFunctionName();
		userDefinedFunction udf = getFunction(methodName);
		if (udf == null)
			return cfFUNCTION.ReturnFormat.WDDX;

		cfFUNCTION function = udf.getParentFunction();
		return (function != null) ? function.getReturnFormat() : udf.getReturnFormat();
	}

	/*
	 * For remote callers we want to get the return the json date of this method
	 */
	public String getReturnJSONDate(javaMethodDataInterface method) throws cfmRunTimeException {
		String methodName = method.getFunctionName();
		userDefinedFunction udf = getFunction(methodName);
		if (udf == null) {
			return null;
		}

		cfFUNCTION function = udf.getParentFunction();
		return (function != null) ? function.getReturnJSONDate() : udf.getReturnJSONDate();
	}
	
	
	public String getReturnJSONCase(javaMethodDataInterface method) throws cfmRunTimeException {
		String methodName = method.getFunctionName();
		userDefinedFunction udf = getFunction(methodName);
		if (udf == null) {
			return null;
		}

		cfFUNCTION function = udf.getParentFunction();
		return (function != null) ? function.getReturnJSONCase() : udf.getReturnJSONCase();
	}
	
	
	/*
	 * For remote callers we want to determine if the SecureJSON flag has been set
	 */
	public boolean isSecureJSon(javaMethodDataInterface method) throws cfmRunTimeException {
		String methodName = method.getFunctionName();
		userDefinedFunction udf = getFunction(methodName);
		if (udf == null) {
			return false;
		}

		cfFUNCTION function = udf.getParentFunction();
		return (function != null) ? function.isSecureJSon() : false;
	}


	/*
	 * For remote callers we want to determine if the ACCESS type is REMOTE
	 */
	public boolean isRemoteable(javaMethodDataInterface method) throws cfmRunTimeException {
		String methodName = method.getFunctionName();
		userDefinedFunction udf = getFunction(methodName);
		if (udf == null) {
			return false;
		}else
			return udf.getAccessType() == userDefinedFunction.ACCESS_REMOTE;
	}


	/*
	 * For remote callers we want to determine if the VERIFYCLIENT flag has been
	 * set
	 */
	public boolean verifyClient(javaMethodDataInterface method) throws cfmRunTimeException {
		String methodName = method.getFunctionName();
		userDefinedFunction udf = getFunction(methodName);
		if (udf == null) {
			return false;
		}

		cfFUNCTION function = udf.getParentFunction();
		return (function != null) ? function.verifyClient() : false;
	}

	public cfData invokeComponentFunction(cfSession session, javaMethodDataInterface method) throws cfmRunTimeException {
		String methodName = method.getFunctionName();
		userDefinedFunction udf = getFunction(methodName);
		
		if (udf == null) {
			/*
			 * The method they were calling wasn't found, so lets see if the
			 * onMissingMethod has been defined
			 */
			udf = getFunction("onmissingmethod");
			if (udf == null) {
				throw new cfmRunTimeException(catchDataFactory.generalException(cfCatchData.TYPE_OBJECT, "errorCode.runtimeError", "cfdata.javaInvalidMethod",new String[] { methodName }));
			} else {
				method.setOnMethodMissing();
			}
		}

		switch (udf.getAccessType()) {
		case userDefinedFunction.ACCESS_PUBLIC:
		case userDefinedFunction.ACCESS_REMOTE:
			return this.invokeComponentFunction(session, method, udf);

		case userDefinedFunction.ACCESS_PRIVATE:
			cfComponentData componentData = session.getActiveComponentData(); // calling component
			if ((componentData != null) && (componentData.equals(this) || // method is being called from within THIS component
					componentData.isTypeOf(session, udf.getParentComponentName()))) { // calling CFC type is the same as or
				// subclass of the function's parent CFC
				return this.invokeComponentFunction(session, method, udf);
			}
			throw new cfmRunTimeException(catchDataFactory.generalException(cfCatchData.TYPE_OBJECT, "errorCode.runtimeError", "cfdata.javaInvalidMethod",
					new String[] { methodName }));

		case userDefinedFunction.ACCESS_PACKAGE:
			componentData = session.getActiveComponentData();
			// determine if method is being called from within a component
			if (componentData != null) {
				if (componentData.equals(this) || // method is being called from within THIS component
						isSamePackage(componentData) || // components are in the same package
						componentData.isTypeOf(session, getComponentPackagePath())) // calling component is a subclass of this component
				{
					return this.invokeComponentFunction(session, method, udf);
				}
				throw new cfmRunTimeException(catchDataFactory.generalException(cfCatchData.TYPE_OBJECT, "errorCode.runtimeError", "cfdata.javaInvalidMethod",new String[] { methodName }));
			}

			// called from a template
			// TODO: what the heck is going on below here?

			int lastSlash = -1;
			String templatePath = null;
			String componentPath = null;

			templatePath = session.getRequestFile().getCfmlURI().getRealPath(session.REQ);
			if (templatePath == null) {
				throw new cfmRunTimeException(catchDataFactory.generalException(cfCatchData.TYPE_OBJECT, "errorCode.runtimeError", "cfdata.javaInvalidMethod",new String[] { methodName }));
			}

			lastSlash = templatePath.lastIndexOf(File.separatorChar);
			if (lastSlash == -1) {
				throw new cfmRunTimeException(catchDataFactory.generalException(cfCatchData.TYPE_OBJECT, "errorCode.runtimeError", "cfdata.javaInvalidMethod",new String[] { methodName }));
			}
			templatePath = templatePath.substring(0, lastSlash);

			if (this._componentPath == null) {
				throw new cfmRunTimeException(catchDataFactory.generalException(cfCatchData.TYPE_OBJECT, "errorCode.runtimeError", "cfdata.javaInvalidMethod",new String[] { methodName }));
			}

			lastSlash = -1;
			componentPath = this._componentPath;
			lastSlash = componentPath.lastIndexOf(File.separatorChar);
			if (lastSlash == -1) {
				throw new cfmRunTimeException(catchDataFactory.generalException(cfCatchData.TYPE_OBJECT, "errorCode.runtimeError", "cfdata.javaInvalidMethod",new String[] { methodName }));
			}
			componentPath = componentPath.substring(0, lastSlash);

			if (templatePath.equals(componentPath)) {
				return this.invokeComponentFunction(session, method, udf);
			}

			throw new cfmRunTimeException(catchDataFactory.generalException(cfCatchData.TYPE_OBJECT, "errorCode.runtimeError", "cfdata.javaInvalidMethod",new String[] { methodName }));

		default: // this should never happen
			throw new cfmRunTimeException(catchDataFactory.generalException(cfCatchData.TYPE_OBJECT, "errorCode.runtimeError", "cfdata.javaInvalidMethod",new String[] { methodName }));
		}
	}

	private boolean isSamePackage(cfComponentData _otherCFC) {
		// see if the packages are the same based on component names
		if (_otherCFC.getComponentPackagePath().equals(this.getComponentPackagePath())) {
			return true;
		}

		// see if the packages are the same based on component paths
		String path = _componentPath.substring(0, _componentPath.lastIndexOf(File.separatorChar));
		if (_otherCFC._componentPath.startsWith(path)) {
			return true;
		}

		return false;
	}

	
	
	// execute a component method
	private cfData invokeComponentFunction(cfSession session, javaMethodDataInterface method, userDefinedFunction udf) throws cfmRunTimeException {
		cfData returnValue = null;

		List<cfData> args = method.getEvaluatedArguments(session.getCFContext(), true);

		if (method.isOnMethodMissing()) {
			/* Repackage it up for the onMissingMethod */
			cfData missingMethodName = new cfStringData(method.getFunctionName());
			cfData missingMethodNameArguments = new cfStructData();

			if (method instanceof cfcMethodData && ((cfcMethodData) method).hasNamedArguments()) {
				cfArgStructData argData = ((cfcMethodData) method).getNamedArguments();
				Iterator<String> it = argData.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					missingMethodNameArguments.setData(key, argData.getData(key));
				}
			} else {
				for (int x = 0; x < args.size(); x++) {
					missingMethodNameArguments.setData(String.valueOf(x + 1), args.get(x));
				}
			}

			// Set the new arguments
			args.clear();
			args.add(missingMethodName);
			args.add(missingMethodNameArguments);
		}

		// execute the function
		cfFUNCTION function = udf.getParentFunction();

		// push component information onto the session's component stack
		enterComponent(session, function);

		// call the method's UDF
		if (method instanceof cfcMethodData && ((cfcMethodData) method).hasNamedArguments() && !method.isOnMethodMissing()) {
			returnValue = udf.execute(session, ((cfcMethodData) method).getNamedArguments(), false);
		} else {
			returnValue = udf.execute(session, args, false);
		}

		// pop component information off the session's component stack
		leaveComponent(session);

		return returnValue;
	}

	public void dump(PrintWriter out) {
		dump(out, false, "", cfDUMP.TOP_DEFAULT ); // SHORT dump (no UDFs)
	}

	public void dump(java.io.PrintWriter out, String _lbl, int _top) {
		dump(out, false, _lbl, _top); // SHORT dump (no UDFs)
	}

	public void dumpLong(java.io.PrintWriter out) {
		dump(out, true, "", cfDUMP.TOP_DEFAULT ); // LONG dump includes UDFs
	}

	public void dumpLong(java.io.PrintWriter out, String _lbl, int _top) {
		dump(out, true, _lbl, _top ); // LONG dump includes UDFs
	}

	protected void dump(PrintWriter out, boolean longVersion, String _label, int _top) {
		out.write("<table class='cfdump_table_object'>");
		out.write("<th class='cfdump_th_object' colspan='2'>");
		if (_label.length() > 0) {
			out.write(_label);
			out.write(" - ");
		}
		out.write("component ");
		out.write(_componentName);
		out.write(longVersion ? " [long version]" : " [short version]");
		// out.write( " (" + this.hashCode() + ")" ); // useful for debugging
		out.write("</th>");

		// handle "this" scoped variables in a synchronized fashion
		// to protect data integrity
		Map<String, cfData> hashMap = super.getHashData();
		synchronized (hashMap) {
			dump(out, false, _top-1);
			if (longVersion) {
				dump(out, true, _top-1 );
			}
		}
		out.write("</table>");
	}

	private void dump(PrintWriter out, boolean dumpUDF, int _top ) {
		if ( _top < 1 ){
			return;
		}

		// "this" scoped variables (non-UDF variables)
		Iterator<String> itr = super.keySet().iterator();
		while (itr.hasNext()) {
			String name = itr.next();
			if ( !isProtectedScope(name)) {
				cfData datum = super.getData(name);
				if (((datum.getDataType() == cfData.CFUDFDATA) && dumpUDF) || ((datum.getDataType() != cfData.CFUDFDATA) && !dumpUDF)) {
					out.write("<tr><td class='cfdump_td_object'>");
					out.write(name);
					out.write("</td><td class='cfdump_td_value'>");
					datum.dump(out, "", _top);
					out.write("</td>");
				}
			}
		}
	}

	private boolean isProtectedScope(String scope) {
		return (scope.equalsIgnoreCase("this") || scope.equalsIgnoreCase("super"));
	}

	public String toString() {
		StringWriter out = new StringWriter();
		this.dump(new PrintWriter(out));
		return out.toString();
	}

	// this version of equals() is for use by the CFML expression engine
	public boolean equals(cfData data) {
		if (data.getDataType() == cfData.CFCOMPONENTOBJECTDATA) {
			return this.equals((Object) data);
		}
		return false;
	}

	public void dumpWDDX(int version, java.io.PrintWriter out) {
		StringWriter sw = new StringWriter();
		StringBuffer sb = sw.getBuffer();
		PrintWriter pw = new PrintWriter(sw);

		// open component's structure
		if (version > 10)
			sb.append("<s>");
		else
			sb.append("<struct>");

		// set component name
		if (version > 10) {
			sb.append("<v n='componentName'>");
			sb.append("<s>");
			sb.append(_componentName);
			sb.append("</s>");
			sb.append("</v>");

			// set "this" scope
			sb.append("<v n='instanceVariables'>");
			sb.append("<s>");
		} else {
			sb.append("<var name='componentName'>");
			sb.append("<string>");
			sb.append(_componentName);
			sb.append("</string>");
			sb.append("</var>");

			// set "this" scope
			sb.append("<var name='instanceVariables'>");
			sb.append("<struct>");
		}

		Object[] keys = super.keys();

		for (int idx = 0; idx < keys.length; idx++) {
			String key = (String) keys[idx];
			cfData datum = super.getData(key);
			byte type = datum.getDataType();
			if (type >= 1 && type <= 20) {
				if (version > 10) {
					sb.append("<v n='");
					sb.append(key);
					sb.append("'>");
					super.getData(key).dumpWDDX(version, pw);
					sb.append("</v>");
				} else {
					sb.append("<var name='");
					sb.append(key);
					sb.append("'>");
					super.getData(key).dumpWDDX(version, pw);
					sb.append("</var>");
				}
			}
		}

		if (version > 10) {
			sb.append("</s>");
			sb.append("</v>");

			// close component's structure
			sb.append("</s>");
		} else {
			sb.append("</struct>");
			sb.append("</var>");

			// close component's structure
			sb.append("</struct>");
		}

		// flush buffer to incoming printwriter
		out.write(sb.toString());
	}

}
