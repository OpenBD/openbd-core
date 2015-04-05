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
 *  
 *  $Id: FileUpload.java 2526 2015-02-26 15:58:34Z alan $
 */

package com.naryx.tagfusion.expression.function.file;

import java.io.File;
import java.util.List;

import com.nary.io.FileUtils;
import com.nary.servlet.MultiPartUploadedFile;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDecodedInput;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructFileUploadData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class FileUpload extends functionBase {
  private static final long serialVersionUID = 1L;

  public FileUpload(){ 
  	min = 1; max = 5; 
  	setNamedParams( new String[]{"destination","filefield","nameconflict","accept","uri"} );
  }
  
	public String[] getParamInfo(){
		return new String[]{
			"local destination of where the file will be uploaded to. this is a local path relative to the server",
			"the form field containing the file; if missing the first file field will be used",
			"what to do if a file name already exists. Valid values: error/skip/overwrite/makeunique",
			"the list of mime types that will be accepted",
			"is the destination expressed as a real path or a URI, default to false"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"file",
				"Manages the UPLOAD of a file", 
				ReturnType.STRUCTURE );
	}
  
  public cfData execute( cfSession _Session, cfArgStructData argStruct )throws cfmRunTimeException{ 
    // Determine the destination
    String destStr	= getNamedStringParam(argStruct, "destination", null );
    if ( destStr == null )
    	throwException(_Session,"Missing the DESTINATION argument");
    
    
    // Get the file
		MultiPartUploadedFile	thisFile = getFile( _Session, getNamedStringParam(argStruct, "filefield", null ) );


		// Can we accept this file?
		String accept	= getNamedStringParam(argStruct, "accept", null );
    if ( accept != null && 
    		!FileUtils.acceptContent( thisFile.contentType, accept ) ){
    	throwException(_Session, "The MIME type of the uploaded file (" + thisFile.contentType + ") is not any of the accepted types (" + accept + ")" );
    }


    boolean bURI	= getNamedBooleanParam(argStruct, "uri", false );
    if ( bURI )
    	destStr	= FileUtils.getRealPath( destStr );
    
    
    // Return Structure of all the data that we need for the "cffile" scope
    cfStructFileUploadData fileData	= new cfStructFileUploadData();

    try{
    	// since the File class ignores trail '/'|'\' in the path we need to establish whether
      // the destination *could* be the path to a file or if it can only be a directory
      boolean isDirOnly = destStr.endsWith( "/" ) || destStr.endsWith( "\\" );
      
      File	destination = new File( destStr ).getCanonicalFile();
      if ( !destination.exists() && ( destination.getParent()==null || isDirOnly ) )
      	throwException( _Session, "The destination directory does not exist: " + destStr );
      
	    // if destination is path to a file that does exist or the path doesn't exist but it's not a directory 
      // (previous if catches the situation where the path doesn't exist but it is a directory)  
	    if ( (destination.exists() && destination.isFile() ) || !destination.exists()  ){
	      thisFile.filename = destination.getName();
	      destination = destination.getParentFile();
	    }// else the destination exists and isDirectory so leave as is

	    fileData.setClientFile( new File( thisFile.filename ) );
	  	
			// Determine the filename
			File newFile	= new File(destination, thisFile.filename);
			boolean bSkip = false;
	
			if ( newFile.exists() ){
	
				fileData.setFileExists( newFile );
				String NAMECONFLICT	= getNamedStringParam(argStruct, "nameconflict", "error" );
	
				if ( NAMECONFLICT.equalsIgnoreCase("ERROR") ){
					throwException( _Session, "The file already exists: " + newFile.toString() );
				} else if ( NAMECONFLICT.equalsIgnoreCase("OVERWRITE") ){
					fileData.setNameConflictOverright( newFile );
				} else if ( NAMECONFLICT.equalsIgnoreCase("SKIP") ){
					fileData.setNameConflictSkip();
					bSkip = true;
				} else if ( NAMECONFLICT.equalsIgnoreCase("MAKEUNIQUE") ){
					newFile = new File( destination, fileData.setNameConflictMakeUnqiue() );
				}
			}
	
	    
			// Set the various parameters
			fileData.setServerFile( newFile );
			fileData.setUploadData( thisFile );

			// Copy over the file
			if ( !bSkip )
				thisFile.copyFileTo( newFile );
	    		
			// Set any remaining parameters
			fileData.setData( "filesize", 	new cfNumberData((int)thisFile.tempFile.length()) );
			fileData.setData( "formfield",	new cfStringData(thisFile.formName) );

    }catch(Exception fse){
    	throwException(_Session, fse.getMessage() );
    }
		
		return fileData;
  }
  
  
  private MultiPartUploadedFile	getFile(cfSession _Session, String filefield )throws cfmRunTimeException{
  	// Make sure we have a file uploaded here
  	cfDecodedInput DI	= (cfDecodedInput)_Session.getDataBin( cfDecodedInput.DATA_BIN_KEY );
		if ( DI == null )
			throwException(_Session, "This was not an uploaded form type" );

		
		// Strip off the "form." qualifier if it is present
  	if ( filefield != null && filefield.length() > 5 && filefield.substring(0,5).toLowerCase().equals( "form." ) ){
      filefield = filefield.substring(5);
    }

		MultiPartUploadedFile	thisFile = null;
		if ( filefield != null ){
			thisFile 	= DI.getFile( filefield );
		}else{
			//Look for the first uploaded FILE
			List<MultiPartUploadedFile>	allFiles	= DI.getFiles();
			if ( allFiles.size() > 0 )
				thisFile	= allFiles.get(0);
		}
		
		if ( thisFile == null )
			throwException(_Session, "There was no appropriate FILE found in the upload" );
		
		return thisFile;
  } 
}