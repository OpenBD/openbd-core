/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  
 *  $Id: $
 */

package com.naryx.tagfusion.cfm.engine;

import java.util.Locale;
import java.util.TimeZone;

import com.nary.util.date.dateTimeTokenizer;

public class cfDateData extends cfJavaObjectData implements java.io.Serializable {

	static final long serialVersionUID = 1;

	// the CFMX Epoch (30th Dec 1899) compared to the java Epoch (1 Jan 1970)
	public final static long CF_EPOCH = -2209161600000L;

	private final static long DAY = 1000 * 60 * 60 * 24;

	/** The time in milliseconds since 01 January 1970. */
	private long time;

	/** The String representation of this date. */
	private String dateString;

	private String formatString; // format used to create dateString

	private String formatStringPrefix;

	public cfDateData(long _time) {
		super(new java.util.Date(_time));
		time = _time;
		formatString = "yyyy-MM-dd HH:mm:ss";
		formatStringPrefix = "ts";
	}

	public cfDateData(java.util.Date _date) {
		super(_date);
		time = _date.getTime();
		formatString = "yyyy-MM-dd HH:mm:ss";
		formatStringPrefix = "ts";
	}

	public cfDateData(java.sql.Timestamp _timeIn) {
		super(new java.util.Date(_timeIn.getTime()));
		time = _timeIn.getTime();
		formatString = "yyyy-MM-dd HH:mm:ss.SSS";
	}

	public cfDateData(java.sql.Date _dateIn) {
		super(new java.util.Date(_dateIn.getTime()));
		time = _dateIn.getTime();
		formatString = "yyyy-MM-dd";
	}

	public cfDateData(java.sql.Time _timeIn) {
		super(new java.util.Date(_timeIn.getTime()));
		time = _timeIn.getTime();
		formatString = "HH:mm:ss";
	}

	public byte getDataType() {
		return cfData.CFDATEDATA;
	}

	public String getDataTypeName() {
		return "date";
	}

	public void setTime(long _time) {
		time = _time;
	}

	public void setString(String _dateString) {
		dateString = _dateString;
	}

	public boolean isDateConvertible() {
		return true;
	}

	public boolean isNumberConvertible() {
		return true;
	}

	public double getDouble() {
		return getDaysSinceCFEpoch();
	}

	public int getInt() {
		return (int) getDaysSinceCFEpoch();
	}

	public long getLong() {
		return time;
	}

	public long getDateLong() {
		return time;
	}

	public cfNumberData getNumberData() {
		return new cfNumberData(getDaysSinceCFEpoch());
	}

	private double getDaysSinceCFEpoch() {
		TimeZone tz = (TimeZone) TimeZone.getDefault().clone();
		long CF_EPOCH = -2209161600000L - tz.getOffset(time);
		return ((double) (time - CF_EPOCH) / DAY);
	}

	private static long getMillisecondsSinceJavaEpoch(double _days) {
		long result = (long) Math.ceil(_days * DAY) + CF_EPOCH;
		TimeZone tz = (TimeZone) TimeZone.getDefault().clone();
		int offset = tz.getOffset(result);
		return result - offset;
	}

	public static cfDateData createDateFromDays(double _days) {
		return new cfDateData(getMillisecondsSinceJavaEpoch(_days));
	}

	public String getString() {
		if (dateString == null) {
			dateString = com.nary.util.Date.formatDate(time, formatString);
			if (formatStringPrefix != null) {
				dateString = "{" + formatStringPrefix + " '" + dateString + "'}";
			}
		}
		return dateString;
	}

	public cfData duplicate() {
		cfDateData copy = new cfDateData(time);
		copy.dateString = this.dateString;
		return copy;
	}

	public void setString() {
		dateString = "{ts '" + com.nary.util.Date.formatDate(time, "yyyy-MM-dd HH:mm:ss") + "'}";
	}

	public void setODBCDate() {
		dateString = "{d '" + com.nary.util.Date.formatDate(time, "yyyy-MM-dd") + "'}";
	}

	public void setODBCTime() {
		dateString = "{t '" + com.nary.util.Date.formatDate(time, "HH:mm:ss") + "'}";
	}

	public void setODBCDateTime() {
		dateString = "{ts '" + com.nary.util.Date.formatDate(time, "yyyy-MM-dd HH:mm:ss") + "'}";
	}

	public void setPOPDate() {
		// gets the date in the format "Wed 2 Apr 2002 12:02:02 GMT +100"
		dateString = com.nary.util.Date.formatDate(time, "E, d MMM yyyy HH:mm:ss z", true);
	}

	public java.util.Calendar getCalendar() {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTime(new java.util.Date(time));
		return c;
	}

	public java.util.Calendar getCalendar(Locale _locale) {
		java.util.Calendar c = java.util.Calendar.getInstance(_locale);
		c.setTime(new java.util.Date(time));
		return c;
	}

	public cfDateData getDateData() {
		return this;
	}

	// this version of equals() is for use by the CFML expression engine
	public boolean equals(cfData _data) throws cfmRunTimeException {
		if (_data.getDataType() == cfData.CFDATEDATA) {
			return ((cfDateData) _data).time == this.time;
		} else if (_data.getDataType() == cfData.CFSTRINGDATA) {
			try {
				String dateStr = _data.getString();
				dateTimeTokenizer dtt = new dateTimeTokenizer(dateStr, dateTimeTokenizer.NEUTRAL);
				if (dtt.validateStructure()) {
					return dtt.getDate().getTime() == this.time;
				}
			} catch (Exception e) {
			}
		}
		return super.equals(_data); // throw an exception
	}

	// this version of equals() is for use by generic Collections classes
	public boolean equals(Object o) {
		if (o instanceof cfDateData) {
			return ((cfDateData) o).time == this.time;
		} else if (o instanceof cfStringData) {
			try {
				String dateStr = ((cfStringData) o).getString();
				dateTimeTokenizer dtt = new dateTimeTokenizer(dateStr, dateTimeTokenizer.NEUTRAL);
				if (dtt.validateStructure()) {
					return dtt.getDate().getTime() == this.time;
				}
			} catch (Exception e) {
			}
		}

		return false;
	}

	public int hashCode() {
		return new Long(time).hashCode();
	}

	public void dump(java.io.PrintWriter out) {
		dump(out, "", -1);
	}

	public void dump(java.io.PrintWriter out, String _label, int _top) {
		out.print(toString());
	}

	public String toString() {
		return getString();
	}

	public void dumpWDDX(int version, java.io.PrintWriter out) {
		if (version > 10)
			out.write("<d>");
		else
			out.write("<dateTime>");

		out.write(com.nary.util.Date.formatDate(time, "yyyy-M-d"));
		out.write("T");
		out.write(com.nary.util.Date.formatDate(time, "H:m:s"));

		TimeZone tz = (TimeZone) TimeZone.getDefault().clone();
		int offsetInMins = tz.getRawOffset() / 60000;

		if (offsetInMins < 0) {
			out.write("-");
			offsetInMins *= -1;
		} else {
			out.write("+");
		}

		out.write((int) (offsetInMins / 60) + "");
		out.write(':');
		out.write((int) (offsetInMins % 60) + "");

		if (version > 10)
			out.write("</d>");
		else
			out.write("</dateTime>");
	}
	
	@Override
	public Object getUnderlyingInstance()
	{
		if ( instance == null ){
			// may be null because it has been read in out of a cache (i.e. serialized)
			synchronized( this ){
				if ( instance == null ){
					instance = new java.util.Date( time );
				}
			}
		}
		
		return instance;
	}


}
