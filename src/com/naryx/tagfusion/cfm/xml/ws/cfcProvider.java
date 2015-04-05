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

package com.naryx.tagfusion.cfm.xml.ws;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.wsdl.OperationType;
import javax.xml.namespace.QName;
import javax.xml.rpc.holders.Holder;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.MessageContext;
import org.apache.axis.constants.Style;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.message.RPCElement;
import org.apache.axis.message.RPCHeaderParam;
import org.apache.axis.message.RPCParam;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.axis.soap.SOAPConstants;
import org.apache.axis.utils.JavaUtils;
import org.apache.axis.utils.Messages;
import org.apache.axis.wsdl.fromJava.Emitter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfEngine;

public class cfcProvider extends RPCProvider {
	private static final long serialVersionUID = 1L;

	public static final String OPTION_WSDL_BINDINGNAME = "wsdlBindingName";

	//private cfComponentData cfc = null;

	public cfcProvider(cfComponentData cfc) {
		super();
		//this.cfc = cfc;
	}

	/**
	 * Generate the WSDL for this service.
	 * 
	 * Put in the "WSDL" property of the message context as a org.w3c.dom.Document
	 * 
	 * @param msgContext
	 *          MessageContext in which to place the WSDL Document.
	 * @throws AxisFault
	 */
	public void generateWSDL(MessageContext msgContext) throws AxisFault {
		if (log.isDebugEnabled())
			log.debug("Enter: BasicProvider::generateWSDL (" + this + ")");

		/* Find the service we're invoking so we can grab it's options */
		/** ************************************************************ */
		SOAPService service = msgContext.getService();

		ServiceDesc serviceDesc = service.getInitializedServiceDesc(msgContext);

		// Calculate the appropriate namespaces for the WSDL we're going
		// to put out.
		//
		// If we've been explicitly told which namespaces to use, respect
		// that. If not:
		//
		// The "interface namespace" should be either:
		// 1) The namespace of the ServiceDesc
		// 2) The transport URL (if there's no ServiceDesc ns)

		try {
			// Location URL is whatever is explicitly set in the MC
			String locationUrl = msgContext.getStrProp(MessageContext.WSDLGEN_SERV_LOC_URL);

			if (locationUrl == null) {
				// If nothing, try what's explicitly set in the ServiceDesc
				locationUrl = serviceDesc.getEndpointURL();
			}

			if (locationUrl == null) {
				// If nothing, use the actual transport URL
				locationUrl = msgContext.getStrProp(MessageContext.TRANS_URL);
			}

			// Interface namespace is whatever is explicitly set
			String interfaceNamespace = msgContext.getStrProp(MessageContext.WSDLGEN_INTFNAMESPACE);

			if (interfaceNamespace == null) {
				// If nothing, use the default namespace of the ServiceDesc
				interfaceNamespace = serviceDesc.getDefaultNamespace();
			}

			if (interfaceNamespace == null) {
				// If nothing still, use the location URL determined above
				interfaceNamespace = locationUrl;
			}

			// Do we want to do this?
			//
			// if (locationUrl == null) {
			// locationUrl = url;
			// } else {
			// try {
			// URL urlURL = new URL(url);
			// URL locationURL = new URL(locationUrl);
			// URL urlTemp = new URL(urlURL.getProtocol(),
			// locationURL.getHost(),
			// locationURL.getPort(),
			// urlURL.getFile());
			// interfaceNamespace += urlURL.getFile();
			// locationUrl = urlTemp.toString();
			// } catch (Exception e) {
			// locationUrl = url;
			// interfaceNamespace = url;
			// }
			// }

			Emitter emitter = new Emitter();

			// This seems like a good idea, but in fact isn't because the
			// emitter will figure out a reasonable name (<classname>Service)
			// for the WSDL service element name. We provide the 'alias'
			// setting to explicitly set this name. See bug 13262 for more info.
			// emitter.setServiceElementName(serviceDesc.getName());

			// service alias may be provided if exact naming is required,
			// otherwise Axis will name it according to the implementing class name
			String alias = (String) service.getOption("alias");
			if (alias != null)
				emitter.setServiceElementName(alias);

			// Set style/use
			emitter.setStyle(serviceDesc.getStyle());
			emitter.setUse(serviceDesc.getUse());

			// Set version info
			emitter.setVersionMessage(System.getProperty("line.separator") + "WSDL created by " + cfEngine.PRODUCT_VERSION + System.getProperty("line.separator"));

			if (serviceDesc instanceof JavaServiceDesc) {
				emitter.setClsSmart(((JavaServiceDesc) serviceDesc).getImplClass(), locationUrl);
			}

			// If a wsdl target namespace was provided, use the targetNamespace.
			// Otherwise use the interfaceNamespace constructed above.
			String targetNamespace = (String) service.getOption(OPTION_WSDL_TARGETNAMESPACE);
			if (targetNamespace == null || targetNamespace.length() == 0) {
				targetNamespace = interfaceNamespace;
			}
			emitter.setIntfNamespace(targetNamespace);

			emitter.setLocationUrl(locationUrl);
			emitter.setServiceDesc(serviceDesc);
			emitter.setTypeMappingRegistry(msgContext.getTypeMappingRegistry());

			String wsdlPortType = (String) service.getOption(OPTION_WSDL_PORTTYPE);
			String wsdlServiceElement = (String) service.getOption(OPTION_WSDL_SERVICEELEMENT);
			String wsdlServicePort = (String) service.getOption(OPTION_WSDL_SERVICEPORT);
			String wsdlInputSchema = (String) service.getOption(OPTION_WSDL_INPUTSCHEMA);
			String wsdlSoapActinMode = (String) service.getOption(OPTION_WSDL_SOAPACTION_MODE);
			String extraClasses = (String) service.getOption(OPTION_EXTRACLASSES);
			String bindingName = (String) service.getOption(OPTION_WSDL_BINDINGNAME);

			if (wsdlPortType != null && wsdlPortType.length() > 0) {
				emitter.setPortTypeName(wsdlPortType);
			}
			if (wsdlServiceElement != null && wsdlServiceElement.length() > 0) {
				emitter.setServiceElementName(wsdlServiceElement);
			}
			if (wsdlServicePort != null && wsdlServicePort.length() > 0) {
				emitter.setServicePortName(wsdlServicePort);
			}
			if (wsdlInputSchema != null && wsdlInputSchema.length() > 0) {
				emitter.setInputSchema(wsdlInputSchema);
			}
			if (wsdlSoapActinMode != null && wsdlSoapActinMode.length() > 0) {
				emitter.setSoapAction(wsdlSoapActinMode);
			}
			if (bindingName != null && bindingName.length() > 0) {
				emitter.setBindingName(bindingName);
			}

			if (extraClasses != null && extraClasses.length() > 0) {
				emitter.setExtraClasses(extraClasses);
			}

			if (msgContext.isPropertyTrue(AxisEngine.PROP_EMIT_ALL_TYPES)) {
				emitter.setEmitAllTypes(true);
			}

			Document doc = emitter.emit(Emitter.MODE_ALL);

			msgContext.setProperty("WSDL", doc);
		} catch (NoClassDefFoundError e) {
			entLog.info(Messages.getMessage("toAxisFault00"), e);
			throw new AxisFault(e.toString(), e);
		} catch (Exception e) {
			entLog.info(Messages.getMessage("toAxisFault00"), e);
			throw AxisFault.makeFault(e);
		}

		if (log.isDebugEnabled())
			log.debug("Exit: BasicProvider::generateWSDL (" + this + ")");
	}

