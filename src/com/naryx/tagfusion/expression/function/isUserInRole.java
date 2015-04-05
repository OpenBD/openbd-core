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
 * Created on Aug 26, 2004
 *
 */
package com.naryx.tagfusion.expression.function;

import java.util.List;
import java.util.Map;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * In order for this function to work properly, it must execute with the same
 * application settings that were used on the page where the user was logged in
 * via &lt;cfloginuser> Specifically, the appName and loginStorage values must
 * be the same.
 * 
 */
public class isUserInRole extends functionBase {
	private static final long serialVersionUID = 1L;

	public isUserInRole() {
		min = max = 1; // set the number of arguments allowed
	}

	public String[] getParamInfo(){
		return new String[]{
			"username"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"security", 
				"Determines if the user specified is logged in (CFLOGINUSER)", 
				ReturnType.BOOLEAN );
	}
 	
	
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		boolean isUserInRole = false;
		String loginTokenValueEncoded = getAuthUser.getLoginTokenValue(_session);

		if (loginTokenValueEncoded != null) {
			Map<String, String> rolesForCurrentAuthUser = _session.getDataFromSecurityStore(loginTokenValueEncoded);
			if (rolesForCurrentAuthUser != null) {
				cfData data2 = parameters.get(0);
				if (data2 != null) {
					String rolesToCheck = data2.getString();
					List<String> tokens = string.split(rolesToCheck, ",");

					if (tokens.size() > 0) {
						synchronized (rolesForCurrentAuthUser) {
							for (int i = 0; i < tokens.size(); i++) {
								String role = tokens.get(i);
								isUserInRole = rolesForCurrentAuthUser.containsKey(role);
								if (isUserInRole) {
									break;
								}
							}
						}
					} else
						// part of the fix for bug #1973
						isUserInRole = rolesForCurrentAuthUser.containsKey("");
				}
			}
		}

		return cfBooleanData.getcfBooleanData(isUserInRole);
	}
}
