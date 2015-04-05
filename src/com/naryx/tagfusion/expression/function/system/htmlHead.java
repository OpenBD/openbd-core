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
 *  http://www.openbluedragon.org/
 *  
 *  $Id: htmlHead.java 2390 2013-06-23 16:34:12Z alan $
 */

package com.naryx.tagfusion.expression.function.system;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
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
		setNamedParams( new String[]{ "text", "position" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"the text to add to the HTML BODY",
			"indicator to where it should be added; APPEND|PREPEND controls where you wish the body text to appear within the head area"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"Allows you to insert content into the HTML <head></head> area of the page.  It must be called before any content is flushed to the request", 
				ReturnType.BOOLEAN );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String text			= getNamedStringParam( argStruct, "text" , null );
		if ( text == null )
			throwException(_session, "parameter 'text' was not given");
		
		String position		= getNamedStringParam( argStruct, "position" , "APPEND" );
		if ( position == null )
			throwException(_session, "parameter 'position' was not given");

		_session.setHeadElement( text, position.equalsIgnoreCase("APPEND") );
		return cfBooleanData.TRUE;
	}

}