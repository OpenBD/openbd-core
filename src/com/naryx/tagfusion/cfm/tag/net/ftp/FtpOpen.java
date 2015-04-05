/* 
 *  Copyright (C) 2012 TagServlet Ltd
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
 *  $Id: FtpOpen.java 2275 2012-09-04 00:20:26Z alan $
 */
package com.naryx.tagfusion.cfm.tag.net.ftp;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class FtpOpen extends functionBase {
	private static final long serialVersionUID = 1L;

	public FtpOpen() {
		min = 1; max = 7;
		setNamedParams( new String[]{ "server", "port", "username", "password", "passive", "timeout", "stoponerror" } );
	}
	

	public String[] getParamInfo(){
		return new String[]{
			"the ftp server to connect to",
			"port of the server to connect to. defaults to 21",
			"username to login to the ftp",
			"password of user account to the ftp server",
			"sets whether this is passive or not. defaults to false",
			"sets the timeout in seconds to use when reading from the data connection. default 30seconds",
			"if something goes wrong, throw an exception. defaults to true"
		};
	}
	
	
	public java.util.Map getInfo(){
		return makeInfo(
				"ftp", 
				"Creates a new connection to the FTP server detailed.  The object passed back is then used to interact with the server through the other FTP functions", 
				ReturnType.OBJECT );
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		String server	= getNamedStringParam(argStruct, "server", null );
		if ( server == null )
			throwException(_session, "missing 'server' attribute");
		
		String username	= getNamedStringParam(argStruct, "username", null );
		String password	= getNamedStringParam(argStruct, "password", null );
		int port				= getNamedIntParam(argStruct, "port", 21 );
		int timeout			= getNamedIntParam(argStruct, "timeout", 30 );
		boolean soe			= getNamedBooleanParam(argStruct, "stoponerror", true);
		boolean passive = getNamedBooleanParam(argStruct, "passive", false);
		
		cfFTPData	ftpdata	= new cfFTPData(server, port, username, password);
		ftpdata.open();
		ftpdata.setTimeout(timeout * 1000);
		ftpdata.setPassive(passive);
		
		if ( soe && !ftpdata.isSucceeded() )
			throwException( _session, ftpdata.getErrorText() );
		
		return ftpdata;
	}

}