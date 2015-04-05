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
 *  $Id: LongTermDataSourcePoolManager.java 2327 2013-02-10 22:26:44Z alan $
 */

package com.naryx.tagfusion.cfm.sql.pool.longterm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;

import org.aw20.net.SocketUtil;
import org.aw20.util.SystemClock;
import org.aw20.util.SystemClockEvent;

import com.nary.util.FastMap;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.sql.cfDataSourceDetails;
import com.naryx.tagfusion.cfm.sql.pool.SQLFailedConnectionException;
import com.naryx.tagfusion.cfm.sql.pool.WrappedConnection;

/*
 * This class handles all the connections within a given datasource
 */
public class LongTermDataSourcePoolManager extends Object implements ConnectionEventListener, SystemClockEvent {
	public static final int	SOCKET_WAIT_TIME_MS = 5000;
	public static final int IDLE_TIMEOUT_MS			= 1000 * 60 * 2;
	
	public static final String USER_PROP_NAME 		= "user";
	public static final String PASSWORD_PROP_NAME = "password";

	private List<WrappedConnection> idleQueue, activeQueue;

	private int maxConnections, loginTimeoutSecs, usageTimeoutMs, maxUsage, maxLiveTimeMs, connectionRetries;

	private String datasourceName, jdbcUri, jdbcUsername, jdbcPassword, initString;

	private int statsNewCon = 0, statsWaits = 0, statsRequests = 0, statsError = 0, statsTimeouts = 0;
	private int statsTotalStatments = 0, statsTotalPrepareds = 0, statsTotalCallable = 0, statsIdleClose = 0, statsInterrupted = 0, statsClosed = 0;
	private long statsTotalUsage = 0, statsMaxUsage = 0, statsMinUsage = Long.MAX_VALUE;

	private AtomicInteger currentConnections;
	private LongTermPoolFactory longTermPoolFactory;
	private String dsnKey;
	private long timeCreated, timeLastUsed;
	
	public LongTermDataSourcePoolManager(LongTermPoolFactory longTermPoolFactory, cfDataSourceDetails dsDetails) {
		this.longTermPoolFactory	= longTermPoolFactory;
		this.dsnKey								= dsDetails.getKey();
		this.idleQueue 						= Collections.synchronizedList(new ArrayList<WrappedConnection>(maxConnections));
		this.activeQueue 					= Collections.synchronizedList(new ArrayList<WrappedConnection>(maxConnections));
		this.datasourceName 			= dsDetails.getDataSourceName();

		this.jdbcUri 						= dsDetails.getHoststring();
		this.jdbcUsername 			= dsDetails.getUsername();
		this.jdbcPassword 			= dsDetails.getPassword();
		this.loginTimeoutSecs 	= dsDetails.getLogintimeout();
		this.usageTimeoutMs 		= dsDetails.getConnectiontimeout() * 1000;
		this.maxLiveTimeMs 			= dsDetails.getMaxLiveTime() * 1000;

		this.maxConnections 		= dsDetails.getLimitconnections();
		this.maxUsage 					= dsDetails.getMaxUsage();
		this.initString 				= dsDetails.getInitString();
		this.connectionRetries 	= dsDetails.getConnectionRetries();

		currentConnections 			= new AtomicInteger();
		timeCreated							= System.currentTimeMillis();
		timeLastUsed						= System.currentTimeMillis();
		
		SystemClock.setListenerMinute(this);
	}


	public void unhookCallback() {
		longTermPoolFactory	= null;
	}


