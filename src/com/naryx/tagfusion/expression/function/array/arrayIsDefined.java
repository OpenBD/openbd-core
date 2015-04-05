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
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class arrayIsDefined extends functionBase {
	private static final long serialVersionUID = 1L;

	public arrayIsDefined() {
		min = max = 2;
		setNamedParams( new String[]{ "array", "index" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"Array Object",
			"Position index to check"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"array", 
				"Determines if the value at the given index has been assigned", 
				ReturnType.BOOLEAN );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		cfData array = getNamedParam(argStruct, "array");
		
		if (array.getDataType() == cfData.CFARRAYDATA) {
			cfArrayData cfArray	= (cfArrayData)array;
		
			int indx = getNamedIntParam(argStruct, "index", 0);
			if ( indx > cfArray.size() || indx <= 0 )
				return cfBooleanData.FALSE;
				
			cfData element	= cfArray.getElement( indx );
			if ( ( element == null ) || ( element.getDataType() == cfData.CFNULLDATA ) )
				return cfBooleanData.FALSE;
			else
				return cfBooleanData.TRUE;

		} else
			throwException(_session, "the parameter is not an Array");

		return null;
	}
}


/*

	<h3>ArrayIsDefined Example</h3>
	<!--- Create a sparse new array. --->
	<cfset MyArray = ArrayNew(1)>
	
	<!--- Populate an element or two. --->
	<cfset MyArray[1] = "Test">
	<cfset MyArray[3] = "Other Test">

	<cfoutput>
  <!--- Display the contents of the array. --->
  <p>Your array contents are:
  <cfdump var="#MyArray#"></p>
  
  <!--- Check if an existing element is defined. --->
  <p>Does element 3 exist?:&nbsp;
  #ArrayIsDefined(MyArray, 3)#</p>
  
  <!--- Check if a non-existent element is defined. --->
  <p>Does element 2 exist?&nbsp;
  #ArrayIsDefined(MyArray, 2)#
	
  <!--- Check if a non-existent element is defined. --->
  <p>Does element 6 exist?&nbsp;
  #ArrayIsDefined(MyArray, 6)#

  <!--- Check if a non-existent element is defined. --->
  <p>Does element 0 exist?&nbsp;
  #ArrayIsDefined(MyArray, 0)#
	</cfoutput>

*/