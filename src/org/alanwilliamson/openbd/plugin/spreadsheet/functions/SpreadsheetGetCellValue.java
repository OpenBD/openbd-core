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


import java.util.List;

import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetGetCellValue extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetGetCellValue(){  min = 3;  max = 3; }
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"rowNo",
			"columnNo"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Returns the value of the cell", 
				ReturnType.OBJECT );
	}
	
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	cfSpreadSheetData	spreadsheet = null;
  	int rowNo, columnNo;
  	
  	/*
  	 * Collect up the parameters
  	 */
		spreadsheet	= (cfSpreadSheetData)parameters.get(2);
		rowNo				= parameters.get(1).getInt() - 1;
		columnNo		= parameters.get(0).getInt() - 1;
  		
		if ( rowNo < 0 )
			throwException(_session, "row must be 1 or greater (" + rowNo + ")");
		if ( columnNo < 0 )
			throwException(_session, "column must be 1 or greater (" + columnNo + ")");
		
		
		/*
		 * Find the cell in question 
		 */
		Sheet	sheet = spreadsheet.getActiveSheet();
		Row row	= sheet.getRow( rowNo );
		if ( row == null )
			row	= sheet.createRow( rowNo );

		Cell cell	= row.getCell( columnNo );
		if ( cell == null )
			cell = row.createCell( columnNo );
		
		FormulaEvaluator evaluator = spreadsheet.getWorkBook().getCreationHelper().createFormulaEvaluator();
		
		if ( cell.getCellType() == Cell.CELL_TYPE_BOOLEAN )
			return cfBooleanData.getcfBooleanData( cell.getBooleanCellValue() );
		else if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC )
			return new cfNumberData( cell.getNumericCellValue() );
		else if ( cell.getCellType() == Cell.CELL_TYPE_BLANK )
			return cfStringData.EMPTY_STRING;
		else if ( cell.getCellType() == Cell.CELL_TYPE_STRING )
			return new cfStringData( cell.getStringCellValue() );
		else if ( cell.getCellType() == Cell.CELL_TYPE_FORMULA ){
			CellValue cellValue = evaluator.evaluate(cell);
			
			switch (cellValue.getCellType()) {
				case Cell.CELL_TYPE_BOOLEAN:
					return cfBooleanData.getcfBooleanData(cellValue.getBooleanValue());
				case Cell.CELL_TYPE_NUMERIC:
					return new cfNumberData(cellValue.getNumberValue());
				case Cell.CELL_TYPE_STRING:
					return new cfStringData(cellValue.getStringValue());
				default:
					return cfStringData.EMPTY_STRING;
			}

		}else
			return cfStringData.EMPTY_STRING;
  }
}
