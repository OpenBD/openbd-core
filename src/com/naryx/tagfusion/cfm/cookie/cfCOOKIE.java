/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  $Id: cfCOOKIE.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.cookie;

import java.io.Serializable;

import javax.servlet.http.Cookie;

import com.nary.net.http.urlEncoder;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfCOOKIE extends cfTag implements Serializable{
  static final long serialVersionUID = 1;

	public java.util.Map getInfo(){
		return createInfo(
				"output", 
				"This tag is used to set and delete outgoing HTTP cookies that can be sent from the CFML application.  You can read cookies using the 'cookie' variable scope.  Cookies are limited to 20 per domain, each one being a maxium of 4KB in size.");
	}
  
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
				createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
				createAttInfo("NAME", "The NAME of the cookie. OpenBD will automatically convert this to UPPERCASE", "", true ),
				createAttInfo("VALUE", "The value of this cookie.  This value will be automatically encoded for you.  You can omit this parameter if you are deleting the cookie. Value cannot be more than 4096 bytes in size", "", false ),
				createAttInfo("EXPIRES", "How long do you want this cookie to persist for.  You can pass in a date in the future, or the number of seconds from this time on, or 'NEVER' if you want to cookie to persist as long as possible.  If you specify 'NOW' this expires the cookie immediately.  If don't specify a value, it will die when the user kills the browser window.", "", false ),
				createAttInfo("SECURE", "Flag to determine if the cookie should only be sent on a secure connection", "false", false ),
				createAttInfo("HTTPONLY", "Flag to advise the browser that this cookie should not be accessible from Javascript", "false", false ),
				createAttInfo("PATH", "The scope of the path that this cookie will be sent back to the browser for", "/", false ),
				createAttInfo("DOMAIN", "The sub-domain that this cookie will be set for", "", false )
		};

	}

  
  protected void defaultParameters( String _tag ) throws cfmBadFileException {
    defaultAttribute( "EXPIRES",	"-1" );
    defaultAttribute( "SECURE", 	"No" );
    defaultAttribute( "HTTPONLY", "No" );
    
    parseTagHeader( _tag );
    
    if ( !containsAttribute("NAME") )
	    throw newBadFileException( "Missing NAME", "You need to provide a NAME for the cookie" );
    	
		setFlushable( false );
  }
  
 	/** 
   * Creates a new cookie and gives it the values specified by the user.
   * This includes value and setting of expiry date.
   *
   * @param _Session		The current session associated with this tag
   *
   * @throws						cfmRuntimeException
   */
 	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		String cookieName = getDynamic(_Session, "NAME").getString();
		
 		//--[ Set the value
 		String VALUE = null;
 		if ( containsAttribute("VALUE") )
			VALUE =	getDynamic( _Session, "VALUE" ).getString(); 
 		
 		
		//--[ Set the expiration
		cfData expiresData	= getDynamic( _Session, "EXPIRES");


		//--[ Set the Security
		boolean bSecure 	= getDynamic( _Session, "SECURE").getBoolean();
		boolean bHttpOnly = getDynamic( _Session, "HTTPONLY").getBoolean();
		
		
		//--[ Set the path
		String PATH = null;
		if ( containsAttribute( "PATH" ) )
      PATH = getDynamic( _Session, "PATH").getString();
    
		
		//--[ Set the domain
		String DOMAIN = null;
    if ( containsAttribute("DOMAIN") )
    	DOMAIN = getDynamic( _Session, "DOMAIN").getString();
			
		//--[ Set the cookie
 		addCookie( _Session, cookieName, VALUE, expiresData, PATH, DOMAIN, bSecure, bHttpOnly );
 		
 		return cfTagReturnType.NORMAL;
	}
 	
 	
 	public static void addCookie( cfSession _Session, String cookieName, String value, cfData expiresData, String path, String domain, boolean bSecure, boolean bHttpOnly ) throws IllegalArgumentException, cfmRunTimeException {
 		Cookie  newCookie = new Cookie( cookieName.toUpperCase(), "" );
 		
		//--[ Set the value
 		String VALUE = "";
 		if ( value != null )
			VALUE =	urlEncoder.encode( value ); 
 		
		newCookie.setValue( VALUE );
   
    
		//--[ Set the expiration
    if ( expiresData.getDataType() == cfData.CFDATEDATA ){
      int age = (int) ( ( ( (cfDateData) expiresData).getLong() - System.currentTimeMillis() ) / 1000 );
      newCookie.setMaxAge( age < 0 ? 0 : age );
    }else{
      String expiresStr = expiresData.getString();
    
      if ( expiresStr.indexOf("/") != -1 ){
        int expSeconds = com.nary.util.Date.secondDifference(expiresStr);
        newCookie.setMaxAge( expSeconds < 0 ? 0 : expSeconds );
      }else if ( expiresStr.equalsIgnoreCase("NOW") ){
        newCookie.setMaxAge( 0 );	
      }else if ( expiresStr.equalsIgnoreCase("NEVER") ){
        newCookie.setMaxAge( 86400 * 365 * 20 );
      }else{
        int expires = com.nary.util.string.convertToInteger( expiresStr, -1 );
        newCookie.setMaxAge( expires < 0 ? -1 : expires * 86400 );
      }
		}
    
    
		//--[ Set the Security
		newCookie.setSecure( bSecure );

    
		//--[ Set the path
    if ( path != null ){
      newCookie.setPath( path );
    }else{
      newCookie.setPath( _Session.REQ.getContextPath() + "/" );
    }
    

		//--[ Set the domain
    if ( domain != null )
			newCookie.setDomain( domain );

 		
		//--[ Set the tag
 		cfCookieData	cookieHolder = (cfCookieData)_Session.getQualifiedData( variableStore.COOKIE_SCOPE );
 		cookieHolder.setData( newCookie, bHttpOnly );
 	}	
}