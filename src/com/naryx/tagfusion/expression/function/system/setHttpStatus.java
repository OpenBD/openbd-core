/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  $Id: setHttpStatus.java 2390 2013-06-23 16:34:12Z alan $
 */

package com.naryx.tagfusion.expression.function.system;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class setHttpStatus extends functionBase {
	private static final long serialVersionUID = 1L;

	public setHttpStatus() {
		min = 1;
		max = 2;
		setNamedParams( new String[]{ "code", "message" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"the HTTP status code value",
			"the associated HTTP message"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"Sets the HTTP status code for this request.  It must be called before any content is flushed to the request", 
				ReturnType.BOOLEAN );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		
		int statusCode		= getNamedIntParam(argStruct, "code", -1 );
		if ( statusCode < 0 )
			throwException(_session, "the parameter 'code' cannot be less than 0");
		
		String message		= getNamedStringParam( argStruct, "message" , null );

		if ( message == null ){
			_session.setStatus(statusCode);
		}else{
			_session.setStatus(statusCode,message);
		}
		
		return cfBooleanData.TRUE;
	}
		
}