/* 
 *  Copyright (C) 2000 - 2009 TagServlet Ltd
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

package com.naryx.tagfusion.cfm.cookie;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


/*
 * The function version of CFCOOKIE
 */

public class SetCookie extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public SetCookie() {
		min = 1; max = 7;
		setNamedParams( new String[]{ "name", "value", "expires", "path", "domain", "secure", "httponly" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"The NAME of the cookie. OpenBD will automatically convert this to UPPERCASE",
			"The value of this cookie.  This value will be automatically encoded for you.  You can omit this parameter if you are deleting the cookie. Value cannot be more than 4096 bytes in size",
			"How long do you want this cookie to persist for.  You can pass in a date in the future, or the number of seconds from this time on, or 'NEVER' if you want to cookie to persist as long as possible.  If you specify 'NOW' this expires the cookie immediately.  If don't specify a value, it will die when the user kills the browser window.",
			"The scope of the path that this cookie will be sent back to the browser for",
			"The sub-domain that this cookie will be set for",
			"Flag to determine if the cookie should only be sent on a secure connection",
			"Flag to advise the browser that this cookie should not be accessible from Javascript"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"This function is used to set and delete outgoing HTTP cookies that can be sent from the CFML application.  You can read cookies using the 'cookie' variable scope.  Cookies are limited to 20 per domain, each one being a maxium of 4KB in size.", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String cookiename = getNamedStringParam( argStruct, "name", null );
		if ( cookiename == null )
			throwException(_session, "You have to provide a NAME parameter" );
		
		
		String VALUE 				= getNamedStringParam( argStruct, "value", null );
		String PATH 				= getNamedStringParam( argStruct, "path", null );
		String DOMAIN 			= getNamedStringParam( argStruct, "domain", null );
		boolean bSecure			= getNamedBooleanParam(argStruct, "secure", false );
		boolean bHttpOnly		= getNamedBooleanParam(argStruct, "httponly", false );
		cfData expiresData	= getNamedParam(argStruct, "expires", new cfNumberData(-1) );
		
		//--[ Set the cookie
 		cfCOOKIE.addCookie( _session, cookiename, VALUE, expiresData, PATH, DOMAIN, bSecure, bHttpOnly );
		
		return cfBooleanData.TRUE;
	}
}