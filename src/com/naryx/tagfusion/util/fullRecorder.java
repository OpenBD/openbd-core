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
 *  http://openbd.org/
 *  $Id: fullRecorder.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.util;

/**
 * This class does nothing with the events triggered for 
 * debugging purposes. Used when debugging is not enabled.
 */

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.sql.cfSQLQueryData;
import com.naryx.tagfusion.cfm.sql.preparedData;
import com.naryx.tagfusion.cfm.sql.resultSetHolder;
import com.naryx.tagfusion.cfm.tag.cfDUMP;
import com.naryx.tagfusion.cfm.tag.cfTag;

public class fullRecorder implements debugRecorder {
	
	public static final String DEFAULT_ENABLED				= "false";
	public static final String DEFAULT_EXEC_TIME_SHOW		= "false";
	public static final String DEFAULT_EXEC_TIME_HIGHLIGHT	= "250";
	public static final String DEFAULT_EXCEPTIONS_SHOW		= "false";
	public static final String DEFAULT_DATABASE_SHOW		= "false";
	public static final String DEFAULT_TRACEPOINTS_SHOW		= "false";
	public static final String DEFAULT_TIMER_SHOW			= "false";
	public static final String DEFAULT_VARIABLES_SHOW		= "false";
	public static final String DEFAULT_VARIABLES			= "true";
	public static final String DEFAULT_IP_ADDRESSES			= "";
	
  private debugQuery		dbgQuery;
  private debugExceptions 	dbgExceptions; // the exceptions thrown in the request
  private debugExecution	dbgExecution;
  private debugStoredProc	dbgStoredProcs;
  private List<String>		tracepoints;
  private List<String>		timer;
	
  private boolean show;   // whether to display the output. Set by CFSETTING
  private boolean showDB; // whether to display the db activity
  
  private long	reqStart;
  private long	reqEnd;
	
	
  public fullRecorder(){
    dbgExecution 	= new debugExecution();
    dbgExceptions = new debugExceptions();
    dbgQuery 	= new debugQuery();
    dbgStoredProcs = new debugStoredProc();
    tracepoints = new ArrayList<String>();
    timer = new ArrayList<String>();
    show = true;
    showDB = getConfigBoolean( "database.show", Boolean.valueOf( DEFAULT_DATABASE_SHOW ).booleanValue() );
  }
  
  private fullRecorder( fullRecorder _recorder ){
    dbgExecution  = _recorder.dbgExecution.copy();
    dbgExceptions = _recorder.dbgExceptions;
    dbgQuery  = _recorder.dbgQuery;
    dbgStoredProcs = _recorder.dbgStoredProcs;
    tracepoints = _recorder.tracepoints;
    timer = _recorder.timer;
    show = _recorder.show;
    showDB = _recorder.showDB;
    reqStart = _recorder.reqStart;
  }
  
  public debugRecorder copy(){
    return new fullRecorder( this );
  }
	
	
  public void startRequest(){
    reqStart = System.currentTimeMillis();
  }
	
  public void endRequest(){
    reqEnd = System.currentTimeMillis();
  }
	
  public void setShow( boolean _show ){
    show = _show;
  }
  
  public boolean getShow(){ 
    return show;
  }
  
  /**
   * This is used by a query to deterine whether it should
   * record debug data (in conjunction with it's own debug attribute)
   */
  public boolean getShowDBActivity(){ 
    return showDB;
  }
  	
  public void startFile( cfFile thisFile ){
    dbgExecution.startFile( thisFile );
  }
	  
  public void endFile( cfFile thisFile ){
    dbgExecution.endFile( thisFile );
  }
	
	 
  public void queryRan( String _template, String _qname, cfSQLQueryData _query, List<preparedData> _qp ){
    dbgQuery.addQuery( _qname, _template, _query, _qp );
  }
	
  public void updateRan( String _template, String _datasrc, String _sql ){
    dbgQuery.addUpdate( _template, _datasrc, _sql );
  }

