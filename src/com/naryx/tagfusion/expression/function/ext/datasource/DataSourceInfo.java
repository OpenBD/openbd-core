/* 
 *  Copyright (C) 2000 - 2009 TagServlet Ltd
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

package com.naryx.tagfusion.expression.function.ext.datasource;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.sql.cfDataSourceDetails;
import com.naryx.tagfusion.cfm.sql.cfDataSourceDetailsFactory;
import com.naryx.tagfusion.expression.function.functionBase;


/*
 * Checks to see if a DataSource is valid
 */

public class DataSourceInfo extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public DataSourceInfo() {
		min = max = 1;
		setNamedParams( new String[]{ "datasource" } );
	}

	public String[] getParamInfo(){
		return new String[]{
				"datasource - database datasource you wish to inspect"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"query", 
				"Returns back a structure with all the details of the datasource.  Please note the password is not returned. Keys=[hoststring, drivername, databasename, username, password, logintimeout, connectiontimeout, maxconnections, maxlivetime, maxusage, connectionretries, initstring]", 
				ReturnType.STRUCTURE );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String datasource = getNamedStringParam( argStruct, "datasource", null );
		
		cfDataSourceDetails ds = cfDataSourceDetailsFactory.get(datasource);
		
		cfStructData s	= new cfStructData();
		
		s.setData( "hoststring", new cfStringData( ds.getHoststring() ) );
		s.setData( "drivername", new cfStringData( ds.getDrivername() ) );
		s.setData( "databasename", new cfStringData( ds.getCatalog() ) );
		s.setData( "username", new cfStringData( ds.getUsername() ) );
		
		s.setData( "logintimeout", new cfNumberData( ds.getLogintimeout() ) );
		s.setData( "connectiontimeout", new cfNumberData( ds.getConnectiontimeout() ) );
		s.setData( "maxconnections", new cfNumberData( ds.getLimitconnections() ) );
		s.setData( "maxusage", new cfNumberData( ds.getMaxUsage() ) );
		s.setData( "maxlivetime", new cfNumberData( ds.getMaxLiveTime() ) );
		s.setData( "connectionretries", new cfNumberData( ds.getConnectionRetries()) );
		
		s.setData( "initstring", new cfStringData( ds.getInitString() ) );
		
		return s;
	}
}