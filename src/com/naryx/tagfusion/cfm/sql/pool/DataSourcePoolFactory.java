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
 *  $Id: DataSourcePoolFactory.java 2327 2013-02-10 22:26:44Z alan $
 */
package com.naryx.tagfusion.cfm.sql.pool;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;

import javax.sql.DataSource;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.sql.cfDataSourceDetails;
import com.naryx.tagfusion.cfm.sql.pool.longterm.LongTermPoolFactory;
import com.naryx.tagfusion.cfm.sql.pool.shortterm.ShortTermPoolFactory;

public class DataSourcePoolFactory {

	private HashSet<String> 				registeredDrivers;
	private LongTermPoolFactory			longTermPoolFactory;
	private ShortTermPoolFactory		shortTermPoolFactory;
	
	public DataSourcePoolFactory(){
		registeredDrivers 		= new HashSet<String>();
		longTermPoolFactory		= new LongTermPoolFactory();
		shortTermPoolFactory	= new ShortTermPoolFactory();
		
		DriverManager.setLoginTimeout(30);
	}

	
	/**
	 * Returns the javax.sql.DataSource for the type that is defined
	 *  
	 * @param dataSourceDetails
	 * @return
	 */
	public Connection getConnection( cfDataSourceDetails dataSourceDetails ) throws SQLException {
		if ( dataSourceDetails.getJ2EEDataSource() != null ){
			DataSource ds = dataSourceDetails.getJ2EEDataSource();

			if ( dataSourceDetails.getUsername() != null && dataSourceDetails.getPassword() != null )
				return ds.getConnection(dataSourceDetails.getUsername(), dataSourceDetails.getPassword());
			else
				return ds.getConnection();

		}else if ( !dataSourceDetails.isUnlimitedPool() )
			return longTermPoolFactory.getConnection( dataSourceDetails );
		else
			return shortTermPoolFactory.getConnection( dataSourceDetails );
	}

	
	
	/**
	 * Deletes the datasource from the underlying factories
	 * 
	 * @param dsName
	 */
	public void deleteDataSource(String dsName){
		longTermPoolFactory.deleteDataSource(dsName);
		shortTermPoolFactory.deleteDataSource(dsName);
	}
	
	
	/**
	 * Close down all the pools; the engine is shutting down when this is being called
	 */
	public void close() {
		longTermPoolFactory.close();
		shortTermPoolFactory.close();
	}
	

	
	/**
	 * Registers the given driver, throwing an SQLException if it couldn't be loaded.
	 * This method ensures that a Driver is only registered with the underlying
	 * DriverManager regardless of how many times this is called with the same driver name
	 * 
	 * @param _driver
	 * @throws SQLException
	 */
	public void registerDriver( String _driver ) throws SQLException {
		if ( !registeredDrivers.contains( _driver ) ){
			synchronized( registeredDrivers ){
				if ( !registeredDrivers.contains( _driver ) ){
					try{
						DriverManager.registerDriver( (Driver)Class.forName( _driver ).newInstance() );
						registeredDrivers.add( _driver );
					}catch(Exception E){
						throw new SQLException( "The JDBC class, " + _driver + ", could not be loaded" );  
					}
				}
			}
		}
	}


	/**
	 * Returns the active pool stats
	 * 
	 * @return
	 * @throws cfmRunTimeException 
	 */
	public cfArrayData getPoolStats() throws cfmRunTimeException {
		return longTermPoolFactory.getPoolStats();
	}
}