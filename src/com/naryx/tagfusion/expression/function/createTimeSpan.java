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
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class createTimeSpan extends functionBase {
  
  private static final long serialVersionUID = 1L;
	
  public createTimeSpan(){
  	min = max = 4;
  	setNamedParams( new String[]{ "day", "hour", "minute", "second" } );
  }
  
  public String[] getParamInfo(){
		return new String[]{
			"day",
			"hour (0-23)",
			"minute",
			"second"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"date", 
				"Creates a CFML time object that represents a time span/range", 
				ReturnType.NUMERIC );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
  	
    //--[ get days, hours, minutes, seconds
  	int day 	= getNamedIntParam( argStruct, "day", 1 );
  	int hour 	= getNamedIntParam( argStruct, "hour", 1 );
  	int min 	= getNamedIntParam( argStruct, "minute", 1 );
  	int sec 	= getNamedIntParam( argStruct, "second", 1 );

    long  timeOverall = (day*86400) + (hour*3600) + (min*60) + (sec);
    return new cfNumberData( (double)((double)timeOverall / (double)86400) );
  }
}