  public void insertRan( String _template, String _datasrc, String _sql ){
    dbgQuery.addInsert( _template, _datasrc, _sql );
  }
	
	
  public void exceptionThrown( cfmRunTimeException exception, cfFile originF, cfTag originT ){
    dbgExceptions.add( exception, originF, originT );
  }
		 
  public void storedProcRan( String _template, String _datasrc, String _procName, long _execTime,
                              List<preparedData> _params, List<resultSetHolder> _results ){
    dbgStoredProcs.add( _template, _datasrc, _procName, _execTime, _params, _results );
  }

  public void recordTracepoint( String tracePoint ) {
      tracepoints.add( tracePoint );
  }

  public void recordTimer( String _timing ) {
    timer.add( _timing );
  }

  public void dump( cfSession session ){
    // Don't dump the debug output if the request comes from the admin
    if ( !show )
      return;
		
    session.setProcessingCfOutput( true );
    endRequest();
    long debugRenderStart = System.currentTimeMillis();
        boolean isWindowsUser = session.isWindowsOrMacUser();
        String fontSize = ( isWindowsUser ? "small" : "medium" );
        String hdrFontSize = ( isWindowsUser ? "medium" : "large" );
		
    // dump a bunch of end tags to ensure that any unclosed elements don't
    // disrupt the format of the debug output
    session.write( "</td></td></td></th></th></th></tr></tr></tr></table></table>" );
    session.write( "</table></a></abbrev></acronym></address></applet></au></b></banner>" );
    session.write( "</big></blink></blockquote></bq></caption></center></cite></code></comment>");
    session.write( "</del></dfn></dir></div></div></dl></em></fig></fn></font></form></frame>");
    session.write( "</frameset></h1></h2></h3></h4></h5></h6></head></i></ins></kbd></listing>" );
    session.write( "</map></marquee></menu></multicol></nobr></noframes></noscript></note></ol>" );
    session.write( "</p></param></person></plaintext></pre></q></s></samp></script></select>" );
    session.write( "</small></strike></strong></sub></sup></table></td></textarea></th></title>" );
    session.write( "</tr></tt></u></ul></var></wbr></xmp>" );
    session.write( "<style type=\"text/css\">\n" );
    session.write( ".debug\n{" );
    session.write( "    color:black;\n" );  
    session.write( "    background-color:white;\n" ); 
    session.write( "    font-family:\"Times New Roman\", Times, serif;\n" ); 
    session.write( "    font-size: " + fontSize + "\n}\n\n" );
    session.write( ".debughdr\n{\n" ); 
    session.write( "  	color:black;\n" ); 
    session.write( "    background-color:white;\n" ); 
    session.write( "    font-family:\"Times New Roman\", Times, serif;\n" ); 
    session.write( "    font-size: " + hdrFontSize + ";\n}\n\n" );
    session.write( "a.debuglink {color:blue; background-color:white }\n" );
    session.write( "</style>\n<div id='openbddebug'>" );

    dumpDebugInfo( session );
    if ( getConfigBoolean( "executiontimes.show", Boolean.valueOf( DEFAULT_EXEC_TIME_SHOW ).booleanValue() ) )
      dumpExecution( session );
		
    // if there are queries to show
    if ( dbgQuery.hasQueries() ){
      dbgQuery.dump( session );
    }
    
    if ( dbgStoredProcs.hasQueries() ){
      dbgStoredProcs.dump( session );
    }
		
    // if exception reporting
    if ( getConfigBoolean( "exceptions.show", Boolean.valueOf( DEFAULT_EXCEPTIONS_SHOW ).booleanValue() ) )
      dumpExceptions( session );
		
    // if tracepoints enabled
    if ( getConfigBoolean( "tracepoints.show", Boolean.valueOf( DEFAULT_TRACEPOINTS_SHOW ).booleanValue() ) )
      dumpTracepoints( session );
				
    // if timer enabled
    if ( getConfigBoolean( "timer.show", Boolean.valueOf( DEFAULT_TIMER_SHOW ).booleanValue() ) )
      dumpTimer( session );

    // if scope dumping enabled
    if ( getConfigBoolean( "variables.show", Boolean.valueOf( DEFAULT_VARIABLES_SHOW ).booleanValue() ) )
      dumpScopeVars( session );
		
    long debugRenderEnd = System.currentTimeMillis();
    session.write( "<p style=\"font-style: italic;\">Debug Rendering Time: " + (debugRenderEnd-debugRenderStart) + " ms</p></div>" );
  }

	
  //--[ methods for getting config values
	
