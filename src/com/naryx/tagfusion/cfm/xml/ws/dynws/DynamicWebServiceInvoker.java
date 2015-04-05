/* 
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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
 */

/*
 * Created on Jan 3, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.naryx.tagfusion.cfm.xml.ws.dynws;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.holders.Holder;

import org.apache.axis.AxisFault;
import org.apache.axis.AxisProperties;
import org.apache.axis.client.Stub;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.utils.ClassUtils;
import org.w3c.dom.Document;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfWSParameter;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.ws.CallParameters;

/**
 * Invokes the web service operation specified by the <cfinvoke> or cfWSObject
 * object.
 */
public class DynamicWebServiceInvoker {
	private DynamicWebServiceStubGeneratorInterface dws = null;
	private List requestHeaders = null;
	private SOAPHeaderElement[] responseHeaders = null;
	private Document requestDoc = null;
	private Document responseDoc = null;
	private Stub currProxy = null;
	private ParameterConverter converter = null;

	/**
	 * Default constructor. Takes the path to the cache directory.
	 * 
	 * @param javaCache
	 *          path to the cache directory
	 * @param converter
	 *          ParameterConverter for converting between web service types and BD
	 *          types
	 */
	public DynamicWebServiceInvoker(String javaCache, ParameterConverter converter) {
		this.dws = (DynamicWebServiceStubGeneratorInterface)cfEngine.thisPlatform.loadClass("com.naryx.tagfusion.cfm.xml.ws.javaplatform.DynamicWebServiceStubGenerator"); 
		this.dws.setCacheDir(javaCache);
		
		this.converter = converter;
		this.requestHeaders = new LinkedList();
	}

	/**
	 * Uses the DynamicWebServiceStubGenerator to create an Axis Stub to execute.
	 * 
	 * @param wsdlURL
	 *          URL for the WSDL
	 * @param portName
	 *          specified port binding name (or null)
	 * @param callParms
	 *          proxy/connection specific settings
	 * @return Axis Stub that can execute the web service operation
	 * @throws cfmRunTimeException
	 */
	public Stub getStub(String wsdlURL, String portName, CallParameters callParms) throws cfmRunTimeException {
		if (this.currProxy == null) {
			// Generate and configure the proxy/stub
			this.currProxy = this.dws.generateStub(wsdlURL, portName, callParms);
			configureStub(this.currProxy, callParms);
		}

		return this.currProxy;
	}

	/**
	 * Configures the specified Stub with the specified CallParameters.
	 * 
	 * @param stub
	 *          Stub to configure
	 * @param callParms
	 *          CalParameters containing the values to configure the Stub
	 * @throws cfmRunTimeException
	 */
	protected void configureStub(Stub stub, CallParameters callParms) throws cfmRunTimeException {
		// Register the stub's classloader
		getParameterConverter().registerLocalClassLoader(stub.getClass().getClassLoader());

		// Set the timeout
		stub.setTimeout(callParms.getTimeout());

		// Set the credentials
		if (callParms.getUsername() != null)
			stub.setUsername(callParms.getUsername());
		if (callParms.getPassword() != null)
			stub.setPassword(callParms.getPassword());
	}

	/**
	 * Returns the ParameterConverter to help convert web service types to BD
	 * types.
	 * 
	 * @return ParameterConverter
	 */
	public ParameterConverter getParameterConverter() {
		return this.converter;
	}

