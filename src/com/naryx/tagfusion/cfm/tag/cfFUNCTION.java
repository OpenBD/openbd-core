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
 *  $Id: cfFUNCTION.java 2486 2015-01-22 03:22:37Z alan $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.aw20.security.MD5;
import org.aw20.util.StringUtil;

import ucar.unidata.util.DateUtil;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.cache.CacheFactory;
import com.naryx.tagfusion.cfm.cache.CacheInterface;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDataFactory;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmExitException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.parser.CFCall;
import com.naryx.tagfusion.cfm.parser.CFUndefinedValue;
import com.naryx.tagfusion.cfm.parser.cfLData;
import com.naryx.tagfusion.cfm.parser.indirectReferenceData;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.expression.function.getAuthUser;

public class cfFUNCTION extends cfTag implements Serializable, Cloneable {
	static final long serialVersionUID = 1;

	public enum ReturnFormat {
		PLAIN, WDDX, JSON, JSONP
	}

	public static ReturnFormat	getReturnFormat(String s){
		if ( s == null )
			return ReturnFormat.WDDX;
		else if ( s.equalsIgnoreCase("json") )
			return ReturnFormat.JSON;
		else if ( s.equalsIgnoreCase("jsonp") )
			return ReturnFormat.JSONP;
		else if ( s.equalsIgnoreCase("plain") )
			return ReturnFormat.PLAIN;
		else
			return ReturnFormat.WDDX;
	}

	public static final cfFUNCTION EMPTY_FUNCTION = new cfFUNCTION();

	public static final String ACCESS = "ACCESS";
	public static final String RETURNTYPE = "RETURNTYPE";

	private static final String NAME = "NAME";
	private static final String OUTPUT = "OUTPUT";
	private static final String SECUREJSON = "SECUREJSON";
	private static final String RETURNFORMAT = "RETURNFORMAT";
	private static final String VERIFYCLIENT = "VERIFYCLIENT";
	private static final String ROLES = "ROLES";
	private static final String TYPE = "TYPE";
	private static final String JSONDATE = "JSONDATE";
	private static final String JSONCASE = "JSONCASE";

	// TYPE attribute values
	private static final String ABSTRACT_TYPE = "abstract";
	private static final String STATIC_TYPE = "static";

	// function types
	private static final int FUNCTION = 0;
	private static final int ABSTRACT = 1;
	private static final int STATIC = 2;

	private int functionType = FUNCTION;

	private String attribName;
	private String attribReturnType;

	private ReturnFormat attribReturnFormat = ReturnFormat.WDDX;
	private String attribAccess;
	private String attribRoles;

	private boolean attribSecureJSon;
	private boolean attribOutput, attribOutputBlank;
	private boolean attribVerifyClient;

	private final List<cfStructData> formalArguments = new ArrayList<cfStructData>(5);
	private final List<String> argumentNames = new ArrayList<String>(5); // just for tracking duplicate names

	private cfStructData metaData = new cfStructData();

	// Caching methods
	private boolean	bCachingEnabled = false;

	public String getFunctionName(){
		return attribName;
	}

	// throws IllegalArgumentException in case of duplicate argument name
	public void addArgument(String argName, cfStructData argData) throws IllegalArgumentException {
		if (argumentNames.contains(argName))
			throw new IllegalArgumentException();

		argumentNames.add(argName);
		formalArguments.add(argData);
	}

	public List<cfStructData> getFormalArguments() {
		return formalArguments;
	}

	private boolean isRunning = false;

	public boolean isRunning() {
		return isRunning;
	}

	public ReturnFormat getReturnFormat() {
		return attribReturnFormat;
	}

	public String getReturnJSONDate() {
		if (containsAttribute(JSONDATE)) {
			return getConstant(JSONDATE).toLowerCase();
		} else {
			return cfEngine.DefaultJSONReturnDate;
		}
	}

	public String getReturnJSONCase() {
		if (containsAttribute(JSONCASE)) {
			return getConstant(JSONCASE).toLowerCase();
		} else {
			return cfEngine.DefaultJSONReturnCase;
		}
	}

	public boolean isSecureJSon() {
		return attribSecureJSon;
	}

	public boolean verifyClient() {
		return attribVerifyClient;
	}

	public cfStructData getMetaData() {
		return metaData;
	}

	public boolean isAbstract() {
		return (functionType == ABSTRACT);
	}

	public boolean isStatic() {
		return (functionType == STATIC);
	}

	@Override
	public String getEndMarker() {
		return "</CFFUNCTION>";
	}

	@Override
	public boolean doesTagHaveEmbeddedPoundSigns() {
		return attribOutput; // if OUTPUT="YES"
	}

	@Override
	public java.util.Map getInfo() {
		return createInfo("control", "Used to define a function within a CFC, or as a UserDefinedFunction (UDF) that can be used in the same context");
	}

