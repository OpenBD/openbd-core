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
 *  $Id: WrappedConnection.java 2404 2013-09-22 21:51:40Z alan $
 */

package com.naryx.tagfusion.cfm.sql.pool;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;

import com.naryx.tagfusion.cfm.engine.cfEngine;

/**
 * This class is a proxy class to the underlying Connection class
 * that allows us to manage the pool and collate various stats on
 * this is being used.  All interfaces implemented up to 1.4, although
 * any calls to 1.4 specific methods will result in an SQLException
 */
public class WrappedConnection extends Object implements Connection, PooledConnection {

  private Connection              con;
  private ConnectionEventListener activeListener;
  private long                    startUsageTime, creationTime;
  private int                     statsTotalStatments, statsTotalPrepareds, statsTotalCallable, totalUsage;
  private String				  				validationQuery;
  private String				  				lastQuery;
  
  private static boolean validateConnections = true;
  
  public static void setValidateConnections( boolean validate ) {
	  validateConnections = validate;
  }
  
  public static boolean isValidateConnections() {
	  return validateConnections;
  }

  public WrappedConnection( Connection con ){
    this.con        	= con;
    activeListener  	= null;
    creationTime			= System.currentTimeMillis();
    validationQuery 	= null;
    
    if ( !validateConnections ) {
    	return;
    }
    
    try {
    	// create validation query
    	DatabaseMetaData dbmd = con.getMetaData();
    	String dbProductName = dbmd.getDatabaseProductName().toLowerCase();
    	
    	if ( ( dbProductName.indexOf( "microsoft" ) >= 0 ) ||
		     ( dbProductName.indexOf( "sql server" ) >= 0 ) || // this value is also returned by the BEA driver for sybase
		     ( dbProductName.indexOf( "access" ) >= 0 ) ||
		     ( dbProductName.indexOf( "adaptive server enterprise" ) >= 0 ) || // sybase using jConnect 5.5 driver
		     ( dbProductName.indexOf( "sybase" ) >= 0 ) ) { // this check may not be needed
				validationQuery = "select 1";
			} else if ( dbProductName.indexOf( "oracle" ) >= 0 ) {
				validationQuery = "select sysdate from dual";
			} else if ( ( dbProductName.indexOf( "mysql" ) >= 0 ) ||
						( dbProductName.indexOf( "postgres" ) >= 0 ) ) {
				validationQuery = "select now()";
			} else if ( dbProductName.indexOf( "informix" ) >= 0 ) {
				validationQuery = "select distinct current timestamp from informix.systables";
			} else if ( ( dbProductName.indexOf( "db2" ) >= 0 ) ||
						( dbProductName.indexOf( "ibm" ) >= 0 ) ) { // this check may not be needed
				validationQuery = "select distinct(current timestamp) from sysibm.systables";
			} else if ( dbProductName.indexOf( "pointbase" ) >= 0 ) {
				validationQuery = "select count(*) from systables";
			} else {
				cfEngine.log( "Can't validate connections for " + dbProductName );
			}
    } catch ( SQLException ignore ) {}
  }
  
  public String getLastQuery(){
  	return lastQuery;
  }
  
  public void setLastQuery( String query ){
	lastQuery = query;
  }

  public Connection getConnection() {
    return con;
  }

	public boolean isOpen() {
		try {
			if ( con.isClosed() )
				return false;
			
			if ( validationQuery != null ) { // if we can't validate, just return true
				Statement stmt = con.createStatement();
				lastQuery = "STATEMENT: " + validationQuery;
				stmt.execute( validationQuery );
				stmt.close();
				lastQuery = null;
			}
			return true;
		} catch ( SQLException e ) {
			try {
				con.close(); // connection is bad, so try to close it
			} catch ( SQLException ignore ) {}
			return false;
		}
	}
	
	public int	getTotalHits(){
		return totalUsage;
	}
	
	public long getAliveTime(){
		return System.currentTimeMillis() - creationTime;
	}

