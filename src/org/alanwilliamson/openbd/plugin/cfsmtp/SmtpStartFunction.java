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
 *  $Id: SmtpStartFunction.java 1762 2011-11-04 06:12:16Z alan $
 */
package org.alanwilliamson.openbd.plugin.cfsmtp;

import java.util.Map;

import com.bluedragon.plugin.PluginManager;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class SmtpStartFunction extends functionBase {
	private static final long	serialVersionUID	= 1L;

	public SmtpStartFunction() {
		min = 2; max = 8;
		setNamedParams( new String[]{ "name", "cfcmail", "cfcfilter", "ip", "port", "application", "maxconnections", "banner" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"Symbolic name to name this SMTP end point",
			"CFC that will be created for each email that comes in.  The method onMailAccept( mail ) must be present.  A new instance is created for each email",
			"IP address to bind the listening to; defaults to all interfaces",
			"Port to listen for incoming mail SMTP requests; defaults to 25",
			"CFC that will be created to handle the filtering of the email.  You can opt to receive an email or not very early on, saving lots of bandwidth by implementing the method onMailTo(email,ip) and onMailFrom(email,ip) returning back true or false depending on if you wish to receive this email",
			"The name of the CFAPPLICATION that is to be present when executing a CFC inside a mail request",
			"The total number of concurrent connections this mail will handle at a time.  Default 5",
			"The banner that is presented to incoming connections"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"cfsmtp-plugin", 
				"Starts up a listening service for incoming emails, calling a CFC in response to all mails", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String	name	= getNamedStringParam( argStruct, "name", null );
		if ( name == null )
			throwException(_session, "missing parameter 'name'");
		
		name	= name.toLowerCase().trim();

		Map<String,SmtpManager> smtpMap = SmtpExtension.getStore();
		if ( smtpMap.containsKey(name) )
			throwException(_session, "This '" + name + "' is already started");

		
		String	ip					= getNamedStringParam( argStruct, "ip", null );
		int port						= getNamedIntParam( argStruct, "port", 25 );
		int maxconnections	= getNamedIntParam( argStruct, "maxconnections", 5 );
	
		String banner						= getNamedStringParam( argStruct, "banner", null );
		String cfcmail					= getNamedStringParam( argStruct, "cfcmail", null );
		String cfcfilter				= getNamedStringParam( argStruct, "cfcfilter", null );
		String applicationName	= getNamedStringParam( argStruct, "application", null );
		if ( cfcmail == null )
			throwException(_session, "missing parameter 'cfcmail'");
		

		// Check to see the cfc's are valid
		try {
			PluginManager.getPlugInManager().createCFC( _session, cfcmail ).getComponentCFC();
		} catch (Exception e) {
			throwException( _session, cfcmail + ":" + e.getMessage() );
		}
		
		if ( cfcfilter != null ){
			try {
				PluginManager.getPlugInManager().createCFC( _session, cfcfilter ).getComponentCFC();
			} catch (Exception e) {
				throwException( _session, cfcfilter + ":" + e.getMessage() );
			}
		}
		
		// Start it and register it
		try {
			SmtpManager	smtpManager	= new SmtpManager( name, ip, port, cfcmail, cfcfilter, applicationName, maxconnections, banner );
			smtpMap.put(name, smtpManager);
		} catch (Exception e) {
			throwException( _session, e.getMessage() );
		}

		return cfBooleanData.TRUE;
	}
	
}
