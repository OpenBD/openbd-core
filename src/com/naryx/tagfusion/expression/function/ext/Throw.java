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
 *  $Id: Throw.java 1612 2011-06-30 18:51:39Z alan $
 */

package com.naryx.tagfusion.expression.function.ext;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class Throw extends functionBase {
	private static final long serialVersionUID = 1L;

	public Throw() {
		min = 0; max = 5;
		setNamedParams( new String[]{ "type", "message", "detail", "errorcode", "extendedinfo" } );
	}

	public String[] getParamInfo(){
		return new String[]{
				"Defaults to Application",
				"The message of the exception",
				"The full detail of the exception",
				"Any code associated with this exception",
				"Any additional information"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"Throws an exception based on a given criteria", 
				ReturnType.UNKNOWN );
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String type					= getNamedStringParam(argStruct, "type", "Application"  );  
		String message			= getNamedStringParam(argStruct, "message", null  );  
		String detail				= getNamedStringParam(argStruct, "detail", null );  
		String errorcode		= getNamedStringParam(argStruct, "errorcode", null );  
		String extendedinfo	= getNamedStringParam(argStruct, "extendedinfo", null );  
		
		// Construct the exception
		cfCatchData catchData = new cfCatchData(_session);
		catchData.setType(type);

		if ( message != null )
			catchData.setMessage(message);

		if ( detail != null )
			catchData.setDetail( detail );

		if ( errorcode != null )
			catchData.setErrorCode( errorcode );

		if ( extendedinfo != null )
			catchData.setExtendedInfo( extendedinfo );
		
		throw new cfmRunTimeException( catchData, false );
	}
}
