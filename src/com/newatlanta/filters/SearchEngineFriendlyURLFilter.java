/*
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  http://openbd.org/
 *  
 *  $Id: $
 */

package com.newatlanta.filters;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/*
 * SearchEngineFriendlyURLFilter
 *
 * This filter is used to allow BlueDragon to process search engine friendly
 * URLs. An example of this is /bdj2eedev/index.cfm/year/2006. This filter will
 * forward a request like this onto BlueDragon with a ServletPath of /index.cfm
 * and PathInfo of /year/2006.
 */
public final class SearchEngineFriendlyURLFilter implements Filter {
	private FilterConfig fConfig = null;

	private String[] extensions = null;

	private boolean debug = false;

	public SearchEngineFriendlyURLFilter() {
		super();
	}

	/**
	 * This method fixes bug #3275: "SearchEngineFriendlyURLFilter may sometimes match a request to the wrong alias" This method will also ignore duplicates, and strip off unwanted whitespace
	 * 
	 * @param mappings
	 * @param aliasToAdd
	 */
	private static void addAlias(Vector<String> mappings, String aliasToAdd) {
		int size = mappings.size();
		int i = 0;
		aliasToAdd = aliasToAdd.trim();

		for (; i < size; i++) {
			String storedAlias = mappings.elementAt(i);
			if (aliasToAdd.length() > storedAlias.length()) {
				mappings.insertElementAt(aliasToAdd, i);
				return;
			} else if (aliasToAdd.equals(storedAlias))
				// The aliases are the same so do nothing
				return;
		}

		// The alias wasn't inserted so place it at the end
		if (i == size)
			mappings.addElement(aliasToAdd);
	}

	/**
	 * This method takes an array Strings and converts it to a single String where each element is separated by a delimitor
	 * 
	 * @param list
	 * @param delimitor
	 * @return
	 */
	public static String stringify(String[] list, String delimitor) {
		StringBuffer buf = new StringBuffer();

		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				if (i > 0)
					buf.append(delimitor);

				buf.append(list[i]);
			}
		}

		return buf.toString();
	}

	/*
	 * init
	 * 
	 * Extracts and stores the configuration data for the filter.
	 */
	public void init(FilterConfig fConfig) throws ServletException {

		this.fConfig = fConfig;

		String debugStr = fConfig.getInitParameter("debug");
		if ((debugStr != null) && (debugStr.equalsIgnoreCase("true")))
			debug = true;

		logDebug("initializing");

		Vector<String> v = new Vector();
		String extensionsParam = fConfig.getInitParameter("extensions");
		if (extensionsParam == null) {
			logDebug("using default extensions");

			v.addElement("cfml");
			v.addElement("cfm");
		} else {
			logDebug("supplied extensions - " + extensionsParam);

			StringTokenizer st = new StringTokenizer(extensionsParam, ",");
			while (st.hasMoreTokens())
				addAlias(v, st.nextToken());
		}

		extensions = new String[v.size()];
		v.toArray(extensions);

		logDebug("extensions will be matched to the request URI in the following order - [" + stringify(extensions, ",") + ']');
	}

	/*
	 * doFilter
	 * 
	 * If the request contains an extension that the filter is configured to process and it contains path info then this method makes a request wrapper to return proper ServletPath and PathInfo values and forwards the request onto BlueDragon.
	 */
	public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest) req;
		String uri = httpReq.getRequestURI();

		logDebug("filtering uri - " + uri);

		String foundExtension = null;
		int extensionPosition = -1;
		for (int i = 0; i < extensions.length; i++) {
			extensionPosition = uri.indexOf("." + extensions[i]);
			if ( extensionPosition != -1 ){
				foundExtension = extensions[i];
			}
		}
		
		if (foundExtension != null) {
			int servletPathEnd = extensionPosition + foundExtension.length() + 1;
			if (uri.length() != servletPathEnd) {
				logDebug("found extension match - " + foundExtension);

				// The URI contains an extension that the filter is configured to look
				// for and there's
				// path info so update the servletPath and pathInfo in a request
				// wrapper and let BD process it.
				int contextPathLen = httpReq.getContextPath().length();
				if (contextPathLen == 1)
					contextPathLen = 0;
				String servletPath = uri.substring(contextPathLen, servletPathEnd);
				String pathInfo = uri.substring(servletPathEnd);
				ReqWrapper reqW = new ReqWrapper(httpReq, servletPath, pathInfo);
				RequestDispatcher rd = req.getRequestDispatcher(servletPath);
				rd.forward(reqW, rsp);
				return;
			}
		}

		// The URI doesn't contain an extension that the filter is configured
		// to look for or there's no path info so process it as it is.
		chain.doFilter(req, rsp);
		return;
	}

	/*
	 * destroy
	 * 
	 * Does nothing...
	 */
	public void destroy() {
	}

	/*
	 * logDebug
	 * 
	 * Logs debug message to System.out and ServletContext.log().
	 */
	private void logDebug(String msg) {
		if (debug) {
			System.out.println("DEBUG SearchEngineFriendlyURLFilter: " + msg);
			fConfig.getServletContext().log("DEBUG SearchEngineFriendlyURLFilter: " + msg);
		}
	}

	/*
	 * ReqWrapper
	 * 
	 * This class is used to wrap the original request and to return proper values for ServletPath and PathInfo.
	 */
	class ReqWrapper extends HttpServletRequestWrapper {
		private String servletPath;

		private String pathInfo;

		public ReqWrapper(HttpServletRequest request) {
			super(request);
		}

		public ReqWrapper(HttpServletRequest request, String servletPath, String pathInfo) {
			super(request);
			this.servletPath = servletPath;
			this.pathInfo = pathInfo;
		}

		public String getServletPath() {
			return servletPath;
		}

		public String getPathInfo() {
			return pathInfo;
		}
	}
}
