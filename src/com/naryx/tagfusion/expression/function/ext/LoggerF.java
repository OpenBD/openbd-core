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

package com.naryx.tagfusion.expression.function.ext;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.tag.cfFUNCTION;
import com.naryx.tagfusion.expression.function.functionBase;


public class LoggerF extends functionBase {
	private static final long serialVersionUID = 1L;
	private static Logger	logger	= Logger.getLogger( LoggerF.class.getName() );

	public LoggerF() {
		min = 1; max = 2;
		setNamedParams( new String[]{ "msg", "level" } );
	}

	public String[] getParamInfo(){
		return new String[]{
				"the message string to send to the logging",
				"the level of logging. Values can be: all, info, warning, fine, finer, config, off, servere, off"
		};
	}

	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"Sends data to the underlying Java Logger process.  It will automatically pick up the context of where you are calling this from", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct )throws cfmRunTimeException{ 
		
		Level level = getLevel( getNamedStringParam(argStruct, "level", "info") );
		String msg	= getNamedStringParam(argStruct, "msg", "" );
		
		// Active File
		cfFile file = _session.activeFile();
		String sourceClass = file.getName();
		String sourceMethod = "";

		// Inside a function?
		cfFUNCTION func = _session.getActiveComponentTag();
		if ( func != null ){
			sourceMethod = func.getConstant("NAME", true);
		}
		
		sourceMethod += " @ Line#" + _session.activeTag().posLine;


		// send to the underlying
		logger.logrb( level, sourceClass, sourceMethod, "", msg );
		return cfBooleanData.TRUE;
	}
	
	private Level getLevel( String level ){
		if ( level.equalsIgnoreCase("info") )
			return Level.INFO;
		else if ( level.equalsIgnoreCase("all") )
			return Level.ALL;
		else if ( level.equalsIgnoreCase("config") )
			return Level.CONFIG;
		else if ( level.equalsIgnoreCase("fine") )
			return Level.FINE;
		else if ( level.equalsIgnoreCase("finer") )
			return Level.FINER;
		else if ( level.equalsIgnoreCase("off") )
			return Level.OFF;
		else if ( level.equalsIgnoreCase("severe") )
			return Level.SEVERE;
		else if ( level.equalsIgnoreCase("warning") )
			return Level.WARNING;
		else
			return Level.INFO;
	}
}
