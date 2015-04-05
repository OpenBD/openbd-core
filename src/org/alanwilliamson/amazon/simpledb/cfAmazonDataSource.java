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

package org.alanwilliamson.amazon.simpledb;

import java.sql.Connection;

import com.naryx.tagfusion.cfm.sql.cfDataSource;
import com.naryx.tagfusion.cfm.sql.cfDataSourceDetails;

public class cfAmazonDataSource extends cfDataSource {
	private static final long serialVersionUID = 1L;

	private static final cfAmazonDataSource defaultInstance = new cfAmazonDataSource();

	public static cfAmazonDataSource getDefault() {
		return defaultInstance;
	}

	private cfAmazonDataSource() {
		this.username = "";
		this.password = "";
		this.dsDetails = new cfAmazonDataSourceDetails();
	}

	public Connection getConnection() {
		return null;
	}

	public String getKey() {
		return "AmazonSimpleDB";
	}

	public void returnConnection(Connection Con) {}
	public void setPassword(String _password) {}
	public void setUsername(String _username) {}
	public Connection takeConnection() {return null;}

	public String toString() {
		return "Amazon Simple DB";
	}

	class cfAmazonDataSourceDetails extends cfDataSourceDetails {
		cfAmazonDataSourceDetails() {
			DataSourceName = "AmazonSimpleDB";
			drivername = "";
			catalog = "";
		}
	}
}