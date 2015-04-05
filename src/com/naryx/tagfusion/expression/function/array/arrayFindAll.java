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
 *  $Id: arrayFindAll.java 2235 2012-08-11 12:35:43Z alan $
 */

package com.naryx.tagfusion.expression.function.array;

import java.util.ArrayList;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.expression.function.functionBase;

public class arrayFindAll extends functionBase {
	private static final long serialVersionUID = 1L;

	public arrayFindAll() {
		min = max = 2;
		setNamedParams( new String[]{ "array", "function" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"Array Object",
			"function to loop over the data, passing in the element as the parameter to each. each function call should return true or false; function(element){}"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"array", 
				"Used to loop over the array, returning an array of indices of the elements where the function that was called returned true", 
				ReturnType.ARRAY );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		cfData array = getNamedParam(argStruct, "array");
		cfData	func = getNamedParam(argStruct, "function" );

		if ( func.getDataType() != cfData.CFUDFDATA )
			throwException(_session, "the parameter is not a function");

		if (array.getDataType() == cfData.CFARRAYDATA) {
			cfArrayData	arrData	= (cfArrayData)array;
			cfArrayData	arrIndices	= cfArrayData.createArray(1);
			
			
			userDefinedFunction	udf	= (userDefinedFunction)func;
			List<cfData>	args	= new ArrayList<cfData>(1);
			cfData result;

			for ( int x = 0; x < arrData.size(); x++ ){
				args.clear();
				args.add( arrData.getData(x+1) );
				result = udf.execute( _session, args );
				
				if ( result.isBooleanConvertible() && result.getBoolean() )
					arrIndices.addElement( new cfNumberData(x+1) );
			}

			return arrIndices;
		} else {
			throwException(_session, "the parameter is not an Array");
		}

		return null;
	}
}
