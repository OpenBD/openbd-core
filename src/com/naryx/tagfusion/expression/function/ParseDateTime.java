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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import com.nary.util.date.dateTimeTokenizer;
import com.nary.util.date.monthConverter;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;


public class ParseDateTime extends functionBase {

	private static final long serialVersionUID = 1L;


	public ParseDateTime() {
		min = 1;
		max = 2;
		setNamedParams( new String[] { "date", "format" } );
	}


	public String[] getParamInfo() {
		return new String[] { "date1", "pop format; 'standard' or 'pop'"
		};
	}


	public java.util.Map getInfo() {
		return makeInfo( "date", "Formats a date string to a given output supports standard formats include SQL, Unix date string and HTTP timestamps.", ReturnType.DATE );
	}


	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		// TODO: add support for additional date-time formats supported by cfmx
		// e.g '24-01-2005 01:31:24', '2-13-2005 01:31:24', '2015-10-06T12:56:55Z'

		boolean convertTimeZone = false;

		String popconversion = getNamedStringParam( argStruct, "format", "standard" );
		if ( popconversion.equalsIgnoreCase( "pop" ) ) {
			convertTimeZone = true;
		} else if ( !popconversion.equalsIgnoreCase( "standard" ) ) {
			throwException( _session, "Invalid value for pop-conversion parameter. Must be \"POP\" or \"STANDARD\"." );
		}

		String dateTimeStr = getNamedStringParam( argStruct, "date", null );
		cfData dateTime = parseDateTimeString( dateTimeStr, convertTimeZone );

