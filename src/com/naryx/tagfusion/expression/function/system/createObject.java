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
 *  $Id: createObject.java 2390 2013-06-23 16:34:12Z alan $
 */

package com.naryx.tagfusion.expression.function.system;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfWSObjectData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class createObject extends functionBase {

	private static final long serialVersionUID = 1L;

	public createObject() {
		min = 1;
		max = 3;
		setNamedParams( new String[]{ "type", "object", "argument" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"type of object ('component', 'java' or 'webservice')",
			"object to create; if 'component' the location of the CFC; if 'java' the full class name; if 'webservice' the WSDL",
			"if type='webservice' then this is the arguments; if type='java' then this is the JAR to load from"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"Creates an object and makes it available in the CFML space. Defaults type to a 'component'", 
				ReturnType.OBJECT );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String type			= getNamedStringParam( argStruct, "type" , "component" );
		
		if ( type.equalsIgnoreCase("component") ){

			List<String> mappings = _session.activeFile().getImportedPaths();
			String object		= getNamedStringParam( argStruct, "object" , null );
			return new cfComponentData(_session, object, mappings );

		}else if ( type.equalsIgnoreCase("java") ){
			
			String argument	= getNamedStringParam( argStruct, "argument" , null );
			String object		= getNamedStringParam( argStruct, "object" , null );
			return loadJava( _session, object, argument );

		}else if ( type.equalsIgnoreCase("webservice") ){
			
			String argument	= getNamedStringParam( argStruct, "argument" , null );
			String object		= getNamedStringParam( argStruct, "object" , null );

			if (argument != null)
				return new cfWSObjectData(_session, object, argument );
			else
				return new cfWSObjectData(_session, object);

		}else{
			return new cfComponentData(_session, getNamedStringParam( argStruct, "type" , null ), _session.activeFile().getImportedPaths() );
		}
	}

	
	
	private cfData loadJava(cfSession _session, String className, String jarfile ) throws cfmRunTimeException {
		
		try {
			
			if ( jarfile == null ){
				Class<?> c = Class.forName(className);
				return new cfJavaObjectData(_session, c);
			}else{
				return null;
			}

		} catch (ClassNotFoundException e) {
			StringBuilder buf = new StringBuilder().append("Failed to load class, ").append(className);
			cfCatchData catchData = new cfCatchData(_session);
			catchData.setType("Invalid Class");
			catchData.setDetail("createObject()");
			catchData.setMessage(buf.toString());
			throw new cfmRunTimeException(catchData);
		} catch (NoClassDefFoundError e) {
			StringBuilder buf = new StringBuilder().append("Failed to load class, ").append(e.getMessage());
			cfCatchData catchData = new cfCatchData(_session);
			catchData.setType("Invalid Class");
			catchData.setDetail("createObject()");
			catchData.setMessage(buf.toString());
			throw new cfmRunTimeException(catchData);
		}
	}
	
	
}
