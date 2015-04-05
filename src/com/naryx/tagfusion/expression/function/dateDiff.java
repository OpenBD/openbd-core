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
 *  http://www.openbluedragon.org/
 *  $Id: $
 */

package com.naryx.tagfusion.expression.function;

import java.util.Calendar;
import java.util.TimeZone;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class dateDiff extends functionBase {

	private static final long serialVersionUID = 1L;

	public dateDiff() {
		min = max = 3;
		setNamedParams( new String[]{ "datepart", "date1", "date2" } );
	}

	public String[] getParamInfo() {
		return new String[] { "date part ('yyyy' year, 'q' quarter, 'm' month, 'y' day of year,'d' days, 'w' week day, 'ww' week of year, 'h' hours, 'n' minutes, 's' seconds, 'l' milliseconds)", "date1", "date2" };
	}

	public java.util.Map getInfo() {
		return makeInfo("date", "Determines the number of given date parts between two different dates", ReturnType.NUMERIC);
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		cfDateData date1, date2;
		String datepart;

		date2 		= getNamedParam(argStruct, "date2").getDateData();
		date1 		= getNamedParam(argStruct, "date1").getDateData();
		datepart 	= getNamedStringParam(argStruct, "datepart", null);
		
		return getDiff( date1, date2, datepart );
	}
		
		
	public static cfNumberData getDiff( cfDateData date1, cfDateData date2, String datepart ){
		long date1L 		= date1.getLong();
		long date2L 		= date2.getLong();
		
		long timeDiff = date2L - date1L;

		// Calculate the difference depending on the input string
		int result = 0;
		if (datepart.equalsIgnoreCase("yyyy")) {
			result = (int) ((long) (timeDiff) / (1000L * 60L * 60L * 24L * 365L));
		} else if (datepart.equalsIgnoreCase("q")) {
			result = (int) ((long) timeDiff / (1000L * 60L * 60L * 24L * 31L * 3L));
		} else if (datepart.equalsIgnoreCase("m")) {
			/* Special case since months are not of equal length */
			Calendar date1Cal = date1.getCalendar();
			Calendar date2Cal = date2.getCalendar();
			int yr1 = date1Cal.get(Calendar.YEAR);
			int yr2 = date2Cal.get(Calendar.YEAR);
			int mth1 = date1Cal.get(Calendar.MONTH);
			int mth2 = date2Cal.get(Calendar.MONTH);
			int day1 = date1Cal.get(Calendar.DAY_OF_MONTH);
			int day2 = date2Cal.get(Calendar.DAY_OF_MONTH);
			result = ((yr2 - yr1) * 12) + (mth2 - mth1) - (day2 < day1 ? 1 : 0);
		} else if (datepart.equalsIgnoreCase("y")) {
			result = (int) ((long) timeDiff / (1000L * 60L * 60L * 24L));
		} else if (datepart.equalsIgnoreCase("d")) {
			// Convert to UTC time (this is the fix for bug NA#2769)
			date1L = date1L + TimeZone.getDefault().getOffset(date1L);
			date2L = date2L + TimeZone.getDefault().getOffset(date2L);
			timeDiff = date2L - date1L;
			result = (int) ((long) timeDiff / (1000L * 60L * 60L * 24L));
		} else if (datepart.equalsIgnoreCase("w")) {
			result = (int) ((long) timeDiff / (1000L * 60L * 60L * 24L * 7L));
		} else if (datepart.equalsIgnoreCase("ww")) {
			result = (int) ((long) timeDiff / (1000L * 60L * 60L * 24L * 7L));
		} else if (datepart.equalsIgnoreCase("h")) {
			result = (int) ((long) timeDiff / (1000L * 60L * 60L));
		} else if (datepart.equalsIgnoreCase("n")) {
			result = (int) ((long) timeDiff / (1000L * 60L));
		} else if (datepart.equalsIgnoreCase("s")) {
			result = (int) ((long) timeDiff / 1000L);
		}

		return new cfNumberData(result);
	}
}
