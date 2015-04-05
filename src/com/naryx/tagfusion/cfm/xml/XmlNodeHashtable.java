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
 *  $Id: XmlNodeHashtable.java 2497 2015-02-02 01:53:48Z alan $
 */

package com.naryx.tagfusion.cfm.xml;

import java.io.PrintWriter;
import java.io.Serializable;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStringData;

public class XmlNodeHashtable extends XmlHashtable implements Serializable {
	private static final long serialVersionUID = 6477763540907593215L;



	protected XmlNodeHashtable(Node n, boolean caseSensitive) {
		super(n, caseSensitive);
	}



	public boolean containsKey(Object key) {
		if (key.toString().equalsIgnoreCase("XmlName"))
			return true;
		else if (key.toString().equalsIgnoreCase("XmlType"))
			return true;
		else if (key.toString().equalsIgnoreCase("XmlValue"))
			return true;
		else
			return super.containsKey(key);
	}



	public Object get(Object key) {
		if (key.toString().equalsIgnoreCase("XmlName"))
			return getXmlName();
		else if (key.toString().equalsIgnoreCase("XmlType"))
			return getXmlType();
		else if (key.toString().equalsIgnoreCase("XmlValue"))
			return getXmlValue();
		else
			return super.get(key);
	}



	public Object put(Object key, Object value) {
		if (key.toString().equalsIgnoreCase("XmlName")) {
			return null;
		} else if (key.toString().equalsIgnoreCase("XmlType")) {
			return null;
		} else if (key.toString().equalsIgnoreCase("XmlValue")) {
			String nv = null;
			try {
				nv = nodeData.getNodeValue();
			} catch (DOMException ex) {
				// Just log it
				com.nary.Debug.printStackTrace(ex);
			}
			try {
				nodeData.setNodeValue(((cfData) value).toString());
				return nv;
			} catch (DOMException ex) {
				// Nothing else we can do here
				com.nary.Debug.printStackTrace(ex);
			}
			return null;
		} else {
			return super.put(key, value);
		}
	}



	public Object remove(Object key) {
		if (key.toString().equalsIgnoreCase("XmlName")) {
			return null;
		} else if (key.toString().equalsIgnoreCase("XmlType")) {
			return null;
		} else if (key.toString().equalsIgnoreCase("XmlValue")) {
			String nv = null;
			try {
				nv = nodeData.getNodeValue();
			} catch (DOMException ex) {
				// Just log it
				com.nary.Debug.printStackTrace(ex);
			}
			try {
				nodeData.setNodeValue("");
				return nv;
			} catch (DOMException ex) {
				// Nothing else we can do here
				com.nary.Debug.printStackTrace(ex);
			}
			return null;
		} else {
			return super.remove(key);
		}
	}



	public void dump(PrintWriter out, String _label, int _top) {
		dumpLong(out, _label, _top);
	}



	public void dumpLong(PrintWriter out, String _label, int _top) {
		cfData dd = null;
		out.write("<table class='cfdump_table_xml'>");
		out.write("<th class='cfdump_th_xml' colspan='2'>xml node</th>");

		out.write("<tr><td class='cfdump_td_xml'>");
		out.write("XmlName");
		out.write("</td><td class='cfdump_td_value'>");
		dd = getXmlName();
		if (dd != null)
			dd.dumpLong(out, "", _top);
		else
			out.write("");
		out.write("</td></tr>");

		out.write("<tr><td class='cfdump_td_xml'>");
		out.write("XmlType");
		out.write("</td><td class='cfdump_td_value'>");
		dd = getXmlType();
		if (dd != null)
			dd.dumpLong(out, "", _top);
		else
			out.write("");
		out.write("</td></tr>");

		out.write("<tr><td class='cfdump_td_xml'>");
		out.write("XmlValue");
		out.write("</td><td class='cfdump_td_value'>");
		dd = getXmlValue();
		if (dd != null)
			dd.dumpLong(out, "", _top);
		else
			out.write("");
		out.write("</td></tr>");

		out.write("</table>");
	}



	protected cfData getXmlName() {
		return new cfStringData((nodeData.getNodeName() == null ? "" : nodeData.getNodeName().trim()));
	}



	protected cfData getXmlType() {
		short type = nodeData.getNodeType();
		switch (type) {
			case Node.CDATA_SECTION_NODE:
				return new cfStringData("CDATA");
			case Node.COMMENT_NODE:
				return new cfStringData("COMMENT");
			case Node.ELEMENT_NODE:
				return new cfStringData("ELEMENT");
			case Node.ENTITY_REFERENCE_NODE:
				return new cfStringData("ENTITYREF");
			case Node.PROCESSING_INSTRUCTION_NODE:
				return new cfStringData("PI");
			case Node.TEXT_NODE:
				return new cfStringData("TEXT");
			case Node.ENTITY_NODE:
				return new cfStringData("ENTITY");
			case Node.NOTATION_NODE:
				return new cfStringData("NOTATION");
			case Node.DOCUMENT_NODE:
				return new cfStringData("DOCUMENT");
			case Node.DOCUMENT_FRAGMENT_NODE:
				return new cfStringData("FRAGMENT");
			case Node.DOCUMENT_TYPE_NODE:
				return new cfStringData("DOCTYPE");
			default:
				return null;
		}
	}



	protected cfData getXmlValue() {
		String nv = null;
		try {
			nv = nodeData.getNodeValue();
		} catch (DOMException ex) {
			// Just log it
			com.nary.Debug.printStackTrace(ex);
		}
		if (nv == null)
			return new cfStringData("");
		else
			return new cfStringData(nv.trim());
	}



	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}
}
