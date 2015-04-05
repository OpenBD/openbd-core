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
 *  $Id: J2EEDataSourceFactory.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.sql.pool;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * Factory class for getting J2EE datasources.
 */
public class J2EEDataSourceFactory extends Object {
	private static Context ctx;

	static {
		try {
			ctx = new InitialContext();
		} catch (NamingException e) {
			cfEngine.log("InitialContext() failed: " + e);
		}
	}

	public static DataSource getJ2eeDataSource(String dsn) throws cfmRunTimeException {
		try {
			if (ctx == null) {
				ctx = new InitialContext();
			}

			try {
				return (DataSource) ctx.lookup("jdbc/" + dsn);
			} catch (NamingException e) {
				try {
					return (DataSource) ctx.lookup("java:comp/env/jdbc/" + dsn);
				} catch (NamingException ee) {
					return (DataSource) ctx.lookup(dsn);
				}
			}
		} catch (NamingException e) {
			throw new cfmRunTimeException(catchDataFactory.extendedException(cfCatchData.TYPE_DATABASE, "errorCode.sqlError", "sql.invalidDatasource", new String[] { dsn }, e.toString()));
		} catch (ClassCastException e) {
			throw new cfmRunTimeException(catchDataFactory.extendedException(cfCatchData.TYPE_DATABASE, "errorCode.sqlError", "sql.invalidDatasource", new String[] { dsn }, e.toString()));
		}
	}
}