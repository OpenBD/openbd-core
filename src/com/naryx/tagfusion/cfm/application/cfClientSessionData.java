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

package com.naryx.tagfusion.cfm.application;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.Cookie;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.cookie.cfCookieData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.sql.cfDataSource;

/**
 * This class enscapsulates all the [client] scope management
 * 
 * Note that the implementation of this has changed so this class is further
 * complicated by maintaining backwards compatibility for 6.1 client data
 * 
 * IMPORTANT! for backwards-compatibility of client data, we must use
 * com.nary.util.HashMap instead of FastMap.
 */

public class cfClientSessionData extends cfStructData implements Serializable {
	private static final long serialVersionUID = 1;

	private static final int DATABASE = 1;

	private static final int COOKIE = 2;

	private static final int INVALID = 3;

	// This is the SQL for the old client data table
	private static String SQL_SELECT = "SELECT CFDATA FROM BDCLIENTDATA WHERE CFID=?";

	private static String SQL_DELETE = "DELETE FROM BDCLIENTDATA WHERE CFID=?";

	private static String SQL_INSERT = "INSERT INTO BDCLIENTDATA (CFID,CFDATA) VALUES (?,?)";

	private static String SQL_UPDATE = "UPDATE BDCLIENTDATA SET CFDATA=? WHERE CFID=?";

	// This is the SQL for the new client data tables
	private static String SQL_SELECT_GLOBAL;

	private static String SQL_INSERT_GLOBAL;

	private static String SQL_UPDATE_GLOBAL;

	private static String[] SQL_CREATE_GLOBAL;

	private static String SQL_SELECT_DATA;

	private static String SQL_INSERT_DATA;

	private static String SQL_UPDATE_DATA;

	private static String[] SQL_CREATE_DATA;

	private static String DATA_TABLE_NAME;

	private static String GLOBAL_TABLE_NAME;

	private static String DATA_COLUMN_NAME;

	private static String GLOBAL_COOKIE_NAME;

	private static String CLIENT_COOKIE_NAME;

	// For fixed-width CHAR(n) columns we're going to use "LIKE" instead of "="
	// when doing
	// selects or inserts (or, eventually, deletes). This was discovered while
	// testing CF5
	// compatible client data on Oracle 8; to minimize the impact of these
	// changes, we're
	// only going to use LIKE when running in CF5 compatibility mode. We may
	// eventually want
	// to change this to always use LIKE.
	private static String COMPARISON_OP;

	private static final String LIKE = "LIKE";

