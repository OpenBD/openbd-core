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

public class datePart extends functionBase {

	private static final long serialVersionUID = 1L;

	public datePart() {
		min = max = 2;
		setNamedParams( new String[]{ "datepart", "date" } );
	}


  public String[] getParamInfo(){
		return new String[]{
			"date part ('yyyy' year, 'q' quarter, 'm' month, 'y' day of year, 'd' days, 'w' week day, 'ww' week of year, 'h' hours, 'n' minutes, 's' seconds, 'l' milliseconds)",
			"date"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"date", 
				"Extracts the given date part from the date object", 
				ReturnType.NUMERIC );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		Calendar c = getNamedParam( argStruct, "date" ).getDateData().getCalendar();
		String dateP = getNamedStringParam( argStruct, "datepart", null ).toLowerCase();

		int result = 0;
		// --[ get the part of the date based on the input parameter
		if (dateP.equals("yyyy")) {
			result = c.get(Calendar.YEAR);
		} else if (dateP.equals("q")) {
			result = calculateQuarter(c);
		} else if (dateP.equals("m")) {
			result = (c.get(Calendar.MONTH) + 1);
		} else if (dateP.equals("y")) {
			result = c.get(Calendar.DAY_OF_YEAR);
		} else if (dateP.equals("d")) {
			result = c.get(Calendar.DAY_OF_MONTH);
		} else if (dateP.equals("w")) {
			result = c.get(Calendar.DAY_OF_WEEK);
		} else if (dateP.equals("ww")) {
			result = c.get(Calendar.WEEK_OF_YEAR);
		} else if (dateP.equals("h")) {
			result = c.get(Calendar.HOUR);

			if (c.get(Calendar.AM_PM) == 1) {
				result += 12;
			}
		} else if (dateP.equals("n")) {
			result = c.get(Calendar.MINUTE);
		} else if (dateP.equals("s")) {
			result = c.get(Calendar.SECOND);
		} else if (dateP.equals("l")) {
			result = c.get(Calendar.MILLISECOND);
		}

		return new cfNumberData(result);
	}

	private static int calculateQuarter(Calendar _c) {
		if ((_c.get(Calendar.MONTH) + 1) <= 3)
			return 1;
		else if ((_c.get(Calendar.MONTH) + 1) > 3 && (_c.get(Calendar.MONTH) + 1) <= 6)
			return 2;
		else if ((_c.get(Calendar.MONTH) + 1) > 6 && (_c.get(Calendar.MONTH) + 1) <= 9)
			return 3;
		else if ((_c.get(Calendar.MONTH) + 1) > 9 && (_c.get(Calendar.MONTH) + 1) <= 12)
			return 4;
		else
			return 0;
	}

}