	public void clockEvent(int type) {
		checkActiveConnections();
		checkIdleConnections();
		
		if ( idleQueue.isEmpty() && activeQueue.isEmpty() ){
			SystemClock.removeListenerMinute(this);

			if ( longTermPoolFactory != null )
				longTermPoolFactory.removePoolFactory(dsnKey);
		}
	}

	
	public Connection getConnection() throws SQLException {
		statsRequests++;
		timeLastUsed	= System.currentTimeMillis();
		
		
		// maxConnections represents both idle and active connections added together
		if (maxConnections == 0) { 
			return createActiveConnection();
		}

		long startRequestTime = System.currentTimeMillis();

		while (true) {
			// Check to see if there are any idle Connections left in the pool
			WrappedConnection con = null;

			synchronized (idleQueue) {
				if (!idleQueue.isEmpty()) {
					con = idleQueue.remove(0);
				}
			}

			// Check to see if its still open
			if ((con != null) && con.isOpen()) {
				con.addConnectionEventListener(this);
				synchronized (activeQueue) {
					activeQueue.add(con); // Add it to the active queue
				}
				return con;
			}


			/*
			 * If we get here see if we can (or need to) create a new connection synchronize
			 * to ensure that we do not exceed the maximum number of connections
			 */
			synchronized (currentConnections) {
				if (currentConnections.get() < maxConnections) {
					return createActiveConnection();
				}
			}

			
			// Lets check to make sure they haven't been waiting too long
			long timeElapsed = System.currentTimeMillis() - startRequestTime;
			if (timeElapsed > (loginTimeoutSecs * 1000)) {
				statsTimeouts++;
				throw new SQLFailedConnectionException("Timed out waiting for idle connection (waited " + timeElapsed + " ms)");
			}


			// By the time it has reached here, then it has to wait for a connection
			try {
				statsWaits++;
				Thread.sleep(50);
			} catch (InterruptedException ignore) {
			}
		}
	}

	// ----------------------------------------------------------------

	private Connection createActiveConnection() throws SQLException {
		WrappedConnection con = createNewConnectionWithRetries();
		currentConnections.incrementAndGet();

		con.addConnectionEventListener(this);
		synchronized (activeQueue) {
			activeQueue.add(con);
		}

		// Check if the connection needs to be initialized with some SQL statements
		if ((initString != null) && (initString.length() > 0)) {
			try {
				Statement stmt = con.createStatement();
				stmt.execute(initString);
				stmt.close();
			} catch (SQLException sqle) {
				SQLException e = new SQLFailedConnectionException("A problem occurred with the initialization string configured for the datasource " + datasourceName + " [init string = " + initString + ", error message = " + sqle.getMessage() + "]", sqle.getSQLState(), sqle.getErrorCode());
				throw e;
			}
		}

		return con;
	}

	
	
	/**
	 * This is to close down a connection that has been taking too long
	 * 
	 * @param con
	 */
	private void forceCloseActiveConnection(WrappedConnection con) {
		// We need to remove it from the active queue
		if ( con.getForceClose() )
			return;

		con.setForceClose();
		synchronized (activeQueue) {
			activeQueue.remove(con);
		}
		currentConnections.decrementAndGet();
	}
	

	private void closeActiveConnection(WrappedConnection con) {
		con.removeConnectionEventListener(this);
		synchronized (activeQueue) {
			activeQueue.remove(con);
		}
		closeUnderlyingConnection(con);
	}

	public void connectionClosed(ConnectionEvent event) {
		// This is called when the connection is closed
		WrappedConnection con = (WrappedConnection) event.getSource();

		con.removeConnectionEventListener(this);
		if (!con.getForceClose() ){
			synchronized (activeQueue) {
				activeQueue.remove(con);
			}
		}
		
		// Pull back the stats on this connection
		statsTotalUsage += con.getUsageTime();
		if (con.getUsageTime() > statsMaxUsage)
			statsMaxUsage = con.getUsageTime();

		if (con.getUsageTime() < statsMinUsage)
			statsMinUsage = con.getUsageTime();

		statsTotalStatments += con.getTotalStatements();
		statsTotalPrepareds += con.getTotalPrepareds();
		statsTotalCallable 	+= con.getTotalCallable();
		con.clearStats();

		/*
		 * At this point the connection is in "limbo"... it's not in the activeQueue
		 * or the idleQueue, we need to figure out whether or not we're going to
		 * reuse it.
		 */
		try {
			con.clearWarnings();
		} catch (SQLException ignore) {}

		
		// Check to see if this connection should NOT be reused again
		if (con.getForceClose() || maxConnections == 0 || currentConnections.get() > maxConnections || con.getTotalHits() > maxUsage || con.getAliveTime() > maxLiveTimeMs) {
			closeUnderlyingConnection(con);
		} else{ // put it back into the pool for reuse
			try {
				con.setAutoCommit(true);
			} catch (SQLException ignore) {}
			
			synchronized (idleQueue) {
				idleQueue.add(con);
			}
		}
	}

