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

package com.naryx.tagfusion.expression.function.ls;

import java.text.DateFormat;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class LSTimeFormat extends functionBase {
	private static final long serialVersionUID = 1L;

	public LSTimeFormat() {
		min = 1;
		max = 2;
	}

	public String[] getParamInfo() {
		return new String[] { "date1", "format string; short cuts ('short', 'medium', 'long', 'full') or customized string" };
	}

	public java.util.Map getInfo() {
		return makeInfo("locale", "Formats a time string to a given output using the current session locale", ReturnType.STRING);
	}

	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		cfData d1;
		String formatString;

		if (parameters.size() == 2) {
			// Retrieve the time parameter
			d1 = parameters.get(1);
			if (d1.getString().equals(""))
				return new cfStringData("");

			// Retrieve the mask parameter
			formatString = parameters.get(0).getString();

			if (formatString.equalsIgnoreCase("short")) {
				DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, _session.getLocale());
				String s = df.format(new java.util.Date(d1.getDateData().getLong()));
				return new cfStringData(s);
			} else if (formatString.equalsIgnoreCase("medium")) {
				DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM, _session.getLocale());
				String s = df.format(new java.util.Date(d1.getDateData().getLong()));
				return new cfStringData(s);
			} else if (formatString.equalsIgnoreCase("long")) {
				DateFormat df = DateFormat.getTimeInstance(DateFormat.LONG, _session.getLocale());
				String s = df.format(new java.util.Date(d1.getDateData().getLong()));
				return new cfStringData(s);
			} else if (formatString.equalsIgnoreCase("full")) {
				DateFormat df = DateFormat.getTimeInstance(DateFormat.FULL, _session.getLocale());
				String s = df.format(new java.util.Date(d1.getDateData().getLong()));
				return new cfStringData(s);
			}
		} else {
			// Retrieve the time parameter
			d1 = parameters.get(0);
			if (d1.getString().equals(""))
				return new cfStringData("");

			// The default is short
			String s = DateFormat.getTimeInstance(DateFormat.SHORT, _session.getLocale()).format(new java.util.Date(d1.getDateData().getLong()));
			return new cfStringData(s);
		}

		// If we reach here then it's a custom format string.
		try {
			// Get the formattedTime
			String formattedTime = com.nary.util.Date.cfmlFormatTime(d1.getDateData().getLong(), formatString, _session.getLocale());

			// Return the formatted time
			return new cfStringData(formattedTime);
		} catch (IllegalArgumentException e) {
			cfCatchData catchData = new cfCatchData(_session);
			catchData.setType("Function");
			catchData.setDetail("timeFormat");
			catchData.setMessage("Invalid time mask " + parameters.get(0).getString()); // note
																																									// this
																																									// assumes
																																									// that
																																									// the
																																									// default
																																									// formatString
																																									// is
																																									// valid.
			throw new cfmRunTimeException(catchData);
		} catch (cfmRunTimeException e) {
			throwException(_session, "invalid date/time string:" + parameters.get(0).getString());
		}

		return null;
	}
}
