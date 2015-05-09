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

/**
 * Class represents the URL scope.
 */
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import com.nary.util.FastMap;
import com.nary.util.SequencedHashMap;
import com.naryx.tagfusion.util.RequestUtil;

public class cfUrlData extends cfStructData implements Serializable {

	static final long serialVersionUID = 1;

	protected String encoding;

	public cfUrlData() {
		super(new SequencedHashMap( cfEngine.isFormUrlCaseMaintained() ? FastMap.CASE_SENSITIVE :FastMap.CASE_INSENSITIVE ) );
	}

	public cfUrlData(cfSession _session) {
		super(new SequencedHashMap( cfEngine.isFormUrlCaseMaintained() ? FastMap.CASE_SENSITIVE :FastMap.CASE_INSENSITIVE ));
		try {
			init(_session);
		} catch (UnsupportedEncodingException e) {
			cfEngine.log(e.getMessage()); // what should we do here?
		}
	}

	// create a shallow copy
	protected Map cloneHashdata() {
		return new SequencedHashMap(getHashData(), cfEngine.isFormUrlCaseMaintained() ? FastMap.CASE_SENSITIVE :FastMap.CASE_INSENSITIVE );
	}

	public boolean isCaseSensitive() {
		return false;
	}

	public void overrideData(String key, cfStringData value) {
		super.setData(key, value);
	}
	
	public void setData(String key, cfData value) {

		// -- This method is overriden to look for the __cfform__xxx variables from a <CFFORM>/<CFTREE> tag
		if (key.startsWith("__cfform__")) {
			String formField = key.substring(10);
			String formData = "";
			try {
				formData = value.getString();
			} catch (Exception ignoreException) {
			}

			if (formData.indexOf(";") == -1) {
				super.setData(formField, new cfStringData(formData));
			} else {
				try {
					String path = formData.substring(formData.indexOf("=") + 1, formData.indexOf(";NODE=")).trim();
					String node = formData.substring(formData.indexOf(";NODE=") + 6).trim();

					cfStructData s = new cfStructData();
					s.setData("path", new cfStringData(path));
					s.setData("node", new cfStringData(node));
					super.setData(formField, s);

				} catch (Exception E) {
					// -- An error occurred with the parse; so just maintain the original field name
					super.setData(formField, new cfStringData(formData));
				}
			}
		}

		// Convert FORM/URL keys to uppercase to match CFMX. This is important
		// because applications (such as FuseTalk) might use the keys as JavaScript
		// variables, which are case-sensitive.

		if ( cfEngine.isFormUrlCaseMaintained() )
			super.setData(key, value);
		else
			super.setData(key.toUpperCase(), value);
	}

	private void init(cfSession _session) throws UnsupportedEncodingException {
		String enc = _session.REQ.getCharacterEncoding();
		if (enc == null)
			enc = cfEngine.getDefaultEncoding();

		init(_session, enc);

	}

	private void init(cfSession _session, String _enc) throws UnsupportedEncodingException {
		encoding = _enc;
		decodeURIString(_session);
	}

	public static String getQueryString(cfSession _session) {
		/*
		 * The following code handles this logic:
		 * 
		 * If this is a direct request then just use the query string from the request object.
		 * 
		 * If this is an include request then use the query string from the "javax.servlet.include.query_string" 
		 * attribute. If it isn't present then use the query string from the request object. This causes a query 
		 * string in an include statement to override the original query string.
		 * 
		 * If this is a forward request then use the query string from the request object. If it isn't present 
		 * then use the query string from the "javax.servlet.forward.query_string" attribute. This causes a 
		 * query string in a forward statement to override the original query string.
		 * 
		 * NOTE: This matches the behavior of CF8 and fixes bug #3238. It also backs out the fix for #2768 which wasn't correct.
		 * 
		 * Other items/areas of interest: - these methods from SE's Request object [getQueryString(), 
		 * addQueryParameters(), removeQueryParameters()] - The last 2 are used by SE's RequestDispatcher 
		 * when forwarding a request so as to leave the original request intact while adding any extra Parameters 
		 * to the request. - See section 2.13.6 of the Servlet 2.2 Specification. In the forwarded request, the new
		 * parameters have precedence over the old ones. - cfcServlet.doGet()
		 */

		String qs_include = (String) _session.REQ.getAttribute("javax.servlet.include.query_string");
		String qs_request = _session.REQ.getQueryString(); // on Jetty this may be <forwarded params>&<original request params>
		String qs_forward = (String) _session.REQ.getAttribute("javax.servlet.forward.query_string");

		String qs = null;
		String appEngineName = _session.CTX.getServerInfo().toLowerCase();

		// Jetty fixup:
		if ((appEngineName.startsWith("jetty") || appEngineName.startsWith("google")) && qs_forward != null && qs_request != null) {
			/*
			 * Jetty Notes: request /Bug02840/testForward.cfm?var01=a&var02=b which forwards to index.cfm?var01=a&var02=c qs 
			 * should be just "var01=a&var02=b" _session.REQ.qetQueryString should be just "var01=a&var02=c"
			 * 
			 * Reality: Jetty 6.0.0 ----------- qs=var01=a&var02=c&var01=a&var02=b _session.REQ.getQueryString()=var01=a&var02=c&var01=a&var02=b
			 * 
			 * Jetty 6.1.10 ------------ qs=var01=a&var02=b _session.REQ.getQueryString()=var01=a&var02=c&var01=a&var02=b
			 * 
			 * Summary: Jetty concatenates the 2 query strings for us (even though we wish it would not since Tomcat 
			 * and SE do not). Jetty appends the original request params to the forwarded ones and returns the result 
			 * on calls to _session.REQ.getQueryString() So now that we no longer want them concatenated (after fixing 
			 * bug #3238) we must extract what we need by removing the erroneously appended
			 * params. (Matt M.)
			 */
			int pos = qs_request.indexOf('&' + qs_forward);
			if (pos > 0)
				qs = qs_request.substring(0, pos); // remove unwanted params

			// last ditch
			if (qs == null)
				qs = qs_request;
		} else {
			qs = qs_include;
			if (qs == null)
				qs = qs_request;
			if (qs == null)
				qs = qs_forward;
		}

		return qs;
	}

	/**
	 * This method has been pulled out to allow super classes the ability to call it. When the scopes ('form' and 'url') 
	 * are combined the cfFormData calls this method to decode any variables that may have been passed in to the page via the URI.
	 * 
	 * @param _session
	 */
	protected void decodeURIString(cfSession _session) throws UnsupportedEncodingException {
		String qs = getQueryString(_session);
		FastMap urlParms = new FastMap();
		RequestUtil.parseParameters(urlParms, qs, encoding);

		Iterator iter = urlParms.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			String[] valArray = (String[]) urlParms.get(key);
			String value = valArray[0];

			// create a comma-separated list for multiple values
			if (valArray.length > 1) {
				StringBuffer valBuffer = new StringBuffer();

				for (int i = 0; i < valArray.length; i++) {
					if (valArray[i].length() > 0) {
						valBuffer.append(valArray[i]);
						valBuffer.append(',');
					}
				}

				if (valBuffer.length() > 0) {
					// remove trailing ','
					value = valBuffer.toString().substring(0, valBuffer.length() - 1);
				}
			}

			this.setData(key, new cfStringData(value));
		}
	}

	public void setEncoding(cfSession _session, String _encoding) throws UnsupportedEncodingException {
		clear();
		init(_session, _encoding);
	}

	public String getEncoding() {
		return encoding;
	}
}
