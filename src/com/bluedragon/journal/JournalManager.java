/*
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: JournalManager.java 2528 2015-02-26 20:20:37Z alan $
 */
package com.bluedragon.journal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.aw20.io.StreamUtil;

import com.bluedragon.platform.java.WalkDirectory;
import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.expression.function.hashBinary;

public class JournalManager implements IJournalManager {

	private String password, ignoreUri;
	private static final String JOURNAL_PARAM = "_openbdjournal";
	private File journalDir = null;
	private int filecount = 0;
	private long journalDirSize = 0, bytesLimit = 0;

	public JournalManager(){
		password	= cfEngine.thisInstance.getSystemParameters().getString("server.system.journal");
		if ( password != null && password.isEmpty() )
			password = null;

		ignoreUri	= cfEngine.thisInstance.getSystemParameters().getString("server.system.journalignoreuri");
		if ( ignoreUri == null || ( ignoreUri != null && ignoreUri.isEmpty() ) )
			ignoreUri = "/journal";
		else if ( ignoreUri.charAt(0) != '/' )
			ignoreUri = "/" + ignoreUri;

		int mblimit	= cfEngine.thisInstance.getSystemParameters().getInt("server.system.journalmb",50);
		bytesLimit = mblimit * 1000000;

		if ( password == null )
			cfEngine.log("JournalManager: Disabled; Set [server.system.journal] to enable" );
		else
			cfEngine.log("JournalManager: Enabled; URI parameter/Cookie/Header " + JOURNAL_PARAM + "=" + password + "; LogLimit=" + mblimit + " MB; ignoreuri=" + ignoreUri );

		try {
			journalDir			= FileUtils.checkAndCreateDirectory(cfEngine.thisPlatform.getFileIO().getWorkingDirectory(), "journal", false);
			journalDirSize	= new WalkDirectory(journalDir).getTotalFileSize();
		} catch (Exception e) {
			cfEngine.log("JournalManager: Disabled; failed to create directory: " + cfEngine.thisPlatform.getFileIO().getWorkingDirectory() );
		}
	}


	/**
	 * Determines if this request can be instrumented.  We are looking for the JOURNAL_PARAM to appear in either
	 * the URI params, an HTTP header, or the cookie.  This has to match the value of the param to ensure no undue
	 * load or hacking attempts.
	 *
	 * @param req
	 * @return
	 */
	public JournalSession	getJournalSession( HttpServletRequest req ){
		if ( password == null )
			return null;

		// Check to see if it is in the request parameters
		String v = req.getParameter(JOURNAL_PARAM);
		if ( v != null && v.startsWith(password) && !req.getRequestURI().startsWith(ignoreUri) ){
			return new JournalSession( this, v.endsWith("-1") );
		}

		// Check to see if it is in cookie
		Cookie[] cookies = req.getCookies();
		if ( cookies != null && cookies.length > 0 ){
			for ( int x=0; x < cookies.length; x++ ){
				if ( cookies[x].getName().equals(JOURNAL_PARAM) && cookies[x].getValue().startsWith(password) && !req.getRequestURI().startsWith(ignoreUri) )
					return new JournalSession( this, cookies[x].getValue().endsWith("-1") );
			}
		}

		// Check to see if the header is there
		v = req.getHeader(JOURNAL_PARAM);
		if ( v != null && v.startsWith(password) && !req.getRequestURI().startsWith(ignoreUri) )
			return new JournalSession( this, v.endsWith("-1") );

		// no param was found
		return null;
	}

	@Override
	public int getFileCount(){
		filecount++;
		if (filecount > 1000 )
			filecount = 0;

		return filecount;
	}

	@Override
	public void onRequestEnd(JournalSession journalSession) {

		cfSession session	= journalSession.getSession();

		File logFile	= new File( journalSession.getDirectory(), journalSession.getLogFileName() );

		PrintWriter	pwriter = null;
		try{
			List<String> logList = journalSession.getTraceList();
			Map<String,Integer> fileIndexMap = journalSession.getFileMap();

			pwriter	= new PrintWriter( new BufferedWriter( new FileWriter(logFile) ) );


			// Print the header
			pwriter.print("{");
			pwriter.print( "\"_uri\":\"" + session.getRequestURI() );
			if ( session.REQ.getQueryString() != null && !session.REQ.getQueryString().isEmpty() )
				pwriter.print( "?" + session.REQ.getQueryString().replace('"', '\'') );
			pwriter.print( "\"" );

			pwriter.print( ",\"_querycount\":" + session.getMetricQuery() );
			pwriter.print( ",\"_querytime\":" + session.getMetricQueryTotalTime() );
			pwriter.print( ",\"_bytes\":" + session.getBytesSent() );
			pwriter.print( ",\"_timems\":" + (System.currentTimeMillis()-journalSession.getRequestEpoch()) );
			pwriter.print( ",\"_method\":\"" + session.REQ.getMethod() + "\"" );
			pwriter.print( ",\"_session\":" + (journalSession.hasSession() ? "true" : "false") + "" );

			// Add the file map
			pwriter.print( "," );

			String hash;
			hashBinary	hasher = new hashBinary();
			Iterator<Entry<String,Integer>> fit = fileIndexMap.entrySet().iterator();
			while ( fit.hasNext() ){
				Entry<String,Integer>	e = fit.next();

				File f = new File(e.getKey());
				if ( f.exists() )
					hash = hasher.getFileHash( f, "MD5");
				else
					hash = "abcd1234";

				pwriter.print( "\"" + e.getKey().replace('\\','/') + "\":{\"id\":" + e.getValue() + ",\"hash\":\"" + hash + "\"}," );
			}
			pwriter.println( "\"root\":{\"id\":0,\"hash\":\"0\"}}" );


			// Print the trace
			Iterator<String> it	= logList.iterator();
			while ( it.hasNext() )
				pwriter.println( it.next() );

			pwriter.flush();
		}catch(Exception e){
			cfEngine.log("JournalManager.onRequestEnd: " + e.getMessage() );
		}finally{
			StreamUtil.closeStream( pwriter );
		}

		onLogFileWrite(logFile);
	}

	private synchronized void onLogFileWrite(File logfile){
		journalDirSize += logfile.length();

		if ( journalDirSize < bytesLimit )
			return;

		// We are over the size limit now; so let us delete the older files and come down to 90% of disk space
		long bytesLimitNinetyPercent	= (long)(bytesLimit * 0.95);

		WalkDirectory	walker = new WalkDirectory( journalDir );

		Iterator<File> it	= walker.iteratorAllFiles();
		long totalSize = 0, totalDeleted = 0;
		int filesDeleted = 0;

		while ( it.hasNext() ){
			File f = it.next();

			totalSize += f.length();

			if ( totalSize > bytesLimitNinetyPercent ){
				totalDeleted += f.length();
				f.delete();
				filesDeleted++;
				totalSize -= f.length();
			}
		}

		cfEngine.log("JournalManager: Disk clean up; files removed=" + filesDeleted + "; bytes=" + totalDeleted );

		// reset the disk space
		journalDirSize	= walker.getTotalFileSize();
	}

	@Override
	public File getDirectory() {
		return journalDir;
	}

	@Override
	public void addBytesWritten(long bytes){
		journalDirSize += bytes;
	}

}