/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  $Id: ShortTermPoolFactory.java 2327 2013-02-10 22:26:44Z alan $
 */
package com.naryx.tagfusion.cfm.sql.pool.shortterm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;

import org.aw20.net.SocketUtil;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.sql.cfDataSourceDetails;
import com.naryx.tagfusion.cfm.sql.pool.SQLFailedConnectionException;
import com.naryx.tagfusion.cfm.sql.pool.WrappedConnection;
import com.naryx.tagfusion.cfm.sql.pool.longterm.JDBCUrlParser;
import com.naryx.tagfusion.cfm.sql.pool.longterm.LongTermDataSourcePoolManager;

public class ShortTermPoolFactory extends Object {

	public void close() {}
	public void deleteDataSource(String dsName) {}

	
	public Connection getConnection(cfDataSourceDetails dsDetails) throws SQLException {
		
    /* Let us check to see if the remote server is actually listening or not; we'll only test ones we can parse */
		String jdbcUri	= dsDetails.getHoststring();
		JDBCUrlParser	jdbc;
		try{
			jdbc	= new JDBCUrlParser( jdbcUri );
		}catch(Exception e){
			jdbc = null;
		}
		
		if ( jdbc != null && !SocketUtil.isRemotePortAlive( jdbc.getHost(), jdbc.getPort(), LongTermDataSourcePoolManager.SOCKET_WAIT_TIME_MS ) ){
	  	throw new SQLFailedConnectionException( "failed to verify remote server/socket @ " + jdbc.getHost() + ":" + jdbc.getPort() );
	  }

		
		try {
			WrappedConnection con = null;
			
			// need special handling for oracle jdbc connection
			if (jdbcUri.startsWith("jdbc:oracle:thin:"))
				con = getOracleConnection(jdbcUri, dsDetails.getUsername(), dsDetails.getPassword() );
			else if ( dsDetails.getUsername() != null && dsDetails.getUsername().length() == 0)
				con = new WrappedConnection(DriverManager.getConnection(jdbcUri));
			else
				con = new WrappedConnection(DriverManager.getConnection(jdbcUri, dsDetails.getUsername(), dsDetails.getPassword()));

			
			// This one we want to close when the close event is called
			con.addConnectionEventListener( new ConnectionEventListener() {

				@Override
				public void connectionErrorOccurred(ConnectionEvent arg0) {}
				
				@Override
				public void connectionClosed(ConnectionEvent connectionEvent) {
					WrappedConnection con = (WrappedConnection)connectionEvent.getSource();
					try {
						con.getConnection().close();
					} catch (SQLException e) {
						cfEngine.log( "ShortTermPoolFactory.exception: " +  e.getMessage() );
					}
				}

			});
			
			return con;
			
		} catch (UnsatisfiedLinkError e) { // can happen with Oracle OCI driver (and other type 2 drivers?)
			throw new SQLFailedConnectionException(e.getMessage());
		}
	}

	

	private WrappedConnection getOracleConnection( String jdbcUri, String jdbcUsername, String jdbcPassword ) throws SQLException {
		int connectStrIndex = jdbcUri.indexOf(';');
		String jdbcUriWithoutParams;

		java.util.Properties info = new java.util.Properties();
		if ( jdbcUsername!= null && jdbcUsername.length() != 0 ) {
			info.put(LongTermDataSourcePoolManager.USER_PROP_NAME, jdbcUsername);
			info.put(LongTermDataSourcePoolManager.PASSWORD_PROP_NAME, jdbcPassword);
		}

		// if there's a connectstring present and it contains at least 1 param
		if (connectStrIndex != -1 && connectStrIndex != jdbcUri.length()) {
			jdbcUriWithoutParams = jdbcUri.substring(0, connectStrIndex);

			// break down the connect string into individual params
			String connectStr = jdbcUri.substring(connectStrIndex + 1);
			String[] params = string.convertToList(connectStr, ';');
			for (int i = 0; i < params.length; i++) {
				int splitIndex = params[i].indexOf('=');
				if (splitIndex != -1 && splitIndex != params.length) {
					info.put(params[i].substring(0, splitIndex), params[i].substring(splitIndex + 1));
				} else {
					info.put(params[i].substring(0, splitIndex), "");
				}
			}
		} else {
			jdbcUriWithoutParams = jdbcUri;
		}

		return new WrappedConnection(DriverManager.getConnection(jdbcUriWithoutParams, info));
	}
	
}
