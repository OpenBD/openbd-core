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
 *  $Id: SmtpStatusFunction.java 1762 2011-11-04 06:12:16Z alan $
 */
package org.alanwilliamson.openbd.plugin.cfsmtp;

import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class SmtpStatusFunction extends functionBase {
	private static final long	serialVersionUID	= 1L;

	public SmtpStatusFunction() {
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
				"Returns back the stats for this SMTP end point; running, totalmails, totalconnections", 
				ReturnType.STRUCTURE );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String	name	= getNamedStringParam( argStruct, "name", null );
		if ( name == null )
			throwException(_session, "missing parameter 'name'");
		
		name	= name.toLowerCase().trim();

		cfStructData s = new cfStructData();
		Map<String,SmtpManager> smtpMap = SmtpExtension.getStore();

		if ( smtpMap.containsKey(name) ){
			SmtpManager smtpManager = smtpMap.get(name);
      s.setData( "running",          cfBooleanData.getcfBooleanData( smtpManager.isServerRunning() ) );
      s.setData( "totalmails",       new cfNumberData( smtpManager.getMailetsRan()) );
      s.setData( "totalconnections", new cfNumberData( smtpManager.getClientsConnected()) );
		} else {
			s.setData( "running",          cfBooleanData.FALSE );
      s.setData( "totalmails",       new cfNumberData(0) );
      s.setData( "totalconnections", new cfNumberData(0) );
		}
		
		return s;
	}
	
}
