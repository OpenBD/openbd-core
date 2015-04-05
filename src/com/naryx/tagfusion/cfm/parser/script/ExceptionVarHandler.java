/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

package com.naryx.tagfusion.cfm.parser.script;

import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.CFException;
import com.naryx.tagfusion.cfm.parser.cfLData;

public class ExceptionVarHandler  implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private cfLData preExistRef; // a reference for restoring the preexiting cfcatchdata
	private cfData preExistVal;

	private cfSession session;
	private CFContext context;
	private String varname;

	public static final String CFCATCH_DATABIN = "cfcatch";

	public ExceptionVarHandler(cfSession _session, String _varname) {
		session = _session;
		context = session.getCFContext();
		varname = _varname;
	}

	public void setExceptionVariable(cfCatchData _catchData)
			throws cfmRunTimeException {
		// we need to store it in the databin for the rethrow statement so it can
		// find it
		session.setDataBin(CFCATCH_DATABIN, _catchData);

		if (varname.length() == 0)
			return;

		if (!context.isCallEmpty()) { // if there is a local scope

			if (context.containsLocal(varname)) {
				cfLData var = context.getLocal(varname);
				cfData val = var.Get(context);

				// throw an exception if another local variable of the same name exists
				// that is not one created by a parent catch block
				if (!(val instanceof cfCatchData)) {
					throw new CFException("The local variable " + varname
							+ " cannot be declared twice.", context);
				} else {
					// save these so we can restore them later
					preExistRef = var;
					preExistVal = val;
				}
				var.Set(_catchData, context);

			} else { // otherwise set the exception variable in the local scope
				context.getLocal(varname).Set(_catchData, context);
			}

		} else {
			cfStructData varScope = (cfStructData) session.getQualifiedData(variableStore.VARIABLES_SCOPE);
			preExistVal = (cfData) varScope.getData(varname); // might be null if it doesn't exist but that's ok
			varScope.setData(varname, _catchData);
		}

	}

	public void deleteExceptionVariable() throws cfmRunTimeException {
		session.deleteDataBin(CFCATCH_DATABIN);

		if (varname.length() == 0)
			return;

		if (preExistRef != null) { // restore pre-existing local variable
			preExistRef.Set(preExistVal, context);

		} else if (preExistVal != null) { // restore pre-existing variable in variable scope
			cfStructData varScope = (cfStructData) session.getQualifiedData(variableStore.VARIABLES_SCOPE);
			varScope.setData(varname, preExistVal);

		} else {
			// otherwise, if a local scope exists remove the catch variable
			if (!context.isCallEmpty()) {
				try {
					context.getLocal(varname).Delete(context);
				} catch (cfmRunTimeException ignored) {
				} // var should exist
			} else {
				cfStructData varScope = (cfStructData) session.getQualifiedData(variableStore.VARIABLES_SCOPE);
				varScope.deleteData(varname);
			}
		}
	}
}