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
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class Int extends functionBase {
  private static final long serialVersionUID = 1L;
	
  public Int(){  min = max = 1;  }

	public String[] getParamInfo(){
		return new String[]{
			"number"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"math", 
				"Returns the largest (closest to positive infinity) double value that is less than or equal to the argument and is equal to a mathematical integer", 
				ReturnType.NUMERIC );
	}

  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
    double d = Math.floor( getDouble( _session, parameters.get(0) ) );
    
    if ( d > Integer.MAX_VALUE ){
      return new cfStringData( String.valueOf( Math.round( d ) ) );
    }else{
      // Cast the value to a double to make sure we call the cfNumberData constructor that takes a double.
      // This is done in order to match the behaviour of CFMX 6.1 and 7.0.  If we don't do this then
      // sometimes the constructor that takes a float will be called causing a slightly different value.
      return new cfNumberData( (double)Math.round( d ) ); 
    }
  } 
}
