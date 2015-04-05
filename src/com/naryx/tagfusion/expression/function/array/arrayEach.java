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
 *  $Id: arrayEach.java 2240 2012-08-12 12:11:59Z alan $
 */

package com.naryx.tagfusion.expression.function.array;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDataSession;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class arrayEach extends functionBase {
	private static final long serialVersionUID = 1L;

	public arrayEach() {
		min = max = 2;
		setNamedParams( new String[]{ "array", "function" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"Array Object",
			"function to loop over the data, passing in the element as the parameter to each. function(el){}"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"array", 
				"Applies the function to each of the elements in the array", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		cfData array = getNamedParam(argStruct, "array");
		cfData	func = getNamedParam(argStruct, "function" );

		if ( func.getDataType() != cfData.CFUDFDATA )
			throwException(_session, "the parameter is not a function");

		if (array.getDataType() == cfData.CFARRAYDATA) {
			cfArrayData	arrData	= (cfArrayData)array;
			arrData.each( new cfDataSession(_session), func );
			return cfBooleanData.TRUE;
		} else {
			throwException(_session, "the parameter is not an Array");
		}

		return null;
	}
}