	@Override
	public java.util.Map[] getAttInfo() {
		return new java.util.Map[] {
				createAttInfo("NAME", "The name for the method. Must not contain any spaces, or special characters (except for _)", "", true),
				createAttInfo("DISPLAYNAME", "The name that is used to describe this function when displayed in meta data", "", false),
				createAttInfo("ACCESS", "Determines who can access this function.  Values: private, package, public, or remote (becomes available in a SOAP or RPC call)", "public", false),
				createAttInfo("OUTPUT", "Controls whether or not this CFC can generate output", "YES", false),
				createAttInfo("ROLES", "Comma-separated list of roles that the user must be in to be able to call this function", "", false),
				createAttInfo("RETURNTYPE", "The type of data this function has to return", "any", false),
				createAttInfo("RETURNFORMAT", "When called as part of a WebService RPC, determines the return format of the data.  Values: PLAIN, WDDX, JSON, JSONP", "", false),
				createAttInfo("SECUREJSON", "When called as part of a WebService RPC, will insert this token infront of the response", "", false),
				createAttInfo("VERIFYCLIENT", "When called as part of the WebService RPC, ensures the client is using the encrypted token", "false", false),
				createAttInfo("JSONDATE", "How dates should be encoded in the outgoing JSON. Values are: LONG, HTTP, JSON, CFML. You can change the default encoding using the bluedragon.xml flag: server.system.jsondate", cfEngine.DefaultJSONReturnDate, false),
				createAttInfo("JSONCASE", "How to treat the case of the keys of outgoing JSON. Values are: lower, upper or maintain. You can change the default encoding using the bluedragon.xml flag: server.system.jsoncase", cfEngine.DefaultJSONReturnCase, false),

				createAttInfo("REGION", 			"Enable per-function caching by enabling the cache. Any valid region can be used ('function' region exists for function calls).  If enabled the cache key is calculated from the function name plus a hash of all the parameters passed in (only simple types are used in the form calculation)", "", false),
				createAttInfo("CACHEDWITHIN", "The time span to which this result will be cached for before it is reexecuted.  As decimal of a whole day (CreateTimeSpan())", "-1", false)
		};

	}

	@Override
	protected void defaultParameters(String tagName) throws cfmBadFileException {
		int parentType = -1;
		if ((parentTag != null) && (parentTag instanceof cfCOMPONENT)) {
			parentType = ((cfCOMPONENT) parentTag).getComponentType();
			if (parentType == cfCOMPONENT.INTERFACE) {
				defaultAttribute(TYPE, ABSTRACT_TYPE);
			}
		}

		parseTagHeader(tagName);

		// initialize metadata
		metaData = super.getMetaData(false);

		// default attributes
		if (!containsAttribute(RETURNTYPE)) {
			defaultAttribute(RETURNTYPE, "ANY");
		}

		// required attributes
		requiredAttribute(NAME);

		// constant attributes
		constantAttribute(NAME);
		attribName = getConstant(NAME);

		// TYPE must be "function", "abstract", or "static"
		if (containsAttribute(TYPE)) {
			constantAttribute(TYPE);
			String typeAttr = getConstant(TYPE).toLowerCase();
			if (typeAttr.equals(ABSTRACT_TYPE)) {
				functionType = ABSTRACT;
			} else if (typeAttr.equals(STATIC_TYPE)) {
				functionType = STATIC;
			}
			// for backwards compatibility, ignore other values for TYPE attribute
		}

		if (functionType == ABSTRACT) {
			if (parentType == -1) {
				// page-based UDFs may not declare abstract functions
				throw newBadFileException("Illegal Attribute", "CFFUNCTION-based UDFs may not be declared abstract");
			} else if (parentType == cfCOMPONENT.COMPONENT) {
				// CFCs of type "component" must not contain abstract functions
				throw newBadFileException("Illegal Attribute", "Components that define one or more abstract functions must specify TYPE=\"abstract\"");
			}
		} else if (parentType == cfCOMPONENT.INTERFACE) {
			// CFCs of type "interface" may only have abstract functions
			throw newBadFileException("Illegal Attribute", "All functions within components of TYPE=\"interface\" must be declared abstract");
		}

		constantAttribute(RETURNTYPE);
		attribReturnType = getConstant(RETURNTYPE);

		// RETURNFORMAT
		if (containsAttribute(RETURNFORMAT)) {
			String tmp = getConstant(RETURNFORMAT).toUpperCase();
			if (!tmp.equals("JSON") && !tmp.equals("WDDX") && !tmp.equals("PLAIN") && !tmp.equals("JSONP")) {
				throw newBadFileException("Illegal Attribute Value", "The RETURNFORMAT attribute can only have one of the following values: JSON, JSONP, WDDX, PLAIN");
			}
			attribReturnFormat = getReturnFormat(tmp);
		}

		attribSecureJSon = false;
		if (containsAttribute(SECUREJSON)) {
			constantAttribute(SECUREJSON);
			booleanAttribute(SECUREJSON);
			attribSecureJSon = getConstantAsBoolean(SECUREJSON);
		} else {
			attribSecureJSon = false;
		}

		attribVerifyClient = false;
		if (containsAttribute(VERIFYCLIENT)) {
			constantAttribute(VERIFYCLIENT);
			booleanAttribute(VERIFYCLIENT);
			attribVerifyClient = getConstantAsBoolean(VERIFYCLIENT);
		} else {
			attribVerifyClient = false;
		}

		// ACCESS attribute must have specific constant values
		if (containsAttribute(ACCESS)) {
			attribAccess = getConstant(ACCESS).toUpperCase();
			if (!attribAccess.equals("PACKAGE") && !attribAccess.equals("PRIVATE") && !attribAccess.equals("PUBLIC") && !attribAccess.equals("REMOTE")) {
				throw newBadFileException("Illegal Attribute Value", "The ACCESS attribute can only have one of the following values: PRIVATE, PACKAGE, PUBLIC, REMOTE");
			}
		} else {
			defaultAttribute(ACCESS, "");
		}

		attribAccess = getConstant(ACCESS);

		// if specified, OUTPUT attribute must be a constant convertible to a boolean

		if (containsAttribute(OUTPUT)) {
			constantAttribute(OUTPUT);
			booleanAttribute(OUTPUT);
			attribOutput = StringUtil.toBoolean(getConstant(OUTPUT), false);
		} else {
			attribOutput = false;
		}
		attribOutputBlank	= StringUtil.toBoolean(getConstant(OUTPUT), true);



		// if specified, ROLES attribute must be a constant value
		if (containsAttribute(ROLES)) {
			constantAttribute(ROLES);
		} else {
			defaultAttribute(ROLES, "");
		}

		attribRoles = getConstant(ROLES);

		// Look for caching
		if ( containsAttribute("REGION") ){
			bCachingEnabled = true;
		}

	}

