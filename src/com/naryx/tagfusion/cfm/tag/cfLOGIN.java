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

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.nary.net.Base64;
import com.nary.security.SessionLoginToken;
import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.application.cfAPPLICATION;
import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.application.cfSessionData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfFormData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;

public class cfLOGIN extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

	public static final String DATA_BIN_KEY = "CFLOGIN_DATA";
	static final String ATT_NAME_IDLETIMEOUT = "IDLETIMEOUT";
	static final String ATT_NAME_APPTOKEN = "APPLICATIONTOKEN";
	static final String ATT_NAME_COOKIEDOMAIN = "COOKIEDOMAIN";
	static final int DEFAULT_IDLE_TIMEOUT = 1800;

	/**
	 * Testing with CFMX 6.1 revealed that if the application is not named (via
	 * <cfapplication name="myApp">) Then the Login cookie is given the name
	 * "CFAUTHORIZATION_"
	 * 
	 * If it IS named... for example using &lt;cfapplication name="myApp"> then
	 * CFMX 6.1 will name the login cookie "CFAUTHORIZATION_myApp"
	 * 
	 * CFMX 6.1 does not appear to honor/use the value of the appToken attribute
	 * at all
	 */
	static final String DEFAULT_LOGIN_COOKIE_NAME = "CFAUTHORIZATION_";

	/**
	 * This is the key used to store the base64 encoded form of
	 * "&lt;username>:&lt;password>:&lt;applicationTokenValue>" in the session
	 * With CFMX 6.1 the key is "cfauthorization"
	 */
	static final String DEFAULT_LOGIN_SESSION_ATTRIBUTE_NAME = "cfauthorization";

	/**
	 * set default values for the tag's attributes
	 * 
	 * @param _tag
	 *          The String representation of the opening tag.
	 * 
	 */
	protected void defaultParameters(String _tag) throws cfmBadFileException {
		// set default values for all tag attributes whose default values we know at
		// this point
		defaultAttribute(ATT_NAME_IDLETIMEOUT, DEFAULT_IDLE_TIMEOUT);

		// now read in all name/value pairs for all attributes of the tag
		// (potentially overwriting default values)
		parseTagHeader(_tag);
	}

	/**
	 * This method is used to perform further attribute defaulting, when the
	 * cfSession object is needed in order to figure out the default value.
	 * 
	 * @param _Session
	 * @throws cfmRunTimeException
	 */
	private void defaultParametersAdvanced(cfSession _Session) {
		unclipAttributes();
		if (!containsAttribute(ATT_NAME_APPTOKEN)) {
			// if we get here, the cflogin tag did not make use of the
			// applicationToken attribute
			String appToken = getAppName(_Session);
			defaultAttribute(ATT_NAME_APPTOKEN, appToken);
		}
	}

	public static String getAppName(cfSession _Session) {
		cfApplicationData appData = _Session.getApplicationData();
		if (appData != null)
			return appData.getAppName();
		else
			return cfAPPLICATION.UNNAMED_APPNAME;
	}

	/**
	 * 
	 * @param _Session
	 * @return the cflogin structure or null
	 */
	public static cfStructData extractUserPass(cfSession _Session) {
		HttpServletRequest req = _Session.REQ;
		boolean gotUsername = false;
		boolean gotPassword = false;
		String nameKey = "name";
		String passKey = "password";
		cfStructData userPass = new cfStructData();

		// setup default values
		userPass.put(nameKey, "");
		userPass.put(passKey, "");

		// 1st look for the j_username and j_password request parameters (CFMX 6.1
		// can get them for both POST and GET requests, so we should too)
		// do it in a way that the cAsE of the parameter names won't matter since
		// CFMX 6.1 does not care about case... (i.e. don't just use
		// req.getParameter() directly)

		// if the parameters were sent via a POST request on a form then we need to
		// look for them in the FORM scope, since they won't be in the request
		// parameters (BD wraps the request object and
		// does special logic in this case).
		cfFormData formData = (cfFormData) _Session.getQualifiedData(variableStore.FORM_SCOPE);
		if (formData != null && formData.size() > 0) {
			// cfformdata is not case-sensitive so we can just use lower-case here.
			if (formData.containsKey("j_username")) {
				gotUsername = true;
				userPass.put(nameKey, formData.getData("j_username"));
			}

			if (formData.containsKey("j_password")) {
				gotPassword = true;
				userPass.put(passKey, formData.getData("j_password"));
			}

			// got at least one
			if (gotUsername || gotPassword)
				return userPass;
		}

		// if we get here, we got neither so try to get username and password a
		// different way...

		// if the parameters were sent via a GET request (querystring on the URL)
		// then we need to look for them in the request parameters, since that's
		// where they
		// will be (i.e. they won't be in the FORM scope).

		Enumeration<String> enumer = req.getParameterNames();

		while (enumer.hasMoreElements()) {
			String name = enumer.nextElement();
			String lName = name.toLowerCase();
			boolean isUsername = false;
			boolean isPassword = false;

			if ((isUsername = lName.equals("j_username")) || (isPassword = lName.equals("j_password"))) {
				String key = null;
				String val = req.getParameter(name);

				if (isUsername && !gotUsername) // only honor the first one sent
				{
					gotUsername = true;
					key = nameKey;
				} else if (isPassword && !gotPassword) // only honor the first one sent
				{
					gotPassword = true;
					key = passKey;
				}

				if (key != null)
					userPass.put(key, val);
			}

			if (gotUsername && gotPassword)
				break;
		}

		// got at least one
		if (gotUsername || gotPassword)
			return userPass;

		// if we get here, we got neither so try to get username and password a
		// different way...

		// 2nd see if the user/pass has been sent using BASIC Authentication
		String authHeader = req.getHeader("authorization");
		if (authHeader != null && authHeader.trim().toUpperCase().startsWith("BASIC")) {
			StringBuilder username = new StringBuilder();
			StringBuilder password = new StringBuilder();

			if (usernamePassword(req, username, password)) {
				userPass.put(nameKey, username.toString());
				userPass.put(passKey, password.toString());
				return userPass;
			}
		}

		// if we get here we still have not gotten the username and password

		// 3rd see if the user/pass has been sent using NTLM Authentication
		/*
		 * CFMX 6.1 livedocs say that in this case: "ColdFusion gets the username
		 * from the web server and sets the cflogin.password value to the empty
		 * string"
		 */
		else if (authHeader != null && authHeader.trim().toUpperCase().startsWith("NTLM")) {
			String usernameFromWebContainer = req.getRemoteUser();
			if (usernameFromWebContainer == null)
				usernameFromWebContainer = "";

			userPass.put(nameKey, usernameFromWebContainer);
			userPass.put(passKey, "");
			return userPass;
		}

		// 4th see if the user/pass has been sent using DIGEST Authentication
		// not implemented at this time (SE 5.0 doesn't even implement this...
		// practically no one uses DIGEST auth)

		return null;
	}

	/**
	 * This method will extract the username and password from the Authorization
	 * request header, Decode them from their base-64 values and store them in the
	 * username and password parameters
	 * 
	 * @param req
	 * @param username
	 * @param password
	 * @return true if username and password were extracted, else false is
	 *         returned
	 */
	public static boolean usernamePassword(HttpServletRequest req, StringBuilder username, StringBuilder password) {
		int pos;
		String auth = req.getHeader("authorization");

		if (auth == null)
			return false;

		// Remove "Basic " from the beginning of the Authorization string
		pos = auth.indexOf(' ');
		if (pos == -1)
			return false;

		auth = auth.substring(pos + 1);

		String str = Base64.base64Decode(auth);

		pos = str.indexOf(':');

		// If we found the colon then extract the username and password.
		if (pos > 0) {
			username.append(str.substring(0, pos));
			password.append(str.substring(pos + 1));

			return true;
		}

		return false;
	}

	/**
	 * 
	 * @param _Session
	 */
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		if (!isUserLoggedIn(_Session)) {
			defaultParametersAdvanced(_Session);

			// get the username and password
			cfStructData userPass = extractUserPass(_Session);

			if (userPass != null) {
				// userPass.getData("name").getString();
				_Session.setData("cflogin", userPass); // expose the cflogin struct
			}

			// communicate some information to the <cfloginuser> subtag
			Map<String, cfData> map = new FastMap<String, cfData>();

			map.put(ATT_NAME_APPTOKEN, getDynamic(_Session, ATT_NAME_APPTOKEN));
			map.put(ATT_NAME_IDLETIMEOUT, getDynamic(_Session, ATT_NAME_IDLETIMEOUT));

			if (containsAttribute(cfLOGIN.ATT_NAME_COOKIEDOMAIN))
				map.put(ATT_NAME_COOKIEDOMAIN, getDynamic(_Session, ATT_NAME_COOKIEDOMAIN));

			_Session.setDataBin(DATA_BIN_KEY, map);
			// now any subtag will be able to see this information via
			// _Session.getDataBin(cdfLOGINUSER.DATA_BIN_KEY);

			super.render(_Session);

			if (userPass != null)
				_Session.deleteData("cflogin"); // remove the cflogin struct so that
																				// it's not visible after the closing
																				// tag
		}

		return cfTagReturnType.NORMAL;
	}

	/**
	 * 
	 * @param appData
	 * @return the value specified by the loginStorage attribute of the
	 *         application (&lt;cfapplication>). This would be either "COOKIE"
	 *         (the default) or "SESSION".
	 */
	public static String getLoginStorageType(cfApplicationData appData) {
		if (appData != null)
			return appData.getLoginStorage();
		else
			return cfAPPLICATION.DEFAULT_LOGIN_STORAGE;
	}

	/**
	 * 
	 * @param appData
	 * @return The name to use for the cookie whose value will be a base-64
	 *         encoded form of
	 *         "&lt;username>:&lt;password>:&lt;applicationTokenValue>" With CFMX
	 *         6.1 this is: <br>
	 *         "CFAUTHORIZATION_" + &lt;application name>
	 * 
	 */
	public static String getLoginCookieName(cfApplicationData appData) {
		if (appData != null)
			return DEFAULT_LOGIN_COOKIE_NAME + appData.getAppName();
		else
			return DEFAULT_LOGIN_COOKIE_NAME;
	}

	/**
	 * 
	 * @return the name to use for the session attribute whose value will be a
	 *         base-64 encoded form of
	 *         "&lt;username>:&lt;password>:&lt;applicationTokenValue>"
	 */
	public static String getLoginSessionAttributeName() {
		return DEFAULT_LOGIN_SESSION_ATTRIBUTE_NAME;
	}

	/**
	 * This method will search either the cookie scope or the session scope for
	 * the value.
	 * 
	 * @param _Session
	 * @return The value of the login token, which will be
	 *         &lt;username>:&lt;password>:&lt;applicationToken> (base64 encoded)
	 * @throws cfmRunTimeException
	 */
	public static String getLoginTokenValue(cfSession _Session) {
		String loginTokenValue = null;

		cfApplicationData appData = _Session.getApplicationData();
		// appData may be null

		String loginStorageType = getLoginStorageType(appData);

		// test for loginStorage=="session"
		if (cfAPPLICATION.ALT_LOGIN_STORAGE_1.equalsIgnoreCase(loginStorageType)) {
			// login token is/will-be an attribute in the session scope (which may be
			// a J2EE session scope or may be the CF session scope)
			cfSessionData session = (cfSessionData) _Session.getQualifiedData(variableStore.SESSION_SCOPE);
			if (session != null) {
				SessionLoginToken loginToken = (SessionLoginToken) session.getData(getLoginSessionAttributeName());
				if (loginToken != null)
					loginTokenValue = loginToken.toString();
			}
		}

		else // the default (login token is/will-be a cookie)
		{
			HttpServletRequest req = _Session.REQ;
			Cookie[] cookies = req.getCookies();

			if (cookies != null) {
				String cookieName = getLoginCookieName(appData);

				for (int i = 0; i < cookies.length; i++) {
					Cookie cookie = cookies[i];
					if (cookie.getName().equals(cookieName)) {
						loginTokenValue = cookie.getValue();
						break;
					}
				}
			}
		}

		return loginTokenValue;
	}

	/**
	 * 
	 * A user is logged in if the decoded loginTokenValue is found, and is found
	 * to be the correct value. This would be the case if:
	 * 
	 * A cookie with the right name is there, and the &lt;applicationToken>
	 * portion of its value is correct. OR A session attribute with the right name
	 * is there, and the &lt;applicationToken> portion of its value is correct.
	 * 
	 * @param _Session
	 * @return true if the user is already logged in, else false is returned.
	 * @throws cfmRunTimeException
	 */
	public boolean isUserLoggedIn(cfSession _Session) throws cfmRunTimeException {
		boolean loggedIn = false;
		String loginTokenValue = null;

		// part of the fix for bug #2008
		// 1st try getting the loginTokenValue this way, to cover the case that
		// <cfloginuser> just occured on the same page (or request)
		Object o = _Session.getDataBin(cfLOGINUSER.DATA_BIN_KEY); // this was set by
																															// cfLOGINUSER.render()
		if (o != null)
			loginTokenValue = (String) o;
		else
			// 2nd check for a cookie or a session attribute
			loginTokenValue = getLoginTokenValue(_Session);

		if (loginTokenValue != null) {
			// make sure that there are some roles defined for the user... if not then
			// they are not logged in
			Map<String, String> data = _Session.getDataFromSecurityStore(loginTokenValue);
			if (data != null) {
				String loginTokenValueDecoded = Base64.base64Decode(loginTokenValue);
				// loginTokenValueDecoded now represents
				// "&lt;username>:&lt;password>:&lt;applicationTokenValue>"
				// we need to ensure that <applicationTokenValue> matches the value of
				// the appToken attribute of the cflogin tag
				int lastColonPos = loginTokenValueDecoded.lastIndexOf(":");
				if (lastColonPos > 0) {
					String storedAppTokenValue = loginTokenValueDecoded.substring(lastColonPos + 1);
					cfData appToken = getDynamic(_Session, ATT_NAME_APPTOKEN);
					if (appToken != null) // testing appToken for null here is the fix for
																// bug #1867
						loggedIn = storedAppTokenValue.equals(appToken.toString());
					else
						// part of the fix for bug #2008
						loggedIn = storedAppTokenValue.equals(getAppName(_Session));
				}
			}
		}

		return loggedIn;
	}

	public String getEndMarker() {
		return "</CFLOGIN>";
	}

}
