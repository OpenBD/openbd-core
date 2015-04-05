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
 *  $Id: cfSpreadSheetWrite.java 1718 2011-10-07 19:22:20Z alan $
 */
package org.alanwilliamson.openbd.plugin.spreadsheet.tags;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

public class cfSpreadSheetWrite implements Serializable {
  static final long serialVersionUID = 1;

	
  public void render( cfSpreadSheet tag, cfSession _Session ) throws cfmRunTimeException {
  	if ( tag.containsAttribute("PASSWORD") )
  		throw tag.newRunTimeException("PASSWORD attribute is presently not supported");

  	if ( !tag.containsAttribute("FILENAME") )
  		throw tag.newRunTimeException("Please specify a FILENAME");
  	
  	File	outFile	= new File( tag.getDynamic(_Session, "FILENAME").getString() );
  	
  	if ( tag.containsAttribute("OVERWRITE") && tag.getDynamic(_Session, "OVERWRITE").getBoolean() ){
  		outFile.delete();
  	}else if ( outFile.exists() ){
  		throw tag.newRunTimeException( outFile + "; already exists" );
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
  
  
  
  /*
   * Takes the query and writes  out the data to the sheet, replacing the sheet
   */
  protected void	writeQueryToSheet( cfQueryResultData queryData, cfSpreadSheetData spreadsheet, String sheetName ) throws dataNotSupportedException{
  	Workbook	workbook	= spreadsheet.getWorkBook();
  	
  	if ( workbook.getSheet(sheetName) != null )
  		workbook.removeSheetAt( workbook.getSheetIndex(sheetName) );

  	Sheet	sheet	= workbook.createSheet( sheetName );
  	
  	//WRITE THE SHEET: 1st row to be the columns
  	String[] columnList = queryData.getColumnList();
  	Row row	= sheet.createRow(0);
  	Cell cell;
  	for ( int c=0; c < columnList.length; c++ ){
  		cell	= row.createCell( c, Cell.CELL_TYPE_STRING );
  		cell.setCellValue( columnList[c] );
  	}
  	
  	//WRITE THE SHEET: Write out all the rows
  	int rowsToInsert	= queryData.getSize(); 
  	for ( int x=0; x < rowsToInsert; x++ ){
  		row = sheet.createRow(x + 1);
  		
  		for ( int c=0; c < columnList.length; c++ ){
  			cell	= row.createCell( c );

    		cfData value	= queryData.getCell( x+1, c + 1 );

    		if ( value.getDataType() == cfData.CFNUMBERDATA ){
    			cell.setCellValue( value.getDouble() );
    			cell.setCellType( Cell.CELL_TYPE_NUMERIC );
    		}else if ( value.getDataType() == cfData.CFDATEDATA ){
    			cell.setCellValue( new Date( value.getDateLong() ) );
    		}else if ( value.getDataType() == cfData.CFBOOLEANDATA ){
    			cell.setCellValue( value.getBoolean() );
    			cell.setCellType( Cell.CELL_TYPE_BOOLEAN );
    		}else{
    			cell.setCellValue( value.getString() );
    			cell.setCellType( Cell.CELL_TYPE_STRING );
    		}
    	}
  		
  	}
  }
  
}