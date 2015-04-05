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

/**
 * This is a wrapper around the ServletResponse interface for CFFORWARD
 * 
 * This class is also used by CFCACHE to retrieve the page that is to be
 * cached.  The status methods are overridden to capture the response code
 * for the page.
 *
 */

package com.naryx.tagfusion.servlet.jsp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;

public class cfIncludeHttpServletResponseWrapper extends HttpServletResponseWrapper {
	
	ByteArrayOutputStream	outputBuffer;
	OutputStreamWriter		osWriter;
	PrintWriter 					outWriter;

	cfStructData	responseHeaders;

	String encoding;

	HttpServletResponse res;	
	String				redirectLocation;
	int						statusCode;
					
	public cfIncludeHttpServletResponseWrapper( HttpServletResponse response ) {
		super( response );
			
		res = response;
		encoding = response.getCharacterEncoding();
		outputBuffer	= new ByteArrayOutputStream( 8192 );
		responseHeaders	= new cfStructData();
		outWriter		= null;
		statusCode	= SC_OK;
	}
		
	public PrintWriter getWriter()  throws java.io.IOException {
		osWriter = new OutputStreamWriter( outputBuffer, encoding );
		outWriter = new PrintWriter( osWriter, true );
		return outWriter;
	}
		
	public ServletOutputStream getOutputStream() throws java.io.IOException {
		return new _ServletOutputStream();
	}
		
	public byte[] getByteArray()
	{
		if ( outWriter != null )
			outWriter.flush();
			
		return outputBuffer.toByteArray();
	}

	public cfStructData	getResponseHeaders(){
		return responseHeaders;
	}
		
	public void addIntHeader(String name,  int value){ responseHeaders.setData( name, new cfNumberData(value) );	}
	public void setIntHeader(String name,  int value){ addIntHeader( name, value );}
		
	public void addHeader(String name, String value){ responseHeaders.setData( name, new cfStringData(value) );	}
	public void setHeader(String name, String value){ addHeader( name, value );	}
				
	public void addDateHeader(String name, long date){ responseHeaders.setData( name, new cfDateData(date) );}
	public void setDateHeader(String name, long date){ addDateHeader( name, date );	}
	
	public boolean containsHeader(String name){	return responseHeaders.containsKey(name);	}

	public void setStatus( int sc ){statusCode	= sc;}
	public void setStatus( int sc, String sm ){statusCode	= sc;}
	public void sendError( int sc ){statusCode	= sc;}
	public void sendError( int sc, String sm ){statusCode	= sc;}

	public void sendRedirect( String loc ){
		//-- CFCACHE requirement
		//-- We need to know if a redirect was called.  If so then 
		//-- we do not wish to cache this.
		statusCode				= SC_TEMPORARY_REDIRECT;
		redirectLocation	= loc;
	}
	
	public String getRedirectURI(){return redirectLocation;}
	public int	getStatusCode(){return statusCode;}
	
	// TODO: should some of these be implemented?	
	public void flushBuffer() throws java.io.IOException {}
	public void reset(){}
	public void resetBuffer(){}

	public void setContentType(java.lang.String type){
		res.setContentType(type);
		// if content type contains charset, set encoding
		int charsetIndex = type.indexOf("charset=" ); 
		if ( charsetIndex != -1 ){
			int charsetEnd = type.indexOf(';', charsetIndex );
			encoding = type.substring( charsetIndex+8, charsetEnd == -1 ? type.length() : charsetEnd ).trim();
		}
				
	}

	public String getCharacterEncoding() {
		return encoding;
	}
	
	public void setContentLength(){}
	public void setBufferSize( int size ){}
	public void setLocale( java.util.Locale Loc ){}
	
	
	//---[ Inner class to provide the handling for the ServletOutput Stream
	class _ServletOutputStream extends ServletOutputStream {
		public _ServletOutputStream(){}
		public void write( int c ) throws IOException {	outputBuffer.write( c ); }
	}
}
