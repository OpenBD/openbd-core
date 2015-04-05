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
 * Macromedia livedocs for CFMX 6.1 say this about GetAuthUser():
 * ---
 * This function works with cflogin authentication or web server authentication. It checks for a logged-in user as follows:
 *
 *  1. It checks for a login made with cfloginuser.
 *  2. If no user was logged in with cfloginuser, it checks for a web server login (cgi.remote_user).

 * ---
 */
package com.naryx.tagfusion.expression.function;

import java.util.List;
import java.util.Map;

import com.nary.net.Base64;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfLOGIN;
import com.naryx.tagfusion.cfm.tag.cfLOGINUSER;

/**
 * In order for this function to work properly, it must execute with the same
 * application settings that were used on the page where the user was logged in
 * via &lt;cfloginuser> Specifically, the appName and loginStorage values must
 * be the same.
 * 
 */
public class getAuthUser extends functionBase {
	private static final long serialVersionUID = 1L;

	public getAuthUser() {
		min = max = 0;
	}

	public java.util.Map getInfo(){
		return makeInfo(
				"security", 
				"Returns the currently logged in authenicated user", 
				ReturnType.STRING );
	}
	
	public static String getLoginTokenValue(cfSession _session) {
		String loginTokenValue = null;
		Object o = _session.getDataBin(cfLOGINUSER.DATA_BIN_KEY); // this was set by
																															// cfLOGINUSER.render()

		if (o != null)
			loginTokenValue = (String) o;
		else
			loginTokenValue = cfLOGIN.getLoginTokenValue(_session); // look in the
																															// cookie or J2EE
																															// session scope

		return loginTokenValue;
	}

	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		String username = null;
		String loginTokenValue = getLoginTokenValue(_session);

		if (loginTokenValue != null) {
			// make sure that there are some roles defined for the user... if not then
			// they are not logged in
			Map<String, String> data = _session.getDataFromSecurityStore(loginTokenValue);
			if (data != null) {
				// decode it
				String loginTokenValueDecoded = Base64.base64Decode(loginTokenValue);

				if (loginTokenValueDecoded != null) {
					// loginTokenValueDecoded now represents
					// "&lt;username>:&lt;password>:&lt;applicationTokenValue>"

					/*
					 * To be truly correct here, we should validate the
					 * <applicationTokenValue> to ensure that it matches the value of the
					 * appToken attribute of the cflogin tag [see cfLOGIN.isUserLoggedIn()
					 * for more details of this] But this function has no way to learn the
					 * value of that attribute so we can't do that here.
					 */
					int pos = loginTokenValueDecoded.indexOf(":");

					if (pos > 0)
						username = loginTokenValueDecoded.substring(0, pos);
				}
			}
		}

		/*
		 * The next bit of code would support the 2nd requirement of this function,
		 * as stated by the Macromedia livedocs for CFMX 6.1:
		 * "If no user was logged in with cfloginuser, it checks for a web server login (cgi.remote_user)."
		 * But it's been commented out for the following reason:
		 * 
		 * Calling HttpServletRequest.getRemoteUser() on the underlying servlet
		 * container can give different results depending on the container and the
		 * request. For example if the request includes a Basic Authorization header
		 * , SE's getRemoteUser() will give the username even if they've not been
		 * logged-in in SE. CFMX 6.1 gives an empty string.
		 * 
		 * Paul recommended that the actual behavior of CFMX 6.1 be mimicked in this
		 * case.
		 */
		/*
		 * if(username == null) username = _session.REQ.getRemoteUser();
		 */

		if (username == null)
			username = "";

		return new cfStringData(username);
	}
}
