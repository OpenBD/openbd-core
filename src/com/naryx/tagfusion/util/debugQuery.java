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
 *  $Id: $
 */

package com.naryx.tagfusion.util;

/**
 * This class is for collating and dumping all the information
 * related to queries as recorded when there is debug output enabled
 *
 */

import java.util.ArrayList;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.sql.cfSQLQueryData;
import com.naryx.tagfusion.cfm.sql.preparedData;

class debugQuery {

	private List<queryInfo>	queries;



	public debugQuery() {
		queries = new ArrayList<queryInfo>();
	}



	public boolean hasQueries() {
		return queries.size() > 0;
	}



	/**
	 * adds the given query with the name _qn sourced from the template _t
	 * to the list of queries, extracting all the relevent data
	 */

	public void addQuery(String _qn, String _t, cfSQLQueryData _q, List<preparedData> _qp) {
		queryInfo info = new queryInfo();
		info.timeStamp = System.currentTimeMillis();
		info.queryName = _qn;
		info.execTime = _q.getExecuteTime();
		info.datasource = _q.getDataSourceName();
		info.template = _t;

		info.sql = _q.getQueryString();
		info.cached = _q.getCacheUsed();
		info.records = _q.getNoRows();
		info.params = _qp;

		queries.add(info);

	}// addQuery()



	public void addUpdate(String _template, String _datasrc, String _sql) {
		addCommon("CFUPDATE", _template, _datasrc, _sql);
	}



	public void addInsert(String _template, String _datasrc, String _sql) {
		addCommon("CFINSERT", _template, _datasrc, _sql);
	}



	private void addCommon(String _type, String _template, String _datasrc, String _sql) {
		queryInfo info = new queryInfo();
		info.timeStamp = System.currentTimeMillis();
		info.queryName = _type;
		info.datasource = _datasrc;
		info.template = _template;
		info.sql = _sql;

		queries.add(info);

	}



	public void dump(cfSession session) {
		String fontSize = (session.isWindowsOrMacUser() ? "small" : "medium");
		if (queries.size() > 0) {
			session.write("<style type=\"text/css\">\n");
			session.write(".queryname\n{");
			session.write("    color:black;\n");
			session.write("    background-color:white;\n");
			session.write("    font-family:\"courier\", arial, serif;\n");
			session.write("    font-size: " + fontSize + ";\n}\n");
			session.write(".querydetails\n{");
			session.write("    color:black;\n");
			session.write("    background-color:white;\n");
			session.write("    font-family:\"Times New Roman\", Times, serif;\n");
			session.write("    font-size: " + fontSize + ";\n}\n\n");
			session.write("</style>\n\n");

			session.write("<hr/><p><b><div class=\"debughdr\">SQL Queries</div></B></p>");

			queryInfo next;
			for (int i = 0; i < queries.size(); i++) {
				next = (queryInfo) queries.get(i);
				session.write("<p><div class=\"queryname\"><b>");
				session.write(next.queryName);
				session.write("</b></div><div class=\"querydetails\"> (Datasource=");
				session.write((next.datasource != null) ? next.datasource : "dbQuery");
				session.write(", Time=");
				session.write(String.valueOf(next.execTime));
				session.write("ms, Records=");
				session.write(String.valueOf(next.records));
				if (next.cached) {
					session.write(", Cached Query");
				}
				session.write(") in ");
				session.write(next.template);
				session.write(" @ ");
				session.write(com.nary.util.Date.formatDate(next.timeStamp, "HH:mm:ss"));
				session.write("</p><p align=center><pre>");
				session.write(next.sql);
				session.write("</pre></p>");

				List<preparedData> qParams = next.params;
				if (qParams != null) {
					session.write("Query Parameter value(s):<BR>\n");
					for (int j = 0; j < qParams.size(); j++) {
						preparedData nextParam = qParams.get(j);
						session.write("Parameter #");
						session.write(String.valueOf(j + 1));
						session.write(" Type: ");
						session.write(nextParam.getSQLType());
						session.write(" Value: ");
						try {
							session.write(nextParam.getDataAsString());
						} catch (dataNotSupportedException ignored) {
						}
						session.write("<br>\n");
					}
				}
				session.write("</div>\n\n");
			}

		}

	}

	class queryInfo {

		public String							queryName;
		public String							datasource;
		public String							sql;
		public String							template;

		public List<preparedData>	params	= null;

		public long								execTime;
		public int								records	= 0;
		public long								timeStamp;

		public boolean						cached	= false;

	}

}
