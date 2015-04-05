/* 
 *  Copyright (C) 2013 TagServlet Ltd
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
 *  $Id: ProfileSession.java 2378 2013-06-10 23:08:14Z alan $
 */
package com.bluedragon.profiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfFormData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfUrlData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.sql.cfSQLQueryData;
import com.naryx.tagfusion.cfm.sql.preparedData;
import com.naryx.tagfusion.cfm.sql.resultSetHolder;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.tagUtils;
import com.naryx.tagfusion.util.debugRecorder;

public class ProfileSession extends Object implements debugRecorder {
	
	public static String	SESSION_DATA_BIN_PROFILE = "profile-data";

	private cfSession session;
	private long startTime;
	private int 	countQuery = 0, countMongo = 0, countException = 0;
	private List<Map>	listMap;

	
	public ProfileSession( cfSession session ){
		this.session 		= session;
		this.startTime	= System.currentTimeMillis();
		this.listMap		= new ArrayList<Map>();
		this.session.registerDebugRecorder( this );
	}

	public void startRequest() {}
	
	public void startFile(cfFile thisFile) {}
	public void setShow(boolean _show) {}
	public boolean getShow() {return false;}
	public boolean getShowDBActivity() {return false;}
	public void recordTracepoint(String tracePoint) {}
	public void recordTimer(String timing) {}
	public debugRecorder copy() {return this;}
	public void dump(cfSession session) {}
	public void execOnStart(cfData sqlData) {}
	
	public void endFile(cfFile thisFile) {}
	public void storedProcRan(String _template, String _datasrc, String _procName, long _execTime, List<preparedData> _params, List<resultSetHolder> _results) {}
	public void queryRan(String template, String qname, cfSQLQueryData query, List<preparedData> _qp) {}
	public void updateRan(String template, String datasrc, String sql) {}
	public void insertRan(String template, String datasrc, String sql) {}

	@Override
	public void exceptionThrown(cfmRunTimeException exception, cfFile f, cfTag t) {
		Map<String,Object>	m = new HashMap<String,Object>();
		cfCatchData catchdata = exception.getCatchData();
		m.put("exception", catchdata.getMessage() + "; " + catchdata.getDetail() );
		m.put("file", f.getURI() );
		listMap.add(m);
		countException++;
	}
	
	public void execOnEnd(cfData sqlData) {
		countQuery++;
		Map<String,Object>	m = new HashMap<String,Object>();
		
		if ( sqlData instanceof cfSQLQueryData ){
			cfSQLQueryData	sqd	= (cfSQLQueryData)sqlData;

			m.put("ds", 	sqd.getDataSourceName() );
			m.put("sql", 	sqd.queryString );
			m.put("ms",		sqd.executeTime );
			
			if ( sqd.getNoRows() > 0 )
				m.put("rows",	sqd.getNoRows() );
			
			listMap.add(m);
		}
	}
	
	@Override
	public void endRequest() {
		Map<String,Object>	requestM = new HashMap<String,Object>();

		cfFormData	form = (cfFormData)session.getQualifiedData( variableStore.FORM_SCOPE );
		if ( form.size() > 0 ){
			Map frm = (Map)tagUtils.getNatural(form, true);
			frm.remove("FIELDNAMES");
			requestM.put("form", frm);
		}
		
		if ( !cfEngine.isFormUrlScopeCombined() ){
			cfUrlData	url = (cfUrlData)session.getQualifiedData( variableStore.URL_SCOPE );
			if ( url.size() > 0 )
				requestM.put("url", tagUtils.getNatural(url, true));
		}
		
		// Is there an extra data
		cfData extraData	= (cfData)session.getDataBin(SESSION_DATA_BIN_PROFILE);
		if ( extraData != null )
			requestM.put("_extra", tagUtils.getNatural(extraData, true) );
				
		requestM.put("ms", 			(System.currentTimeMillis() - startTime) );
		requestM.put("bytes", 	session.getBytesSent() );
		requestM.put("uri",			session.REQ.getContextPath() + session.REQ.getServletPath() );

		// ip address
		requestM.put("ip", session.REQ.getRemoteAddr() );
		
		String xforwardedfor = session.REQ.getHeader("x-forwarded-for");
		if ( xforwardedfor != null && !xforwardedfor.isEmpty() )
			requestM.put("xip", xforwardedfor );

		// query data
		if ( session.getMetricQueryTotalTime() > 0 )
			requestM.put("msquery",	session.getMetricQueryTotalTime() );
		if ( countQuery > 0 )
			requestM.put("noquery", countQuery );
		if ( countMongo > 0 )
			requestM.put("nomongo", countMongo );
		if ( countException > 0 )
			requestM.put("noexc", 	countException );

		String query = cfUrlData.getQueryString(session);
		if ( query != null )
			requestM.put("query", query );
		
		if ( listMap.size() > 0 )
			requestM.put("io", listMap );

		ProfilerExtension.log(requestM);
	}

	@Override
	public void execStoredProc(String datasourceName, String callString, String procName, long execTime) {
		Map<String,Object>	m = new HashMap<String,Object>();
		
		m.put("ms", 		execTime );
		m.put("call",		callString );
		m.put("proc", 	procName );
		m.put("ds",			datasourceName );

		listMap.add(m);
	}
	
	public void execMongo( DBCollection col, String action, DBObject qry, long execTime ){
		Map<String,Object>	m = new HashMap<String,Object>();
		
		m.put("ms", 		execTime );
		m.put("action",	action );
		m.put("col", 		col.getName() );
		m.put("db",			col.getDB().getName() );

		if ( qry != null )
			m.put("qry", qry.toString() );

		listMap.add(m);
	}
	
}