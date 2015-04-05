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

package com.naryx.tagfusion.expression.function;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class getTimeZoneInfo extends functionBase {

	private static final long serialVersionUID = 1L;

	public getTimeZoneInfo() {
		min = max = 0;
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"engine", 
				"Returns back a structure regarding the current timezone; utctotaloffset, utchouroffset, utcminuteoffset, isdston", 
				ReturnType.STRUCTURE );
	}
  	
	
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		cfStructData timeInfo = new cfStructData();
		TimeZone tz = (TimeZone) TimeZone.getDefault().clone();
		int dstCompensation = 0;

		boolean dst = tz.inDaylightTime(new Date());

		if (dst)
			dstCompensation = tz.getDSTSavings(); // the # of milliseconds in an hour.

		long offset = tz.getRawOffset() + dstCompensation;

		offset = offset * -1; // cfmx livedocs for this function mandate that the
													// sign be this way (which happens to be opposite of
													// what Java does)

		long totalOffSet = offset / 1000L;
		long hour = offset / (1000L * 60L * 60L);
		long minutes = offset / (1000L * 60L);
		long partialHourAsMinutes = minutes % 60; // to remove all whole hours

		timeInfo.setData("utctotaloffset", new cfNumberData(totalOffSet));
		timeInfo.setData("utchouroffset", new cfNumberData(hour));
		timeInfo.setData("utcminuteoffset", new cfNumberData(partialHourAsMinutes));
		timeInfo.setData("isdston", cfBooleanData.getcfBooleanData(dst));

		return timeInfo;
	}
}
