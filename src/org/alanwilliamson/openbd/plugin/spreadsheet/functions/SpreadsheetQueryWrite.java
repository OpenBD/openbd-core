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
 *  $Id: SpreadsheetQueryWrite.java 1891 2011-12-27 20:41:26Z alan $
 */
package org.alanwilliamson.openbd.plugin.spreadsheet.functions;

import java.util.Date;

import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetQueryWrite extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetQueryWrite(){  
		min = 3;  
		max = 3; 
		setNamedParams( new String[]{ "spreadsheet", "query", "sheetname" } );
	}
  
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"the query object",
			"the name of the sheet name.  Must already be created.  Use SpreadSheetCreateSheet()"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Writes out the query to the sheetname", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
  	cfSpreadSheetData	spreadsheet = (cfSpreadSheetData)getNamedParam(argStruct, "spreadsheet", null);
  	if ( spreadsheet == null )
  		throwException(_session, "spreadsheet object not given");

  	cfData	cfdata	= getNamedParam(argStruct, "query", null );
  	if ( cfdata == null )
  		throwException(_session, "query object not given");
  	
  	String sheetname	= getNamedStringParam(argStruct, "sheetname", null );
  	if ( sheetname == null )
  		throwException(_session, "sheetname not given");

  	spreadsheet.setActiveSheet(sheetname);
  	if ( !sheetname.equals( spreadsheet.getActiveSheetName() ))
  		throwException(_session, "sheetname was not valid");
  	
  	cfQueryResultData	queryData	= (cfQueryResultData)cfdata;

  	Sheet sheet	= spreadsheet.getActiveSheet();
  	
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
  	
  	
  	return cfBooleanData.TRUE;
  }
}
