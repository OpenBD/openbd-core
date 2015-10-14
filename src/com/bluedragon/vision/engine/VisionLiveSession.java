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
 *  $Id: VisionLiveSession.java 2496 2015-02-01 02:19:29Z alan $
 */
package com.bluedragon.vision.engine;

import java.util.Iterator;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.parser.script.CFParsedStatement;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.cfm.sql.cfSQLQueryData;
import com.naryx.tagfusion.cfm.sql.preparedData;
import com.naryx.tagfusion.cfm.sql.resultSetHolder;
import com.naryx.tagfusion.cfm.tag.cfCOMPONENT;
import com.naryx.tagfusion.cfm.tag.cfFUNCTION;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.util.debugRecorder;
import com.naryx.tagfusion.util.debuggerListener;

public class VisionLiveSession implements debuggerListener, debugRecorder {
	private cfSession session;
	private boolean	bCurrentlyStopped = false, bRunToEnd = false, bStepToBreakPoint = true, bOnException = false, bStepOver = false;
	private cfTag	currentTag = null;
	private String currentFile = null;
	private cfArrayData	queryStack;
	private int	currentLine = 0;
	
	private cfTag	pCurrentTag = null;
	private cfData pcfData = null;
	private boolean bTerminate = false;
	
	private int statsTotalTags=0, statsTotalStatements=0;
	private long	startTime;
	
	public VisionLiveSession( cfSession session ){
		this.session 		= session;
		this.session.registerDebugger( this );
		this.session.registerDebugRecorder( this );
		this.queryStack	= cfArrayData.createArray(1);
		this.startTime	= System.currentTimeMillis();
	}
	
	public cfSession getCFSession(){
		return session;
	}
	
	public long getExecTime(){
		return System.currentTimeMillis() - startTime;
	}
	
	public cfTag	getProfileCurrentTag(){
		return pCurrentTag;
	}
	
	public cfData	getProfileData(){
		return pcfData;
	}

	public final void endFile(cfFile thisFile) {}
	public final void endSession() {}
	public final void write(int ch) {}
	public final void write(byte[] arrayCh) {}
	public final void writtenBytes(int total) {}
	public final void registerSession(cfSession thisSession) {}
	public final void runExpression(CFExpression expr){}
	public final void startFunction(cfFUNCTION cfFUNCTION) {}
	public final void endFunction(cfFUNCTION cfFUNCTION) {}
	public final void setHTTPHeader(String name, String value) {}
	public final void setHTTPStatus(int sc, String value) {}

	public final void startFile(cfFile thisFile) {}

	public void endTag(cfTag thisTag) {
		pCurrentTag	= null;
		if ( bTerminate )
			session.abortPageProcessing();
	}

	private void pauseEngine(){
		synchronized( this ){
			try {
				bStepToBreakPoint	= false;
				bStepOver					= false;
				bCurrentlyStopped = true;
				wait();
			} catch (InterruptedException e) {
			} finally {
				bCurrentlyStopped = false;
				currentTag				= null;
			}
		}
	}
	
	public void startScriptStatement(CFParsedStatement statement) {
		if ( bTerminate ){
			session.abortPageProcessing();
			return;
		}

		statsTotalStatements++;
		if ( bRunToEnd ) return;

		if ( statement.getHostTag() == null )
			return;
		
		int	linePos = statement.getLine() + statement.getHostTag().posLine - 1;
		
		if ( bStepToBreakPoint ){
			if ( CoreServer.thisInst.fileBreakPoints.size() == 0 )
				return;
			
			String uri 	= statement.getHostTag().getFile().getPath();
			if ( !CoreServer.thisInst.fileBreakPoints.containsKey( uri + "@" + linePos ) )
				return;
		} else if ( bStepOver ){

			if ( !getActiveFile(statement.getHostTag()).equals( currentFile ) )
				return;

		}
	
		currentTag	= statement.getHostTag();
		currentLine	= linePos;
		pauseEngine();
	}

	
	public void startTag(cfTag thisTag) {
		pCurrentTag = thisTag;
		statsTotalTags++;
		if ( bRunToEnd ) return;

		if ( bStepToBreakPoint ){
	  	String uri = thisTag.getFile().getPath();
			if ( !CoreServer.thisInst.fileBreakPoints.containsKey( uri + "@" + thisTag.posLine ) )
				return;
		} else if ( bStepOver ){
			
			if ( !getActiveFile(thisTag).equals( currentFile ) )
				return;
			
		}

		currentTag		= thisTag;
		currentLine		= thisTag.posLine;
		pauseEngine();
	}

	public void	step(){
		synchronized( this ){
			notify();
		}
	}
	
	public void stepToBreakPoint(){
		bStepToBreakPoint = true;
		step();
	}
	
	public void stepToEnd(){
		bRunToEnd = true;
		step();
	}
	
	public void stepOver(){
		bStepOver 	= true;
		currentFile = getActiveFile();
		step();
	}