	// ----------------------------------------------------------------

	public void connectionErrorOccurred(ConnectionEvent event) {
		// This is called when an error occurred with the connection
		statsError++;
		closeActiveConnection((WrappedConnection) event.getSource());
	}

	// ----------------------------------------------

	private WrappedConnection createNewConnectionWithRetries() throws SQLException {
		// connectionRetries indicates how many more attempts should be made
		// if the initial newConnection call fails. For example, if it is
		// set to 1 then up to 2 attempts will be made
		if ( connectionRetries > 0 ){
			for (int i = 0; i < connectionRetries; i++) {
				try {
					return createConnection();
				} catch (SQLException ignore) {}
			}
		}

		// Do the last attempt outside the try/catch block so that any
		// exception it throws will make its way back to the client.
		return createConnection();
	}

	
	protected WrappedConnection createConnection() throws SQLException {

    /* Let us check to see if the remote server is actually listening or not; we'll only test ones we can parse */
		JDBCUrlParser	jdbc;
		try{
			jdbc	= new JDBCUrlParser(jdbcUri);
		}catch(Exception e){
			jdbc = null;
		}
		
		if ( jdbc != null && !SocketUtil.isRemotePortAlive( jdbc.getHost(), jdbc.getPort(), SOCKET_WAIT_TIME_MS ) ){
	  	throw new SQLFailedConnectionException( "failed to verify remote server/socket @ " + jdbc.getHost() + ":" + jdbc.getPort() );
	  }


		WrappedConnection con = null;
		try {
			// need special handling for oracle jdbc connection
			if (jdbcUri.startsWith("jdbc:oracle:thin:"))
				con = newOracleConnection();
			else if (jdbcUsername.length() == 0)
				con = new WrappedConnection(DriverManager.getConnection(jdbcUri));
			else
				con = new WrappedConnection(DriverManager.getConnection(jdbcUri, jdbcUsername, jdbcPassword));

		} catch (UnsatisfiedLinkError e) { // can happen with Oracle OCI driver (and other type 2 drivers?)
			throw new SQLFailedConnectionException(e.getMessage());
		}

		// Only increment the statsNewCon counter after the connection has been successfully created
		statsNewCon++;
		return con;
	}

