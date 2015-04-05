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
 *  
 *  $Id: cfSpreadSheetUpdate.java 1718 2011-10-07 19:22:20Z alan $
 */
package org.alanwilliamson.openbd.plugin.spreadsheet.tags;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfSpreadSheetUpdate extends cfSpreadSheetWrite implements Serializable {
  static final long serialVersionUID = 1;

	
  public void render( cfSpreadSheet tag, cfSession _Session ) throws cfmRunTimeException {
  	if ( tag.containsAttribute("PASSWORD") )
  		throw tag.newRunTimeException("PASSWORD attribute is presently not supported");

  	if ( !tag.containsAttribute("FILENAME") )
  		throw tag.newRunTimeException("Please specify a FILENAME");
  	
  	File	outFile	= new File( tag.getDynamic(_Session, "FILENAME").getString() );
  	if ( !outFile.exists() ){
  		throw tag.newRunTimeException( outFile + "; does not exist.  Use ACTION=WRITE instead" );
  	}
  	
  	
  	if ( tag.containsAttribute("NAME") && !tag.containsAttribute("FORMAT") ){

  		cfData	data	= tag.getDynamic(_Session, "NAME");
  		if ( data instanceof cfSpreadSheetData ){
  			try {
					((cfSpreadSheetData) data).write( outFile, null );
				} catch (IOException e) {
					throw tag.newRunTimeException( "Failed to write the file: " + outFile );
				}
  		}else{
  			throw tag.newRunTimeException( "NAME must be a valid SPREADSHEET object" );
  		}
  		
  	}else if ( tag.containsAttribute("QUERY") ){
  		
  		cfData data	= tag.getDynamic(_Session, "QUERY");
  		if ( data.getDataType() != cfData.CFQUERYRESULTDATA )
  			throw tag.newRunTimeException( "QUERY attribute must be an query object" );
  		
  		String sheetName	= "Sheet 1";
  		if ( tag.containsAttribute("SHEETNAME") )
  			sheetName	= tag.getDynamic(_Session, "SHEETNAME").getString();
  		
  		cfQueryResultData	queryData	= (cfQueryResultData)data;
  		
  		boolean xlsx	= ( outFile.getName().endsWith(".xlsx") ) ? true : false;
  		
  		cfSpreadSheetData spreadsheet = new cfSpreadSheetData( xlsx, sheetName );
  		writeQueryToSheet( queryData, spreadsheet, sheetName );
  		try {
  			spreadsheet.write( outFile, null );
			} catch (IOException e) {
				throw tag.newRunTimeException( "Failed to write the file: " + outFile );
			}
			
		} else
			throw tag.newRunTimeException( "CSV Conversion now presently supported" );
  }
  
}