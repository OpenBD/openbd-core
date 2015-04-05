/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

/**
 * Author: Alan Williamson
 * Created on 02-Sep-2004
 * 
 * BlueDragon only Expression
 * 
 * QuerySort( query, sort_column, sort_type, [, sort_order ] )
 */

package com.naryx.tagfusion.expression.function.query;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class queryRequestMetrics extends functionBase {

	private static final long serialVersionUID = 1L;

	public queryRequestMetrics() { min = max = 0; }
	
	public java.util.Map getInfo(){
		return makeInfo(
				"query", 
				"Returns a structure {total,time} which indicates the total number of queries and the total time taken by them up to this point in this given request.  Used for statisical tracking.", 
				ReturnType.STRUCTURE );
	}
 
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		cfStructData	s = new cfStructData();
		s.setData("total", 	new cfNumberData(_session.getMetricQuery()) );
		s.setData("time", 	new cfNumberData(_session.getMetricQueryTotalTime()) );
		return s;
	}
}
