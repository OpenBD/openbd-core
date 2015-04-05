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

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.message.SOAPHeaderElement;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class addSOAPResponseHeader extends functionBase {
	private static final long serialVersionUID = 1L;

	public addSOAPResponseHeader() {
		super.min = 3;
		super.max = 4;
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"namespace",
			"name",
			"value",
			"mustUnderstand - default to false"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"xml", 
				"Sets the given SOAP response header with the values specified", 
				ReturnType.BOOLEAN );
	}
	

	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		String ns = null;
		String n = null;
		cfData val = null;
		boolean mustUnderstand = false;

		int offset = 0;
		if (parameters.size() == 4) {
			mustUnderstand = parameters.get(0).getBoolean();
			offset = 1;
		}
		val = parameters.get(0 + offset);
		n = parameters.get(1 + offset).getString();
		ns = parameters.get(2 + offset).getString();

		// Create the header
		SOAPHeaderElement header = addSOAPRequestHeader.createSOAPHeader(val, ns, n, mustUnderstand);

		// Add the header
		try {
			MessageContext mc = MessageContext.getCurrentContext();
			if (mc != null && mc.getResponseMessage() != null && mc.getResponseMessage().getSOAPEnvelope() != null) {
				// Check to see if the same header has already been added
				// (same meaning, having the same namespace/name pair)
				if (mc.getResponseMessage().getSOAPEnvelope().getHeaderByName(header.getNamespaceURI(), header.getName()) != null)
					throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "SOAP header value: " + header.getNamespaceURI() + ":" + header.getName() + " already set."));
				else
					mc.getResponseMessage().getSOAPEnvelope().addHeader(header);
			} else {
				throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Could not set SOAP header value. MessageContext response message is not available. " + "Be sure this is being called from a CFC web service function."));
			}
		} catch (AxisFault ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		}
		return cfBooleanData.TRUE;
	}

}
