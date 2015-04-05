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
 *  $Id: cfXmlData.java 2506 2015-02-08 22:25:59Z alan $
 */

package com.naryx.tagfusion.cfm.xml;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfDUMP;
import com.naryx.tagfusion.cfm.xml.parse.NoValidationResolver;
import com.naryx.tagfusion.cfm.xml.parse.ValidationErrorHandler;
import com.naryx.tagfusion.cfm.xml.parse.ValidationInputSource;
import com.naryx.tagfusion.cfm.xml.parse.ValidationResolver;
import com.naryx.tagfusion.cfm.xml.parse.ValidatorSource;
import com.naryx.tagfusion.cfm.xml.parse.XmlSource;

public class cfXmlData extends cfStructData implements Serializable {

	private static final long	serialVersionUID	= 1;

	/* Used as the flag to check if current parser is JAXP 1.3 compliant */
	private static boolean		complianceChecked	= false;

	/* Used to get the JDK version number for working around Xerces bug in JDK 1.5 */
	private static String			jdkVer						= System.getProperty("java.version");



	/**
	 * Default constructor. Performs the JAXP 1.3 compliance check if not already checked.
	 * 
	 * @param n
	 *          Node to create a new cfXmlData with
	 * @param caseSensitive
	 *          true if the cfXmlData should be caseSensitive, false otherwise
	 */
	public cfXmlData(Node n, boolean caseSensitive) throws cfmRunTimeException {
		super(XmlHashtableFactory.newXmlHashtable(n, caseSensitive));
		if (!complianceChecked)
			checkCompliance();
	}



	/**
	 * Internal constructor that bypasses the compliance check.
	 * 
	 * @param xmlData
	 *          cfXmlData that is calling this method
	 * @param n
	 *          Node to create a new cfXmlData with
	 * @param caseSensitive
	 *          true if the cfXmlData should be caseSensitive, false otherwise
	 */
	protected cfXmlData(cfXmlData xmlData, Node n, boolean caseSensitive) {
		super(XmlHashtableFactory.newXmlHashtable(n, caseSensitive));
	}



	/**
	 * Internal constructor that bypasses the compliance check.
	 * 
	 * @param xmlHashtable
	 *          XmlHashtable that is calling this method
	 * @param n
	 *          Node to create a new cfXmlData with
	 * @param caseSensitive
	 *          true if the cfXmlData should be caseSensitive, false otherwise
	 */
	protected cfXmlData(XmlHashtable xmlHashtable, Node n, boolean caseSensitive) {
		super(XmlHashtableFactory.newXmlHashtable(n, caseSensitive));
	}



	/**
	 * Checks that the underlying XML parser is JAXP 1.3 compliant. Throws a cfmRunTimeException
	 * if it is not compliant.
	 * 
	 * @throws cfmRunTimeException
	 */
	protected static void checkCompliance() throws cfmRunTimeException {
		// Verify that the XML parser is JAX 1.3 compliant
		try {
			DOMImplementation domImpl = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
			if (!DOMImplementationLS.class.isAssignableFrom(domImpl.getClass())) {
				String errMsg = "The configured XML parser does not support JAXP 1.3. Please configure the " + "JVM to use a JAXP 1.3 compliant XML parser. If using Sun Microsystems' JDK 1.5, " + "this can be done by setting the system property javax.xml.parsers.DocumentBuilderFactory " + "to \"com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl\".";
				throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", errMsg));
			}
		} catch (Exception ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		}
		complianceChecked = true;
	}



	public String getDataTypeName() {
		return "xml";
	}



	public cfData duplicate() {
		Node cn = getXMLNode().cloneNode(true);
		return new cfXmlData(this, cn, isCaseSensitive());
	}