  public void close() {
		if ( activeListener != null )
			activeListener.connectionClosed( new ConnectionEvent(this) );
  }
  
	public ConnectionEventListener getConnectionEventListener(){
		return activeListener;
	}
   
  public void addConnectionEventListener(ConnectionEventListener listener){
  	if ( ( activeListener != null ) && ( activeListener != listener ) ) {
  		throw new IllegalArgumentException( "Only one listener allowed per nConnection" );
  	}
    activeListener  = listener;
    startUsageTime  = System.currentTimeMillis();
  }

  public void removeConnectionEventListener(ConnectionEventListener listener){
  	if ( activeListener != listener ) {
  		throw new IllegalArgumentException( "Invalid attempt to remove listener" );
  	}
    activeListener  = null;
    startUsageTime  = System.currentTimeMillis();
  }
  
  public long getUsageTime(){
    return System.currentTimeMillis() - startUsageTime;  
  }
  
  public void clearStats(){
		totalUsage += statsTotalStatments + statsTotalPrepareds + statsTotalCallable;
    statsTotalStatments = 0;
    statsTotalPrepareds = 0;
    statsTotalCallable = 0;
  }
  
  public int getTotalStatements(){ return statsTotalStatments; }
  public int getTotalPrepareds(){ return statsTotalPrepareds; }
  public int getTotalCallable(){ return statsTotalCallable; }
  
  //-------------------------------------
  //-- Methods from the Connection class
  public Statement createStatement() throws SQLException{
  	lastQuery = "STATEMENT: unknown sql";
    statsTotalStatments++;
    return con.createStatement();
  }
  
