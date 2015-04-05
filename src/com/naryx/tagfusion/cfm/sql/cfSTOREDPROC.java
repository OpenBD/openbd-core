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
 *  $Id: cfSTOREDPROC.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.sql;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;


public class cfSTOREDPROC extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

	public static final String DATA_BIN_KEY = "CFSTOREDPROC_DATA";

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute("RETURNCODE", "NO");
		parseTagHeader(_tag);

		if (!containsAttribute("PROCEDURE"))
			throw newBadFileException("Missing Attribute", "You need to provide a PROCEDURE for the query");

		if (containsAttribute("DBTYPE")) {
			if (!getConstant("DBTYPE").equalsIgnoreCase("dynamic"))
				throw newBadFileException("Invalid Attribute", "Only a DBTYPE of dynamic is supported");
			if (!containsAttribute("CONNECTSTRING"))
				throw newBadFileException("Missing Attribute", "When a DBTYPE of dynamic is specified, you must provide a CONNECTSTRING");
		} else {
			if (containsAttribute("CONNECTSTRING"))
				throw newBadFileException("Invalid Attribute", "CONNECTSTRING is only valid when DBTYPE is dynamic");
		}
	}

	public String getEndMarker() {
		return "</CFSTOREDPROC>";
	}

	
	/**
	 * Gets the datasource value from first the TAG attribute, and if that does not exist, then from the application data
	 * @param tag
	 * @param _Session
	 * @return
	 * @throws cfmRunTimeException
	 */
	private String	getDataSourceValue( cfTag tag, cfSession _Session ) throws cfmRunTimeException {
		
		if ( tag.containsAttribute("DATASOURCE") )
			return tag.getDynamic(_Session, "DATASOURCE").getString();
		else{
			cfApplicationData appData = _Session.getApplicationData();
			if ( appData != null )
				return appData.getDataSource();
		}
		
		return null;
	}
	
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {

		// Create the cfDataSource
		cfDataSource dataSource = null;
		String procName = getDynamic(_Session, "PROCEDURE").getString();

		String datasourceName = getDataSourceValue( this, _Session );
		if ( datasourceName != null ) {
			dataSource = new cfDataSource(datasourceName, _Session);
		} else {
			datasourceName = "DYNAMIC";
			dataSource = new cfDynamicDataSource(datasourceName, _Session, getDynamic(_Session, "CONNECTSTRING").getString());
		}

		if (!dataSource.isSql_storedprocedures()) {
			throw new cfmRunTimeException(catchDataFactory.generalException(cfCatchData.TYPE_DATABASE, "errorCode.sqlError", "sql.storedProcedure",	new String[] { datasourceName }));
		}

		if (containsAttribute("USERNAME"))
			dataSource.setUsername(getDynamic(_Session, "USERNAME").getString());

		if (containsAttribute("PASSWORD"))
			dataSource.setPassword(getDynamic(_Session, "PASSWORD").getString());

		// Get the java.sql.Connection object
		Connection con = null;
		CallableStatement call = null;
		String callString = null;

		try {
			con = setupDataConnection(dataSource);
			boolean bSupportStored = false;
			try {
				bSupportStored = con.getMetaData().supportsStoredProcedures();
			} catch (Exception e) {}

			if (!bSupportStored) {
				throw newRunTimeException("The datasource, " + datasourceName + ", does not support stored procedures");
			}

			
			// Set the internal data structure and render all the internal tags
			cfStoredProcData storedProcData = new cfStoredProcData(con);
			_Session.setDataBin(DATA_BIN_KEY, storedProcData);

			renderToString(_Session);

			// Determine if we should use named parameters for this stored procedure
			// call NOTE: this method will set a flag in each preparedData object too.
			boolean useNamedParameters = storedProcData.setUseNamedParameters();
			boolean returnCode = getDynamic(_Session, "RETURNCODE").getBoolean();

			// Get the java.sql.CallableStatement object
			callString = "{ " + (returnCode ? "? = " : "") + "call " + procName + storedProcData.getQueryString() + " }";
			call = setUpStoredProcedure(datasourceName, callString, procName, con, returnCode, useNamedParameters);
			setParams(datasourceName, callString, call, returnCode, storedProcData.iterator(), con);

			long start = System.currentTimeMillis();
			call.execute();
			long execTime = System.currentTimeMillis() - start;

			_Session.getDebugRecorder().execStoredProc( datasourceName, callString, procName, execTime );
			
			_Session.metricQueryTimeAdd(execTime);

			// get the results; must be done BEFORE return code and out variables
			// are read otherwise we won't be able to read them with certain drivers (ie. Oracle)
			storedProcData.retrieveAndStoreResultSets(_Session, datasourceName, call);

			// set cfstoredproc.executionTime
			cfStructData returnData = new cfStructData();
			returnData.setData("executiontime", new cfNumberData(execTime));

			// get the return code
			if (returnCode) {
				try {
					returnData.setData("statuscode", new cfNumberData(call.getInt(1)));
				} catch (SQLException ignored) {
					returnData.setData("statuscode", new cfNumberData(0));	
				}
			}

			if (containsAttribute("RESULT")) {
				_Session.setData(getDynamic(_Session, "RESULT").getString(), returnData);
			} else {
				_Session.setData("cfstoredproc", returnData);
			}

			// get out variables
			retrieveOutVariables(_Session, callString, datasourceName, call, returnCode, storedProcData.iterator());

			// Tracer details
			querySlowLog.record( this, procName, execTime );
			
			// Debug recording
			if (_Session.getShowDBActivity()) {
				if (!containsAttribute("DEBUG") || getDynamic(_Session, "DEBUG").getString().equals("") || getDynamic(_Session, "DEBUG").getBoolean()) {
					_Session.storedProcRan(getFile(), datasourceName, procName, execTime, storedProcData.getParams(), storedProcData.getResults());
				}
			} else if (containsAttribute("DEBUG") && (getDynamic(_Session, "DEBUG").getString().equals("") || getDynamic(_Session, "DEBUG").getBoolean())) {
				_Session.storedProcRan(getFile(), datasourceName, procName, execTime, storedProcData.getParams(), storedProcData.getResults());
			}

		} catch (SQLException e) {
			throw new cfmRunTimeException(catchDataFactory.databaseException(datasourceName, "sql.storedProcedureExecute", null, callString, e));
		} finally {
			// Close off the connections
			_Session.deleteDataBin(DATA_BIN_KEY);
			closeConnections(con, call, dataSource);
		}

		return cfTagReturnType.NORMAL;
	}

	private static Connection setupDataConnection(cfDataSource thisDataSource) throws cfmRunTimeException {
		try {
			return thisDataSource.takeConnection();
		} catch (SQLException e) {
			throw new cfmRunTimeException(catchDataFactory.databaseException(thisDataSource.getDataSourceName(), "sql.connecting",
					new String[] { com.naryx.tagfusion.cfm.tag.tagUtils.trimError(e.getMessage()) }, "", e));
		}
	}

	private static void closeConnections(Connection dataConnection, PreparedStatement Statmt, cfDataSource thisDataSource) {
		if (dataConnection != null) {
			try {
				Statmt.close();
			} catch (Exception ignoreException) {}
			thisDataSource.returnConnection(dataConnection);
		}
	}

	private CallableStatement setUpStoredProcedure(String _datasourceName, String callString, String _procName, Connection _con, boolean returnCode, boolean useNamedParameters) throws cfmRunTimeException {
		try {
			CallableStatement stmt = _con.prepareCall(callString);
			if (returnCode) {
				if (useNamedParameters) {
					// Using "@RETURN_VALUE" for the return value is specific to SQL Server.
					// For other databases we may need to use a different value.
					stmt.registerOutParameter("RETURN_VALUE", java.sql.Types.INTEGER);
				} else {
					stmt.registerOutParameter(1, java.sql.Types.INTEGER);
				}
			}
			return stmt;
		} catch (SQLException e) {
			throw new cfmRunTimeException(catchDataFactory.databaseException(_datasourceName, "sql.storedProcedureSetup", new String[] { _procName }, callString, e));
		}
	}

	private boolean retrieveOutVariables(cfSession _Session, String callString, String _datasourceName, CallableStatement _stmt, boolean returnCode, Iterator<preparedData> iter)throws cfmRunTimeException {
		try {
			int counter = (returnCode ? 2 : 1);
			while (iter.hasNext())
				iter.next().retrieveOutVariables(counter++, _Session, _stmt);

			return true;
		} catch (SQLException e) {
			throw new cfmRunTimeException(catchDataFactory.databaseException(_datasourceName, "sql.storedProcedureOUT", null, callString, e));
		}
	}

	private void setParams(String _datasourceName, String callString, CallableStatement _call, boolean returnCode, Iterator<preparedData> iter, Connection _con)
			throws cfmRunTimeException {
		try {
			int counter = (returnCode ? 2 : 1);
			while (iter.hasNext())
				counter = iter.next().prepareStatement(counter, _call, _con);

		} catch (SQLException e) {
			throw new cfmRunTimeException(catchDataFactory.databaseException(_datasourceName, "sql.storedProcedureParams", null, callString, e));
		}
	}
}
