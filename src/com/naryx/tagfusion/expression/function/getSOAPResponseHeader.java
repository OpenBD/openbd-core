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

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfWSObjectData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class getSOAPResponseHeader extends functionBase {

	private static final long serialVersionUID = 1L;

	public getSOAPResponseHeader() {
		super.min = 3;
		super.max = 4;
	}

	public String[] getParamInfo(){
		return new String[]{
			"webservice",
			"namespace",
			"name",
			"return xml - defaults to false"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"xml", 
				"Returns back the SOAP header response", 
				ReturnType.XML );
	}
	
	
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		SOAPHeaderElement header = null;

		cfWSObjectData wso = null;
		String ns = null;
		String n = null;
		boolean asXml = false;

		int offset = 0;
		if (parameters.size() == 4) {
			asXml = parameters.get(0).getBoolean();
			offset = 1;
		}
		n = parameters.get(0 + offset).getString();
		ns = parameters.get(1 + offset).getString();
		wso = (cfWSObjectData) parameters.get(2 + offset);

		// Make sure we have sent the request
		if (wso.getInvoked()) {
			SOAPHeaderElement[] headers = (SOAPHeaderElement[]) wso.getResponseHeaders();
			for (int i = 0; i < headers.length; i++) {
				if (headers[i].getNamespaceURI().equals(ns) && headers[i].getName().equals(n)) {
					header = headers[i];
					break;
				}
			}

			// Convert the header value into the desired cfData type
			return getSOAPRequestHeader.getSOAPHeaderValue(header, asXml);
		} else {
			return cfNullData.NULL;
		}
	}

}
