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
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class dateCompare extends functionBase {

	private static final long serialVersionUID = 1L;

	public dateCompare() {
		min = 2;
		max = 3;
		setNamedParams( new String[]{ "date1", "date2", "datepart" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"date1",
			"date2",
			"date part ('yyyy' year, 'q' quarter, 'm' month, 'y' day of year,'d' days, 'w' week day, 'ww' week of year, 'h' hours, 'n' minutes, 's' seconds, 'l' milliseconds)",
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"date", 
				"Compares to date, to the given optional resolution.  Returns -1 if date1 is before date2.  Returns 1 if date1 is after date2.  Returns 0 if equal", 
				ReturnType.NUMERIC );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		String precision = getNamedStringParam(argStruct, "datepart", "s");
		Calendar date1, date2;
		date1 = getNamedParam(argStruct, "date1").getDateData().getCalendar();
		date2 = getNamedParam(argStruct, "date2").getDateData().getCalendar();


		precision = precision.toLowerCase();
		date1.set(Calendar.MILLISECOND, 0);
		date2.set(Calendar.MILLISECOND, 0);

		// --[ Check the precision
		if (precision.equals("n")) {

			date1.set(Calendar.SECOND, 0);
			date2.set(Calendar.SECOND, 0);

		} else if (precision.equals("h")) {

			date1.set(Calendar.SECOND, 0);
			date2.set(Calendar.SECOND, 0);

			date1.set(Calendar.MINUTE, 0);
			date2.set(Calendar.MINUTE, 0);

		} else if (precision.equals("d")) {

			date1.set(Calendar.SECOND, 0);
			date2.set(Calendar.SECOND, 0);

			date1.set(Calendar.MINUTE, 0);
			date2.set(Calendar.MINUTE, 0);

			date1.set(Calendar.HOUR, 0);
			date2.set(Calendar.HOUR, 0);
			date1.set(Calendar.HOUR_OF_DAY, 0);
			date2.set(Calendar.HOUR_OF_DAY, 0);

		} else if (precision.equals("m")) {

			date1.set(Calendar.SECOND, 0);
			date2.set(Calendar.SECOND, 0);

			date1.set(Calendar.MINUTE, 0);
			date2.set(Calendar.MINUTE, 0);

			date1.set(Calendar.HOUR, 0);
			date2.set(Calendar.HOUR, 0);
			date1.set(Calendar.HOUR_OF_DAY, 0);
			date2.set(Calendar.HOUR_OF_DAY, 0);

			date1.set(Calendar.DAY_OF_MONTH, 1);
			date2.set(Calendar.DAY_OF_MONTH, 1);

		} else if (precision.equals("yyyy")) {

			date1.set(Calendar.SECOND, 0);
			date2.set(Calendar.SECOND, 0);

			date1.set(Calendar.MINUTE, 0);
			date2.set(Calendar.MINUTE, 0);

			date1.set(Calendar.HOUR, 0);
			date2.set(Calendar.HOUR, 0);
			date1.set(Calendar.HOUR_OF_DAY, 0);
			date2.set(Calendar.HOUR_OF_DAY, 0);

			date1.set(Calendar.DAY_OF_MONTH, 1);
			date2.set(Calendar.DAY_OF_MONTH, 1);

			date1.set(Calendar.MONTH, 1);
			date2.set(Calendar.MONTH, 1);
		}

		date1.getTime();
		date2.getTime();

		if (date1.equals(date2))
			return new cfNumberData(0);
		else if (date1.after(date2))
			return new cfNumberData(1);
		else
			return new cfNumberData(-1);
	}
}
