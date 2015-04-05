/* 
 *  Copyright (C) 2000 - 2012 aw2.0 Ltd
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
 *  $Id: JDBCUrlParser.java 2327 2013-02-10 22:26:44Z alan $
 */
package com.naryx.tagfusion.cfm.sql.pool.longterm;

import java.sql.SQLException;

public class JDBCUrlParser extends Object {

	private String url;
	private String host;
	private int port;
	
	public JDBCUrlParser( String _url ) throws Exception {
		
		// Check to see if we are disabling the check
		if ( _url.indexOf("openbdnocheck") != -1 )
			throw new Exception("network/port verification disabled");
		
		this.url = _url;
		
		int c1 = url.indexOf("://");
		if ( c1 == -1 )
			throw new SQLException("invalid url");
		
		int c2 = url.indexOf("/", c1 + 4) ;
		if ( c2 == -1 ){
			// could be a sql server jdbc url
			c2 = url.indexOf(";", c1 + 4);
			if ( c2 == -1 ){
				throw new Exception("invalid url");
			}
		}
			
		host	= url.substring( c1+3, c2 );
		
		//It may have multiple hosts in the string in which case give up validating and leave it to the driverManager
		if (host.indexOf(",")!=-1)
			throw new Exception("load balanced hosts defined give up validating");
		
		c1 = host.indexOf(":");
		if ( c1 != -1 ){
			port 	= toInteger( host.substring( c1 + 1 ), -1 );
			host	= host.substring( 0, c1 );
		}else{
			
			if ( _url.indexOf("sqlserver") != -1 )
				port = 1433;
			else if ( _url.indexOf("oracle") != -1 )
				port = 1521;
			else
				port = 3306;
		}
	}
	
	
	private int toInteger(String str, int defaultI) {
		try	{
			return Integer.parseInt( str );
		}catch(Exception e){}
		return defaultI;
	}

	public String getJdbcUrl() throws SQLException {
		return url;
	}

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}
}