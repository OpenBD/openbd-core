/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

package com.naryx.tagfusion.cfm.engine;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.tag.cfINVOKE;
import com.naryx.tagfusion.cfm.xml.ws.CallParameters;

public class cfWSObjectData extends cfJavaObjectData implements java.io.Serializable {

	static final long serialVersionUID = 1;

	private cfSession session = null;
	private Map<String, cfData> properties = null;
	private String wsdlURL = null;
	private String portName = null;
	private Object invoker = null;
	private boolean invoked = false;
	private Object requestXml = null;
	private Object responseXml = null;
	private Object responseHeaders = null;
	private Object requestHeaders = null;
	private CallParameters callParameters = null;

	/**
	 * Default constructor. Defines the WSDL url, the port name, as well as the
	 * CallParameters for accessing the WSDL URL. This constructor does not
	 * attempt to lookup the WSDL url and CallParameters from the registered web
	 * services configuration.
	 * 
	 * @param _session
	 *          current cfSession
	 * @param wsdlURL
	 *          WSDL url for the web service
	 * @param portName
	 *          port name of the port binding to use
	 * @param cp
	 *          CallParameters to access the wsdlURL
	 * @throws cfmRunTimeException
	 */
	public cfWSObjectData(cfSession _session, String wsdlURL, String portName, CallParameters cp) throws cfmRunTimeException {
		super(_session, null);
		session = _session;
		properties = new FastMap<String, cfData>();
		setPortName(portName);
		setInvoked(false);
		setWSDLUrl(wsdlURL);
		setCallParameters(cp);

		try {
			cfINVOKE.prepareWSObjectData(session, this);
		} catch (Exception e) {
			throw newException("FileSystemException", "", e.getMessage());
		}
	}

	/**
	 * Alternate constructor. Defines the WSDL url and the port name. Will attempt
	 * to lookup the WSDL url and CallParameters from the registered web services
	 * config.
	 * 
	 * @param _session
	 *          current cfSession
	 * @param wsdlURL
	 *          WSDL url for the web service
	 * @param portName
	 *          port name of the port binding to use
	 * @throws cfmRunTimeException
	 */
	public cfWSObjectData(cfSession _session, String wsdlURL, String portName) throws cfmRunTimeException {
		this(_session, lookupWebServiceWSDL(_session, wsdlURL), portName, lookupWebServiceCallParameters(_session, wsdlURL));
	}

	/**
	 * Alternate constructor. Used when no port name is specified. Will attempt to
	 * lookup the WSDL url and CallParameters from the registered web services
	 * config.
	 * 
	 * @param _session
	 *          current cfSession
	 * @param wsdlURL
	 *          WSDL url for the web service
	 * @throws cfmRunTimeException
	 */
	public cfWSObjectData(cfSession _session, String wsdlURL) throws cfmRunTimeException {
		this(_session, wsdlURL, null);
	}

	public byte getDataType() {
		return cfData.CFWSOBJECTDATA;
	}

	public String getDataTypeName() {
		return "web service object";
	}

	public void setStub(Object stub, Object invoker) {
		this.instance = stub;
		this.invoker = invoker;
	}

	public void setResponseHeaders(Object o) {
		setInvoked(true);
		this.responseHeaders = o;
	}

	public Object getResponseHeaders() {
		return this.responseHeaders;
	}

	public void setRequestHeaders(Object o) {
		this.requestHeaders = o;
	}

	public Object getRequestHeaders() {
		return this.requestHeaders;
	}

	public void setRequestXml(Object doc) {
		this.requestXml = doc;
	}

	public Object getRequestXml() {
		return this.requestXml;
	}

	public void setResponseXml(Object doc) {
		this.responseXml = doc;
	}

	public Object getResponseXml() {
		return this.responseXml;
	}

	private void setInvoked(boolean b) {
		this.invoked = b;
	}

	public boolean getInvoked() {
		return this.invoked;
	}

	public Object getPreparedInvoker() {
		return this.invoker;
	}

	/**
	 * Looks up a registered web service using the specified name. Returns the
	 * WSDL URL from the registered web service if found or the specified name if
	 * no matching registered web service was found.
	 * 
	 * @param _Session
	 *          current cfSession
	 * @param name
	 *          wsdl url or registered web service name
	 * @return wsdl url or the specified name
	 * @throws cfmRunTimeException
	 */
	public static String lookupWebServiceWSDL(cfSession _Session, String name) throws cfmRunTimeException {
		cfStructData struct = null;

		// Read the local app settings
		if (cfEngine.getConfig() != null) {
			struct = searchConfigForWebService(cfEngine.getConfig().getCFMLData(), name);
			if (struct != null)
				return struct.getData("WSDL").toString();
		}

		// Return the name as the web service WSDL URL
		return name;
	}

