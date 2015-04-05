/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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
 */

/*
 * Created on 18-Feb-2005
 *
 * Handles the background processing for batched queries
 *
 */
package com.naryx.tagfusion.cfm.sql.platform.java;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nary.Debug;
import com.nary.io.FileUtils;
import com.nary.util.LogFile;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.engineListener;
import com.naryx.tagfusion.cfm.sql.cfDataSource;
import com.naryx.tagfusion.cfm.sql.cfSQLQueryData;
import com.naryx.tagfusion.cfm.sql.preparedData;
import com.naryx.tagfusion.cfm.sql.querySlowLog;
import com.naryx.tagfusion.cfm.sql.pool.SQLFailedConnectionException;
import com.naryx.tagfusion.util.dummyServletRequest;
import com.naryx.tagfusion.util.dummyServletResponse;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class queryBatchServer extends Thread implements engineListener {
	static final long serialVersionUID = 1;
	
  private File mainDirectory, spoolDirectory, failedDirectory;
  private String spoolDirectoryString;
	private long uniqueID = System.currentTimeMillis() - 1284680753630L;
	private volatile boolean stayAlive = true;
	private int hashdir1=0, hashdir2=0, MAXDEPTH=9;

	// We can safely reuse these chaps
	private HttpServletRequest dummyReq = new dummyServletRequest();
	private HttpServletResponse dummyRes = new dummyServletResponse();
	
	
  public queryBatchServer( xmlCFML config ) {
		this.setName("CFQUERY Backgrounder");
		mainDirectory = new File( cfEngine.thisPlatform.getFileIO().getWorkingDirectory(), "cfquerybatch" );
		
		try{
			checkAndCreateSpoolDirectory();
		}catch(Exception e){
			cfEngine.log( "-] Failed to create the cfQueryBatchServer: " + e.getMessage() );
			return;
		}

		LogFile.open( "QUERYBATCH", 			new File( mainDirectory, "querybatch.log" ).toString() );
		LogFile.open( "QUERYBATCH-ERROR", new File( mainDirectory, "querybatch-error.log" ).toString() );
		
		cfEngine.registerEngineListener( this );		
		cfEngine.log( "queryBatchServer started: " + mainDirectory );

		setDaemon( true );
		setPriority( Thread.MIN_PRIORITY );
		start();
  }
	
	public int	getQueueSize(){
		List<String>	dirList	= new ArrayList<String>();
		listAllSpool( dirList, spoolDirectory );
		return dirList.size();
	}
  
  public void engineAdminUpdate(xmlCFML config) {}
  public void engineShutdown() {
		stayAlive = false;
		try{
			this.interrupt();
			this.join(3000);
		}catch(Exception E){ LogFile.println( "QUERYBATCH",E);}
		cfEngine.log( "cfQueryBatchServer: Shutdown" );
  }
  
  
  
  /**
   * Accepts a new SQL statement to the batch server for running
   * @param sql
   */
	public void acceptSQL( queryBatchSQL sql	){
		// Get a handle to the directory we want to save this into
		String fileToUse;
		synchronized(this){

			fileToUse = spoolDirectoryString + hashdir1 + File.separator + hashdir2 + File.separator;
			fileToUse += uniqueID + ".batchsql";

			hashdir2 += 1;
			if (hashdir2 > MAXDEPTH){
				hashdir2 = 0;
				hashdir1 += 1;
				if (hashdir1 > MAXDEPTH){
					hashdir1 = 0;
				}
			}

			uniqueID++;
		}

		// Save the file
		com.nary.Debug.saveClass( fileToUse, sql );
	}
	

	
	public void run(){
    // Wait 10secs before creating the tmp session; let the rest of the engine startup
    try{ sleep( 10 * 1000 ); }catch(Exception E){}
    
    while( stayAlive ){
			try{
			  processQueryList();
			  sleep( 5 * 60 * 1000 );
			}catch(Exception E){ 
				LogFile.println( "QUERYBATCH", Debug.getStackTraceAsString(E) );
			}
	  }
	  
    cfEngine.log( "cfQueryBatchServer: thread stop." );
  }
	
	private void processQueryList(){
		
		// If the init code failed to create the spool or failed directory or they have
		// since been deleted then try to re-create them. This is the fix for bug #3309.		
		try{
			checkAndCreateSpoolDirectory();
		}catch(Exception e){
			LogFile.println( "QUERYBATCH", "processQueryList.Error: Failed to create spool/failed directory - " + e.getMessage() );
			return;
		}

		// Get the list of spools to do
		List<String>	dirList	= new ArrayList<String>();
		listAllSpool( dirList, spoolDirectory );

		int count = 0, failedConnectCount = 0, failedSQLCount = 0;
		long timeSplit	= System.currentTimeMillis();
		
		Iterator<String> dirIt = dirList.iterator();
		while ( dirIt.hasNext() ){
			File thisFile	= new File( dirIt.next() );
			dirIt.remove();
			
			int returnCode = runSQL( thisFile );
			if ( returnCode < 0 ){
				
				// rename the file; giving a little hint on what went wrong
				if ( returnCode == -1 ){
					thisFile.renameTo( new File(failedDirectory, "connectfail-" + thisFile.getName()) );
					failedConnectCount++;
				}else{
					thisFile.renameTo( new File(failedDirectory, "sqlfail-" + thisFile.getName()) );
					failedSQLCount++;
				}

			}else{
    		thisFile.delete();
    		count++;
			}
		}

		if ( count > 0 || failedSQLCount > 0 || failedConnectCount > 0 ){
			timeSplit = System.currentTimeMillis() - timeSplit;
		  LogFile.println( "QUERYBATCH", "runSQL: success=" + count + "; SQLFailed=" + failedSQLCount + "; ConnectedFailed=" + failedConnectCount + "; SQL=" + timeSplit + "ms; avg=" + (int)(timeSplit/(count+failedSQLCount+failedConnectCount)) + "ms" );
		}
	}

	
	/**
	 * Executes the actual SQL batch file that is determine here
	 * 
	 * @param filename
	 * @return 0 if all is well; -1 if connection problem; -2 if other problem
	 */
	private int runSQL( File filename ){
		
		cfSession  tmpSession = null;
		queryBatchSQL	sql = null;
	  try{
		  sql	= (queryBatchSQL)com.nary.Debug.loadClass( new BufferedInputStream( new FileInputStream(filename.toString() ), 32000 ) );
		  if ( sql == null )	return -2;
	    
	    tmpSession = new cfSession( dummyReq, dummyRes, cfEngine.thisServletContext);

	    cfDataSource dataSource = new cfDataSource( sql.getDatasourceName(), tmpSession );
	    dataSource.setUsername( sql.getDatasourceUser() );
	    dataSource.setPassword( sql.getDatasourcePass() );
	  
	    cfSQLQueryData	queryData	= new cfSQLQueryData( dataSource );
	    queryData.setQueryString( sql.getSqlString() );

      if ( sql.getQueryParams() != null ){
        Iterator<preparedData> it	= sql.getQueryParams().iterator();
        while ( it.hasNext() )
          queryData.addPreparedData( it.next() );
      }
	    queryData.runQuery( tmpSession );

	    querySlowLog.record( queryData );
	    return 0;

	  }catch(cfmRunTimeException cfe){
	  	LogFile.println( "QUERYBATCH-ERROR", cfe.getMessage() + "; SQL=" + sql.getSqlString() );

	  	Throwable	sqlQ = cfe.getCatchData().getExceptionThrown();
	  	if ( sqlQ != null && sqlQ instanceof SQLFailedConnectionException )
	  		return -1;
	  	else
	  		return -2;
	  }catch(IOException ioe){
	  	LogFile.println( "QUERYBATCH-ERROR", ioe.getMessage() );
	    return -2;	  
	  }catch(Exception e){
	  	LogFile.println( "QUERYBATCH-ERROR", e.getMessage() + "; SQL=" + sql.getSqlString() );
	    return -2;	  
	  } finally {
	  	if ( tmpSession != null )
	  		tmpSession.sessionEnd();
	  }
	}
	
	
	
	/**
	 * Runs down the list of all the directories for purposes of this file
	 * 
	 * @param fileList
	 * @param dir
	 */
	private void listAllSpool( List<String> fileList, File dir) { 
		if (dir.isDirectory()) { 
			String[] children = dir.list(new fileFilter()); 
			if ( children == null )
				return;

			for (int i=0; i<children.length; i++) 
				listAllSpool(fileList, new File(dir, children[i])); 

		} else { 
			fileList.add( dir.getAbsolutePath() );
		} 
	} 
	
	class fileFilter implements FilenameFilter {
		public fileFilter(){} 
			
		public boolean accept( File dir, String name ){
			if( name.indexOf(".batchsql") != -1 || dir.isDirectory() )
				return true;
			else
				return false;
		}
	}

	/**
	 * Creates the levels of directories internally required to support the 
	 * fact we don't want processes to be removing themselves
	 * 
	 * @throws Exception
	 */
	private void checkAndCreateSpoolDirectory() throws Exception {
		if (spoolDirectory == null || !spoolDirectory.isDirectory() )
			spoolDirectory 	= FileUtils.checkAndCreateDirectory( mainDirectory, "spool", false );
		
		spoolDirectoryString	= spoolDirectory.getAbsolutePath();
		if ( !spoolDirectoryString.endsWith(File.separator) )
			spoolDirectoryString += File.separator;

		if (failedDirectory == null || !failedDirectory.isDirectory())
			failedDirectory = FileUtils.checkAndCreateDirectory( mainDirectory, "failed", false );

		
		// spoolDirectory needs to have a level of subdirectories
		for ( int x=0; x <= MAXDEPTH; x++ ){
			File hashDir	= new File(spoolDirectory, String.valueOf(x) );
			
			if ( !hashDir.isDirectory() )
				hashDir.mkdir();
			
			for ( int xx=0; xx <= MAXDEPTH; xx++ ){
				File hashhashDir	= new File(hashDir, String.valueOf(xx) );
				
				if ( !hashhashDir.isDirectory() )
					hashhashDir.mkdir();
			}
		}
	}
}