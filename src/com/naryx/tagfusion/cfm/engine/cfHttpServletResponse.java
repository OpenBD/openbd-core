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

package com.naryx.tagfusion.cfm.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.nary.util.BMPattern;
import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.application.cfClientSessionData;
import com.naryx.tagfusion.cfm.cookie.cfCookieData;

public class cfHttpServletResponse extends HttpServletResponseWrapper {

	// for buffering the entire response (buffer can't be larger than this)
	public static final int MAX_SIZE = 128 * 1024;
	public static final int UNLIMITED_SIZE = Integer.MAX_VALUE;
	private static int DEFAULT_SIZE = 32 * 1024;
	private static final int CHILD_DEFAULT_SIZE = 4 * 1024;
	private static int USER_SIZE = DEFAULT_SIZE;

	public static void setUserSize(int size) {
		if (size <= 0) {
			USER_SIZE = UNLIMITED_SIZE;
		} else {
			USER_SIZE = size;
			if (USER_SIZE < DEFAULT_SIZE)
				DEFAULT_SIZE = USER_SIZE;
		}
	}

	public static int getUserSize() {
		return USER_SIZE;
	}

	private static String DEFAULT_CONTENT_TYPE = "text/html; charset=" + cfEngine.getDefaultCharset();

	private cfStringWriter writer; // for writing string data

	private ServletOutputStream outputStream; // for writing binary data

	private cfHttpServletResponse parent;

	private boolean jspInclude = false;

	public cfHttpServletResponse(cfSession _session, HttpServletResponse response) {
		super(response);
		writer = new cfStringWriter(_session, response, DEFAULT_SIZE, USER_SIZE);
	}

	public cfHttpServletResponse createChild(cfSession _session) {
		return new cfHttpServletResponse(this, _session);
	}

	// used only for creating children
	private cfHttpServletResponse(cfHttpServletResponse _parent, cfSession _session) {
		super((HttpServletResponse) _parent.getResponse());

		writer = new cfStringWriter(_session, (HttpServletResponse) _parent.getResponse(), CHILD_DEFAULT_SIZE, UNLIMITED_SIZE);
		writer.setSuppressWhiteSpace(_parent.isSuppressWhiteSpace());

		parent = _parent;
	}

	public void setJspInclude(boolean include) {
		jspInclude = include;
	}

	public boolean isJspInclude() {
		return jspInclude;
	}

	public boolean isSuppressWhiteSpace() {
		return writer.isSuppressWhiteSpace();
	}

	public void setSuppressWhiteSpace(boolean suppress) {
		writer.setSuppressWhiteSpace(suppress);
	}

	public void write(String str) {
		writer.write(str);
	}

	public void write(char[] c, int off, int len) {
		writer.write(c, off, len);
	}

	public void write(cfSession _session, byte[] buf) throws IOException {
		if (outputStream == null) {
			super.setContentType(writer.getContentType());
			addCookies(_session);
			outputStream = getOutputStream();
		}
		outputStream.write(buf);
	}

	public void write(cfSession _session, byte[] buf, int off, int len) throws IOException {
		if (outputStream == null) {
			super.setContentType(writer.getContentType());
			addCookies(_session);
			outputStream = getOutputStream();
		}
		outputStream.write(buf, off, len);
	}

	public ServletOutputStream getOutputStream() throws IOException {
		// With WLS 8.1SP4 and 9.0, a rd.include() causes it to call
		// getOutputStream()
		// so we need to return our own ouput stream that will write the data to our
		// internal buffer. Refer to bug #2375.
		if (jspInclude)
			return new _ServletOutputStream();

		outputStream = (parent != null ? parent.getOutputStream() : super.getOutputStream());
		return outputStream;
	}

	// ---[ Inner class to provide the handling for the ServletOutput Stream
	class _ServletOutputStream extends ServletOutputStream {
		public _ServletOutputStream() {
		}

		public void write(int c) {
			writer.write(c);
		}
	}

