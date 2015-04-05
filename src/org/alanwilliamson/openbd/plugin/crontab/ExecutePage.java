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
 *  http://openbd.org/
 *  $Id: ExecutePage.java 2082 2012-05-07 17:34:00Z andy $
 */
package org.alanwilliamson.openbd.plugin.crontab;

import java.io.File;

import com.bluedragon.plugin.PluginManager;
import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.util.dummyServletRequest;
import com.naryx.tagfusion.util.dummyServletResponse;

/*
 * Manages the execution of a given URI, utilising a direct call to the cfEngine
 * class.  This is a more efficient manner than going out to the ServletEngine
 * and back. 
 */
public class ExecutePage extends Object {
	private String uri;
	private File logFile;
	
	public ExecutePage( File outputDir, String uri ) throws Exception{
		this.uri			= uri;
		this.logFile	= new File( outputDir, uri.substring( uri.lastIndexOf("/")+1 ) + ".htm" );
	}
	
	public void service(){
		dummyServletRequest 	req = new CronServletRequest();
		dummyServletResponse 	res	= new dummyServletResponse( true );
		
		PluginManager.getPlugInManager().log( "CronPlugin.ExecutePage(" + uri + ")" );
		
		try {
			cfEngine.service(req, res);
			
			if ( logFile.exists() )
				logFile.delete();
			
			FileUtils.writeFile( logFile, res.getOutput() );
		} catch (Throwable e) {
			PluginManager.getPlugInManager().log( "CronPlugin.ExecutePage(" + uri + ").Exception: " + e.getMessage() );
		}
	}
	

	
	/*
	 * Inner request wrapper for
	 */
	class CronServletRequest extends dummyServletRequest {
		public CronServletRequest(){
			super( cfEngine.thisServletContext.getRealPath("/") );
		}
		
		public String getServletPath() {
			return uri;
		}
	}
}