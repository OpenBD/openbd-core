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

package com.naryx.tagfusion.expression.function;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class inputBaseN extends functionBase {
  
  private static final long serialVersionUID = 1L;
	
  public inputBaseN(){
     min = max = 2;
  }
  
	public String[] getParamInfo(){
		return new String[]{
			"string",
			"radix"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"format", 
				"Attempts to convert the string specified, using the radix, to a number", 
				ReturnType.NUMERIC );
	}
    
  
  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{

    String 	a = parameters.get(1).getString();
    int 	b	= (int)getDouble( _session, parameters.get(0) );
    
	// This check fixes bug #780 - inputbasen function produces wrong result with certain input
    if ( b == 16 )
    	a = removeHexPrefix( a );
    
    int result = 0;
    try{
	    result = (int)Long.valueOf( removeDot(a), b ).longValue();
	  }catch(Exception E){
	  	throwException( _session, E.toString() );
	  }
    
    return new cfNumberData( result );
  }
  
  private static String removeDot(String a){
  	int c1 = a.indexOf(".");
  	if ( c1 != -1 )
  		return a.substring( 0, c1 );
  	else	
  		return a;
  }
  
  /*
   * removeHexPrefix
   * 
   * This method removes the hex prefix (if present) from a hex string.  This method fixes
   * bug #780 - inputbasen function produces wrong result with certain input.
   */
  private static String removeHexPrefix( String a )
  {
  	if ( a.startsWith( "0x" ) )
  		return a.substring( 2 );
  		
  	return a;
  }
}
