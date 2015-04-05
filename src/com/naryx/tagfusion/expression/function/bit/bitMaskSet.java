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

package com.naryx.tagfusion.expression.function.bit;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;
 
public class bitMaskSet extends functionBase {
 
  private static final long serialVersionUID = 1L;
	
  public bitMaskSet(){ min = max = 4; }
  
	public String[] getParamInfo(){
		return new String[]{
			"number",
			"mask",
			"bit number to start",
			"length of bits"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"math", 
				"Set a mask on the given number covering the range given", 
				ReturnType.NUMERIC );
	}
  
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException{
		int number 	= getInt( _session, parameters.get(3) );
		int mask 		= getInt( _session, parameters.get(2) );
		int start 	= getInt( _session, parameters.get(1) );
		int length 	= getInt( _session, parameters.get(0) );
		
		//--[ Check some bounds
		if ( start < 0 || start > 31 || length < 0 || length > 31 )
			throwException( _session, "Parameters start and length must be in the range 0 - 31" );
		
		//--[ Create the mask
		int maskMask = 0;
		for (int x=0; x < length; x++ )
			maskMask = maskMask | ( 1 << x );
		
		maskMask = mask & maskMask;
    return new cfNumberData( number | (maskMask << start) );
  } 
}
