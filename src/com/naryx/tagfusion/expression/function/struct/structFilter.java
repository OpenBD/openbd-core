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
 *  $Id: structFilter.java 2229 2012-08-05 20:24:40Z alan $
 */

package com.naryx.tagfusion.expression.function.struct;

import java.util.ArrayList;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.expression.function.functionBase;

public class structFilter extends functionBase {
	private static final long serialVersionUID = 1L;

	public structFilter() {
		min = max = 2;
		setNamedParams( new String[]{ "struct", "function" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"Structure Object",
			"function to loop over the data, passing in the element as the parameter to each. each function call should return true or false; function(key,element){}"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"structure", 
				"Used to loop over the structure to create a new structure based on the inner function applied to each element that will return true/false if it is to be included", 
				ReturnType.STRUCTURE );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		cfData data = getNamedParam(argStruct, "struct");
		cfData	func = getNamedParam(argStruct, "function" );

		if ( func.getDataType() != cfData.CFUDFDATA )
			throwException(_session, "the parameter is not a function");

		if (data.getDataType() == cfData.CFSTRUCTDATA) {
			cfStructData	arrData	= (cfStructData)data;
			cfStructData	newArr	= new cfStructData();
			
			userDefinedFunction	udf	= (userDefinedFunction)func;
			List<cfData>	args	= new ArrayList<cfData>(1);
			cfData result;

			Object[]	keys	= arrData.keys();
			for ( int x = 0; x < keys.length; x++ ){
				args.clear();

				String k	= String.valueOf(keys[x]);
				cfData	d	= arrData.getData( (String)keys[x] );
				args.add( new cfStringData(k) );
				args.add( d );

				result = udf.execute( _session, args );

				if ( result.isBooleanConvertible() && result.getBoolean() )
					newArr.setData( k, d );
			}

			return newArr;
		} else {
			throwException(_session, "the parameter is not an structure");
		}

		return null;
	}
}
