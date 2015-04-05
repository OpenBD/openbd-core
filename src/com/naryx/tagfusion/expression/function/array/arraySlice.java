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
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class arraySlice extends functionBase {
	private static final long serialVersionUID = 1L;

	public arraySlice() {
		min = 2; max = 3;
		setNamedParams( new String[]{ "array", "index", "count" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"Array Object",
			"start position",
			"count - default is to go to the end of the array"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"array", 
				"Returns a new array, from the start position up to the count of elements", 
				ReturnType.ARRAY );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		cfData data = getNamedParam(argStruct, "array");
		int startPosition = getNamedIntParam(argStruct, "index", -1);
		if ( startPosition < 1 )
			throwException(_session, "the parameter for startPosition not given or less than 0");
		
		int count = getNamedIntParam(argStruct, "count", -1);
		
		if ( data.getDataType() == cfData.CFARRAYDATA ){
			cfArrayData	array		= (cfArrayData)data;
			cfArrayData	newArr	= cfArrayData.createArray( 1 );
			
			for ( int x=startPosition; x <= array.size(); x++ ){
				newArr.addElement( array.getElement(x) );
				
				if ( count != -1 && newArr.size() == count )
					break;
			}
			
			return newArr;
		} else
			throwException(_session, "the parameter is not an Array");

		return null;
	}

}
