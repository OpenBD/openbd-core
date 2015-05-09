/*
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: cfCookieData.java 2497 2015-02-02 01:53:48Z alan $
 */

package com.naryx.tagfusion.cfm.cookie;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.Cookie;

import com.nary.net.http.urlEncoder;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfCookieData extends cfStructData implements java.io.Serializable {
	static final long serialVersionUID = 1;

	private transient cfSession session;
	
	private static SimpleDateFormat simpledateformat;
	static{
		simpledateformat = new SimpleDateFormat("EEE, dd-MMM-yy HH:mm:ss", Locale.UK);
		simpledateformat.setTimeZone(TimeZone.getTimeZone("GMT"));
	};

	public cfCookieData(cfSession _parent) {
		super();
		session = _parent;
		
		//-- Build up the list of cookies if there are any
		Cookie[] cList = session.REQ.getCookies();
		if (cList != null && cList.length > 0) {
			for (int x = 0; x < cList.length; x++)
				super.setData(cList[x].getName(), new cookieWrapper(cList[x], false, false));
		}
	}


	/**
	 * this function is called by the cfHttpServletResposne to add the cookies to the 
	 * outgoing stream.  Once written, any further cookies can't be added.	
	 */
	public void addCookies() {
		Map hashdata = getHashData();
		synchronized (hashdata) {
			Iterator values = hashdata.values().iterator();
			while (values.hasNext()) {
				cookieWrapper cW = (cookieWrapper) values.next();
				if (cW.bOutgoing){
					session.RES.addHeader("Set-Cookie", cW.getHeaderValue() );
				}
					
			}
		}
	}

	public void deleteCookie(String _key) throws cfmRunTimeException {
		super.deleteData(_key);
	}

	public void setDomainCookie(cfSession _Session, String _key) {
		cookieWrapper cW = (cookieWrapper) super.getData(_key);
		if (cW != null && cW.bOutgoing)
			cW.cookie.setDomain(transferTopLevelDomain(_Session, cW.cookie.getDomain()));
	}

	
	/*
	 * clear
	 * 
	 * This method clears the cookie structure by setting all of the cookie
	 * value's to an empty string and setting all of them to expire now.
	 */
	public void clear() {
		Map hashdata = getHashData();
		synchronized (hashdata) {
			Iterator values = hashdata.values().iterator();
			while (values.hasNext()) {
				cookieWrapper cW = (cookieWrapper) values.next();
				cW.cookie.setValue("");
				cW.cookie.setMaxAge(0);
				cW.bOutgoing = true;
			}
		}
	}

	/*
	 * deleteData
	 * 
	 * This method deletes a cookie by setting its value to an empty string and
	 * setting it to expire now.
	 */
	public void deleteData(String _key) throws cfmRunTimeException {
		cookieWrapper cW = (cookieWrapper) super.getData(_key);
		if (cW != null) {
			cW.cookie.setValue("");
			cW.cookie.setMaxAge(0);
			cW.bOutgoing = true;
		}
	}

	public void setData(String _key, cfData _data) {
		// this method overrides the setData method in cfStructData and
		// therefore can't throw an exception
		try {
			setData(new cfStringData(_key), _data);
		} catch (cfmRunTimeException exc) {
			cfEngine.log("ERROR: failed to set data in cookie structure.");
		}
	}

	public void setData(cfData _key, cfData _data) throws cfmRunTimeException {
		String cookieVal = urlEncoder.encode(_data.getString());
		Cookie newCookie = new Cookie(_key.getString().toUpperCase(), cookieVal);
		newCookie.setPath(session.REQ.getContextPath() + "/");
		setData( newCookie, false );
	}

	public void setData(Cookie _cdata, boolean bHttpOnly ) throws cfmRunTimeException {
		if (session.isFlushed()) {
			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_TEMPLATE);
			catchData.setDetail("Unable to create Cookie because the page has been flushed");
			catchData.setMessage("Cannot create Cookie");
			throw new cfmRunTimeException(catchData);
		}
		super.setData(_cdata.getName(), new cookieWrapper(_cdata, true, bHttpOnly));
	}

	public String getTypeString() {
		return "cookie";
	}

	@SuppressWarnings("deprecation")
	public cfData getData(String _key) {
		cookieWrapper cW = (cookieWrapper) super.getData(_key);
		return (cW == null) ? null : new cfStringData(java.net.URLDecoder.decode(cW.cookie.getValue()));
	}

	protected cfData getForDump(String _key) {
		return getData(_key);
	}

	// used by Script Protect functionality
	public void overrideData(String _key, String _data) {
		cookieWrapper cW = (cookieWrapper) super.getData(_key);
		if (cW != null) {
			cW.cookie.setValue(_data);
		}
	}

	private static String transferTopLevelDomain(cfSession _Session, String _domain) {
		if (_domain == null)
			_domain = _Session.REQ.getServerName().toLowerCase();

		int dotCount = 3;
		if (_domain.indexOf(".com") != -1 || _domain.indexOf(".net") != -1 || _domain.indexOf(".org") != -1)
			dotCount = 2;

		try {
			int c1 = _domain.length();
			while (dotCount > 0) {
				c1 = _domain.lastIndexOf(".", c1 - 1);
				--dotCount;
			}

			_domain = _domain.substring(c1);
		} catch (Exception E) {
		}
		return _domain;
	}

	// ------------------------------------------------------------------
	// --[ Inner class to wrap up the Cookie object in a cfData

	class cookieWrapper extends cfData implements java.io.Serializable {
		private static final long serialVersionUID = 1L;
		
		
		
		Cookie cookie;
		boolean bOutgoing = false;
		boolean bHttpOnly = false;
		
		public cookieWrapper(Cookie _cookie, boolean _bOutgoing, boolean _bHttpOnly ) {
			cookie 		= _cookie;
			bOutgoing = _bOutgoing;
			bHttpOnly = _bHttpOnly;
		}
		
		public String getHeaderValue(){
			StringBuilder sb = new StringBuilder( 64 );
			
			sb.append( cookie.getName() );
			sb.append( "=" );
			sb.append( cookie.getValue() );
			
			if ( cookie.getMaxAge() >= 0 ){
				if ( cookie.getMaxAge() == 0 ){
					sb.append( "; expires=Thu, 01 Jan 1970 00:00:01 GMT" );
				}else{
					sb.append( "; expires=" + simpledateformat.format( System.currentTimeMillis()  + (long)((long)cookie.getMaxAge() * 1000l) ) + " GMT" );
				}
			}
			
			if ( cookie.getDomain() != null )
				sb.append( "; domain=" + cookie.getDomain() );
			
			if ( cookie.getPath() != null )
				sb.append( "; path=" + cookie.getPath() );
			
			if ( cookie.getSecure() )
				sb.append( "; secure" );
			
			if ( bHttpOnly )
				sb.append( "; HttpOnly" );
			
			return sb.toString();
		}
	}
}
