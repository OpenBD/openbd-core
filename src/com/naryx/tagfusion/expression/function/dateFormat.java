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

import java.util.Locale;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

public class dateFormat extends functionBase {
	private static final long serialVersionUID = 1L;

	public dateFormat() {
		min = 1;
		max = 2;
		setNamedParams( new String[]{ "date", "mask" } );
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
				"Formats a date string to a given output", 
				ReturnType.STRING );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		long cal;
		String specifiedFormat = null;
		String cfmlFormatString;

		if (isEmptyString(getNamedParam(argStruct, "date")))
			return new cfStringData("");

		cal = getNamedParam(argStruct, "date").getDateData().getLong();
		specifiedFormat = getNamedStringParam(argStruct, "mask", null);
		
		if(specifiedFormat == null) {
			cfmlFormatString = "dd-mmm-yy";
		} else {
			// Convert the format to all lowercase so it won't be case-sensitive
			// (refer to bug #1908)
			cfmlFormatString = specifiedFormat.toLowerCase();
			if (cfmlFormatString.equals("short")) {
				cfmlFormatString = "m/d/yy";
			} else if (cfmlFormatString.equals("medium")) {
				cfmlFormatString = "mmm d, yyyy";
			} else if (cfmlFormatString.equals("long")) {
				cfmlFormatString = "mmmm d, yyyy";
			} else if (cfmlFormatString.equals("full")) {
				cfmlFormatString = "dddd, mmmm d, yyyy";
			}
		}

		try {
			return new cfStringData(com.nary.util.Date.cfmlFormatDate(cal, cfmlFormatString, Locale.US));
		} catch (IllegalArgumentException e) {
			cfCatchData catchData = new cfCatchData(_session);
			catchData.setType("Function");
			catchData.setDetail("DateFormat");
			catchData.setMessage("Invalid date mask. (mask=" + cfmlFormatString + ", specified format=" + specifiedFormat + ")");
			throw new cfmRunTimeException(catchData);
		}
	}

	public static boolean isEmptyString(cfData data) throws dataNotSupportedException {
		// The data.getString() call can be slow for some data types (in particular
		// cfDateData)
		// so only make this call for cfStringData types.
		// NOTE: only the following types are currently passed to this method:
		//
		// 1. cfStringData (ex. "", "10-Oct-06", etc.)
		// 2. cfDateData (ex. Now(), CreateDate(2001, 3, 3), etc.)
		// 3. cfNumberData (ex. 0, 2134214, etc.)
		if (((data.getDataType() == cfData.CFSTRINGDATA) && (data.getString().equals(""))) || data.getDataType() == cfData.CFNULLDATA)
			return true;

		return false;
	}
}
