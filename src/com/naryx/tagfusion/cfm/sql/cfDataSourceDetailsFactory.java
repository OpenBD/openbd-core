/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * Every time a CFQUERY (or any related DB tag) is called it needs to use a DataSource that will
 * hold all the information regarding the underlying db connection.  This class holds a cached
 * version of these.
 */
public class cfDataSourceDetailsFactory {

	// since dataSourceList is not synchronized, make sure all references are explicitly wrapped in synchronization blocks
	private static FastMap<String,cfDataSourceDetails> dataSourceList = new FastMap<String,cfDataSourceDetails>( FastMap.CASE_INSENSITIVE );
	
	public static synchronized cfDataSourceDetails get(String _DataSource) throws cfmRunTimeException {
		// need to preserve case of _DataSource for potential JNDI lookups (#2102, #2041)
		cfDataSourceDetails cDS	= (cfDataSourceDetails)dataSourceList.get(_DataSource);
		if ( cDS == null ){
			cDS	= new cfDataSourceDetails( _DataSource );
			dataSourceList.put( cDS.getKey(), cDS );
		}
		return cDS;
	}
	
	public static synchronized cfDataSourceDetails remove( String _DataSource ) {
		return (cfDataSourceDetails)dataSourceList.remove( _DataSource );
	}
	
	public static synchronized void clearAll(){
		dataSourceList.clear();
	}
	
	public static synchronized boolean isRegistered( String _DataSource ){
		if ( !dataSourceList.containsKey(_DataSource) ){

			try{
				new cfDataSourceDetails( _DataSource );
				return true;
			}catch(Exception somethingMeantWeCouldntCreateItFromXmlFileOrJ2EE){
				return false;
			}

		}else
			return true;
	}
	
	public static synchronized void add( String _DataSource, cfDataSourceDetails datasourcedetails ){
		dataSourceList.put( _DataSource, datasourcedetails );
	}
}