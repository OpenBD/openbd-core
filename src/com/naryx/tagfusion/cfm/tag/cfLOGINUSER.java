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

/**
 * This class implements the <cfloginuser> tag.
 */

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import com.nary.net.Base64;
import com.nary.security.SessionLoginToken;
import com.nary.util.FastMap;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.application.cfAPPLICATION;
import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.application.cfSessionData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;

public class cfLOGINUSER extends cfTag implements Serializable
{
  static final long serialVersionUID = 1;
  
  public static final String DATA_BIN_KEY = "CFLOGINUSER_DATA";
  
  static final String ATT_NAME_NAME 	= "NAME";
  static final String ATT_NAME_PASSWORD = "PASSWORD";
  static final String ATT_NAME_ROLES 	= "ROLES";
  
  /**
   * set default values for the tag's attributes
   * @param _tag The String representation of the opening tag.
   * 
   */
   protected void defaultParameters(String _tag) throws cfmBadFileException
   {
	 //read in all name/value pairs for all attributes of the tag
	 parseTagHeader(_tag);
   }
   
   private void validateAttributes(cfSession _Session) throws cfmBadFileException
   {
     if(!containsAttribute(ATT_NAME_NAME))
		throw newBadFileException( "Missing Attribute", "The " +ATT_NAME_NAME+ " attribute must be specified." );
		
	 if(!containsAttribute(ATT_NAME_PASSWORD))
		throw newBadFileException( "Missing Attribute", "The " +ATT_NAME_PASSWORD+ " attribute must be specified." );
			
	 if(!containsAttribute(ATT_NAME_ROLES))
		throw newBadFileException( "Missing Attribute", "The " +ATT_NAME_ROLES+ " attribute must be specified." );	
   }
   
   /**
	 * 
	 * @param _Session
	 */
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException
	{
		validateAttributes(_Session);
		
		//get some attribute values for this tag
		String name     = getDynamic(_Session, ATT_NAME_NAME).getString();
		String pass     = getDynamic(_Session, ATT_NAME_PASSWORD).getString();
		String appToken = null;
		long rolesLifespan = -1; //how long (in milliseconds) the roles will remain valid in the Security scope
		
		//get some attribute values of the parent tag [<cflogin>]
		Map<String, cfData> map = null;
		Object o = _Session.getDataBin( cfLOGIN.DATA_BIN_KEY );
		if(o != null)
		{
			map = (Map<String, cfData>) o;
			appToken = map.get(cfLOGIN.ATT_NAME_APPTOKEN).toString();
		}
		
		else
		{
			/*
			  if we get here, then this is a special case where the cfloginuser tag
			  is being used outside of the cflogin tag, by itself.
			  Macromedia's livedocs say this about it:
			  "you can use cfloginuser outside of cflogin, but it it only applies for the life of the request."
			  It seems this would be done  if the user's browser does not support cookies.
			  "You must provide your own authentication mechanism and call cfloginuser on each page on which you use ColdFusion login identification."
			*/
			appToken = cfLOGIN.getAppName(_Session);
		}
		
		//<username>:<password>:<applicationToken>
		String loginTokenValue = name+ ':' +pass+ ':' +appToken; 
		
		//now encrypt/encode it
		String loginTokenValueEncoded = Base64.base64Encode(loginTokenValue);
	
		//put the encoded token value into the cfSession so that
		//GetAuthUser() can find it if necessary, otherwise GetAuthUser()
		//won't be able to get the user until the NEXT request, when the cookie comes back to the server.
		_Session.setDataBin(DATA_BIN_KEY, loginTokenValueEncoded);
		
		//prepare the roles information for usage
		String roles = getDynamic(_Session, ATT_NAME_ROLES).getString();
		
		//put the comma-separated roles into a HashMap
		Map<String, String> map2 = new FastMap<String, String>();
		List<String> tokens = string.split(roles, ",");

		for ( int i = 0; i < tokens.size(); i++ )
		{
		  map2.put(tokens.get(i), ""); //only the key is used
		}
		
		if(map2.isEmpty())
		  map2.put("", ""); //part of the fix for bug #1973
		
		if(map != null)
		{ 
			cfApplicationData appData = _Session.getApplicationData();
			//appData may be null
			
			String loginStorageType = cfLOGIN.getLoginStorageType(appData);
	
			//test for loginStorage=="session"
			if(cfAPPLICATION.ALT_LOGIN_STORAGE_1.equalsIgnoreCase(loginStorageType))
			{
				//login token is/will-be an attribute in the session scope (which may be a J2EE session scope or may be the CF session scope)
				cfSessionData session = (cfSessionData) _Session.getQualifiedData(variableStore.SESSION_SCOPE);
				if(session != null)
				{
					SessionLoginToken token = new SessionLoginToken(loginTokenValueEncoded);
					session.setData(cfLOGIN.getLoginSessionAttributeName(), token);
				}
			}
	
			else //the default (login token is/will-be a cookie)
			{
				Cookie loginTokenCookie = new Cookie(cfLOGIN.getLoginCookieName(appData), loginTokenValueEncoded);
				
				//now set any special attributes on the cookie
				loginTokenCookie.setMaxAge(-1); //here's the fix for bug #2309 (leaving rolesLifespan alone too)
					
				Object o2 = map.get(cfLOGIN.ATT_NAME_COOKIEDOMAIN);
				if(o2 != null)
				{
					cfData data = (cfData) o2;
					String cookieDomain = data.getString();
					loginTokenCookie.setDomain(cookieDomain);
				}
				
				//With CFMX 6.1 the cookie path always seems to be '/', so we'll do the same
				loginTokenCookie.setPath("/");
				
				_Session.RES.addCookie(loginTokenCookie);
			}
		}
		else
		{
			//special case... cfloginuser outside the cflogin tag, do nothing
		}
		
		//now store the roles in the Security scope using the encrypted tokenValue as the key
	  	_Session.setDataInSecurityStore(loginTokenValueEncoded, map2, rolesLifespan);

		return super.render(_Session);
	}
}
