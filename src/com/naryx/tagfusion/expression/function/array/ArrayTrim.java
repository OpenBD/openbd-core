/* 
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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

package com.naryx.tagfusion.expression.function.array;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfFixedArrayData;
import com.naryx.tagfusion.cfm.engine.cfQueryColumnData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


/*
 * Resizes the array down to the size passed in
 * If the trimSize is greater than the size of the array, nothing happens
 */

public class ArrayTrim extends functionBase {
	private static final long serialVersionUID = 1L;

	public ArrayTrim() {
		min = max = 2;
		setNamedParams( new String[]{ "array", "size" } );
	}

	public String[] getParamInfo(){
		return new String[]{
				"array",	
				"new array size"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"array", 
				"Resizes the array down to the size given, removing the extra elements from the end", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		cfData array = getNamedParam(argStruct, "array");
		
		if (array.getDataType() == cfData.CFARRAYDATA) {

			if (array instanceof cfFixedArrayData) {
				throwException(_session, "Cannot perform this function on an unmodifiable array.");
			} else if (array instanceof cfQueryColumnData){
				throwException(_session, "Cannot perform this function on an query column.");
			}

			cfArrayData cfarray = (cfArrayData)array;
			
			int trimSize = getNamedIntParam(argStruct, "size", cfarray.size());
			if ( trimSize <= 0 ){
				cfarray.removeAllElements();
			}else{
				while ( trimSize < cfarray.size() ){
					cfarray.removeElementAt( cfarray.size() );
				}
			}

			return cfBooleanData.TRUE;

		} else
			throwException(_session, "the parameter is not an Array");

		return cfBooleanData.TRUE;
	}
}
