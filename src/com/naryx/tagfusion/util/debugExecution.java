/* 
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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

package com.naryx.tagfusion.util;


/**
 * This class is for collating and dumping all the information
 * related to exceptions as recorded when there is debug output enabled
 *
 */
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.file.cfFile;

class debugExecution{
  
  private Map<String, fileRecord> 			files; 			 // the files touched in the request
  private Stack<fileRecord>			startedFiles; // files started
	
  public debugExecution(){
    files = new FastMap<String, fileRecord>();
		startedFiles = new Stack<fileRecord>();
  }
  
  public debugExecution copy(){
    // loop thru the startedFiles
    debugExecution copy = new debugExecution();
    
    Iterator<String> filesIterator = files.keySet().iterator();
    while ( filesIterator.hasNext() ){
      String nextFileName = filesIterator.next();
      fileRecord nextFile = files.get( nextFileName );
      copy.files.put( nextFileName, nextFile.copy() );
    }
    
    Enumeration<fileRecord> startedEnum = startedFiles.elements();
    while ( startedEnum.hasMoreElements() ){
      copy.startedFiles.addElement( copy.files.get( startedEnum.nextElement().filename ) ); 
    }
    
    return copy;
  }
	
	public void startFile( cfFile thisFile ){
		String filename = thisFile.getName();

		// If the filename is null then just return.
		if ( filename == null )
			return;

		fileRecord theFile; 
		
		if ( files.containsKey( filename ) ){
			theFile = files.get( filename );
		}else{
			theFile = new fileRecord( filename );
			if ( files.size() == 0 ){
				theFile.topLevel = true;
			}
			files.put( filename, theFile );
		}
		
		theFile.startFile();
		startedFiles.push( theFile );
  }
	
	public void endFile( cfFile thisFile ){
		String filename = thisFile.getName();

		// If the filename is null then just return.
		if ( filename == null )
			return;

		if ( files.containsKey( filename ) )
		{
			fileRecord theFile = files.get( filename );
			theFile.endFile();
		}else{
			cfEngine.log("Debugging: internal error. End file not found (" + filename + ")" );
		}
		if ( !startedFiles.isEmpty() ) { // see bug #3128
			startedFiles.pop();
		}
  }
  
	// calls endFile on all unclosed files 
	private void cleanup(){
		fileRecord nextFile;
		while ( !startedFiles.empty() ){
			nextFile = startedFiles.pop();
			nextFile.endFile();
		}
	}
	
