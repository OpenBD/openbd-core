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

import java.util.List;

import org.w3c.dom.Node;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.expression.function.functionBase;

public class XmlChildPos extends functionBase {
	private static final long serialVersionUID = 1L;

	public XmlChildPos() {
		min = max = 3;
	}

  public String[] getParamInfo(){
		return new String[]{
			"xml object",
			"name of node",
			"occurence - which element of the child you wish to find"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"xml", 
				"Finds the given occurence number of the node within the XML object, returning -1 if not found", 
				ReturnType.NUMERIC );
	}

	
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		int p 			= parameters.get(0).getInt();
		String name = parameters.get(1).getString().trim();
		cfData d 		= parameters.get(2);

		// Get the child element
		if (!(d instanceof cfXmlData))
			throwException(_session, "Elem parameter isn't of type XML");
		
		cfData child = ((cfXmlData) d).getData(name);
		if (child == null) {
			return new cfNumberData(-1);
		} else if (child instanceof cfXmlData) {
			if (p > 1) {
				return new cfNumberData(-1);
			}
		} else if (child instanceof cfArrayData) {
			child = ((cfArrayData) child).getElement(p);
		}

		// Find the position in the XmlChildren array
		return new cfNumberData( findPosition(_session, (cfXmlData) child, (cfArrayData) d.getData("XmlChildren")));
	}

	protected int findPosition(cfSession _session, cfXmlData child, cfArrayData children) throws cfmRunTimeException {
		if (child == null)
			return -1;

		Node n = child.getXMLNode();
		for (int i = 1; i <= children.size(); i++) {
			if (((cfXmlData) children.getElement(i)).getXMLNode() == n)
				return i;
		}
		return -1;
	}
}
