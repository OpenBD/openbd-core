/* 
 *  Copyright (C) 2000 - 2009 TagServlet Ltd
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
package org.alanwilliamson.openbd.plugin.spreadsheet.functions;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetWrite extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetWrite(){  min = 2;  max = 4; }
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"filename - full path to the file that is to be saved to",
			"overwrite - default false, if the file already exists overwrite it",
			"password - apply a password to the spreadsheet, if the format supports it"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Writes the spreadsheet out to a file on the local file system", 
				ReturnType.BOOLEAN );
	}
	
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	boolean	overwrite	= false;
  	String password = null;
  	String filename	= null;
  	cfSpreadSheetData	spreadsheet = null;
  	
  	/*
  	 * Collect up the parameters
  	 */
  	if ( parameters.size() == 2 ){
  		
  		spreadsheet		= (cfSpreadSheetData)parameters.get(1);
  		filename			= parameters.get(0).getString();
  		
  	}else if ( parameters.size() == 3 ){
  		
  		spreadsheet		= (cfSpreadSheetData)parameters.get(2);
  		filename			= parameters.get(1).getString();
  		
  		cfData param = parameters.get(0);
  		try{
  			overwrite	= param.getBoolean();
  		}catch(Exception e){
  			password = param.getString();
  		}
  		
  	}else if ( parameters.size() == 4 ){
  		
  		spreadsheet		= (cfSpreadSheetData)parameters.get(3);
  		filename			= parameters.get(2).getString();
  		overwrite			= parameters.get(1).getBoolean();
  		password			= parameters.get(0).getString();
  		
  	}
  	
  	/*
  	 * Lets make a check to make sure the file is available to us
  	 */
  	File	outFile	= new File( filename );
  	if ( outFile.exists() ){
  		if ( !overwrite )
  			throwException(_session, "File already exists: " + filename );
  		else
  			outFile.delete();
  	}
  	
  	
  	/*
  	 * Perform the write
  	 */
  	try{
  		spreadsheet.write( outFile, password );
  	}catch(IOException ioe){
  		throwException(_session, "Problem writing the Spreadsheet file: " + ioe.getMessage() );
  	}

  	return cfBooleanData.TRUE;
  }
}
