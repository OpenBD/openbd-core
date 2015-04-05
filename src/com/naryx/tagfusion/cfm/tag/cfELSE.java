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


package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;

public class cfELSE extends cfIF implements Serializable
{

	static final long serialVersionUID = 1;

  protected void parseExpression( String _tagName, String _tag ){}


 	/** 
   * Overrides the end marker of CFIF tag. These is no end tag
   * marker for an ELSE statement, so always returns a null
   *
   * @returns 		null
   */ 
  public String getEndMarker(){
  	return null;
  }
  
	/** Overrides the method in cfIF. Always returns true.
	 *
   * @param     _Session The cfSession which holds the value of required variables.
   *
   * @returns   boolean to indicate true.
   */
 	protected boolean evaluateExpression( cfSession _Session ){
 		return true;
 	}

}