		// --[ if the format was bad throw exception
		if ( dateTime == null ) {
			java.util.Date jdateTime = parseSQLDateTime( dateTimeStr );
			if ( jdateTime == null )
				jdateTime = parseDateString( dateTimeStr );
			if ( jdateTime == null )
				jdateTime = parseHttpDateTime( dateTimeStr );
			if ( jdateTime == null )
				jdateTime = parseISO8601DateTime( dateTimeStr );
			if ( jdateTime == null )
				jdateTime = parseUnixDateTime( dateTimeStr, convertTimeZone );
			if ( jdateTime == null )
				throwException( _session, "invalid date/time string: " + dateTimeStr );
			dateTime = new cfDateData( jdateTime );
		}
		return dateTime;
	}


	// -----------------------


	// this converts a date-time string to a date object. This is completely different
	// to parseDateString. It only accepts a date formatted in one way.
	// Tues, 27 March 1999 19:19:23 -0500
	public static cfDateData parseDateTimeString( String _dateTimeString, boolean _convertTimeZone ) {
		String[] match;
		int offset = 0;
		try {
			match = regexpMatch( "[a-zA-Z]+,[[:space:]]*([0-9]+)[[:space:]]+([a-zA-Z]+)[[:space:]]+([0-9]+)[[:space:]]+([0-9]{1,2}):([0-9]{1,2})(?::([0-9]{1,2}))?([[:space:]]*(?:[+-][0-9]{2}:?[0-9]{2}(?: \\([a-zA-Z]+\\))?)|[[:space:]]*(?:[a-zA-Z]{3}))?", _dateTimeString );
			if ( match.length > 0 ) {
				java.util.GregorianCalendar cal = (GregorianCalendar) java.util.GregorianCalendar.getInstance();
				cal.setLenient( false );

				cal.set( java.util.Calendar.YEAR, Integer.parseInt( match[3] ) );
				cal.set( java.util.Calendar.MONTH, monthConverter.convertMonthToInt( match[2].toLowerCase().toCharArray() ) );
				cal.set( java.util.Calendar.DAY_OF_MONTH, Integer.parseInt( match[1] ) );

				cal.set( java.util.Calendar.HOUR_OF_DAY, Integer.parseInt( match[4] ) );
				cal.set( java.util.Calendar.MINUTE, Integer.parseInt( match[5] ) );

				// if ( match.length == 7 ){
				if ( match[6] != null ) {
					cal.set( java.util.Calendar.SECOND, Integer.parseInt( match[6] ) );
				} else {
					cal.set( java.util.Calendar.SECOND, 0 );
				}
				cal.set( java.util.Calendar.MILLISECOND, 0 );

				// if POP and we have a timezone match
				if ( _convertTimeZone && match.length > 7 && match[7] != null ) {
					offset = convertOffset( match[7] );
				}

				try {
					if ( offset != 0 ) {
						cal.add( Calendar.MILLISECOND, offset );
					}
					return new cfDateData( cal.getTime() );
				} catch ( IllegalArgumentException e ) { // thrown if invalid date
					return null;
				}


			}
		} catch ( MalformedPatternException e ) {
			return null; // shouldn't happen since the reg exp is hard coded
		}

		return null;
	}


	public static java.util.Date parseDateString( String _dateString ) {
		java.util.Date d = dateTimeTokenizer.getUSDate( _dateString );
		if ( d == null ) {
			d = dateTimeTokenizer.getNeutralDate( _dateString );
		}

		if ( d == null ) {
			d = dateTimeTokenizer.getUKDate( _dateString );
		}

		return d;
	}// parseDateString()


	/*
	 * takes a string in the format +0500, -12:00 etc or short-style TimeZone and
	 * return the integer value it corresponds to in ms.
	 */
	private static int convertOffset( String _offset ) {
		String offset = _offset.trim();
		int offsetMs = 0;
		try {
			// Test if offset is short TZ
			if ( regexpMatch( "[A-Za-z]{3}", offset ).length == 1 ) {
				TimeZone tz = TimeZone.getTimeZone( offset );
				offsetMs = -tz.getRawOffset();
				// Else fallback on to +/- numeric ofsets
			} else {
				int spaceIndx = offset.indexOf( ' ' );
				if ( spaceIndx != -1 ) {
					offset = offset.substring( 0, spaceIndx );
				}

				// remove the colon if one exists
				int colonIndex = offset.indexOf( ":" );
				if ( colonIndex != -1 ) {
					offset = offset.substring( 0, colonIndex ) + offset.substring( colonIndex + 1 );
				}

				if ( offset.charAt( 0 ) == '+' ) {
					offset = offset.substring( 1 );
				}

				// Convert offset into ms
				offsetMs = Integer.parseInt( offset );
				offsetMs = -1 * ( ( offsetMs / 100 ) * 60 + ( offsetMs % 100 ) ) * 60 * 1000;
			}

			return offsetMs;
		} catch ( Exception e ) {
			return 0;
		}
	}// convertOffset()


	private static java.util.Date parseSQLDateTime( String _dateTime ) throws cfmRunTimeException {

		// The reg exp below represents the format "(year)-(month)-(day) (hour):(min):(sec)"
		// Note that it allows for invalid dates/times like 2004-99-99 99:99:99
		// but that's ok as it's format that matters at this point
		String regexp = "([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{1,2}):([0-9]{1,2})(:([0-9]{1,2}))?";

		try {
			String[] match = regexpMatch( regexp, _dateTime );

			if ( match.length >= 5 ) {
				java.util.GregorianCalendar cal = (GregorianCalendar) java.util.GregorianCalendar.getInstance();
				cal.setLenient( false );

				cal.set( java.util.Calendar.YEAR, Integer.parseInt( match[1] ) );
				cal.set( java.util.Calendar.MONTH, Integer.parseInt( match[2] ) - 1 );
				cal.set( java.util.Calendar.DAY_OF_MONTH, Integer.parseInt( match[3] ) );

				cal.set( java.util.Calendar.HOUR_OF_DAY, Integer.parseInt( match[4] ) );
				cal.set( java.util.Calendar.MINUTE, Integer.parseInt( match[5] ) );

				if ( match.length == 8 && match[7] != null ) {
					cal.set( java.util.Calendar.SECOND, Integer.parseInt( match[7] ) );
				} else {
					cal.set( java.util.Calendar.SECOND, 0 );
				}
				cal.set( java.util.Calendar.MILLISECOND, 0 );

				try {
					return cal.getTime();
				} catch ( IllegalArgumentException e ) { // thrown if invalid date
					return null;
				}
			} else {
				return null;
			}

		} catch ( MalformedPatternException e ) { // shouldn't happen as we control the RegEx
			// unlikely exception since we've set the regexp
			cfCatchData catchD = new cfCatchData();
			catchD.setType( "Function" );
			catchD.setMessage( "parseSQLDateTime - internal error" );
			catchD.setDetail( "Invalid regular expression ( " + regexp + " )" );
			throw new cfmRunTimeException( catchD );
		}

	}


	private static java.util.Date parseHttpDateTime( String _dateTime ) throws cfmRunTimeException {

		// The reg exp below represents the format "dd MMM yyyy HH:mm:ss GMT" - 11 Aug 2010 17:58:48 GMT
		// Note that it allows for invalid dates/times like 99 Zbr 9999 99:99:99 GMT"
		// but that's ok as it's format that matters at this point
		String regexp = "([0-9]{2}) ([A-Za-z]{3}) ([0-9]{4}) ([0-9]{2}):([0-9]{2}):([0-9]{2}) GMT";

		try {
			String[] match = regexpMatch( regexp, _dateTime );

			if ( match.length == 7 ) {

				try {
					Date dateTime = null;
					java.util.GregorianCalendar cal = (GregorianCalendar) java.util.GregorianCalendar.getInstance();

					cal.set( java.util.Calendar.YEAR, Integer.parseInt( match[3] ) );
					cal.set( java.util.Calendar.MONTH, monthConverter.convertMonthToInt( match[2].toLowerCase().toCharArray() ) );
					cal.set( java.util.Calendar.DAY_OF_MONTH, Integer.parseInt( match[1] ) );

					cal.set( java.util.Calendar.HOUR_OF_DAY, Integer.parseInt( match[4] ) );
					cal.set( java.util.Calendar.MINUTE, Integer.parseInt( match[5] ) );
					cal.set( java.util.Calendar.SECOND, Integer.parseInt( match[6] ) );

					dateTime = cal.getTime();

					return dateTime;

				} catch ( IllegalArgumentException e ) { // thrown if invalid date
					return null;
				}

			} else {
				return null;
			}

		} catch ( MalformedPatternException e ) {
			// unlikely exception since we've set the regexp
			cfCatchData catchD = new cfCatchData();
			catchD.setType( "Function" );
			catchD.setMessage( "ParseDateTime - internal error" );
			catchD.setDetail( "Invalid regular expression ( " + regexp + " )" );
			throw new cfmRunTimeException( catchD );
		}

	}


	private static java.util.Date parseUnixDateTime( String _dateTime, boolean _convertTimeZone ) throws cfmRunTimeException {

		// The reg exp below represents the format "EEE MMM dd HH:mm:ss Z yyyy"
		// Note that it allows for invalid dates/times like "Zbr Hot 99, 99:99:99 UTC 0100"
		// but that's ok as it's format that matters at this point
		String regexp = "([A-Za-z]{3}) ([A-Za-z]{3}) ([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2}) ([A-Za-z]{3}|\\+[0-9]{4}|-[0-9]{4}) ([0-9]{4})";
		int offset = 0;

		try {
			String[] match = regexpMatch( regexp, _dateTime );

			if ( match.length == 9 ) {

				try {
					Date dateTime = null;
					java.util.GregorianCalendar cal = (GregorianCalendar) java.util.GregorianCalendar.getInstance();

					cal.set( java.util.Calendar.YEAR, Integer.parseInt( match[8] ) );
					cal.set( java.util.Calendar.MONTH, monthConverter.convertMonthToInt( match[2].toLowerCase().toCharArray() ) );
					cal.set( java.util.Calendar.DAY_OF_MONTH, Integer.parseInt( match[3] ) );

					cal.set( java.util.Calendar.HOUR_OF_DAY, Integer.parseInt( match[4] ) );
					cal.set( java.util.Calendar.MINUTE, Integer.parseInt( match[5] ) );
					cal.set( java.util.Calendar.SECOND, Integer.parseInt( match[6] ) );

					// if POP and we have a timezone match
					if ( _convertTimeZone ) {
						offset = convertOffset( match[7] );
					}

					if ( offset != 0 ) {
						cal.add( Calendar.MILLISECOND, offset );
					}

					dateTime = cal.getTime();

					return dateTime;

				} catch ( IllegalArgumentException e ) { // thrown if invalid date
					return null;
				}

			} else {
				return null;
			}

		} catch ( MalformedPatternException e ) {
			// unlikely exception since we've set the regexp
			cfCatchData catchD = new cfCatchData();
			catchD.setType( "Function" );
			catchD.setMessage( "ParseDateTime - internal error" );
			catchD.setDetail( "Invalid regular expression ( " + regexp + " )" );
			throw new cfmRunTimeException( catchD );
		}

	}


	private static java.util.Date parseISO8601DateTime( String _dateTime ) throws cfmRunTimeException {

		// The reg exp below represents the format "(year)-(month)-(day)T(hour):(min):(sec)Z(+|-)(hour):(min)"
		// Note that it allows for invalid dates/times like "2004-99-99T99:99:99Z99:99+99:99"
		// but that's ok as it's format that matters at this point
		String regexp = "([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{1,2}):([0-9]{1,2}):([0-9]{1,2})(Z|((\\+|-)([0-9]{1,2}):([0-9]{1,2})))?";
		long offset = 0;
		try {
			String[] match = regexpMatch( regexp, _dateTime );

			if ( match.length >= 6 ) { // Date is at least "2015-01-01T13:15:11"
				java.util.GregorianCalendar cal = (GregorianCalendar) java.util.GregorianCalendar.getInstance();
				cal.setLenient( false );
				Integer hour = Integer.parseInt( match[4] );
				Integer minute = Integer.parseInt( match[5] );


				cal.set( java.util.Calendar.YEAR, Integer.parseInt( match[1] ) );
				cal.set( java.util.Calendar.MONTH, Integer.parseInt( match[2] ) - 1 );
				cal.set( java.util.Calendar.DAY_OF_MONTH, Integer.parseInt( match[3] ) );

				cal.set( java.util.Calendar.HOUR_OF_DAY, hour );
				cal.set( java.util.Calendar.MINUTE, minute );
				cal.set( java.util.Calendar.SECOND, Integer.parseInt( match[6] ) );

				if ( match[10] != null ) { // if there's an offset
					if ( Integer.parseInt( match[10] ) < 24 && Integer.parseInt( match[11] ) <= 59 ) { // check offset < 23:59
						long timeInMills = cal.getTimeInMillis();
						offset = convertOffset( match[7] ); // get offset in ms
						timeInMills += offset; // add offset to datetime
						cal.setTimeInMillis( timeInMills );
					}
					else {
						return null;
					}
				}

				try {
					return cal.getTime();
				} catch ( IllegalArgumentException e ) { // thrown if invalid date
					return null;
				}
			} else {
				return null;
			}

		} catch ( MalformedPatternException e ) { // shouldn't happen as we control the RegEx
			// unlikely exception since we've set the regexp
			cfCatchData catchD = new cfCatchData();
			catchD.setType( "Function" );
			catchD.setMessage( "parseSQLDateTime - internal error" );
			catchD.setDetail( "Invalid regular expression ( " + regexp + " )" );
			throw new cfmRunTimeException( catchD );
		}

	}


	private static String[] regexpMatch( String _re, String _txt ) throws MalformedPatternException {
		PatternMatcher matcher;
		PatternCompiler compiler;
		Pattern pattern = null;
		PatternMatcherInput input;
		MatchResult result;

		compiler = new Perl5Compiler();
		matcher = new Perl5Matcher();

		pattern = compiler.compile( _re );

		input = new PatternMatcherInput( _txt );

		if ( matcher.matches( input, pattern ) ) {
			result = matcher.getMatch();
			int noGroups = result.groups();
			String[] matches = new String[noGroups];
			for ( int i = 0; i < noGroups; i++ ) {
				matches[i] = result.group( i );
			}
			return matches;
		} else {
			return new String[0];
		}
	}


	public static void main( String[] _args ) {
		System.out.println( parseDateString( "22-Mar-2000 11:02" ) );
		System.out.println( parseDateString( "22 Mar 2000 11:02" ) );
		System.out.println( parseDateString( "2 March 2000 11:02" ) );

		System.out.println( parseDateString( "Tue, 27 Mar 2001 12:25:08 -0500" ) );
		System.out.println( parseDateString( "01/02/89" ) );
		System.out.println( parseDateString( "18:12" ) );
	}

}
