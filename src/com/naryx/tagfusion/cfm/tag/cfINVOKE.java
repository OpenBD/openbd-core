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
 *  README.txt @ http://openbd.org/license/README.txt
 *  
 *  http://openbd.org/
 *  $Id: cfINVOKE.java 1831 2011-11-27 16:28:49Z alan $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfWSObjectData;
import com.naryx.tagfusion.cfm.engine.cfWSParameter;
import com.naryx.tagfusion.cfm.engine.cfWSParameters;
import com.naryx.tagfusion.cfm.engine.cfWebServices;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.ws.CallParameters;
import com.naryx.tagfusion.cfm.xml.ws.dynws.DynamicWebServiceInvoker;
import com.naryx.tagfusion.cfm.xml.ws.dynws.ParameterConverter;

public class cfINVOKE extends cfINVOKECommon implements Serializable {

	static final long serialVersionUID = 1;
	static final String[] RESERVED_WORDS = { "TIMEOUT", "PROXYSERVER", "PROXYPORT", "PROXYUSER", "PROXYPASSWORD", "USERNAME", "PASSWORD" };

	public cfINVOKE() {}
	
	public java.util.Map getInfo(){
		return createInfo("system", "This tag lets you call a CFC, WebService or JavaComponent");
	}

	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
			createAttInfo( "COMPONENT", "The name of the component to call", "", false ),
			createAttInfo( "METHOD", "The name of the method from the component to call", "", false ),
			createAttInfo( "RETURNVARIABLE", "The name variable to put the result in", "", false ),
			
