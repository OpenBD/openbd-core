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
 *  $Id: $
 */

package com.naryx.tagfusion.cfm.xml;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class XmlElementHashtable extends XmlNodeHashtable {

	protected XmlElementHashtable(Node n, boolean caseSensitive) {
		super(n, caseSensitive);
	}



	public boolean containsKey(Object key) {
		if (key.toString().equalsIgnoreCase("XmlNsPrefix"))
			return true;
		else if (key.toString().equalsIgnoreCase("XmlNsURI"))
			return true;
		else if (key.toString().equalsIgnoreCase("XmlText"))
			return true;
		else if (key.toString().equalsIgnoreCase("XmlCData"))
			return true;
		else if (key.toString().equalsIgnoreCase("XmlComment"))
			return true;
		else if (key.toString().equalsIgnoreCase("XmlAttributes"))
			return true;
		else if (key.toString().equalsIgnoreCase("XmlChildren"))
			return true;
		else if (key.toString().equalsIgnoreCase("XmlParent"))
			return true;
		else if (key.toString().equalsIgnoreCase("XmlNodes"))
			return true;
		else
			return super.containsKey(key);
	}



	public Object get(Object key) {
		if (key.toString().equalsIgnoreCase("XmlNsPrefix"))
			return getXmlNsPrefix();
		else if (key.toString().equalsIgnoreCase("XmlNsURI"))
			return getXmlNsURI();
		else if (key.toString().equalsIgnoreCase("XmlText"))
			return getXmlTextAndCData();
		else if (key.toString().equalsIgnoreCase("XmlCData"))
			return getXmlTextAndCData();
		else if (key.toString().equalsIgnoreCase("XmlComment"))
			return getXmlComment();
		else if (key.toString().equalsIgnoreCase("XmlAttributes"))
			return getXmlAttributes();
		else if (key.toString().equalsIgnoreCase("XmlChildren"))
			return getXmlChildren();
		else if (key.toString().equalsIgnoreCase("XmlParent"))
			return getXmlParent();
		else if (key.toString().equalsIgnoreCase("XmlNodes"))
			return getXmlNodes();

		// Check for nodes of the same name
		NodeList nl = nodeData.getChildNodes();
		Vector<cfXmlData> rtn = new Vector<cfXmlData>();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (equalKeys(node.getNodeName(), (String) key))
				rtn.add(new cfXmlData(this, node, isCaseSensitive()));
		}

		if (rtn.size() == 1) {
			return rtn.get(0);
		} else if (rtn.size() > 1) {
			return new cfXmlDataArray(new cfXmlData(this, nodeData, isCaseSensitive()), rtn);
		} else {
			// Check for all other types of nodes
			Object o = super.get(key);
			if (o == null) {
				// Check if looking for an attribute
				o = ((cfXmlDataAttributeStruct) getXmlAttributes()).getData((String) key);
			}
			return o;
		}
	}



	public Object put(Object key, Object value) {
		try {
			if (key.toString().equalsIgnoreCase("XmlNsPrefix")) {
				String oldPrefix = null;
				try {
					oldPrefix = nodeData.getPrefix();
				} catch (DOMException ex) {
					// Just log it
					com.nary.Debug.printStackTrace(ex);
				}
				nodeData.setPrefix(((cfData) value).toString());
				return oldPrefix;
			} else if (key.toString().equalsIgnoreCase("XmlNsURI")) {
				// Do nothing
				return null;
			} else if (key.toString().equalsIgnoreCase("XmlText") || key.toString().equalsIgnoreCase("XmlCData")) {
				boolean isCDATA = key.toString().equalsIgnoreCase("XmlCData");
				// Remove all text and CDATA nodes, and add 1 new one at the top
				remove("XmlText");
				remove("XmlCData");
				if (!((cfData) value).toString().trim().equals("")) {
					Node t = null;
					if (isCDATA)
						t = nodeData.getOwnerDocument().createCDATASection(((cfData) value).toString());
					else
						t = nodeData.getOwnerDocument().createTextNode(((cfData) value).toString());
					nodeData.insertBefore(t, nodeData.getFirstChild());
				}
				nodeData.normalize();
				return null;
			} else if (key.toString().equalsIgnoreCase("XmlComment")) {
				// Remove all comment nodes, and add 1 new one at the top
				remove(key);
				if (!((cfData) value).toString().trim().equals("")) {
					Comment c = nodeData.getOwnerDocument().createComment(((cfData) value).toString());
					nodeData.insertBefore(c, nodeData.getFirstChild());
				}
				nodeData.normalize();
				return null;
			} else if (key.toString().equalsIgnoreCase("XmlAttributes")) {
				// Overwrite any existing xml attributes with these new ones
				if (value instanceof cfStructData) {
					cfXmlDataAttributeStruct attribs = (cfXmlDataAttributeStruct) getXmlAttributes();
					cfStructData attribsToAdd = (cfStructData) value;

					Object[] keys = attribsToAdd.keys();
					for (int i = 0; i < keys.length; i++) {
						String str = keys[i].toString();
						cfData valueData = attribsToAdd.getData(str);
						attribs.setData(str, valueData);
					}
				}
				return null;
			} else if (key.toString().equalsIgnoreCase("XmlChildren")) {
				// Do nothing
				return null;
			} else if (key.toString().equalsIgnoreCase("XmlParent")) {
				// Do nothing
				return null;
			} else if (key.toString().equalsIgnoreCase("XmlNodes")) {
				// Do nothing
				return null;
			} else {
				if (value instanceof cfXmlData) {
					Node n = ((cfXmlData) value).getXMLNode();
					if (n.getParentNode() != null)
						n = n.cloneNode(true);

					cfData d = (cfData) get(key);
					if (d != null) {
						if (d instanceof cfXmlData)
							return nodeData.replaceChild(n, ((cfXmlData) d).getXMLNode());
						else if (d instanceof cfXmlDataArray)
							((cfXmlDataArray) d).setData(1, new cfXmlData(n, ((cfXmlData) value).isCaseSensitive()));
					} else {
						nodeData.appendChild(n);
					}
					nodeData.normalize();
				} else if (value instanceof cfStructData) {
					Element e = nodeData.getOwnerDocument().createElement((String) key);
					nodeData.appendChild(e);
					buildParentElement((cfStructData) value, e);
					nodeData.normalize();
				} else if (value instanceof cfStringData) {
					if (((cfData) value).toString().trim().equals("")) {
						cfData d = (cfData) get(key);
						if (d != null) {
							if (d instanceof cfXmlData)
								nodeData.removeChild(((cfXmlData) d).getXMLNode());
							else if (d instanceof cfXmlDataArray)
								((cfXmlDataArray) d).removeAllElements();
							nodeData.normalize();
						}
					}
				} else {
					super.put(key, value);
				}

				return null;
			}
		} catch (cfmRunTimeException ex) {
			// Nothing else we can do here
			com.nary.Debug.printStackTrace(ex);
		} catch (DOMException ex) {
			// Nothing else we can do here
			com.nary.Debug.printStackTrace(ex);
		}
		return null;
	}



	protected void buildParentElement(cfStructData s, Node parent) throws cfmRunTimeException {
		try {
			Object[] keys = s.keys();
			for (int i = 0; i < keys.length; i++) {
				String k = (String) keys[i];
				cfData d = s.getData(k);
				if (d instanceof cfXmlData) {
					// Add as a child node
					parent.appendChild(((cfXmlData) d).getXMLNode());
				} else if (d instanceof cfStructData) {
					// Create a child node and continue recursion
					Element e = parent.getOwnerDocument().createElement(k);
					parent.appendChild(e);
					buildParentElement((cfStructData) d, e);
				}
			}
		} catch (DOMException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		}
	}



	public Object remove(Object key) {
		try {
			if (key.toString().equalsIgnoreCase("XmlNsPrefix")) {
				String oldPrefix = null;
				try {
					oldPrefix = nodeData.getPrefix();
				} catch (DOMException ex) {
					// Just log it
					com.nary.Debug.printStackTrace(ex);
				}
				nodeData.setPrefix(null);
				return oldPrefix;
			} else if (key.toString().equalsIgnoreCase("XmlNsURI")) {
				return null;
			} else if (key.toString().equalsIgnoreCase("XmlText")) {
				// Remove all text nodes
				List<Node> ln = new ArrayList<Node>();
				NodeList nl = nodeData.getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) {
					Node node = nl.item(i);
					if (node.getNodeType() == Node.TEXT_NODE)
						ln.add(node);
				}

				Iterator<Node> itr = ln.iterator();
				while (itr.hasNext())
					nodeData.removeChild(itr.next());
				nodeData.normalize();
				return null;
			} else if (key.toString().equalsIgnoreCase("XmlCData")) {
				// Remove all cdata nodes
				List<Node> ln = new ArrayList<Node>();
				NodeList nl = nodeData.getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) {
					Node node = nl.item(i);
					if (node.getNodeType() == Node.CDATA_SECTION_NODE)
						ln.add(node);
				}

				Iterator<Node> itr = ln.iterator();
				while (itr.hasNext())
					nodeData.removeChild(itr.next());
				nodeData.normalize();
				return null;
			} else if (key.toString().equalsIgnoreCase("XmlComment")) {
				// Remove all comment nodes
				List<Node> ln = new ArrayList<Node>();
				NodeList nl = nodeData.getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) {
					Node node = nl.item(i);
					if (node.getNodeType() == Node.COMMENT_NODE)
						ln.add(node);
				}

				Iterator<Node> itr = ln.iterator();
				while (itr.hasNext())
					nodeData.removeChild((Node) itr.next());
				nodeData.normalize();
				return null;
			} else if (key.toString().equalsIgnoreCase("XmlAttributes")) {
				((cfXmlDataAttributeStruct) getXmlAttributes()).clear();
				return null;
			} else if (key.toString().equalsIgnoreCase("XmlChildren")) {
				((cfXmlDataArray) getXmlChildren()).removeAllElements();
				nodeData.normalize();
				return null;
			} else if (key.toString().equalsIgnoreCase("XmlParent")) {
				return null;
			} else if (key.toString().equalsIgnoreCase("XmlNodes")) {
				((cfXmlDataArray) getXmlNodes()).removeAllElements();
				nodeData.normalize();
				return null;
			} else {
				cfData d = (cfData) get(key);
				if (d != null) {
					if (d instanceof cfXmlData)
						nodeData.removeChild(((cfXmlData) d).getXMLNode());
					else if (d instanceof cfXmlDataArray)
						((cfXmlDataArray) d).removeAllElements();
					nodeData.normalize();
				}
				return null;
			}
		} catch (cfmRunTimeException ex) {
			// Nothing else we can do here
			com.nary.Debug.printStackTrace(ex);
		} catch (DOMException ex) {
			// Nothing else we can do here
			com.nary.Debug.printStackTrace(ex);
		}
		return null;
	}



	// default is a short dump
	public void dump(PrintWriter out, String _label, int _top) {
		cfData dd = null;
		out.write("<table class='cfdump_table_xml'>");

		out.write("<tr><td class='cfdump_td_xml'>");
		out.write("XmlText");
		out.write("</td><td class='cfdump_td_value'>");
		dd = getXmlTextAndCData();
		if (dd != null)
			dd.dump(out, "", _top);
		else
			out.write("");
		out.write("</td></tr>");

		dd = getXmlAttributes();
		if ((dd != null) && (((cfXmlDataAttributeStruct) dd).keys().length > 0)) {
			out.write("<tr><td class='cfdump_td_xml'>");
			out.write("XmlAttributes");
			out.write("</td><td class='cfdump_td_value'>");
			dd.dump(out, "", _top);
			out.write("</td></tr>");
		}

		// dump children
		NodeList nl = nodeData.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				out.write("<tr><td class='cfdump_td_xml'>");
				out.write(n.getNodeName());
				out.write("</td><td class='cfdump_td_value'>");
				dd = new cfXmlData(this, n, isCaseSensitive());
				dd.dump(out, "", _top);
				out.write("</td></tr>");
			}
		}

		out.write("</table>");
	}



	public void dumpLong(PrintWriter out, String _label, int _top) {
		cfData dd = null;
		out.write("<table class='cfdump_table_xml'>");
		out.write("<th class='cfdump_th_xml' colspan='2'>xml element</th>");

		out.write("<tr><td class='cfdump_td_xml'>");
		out.write("XmlName");
		out.write("</td><td class='cfdump_td_value'><b>");
		dd = getXmlName();
		if (dd != null)
			dd.dumpLong(out, "", _top);
		else
			out.write("");
		out.write("</b></td></tr>");

		out.write("<tr><td class='cfdump_td_xml'>");
		out.write("XmlNsPrefix");
		out.write("</td><td class='cfdump_td_value'>");
		dd = getXmlNsPrefix();
		if (dd != null)
			dd.dumpLong(out, "", _top);
		else
			out.write("");
		out.write("</td></tr>");

		out.write("<tr><td class='cfdump_td_xml'>");
		out.write("XmlNsURI");
		out.write("</td><td class='cfdump_td_value'>");
		dd = getXmlNsURI();
		if (dd != null)
			dd.dumpLong(out, "", _top);
		else
			out.write("");
		out.write("</td></tr>");

		out.write("<tr><td class='cfdump_td_xml'>");
		out.write("XmlText");
		out.write("</td><td class='cfdump_td_value'>");
		dd = getXmlTextAndCData();
		if (dd != null)
			dd.dumpLong(out, "", _top);
		else
			out.write("");
		out.write("</td></tr>");

		out.write("<tr><td class='cfdump_td_xml'>");
		out.write("XmlComment");
		out.write("</td><td class='cfdump_td_value'>");
		dd = getXmlComment();
		if (dd != null)
			dd.dumpLong(out, "", _top);
		else
			out.write("");
		out.write("</td></tr>");

		out.write("<tr><td class='cfdump_td_xml'>");
		out.write("XmlAttributes");
		out.write("</td><td class='cfdump_td_value'>");
		dd = getXmlAttributes();
		if (dd != null)
			dd.dumpLong(out, "", _top);
		else
			out.write("");
		out.write("</td></tr>");

		out.write("<tr><td class='cfdump_td_xml'>");
		out.write("XmlChildren");
		out.write("</td><td class='cfdump_td_value'>");
		dd = getXmlChildren();
		if (dd != null)
			((cfXmlDataArray) dd).dumpAsChildren(out);
		else
			out.write("");
		out.write("</td></tr>");

		out.write("</table>");
	}



	protected cfData getXmlNsPrefix() {
		String oldPrefix = null;
		try {
			oldPrefix = nodeData.getPrefix();
		} catch (DOMException ex) {
			// Just log it
			com.nary.Debug.printStackTrace(ex);
		}

		if (oldPrefix != null)
			return new cfStringData(oldPrefix.trim());
		else
			return new cfStringData("");
	}



	protected cfData getXmlNsURI() {
		if (nodeData.getNamespaceURI() != null)
			return new cfStringData(nodeData.getNamespaceURI().trim());
		else
			return new cfStringData("");
	}



	protected cfData getXmlTextAndCData() {
		boolean tfirst = true;
		NodeList nl = nodeData.getChildNodes();
		StringBuilder texts = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			short nodeType = node.getNodeType();
			if (nodeType == Node.CDATA_SECTION_NODE || nodeType == Node.TEXT_NODE) {
				if (tfirst)
					texts = new StringBuilder();
				else
					texts.append(System.getProperty("line.separator"));
				String nv = "";
				try {
					nv = node.getNodeValue();
				} catch (DOMException ex) {
					// Just log it
					com.nary.Debug.printStackTrace(ex);
				}
				texts.append(nv.trim());
				tfirst = false;
			}
		}

		if (texts != null)
			return new cfStringData(texts.toString().trim());
		else
			return new cfStringData("");
	}



	protected cfData getXmlComment() {
		boolean cfirst = true;
		NodeList nl = nodeData.getChildNodes();
		StringBuilder comments = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getNodeType() == Node.COMMENT_NODE) {
				if (cfirst)
					comments = new StringBuilder();
				else
					comments.append(System.getProperty("line.separator"));
				String nv = "";
				try {
					nv = node.getNodeValue();
				} catch (DOMException ex) {
					// Just log it
					com.nary.Debug.printStackTrace(ex);
				}
				comments.append(nv.trim());
				cfirst = false;
			}
		}

		if (comments != null)
			return new cfStringData(comments.toString().trim());
		else
			return new cfStringData("");
	}



	protected cfData getXmlAttributes() {
		return new cfXmlDataAttributeStruct(new cfXmlData(this, nodeData, isCaseSensitive()), nodeData.getAttributes());
	}



	protected cfData getXmlChildren() {
		NodeList nl = nodeData.getChildNodes();
		Vector<cfXmlData> list = new Vector<cfXmlData>();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
				list.add(new cfXmlData(this, node, isCaseSensitive()));
		}
		cfXmlDataArray rtn = new cfXmlDataArray(new cfXmlData(this, nodeData, isCaseSensitive()), list);
		rtn.setRequestedAsXmlChildren(true);
		return rtn;
	}



	protected cfData getXmlParent() {
		return new cfXmlData(this, nodeData.getParentNode(), isCaseSensitive());
	}



	protected cfData getXmlNodes() {
		NodeList nl = nodeData.getChildNodes();
		Vector<cfXmlData> list = new Vector<cfXmlData>();
		for (int i = 0; i < nl.getLength(); i++)
			list.add(new cfXmlData(this, nl.item(i), isCaseSensitive()));

		return new cfXmlDataArray(new cfXmlData(this, nodeData, isCaseSensitive()), list);
	}

}
