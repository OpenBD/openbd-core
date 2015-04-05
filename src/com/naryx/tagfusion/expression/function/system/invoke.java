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
 *  $Id: invoke.java 2390 2013-06-23 16:34:12Z alan $
 */

package com.naryx.tagfusion.expression.function.system;

import java.util.Iterator;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfcMethodData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class invoke extends functionBase {

	private static final long serialVersionUID = 1L;

	public invoke() {
		min = 2;
		max = 3;
		setNamedParams( new String[]{ "cfcobj", "method", "arguments" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"Instance of the CFC object",
			"the name of the method to call on the CFC",
			"the structure that represents the arguments that will be passed to the method"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"Calls a method on the given object passing in the attributes", 
				ReturnType.OBJECT );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		
		// Get the CFC obj
		cfData	objData	= getNamedParam(argStruct, "cfcobj", null );
		if ( objData == null )
			throwException(_session, "no 'cfcobj' parameter was given");
		
		if ( objData.getDataType() != cfData.CFCOMPONENTOBJECTDATA )
			throwException(_session, "'cfcobj' parameter was not a CFC instance");
		
		
		// Get the method
		String methodName		= getNamedStringParam( argStruct, "method" , null );
		if ( methodName == null )
			throwException(_session, "no 'method' parameter was given");

		
		// Get the arguments
		cfData	args	= getNamedParam(argStruct, "arguments", null );
		if ( args != null && !args.isStruct() )
			throwException(_session, "the 'arguments' was not a structure");
		
		cfArgStructData namedArguments = new cfArgStructData();
		if ( args != null ){
			Iterator<String> it	= ((cfStructData)args).keySet().iterator();
			while ( it.hasNext() ){
				String key	= it.next();
				namedArguments.setData( key, ((cfStructData)args).getData(key) );
			}
		}
		
		
		// Invoke the method
		cfcMethodData invocationData = new cfcMethodData(_session, methodName, namedArguments );
		return ((cfComponentData)objData).invokeComponentFunction(_session, invocationData);
	}
		
}