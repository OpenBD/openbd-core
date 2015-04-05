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
 *  http://www.openbluedragon.org/
 *  $Id: VisionExtension.java 2177 2012-07-12 23:18:17Z alan $
 */
package com.bluedragon.vision;

import com.bluedragon.plugin.Plugin;
import com.bluedragon.plugin.PluginManagerInterface;
import com.bluedragon.vision.engine.CoreServer;
import com.naryx.tagfusion.xmlConfig.xmlCFML;


public class VisionExtension implements Plugin {	
	
	public static CoreServer coreserver;
	
	public String getPluginDescription() {
		return "Vision";
	}

	public String getPluginName() {
		return "Vision";
	}

	public String getPluginVersion() {
		String r = "$Revision: 2177 $";
		return "1." + r.substring( 10, r.length()-1 ).trim();
	}

	@Override
	public void pluginStart(PluginManagerInterface manager, xmlCFML systemParameters) {
		manager.registerFunction("debuggerstep", 							"com.bluedragon.vision.functions.Step" );
		manager.registerFunction("debuggerstepover", 					"com.bluedragon.vision.functions.StepOver" );
		manager.registerFunction("debuggersteptobreakpoint", 	"com.bluedragon.vision.functions.StepToBreakPoint" );
		manager.registerFunction("debuggersteptoend", 				"com.bluedragon.vision.functions.StepToEnd" );
		manager.registerFunction("debuggergetsessions", 			"com.bluedragon.vision.functions.GetActiveSessions" );
		
		manager.registerFunction("debuggergetrequestcount",				"com.bluedragon.vision.functions.GetRequestCount" );
		manager.registerFunction("debuggergetactiverequestcount",	"com.bluedragon.vision.functions.GetActiveRequestCount" );
		
		manager.registerFunction("debuggergetstats", 							"com.bluedragon.vision.functions.GetStats" );
		
		manager.registerFunction("debuggerkillsession", 					"com.bluedragon.vision.functions.KillSession" );
		manager.registerFunction("debuggerissession", 						"com.bluedragon.vision.functions.SessionActive" );
		manager.registerFunction("debuggerinspectprofilesession", "com.bluedragon.vision.functions.InspectProfileSession" );
		
		manager.registerFunction("debuggersetbreakpoint", 					"com.bluedragon.vision.functions.SetBreakPoint" );
		manager.registerFunction("debuggersetbreakpointonexception", "com.bluedragon.vision.functions.SetBreakPointOnException" );
		manager.registerFunction("debuggergetbreakpointonexception", "com.bluedragon.vision.functions.GetBreakPointOnException" );
		
		manager.registerFunction("debuggerclearbreakpoint", 		"com.bluedragon.vision.functions.ClearBreakPoint" );
		manager.registerFunction("debuggergetbreakpoints", 			"com.bluedragon.vision.functions.GetBreakPoints" );
		manager.registerFunction("debuggerclearallbreakpoints", "com.bluedragon.vision.functions.ClearAllBreakPoints" );

		manager.registerFunction("debuggerinspect", 						"com.bluedragon.vision.functions.InspectVar" );
		manager.registerFunction("debuggerinspecttopscopes", 		"com.bluedragon.vision.functions.InspectTopScopes" );
		manager.registerFunction("debuggerinspectlocalscope", 	"com.bluedragon.vision.functions.InspectLocalsVar" );
		manager.registerFunction("debuggerinspectfilestack", 		"com.bluedragon.vision.functions.InspectFileStack" );
		manager.registerFunction("debuggerinspecttagstack", 		"com.bluedragon.vision.functions.InspectTagStack" );
		manager.registerFunction("debuggerinspectquerystack",		"com.bluedragon.vision.functions.InspectQuery" );
		
		manager.registerFunction("debuggerenable",			"com.bluedragon.vision.functions.EnableDisable" );
		manager.registerFunction("debuggerisenabled",		"com.bluedragon.vision.functions.IsEnabled" );

		coreserver	= new CoreServer();
	}

	public void pluginStop(PluginManagerInterface manager) {}
	
}