	/**
	 * Fill in a service description with the correct impl class and typemapping
	 * set. This uses methods that can be overridden by other providers (like the
	 * EJBProvider) to get the class from the right place.
	 * 
	 * @param service
	 *          SOAPService wrapper
	 * @param msgContext
	 *          MessageContext for this SOAPService
	 * @throws AxisFault
	 */
	public void initServiceDesc(SOAPService service, MessageContext msgContext) throws AxisFault {
		// Normal initialization
		super.initServiceDesc(service, msgContext);

		// Remove the IComplexObject operations
		removeIComplexObjectOps(service.getServiceDescription());
	}

	/**
	 * Removes all operations from the ServiceDesc that correspond to the
	 * IComplexObject interface. We don't want that in the WSDL.
	 * 
	 * @param sd
	 *          ServiceDesc from which we need to remove OperationDescs.
	 */
	protected void removeIComplexObjectOps(ServiceDesc sd) {
		OperationDesc[] ops = null;
		ArrayList opParms = null;

		ops = sd.getOperationsByName("bd_setFieldValues");
		for (int i = 0; i < ops.length; i++) {
			opParms = ops[i].getAllInParams();
			if (opParms.size() == 2 && ops[i].getReturnClass().equals(void.class)) {
				ParameterDesc p1 = (ParameterDesc) opParms.get(0);
				ParameterDesc p2 = (ParameterDesc) opParms.get(1);
				if ((p1.getName().equals("data") && p1.getJavaType().equals(Map.class) && p2.getName().equals("missingRequiredFieldNames") && p2.getJavaType().equals(List.class)) || ((p2.getName().equals("data") && p2.getJavaType().equals(Map.class) && p1.getName().equals("missingRequiredFieldNames") && p1.getJavaType().equals(List.class)))) {
					sd.removeOperationDesc(ops[i]);
					break;
				}
			}
		}

		ops = sd.getOperationsByName("bd_getFieldValues");
		for (int i = 0; i < ops.length; i++) {
			opParms = ops[i].getAllInParams();
			if (opParms.size() == 1 && ops[i].getReturnClass().equals(void.class)) {
				ParameterDesc p1 = (ParameterDesc) opParms.get(0);
				if (p1.getName().equals("data") && p1.getJavaType().equals(Map.class)) {
					sd.removeOperationDesc(ops[i]);
					break;
				}
			}
		}

		ops = sd.getOperationsByName("bd_getFieldTypes");
		for (int i = 0; i < ops.length; i++) {
			opParms = ops[i].getAllInParams();
			if (opParms.size() == 1 && ops[i].getReturnClass().equals(void.class)) {
				ParameterDesc p1 = (ParameterDesc) opParms.get(0);
				if (p1.getName().equals("data") && p1.getJavaType().equals(Map.class)) {
					sd.removeOperationDesc(ops[i]);
					break;
				}
			}
		}

		ops = sd.getOperationsByName("bd_getCfcName");
		for (int i = 0; i < ops.length; i++) {
			opParms = ops[i].getAllInParams();
			if ((opParms == null || opParms.size() == 0) && ops[i].getReturnClass().equals(String.class)) {
				sd.removeOperationDesc(ops[i]);
				break;
			}
		}
	}