  // returns a boolean value from the server.debugoutput structure within the config
  public static boolean getConfigBoolean( String _k, boolean _default ){
    return cfEngine.getConfig().getBoolean( "server.debugoutput." + _k, _default );
  }
	
  // returns an integer value from the server.debugoutput structure within the config
  private static int getConfigInt( String _k, int _default )
	{
    return cfEngine.getConfig().getInt( "server.debugoutput." + _k, _default );
  }

  //--[ Dump methods	
	
  private static void dumpDebugInfo( cfSession session ){
    (new debugInfo() ).dump( session );
  }
	
  private void dumpTracepoints( cfSession session ) {
    if ( tracepoints.size() > 0 ) {
      session.write( "<hr/><p><b><div class=\"debughdr\">Trace Points</div></b></p>\n<p>\n" );
      session.write( "<ul>" );
			
      Iterator<String> iter = tracepoints.iterator();
      while ( iter.hasNext() ) {
        session.write( "<li>" + iter.next() );
      }
      session.write( "</ul>" );
    }
  }

  private void dumpTimer( cfSession session ) {
    if ( timer.size() > 0 ) {
      session.write( "<hr/><p><b><div class=\"debughdr\">CFTimer timings</div></b></p>\n<p>\n" );
      session.write( "<ul>" );
			
      Iterator<String> iter = timer.iterator();
      while ( iter.hasNext() ) {
        session.write( "<li>" + iter.next() );
      }
      session.write( "</ul>" );
    }
  }

  private void dumpExecution( cfSession session ){
    dbgExecution.dump( session, (reqEnd-reqStart), getConfigInt( "executiontimes.highlight", Integer.parseInt( DEFAULT_EXEC_TIME_HIGHLIGHT ) ) );
  }// dumpExecution()
	
	
  private void dumpExceptions( cfSession session ){
    dbgExceptions.dump( session );		
  }

	
  private void dumpScopeVars( cfSession session ){
    CharArrayWriter	outChar	= new CharArrayWriter( 1024 );
    PrintWriter	out	= new PrintWriter( outChar );
		
    cfDUMP.writeDumpStyles( session, out );
    out.write( "<hr/><p><b><div class=\"debughdr\">Scope Variables</div></b></p>\n<p>\n" );
	
    Map<String, cfStructData> hT = session.getDataStore();
    Enumeration<String> E = new com.nary.util.StringSortedEnumeration( Collections.enumeration( hT.keySet() ) );
    
    while ( E.hasMoreElements() ){
      String key = E.nextElement();
      boolean dumpScope = getConfigBoolean( "variables." + key, Boolean.valueOf( DEFAULT_VARIABLES ).booleanValue() );
      if ( dumpScope ){
    	cfStructData data = hT.get( key );
        out.write( "<p><div class=\"debughdr\">" );
        out.write( key ); 
        out.write( "</div></b></p>\n<p>\n" );
        out.write( "<table width='100%' cellspacing='0' cellpadding='1' border='1' bgcolor='white'>" );
					 
        if ( key.equalsIgnoreCase("url") && cfEngine.isFormUrlScopeCombined() )
          out.write( "<tr><td><i>see [form] scope</i></td></tr>" );
        else
          data.dump( out );
							
        out.write( "</table>\n</p>" );
      }	
    }
	
    out.flush();
    session.write( outChar.toString() );
	
  }// dumpScopeVars()

	@Override
	public void execOnStart(cfData sqlData) {}

	@Override
	public void execOnEnd(cfData sqlData) {}

	@Override
	public void execStoredProc(String datasourceName, String callString, String procName, long execTime) {
	}
	
	public void execMongo( MongoCollection<Document> col, String action, Document qry, long execTime ){}
	
}
