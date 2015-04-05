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

package com.nary.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.util.RandomAccessMode;

import com.naryx.tagfusion.cfm.engine.cfEngine;

/**
 * This class provides an easy way for an application to maintain a log file
 * that is unique to it.
 * 
 * The logfile has an auto-rotation facility, initially set at 25MB
 */

public class LogFileObject extends Object {
	private static final String LOG_DATE_FORMAT = "dd/MM/yy HH:mm.ss: ";

	private RandomAccessContent outFileRandomAccess;
	private long logFileSize = 0;
	private FileObject filename;

	private static long maxLogFileSize = 25000000; // - default to 25MB
	private static Hashtable logfiles = new Hashtable();

	
	private LogFileObject( FileObject logPath ) throws Exception {
		filename	= logPath;
		
		if ( !logPath.exists() )
			logPath.createFile();
		
    outFileRandomAccess = logPath.getContent().getRandomAccessContent( RandomAccessMode.READWRITE ); 
    logFileSize					= outFileRandomAccess.length();
    outFileRandomAccess.seek( logFileSize );
  }

	
	private void println(String _line) {
		try {
			writeData(com.nary.util.Date.formatNow(LOG_DATE_FORMAT) + _line + "\r\n");
		} catch (Exception e) {
			// DateFormat.format() has been observed to throw NullPointerExceptions
			cfEngine.log("Error " + e + " formatting date in LogFile.println( String _line )");
		}
	}
	

	private void close() {
		try {
			outFileRandomAccess.close();
		} catch (Exception E) {}
	}
	

	private synchronized void writeData(String _line) {
		try {
			outFileRandomAccess.writeBytes(_line);
			logFileSize += _line.length();
		} catch (IOException EE) {}

		if (logFileSize > maxLogFileSize)
			rotateLogFile();
	}

	
	
	private void rotateLogFile() {
		try {
			outFileRandomAccess.close();

			// rename the old file to a new one
			int x = 1;
			String sfname	= filename.getName().getBaseName(); 
			FileObject newFile = filename.getParent().resolveFile( sfname + "." + x ); 
				
			while ( newFile.exists() ) {
				newFile = filename.getParent().resolveFile( sfname + "." + (x++) ); 
			}

			filename.moveTo( newFile );

			//Delete the old one
			filename.delete();
			filename.createFile();

			outFileRandomAccess = filename.getContent().getRandomAccessContent( RandomAccessMode.READWRITE );;
			logFileSize = 0;

		} catch (IOException ignoreException) {
			// - the rotation failed; so lets just reset the current file
		}
	}

	// -------------------------------------------------------


	public static void println(FileObject outFile, String _Line) {
		String key = outFile.toString();
		
		if (!logfiles.containsKey( key )) {
			try {
				logfiles.put( key, new LogFileObject(outFile) );
			} catch (Exception E) {
				return;
			}
		}

		((LogFileObject) (logfiles.get( key ))).println(_Line);
	}

	public static void closeAll() {
		Enumeration E = logfiles.elements();
		while (E.hasMoreElements())
			((LogFileObject) E.nextElement()).close();
	}
}
