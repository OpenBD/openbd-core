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
 * This is a convienent method that implements a NULL debugger session.  This allows
 * us to keep the internal code clean and stops us continually checking against a 
 * null value.  All these methods are empty.
 *
 * This class is a SINGLETON. 
 *
 */
 
public class nullDebugger extends Object implements debuggerListener {

	public static nullDebugger staticInstance	= new nullDebugger(); 
	
	private nullDebugger(){}
	public final void registerSession( cfSession thisSession ){}
		
	public final void startTag( cfTag thisTag ){}
	public final void endTag( cfTag thisTag ){}

	public final void startFile( cfFile thisFile ){}
	public final void endFile( cfFile thisFile ){}
	
	public final void write( int ch ){}
	public final void write( byte[] arrayCh ){}
	public final void writtenBytes(int total) {}
	public final void runExpression(CFExpression expr){}

	
	public final void setHTTPHeader( String name, String value ){}
	public final void setHTTPStatus( int sc, String value ){}
	
	public final void endSession(){}
	
  public final void startScriptStatement( CFParsedStatement statement ) {}
	public final void startFunction(cfFUNCTION cfFUNCTION) {}
	public final void endFunction(cfFUNCTION cfFUNCTION) {}
	
	public final void startScriptFunction(userDefinedFunction userDefinedFunction) {}
	public final void endScriptFunction(userDefinedFunction userDefinedFunction) {}
}
