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

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetRead extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetRead(){  min = 1;  max = 2; }
	 
	public String[] getParamInfo(){
		return new String[]{
			"full path location of the spreadsheet file to read",
			"Sheet number, or index of the initial active sheet"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Creates a new spreadsheet object from the file specified", 
				ReturnType.SPREADSHEET );
	}
  
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	cfSpreadSheetData	spreadsheet = null;
  	
  	try{
	  	if ( parameters.size() == 1 ){
	  		spreadsheet	= new cfSpreadSheetData( new File( parameters.get(0).getString() ) );
	  	} else if ( parameters.size() == 2 ){
	  		spreadsheet	= new cfSpreadSheetData( new File( parameters.get(1).getString() ) );
	  	}
  	}catch(IOException ioe){
  		throwException(_session, "Problem reading the spreadsheet: " + ioe.getMessage() );
  		return null;
  	}
  	

  	/*
  	 * Now set the active sheet; if specified
  	 */
  	if ( parameters.size() == 2 ){
  		cfData data	= parameters.get(0);
  		if ( data.getDataType() == cfData.CFNUMBERDATA ){
  			spreadsheet.setActiveSheet( data.getInt() );
  		}else{
  			spreadsheet.setActiveSheet( data.getString() );
  		}
  	}

  	return spreadsheet;
  }
}
