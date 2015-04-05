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
 *  $Id: SocketConnectionFunction.java 1894 2011-12-28 19:20:01Z alan $
 */

package com.bluedragon.net.socket;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SocketConnectionFunction extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public SocketConnectionFunction() {
		min = 2;
		max = 3;
		setNamedParams( new String[]{ "ip", "port", "throwonerror" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"the remote server name, or IP address to make a connection to",
			"the port to make the the connection on",
			"flag to determine whether an exception is thrown on error, defaults to true"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"socket", 
				"Connects to a remote socket and returns back a SocketData class that lets you interact with the remote server", 
				ReturnType.STRUCTURE );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
	
		String ip	= getNamedStringParam(argStruct, "ip", null );
		if ( ip == null )
			throwException(_session, "Missing IP parameter");
		
		int	port	= getNamedIntParam(argStruct, "port", -1 );
		if ( port < 0 )
			throwException(_session, "Port must be greater than 0");
		
		boolean bThrowOnError = getNamedBooleanParam(argStruct, "throwonerror", true);
		
		try {
			return new SocketData( new Socket(ip, port), bThrowOnError );
		} catch (UnknownHostException e) {
			throwException(_session, "UnkownHostException: " + e.getMessage());
		} catch (IOException e) {
			throwException(_session, "IOException: " + e.getMessage());
		}
		return null;
	}
}