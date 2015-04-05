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
 *  $Id: Pause.java 2391 2013-07-01 14:03:43Z alan $
 */


package com.naryx.tagfusion.expression.function.ext;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class Pause extends functionBase {
	private static final long serialVersionUID = 1L;

	public Pause(){
  	min = 1; max = 1; 
  	setNamedParams( new String[]{ "seconds" } ); 
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"seconds to pause for"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"Pauses the execution of the request for a given amount of seconds", 
				ReturnType.BOOLEAN );
	} 
  
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		int seconds	= getNamedIntParam(argStruct, "seconds", 2 );
		if ( seconds < 0 )
			return cfBooleanData.TRUE;

		try {
			Thread.sleep( seconds * 1000 );
		} catch (InterruptedException ignore) {}
		
		return cfBooleanData.TRUE;
	}
}
