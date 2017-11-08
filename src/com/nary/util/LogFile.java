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
 *  $Id: LogFile.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.nary.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.aw20.util.SystemClock;
import org.aw20.util.SystemClockEvent;

import com.naryx.tagfusion.cfm.engine.cfEngine;

/**
 * This class provides an easy way for an application to maintain a log file that is unique to it.
 * 
 * The logfile has an auto-rotation facility, initially set at 25MB
 */

public class LogFile extends Object implements SystemClockEvent {

	private static final String LOG_DATE_FORMAT = "dd/MM/yy HH:mm.ss: ";

	private FileOutputStream fileWriter;
	private OutputStreamWriter outWriter;
	
	private long logFileSize = 0;
	private String filename;
	private String encoding;
	private boolean prependTimeStamp;
	
	private long maxLogFileSize = 25000000; // - default to 25MB
	
	private long maxLogFileAge = TimeUnit.DAYS.toMillis( 30 ); // - default to 30 days

	private LogFile(String logPath, String _encoding, boolean _PrependTimeStamp) throws Exception {
		filename = logPath;
		fileWriter = new FileOutputStream(logPath,true);
		outWriter = new OutputStreamWriter( fileWriter, _encoding );
		logFileSize = new File( logPath ).length();
		
		
		SystemClock.setListenerDay( this );
	}

	private void setMaxLogFileSize(long maxLogFileSize) {
		this.maxLogFileSize = maxLogFileSize;
	}

	private void setMaxLogFileAge(int maxLogFileAge) {
		this.maxLogFileSize = TimeUnit.DAYS.toMillis( maxLogFileAge );
	}

	
	@Override
	public void clockEvent( int arg0 ) {
		// clean up old files
		File logFile = new File( filename );
		File logDirectory = ( logFile ).getParentFile();
		File [] files = logDirectory.listFiles( new FileFilter(){

			@Override
			public boolean accept( File _file ) {
				return _file.getName().startsWith( logFile.getName() ) 
						&& _file.lastModified() > ( System.currentTimeMillis() - maxLogFileAge );
			}} );
		
		for ( File nextFile : files ){
			nextFile.delete();
		}
		
	}

	private void println(String _line) {
		try {
			writeData( ( prependTimeStamp ? com.nary.util.Date.formatNow(LOG_DATE_FORMAT) : "" ) + _line + "\r\n");
		} catch (Exception e) {
			// DateFormat.format() has been observed to throw NullPointerExceptions
			cfEngine.log("Error " + e + " formatting date in LogFile.println( String _line )");
		}
	}

	private void println(Object _ob) {
		writeData( ( prependTimeStamp ? com.nary.util.Date.formatNow(LOG_DATE_FORMAT) : "" ) + _ob.toString() + "\r\n");
	}

	private void close() {
		try {
			outWriter.close();
			fileWriter.close();
		} catch (Exception E) {
		}
	}

	private synchronized void writeData(String _line) {
		try {
			writeToFile(_line);
		} catch (IOException EE) {
			rotateLogFile();
			try {
				writeToFile(_line);
			} catch (IOException e) {
				cfEngine.log("LogFile.Failed:" + e.getMessage());
			}
		}

		if (logFileSize > maxLogFileSize)
			rotateLogFile();
	}

	private void writeToFile(String _line) throws IOException {
		outWriter.write(_line);
		outWriter.flush();
		logFileSize += _line.length();
	}

	private void rotateLogFile() {

		// Close off the file
		try {
			outWriter.close();
			fileWriter.close();
		} catch (IOException e) {
		}

		// Reopen the log file, rotating the old one if needbe
		try {
			File thisFile = new File(filename);
			if (!thisFile.getParentFile().exists()) {
				thisFile.getParentFile().mkdirs();
				if (!thisFile.getParentFile().exists())
					throw new IOException("Failed to created:" + thisFile.getParentFile());
			}

			// rename the old file to a new one
			if (thisFile.exists()) {
				int x = 1;
				File newFile = new File(filename + "." + x);
				while (newFile.exists()) {
					newFile = new File(filename + "." + (x++));
				}

				// Rename the old file
				new File(filename).renameTo(newFile);

				// Delete the old one
				new File(filename).delete();
			}

			// Open up the file
			fileWriter = new FileOutputStream(filename,true);
			outWriter = new OutputStreamWriter( fileWriter, encoding );

			logFileSize = 0;

		} catch (IOException ignoreException) {
			// the rotation failed; so lets just reset the current file
		}
	}

	// -------------------------------------------------------

	private static Hashtable logfiles = new Hashtable();

	public static synchronized boolean open(String _Name, String _Path ) {
		return open( _Name, _Path, Charset.defaultCharset().toString(), true );
	}
	
	public static synchronized boolean open(String _Name, String _Path, String _encoding, boolean _PrependTimeStamp ) {
		LogFile LF;
		try {
			LF = new LogFile(_Path, _encoding, _PrependTimeStamp);
		} catch (Exception E) {
			return false;
		}

		logfiles.put(_Name.toUpperCase(), LF);
		return true;
	}

	public static void println(File outFile, String _Line) {
		if (!logfiles.containsKey(outFile.toString())) {
			try {
				logfiles.put(outFile.toString(), new LogFile(outFile.toString(), Charset.defaultCharset().toString(), true));
			} catch (Exception E) {
				return;
			}
		}

		((LogFile) (logfiles.get(outFile.toString()))).println(_Line);
	}

	public static void println(String _Name, String _Line) {
		if (logfiles.containsKey(_Name.toUpperCase()))
			((LogFile) (logfiles.get(_Name.toUpperCase()))).println(_Line);
	}

	public static void println(String _Name, Object _OB) {
		if (logfiles.containsKey(_Name.toUpperCase()))
			((LogFile) (logfiles.get(_Name.toUpperCase()))).println(_OB);
	}

	public static void setRotationSize(String _Name, long rotationSize) {
		if (logfiles.containsKey(_Name.toUpperCase()))
			((LogFile) (logfiles.get(_Name.toUpperCase()))).setMaxLogFileSize(rotationSize);
	}

	public static void setMaxAge(String _Name, int maxAgeDays) {
		if (logfiles.containsKey(_Name.toUpperCase()))
			((LogFile) (logfiles.get(_Name.toUpperCase()))).setMaxLogFileAge(maxAgeDays);
	}

	public static void closeAll() {
		Enumeration E = logfiles.elements();
		while (E.hasMoreElements())
			((LogFile) E.nextElement()).close();
	}

}