	@Override
	protected void tagLoadingComplete() throws cfmBadFileException {
		// check to see that CFFUNCTION is not nested within another CFFUNCTION tag
		if (isSubordinate("CFFUNCTION", false)) {
			throw newBadFileException("Illegal Nesting", "CFFUNCTION tags cannot be nested within other CFFUNCTION tags");
		}

		// make sure CFARGUMENT tags appear first (not really needed for BD, but is compatible with CFMX)
		if (!areSubordinatesFirst("CFARGUMENT")) {
			throw newBadFileException("Illegal Tag Order", "CFARGUMENT tags must appear before any other tags in the CFFUNCTION body");
		}

		// make sure abstract functions don't have a tag body (other than CFARGUMENT tags)
		if (functionType == ABSTRACT) {
			for (int i = 0; i < childTagList.length; i++) {
				if (!(childTagList[i] instanceof cfARGUMENT))
					throw newBadFileException("Illegal CFFUNCTION Body", "Abstract functions may not define a CFFUNCTION tag body");
			}
		}

		// create userDefinedFunction class
		this.getFile().addUDF(this, new userDefinedFunction(attribName, this));

		cfArrayData argData = cfArrayData.createArray(1);
		for (int i = 0; i < this.formalArguments.size(); i++) {
			try {
				argData.addElement(formalArguments.get(i));
			} catch (cfmRunTimeException ignore) {}
		}

		metaData.setData("PARAMETERS", argData);
		if (this.isSubordinate("CFCOMPONENT"))
			((cfCOMPONENT) parentTag).addFunctionMetaData(metaData);
	}

	public String getParentComponentName() {
		if (parentTag instanceof cfCOMPONENT)
			return parentTag.getFile().getComponentName();

		return null;
	}

	public String getParentComponentPath() {
		if (parentTag instanceof cfCOMPONENT) {
			String componentName = parentTag.getFile().getComponentName();
			int lastDot = componentName.lastIndexOf(".");
			return (lastDot > 0 ? componentName.substring(0, lastDot) : "");
		}
		return null;
	}

	@Override
	public cfTagReturnType render(cfSession session) {
		// Because of UDF forward references, the cfFUNCTION.render() method might
		// not be invoked before the function is executed. Therefore, this method should
		// never do anything.
		return cfTagReturnType.NORMAL;
	}