	public void exceptionThrown(cfmRunTimeException cfException, cfFile f, cfTag thisTag) {
		if ( bRunToEnd || !CoreServer.thisInst.bStopOnException )
			return;
		
		try {
			session.setData( "cfcatch", cfException.getCatchData() );
		} catch (cfmRunTimeException e1) {}
		
		currentTag	= thisTag;
		currentLine	= thisTag.posLine;
		pauseEngine();
	}


	public void queryRan(String template, String qname, cfSQLQueryData query, List<preparedData> preparedDataList) {
		try {
			cfStructData	data = new cfStructData(); 

			data.setData( "querytype", 	new cfStringData( "query" ) );
			data.setData( "f", 					new cfStringData( template.replace('\\', '/') ) );
			data.setData( "name", 			new cfStringData( qname ) );
			data.setData( "rows", 			new cfNumberData( query.getSize() ) );
			data.setData( "time", 			new cfNumberData( query.getExecuteTime() ) );
			data.setData( "ds", 				new cfStringData( query.getDataSourceName() ) );
			data.setData( "dstype",			new cfStringData( query.getDataTypeName() ) );
			data.setData( "columns",		new cfStringData( query.getColumns() ) );
			data.setData( "sql",				new cfStringData( query.getQueryString() ) );

			if ( preparedDataList != null && preparedDataList.size() > 0 ){
				StringBuilder sb = new StringBuilder( 32 );
				Iterator<preparedData> it = preparedDataList.iterator();
				while ( it.hasNext() ){
					preparedData pData = it.next();
					sb.append( pData.toString() );
					sb.append( ";" );
				}
				data.setData( "prepared",	new cfStringData( sb.toString() ) );
			}else
				data.setData( "prepared",	cfStringData.EMPTY_STRING );
			
			queryStack.addElement( data );
		} catch (cfmRunTimeException e) {}
	}

	public void storedProcRan(String template, String datasrc, String procName, long execTime, List<preparedData> preparedDataList, List<resultSetHolder> results) {
		try {
			cfStructData	data = new cfStructData(); 

			data.setData( "querytype", 	new cfStringData( "proc" ) );
			data.setData( "f", 					new cfStringData( template.replace('\\', '/') ) );
			data.setData( "name", 			new cfStringData( procName ) );
			data.setData( "time", 			new cfNumberData( execTime ) );
			data.setData( "ds", 				new cfStringData( datasrc ) );

			if ( preparedDataList != null && preparedDataList.size() > 0 ){
				StringBuilder sb = new StringBuilder( 32 );
				Iterator<preparedData> it = preparedDataList.iterator();
				while ( it.hasNext() ){
					preparedData pData = it.next();
					sb.append( pData.toString() );
					sb.append( ";" );
				}
				data.setData( "prepared",	new cfStringData( sb.toString() ) );
			}else
				data.setData( "prepared",	cfStringData.EMPTY_STRING );
			
			queryStack.addElement( data );
		} catch (cfmRunTimeException e) {}
	}
	
	public void onRunTimeException(cfmRunTimeException cfException) {}
	public void insertRan(String template, String datasrc, String sql) {}
	public void updateRan(String template, String datasrc, String sql) {}

	public debugRecorder copy() {return this;	}
	public void dump(cfSession session) {}
	public void recordTimer(String timing) {}
	public void recordTracepoint(String tracePoint) {}
	public void setShow(boolean show) {}
	public void startRequest() {}
	public void endRequest() {}
	public boolean getShow() {return false;}
	public boolean getShowDBActivity() {return true;}

	
	public cfData 	getQueryStack() {return queryStack;	}
	public int 			getCurrentLine(){return currentLine;}
	public boolean 	isStopped(){	return bCurrentlyStopped;}
	public boolean 	isTerminating(){	return bTerminate;}
	public boolean	isOnException(){return bOnException;}
	public cfTag		getStoppedTag(){return currentTag;}
	public int			getStatsTags(){ return statsTotalTags; }
	public int			getStatsStatements(){ return statsTotalStatements; }
	
	public String	getActiveFile(){
		return getActiveFile( currentTag );
	}
	
	public String	getActiveFile( cfTag tag ){
		if ( tag != null )
			return tag.getFile().getPath().replace('\\','/');
		
  	/* If we are running the CFCOMPONENT/CFFUNCTION then the active file is actually wrong; we have to look for it else where */
  	if ( tag instanceof cfFUNCTION || tag instanceof cfCOMPONENT ){
  		cfComponentData c = session.getActiveComponentData();
  		if ( c != null ){
  			return c.getComponentPath().replace('\\','/');	
  		}
  	}
  	
  	/* If we still don't have it, then lets just use this one */
  	return session.activeFile().getPath().replace('\\','/');
	}

	public void execOnStart(cfData data) {
		pcfData	= data;
	}

	public void execOnEnd(cfData data) {
		pcfData = null;
	}

	public void terminate() {
		bTerminate	= true;
	}
	

	@Override
	public void execStoredProc(String datasourceName, String callString, String procName, long execTime) {}
	public void execMongo( MongoCollection<Document> col, String action, Document qry, long execTime ){}

	public final void startScriptFunction(userDefinedFunction userDefinedFunction) {}
	public final void endScriptFunction(userDefinedFunction userDefinedFunction) {}
}