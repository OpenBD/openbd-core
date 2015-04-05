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
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.runTime;

public class iif extends evaluate {
  
  private static final long serialVersionUID = 1L;
	
  public iif(){
     min = max = 3;
  }
  
  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
  
		/*
	 	 * NOTE: Although designed to evaluate a list of string expressions, CF does support non-string
		 *  expressions hence the check to see if the expression is a String expression. 
		 */
	
  		// the if condition will be evaluated prior to this
		cfData ifCond = parameters.get(2);
		cfData expOne = parameters.get(1);
		cfData expTwo = parameters.get(0);
		
		boolean conditionVal = false;
		
		try{
			conditionVal = ifCond.getBoolean();
		}catch(Exception E){
			throwException( _session, "Invalid condition. Condition must evaluate to a boolean value." );
		}
		
		if ( conditionVal ){
			try{
				if ( expOne.getDataType() == cfData.CFSTRINGDATA ){
					return runTime.runExpression( _session, expOne.getString() );
				}else{
					return expOne;
				}
			}catch(Exception E){
				throwException( _session, "Invalid expression. " + E.getMessage() );
			}
		}else{
			try{
				if ( expOne.getDataType() == cfData.CFSTRINGDATA ){
					return runTime.runExpression( _session, expTwo.getString() );
				}else{
					return expTwo;
				}
			}catch(Exception E){
				throwException( _session, "Invalid expression. " + E.getMessage() );
			}
		}
		
		return null;
  } 

}
