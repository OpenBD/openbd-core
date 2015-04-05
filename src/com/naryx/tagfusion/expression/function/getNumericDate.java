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

import java.math.BigDecimal;
import java.util.Calendar;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * Infomation about the getNumericDate() function can be found in the following
 * O'Reilly Press book:
 *
 * Programming ColdFusion MX, 2nd Edition Creating Dynamic Web Applications By
 * Rob Brooks-Bilson 2nd Edition August 2003 ISBN: 0-596-00380-3
 *
 * It says that 12 AM on 12-30-1899 represents the starting point (epoch) and so
 * GetNumericDate('12/30/1899 00:00:00') returns just zero. Otherwise, the
 * function returns a real number whose integer part represents the number of
 * days since 12 AM on 12-30-1899 and whose fractional part represents the time
 * value expressed in hours then divided by 24.
 *
 * Here is how the time is converted to the fractional part of the real number
 * that's returned using 19:28:10 as an example:
 *
 * 19 + 28/60 + 10/3600 = 194694444445
 *
 * 194694444445 / 24 = 0.81122685185
 */
public class getNumericDate extends functionBase {
	private static final long serialVersionUID = 1L;

	private static cfDateData epoch = null;

	/**
	 * Each app server (cfmx 6.1, bdjava, bdnet) was returning the right values,
	 * but with varying degrees of precision and total number of digits. For
	 * example here is some sample output that was observed by Matt M. during
	 * development of this function: cfmx 37621.8112269 bdjava 37621.81122685185
	 * bdnet 37621.811226851853
	 *
	 * So we do rounding of the fractional part accounting for the number of
	 * digits in the whole number part. This is done at the end of execute()
	 */
	private static int totalNumDigits = 12; // cfmx 6.1 returns a real number that
																					// uses this many digits (this counts
																					// all the digits, no matter which
																					// side of the decimal they are on)

	public getNumericDate() {
		min = max = 1;
		setNamedParams( new String[]{ "date" } );
	}

	@Override
	public String[] getParamInfo(){
		return new String[]{
			"date"
		};
	}

	@Override
	public java.util.Map getInfo(){
		return makeInfo(
				"date",
				"Returns a real number whose integer part represents the number of days since the EPOCH (12 AM on 12-30-1899) and whose fractional part represents the time value expressed in hours then divided by 24",
				ReturnType.NUMERIC );
	}

	private synchronized void setupEpoch(cfSession _session) throws cfmRunTimeException {
		if (epoch == null) {
			int year = 1899;
			int month = 12 - 1; // months are 0-11
			int day = 30;
			int hour = 0;
			int min = 0;
			int sec = 0;

			Calendar c = Calendar.getInstance();
			c.setLenient(false);
			c.clear();
			c.set(year, month, day, hour, min, sec);

			try {
				epoch = new cfDateData(c.getTime().getTime());
			} catch (Exception e) {
				throwException(_session, "Unable to create epoch value. " + e);
			}
		}
	}

	@Override
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		if (epoch == null)
			setupEpoch(_session);

		cfData	dateObj	= getNamedParam(argStruct, "date", null );
		if ( dateObj == null )
			throwException(_session, "Missing 'date' parameter");

		cfDateData dateTime;
		if ( dateObj.getDataType() == cfData.CFDATEDATA )
			dateTime	= (cfDateData)dateObj;
		else
			dateTime	= (cfDateData)dateObj.coerce(cfData.CFDATEDATA);

		// use the dateDiff() function
		cfNumberData dayDiff = dateDiff.getDiff(epoch, dateTime, "d");

		long numDays = dayDiff.getLong();
		int numDaysDigits = String.valueOf(Math.abs(numDays)).length();

		// now let's convert the hours, minutes and seconds into hours
		Calendar cal	= dateTime.getCalendar();

		int hour = cal.get( Calendar.HOUR );
		int minute = cal.get( Calendar.MINUTE );
		int second = cal.get( Calendar.SECOND );

		double timeInHours = hour + (((double) minute) / 60) + (((double) second) / 3600);
		double time = timeInHours / 24;

		int scale = totalNumDigits - numDaysDigits;
		BigDecimal roundedTime = new BigDecimal(time);
		roundedTime = roundedTime.setScale(scale, BigDecimal.ROUND_HALF_UP);

		boolean isNegative = numDays < 0;

		double decimalDiff = roundedTime.doubleValue();
		if (isNegative && decimalDiff > 0)
			decimalDiff = 1.0 - decimalDiff;

		double datePlusTime = Math.abs(numDays) + decimalDiff;

		if (isNegative)
			datePlusTime = -1.0 * datePlusTime;

		return new cfNumberData(datePlusTime);
	}
}
