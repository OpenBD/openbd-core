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
 *  $Id: cfDataSource.java 2379 2013-06-14 23:37:53Z alan $
 *  http://openbd.org/
 */

package com.naryx.tagfusion.cfm.sql;

/** 
 * This class handles the ColdFusion DATASOURCE used for accessing database resources.
 */

import java.sql.Connection;
import java.sql.SQLException;

import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfDataSource extends Object implements java.io.Serializable {

	private static final long serialVersionUID = 1;
	
	protected cfDataSourceDetails	dsDetails;
	protected cfSession 			Session;
	protected String				username, password;
	
	// WARNING! don't reference key directly, but only via the getKey() method
	protected String key;

	protected cfDataSource() {}
	
	public cfDataSource( String _DataSource ) throws cfmRunTimeException {
		this( _DataSource, null );
	}
	
	public cfDataSource( String _DataSource, cfSession _Session ) throws cfmRunTimeException {
		Session 	= _Session;
		dsDetails = cfDataSourceDetailsFactory.get( _DataSource );
		username 	= dsDetails.getUsername();
		password 	= dsDetails.getPassword();
	}
	

	
	/**
	 * Code that can run inside a CFTRANSACTION must call this method 
	 * instead of getConnection().  When the code is done with the
	 * connection, it must call returnConnection().
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Connection takeConnection() throws SQLException {
		// Check to see if we are running inside a CFTRANSACTION
		cfTransactionCache tCache = (cfTransactionCache)Session.getDataBin( cfTRANSACTION.DATA_BIN_KEY );
		if ( tCache == null ) {
			Connection con = getPooledConnection();
			try {
				con.setAutoCommit( true );
			} catch ( SQLException ignore ) {} // this will be handled better later when we try to use the connection

			return con;
		} else {
			return tCache.pop( this );
		}
	}



	/**
	 * Code that can run inside a CFTRANSACTION must call this 
	 * method to release a connection when they are done with it.
	 * The code must not call connection.close() to release it.
	 * 
	 * @param Con
	 */
	public void returnConnection( Connection Con ) {
		// Check to see if we are running inside a CFTRANSACTION
		cfTransactionCache tCache = (cfTransactionCache)Session.getDataBin( cfTRANSACTION.DATA_BIN_KEY );
		if ( tCache == null ) {
			close( Con );
		} else {
			tCache.push( this, Con );
		}
	}
	
	
	
	/**
	 * close or reset the connection for reuse
	 * @param con
	 */
	public void close( Connection con ) {
		if ( con != null ) {
			try {
				if ( dsDetails.perRequestConnections || dsDetails.isUnlimitedPool() ) { // reset for reuse within this request
					con.clearWarnings();
					con.setAutoCommit( true );
				} else {
					con.close(); // put back into the connection pool
				}
			} catch ( SQLException ignore ) {}
		}
	}
	
	
	
	/**
	 * Connections can be saved in a per-request cache managed by the cfSession instance. This
	 * allows connections for the same datasource to be re-used within a single request.
	 * This is necessary to support MySQL temporary tables (see bug #2670). This can also
	 * provide a performance benefit since we don't have to go back to the connection pool
	 * every time if the same datasource is used multiple times within a single request (a
	 * common usage scenario).
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Connection getPooledConnection() throws SQLException {
		if ( dsDetails.perRequestConnections || dsDetails.isUnlimitedPool() ) {
			Connection con = Session.getConnection( getKey() ); 								// look for a connection in the per-request cache
			if ( con == null ) {
				con = cfEngine.dataSourcePoolFactory.getConnection(dsDetails); 		// get a connection from the connection pool
				Session.putConnection( getKey(), con ); 													// save the connection in the per-request cache
			}
			return con;
		}else
			return cfEngine.dataSourcePoolFactory.getConnection(dsDetails);
	}
	
	
	
	/**
	 * Get a connection from the connection pool.  This method must be called by code that cannot run inside a CFTRANSACTION.  
	 * The code must call connection.close() and not returnConnection() or this.close() to release the connection.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		return cfEngine.dataSourcePoolFactory.getConnection(dsDetails);
	}
	
	public String getKey() {
		if ( key == null ) {
			key = dsDetails.getDataSourceName() + "." + username + "." + password;
		}
		return key;
	}

  public String toString(){
  	return dsDetails.toString() + "\r\n" +
  			 	"Username            = " + username +"\r\n" +
 	  			"Password            = " + password;
 
  }
  
  public final String getUsername(){ return username; }
  public final String getPassword(){ return password; }
  
  public void setUsername( String _username ) {
	  if ( ( _username != null ) && ( _username.length() > 0 ) ) {
		  username = _username;
		  dsDetails.username = _username;
	  }
  }
  
  public void setPassword( String _password ) {
	  if ( ( _password != null ) && ( _password.length() > 0 ) ) {
		  password = _password;
		  dsDetails.password = _password;
	  }
  }
  
  public final String getCatalog(){ return dsDetails.getCatalog(); }
  public final String getDataSourceName(){ return dsDetails.getDataSourceName(); }
  
  public final boolean isSql_delete() { return dsDetails.sql_delete;  }
  public final boolean isSql_insert() {	return dsDetails.sql_insert;  }
  public final boolean isSql_select() {	return dsDetails.sql_select;  }
  public final boolean isSql_storedprocedures() {	return dsDetails.sql_storedprocedures;  }
  public final boolean isSql_update() {	return dsDetails.sql_update;  }
  
}