	public void processMessage(MessageContext msgContext, SOAPEnvelope reqEnv, SOAPEnvelope resEnv, Object obj) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Enter: RPCProvider.processMessage()");
		}

		SOAPService service = msgContext.getService();
		ServiceDesc serviceDesc = service.getServiceDescription();
		OperationDesc operation = msgContext.getOperation();

		Vector bodies = reqEnv.getBodyElements();
		if (log.isDebugEnabled()) {
			log.debug(Messages.getMessage("bodyElems00", "" + bodies.size()));
			if (bodies.size() > 0) {
				log.debug(Messages.getMessage("bodyIs00", "" + bodies.get(0)));
			}
		}

		RPCElement body = null;

		// Find the first "root" body element, which is the RPC call.
		for (int bNum = 0; body == null && bNum < bodies.size(); bNum++) {
			// If this is a regular old SOAPBodyElement, and it's a root,
			// we're probably a non-wrapped doc/lit service. In this case,
			// we deserialize the element, and create an RPCElement "wrapper"
			// around it which points to the correct method.
			// FIXME : There should be a cleaner way to do this...
			if (!(bodies.get(bNum) instanceof RPCElement)) {
				SOAPBodyElement bodyEl = (SOAPBodyElement) bodies.get(bNum);
				// igors: better check if bodyEl.getID() != null
				// to make sure this loop does not step on SOAP-ENC objects
				// that follow the parameters! FIXME?
				if (bodyEl.isRoot() && operation != null && bodyEl.getID() == null) {
					ParameterDesc param = operation.getParameter(bNum);
					// at least do not step on non-existent parameters!
					if (param != null) {
						Object val = bodyEl.getValueAsType(param.getTypeQName());
						body = new RPCElement("", operation.getName(), new Object[] { val });
					}
				}
			} else {
				body = (RPCElement) bodies.get(bNum);
			}
		}

		// special case code for a document style operation with no
		// arguments (which is a strange thing to have, but whatever)
		if (body == null) {
			// throw an error if this isn't a document style service
			if (!(serviceDesc.getStyle().equals(Style.DOCUMENT))) {
				throw new Exception(Messages.getMessage("noBody00"));
			}

			// look for a method in the service that has no arguments,
			// use the first one we find.
			ArrayList ops = serviceDesc.getOperations();
			for (Iterator iterator = ops.iterator(); iterator.hasNext();) {
				OperationDesc desc = (OperationDesc) iterator.next();
				if (desc.getNumInParams() == 0) {
					// found one with no parameters, use it
					msgContext.setOperation(desc);
					// create an empty element
					body = new RPCElement(desc.getName());
					// stop looking
					break;
				}
			}

			// If we still didn't find anything, report no body error.
			if (body == null) {
				throw new Exception(Messages.getMessage("noBody00"));
			}
		}

		String methodName = body.getMethodName();
		Vector args = null;
		try {
			args = body.getParams();
		} catch (SAXException e) {
			if (e.getException() != null)
				throw e.getException();
			throw e;
		}
		int numArgs = args.size();

		// This may have changed, so get it again...
		// FIXME (there should be a cleaner way to do this)
		operation = msgContext.getOperation();

		if (operation == null) {
			QName qname = new QName(body.getNamespaceURI(), body.getName());
			try {
				// Seems to be a bug in Axis such that the client detects the
				// implementation
				// class and thus creates different operations indexed by QNames with
				// namespaces
				// whereas the document style generated WSDL doesn't have namespace
				// information.
				operation = serviceDesc.getOperationByElementQName(qname);
			} catch (Exception ex) {
				// Try to find the operation by matching only the local part of the
				// QName.
				Iterator itr = serviceDesc.getOperations().iterator();
				while (itr.hasNext()) {
					OperationDesc opDesc = (OperationDesc) itr.next();
					if (opDesc.getElementQName().getLocalPart().equals(qname.getLocalPart())) {
						// Let's try this
						operation = serviceDesc.getOperationByElementQName(opDesc.getElementQName());
						break;
					}
				}
				if (operation == null) {
					// Didn't find a matching operation. Throw the original exception.
					throw ex;
				}
			}

			if (operation == null) {
				SOAPConstants soapConstants = msgContext == null ? SOAPConstants.SOAP11_CONSTANTS : msgContext.getSOAPConstants();
				if (soapConstants == SOAPConstants.SOAP12_CONSTANTS) {
					AxisFault fault = new AxisFault(Constants.FAULT_SOAP12_SENDER, Messages.getMessage("noSuchOperation", methodName), null, null);
					fault.addFaultSubCode(Constants.FAULT_SUBCODE_PROC_NOT_PRESENT);
					throw new SAXException(fault);
				} else {
					throw new AxisFault(Constants.FAULT_CLIENT, Messages.getMessage("noSuchOperation", methodName), null, null);
				}
			} else {
				msgContext.setOperation(operation);
			}
		}

		// Create the array we'll use to hold the actual parameter
		// values. We know how big to make it from the metadata.
		Object[] argValues = new Object[operation.getNumParams()];

		// A place to keep track of the out params (INOUTs and OUTs)
		ArrayList outs = new ArrayList();

		// Put the values contained in the RPCParams into an array
		// suitable for passing to java.lang.reflect.Method.invoke()
		// Make sure we respect parameter ordering if we know about it
		// from metadata, and handle whatever conversions are necessary
		// (values -> Holders, etc)
		for (int i = 0; i < numArgs; i++) {
			RPCParam rpcParam = (RPCParam) args.get(i);
			Object value = rpcParam.getObjectValue();

			// first check the type on the paramter
			ParameterDesc paramDesc = rpcParam.getParamDesc();

			// if we found some type info try to make sure the value type is
			// correct. For instance, if we deserialized a xsd:dateTime in
			// to a Calendar and the service takes a Date, we need to convert
			if (paramDesc != null && paramDesc.getJavaType() != null) {

				// Get the type in the signature (java type or its holder)
				Class sigType = paramDesc.getJavaType();

				// Convert the value into the expected type in the signature
				value = JavaUtils.convert(value, sigType);

				rpcParam.setObjectValue(value);
				if (paramDesc.getMode() == ParameterDesc.INOUT) {
					outs.add(rpcParam);
				}
			}

			// Put the value (possibly converted) in the argument array
			// make sure to use the parameter order if we have it
			if (paramDesc == null || paramDesc.getOrder() == -1) {
				argValues[i] = value;
			} else {
				argValues[paramDesc.getOrder()] = value;
			}

			if (log.isDebugEnabled()) {
				log.debug("  " + Messages.getMessage("value00", "" + argValues[i]));
			}
		}

		// See if any subclasses want a crack at faulting on a bad operation
		// FIXME : Does this make sense here???
		String allowedMethods = (String) service.getOption("allowedMethods");
		checkMethodName(msgContext, allowedMethods, operation.getName());

		// Now create any out holders we need to pass in
		int count = numArgs;
		for (int i = 0; i < argValues.length; i++) {

			// We are interested only in OUT/INOUT
			ParameterDesc param = operation.getParameter(i);
			if (param.getMode() == ParameterDesc.IN)
				continue;

			Class holderClass = param.getJavaType();
			if (holderClass != null && Holder.class.isAssignableFrom(holderClass)) {
				int index = count;
				// Use the parameter order if specified or just stick them to the end.
				if (param.getOrder() != -1) {
					index = param.getOrder();
				} else {
					count++;
				}
				// If it's already filled, don't muck with it
				if (argValues[index] != null) {
					continue;
				}
				argValues[index] = holderClass.newInstance();
				// Store an RPCParam in the outs collection so we
				// have an easy and consistent way to write these
				// back to the client below
				RPCParam p = new RPCParam(param.getQName(), argValues[index]);
				p.setParamDesc(param);
				outs.add(p);
			} else {
				throw new AxisFault(Messages.getMessage("badOutParameter00", "" + param.getQName(), operation.getName()));
			}
		}

		// OK! Now we can invoke the method
		Object objRes = null;
		try {
			objRes = invokeMethod(msgContext, operation.getMethod(), obj, argValues);
		} catch (IllegalArgumentException e) {
			String methodSig = operation.getMethod().toString();
			String argClasses = "";
			for (int i = 0; i < argValues.length; i++) {
				if (argValues[i] == null) {
					argClasses += "null";
				} else {
					argClasses += argValues[i].getClass().getName();
				}
				if (i + 1 < argValues.length) {
					argClasses += ",";
				}
			}
			log.info(Messages.getMessage("dispatchIAE00", new String[] { methodSig, argClasses }), e);
			throw new AxisFault(Messages.getMessage("dispatchIAE00", new String[] { methodSig, argClasses }), e);
		}

		/**
		 * If this is a one-way operation, there is nothing more to do.
		 */
		if (OperationType.ONE_WAY.equals(operation.getMep()))

			return;

		/* Now put the result in the result SOAPEnvelope */
		/** ********************************************** */
		RPCElement resBody = new RPCElement(methodName + "Response");
		resBody.setPrefix(body.getPrefix());
		resBody.setNamespaceURI(body.getNamespaceURI());
		resBody.setEncodingStyle(msgContext.getEncodingStyle());

		try {
			// Return first
			if (operation.getMethod().getReturnType() != Void.TYPE) {
				QName returnQName = operation.getReturnQName();
				if (returnQName == null) {
					String nsp = body.getNamespaceURI();
					if (nsp == null || nsp.length() == 0) {
						nsp = serviceDesc.getDefaultNamespace();
					}
					returnQName = new QName(msgContext.isEncoded() ? "" : nsp, methodName + "Return");
				}

				RPCParam param = new RPCParam(returnQName, objRes);
				param.setParamDesc(operation.getReturnParamDesc());

				if (!operation.isReturnHeader()) {
					// For SOAP 1.2 rpc style, add a result
					if (msgContext.getSOAPConstants() == SOAPConstants.SOAP12_CONSTANTS && (serviceDesc.getStyle().equals(Style.RPC))) {
						RPCParam resultParam = new RPCParam(Constants.QNAME_RPC_RESULT, returnQName);
						resultParam.setXSITypeGeneration(Boolean.FALSE);
						resBody.addParam(resultParam);
					}
					resBody.addParam(param);
				} else {
					resEnv.addHeader(new RPCHeaderParam(param));
				}

			}

			// Then any other out params
			if (!outs.isEmpty()) {
				for (Iterator i = outs.iterator(); i.hasNext();) {
					// We know this has a holder, so just unwrap the value
					RPCParam param = (RPCParam) i.next();
					Holder holder = (Holder) param.getObjectValue();
					Object value = JavaUtils.getHolderValue(holder);
					ParameterDesc paramDesc = param.getParamDesc();

					param.setObjectValue(value);
					if (paramDesc != null && paramDesc.isOutHeader()) {
						resEnv.addHeader(new RPCHeaderParam(param));
					} else {
						resBody.addParam(param);
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}

		// Add the responseBody to the envelope
		resEnv.addBodyElement(resBody);

		// Axis has a bug that swallows all AxisFaults when serializing
		// the response message. This results in an empty string response
		// if something goes wrong during the serialization. To avoid this,
		// we'll serialize the responseBody here/now so that any problems
		// will be properly logged and returned as an AxisFault to the client.
		resBody.getAsString();
	}

	/**
	 * This method encapsulates the method invocation.
	 * 
	 * @param msgContext
	 *          MessageContext
	 * @param method
	 *          the target method.
	 * @param obj
	 *          the target object
	 * @param argValues
	 *          the method arguments
	 */
	protected Object invokeMethod(MessageContext msgContext, Method method, Object obj, Object[] argValues) throws Exception {
		return (method.invoke(obj, argValues));
	}

	/**
	 * Throw an AxisFault if the requested method is not allowed.
	 * 
	 * @param msgContext
	 *          MessageContext
	 * @param allowedMethods
	 *          list of allowed methods
	 * @param methodName
	 *          name of target method
	 */
	protected void checkMethodName(MessageContext msgContext, String allowedMethods, String methodName) throws Exception {
		// Our version doesn't need to do anything, though inherited
		// ones might.
	}

}
