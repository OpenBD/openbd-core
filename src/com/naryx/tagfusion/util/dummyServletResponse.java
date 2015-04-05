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

/*
 * This is a dummy ServletResponse that allows various tags to have a virtual session
 * 
 * CFMULTICAST / CFTHREAD / CFMESSAGE
 *  
 * @author Alan Williamson
 */
package com.naryx.tagfusion.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.naryx.tagfusion.cfm.engine.cfEngine;


public class dummyServletResponse implements HttpServletResponse {

  ServletOutputStream outputStream;
  int statusCode = 0;
  
  public dummyServletResponse(){
    this( false );
  }

  public dummyServletResponse( boolean bufferOutput ){
    if ( bufferOutput ){
      outputStream = new bufferServletOutputStream();
    }else{
      outputStream = new nullServletOutputStream();
    }
  }
  
  public int getStatusCode(){
  	return statusCode;
  }
  
	public void addCookie(Cookie arg0){}

	public boolean containsHeader(String arg0) {	return false;	}

	public String encodeURL(String arg0) { return arg0;	}
	public String encodeRedirectURL(String arg0) { return arg0;	}
	public String encodeUrl(String arg0) {	return arg0;}
	public String encodeRedirectUrl(String arg0) {return arg0;}

	public void sendError(int arg0, String arg1) throws IOException {}
	public void sendError(int arg0) throws IOException {}
	public void sendRedirect(String arg0) throws IOException {}

	public void setDateHeader(String arg0, long arg1) {}
	public void setHeader(String arg0, String arg1) {}

	public void setIntHeader(String arg0, int arg1) {}
	public void setStatus(int _statusCode) { statusCode = _statusCode; }
	public void setStatus(int _statusCode, String arg1) { statusCode = _statusCode; }
	
	public void addHeader(String arg0, String arg1) {}
	public void addDateHeader(String arg0, long arg1) {}

	public void addIntHeader(String arg0, int arg1) {}

	public String getCharacterEncoding() { return cfEngine.getDefaultEncoding(); }

	public Locale getLocale() {	return null;}

	public ServletOutputStream getOutputStream() throws IOException {
		return outputStream;
	}

	public PrintWriter getWriter() throws IOException {
		return new PrintWriter( outputStream );
	}

	public void setContentLength(int arg0) {}
	public void setContentType(String arg0) {}
	public boolean isCommitted() {return false;}
	
  public void reset() {}
  
	public void resetBuffer() {
	  if ( outputStream instanceof bufferServletOutputStream )
      ((bufferServletOutputStream)outputStream).reset();
  }

  public void setBufferSize(int arg0) {}
  public int getBufferSize() {
    if ( outputStream instanceof bufferServletOutputStream )
      return ((bufferServletOutputStream)outputStream).getBufferSize();
    else
      return 0; 
  }
  
	public void flushBuffer() throws IOException {}
	public void setLocale(Locale arg0) {}
  
  
  /*
   * Specific to dummyServletResponse
   */
  public String getOutput(){
    if ( outputStream instanceof bufferServletOutputStream )
      return ((bufferServletOutputStream)outputStream).getBuffer();
    else
      return ""; 
  }
  
  /*
   * Inner classes for handling the output
   * 
   * nullServletOutputStream - eats all the content it is given
   * 
   * bufferServletOutputStream - buffers all the content it is given 
   */
  
  public class nullServletOutputStream extends ServletOutputStream {
    public void write(int throwAway) throws IOException {}
  }
  
  public class bufferServletOutputStream extends ServletOutputStream {
    ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
    
    public void write(int ch) throws IOException {
      outBuffer.write( ch );
    }
    
    public void reset(){
      outBuffer = new ByteArrayOutputStream();
    }
    
    public String getBuffer(){
      return outBuffer.toString();
    }
    
    public int getBufferSize(){
      return outBuffer.size();
    }
  }

  public String getContentType() {
  	return null;
  }

  public void setCharacterEncoding( String arg0 ) {
  }
}
