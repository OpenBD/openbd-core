/* 
 *  Copyright (C) 2000 - 2009 TagServlet Ltd
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


package com.nary.io;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/*
 * Utility class to read a given inputstream to a string
 */
public class StreamUtils extends Object {

	public static String readToString( InputStream in ) throws IOException{
		return readToString( in, null );
	}
	
	public static String readToString( InputStream in, String _encoding ) throws IOException{
		BufferedReader reader = null;
		InputStreamReader inreader = null; 
		CharArrayWriter writer = null;
		
		try {
			inreader = _encoding == null ? new InputStreamReader(in) : new InputStreamReader(in, _encoding );
			reader = new BufferedReader( inreader );
			writer = new CharArrayWriter();
			
			char [] chars = new char[2048];
			int read;
			while ( (read=reader.read(chars, 0, chars.length) ) != -1 ){
				writer.write( chars, 0, read );
			}
			
			return writer.toString();
		}finally{
			if ( in != null )try{ in.close(); }catch( IOException ignored ){}
			if ( inreader != null )try{ inreader.close(); }catch( IOException ignored ){}
		}
	}
	
}