	// "parameters" are the actual parameters, in order and unnamed
	public cfData run(cfSession session, String tagName, List<cfData> parameters, cfComponentData superScope, boolean _isLocalExec) throws cfmRunTimeException {
		boolean popFile = enterFunction(session);

		// Cache enabled?
		String cacheKey = null;
		if ( bCachingEnabled ){
			cacheKey	= attribName;
		}

		cfArgStructData actualArgs = new cfArgStructData();

		// loop through the formal arguments and validate the actual parameter values
		for (int i = 0; i < formalArguments.size(); i++) {
			cfStructData argumentAttributes = formalArguments.get(i);
			String formalName = argumentAttributes.getData(NAME).getString();
			cfData instanceValue = null;
			if (i < parameters.size())
				instanceValue = parameters.get(i);

			instanceValue = coerceArgumentType(session, argumentAttributes, formalName, instanceValue);
			if (instanceValue != null){
				actualArgs.setData(formalName, instanceValue);

				if ( bCachingEnabled )
					cacheKey += formalName + ":" + (cfData.isSimpleValue(instanceValue) ? MD5.getDigest(instanceValue.getString()) : "" );
			}
		}

		// process actual arguments in greater count than formal arguments
		for (int i = formalArguments.size(); i < parameters.size(); i++) {
			actualArgs.setData(i + 1, parameters.get(i));
		}



		// Let us now attempt to run the function after collecting up all the parameters
		CacheInterface cacheEngine = null;
		if ( bCachingEnabled ){
			cacheEngine		= CacheFactory.getCacheEngine( getConstant("REGION") );
			cfData retval	= cacheEngine.get( cacheKey );

			if ( retval != null ){
				leaveFunction(session, popFile);
				return retval;
			}
		}


		cfData retval = run(session, actualArgs, superScope, _isLocalExec, null);

		// If we are using caching then put it back
		if ( bCachingEnabled ){
			long timeOut = containsAttribute("CACHEDWITHIN") ? (long)( getDynamic(session, "cachedwithin" ).getDouble() * DateUtil.MILLIS_DAY) : -1;
			cacheEngine.set(cacheKey, retval, timeOut, timeOut);
		}


		leaveFunction(session, popFile);
		return retval;
	}


	// "parameters" are the actual named parameters
	public cfData run(cfSession session, String tagName, cfArgStructData parameters, cfComponentData superScope, boolean _isLocalExec) throws cfmRunTimeException {
		boolean popFile = enterFunction(session);

		// Cache enabled?
		String cacheKey = null;
		if ( bCachingEnabled ){
			cacheKey	= attribName;
		}

		cfArgStructData actualArgs = new cfArgStructData();

		// loop through the formal arguments and validate the actual parameter values
		for (int i = 0; i < formalArguments.size(); i++) {
			cfStructData argumentAttributes = formalArguments.get(i);
			String formalName = argumentAttributes.getData(NAME).getString();
			cfData instanceValue = parameters.removeData(formalName);
			if (instanceValue == null) {
				instanceValue = parameters.removeData(String.valueOf(i + 1));
			}

			instanceValue = coerceArgumentType(session, argumentAttributes, formalName, instanceValue);
			if (instanceValue != null) {
				actualArgs.setData(formalName, instanceValue);

				if ( bCachingEnabled )
					cacheKey += formalName + ":" + ( cfData.isSimpleValue(instanceValue) ? MD5.getDigest(instanceValue.getString()) : "" );

			} else {
				actualArgs.setData(formalName, CFUndefinedValue.UNDEFINED);
			}
		}


		Map<String, cfData> additionalNamedArgs = null;
		if (parameters.size() > 0) {
			additionalNamedArgs = new FastMap<String, cfData>();
			// process actual arguments in greater count than formal arguments
			for (int i = 0; i < parameters.size(); i++) {
				String nextKey 		= parameters.getKey(i);
				cfData nextValue 	= parameters.getData(i);

				if ( bCachingEnabled )
					cacheKey += nextKey + ":" + ( cfData.isSimpleValue(nextValue) ? MD5.getDigest(nextValue.getString()) : "" );

				actualArgs.setData(nextKey, nextValue);
				additionalNamedArgs.put(nextKey, nextValue);
			}
		}


		// Let us now attempt to run the function after collecting up all the parameters
		CacheInterface cacheEngine = null;
		if ( bCachingEnabled ){
			cacheEngine		= CacheFactory.getCacheEngine( getConstant("REGION") );
			cfData retval	= cacheEngine.get( cacheKey );

			if ( retval != null ){
				leaveFunction(session, popFile);
				return retval;
			}
		}


		// Run the query
		cfData retval = run(session, actualArgs, superScope, _isLocalExec, additionalNamedArgs);


		// If we are using caching then put it back
		if ( bCachingEnabled ){
			long timeOut = containsAttribute("CACHEDWITHIN") ? (long)( getDynamic(session, "cachedwithin" ).getDouble() * DateUtil.MILLIS_DAY) : -1;
			cacheEngine.set(cacheKey, retval, timeOut, timeOut);
		}


		// We're all finished
		leaveFunction(session, popFile);
		return retval;
	}


