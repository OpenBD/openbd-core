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

package com.naryx.tagfusion.expression.function.string;

/**
 * Implements the reFindNoCase function. Note that it 
 * just executes an instance of reFind but sets caseSensitive to false first
 */

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class reFindNoCase extends reFind {

	private static final long serialVersionUID = 1L;

	public reFindNoCase() {
		min = 2;
		max = 4;
		setNamedParams( new String[]{ "regular", "string","start","subexpression"} );
	}


	public String[] getParamInfo(){
		return new String[]{
			"regular expression",
			"string to search",
			"start position - default 1",
			"subexpression flag - default false; determines if a structure of the position is returned, or just the position"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"string", 
				"Performs a case-insensitive regular expression match to the given string.  If subexpression=true then it returns a structure (pos,len)", 
				ReturnType.STRUCTURE );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		setCaseSensitive(false);
		return super.execute(_session, argStruct);
	}
}