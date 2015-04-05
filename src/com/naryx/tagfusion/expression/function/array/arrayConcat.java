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
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfFixedArrayData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class arrayConcat extends functionBase  {
	private static final long serialVersionUID = 1L;

	public arrayConcat() {
		min = max = 2;
		setNamedParams( new String[]{ "array1", "array2" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"array1/binary1",
			"array2/binary2"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"array", 
				"Appends all the elements from array2 into array1; supports binary objects", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		cfData data1 = getNamedParam(argStruct, "array1");
		cfData data2 = getNamedParam(argStruct, "array2");
		
		if ( data1 instanceof cfFixedArrayData ) {
			throwException(_session, "Cannot perform this function on an unmodifiable array");
		}
		
		if ( data1.getDataType() == cfData.CFARRAYDATA && data2.getDataType() == cfData.CFARRAYDATA ){
			cfArrayData	array1	= (cfArrayData)data1; 
			cfArrayData	array2	= (cfArrayData)data2; 
		
			
			int size = array2.size();
			for ( int x=0; x < size; x++ )
				array1.addElement( array2.getElement(x+1) );
		
		} else if ( data1.getDataType() == cfData.CFBINARYDATA && data2.getDataType() == cfData.CFBINARYDATA ) {
			
			cfBinaryData	bin1	= (cfBinaryData)data1;
			cfBinaryData	bin2	= (cfBinaryData)data2;
			
			bin1.appendByteArray( bin2.getByteArray() );
			
		}else
			throwException(_session, "the parameters are not an array");

		return cfBooleanData.TRUE;
	}
}
