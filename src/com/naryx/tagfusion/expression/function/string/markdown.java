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
 *  $Id: markdown.java 2447 2014-07-30 16:03:04Z andy $
 */

package com.naryx.tagfusion.expression.function.string;

import java.io.IOException;

import org.markdown4j.Markdown4jProcessor;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;
import com.naryx.tagfusion.expression.function.xml.xmlFormat;


public class markdown extends functionBase {

	private static final long serialVersionUID = 1L;


	public markdown() {
		min = 1;
		max = 2;
		setNamedParams( new String[] { "markdown", "escapehtml" } );
	}


	public String[] getParamInfo() {
		return new String[] {
				"string representing the MarkDown language to be converted to HTML",
				"flag to determine if the existing HTML should be escaped before encoding; defaults to true"
		};
	}


	public java.util.Map getInfo() {
		return makeInfo(
				"string",
				"Converts the MarkDown string to HTML, optionally preserving the HTML code prior to encoding. http://markdownj.org/ ",
				ReturnType.STRING );
	}


	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		String str = getNamedStringParam( argStruct, "markdown", null );
		if ( str == null )
			throwException( _session, "missing markdown parameter" );

		if ( getNamedBooleanParam( argStruct, "escapehtml", true ) )
			str = xmlFormat.formatXML( str );

		try {
			return new cfStringData( new Markdown4jProcessor().process( str ) );
		} catch ( IOException e ) {
			throwException( _session, "Failed to convert markdown due to error: " + e.getMessage() );
			return null; // keep the compiler happy
		}

	}
}
