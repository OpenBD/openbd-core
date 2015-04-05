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

package com.nary.util;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.nary.cache.MapTimedCache;

public class Date {

	private static final MapTimedCache cfmlTimeFormatCache = new MapTimedCache(10 * 60);

	private static final MapTimedCache cfmlDateFormatCache = new MapTimedCache(10 * 60);

	private static int dateRes[] = { Calendar.MILLISECOND, Calendar.SECOND, Calendar.MINUTE, Calendar.HOUR_OF_DAY, Calendar.DAY_OF_MONTH };

	public static boolean after(Calendar LHS, Calendar RHS, int _Resolution) {
		Calendar lhs = (Calendar) LHS.clone();
		Calendar rhs = (Calendar) RHS.clone();

		for (int x = 0; x < dateRes.length; x++) {
			if (_Resolution != dateRes[x]) {
				lhs.set(dateRes[x], 0);
				rhs.set(dateRes[x], 0);
			} else
				break;
		}

		return lhs.after(rhs);
	}

	public static GregorianCalendar createCalendar(long timeMS) {
		GregorianCalendar G = new GregorianCalendar();
		G.setTime(new java.util.Date(timeMS));
		return G;
	}

	public static String formatNow() {
		return formatDate(System.currentTimeMillis(), "dd/MMM/yyyy HH:mm.ss");
	}

	public static String formatNow(String javaFormatString) {
		return formatDate(System.currentTimeMillis(), javaFormatString);
	}

	public static String formatNow(String javaFormatString, Locale loc) {
		return formatDate(System.currentTimeMillis(), javaFormatString, loc);
	}

	public static String formatDate(long timeMS) {
		return formatDate(timeMS, "dd/MMM/yyyy HH:mm.ss");
	}

	public static String formatDate(long timeMS, String javaFormatString) {
		String javaValue = new SimpleDateFormat(javaFormatString).format(new java.util.Date(timeMS));
		return javaValue;
	}

	public static String formatDate(long timeMS, String javaFormatString, Locale loc) {
		String javaValue = new SimpleDateFormat(javaFormatString, new DateFormatSymbols(loc)).format(new java.util.Date(timeMS));
		return javaValue;
	}

	/*
	 * formatDate( long timeMS, String formatString, boolean displayTimeZone )
	 * 
	 * If displayTimeZone is true then it will format the time zone as
	 * '[+|-]hh:mm' instead of as 'EST' or 'GMT[+|-]hh:mm'. This is used for
	 * displaying the sent date of a POP message.
	 */
	public static String formatDate(long timeMS, String formatString, boolean displayTimeZone) {
		if (displayTimeZone) {
			SimpleDateFormat sdm = new SimpleDateFormat(formatString);

			sdm.getTimeZone().setID("GMT [+|-]hhmm");

			// this little fudge removes the "GMT" from the date string
			String result = sdm.format(new java.util.Date(timeMS));
			int gmtIndex = result.indexOf("GMT");
			result = result.substring(0, gmtIndex) + result.substring(gmtIndex + 3);
			return result;
		} else {
			return formatDate(timeMS, formatString);
		}
	}

	/*
	 * cfmlFormatTime
	 * 
	 * This method formats a time using the passed in CFML time format string and
	 * Locale.
	 */
	public static String cfmlFormatTime(long timeMS, String cfmlFormatString, Locale loc) {
		boolean replaceTimeMarker = false;

		// Convert the CFML time format string to a Java format String
		String javaFormatString = cfmlTimeToJavaFormatString(cfmlFormatString);

		// If the mask contained only a single 't' (now an 'a') then we need
		// to replace the time marker (AM or PM) with (A or P).
		int pos = javaFormatString.indexOf('a');
		if (pos != -1) {
			if ((pos + 1 == javaFormatString.length()) || (javaFormatString.charAt(pos + 1) != 'a'))
				replaceTimeMarker = true;
		}

		// Format the date
		String javaValue = new SimpleDateFormat(javaFormatString, new DateFormatSymbols(loc)).format(new java.util.Date(timeMS));

		// See if we need to replace the time marker (AM or PM) with (A or P).
		if (replaceTimeMarker) {
			pos = javaValue.indexOf("PM");
			if (pos == -1)
				pos = javaValue.indexOf("AM");
			if (pos != -1) {
				if (pos + 2 == javaValue.length())
					javaValue = javaValue.substring(0, pos + 1);
				else
					javaValue = javaValue.substring(0, pos + 1) + javaValue.substring(pos + 2);
			}
		}

		return javaValue;
	}

	/*
	 * cfmlFormatDate
	 * 
	 * This method formats a date using the passed in CFML date format string and
	 * Locale.
	 */
	public static String cfmlFormatDate(long timeMS, String cfmlFormatString, Locale loc) {
		// Convert the CFML date format string to a Java format String
		String javaFormatString = cfmlDateToJavaFormatString(cfmlFormatString);

		// Format the date
		String javaValue = new SimpleDateFormat(javaFormatString, new DateFormatSymbols(loc)).format(new java.util.Date(timeMS));

		return javaValue;
	}

