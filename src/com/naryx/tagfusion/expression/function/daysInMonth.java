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
import java.util.GregorianCalendar;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class daysInMonth extends functionBase {

	private static final long serialVersionUID = 1L;

	public daysInMonth() {
		min = max = 1;
		setNamedParams( new String[]{ "date" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"date"	
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"date", 
				"Returns the number of days in the month that the date object represents", 
				ReturnType.NUMERIC );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		return new cfNumberData(determineDays(getNamedParam( argStruct, "date" ).getDateData().getCalendar()));
	}

	private static int determineDays(Calendar _cal) {
		int month = _cal.get(Calendar.MONTH);

		if (month == 8 || month == 3 || month == 5 || month == 10)
			return 30;
		else if (month == 1) {
			GregorianCalendar greg = new GregorianCalendar(_cal.get(Calendar.YEAR), _cal.get(Calendar.MONTH), _cal.get(Calendar.DATE));
			if (greg.isLeapYear(greg.get(Calendar.YEAR)))
				return 29;
			else
				return 28;
		} else
			return 31;
	}
}
