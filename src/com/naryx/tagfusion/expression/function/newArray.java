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

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class newArray extends functionBase {
  private static final long serialVersionUID = 1L;
	
  public newArray(){ min = 0; max = 1; }
  
	public String[] getParamInfo(){
		return new String[]{
			"dimension - default 1"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"array", 
				"Creates a new array of specific dimension", 
				ReturnType.ARRAY );
	} 

  public cfData execute( cfSession _Session, List<cfData> parameters )throws cfmRunTimeException{
  	int dim = 1;
    if ( parameters.size() == 1 )
    	dim = ((cfNumberData)parameters.get(0)).getInt();
    
    if (dim < 1){
      cfCatchData catchData = new cfCatchData( _Session );
      catchData.setType( "Array" );
      catchData.setDetail( "ArrayNew() error" );
      catchData.setMessage( "Invalid array dimensions: " + dim + ". Dimension must be 1 or greater");
      throw new cfmRunTimeException(catchData);
    }
    return cfArrayData.createArray( dim );
  }
}