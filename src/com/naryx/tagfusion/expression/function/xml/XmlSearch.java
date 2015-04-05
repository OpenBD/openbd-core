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
 *  
 *  $Id: XmlSearch.java 1543 2011-04-14 17:16:50Z alan $
 */

package com.naryx.tagfusion.expression.function.xml;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathEvaluator;
import org.w3c.dom.xpath.XPathException;
import org.w3c.dom.xpath.XPathNSResolver;
import org.w3c.dom.xpath.XPathResult;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.expression.function.functionBase;

public class XmlSearch extends functionBase {
	private static final long serialVersionUID = 1L;

	public XmlSearch() {
		min = max = 2;
	}

  public String[] getParamInfo(){
		return new String[]{
			"xml",
			"xpath search"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"xml", 
				"Performs an XPATH search against the XML, returning back an array of found nodes, or boolean, or count, or string depending on the search type", 
				ReturnType.OBJECT );
	}

	
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		try {
			// Get a DOM tree to query.
			DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
			fact.setNamespaceAware(true);
			DocumentBuilder parser = fact.newDocumentBuilder();

			Document doc = null;
			Node node = null;
			boolean caseSensitive = false;
			cfData xml = parameters.get(1);
			if (xml instanceof cfXmlData) {
				node = ((cfXmlData) xml).getXMLNode();
				doc = node.getOwnerDocument();
				caseSensitive = ((cfXmlData) xml).isCaseSensitive();
			} else {
				doc = parser.parse(new InputSource(new StringReader(xml.getString())));
				node = doc;
			}

			/*
			 * TODO: This is here due to a memory leak bug in the XALAN library
			 * https://issues.apache.org/jira/browse/XALANJ-1673
			 */
			///node = node.cloneNode(true);
					
			// Use the DOM L3 XPath API to apply the xpath expression to the doc.
			// Create an XPath evaluator and pass in the document.
			XPathEvaluator evaluator = new org.apache.xpath.domapi.XPathEvaluatorImpl(doc);
			XPathNSResolver resolver = evaluator.createNSResolver(node);

			
			
			// Check the xpath expression
			String xpath = parameters.get(0).getString().trim();
			if (xpath.endsWith("/") && !xpath.equals("/"))
				xpath = xpath.substring(0, xpath.length() - 1);

			// Evaluate the xpath expression
			XPathResult result;
			synchronized( node ){
				result = (XPathResult) evaluator.evaluate(xpath, node, resolver, XPathResult.ANY_TYPE, null);
			}

			// If it's an iterator type
			if (result.getResultType() == XPathResult.UNORDERED_NODE_ITERATOR_TYPE) {
				// Return the array
				cfArrayData arr = cfArrayData.createArray(1);
				Node n = null;
				while ((n = result.iterateNext()) != null)
					arr.addElement(new cfXmlData(n, caseSensitive));
			
				return arr;
			}
			// It's a number type
			else if (result.getResultType() == XPathResult.NUMBER_TYPE) {
				return new cfNumberData(result.getNumberValue());
			}
			// It's a boolean type
			else if (result.getResultType() == XPathResult.BOOLEAN_TYPE) {
				return cfBooleanData.getcfBooleanData(result.getBooleanValue());
			}
			// It's a string type
			else if (result.getResultType() == XPathResult.STRING_TYPE) {
				return new cfStringData(result.getStringValue());
			}
			// It's not a supported type
			else {
				return null;
			}
		} catch (IOException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		} catch (SAXException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		} catch (ParserConfigurationException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		} catch (DOMException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		} catch (XPathException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		}
	}
}
