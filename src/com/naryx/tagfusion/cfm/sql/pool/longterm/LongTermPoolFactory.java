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
 *  $Id: LongTermPoolFactory.java 2327 2013-02-10 22:26:44Z alan $
 */
package com.naryx.tagfusion.cfm.sql.pool.longterm;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.sql.cfDataSourceDetails;

public class LongTermPoolFactory {
	private FastMap<String, LongTermDataSourcePoolManager> poolCollectionMap;
	
	public LongTermPoolFactory(){
		poolCollectionMap = new FastMap<String, LongTermDataSourcePoolManager>();
	}

	
	/**
	 * Based on the cfDataSourceDetails return back the connection
	 * 
	 * @param dataSourceDetails
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection(cfDataSourceDetails dataSourceDetails) throws SQLException  {

		LongTermDataSourcePoolManager	poolManager;

		synchronized ( poolCollectionMap ){

			poolManager	= poolCollectionMap.get( dataSourceDetails.getKey() );
			if ( poolManager == null ){
				poolManager	= new LongTermDataSourcePoolManager( this, dataSourceDetails );
				poolCollectionMap.put( dataSourceDetails.getKey(), poolManager );
			}

		}
		
		return poolManager.getConnection();
	}

	
	public void removePoolFactory(String key){
		synchronized ( poolCollectionMap ){
			LongTermDataSourcePoolManager	poolManager = poolCollectionMap.remove(key);
			if ( poolManager != null )
				poolManager.unhookCallback();
				
			cfEngine.log( "LongTermPoolFactory.removePoolFactory( " + ( (key.indexOf("@")==-1) ? key : key.substring(0,key.indexOf("@")) ) + " )" );
		}
	}

	public void close() {

		synchronized (poolCollectionMap){
			Iterator<LongTermDataSourcePoolManager>	it	= poolCollectionMap.values().iterator();
			while ( it.hasNext() ){
				LongTermDataSourcePoolManager	poolManager	= it.next();
				poolManager.closeAll();
				it.remove();
			}

		}
		
	}
	


	/**
	 * Deletes the datasource's associated with this name; this will be all the datasources that start with this
	 * particular one; as we have the ones with username/passwords
	 * 
	 * @param dsName
	 */
	public void deleteDataSource(String dsName) {
	
		synchronized (poolCollectionMap){
			Iterator<String>	kit	= poolCollectionMap.keySet().iterator();
			while ( kit.hasNext() ){
				String thisDsName	= kit.next();

				if ( thisDsName.startsWith(dsName) ){
					LongTermDataSourcePoolManager	poolManager	= poolCollectionMap.get(thisDsName);
					poolManager.closeAll();
					kit.remove();
				}
				
			}
		}
		
	}


	/**
	 * Run through all the pool collection and get the stats
	 * 
	 * @return
	 * @throws cfmRunTimeException 
	 */
	public cfArrayData getPoolStats() throws cfmRunTimeException {
		cfArrayData array	= cfArrayData.createArray(1);
		
		synchronized (poolCollectionMap){
			Iterator<LongTermDataSourcePoolManager>	it	= poolCollectionMap.values().iterator();
			while ( it.hasNext() ){
				LongTermDataSourcePoolManager	poolManager	= it.next();
				array.addElement( poolManager.getPoolStats() );
			}
		}

		
		return array;
	}
	
}
