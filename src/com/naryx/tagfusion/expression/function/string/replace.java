/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class replace extends functionBase {
	
  private static final long serialVersionUID = 1L;
  
  public replace(){
     min = 3; max = 4;
     setNamedParams( new String[]{ "string","substring","new","flag"} );
  }

	public String[] getParamInfo(){
		return new String[]{
			"string",
			"substring to look for",
			"new string",
			"flag to determine if all or just the first is to be replaced - ONE [default] / ALL",
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"string", 
				"Looks for the occurences of a given substring within a string, replacing it with a new one", 
				ReturnType.STRING );
	}
  
  
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		
		String str = getNamedStringParam(argStruct, "string", "");
  	String sub1  = getNamedStringParam(argStruct, "substring", "");
  	String sub2  = getNamedStringParam(argStruct, "new", "");
  	String scope = getNamedStringParam(argStruct, "flag", "ONE");
  	
		if ( sub1.equals( "" ) ){
			throwException( _session, "The second argument must be a non-empty string.");
		}
		
    if ( sub1.equals( sub2 ) )
    	return new cfStringData( str );
           
    String result = "";       
 		//--[ check scope
		if ( scope.equalsIgnoreCase( "ONE" ) )
			result = com.nary.util.string.replaceString( str, sub1, sub2, false );
		else if ( scope.equalsIgnoreCase( "ALL" ) )
			result = com.nary.util.string.replaceString( str, sub1, sub2 );
 		
 		if ( result == null )
 			return new cfStringData( "" );  
 			
    return new cfStringData( result );
  } 	 
}
