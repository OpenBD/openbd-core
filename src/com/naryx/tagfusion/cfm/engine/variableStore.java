/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: variableStore.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.engine;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import com.nary.security.SecurityCache;
import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.application.cfJ2EERequestStructData;
import com.naryx.tagfusion.cfm.cookie.cfCookieData;

public class variableStore implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	// make sure numerical order matches alphabetical order (for CFDUMP)
	public static final int APPLICATION_SCOPE = 0;
	// public static final int ARGUMENTS_SCOPE = 1;
	public static final int ATTRIBUTES_SCOPE = 2;
	public static final int CALLER_SCOPE = 3;
	public static final int CGI_SCOPE = 4;
	public static final int CLIENT_SCOPE = 5;
	public static final int COOKIE_SCOPE = 6;
	public static final int FILE_SCOPE = 7;
	public static final int FORM_SCOPE = 8;
	public static final int REQUEST_SCOPE = 9;
	public static final int SERVER_SCOPE = 10;
	public static final int SESSION_SCOPE = 11;
	public static final int SUPER_SCOPE = 12;
	public static final int URL_SCOPE = 13;
	public static final int VARIABLES_SCOPE = 14;
	public static final int CFTHREAD_SCOPE = 15;

	private static final int NUM_SCOPES = 16;

	// we still need the names to be public in a few cases
	private static final String APPLICATION_SCOPE_NAME = "application";
	// public static final String ARGUMENTS_SCOPE_NAME = "arguments";
	public static final String ATTRIBUTES_SCOPE_NAME = "attributes";
	public static final String CALLER_SCOPE_NAME = "caller";
	public static final String CGI_SCOPE_NAME = "cgi";
	private static final String CLIENT_SCOPE_NAME = "client";
	public static final String COOKIE_SCOPE_NAME = "cookie";
	public static final String HTTP_SCOPE_NAME = "http"; // an alias for CGI only
	private static final String FILE_SCOPE_NAME = "cffile";
	public static final String FORM_SCOPE_NAME = "form";
	private static final String REQUEST_SCOPE_NAME = "request";
	public static final String SERVER_SCOPE_NAME = "server";
	private static final String SESSION_SCOPE_NAME = "session";
	public static final String SUPER_SCOPE_NAME = "super";
	public static final String URL_SCOPE_NAME = "url";
	public static final String VARIABLES_SCOPE_NAME = "variables";
	public static final String CFTHREAD_SCOPE_NAME = "cfthread";

	// --[ Setup the server and security information, once.
	private static cfStructSelectiveReadOnlyData serverData;
	private static SecurityCache securityStore;

	// mapping from scope name to scope index, for indexing dataStore
	protected static final Map<String, Integer> scopeNameMap = new FastMap<String, Integer>(FastMap.CASE_INSENSITIVE);

	// mapping from scope index to scope name
	private static final String[] scopeNames = new String[NUM_SCOPES];

	public static String getScopeName(int i) {
		return scopeNames[validateScopeIndex(i)];
	}

	private static int validateScopeIndex(int i) throws IllegalArgumentException {
		if ((i < 0) || (i >= NUM_SCOPES)) {
			throw new IllegalArgumentException(Integer.toString(i));
		}
		return i;
	}

	static {
		try {
			scopeNameMap.put(APPLICATION_SCOPE_NAME, new Integer(APPLICATION_SCOPE));
			// scopeNameMap.put( ARGUMENTS_SCOPE_NAME, new Integer( ARGUMENTS_SCOPE )
			// );
			scopeNameMap.put(ATTRIBUTES_SCOPE_NAME, new Integer(ATTRIBUTES_SCOPE));
			scopeNameMap.put(CALLER_SCOPE_NAME, new Integer(CALLER_SCOPE));
			scopeNameMap.put(CGI_SCOPE_NAME, new Integer(CGI_SCOPE));
			scopeNameMap.put(HTTP_SCOPE_NAME, new Integer(CGI_SCOPE));
			scopeNameMap.put(CLIENT_SCOPE_NAME, new Integer(CLIENT_SCOPE));
			scopeNameMap.put(COOKIE_SCOPE_NAME, new Integer(COOKIE_SCOPE));
			scopeNameMap.put(FILE_SCOPE_NAME, new Integer(FILE_SCOPE));
			scopeNameMap.put(FORM_SCOPE_NAME, new Integer(FORM_SCOPE));
			scopeNameMap.put(REQUEST_SCOPE_NAME, new Integer(REQUEST_SCOPE));
			scopeNameMap.put(SERVER_SCOPE_NAME, new Integer(SERVER_SCOPE));
			scopeNameMap.put(SESSION_SCOPE_NAME, new Integer(SESSION_SCOPE));
			scopeNameMap.put(SUPER_SCOPE_NAME, new Integer(SUPER_SCOPE));
			scopeNameMap.put(URL_SCOPE_NAME, new Integer(URL_SCOPE));
			scopeNameMap.put(VARIABLES_SCOPE_NAME, new Integer(VARIABLES_SCOPE));
			scopeNameMap.put(CFTHREAD_SCOPE_NAME, new Integer(CFTHREAD_SCOPE));

			scopeNames[APPLICATION_SCOPE] = APPLICATION_SCOPE_NAME;
			// scopeNames[ ARGUMENTS_SCOPE ] = ARGUMENTS_SCOPE_NAME;
			scopeNames[ATTRIBUTES_SCOPE] = ATTRIBUTES_SCOPE_NAME;
			scopeNames[CALLER_SCOPE] = CALLER_SCOPE_NAME;
			scopeNames[CGI_SCOPE] = CGI_SCOPE_NAME;
			scopeNames[CLIENT_SCOPE] = CLIENT_SCOPE_NAME;
			scopeNames[COOKIE_SCOPE] = COOKIE_SCOPE_NAME;
			scopeNames[FILE_SCOPE] = FILE_SCOPE_NAME;
			scopeNames[FORM_SCOPE] = FORM_SCOPE_NAME;
			scopeNames[REQUEST_SCOPE] = REQUEST_SCOPE_NAME;
			scopeNames[SERVER_SCOPE] = SERVER_SCOPE_NAME;
			scopeNames[SESSION_SCOPE] = SESSION_SCOPE_NAME;
			scopeNames[SUPER_SCOPE] = SUPER_SCOPE_NAME;
			scopeNames[URL_SCOPE] = URL_SCOPE_NAME;
			scopeNames[VARIABLES_SCOPE] = VARIABLES_SCOPE_NAME;
			scopeNames[CFTHREAD_SCOPE] = CFTHREAD_SCOPE_NAME;

			Map<String, cfData> serverReadOnlyData = new FastMap<String, cfData>(FastMap.CASE_INSENSITIVE);
			serverReadOnlyData.put("bluedragon", cfStringData.EMPTY_STRING);
			serverReadOnlyData.put("coldfusion", cfStringData.EMPTY_STRING);
			serverReadOnlyData.put("os", cfStringData.EMPTY_STRING);
			serverData = new cfStructSelectiveReadOnlyData(serverReadOnlyData);

			// Set the server.bluedragon values
			cfStructReadOnlyData bluedragon = new cfStructReadOnlyData();
			bluedragon.setPrivateData("edition", 			new cfNumberData(8) ); // EDITION_J2EE
			bluedragon.setPrivateData("builddate", 		new cfStringData(cfEngine.BUILD_ISSUE));
			bluedragon.setPrivateData("level", 				new cfStringData("GPL"));
			bluedragon.setPrivateData("state", 				new cfStringData( cfEngine.PRODUCT_STATE ));
			bluedragon.setPrivateData("releasedate", 	new cfStringData( cfEngine.PRODUCT_RELEASEDATE ) );
			bluedragon.setPrivateData("version", 			new cfStringData(cfEngine.PRODUCT_VERSION));
			serverData.setPrivateData("bluedragon", bluedragon);

			// Build the supportedlocales value
			StringBuilder str = new StringBuilder();
			Locale[] locales = Locale.getAvailableLocales();
			for (int i = 0; i < locales.length; i++) {
				String displayName = locales[i].getDisplayName(Locale.US);
				if ((displayName != null) && (displayName.indexOf(",") == -1)) {
					String country = locales[i].getCountry();

					if ((country == null) || (country.length() == 0)) {
						// skip language-only
						continue;
					}

					str.append(displayName);
					str.append(", ");
				}
			}

			// [- add the Cf5.0 Strings
			str.append("Dutch (Belgian),French (Canadian),Norwegian (Bokmal),Dutch (Standard),French (Standard),Norwegian (nynorsk),English (Australian),French (Swiss),Portuguese (Brazilian),English (Canadian),German (Austrian),Portuguese (Standard),English (New Zealand),German (Standard),Spanish (Mexican),English (UK),German (Swiss),Spanish (Modern),English (US),Italian (Standard),Spanish (Standard),French (Belgian),Italian (Swiss),Swedish");

			for (int i = 0; i < locales.length; i++) {
				str.append(", ");
				str.append(locales[i].getLanguage());
				if (locales[i].getCountry().length() > 0) {
					str.append("_");
					str.append(locales[i].getCountry());
				}
			}

			// Set the server.coldfusion values
			try {
				Map<String, cfData> coldfusionReadOnlyData = new FastMap<String, cfData>(FastMap.CASE_INSENSITIVE);
				coldfusionReadOnlyData.put("supportedlocales", cfStringData.EMPTY_STRING);
				coldfusionReadOnlyData.put("productlevel", cfStringData.EMPTY_STRING);
				coldfusionReadOnlyData.put("productname", cfStringData.EMPTY_STRING);
				coldfusionReadOnlyData.put("productversion", cfStringData.EMPTY_STRING);
				coldfusionReadOnlyData.put("appserver", cfStringData.EMPTY_STRING);
				coldfusionReadOnlyData.put("installkit", cfStringData.EMPTY_STRING);
				coldfusionReadOnlyData.put("rootdir", cfStringData.EMPTY_STRING);

				cfStructSelectiveReadOnlyData coldfusion = new cfStructSelectiveReadOnlyData(coldfusionReadOnlyData);
				coldfusion.setPrivateData("supportedlocales", new cfStringData(str.toString()));
				coldfusion.setPrivateData("productlevel", 		new cfStringData("GPL"));
				coldfusion.setPrivateData("productname", 			new cfStringData(cfEngine.PRODUCT_NAME));
				coldfusion.setPrivateData("productversion", 	new cfStringData(cfEngine.PRODUCT_VERSION.replace('.', ',')));
				coldfusion.setPrivateData("appserver", 				new cfStringData(cfEngine.thisServletContext.getServerInfo()));
				coldfusion.setPrivateData("installkit", 			cfStringData.EMPTY_STRING);
				coldfusion.setPrivateData("rootdir", 					new cfStringData(cfEngine.thisServletContext.getRealPath("/")));
				serverData.setPrivateData("coldfusion", 			coldfusion);
			} catch (Throwable t) {
				cfEngine.log("-] ERROR: caught exception while setting the server.coldfusion values - " + t.toString());
			}

			// Set the server.os values
			try {
				Map<String, cfData> osReadOnlyData = new FastMap<String, cfData>(FastMap.CASE_INSENSITIVE);
				osReadOnlyData.put("name", cfStringData.EMPTY_STRING);
				osReadOnlyData.put("version", cfStringData.EMPTY_STRING);
				osReadOnlyData.put("arch", cfStringData.EMPTY_STRING);
				osReadOnlyData.put("buildnumber", cfStringData.EMPTY_STRING);
				osReadOnlyData.put("additionalinformation", cfStringData.EMPTY_STRING);
				cfStructSelectiveReadOnlyData os = new cfStructSelectiveReadOnlyData(osReadOnlyData);
				os.setPrivateData("name", new cfStringData(System.getProperty("os.name")));
				os.setPrivateData("version", new cfStringData(System.getProperty("os.version")));
				os.setPrivateData("arch", new cfStringData(System.getProperty("os.arch")));
				os.setPrivateData("additionalinformation", new cfStringData(System.getProperty("java.vendor") + " " + System.getProperty("java.version")));
				serverData.setPrivateData("os", os);
			} catch (Throwable t) {
				cfEngine.log("ERROR: caught exception while setting the server.os values - " + t.toString());
			}

			
			Properties securityScopeProps = new Properties();
			securityScopeProps.put(com.nary.security.SecurityCache.STORAGE_TYPE_KEY, "file");
			securityScopeProps.put(com.nary.security.SecurityCache.STORAGE_LOCATION_KEY, cfEngine.thisPlatform.getFileIO().getWorkingDirectory().getAbsolutePath() + java.io.File.separatorChar + "cfloginuser" + java.io.File.separatorChar + com.nary.security.SecurityCache.STORAGE_FILENAME);
			securityStore = new SecurityCache(1, securityScopeProps);
			
		} catch (Throwable t) {
			cfEngine.log("ERROR: caught exception in variableStore static initializer - " + t.toString());
			com.nary.Debug.printStackTrace(t);
		}
	}

	protected cfStructData[] dataStore;
	protected Stack<cfQueryResultData> queryStack;

	public variableStore() {
		dataStore = new cfStructData[NUM_SCOPES];
		dataStore[VARIABLES_SCOPE] = new cfStructData();
	}

	public Map<String, cfStructData> getDataStore() {
		Map<String, cfStructData> dataStoreMap = new FastMap<String, cfStructData>();
		for (int i = 0; i < NUM_SCOPES; i++) {
			if (dataStore[i] != null)
				dataStoreMap.put(getScopeName(i), dataStore[i]);
		}
		return dataStoreMap;
	}

	public variableStore(cfSession _session) {
		this();

		// --[ Setup the input data
		if (cfEngine.isFormUrlScopeCombined()) { // this is the old way - combined Form/URL scope
			cfFormData FD = new cfFormData(_session, true);
			setQualifiedData(FORM_SCOPE, FD);
			setQualifiedData(URL_SCOPE, FD);
		} else { // this is the new way - separate Form and URL scopes
			setQualifiedData(FORM_SCOPE, new cfFormData(_session));
			setQualifiedData(URL_SCOPE, new cfUrlData(_session));
		}

		// --[ Setup the CGI data
		setQualifiedData(CGI_SCOPE, new cfCGIData(_session));

		// --[ Setup the CFFILE data
		setQualifiedData(FILE_SCOPE, new cfStructData());

		// --[ Setup the CFTHREAD data
		setQualifiedData(CFTHREAD_SCOPE, new cfStructData());

		// --[ Setup the Cookie information
		setQualifiedData(COOKIE_SCOPE, new cfCookieData(_session));

		// --[ Setup the Server information
		setQualifiedData(SERVER_SCOPE, serverData);

		// --[ Setup the request scope
		cfStructData request = new cfJ2EERequestStructData(_session);
		setQualifiedData(REQUEST_SCOPE, request);

		// --[ add disabled scopes
		setQualifiedData(SESSION_SCOPE, disabledSessionScope );
		setQualifiedData(CLIENT_SCOPE, disabledClientScope );

		// --[ Don't need the query stack just yet
		queryStack = null;
	}

	private static cfStructData	disabledSessionScope	= new cfDisabledStructData(SESSION_SCOPE_NAME);
	private static cfStructData	disabledClientScope		= new cfDisabledStructData(CLIENT_SCOPE_NAME);
	
	/**
	 * ATTENTION! Calls to setQualifiedData(), getQualifiedData(), and
	 * deleteQualifiedData() must always be made using the constants defined at
	 * the top of this class as keys.
	 */
	public void setQualifiedData(int _key, cfStructData _cfData) {
		dataStore[validateScopeIndex(_key)] = _cfData;
	}

	public cfStructData getQualifiedData(int _key) {
		return dataStore[validateScopeIndex(_key)];
	}

	public cfStructData deleteQualifiedData(int _key) {
		cfStructData temp = dataStore[validateScopeIndex(_key)];
		dataStore[_key] = null;
		return temp;
	}

	public cfData getData(String _key) {
		return getData(_key, true, true);
	}

	/**
	 * WARNING! The performance of this method is critical to overall system
	 * performance. Do not make any changes to this method without doing
	 * before-and-after timing measurements to make sure you have not decreased
	 * performance.
	 */
	public cfData getData(String _key, boolean _doQuerySearch, boolean _doVarSearch) {
		if (_key == null)
			return null;

		cfData tData = null;

		// --[ Search the first Query Object
		if (_doQuerySearch && (queryStack != null) && !queryStack.empty()) {
			tData = peekQuery().getData(_key);
			if (tData != null)
				return tData;
		}

		// --[ The data may have been fully addressed, so lets check it first
		Integer scopeIndex = scopeNameMap.get(_key);
		if (scopeIndex != null) {
			tData = dataStore[scopeIndex.intValue()];
			if (tData != null)
				return tData;
		}

		// --[ If its get this far, we need to go searching through the scopes to
		// --[ find the variable: variables, cgi, cffile, url, form, cookie, client

		// --[ Search the [variables] scope
		if (_doVarSearch) {
			tData = dataStore[VARIABLES_SCOPE].getData(_key);
			if (tData != null)
				return tData;
		}

		// --[ Search the [thread] scope
		tData = dataStore[CFTHREAD_SCOPE].getData(_key);
		if (tData != null)
			return tData;

		// --[ Search the [cgi] scope
		tData = dataStore[CGI_SCOPE].getData(_key);
		// checks if the value is 'really' there
		if (tData != null && tData != cfStringData.EMPTY_STRING)
			return tData;

		// --[ Search the [cffile] scope if there
		tData = dataStore[FILE_SCOPE].getData(_key);
		if (tData != null)
			return tData;

		// --[ Search the [url] scope if there
		tData = dataStore[URL_SCOPE].getData(_key);
		if (tData != null)
			return tData;

		// --[ Search the [form] scope if there
		tData = dataStore[FORM_SCOPE].getData(_key);
		if (tData != null)
			return tData;

		// --[ Search the [cookie] scope if there
		tData = dataStore[COOKIE_SCOPE].getData(_key);
		if (tData != null)
			return tData;

		// --[ Search the [client] scope if there
		cfData scope = dataStore[CLIENT_SCOPE];
		if (scope != null) {
			tData = scope.getData(_key);
			if (tData != null)
				return tData;
		}

		// --[ If its got down as far as here, then it hasn't been found
		return null;
	}

	public void deleteData(String _key) throws cfmRunTimeException {
		String key, subkey;

		int c1 = getLastIndex(_key);
		if (c1 == -1) {
			subkey = _key;
			key = VARIABLES_SCOPE_NAME;
		} else {
			subkey = _key.substring(c1 + 1);
			key = _key.substring(0, c1);
		}

		cfData tData = getData(key);
		if (tData != null)
			tData.deleteData(subkey);
	}

	public void pushQuery(cfQueryResultData _query) {
		if (queryStack == null)
			queryStack = new Stack<cfQueryResultData>();

		queryStack.push(_query);
	}

	public cfQueryResultData popQuery() {
		if (queryStack == null || queryStack.empty())
			return null;

		return queryStack.pop();
	}

	public cfQueryResultData peekQuery() {
		if (queryStack == null || queryStack.empty())
			return null;

		return queryStack.peek();
	}

	public Stack<cfQueryResultData> removeQueryStack() {
		Stack<cfQueryResultData> temp = queryStack;
		queryStack = null;
		return temp;
	}

	public Stack<cfQueryResultData> getQueryStack() {
		return queryStack;
	}

	public void setQueryStack(Stack<cfQueryResultData> _qs) {
		queryStack = _qs;
	}

	private static int getLastIndex(String key) {
		if (key.charAt(key.length() - 1) == ']')
			return key.lastIndexOf("[");
		else
			return key.lastIndexOf(".");
	}

	public static SecurityCache getSecurityStore() {
		return securityStore;
	}

	public static void shutdown() {
		securityStore.shutdown();
	}
}
