/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: queryOfQueryRun.java 2210 2012-07-26 23:51:20Z alan $
 */


package com.naryx.tagfusion.cfm.sql;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.queryofqueries.cfQofQueryResultData;

public class queryOfQueryRun extends queryRun {
	private static final long serialVersionUID = 1L;

	public queryOfQueryRun() {
		min = 1;
		max = 2;
		setNamedParams( new String[]{ "sql", "params" } );
	}

	public String[] getParamInfo() {
		return new String[] { 
				"SQL string", 
				"array of structures {value, padding, scale, maxlength, separator, list, defaultlist, nullvalue, cfsqltype} representing the attributes of CFQUERYPARAM; one for each ? within the SQL string" 
			};
	}

	public java.util.Map<String,String> getInfo() {
		return makeInfo("query", "Executes a Query-of-Query against a previous SQL result sets.  Function version of CFQUERY", ReturnType.QUERY);
	}

	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		String sql = getNamedStringParam( argStruct, "sql", null );
		if ( sql == null )
			throwException(_session,"provide sql parameter");
		
		cfData paramData = getNamedParam( argStruct, "params", null );
	
		cfQofQueryResultData queryData = new cfQofQueryResultData();

		if (paramData != null) {
			if (paramData.getDataType() == cfData.CFARRAYDATA) {
				sql	= prepareSQL( sql, prepareParams(_session, queryData, (cfArrayData) paramData) );
			} else
				throwException(_session, "params must be an array of structures");
		}

		// Perform the query execution
		queryData.setQueryString(sql);
		queryData.execute(_session);

		return queryData;
	}
}