/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: htmlHead.java 2383 2013-06-16 23:04:20Z alan $
 */

package com.naryx.tagfusion.expression.function.string;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class htmlHead extends functionBase {

	private static final long serialVersionUID = 1L;

	public htmlHead() {
		min = 1;
		max = 2;
	}
	
  public String[] getParamInfo(){
		return new String[]{
			"string",
			"append flag - default true; false if you wish to prepend"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"engine", 
				"Add content to the HEAD part of the HTML page", 
				ReturnType.BOOLEAN );
	}

	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		String htmlHeader;
		boolean append = true;

		if (parameters.size() == 2) {
			htmlHeader 	= parameters.get(1).getString();
			append 			= parameters.get(0).getBoolean();
		} else {
			htmlHeader = parameters.get(0).getString();
		}

		_session.setHeadElement( htmlHeader, append );
		
		return cfBooleanData.TRUE;
	}
}
