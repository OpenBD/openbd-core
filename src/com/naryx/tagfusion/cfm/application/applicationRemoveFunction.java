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
 *  $Id: applicationRemoveFunction.java 2131 2012-06-27 19:02:26Z alan $
 */

package com.naryx.tagfusion.cfm.application;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class applicationRemoveFunction extends functionBase {
	private static final long serialVersionUID = 1L;

	public applicationRemoveFunction() {
		min = max = 1;
		setNamedParams( new String[]{ "appname" } );
	}
		
	public String[] getParamInfo(){
		return new String[]{
			"appname"
		};
	}

	
	public java.util.Map getInfo(){
		return makeInfo(
				"System", 
				"Removes the given CF application from memory, running onApplicationEnd() if loaded via Application.cfc", 
				ReturnType.BOOLEAN );
	}

	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		String appName	= getNamedStringParam(argStruct, "appname",null);
		if ( appName == null )
			return cfBooleanData.FALSE;

		//Attempt to retrieve application
		Object attr = cfEngine.thisServletContext.getAttribute(appName);
		if (attr != null && (attr instanceof cfApplicationData) ) {
			cfEngine.thisServletContext.removeAttribute(appName);

			((cfApplicationData)attr).onApplicationEnd();
			cfApplicationManager.onApplicationEnd( ((cfApplicationData)attr) );
			
			cfEngine.log("ApplicationRemove(): " + appName );
			
			return cfBooleanData.TRUE;
		}else{
			return cfBooleanData.FALSE;	
		}
	}
}