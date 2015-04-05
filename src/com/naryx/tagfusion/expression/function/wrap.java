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

/*
 * This function implements the WRAP function which wraps text.
 * 
 * It is a wrapper to the underlying routine in the nary package
 */
package com.naryx.tagfusion.expression.function;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class wrap extends functionBase {

	private static final long serialVersionUID = 1L;
	
	public wrap(){
	   min = 2; max = 3;
	   setNamedParams( new String[]{ "string","width", "carriage"} );
	}

  public String[] getParamInfo(){
		return new String[]{
			"string",
			"column width",
			"carriage returns - false"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"string", 
				"Wraps the given string at the column width, optionally ignoring the any carriage returns from the original", 
				ReturnType.STRING );
	}
  
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String intText = getNamedStringParam( argStruct, "string","");
		int colWidth = getNamedIntParam( argStruct, "width",0);
		boolean ignorecr = getNamedBooleanParam ( argStruct, "carriage",false);

		return new cfStringData( string.wrap( intText,colWidth,ignorecr) );

	}

}