	public void dump( cfSession session, long _execTime, int _maxTime ){
		cleanup();
		List<fileRecord> fileRecList = new ArrayList<fileRecord>( files.values() );
		Collections.sort( fileRecList );
		
		// calc total time across all pages
		int overallTotal = 0;
		
		session.write( "<style type=\"text/css\">\n" );
		session.write( ".execution\n" );
    session.write( "{	color: black;\n" ); 
    session.write( "  font-family: \"Times New Roman\", Times, serif;\n" ); 
    session.write( "  font-weight: normal; }\n" );
		session.write( ".execution_max\n" );
		session.write( "{	color: red;\n" ); 
		session.write( "  background-color: white;\n" ); 
		session.write( "  font-family: \"Times New Roman\", Times, serif;\n" ); 
		session.write( "  font-weight: bold; }\n" );
		session.write( "</style>\n\n" );
		session.write( "<HR><b><div class=\"debughdr\">Execution</div></b>\n" );
		session.write( "<P><table class=\"debug\" border=\"1\" cellpadding=\"2\" cellspacing=\"0\" style=\"border: 1px solid black;\">\n" );
		session.write( "<tr><td class=\"debug\" align=\"center\"><b>Total Time</b></td><td class=\"debug\" align=\"center\"><b>Avg Time</b></td>" );
		session.write( "<td class=\"debug\" align=\"center\"><b>Count</b></td><td class=\"cfdebug\"><b>Template</b></td></tr>\n" );
		
		// FUDGE: since the topLevel file is pushed on prior to the Application.cfm it
		// contains the total time to process both. We need to find and subtract the Application.cfm
		// process time (if there is indeed an Application.cfm)
		fileRecord appRecord = null;
		fileRecord topLvlRecord = null;
		fileRecord next;
		for ( int i = 0; i < fileRecList.size(); i++ ){
			next = fileRecList.get(i);
		  if ( next.topLevel ){
				topLvlRecord = next;
			}else if ( next.filename.endsWith( "Application.cfm" ) ){
				appRecord = next;
			}
		}
		
		if ( appRecord != null ){
			topLvlRecord.totalTime -= appRecord.totalTime;
		}
		// END FUDGE
		
		fileRecord nextRecord;
		String rowClass = "execution";
		int aveExecTime;
		for ( int i = 0; i < fileRecList.size(); i++ ){
			nextRecord = fileRecList.get(i);
			if ( nextRecord.topLevel || nextRecord.filename.endsWith( "Application.cfm" )
					|| nextRecord.filename.endsWith( "OnRequestEnd.cfm" ) ){
				overallTotal += nextRecord.totalTime;
			}
			aveExecTime 	= nextRecord.totalTime / nextRecord.count;
			// update row colour
			if ( aveExecTime > _maxTime ) rowClass = "execution_max"; else rowClass = "execution";
			
			session.write( "<tr class=\"" + rowClass + "\" >" );
			session.write( "<td align=\"right\" nowrap>" + nextRecord.totalTime + " ms</td>\n" );
			session.write( "<td align=\"right\" nowrap>" + aveExecTime + " ms</td>\n" );
			session.write( "<td align=\"center\" nowrap>" + nextRecord.count + "</td>\n" );
			// if original request file then highlight it
			if ( nextRecord.topLevel ){
				session.write( "<td align=\"left\" nowrap><b>" + nextRecord.filename + "</b></td>\n" );
			}else{
				session.write( "<td align=\"left\" nowrap>" + nextRecord.filename + "</td>\n" );
			}
			session.write( "</tr>\n" );
		}
		
		
		// print summary 
		String startupTime = _execTime > overallTotal ? String.valueOf( (int) (_execTime) - overallTotal ) : "0";
		session.write( "<tr style=\"font-style: italic;\"><td align=\"right\">" );
		session.write( startupTime );
		session.write( " ms</td><td colspan=2>&nbsp;</td><td>STARTUP, PARSING, COMPILING, LOADING & SHUTDOWN</td><tr>\n" );
		session.write( "<tr style=\"font-style: italic;\"><td align=\"right\">" );
		session.write( String.valueOf( _execTime ) );
		session.write( " ms</td><td colspan=2>&nbsp;</td><td>TOTAL EXECUTION TIME</td><tr>\n" );
		session.write( "</table></P>\n" );
		session.write( "<p style=\"color: red;\"><b>red = over " );
		session.write( String.valueOf(_maxTime) );
		session.write( " ms average execution time </b></p>\n" );
		
	}
 
 	class fileRecord implements Comparable<fileRecord> {
		
		// this stack records the times started so we can calculate the end times
		// of each file. When a file is finished it's start time will be the one at the
		// top of the stack
		private Stack<Long> timeStarted;  
		public int 	count;
		public int 	totalTime;
		public boolean topLevel;
		public String filename;
		public boolean ended; // notes if a file has been started but not ended
	
		public fileRecord( String _fn ){
			timeStarted = new Stack<Long>();
			count 		= 0;
			totalTime = 0;
			filename  = _fn;
			topLevel  = false;
			ended			= true;
		}
		
    private fileRecord( fileRecord _fr ){
      timeStarted = new Stack<Long>();
      Enumeration<Long> origElems = _fr.timeStarted.elements();
      while ( origElems.hasMoreElements() ){
        timeStarted.addElement( origElems.nextElement() );
      }
      count = _fr.count;
      totalTime = _fr.totalTime;
      filename = _fr.filename;
      topLevel = _fr.topLevel;
      ended = _fr.ended;
    }
    
		public fileRecord copy(){
		  return new fileRecord( this );
    }
    
		public void startFile(){
			timeStarted.push( new Long( System.currentTimeMillis() ) );
			count++;
			ended = false;
		}
		
		
		public void endFile(){
			if ( !ended ) { // see bug #3128
				long endTime = System.currentTimeMillis();
				long startTime = timeStarted.pop().longValue();
				long timeTaken = endTime - startTime;
				totalTime += timeTaken;
				ended = true;
			}
		}
		
		public int compareTo( fileRecord fr2 ){
			if ( totalTime == fr2.totalTime ){
				return filename.compareTo( fr2.filename );
			}else if ( totalTime > fr2.totalTime ){
				return -1;
			}else{
				return 1;
			}
			
		}
		
		public String toString(){
			return "Count : " + count + "  totalTime : " + totalTime + "  filename : " + filename;
		}
	
	}// fileRecord
	 
}
