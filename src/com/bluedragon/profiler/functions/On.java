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
 *  $Id: On.java 2349 2013-03-21 13:12:46Z alan $
 */
package com.bluedragon.profiler.functions;

import com.bluedragon.plugin.PluginManager;
import com.bluedragon.profiler.ProfilerExtension;
import com.bluedragon.vision.engine.CoreServer;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class On extends functionBase {
	private static final long serialVersionUID = 1L;

	public On(){
		min = 3; 
		max = 3;
		setNamedParams( new String[]{ "mongourl", "database", "collection" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"Where the logging data will be logged. In the format of mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]",
			"Name of the database",
			"Name of the collection"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"cfmlbug", 
				"Turns on the high performance profiler to log data straight into MongoDB.  This will only create one connection to the server and use the disk to page data out.  Form, URL, Database and Mongo activity, page performance data is logged", 
				ReturnType.BOOLEAN );
	}

	public cfData execute( cfSession _session, cfArgStructData argStruct )throws cfmRunTimeException{ 
		if ( cfEngine.thisInstance.getRequestListener() == ProfilerExtension.thisInst )
			return cfBooleanData.TRUE;
		
		String mongourl	= getNamedStringParam(argStruct, "mongourl", null );
		if ( mongourl == null || !mongourl.startsWith("mongodb://") )
			throwException(_session, "invalid mongourl parameter");
		
		String database	= getNamedStringParam(argStruct, "database", null );
		if ( database == null )
			throwException(_session, "invalid database parameter");
		
		String collection	= getNamedStringParam(argStruct, "collection", null );
		if ( collection == null )
			throwException(_session, "invalid collection parameter");
		
		// Close off the Vision Plugin if it is active
		CoreServer.thisInst.enable(false);
		
		try {
			ProfilerExtension.setMongo(mongourl, database, collection);
		} catch (Exception e) {
			throwException(_session, e.getMessage() );
		}
		
		cfEngine.thisInstance.registerRequestListener( ProfilerExtension.thisInst );
		PluginManager.getPlugInManager().stopRequestStats();
		ProfilerExtension.thisInst.requestStart(_session);

		cfEngine.log( "ProfilerOn( " + mongourl + ", " + database + ", " + collection + " )" );

	  return cfBooleanData.TRUE;
	}
}