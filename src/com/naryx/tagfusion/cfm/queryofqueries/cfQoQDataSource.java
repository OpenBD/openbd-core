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

package com.naryx.tagfusion.cfm.queryofqueries;

import java.sql.Connection;

import com.naryx.tagfusion.cfm.sql.cfDataSource;
import com.naryx.tagfusion.cfm.sql.cfDataSourceDetails;

public class cfQoQDataSource extends cfDataSource {

	private static final long serialVersionUID = 1L;
	
	private static final cfQoQDataSource defaultInstance = new cfQoQDataSource();
	
	public static cfQoQDataSource getDefault(){
		return defaultInstance;
	}
	
	private cfQoQDataSource(){
		this.username = "";
		this.password = "";
		this.dsDetails = new cfQoQDataSourceDetails();
	}
	
	public Connection getConnection() {
		return null;
	}

	public String getKey() {
		return "Query of Queries";
	}

	public void returnConnection( Connection Con ) {}

	public void setPassword( String _password ) {}

	public void setUsername( String _username ) {}

	public Connection takeConnection() {
		return null;
	}

	public String toString() {
		return "Query of Queries";
	}
	
	class cfQoQDataSourceDetails extends cfDataSourceDetails{
		
		cfQoQDataSourceDetails(){
			DataSourceName = "Query of Queries";
			drivername = "";
			catalog = "";
		}
	}

}