	/*
	 * Returns the difference in seconds from the today and the given date String.
	 * The date String should be of the format mm/dd/yy.
	 */
	public static int secondDifference(String _date) {
		SimpleDateFormat eudate = new SimpleDateFormat("MM/dd/yy");
		eudate.setLenient(false);
		try {
			java.util.Date test = eudate.parse(_date);
			java.util.Date today = new java.util.Date();
			int differ = (int) ((test.getTime() - today.getTime()) / 1000);
			return differ;
		} catch (Exception e) {
			return 0;
		}
	}

	/*
	 * cfmlTimeToJavaFormatString
	 * 
	 * This method converts a CFML time format string to a Java format string.
	 */
	private static String cfmlTimeToJavaFormatString(String cfmlFormatString) {
		String cachedFormatString = (String) cfmlTimeFormatCache.getFromCache(cfmlFormatString);
		if (cachedFormatString != null)
			return cachedFormatString;

		try {
			// Get a lock just for this entry
			synchronized (cfmlTimeFormatCache.getLock(cfmlFormatString)) {
				// we need to check the cache again since another thread could
				// have loaded the entry
				cachedFormatString = (String) cfmlTimeFormatCache.getFromCache(cfmlFormatString);
				if (cachedFormatString != null)
					return cachedFormatString;

				int len = cfmlFormatString.length();
				StringBuilder javaFormatString = new StringBuilder(len);

				for (int i = 0; i < len; i++) {
					char ch = cfmlFormatString.charAt(i);
					switch (ch) {
					case 'T':
					case 't':
						javaFormatString.append('a');
						break;

					case 'L':
					case 'l':
						javaFormatString.append('S');
						break;

					case 'M':
						javaFormatString.append('m');
						break;

					case 'S':
						javaFormatString.append('s');
						break;

					case 'Z':
					case 'z':
						// Place one or more consecutive 'Z' or 'z' characters
						// within single quotes.
						javaFormatString.append('\'');
						javaFormatString.append(ch);
						while (i + 1 < len) {
							char nextCh = cfmlFormatString.charAt(i + 1);
							if ((nextCh == 'Z') || (nextCh == 'z')) {
								javaFormatString.append(nextCh);
								i++;
							} else {
								break;
							}
						}
						javaFormatString.append('\'');
						break;

					case '\'':
						// skip over quoted characters
						javaFormatString.append('\'');
						i++;
						while (i < len) {
							char nextCh = cfmlFormatString.charAt(i);
							javaFormatString.append(nextCh);
							i++;
							if ((nextCh == '\''))
								break;
						}
						break;

					default:
						javaFormatString.append(ch);
						break;
					}
				}
				String fs = javaFormatString.toString();
				cfmlTimeFormatCache.setInCache(cfmlFormatString, fs);
				return fs;
			}
		} finally {
			// Be sure to remove the lock for this entry
			cfmlTimeFormatCache.removeLock(cfmlFormatString);
		}
	}

	/*
	 * cfmlDateToJavaFormatString
	 * 
	 * This method converts a CFML date format string to a Java format string.
	 */
	private static String cfmlDateToJavaFormatString(String cfmlFormatString) {
		String cachedFormatString = (String) cfmlDateFormatCache.getFromCache(cfmlFormatString);
		if (cachedFormatString != null)
			return cachedFormatString;

		try {
			// Get a lock just for this entry
			synchronized (cfmlDateFormatCache.getLock(cfmlFormatString)) {
				// we need to check the cache again since another thread could have
				// loaded the entry
				cachedFormatString = (String) cfmlDateFormatCache.getFromCache(cfmlFormatString);
				if (cachedFormatString != null)
					return cachedFormatString;

				int len = cfmlFormatString.length();
				StringBuilder javaFormatString = new StringBuilder(len);

				for (int i = 0; i < len; i++) {
					int num;
					char ch = cfmlFormatString.charAt(i);
					switch (ch) {
					case 'm':
						javaFormatString.append('M');
						break;

					case 'g':
						javaFormatString.append('G');
						break;

					case 'd':
						// Move past all consecutive 'd' characters while counting them
						num = 1;
						while ((i + 1 < len) && (cfmlFormatString.charAt(i + 1) == 'd')) {
							num++;
							i++;
						}

						// Replace 3 consecutive 'd' characters with 'EEE'
						// Replace 4 or more consecutive 'd' characters with 'EEEE'
						if (num == 1)
							javaFormatString.append('d');
						else if (num == 2)
							javaFormatString.append("dd");
						else if (num == 3)
							javaFormatString.append("EEE");
						else
							javaFormatString.append("EEEE");
						break;

					case '\'':
						// escape quoted characters
						javaFormatString.append('\'');
						javaFormatString.append('\'');
						break;

					case ' ':
					case ',':
					case '-':
					case '/':
					case 'y':
						javaFormatString.append(ch);
						break;

					default:
						javaFormatString.append('\'');
						do{
							javaFormatString.append(ch);
							i++;	
							if ( i >= len ){
								break;
							}
							ch = cfmlFormatString.charAt(i);
						}while ( ch != 'm' && ch != 'g' && ch != 'y' && ch != 'd' && ch != ' ' && ch != ',' && ch != '\'' );
						i--;
						javaFormatString.append('\'');
						break;
					}
				}

				String fs = javaFormatString.toString();
				cfmlDateFormatCache.setInCache(cfmlFormatString, fs);
				return fs;
			}
		} finally {
			// Be sure to remove the lock for this entry
			cfmlDateFormatCache.removeLock(cfmlFormatString);
		}
	}
}
