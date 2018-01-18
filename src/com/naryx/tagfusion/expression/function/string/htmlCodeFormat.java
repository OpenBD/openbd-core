/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: htmlCodeFormat.java 1982 2012-03-15 02:03:07Z alan $
 */

package com.naryx.tagfusion.expression.function.string;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class htmlCodeFormat extends functionBase {

	private static final long serialVersionUID = 1L;

	public htmlCodeFormat() {
		min = 1;
		max = 2;
		setNamedParams( new String[]{ "string", "attributes"} );
	}
	
  public String[] getParamInfo(){
		return new String[]{
			"code to be escaped inside of &lt;pre&gt; tags",
			"any attributes to add to the &lt;pre%gt; tag"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"string", 
				"Wraps the given string inside of the HTML <pre> .. </pre> tags, escaping the string accordingly", 
				ReturnType.STRING );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		String preAtt	= getNamedStringParam(argStruct, "attributes", "" );

		preAtt = (preAtt.length() > 0) ? " " + preAtt : "";
		
		String str = getNamedStringParam(argStruct, "string", "");
		str = com.nary.util.string.escapeHtml(str);
		str = com.nary.util.string.replaceString(str, "\r", "");
		
		return new cfStringData("<pre" + preAtt + ">" + str + "</pre>");
	}
}
