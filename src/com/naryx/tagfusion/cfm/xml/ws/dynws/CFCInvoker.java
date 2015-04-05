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

/*
 * Created on Jan 22, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.naryx.tagfusion.cfm.xml.ws.dynws;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfcMethodData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.ws.dynws.convert.TypeConverter;

public class CFCInvoker {
	private static Map threadMap = new FastMap(); // references to threadMap must
																								// be synchronized

	private cfComponentData cfc = null;

	private cfSession session = null;

	public CFCInvoker(cfComponentData cfc, cfSession s) {
		this.cfc = cfc;
		this.session = s;
	}

	public Object invoke(String functionName, String[] argNames, Object[] argVals, Class rtnType, ClassLoader cl) throws cfmRunTimeException {
		// Get the function
		cfStructData m = getFunction(functionName);
		if (m == null)
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.classError", "Invalid operation name. Function " + functionName + " not found on " + cfc.getComponentName()));

		// Convert the params
		cfArgStructData args = prepareArgs(m, argNames, argVals);

		// Execute the cfc function
		cfData rtn = invokeComponentMethod(m, args);

		// Make sure per request connections are closed (bug #3174)
		session.sessionEnd();

		if (rtn == null || rtn instanceof cfNullData) {
			return null;
		} else {
			Object myobj = TypeConverter.toWebServiceType(rtn, rtnType, cl);
			return myobj;
		}
	}

	public static CFCInvoker getCFCInvoker(Thread th) {
		synchronized (threadMap) {
			return (CFCInvoker) threadMap.remove(th);
		}
	}

	public static void associate(Thread th, CFCInvoker cfcInvoker) {
		synchronized (threadMap) {
			threadMap.put(th, cfcInvoker);
		}
	}

	public static void disassociate(Thread th) {
		synchronized (threadMap) {
			threadMap.remove(th);
		}
	}

	protected cfStructData getFunction(String name) {
		cfStructData fxn = null;
		cfStructData rtn = null;
		cfStructData md = cfc.getMetaData();
		List prevParents = new LinkedList();
		outer: while (md != null) {
			cfArrayData fxns = (cfArrayData) md.getData("FUNCTIONS");
			for (int i = 1; i <= fxns.size(); i++) {
				fxn = (cfStructData) fxns.getElement(i);
				if (fxn != null) {
					if (fxn.getData("NAME").toString().trim().equalsIgnoreCase(name.trim())) {
						// Found it
						rtn = fxn;
						break outer;
					}
				}
			}
			md = getSuperMetaData(md, prevParents);
		}
		return rtn;
	}

	protected cfStructData getSuperMetaData(cfStructData localMd, List prevParents) {
		cfStructData smd = (cfStructData) localMd.getData("EXTENDS");
		if (smd != null) {
			if (!prevParents.contains(smd.getData("NAME").toString())) {
				prevParents.add(smd.getData("NAME").toString());
				return smd;
			}
		}
		return null;
	}

	protected cfData invokeComponentMethod(cfStructData method, cfArgStructData args) throws cfmRunTimeException {
		String methodName = method.getData("NAME").toString();
		cfcMethodData invocationData = new cfcMethodData(session, methodName, args);
		return cfc.invokeComponentFunction(session, invocationData);
	}

	protected cfArgStructData prepareArgs(cfStructData method, String[] argNames, Object[] argValues) throws cfmRunTimeException {
		if (argNames == null || argValues == null || argNames.length != argValues.length)
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Invalid operation parameters."));

		cfArgStructData rtn = new cfArgStructData();
		if (argValues.length > 0) {
			cfArrayData params = (cfArrayData) method.getData("PARAMETERS");
			if (params == null || params.size() != argValues.length)
				throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Incorrect number of parameters for operation: " + method.getData("NAME").toString() + " . Expecting " + params.size() + " parameters."));

			for (int i = 0; i < argNames.length; i++) {
				cfData d = TypeConverter.toBDType(argValues[i], this.session);
				rtn.setData(argNames[i], d);
			}
		}
		return rtn;
	}

}