	static {
		// Use different table names for ColdFusion compatibility. Use LIKE for CF5
		// compatibility.
		if (cfApplicationManager.cf5ClientData) {
			DATA_TABLE_NAME = "CDATA";
			GLOBAL_TABLE_NAME = "CGLOBAL";
			DATA_COLUMN_NAME = "DATA";
			GLOBAL_COOKIE_NAME = "CFGLOBALS";
			CLIENT_COOKIE_NAME = "CFCLIENT_";
			COMPARISON_OP = LIKE;
		} else {
			DATA_TABLE_NAME = "BDDATA";
			GLOBAL_TABLE_NAME = "BDGLOBAL";
			DATA_COLUMN_NAME = "CFDATA";
			GLOBAL_COOKIE_NAME = "bdglobals";
			CLIENT_COOKIE_NAME = "bdclient_";
			COMPARISON_OP = "=";
		}

		// Initialize the SQL statements needed for the global table
		SQL_SELECT_GLOBAL = "SELECT " + DATA_COLUMN_NAME + " FROM " + GLOBAL_TABLE_NAME + " WHERE CFID " + COMPARISON_OP + " ?";
		SQL_INSERT_GLOBAL = "INSERT INTO " + GLOBAL_TABLE_NAME + " (" + DATA_COLUMN_NAME + ",LVISIT,CFID) VALUES (?,?,?)";
		SQL_UPDATE_GLOBAL = "UPDATE " + GLOBAL_TABLE_NAME + " SET " + DATA_COLUMN_NAME + "=?, LVISIT=? WHERE CFID " + COMPARISON_OP + " ?";

		// don't create tables when in ColdFusion-compatible mode
		SQL_CREATE_GLOBAL = new String[] {
				// Microsoft Access (ASCII/UNICODE)
				"CREATE TABLE " + GLOBAL_TABLE_NAME + " ( CFID VARCHAR(48), CFDATA MEMO, LVISIT DATETIME, CONSTRAINT PK_BDGLOBAL PRIMARY KEY (CFID) )",
				// PointBase (NOTE: must come before Oracle entry otherwise it will be
				// used.)
				"CREATE TABLE " + GLOBAL_TABLE_NAME + " ( CFID VARCHAR(48) PRIMARY KEY, CFDATA CLOB(2G), LVISIT TIMESTAMP )",
				// Oracle (ASCII)
				"CREATE TABLE " + GLOBAL_TABLE_NAME + " ( CFID VARCHAR(48) PRIMARY KEY, CFDATA CLOB, LVISIT DATE )",
				// Microsoft SQL Server (ASCII), MySQL, Sybase
				"CREATE TABLE " + GLOBAL_TABLE_NAME + " ( CFID VARCHAR(48) PRIMARY KEY, CFDATA TEXT, LVISIT DATETIME )",
				// PostgreSQL
				"CREATE TABLE " + GLOBAL_TABLE_NAME + " ( CFID VARCHAR(48) PRIMARY KEY, CFDATA TEXT, LVISIT TIMESTAMP )",
				// DB2
				"CREATE TABLE " + GLOBAL_TABLE_NAME + " ( CFID VARCHAR(48) NOT NULL PRIMARY KEY, CFDATA LONG VARCHAR, LVISIT TIMESTAMP )",
				// Informix
				"CREATE TABLE " + GLOBAL_TABLE_NAME + " ( CFID VARCHAR(48) NOT NULL PRIMARY KEY, CFDATA LONG VARCHAR, LVISIT datetime year to fraction(5) )" };

		// Initialize the SQL statements needed for the data table
		SQL_SELECT_DATA = "SELECT " + DATA_COLUMN_NAME + " FROM " + DATA_TABLE_NAME + " WHERE CFID " + COMPARISON_OP + " ? AND APP " + COMPARISON_OP + " ?";
		SQL_INSERT_DATA = "INSERT INTO " + DATA_TABLE_NAME + " (" + DATA_COLUMN_NAME + ",CFID,APP) VALUES (?,?,?)";
		SQL_UPDATE_DATA = "UPDATE " + DATA_TABLE_NAME + " SET " + DATA_COLUMN_NAME + "=? WHERE CFID " + COMPARISON_OP + " ? AND APP " + COMPARISON_OP + " ?";

		// don't create tables when in ColdFusion-compatible mode
		SQL_CREATE_DATA = new String[] {
				// Microsoft Access (ASCII/UNICODE)
				"CREATE TABLE " + DATA_TABLE_NAME + " ( CFID VARCHAR(48), APP VARCHAR(64), CFDATA MEMO, CONSTRAINT PK_BDDATA PRIMARY KEY (CFID,APP) )",
				// PointBase (NOTE: must come before Oracle entry otherwise it will be
				// used.)
				"CREATE TABLE " + DATA_TABLE_NAME + " ( CFID VARCHAR(48), APP VARCHAR(64), CFDATA CLOB(2G), PRIMARY KEY (CFID,APP) )",
				// Oracle (ASCII)
				"CREATE TABLE " + DATA_TABLE_NAME + " ( CFID VARCHAR(48), APP VARCHAR(64), CFDATA CLOB, PRIMARY KEY (CFID,APP) )",
				// Microsoft SQL Server (ASCII), MySQL, PostgreSQL, Sybase
				"CREATE TABLE " + DATA_TABLE_NAME + " ( CFID VARCHAR(48) NOT NULL, APP VARCHAR(64) NOT NULL, CFDATA TEXT, PRIMARY KEY (CFID,APP) )",
				// DB2
				"CREATE TABLE " + DATA_TABLE_NAME + " ( CFID VARCHAR(48) NOT NULL, APP VARCHAR(64) NOT NULL, CFDATA LONG VARCHAR, PRIMARY KEY (CFID,APP) )" };
	}

	// ---------------------------------------------

	String appName;

	private String CFID, CFTOKEN;

	private int clientStorageType;

	private String dataSource;

	private cfDataSource sqldataSource;

	private boolean bLoadedFromTable = false;

	// these indicate whether the global and app-specific data has been loaded in
	// from the db
	private boolean loadedGlobalFromTable = false;

	private boolean loadedAppFromTable = false;

	private boolean usingNewDB = false;

	// indicates whether old or new client db tables are used
	private cfSession session;

	private boolean clientStart = false;

	private boolean isModified = false; // has client data been modified this
																			// request?

	public boolean isClientStart() {
		return clientStart;
	}

	// ---------------------------------------------

	public cfClientSessionData(cfSession _Session, sessionUtility global, String _clientStorageType, String _appName, boolean _updateAccessData) throws cfmRunTimeException {
		super(null); // null because we're going to use setHashData()

		// --[ Setup the Client data
		CFID = global.CFID;
		CFTOKEN = global.CFTOKEN;

		session = _Session;
		appName = _appName;

		// Oracle doesn't allow primary key fields to be assigned an empty string
		// so we need to set the unnamed app to a different value
		if ((appName == null) || (appName.length() == 0))
			appName = (cfApplicationManager.cf5ClientData ? "        " : "BD_Unnamed_App");

		if (cfApplicationManager.cf5ClientData) {
			appName = appName.toUpperCase();
		}

		// --[ Determine the type of storage we are using for this one
		if (_clientStorageType == null || _clientStorageType.length() == 0 || _clientStorageType.equalsIgnoreCase("COOKIE")) {
			// --[ If the storage type is a cookie, we will have to hold the page back
			// until all the processing is complete
			clientStorageType = COOKIE;
			loadCookie(_Session);

		} else if (_clientStorageType.equalsIgnoreCase("REGISTRY")) {
			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_APPLICATION);
			catchData.setDetail("As of BlueDragon 7.0, the registry can no longer be used to store client data.");
			catchData.setMessage("Cannot use the REGISTRY to store client data");
			throw new cfmRunTimeException(catchData);
		} else {
			clientStorageType = DATABASE;
			dataSource = _clientStorageType;
			loadDB(_Session);
		}

