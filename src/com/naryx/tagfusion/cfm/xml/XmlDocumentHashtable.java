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
 *  $Id: XmlDocumentHashtable.java 2497 2015-02-02 01:53:48Z alan $
 */

package com.naryx.tagfusion.cfm.xml;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStringData;

public class XmlDocumentHashtable extends XmlNodeHashtable implements Serializable {
	private static final long serialVersionUID = 9072585765275017281L;



	protected XmlDocumentHashtable(Node n, boolean caseSensitive) {
		super(n, caseSensitive);
	}



	public boolean containsKey(Object key) {
		if (key.toString().equalsIgnoreCase("XmlRoot"))
			return true;
		else if (key.toString().equalsIgnoreCase("XmlComment"))
			return true;
		else if (key.toString().equalsIgnoreCase("XmlDocType"))
			return true;
		else
			return super.containsKey(key);
	}



	public Object get(Object key) {
		if (key.toString().equalsIgnoreCase("XmlRoot"))
			return getXmlRoot();
		else if (key.toString().equalsIgnoreCase("XmlComment"))
			return getXmlComment();
		else if (key.toString().equalsIgnoreCase("XmlDocType"))
			return getXmlDocType();
		else
			return super.get(key);
	}



	public Object put(Object key, Object value) {
		if (key.toString().equalsIgnoreCase("XmlRoot")) {
			return super.put(key, value);
		} else if (key.toString().equalsIgnoreCase("XmlComment")) {
			// Remove all comment nodes, and add 1 new one at the top
			remove(key);
			if (!((cfData) value).toString().trim().equals("")) {
				try {
					Comment c = ((Document) nodeData).createComment(((cfData) value).toString());
					nodeData.insertBefore(c, nodeData.getFirstChild());
				} catch (DOMException ex) {
					// Nothing else we can do here
					com.nary.Debug.printStackTrace(ex);
				}
			}
			nodeData.normalize();
			return null;
		} else if (key.toString().equalsIgnoreCase("XMLDocType")) {
			// Do nothing
			return null;
		} else {
			return super.put(key, value);
		}
	}



	public Object remove(Object key) {
		if (key.toString().equalsIgnoreCase("XmlRoot")) {
			Document doc = (Document) nodeData;
			if (doc.getDocumentElement() == null) {
				return null;
			} else {
				try {
					cfXmlData rtn = new cfXmlData(this, doc.removeChild(doc.getDocumentElement()), isCaseSensitive());
					return rtn;
				} catch (DOMException ex) {
					// Nothing else we can do here
					com.nary.Debug.printStackTrace(ex);
					return null;
				}
			}
		} else if (key.toString().equalsIgnoreCase("XmlComment")) {
			// Remove all comment nodes
			List ln = new ArrayList();
			NodeList nl = nodeData.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				if (node.getNodeType() == Node.COMMENT_NODE)
					ln.add(node);
			}

			Iterator itr = ln.iterator();
			while (itr.hasNext()) {
				try {
					nodeData.removeChild((Node) itr.next());
				} catch (DOMException ex) {
					// Nothing else we can do here
					com.nary.Debug.printStackTrace(ex);
					return null;
				}
			}
			nodeData.normalize();
			return null;
		} else if (key.toString().equalsIgnoreCase("XMLDocType")) {
			return null;
		} else {
			return super.remove(key);
		}
	}



	// default is a short dump
	public void dump(PrintWriter out, String _label, int _top) {
		cfData dd = null;
		out.write("<table class='cfdump_table_xml'>");
		out.write("<th class='cfdump_th_xml' colspan='2'>");
		if (_label.length() > 0)
			out.write(_label + " - ");
		out.write("xml document [short version]</th>");
		out.write("<tr><td class='cfdump_td_xml'>");
		// XmlRoot.XmlName
		Document doc = (Document) nodeData;
		if (doc.getDocumentElement() != null)
			out.write(doc.getDocumentElement().getNodeName());
		else
			out.write("");
		out.write("</td><td class='cfdump_td_value'>");
		dd = getXmlRoot();
		if (dd != null)
			dd.dump(out);
		else
			out.write("");
		out.write("</td></tr>");

		out.write("</table>");
	}



	public void dumpLong(PrintWriter out, String _label, int _top) {
		cfData dd = null;
		out.write("<table class='cfdump_table_xml'>");
		out.write("<th class='cfdump_th_xml' colspan='2'>");
		if (_label.length() > 0)
			out.write(_label + " - ");
		out.write("xml document [long version]</th>");
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
		out.write("XmlRoot");
		out.write("</td><td class='cfdump_td_value'>");
		dd = getXmlRoot();
		if (dd != null)
			dd.dumpLong(out, "", _top);
		else
			out.write("");
		out.write("</td></tr>");

		out.write("</table>");
	}



	protected cfData getXmlRoot() {
		Document doc = (Document) nodeData;
		if (doc.getDocumentElement() != null)
			return new cfXmlData(this, doc.getDocumentElement(), isCaseSensitive());
		else
			return null;
	}



	protected cfData getXmlDocType() {
		Document doc = (Document) nodeData;
		if (doc.getDoctype() != null)
			return new cfXmlData(this, doc.getDoctype(), isCaseSensitive());
		else
			return null;
	}



	protected cfData getXmlComment() {
		Document doc = (Document) nodeData;
		StringBuilder comments = null;

		Node n = null;
		NodeList nl = doc.getChildNodes();
		boolean cfirst = true;
		for (int i = 0; i < nl.getLength(); i++) {
			n = nl.item(i);
			if (n.getNodeType() == Node.COMMENT_NODE) {
				if (cfirst)
					comments = new StringBuilder();
				else
					comments.append(System.getProperty("line.separator"));
				String nv = "";
				try {
					nv = n.getNodeValue();
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

}