	/**
	 * Adds the specified SOAP request header to the operation invocation.
	 * 
	 * @param header
	 *          SOAPHeaderElement to add
	 * @throws cfmRunTimeException
	 */
	public void addRequestHeader(SOAPHeaderElement header) throws cfmRunTimeException {
		// If this same header exists, throw an exception
		// (same meaning, identical namespace/name pair)
		for (int i = 0; i < this.requestHeaders.size(); i++) {
			SOAPHeaderElement h = (SOAPHeaderElement) this.requestHeaders.get(i);
			if ((h.getNamespaceURI() == null && header.getNamespaceURI() == null && h.getName() == null && header.getName() == null) || (h.getNamespaceURI() != null && h.getNamespaceURI().equalsIgnoreCase(header.getNamespaceURI()) && h.getName() != null && h.getName().equalsIgnoreCase(header.getName())))
				throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "SOAP header value: " + header.getNamespaceURI() + ":" + header.getName() + " already set."));
		}
		this.requestHeaders.add(header);
	}

	/**
	 * Returns the response SOAP headers.
	 * 
	 * @return response SOAP headers
	 */
	public SOAPHeaderElement[] getResponseHeaders() {
		return this.responseHeaders;
	}

	/**
	 * Returns the xml comprising the web service request.
	 * 
	 * @return xml comprising the web service request
	 */
	public Object getRequestXml() {
		return this.requestDoc;
	}

	/**
	 * Returns the xml comprising the web service response.
	 * 
	 * @return xml comprising the web service response
	 */
	public Object getResponseXml() {
		return this.responseDoc;
	}

	/**
	 * Invokes the web service operation specified and returns the response object
	 * (if any).
	 * 
	 * @param wsdlURL
	 *          URL for the web service WSDL
	 * @param portName
	 *          name of the port binding (if any)
	 * @param callParms
	 *          proxy/connection specific settings
	 * @param operationName
	 *          name of the operation to execute
	 * @param parms
	 *          user specified web service parameters
	 * @param outParms
	 *          List of out parameters for the web service operation
	 * @return response object (if any)
	 * @throws cfmRunTimeException
	 */
	public Object invoke(String wsdlURL, String portName, CallParameters callParms, String operationName, cfWSParameter[] parms, List outParms) throws cfmRunTimeException {
		String oldProxyHost = null;
		String oldProxyPort = null;
		String oldNonProxyHosts = null;
		String nonProxyHosts = null;
		String oldProxyUser = null;
		String oldProxyPassword = null;

		try {
			// Get the stub
			Stub stub = getStub(wsdlURL, portName, callParms);

			// Find an operation with the correct name and the correct number
			// of arguments (correct signature)
			OperationSearchResult searchResult = findOperation(stub, operationName, this.dws.getStubInfo(), parms);

			// Verify the operation was found
			if (searchResult.method == null) {
				// Try for a method instead
				searchResult = findMethod(stub, operationName, this.dws.getStubInfo(), parms);
			}

			// Verify the method was found
			if (searchResult.method == null) {
				// Didn't find the operation method
				throw noOperationMethodFound(operationName, searchResult, parms);
			}

			// Convert the parameters
			Object[] params = convertParameters(searchResult, outParms);

			if (searchResult.isWebServiceOperation) {
				// Add the SOAPHeaderElements (if any)
				for (int i = 0; i < this.requestHeaders.size(); i++)
					stub.setHeader((SOAPHeaderElement) this.requestHeaders.get(i));

				// Register the ClassLoader for all the classes
				DynamicCacheClassLoader cl = (DynamicCacheClassLoader) stub.getClass().getClassLoader();
				Class[] klasses = cl.findAllClasses();
				for (int i = 0; i < klasses.length; i++) {
					ClassUtils.setClassLoader(klasses[i].getName(), cl);
					ClassUtils.setClassLoader("[L" + klasses[i].getName() + ";", cl);
				}

				if (callParms.getProxyServer() != null) {
					// Set the proxy server settings
					oldProxyHost = AxisProperties.getProperty("http.proxyHost");
					System.setProperty("http.proxyHost", callParms.getProxyServer());
					oldProxyPort = AxisProperties.getProperty("http.proxyPort");
					System.setProperty("http.proxyPort", String.valueOf(callParms.getProxyPort()));
					oldNonProxyHosts = AxisProperties.getProperty("http.nonProxyHosts");
					if (oldNonProxyHosts == null)
						oldNonProxyHosts = "";
					nonProxyHosts = (oldNonProxyHosts.trim().equals("") ? "" : oldNonProxyHosts + "|");
					nonProxyHosts += "localhost|127.0.0.1";
					System.setProperty("http.nonProxyHosts", nonProxyHosts);
					if (callParms.getProxyUser() != null || callParms.getProxyPassword() != null) {
						// Set the proxy credentials
						oldProxyUser = AxisProperties.getProperty("http.proxyUser");
						System.setProperty("http.proxyUser", callParms.getProxyUser());
						oldProxyPassword = AxisProperties.getProperty("http.proxyPassword");
						System.setProperty("http.proxyPassword", callParms.getProxyPassword());
					}
				}
			}

			Object result = null;
			try {
				// Execute the method
				result = searchResult.method.invoke(stub, params);

				if (searchResult.isWebServiceOperation) {
					// Get the request, response, and headers (if any)
					this.responseHeaders = stub.getResponseHeaders();
					this.requestDoc = stub._getCall().getMessageContext().getRequestMessage().getSOAPEnvelope().getAsDocument();
					this.responseDoc = stub._getCall().getMessageContext().getResponseMessage().getSOAPEnvelope().getAsDocument();

					// Update any in/out parameters.
					Iterator itr = outParms.iterator();
					while (itr.hasNext()) {
						Object[] tuple = (Object[]) itr.next();
						int i = ((Integer) tuple[0]).intValue();
						tuple[0] = tuple[1];
						tuple[1] = params[i];
					}
				}
			} catch (Exception ex) {
				StringBuilder buffy = new StringBuilder();
				String faultString = null;
				Throwable th = ex;
				while (th != null) {
					if (th instanceof AxisFault)
						faultString = ((AxisFault) th).dumpToString();
					buffy.append(th.getMessage() + " ");
					th.printStackTrace();
					th = th.getCause();
				}
				cfCatchData catchData = catchDataFactory.generalException("errorCode.runtimeError", "Invalid web service operation. Cannot call operation " + operationName + ". Error: " + buffy.toString());
				if (faultString != null)
					catchData.setExtendedInfo(faultString);
				throw new cfmRunTimeException(catchData);
			}

			return result;
		} finally {
			if (oldProxyHost != null)
				System.setProperty("http.proxyHost", oldProxyHost);
			if (oldProxyPort != null)
				System.setProperty("http.proxyPort", oldProxyPort);
			if (oldNonProxyHosts != null)
				System.setProperty("http.nonProxyHosts", oldNonProxyHosts);
			if (oldProxyUser != null)
				System.setProperty("http.proxyUser", oldProxyUser);
			if (oldProxyPassword != null)
				System.setProperty("http.proxyPassword", oldProxyPassword);
		}
	}

	/**
	 * Returns a cfmRuntimeExcxeption suitable to throw that contains information
	 * regarding the operation not found.
	 * 
	 * @param operationName
	 *          name of the operatio we're looking for
	 * @param result
	 *          OperationSearchResult from the operation search
	 * @param parms
	 *          array of cfWSParameter parameters
	 * @return new cfmRuntimeException suitable to throw
	 */
	private cfmRunTimeException noOperationMethodFound(String operationName, OperationSearchResult result, cfWSParameter[] parms) {
		// Describe the parameter types we're looking for
		StringBuilder paramTypes = new StringBuilder("{ ");
		boolean first = true;
		for (int i = 0; i < parms.length; i++) {
			if (!first)
				paramTypes.append(", ");
			if (parms[i] == null) {
				paramTypes.append("null");
				first = false;
			} else if (!parms[i].isOmit() && parms[i].getVal() != null) {
				paramTypes.append(parms[i].getVal().getDataTypeName());
				if (parms[i].getName() != null)
					paramTypes.append(" (" + parms[i].getName() + ")");
				first = false;
			} else if (!parms[i].isOmit()) {
				paramTypes.append(parms[i].getVal().getClass().getName());
				if (parms[i].getName() != null)
					paramTypes.append(" (" + parms[i].getName() + ")");
				first = false;
			}
		}
		paramTypes.append(" }");

		// Describe the closest match we found (if any)
		StringBuilder closest = new StringBuilder();
		if (result.closestOp != null) {
			boolean userPassParams = false;
			closest.append(". The closest match found was: ");
			closest.append(result.closestOp.getName());
			closest.append("(");
			first = true;
			for (int i = 0; i < result.closestOp.getParameters().length; i++) {
				if (!first)
					closest.append(", ");
				else
					first = false;
				closest.append(result.closestOp.getParameters()[i].getName());
				if (result.closestOp.getParameters()[i].getName().equalsIgnoreCase("USERNAME") || result.closestOp.getParameters()[i].getName().equalsIgnoreCase("PASSWORD"))
					userPassParams = true;
			}
			closest.append(").");
			if (userPassParams)
				closest.append(" Be sure to specify the username and/or password arguments using " + " <CFINVOKEARGUMENT> or as an entry in a struct for <CFINVOKE>'s ARGUMENTCOLLECTION attribute.");
			if (result.closestOpMsg != null)
				closest.append(" Also please note. " + result.closestOpMsg);
		}

		// Throw the exception
		return new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Invalid web service operation. Cannot locate operation " + operationName + " that accepts parameters: " + paramTypes.toString() + closest.toString()));
	}

	/**
	 * Looks for the correct operation on the Stub that matches the specified
	 * operationName and parms. The search will attempt to match up argument names
	 * if they are specified. Additionally, for 1 arg operations, a convenience
	 * search is performed that will match individual parms to a single arg
	 * operation if the parms are named and they correspond to the names for the
	 * properties of the single argument type.
	 * 
	 * Operations with the most arguments will be attempt to be matched first
	 * using all specified parameters (including those marked omitted).
	 * 
	 * @param stub
	 *          Axis Stub to execute
	 * @param operationName
	 *          name of the operation
	 * @param stubInfo
	 *          web service WSDL operation/parameter information
	 * @param parms
	 *          user specified parameters
	 * @return results of searching for the web service operation
	 */
	private OperationSearchResult findOperation(Stub stub, String operationName, StubInfo stubInfo, cfWSParameter[] parms) {
		OperationSearchResult result = new OperationSearchResult();
		result.isWebServiceOperation = true;

		// Create a name indexed Map for easy parm access
		HashMap parmMap = new HashMap();
		int nonOmittedParmCount = 0;
		for (int i = 0; i < parms.length; i++) {
			if (!parms[i].isOmit())
				nonOmittedParmCount++;
			if (parms[i].getName() != null)
				parmMap.put(parms[i].getName().trim().toLowerCase(), parms[i]);
		}

		// Get all the operations with the correct name and order them by
		// number of parameters, descending.
		LinkedList operations = new LinkedList();
		for (int i = 0; i < stubInfo.getOperations().length; i++) {
			if (stubInfo.getOperations()[i].getName().equalsIgnoreCase(operationName))
				operations.add(stubInfo.getOperations()[i]);
		}
		Collections.sort(operations, new Comparator() {
			public int compare(Object o1, Object o2) {
				return (((StubInfo.Operation) o2).getParameters().length - ((StubInfo.Operation) o1).getParameters().length);
			}
		});

		// Examine all the operations
		Iterator itr = operations.iterator();
		while (itr.hasNext()) {
			// Try to match up the parameters
			StubInfo.Operation operation = (StubInfo.Operation) itr.next();
			StubInfo.Parameter[] parameters = operation.getParameters();
			examineOperation(result, operation, parameters, parms, (HashMap) parmMap.clone(), nonOmittedParmCount);
			if (result.foundOp != null)
				break;
		}

		if (result.foundOp == null) {
			// Examine all the 1 arg operations using the single argument's
			// type definition as the argument parameters.
			itr = operations.iterator();
			while (itr.hasNext()) {
				// Try to match up the sub parameters
				StubInfo.Operation operation = (StubInfo.Operation) itr.next();
				if (operation.getParameters().length == 1) {
					StubInfo.Parameter[] parameters = operation.getSubParameters();
					if (parameters != null && parameters.length > 0) {
						examineOperation(result, operation, parameters, parms, (HashMap) parmMap.clone(), nonOmittedParmCount);
						if (result.foundOp != null) {
							result.singleArgUnwrap = true;
							break;
						}
					}
				}
			}
		}

		if (result.foundOp != null) {
			// Get the method equivalent
			Method[] methods = stub.getClass().getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				if (Modifier.isPublic(methods[i].getModifiers()) && methods[i].getName().equalsIgnoreCase(result.foundOp.getName())) {
					int argCount = methods[i].getParameterTypes().length;
					if ((result.singleArgUnwrap && argCount == 1) || (!result.singleArgUnwrap && argCount == result.foundOp.getParameters().length)) {
						result.method = methods[i];
						break;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Looks for the correct local method (i.e. not a web service operation proxy
	 * method) on the Stub that matches the specified operationName and parms.
	 * 
	 * Operations with the most arguments will be matched first using all
	 * specified parameters.
	 * 
	 * @param stub
	 *          Axis Stub to execute
	 * @param operationName
	 *          name of the operation
	 * @param stubInfo
	 *          web service WSDL operation/parameter information
	 * @param parms
	 *          user specified parameters
	 * @return results of searching for the web service operation
	 */
	private OperationSearchResult findMethod(Stub stub, String operationName, StubInfo stubInfo, cfWSParameter[] parms) {
		OperationSearchResult result = new OperationSearchResult();
		result.isWebServiceOperation = false;

		// Get all the methods with the correct name and order them by
		// number of parameters, descending.
		Method[] methods = stub.getClass().getMethods();
		LinkedList operations = new LinkedList();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equalsIgnoreCase(operationName))
				operations.add(methods[i]);
		}
		Collections.sort(operations, new Comparator() {
			public int compare(Object o1, Object o2) {
				return (((Method) o2).getParameterTypes().length - ((Method) o1).getParameterTypes().length);
			}
		});

		// Examine all the methods
		Iterator itr = operations.iterator();
		while (itr.hasNext()) {
			// Try to match up the parameters
			Method operation = (Method) itr.next();
			examineMethod(result, operation, parms);
			if (result.method != null)
				break;
		}

		return result;
	}

	/**
	 * Examines the specified web service operation to see if it is the one that
	 * needs to be executed. Populates the OperationSearchResult with the
	 * operation information if this is indeed the correct operation.
	 * 
	 * @param result
	 *          results of searching for the web service operation
	 * @param operation
	 *          WSDL operation info
	 * @param parameters
	 *          WSDL parameters for the operation
	 * @param parms
	 *          user supplied parameters
	 * @param parmMap
	 *          Map of user supplied parameters
	 * @param nonOmittedParmCount
	 *          number of user supplied parameters that are not marked omit=true
	 */
	private void examineOperation(OperationSearchResult result, StubInfo.Operation operation, StubInfo.Parameter[] parameters, cfWSParameter[] parms, Map parmMap, int nonOmittedParmCount) {
		// Try to match up the parameters
		result.closestOp = operation;
		result.closestOpMsg = null;
		if (parameters.length == 0) {
			// No matching needed, operation is a no-arg operation so we
			// shouldn't have any parameters we need to send. Since we've
			// examined all the operations with arguments first, we know
			// we're not choosing this over an operation that has args
			// but that just all happen to be "omitted".
			if (nonOmittedParmCount == 0) {
				result.foundOp = result.closestOp;
				result.usableParms = new cfWSParameter[0];
				return;
			}
		} else if (parms.length > 0) {
			if (parms[0].getName() == null) {
				// No names for these parameters, so match up by total count.
				// All parms will be non-omitted as there's no way to specify
				// an omit=true if the names are null (i.e. via the
				// function(arg1, arg2, ... ) style).
				if (parameters.length == parms.length) {
					result.foundOp = result.closestOp;
					result.usableParms = parms;
					return;
				}
			} else {
				// Parameters are named, so try to match them up with the names
				// known to the WSDL (i.e. the StubInfo.Parameter names).
				LinkedList usableParms = new LinkedList();
				boolean correctMatch = true;
				for (int j = 0; j < parameters.length; j++) {
					// Get the parameter name
					String parmName = parameters[j].getName().trim().toLowerCase();

					cfWSParameter parm = findParameter(j, parmName, parms);
					if (parm == null) {
						// Not the right operation
						correctMatch = false;
						break;
					} else {

						if (parm.isOmit() && !parameters[j].getNillable() && !parameters[j].getOmittable()) {
							// Cannot omit this parameter
							result.closestOpMsg = "Parameter: " + parmName + " cannot be omitted according to the web service WSDL.";
							correctMatch = false;
							break;
						} else {
							// Remove it from our specified parms collection, add it to
							// our usable parms collection, and continue
							usableParms.add(parmMap.remove(parm.getName().toLowerCase()));
						}
					}
				}

				if (correctMatch) {
					// See if we found an operation that uses all the parameters
					Iterator tmpItr = parmMap.values().iterator();
					while (tmpItr.hasNext()) {
						cfWSParameter parm = (cfWSParameter) tmpItr.next();
						if (!parm.isOmit()) {
							// Didn't use all the parameters specified
							correctMatch = false;
							break;
						}
					}
				}

				if (correctMatch) {
					// We've used all the non-omitted parameters and possibly some of the
					// omitted ones (if the WSDL allows it), and we're not missing any
					// parameters.
					result.foundOp = result.closestOp;
					result.usableParms = (cfWSParameter[]) usableParms.toArray(new cfWSParameter[usableParms.size()]);
				}
			}
		}
	}

	/**
	 * Examines the specified Method to see if it is the one that needs to be
	 * executed. Populates the OperationSearchResult with the method information
	 * if this is indeed the correct method.
	 * 
	 * @param result
	 *          results of searching for the local method
	 * @param operation
	 *          Method matching the operation name
	 * @param parms
	 *          user supplied parameters
	 */
	private void examineMethod(OperationSearchResult result, Method operation, cfWSParameter[] parms) {
		Class[] parameterTypes = operation.getParameterTypes();

		// Try to match up the parameters
		if (parameterTypes.length == 0) {
			// No matching needed, method is a no-arg method so we
			// shouldn't have any parameters we need to send. Since we've
			// examined all the method with arguments first, we know
			// we're not choosing this over a method that has args.
			if (parms.length == 0) {
				result.method = operation;
				result.usableParms = new cfWSParameter[0];
				return;
			}
		} else if (parms.length > 0) {
			// No names/types for these parameters, so match up by total count
			// (i.e. via the function(arg1, arg2, ... ) style).
			if (parameterTypes.length == parms.length) {
				result.method = operation;
				result.usableParms = parms;
				return;
			}
		}
	}

	/**
	 * Converts the user specified parameters into the expected web service
	 * operation argument types and returns them as an Object array.
	 * 
	 * @param result
	 *          results of the web service operation search
	 * @param outParms
	 *          List of out parameters for the web service operation
	 * @return array of converted parameters
	 * @throws cfmRunTimeException
	 */
	private Object[] convertParameters(OperationSearchResult result, List outParms) throws cfmRunTimeException {
		// Check the arguments and get the values from each parameter
		Class[] parmKlass = result.method.getParameterTypes();
		Object[] rtn = new Object[parmKlass.length];

		if (result.singleArgUnwrap) {
			// We need to "wrap up" all the parameters as if they
			// were supplied as struct attributes (this is a convenience
			// enhancement for developers that don't read WSDL very well).
			// Note, this means there should only be a single argument
			// to the result.method.
			cfStructData struct = new cfStructData();
			for (int i = 0; i < result.usableParms.length; i++) {
				if (!result.usableParms[i].isOmit())
					struct.setData(result.usableParms[i].getName(), result.usableParms[i].getVal());
			}
			rtn[0] = convertParameter(0, result.foundOp.getParameters()[0].getName(), struct, parmKlass[0], outParms);
		} else {
			// For each method argument, find the parameter value in
			// result.usableParms and convert it. The name for method
			// argument i should be the name in the StubInfo.Operation
			// at the same position.
			for (int i = 0; i < parmKlass.length; i++) {
				String name = null;
				if (result.foundOp != null && result.foundOp.getParameters() != null)
					name = result.foundOp.getParameters()[i].getName();
				cfWSParameter parm = findParameter(i, name, result.usableParms);
				if (parm == null) {
					throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Invalid web service parameters supplied. Missing value for " + (name == null ? "position " + String.valueOf(i) : name)));
				}
				if (!parm.isOmit())
					rtn[i] = convertParameter(i, name, parm.getVal(), parmKlass[i], outParms);
			}
		}
		return rtn;
	}

	/**
	 * Returns the cfWSParameter from the array of parms with the specified name
	 * (or at the specified position).
	 * 
	 * @param methodArgNdx
	 *          position of the parameter in the web service operation
	 * @param name
	 *          name of the parameter in the web service operation
	 * @param parms
	 *          array of cfWSParameters to search
	 * @return cfWSParameter from the array of parms with the specified name (or
	 *         at the specified position)
	 */
	private cfWSParameter findParameter(int methodArgNdx, String name, cfWSParameter[] parms) {
		if (parms[0].getName() == null || name == null) {
			// No parameter names so let's just hope the array of
			// parameters matches up with the method arguments
			return parms[methodArgNdx];
		} else {
			// Find the parameter with the specified name
			for (int i = 0; i < parms.length; i++) {
				if (parms[i].getName().equalsIgnoreCase(name))
					return parms[i];
			}
		}

		return null;
	}

	/**
	 * Converts the specified user supplied parameter (val) into the target type
	 * using this instance's ParameterConverter. Populates the List of outParms in
	 * the process. Returns the converted parameter.
	 * 
	 * @param methodArgNdx
	 *          position of the parameter in the operation
	 * @param name
	 *          name of the parameter in the operation
	 * @param val
	 *          user supplied parameter
	 * @param targetClass
	 *          type to which we need to convert the user supplied parameter
	 * @param outParms
	 *          List of out parameters, we populate this list if the user
	 *          parameter is of the out or in/out type.
	 * @return converted parameter
	 * @throws cfmRunTimeException
	 */
	private Object convertParameter(int methodArgNdx, String name, cfData val, Class targetClass, List outParms) throws cfmRunTimeException {
		Object rtn = this.getParameterConverter().toWebServiceType(val, targetClass);

		// See if it's an out parameter (denoted by the Holder class)
		if (rtn != null && Holder.class.isAssignableFrom(rtn.getClass())) {
			// We'll use the value of string out parameters as the name
			// of a new variable to create with the returned (out) value.
			// If the out parameter isn't a string, then use the name of
			// that non-string out parameter as the name of the variable
			// to assign the returned (out) value.
			if (val instanceof cfStringData)
				outParms.add(new Object[] { new Integer(methodArgNdx), val.toString() });
			else if (name != null)
				outParms.add(new Object[] { new Integer(methodArgNdx), name });
		}
		return rtn;
	}

	/**
	 * Value object used to hold the results of searching for the correct web
	 * service operation.
	 */
	private class OperationSearchResult {
		public Method method = null;

		public StubInfo.Operation foundOp = null;
		public StubInfo.Operation closestOp = null;

		public String closestOpMsg = null;
		public boolean singleArgUnwrap = false;
		public cfWSParameter[] usableParms = null;
		public boolean isWebServiceOperation = false;

		public OperationSearchResult() {
		}
	}
}
