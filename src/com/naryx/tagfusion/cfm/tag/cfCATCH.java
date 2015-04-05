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

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfmBadFileException;

public class cfCATCH extends cfTag implements Serializable {

	static final long serialVersionUID = 1;

  public java.util.Map getInfo(){
  	return createInfo("control", "For use within the CFTRY tag, allowing you to catch one of more different exceptions");
  }
  
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
 			createAttInfo("TYPE", 	"The type of exception to catch for", "Any", false ),
  	};
  }
  	
	
	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute("TYPE", "Any");
		parseTagHeader(_tag);
	}

	public String getEndMarker() {
		return "</CFCATCH>";
	}

	public boolean isCatchAny() {
		// Java exception types are case-sensitive, CFML exception types are not
		return getConstant("TYPE").equalsIgnoreCase("any");
	}
}
