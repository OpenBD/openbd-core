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

import com.naryx.tagfusion.cfm.application.cfAPPLICATION;
import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.application.cfSessionData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.expression.function.getAuthUser;

/**
 * In order for the &lt;cflogout> tag to function properly, it must execute
 * with the same application settings that were used on the page where the user was logged in via
 * &lt;cfloginuser>
 * Specifically, the appName and loginStorage values must be the same. 
 *
 */
public class cfLOGOUT extends cfTag implements Serializable
{
  static final long serialVersionUID = 1;
  
  private static void execute(cfSession _Session) throws cfmRunTimeException
  {
	String loginTokenValueEncoded = getAuthUser.getLoginTokenValue(_Session);
	
	//1st clean up the roles	
	if(loginTokenValueEncoded != null)
		_Session.removeDataFromSecurityStore(loginTokenValueEncoded);
		
	//2nd clean up the login token (cookie or session attribute)	
	cfApplicationData appData = _Session.getApplicationData();
	//appData may be null
	
	String loginStorageType = cfLOGIN.getLoginStorageType(appData);

	//test for loginStorage=="session"
	if(cfAPPLICATION.ALT_LOGIN_STORAGE_1.equalsIgnoreCase(loginStorageType))
	{
		//login token is/will-be an attribute in the session scope (which may be a J2EE session scope or may be the CF session scope)
		cfSessionData session = (cfSessionData) _Session.getQualifiedData(variableStore.SESSION_SCOPE);
		if(session != null)
			session.deleteData(cfLOGIN.getLoginSessionAttributeName());
	}
    
    /* This is commented out since it does not do what was hoped, and isn't a big deal anyway.
     * Leave it here as "documentation"
     */
    /*
	else //the default (login token is/will-be a cookie)
	{
		Cookie[] cookies = req.getCookies();
		String cookieName = cfLOGIN.getLoginCookieName(appData);

		for(int i=0; i<cookies.length; i++)
		{
			Cookie cookie = cookies[i];
			if(cookie.getName().equals(cookieName))
			{
				//Tell the browser to kill the cookie.
				//Unfortunatly this does not work as expected.
				//With Netscape 7.1 browser for example, the original cookie is
				//not removed. If setMaxAge(100) were used here instead, then Netscape
				//would have 2 cookies that are exactly the same except for their timeout values.  

				cookie.setMaxAge(0); 
				_Session.RES.addCookie(cookie);
			}
		}
	}	*/
  }
  
  public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException
  {
  	execute(_Session);
  	return super.render(_Session);
  }
}
