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

import java.io.RandomAccessFile;

import com.nary.util.Date;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.parser.script.CFParsedStatement;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.cfm.tag.cfFUNCTION;
import com.naryx.tagfusion.cfm.tag.cfParseTag;
import com.naryx.tagfusion.cfm.tag.cfTag;
 
public class logDebugger extends Object implements debuggerListener {
	
	RandomAccessFile	outFile;
	cfSession	thisSession;
	int	traceStep	= 0;
	boolean tagOnly;
	
	public logDebugger( String outputFile, boolean _tagOnly ) throws Exception {
		tagOnly	= _tagOnly;
		outFile	= new RandomAccessFile( outputFile, "rw" );
		outFile.seek( outFile.length() );
	}
	
	public void registerSession( cfSession _thisSession ){
		thisSession	= _thisSession;
		writeLine( "CFDEBUGGER trace started @ " + Date.formatNow() );
		
		if ( thisSession.activeFile() != null ){
			writeLine( "active.file=" + thisSession.activeFile().getName() );
		}
	}
	
	//------------------------------------------------
	
	public void startTag( cfTag thisTag ){
		writeLine( "tag.start=" + thisTag.getTagName() + "; L/C=(" + thisTag.posLine + "," + thisTag.posColumn + "); File=" + thisTag.getFile().getName() );
	}
	
	public void endTag( cfTag thisTag ){
		writeLine( "tag.end=" + thisTag.getTagName() + "; L/C=(" + thisTag.posLine + "," + thisTag.posColumn + "); File=" + thisTag.getFile().getName() );
	}

	//------------------------------------------------
	
	public void startScriptStatement( CFParsedStatement statement ) {}

	//------------------------------------------------
	
	public void startFile( cfFile thisFile ){
		writeLine( "file.start=" + thisFile.getName() );
	}
		
	public void endFile( cfFile thisFile ){
		writeLine( "file.end=" + thisFile.getName() );
	}

	//------------------------------------------------
	
	public void setHTTPHeader( String name, String value ){
		writeLine( "http.header;  name=" + name + "; value=" + value );
	}
	
	public void setHTTPStatus( int sc, String value ){
		writeLine( "http.statuscode=" + sc + "; value=" + value );
	}
	
	//------------------------------------------------
	
	@Override
	public void writtenBytes( int total ){
	}

	public void runExpression(CFExpression expr){}
	
	public void write( int ch ){
		if ( tagOnly ) return;
		
		writeLine( "[" + convertChar(ch) + "]" );
	}
	
	public void write( byte[] arrayCh ){
		if ( tagOnly ) return;
		
		StringBuilder st = new StringBuilder( arrayCh.length );
		for ( int x=0; x < arrayCh.length; x++ )
			st.append( convertChar( (int)arrayCh[x] ) );
			
		writeLine( "[" + st.toString() + "]" );
	}
	
	//------------------------------------------------
	
	public void endSession(){
		writeLine( "Session Ended\r\n" );

		try{
			outFile.close();
		}catch(Exception E){}
	}
	
	private void writeLine( String line ){
		try{
			outFile.writeBytes( new String( "#" + (traceStep++) + ": " ) );
			outFile.writeBytes( line + "\r\n" );
		}catch(Exception E){}
	}
	
	private static String convertChar( int ch ){
		if ( ch == cfParseTag.CHAR_LINEFEED )
			return "<LF>";
		else if ( ch == cfParseTag.CHAR_CARRIAGERETURN )
			return "<CR>";
		else if ( ch == cfParseTag.CHAR_TAB )
			return "<TAB>";
		else
			return "" + (char)ch;
	}
	
	public final void startFunction(cfFUNCTION cfFUNCTION) {}
	public final void endFunction(cfFUNCTION cfFUNCTION) {}

	public final void startScriptFunction(userDefinedFunction userDefinedFunction) {}
	public final void endScriptFunction(userDefinedFunction userDefinedFunction) {}

}
