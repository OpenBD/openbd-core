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

/*
 * Returns back a date formatted for the use in HTTP requests.
 * 
 * If no date is passed in then the current time is used 
 */

package com.naryx.tagfusion.expression.function;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class getHttpTimeString extends functionBase {
	private static final long serialVersionUID = 1L;

	public getHttpTimeString() {
		min = 0;
		max = 1;
		setNamedParams( new String[]{ "date" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"date1"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"date", 
				"Returns back the time given (or the current time) in the format of the HTTP header", 
				ReturnType.STRING );
	}
 

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		long date;

		cfData	p = getNamedParam(argStruct, "date");
		if ( p == null ){
			date = System.currentTimeMillis();
		}else{
			date = p.getDateData().getLong();
		}
		
		// The following code is the fix for bug #2578

		// Get the time
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(new Date(date));

		// Convert to GMT
		TimeZone zone = cal.getTimeZone();
		int offset = zone.getOffset(cal.get(Calendar.ERA), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.DAY_OF_WEEK), cal.get(Calendar.MILLISECOND));
		cal.add(Calendar.MILLISECOND, -offset);

		return new cfStringData(com.nary.util.Date.formatDate(cal.getTime().getTime(), "EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US));
	}
}
