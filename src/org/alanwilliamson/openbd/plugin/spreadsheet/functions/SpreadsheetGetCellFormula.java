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


import java.util.Iterator;
import java.util.List;

import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfArrayListData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetGetCellFormula extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetGetCellFormula(){  min = 1;  max = 3; }
  
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
				"Returns an array of structures (formula, row, column) looking for the formula.  If rowNo/columnNo omitted, then all the formulae for the active sheet is returned", 
				ReturnType.ARRAY );
	}
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	cfSpreadSheetData	spreadsheet = null;
  	
  	if ( parameters.size() == 3 ){
  		int rowNo, columnNo;
  		spreadsheet	= (cfSpreadSheetData)parameters.get(2);
  		rowNo				= parameters.get(1).getInt() - 1;
  		columnNo		= parameters.get(0).getInt() - 1;
  		return getFormulaForOneCell( _session, spreadsheet, rowNo, columnNo );
  	}else if (parameters.size() == 1){
  		spreadsheet	= (cfSpreadSheetData)parameters.get(0);
  		return getAllFormulaForSheet( _session, spreadsheet );
  	}else{
  		throwException(_session, "You must specify row and column parameters");
  		return null;
  	}
  	
  }
  
  private cfData	getAllFormulaForSheet( cfSession _session, cfSpreadSheetData	spreadsheet ) throws cfmRunTimeException {
  	cfArrayData	array	= cfArrayListData.createArray(1);	
  	
  	Iterator<Row>	rowIt	= spreadsheet.getActiveSheet().rowIterator();
  	while ( rowIt.hasNext() ){
  		Row row = rowIt.next();
  		
  		Iterator<Cell> cellIt	= row.cellIterator();
  		while ( cellIt.hasNext() ){
  			Cell cell	= cellIt.next();
  			
  			if ( cell.getCellType() == Cell.CELL_TYPE_FORMULA ){
  				cfStructData	s = new cfStructData();
  				s.setData( "formula", new cfStringData( cell.getCellFormula() ) );
  				s.setData( "row", 		new cfNumberData( row.getRowNum() + 1 ) );
  				s.setData( "column", 	new cfNumberData( cell.getColumnIndex() + 1 ) );
  				array.addElement( s );
  			}
  		}
  	}
  	
  	return array;
  }
  
  
  private cfData	getFormulaForOneCell( cfSession _session, cfSpreadSheetData	spreadsheet, int rowNo, int columnNo ) throws cfmRunTimeException {
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
		
		
		if ( cell.getCellType() == Cell.CELL_TYPE_FORMULA )
			return new cfStringData( cell.getStringCellValue() );
		else
			return cfStringData.EMPTY_STRING;
  }
  
}
