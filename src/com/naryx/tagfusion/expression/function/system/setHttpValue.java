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
 *  $Id: setHttpValue.java 2390 2013-06-23 16:34:12Z alan $
 */

package com.naryx.tagfusion.expression.function.system;

import java.io.UnsupportedEncodingException;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class setHttpValue extends functionBase {
	private static final long serialVersionUID = 1L;

	public setHttpValue() {
		min = 2;
		max = 3;
		setNamedParams( new String[]{ "name", "value", "charset" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"the name of the HTTP parameter to set",
			"the value of the HTTP parameter",
			"the charset of the value; (optional)"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"Sets the HTTP header name/value for this request.  It must be called before any content is flushed to the request", 
				ReturnType.BOOLEAN );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		
		String name			= getNamedStringParam( argStruct, "name" , null );
		if ( name == null )
			throwException(_session, "parameter 'name' was not given");
		
		String value		= getNamedStringParam( argStruct, "value" , null );
		if ( value == null )
			throwException(_session, "parameter 'value' was not given");

		String charset	= getNamedStringParam( argStruct, "charset" , null );
		if ( charset != null ){
			try {
				value = new String( value.getBytes(), charset);
			} catch (UnsupportedEncodingException u) {
				throwException(_session, "charset: "+ charset + " not supported");
			}
		}

		
		if ("Content-Type".equalsIgnoreCase(name)) {
			_session.setContentType(value);
		} else {
			_session.setHeader(name, value);
		}
		
		return cfBooleanData.TRUE;
	}

}