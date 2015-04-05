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
 *  $Id: XmlHashtable.java 2497 2015-02-02 01:53:48Z alan $
 */

package com.naryx.tagfusion.cfm.xml;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nary.util.CaseSensitiveMap;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.tag.cfDUMP;

public abstract class XmlHashtable implements CaseSensitiveMap,Serializable {
	private static final long serialVersionUID = -5659752826018322960L;
	protected Node	nodeData	= null;
	private boolean	isCaseSensitive;



	protected XmlHashtable(Node n, boolean caseSensitive) {
		nodeData = n;
		isCaseSensitive = caseSensitive;
	}



	public Node getXMLNode() {
		return nodeData;
	}



	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}



	public void dump(PrintWriter out) {
		dump(out, "", cfDUMP.TOP_DEFAULT);
	}



	public abstract void dump(PrintWriter out, String _label, int _top);



	public void dumpLong(PrintWriter out) {
		dumpLong(out, "", cfDUMP.TOP_DEFAULT);
	}



	public abstract void dumpLong(PrintWriter out, String _label, int _top);



	public boolean containsKey(Object key) {
		NodeList nl = nodeData.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (equalKeys(nl.item(i).getNodeName(), (String) key))
				return true;
		}
		return false;
	}



	public Object get(Object key) {
		NodeList nl = nodeData.getChildNodes();
		// Try elements first
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Node node = nl.item(i);
				if (equalKeys(node.getNodeName(), (String) key))
					return new cfXmlData(this, node, isCaseSensitive);
			}
		}
		// OK, try every other type of node then
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeType() != Node.ELEMENT_NODE) {
				Node node = nl.item(i);
				if (equalKeys(node.getNodeName(), (String) key))
					return new cfXmlData(this, node, isCaseSensitive);
			}
		}
		return null;
	}



	public Set keySet() {
		NodeList nl = nodeData.getChildNodes();
		Set rtn = new HashSet(nl.getLength());
		for (int i = 0; i < nl.getLength(); i++)
			rtn.add(nl.item(i).getNodeName());
		return rtn;
	}



	public Collection values() {
		NodeList nl = nodeData.getChildNodes();
		Set rtn = new HashSet(nl.getLength());
		for (int i = 0; i < nl.getLength(); i++)
			rtn.add(nl.item(i));
		return rtn;
	}



	public Set entrySet() {
		NodeList nl = nodeData.getChildNodes();
		Set rtn = new HashSet(nl.getLength());
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			rtn.add(new Entry(node.getNodeName(), node));
		}
		return rtn;
	}



	public void putAll(Map map) {
		Iterator itr = map.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry entry = (Map.Entry) itr.next();
			put(entry.getKey(), entry.getValue());
		}
	}



	public Object put(Object key, Object value) {
		try {
			if (value instanceof cfXmlData) {
				Node n = ((cfXmlData) value).getXMLNode();
				if (n.getParentNode() != null)
					n = n.cloneNode(true);
				cfData d = (cfData) get(key);
				if (d != null)
					return nodeData.replaceChild(n, ((cfXmlData) d).getXMLNode());
				else
					nodeData.appendChild(n);
				nodeData.normalize();
			} else if (value instanceof cfStringData) {
				if (((cfData) value).toString().trim().equals("")) {
					cfXmlData d = (cfXmlData) get(key);
					if (d != null)
						nodeData.removeChild(d.getXMLNode());
					nodeData.normalize();
				}
			}
		} catch (DOMException ex) {
			// Nothing else we can do here
			com.nary.Debug.printStackTrace(ex);
		}
		return null;
	}



	public void clear() {
		List toRemove = null;
		Iterator itr = null;

		try {
			NodeList nl = nodeData.getChildNodes();
			if (nl != null) {
				// Get all the children
				toRemove = new ArrayList(nl.getLength());
				for (int i = 0; i < nl.getLength(); i++)
					toRemove.add(nl.item(i));

				// Now remove them
				itr = toRemove.iterator();
				while (itr.hasNext())
					nodeData.removeChild((Node) itr.next());
			}

			NamedNodeMap attribs = nodeData.getAttributes();
			if (attribs != null) {
				// Get all the attributes
				toRemove = new ArrayList(nl.getLength());
				for (int i = 0; i < attribs.getLength(); i++)
					toRemove.add(new String[] { attribs.item(i).getNamespaceURI(), attribs.item(i).getLocalName() });

				// Now remove them
				itr = toRemove.iterator();
				while (itr.hasNext()) {
					String[] attrib = (String[]) itr.next();
					if (attrib[0] == null)
						attribs.removeNamedItem(attrib[1]);
					else
						attribs.removeNamedItemNS(attrib[0], attrib[1]);
				}
			}
		} catch (DOMException ex) {
			// Nothing else we can do here
			com.nary.Debug.printStackTrace(ex);
		}
	}



	public Object remove(Object key) {
		try {
			cfData d = (cfData) get(key);
			if (d != null) {
				nodeData.removeChild(((cfXmlData) d).getXMLNode());
				nodeData.normalize();
			}
		} catch (DOMException ex) {
			// Nothing else we can do here
			com.nary.Debug.printStackTrace(ex);
		}
		return null;
	}



	// Utility comparison method
	protected boolean equalKeys(String k1, String k2) {
		return (isCaseSensitive ? k1.equals(k2) : k1.equalsIgnoreCase(k2));
	}



	public int size() {
		return nodeData.getChildNodes().getLength();
	}



	public boolean isEmpty() {
		return nodeData.hasChildNodes();
	}

	private class Entry implements Map.Entry {

		private Object	k	= null;
		private Object	v	= null;



		public Entry(Object k, Object v) {
			this.k = k;
			this.v = v;
		}



		public boolean equals(Object o) {
			boolean rtn = (o instanceof Entry && o != null && ((Entry) o).getKey().equals(getKey()));
			if (rtn) {
				if (getValue() != null) {
					if (((Entry) o).getValue() != null)
						return ((Entry) o).getValue().equals(getValue());
					else
						return false;
				} else {
					if (((Entry) o).getValue() != null)
						return false;
					else
						return true;
				}
			} else {
				return false;
			}
		}



		public Object getValue() {
			return v;
		}



		public Object getKey() {
			return k;
		}



		public Object setValue(Object val) {
			Object rtn = v;
			v = val;
			return rtn;
		}



		public int hashCode() {
			return getKey().hashCode();
		}
	}

}
