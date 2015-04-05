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
 *  $Id: JournalSession.java 2503 2015-02-04 14:53:31Z alan $
 */
package com.bluedragon.journal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aw20.io.ByteArrayOutputStreamRaw;
import org.aw20.io.FileUtil;
import org.aw20.io.StreamUtil;
import org.aw20.util.DateUtil;

import com.bluedragon.plugin.RequestListener;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.parser.script.CFParsedStatement;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.cfm.tag.cfFUNCTION;
import com.naryx.tagfusion.cfm.tag.cfTRY;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.util.debuggerListener;

public class JournalSession implements debuggerListener, RequestListener {

	private final IJournalManager journalmanager;

	private final long msEpoch;

	private int fileId = 0;
	private final Map<String,Integer>	fileIndexMap;

	private cfSession thisSession;
	private final List<String>	logList;
	private final StringBuilder sb;
	private File outputDir;
	private final String logFileName;
	private final boolean bSaveSession;

	private int sessionPagedCount = 0; 
	private BufferedOutputStream fileOutputBufferedOS = null;
	private ByteArrayOutputStreamRaw previousSession = null;

	public JournalSession( IJournalManager journalmanager, boolean bSaveSession ){
		this.journalmanager = journalmanager;
		this.bSaveSession 	= bSaveSession;
		
		fileIndexMap	= new HashMap<String,Integer>();
		logList				= new LinkedList<String>();

		msEpoch				= System.currentTimeMillis();
		sb 						= new StringBuilder(64);
		
		logFileName		= DateUtil.getDateString( System.currentTimeMillis(), "yyyy-MM-dd_HH.mm.ss-" + journalmanager.getFileCount() ) + ".txt";
	}

	public long getRequestEpoch(){
		return msEpoch;
	}

	public cfSession getSession(){
		return thisSession;
	}
	
	public boolean hasSession(){
		return bSaveSession;
	}
	
	private int	getFileId(cfFile file){
		String t 	= file.getCfmlURI().getRealPath();

		if ( fileIndexMap.containsKey(t) )
			return fileIndexMap.get(t);

		fileIndexMap.put( t, ++fileId );
		return fileId;
	}

	@Override
	public final void registerSession(cfSession thisSession) {
		this.thisSession = thisSession;
		
		// Get the path to the initial request; so we can make a director structure for this
		outputDir	= new File( journalmanager.getDirectory(), thisSession.getPresentURIPath() );
		if ( !outputDir.isDirectory() && !outputDir.mkdirs() ){
			outputDir	= journalmanager.getDirectory();
		}
		
		if ( bSaveSession )	saveSession();
	}


	@Override
	public void startScriptFunction(userDefinedFunction userDefinedFunction) {
		if ( bSaveSession )	saveSession();
		
		sb.setLength(0);
		sb.append( System.currentTimeMillis() - msEpoch ).append(',')
			.append( "SF," ).append( getFileId(thisSession.activeFile()) )
			.append( ',' ).append( sessionPagedCount )
			.append( ',' ).append( thisSession.getFileStack().size() )
			.append( ',' ).append( thisSession.getTagStack().size() )
			.append( "," ).append( userDefinedFunction.getName() )
			;

		logList.add( sb.toString() );
	}

	@Override
	public void endScriptFunction(userDefinedFunction userDefinedFunction) {
		if ( bSaveSession )	saveSession();
		
		sb.setLength(0);
		sb.append( System.currentTimeMillis() - msEpoch ).append(',')
			.append( "SE," ).append( getFileId(thisSession.activeFile()) )
			.append( ',' ).append( sessionPagedCount )
			.append( ',' ).append( thisSession.getFileStack().size() )
			.append( ',' ).append( thisSession.getTagStack().size() )
			.append( "," ).append( userDefinedFunction.getName() )
			;

		logList.add( sb.toString() );
	}

	
	@Override
	public final void startScriptStatement(CFParsedStatement statement) {
		sb.setLength(0);
		sb.append( System.currentTimeMillis() - msEpoch ).append(',')
			.append( "SS," ).append( getFileId(statement.getHostTag().getFile()) )
			.append( ',' ).append( sessionPagedCount )
			.append( ',' ).append( thisSession.getFileStack().size() )
			.append( ',' ).append( thisSession.getTagStack().size() )
			
			.append( "," ).append( '-' )
			.append( "," ).append( statement.getLine() + statement.getHostTag().posLine - 1 )
			.append( ',' ).append( statement.getColumn() )
			.append( ',' ).append( statement.getLine() )
			;

		logList.add( sb.toString() );
	}

