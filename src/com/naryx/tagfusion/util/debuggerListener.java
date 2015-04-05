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

package com.naryx.tagfusion.util;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.parser.script.CFParsedStatement;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.cfm.tag.cfFUNCTION;
import com.naryx.tagfusion.cfm.tag.cfTag;

/*
 * This is the interface that will be used to get a handle to the 
 * core rendering of the engine.
 *
 */
 
public interface debuggerListener {
	
	public void registerSession( cfSession thisSession );
	
	public void startScriptStatement( CFParsedStatement statement );
	
	public void startTag( cfTag thisTag );
	public void endTag( cfTag thisTag );

	public void startFile( cfFile thisFile );
	public void endFile( cfFile thisFile );
	
	public void writtenBytes( int total );
	
	public void setHTTPHeader( String name, String value );
	public void setHTTPStatus( int sc, String value );
	
	public void endSession();

	public void runExpression(CFExpression expr);

	public void startFunction(cfFUNCTION cfFUNCTION);
	public void endFunction(cfFUNCTION cfFUNCTION);

	public void startScriptFunction(userDefinedFunction userDefinedFunction);
	public void endScriptFunction(userDefinedFunction userDefinedFunction);
}
