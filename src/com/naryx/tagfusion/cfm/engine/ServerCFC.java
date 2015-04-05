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
 *  $Id: ServerCFC.java 2144 2012-07-01 23:03:13Z alan $
 */
package com.naryx.tagfusion.cfm.engine;

import com.bluedragon.plugin.ObjectCFC;
import com.bluedragon.plugin.PluginManager;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class ServerCFC extends Object {
	static final long serialVersionUID = 1;

	public void onServerStart(xmlCFML systemParameters) {
		String cfcpath = systemParameters.getString("server.system.servercfc", "/Server" );
		if ( cfcpath.endsWith(".cfc") )
			cfcpath	= cfcpath.substring( 0, cfcpath.length()-4 );
		
		cfSession session	= PluginManager.getPlugInManager().createBlankSession();

		ObjectCFC serverCFC;
		try {
			serverCFC = PluginManager.getPlugInManager().createCFC( session, cfcpath );
			cfEngine.log( "/Server.cfc found: " + cfcpath + ".cfc; RealPath: " + serverCFC.getComponentCFC().getComponentFile().getCfmlURI().getRealPath() );
		}catch(Exception e){
			cfEngine.log( "/Server.cfc not available: " + cfcpath + ".cfc; exception:" + e.getMessage() );
			return;
		}
		
		
		// Execute the method
		try{
			serverCFC.runMethod(session, "onServerStart" );
			cfEngine.log( "/Server.cfc Executed Successfully" );
		} catch (Exception rte) {
			cfEngine.log( "/Server.cfc Execution Error: " + rte.getMessage() );
		}
	}
}
