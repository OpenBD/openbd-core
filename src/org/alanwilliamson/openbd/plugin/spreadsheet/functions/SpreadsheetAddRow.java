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

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetAddRow extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetAddRow(){  min = 2;  max = 6; }
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"data - either a Query object, Array Object, or CSV string",
			"row - to insert at",
			"column - to insert at",
			"insert - replace or insert the data (defaults to true)",
			"delimiter - if the data is a CSV format, defaults to comma"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Inserts new rows to the active sheet", 
				ReturnType.BOOLEAN );
	}
	
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	Collections.reverse( parameters );
  	
  	cfSpreadSheetData	spreadsheet = null;
  	cfData	data;
  	int rowNo = -1, column = 0;
  	boolean bInsert = true;
  	String delimitor = ",";
  	
  	/*
  	 * Collect up the parameters
  	 */
  	spreadsheet	= (cfSpreadSheetData)parameters.get(0);
  	data				= parameters.get(1);
  	rowNo				= getIntParam( parameters, 2, 1 ) - 1;
  	column			= getIntParam( parameters, 3, 1 ) - 1;
  	bInsert			= getBooleanParam( parameters, 4, true );
  	delimitor		= getStringParam( parameters, 5, "," );
  	
  	
  	/*
  	 * Validate parameters
  	 */
  	if ( column < 0 )
  		throwException(_session, "column must be 1 or greater (" + column + ")");
  	
  	if ( rowNo < 0 )
  		throwException(_session, "row must be 1 or greater (" + rowNo + ")");

  	
  	cfQueryResultData	queryData = null;
  	cfArrayData arrayData = null;
  	String[] colData = null;
  	int	rowsToInsert;
  	
  	if ( data.getDataType() == cfData.CFARRAYDATA ){
  		arrayData	= (cfArrayData)data;
  		rowsToInsert	= arrayData.size();
  	}else if ( data.getDataType() == cfData.CFQUERYRESULTDATA ){
  		queryData	= (cfQueryResultData)data;
  		rowsToInsert = queryData.getSize();
  	}else{
  		List<String> elements = string.split(data.getString(), delimitor);
  		colData = elements.toArray( new String[0] );
  		rowsToInsert	= 1;
  	}
  	
  	
  	Sheet	sheet = spreadsheet.getActiveSheet();
  	
  	for ( int x=0; x < rowsToInsert; x++ ){
  		
  		// Create the row we need
    	Row row = null;
    	if ( rowNo == -1 )
    		row = sheet.createRow( sheet.getLastRowNum()+1 );
    	else if ( !bInsert ){
    		row = sheet.getRow( rowNo + x );
    		if ( row == null )
    			throwException(_session, "the row you are looking for does not exist");
    	}else if ( bInsert ){
    		SheetUtility.insertRow( sheet, rowNo + x );
    		row = sheet.getRow( rowNo + x );
    	}
  		
    	
  		// now set the data
  		Cell cell;
    	if ( colData != null ){
	     	
	    	for ( int c=0; c < colData.length; c++ ){
	    		if ( bInsert )
	    			cell	= row.createCell( column + c );
	    		else{
	    			cell	= row.getCell( column + c );
	    			if ( cell == null )
	    				cell	= row.createCell( column + c );
	    		}
	
	    		
	    		// Set the data; trying to see if its a number
	    		try{
	    			cell.setCellValue( Double.valueOf(colData[c]) );
	    			cell.setCellType( Cell.CELL_TYPE_NUMERIC );
	    		}catch(Exception e){
	    			cell.setCellValue( colData[c] );
	    			cell.setCellType( Cell.CELL_TYPE_STRING );
	    		}
	    	}
	    	
    	}else if ( arrayData != null ){
    		
    		String acolData[]	= arrayData.getData( new cfNumberData(x+1) ).getString().split(",");
    		
	    	for ( int c=0; c < acolData.length; c++ ){
	    		if ( bInsert )
	    			cell	= row.createCell( column + c );
	    		else{
	    			cell	= row.getCell( column + c );
	    			if ( cell == null )
	    				cell	= row.createCell( column + c );
	    		}
	    		
	    		// Set the data; trying to see if its a number
	    		try{
	    			cell.setCellValue( Double.valueOf(acolData[c]) );
	    			cell.setCellType( Cell.CELL_TYPE_NUMERIC );
	    		}catch(Exception e){
	    			cell.setCellValue( acolData[c] );
	    			cell.setCellType( Cell.CELL_TYPE_STRING );
	    		}
	    	}
    		
    	}else if ( queryData != null ){
	    	
    		int columnCount	= queryData.getNoColumns();
    		
    		for ( int c=0; c < columnCount; c++ ){
	    		if ( bInsert )
	    			cell	= row.createCell( column + c );
	    		else{
	    			cell	= row.getCell( column + c );
	    			if ( cell == null )
	    				cell	= row.createCell( column + c );
	    		}
	    		
	    		cfData value	= queryData.getCell( x+1, c + 1 );
	    		
	    		SheetUtility.setCell( cell, value );
	    	}
    	}
    	
  	}
  	
  	
  	return cfBooleanData.TRUE;
  }
  
}
