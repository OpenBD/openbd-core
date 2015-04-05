/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: SalesForceExtension.java 2177 2012-07-12 23:18:17Z alan $
 */
package org.alanwilliamson.openbd.plugin.salesforce;

import com.bluedragon.plugin.Plugin;
import com.bluedragon.plugin.PluginManagerInterface;
import com.naryx.tagfusion.xmlConfig.xmlCFML;


public class SalesForceExtension implements Plugin {
	public String getPluginDescription() {
		return "OpenBD:SalesForce";
	}

	public String getPluginName() {
		return "SalesForce";
	}

	public String getPluginVersion() {
		String r = "$Revision: 2177 $";
		return "1." + r.substring( r.indexOf(" "), r.length()-1 ).trim();
	}

	public void pluginStart(PluginManagerInterface manager, xmlCFML systemParameters) {
		manager.registerFunction("salesforcequery", 				"org.alanwilliamson.openbd.plugin.salesforce.SalesForceQuery" );
		manager.registerFunction("salesforcequerycallback", "org.alanwilliamson.openbd.plugin.salesforce.SalesForceQueryCallback" );
		
		manager.registerFunction("salesforcecreate", 		"org.alanwilliamson.openbd.plugin.salesforce.SalesForceCreate" );
		manager.registerFunction("salesforceupdate", 		"org.alanwilliamson.openbd.plugin.salesforce.SalesForceUpdate" );
		manager.registerFunction("salesforcedelete", 		"org.alanwilliamson.openbd.plugin.salesforce.SalesForceDelete" );
		manager.registerFunction("salesforcedescribe",	"org.alanwilliamson.openbd.plugin.salesforce.SalesForceDescribe" );
	}
	
	public void pluginStop(PluginManagerInterface manager) {}
}