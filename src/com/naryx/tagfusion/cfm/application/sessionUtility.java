/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: $
 */

package com.naryx.tagfusion.cfm.application;

import javax.servlet.http.Cookie;

import com.naryx.tagfusion.cfm.cookie.cfCookieData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;

public class sessionUtility extends Object {
	private static int cfid = 0;

	public String CFID;
	public String CFTOKEN;

	boolean bCookieSet = false, bSessionFromUrl = false, setDomainCookies = false;

	public sessionUtility(cfSession Session, boolean _setDomainCookies) {

		setDomainCookies = _setDomainCookies;

		cfStringData cCFID = null;
		cfStringData cCFTOKEN = null;

		bSessionFromUrl = true;

		// Check to see if they are in the [url.] first
		cfData urlData = Session.getQualifiedData(variableStore.URL_SCOPE);
		cCFID = (cfStringData) urlData.getData("cfid");
		cCFTOKEN = (cfStringData) urlData.getData("cftoken");

		if (cCFID == null || cCFTOKEN == null) {
			// Check to see if they are in the [form.]
			cfData formData = Session.getQualifiedData(variableStore.FORM_SCOPE);
			cCFID = (cfStringData) formData.getData("cfid");
			cCFTOKEN = (cfStringData) formData.getData("cftoken");

			if (cCFID == null || cCFTOKEN == null) {
				// Attempt to get them from a COOKIE
				cfData cookie = Session.getQualifiedData(variableStore.COOKIE_SCOPE);
				if (cookie != null) {
					cCFID 					= (cfStringData) cookie.getData("cfid");
					cCFTOKEN 				= (cfStringData) cookie.getData("cftoken");
					bCookieSet 			= true;
					bSessionFromUrl = false;
				}
			}
		}

		if (cCFID == null || cCFTOKEN == null || (cCFID != null && cCFID.getLength() == 0) || (cCFTOKEN != null && cCFTOKEN.getLength() == 0)) {
			// Generate new ones
			bCookieSet = false;
			bSessionFromUrl = false;
			CFID = (cfid++) + "";
			CFTOKEN = generateCFTOKEN(Session);
		} else {
			// Use old ones
			CFID = cleanString(cCFID.getString());
			CFTOKEN = cleanString(cCFTOKEN.getString());
		}
	}

	public boolean IsSessionFromCookie() {
		return bCookieSet;
	}

	public String urlToken() {
		return "CFID=" + CFID + "&CFTOKEN=" + CFTOKEN;
	}
	
	public String getTokenShort(){
		return CFID + ":" + CFTOKEN;
	}

	public String cleanString(String str) {
		if (str == null)
			return null;

		int c1 = str.indexOf(",");
		if (c1 == -1)
			return str;
		else
			return str.substring(0, c1);
	}

	public void setCookie(cfSession _Session) throws cfmRunTimeException {
		if (!bCookieSet && !bSessionFromUrl) { // don't overwrite existing cookies, don't set cookies if CFID/CFTOKEN from URL
			cfCookieData cookieHolder = (cfCookieData) _Session.getQualifiedData(variableStore.COOKIE_SCOPE);

			Cookie newCookie = new Cookie("CFID", CFID);
			newCookie.setMaxAge(9 * 365 * 24 * 60 * 60);
			newCookie.setPath("/");
			cookieHolder.setData(newCookie, true);

			newCookie = new Cookie("CFTOKEN", CFTOKEN);
			newCookie.setMaxAge(9 * 365 * 24 * 60 * 60);
			newCookie.setPath("/");
			cookieHolder.setData(newCookie, true);

			if (setDomainCookies) {
				cookieHolder.setDomainCookie(_Session, "CFTOKEN");
				cookieHolder.setDomainCookie(_Session, "CFID");
			}
		}
	}

	private static String generateCFTOKEN(cfSession _Session) {
		if (cfApplicationManager.cftokenUUID) {
			return com.nary.util.UUID.generateKey();
		}

		// generate a random number between 10,000,000 and 99,999,999
		int cftoken = 0;
		while (cftoken < 10000000) {
			cftoken = new java.util.Random().nextInt(99999999);
		}
		return String.valueOf(cftoken);
	}
}
