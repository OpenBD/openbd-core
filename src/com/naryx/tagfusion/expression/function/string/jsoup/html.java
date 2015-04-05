/* 
 *  Copyright (C) 2011 TagServlet Ltd
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
 *  
 *  $Id: html.java 1605 2011-06-25 14:25:08Z alan $
 */

package com.naryx.tagfusion.expression.function.string.jsoup;

import org.jsoup.Jsoup;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class html extends functionBase {

	private static final long serialVersionUID = 1L;

	public html() {
		min = 1;
		max = 1;
		setNamedParams( new String[]{ "htmlbody" } );
	}
	
  public String[] getParamInfo(){
		return new String[]{
			"the HTML body or snippet"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"string", 
				"An advanced parsing and selection model to work with HTML blocks in a very JQuery like selector manner", 
				ReturnType.OBJECT );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		String htmlBody	= getNamedStringParam(argStruct, "htmlbody", null );
		return new cfJavaObjectData( Jsoup.parse(htmlBody) );
	}
}
