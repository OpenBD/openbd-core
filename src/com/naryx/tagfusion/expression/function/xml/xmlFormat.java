/* 
 *  Copyright (C) 2000 - 2014 TagServlet Ltd
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
 *  $Id: xmlFormat.java 2443 2014-05-20 10:35:56Z alan $
 */

package com.naryx.tagfusion.expression.function.xml;

import java.util.List;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class xmlFormat extends functionBase {
	private static final long serialVersionUID = 1L;

	public xmlFormat() {
		min = max = 1;
	}

  public String[] getParamInfo(){
		return new String[]{
			"string"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"xml", 
				"Encodes the given string for safe usage within XML nodes; escapes >, <, ', \", & and also any high ASCII characters", 
				ReturnType.STRING );
	}
	
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		String str = ((cfData) parameters.get(0)).getString();
		return new cfStringData( formatXML(str) );
	}

	public static String formatXML(String str){
		// Remove carriage returns from the string
		str = string.replaceString(str, "\r", "");

		// Escape the following characters: >, <, ', ", &
		str = replaceChars(str);

		// Escape High ASCII characters
		str = replaceHighASCII(str);

		return str;
	}
	
	/*
	 * replaceHighASCII
	 * 
	 * Escape the High ASCII characters for use as text in XML.
	 */
	public static String replaceHighASCII(String str) {
		StringBuilder sb = new StringBuilder(str.length());

		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if ((c > 127) && (c < 256))
				sb.append("&#x" + Integer.toHexString((int) c) + ";");
			else
				sb.append(c);
		}

		return sb.toString();
	}

	/*
	 * replaceChars
	 * 
	 * Escape the following characters for use as text in XML: >, <, ', ", &.
	 * 
	 * NOTE: this method is also used by cfJavaObjectData.dumpWDDX() so be careful
	 * if you modify it.
	 */
	public static String replaceChars(String str) {
		str = string.replaceString(str, "&", "&amp;");
		str = string.replaceString(str, "\"", "&quot;");
		str = string.replaceString(str, ">", "&gt;");
		str = string.replaceString(str, "<", "&lt;");
		str = string.replaceString(str, "\'", "&apos;");
		return str;
	}
}
