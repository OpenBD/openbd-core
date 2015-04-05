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
package org.alanwilliamson.openbd.plugin.spreadsheet.functions;

import java.util.Collections;
import java.util.List;

import org.alanwilliamson.openbd.plugin.spreadsheet.SheetUtility;
import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetAddColumn extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetAddColumn(){  min = 2;  max = 6; }
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"data - CSV string, or an Array object",
			"rowNo - default at the end",
			"columnNo - default at the right most",
			"bInsert - default true, false to replace column",
			"delimiter - if the data is a CSV format, defaults to comma"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Inserts a new column to the current sheet", 
				ReturnType.BOOLEAN );
	}
	
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	Collections.reverse( parameters );
  	
  	cfSpreadSheetData	spreadsheet = null;
  	cfData	data;
  	int rowNo, column;
  	boolean bInsert = true;
  	String delimitor = ",";
  	
  	/*
  	 * Collect up the parameters
  	 */
  	spreadsheet	= (cfSpreadSheetData)parameters.get(0);
  	data				= parameters.get(1);
  	rowNo				= getIntParam( parameters, 2, 1 ) - 1;
  	column			= getIntParam( parameters, 3, 1 ) - 1;

  	if ( column < 0 )
  		throwException(_session, "column must be 1 or greater (" + column + ")");
  	
  	if ( rowNo < 0 )
  		throwException(_session, "row must be 1 or greater (" + rowNo + ")");
  	
  	
  	bInsert						= getBooleanParam( parameters, 4, true );
   	Sheet	sheet 			= spreadsheet.getActiveSheet();
 	
  	if ( data.getDataType() == cfData.CFARRAYDATA ){
  		
  		addColumn( sheet, (cfArrayData)data, bInsert, rowNo, column );
  		
  	}else{

    	delimitor					=	getStringParam( parameters, 5, "," );
     	String[] colData	= data.getString().split( delimitor );
     	addColumn( sheet, colData, bInsert, rowNo, column );
  		
  	}
  	
  	
  	
   	
  	return cfBooleanData.TRUE;
  }


  private void addColumn( Sheet sheet, cfArrayData data, boolean bInsert, int rowNo, int column ) throws dataNotSupportedException{
   	/*
  	 * Run around the loop
  	 */
  	for ( int r=0; r < data.size(); r++ ){
  		int rowCurrent	= rowNo + r;
  		
  		// Create the necessary row
    	Row row = sheet.getRow( rowCurrent );
    	if ( row == null ){
    		SheetUtility.insertRow( sheet, rowCurrent );
    		row = sheet.getRow( rowCurrent );
    	}

    	// We will have to shift the cells up one
    	if ( bInsert && column < row.getLastCellNum() ){
  			SheetUtility.shiftCellRight( row, column );
  		}
    	
    	Cell cell = row.createCell( column );

    	// Set the data; trying to see if its a number
    	SheetUtility.setCell( cell, data.getElement(r+1) );
  	}
  }
  
  
  
  private void addColumn( Sheet sheet, String[] colData, boolean bInsert, int rowNo, int column ){
   	/*
  	 * Run around the loop
  	 */
  	for ( int r=0; r < colData.length; r++ ){
  		int rowCurrent	= rowNo + r;
  		
  		// Create the necessary row
    	Row row = sheet.getRow( rowCurrent );
    	if ( row == null ){
    		SheetUtility.insertRow( sheet, rowCurrent );
    		row = sheet.getRow( rowCurrent );
    	}

    	// We will have to shift the cells up one
    	if ( bInsert && column < row.getLastCellNum() ){
  			SheetUtility.shiftCellRight( row, column );
  		}
    	
    	Cell cell = row.createCell( column );

    	// Set the data; trying to see if its a number
  		try{
  			cell.setCellValue( Double.valueOf(colData[r]) );
  			cell.setCellType( Cell.CELL_TYPE_NUMERIC );
  		}catch(Exception e){
  			cell.setCellValue( colData[r] );
  			cell.setCellType( Cell.CELL_TYPE_STRING );
  		}
  	}
  }
  
}



