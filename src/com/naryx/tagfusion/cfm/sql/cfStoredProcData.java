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
 *  
 *  $Id: cfStoredProcData.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.sql;

/**
 * Works exclusively with the cfSTOREDPROC and cfPROCPARAM class.
 */

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfStoredProcData extends Object {

	private Connection		con;
	List<preparedData>		params;
	List<resultSetHolder>	results;
	int										maxRows	= -1;



	public cfStoredProcData(Connection con) {
		this.con = con;
		params = new ArrayList<preparedData>();
		results = new ArrayList<resultSetHolder>();
	}



	public boolean resultsExpected() {
		return (results.size() > 0);
	}



	public Iterator<preparedData> iterator() {
		return params.iterator();
	}



	public List<preparedData> getParams() {
		return params;
	}



	public List<resultSetHolder> getResults() {
		return results;
	}



	public void addPreparedData(preparedData _data) {
		// If it's an Oracle cursor output parameter then check if we need to also
		// register it as a result set
		if ((_data.isOUT()) && (_data.getCfSqlType() == preparedData.CF_SQL_REFCURSOR)) {
			// With the JDBC-ODBC bridge the result set is returned as a result set
			// instead of as an output parameter so we need to register it as a result set too.
			// NOTE: We still need to register it as a parameter too so the stored procedure will be
			// passed the correct number of parameters and in the correct order.
			// NOTE: This check needs to check a value that will also work when a J2EE data source is used.
			// That's why the DBMD.getDriverName() method is being used.
			String driverName = null;
			try {
				driverName = con.getMetaData().getDriverName();
			} catch (SQLException e) {
				// ignore
			}
			if ((driverName != null) && driverName.startsWith("JDBC-ODBC Bridge")) {
				// Need to also add it as a result set
				results.add(new resultSetHolder(_data.getOutVariable(), results.size() + 1, -1));

				// Now change the name of the output parameter so it won't conflict with the result set
				_data.setOutVariable(_data.getOutVariable() + "_dummy_out_param");
			}
		}

		params.add(_data);
	}



	public void setResultSet(String name, int index, int maxRows) {
		results.add(new resultSetHolder(name, index, maxRows));
	}



	public String getQueryString() {
		String paramString = "";

		if (params.size() > 0) {
			paramString = "(";

			for (int i = 0; i < params.size(); i++) {
				if (i < params.size() - 1)
					paramString += "?,";
				else
					paramString += "?";
			}
			paramString += ")";
		} else {
			try {
				String driverName = con.getMetaData().getDriverName();
				if (driverName.equals("MySQL-AB JDBC Driver"))
					paramString += "()";
			} catch (SQLException sqlExc) {
			}
		}

		return paramString;
	}



	public boolean setUseNamedParameters() {
		// If there are no parameters then just return false
		if (params.size() == 0)
			return false;

		// Check if all of the parameters contain a paramName (ie. DBVARNAME)
		boolean useNamedParameters = true;
		for (int i = 0; i < params.size(); i++) {
			preparedData pData = params.get(i);
			if (pData.paramName == null) {
				useNamedParameters = false;
				break;
			}
		}

		// if all of the parameters contain a paramName (ie. DBVARNAME)
		// then check if the driver supports named parameters
		if (useNamedParameters)
			useNamedParameters = preparedData.supportsNamedParameters(con);

		// Set the useNamedParameters flag in each preparedData object
		for (int i = 0; i < params.size(); i++) {
			preparedData pData = params.get(i);
			pData.setUseNamedParameters(useNamedParameters);
		}

		return useNamedParameters;
	}



	// -----------------------------------------

	public void retrieveAndStoreResultSets(cfSession Session, String datasourceName, CallableStatement call) throws cfmRunTimeException {
		// the Oracle 10g JDBC driver has a bug: call.getUpdateCount() always
		// returns a positive number, instead of -1 when there are no more
		// results; this bug puts the following code into an infinite loop;
		// just return because Oracle returns result sets as output parameters
		// anyway (there are never any result sets returned here)
		if (call.getClass().getName().equals("oracle.jdbc.driver.T4CCallableStatement"))
			return;

		try {
			int currentResultSet = 1;

			do { // there may be multiple result sets and/or update counts
				ResultSet rs = call.getResultSet();
				if (rs != null) {
					resultSetHolder rsh = getResultSetHolder(currentResultSet);
					if (rsh != null) {
						Session.setData(rsh.name, new cfQueryResultData(rs, "Stored Procedure", rsh.maxRows));
					}
					currentResultSet++; // moved this out of the above 'if' clause to fix bug #2376
					rs.close();
				}
			} while (call.getMoreResults() || (call.getUpdateCount() >= 0));

		} catch (SQLException e) {
			throw new cfmRunTimeException(catchDataFactory.databaseException(datasourceName, "sql.storedProcedureResult", null, "", e));
		}
	}



	// --------------------------------------------------------------

	private resultSetHolder getResultSetHolder(int indx) {
		Iterator<resultSetHolder> iter = results.iterator();
		resultSetHolder rsh;
		while (iter.hasNext()) {
			rsh = iter.next();
			if (rsh.resultIndex == indx)
				return rsh;
		}
		return null;
	}

}