  public PreparedStatement prepareStatement(String sql) throws SQLException{
  	lastQuery = "PREPARE STATEMENT: " + sql;
    statsTotalPrepareds++;
    return con.prepareStatement( sql );
  }
  
  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys ) throws SQLException{
		lastQuery = "PREPARE STATEMENT: " + sql;
		statsTotalPrepareds++;
		return con.prepareStatement( sql, autoGeneratedKeys );
  }
  
  public PreparedStatement prepareStatement(String sql, int[] columnIndexes ) throws SQLException{
		throw new SQLException( "method not supported" );
  }
  
  public PreparedStatement prepareStatement(String sql, String[] columnNames ) throws SQLException{
		throw new SQLException( "method not supported" );
  }

  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability ) throws SQLException{
		throw new SQLException( "method not supported" );
  }
  
  public CallableStatement prepareCall(String sql) throws SQLException {
  	lastQuery = "PREPARE CALL: " + sql;
    statsTotalCallable++;
    return con.prepareCall( sql );  
  }
  
  public String nativeSQL(String sql) throws SQLException{
    return con.nativeSQL( sql );
  }
  
  public void setAutoCommit(boolean autoCommit) throws SQLException{
    con.setAutoCommit( autoCommit );
  }
  
  public boolean getAutoCommit()throws SQLException{
    return con.getAutoCommit();  
  }
  
  public void rollback() throws SQLException{
    con.rollback();
  }
  
  public void rollback(Savepoint savepoint) throws SQLException{
		throw new SQLException( "method not supported" );
	}
  
  public boolean isClosed() throws SQLException{
    return con.isClosed();
  }
  
  public void commit() throws SQLException{
    con.commit();
  }
  
  public DatabaseMetaData getMetaData() throws SQLException{
    return con.getMetaData();
  }
  
  public void setReadOnly(boolean readOnly) throws SQLException{
    con.setReadOnly( readOnly );  
  }
  
  public boolean isReadOnly()throws SQLException {
    return con.isReadOnly();  
  }
  
  public void setCatalog(String catalog) throws SQLException{
    con.setCatalog( catalog );  
  }
  
  public String getCatalog() throws SQLException{
    return con.getCatalog();
  }
  
  public void setTransactionIsolation(int level) throws SQLException{
    con.setTransactionIsolation( level );
  }
  
  public int getTransactionIsolation() throws SQLException{
    return con.getTransactionIsolation();
  }
  
  public SQLWarning getWarnings() throws SQLException{
    return con.getWarnings();  
  }
  
  public void clearWarnings() throws SQLException{
    con.clearWarnings();  
  }
  
  public Statement createStatement(int resultSetType,int resultSetConcurrency)throws SQLException{
  	throw new SQLException( "method not supported" );
  }
  
  public PreparedStatement prepareStatement(String sql,int resultSetType,int resultSetConcurrency) throws SQLException{
  	throw new SQLException( "method not supported" );
  }
  
  public CallableStatement prepareCall(String sql,int resultSetType,int resultSetConcurrency) throws SQLException{
  	throw new SQLException( "method not supported" );
  }
  
  public CallableStatement prepareCall(String sql,int resultSetType,int resultSetConcurrency, int resultSetHoldability) throws SQLException{
		throw new SQLException( "method not supported" );
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
	public Map getTypeMap() throws SQLException{
    throw new SQLException( "method not supported" );
  }
  
	@Override
  public void setTypeMap( Map<String, Class<?>> arg0 ) throws SQLException {
    throw new SQLException( "method not supported" );
  }
  
  public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		throw new SQLException( "method not supported" );
  }
  
  public void releaseSavepoint(Savepoint savepoint)	throws SQLException {
		throw new SQLException( "method not supported" );
  }
  
  public int getHoldability() throws SQLException{
		throw new SQLException( "method not supported" );
	}
  
  public void setHoldability(int holdability) throws SQLException{
		throw new SQLException( "method not supported" );
  }
  
  public Savepoint setSavepoint(String name) throws SQLException {
		throw new SQLException( "method not supported" );
  }
  
  public Savepoint setSavepoint() throws SQLException {
		throw new SQLException( "method not supported" );
  }

	@Override
  public Array createArrayOf( String arg0, Object[] arg1 ) throws SQLException {
		throw new SQLException( "method not supported" );
  }

	@Override
  public Blob createBlob() throws SQLException {
		throw new SQLException( "method not supported" );
  }

	@Override
  public Clob createClob() throws SQLException {
		throw new SQLException( "method not supported" );
  }

	@Override
  public NClob createNClob() throws SQLException {
		throw new SQLException( "method not supported" );
	}

	@Override
  public SQLXML createSQLXML() throws SQLException {
		throw new SQLException( "method not supported" );
	}

	@Override
  public Struct createStruct( String arg0, Object[] arg1 ) throws SQLException {
		throw new SQLException( "method not supported" );
  }

	@Override
  public Properties getClientInfo() throws SQLException {
		throw new SQLException( "method not supported" );
  }

	@Override
  public String getClientInfo( String arg0 ) throws SQLException {
		throw new SQLException( "method not supported" );
  }

	@Override
  public boolean isValid( int arg0 ) throws SQLException {
		throw new SQLException( "method not supported" );
  }

	@Override
  public void setClientInfo( Properties arg0 ) throws SQLClientInfoException {}

	@Override
  public boolean isWrapperFor( Class<?> iface ) throws SQLException {
	  return false;
  }

	@Override
  public <T> T unwrap( Class<T> iface ) throws SQLException {
		throw new SQLException( "method not supported" );
  }

	@Override
  public void addStatementEventListener( StatementEventListener arg0 ) {}

	@Override
  public void removeStatementEventListener( StatementEventListener arg0 ) {}

	@Override
	public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {
	}

	
	private boolean bForceClosed = false;
	
	public void setForceClose() {
		bForceClosed = true;
	}
	
	public boolean getForceClose(){
		return bForceClosed;
	}

	public void abort( Executor arg0 ) throws SQLException {
		
	}

	public int getNetworkTimeout() throws SQLException {
		return 0;
	}

	public String getSchema() throws SQLException {
		return null;
	}

	public void setNetworkTimeout( Executor arg0, int arg1 ) throws SQLException {
	}

	public void setSchema( String arg0 ) throws SQLException {

	}
}
