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
import org.w3c.dom.Document;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfWSObjectData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.cfXmlData;

public class getSOAPRequest extends functionBase {

	private static final long serialVersionUID = 1L;

	public getSOAPRequest() {
		super.min = 0;
		super.max = 1;
	}

	public String[] getParamInfo(){
		return new String[]{
			"soap request"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"xml", 
				"Returns back the original SOAP request for this object, or the current one if processing.", 
				ReturnType.XML );
	}
  	
	
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		if (parameters.size() == 1) {
			cfWSObjectData wso = (cfWSObjectData) parameters.get(0);
			if (wso.getInvoked() && wso.getRequestXml() != null)
				return new cfXmlData((Document) wso.getRequestXml(), true);
			else
				return cfNullData.NULL;
		} else {
			try {
				MessageContext mc = MessageContext.getCurrentContext();
				if (mc != null && mc.getRequestMessage() != null && mc.getRequestMessage().getSOAPEnvelope() != null)
					return new cfXmlData(mc.getRequestMessage().getSOAPEnvelope().getAsDocument(), true);
				else
					throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Unable to use getSOAPRequest: not processing a web service request."));
			} catch (AxisFault ex) {
				throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
			} catch (Exception ex) {
				throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
			}
		}
	}

}
