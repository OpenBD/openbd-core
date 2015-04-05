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

import com.naryx.tagfusion.cfm.engine.cfmBadFileException;

public class cfINTERFACE extends cfCOMPONENT implements Serializable {
	static final long serialVersionUID = 1;

	public java.util.Map getInfo(){
		return createInfo("control", "Used to define  an interface.  Inside this tag, contains the CFFUNCTION tags. Should only ever be one in a single file.");
	}
  
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
				createAttInfo("DISPLAYNAME", "The name that is used to describe this interface when displayed in meta data", "", false ),
				createAttInfo("HINT", "A small description for this interface", "", false ),
				createAttInfo("EXTENDS", "A list of CFCs that this interface will extends", "", false )
		};

	}

	public String getEndMarker() {
		return "</CFINTERFACE>";
	}
	
	protected void defaultParameters( String tagName ) throws cfmBadFileException {
		super.defaultParameters( tagName, INTERFACE );
	}

}
