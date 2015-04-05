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
 * Created on Oct 6, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.naryx.tagfusion.cfm.xml.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMapping;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.MessageContext;
import org.apache.axis.constants.Scope;
import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.BasicProvider;
import org.apache.axis.providers.java.JavaProvider;
import org.apache.axis.transport.http.HTTPConstants;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.ComponentFactory;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfWebServices;
import com.naryx.tagfusion.cfm.engine.cfmAccessForbiddenException;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.tag.cfCOMPONENT;
import com.naryx.tagfusion.cfm.xml.ws.dynws.CFCDescriptor;
import com.naryx.tagfusion.cfm.xml.ws.dynws.CFCInvoker;
import com.naryx.tagfusion.cfm.xml.ws.dynws.ContextRegistrar;
import com.naryx.tagfusion.cfm.xml.ws.dynws.DynamicCacheClassLoader;
import com.naryx.tagfusion.cfm.xml.ws.dynws.DynamicWebServiceTypeGenerator;
import com.naryx.tagfusion.cfm.xml.ws.encoding.ser.QueryBean;
import com.naryx.tagfusion.cfm.xml.ws.encoding.ser.StructMap;
import com.naryx.tagfusion.cfm.xml.ws.encoding.ser.StructMapItem;

public class cfcHandler extends BasicHandler {
	private static final long serialVersionUID = 1L;

	private Map soapServices = null;

	private TypeMapping tm = null;

	private DynamicWebServiceTypeGenerator gen = null;

	/**
	 * Default constructor.
	 */
	public cfcHandler() {
		// Setup caches
		this.soapServices = new FastMap();

		// Create our type generator
		this.gen = new DynamicWebServiceTypeGenerator(cfWebServices.getJavaCacheDir());
	}

	/**
	 * Invokes the web service.
	 * 
	 * @param msgContext
	 *          MessageContext for the current request.
	 */
	public void invoke(MessageContext msgContext) throws AxisFault {
		setupService(msgContext);
	}

	/**
	 * Generates and returns WSDL xml data for the web service.
	 * 
	 * @param msgContext
	 *          MessageContext for the current request.
	 */
	public void generateWSDL(MessageContext msgContext) throws AxisFault {
		setupService(msgContext);
	}

	/**
	 * Initializes the default TypeMappingRegistry.
	 * 
	 * @param msgContext
	 *          MessageContext for the current request.
	 */
	private void initializeTypeMapping(MessageContext msgContext) {
		// Initialize our TypeMapping if needed
		this.tm = msgContext.getAxisEngine().getTypeMappingRegistry().createTypeMapping();
		msgContext.getAxisEngine().getTypeMappingRegistry().register(SOAPConstants.URI_NS_SOAP_ENCODING, this.tm);
	}

