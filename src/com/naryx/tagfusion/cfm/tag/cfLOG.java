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
 *  $Id: cfLOG.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.xmlConfig.xmlCFML;


public class cfLOG extends cfTag implements Serializable{
  static final long serialVersionUID = 1;
	transient private static File logDirectory = null;
	
	public static void init( xmlCFML configFile ) {
		initLogDir();
	}

	protected static void initLogDir(){
		if ( logDirectory == null ){
			try {
				logDirectory = FileUtils.checkAndCreateDirectory( cfEngine.thisPlatform.getFileIO().getWorkingDirectory(), "cflog", false );
			} catch (Exception e) {}
		}
	}

	public java.util.Map getInfo(){
		return createInfo("file", "This tag is used to write to the logging system of OpenBD. If no \"LOG\" or \"FILE\" attribute is specified, then the log is written to the standard OpenBD application log file.");
	}
  
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
   			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
				createAttInfo("TEXT", "Specifies the text that should be logged.", "NO", true ),
				createAttInfo("APPLICATION", "If set to YES, the web application name will be stored with the output of this log entry.", "YES", false ),
				createAttInfo("TYPE", "Specifies a category for your log (e.g. Information, Warning, Error etc).", "Information", false ),
				createAttInfo("THREAD", "Setting this attribute to YES will gather the thread reference this log was called within and output it with the log. This is useful to find out which thread has generated a log if being used in a threaded environment.", "YES", false ),
				createAttInfo("LOG", "Specifies a standard openbd log file to write this log to. Possible options include \"scheduler\", \"trace\" or \"application\". This attribute cannot be applied with the \"FILE\" attribute.", "", false ),
				createAttInfo("FILE", "Specifies a file name that this log will be written to. This attribute cannot be applied with the \"LOG\" attribute.", "", false )

		};

	}

	protected void defaultParameters( String _tag ) throws cfmBadFileException {
    defaultAttribute( "APPLICATION",  "Yes" );
		defaultAttribute( "TYPE",         "Information" );
		defaultAttribute( "THREAD",       "Yes" );
		parseTagHeader( _tag );
		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;

		if ( containsAttribute("LOG") && containsAttribute("FILE") )
			throw newBadFileException( "Invalid Combination", "You cannot specify FILE and LOG" );
		else if ( !containsAttribute("TEXT") )
			throw newBadFileException( "Missing Attribute", "You have to specify a TEXT attribute" );
	}
  
  
	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if ( containsAttribute(attributes,"LOG") && containsAttribute(attributes,"FILE") )
			throw newBadFileException( "Invalid Combination", "You cannot specify FILE and LOG" );
		else if ( !containsAttribute(attributes,"TEXT") )
			throw newBadFileException( "Missing Attribute", "You have to specify a TEXT attribute" );

		return	attributes;
	}



	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
  	cfStructData attributes = setAttributeCollection(_Session);
		String text 				 = getLogText( attributes, _Session );
		String type					 = getDynamic( attributes, _Session, "TYPE" ).getString();
		boolean bApplication = getDynamic( attributes, _Session, "APPLICATION" ).getBoolean();
		boolean bThread			 = getDynamic( attributes, _Session, "THREAD" ).getBoolean();
		
		String file = null, log = null;
		
		if ( containsAttribute( attributes,"LOG") ) {
			log = getDynamic( attributes, _Session, "LOG" ).getString().toLowerCase();
		} else if ( containsAttribute( attributes,"FILE") ) {
			file = getDynamic( attributes,_Session, "FILE").getString();
		}

		// All the information is now gathered, write it to the file
		toLog( _Session, text, type, file, log, bApplication, bThread );

		return cfTagReturnType.NORMAL;
	}
	
	
	/**
	 * Method for actual logging to the file; this is called by both WriteLog() and the CFLOG
	 * 
	 * @param _Session
	 * @param text
	 * @param type
	 * @param file
	 * @param log
	 * @param bApplication
	 * @param bThread
	 * @throws cfmRunTimeException
	 */
	public static void toLog( cfSession _Session, String text, String type, String file, String log, boolean bApplication, boolean bThread ) throws cfmRunTimeException {
		File fileInstance = null;
		
		if ( log != null && log.length() > 0 ){
			if ( log.equals( "scheduler" ) ) {
				fileInstance = new File( logDirectory, "schedule.log" );
			} else if ( log.equals( "trace" ) ) {
				fileInstance = new File( logDirectory, "cftrace.log" );
			} else {
				fileInstance = new File( logDirectory, "application.log" );
			}
		}else if ( file != null && file.length() > 0 ) {
			fileInstance = new File( logDirectory, file + ".log");
		}else{
			fileInstance = new File( logDirectory, "application.log" );
		}
		
		// Application Name
		String applicationName = "";
		if ( bApplication ) {
			cfApplicationData application = _Session.getApplicationData();
			String appName = null;
			if ( application != null )
				appName = application.getAppName();
			if ( appName != null )
				applicationName = "; " + appName;
		}

		// Thread Details
		String threadInfo = "";
		if ( bThread )
			threadInfo = "; " + Thread.currentThread();
		
		
		// All the information is now gathered, write it to the file
		com.nary.util.LogFile.println( fileInstance, type + applicationName + threadInfo + "; " + text);
	}

	
	protected String getLogText( cfStructData attributes, cfSession _Session ) throws cfmRunTimeException {
		return getDynamic( attributes, _Session, "TEXT" ).getString();
	}

	private void readObject( ObjectInputStream aInputStream ) throws ClassNotFoundException, IOException {
		// always perform the default de-serialization first
		aInputStream.defaultReadObject();

		initLogDir();
	}
}