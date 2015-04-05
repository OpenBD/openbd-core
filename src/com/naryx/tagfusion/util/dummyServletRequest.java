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

/*
 * @author Alan Williamson
 */
package com.naryx.tagfusion.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.nary.io.FileUtils;

public class dummyServletRequest implements HttpServletRequest {

	private Hashtable	attributes;
  private String    webServerRoot;

  public dummyServletRequest() {
    this(null);
  }

  public dummyServletRequest(String webServerRoot) {
		attributes	       = new Hashtable();
    this.webServerRoot = webServerRoot;
    
    if ( this.webServerRoot != null && !this.webServerRoot.endsWith( java.io.File.separator ) )
      this.webServerRoot  = this.webServerRoot + java.io.File.separator;
	}

  public String getRealPath(String uri) {
    /*
     * We need to provide a very minimal implementation of this method
     * for background thread processing, such as for CFMESSAGE, CFFORK,
     * onApplicationEnd(), and onSessionEnd() to support CFINCLUDE,
     * CFMODULE, and CFC relative (URI) path lookups
     */
    if ( this.webServerRoot == null ) {
    	return null;
    }
    return FileUtils.combine( webServerRoot, uri );
  }
  
	public String getAuthType() {	return null;}

	public Cookie[] getCookies() {return null;}

	public long getDateHeader(String arg0) {return 0;}

	public String getHeader(String arg0) {return null;}

	public Enumeration getHeaderNames() {	return null;}

	public int getIntHeader(String arg0) {return 0;}

	public String getMethod() {	return "";	}

	public String getPathInfo() {	return "/";	}

	public String getPathTranslated() {	return null; }

	public String getQueryString() {	return "";}

	public String getRemoteUser() {	return "";	}

	public String getRequestedSessionId() {	return null;}

	public String getRequestURI() {return "/";}

	public String getServletPath() {return "index.cfm";}

	public HttpSession getSession(boolean arg0) {	return null;}
	public HttpSession getSession() {	return null;}

	public boolean isRequestedSessionIdValid() {return false;}

	public boolean isRequestedSessionIdFromCookie() {	return false;}

	public boolean isRequestedSessionIdFromURL() {return false;	}

	public StringBuffer getRequestURL() {	return new StringBuffer("/");	}

	public boolean isRequestedSessionIdFromUrl() {return false;}

	public Enumeration getHeaders(String arg0) {return null;}

	public String getContextPath() {	return "/";	}

	public boolean isUserInRole(String arg0) {	return false;}

	public Principal getUserPrincipal() {	return null;}

	public Object getAttribute(String arg0) {	return attributes.get(arg0);	}

	public Enumeration getAttributeNames() {	return attributes.keys();	}

	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {}

	public Map getParameterMap() {return null;}

	public String getCharacterEncoding() {return null;}

	public int getContentLength() {	return 0;}

	public String getContentType() {return "text/html";}

	public ServletInputStream getInputStream() throws IOException {		return null;	}

	public String getParameter(String arg0) {	return null;	}
	public Enumeration getParameterNames() {return null;}
	public String[] getParameterValues(String arg0) {	return new String[0];	}

	public String getProtocol() {	return "";	}
	public String getScheme() {	return null;	}

	public String getServerName() {return "BlueDragon";	}

	public int getServerPort() {return 80;}

	public BufferedReader getReader() throws IOException {
		return null;
	}

	public String getRemoteAddr() {	return "127.0.0.1";	}
	public String getRemoteHost() {	return "127.0.0.1";	}

	public void setAttribute(String arg0, Object arg1) {
		attributes.put(arg0,arg1);
	}

	public Locale getLocale() {return null;	}
	public Enumeration getLocales() {return null;	}

	public boolean isSecure() {
		return false;
	}

	public RequestDispatcher getRequestDispatcher(String arg0) {return null;}

	public void removeAttribute(String arg0) {
		attributes.remove( arg0 );
	}

	public String getLocalAddr() {
		return null;
	}

	public String getLocalName() {
		return null;
	}

	public int getLocalPort() {
		return 0;
	}

	public int getRemotePort() {
		return 0;
	}
}