	public void setContentType(String contentType) {
		if (parent != null) {
			parent.setContentType(contentType);
		}
		writer.setContentType(contentType);
	}

	public void setStatus(int statusCode) {
		if (!jspInclude) {
			if (isCommitted()) {
				throw new IllegalStateException("Response committed, cannot set status");
			}
			super.setStatus(statusCode);
		}
	}

	public void setStatus(int statusCode, String value) {
		if (!jspInclude) {
			if (isCommitted()) {
				throw new IllegalStateException("Response committed, cannot set status");
			}
			super.setStatus(statusCode, value);
		}
	}

	public void addHeader(String name, String value) {
		if (!jspInclude) {
			if (isCommitted()) {
				throw new IllegalStateException("Response committed, cannot add header");
			}
			super.addHeader(name, value);
		}
	}

	public void setHeader(String name, String value) {
		if (!jspInclude) {
			if (isCommitted()) {
				throw new IllegalStateException("Response committed, cannot set header");
			}
			super.setHeader(name, value);
		}
	}

	public void setHeadElement(String str, boolean append) {
		if (parent != null) {
			parent.setHeadElement(str,append);
		} else {
			if (isCommitted()) {
				throw new IllegalStateException("Response committed, cannot set HEAD element");
			}
			writer.setHeadElement(str,append);
		}
	}

	public void setBodyElement(String str, boolean append) {
		if (parent != null) {
			parent.setBodyElement(str,append);
		} else {
			if (isCommitted()) {
				throw new IllegalStateException("Response committed, cannot set BODY element");
			}
			writer.setBodyElement(str,append);
		}
	}

	public void flush() {
		// if there's a parent, flush its output first
		if (parent != null) {
			parent.flush();
		}

		// now flush our own output
		if (outputStream != null) {
			try {
				outputStream.flush();
			} catch (IOException ignore) {
				// only happens if client disconnects
			}
		} else if (parent != null) {
			// write to the parent, then flush
			parent.write(writer.toString());
			parent.flush();
			writer.resetBuffer();
		} else if (!jspInclude) {
			writer.flush();
		}
	}

	public void reset() {
		writer.reset();
	}

	public void resetBuffer() {
		if (parent != null) {
			parent.resetBuffer();
		}
		writer.resetBuffer();
	}

	public PrintWriter getWriter() {
		return new PrintWriter(writer);
	}

	public void sendRedirect(String location) throws IOException {
		writer.addCookies();
		super.sendRedirect(location);
	}

	public void setBufferSize(int size) {
		writer.setBufferSize(size > MAX_SIZE ? UNLIMITED_SIZE : size);
	}

	public String getOutputAsString() {
		if (parent == null) {
			writer.writeHeadBodyElement();
		}
		return writer.toString();
	}

	
	/*
	 * getString
	 * 
	 * This method was added to support the CfmlJspWriter.getString() method.
	 */
	public String getString() {
		String s;
		if (parent != null)
			s = parent.getOutputAsString();
		else
			s = "";

		return s + writer.toString();
	}

	private static void addCookies(cfSession _session) {
		if (_session.RES.isCommitted()){
			return;
		}
		
		/**
		 * If the client data is saved as cookies then we want these to be sent out
		 * on a flush. Otherwise they'll not appear at all.
		 */
		cfApplicationData appData = _session.getApplicationData();
		if ((appData != null) && appData.isClientEnabled()) {
			cfData clientData = _session.getQualifiedData(variableStore.CLIENT_SCOPE);
			if ((clientData != null) && (clientData instanceof cfClientSessionData)) {
				// creates cookie if storage type == COOKIE
				((cfClientSessionData) clientData).flush(_session); 
			}
		}

		// Setup any cookies that may exist
		cfData cookieData = _session.getQualifiedData(variableStore.COOKIE_SCOPE);
		if ((cookieData != null) && (cookieData instanceof cfCookieData)) {
			// add cookies to the servlet response
			((cfCookieData) cookieData).addCookies(); 
		}
	}

