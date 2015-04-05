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
 *  $Id: SmtpStopFunction.java 1762 2011-11-04 06:12:16Z alan $
 */
package org.alanwilliamson.openbd.plugin.cfsmtp;

import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class SmtpStopFunction extends functionBase {
	private static final long	serialVersionUID	= 1L;

	public SmtpStopFunction() {
		min = 1; max = 1;
		setNamedParams( new String[]{ "name" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"Symbolic name to name this SMTP end point"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"cfsmtp-plugin", 
				"Stops a running SMTP server.  Returns true if it was stopped; false if it wasn't running", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String	name	= getNamedStringParam( argStruct, "name", null );
		if ( name == null )
			throwException(_session, "missing parameter 'name'");
		
		name	= name.toLowerCase().trim();

		Map<String,SmtpManager> smtpMap = SmtpExtension.getStore();
		if ( smtpMap.containsKey(name) ){
			smtpMap.get(name).stopServer();
			smtpMap.remove(name);
			return cfBooleanData.TRUE;
		}else{
			return cfBooleanData.FALSE;
		}
	}
	
}
