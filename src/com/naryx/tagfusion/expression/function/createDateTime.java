/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: createDateTime.java 2381 2013-06-15 02:02:38Z alan $
 */

package com.naryx.tagfusion.expression.function;

import java.util.Calendar;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class createDateTime extends functionBase {

	private static final long serialVersionUID = 1L;

	public createDateTime() {
		min = 6; max =7;
		setNamedParams( new String[]{ "year", "month", "day", "hour", "minute", "second", "ms" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"year",
			"month (1-12)",
			"day",
			"hour (0-23)",
			"minute",
			"second",
			"ms"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"date", 
				"Creates a CFML date/time object from the given year, month, day, hour, minute, second and optional millisecond", 
				ReturnType.DATE );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		int year 	= getNamedIntParam( argStruct, "year", 1 );
		int month = getNamedIntParam( argStruct, "month", 1 ) - 1;
		int day	 	= getNamedIntParam( argStruct, "day", 1 );

		int hour 	= getNamedIntParam( argStruct, "hour", 1 );
		int min 	= getNamedIntParam( argStruct, "minute", 1 );
		int sec 	= getNamedIntParam( argStruct, "second", 1 );
		int ms 		=	getNamedIntParam( argStruct, "ms", 0 );

		try {
			long time = getTime(year, month, day, hour, min, sec, ms);
			return new cfDateData(time);
		} catch (java.lang.IllegalArgumentException e) {
			throwException(_session, "Invalid argument. Check the values provided are in the correct range. (" + e.toString() + ")");
		}
		return null; // keep the compiler happy

	}

	/*
	 * getTime
	 * 
	 * This method takes in the year, month, day, hour, minutes and seconds and
	 * returns the time in milliseconds.
	 */
	public static long getTime(int year, int month, int day, int hour, int min, int sec, int ms) throws IllegalArgumentException {
		if (month > 11)
			throw new IllegalArgumentException("month field is too large");
		
		if (ms > 1000)
			throw new IllegalArgumentException("ms field is too large");

		if (0 > hour || hour > 23 || 0 > min || min > 59 || 0 > sec || sec > 59)
			throw new IllegalArgumentException("the hour/min/sec is out of range");

		// Here's the fix for bug #1446
		if (year >= 0 && year <= 29)
			year = 2000 + year;
		else if (year >= 30 && year <= 99)
			year = 1900 + year;

		// Create a java Calendar object
		Calendar c = Calendar.getInstance();
		c.setLenient(false);
		c.clear();
		c.set(year, month, day, hour, min, sec);
		c.set( Calendar.MILLISECOND, ms );
		
		// Get the time in milliseconds from the Calendar object
		return c.getTime().getTime();
	}
}
