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
 *  $Id: cfSQLQueryData.java 2388 2013-06-19 10:54:52Z andy $
 */
package com.naryx.tagfusion.cfm.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import com.naryx.tagfusion.cfm.cache.CacheFactory;
import com.naryx.tagfusion.cfm.cache.CacheInterface;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryInterface;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.sql.pool.WrappedConnection;

public class cfSQLQueryData extends cfQueryResultData implements cfQueryInterface, java.io.Serializable {
	static final long serialVersionUID = 1;

	public static final int SQL_SELECT = 0;
	public static final int SQL_INSERT = 1;
	public static final int SQL_DELETE = 2;
	public static final int SQL_UPDATE = 3;
	public static final int SQL_UNKNOWN = 4; // assumed to be a stored procedure/ call

	transient private cfDataSource thisDataSource; // don't need to serialise this out

	transient private int sql_type;
	transient private String errMessage;

	transient protected boolean resultSet;

	transient private long 		cacheTimeOut = -1;
	transient private String 	cacheName = null;
	transient private String 	cacheRegion = null;
	transient private String 	internalCacheName = null;
	private boolean usingCache;
	public long lastCacheRead;

	
	// The following items are displayed in a dump so let's serialize them out.
	private String dataSourceName;

	private boolean queryRun;
	private boolean retrieveGenKeys;

	private String generatedKeys;
	private String keyColName;
	private int recordsUpdated;

	// attribute getters for use by the admin console
	public String getDataSourceName() {
		return dataSourceName;
	}

	public boolean getCacheUsed() {
		return usingCache;
	}

	public String getCacheName() {
		return cacheName;
	}

	public String getCacheTimeOut() {
		return (cacheTimeOut > 0 ? com.nary.util.Date.formatDate(cacheTimeOut, "yyyy-MM-dd HH:mm:ss") : "");
	}

	/*
	 * setRetrieveGeneratedKeys
	 * 
	 * This is called by the CFQUERY code to set retrieveGenKeys to true if the
	 * CFQUERY tag has a RESULT attribute.
	 */
	public void setRetrieveGeneratedKeys(boolean _b) {
		retrieveGenKeys = _b;
	}

	public boolean hasGeneratedKeys() {
		return keyColName != null;
	}

	public int getUpdatedCount(){
		return recordsUpdated;
	}
	
	public String getGeneratedKeys() {
		return generatedKeys;
	}

	public String getGeneratedKeysName() {
		return keyColName;
	}

	// req'd for debug
	public List<preparedData> getParams() {
		return preparedDataList;
	}

	public cfSQLQueryData(String _querySource) {
		super(new String[] {}, _querySource);
	}

	public cfSQLQueryData(cfDataSource _thisDataSource) {
		super(new String[] {}, "SQL Query");

		setDataSource(_thisDataSource);

		queryString = null;
		sql_type = SQL_UNKNOWN;
		maxRows = -1;
		queryRun = false;
		errMessage = null;
		usingCache = false;
	}

	public void setDataSource(cfDataSource _thisDataSource) {
		thisDataSource = _thisDataSource;
		dataSourceName = thisDataSource.getDataSourceName();
	}

	public void setQueryString(String _queryString) {
		queryString = _queryString.trim();
		String lcaseqs = queryString.toLowerCase();
		if (lcaseqs.indexOf("select") != -1)
			sql_type = SQL_SELECT;
		else if (lcaseqs.indexOf("insert") != -1)
			sql_type = SQL_INSERT;
		else if (lcaseqs.indexOf("delete") != -1)
			sql_type = SQL_DELETE;
		else if (lcaseqs.indexOf("update") != -1)
			sql_type = SQL_UPDATE;
		else
			sql_type = SQL_UNKNOWN; // assumed to be a stored procedure call

		// a trailing ';' causes problems for Oracle in some cases, so delete it
		// (this was a CF5 compatibility problem for ebags.com; see bug #2209)
		if ((sql_type != SQL_UNKNOWN) && (queryString.charAt(queryString.length() - 1) == ';')) {
			queryString = queryString.substring(0, queryString.length() - 1);
		}

		// If this isn't an INSERT statement then force retrieveGenKeys to false to avoid trying to retrieve generated keys.
		if (sql_type != SQL_INSERT)
			retrieveGenKeys = false;
		
		super.setQueryString(queryString);
	}

	public int getQueryType() {
		return sql_type;
	}

	public boolean hasResultSet() {
		return resultSet;
	}

	public void runQuery(cfSession _Session) throws cfmRunTimeException {
		if (!queryRun) {
			if (usingCache && (sql_type == SQL_SELECT || sql_type == SQL_UNKNOWN)) // Only use the cache for SELECT statements
				loadDataFromCache(_Session);
			else
				execute(_Session);
		}
	}

