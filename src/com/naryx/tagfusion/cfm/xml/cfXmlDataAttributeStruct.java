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

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

public class cfXmlDataAttributeStruct extends cfStructData implements Serializable {

	private static final long	serialVersionUID	= 1L;

	protected NamedNodeMap		nnm								= null;

	protected cfXmlData				parent						= null;



	public cfXmlDataAttributeStruct(cfXmlData p, NamedNodeMap nm) {
		super(p.isCaseSensitive());
		parent = p;
		nnm = nm;
		for (int i = 0; i < nnm.getLength(); i++) {
			String nv = "";
			try {
				nv = nnm.item(i).getNodeValue();
			} catch (DOMException ex) {
				// Just log it
				com.nary.Debug.printStackTrace(ex);
			}
			super.setData(nnm.item(i).getNodeName(), new cfStringData(nv));
		}
	}



	protected boolean equalKeys(String k1, String k2) {
		return (isCaseSensitive() ? k1.equals(k2) : k1.equalsIgnoreCase(k2));
	}



	public void setData(cfData _key, cfData _data) throws cfmRunTimeException {
		setData(_key.getString(), _data);
	}



	public void setData(String _key, cfData _data) {
		super.setData(_key, _data);
		Node n = null;
		for (int i = 0; i < nnm.getLength(); i++) {
			if (equalKeys(nnm.item(i).getNodeName(), _key)) {
				n = nnm.item(i);
				break;
			}
		}

		try {
			if (n != null) {
				n.setNodeValue(((cfStringData) _data).getString());
			} else {
				Attr attr = parent.getXMLNode().getOwnerDocument().createAttribute(_key);
				attr.setValue(_data.getString());
				nnm.setNamedItem(attr);
			}
		} catch (dataNotSupportedException ex) {
			// Cannot throw anything here
			com.nary.Debug.printStackTrace(ex);
		} catch (DOMException ex) {
			// Cannot throw anything here
			com.nary.Debug.printStackTrace(ex);
		}
	}



	public void deleteData(String _key) throws cfmRunTimeException {
		super.deleteData(_key);
		String realKey = null;
		for (int i = 0; i < nnm.getLength(); i++) {
			if (equalKeys(nnm.item(i).getNodeName(), _key)) {
				realKey = nnm.item(i).getNodeName();
				break;
			}
		}

		if (realKey != null) {
			try {
				nnm.removeNamedItem(realKey);
			} catch (DOMException ex) {
				// Cannot throw anything here
				com.nary.Debug.printStackTrace(ex);
			}
		}
	}



	public void clear() {
		super.clear();
		while (nnm.getLength() > 0) {
			try {
				nnm.removeNamedItem(nnm.item(0).getNodeName());
			} catch (DOMException ex) {
				// Cannot throw anything here
				com.nary.Debug.printStackTrace(ex);
			}
		}
	}



	public cfData duplicate() {
		return new cfXmlDataAttributeStruct((cfXmlData) parent.duplicate(), nnm);
	}

}
