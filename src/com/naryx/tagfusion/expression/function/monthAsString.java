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
import java.util.Locale;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.ls.setLocale;

public class monthAsString extends functionBase {

	private static final long serialVersionUID = 1L;

	public monthAsString() {
		min = 1;
		max = 2;
		setNamedParams( new String[]{ "month", "locale" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"month number - (1=Jan .. 12=Dec)",
			"locale"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"date", 
				"Returns the string label for the given month index in the given locale (if supplied)", 
				ReturnType.STRING );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		Locale locale;
		int month;
			String localeStr = getNamedStringParam( argStruct, "locale", null );
			if ( localeStr == null ){
				locale = _session.getLocale();
			}else{
				locale = setLocale.getLocale(localeStr);
			}
			if (locale == null) {
				throwException(_session, localeStr + " must be a valid locale name");
			}
			month = getNamedIntParam( argStruct, "month", 1);

		if (month < 1 || month > 12)
			throwException(_session, "month must be within 1 and 12");

		DateFormatSymbols dfs = new DateFormatSymbols(locale);
		return new cfStringData(dfs.getMonths()[month - 1]);
	}
}
