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

package com.naryx.tagfusion.cfm.xml.ws;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.utils.Messages;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfcMethodData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;
import com.naryx.tagfusion.cfm.xml.cfXmlData;

public class cfcInvoker {

	public cfcInvoker() {
		super();
	}

	public Object invokeCFCFunction(MessageContext msgContext, cfSession session, String cfcName, cfStructData method, Object[] argValues) throws Exception {
		// Create the cfc
		cfComponentData cfc = new cfComponentData(session, cfcName);
		if (cfc == null || cfc.getMetaData() == null || cfc.getMetaData().isEmpty())
			throw new AxisFault(Messages.getMessage("noClassForService00", cfcName));

		// Convert the params
		cfArgStructData args = prepareArgs(session, method, argValues);

		// Execute the cfc
		cfData rtn = invokeComponentMethod(session, method, args, cfc);
		if (rtn == null || rtn instanceof cfNullData)
			return null;
		else {
			return tagUtils.getNatural(rtn, true, true, true);
		}
	}

	private cfData invokeComponentMethod(cfSession session, cfStructData method, cfArgStructData args, cfComponentData component) throws cfmRunTimeException {
		String methodName = method.getData("NAME").toString();
		cfcMethodData invocationData = new cfcMethodData(session, methodName, args);
		return component.invokeComponentFunction(session, invocationData);
	}

	private cfArgStructData prepareArgs(cfSession session, cfStructData method, Object[] argValues) throws AxisFault {
		cfArgStructData rtn = new cfArgStructData();
		if (argValues != null && argValues.length > 0) {
			cfArrayData params = (cfArrayData) method.getData("PARAMETERS");
			if (params == null || params.size() != argValues.length)
				throw new AxisFault("Incorrect number of parameters for method: " + method.getData("NAME").toString());

			for (int i = 1; i <= params.size(); i++) {
				cfData d = null;
				if (argValues[i - 1] instanceof String && tagUtils.isXmlString((String) argValues[i - 1])) {
					try {
						d = cfXmlData.parseXml(argValues[i - 1], true, null);
					} catch (cfmRunTimeException ex) {
						// Do nothing, just move on
					}
				}
				if (d == null) {
					d = tagUtils.convertToCfData(argValues[i - 1]);
				}
				rtn.setData(((cfStructData) params.getElement(i)).getData("NAME").toString(), d);
			}
		}
		return rtn;
	}

}
