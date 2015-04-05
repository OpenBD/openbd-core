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
 *  $Id: GetActiveSessions.java 2122 2012-06-22 10:59:34Z alan $
 */
package com.bluedragon.vision.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.bluedragon.vision.engine.CoreServer;
import com.bluedragon.vision.engine.VisionLiveSession;
import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.expression.function.functionBase;

public class GetActiveSessions extends functionBase {
	private static final long serialVersionUID = 1L;

	public GetActiveSessions(){
		min = max = 0;
	}
	
	public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException { 
		cfQueryResultData queryResult = new cfQueryResultData(new String[] { "id", "uri", "pf", "f", "ip", "time", "bytes", "paused", "isterm", "onexception", "tag", "line", "ct", "cl" }, "GetActiveSessions");
	  
	  HashMap<Integer,VisionLiveSession>	activeSessions	= CoreServer.thisInst.getActiveSessions();
	  List<Map<String, cfData>> vResults	= new ArrayList<Map<String, cfData>>();
	  
	  String rootPath	= FileUtils.getRealPath("/").replace('\\','/');
	  if ( !rootPath.endsWith("/") )
	  	rootPath	+= "/";
	  
	  Iterator<VisionLiveSession>	it	= activeSessions.values().iterator();
	  while (it.hasNext() ){
	  	VisionLiveSession ds = it.next();

	  	HashMap<String, cfData>	sd = new HashMap<String, cfData>();
	  	
	  	sd.put( "id", 		new cfNumberData(ds.getCFSession().getSessionID()) );
	  	sd.put( "time", 	new cfNumberData(ds.getExecTime()));
	  	sd.put( "bytes", 	new cfNumberData(ds.getCFSession().getBytesSent()));
	  	sd.put( "ip", 		new cfStringData(ds.getCFSession().getRemoteIP()) );
	  	sd.put( "uri",    new cfStringData(ds.getCFSession().REQ.getContextPath() + ds.getCFSession().REQ.getServletPath() ) );
	  	
	  	cfTag	currentTag	= ds.getProfileCurrentTag();
	  	if ( currentTag != null ){
	  		sd.put( "ct", 	new cfStringData(currentTag.getTagName()) );
	  		sd.put( "cl", 	new cfNumberData(currentTag.posLine) );
	  	}
	  	
	  	String filePath	= ds.getActiveFile();
	  	sd.put( "f", 			new cfStringData( filePath ) );
	  	if ( filePath.startsWith( rootPath ) ){
	  		sd.put( "pf", 	new cfStringData( filePath.substring(rootPath.length()-1) ) );
	  	}else{
	  		sd.put( "pf", 	new cfStringData( filePath ) );
	  	}

	  	sd.put( "paused", 			cfBooleanData.getcfBooleanData( ds.isStopped() ) );
	  	sd.put( "onexception", 	cfBooleanData.getcfBooleanData( ds.isOnException() ) );
	  	sd.put( "isterm", 			cfBooleanData.getcfBooleanData( ds.isTerminating() ) );
	  	
	  	if ( ds.getStoppedTag() != null ){
	  		sd.put( "tag", 		new cfStringData( ds.getStoppedTag().getTagName() )  );
	  		sd.put( "line", 	new cfNumberData( ds.getCurrentLine() )  );
	  	}else{
	  		sd.put( "tag", 		new cfStringData("") );
	  		sd.put( "line", 	new cfNumberData( 0 ) );
	  	}
	  	
	  	vResults.add( sd );
	  }
	  
	  queryResult.populateQuery( vResults );
	  return queryResult;
	}
}