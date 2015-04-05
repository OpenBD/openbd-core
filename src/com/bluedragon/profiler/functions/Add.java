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
 *  http://openbd.org/
 *  $Id: Add.java 2351 2013-03-22 19:44:18Z alan $
 */
package com.bluedragon.profiler.functions;

import com.bluedragon.profiler.ProfileSession;
import com.bluedragon.profiler.ProfilerExtension;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class Add extends functionBase {
	private static final long serialVersionUID = 1L;

	public Add(){
		min = 1; 
		max = 1;
		setNamedParams( new String[]{ "data" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"Any type of data to add"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"cfmlbug", 
				"Ability to log extra data to the main profile data that is paged at the end of each request.  This data will be in the '_extra' key.  If this method is called multiple times, then the data is replaced with the last call", 
				ReturnType.BOOLEAN );
	}

	public cfData execute( cfSession _session, cfArgStructData argStruct )throws cfmRunTimeException{ 
		if ( cfEngine.thisInstance.getRequestListener() != ProfilerExtension.thisInst )
			return cfBooleanData.FALSE;
		
		cfData data	= getNamedParam(argStruct, "data", null );
		if ( data == null )
			throwException(_session, "invalid data parameter");
		
		_session.setDataBin( ProfileSession.SESSION_DATA_BIN_PROFILE, data.duplicate() );
		
		return cfBooleanData.TRUE;
	}
}