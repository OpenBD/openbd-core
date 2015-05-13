/*
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
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
 */

package com.naryx.tagfusion.cfm.engine;

import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.nary.io.FileUtils;
import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.application.ScriptProtect;

public class cfCGIData extends cfStructData implements java.io.Serializable {

	static final long serialVersionUID = 1;

	transient private HttpServletRequest REQ;

	transient private ServletContext CTX;

	transient private cfSession _session;
	
	transient private boolean bScriptProtect = false;

	private static final String cgiConstants[] = { "SERVER_SOFTWARE", "SERVER_NAME", "GATEWAY_INTERFACE", "SERVER_PROTOCOL", "SERVER_PORT", "SERVER_PORT_SECURE", "REQUEST_METHOD", "PATH_INFO", "PATH_TRANSLATED", "SCRIPT_NAME", "QUERY_STRING", "REMOTE_HOST", "REMOTE_ADDR", "AUTH_TYPE", "AUTH_USER", "REMOTE_USER", "REMOTE_IDENT", "CONTENT_TYPE", "CONTENT_LENGTH", "CONTEXT_PATH", "HTTPS",
			"CF_TEMPLATE_PATH" };

	private static final Map<String, Integer> indexMap = new FastMap<String, Integer>(FastMap.CASE_INSENSITIVE);

	static {
		indexMap.put("remote_addr", new Integer(0));
		indexMap.put("http_remote_addr", new Integer(0));

		indexMap.put("remote_host", new Integer(1));
		indexMap.put("http_remote_host", new Integer(1));

		indexMap.put("server_software", new Integer(2));
		indexMap.put("http_server_software", new Integer(2));

		indexMap.put("server_name", new Integer(3));
		indexMap.put("http_server_name", new Integer(3));

		indexMap.put("server_port", new Integer(4));
		indexMap.put("http_server_port", new Integer(4));

		indexMap.put("content_length", new Integer(5));
		indexMap.put("http_content_length", new Integer(5));

		indexMap.put("content_type", new Integer(6));
		indexMap.put("http_content_type", new Integer(6));

		indexMap.put("server_protocol", new Integer(7));
		indexMap.put("http_server_protocol", new Integer(7));

		indexMap.put("path_info", new Integer(8));
		indexMap.put("http_path_info", new Integer(8));

		indexMap.put("request_uri", new Integer(9));
		indexMap.put("http_request_uri", new Integer(9));

		indexMap.put("path_translated", new Integer(10));
		indexMap.put("http_path_translated", new Integer(10));

		indexMap.put("query_string", new Integer(11));
		indexMap.put("http_query_string", new Integer(11));

		indexMap.put("auth_type", new Integer(12));
		indexMap.put("http_auth_type", new Integer(12));

		indexMap.put("request_method", new Integer(13));
		indexMap.put("http_request_method", new Integer(13));

		indexMap.put("remote_user", new Integer(14));
		indexMap.put("http_remote_user", new Integer(14));
		indexMap.put("auth_user", new Integer(14));
		indexMap.put("http_auth_user", new Integer(14));

		indexMap.put("script_name", new Integer(15));
		indexMap.put("http_script_name", new Integer(15));

		indexMap.put("context_path", new Integer(16));
		indexMap.put("http_context_path", new Integer(16));

		indexMap.put("https", new Integer(17));

		indexMap.put("cf_template_path", new Integer(18));

		indexMap.put("server_port_secure", new Integer(19));
		indexMap.put("http_server_port_secure", new Integer(19));

		indexMap.put("user_agent", new Integer(20));
	}

	private Map<String, String> customHeaderMapping;

	public cfCGIData(cfSession _parent) {
		REQ = _parent.REQ;
		CTX = _parent.CTX;
		_session = _parent;

		// For custom types we have to store the original 'unbuggered around'
		// with name in a hashtable. That way we can use that value to return it.
		customHeaderMapping = new FastMap<String, String>(FastMap.CASE_INSENSITIVE);
		Enumeration<String> E = _parent.REQ.getHeaderNames();

		if (E != null) {
			String oldHdr, newHdr;
			while (E.hasMoreElements()) {
				oldHdr = E.nextElement();
				newHdr = "http_" + oldHdr.replace('-', '_');
				customHeaderMapping.put(newHdr, oldHdr);
				super.setData(newHdr, cfStringData.EMPTY_STRING);
			}
		}
	}

	private static String getCanonicalPath(HttpServletRequest req, String path) {
		String canonicalPath = FileUtils.getCanonicalPath(FileUtils.getRealPath(req, path));
		if (canonicalPath == null) {
			return "";
		}
		return canonicalPath;
	}