	/**
	 * If our path ends in the right file extension (*.cfc), handle all the work
	 * necessary to compile the source file if it needs it, and set up the "proxy"
	 * RPC service surrounding it as the MessageContext's active service.
	 * 
	 */
	protected void setupService(MessageContext msgContext) throws AxisFault {
		// FORCE the targetService to be CFC if the URL is right.
		String realpath = msgContext.getStrProp(Constants.MC_REALPATH);
		if (realpath != null && realpath.length() > 4 && realpath.substring(realpath.length() - 4).equalsIgnoreCase(".cfc")) {
			// Reset the request stream
			resetRequestStream(msgContext);

			cfSession cfSes = getSession(msgContext);

			try {
				// Need to push the active file
				cfFile svrFile = cfSes.getRequestFile();

				// Need to run the application.cfm
				cfSes.onRequestStart(svrFile);

				// Initialize the TypeMapping if necessary
				if (this.tm == null)
					initializeTypeMapping(msgContext);

				// Setup the dynamic class
				Class kls = null;
				cfComponentData cfc = null;
				String clsName = null;
				String compName = getComponentName(svrFile);

				synchronized (DynamicCacheClassLoader.SKEL_MUTEX) {
					// Check for modifications first
					kls = DynamicCacheClassLoader.findLoadedClass(DynamicWebServiceTypeGenerator.getFQName(compName), DynamicCacheClassLoader.SKEL_CLASSES);
					if (kls != null) {
						// Validate that all the involved classes are still valid
						DynamicCacheClassLoader dcl = (DynamicCacheClassLoader) kls.getClassLoader();
						if (!dcl.areClassesValid()) {
							// Get rid of the service cache
							soapServices.clear();

							// Unregister the classes corresponding to the CFC from the TMR
							unregisterClasses(msgContext, dcl);

							// Clear the loaded classes
							dcl.invalidate();
						}
					}

					// Create the CFC
					svrFile.setComponentName(ComponentFactory.normalizeComponentName(compName));
					cfc = new cfComponentData(cfSes, svrFile);
					CFCDescriptor cfcDescriptor;
					try {
						cfcDescriptor = new CFCDescriptor(cfc.getMetaData(), cfSes);
					} catch (IllegalStateException ise) {
						// If a function is missing a returnType attribute then the
						// CFCDescriptor
						// will throw an IllegalStateException. Catch it and re-throw as
						// runtime exception.
						// This is the fix for bug #2934.
						cfCatchData cd = new cfCatchData();
						cd.setMessage("General Runtime Error");
						cd.setDetail(ise.getMessage());
						throw new cfmRunTimeException(cd);
					}
					clsName = gen.generateType(cfcDescriptor, msgContext);

					// Register the classes corresponding to the CFC in the TMR (if
					// necessary)
					registerClasses(msgContext, gen.getClassLoader(clsName));
				}

				// Register the CFCInvoker for this Thread
				CFCInvoker inv = new CFCInvoker(cfc, cfSes);
				CFCInvoker.associate(Thread.currentThread(), inv);

				ClassLoader cl = gen.getClassLoader(clsName);
				msgContext.setClassLoader(cl);

				// Create a new RPCProvider - this will be the "service"
				// that we invoke.
				SOAPService rpc = null;
				rpc = (SOAPService) soapServices.get(compName);
				if (rpc == null) {
					rpc = new SOAPService(new cfcProvider(cfc));
					rpc.setOption(JavaProvider.OPTION_CLASSNAME, clsName);
					rpc.setEngine(msgContext.getAxisEngine());

					// Support specification of "allowedMethods" as a parameter.
					String allowed = (String) getOption(JavaProvider.OPTION_ALLOWEDMETHODS);
					if (allowed == null)
						allowed = "*";
					rpc.setOption(JavaProvider.OPTION_ALLOWEDMETHODS, allowed);
					// Take the setting for the scope option from the handler
					// parameter named "scope"
					String scope = (String) getOption(JavaProvider.OPTION_SCOPE);
					if (scope == null)
						scope = Scope.DEFAULT.getName();
					rpc.setOption(JavaProvider.OPTION_SCOPE, scope);

					// Set up service description
					ServiceDesc sd = rpc.getServiceDescription();
					if (cfc.getMetaData().get(cfCOMPONENT.WSDLFILE) != null) {
						sd.setWSDLFile(cfc.getMetaData().get(cfCOMPONENT.WSDLFILE).toString().trim());
					} else {
						if (cfc.getMetaData().get(cfCOMPONENT.HINT) != null)
							sd.setDocumentation(cfc.getMetaData().get(cfCOMPONENT.HINT).toString());

						
						if (cfc.getMetaData().get(cfCOMPONENT.STYLE) != null && cfc.getMetaData().get(cfCOMPONENT.STYLE).toString().equalsIgnoreCase("document")) {
							sd.setStyle(Style.DOCUMENT);
						  sd.setUse(Use.LITERAL);
						} else if (cfc.getMetaData().get(cfCOMPONENT.STYLE) != null && cfc.getMetaData().get(cfCOMPONENT.STYLE).toString().equalsIgnoreCase("document-wrapped")) {
						  sd.setStyle(Style.WRAPPED);
						  sd.setUse(Use.LITERAL);
						} else {
						  sd.setStyle(Style.RPC);
						  sd.setUse(Use.ENCODED);
						}

						// Update any specified names/strings for the WSDL
						if (cfc.getMetaData().get(cfCOMPONENT.DISPLAYNAME) != null && !cfc.getMetaData().get(cfCOMPONENT.DISPLAYNAME).toString().trim().equals("")) {
							rpc.setOption(BasicProvider.OPTION_WSDL_SERVICEELEMENT, cfc.getMetaData().get(cfCOMPONENT.DISPLAYNAME).toString().trim());
						}
						if (cfc.getMetaData().get(cfCOMPONENT.SERVICEPORTNAME) != null && !cfc.getMetaData().get(cfCOMPONENT.SERVICEPORTNAME).toString().trim().equals("")) {
							rpc.setOption(BasicProvider.OPTION_WSDL_SERVICEPORT, cfc.getMetaData().get(cfCOMPONENT.SERVICEPORTNAME).toString().trim());
						}
						if (cfc.getMetaData().get(cfCOMPONENT.PORTTYPENAME) != null && !cfc.getMetaData().get(cfCOMPONENT.PORTTYPENAME).toString().trim().equals("")) {
							rpc.setOption(BasicProvider.OPTION_WSDL_PORTTYPE, cfc.getMetaData().get(cfCOMPONENT.PORTTYPENAME).toString().trim());
						}
						if (cfc.getMetaData().get(cfCOMPONENT.BINDINGNAME) != null && !cfc.getMetaData().get(cfCOMPONENT.BINDINGNAME).toString().trim().equals("")) {
							rpc.setOption(cfcProvider.OPTION_WSDL_BINDINGNAME, cfc.getMetaData().get(cfCOMPONENT.BINDINGNAME).toString().trim());
						}
					}

					// Update some of the namespaces
					String modLoc = null;
					if (cfc.getMetaData().get(cfCOMPONENT.NAMESPACE) != null && !cfc.getMetaData().get(cfCOMPONENT.NAMESPACE).toString().trim().equals("")) {
						modLoc = cfc.getMetaData().get(cfCOMPONENT.NAMESPACE).toString().trim();
					} else {
						HttpServletRequest req = (HttpServletRequest) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
						modLoc = req.getScheme() + ":/";
						if (req.getServletPath() == null || req.getServletPath().trim().equals(""))
							modLoc += req.getRequestURI().substring(req.getRequestURI().lastIndexOf("/"));
						else if (!req.getServletPath().startsWith("/"))
							modLoc += "/" + req.getServletPath();
						else
							modLoc += req.getServletPath();
					}
					msgContext.setProperty(MessageContext.WSDLGEN_INTFNAMESPACE, modLoc);
					rpc.setOption(BasicProvider.OPTION_WSDL_TARGETNAMESPACE, modLoc);

					// Update the TypeMappings
					sd.setTypeMappingRegistry(msgContext.getAxisEngine().getTypeMappingRegistry());
					sd.setTypeMapping(msgContext.getTypeMapping());

					// Necessary to "seed" with these types as Axis will
					// use the package as the default targetNamespace for
					// these types. The specified namespaces here correspond
					// to the the ones defined in the classes themselves.
					QName qn = null;
					qn = new QName("http://wstypes.newatlanta.com", "QueryBean");
					msgContext.getTypeMapping().register(QueryBean.class, qn, new BeanSerializerFactory(QueryBean.class, qn), new BeanDeserializerFactory(QueryBean.class, qn));
					qn = new QName("http://wstypes.newatlanta.com", "StructMap");
					msgContext.getTypeMapping().register(StructMap.class, qn, new BeanSerializerFactory(StructMap.class, qn), new BeanDeserializerFactory(StructMap.class, qn));
					qn = new QName("http://wstypes.newatlanta.com", "StructMapItem");
					msgContext.getTypeMapping().register(StructMapItem.class, qn, new BeanSerializerFactory(StructMapItem.class, qn), new BeanDeserializerFactory(StructMapItem.class, qn));
					rpc.getInitializedServiceDesc(msgContext);

					soapServices.put(compName, rpc);
				}

				// Set engine, which hooks up type mappings.
				rpc.setEngine(msgContext.getAxisEngine());

				rpc.init(); // ??

				// OK, this is now the destination service!
				msgContext.setService(rpc);

				// Process onRequestEndFile
				cfSes.onRequestEnd(svrFile.getURI());
			} catch (cfmAccessForbiddenException e) {
				processException(e, cfSes);
				throw new AxisFault("Access Forbidden", e);
			} catch (cfmBadFileException e) {
				// This exception is thrown by cfSession.getRequestFile() when the
				// request is for
				// an application.cfm or onrequestend.cfm page
				processException(e, cfSes);
				throw new AxisFault("Could not get requested file.", e);
			} catch (cfmRunTimeException e) {
				processException(e, cfSes);
				throw new AxisFault("Error processing request.", e);
			} catch (Exception e) {
				processException(e, cfSes);
				throw AxisFault.makeFault(e);
			} finally {
				cfSes.close();
			}
		}
	}

