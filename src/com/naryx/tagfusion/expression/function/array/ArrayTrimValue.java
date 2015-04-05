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
 *  $Id: ArrayTrimValue.java 2180 2012-07-13 00:07:30Z alan $
 */

package com.naryx.tagfusion.expression.function.array;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class ArrayTrimValue extends functionBase {
	private static final long serialVersionUID = 1L;

	public ArrayTrimValue() {
		min = max = 1;
		setNamedParams( new String[]{ "array" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"Array Object"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"array", 
				"Runs through the array and trims the whitespace away from each of the elements", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		
		cfData data = getNamedParam(argStruct, "array");
		
		if (data.getDataType() == cfData.CFARRAYDATA) {
			cfArrayData	array	= (cfArrayData)data;
			
			for ( int x=1; x <= array.size(); x++ ){
				cfData	element	= array.getElement( x ); 
				
				if ( element.getDataType() == cfData.CFSTRINGDATA ){
					array.replace( x, new cfStringData(element.getString().trim()) );
				}
			}

		} else
			throwException(_session, "the parameter is not an Array");

		return cfBooleanData.TRUE;
	}

}