	public static cfData coerceArgumentType(cfSession session, String formalType, String formalName, cfData instanceValue) throws dataNotSupportedException {
		if ((formalType != null) && !formalType.equalsIgnoreCase("ANY") && !formalType.equalsIgnoreCase("VARIABLENAME")) {
			byte actualType = instanceValue.getDataType();

			switch (actualType) {
				case cfData.CFNULLDATA:
					break; // let null match anything

				case cfData.CFJAVAOBJECTDATA:
					if (formalType.equalsIgnoreCase("String")) {
						try {
							instanceValue = new cfStringData(instanceValue.getString());
						} catch (cfmRunTimeException rte) {
							throw new dataNotSupportedException();
						}

						break;
					} else {
						throw new dataNotSupportedException();
					}
				case cfData.CFCOMPONENTOBJECTDATA:
					// check to see if component is polymorphic with type specified
					if (!formalType.equalsIgnoreCase("component") && !((cfComponentData) instanceValue).isTypeOf(session, formalType) && (cfDataFactory.getDatatypeByteValue(formalType) != cfData.CFSTRUCTDATA)) {
						throw new dataNotSupportedException();
					}
					break;

				case cfData.CFSTRINGDATA:
					// check for UUID, GUID, and XML types
					if (formalType.equalsIgnoreCase("UUID")) {
						if (!tagUtils.isUUID(instanceValue))
							throw new dataNotSupportedException();
					} else if (formalType.equalsIgnoreCase("GUID")) {
						if (!tagUtils.isGUID(instanceValue))
							throw new dataNotSupportedException();
					} else if (formalType.equalsIgnoreCase("XML")) {
						try {
							if (cfXmlData.parseXml(instanceValue.getString(), true, null) == null)
								throw new dataNotSupportedException();
						} catch (cfmRunTimeException rte) {
							throw new dataNotSupportedException();
						}
					} else {
						// check the instance datatype against the formally spec'd datatype
						instanceValue = instanceValue.coerce(cfDataFactory.getDatatypeByteValue(formalType));
					}
					break;

				case cfData.CFSTRUCTDATA:
					// check for XmlNodeData, which can be validated as a string by CFMX
					if ((instanceValue instanceof cfXmlData) && ((cfDataFactory.getDatatypeByteValue(formalType) == cfData.CFSTRINGDATA) || formalType.equalsIgnoreCase(cfDataFactory.getDatatypeString((instanceValue))))) {
						break;
					}
					// else, fall through to default case

				default:
					// Issue #52 - support array of CFCs like path.to.cfc[]
					if (formalType.endsWith("[]") && actualType == cfData.CFARRAYDATA) {
						try {
							cfArrayData data = ((cfArrayData) instanceValue);
							String modifiedFormalType = formalType.substring(0, formalType.length() - 2);

							for (int i = 0; i < data.size(); i++) {
								cfData item = data.getElement(i + 1);

								// check to see if component is polymorphic with type specified
								if (item.getDataType() != cfData.CFCOMPONENTOBJECTDATA || (item.getDataType() == cfData.CFCOMPONENTOBJECTDATA && !((cfComponentData) item).isTypeOf(session, modifiedFormalType))) {
									throw new dataNotSupportedException();
								}
							}
						} catch (cfmRunTimeException rte) {
							throw new dataNotSupportedException();
						}
					} else {
						// check the instance datatype against the formally spec'd datatype
						instanceValue = instanceValue.coerce(cfDataFactory.getDatatypeByteValue(formalType));
					}
					break;
			}
		}
		return instanceValue;
	}

	private cfData coerceArgumentType(cfSession session, cfStructData argumentAttributes, String formalName, cfData instanceValue) throws cfmRunTimeException {
		String formalType = argumentAttributes.getData("TYPE").toString();

		if ((instanceValue != null) && (instanceValue != CFUndefinedValue.UNDEFINED)) {
			try {
				return coerceArgumentType(session, formalType, formalName, instanceValue);
			} catch (dataNotSupportedException e) {
				cfCatchData catchData = catchDataFactory.runtimeException(this, null);
				catchData.setMessage("The argument " + formalName.toUpperCase() + " passed to function " + attribName + "() is not of type " + formalType);
				catchData.setDetail("Function: " + attribName);
				if (formalType.equalsIgnoreCase("UUID") || formalType.equalsIgnoreCase("GUID")) {
					catchData.setExtendedInfo("Argument name: " + formalName + ", Formal type: " + formalType + ", Argument value: " + instanceValue);
				} else {
					catchData.setExtendedInfo("Argument name: " + formalName + ", Formal type: " + formalType + ", Actual type: " + cfDataFactory.getDatatypeString(instanceValue));
				}
				throw new cfmRunTimeException(catchData);
			}
		} else { // instanceValue == null (argument not provided)
			cfData defaultValue = argumentAttributes.getData("DEFAULT");
			if (defaultValue != null) { // use default if specified
				defaultValue.setExpression(true); // might contain an expression that
																					// needs to be evaluated
				instanceValue = defaultValue;
			} else {
				boolean required = argumentAttributes.getData("REQUIRED").getBoolean();
				if (required) {
					cfCatchData catchData = catchDataFactory.runtimeException(this, null);
					catchData.setMessage("Value not provided for required argument " + formalName.toUpperCase() + " of function " + attribName + "()");
					throw new cfmRunTimeException(catchData);
				}
			}
		}

		return instanceValue;
	}

