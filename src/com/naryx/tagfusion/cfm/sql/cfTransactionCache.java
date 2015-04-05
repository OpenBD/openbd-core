/* 
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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
 */

package com.naryx.tagfusion.cfm.sql;

import java.sql.Connection;
import java.sql.SQLException;

public class cfTransactionCache extends Object {

	private cfDataSource 	currentDataSource;
	private Connection		currentConnection;
	private int				isolationLevel = Connection.TRANSACTION_NONE;
	
	public cfTransactionCache(){
		currentDataSource = null;
		currentConnection	= null;
	}

	public cfTransactionCache( int il )
	{
		currentDataSource = null;
		currentConnection	= null;
		isolationLevel = il;
	}
	
	public Connection pop( cfDataSource dataSource ) throws SQLException {
		if ( currentDataSource == null ){
			currentConnection = dataSource.getPooledConnection();
			currentDataSource = dataSource;
			
			try{ currentConnection.setAutoCommit( false ); }catch(SQLException ignoreException){}

			if ( isolationLevel != Connection.TRANSACTION_NONE )
				currentConnection.setTransactionIsolation( isolationLevel );
					
			return currentConnection;
		} else if ( dataSource.getKey().equals( currentDataSource.getKey() ) ) {
			return currentConnection;
		} else {
			throw new SQLException( "You must COMMIT/ROLLBACK before using another datasource ( ds1=" + dataSource.getKey() + ", ds2=" + currentDataSource.getKey() + " )" );
		}
	}
	
	public void push( cfDataSource dataSource, Connection Con ) {
		if ( currentDataSource == null ) {
			try {
				Con.close();
			} catch ( SQLException ignore ) {
			}
		}
	}
	
	public void commit(){
		if ( currentConnection != null ){
			try{ currentConnection.commit(); }catch(SQLException ignoreException){}
			close();
		}
	}
	
	public void rollback(){
		if ( currentConnection != null ){
			try{ currentConnection.rollback(); }catch(SQLException ignoreException){}
			close();
		}
	}
	
	public void close(){
		if ( currentDataSource != null ) {
			currentDataSource.close( currentConnection );
		} else if ( currentConnection != null ) {
			try {
				currentConnection.close();
			} catch ( SQLException ignore ) {
			}
		}
			
		currentDataSource = null;
		currentConnection = null;
	}
}
