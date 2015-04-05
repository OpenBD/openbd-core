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

package com.naryx.tagfusion.expression.function;

import java.util.List;

import org.apache.axis.message.SOAPHeaderElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfWSObjectData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.cfm.xml.ws.dynws.DynamicWebServiceInvoker;

public class addSOAPRequestHeader extends functionBase {
	private static final long serialVersionUID = 1L;

	public addSOAPRequestHeader() {
		super.min = 4;
		super.max = 5;
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"webservice",
			"namespace",
			"name",
			"value",
			"mustUnderstand - default to false"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"xml", 
				"Sets the given SOAP request header with the values specified", 
				ReturnType.BOOLEAN );
	}
 

	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		cfWSObjectData wso = null;
		String ns = null;
		String n = null;
		cfData val = null;
		boolean mustUnderstand = false;

		int offset = 0;
		if (parameters.size() == 5) {
			mustUnderstand = parameters.get(0).getBoolean();
			offset = 1;
		}
		val = parameters.get(0 + offset);
		n = parameters.get(1 + offset).getString();
		ns = parameters.get(2 + offset).getString();
		wso = (cfWSObjectData) parameters.get(3 + offset);

		// Make sure we haven't already sent the request
		if (!wso.getInvoked()) {
			// Create the header
			SOAPHeaderElement header = createSOAPHeader(val, ns, n, mustUnderstand);

			// Add the header
			((DynamicWebServiceInvoker) wso.getPreparedInvoker()).addRequestHeader(header);

			return cfBooleanData.TRUE;
		} else {
			return cfBooleanData.FALSE;
		}
	}

	public static SOAPHeaderElement createSOAPHeader(cfData val, String ns, String n, boolean mustUnderstand) throws cfmRunTimeException {
		SOAPHeaderElement header = null;
		if (val instanceof cfXmlData) {
			Element e = null;
			Node node = ((cfXmlData) val).getXMLNode();
			if (node.getNodeType() == Node.DOCUMENT_NODE)
				e = ((Document) node).getDocumentElement();
			else if (node.getNodeType() == Node.ELEMENT_NODE)
				e = (Element) node;
			else
				throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Invalid SOAP header value: " + node.getClass().getName()));
			header = new SOAPHeaderElement(e);
		} else {
			header = new SOAPHeaderElement(ns, n, val.getString());
		}
		header.setMustUnderstand(mustUnderstand);
		return header;
	}
}