	private boolean isAllowed(cfSession session) {
		if ((attribRoles == null) || attribRoles.equals("")) {
			return true;
		}

		String loginTokenValue = getAuthUser.getLoginTokenValue(session);
		if (loginTokenValue != null) {
			// get the roles defined for the current user... if there
			// are none then they are not logged in
			Map<String, String> userRoles = session.getDataFromSecurityStore(loginTokenValue);
			if (userRoles != null) {
				List<String> tokens = string.split(attribRoles, ",");
				for (int i = 0; i < tokens.size(); i++) {
					String allowedRole = tokens.get(i).toString();
					if (userRoles.containsKey(allowedRole)) {
						return true; // we found one match
					}
				}
			}
		}

		return false;
	}

	private cfData run(cfSession session, cfArgStructData actualArgs, cfComponentData superScope, boolean _isLocalExec, Map<String, cfData> _additionalNamedArgs) throws cfmRunTimeException {
		// we need to make sure to run on a separate instance to support recursive
		// calls, so that the "isRunning" attribute always has the correct value
		// see bug #1925
		try {
			cfFUNCTION clone = (cfFUNCTION) this.clone();
			session.replaceComponentTag(this, clone);
			session.replaceTag(clone);
			return clone.realRun(session, actualArgs, superScope, _isLocalExec, _additionalNamedArgs);
		} catch (CloneNotSupportedException ignore) {
			// will never happen
			return cfNullData.NULL; // keep the compiler happy
		}
	}

	private cfData realRun(cfSession session, cfArgStructData actualArgs, cfComponentData superScope, boolean _isLocalExec, Map<String, cfData> _additionalNamedArgs) throws cfmRunTimeException {
		if (functionType == ABSTRACT) {
			throw newRunTimeException("Abstract functions may not be invoked");
		}

		if (!isAllowed(session)) {
			cfCatchData catchData = catchDataFactory.runtimeException(this, null);
			catchData.setMessage("The current user was not authorized to invoke this function");
			catchData.setDetail("Function: " + attribName);
			throw new cfmRunTimeException(catchData);
		}

		// create and push variables to function stack
		CFCall call = session.enterUDF(actualArgs, superScope, _isLocalExec);
		isRunning = true;

		// copy formal (named) arguments into the function local scope
		for (int i = 0; i < formalArguments.size(); i++) {
			cfStructData argumentAttributes = formalArguments.get(i);
			String key = argumentAttributes.getData(NAME).getString();
			cfData data = actualArgs.getData(key);

			if (data != null) {
				if (data.isExpression()) { // evaluate DEFAULT attribute expressions
					cfData evaluated = evaluateDefaultAttribute(session, data.getString());
					if (evaluated != null) {
						data = evaluated;
						actualArgs.setData(key, data);
					}
				}
				if (data != CFUndefinedValue.UNDEFINED) {
					data = new indirectReferenceData(key, actualArgs, new cfStringData(key));
				}
				call.put(key, data, null);
			}
		}

		if (_additionalNamedArgs != null) {
			Iterator<String> keys = _additionalNamedArgs.keySet().iterator();
			while (keys.hasNext()) {
				String nextKey = keys.next();
				call.put(nextKey, actualArgs.getData(nextKey), null);
			}
		}

		// execute function logic
		String functionResults = null;
		cfData returnValue = null;

		// if we're within a CFOUTPUT tag or OUTPUT="YES", set processingCfOutput to
		// true
		boolean processingCfOutput = session.setProcessingCfOutput(session.getProcessingCfOutput() | attribOutput);
		boolean suppressWhiteSpace = session.setSuppressWhiteSpace(false);

		try {
			int renderOptions = cfTag.DEFAULT_OPTIONS;
			if (!processingCfOutput)
				renderOptions = renderOptions | cfTag.CF_OUTPUT_ONLY;

			if (suppressWhiteSpace)
				renderOptions = renderOptions | cfTag.SUPPRESS_WHITESPACE;

			cfTagReturnType rt = this.renderToString(session, renderOptions);
			if (rt.isReturn()) {
				returnValue = rt.getReturnValue();

				if ((returnValue != null) && (returnValue.getDataType() == cfData.CFLDATA))
					returnValue = ((cfLData) returnValue).Get(session.getCFContext());

			}

			functionResults = rt.getOutput();
		} catch (cfmExitException ee) {
			// treat CFEXIT the same as CFRETURN except no return code
			restoreTagStack(session);
			functionResults = ee.getOutput();
		} catch (cfmRunTimeException rte) {
			cfCatchData catchData = rte.getCatchData();
			catchData.addFunction(attribName + " (" + this.getFile().getName() + ", Line=" + this.posLine + ", Column=" + this.posColumn + ")");
			throw rte;
		}

		// if OUTPUT="YES" or OUTPUT=""
		if ( (attribOutput || attribOutputBlank) && (functionResults != null) && (functionResults.trim().length() > 0))
			session.forceWrite(functionResults);

		session.setSuppressWhiteSpace(suppressWhiteSpace);
		session.setProcessingCfOutput(processingCfOutput);

		// check if trying to return a value when the return type is VOID
		if ("VOID".equalsIgnoreCase(attribReturnType) && returnValue != null && returnValue.getDataType() != cfData.CFNULLDATA && !(returnValue instanceof CFUndefinedValue)) {
			cfCatchData catchData = catchDataFactory.runtimeException(this, null);
			catchData.setMessage("A value cannot be returned when the return type is VOID");
			catchData.setDetail("Function: " + attribName);
			throw new cfmRunTimeException(catchData);
		}

		try {
			returnValue = coerceReturnType(session, returnValue, attribReturnType);
		} catch (dataNotSupportedException dnse) {
			// throw error
			cfCatchData catchData = catchDataFactory.runtimeException(this, null);
			catchData.setMessage("The value returned from function " + attribName + "() is not of type " + attribReturnType);
			catchData.setDetail("Function: " + attribName);

			if (attribReturnType.equalsIgnoreCase("UUID") || attribReturnType.equalsIgnoreCase("GUID"))
				catchData.setExtendedInfo("Formal type: " + attribReturnType + ", Return value: " + returnValue);
			else
				catchData.setExtendedInfo("Formal type: " + attribReturnType + ", Actual type: " + cfDataFactory.getDatatypeString(returnValue));

			throw new cfmRunTimeException(catchData);
		}

		// restore variables
		session.leaveUDF();
		isRunning = false;

		return returnValue;
	}

