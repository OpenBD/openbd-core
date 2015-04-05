/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

package com.naryx.tagfusion.expression.function;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class IsInstanceOf extends functionBase {
	private static final long serialVersionUID = 1L;

	public IsInstanceOf() {
		min = max = 2;
	}

	public String[] getParamInfo(){
		return new String[]{
			"cfc or java object",
			"object type to check for",
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"decision", 
				"Determines if the object passed in is an instance of the type passed in", 
				ReturnType.BOOLEAN );
	}

	
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		cfData obj	= parameters.get(1);
		String type = parameters.get(0).getString();;

		if ( obj.getDataType() == cfData.CFJAVAOBJECTDATA ){
			cfJavaObjectData jDataObject = (cfJavaObjectData)obj;
			
			Class<?> C1, C2;
			try {
				C1 = Class.forName( type );
				C2 = jDataObject.getInstanceClass();  
				return cfBooleanData.getcfBooleanData( C1.isAssignableFrom(C2) );
			} catch (ClassNotFoundException e) {
				throwException(_session, "Could not find the class type: " + type );
			}

		} else if ( obj.getDataType() == cfData.CFCOMPONENTOBJECTDATA ){
			
			cfComponentData	cfcComp	= (cfComponentData)obj;
			
			while ( cfcComp != null ){
				String componentName	= cfcComp.getComponentName();	
				if ( isCFCTypeOf( componentName, type ) )
					return cfBooleanData.TRUE;
				
				cfcComp	= cfcComp.getSuperComponent();
			}

		}else
			throwException(_session, "The first parameter was not of a Java Object or  CFC" );

		return cfBooleanData.FALSE;
	}
	
	
	private boolean isCFCTypeOf( String cfcName, String type ){
		return cfcName.endsWith( type );
	}
}