	/*
	 * called if an Oracle connection is required.
	 */
	private WrappedConnection newOracleConnection() throws SQLException {
		int connectStrIndex = jdbcUri.indexOf(';');
		String jdbcUriWithoutParams;

		java.util.Properties info = new java.util.Properties();
		if (jdbcUsername.length() != 0) {
			info.put(USER_PROP_NAME, jdbcUsername);
			info.put(PASSWORD_PROP_NAME, jdbcPassword);
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

	// ----------------------------------------------------------------

	private void checkActiveConnections() {
		synchronized (activeQueue) {
			// Need to start at the end of the list since we might remove some elements
			for (int i = activeQueue.size() - 1; i >= 0; i--) {
				WrappedConnection con = activeQueue.get(i);
				
				/*
				 * If the connection has been in use for too long, then remove it
				 * NOTE: this can happen with long running SQL statements and
				 * connections that have been leaked from the connection pool.
				 */
				long usageTime = con.getUsageTime();
				if (usageTime > usageTimeoutMs) {
					cfEngine.log("WARNING: removing connection that has been active too long! (dataSource=" + datasourceName + ", usageTime=" + usageTime + " ms). SQL=" + con.getLastQuery() );
					forceCloseActiveConnection(con);
				}
			}
		}
	}
	
	

	private void checkIdleConnections() {
		synchronized (idleQueue) {
			// Need to start at the end of the list since we might remove some elements
			for (int i = idleQueue.size() - 1; i >= 0; i--) {
				WrappedConnection con = idleQueue.get(i);

				// if been idle close it off
				if ( con.getUsageTime() > IDLE_TIMEOUT_MS ) {
					statsIdleClose++;
					idleQueue.remove(i);
					cfEngine.log("Closing idle connection (dataSource=" + datasourceName + ")" );
					closeUnderlyingConnection(con);
				}
			}
		}
	}

	// ----------------------------------------------------------------

	private void closeAllIdle() {
		synchronized (idleQueue) {
			// Need to start at the end of the list since we're removing elements
			for (int i = idleQueue.size() - 1; i >= 0; i--) {
				WrappedConnection con = idleQueue.remove(i);
				closeUnderlyingConnection(con);
			}
		}
	}

	private void closeAllActive() {
		synchronized (activeQueue) {
			// Need to start at the end of the list since we're removing elements
			for (int i = activeQueue.size() - 1; i >= 0; i--) {
				WrappedConnection con = activeQueue.remove(i);
				closeUnderlyingConnection(con);
			}
		}
	}

	private void closeUnderlyingConnection(WrappedConnection con) {
		statsClosed++;
		
		if ( !con.getForceClose() )
			currentConnections.decrementAndGet();

		try {
			con.getConnection().close();
		} catch (SQLException ignore) {}
	}

	public void closeAll() {
		closeAllIdle();
		closeAllActive();

		SystemClock.removeListenerMinute(this);
	}

	public boolean isEmpty() {
		return (idleQueue.size() + activeQueue.size() == 0) ? true : false;
	}

	public Map<String,Object> getStatistics() {
		Map<String,Object> stats = new FastMap<String,Object>();
		stats.put("name", new String(datasourceName));
		stats.put("id", new String(jdbcUri + "@" + jdbcUsername));
		stats.put("requests", new Integer(statsRequests));

		stats.put("connectionsinuse", new Integer(activeQueue.size()));
		stats.put("connectionsfree", new Integer(idleQueue.size()));
		stats.put("connectionsmax", new Integer(maxConnections));

		StringBuilder buffer = new StringBuilder(64);
		buffer.append("newconnections=");
		buffer.append(statsNewCon);

		buffer.append("; waitconnections=");
		buffer.append(statsWaits);

		buffer.append("; error=");
		buffer.append(statsError);

		buffer.append("; statements=");
		buffer.append(statsTotalStatments);

		buffer.append("; preparedstatements=");
		buffer.append(statsTotalPrepareds);

		buffer.append("; callablestatements=");
		buffer.append(statsTotalCallable);

		buffer.append("; idleclose=");
		buffer.append(statsIdleClose);

		buffer.append("; closed=");
		buffer.append(statsClosed);

		buffer.append("; interrupted=");
		buffer.append(statsInterrupted);

		buffer.append("; timeouts=");
		buffer.append(statsTimeouts);

		buffer.append("; totalusagetime=");
		buffer.append(statsTotalUsage);

		buffer.append("; maxusagetime=");
		buffer.append(statsMaxUsage);

		buffer.append("; minusagetime=");
		buffer.append(statsMinUsage);

		stats.put("extra", buffer.toString());

		return stats;
	}


	public cfStructData getPoolStats() {
		cfStructData	s	= new cfStructData();
		
		s.put("dsnname", 	datasourceName);
		s.put("id", 			jdbcUri + "@" + jdbcUsername );
		s.put("requests", statsRequests);
		
		s.setData( "newconnections", statsNewCon );
		s.setData( "waitconnections", statsWaits );
		
		s.setData( "totalstatements", statsTotalStatments );
		s.setData( "totalpreparedstatements", statsTotalPrepareds );
		s.setData( "totalcallablestatements", statsTotalCallable );
		
		s.setData( "idleclose", statsIdleClose );
		s.setData( "closed", statsClosed );
		s.setData( "interrupted", statsInterrupted );
		s.setData( "timeouts", statsTimeouts );
		s.setData( "maxusagetime", statsMaxUsage );
		s.setData( "minusagetime", statsMinUsage );
		
		s.setData( "created", new cfDateData(timeCreated) );
		s.setData( "lastused", new cfDateData(timeLastUsed) );
		
		return s;
	}
}