	private static cfData evaluateDefaultAttribute(cfSession session, String defaultAttr) throws cfmRunTimeException {
		if (defaultAttr.length() == 0)
			return null;

		if (defaultAttr.indexOf('"') >= 0) {
			defaultAttr = "\'" + defaultAttr + "\'";
		} else {
			defaultAttr = "\"" + defaultAttr + "\"";
		}
		return runTime.runExpression(session, defaultAttr);
	}

	private boolean enterFunction(cfSession session) {
		// push file and tag stacks for error reporting
		session.pushTag(this);
		session.debugger.startFunction( this );

		cfFile thisFile = this.getFile();
		boolean pushFile = !thisFile.equals(session.activeFile());
		if (pushFile) {
			session.pushActiveFile(thisFile);
		}
		return pushFile;
	}

	private void leaveFunction(cfSession session, boolean popFile) {
		// restore file and tag stacks
		if (popFile)
			session.popActiveFile();

		session.popTag();
		session.debugger.endFunction( this );
	}

	public static cfData coerceReturnType(cfSession session, cfData returnValue, String returnType) throws dataNotSupportedException {
		if (returnValue == null || "VOID".equalsIgnoreCase(returnType))
			return cfNullData.NULL;


		if ((returnType != null) && (returnType.length() > 0) && !returnType.equalsIgnoreCase("ANY") && !returnType.equalsIgnoreCase("VARIABLENAME")) {
			switch (returnValue.getDataType()) {
				case cfData.CFNULLDATA:
					if (((cfNullData) returnValue).isJavaNull()) {
						throw new dataNotSupportedException();
					}
					break; // let non-Java null match anything

				case cfData.CFJAVAOBJECTDATA:
					// Java objects are only valid if RETURNTYPE="any"
					throw new dataNotSupportedException();

				case cfData.CFCOMPONENTOBJECTDATA:
					// check to see if component is polymorphic with type specified
					if (!returnType.equalsIgnoreCase("component") && !((cfComponentData) returnValue).isTypeOf(session, returnType) && cfDataFactory.getDatatypeByteValue(returnType) != cfData.CFSTRUCTDATA) {
						throw new dataNotSupportedException();
					}
					break;

				case cfData.CFUDFDATA:
					// check to see if component is polymorphic with type specified
					if ( !returnType.equalsIgnoreCase("function") ) {
						throw new dataNotSupportedException();
					}
					break;

				case cfData.CFSTRINGDATA:
					// check for UUID, GUID, and XML types
					if (returnType.equalsIgnoreCase("UUID")) {
						if (!tagUtils.isUUID(returnValue)) {
							throw new dataNotSupportedException();
						}
						break;
					} else if (returnType.equalsIgnoreCase("GUID")) {
						if (!tagUtils.isGUID(returnValue)) {
							throw new dataNotSupportedException();
						}
						break;
					} else if (returnType.equalsIgnoreCase("XML")) {
						try {
							if (cfXmlData.parseXml(returnValue.getString(), true, null) == null)
								throw new dataNotSupportedException();
						} catch (cfmRunTimeException e) {
							throw new dataNotSupportedException();
						}
						break;
					}


				case cfData.CFSTRUCTDATA:
					// check for XmlNodeData
					if (returnValue instanceof cfXmlData && returnType.equalsIgnoreCase("XML")) {
						break;
					}
					// else, fall through to default case

				default:
					// check the instance datatype against the formally spec'd datatype
					returnValue = returnValue.coerce(cfDataFactory.getDatatypeByteValue(returnType));
					break;
			}
		}

		return returnValue;
	}

