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

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class spanIncluding extends functionBase {
  
  private static final long serialVersionUID = 1L;
	
  public spanIncluding(){
     min = max = 2;
     setNamedParams( new String[]{ "string", "substring" } );
  }

	public String[] getParamInfo(){
		return new String[]{
			"string",
			"sub-string"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"string", 
				"Returns back the string up to the point where any of the strings in sub-string is matched, including the substring", 
				ReturnType.STRING );
	}
   
  
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		cfData data = getNamedParam( argStruct, "string");
    String str = data.getString();
    
    data = getNamedParam( argStruct, "substring");
    String set = data.getString();
       
    char[] str_Array = str.toCharArray();
    char[] set_Array = set.toCharArray();
       
    String result = "";   
    //-- span each char in the string and compare to each char in the set
    for ( int i = 0; i < str_Array.length; i++ ){
    	for ( int j = 0; j < set_Array.length; j++ ){
    		if ( str_Array[i] == set_Array[j] ){
    			result += str_Array[i];
    			break;
    		}else if ( j == set_Array.length-1 )
    			return new cfStringData( result ); 
    	}
    }   
		
    return new cfStringData( result );
  } 
}
