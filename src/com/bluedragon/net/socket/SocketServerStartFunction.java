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
 *  $Id: SocketServerStartFunction.java 1903 2011-12-30 20:46:20Z alan $
 */

package com.bluedragon.net.socket;

import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SocketServerStartFunction extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public SocketServerStartFunction() {
		min = 2;
		max = 3;
		setNamedParams( new String[]{ "port", "cfc", "ip" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"the port to listen for incoming requests on",
			"the cfc that will be used for the callback of new connections",
			"the IP interface to listen on, defaults to all interfaces on the machine"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"socket", 
				"Creates a listening server that clients can connect to and interact with.  CFC's that are created for each client, you will access to the current application scope inside the CFC", 
				ReturnType.BOOLEAN );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String ip	= getNamedStringParam(argStruct, "ip", null );
		
		int	port	= getNamedIntParam(argStruct, "port", -1 );
		if ( port < 0 )
			throwException(_session, "Port must be greater than 0");
		
		// Get the CFC
		String cfc	= getNamedStringParam(argStruct, "cfc", null );
		if ( cfc == null )
			throwException(_session, "Please specify a path to CFC");
		
		// Get the application name
		String appname = null;
		cfApplicationData appdata = _session.getApplicationData();
		if ( appdata != null )
			appname	= appdata.getAppName();

		try {
			return cfBooleanData.getcfBooleanData( SocketServerDataFactory.thisInst.addServer(ip, port, cfc, appname) );
		} catch (Exception e) {
			throwException(_session, e.getMessage() );
			return null;
		}
	}
}