	public void dump(PrintWriter out, String _label) {
		out.write("<table class='cfdump_table_udf' width=\"100%\">");
		out.write("<th class='cfdump_th_udf'>");
		if (_label.length() > 0)
			out.write(_label + " - ");
		out.write("function ");
		out.write(attribName);
		// out.write( " (" + this.hashCode() + ")" ); // useful for debugging
		out.write("</th>");
		out.write("<tr><td class='cfdump_td_value'><table cellspacing=2 width=\"100%\">");

		if (formalArguments.size() == 0) {
			out.write("<tr><td  class='cfdump_td_udf'>Arguments:</td>");
			out.write("<td class='cfdump_td_value'>none</td></tr>");
		} else {
			out.write("<tr><td class='cfdump_td_udf' colspan=2>Arguments:</td></tr>");
			out.write("<tr><td class='cfdump_td_udf_args' colspan=2>");
			out.write("<table class='cfdump_table_udf_args'>");
			out.write("<th class='cfdump_th_udf_args'>Name</th>");
			out.write("<th class='cfdump_th_udf_args'>Required</th>");
			out.write("<th class='cfdump_th_udf_args'>Type</th>");
			out.write("<th class='cfdump_th_udf_args'>Default</th>");

			for (int i = 0; i < formalArguments.size(); i++) {
				cfStructData arg = formalArguments.get(i);
				out.write("<tr><td class='cfdump_td_value'>" + arg.getData("NAME").toString().toLowerCase() + "</td>");
				out.write("<td class='cfdump_td_value'>" + arg.getData("REQUIRED").toString().toLowerCase() + "</td>");
				out.write("<td class='cfdump_td_value'>" + arg.getData("TYPE").toString().toLowerCase() + "</td>");
				cfData defaultValue = arg.getData("DEFAULT");
				out.write("<td class='cfdump_td_value'>" + (defaultValue == null ? "" : defaultValue.toString()) + "</td></tr>");
			}

			out.write("</table>");
			out.write("</td></tr>");
		}

		out.write("<tr><td class='cfdump_td_udf' width='50%'>Return Type:</td>");
		out.write("<td class='cfdump_td_value'>" + attribReturnType + "</td></tr>");
		out.write("<tr><td class='cfdump_td_udf' width='50%'>Roles:</td>");
		out.write("<td class='cfdump_td_value'>" + attribRoles.toLowerCase() + "</td></tr>");
		out.write("<tr><td class='cfdump_td_udf' width='50%'>Access:</td>");
		out.write("<td class='cfdump_td_value'>" + attribAccess.toLowerCase() + "</td></tr>");
		out.write("<tr><td class='cfdump_td_udf' width='50%'>Output:</td>");
		out.write("<td class='cfdump_td_value'>" + attribOutput + "</td></tr>");
		out.write("</table></td></tr>");
		out.write("</table>");
	}

	/*
	 * reInitialiseFunctionTags
	 *
	 * This method is called whenever a CFC is retrieved from the session scope and session data is
	 * being serialized. It is the same as cfTag.reInitialiseTags() except that it doesn't call
	 * tagLoadingComplete(). It avoids calling tagLoadingComplete() because that method would
	 * try to re-add the function to cfFile by calling cfFile.addUDF() causing a duplicate
	 * name exception.
	 */
	private void reInitialiseFunctionTags() {
		if (childTagList != null) {
			// Go through the child tags first of all
			for (int i = 0; i < childTagList.length; i++) {
				try {
					childTagList[i].reInitialiseTags();
				} catch (cfmBadFileException e) {
					com.nary.Debug.printStackTrace(e); // should never happen
				}
			}
		}
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		this.reInitialiseFunctionTags();
	}
}
