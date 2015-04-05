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

package com.nary.net.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;

/**
 * Class that provides methods for URLEncoding. Req'd for jdks below 1.4
 * where java.net.URLEncoder doesn't provide an encode method that
 * takes a charset parameter
 */

public class urlEncoder {

  // permitted chars in the url
  private static final BitSet SAFECHARS = new BitSet(256);
  
  static {
    for (int i = 'a'; i <= 'z'; i++) {
      SAFECHARS.set(i);
    }
    for (int i = 'A'; i <= 'Z'; i++) {
      SAFECHARS.set(i);
    }
    for (int i = '0'; i <= '9'; i++) {
      SAFECHARS.set(i);
    }
    
  }

  // encodes with UTF-8 charset, which is the W3C recommendation (see
  // javadoc comments for java.net.URLEncoder
  @SuppressWarnings("deprecation")
	public static String encode( String s ) {
      try {
        return encode( s, "UTF-8" );
    } catch ( UnsupportedEncodingException e ) {
        // this should not be possible?!
        return java.net.URLEncoder.encode( s );
    }
  }

  public static String encode( String s, String _enc ) throws UnsupportedEncodingException{
  	if ( !needsEncoded( s ) )
			return s;
		
    boolean wroteUnencodedChar = false; 
    int strLen = s.length();
    int maxBytes = 10; // max no. of bytes req'd for a single char   
    String enc = com.nary.util.Localization.convertCharSetToCharEncoding( _enc );
    
    StringBuilder out = new StringBuilder( strLen );
    
    ByteArrayOutputStream buf = new ByteArrayOutputStream( maxBytes );
    OutputStreamWriter writer = new OutputStreamWriter( buf, enc );

    for (int i = 0; i < strLen; i++) {
      int c = (int) s.charAt(i);
      
      // if we don't need to encode this char
      if ( SAFECHARS.get(c) ){
      	if ( wroteUnencodedChar ){ // write out any chars that have been written to the writer
	        writeHex( out, buf.toByteArray() );
	        buf.reset();
	        wroteUnencodedChar = false;
      	}
      	
      	out.append( (char) c );
      }else{
        // convert to external encoding before hex conversion
        try {
        	wroteUnencodedChar = true;

        	writer.write(c);
          if (c >= 0xD800 && c <= 0xDBFF) {
            if ( (i+1) < strLen ) {
              int d = (int) s.charAt(i+1);
              if ( d >= 0xDC00 && d <= 0xDFFF ) {
                writer.write(d);
                i++;
              }
            }
          }
          writer.flush();
        }catch( IOException e ){
          buf.reset();
          continue;
        }
        
      }
    }

    // write out any remaining characters in the buffer
  	if ( wroteUnencodedChar ){
  		writeHex( out, buf.toByteArray() );
  	}

    return out.toString();
  }
  
  private static void writeHex( StringBuilder _out, byte [] _ba ){
    for (int j = 0; j < _ba.length; j++) {
    	_out.append('%');
      char ch = Character.toUpperCase( Character.forDigit( (_ba[j] >> 4) & 0xF, 16 ) );
      _out.append(ch);
      ch = Character.toUpperCase( Character.forDigit( _ba[j] & 0xF, 16 ) );
      _out.append(ch);
    }
  }
  

	private static boolean needsEncoded( String _s ){
	
	  int strLen = _s.length();
	  for(int i = 0; i < strLen; i++){
	      int ch = _s.charAt(i);
	      if ( !( (ch >= 'a' && ch <= 'z') || (ch>= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') ) ){
	      	return true;
	      }
	  }
	
	  return false;
	}

  
}// urlEncoder