	public void setScriptProtect() {
		bScriptProtect = true;
	}

	
	public void setData(cfData _key, cfData _data) throws cfmRunTimeException {
		cfCatchData catchData = new cfCatchData();
		catchData.setMessage("Setting CGI scope variables is not permitted");
		catchData.setType("Application");
		throw new cfmRunTimeException(catchData);
	}

	public synchronized void deleteData(String _key) {
		return; // don't allow deleting from CGI scope
	}

	public synchronized boolean containsKey(String _key) {
		if (super.containsKey(_key))
			return true;

		for (int i = 0; i < cgiConstants.length; i++) {
			if (_key.equalsIgnoreCase(cgiConstants[i]))
				return true;
		}
		return false;
	}

	// return keys in uppercase to match CFMX
	public synchronized Object[] keys() {
		Object[] superKeys = super.keys();
		Object[] keys = new Object[superKeys.length + cgiConstants.length];

		for (int i = 0; i < superKeys.length; i++)
			keys[i] = superKeys[i].toString().toUpperCase();

		for (int j = 0; j < cgiConstants.length; j++)
			keys[j + superKeys.length] = cgiConstants[j];

		return keys;
	}

	public synchronized Set<String> keySet() {
		Set<String> keySet = super.keySet();

		for (int i = 0; i < cgiConstants.length; i++)
			keySet.add(cgiConstants[i]);

		return keySet;
	}

	/**
	 * WARNING! The performance of this method is critical to overall system performance. Do not make any changes to this method without doing before-and-after timing measurements to make sure you have not decreased performance.
	 */
	public cfData getData(String _key) {
		// --[ Note: if a method returns a null, we want it to be a literal null
		// --[ and not an object null. Therefore we cast it to a string using + ""

		Integer index = indexMap.get(_key);
		if (index != null) {
			switch (index.intValue()) {
				case 0:
					return new cfStringData(REQ.getRemoteAddr());
				case 1:
					return new cfStringData(REQ.getRemoteHost());
				case 2:
					return new cfStringData(CTX.getServerInfo());
				case 3:
					return new cfStringData(REQ.getServerName());
				case 4:
					return new cfStringData(REQ.getServerPort() + "");
				case 5:
					return new cfStringData(REQ.getContentLength() + "");
				case 6:
					return new cfStringData(REQ.getContentType());
				case 7:
					return new cfStringData(REQ.getProtocol());
				case 8:
					return new cfStringData(REQ.getPathInfo());
				case 9:
					
					String value = REQ.getRequestURI();
					if ( bScriptProtect & value != null )
						value = ScriptProtect.sanitize( value );
						
					return new cfStringData(value);

				case 10:
					String pathInfo = REQ.getPathInfo();
					if ((pathInfo == null) || (pathInfo.length() == 0)) {
						return new cfStringData(getCanonicalPath(REQ, REQ.getServletPath())); // equivalent to CF_TEMPLATE_PATH
					} else {
						return new cfStringData(getCanonicalPath(REQ, pathInfo));
					}

				case 11:

					String v = cfUrlData.getQueryString(_session);
					if ( bScriptProtect && v != null )
						v = ScriptProtect.sanitize( v );
					
					return new cfStringData(v);

				case 12:
					return new cfStringData(REQ.getAuthType());
				case 13:
					return new cfStringData(REQ.getMethod());
				case 14:
					return new cfStringData(REQ.getRemoteUser());
				case 15:
					return new cfStringData(REQ.getContextPath() + REQ.getServletPath());
				case 16:
					return new cfStringData(REQ.getContextPath());
				case 17:
					return new cfStringData(REQ.isSecure() ? "on" : "off");
				case 18:
					return new cfStringData(getCanonicalPath(REQ, REQ.getServletPath()));
				case 19:
					return new cfStringData(REQ.isSecure() ? "1" : "0");
				case 20: // user_agent
					_key = "http_user_agent";
					// fall through
				default:
					break;
			}
		}

		String oldHdr = customHeaderMapping.get(_key);
		if (oldHdr != null) {	
			String v = REQ.getHeader(oldHdr);
			if ( bScriptProtect && v != null )
				v = ScriptProtect.sanitize( v );
			
			return new cfStringData(v);
		}

		cfData data = super.getData(_key);
		if (data != null) {
			return data;
		}

		String hdr = REQ.getHeader(_key);
		if (hdr != null) {
			String v = hdr;
			if ( bScriptProtect )
				v = ScriptProtect.sanitize( v );

			
			return new cfStringData(v);
		}

		return cfStringData.EMPTY_STRING;
	}

	protected cfData getForDump(String _key) {
		return getData(_key);
	}
}
