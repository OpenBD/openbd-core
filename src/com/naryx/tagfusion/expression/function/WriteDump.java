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

package com.naryx.tagfusion.expression.function;

import java.io.File;
import java.io.IOException;

import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfDUMP;

public class WriteDump extends functionBase {
  
  private static final long serialVersionUID = 1L;
	
  public WriteDump(){ 
  	min = 0; 
  	max = 6;
  	
  	setNamedParams( new String[]{ "var", "output", "abort", "top", "version", "label" } );
  }
  
	public String[] getParamInfo(){
		return new String[]{
				"A variable or text string to be output to the browser at runtime.",
				"Determines where the output of this dump goes. Can be 'browser', or the full path to a file name to use",
				"Abort this request as soon as the tag has finished", 
				"How deep to iterate into the variable, e.g. the number of rows to output.",
				"Determines how verbose query and java objects should outputted",
				"A label or title to be shown for the output of the VAR attribute."
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"Outputs the content of a CFML variable to the browser, often used during debugging or as a quick way to view variables at runtime",
				ReturnType.BOOLEAN );
	}
  
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		String output 		= getNamedStringParam( argStruct, "output", "browser" );
		cfData version 		= getNamedParam( argStruct, "version", null );
		String label 			= getNamedStringParam( argStruct, "label", "" );
		boolean bAbort		= getNamedBooleanParam(argStruct, "abort", false );
		int top						= getNamedIntParam(argStruct, "top", 9999 );	
		cfData _variable	= getNamedParam(argStruct, "var", null );
		

		// query variables and CFCs, SHORT for everything else
		if (version == null) {
			if ((_variable != null) && ((_variable.getDataType() == cfData.CFQUERYRESULTDATA) || (_variable.getDataType() == cfData.CFCOMPONENTOBJECTDATA))) {
				version = new cfStringData("LONG");
			} else {
				version = new cfStringData("SHORT");
			}
		}

		// Get the output
		String outString;
		if ( output.equalsIgnoreCase("browser") ){
			outString = cfDUMP.dumpSession(_session, _variable, version, label, top, false, false );
			if (outString != null) {
				_session.forceWrite(outString);
			}
		}else{
			outString = cfDUMP.dumpSession(_session, _variable, version, label, top, false, true );
			try {
				FileUtils.writeFile( new File(output), outString);
			} catch (IOException e) {
				throwException( _session, "Failed to write file [" + output + "]; " + e.getMessage() );	
			}
		}

		// If its set abort 
		if ( bAbort )
			_session.abortPageProcessing(true);
		
		return cfBooleanData.TRUE;
  } 
}
