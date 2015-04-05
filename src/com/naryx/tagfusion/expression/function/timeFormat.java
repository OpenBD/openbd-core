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

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class timeFormat extends functionBase {
	private static final long serialVersionUID = 1L;

	public timeFormat() {
		min = 1;
		max = 2;
		setNamedParams( new String[]{ "date", "format" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"date1",
			"format string; short cuts ('short', 'medium', 'long', 'full') or customized string"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"date", 
				"Formats a time string to a given output", 
				ReturnType.STRING );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		cfData d1 = getNamedParam( argStruct, "date" );
		String cfmlFormatString;

			// Retrieve the time parameter
			if (d1.getString().equals(""))
				return new cfStringData("");

			// Retrieve the mask parameter
			cfmlFormatString = getNamedStringParam( argStruct, "format", "hh:mm tt" );

			if (cfmlFormatString.equalsIgnoreCase("short")) {
				cfmlFormatString = "h:mm tt";
			} else if (cfmlFormatString.equalsIgnoreCase("medium")) {
				cfmlFormatString = "h:mm:ss tt";
			} else if ((cfmlFormatString.equalsIgnoreCase("long")) || (cfmlFormatString.equalsIgnoreCase("full"))) {
				return new cfStringData(new SimpleDateFormat("h:mm:ss aa zz", new DateFormatSymbols(Locale.US)).format(new java.util.Date(d1.getDateData().getLong())));
			}


		try {
			// Get the formattedTime
			String formattedTime = com.nary.util.Date.cfmlFormatTime(d1.getDateData().getLong(), cfmlFormatString, Locale.US);

			// Return the formatted time
			return new cfStringData(formattedTime);
		} catch (IllegalArgumentException e) {
			cfCatchData catchData = new cfCatchData(_session);
			catchData.setType("Function");
			catchData.setDetail("timeFormat");
			catchData.setMessage("Invalid time mask " + getNamedStringParam( argStruct, "format", null )); // note
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
			throwException(_session, "invalid date/time string:" + getNamedStringParam( argStruct, "date", null ));
		}

		return null;
	}

}
