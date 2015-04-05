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
 *  http://www.openbluedragon.org/
 *  
 *  $Id: GetHttpStatusLabel.java 1660 2011-09-09 09:41:02Z alan $
 */

package com.naryx.tagfusion.expression.function;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class GetHttpStatusLabel extends functionBase {

	private static final long serialVersionUID = 1;

	public GetHttpStatusLabel() {
		min = max = 1;
	}

  public String[] getParamInfo(){
		return new String[]{
			"statuscode"
		};
	}

	public java.util.Map getInfo(){
		return makeInfo(
				"network",
				"For the given HTTP Status Code returns the standard status message",
				ReturnType.STRING );
	}

	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		return new cfStringData( getStatusLabel(parameters.get(0).getInt() ));
	}

	public static String getStatusLabel( int sc ){
		switch ( sc ){
			case 100:
				return "Continue";
			case 101:
				return "Switching Protocols";
			case 102:
				return "Processing";
			case 103:
				return "Checkpoint";
			case 122:
				return "Request-URI too long";
		
			case 200:
				return "OK";
			case 201:
				return "Created";
			case 202:
				return "Accepted";
			case 203:
				return "Non-Authoritative Information";
			case 204:
				return "No Content";
			case 205:
				return "Reset Content";
			case 206:
				return "Partial Content";
			case 207:
				return "Multi-Status";
			case 226:
				return "IM Used";
				
			case 300:
				return "Multiple Choices";
			case 301:
				return "Moved Permanently";
			case 302:
				return "Found";
			case 303:
				return "See Other";
			case 304:
				return "Not Modified";
			case 305:
				return "Use Proxy";
			case 306:
				return "Switch Proxy";
			case 307:
				return "Temporary Redirect";
			case 308:
				return "Resume Incomplete	";

			case 400:
				return "Bad Request";
			case 401:
				return "Unauthorized";
			case 402:
				return "Payment Required";
			case 403:
				return "Forbidden";
			case 404:
				return "Not Found";
			case 405:
				return "Method Not Allowed";
			case 406:
				return "Not Acceptable";
			case 407:
				return "Proxy Authentication Required";
			case 408:
				return "Request Timeout";
			case 409:
				return "Conflict";
			case 410:
				return "Gone";
			case 411:
				return "Length Required";
			case 412:
				return "Precondition Failed";
			case 413:
				return "Request Entity Too Large";
			case 414:
				return "Request-URI Too Long";
			case 415:
				return "Unsupported Media Type";
			case 416:
				return "Requested Range Not Satisfiable";
			case 417:
				return "Expectation Failed";
			case 418:
				return "I'm a teapot";
			case 422:
				return "Unprocessable Entity";
			case 423:
				return "Locked";
			case 424:
				return "Failed Dependency";
			case 425:
				return "Unordered Collection";
			case 426:
				return "Upgrade Required";
			case 444:
				return "No Response";
			case 449:
				return "Retry With";
			case 450:
				return "Blocked by Windows Parental Controls";
			case 499:
				return "Client Closed Request";
			
			case 500:
				return "Internal Server Error";
			case 501:
				return "Not Implemented";
			case 502:
				return "Bad Gateway";
			case 503:
				return "Service Unavailable";
			case 504:
				return "Gateway Timeout";
			case 505:
				return "HTTP Version Not Supported";
			case 506:
				return "Variant Also Negotiates";
			case 507:
				return "Insufficient Storage";
			case 509:
				return "Bandwidth Limit Exceeded";
			case 510:
				return "Not Extended";
		}
		
		return "";
	}
	
}