	public void importXml(cfXmlData toAdd, boolean overwrite) throws cfmRunTimeException {
		try {
			// Get the document object (for importing)
			Document doc = null;
			if (getXMLNode().getNodeType() == Node.DOCUMENT_NODE)
				doc = (Document) getXMLNode();
			else
				doc = getXMLNode().getOwnerDocument();

			// Import the node
			Node child = doc.importNode(toAdd.getXMLNode(), true);
			if (!overwrite) {
				NodeList nl = getXMLNode().getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) {
					if (nl.item(i).getNodeName().equals(child.getNodeName()))
						return;
				}
			}
			getXMLNode().appendChild(child);
		} catch (DOMException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		}
	}



	public Node getXMLNode() {
		return ((XmlHashtable) getHashData()).getXMLNode();
	}



	/*
	 * XmlNode api that needed to be exposed for other function manipulation
	 */
	public String getName() {
		return getXMLNode().getNodeName();
	}



	public cfXmlData getParent() throws cfmRunTimeException {
		if (getXMLNode().getParentNode() != null)
			return new cfXmlData(getXMLNode().getParentNode(), isCaseSensitive());
		else
			return null;
	}



	public void removeChild(cfXmlData child) throws cfmRunTimeException {
		try {
			getXMLNode().removeChild(child.getXMLNode());
		} catch (DOMException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		}
	}



	public void insertBefore(cfXmlData child1, cfXmlData child2) throws cfmRunTimeException {
		try {
			getXMLNode().insertBefore(child1.getXMLNode(), child2.getXMLNode());
		} catch (DOMException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		}
	}



	public void appendChild(cfXmlData child) throws cfmRunTimeException {
		try {
			getXMLNode().appendChild(child.getXMLNode());
		} catch (DOMException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		}
	}



	public String getString() {
		return toString();

		// this is the original code; it was modified to support the ToString()
		// function;
		// it's not clear if this method is ever invoked from anywhere else--if
		// it is,
		// we may need to put this code back and find a different solution for
		// ToString()

		// String val = getXMLNode().getNodeValue();
		// if (val == null)
		// return "";
		// else
		// return val;
	}



	public String toString() {
		return cfXmlData.toString(getXMLNode());
	}



	public static String toString(Node n) {
		Writer writer = new StringWriter();
		Node tmp = null;
		Document doc = n.getOwnerDocument();
		if (doc == null)
			doc = (Document) n;
		DOMImplementation impl = doc.getImplementation();
		DOMImplementationLS implls = null;
		if (DOMImplementationLS.class.isAssignableFrom(impl.getClass())) {
			implls = (DOMImplementationLS) impl;
			LSOutput lsout = implls.createLSOutput();
			lsout.setCharacterStream(writer);
			try {
				// Try to avoid serializing nodes that have no children.
				// This is a patch to avoid bug XERCESJ-1023.
				// See http://issues.apache.org/jira/browse/XERCESJ-1023
				if (n.getFirstChild() == null && jdkVer.startsWith("1.5")) {
					// Add an empty node child to n so that the call to prepareForSerialization()
					// traverses down before it walks back up. This is necessary for the
					// traversal to terminate back at n. Otherwise the traversal will encounter
					// a NPE at the document's parent.
					tmp = doc.createTextNode("");
					n.appendChild(tmp);
				}
				implls.createLSSerializer().write(n, lsout);
			} finally {
				if (tmp != null) {
					// Remove the temp node.
					n.removeChild(tmp);
				}
			}
		} else if (!complianceChecked) {
			// Very few ways to get here without a compliance check
			// but it is posible, so let's log the problem and move on.
			try {
				checkCompliance();
			} catch (cfmRunTimeException ex) {
				com.nary.Debug.printStackTrace(ex);
			}
		}

		/*
		 * Old Transformer way of doing things that doesn't handle namespace fix up.
		 * writer = new StringWriter();
		 * try
		 * {
		 * TransformerFactory transformerFactory = TransformerFactory.newInstance();
		 * Transformer transformer = transformerFactory.newTransformer();
		 * transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		 * transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		 * transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		 * transformer.transform(new DOMSource(n), new StreamResult(writer));
		 * }
		 * catch (TransformerException ex)
		 * {
		 * com.nary.Debug.printStackTrace(ex);
		 * }
		 */
		return writer.toString().trim();
	}



	public void dump(PrintWriter out) {
		dump(out, "", cfDUMP.TOP_DEFAULT);
	}



	public void dump(PrintWriter out, String label, int _top) {
		((XmlHashtable) getHashData()).dump(out, label, _top);
	}



	public void dumpLong(PrintWriter out) {
		dumpLong(out, "", cfDUMP.TOP_DEFAULT);
	}



	public void dumpLong(PrintWriter out, String _label, int _top) {
		((XmlHashtable) getHashData()).dumpLong(out, _label, _top);
	}



	/**
	 * Parses and potentially validates a xml document using the specified
	 * ValidatorSource. The InputSourceGenerator produces InputSource objects that wrap
	 * the xml document. The ValiatorSource wraps the validation object (DTD/Schema).
	 * Returns a parsed and potentially validated cfXmlData instance.
	 * Note, because DTD validation occurs during parsing, and xml schema validation takes
	 * place after parsing, we need to separate the validation types into two steps.
	 * Also note, if a customHandler is specified, it may not throw any parse or
	 * validation errors/warnings. Which could lead to a null Document being returned.
	 * 
	 * @param xs
	 *          generator that creates new InputSource instances
	 * @param validator
	 *          wrapper for the validation object (DTD/Schema)
	 * @param customHandler
	 *          custom ErrorHandler, may be null
	 * @return parsed and potentially validated xml object, or null
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	protected static Document parseXml(XmlSource xs, ValidatorSource validator, ErrorHandler customHandler) throws IOException, SAXException, ParserConfigurationException {
		InputSource is = null;
		boolean schemaValidationRequired = false;
		boolean dtdRemovalRequired = false;
		EntityResolver dtdResolver = null;
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		fact.setNamespaceAware(true);
		fact.setIgnoringElementContentWhitespace(true);

		if (validator.isNull()) {
			// Return empty content for all entity references (presumably from a <!DOCTYPE ...>
			// element) so the parser will parse without any DTD validation and not complain when
			// resolving <!DOCTYPE ...> entity references (if it exists).
			is = new ValidationInputSource(xs, ValidationInputSource.NO_CHANGE);
			dtdResolver = new NoValidationResolver();
		} else if (validator.isSchema()) {
			// Return empty content for all entity references (presumably from a <!DOCTYPE ...>
			// element) so the parser will parse without any DTD validation and not complain when
			// resolving <!DOCTYPE ...> entity references (if it exists).
			is = new ValidationInputSource(xs, ValidationInputSource.NO_CHANGE);
			dtdResolver = new NoValidationResolver();
			// Note that we must do some post parse xml schema validation.
			schemaValidationRequired = true;
		} else if (validator.isEmptyString() && !xs.hasDTD()) {
			// Return empty content for all entity references (presumably from a <!DOCTYPE ...>
			// element) so the parser will parse without any DTD validation and not complain when
			// resolving <!DOCTYPE ...> entity references (if it exists).
			is = new ValidationInputSource(xs, ValidationInputSource.NO_CHANGE);
			dtdResolver = new NoValidationResolver();
			// Note that we must do some post parse xml schema validation. This assumes
			// that the xml doc has some embedded xml schema reference.
			schemaValidationRequired = true;
		} else if (validator.isEmptyString()) {
			// Best have DTD referenced in the xml source. Set DTD validation to true,
			// leave the existing <!DOCTYPE ...> element intact.
			fact.setValidating(true);
			if (customHandler == null)
				customHandler = new ValidationErrorHandler();
			is = new ValidationInputSource(xs, ValidationInputSource.NO_CHANGE);
		} else {
			// Must have specified a DTD validator object so set DTD validation
			// to true, read the <!DOCTYPE ...> element while parsing and return
			// the specified DTD validator during entity reference lookup, or if
			// no <!DOCTYPE ..> element exists, add our own so we can return the
			// specified DTD validator content during entity reference lookup.
			fact.setValidating(true);
			if (customHandler == null)
				customHandler = new ValidationErrorHandler();
			dtdRemovalRequired = !xs.hasDTD();
			ValidationResolver vr = new ValidationResolver(validator);
			dtdResolver = vr;
			is = new ValidationInputSource(xs, vr, ValidationInputSource.READ_ADD);
		}

		DocumentBuilder parser = fact.newDocumentBuilder();
		parser.setEntityResolver(dtdResolver); // if these are null, it doesn't matter,
		parser.setErrorHandler(customHandler); // setting these won't change default behavior
		Document doc = parser.parse(is);
		
		if (doc != null) {
			doc.normalize();
		
			// Now see if we need to do any schema validation
			if (schemaValidationRequired)
				validateXml(doc, validator, customHandler);
		}

		// Remove the inserted DTD (if necessary)
		if (doc != null && dtdRemovalRequired && doc.getDoctype() != null)
			doc.removeChild(doc.getDoctype());

		// Return the parsed (and possibly validated Document)
		return doc;
	}



	/**
	 * Validates the specified Document against a ValidatorSource. The ValidatorSource
	 * could represent a DTD or xml schema document. If a validation exception arises, it
	 * will be thrown.
	 * 
	 * @param doc
	 *          Document to validate
	 * @param validator
	 *          ValidatorSource to validate against
	 * @throws cfmRunTimeException
	 */
	public static void validateXml(Node node, ValidatorSource validator) throws cfmRunTimeException {
		validateXml(node, validator, (cfStructData) null);
	}



	/**
	 * Validates the specified Document against a ValidatorSource. The ValidatorSource
	 * could represent a DTD or xml schema document. If a validation exception arises, it
	 * will be thrown. However, if the validationMsgs struct is specified a custom
	 * ErrorHandler will be used during validation that collects the validation exceptions
	 * and places them in the specified validationMsgs struct. No error/warning exceptions
	 * will be thrown.
	 * 
	 * @param doc
	 *          Document to validate
	 * @param validator
	 *          ValidatorSource to validate against
	 * @param validationMsgs
	 *          cfStructData to collect all the validation messages
	 * @throws cfmRunTimeException
	 */
	public static void validateXml(Node node, ValidatorSource validator, cfStructData validationMsgs) throws cfmRunTimeException {
		ErrorHandler customHandler = null;
		if (validationMsgs != null)
			customHandler = new ValidationErrorHandler(validationMsgs);
		try {
			validateXml(node, validator, customHandler);
		} catch (IOException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		} catch (SAXParseException ex) {
			// Since we will have already received/handled this in
			// our custom ErrorHandler, we don't need to re-handle it.
			if (customHandler == null) {
				throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
			}
		} catch (SAXException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		}
	}



	/**
	 * Validates the specified Document against a ValidatorSource. The ValidatorSource
	 * could represent a DTD or xml schema document. If a validation exception arises, it
	 * will be thrown. However, if the customHandler is specified and it does not throw
	 * error/warning exceptions, it may be the case that this method does not throw
	 * validation exceptions.
	 * 
	 * @param doc
	 *          Document to validate
	 * @param validator
	 *          ValidatorSource to validate against
	 * @param customHandler
	 *          custom ErrorHandler, or null
	 * @throws IOException
	 * @throws SAXException
	 */
	protected static void validateXml(Node node, ValidatorSource validator, ErrorHandler customHandler) throws IOException, SAXException {
		SchemaFactory fact = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		if (customHandler == null)
			customHandler = new ValidationErrorHandler();
		fact.setErrorHandler(customHandler);
		Schema schema = null;

		// Either use the specified xml schema or assume the xml document
		// itself has xml schema location hints.
		if (validator.isEmptyString())
			schema = fact.newSchema();
		else
			schema = fact.newSchema(validator.getAsSource());

		// Validate it against xml schema
		Validator v = schema.newValidator();
		v.setErrorHandler(customHandler);
		v.validate(new DOMSource(node));
	}



	/**
	 * Supports parsing File instances, URL instances (that point to the xml content), and
	 * String instances (that represent the xml content itself). The validator object is
	 * also an instance of File, URL, or String. It represents a DTD or xml schema
	 * definition. Returns the parsed xml as an object.
	 * 
	 * @param value
	 *          a File, URL, or String
	 * @param caseSensitive
	 *          true if the cfXmlData is created with case sensitivity, false
	 *          otherwise
	 * @param validator
	 *          a File, URL, or String
	 * @return parsed xml as a cfXmlData object
	 * @throws cfmRunTimeException
	 */
	public static cfXmlData parseXml(Object value, boolean caseSensitive, Object validator) throws cfmRunTimeException {
		return parseXml(value, caseSensitive, validator, null);
	}



	/**
	 * Supports parsing File instances, URL instances (that point to the xml content), and
	 * String instances (that represent the xml content itself). The validator object is
	 * also an instance of File, URL, or String. It represents a DTD or xml schema
	 * definition. Returns the parsed xml as an object. If parseMsgs is specified, a
	 * custom ErrorHandler will be used during parsing and validation that will collect
	 * all the error/warning messages and add them to the specified parseMsgs struct.
	 * Note, no parsing or validation exceptions will be thrown if this parameter is
	 * non-null. Therefore the value of the return may be null.
	 * 
	 * @param value
	 *          a File, URL, or String
	 * @param caseSensitive
	 *          true if the cfXmlData is created with case sensitivity, false
	 *          otherwise
	 * @param validator
	 *          a File, URL, or String
	 * @param parseMsgs
	 *          cfStructData to collect all the parse/validation messages
	 * @return parsed xml as a cfXmlData object
	 * @throws cfmRunTimeException
	 */
	public static cfXmlData parseXml(Object value, boolean caseSensitive, Object validator, cfStructData parseMsgs) throws cfmRunTimeException {
		XmlSource isg = null;
		ValidatorSource vs = null;
		ValidationErrorHandler customHandler = null;
		String msg = null;

		if (value instanceof XmlSource)
			isg = (XmlSource) value;
		else
			isg = new XmlSource(value);
		if (validator instanceof ValidatorSource)
			vs = (ValidatorSource) validator;
		else
			vs = new ValidatorSource(validator);
		if (parseMsgs != null)
			customHandler = new ValidationErrorHandler(parseMsgs);

		Document doc = null;
		try {
			doc = parseXml(isg, vs, customHandler);
		} catch (IOException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		} catch (SAXParseException ex) {
			// Since we will have already received/handled this in
			// our custom ErrorHandler, we don't need to re-handle it.
			if (customHandler == null) {
				msg = ex.getMessage() + " Line: " + ex.getLineNumber() + " Column: " + ex.getColumnNumber();
				throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), msg, ex));
			}
		} catch (SAXException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		} catch (ParserConfigurationException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		} catch (IllegalArgumentException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		}

		// Return
		if (doc != null)
			return new cfXmlData(doc, caseSensitive);
		else
			return null;
	}



	/**
	 * Interprets a String value to determine if it represents a File path, or a URL path,
	 * or xml data (DTD/Schema data) itself. Returns a File object, or URL object, or
	 * String object accordingly.
	 * 
	 * @param _session
	 *          cfSession to determine current working directory
	 * @param str
	 *          value to interpret
	 * @return a File, URL, or String object
	 * @throws cfmRunTimeException
	 */
	public static Object interpretString(cfSession _session, String str) throws cfmRunTimeException {
		if (str == null) {
			return null;
		} else {
			str = str.trim();
			if (str.isEmpty() || str.charAt(0) == '<') {
				return str;
			} else if (str.toLowerCase().startsWith("http:") || str.toLowerCase().startsWith("https:") || str.toLowerCase().startsWith("ftp:")) {
				// Open a URL to the data
				try {
					URL url = new URL(str);
					return url;
				} catch (MalformedURLException ex) {
					throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
				}
			} else {
				// Try it as a file
				File f = new File(str);
				if (!f.exists()){
					f = new File(_session.getPresentDirectory(), str);
					if (!f.exists())
						throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Cannot locate: " + str));
				}
				return f;
			}
		}
	}



	public static cfXmlData newInstance(Object obj, boolean caseSensitive) {
		try {
			return new cfXmlData((Node) obj, caseSensitive);
		} catch (cfmRunTimeException ex) {
			// Just log it and return a cfXmlData object anyway.
			com.nary.Debug.printStackTrace(ex);
			return new cfXmlData((cfXmlData) null, (Node) obj, caseSensitive);
		}
	}



	public static boolean isXmlObject(Object obj) {
		if (obj == null)
			return false;
		else
			return (Node.class.isAssignableFrom(obj.getClass()));
	}

}