	/**
	 * This class is used to wrap the servlet response writer--all output written
	 * to the client must be done via this class. This class performs several
	 * functions:
	 * 
	 * 1. Buffering. The first 32K of client output is buffered by this class.
	 * After the first 32K is flushed to the client, the remaining output is
	 * written directly to the underlying servlet response writer.
	 * 
	 * 2. Whitespace suppression. If whitespace suppression is enabled, all runs
	 * of whitespace are collapsed to a single space character.
	 */
	private class cfStringWriter extends Writer {

		private StringBuilder sb;

		private int bufferSize; // the maximum size of the buffer

		private String contentType = DEFAULT_CONTENT_TYPE;
		private boolean suppressWhiteSpace;
		private boolean lastCharWhiteSpace;
		private boolean fullFlush = false;

		private cfSession session;
		private HttpServletResponse response;
		private PrintWriter responseWriter;

		private StringBuilder headElement;
		private boolean	headAppend = true;
		private StringBuilder bodyElement;
		private boolean	bodyAppend = true;

		private cfStringWriter(cfSession _session, HttpServletResponse _response, int initialSize, int maxSize) {
			sb = new StringBuilder(initialSize);
			bufferSize = maxSize;
			session = _session;
			response = _response;
		}

		private void setBufferSize(int size) {
			if (size != UNLIMITED_SIZE) {
				if (sb.length() >= size)
					flush();

				// do a full flush when buffer is full because that's what the
				// user wants (this was invoked from CFFLUSH INTERVAL="<size>")
				fullFlush = true;
			}

			bufferSize = size;
		}

		private void setContentType(String _contentType) {
			if (responseWriter != null) {
				throw new IllegalStateException("Response committed, cannot set content type");
			}
			contentType = _contentType;
		}

		private String getContentType() {
			return contentType;
		}

		private boolean isSuppressWhiteSpace() {
			return suppressWhiteSpace;
		}

		private void setSuppressWhiteSpace(boolean suppress) {
			suppressWhiteSpace = suppress;
			lastCharWhiteSpace = false;
		}

		public void write(char[] cbuf, int off, int len) {
			//sb.append(cbuf, off, len);
			
			if (suppressWhiteSpace) {

				for ( int i=0;i<len;i++)
					write(cbuf[i]);

			}else{

				// If we are paging out; delegate to the write(str)
				if ((bufferSize > 0) && (bufferSize < cfHttpServletResponse.UNLIMITED_SIZE)) {
					write( new String(cbuf,off,len) );
					return;
				}


				// Output to the response
				if (responseWriter != null) {
					responseWriter.write(cbuf,off,len);
				} else {
					if (sb.append(cbuf,off,len).length() >= bufferSize)
						flushToResponseWriter();
				}

			}
			
		}

		public void write(String str) {
			if (suppressWhiteSpace) {
				for (int i = 0; i < str.length(); i++) {
					write(str.charAt(i));
				}
			} else {
				if ((bufferSize > 0) && (bufferSize < cfHttpServletResponse.UNLIMITED_SIZE)) {
					int available = bufferSize - sb.length();
					while (str.length() > available) {
						write(str.substring(0, available)); // causes flush()
						str = str.substring(available);
						available = bufferSize; // sb.length() == 0 after flush()
					}
				}

				if (responseWriter != null) {
					responseWriter.write(str);
				} else {
					if (sb.append(str).length() >= bufferSize)
						flushToResponseWriter();
				}
			}
		}

		public final void write(int c) {
			if (suppressWhiteSpace) {
				boolean isWhiteSpace = Character.isWhitespace((char) c);

				if (lastCharWhiteSpace && isWhiteSpace) {
					// replace previous whitespace char with newline
					if (((char) c == '\n') && (sb.length() > 0) && (responseWriter == null))
						sb.setCharAt(sb.length() - 1, (char) c);
				} else {
					writeChar(c);
				}

				lastCharWhiteSpace = isWhiteSpace;
			} else {
				writeChar(c);
			}
		}

