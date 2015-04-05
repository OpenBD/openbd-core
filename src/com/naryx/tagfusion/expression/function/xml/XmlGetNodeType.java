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

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.expression.function.functionBase;

public class XmlGetNodeType extends functionBase {
	private static final long serialVersionUID = 1L;

	public XmlGetNodeType() {
		min = max = 1;
	}

  public String[] getParamInfo(){
		return new String[]{
			"xml node",
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"xml", 
				"For the given XML node passed in, determines the type; returns NULL if not determined", 
				ReturnType.STRING );
	}

	
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		cfData d = parameters.get(0);
		if (d instanceof cfXmlData) {
			cfXmlData xml = (cfXmlData) d;
			return getXmlNodeTypeName(xml.getXMLNode());
		} else {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Invalid xml object: " + d));
		}
	}

	protected cfData getXmlNodeTypeName(Node nodeData) {
		short type = nodeData.getNodeType();
		switch (type) {
		case Node.ATTRIBUTE_NODE:
			return new cfStringData("ATTRIBUTE_NODE");
		case Node.CDATA_SECTION_NODE:
			return new cfStringData("CDATA_SECTION_NODE");
		case Node.COMMENT_NODE:
			return new cfStringData("COMMENT_NODE");
		case Node.ELEMENT_NODE:
			return new cfStringData("ELEMENT_NODE");
		case Node.ENTITY_REFERENCE_NODE:
			return new cfStringData("ENTITY_REFERENCE_NODE");
		case Node.PROCESSING_INSTRUCTION_NODE:
			return new cfStringData("PROCESSING_INSTRUCTION_NODE");
		case Node.TEXT_NODE:
			return new cfStringData("TEXT_NODE");
		case Node.ENTITY_NODE:
			return new cfStringData("ENTITY_NODE");
		case Node.NOTATION_NODE:
			return new cfStringData("NOTATION_NODE");
		case Node.DOCUMENT_NODE:
			return new cfStringData("DOCUMENT_NODE");
		case Node.DOCUMENT_FRAGMENT_NODE:
			return new cfStringData("DOCUMENT_FRAGMENT_NODE");
		case Node.DOCUMENT_TYPE_NODE:
			return new cfStringData("DOCUMENT_TYPE_NODE");
		default:
			return cfNullData.NULL;
		}
	}
}
