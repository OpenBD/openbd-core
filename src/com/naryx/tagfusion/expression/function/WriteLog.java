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

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfLOG;

public class WriteLog extends functionBase {
  
  private static final long serialVersionUID = 1L;
	
  public WriteLog(){ 
  	min = 1; 
  	max = 6;
  	
  	setNamedParams( new String[]{ "text", "type", "application", "file", "log", "thread" } );
  }
  
	public String[] getParamInfo(){
		return new String[]{
				"Specifies the text that should be logged.",
				"Specifies a category for your log (e.g. Information, Warning, Error etc).",
				"If set to YES, the web application name will be stored with the output of this log entry.",
				"Specifies a file name that this log will be written to. This attribute cannot be applied with the \"LOG\" attribute.",
				"Specifies a standard openbd log file to write this log to. Possible options include \"scheduler\", \"trace\" or \"application\". This attribute cannot be applied with the \"FILE\" attribute.",
				"Setting this attribute to YES will gather the thread reference this log was called within and output it with the log. This is useful to find out which thread has generated a log if being used in a threaded environment."
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"This function is used to write to the logging system of OpenBD. If no \"LOG\" or \"FILE\" attribute is specified, then the log is written to the standard OpenBD application log file",
				ReturnType.BOOLEAN );
	}
  
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		String text = getNamedStringParam( argStruct, "text", null );
		if ( text == null )
			throwException(_session, "You must specify the 'text' attribute" );
		
		String file 					= getNamedStringParam( argStruct, "file", "" );
		String log 						= getNamedStringParam( argStruct, "log", "" );		
		if ( file.length() != 0 && log.length() != 0 )
			throwException(_session, "You cannot specify values for both FILE and LOG parameters" );
		
		boolean bThread				= getNamedBooleanParam(argStruct, "thread", true );
		boolean bApplication	= getNamedBooleanParam(argStruct, "application", true );
		String type						= getNamedStringParam(argStruct, "type", "Information" );	
		
		cfLOG.toLog( _session, text, type, file, log, bApplication, bThread );
		
		return cfBooleanData.TRUE;
  } 
}
