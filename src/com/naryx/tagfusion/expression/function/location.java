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
 *  http://www.openbluedragon.org/
 *  
 *  $Id: location.java 1661 2011-09-09 09:41:42Z alan $
 */

package com.naryx.tagfusion.expression.function;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class location extends functionBase {
  private static final long serialVersionUID = 1L;
	
  public location(){ 
  	min = 1; max = 3; 
  	setNamedParams( new String[]{ "url", "addtoken", "statuscode"} );
  }
  
	public String[] getParamInfo(){
		return new String[]{
			"url to redirect",
			"add session flag - default true",
			"status code to send - default 302"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"network", 
				"Redirects the main request to the location given", 
				ReturnType.STRING );
	} 
  
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
  	String urlString = getNamedStringParam( argStruct, "url",null);
  	if ( urlString == null )
  		throwException(_session, "Missing the 'url' parameter to redirect to");
  	
  	boolean	addToken 	= getNamedBooleanParam(argStruct, "addtoken", true );
  	int statusCode 		= getNamedIntParam(argStruct, "statuscode", 302);
  	
  	if ( addToken )
  		urlString = _session.encodeURL( urlString );
  	
  	// Now do the redirect
  	_session.sendRedirect( urlString, true, statusCode );
    return cfBooleanData.TRUE;
  } 
}