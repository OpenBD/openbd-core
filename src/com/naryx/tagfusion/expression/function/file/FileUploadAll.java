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
 *  $Id: FileUploadAll.java 2526 2015-02-26 15:58:34Z alan $
 */


package com.naryx.tagfusion.expression.function.file;

import java.util.Iterator;
import java.util.List;

import com.nary.servlet.MultiPartUploadedFile;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDecodedInput;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class FileUploadAll extends functionBase {
  private static final long serialVersionUID = 1L;

  public FileUploadAll(){ 
  	min = 1; max = 4; 
  	setNamedParams( new String[]{"destination","nameconflict","accept","uri"} );
  }
  
	public String[] getParamInfo(){
		return new String[]{
			"local destination of where the file will be uploaded to",
			"what to do if a file name already exists. Valid values: error/skip/overwrite/makeunique",
			"the list of mime types that will be accepted",
			"is the destination expressed as a real path or a URI, default to false"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"file",
				"Manages the uploading of all files in a form.  Returns a list of all the files uploaded", 
				ReturnType.ARRAY );
	}
  
  public cfData execute( cfSession _Session, cfArgStructData argStruct )throws cfmRunTimeException{
  	// Make sure we have a file uploaded here
  	cfDecodedInput DI	= (cfDecodedInput)_Session.getDataBin( cfDecodedInput.DATA_BIN_KEY );
		if ( DI == null )
			throwException(_Session, "This was not an uploaded form type" );
  	
		FileUpload	fileUploadFunction	= new FileUpload();
		cfArgStructData functionArgs	= new cfArgStructData( true );
		
		if ( argStruct.containsKey("destination") )	
			functionArgs.setData("destination", getNamedParam(argStruct, "destination") );
		
		if ( argStruct.containsKey("nameconflict") )		
			functionArgs.setData("nameconflict", getNamedParam(argStruct, "nameconflict") );
		
		if ( argStruct.containsKey("accept") )
			functionArgs.setData("accept", getNamedParam(argStruct, "accept") );
		
		if ( argStruct.containsKey("uri") )
			functionArgs.setData("uri", getNamedParam(argStruct, "uri") );
		
		
  	cfArrayData	fileArray	= cfArrayData.createArray(1);
  	
  	List<MultiPartUploadedFile>	allFiles	= DI.getFiles();
  	Iterator<MultiPartUploadedFile>	it	= allFiles.iterator();
  	while ( it.hasNext() ){
  		String filefield	= it.next().formName;
  		
  		functionArgs.setData("filefield", new cfStringData( filefield ) );
  		
  		fileArray.addElement( fileUploadFunction.execute(_Session, functionArgs) );  		
  	}
  	
  	return fileArray;
  } 
}