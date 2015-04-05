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

/*
 * Author: Alan Williamson
 * Created on 02-Sep-2004
 * 
 * BlueDragon only Expression
 * 
 * Treats the input string as a raw CFML file.  This function will
 * parse the CFML data, render it, and return the string.
 */
package com.naryx.tagfusion.expression.function.ext;

import java.io.StringReader;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.file.cfmlURI;
import com.naryx.tagfusion.expression.function.functionBase;

public class render extends functionBase {
  
  private static final long serialVersionUID = 1L;
	
  public render(){  min = 1; max = 1; }
  
	public String[] getParamInfo(){
		return new String[]{
			"cfml block",
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"engine", 
				"Interprets the string passed in as a CFML block and returns the resulting block.  Similar to what CFINCLUDE would do", 
				ReturnType.STRING );
	}
 
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {

  	//-- Create a StringReader to this string
  	StringReader	readerCfml	= new StringReader( parameters.get(0).getString() );

  	//-- Create our file instance to this string
  	cfFile	stringFile	= new cfFile( new cfmlURI(""), readerCfml, "UTF-8" );

  	//-- Render the string and return
  	return new cfStringData( stringFile.renderToString(_session).getOutput() );
  }
}