			createAttInfo( "WEBSERVICE", "The WSDL URL of the webservice to call", "", false ),
			createAttInfo( "TIMEOUT", "The timeout variable for the SOAP call", "", false ),
			createAttInfo( "PROXYSERVER", "The proxy server for the SOAP call", "", false ),
			createAttInfo( "PROXYPORT", "The proxy port for the SOAP call", "", false ),
			createAttInfo( "PROXYUSER", "The proxy username for the SOAP call", "", false ),
			createAttInfo( "PROXYPASSWORD", "The proxy password for the SOAP call", "", false ),
			createAttInfo( "USERNAME", "The username for the SOAP call", "", false ),
			createAttInfo( "PASSWORD", "The password for the SOAP call", "", false ),
			createAttInfo( "SERVICEPORT", "The port name for the SOAP call", "", false ),
			createAttInfo( "ARGUMENTCOLLECTION", "The structure that holds all the arguments", "", false )
		};
	}


	protected void renderWebService(cfStructData attributes, cfSession session) throws cfmRunTimeException {
		cfData rtn = null;
		try {
			cfData ws = getDynamic(attributes, session, "WEBSERVICE");
			
			if (ws.getDataType() == cfData.CFSTRINGDATA) {
				// must be a wsdl url
				cfWSParameters op = getArguments(attributes, session, RESERVED_WORDS);
				rtn = cfINVOKE.invokeWSMethod(session, ws.toString(), (containsAttribute(attributes, "SERVICEPORT") ? getDynamic(attributes, session, "SERVICEPORT").toString() : null), getDynamic(attributes, session, "METHOD").toString(), op);
			} else if (ws.getDataType() == cfData.CFWSOBJECTDATA) {
				cfWSParameters op = getArguments(attributes, session, RESERVED_WORDS);
				rtn = cfINVOKE.invokeWSMethod(session, ((cfWSObjectData) ws).getWSDLUrl(), ((cfWSObjectData) ws).getPortName(), getDynamic(attributes, session, "METHOD").toString(), op);
			} else
				throw newRunTimeException(catchDataFactory.invalidTagAttributeException(this, "WEBSERVICE attribute contains unsupported type"));
			
		} catch (cfmRunTimeException e) {
			throw e;
		} catch (Exception e) {
			com.nary.Debug.printStackTrace(e);
			throw newRunTimeException(catchDataFactory.tagRuntimeException(this, "Invalid web service operation.", e.getMessage()));
		}

		if (containsAttribute(attributes, "RETURNVARIABLE"))
			session.setData(getDynamic(attributes, session, "RETURNVARIABLE").toString(), rtn);
	}

	
	
	protected cfData invokeJavaMethod(cfStructData attributes, cfSession _Session, Object theObj) throws cfmRunTimeException {
		cfData rtn = null;
		cfWSParameters args = null;
		Method m = null;
		cfData methodName = getDynamic(attributes, _Session, "METHOD");
		Class<?> theKlass = theObj.getClass();

		// Get the arguments and find the method
		try {
			args = getArguments(attributes, _Session, null);
			Class<?>[] kls = new Class[args.getValues().size()];
			Iterator<Object> itr = args.getValues().iterator();
			for (int i = 0; itr.hasNext(); i++)
				kls[i] = itr.next().getClass();
			
			m = theKlass.getMethod(methodName.getString(), kls);
		} catch (NoSuchMethodException ex) {
			throw newRunTimeException(catchDataFactory.tagRuntimeException(this, "Could not execute function.", ex.getMessage()));
		}

		// Unwrap the values (if necessary)
		Object[] vals = args.getValuesArray();
		for (int i = 0; i < vals.length; i++) {
			if ((vals[i] instanceof cfJavaObjectData) && !(vals[i] instanceof cfComponentData))
				vals[i] = ((cfJavaObjectData) vals[i]).getInstance();
		}

		// Invoke the method
		try {
			// If the method is not static, make sure we have an instance
			if (!Modifier.isStatic(m.getModifiers()))
				theObj = theKlass.newInstance();
			
			rtn = tagUtils.convertToCfData(m.invoke(theObj, vals));
		} catch (InstantiationException ex) {
			throw newRunTimeException(catchDataFactory.tagRuntimeException(this, "Could not execute function.", ex.getMessage()));
		} catch (InvocationTargetException ex) {
			throw newRunTimeException(catchDataFactory.tagRuntimeException(this, "Could not execute function.", ex.getMessage()));
		} catch (IllegalAccessException ex) {
			throw newRunTimeException(catchDataFactory.tagRuntimeException(this, "Could not execute function.", ex.getMessage()));
		} catch (IllegalArgumentException ex) {
			throw newRunTimeException(catchDataFactory.tagRuntimeException(this, "Could not execute function.", ex.getMessage()));
		}
		return rtn;
	}

	
	
	public static void prepareWSObjectData(cfSession _Session, cfWSObjectData wso) throws cfmRunTimeException, Exception {
		DynamicWebServiceInvoker invoker = new DynamicWebServiceInvoker(cfWebServices.getJavaCacheDir(), new ParameterConverter());
		Object stub = invoker.getStub(wso.getWSDLUrl(), wso.getPortName(), wso.getCallParameters());
		wso.setStub(stub, invoker);
	}

	
	
	public static cfData invokeWSMethod(cfSession _Session, cfWSObjectData wso, String operationName, Object argObj) throws cfmRunTimeException {
		return invokeWSMethod(_Session, wso.getWSDLUrl(), wso.getPortName(), operationName, argObj, wso, (DynamicWebServiceInvoker) wso.getPreparedInvoker());
	}

	
	
	public static cfData invokeWSMethod(cfSession _Session, String wsdlURL, String portName, String operationName, Object argObj) throws cfmRunTimeException {
		return invokeWSMethod(_Session, wsdlURL, portName, operationName, argObj, null, null);
	}

	
	
	private static cfData invokeWSMethod(cfSession _Session, String wsdlURL, String portName, String operationName, Object argObj, cfWSObjectData wso, DynamicWebServiceInvoker invoker) throws cfmRunTimeException {
		// Create an invoker & parameter converter
		if (invoker == null)
			invoker = new DynamicWebServiceInvoker(cfWebServices.getJavaCacheDir(), new ParameterConverter());

		// Get the parameters
		CallParameters cp = null;
		if (wso != null) {
			cp = wso.getCallParameters();
		} else {
			// wsdlURL might be a web service name. If it is then lookup it's
			// associated call parameters.
			// NOTE: this is the fix for bug NA#3103.
			cp = cfWSObjectData.lookupWebServiceCallParameters(_Session, wsdlURL);

			// wsdlURL might be a web service name. If it is then lookup it's
			// associated WSDL URL.
			// NOTE: this is the fix for bug NA#3103.
			wsdlURL = cfWSObjectData.lookupWebServiceWSDL(_Session, wsdlURL);
		}

		cfWSParameter[] parms = null;
		if (argObj != null && argObj instanceof cfWSParameters) {
			parms = cfINVOKE.convertLocalArgumentData((cfWSParameters) argObj);
			cp.convertFrom(((cfWSParameters) argObj).getReservedParameters());
			if (cp.getTimeout() > 0)
				cp.setTimeout(cp.getTimeout() * 1000); // Remember the cfINVOKE parameter is in seconds, not milliseconds
			
		} else if (argObj != null && argObj instanceof List) {
			parms = cfINVOKE.convertUnnamedArgumentData((List) argObj);
		} else {
			throw cfINVOKE.newException(_Session, "Invalid web service invocation.", "No parameters specified for operation: " + operationName);
		}

		Object rtn = null;
		List outParms = new LinkedList();
		try {
			// Execute the operation
			rtn = invoker.invoke(wsdlURL, portName, cp, operationName, parms, outParms);
			// Save the response headers (if any)
			if (wso != null && invoker.getRequestXml() != null) {
				wso.setResponseHeaders(invoker.getResponseHeaders());
				wso.setRequestXml(invoker.getRequestXml());
				wso.setResponseXml(invoker.getResponseXml());
			}
		} catch (cfmRunTimeException ex) {
			throw ex;
		} catch (Throwable ex) {
			ex.printStackTrace();
			StringBuilder buffy = new StringBuilder();
			buffy.append((ex.getMessage() == null ? "" : ex.getMessage()));
			Throwable xpn = null;
			if (RemoteException.class.isAssignableFrom(ex.getClass()))
				xpn = ((RemoteException) ex).detail;
			else if (InvocationTargetException.class.isAssignableFrom(ex.getClass()))
				xpn = ((InvocationTargetException) ex).getTargetException();
			
			while (xpn != null) {
				buffy.append(System.getProperty("line.separator"));
				buffy.append(xpn.getMessage());
				if (RemoteException.class.isAssignableFrom(xpn.getClass()))
					xpn = ((RemoteException) xpn).detail;
				else if (InvocationTargetException.class.isAssignableFrom(xpn.getClass()))
					xpn = ((InvocationTargetException) xpn).getTargetException();
				else
					xpn = null;
			}
			throw cfINVOKE.newException(_Session, "Could not execute web service.", buffy.toString());
		}

		if (invoker.getRequestXml() != null) {
			// Set any out or in/out parameters into session
			try {
				Iterator itr = outParms.iterator();
				while (itr.hasNext()) {
					Object[] tuple = (Object[]) itr.next();
					cfData cfd = (cfData) invoker.getParameterConverter().toBDType(tuple[1], _Session);
					if (cfd != null)
						_Session.setData(tuple[0].toString(), cfd);
				}
			} catch (cfmRunTimeException ex) {
				if (ex.getCatchData() != null)
					ex.getCatchData().setMessage("Could not assign web service out parameter to variable. " + ex.getMessage());
				throw ex;
			}
		}

		// Return the value(s) to BD type(s)
		return (cfData) invoker.getParameterConverter().toBDType(rtn, _Session);
	}

	
	
	protected Object getNatural(cfSession _Session, cfData value, String attributeName) {
		return value;
	}
	
	

	/**
	 * Converts the cfWSParameters argument data to natural Java API objects so it
	 * can be easily passed into a C# library. Returns a List array of the
	 * parameter names and values respectively.
	 * 
	 * @param op
	 *          cfWSParameters of params to convert
	 * @return List array of the parameter names, values, and omitted flags
	 */
	private static cfWSParameter[] convertLocalArgumentData(cfWSParameters op) {
		cfWSParameter[] parms = new cfWSParameter[op.getNames().size()];
		Iterator<String> names = op.getNames().iterator();
		Iterator<Object> vals = op.getValues().iterator();
		Iterator<Boolean> omits = op.getOmitted().iterator();
		for (int i = 0; names.hasNext(); i++) {
			parms[i] = new cfWSParameter(names.next(), (cfData) vals.next(), omits.next().booleanValue());
		}
		return parms;
	}

	
	
	/**
	 * Converts the specified Vector of argument data to natural Java API objects
	 * so it can be easily passed into a C# library. Returns a List array of the
	 * parameter names and values respectively.
	 * 
	 * @param op
	 *          Vector of params to convert
	 * @return List array of the parameter names and values
	 */
	private static cfWSParameter[] convertUnnamedArgumentData(List<cfData> op) {
		cfWSParameter[] parms = new cfWSParameter[op.size()];
		Iterator<cfData> itr = op.iterator();
		for (int i = 0; itr.hasNext(); i++)
			parms[i] = new cfWSParameter(null, itr.next(), false);
		return parms;
	}

	
	
	private static cfmRunTimeException newException(cfSession _Session, String message, String detail) {
		cfCatchData catchData = new cfCatchData(_Session);
		catchData.setType(cfCatchData.TYPE_TEMPLATE);
		catchData.setErrorCode("errorCode.runtimeError");
		catchData.setDetail(detail);
		catchData.setMessage(message);
		return new cfmRunTimeException(catchData);
	}
}