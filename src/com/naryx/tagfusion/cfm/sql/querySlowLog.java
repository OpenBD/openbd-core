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
 *  $Id: querySlowLog.java 1766 2011-11-04 08:01:37Z alan $
 */

package com.naryx.tagfusion.cfm.sql;

import java.io.File;

import com.nary.util.LogFile;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

/*
 * Permits the monitoring of queries, via CFQUERY/CFSTOREDPROC/Background
 * Works like MySQL's slowlog setting; where by queries are logged that
 * take over a given value.  -1 disables it.
 * 
 * Expressed in seconds
 * 
 * <server>
 *   <cfquery>
 *     <slowlog>10</slowlog>
 *   </cfquery>
 * </server>
 * 
 */

public class querySlowLog extends Object {
	static final long serialVersionUID = 1;

	private static querySlowLog thisInst = null;
	private int	tracerTimeout = -1;
	
	public static void init( xmlCFML config ){
		if ( thisInst != null )
			return;
		else
			thisInst = new querySlowLog( config );
	}
	
	
	private querySlowLog(xmlCFML config){
		try{
			tracerTimeout = config.getInt("server.cfquery.slowlog", -1 );
		}catch(Exception e){
			tracerTimeout = -1;
		}
		
		if ( tracerTimeout >= 0 ){
			cfEngine.log( "querySlowLog tracking SQL activity over: " + tracerTimeout + " seconds");
			tracerTimeout = tracerTimeout * 1000;
			
			File mainDirectory = new File( cfEngine.thisPlatform.getFileIO().getWorkingDirectory(), "slowlog" );
			if ( !mainDirectory.isDirectory() )
				mainDirectory.mkdirs();

			LogFile.open( "SLOWLOG", new File( mainDirectory, "slow.log" ).toString() );
		}
	}

	
	public static void record( cfTag cftag, cfSQLQueryData queryData ){
		if ( thisInst.tracerTimeout != -1 && thisInst.tracerTimeout < queryData.executeTime ){
			StringBuilder sb = new StringBuilder( 128 );
			sb.append( "Time: " + queryData.executeTime + " ms; " + cftag.getTagName() + " @ Line=" + cftag.posLine + "; " + cftag.getFile().getURI() );
			sb.append( "\r\n" );
			sb.append( queryData.queryString );
			LogFile.println( "SLOWLOG", sb.toString() );
		}
	}


	public static void record( cfTag cftag, String procName, long execTime) {
		if ( thisInst.tracerTimeout != -1 && thisInst.tracerTimeout < execTime ){
			LogFile.println( "SLOWLOG", "Time: " + execTime + " ms; ProcName:" + procName + "; " + cftag.getTagName() + " @ Line=" + cftag.posLine + "; " + cftag.getFile().getURI() );
		}
	}


	public static void record( cfSQLQueryData queryData ) {
		if ( thisInst.tracerTimeout != -1 && thisInst.tracerTimeout < queryData.executeTime ){
			StringBuilder sb = new StringBuilder( 128 );
			sb.append( "Time: " + queryData.executeTime + " ms; BackgroundQuery" );
			sb.append( "\r\n" );
			sb.append( queryData.queryString );
			LogFile.println( "SLOWLOG", sb.toString() );
		}
	}
}