	// override this method to ensure that the query has run before anyone tries to access a variable
	public cfData getData(String _key) {
		return super.getData(_key);
	}

	public void execute(cfSession _Session) throws cfmRunTimeException {
		try {
			_Session.getDebugRecorder().execOnStart(this);

			if (preparedDataList != null)
				executeAsPreparedStatement();
			else
				executeAsStatement();

		} finally {
			_Session.getDebugRecorder().execOnEnd(this);
		}
	}

	
	
	/*
	 * supportsGetGeneratedKeys
	 * 
	 * Returns true if the driver supports the retrieval of generated keys.
	 */
	private boolean supportsGetGeneratedKeys(Connection dataConnection) throws SQLException {
		try {
			return dataConnection.getMetaData().supportsGetGeneratedKeys();
		} catch (AbstractMethodError exc) {
			// Older JDBC drivers won't support the supportsGetGeneratedKeys() method/ and
			// will throw an AbstractMethodError. In this case return false.
			// NOTE: this was seen with the PostgreSQL JDBC driver.
			return false;
		}
	}

	private void executeAsStatement() throws cfmRunTimeException {
		Connection dataConnection = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			dataConnection = setupDataConnection();
			stmt = dataConnection.createStatement();

			if (dataConnection instanceof WrappedConnection)
				((WrappedConnection) dataConnection).setLastQuery("STATEMENT: " + queryString);

			boolean oracleDriver = stmt.getClass().getName().equals("oracle.jdbc.driver.T4CStatement");

			if (!oracleDriver) {
				// the Oracle JDBC driver has a bug: stmt.getUpdateCount() always
				// returns a positive number, instead of -1 when there are no more
				// results; this bug puts the following code into an infinite loop

				// If retrieveGenKeys is true then force it to false if the driver
				// doesn't support the retrieval of generated keys.
				if (retrieveGenKeys)
					retrieveGenKeys = supportsGetGeneratedKeys(dataConnection);

				if (retrieveGenKeys)
					stmt.execute(queryString, Statement.RETURN_GENERATED_KEYS);
				else
					stmt.execute(queryString);

				// With MySQL we need to retrieve the generated keys before
				// stmt.getMoreResults() and stmt.getUpdateCount() are called.
				ResultSet rsGK = initGeneratedKeys(dataConnection, stmt);
				recordsUpdated = 0;

				do { // there may be multiple result sets and/or update counts
					rs = stmt.getResultSet();
					
					if (rs != null && rs != rsGK) { // return the first result set (if any)
						resultSet = true;
						populate(rs, maxRows);
						rs.close();
						break;
					}
					
					recordsUpdated += stmt.getUpdateCount();
				} while (stmt.getMoreResults() || ( (stmt.getUpdateCount()) >= 0));
				
			} else {
				// this is the workaround for the Oracle bug mentioned above
				boolean hasResultSet;
				if (retrieveGenKeys)
					hasResultSet = stmt.execute(queryString, Statement.RETURN_GENERATED_KEYS);
				else
					hasResultSet = stmt.execute(queryString);
				if (hasResultSet) { // Oracle only returns a single result
					rs = stmt.getResultSet();
					if (rs != null) {
						resultSet = true;
						populate(rs, maxRows);
					}
				}
				initGeneratedKeys(dataConnection, stmt);
			}
		} catch (SQLException e) {
			throw new cfmRunTimeException(catchDataFactory.databaseException(dataSourceName, "sql.execution", new String[] { com.naryx.tagfusion.cfm.tag.tagUtils.trimError(e.getMessage()) }, queryString, e));
		} finally {
			queryRun = true;
			closeConnections(dataConnection, stmt, rs);
		}
	}

	private void executeAsPreparedStatement() throws cfmRunTimeException {
		Connection dataConnection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			dataConnection = setupDataConnection();
			stmt = prepareStatement(dataConnection);
			boolean oracleDriver = stmt.getClass().getName().equals("oracle.jdbc.driver.OraclePreparedStatement");

			if (!oracleDriver) {
				// the Oracle JDBC driver has a bug: stmt.getUpdateCount() always
				// returns a positive number, instead of -1 when there are no more
				// results; this bug puts the following code into an infinite loop
				stmt.execute();

				// With MySQL we need to retrieve the generated keys before
				// stmt.getMoreResults() and stmt.getUpdateCount() are called.
				ResultSet rsGK = initGeneratedKeys(dataConnection, stmt);
				recordsUpdated = 0;
				
				do { // there may be multiple result sets and/or update counts
					rs = stmt.getResultSet();
					if (rs != null && rs != rsGK ) { // return the first result set (if any). Also need to check it isn't the generated keys ResultSet which SQL Server returns here too
						resultSet = true;
						populate(rs, maxRows);
						rs.close();
						break;
					}
					recordsUpdated += stmt.getUpdateCount();
				} while (stmt.getMoreResults() || (stmt.getUpdateCount() >= 0));

			} else {
				// this is the workaround for the Oracle bug mentioned above
				if (stmt.execute()) { // Oracle only returns a single result
					rs = stmt.getResultSet();
					if (rs != null) {
						resultSet = true;
						populate(rs, maxRows);
					}
				}
				initGeneratedKeys(dataConnection, stmt);
			}
		} catch (SQLException e) {
			throw new cfmRunTimeException(catchDataFactory.databaseException(dataSourceName, "sql.execution", new String[] { com.naryx.tagfusion.cfm.tag.tagUtils.trimError(e.getMessage()) }, queryString, e));
		} finally {
			queryRun = true;
			closeConnections(dataConnection, stmt, rs);
		}
	}

	
	
	/*
	 * initGeneratedKeys
	 * 
	 * Only initialize the generated keys variables if retrieveGenKeys is true.
	 * retrieveGenKeys should only be true if all of the following are true:
	 * 
	 * - the CFQUERY tag has a RESULT attribute specified - it's an INSERT
	 * statement - the driver supports retrieving generated keys
	 */
	private ResultSet initGeneratedKeys(Connection dataConnection, Statement _stmt) throws SQLException {
		if (retrieveGenKeys) {
			ResultSet keys = _stmt.getGeneratedKeys();
			try {
				if (keys.next()) {
					generatedKeys = keys.getString(1);

					// The JTurbo and Microsoft JDBC drivers return an empty string for
					// the
					// column name so force the keyColName to IDENTITYCOL to match CF8.
					String databaseName = dataConnection.getMetaData().getDatabaseProductName();
					if (databaseName.equals("Microsoft SQL Server"))
						keyColName = "IDENTITYCOL";
					else
						keyColName = keys.getMetaData().getColumnName(1);
				}
				
				return keys;
			} finally {
				keys.close();
			}
		}
		
		return null;
		
	}

	// ----------------------------------------------

	private PreparedStatement prepareStatement(Connection dataConnection) throws cfmRunTimeException, SQLException {
		PreparedStatement Statmt;

		// If retrieveGenKeys is true then force it to false if the driver doesn't support the retrieval of generated keys.
		if (retrieveGenKeys)
			retrieveGenKeys = supportsGetGeneratedKeys(dataConnection);

		if (retrieveGenKeys)
			Statmt = dataConnection.prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
		else
			Statmt = dataConnection.prepareStatement(queryString);

		if (preparedDataList != null) {

			Iterator<preparedData> iter = preparedDataList.iterator();
			int colIndex = 1;
			while (iter.hasNext()) {
				preparedData pData = iter.next();
				colIndex = pData.prepareStatement(colIndex, Statmt, dataConnection);
			}

		}

		return Statmt;
	}

	// ----------------------------------------------
	// Functions for retrieving and placing back the
	// java.sql.Connection for the given datasource
	// ----------------------------------------------

	private Connection setupDataConnection() throws cfmRunTimeException {
		executeTime = System.currentTimeMillis();
		Connection con = null;
		try {
			checkUsersSecurity();

			con = thisDataSource.takeConnection();
		} catch (SQLException e) {
			throw new cfmRunTimeException(catchDataFactory.databaseException(dataSourceName, "sql.connecting", new String[] { com.naryx.tagfusion.cfm.tag.tagUtils.trimError(e.getMessage()) }, queryString, e));
		}
		return con;
	}

	private void closeConnections(Connection dataConnection, Statement Statmt, ResultSet Res) {
		if (dataConnection != null) {
			executeTime = System.currentTimeMillis() - executeTime;
			try {
				if (Res != null)
					Res.close();
			} catch (Exception ignoreException) {
			}
			try {
				if (Statmt != null)
					Statmt.close();
			} catch (Exception ignoreException) {
			}
			thisDataSource.returnConnection(dataConnection);
			thisDataSource = null;
		}
	}

	// ---------------------------------------------

	private void checkUsersSecurity() throws cfmRunTimeException {
		boolean bError = false;

		if (sql_type == SQL_SELECT && !thisDataSource.isSql_select())
			bError = true;
		else if (sql_type == SQL_INSERT && !thisDataSource.isSql_insert())
			bError = true;
		else if (sql_type == SQL_DELETE && !thisDataSource.isSql_delete())
			bError = true;
		else if (sql_type == SQL_UPDATE && !thisDataSource.isSql_update())
			bError = true;
		else if (sql_type == SQL_UNKNOWN && !thisDataSource.isSql_storedprocedures())
			bError = true;

		if (bError) {
			throw new cfmRunTimeException(catchDataFactory.extendedException(cfCatchData.TYPE_DATABASE, "errorCode.sqlError", "sql.disabled", null, queryString));
		}
	}

	// --------------------------------------------------------------

	public String toString() {
		StringBuilder buffer = new StringBuilder(256);
		buffer.append("{QUERY: ");
		buffer.append("DataSource=" + dataSourceName + ",");
		buffer.append("Query=" + queryString + ",");
		buffer.append("Time=" + executeTime + ",");
		if (errMessage != null)
			buffer.append("Error=" + errMessage + ",");
		buffer.append("Result=" + super.toString());
		buffer.append("}");

		return buffer.toString();
	}

	protected String getExtraInfo(boolean _isLong) {
		StringBuilder buffer = new StringBuilder(512);
		String colspan = _isLong ? ("colspan='" + getNoColumns() + "' ") : "";

		buffer.append("<TR><TD class='cfdump_td_query'>Datasource:</TD><TD class='cfdump_td_value'");
		buffer.append(colspan);
		buffer.append(">");
		buffer.append(dataSourceName);
		buffer.append("</TD></TR><TR><TD class='cfdump_td_query'>Query:</TD><TD class='cfdump_td_value'");
		buffer.append(colspan);
		buffer.append(">");
		buffer.append(queryString);
		buffer.append("</TD></TR><TR><TD class='cfdump_td_query'>Time:</TD><TD class='cfdump_td_value'");
		buffer.append(colspan);
		buffer.append(">");
		buffer.append(executeTime);
		buffer.append(" ms</TD></TR>");

		if (preparedDataList != null && preparedDataList.size() > 0) {
			buffer.append("<TR><TD class='cfdump_td_query'>CFQUERYPARAMS:</TD><TD class='cfdump_td_value'");
			buffer.append(colspan);
			buffer.append("><ol>");

			Iterator<preparedData> it = preparedDataList.iterator();
			while (it.hasNext()) {
				preparedData pData = it.next();
				buffer.append("<li>");
				buffer.append(pData.toString());
				buffer.append("</li>");
			}
			buffer.append("</ol></TD></TR>");
		}

		if (errMessage != null)
			buffer.append("<TR><TD class='cfdump_td_query'>Error:</TD><TD class='cfdump_td_value' colspan='" + getNoColumns() + ">" + errMessage + "</TD></TR>");

		return buffer.toString();
	}

	public void setCacheData(String region, long timeOut, String _cacheName) {
		usingCache 		= true;
		cacheTimeOut	= timeOut;
		cacheName 		= _cacheName;
		cacheRegion		= region;
	}

	private void loadDataFromCache(cfSession _Session) throws cfmRunTimeException {
		/* 
		 * This method was called if the caching has been enabled
		 * Attempt to retrieve the query from the cache Calculate the CACHE name; 
		 * this can be given by the TAG if the
		 * developer wishes to use their own
		 */
		if (cacheName != null)
			internalCacheName = cacheName;
		else
			internalCacheName = CacheFactory.createCacheKey(queryString + ((preparedDataList != null) ? preparedDataList.hashCode() : 0) + thisDataSource.getUsername() + thisDataSource.getPassword() + thisDataSource.getDataSourceName());

		// Attempt to retrieve the query from the cache
		CacheInterface cacheEngine = CacheFactory.getCacheEngine(cacheRegion);

		if (cacheTimeOut == 0)
			cacheEngine.delete(internalCacheName, false);

		cfData cfdata = cacheEngine.get(internalCacheName);
		if (cfdata != null && cfdata.getDataType() != cfData.CFQUERYRESULTDATA)
			throw new cfmRunTimeException(_Session, new Exception("Cached Data is not a QUERY object"));

		cfSQLQueryData newQuery = (cfSQLQueryData) cfdata;

		if (newQuery != null) {
			queryRun 		= true;
			resultSet 	= true;
			setQueryData(newQuery, "SQLCacheRegion: " + cacheRegion);
			usingCache 	= newQuery.usingCache;
		} else {

			// Get a lock just for this entry
			synchronized (CacheFactory.getLock(internalCacheName)) {
				try {

					// we need to check the cache again since another thread might have set this entry
					newQuery = (cfSQLQueryData)cacheEngine.get(internalCacheName);

					if (newQuery != null) {
						queryRun 	= true;
						resultSet = true;
						setQueryData(newQuery, "SQLCacheRegion: " + cacheRegion);
						usingCache = newQuery.usingCache;
					} else {
						
						// The query was not found in the cache, so lets run it
						setQuerySource("SQLCacheRegion: " + cacheRegion + "; Refresh" );

						execute(_Session); 

						if (resultSet)
							cacheEngine.set(internalCacheName, this, cacheTimeOut, cacheTimeOut);
					}

				} finally {
					// Be sure to remove the lock for this entry
					CacheFactory.removeLock(internalCacheName);
				}
			}
		}
	}
}
