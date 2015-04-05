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

package com.naryx.tagfusion.xmlConfig;

import java.io.IOException;
import java.io.Reader;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class xmlConfigManager {

	private Document document;
	private xmlCFML topLevel;
	Vector<String> elementName;

	public xmlConfigManager(Reader inStream) throws ParserConfigurationException, SAXException, IOException {
		try{
		
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			elementName = new Vector<String>();
			topLevel = new xmlCFML();
					
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(new InputSource(inStream));
			buildTree(document);
		
		}finally{
			inStream.close();
		}
	}

	private void buildTree(Node node) {
		// Determine action based on Node type.
		switch (node.getNodeType()) {
		case Node.DOCUMENT_NODE:
			Document doc = (Document) node;
			buildTree(doc.getDocumentElement());
			break;

		case Node.ELEMENT_NODE:
			String name = node.getNodeName();
			elementName.addElement(name);
			String path = "";
			if (node.getFirstChild() == null) {
				for (int x = 0; x < elementName.size(); x++)
					path += elementName.elementAt(x) + ".";

				// replace any ".[" strings
				path = com.nary.util.string.replaceString(path, ".[", "[");

				if (path.lastIndexOf(".") != -1)
					path = path.substring(0, path.lastIndexOf("."));

				try {
					topLevel.setData(path, "");
				} catch (Exception EEE) {
				}
			}

			// Attributes
			NamedNodeMap attributes = node.getAttributes();
			for (int j = 0; j < attributes.getLength(); j++) {
				Node current = attributes.item(j);
				elementName.addElement("[" + current.getNodeValue() + "]");
			}

			// Recurse each child
			NodeList children = node.getChildNodes();
			if (children != null) {
				for (int i = 0; i < children.getLength(); i++) {
					buildTree(children.item(i));
				}
			}

			if (elementName.size() != 0) {
				if (elementName.lastElement().indexOf("[") != -1) {
					elementName.removeElementAt(elementName.size() - 1);
					elementName.removeElementAt(elementName.size() - 1);
				} else
					elementName.removeElementAt(elementName.size() - 1);
			}

			break;

		case Node.CDATA_SECTION_NODE:
		case Node.TEXT_NODE:
			String structName = "";

			for (int x = 0; x < elementName.size(); x++)
				structName += elementName.elementAt(x) + ".";

			// replace any ".[" strings
			structName = com.nary.util.string.replaceString(structName, ".[", "[");

			if (structName.lastIndexOf(".") != -1)
				structName = structName.substring(0, structName.lastIndexOf("."));

			if (elementName.lastElement().indexOf("[") == -1 && !whiteSpace(node.getNodeValue())) {

				try {
					topLevel.setData(structName, node.getNodeValue());
				} catch (Exception E) {
				}
			}
			break;
		case Node.PROCESSING_INSTRUCTION_NODE:
			break;
		case Node.ENTITY_REFERENCE_NODE:
			break;
		case Node.DOCUMENT_TYPE_NODE:
			break;
		default:
			break;
		}
	}

	private static boolean whiteSpace(String text) {

		boolean white = true;

		for (int x = 0; x < text.length(); x++) {
			if (!Character.isWhitespace(text.charAt(x)))
				white = false;
		}
		return white;
	}

	public xmlCFML getXMLCFML() {
		return topLevel;
	}
}
