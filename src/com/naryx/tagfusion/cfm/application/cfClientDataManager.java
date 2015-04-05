/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  $Id: $
 */

package com.naryx.tagfusion.cfm.application;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.aw20.util.SystemClockEvent;

import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.sql.cfDataSource;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

/**
 * Responsible for purging client data that is x days old from any datasources where this is enabled
 */
public class cfClientDataManager implements SystemClockEvent {

	private final long DEFAULT_CLIENT_EXPIRY = 1;

	private String SQL_SELECT;

	private String SQL_DELETE_GLOBAL;

	private String SQL_DELETE_APPDATA;

	private String DATA_TABLE_NAME;

	private String SQL_DELETE_ALL; // a multi statement string to DELETE all expired entries from both tables

	public cfClientDataManager() {
		String GLOBAL_TABLE_NAME;

		// Also, set up different table names for ColdFusion compatibility
		if (cfApplicationManager.cf5ClientData) {
			DATA_TABLE_NAME = "CDATA";
			GLOBAL_TABLE_NAME = "CGLOBAL";
		} else {
			DATA_TABLE_NAME = "BDDATA";
			GLOBAL_TABLE_NAME = "BDGLOBAL";
		}

		SQL_SELECT = "SELECT CFID FROM " + GLOBAL_TABLE_NAME + " WHERE LVISIT < ?";
		SQL_DELETE_GLOBAL = "DELETE FROM " + GLOBAL_TABLE_NAME + " WHERE LVISIT < ?";
		SQL_DELETE_APPDATA = "DELETE FROM " + DATA_TABLE_NAME + " WHERE CFID IN ( SELECT CFID FROM " + GLOBAL_TABLE_NAME + " WHERE LVISIT < ? )";
		SQL_DELETE_ALL = SQL_DELETE_APPDATA + ";" + SQL_DELETE_GLOBAL;

		cfEngine.thisPlatform.timerSetListenerHourly(this);
	}

	public void clockEvent(int type) {

		// loop thru the datasources
		xmlCFML config = cfEngine.getConfig();

		String clientstorage = config.getString("server.cfapplication.clientstorage", "cookie").toLowerCase();
		if (clientstorage == null || clientstorage.equals("cookie")) { // can't purge cookie based client data
			return;
		}

		// is purge enabled?
		try {
			// default this to false since if this data isn't present it's unlikely the clientexpiry is either
			boolean purgeEnabled = config.getBoolean("server.cfapplication.clientpurgeenabled", false);

			if (purgeEnabled) { // what's the expiry period
				long period = config.getLong("server.cfapplication.clientexpiry", DEFAULT_CLIENT_EXPIRY);
				period = period * 1000 * 60 * 60 * 24; // convert from no of days to milliseconds
				purgeDBData(clientstorage, period);
			}

		} catch (SQLException e) {
			cfEngine.log("Failed to delete client data from database: " + e.getMessage());
		} catch (cfmRunTimeException e) {
			cfEngine.log("Failed to delete client data: " + e.getMessage());
		}

	}

	private void purgeDBData(String _datasourcename, long _period) throws SQLException, cfmRunTimeException {
		java.sql.Date expiredDate = new java.sql.Date(System.currentTimeMillis() - _period);

		cfDataSource datasource = new cfDataSource(_datasourcename);

		Connection connection = null;
		try {
			// get cfid's where lvisit is l
			connection = datasource.getConnection();
			connection.setAutoCommit(false);

			try {
				// -- first try the most efficient 1 statement method (some db's don't support this)
				PreparedStatement delStmtGlob = connection.prepareStatement(SQL_DELETE_ALL);
				try {
					delStmtGlob.setDate(1, expiredDate);
					delStmtGlob.setDate(2, expiredDate);
				} catch (SQLException sqlExc) {
					// When setDate() is called the ODBC SQL Server Driver will throw an exception stating
					// "Optional feature not implemented". To work around this we catch the exception and
					// call setString instead.
					String expiredDateStr = expiredDate.toString();
					delStmtGlob.setString(1, expiredDateStr);
					delStmtGlob.setString(2, expiredDateStr);
				}
				delStmtGlob.execute();
				delStmtGlob.close();

				connection.commit();

			} catch (SQLException e) {
				try {
					// -- next try the 2 statement method

					PreparedStatement delStmtGlob = connection.prepareStatement(SQL_DELETE_APPDATA);
					delStmtGlob.setDate(1, expiredDate);
					delStmtGlob.execute();
					delStmtGlob.close();

					PreparedStatement delStmtApp = connection.prepareStatement(SQL_DELETE_GLOBAL);
					delStmtApp.setDate(1, expiredDate);
					delStmtApp.execute();
					delStmtApp.close();

					connection.commit();

				} catch (SQLException e2) {

					// -- the first 2 methods failed so try this method
					connection.setAutoCommit(true); // restore this for the SELECT

					List<String> cfids = getCFIDtoDelete(connection, expiredDate);

					if (cfids.size() > 0) {
						int cfidsCount = cfids.size();
						int batchSize = 100;
						connection.setAutoCommit(false);

						// -- delete in batches of 100
						for (int i = 0; i < cfidsCount; i += batchSize) {

							StringBuilder sb = new StringBuilder("DELETE FROM " + DATA_TABLE_NAME + " WHERE CFID IN (");
							int start = i;
							int end = i + batchSize;
							for (int j = start; j < end && j < cfidsCount; j++) {
								sb.append("'");
								sb.append(cfids.get(j));
								sb.append("',");
							}
							sb = sb.deleteCharAt(sb.length() - 1);
							sb.append(")");

							PreparedStatement delStmtApp = connection.prepareStatement(sb.toString());
							delStmtApp.execute();
							delStmtApp.close();
						}

						// -- DELETE BDGLOBAL entries
						PreparedStatement delStmtApp = connection.prepareStatement(SQL_DELETE_GLOBAL);
						delStmtApp.setDate(1, expiredDate);
						delStmtApp.execute();
						delStmtApp.close();

						connection.commit();
					}
				}
			}
		} finally {
			if (connection != null) {
				try {
					connection.setAutoCommit(true);
				} catch (SQLException ignore) {
				}
				connection.close();
			}
		}
	}

	private List<String> getCFIDtoDelete(Connection _connection, Date _expiredDate) throws SQLException {
		ResultSet result = null;
		try {
			List<String> cfids = new ArrayList<String>();
			PreparedStatement stmt = _connection.prepareStatement(SQL_SELECT);
			stmt.setDate(1, _expiredDate);
			result = stmt.executeQuery();

			while (result.next()) {
				cfids.add(result.getString(1));
			}
			stmt.close();

			return cfids;
		} finally {
			try {
				if (result != null)
					result.close();
			} catch (SQLException ignore1) {
			}
		}
	}
}
