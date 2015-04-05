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
 *  $Id: SmtpExtension.java 1762 2011-11-04 06:12:16Z alan $
 */
package org.alanwilliamson.openbd.plugin.cfsmtp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.bluedragon.plugin.Plugin;
import com.bluedragon.plugin.PluginManagerInterface;
import com.naryx.tagfusion.xmlConfig.xmlCFML;


public class SmtpExtension implements Plugin {	
	static private Map<String,SmtpManager> smtpManagerMap = null;
	
	public static Map<String,SmtpManager> getStore(){
		return smtpManagerMap;
	}
	
	public String getPluginDescription() {
		return "OpenBD:CFSMTP";
	}

	public String getPluginName() {
		return "CFSMTP";
	}

	public String getPluginVersion() {
		return "1.2011.11.4";
	}

	public void pluginStart(PluginManagerInterface manager, xmlCFML systemParameters) {
		smtpManagerMap	= new HashMap<String,SmtpManager>();
		manager.registerTag( "cfsmtp", "org.alanwilliamson.openbd.plugin.cfsmtp.cfSMTP" );
		
		manager.registerFunction("mailread", 		"org.alanwilliamson.openbd.plugin.cfsmtp.MailReadFunction");
		manager.registerFunction("mailwrite", 	"org.alanwilliamson.openbd.plugin.cfsmtp.MailWriteFunction");
		manager.registerFunction("maildeliver", "org.alanwilliamson.openbd.plugin.cfsmtp.MailDeliverFunction");
		
		manager.registerFunction("smtpstart", 	"org.alanwilliamson.openbd.plugin.cfsmtp.SmtpStartFunction");
		manager.registerFunction("smtpstop", 		"org.alanwilliamson.openbd.plugin.cfsmtp.SmtpStopFunction");
		manager.registerFunction("smtpstatus",	"org.alanwilliamson.openbd.plugin.cfsmtp.SmtpStatusFunction");
	}

	
	public void pluginStop(PluginManagerInterface manager) {
		Iterator<SmtpManager>	it = smtpManagerMap.values().iterator();
		while ( it.hasNext() )
			it.next().stopServer();
	}
}