	@Override
	public final void startTag(cfTag thisTag) {
		if ( thisTag instanceof cfFUNCTION )
			return;
		
		sb.setLength(0);

		sb.append( System.currentTimeMillis() - msEpoch ).append(',');

		if ( thisTag.getEndMarker() == null )
			sb.append( "TT," );
		else
			sb.append( "TS," );

		sb.append( getFileId(thisTag.getFile()) )
			.append( ',' ).append( sessionPagedCount )
			.append( ',' ).append( thisSession.getFileStack().size() )
			.append( ',' ).append( thisSession.getTagStack().size() )
			.append( ',' ).append( thisTag.getTagName() )
			.append( ',' ).append( thisTag.posLine )
			.append( ',' ).append( thisTag.posColumn );

		logList.add( sb.toString() );
	}

	@Override
	public final void endTag(cfTag thisTag) {
		if ( thisTag.getEndMarker() == null || thisTag instanceof cfFUNCTION )
			return;

		sb.setLength(0);

		sb.append( System.currentTimeMillis() - msEpoch ).append(',')
			.append( "TE," ).append( getFileId(thisTag.getFile()) )
			.append( ',' ).append( sessionPagedCount )
			.append( ',' ).append( thisSession.getFileStack().size() )
			.append( ',' ).append( thisSession.getTagStack().size() )
			.append( ',' ).append( thisTag.getTagName() )
			.append( ',' ).append( thisTag.posEndLine )
			.append( ',' ).append( thisTag.posEndColumn );

		logList.add( sb.toString() );
	}

	@Override
	public final void startFunction(cfFUNCTION thisTag) {
		sb.setLength(0);

		if ( bSaveSession )	saveSession();
		
		sb.append( System.currentTimeMillis() - msEpoch ).append(',')
			.append( "MS," ).append( getFileId(thisTag.getFile()) )
			.append( ',' ).append( sessionPagedCount )
			.append( ',' ).append( thisSession.getFileStack().size() )
			.append( ',' ).append( thisSession.getTagStack().size() )
			.append( ',' ).append( thisTag.getTagName() )
			.append( ',' ).append( thisTag.posEndLine )
			.append( ',' ).append( thisTag.posEndColumn )
			.append( ',' ).append( thisTag.getFunctionName() )
			;
		
		logList.add( sb.toString() );
	}
	
	@Override
	public final void endFunction(cfFUNCTION thisTag) {
		sb.setLength(0);
		
		if ( bSaveSession )	saveSession();
		
		sb.append( System.currentTimeMillis() - msEpoch ).append(',')
			.append( "ME," ).append( getFileId(thisTag.getFile()) )
			.append( ',' ).append( sessionPagedCount )
			.append( ',' ).append( thisSession.getFileStack().size() )
			.append( ',' ).append( thisSession.getTagStack().size() )
			.append( ',' ).append( thisTag.getTagName() )
			.append( ',' ).append( thisTag.posEndLine )
			.append( ',' ).append( thisTag.posEndColumn )
			.append( ',' ).append( thisTag.getFunctionName() )
			;
		
		logList.add( sb.toString() );
	}

	
	@Override
	public final void startFile(cfFile thisFile) {
		sb.setLength(0);

		if ( bSaveSession )	saveSession();
		
		sb.append( System.currentTimeMillis() - msEpoch ).append(',')
			.append( "FS," ).append( getFileId(thisFile) )
			.append( ',' ).append( sessionPagedCount )
			.append( ',' ).append( thisSession.getFileStack().size() )
			.append( ',' ).append( thisSession.getTagStack().size() )
			;
		logList.add( sb.toString() );
	}

	@Override
	public final void endFile(cfFile thisFile) {
		sb.setLength(0);

		if ( bSaveSession )	saveSession();
				
		sb.append( System.currentTimeMillis() - msEpoch ).append(',')
			.append( "FE," ).append( getFileId(thisFile) )
			.append( ',' ).append( sessionPagedCount )
			.append( ',' ).append( thisSession.getFileStack().size() )
			.append( ',' ).append( thisSession.getTagStack().size() )
			;

		logList.add( sb.toString() );
	}

	@Override
	public final void writtenBytes( int total ){
		sb.setLength(0);

		sb.append( System.currentTimeMillis() - msEpoch ).append(',')
			.append( "BW," )
			.append( total );

		logList.add( sb.toString() );
	}

	@Override
	public final void runExpression(CFExpression expr){
		sb.setLength(0);

		sb.append( System.currentTimeMillis() - msEpoch ).append(',')
			.append( "HE," ).append( getFileId(thisSession.activeFile()) )
			.append( ',' ).append( sessionPagedCount )
			.append( ',' ).append( thisSession.getFileStack().size() )
			.append( ',' ).append( thisSession.getTagStack().size() )
			.append( ",reserved,0,0," )
			.append( expr.Decompile(0) );

		logList.add( sb.toString() );
	}
	
	@Override
	public final void setHTTPHeader(String name, String value) {}

	@Override
	public final void setHTTPStatus(int sc, String value) {}

