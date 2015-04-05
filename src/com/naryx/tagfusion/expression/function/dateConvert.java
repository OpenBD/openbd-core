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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class dateConvert extends functionBase {

	private static final long serialVersionUID = 1L;

	public dateConvert() {
		min = max = 2;
		setNamedParams( new String[]{ "conversion", "date" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"conversion ('local2utc' or 'utc2local')",
			"date object"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"date", 
				"Converts a date to the given locale", 
				ReturnType.DATE );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		String conversionType = getNamedStringParam(argStruct, "conversion", null);
		Calendar DD = getNamedParam(argStruct, "date").getDateData().getCalendar();
		Calendar c = Calendar.getInstance();

		TimeZone tz = (TimeZone) TimeZone.getDefault().clone();
		long utcDiff = tz.getRawOffset();
		long dstDiff = tz.useDaylightTime() && tz.inDaylightTime(DD.getTime()) ? tz.getDSTSavings() : 0;
		long offset = utcDiff + dstDiff; // the computing and use of offset is what
																			// fixes bug #1449

		if (conversionType.equalsIgnoreCase("local2Utc")) {
			long time = DD.getTime().getTime() - offset;
			Date convertedDate = new Date(time);
			c.setTime(convertedDate);
			c.setTimeZone(TimeZone.getTimeZone("UTC"));
		} else if (conversionType.equalsIgnoreCase("utc2Local")) {
			long time = DD.getTime().getTime() + offset;
			Date convertedDate = new Date(time);
			c.setTime(convertedDate);
			c.setTimeZone(tz);
		}

		long time = c.getTime().getTime();
		cfDateData result = new cfDateData(time);
		result.setString("{ts '" + com.nary.util.Date.formatDate(time, "yyyy-MM-dd HH:mm:ss") + "'}");
		return result;
	}
}