		private final void writeChar(int c) {
			if (responseWriter != null) {
				responseWriter.write(c);
			} else {
				if (sb.append((char)c).length() >= bufferSize)
					flushToResponseWriter();
			}
		}

		private void setHeadElement(String str, boolean append) {
			if (headElement == null) {
				headElement = new StringBuilder(str.length());
			}
			headAppend = append;
			headElement.append(str);
		}

		private void setBodyElement(String str, boolean append) {
			if (bodyElement == null) {
				bodyElement = new StringBuilder(str.length());
			}
			bodyAppend = append;
			bodyElement.append(str);
		}

		private void writeHeadBodyElement() {
			if ( headElement != null && headElement.length() > 0 ) {

				// --[ Find the position to insert into
				if ( headAppend ){
					BMPattern pattern = new BMPattern("</head>", true);
					int headEndTagPos = pattern.matches(sb.toString(), 0, sb.length());
					sb.insert(headEndTagPos == -1 ? 0 : headEndTagPos, headElement.toString());
				}else{
					BMPattern pattern = new BMPattern("<head>", true);
					int headEndTagPos = pattern.matches(sb.toString(), 0, sb.length());
					sb.insert(headEndTagPos == -1 ? 0 : headEndTagPos + 6, headElement.toString());
				}
	
				headElement = null;
				
			}

			if ( bodyElement != null && bodyElement.length() > 0 ) {

				// --[ Find the position to insert into
				if ( bodyAppend ){
					BMPattern pattern = new BMPattern("</body>", true);
					int bodyEndTagPos = pattern.matches(sb.toString(), 0, sb.length());
					sb.insert(bodyEndTagPos == -1 ? 0 : bodyEndTagPos, bodyElement.toString());
				}else{
					BMPattern pattern = new BMPattern("<body>", true);
					int bodyEndTagPos = pattern.matches(sb.toString(), 0, sb.length());
					sb.insert(bodyEndTagPos == -1 ? 0 : bodyEndTagPos + 6, bodyElement.toString());
				}
	
				bodyElement = null;
			}
			
		}

		public void reset() {
			sb.setLength(0);
			response.reset();
			contentType = DEFAULT_CONTENT_TYPE;
		}

		public void resetBuffer() {
			sb.setLength(0);
			try {
				if (responseWriter != null) {
					response.resetBuffer();
				}
			} catch (NoSuchMethodError e) { // for WebSphere 4.0
				response.reset();
			}
		}

		public void flush() {
			if (jspInclude)
				return;

			if (responseWriter == null) {
				fullFlush = true;
				flushToResponseWriter();
			} else {
				responseWriter.flush();
			}
		}

		private void flushToResponseWriter() {
			try {
				response.setContentType(contentType);

				// Bug #2468:
				// the Servlet API states that you can't call both getOutputstream() and
				// getWriter()
				// In the case where getOutputStream() has already been called (only
				// where CFCONTENT has been used) we create a dummy
				// writer essentially thus any remaining output is not written to the
				// real response
				// Note that the outputStream should only be null if CFCONTENT has been
				// used to return binary data
				if (outputStream != null) {
					responseWriter = new PrintWriter(new ByteArrayOutputStream());
				} else {
					responseWriter = response.getWriter();
				}

				addCookies();
				writeHeadBodyElement();

				responseWriter.write(sb.toString());
				sb.setLength(0);

				if (fullFlush) {
					responseWriter.flush();
				}

				// after flushing once, we're going to write all output
				// to the underlying responseWriter
				bufferSize = UNLIMITED_SIZE;
			} catch (IOException e) {
				cfEngine.log("Error flushing response buffer: " + e);
			}
		}

		private void addCookies() {
			cfHttpServletResponse.addCookies(session);
		}

		public String toString() {
			if (responseWriter != null) { // response has already been sent to browser
				return "";
			} else {
				return sb.toString();
			}
		}

		public void close() throws IOException {
		}
	}
}
