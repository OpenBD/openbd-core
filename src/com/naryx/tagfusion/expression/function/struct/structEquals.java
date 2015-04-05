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

package com.naryx.tagfusion.expression.function.struct;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class structEquals extends functionBase {
	private static final long serialVersionUID = 1L;

	public structEquals() {
		min = max = 2;
		setNamedParams( new String[]{ "struct1", "struct2" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"struct1",
			"struct2",
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"structure", 
				"Performs a deep comparison of two structures to see if they represent the same values", 
				ReturnType.BOOLEAN  );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		cfData struct1 = getNamedParam(argStruct, "struct1");
		cfData struct2 = getNamedParam(argStruct, "struct2");
		
		if ( !struct1.isStruct() )
			throwException(_session, "First Parameter isn't of type STRUCTURE");
		if ( !struct2.isStruct() )
			throwException(_session, "Second Parameter isn't of type STRUCTURE");
		
		cfStructData	lhs	= (cfStructData)struct1; 
		cfStructData	rhs	= (cfStructData)struct2; 
		
		if ( lhs.size() != rhs.size() )
			return cfBooleanData.FALSE;
		
		return cfBooleanData.getcfBooleanData( lhs.hashCode() == rhs.hashCode() );
	}
}