		// --[ Something has gone wrong, and we need to reset the scope
		if (clientStorageType == INVALID) {
			setHashData(new FastMap<String, cfData>(FastMap.CASE_INSENSITIVE));
			initialize();
		}

		// --[ Update the client scope with data
		if (_updateAccessData)
			updateAccessData();
	}

	// ----------------------------------------------------

	public void setData(cfData _key, cfData _data) throws cfmRunTimeException {
		flushCheck();
		if (cfData.isSimpleValue(_data)) {
			this.isModified = true;
			super.setData(_key, _data);
		}
	}

	public void setData(String _key, cfData _data) {
		if (cfData.isSimpleValue(_data)) {
			this.isModified = true;
			super.setData(_key, _data);
		}
	}

	public void deleteData(String _key) throws cfmRunTimeException {
		if (this.containsKey(_key)) {
			this.isModified = true;
			super.deleteData(_key);
		}
	}

	public void clear() {
		this.isModified = true;
		super.clear();
	}

	private void flushCheck() throws cfmRunTimeException {
		if (clientStorageType == COOKIE && session.isFlushed()) {
			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_TEMPLATE);
			catchData.setDetail("When CLIENT variables are stored as cookies you cannot set a CLIENT variable after the page has been flushed");
			catchData.setMessage("Cannot set CLIENT variable");
			throw new cfmRunTimeException(catchData);
		}
	}

	// -----------------------------------------------------

	// req'd by cfFlush so as cookies are set if the client data is saved
	// as cookies.
	public void flush(cfSession _Session) {
		if (clientStorageType == COOKIE) {
			saveCookie(_Session);
		}
	}

	public void close(cfSession _Session) throws cfmRunTimeException {
		getHashData().put("lastvisit", new cfDateData(System.currentTimeMillis()));

		switch (clientStorageType) {
		case COOKIE:
			saveCookie(_Session);
			break;
		case DATABASE:
			if (!cfApplicationManager.clientGlobalUpdateDisabled || this.isModified) {
				saveDB();
			}
			break;
		default:
			throw new IllegalStateException("illegal client storage type - " + clientStorageType);
		}
	}

	private void initialize() {
		Map<String, cfData> hashdata = getHashData();
		hashdata.put("hitcount", new cfNumberData(0));
		hashdata.put("cfid", new cfStringData(CFID));
		hashdata.put("cftoken", new cfStringData(CFTOKEN));
		hashdata.put("timecreated", new cfDateData(System.currentTimeMillis()));
		hashdata.put("lastvisit", new cfDateData(System.currentTimeMillis()));
		hashdata.put("urltoken", new cfStringData("CFID=" + CFID + "&CFTOKEN=" + CFTOKEN));

		clientStart = true;
	}

	private void updateAccessData() throws dataNotSupportedException {
		Map<String, cfData> hashdata = getHashData();

		if (hashdata.containsKey("hitcount")) {
			cfNumberData hitcount = ((cfData) hashdata.get("hitcount")).getNumber();
			if (hitcount != null) {
				hitcount.add(1);
				hashdata.put("hitcount", hitcount);
			} else {
				hashdata.put("hitcount", new cfNumberData(1));
			}
		} else {
			hashdata.put("hitcount", new cfNumberData(1));
		}

		if (!hashdata.containsKey("lastvisit")) {
			hashdata.put("lastvisit", new cfDateData(System.currentTimeMillis()));
		} else {
			cfDateData lastvisit = ((cfData) hashdata.get("lastvisit")).getDateData();
			if (lastvisit == null) {
				hashdata.put("lastvisit", new cfDateData(System.currentTimeMillis()));
			}
		}
	}

	public String getNonReadOnlyList() {
		String list = "";
		Map<String, cfData> hashdata = getHashData();
		synchronized (hashdata) {
			Iterator<String> iter = hashdata.keySet().iterator();
			while (iter.hasNext()) {
				String K = iter.next();
				if (!K.equalsIgnoreCase("hitcount") && !K.equalsIgnoreCase("cfid") && !K.equalsIgnoreCase("cftoken") && !K.equalsIgnoreCase("timecreated") && !K.equalsIgnoreCase("lastvisit") && !K.equalsIgnoreCase("urltoken"))
					list += K + ",";
			}
		}

		if (list.length() > 0)
			list = list.substring(0, list.length() - 1);

		return list;
	}

	// -------------------------------------------------------------------
	// --[ Routines to save/load to the database
	// -------------------------------------------------------------------

	private void loadDB(cfSession _Session) throws cfmRunTimeException {

		// -----------------------------------------------------------------
		// --[ Attempt to locate the datasource, throw an error if not found
		try {
			sqldataSource = new cfDataSource(dataSource, _Session);
		} catch (cfmRunTimeException RFE) {
			RFE.getCatchData().setExtendedInfo(cfEngine.getMessage("cfapplication.clientBadDataSource"));
			throw RFE;
		}

		// if the BDCLIENTDATA table exists then we want to use the old method
		// otherwise look to BDGLOBAL/BDDATA for client data creating the tables if
		// required
		if (cfApplicationManager.cf5ClientData || !loadDBOld()) {
			loadDBNew();
			usingNewDB = true;
		}

		// --[ The hashdata may not have been initialised, so lets make sure it
		// lives!
		Map<String, cfData> hashdata = getHashData();
		if (hashdata == null) {
			setHashData(new FastMap<String, cfData>(FastMap.CASE_INSENSITIVE));
			hashdata = getHashData();
		}
		if (hashdata.size() == 0)
			initialize();
	}

	private boolean loadDBOld() {
		// --[ Attempt to load the table. if it's not there then don't create it
		if (!loadData(sqldataSource)) {
			return false;
		} else {
			return true;
		}

	}

	private void loadDBNew() throws cfmRunTimeException {
		if (!loadDataNew(sqldataSource)) {
			if (cfApplicationManager.cf5ClientData) { // don't create tables when in
																								// ColdFusion-compatible mode
				clientStorageType = INVALID;
				throw newInvalidDatasourceException("Failed to load data");
			}

			if (!createTable(sqldataSource)) {
				clientStorageType = INVALID;
				throw newInvalidDatasourceException("Failed to create table");
			}

			// try again after creating tables
			if (!loadDataNew(sqldataSource)) {
				clientStorageType = INVALID;
				throw newInvalidDatasourceException("Failed to load data");
			}
		}
	}

	private cfmRunTimeException newInvalidDatasourceException(String message) {
		cfCatchData catchData = new cfCatchData();
		catchData.setType(cfCatchData.TYPE_EXPRESSION);
		catchData.setDetail("Check that the datasource '" + sqldataSource.getDataSourceName() + "' exists and the necessary write permissions are configured.");
		catchData.setMessage("Invalid client storage datasource (" + message + ")");
		return new cfmRunTimeException(catchData);
	}

	private void saveDB() throws cfmRunTimeException {
		if (usingNewDB) {
			saveDBNew();
		} else {
			saveDBOld();
		}
	}

	private void saveDBNew() throws cfmRunTimeException {
		String key = CFID + (cfApplicationManager.cf5ClientData ? ":" : "-") + CFTOKEN;
		Connection con = null;

		// INSERT or UPDATE global data?
		Map<String, cfData> globalData = getGlobalData();

		try {
			con = sqldataSource.getConnection();
			PreparedStatement statmt = null;

			// --[ select the SQL string
			if (loadedGlobalFromTable) { // UPDATE since the data is already in the
																		// table
				statmt = con.prepareStatement(SQL_UPDATE_GLOBAL);
			} else { // otherwise entry not already there so INSERT
				statmt = con.prepareStatement(SQL_INSERT_GLOBAL);
			}

			// --[ set the parameters. Note the 2 SQL statements list the params in
			// the same order
			String tmp = encodeData(globalData);

			statmt.setAsciiStream(1, new ByteArrayInputStream(tmp.getBytes()), tmp.length());
			statmt.setTimestamp(2, new java.sql.Timestamp(((cfData) globalData.get("lastvisit")).getDateData().getLong()));
			if (loadedGlobalFromTable && (COMPARISON_OP == LIKE)) {
				statmt.setString(3, key + "%");
			} else {
				statmt.setString(3, key);
			}

			statmt.executeUpdate();
			statmt.close();

			if (!con.getAutoCommit()) {
				cfEngine.log("CLIENT data update: auto-commit disabled for " + sqldataSource.getDataSourceName());
				con.commit();
			}
		} catch (SQLException E) {
			cfEngine.log("Error updating CLIENT data for " + CFID + "-" + CFTOKEN + ": " + E);
			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_APPLICATION);
			catchData.setDetail("Database error: " + E.getMessage());
			catchData.setMessage("Error occurred when attempting to save client data to database");
			throw new cfmRunTimeException(catchData);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
		}

		// INSERT or UPDATE app-specific data?
		Map<String, cfData> appData = getAppData();

		try {
			con = sqldataSource.getConnection();
			PreparedStatement statmt = null;

			// --[ select the SQL string
			if (loadedAppFromTable) { // UPDATE since the data is already in the table
				statmt = con.prepareStatement(SQL_UPDATE_DATA);
			} else { // otherwise entry not already there so INSERT
				statmt = con.prepareStatement(SQL_INSERT_DATA);
			}

			// --[ set the parameters. Note the 2 SQL statements list the params in
			// the same order
			String tmp;
			tmp = encodeData(appData);

			statmt.setAsciiStream(1, new ByteArrayInputStream(tmp.getBytes()), tmp.length());
			if (loadedAppFromTable && (COMPARISON_OP == LIKE)) {
				statmt.setString(2, key + "%");
				statmt.setString(3, appName + "%");
			} else {
				statmt.setString(2, key);
				statmt.setString(3, appName);
			}

			statmt.executeUpdate();
			statmt.close();

			if (!con.getAutoCommit()) {
				cfEngine.log("CLIENT data update: auto-commit disabled for " + sqldataSource.getDataSourceName());
				con.commit();
			}
		} catch (SQLException E) {
			cfEngine.log("Error updating CLIENT data for " + CFID + "-" + CFTOKEN + ": " + E);
			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_APPLICATION);
			catchData.setDetail("Database error: " + E.getMessage());
			catchData.setMessage("Error occurred when attempting to save client data to database");
			throw new cfmRunTimeException(catchData);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
		}

	}

	private void saveDBOld() throws cfmRunTimeException {
		Connection Con = null;

		// --[ Insert the new one
		if (bLoadedFromTable) {
			try {
				Con = sqldataSource.getConnection();
				PreparedStatement Statmt = Con.prepareStatement(SQL_UPDATE);
				Statmt.setString(2, CFID + "-" + CFTOKEN);
				String tmp = encode(getHashData());
				Statmt.setAsciiStream(1, new ByteArrayInputStream(tmp.getBytes()), tmp.length());
				Statmt.executeUpdate();
				Statmt.close();
				if (!Con.getAutoCommit()) {
					cfEngine.log("CLIENT data update: auto-commit disabled for " + sqldataSource.getDataSourceName());
					Con.commit();
				}
			} catch (SQLException E) {
				cfEngine.log("Error updating CLIENT data for " + CFID + "-" + CFTOKEN + ": " + E);
				cfCatchData catchData = new cfCatchData();
				catchData.setType(cfCatchData.TYPE_APPLICATION);
				catchData.setDetail("Database error: " + E.getMessage());
				catchData.setMessage("Error occurred when attempting to save client data to database");
				throw new cfmRunTimeException(catchData);
			} finally {
				if (Con != null) {
					try {
						Con.close();
					} catch (SQLException ignore) {
					}
				}
			}
		} else {
			try {
				Con = sqldataSource.getConnection();
				PreparedStatement Statmt = Con.prepareStatement(SQL_INSERT);
				Statmt.setString(1, CFID + "-" + CFTOKEN);
				String tmp = encode(getHashData());
				Statmt.setAsciiStream(2, new ByteArrayInputStream(tmp.getBytes()), tmp.length());
				Statmt.executeUpdate();
				Statmt.close();
				if (!Con.getAutoCommit()) {
					cfEngine.log("CLIENT data insert: auto-commit disabled for " + sqldataSource.getDataSourceName());
					Con.commit();
				}
			} catch (Exception E) {
				cfEngine.log("Error inserting CLIENT data for " + CFID + "-" + CFTOKEN + ": " + E);
				cfCatchData catchData = new cfCatchData();
				catchData.setType(cfCatchData.TYPE_APPLICATION);
				catchData.setDetail("Database error: " + E.getMessage());
				catchData.setMessage("Error occurred when attempting to save client data to database");
				throw new cfmRunTimeException(catchData);
			} finally {
				if (Con != null) {
					try {
						Con.close();
					} catch (SQLException ignore) {
					}
				}
			}
		}
	}

	// ---------------------------------------------------------

	// creates the tables BDGLOBAL/BDDATA if they don't already exist at the
	// specified datasource
	private static boolean createTable(cfDataSource _sqldataSource) {
		Connection Con = null;
		try {
			Con = _sqldataSource.getConnection();

			boolean bdGlobalExists = com.nary.db.metaDatabase.tableExist(Con, _sqldataSource.getCatalog(), GLOBAL_TABLE_NAME);
			boolean bdDataExists = com.nary.db.metaDatabase.tableExist(Con, _sqldataSource.getCatalog(), DATA_TABLE_NAME);

			// If both tables already exist then return true
			if (bdGlobalExists && bdDataExists)
				return true;

			if (!bdGlobalExists) {
				// --[ The table doesn't exist, now we need to attempt to create it
				boolean bFound = false;

				for (int x = 0; x < SQL_CREATE_GLOBAL.length; x++) {
					if (com.nary.db.metaDatabase.createTable(Con, SQL_CREATE_GLOBAL[x])) {
						bFound = true;
						break;
					} else {
						cfEngine.log("-] Application.Client.FailedToCreateTable:[" + SQL_CREATE_GLOBAL[x] + "]");
					}
				}

				if (!bFound) { // if there's an error return now, else continue
					return bFound;
				}
			}

			// check also that the BDDATA table exists. We could assume that it exists
			// if BDGLOBAL
			// exists but best to be tolerant
			if (!bdDataExists) {
				// --[ The table doesn't exist, now we need to attempt to create it
				boolean bFound = false;

				for (int x = 0; x < SQL_CREATE_DATA.length; x++) {
					if (com.nary.db.metaDatabase.createTable(Con, SQL_CREATE_DATA[x])) {
						bFound = true;
						break;
					} else {
						cfEngine.log("-] Application.Client.FailedToCreateTable:[" + SQL_CREATE_DATA[x] + "]");
					}
				}
				return bFound;
			}
		} catch (Exception E) {
			cfEngine.log("-] Application.Client.FailedToGetConnection:[" + E + "]");
		} finally {
			if (Con != null) {
				try {
					Con.close();
				} catch (SQLException ignore) {
				}
			}
		}

		return false;
	}

	private boolean loadData(cfDataSource _sqldataSource) {
		Connection Con = null;
		ResultSet Res;
		PreparedStatement Statmt;
		boolean bDeleteClientRow = false;

		try {
			Con = _sqldataSource.getConnection();
			Statmt = Con.prepareStatement(SQL_SELECT);
			Statmt.setString(1, CFID + "-" + CFTOKEN);
			Res = Statmt.executeQuery();

			if (Res.next()) {
				Map<String, cfData> hashdata = decode(Res.getString(1));
				if (hashdata != null) {
					setHashData(hashdata);
					bLoadedFromTable = true;
				} else {
					cfEngine.log("Deleting bad CLIENT data for: " + CFID + "-" + CFTOKEN);
					bDeleteClientRow = true;
					// the data in the table is bad--so delete it
				}
			}

			// --[ Close off the database connections
			Res.close();
			Statmt.close();

			// --[ Since the connections are closed off, we can delete any bad data
			// sitting in the table
			if (bDeleteClientRow)
				deleteClientData(Con);

			return true;
		} catch (Exception E) {
			// this exception is expected when the BDCLIENTDATA table hasn't been
			// created yet
			// CFID+"-"+CFTOKEN + ": " + E );
		} finally {
			if (Con != null) {
				try {
					Con.close();
				} catch (SQLException ignore) {
				}
			}
		}

		return false;
	}

	private boolean loadDataNew(cfDataSource _sqldataSource) {
		Connection Con = null;
		ResultSet Res = null;
		PreparedStatement Statmt = null;

		// first load global data
		try {
			String key = CFID + (cfApplicationManager.cf5ClientData ? ":" : "-") + CFTOKEN;
			Con = _sqldataSource.getConnection();
			Statmt = Con.prepareStatement(SQL_SELECT_GLOBAL);
			if (COMPARISON_OP == LIKE) {
				Statmt.setString(1, key + "%");
			} else {
				Statmt.setString(1, key);
			}
			Res = Statmt.executeQuery();

			Map<String, cfData> globalData = null;
			Map<String, cfData> appData = null;

			if (Res.next()) {
				loadedGlobalFromTable = true;
				globalData = decodeData(Res.getString(1));

				if (cfApplicationManager.cf5ClientData) {
					globalData.put("URLTOKEN", new cfStringData("CFID=" + CFID + "&CFTOKEN=" + CFTOKEN));
				}
			}

			// --[ Close off the database connections in preparation for next query
			Res.close();
			Statmt.close();

			// --[ Now load the app-specific data
			Statmt = Con.prepareStatement(SQL_SELECT_DATA);
			if (COMPARISON_OP == LIKE) {
				Statmt.setString(1, key + "%");
				Statmt.setString(2, appName + "%");
			} else {
				Statmt.setString(1, key);
				Statmt.setString(2, appName);
			}

			Res = Statmt.executeQuery();
			if (Res.next()) {
				loadedAppFromTable = true;
				appData = decodeData(Res.getString(1));
			}

			// --[ Close off the database connections
			Res.close();
			Statmt.close();

			Map<String, cfData> hashdata = new FastMap<String, cfData>(FastMap.CASE_INSENSITIVE);
			if (globalData != null) {
				hashdata.putAll(globalData);
			}
			if (appData != null) {
				hashdata.putAll(appData);
			}
			setHashData(hashdata);

			return true;
		} catch (Exception E) {
			// this exception is expected when the BDCLIENTDATA table hasn't been
			// created yet
			// CFID+"-"+CFTOKEN + ": " + E );

			// If an exception was thrown then make sure we close the resultset and
			// statement
			if (Res != null) {
				try {
					Res.close();
				} catch (java.sql.SQLException e1) {
				}
			}

			if (Statmt != null) {
				try {
					Statmt.close();
				} catch (java.sql.SQLException e1) {
				}
			}
		} finally {
			if (Con != null) {
				try {
					Con.close();
				} catch (SQLException ignore) {
				}
			}
		}

		return false;
	}

	private String encodeData(Map<String, cfData> globalData) throws dataNotSupportedException {
		if (cfApplicationManager.cf5ClientData) {
			return encodeColdFusionData(globalData);
		}
		return encode(globalData);
	}

	private Map<String, cfData> decodeData(String data) throws IOException {
		if (cfApplicationManager.cf5ClientData) {
			return decodeColdFusionData(data);
		}
		return decode(data);
	}

	// method for deleting client data (old version)
	private void deleteClientData(Connection Con) {
		PreparedStatement Statmt = null;
		try {
			Statmt = Con.prepareStatement(SQL_DELETE);
			Statmt.setString(1, CFID + "-" + CFTOKEN);
			Statmt.executeUpdate();
		} catch (Exception e) {
			cfEngine.log("Error deleting CLIENT data for " + CFID + "-" + CFTOKEN + ": " + e);
		} finally {
			try {
				if (Statmt != null)
					Statmt.close();
			} catch (Exception ignoreException2) {
			}
		}
	}

	// -------------------------------------------------------------------
	// --[ Routines to save/load to COOKIE
	// -------------------------------------------------------------------

	private void saveCookie(cfSession _Session) {
		Map<String, cfData> globalData = getGlobalData();

		// take a copy of the client scope removing all the global variables leaving
		// us with the app specific variables
		Map<String, cfData> appData = getAppData();

		try {
			cfCookieData cookieHolder = (cfCookieData) _Session.getQualifiedData(variableStore.COOKIE_SCOPE);

			String encodedGlobalData = encodeData(globalData);

			// The Netscape cookie spec. limits the cookie name and value to 4k so log
			// a warning if they are longer than that.
			int cookieLen = GLOBAL_COOKIE_NAME.length() + encodedGlobalData.length();
			if (cookieLen > 4096)
				cfEngine.log("-] WARNING: the client global data cookie is greater than 4k so some browsers may not accept it.(len=" + cookieLen + ")");

			Cookie globCookie = new Cookie(GLOBAL_COOKIE_NAME, encodedGlobalData);
			globCookie.setMaxAge(9 * 365 * 24 * 60 * 60);
			globCookie.setPath("/");
			cookieHolder.setData(globCookie, true);

			String encodedAppData = encodeData(appData);

			// The Netscape cookie spec. limits the cookie name and value to 4k so log
			// a warning if they are longer than that; we want to remove spaces from
			// the cookie name
			String cookieName = (CLIENT_COOKIE_NAME + appName).trim();
			cookieName = cookieName.replace(' ', '_');
			cookieLen = cookieName.length() + encodedGlobalData.length();
			if (cookieLen > 4096)
				cfEngine.log("-] WARNING: the client application data cookie for " + appName + " is greater than 4k so some browsers may not accept it.(len=" + cookieLen + ")");

			Cookie appCookie = new Cookie(cookieName, encodedAppData);
			appCookie.setMaxAge(9 * 365 * 24 * 60 * 60);
			appCookie.setPath("/");
			cookieHolder.setData(appCookie, true);
		} catch (cfmRunTimeException e) {
		}

	}

	private void loadCookie(cfSession _Session) {
		// CFGLOBALS - urltoken, lastvisit, timecreated, hitcount, cftoken, cfid
		Map<String, cfData> hashdata = new FastMap<String, cfData>(FastMap.CASE_INSENSITIVE);

		try {
			cfData cookieData = _Session.getQualifiedData(variableStore.COOKIE_SCOPE);

			// load global data
			Map<String, cfData> globData = null;
			if (cookieData != null) {
				cfData data = cookieData.getData(GLOBAL_COOKIE_NAME);
				if (data != null) {
					globData = decodeData(data.getString());
				}
			}

			// if there's no client data found in the cookie scope
			if (globData == null) {
				setHashData(new FastMap<String, cfData>(FastMap.CASE_INSENSITIVE));
				initialize();

			} else {
				// add global data to scope
				Iterator<String> keys = globData.keySet().iterator();
				while (keys.hasNext()) {
					String nextKey = keys.next();
					hashdata.put(nextKey, globData.get(nextKey));
				}

				// load app-specific client data
				Map<String, cfData> appData = null;
				if (cookieData != null) {
					cfData data = cookieData.getData((CLIENT_COOKIE_NAME + appName.replace(' ', '_')).trim());
					if (data != null)
						appData = decodeData(data.getString());
				}

				if (appData != null) {
					keys = appData.keySet().iterator();
					while (keys.hasNext()) {
						String nextKey = keys.next();
						hashdata.put(nextKey, appData.get(nextKey));
					}
				}

				setHashData(hashdata);
			}

		} catch (IOException ie) {
			setHashData(new FastMap<String, cfData>(FastMap.CASE_INSENSITIVE));
			initialize();
		} catch (cfmRunTimeException E) {
			setHashData(new FastMap<String, cfData>(FastMap.CASE_INSENSITIVE));
			initialize();
		}
	}

	// -------------------------------------------------------------------
	// --[ Routines for encoding and decoding the string data
	// -------------------------------------------------------------------

	// returns a map with all the application specific data i.e. variables such as
	// hitcount are ommitted
	private Map<String, cfData> getAppData() {
		Map<String, cfData> appData = new FastMap<String, cfData>(FastMap.CASE_INSENSITIVE);
		appData.putAll(getHashData());
		appData.remove("urltoken");
		appData.remove("lastvisit");
		appData.remove("timecreated");
		appData.remove("hitcount");
		appData.remove("cftoken");
		appData.remove("cfid");
		return appData;
	}

	private Map<String, cfData> getGlobalData() {
		Map<String, cfData> hashdata = getHashData();
		Map<String, cfData> globalData = new FastMap<String, cfData>(FastMap.CASE_INSENSITIVE);
		cfData nextItem;

		if ((nextItem = hashdata.get("hitcount")) != null) {
			globalData.put("hitcount", nextItem);
		} else {
			globalData.put("hitcount", new cfNumberData(1));
		}

		if ((nextItem = hashdata.get("lastvisit")) != null) {
			globalData.put("lastvisit", nextItem);
		} else {
			globalData.put("lastvisit", new cfDateData(System.currentTimeMillis()));
		}

		if ((nextItem = hashdata.get("timecreated")) != null) {
			globalData.put("timecreated", nextItem);
		} else {
			globalData.put("timecreated", new cfDateData(System.currentTimeMillis()));
		}

		if ((nextItem = hashdata.get("urltoken")) != null) {
			globalData.put("urltoken", nextItem);
		} else {
			globalData.put("urltoken", new cfStringData("CFID=" + CFID + "&CFTOKEN=" + CFTOKEN));
		}

		if ((nextItem = hashdata.get("cftoken")) != null) {
			globalData.put("cftoken", nextItem);
		} else {
			globalData.put("cftoken", new cfStringData(CFTOKEN));
		}

		if ((nextItem = hashdata.get("cfid")) != null) {
			globalData.put("cfid", nextItem);
		} else {
			globalData.put("cfid", new cfStringData(CFID));
		}

		return globalData;
	}

	private static String encode(Map<String, cfData> hashdata) {
		try {
			ByteArrayOutputStream FS = new ByteArrayOutputStream();
			ObjectOutputStream OS = new ObjectOutputStream(FS);
			OS.writeObject(hashdata);
			OS.close();
			return new String(com.nary.net.Base64.base64Encode(FS.toByteArray()));
		} catch (Exception E) {
			cfEngine.log("Error encoding CLIENT data: " + E);
			return "";
		}
	}

	private static Map<String, cfData> decode(String data64) {
		if (data64 == null)
			return null;

		ObjectInputStream IS = null;

		try {
			ByteArrayInputStream FS = new ByteArrayInputStream(com.nary.net.Base64.base64Decode(data64.getBytes()));
			IS = new ObjectInputStream(FS);
			return (Map<String, cfData>) IS.readObject();
		} catch (Exception E) {
			cfEngine.log("Error decoding CLIENT data: " + E);
			return null;
		} finally {
			try {
				if (IS != null)
					IS.close();
			} catch (IOException ignore) {
			}
		}
	}

	/**
	 * ColdFusion (CF5 and CFMX) encode client data as a sequence of name=value
	 * pairs delimited by hash symbols (#); for example:
	 * 
	 * name1=value1#name2=value2#name3=value3
	 * 
	 * A double-hash (##) is not treated as a delimiter, but as part of the value;
	 * the value associated with "name2" is "value2##contains##hashes":
	 * 
	 * name1=value1#name2=value2##contains##hashes#name3=value3
	 * 
	 * Further, CFMX seems to escape equals signs (=) embeded within values by
	 * preceding them with a hash, though CF5 does not, and CFMX can properly read
	 * client data written by CF5; so escaping embedded equals seems optional. For
	 * example, the value associated with "name2" is "color=red" on both CF5 and
	 * CFMX:
	 * 
	 * name1=value1#name2=color=red#name3=value3
	 * 
	 * On CFMX with optional escaping it would appear like this:
	 * 
	 * name1=value1#name2=color#=red#name3=value3
	 * 
	 * Therefore, our implementation, below, has the ability to read escaped
	 * equals signs, but does not escape them when writing out client data.
	 */
	private static Map<String, cfData> decodeColdFusionData(String data) throws IOException {
		if (data == null) {
			return null;
		}
		Map<String, cfData> dataMap = new FastMap<String, cfData>(FastMap.CASE_INSENSITIVE);
		StringBuilder sb = new StringBuilder();
		Reader reader = new BufferedReader(new StringReader(data));
		String name = null;
		int ch;
		while ((ch = reader.read()) != -1) {
			// prior to reading name, = is terminator for name, after that it's part
			// of the value
			if ((name == null) && (ch == '=')) {
				name = sb.toString();
				sb.setLength(0);
			} else if (ch == '#') { // look at the next char for escaped # or =
				int next = reader.read();
				if (next == '#') { // write out both # chars
					sb.append((char) ch);
					sb.append((char) next);
				} else if (next == '=') { // discard the # and write out the =
					sb.append((char) next);
				} else { // # is value terminator
					dataMap.put(name, new cfStringData(sb.toString()));
					if (next == -1) { // we're done
						break;
					} else { // reset for next name=value pair
						sb.setLength(0);
						sb.append((char) next);
						name = null;
					}
				}
			} else {
				sb.append((char) ch);
			}
		}
		return dataMap;
	}

	private static String encodeColdFusionData(Map<String, cfData> data) throws dataNotSupportedException {
		StringBuilder sb = new StringBuilder();
		Iterator<String> iter = data.keySet().iterator();
		while (iter.hasNext()) {
			// CF5 writes names out in uppercase; CFMX in lowercase
			String name = iter.next().toUpperCase();
			sb.append(name);
			sb.append('=');
			sb.append(((cfData) data.get(name)).getString());
			sb.append('#');
		}
		return sb.toString();
	}
}