	/**
	 * Looks up a registered web service using the specified name. Returns the
	 * CallParameters from the registered web service if found or empty/default
	 * CallParameters if no matching registered web service was found.
	 * 
	 * @param _Session
	 *          current cfSession
	 * @param name
	 *          wsdl url or registered web service name
	 * @return CallParameters for the registered web service or empty/default
	 *         CallParameters
	 * @throws cfmRunTimeException
	 */
	public static CallParameters lookupWebServiceCallParameters(cfSession _Session, String name) throws cfmRunTimeException {
		cfStructData struct = null;
		CallParameters rtn = new CallParameters();

		// Read the local app settings
		if (cfEngine.getConfig() != null) {
			struct = searchConfigForWebService(cfEngine.getConfig().getCFMLData(), name);
			if (struct != null) {
				rtn.setUsername(struct.getData("USERNAME").toString());
				rtn.setPassword(struct.getData("PASSWORD").toString());
				return rtn;
			}
		}

		// Return the name as the web service WSDL URL
		return rtn;
	}

	/**
	 * Searches the specified config xml for the web service registered under the
	 * specifed name. Returns the cfStructData representing the web service null
	 * if not found.
	 * 
	 * @param configData
	 *          xml representation of the config file
	 * @param name
	 *          registered web service name
	 * @return cfStructData representing the registered web service or null if no
	 *         web service was found with the specified name
	 */
	private static cfStructData searchConfigForWebService(cfStructData configData, String name) {
		cfArrayData wsArray = null;

		// Read the local app settings
		if (configData != null) {
			configData = (cfStructData) configData.getData("server");
			if (configData != null) {
				configData = (cfStructData) configData.getData("webservices");
				if (configData != null) {
					wsArray = (cfArrayData) configData.getData("webservice");
					if (wsArray != null) {
						for (int i = 1; i <= wsArray.size(); i++) {
							cfStructData ws = (cfStructData) wsArray.getElement(i);
							if (ws.getData("NAME").toString().equalsIgnoreCase(name))
								return ws;
						}
					}
				}
			}
		}

		// Return nothing
		return null;
	}

	public cfData getWSData(cfData _field) throws cfmRunTimeException {
		if (_field.getDataType() == cfData.CFSTRINGDATA)
			return getWSData(((cfStringData) _field).getString());
		else
			throw new cfmRunTimeException(catchDataFactory.generalException(cfCatchData.TYPE_OBJECT, "errorCode.runtimeError", "cfdata.javaInvalidClass", null));
	}

	public cfData getWSData(String _Field) throws cfmRunTimeException {
		if (properties.containsKey(_Field))
			return properties.get(_Field);
		else
			throw newException("WSDL", "wsdl", "No such field: " + _Field);
	}

	public cfData getWSData(javaMethodDataInterface _method, CFContext _context) throws cfmRunTimeException {
		cfData returnValue = null;
		String methodName = _method.getFunctionName();
		List<cfData> args = _method.getEvaluatedArguments(_context, true);
		returnValue = invokeWSMethod(methodName, args);

		return returnValue;
	}

	public cfData invokeWSMethod(String operationName, List<cfData> arguments) throws cfmRunTimeException {
		return cfINVOKE.invokeWSMethod(session, this, operationName, arguments);
	}

	private cfmRunTimeException newException(String type, String detail, String message) {
		cfCatchData catchData = new cfCatchData(session);
		catchData.setType(type);
		catchData.setDetail(detail);
		catchData.setMessage(message);
		return new cfmRunTimeException(catchData);
	}

	public String toString() {
		return wsdlURL;
	}

	// this version of equals() is for use by the CFML expression engine
	public boolean equals(cfData _data) throws cfmRunTimeException {
		return super.equals(_data); // throws unsupported exception
	}

	// this version of equals() is for use by generic Collections classes
	public boolean equals(Object o) {
		if (o instanceof cfWSObjectData)
			return instance.equals(((cfWSObjectData) o).instance);

		return false;
	}

	public int hashCode() {
		return wsdlURL.hashCode();
	}

	// --[ Override the Serialising methods as we don't wish this object to be
	// serialised
	// --[ Causes problems when we would doing things with the client scope for
	// example.
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
	}

	/**
	 * Returns the WSDL url.
	 * 
	 * @return WSDL url
	 */
	public String getWSDLUrl() {
		return wsdlURL;
	}

	/**
	 * Sets the WSDL url.
	 * 
	 * @param url
	 *          WSDL url
	 */
	private void setWSDLUrl(String url) {
		this.wsdlURL = url;
	}

	/**
	 * Returns the port name to use.
	 * 
	 * @return port name of the port binding to use
	 */
	public String getPortName() {
		return portName;
	}

	/**
	 * Sets the port name to use.
	 * 
	 * @param portName
	 *          port name of the port binding to use
	 */
	private void setPortName(String portName) {
		this.portName = portName;
	}

	/**
	 * Returns the CallParameters for the WSDL.
	 * 
	 * @return CallParameters for the WSDL
	 */
	public CallParameters getCallParameters() {
		return callParameters;
	}

	/**
	 * Sets the CallParameters for the WSDL.
	 * 
	 * @param cp
	 *          CallParameters for the WSDL
	 */
	private void setCallParameters(CallParameters cp) {
		this.callParameters = cp;
	}
}
