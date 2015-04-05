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

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class createDate extends functionBase {

	private static final long serialVersionUID = 1L;

	public createDate() {
		min = max = 3;
		setNamedParams( new String[]{ "year", "month", "day" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"year",
			"month (1-12)",
			"day"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"date", 
				"Creates a CFML date object from the given year, month and day", 
				ReturnType.DATE );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		// --[ get year, month, and date parameters
		int year = getNamedIntParam( argStruct, "year", 1 );
		int month = getNamedIntParam( argStruct, "month", 1 ) - 1;
		int day = getNamedIntParam( argStruct, "day", 1 );

		// Get the time in milliseconds
		long time = -1;
		try {
			time = createDateTime.getTime(year, month, day, 0, 0, 0, 0);
		} catch (IllegalArgumentException e) {
			throwException(_session, e.getMessage());
		}

		try {
			return new cfDateData(time);
		} catch (java.lang.IllegalArgumentException e) {
			throwException(_session, "Invalid argument. Check the values provided are in the correct range.");
		}
		return null; // keep the compiler happy

	}
}
