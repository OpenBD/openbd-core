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
 *  $Id: cfStructFileUploadData.java 2526 2015-02-26 15:58:34Z alan $
 */

package com.naryx.tagfusion.cfm.engine;

import java.io.File;

import com.nary.servlet.MultiPartUploadedFile;

/**
 * This class implements all the functionality for the File upload structure
 */

public class cfStructFileUploadData extends cfStructData implements java.io.Serializable{
	
  static final long serialVersionUID = 1;

  private static Long makeUniqueCounter = new Long(0);
  
  public cfStructFileUploadData(){
		super();
		
		// Setup the initial parameters
		cfDateData	now = new cfDateData(System.currentTimeMillis());
		
		setData( "attemptedserverfile", 		cfStringData.EMPTY_STRING );
		
		setData( "clientdirectory", 				cfStringData.EMPTY_STRING );
		setData( "clientfile", 							cfStringData.EMPTY_STRING );
		setData( "clientfileext",						cfStringData.EMPTY_STRING );
		setData( "clientfilename",					cfStringData.EMPTY_STRING );

		setData( "contentsubtype",					cfStringData.EMPTY_STRING );
		setData( "contenttype",							cfStringData.EMPTY_STRING );

		setData( "datelastaccessed",				now );
		
		setData( "fileexisted",							cfBooleanData.FALSE );
		setData( "filesize",								new cfNumberData(0) );
		setData( "filewasappended",					cfBooleanData.FALSE );
		setData( "filewasoverwritten",			cfBooleanData.FALSE );
		setData( "filewasrenamed",					cfBooleanData.FALSE );
		setData( "filewassaved",						cfBooleanData.TRUE );
		
		setData( "oldfilesize",							new cfNumberData(0) );
		
		setData( "serverdirectory", 				cfStringData.EMPTY_STRING );
		setData( "serverfile", 							cfStringData.EMPTY_STRING );
		setData( "serverfileext",						cfStringData.EMPTY_STRING );
		setData( "serverfilename",					cfStringData.EMPTY_STRING );
		setData( "serverfileuri",						cfStringData.EMPTY_STRING );
		
		setData( "timecreated",							now );
		setData( "timelastmodified",				now );

		setData( "serverfilename",					cfStringData.EMPTY_STRING );
 
  }
	
	public void setServerFile( File ServerFile ) throws Exception{
		String fullpath	= ServerFile.toString();
		String name;
		
		if ( fullpath.startsWith("file://") ){
			File	f	= new File( fullpath.substring(7) );
			setData( "serverfileuri", 					new cfStringData( f.getCanonicalPath() ) );
			setData( "serverdirectory", 				new cfStringData( f.getParent() ) );
			name	= f.getName();
			setData( "serverfile", 							new cfStringData( name ) );
			setData( "attemptedserverfile", 		new cfStringData( name ) );
			
		}else{
			setData( "serverfileuri", 					new cfStringData( ServerFile.toString() ) );
			setData( "serverdirectory", 				new cfStringData( ServerFile.getParent().toString() ) );
			
			name	= ServerFile.getName();
			setData( "serverfile", 							new cfStringData( name ) );
			setData( "attemptedserverfile", 		new cfStringData( name ) );
		}

		int c1 = name.lastIndexOf(".");
		if ( c1 != -1 ){
			setData( "serverfileext",						new cfStringData( name.substring(c1 + 1) ) );
			setData( "serverfilename",					new cfStringData( name.substring(0,c1) ) );
		}
	}

	public void setClientFile( File ClientFile ){
		setData( "clientdirectory", 				new cfStringData( ClientFile.getParent() ) );
		
		String name	= ClientFile.getName();
		setData( "clientfile", 							new cfStringData( name ) );
		
		int c1 = name.lastIndexOf(".");
		if ( c1 != -1 ){
			setData( "clientfileext",						new cfStringData( name.substring(c1 + 1) ) );
			setData( "clientfilename",					new cfStringData( name.substring(0,c1) ) );
		}
	}
	
	public String setNameConflictMakeUnqiue(){
		setData( "filewassaved", 				cfBooleanData.TRUE );
		setData( "filewasrenamed",			cfBooleanData.TRUE );
		try{
			return new String( getData( "clientfilename" ).getString() + "_" + System.currentTimeMillis() + "_" + getMakeUniqueCounter() + "." + getData( "clientfileext" ).getString() );
		}catch(Exception E){
			return new String( System.currentTimeMillis() + "_" + getMakeUniqueCounter() + "" );
		}
	}
	
	/*
	 * getMakeUniqueCounter
	 * 
	 * Increments a counter every time we have to make a new unique name due to a conflict.
	 * This counter value is used in the new unique name to avoid conflicts.
	 * This is the fix for New Atlanta bug #2976.
	 */
	private static long getMakeUniqueCounter(){
		synchronized ( makeUniqueCounter ){
			// Increment the counter by 1
			makeUniqueCounter = new Long( makeUniqueCounter.longValue() + 1 );
			
			// If the counter rolled over to a negative value then set it back to 1
			if (makeUniqueCounter.longValue() <= 0)
				makeUniqueCounter = new Long(1);
	
			// Return the counter value
			return makeUniqueCounter.longValue();
	 	}
	}
	
	public void setNameConflictSkip(){
		setData( "filewassaved", 		cfBooleanData.FALSE );
	}
	
	public void setNameConflictOverright( File newFile ) {
		setData( "filewasoverwritten", 	cfBooleanData.TRUE );
		setData( "filewasreplaced", 		cfBooleanData.TRUE );
		setData( "filewassaved", 				cfBooleanData.TRUE );
		newFile.delete();
	}

	public void setFileExists( File newFile ) {
		setData( "oldfilesize", new cfNumberData( (int)newFile.length() ) );
		setData( "fileexisted", cfBooleanData.TRUE );
	}
	
	public void setUploadData( MultiPartUploadedFile	thisFile ){
		setData( "clientdirectory", 				new cfStringData( thisFile.remoteDirectory ) );

		// in the unlikely case that the Content Type isn't set, default it to text/plain (see bug #2444)
		if ( thisFile.contentType == null ){
			thisFile.contentType = "text/plain";
		}
		
		int c1 	= thisFile.contentType.indexOf( "/" );
		String minor = "", major = "";
		if ( c1 != -1 ){
			major	= thisFile.contentType.substring( 0, c1 );
			minor	= thisFile.contentType.substring( c1+1 );
		}else
			major	= thisFile.contentType;
		
		setData( "contentsubtype",					new cfStringData(minor) );
		setData( "contenttype",							new cfStringData(major) );
	}
}
