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

package com.naryx.tagfusion.expression.function.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.expression.function.functionBase;

public class XmlTransform extends functionBase {
	private static final long serialVersionUID = 1L;

	public XmlTransform() {
		min = 2;
		max = 3;
	}

  public String[] getParamInfo(){
		return new String[]{
			"xml object",
			"xslt - the XSL document, which can be a string, URL or path to a local file",
			"xslt parameters - structure to pass to the XSLT transformer"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"xml", 
				"Runs the given XSLT against the XML document specified", 
				ReturnType.STRING );
	}

	
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		try {
			// Get a DOM tree to transform.
			DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
			fact.setNamespaceAware(true);
			DocumentBuilder parser = fact.newDocumentBuilder();

			Document doc = null;
			String xsl = null;
			cfData xml = null;
			cfStructData structure = null;

			if (parameters.size() == 2) {
				xml = parameters.get(1);
				xsl = parameters.get(0).getString();
			} else {
				xml = parameters.get(2);
				xsl = parameters.get(1).getString();
				if (parameters.get(0).getDataType() != cfData.CFSTRUCTDATA)
					throwException(_session, "Parameter isn't of type STRUCTURE");
				structure = (cfStructData) parameters.get(0);
			}

			if (xml instanceof cfXmlData)
				doc = (Document) ((cfXmlData) xml).getXMLNode();
			else
				doc = parser.parse(new InputSource(new StringReader(xml.getString())));

			DOMSource xmlSource = new DOMSource(doc);
			StreamSource xslSource = null;
			Object xslObj = cfXmlData.interpretString(_session, xsl);
			if (xslObj instanceof String)
				xslSource = new StreamSource(new StringReader(((String) xslObj).trim()));
			else if (xslObj instanceof File)
				xslSource = new StreamSource((File) xslObj);
			else if (xslObj instanceof URL)
				xslSource = new StreamSource(((URL) xslObj).openStream());
			else
				throwException(_session, "XSL transformation parameter unknown");

			// Prepare for transformation
			StringWriter sWriter = new StringWriter();
			Transformer transformer = TransformerFactory.newInstance().newTransformer(xslSource);

			// Add parameters (if any)
			if (structure != null) {
				Object[] keys = structure.keys();
				for (int i = 0; i < keys.length; i++)
					transformer.setParameter(keys[i].toString(), tagUtils.getNatural(structure.getData(keys[i].toString())));
			}

			// Transform
			transformer.transform(xmlSource, new StreamResult(sWriter));

			// Return the output
			String str = sWriter.toString();
			if (tagUtils.isXmlString(str))
				return cfXmlData.parseXml(str, true, null);
			else
				return tagUtils.convertToCfData(str);
		} catch (IOException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		} catch (SAXException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		} catch (ParserConfigurationException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		} catch (TransformerException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		}
	}
}
