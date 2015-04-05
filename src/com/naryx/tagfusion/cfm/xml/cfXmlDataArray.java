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

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfArrayListData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfXmlDataArray extends cfArrayListData implements Serializable {

	private static final long	serialVersionUID				= 1;

	protected cfXmlData				parent									= null;
	protected boolean					requestedAsXmlChildren	= false;



	public cfXmlDataArray(cfXmlData p, Vector<? extends cfData> children) {
		super(1, children);
		parent = p;
	}



	public void setData(int _index, cfData _element) throws cfmRunTimeException {
		if (_element instanceof cfXmlData) {
			Node n = ((cfXmlData) _element).getXMLNode();
			// What about Document Nodes? Probably don't want to clone them
			// when adding/setting in an array.
			if (n.getParentNode() != null)
				n = n.cloneNode(true);

			if (_index > data.size())
				data.setSize(_index);

			Node next = null;
			cfXmlData cur = (cfXmlData) data.get(_index - 1);
			if (cur == null)
				next = findNextNode(_index - 1);
			data.set(_index - 1, new cfXmlData(n, ((cfXmlData) _element).isCaseSensitive()));

			// Adjust the parent node (if available)
			if (parent != null) {
				try {
					if (cur != null)
						parent.getXMLNode().replaceChild(n, cur.getXMLNode());
					else if (next != null)
						parent.getXMLNode().insertBefore(n, next);
					else
						parent.getXMLNode().appendChild(n);
				} catch (DOMException ex) {
					throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
				}
			}
		} else if (_element instanceof cfStringData) {
			if (((cfStringData) _element).getString().trim().equals("")) {
				cfXmlData cur = (cfXmlData) data.get(_index - 1);
				if (cur != null) {
					// Adjust the parent node (if available)
					if (parent != null) {
						try {
							parent.getXMLNode().removeChild(cur.getXMLNode());
						} catch (DOMException ex) {
							throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
						}
					}
					data.remove(_index - 1);
				}
			}
		}
	}



	protected Node findNextNode(int ndx) {
		cfXmlData next = null;
		for (int i = ndx + 1; i < data.size(); i++) {
			if ((next = (cfXmlData) data.get(i)) != null)
				return next.getXMLNode();
		}
		for (int i = ndx; i >= 0 && i < data.size(); i--) {
			if ((next = (cfXmlData) data.get(i)) != null)
				return next.getXMLNode().getNextSibling();
		}
		return null;
	}



	public void addElement(cfData _element) throws cfmRunTimeException {
		_element = _element.duplicate();
		data.add(_element);

		// Adjust the parent node (if available)
		if (parent != null) {
			try {
				parent.getXMLNode().appendChild(((cfXmlData) _element).getXMLNode());
			} catch (DOMException ex) {
				throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
			}
		}
	}



	public void addElementAt(cfData _element, int _index) throws cfmRunTimeException {
		cfXmlData next = null;
		for (int i = _index - 1; i < data.size(); i++) {
			if ((next = (cfXmlData) data.get(i)) != null)
				break;
		}

		_element = _element.duplicate();
		data.set(_index - 1, _element);
		// Adjust the parent node (if available)
		if (parent != null) {
			try {
				parent.getXMLNode().insertBefore(((cfXmlData) _element).getXMLNode(), (next != null) ? next.getXMLNode() : null);
			} catch (DOMException ex) {
				throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
			}
		}
	}



	public void removeAllElements() throws cfmRunTimeException {
		// Adjust the parent node (if available)
		if (parent != null) {
			Iterator<cfData> itr = data.iterator();
			while (itr.hasNext()) {
				try {
					parent.getXMLNode().removeChild(((cfXmlData) itr.next()).getXMLNode());
				} catch (DOMException ex) {
					throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
				}
			}
		}
		data.clear();
	}



	public void removeElementAt(int _no) throws cfmRunTimeException {
		cfXmlData d = (cfXmlData) data.get(_no - 1);

		data.remove(_no - 1);
		if (d != null) {
			// Adjust the parent node (if available)
			if (parent != null) {
				try {
					parent.getXMLNode().removeChild(d.getXMLNode());
				} catch (DOMException ex) {
					throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
				}
			}
		}
	}



	public void setElements(int _start, int _end, cfData _value) throws cfmRunTimeException {
		for (int x = _start; x <= _end; x++)
			setData(x, _value.duplicate());
	}



	public void elementSwap(int _start, int _end) throws cfmRunTimeException {
		cfXmlData first = (cfXmlData) data.get(_start - 1);
		cfXmlData second = (cfXmlData) data.get(_end - 1);
		data.set(_end - 1, first);
		data.set(_start - 1, second);

		// Adjust the parent node (if available)
		if (parent != null) {
			try {
				Node dummyNode = parent.getXMLNode().getOwnerDocument().createTextNode("placeholder");
				parent.getXMLNode().insertBefore(dummyNode, second.getXMLNode().getNextSibling());
				Node firstNode = parent.getXMLNode().replaceChild(second.getXMLNode(), first.getXMLNode());
				parent.getXMLNode().insertBefore(firstNode, dummyNode);
				parent.getXMLNode().removeChild(dummyNode);
			} catch (DOMException ex) {
				throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
			}
		}
	}



	public cfArrayData copy() {
		cfXmlDataArray arr = new cfXmlDataArray(parent, data);

		arr.data = (Vector<cfData>) data.clone();
		return arr;
	}



	public cfData duplicate() {
		cfXmlDataArray arrCopy = (cfXmlDataArray) copy();
		Vector<cfData> theData = arrCopy.data;
		Vector<cfData> clonedArrData = new Vector<cfData>();
		int arrLen = theData.size();

		for (int i = 0; i < arrLen; i++) {
			cfData clonedData = theData.get(i).duplicate();

			if (clonedData == null)
				return null;
			clonedArrData.add(clonedData);
		}

		arrCopy.data = clonedArrData;
		return arrCopy;
	}



	public void setRequestedAsXmlChildren(boolean pVal) {
		this.requestedAsXmlChildren = pVal;
	}



	public boolean wasRequestedAsXmlChildren() {
		return this.requestedAsXmlChildren;
	}



	public String createList(String delimiter) {
		String list = "";
		cfStringData text = null;
		for (Iterator<cfData> iter = data.iterator(); iter.hasNext();) {
			cfXmlData nextElement = (cfXmlData) iter.next();
			if (nextElement != null) {
				text = (cfStringData) nextElement.getData("XmlText");
				if (text != null)
					list = list + text.getString();
			}
			list = list + delimiter;
		}

		if (data.size() > 0)
			list = list.substring(0, list.length() - 1);
		return list;
	}



	// for XML document "long" dump
	public void dumpAsChildren(java.io.PrintWriter out) {
		out.write("<table cellpadding='2' cellspacing='0'>");
		if (data.size() > 0) {
			for (int x = 0; x < data.size(); x++) {
				out.write("<tr><td>");
				cfData element = data.get(x);
				if ((element == null) || (element.getDataType() == cfData.CFNULLDATA))
					out.write("");
				else
					element.dumpLong(out);

				out.write("</td></tr>");
			}
		} else {
			out.write("");
		}
		out.write("</table>");
	}



	// same as cfArrayData except that it does a long dump of elements
	public void dump(java.io.PrintWriter out) {
		out.write("<table class='cfdump_table_array'>");
		if (data.size() > 0) {
			out.write("<th class='cfdump_th_array' colspan='2'>array</th>");
			for (int x = 0; x < data.size(); x++) {
				out.write("<tr><td class='cfdump_td_array'>");
				out.write((x + 1) + "");
				out.write("</td><td class='cfdump_td_value'>");

				cfData element = data.get(x);
				if ((element == null) || (element.getDataType() == cfData.CFNULLDATA))
					out.write("[undefined array element]");
				else
					element.dumpLong(out);

				out.write("</td></tr>");
			}
		} else {
			out.write("<th class='cfdump_th_array' colspan='2'>array [empty]</th>");
		}
		out.write("</table>");
	}
}
