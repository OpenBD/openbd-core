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
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;
import com.naryx.tagfusion.cfm.xml.cfXmlData;

public class getSOAPRequestHeader extends functionBase {

	private static final long serialVersionUID = 1L;

	public getSOAPRequestHeader() {
		super.min = 2;
		super.max = 3;
	}

	public String[] getParamInfo(){
		return new String[]{
			"namespace",
			"name",
			"return xml - defaults to false"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"xml", 
				"Returns back the SOAP header request.  Use only inside a WebServices call", 
				ReturnType.XML );
	}
	
	
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		SOAPHeaderElement header = null;

		String ns = null;
		String n = null;
		boolean asXml = false;

		int offset = 0;
		if (parameters.size() == 3) {
			asXml = parameters.get(0).getBoolean();
			offset = 1;
		}
		n = parameters.get(0 + offset).getString();
		ns = parameters.get(1 + offset).getString();

		// Get the header
		try {
			MessageContext mc = MessageContext.getCurrentContext();
			if (mc != null && mc.getRequestMessage() != null && mc.getRequestMessage().getSOAPEnvelope() != null)
				header = mc.getRequestMessage().getSOAPEnvelope().getHeaderByName(ns, n);
			else
				throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Could not get SOAP header. MessageContext request message is not available. " + "Be sure this is being called from a CFC web service function."));
		} catch (AxisFault ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		}

		// Convert the header value into the desired cfData type
		return getSOAPHeaderValue(header, asXml);
	}

	public static cfData getSOAPHeaderValue(SOAPHeaderElement header, boolean asXml) throws cfmRunTimeException {
		try {
			if (header != null) {
				if (!asXml) {
					// Try normal registered conversion
					Object ov = header.getObjectValue();
					if (ov != null) {
						if (ov instanceof String && tagUtils.isXmlString((String) ov)) {
							try {
								return cfXmlData.parseXml((String) ov, true, null);
							} catch (cfmRunTimeException ex) {
								// Do nothing, just move on
							}
						} else {
							return tagUtils.convertToCfData(ov);
						}
					}
					// Try to get as a string
					String s = header.getValue();
					if (s != null)
						return new cfStringData(s);
				}

				// OK, either we asked for it as the raw
				// xml, or we simply couldn't convert it.
				return new cfXmlData(header.getAsDOM(), true);
			} else {
				return cfNullData.NULL;
			}
		} catch (Exception ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		}
	}
}
