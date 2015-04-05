/*
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: createTime.java 2400 2013-08-19 12:02:12Z alan $
 */

package com.naryx.tagfusion.expression.function;

import java.util.Calendar;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class createTime extends functionBase {

	private static final long serialVersionUID = 1L;

	public createTime() {
		min = 1; max = 5;
		setNamedParams( new String[]{ "hour", "minute", "second", "ms", "epoch" } );
	}

  @Override
	public String[] getParamInfo(){
		return new String[]{
			"hour (0-23)",
			"minute",
			"second",
			"milliseconds - defaults to 0",
			"if used then creates a date from the epoch time"
		};
	}

	@Override
	public java.util.Map getInfo(){
		return makeInfo(
				"date",
				"Creates a CFML time object from the given hour, minute, second and millisecond, or from the epoch",
				ReturnType.DATE );
	}


	@Override
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		long	epoch	= getNamedLongParam(argStruct, "epoch", Long.MIN_VALUE );

		if ( epoch != Long.MIN_VALUE ){

			return new cfDateData( epoch );

		}else{

			int hour 	= getNamedIntParam( argStruct, "hour", 1 );
			int min 	= getNamedIntParam( argStruct, "minute", 1 );
			int sec 	= getNamedIntParam( argStruct, "second", 1 );
			int ms 		= getNamedIntParam( argStruct, "ms", 0 );

			if (0 > hour || hour > 23 || 0 > min || min > 59 || 0 > sec || sec > 59)
				throwException(_session, "the hour/min/sec is out of range");

			// create a java Calendar object and get its time in milliseconds
			Calendar c = Calendar.getInstance();
			c.set(1899, 11, 30, hour, min, sec);
			c.set( Calendar.MILLISECOND, ms );
			return new cfDateData(c.getTime().getTime());

		}

	}
}
