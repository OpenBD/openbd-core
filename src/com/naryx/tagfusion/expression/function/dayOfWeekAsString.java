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

import java.text.DateFormatSymbols;
import java.util.Calendar;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class dayOfWeekAsString extends functionBase {

	private static final long serialVersionUID = 1L;

	public dayOfWeekAsString() {
		min = max = 1;
		setNamedParams( new String[]{ "dayOfWeek" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"day of week (1-7)"	
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"date", 
				"Returns the string representation of the given day index", 
				ReturnType.STRING );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		int weekDay = getNamedIntParam(argStruct, "dayOfWeek", 1);
		if (weekDay < 1 || weekDay > 7)
			throwException(_session, "Day must be within 1 and 7");

		DateFormatSymbols dfs = new DateFormatSymbols(_session.getLocale());
		String[] weekdays = dfs.getWeekdays();

		String weekDayString = "";
		switch (weekDay) {
		case 0:
			weekDayString = weekdays[Calendar.SATURDAY];
			break;
		case 1:
			weekDayString = weekdays[Calendar.SUNDAY];
			break;
		case 2:
			weekDayString = weekdays[Calendar.MONDAY];
			break;
		case 3:
			weekDayString = weekdays[Calendar.TUESDAY];
			break;
		case 4:
			weekDayString = weekdays[Calendar.WEDNESDAY];
			break;
		case 5:
			weekDayString = weekdays[Calendar.THURSDAY];
			break;
		case 6:
			weekDayString = weekdays[Calendar.FRIDAY];
			break;
		case 7:
			weekDayString = weekdays[Calendar.SATURDAY];
			break;
		default:
			throw new IllegalStateException("invalid week day - " + weekDay);
		}

		return new cfStringData(weekDayString);
	}
}