	/**
	 * Registers all the classes associated with the specified CFC generated class
	 * in the specified MessageContext's TypeMapping, for use by Axis. Assumes the
	 * specified DynamicCacheClassLoader is the one responsible for the type/class
	 * being generated and that it has already been linked/associated to dependent
	 * DynamicCacheClassLoader instances.
	 * 
	 * @param msgContext
	 * @param dcl
	 */
	private void registerClasses(MessageContext msgContext, DynamicCacheClassLoader dcl) {
		String scm = ((HttpServletRequest) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST)).getScheme();
		dcl.registerClasses(new ContextRegistrar(msgContext, scm));
	}

	/**
	 * Registers all the classes associated with the specified CFC generated class
	 * in the specified MessageContext's TypeMapping, for use by Axis. Assumes the
	 * specified DynamicCacheClassLoader is the one responsible for the type/class
	 * being generated and that it has already been linked/associated to dependent
	 * DynamicCacheClassLoader instances.
	 * 
	 * @param msgContext
	 * @param dcl
	 */
	private void unregisterClasses(MessageContext msgContext, DynamicCacheClassLoader dcl) {
		String scm = ((HttpServletRequest) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST)).getScheme();
		dcl.unregisterClasses(new ContextRegistrar(msgContext, scm));
	}

	/**
	 * Handles rendering the caught Exception in a manner suitable to the
	 * invocation context.
	 * 
	 * @param ex
	 *          Exception to handle.
	 * @param session
	 *          cfSession for the current request
	 */
	private void processException(Exception ex, cfSession session) {
		ex.printStackTrace();
		if (session != null && session.REQ.getMethod().equalsIgnoreCase("GET")) {
			if (ex instanceof cfmRunTimeException)
				((cfmRunTimeException) ex).handleException(session);
			else
				new cfmRunTimeException(session, ex).handleException(session);
		}
	}

	/**
	 * Resets the request message by writing it out completely (thereby resetting
	 * the buffer read position).
	 * 
	 * @param msgContext
	 *          MessageContext for the current request.
	 * @throws AxisFault
	 */
	private void resetRequestStream(MessageContext msgContext) throws AxisFault {
		try {
			// For some reason part of the message has been read. To
			// avoid a "missing root document element" exception, we
			// reset the message stream by writing it out to a throw
			// away buffer.
			if (msgContext != null && msgContext.getRequestMessage() != null)
				msgContext.getRequestMessage().writeTo(new ByteArrayOutputStream());
		} catch (IOException ex) {
			throw new AxisFault("Could not reset message request.", ex);
		} catch (SOAPException ex) {
			throw new AxisFault("Could not reset message request.", ex);
		}
	}

	/**
	 * Returns a new cfSession from the specified MessageContext.
	 * 
	 * @param msgContext
	 *          MessageContext for the current request.
	 * @return a new cfSession from the specified MessageContext
	 */
	private cfSession getSession(MessageContext msgContext) {
		HttpServletRequest req = (HttpServletRequest) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		HttpServletResponse res = (HttpServletResponse) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE);
		ServletContext ctxt = req.getSession(true).getServletContext();
		return new cfSession(req, res, ctxt);
	}

	/**
	 * Returns the CFC name from the cfFile representing the component.
	 * 
	 * @param svrFile
	 *          cfFile representing the component
	 * @return CFC name from the cfFile representing the component
	 */
	protected String getComponentName(cfFile svrFile) {
		String compName = svrFile.getURI();
		int ndx = compName.indexOf(':');
		if (ndx != -1) {
			// This means we have a http://servername:port/path/to/cfm string
			// (or similar). Which means the cfSession couldn't map the request
			// to a real file. It's doubtful that we'll be able to find the
			// component this way, but at least we won't be looking for a
			// component that has the protocol, server name, port, etc. in it.
			compName = compName.substring(compName.indexOf('/', ndx));
		}
		compName = compName.substring(1).replace('/', '.');
		compName = compName.replace('\\', '.');
		compName = compName.substring(0, compName.length() - 4);
		return compName;
	}

}
