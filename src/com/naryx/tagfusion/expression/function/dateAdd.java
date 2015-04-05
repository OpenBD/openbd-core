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

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class dateAdd extends functionBase {

	private static final long serialVersionUID = 1L;

	public dateAdd() {
		min = max = 3;
		setNamedParams( new String[]{ "datepart", "period", "date" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"date part ('yyyy' year, 'q' quarter, 'm' month, 'y' day of year, 'd' days, 'w' week day, 'ww' week of year, 'h' hours, 'n' minutes, 's' seconds, 'l' milliseconds)",
			"period",
			"date object"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"date", 
				"Adds/Subtracts date units from a given date", 
				ReturnType.DATE );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		Calendar DD = getNamedParam( argStruct, "date" ).getDateData().getCalendar();
		int number = getNamedIntParam( argStruct, "period", 1 );

		String datePart = getNamedStringParam( argStruct, "datepart", null ).toLowerCase();

		// --[ make the addition based on the input parameters
		if (datePart.equals("yyyy")) {
			DD.add(Calendar.YEAR, number);
		} else if (datePart.equals("q")) {
			DD.add(Calendar.MONTH, number * 3);
		} else if (datePart.equals("m")) {
			DD.add(Calendar.MONTH, number);
		} else if (datePart.equals("y")) {
			DD.add(Calendar.DAY_OF_YEAR, number);
		} else if (datePart.equals("d")) {
			DD.add(Calendar.DAY_OF_YEAR, number);
		} else if (datePart.equals("w")) {

			if (number != 0) {
				if (DD.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
					if (number > 0) {
						DD.add(Calendar.DAY_OF_YEAR, 2);
						number--;
					} else {
						DD.add(Calendar.DAY_OF_YEAR, -1);
						number++;
					}
				} else if (DD.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
					if (number > 0) {
						DD.add(Calendar.DAY_OF_YEAR, 1);
						number--;
					} else {
						DD.add(Calendar.DAY_OF_YEAR, -2);
						number++;
					}
				} else {
					int rem = number % 5;
					int foo = rem + DD.get(Calendar.DAY_OF_WEEK) - 2;
					if (foo >= 5) {
						DD.add(Calendar.DAY_OF_YEAR, 2);
					} else if (foo < 0) {
						DD.add(Calendar.DAY_OF_YEAR, -2);
					}
				}
			}

			int daysToAdd = (number / 5) * 7 + (number % 5);
			DD.add(Calendar.DAY_OF_YEAR, daysToAdd);

		} else if (datePart.equals("ww")) {
			DD.add(Calendar.WEEK_OF_YEAR, number);
		} else if (datePart.equals("h")) {
			DD.add(Calendar.HOUR, number);
		} else if (datePart.equals("n")) {
			DD.add(Calendar.MINUTE, number);
		} else if (datePart.equals("s")) {
			DD.add(Calendar.SECOND, number);
		} else if (datePart.equals("l")) {
			DD.add(Calendar.MILLISECOND, number);
		}

		return new cfDateData(DD.getTime().getTime());
	}
}