	@Override
	public final void endSession() {
		if ( fileOutputBufferedOS != null ){
			try {
				fileOutputBufferedOS.flush();
			} catch (IOException e) {}
			StreamUtil.closeStream( fileOutputBufferedOS );
		}
	}

	@Override
	public final void requestStart(cfSession session) {
		session.registerDebugger(this);
	}

	@Override
	public final void requestEnd(cfSession session) {
		journalmanager.onRequestEnd(this);
	}

	@Override
	public final void requestBadFileException(cfmBadFileException bfException, cfSession session) {
		sb.setLength(0);

		sb.append( System.currentTimeMillis() - msEpoch ).append(',')
			.append( "EF," )
			.append( bfException.getMessage() );

		logList.add( sb.toString() );
	}

	@Override
	public final void requestRuntimeException(cfmRunTimeException cfException, cfSession session) {
		sb.setLength(0);

		cfCatchData	catchdata	= (cfCatchData)session.getData( cfTRY.CFCATCHVAR );
		
		sb.append( System.currentTimeMillis() - msEpoch ).append(',')
			.append( "ER" );

		if ( catchdata != null && catchdata.containsKey("message") )
			sb.append(',').append( catchdata.getString("message") );

		logList.add( sb.toString() );
	}

	public final List<String> getTraceList() {
		return logList;
	}

	public final Map<String,Integer> getFileMap(){
		return fileIndexMap;
	}

	public File getDirectory() {
		return outputDir;
	}
	
	
	
	private void saveSession() {
		Map<String, cfStructData> ds = thisSession.getDataStore();
		
		
		// Clean up the data we want to store
		cfStructData	dump = new cfStructData();
		java.util.Iterator<Entry<String, cfStructData>> it	= ds.entrySet().iterator();
		while ( it.hasNext() ){
			Entry<String, cfStructData>	entry	= it.next();
			
			if ( entry.getKey().equals("cgi") || entry.getKey().equals("cookie") ){
				
				// CGI/Cookie never changes; so only page it out on the start
				if ( sessionPagedCount == 0 )
					dump.put(entry.getKey(), copyStruct(entry.getValue()));
				
			}else if ( entry.getKey().equals("request") ){
				
				// the request uses the servletcontainer which may not be here the next time
				cfStructData	s = copyStruct(entry.getValue());
				if ( !s.isEmpty() )
					dump.put(entry.getKey(), s);
				
			}else if ( entry.getKey().equals("server") ){
				
				// We don't need to keep repeating the standard variables that appear in all releases
				cfStructData	s = copyStruct(entry.getValue());
				s.remove("os");
				s.remove("bluedragon");
				s.remove("coldfusion");
				
				if ( !s.isEmpty() )
					dump.put(entry.getKey(), s);

			}else{
				if ( !entry.getValue().isEmpty() )				
					dump.put(entry.getKey(), entry.getValue());
			}
		}
		
		
		ByteArrayOutputStreamRaw mout;
		try {
			mout	= new ByteArrayOutputStreamRaw(128000);
			FileUtil.saveClass( mout, dump, true );
			
			if ( !byteArrayEquals(previousSession, mout) ){
				previousSession = mout;

				sessionPagedCount++;
				
				// if we don't have the file open; we need to do that
				if ( fileOutputBufferedOS == null ){				
					File sessionFile	= new File( outputDir, logFileName + ".session" );
					fileOutputBufferedOS = new BufferedOutputStream( new FileOutputStream( sessionFile ), 128000 );
				}
				
				// We will write the size of the session page
				byte[] sizeBytes = ByteBuffer.allocate(4).putInt(previousSession.size()).array();
				fileOutputBufferedOS.write( sizeBytes, 0, 4 );
				
				// now we write the session size
				fileOutputBufferedOS.write(previousSession.getByteArray(), 0, previousSession.size() );
				
				journalmanager.addBytesWritten( previousSession.size() + 4 );
			}
			
		} catch ( Exception E ) {
			cfEngine.log( "JournalSession.saveSession() e=" + E.getMessage() );
		} catch ( Throwable E ) {} 
	}

	
	private boolean byteArrayEquals(ByteArrayOutputStreamRaw in1, ByteArrayOutputStreamRaw in2) {
		if ( in1 == null || in2 == null || in1.size() != in2.size() )
			return false;
		
		byte[] in1B = in1.getByteArray();
		byte[] in2B = in2.getByteArray();
		int s = in1.size();
		
		for ( int x=0; x < s; x++ ){
			if ( in1B[x] != in2B[x] )
				return false;
		}
		
		return true;
	}

	private cfStructData copyStruct( cfStructData src ){
		cfStructData	dest = new cfStructData();
		
		Object[] keys = src.keys();
		for ( Object key : keys ){
			dest.put(key, src.get(key) );
		}
		
		return dest;
	}
	
	
	public String getLogFileName() {
		return logFileName;
	}
	
}
