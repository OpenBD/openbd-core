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
 *  $Id: SpreadsheetQueryRead.java 2108 2012-06-07 00:32:22Z alan $
 */
package org.alanwilliamson.openbd.plugin.spreadsheet.functions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alanwilliamson.openbd.plugin.spreadsheet.SheetUtility;
import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetQueryRead extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetQueryRead(){  
		min = 2;  
		max = 6; 
		setNamedParams( new String[]{ "spreadsheet", "sheet", "headerrow", "columnnames", "columns", "rows" } );
	}
  
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"the sheet number to be active; the first sheet starts at 0",
			"the row in the spreadsheet that is to be considered the header for the query (only HEADERROW or COLUMNNAMES must be present)",
			"the comma-separated names for the column names (only HEADERROW or COLUMNNAMES must be present)",
			"the columns to be used in the format (1,2,3,5-6); defaults to all",
			"the rows to be used in the format (1,2,3,5-6); defaults to all"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Reads the given sheet from the spreadsheet object as a query.  If there are formulae in the cells they will be evaluated", 
				ReturnType.QUERY );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
  	cfSpreadSheetData	spreadsheet = (cfSpreadSheetData)getNamedParam(argStruct, "spreadsheet", null);
  	if ( spreadsheet == null )
  		throwException(_session, "spreadsheet object not given");

  	
  	//How many columns are we using
  	int[]	columnsToUse;
  	String columns	= getNamedStringParam(argStruct, "columns", null );
  	if ( columns != null ){
  		
  		int x = 0;
  		List<Integer>	numbers	= tagUtils.getNumberListSorted( columns );
  		columnsToUse	= new int[ numbers.size() ];
  		Iterator<Integer> numbersIT = numbers.iterator();
  		while ( numbersIT.hasNext() )
  			columnsToUse[x++]	= (numbersIT.next() - 1);
  		
  	}else{
  		int maxColumns	= SheetUtility.getMaxColumn( spreadsheet.getActiveSheet() );
  		columnsToUse	= new int[ maxColumns ];
  		for ( int x=0; x<maxColumns; x++ )
  			columnsToUse[x]	= x;
  	}
  	
  	
  	//Figure out the columns
  	String columnLabels[] = null;
  	int startRow	 = 0;
  	String columnnames	= getNamedStringParam(argStruct, "columnnames", null );
  	int 		headerRow		= getNamedIntParam(argStruct, "headerrow", -1 );
  	
  	FormulaEvaluator evaluator = spreadsheet.getWorkBook().getCreationHelper().createFormulaEvaluator();
  	
  	if ( columnnames != null ){
  		columnLabels	= (columnnames.split("\\s*,\\s*"));
  		if ( columnLabels.length != columnsToUse.length )
  			throwException(_session, "The COLUMNNAMES does not match the number of columns" );
  	}else if ( headerRow > -1 ){
  		
   		headerRow = headerRow-1;
  		Row row	= spreadsheet.getActiveSheet().getRow( headerRow );
  		if ( row == null )
  			throwException(_session, "The HEADERROW does not exist" );
  		
  		columnLabels	= new String[ columnsToUse.length ];
  		
   		for ( int c=0; c < columnsToUse.length; c++ ){
  			Cell cell	= row.getCell( columnsToUse[c] );
 
  			if ( cell == null )
  				columnLabels[c]	= "";
  			else
  				columnLabels[c] = cell.toString();
  		}
  		
  		startRow	= headerRow + 1;
  		
		}else{
			
	 		columnLabels	= new String[ columnsToUse.length ];
  		for ( int x=0; x < columnLabels.length; x++ )
  			columnLabels[x] = "Column " + (columnsToUse[x] + 1);
  		
  	}

  	
  	//Create the query
  	cfQueryResultData queryData = new cfQueryResultData( columnLabels, "SpreadSheet" );
  	List<Map<String, cfData>> vResults = new ArrayList<Map<String, cfData>>();

  	int sheetNo	= getNamedIntParam(argStruct, "sheet", -1 );
  	if ( sheetNo != -1 )
  		spreadsheet.setActiveSheet(sheetNo);
  	
  	Sheet sheet	= spreadsheet.getActiveSheet();
		Row row;
		Cell cell;
		cfData cfdata;
  	
		String rowsSt	= getNamedStringParam(argStruct, "rows", null );
		if ( rowsSt != null ){
			
  		List<Integer>	rows	= tagUtils.getNumberListSorted( rowsSt );
  		Iterator<Integer> rowsIT	= rows.iterator();
  		while ( rowsIT.hasNext() ){
  			int r = rowsIT.next() - 1;
  			
  			Map<String, cfData> hm = new FastMap<String, cfData>();
  			
  			if ( (row=sheet.getRow(r)) == null ) 
  				continue;
  			
  			for ( int c=0; c < columnsToUse.length; c++ ){
  				cell	= row.getCell( columnsToUse[c] );
  				if ( cell == null )
  					cfdata	= new cfStringData("");
  				else{
  					if ( cell.getCellType() == Cell.CELL_TYPE_BOOLEAN )
  						cfdata = cfBooleanData.getcfBooleanData( cell.getBooleanCellValue() );
  					else if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC )
  						cfdata = new cfNumberData( cell.getNumericCellValue() );
  					else if ( cell.getCellType() == Cell.CELL_TYPE_BLANK )
  						cfdata = new cfStringData("");
  					else if ( cell.getCellType() == Cell.CELL_TYPE_STRING )
  						cfdata = new cfStringData( cell.getStringCellValue() );
  					else if ( cell.getCellType() == Cell.CELL_TYPE_FORMULA ){
  						CellValue cellValue = evaluator.evaluate(cell);
  						
							switch (cellValue.getCellType()) {
								case Cell.CELL_TYPE_BOOLEAN:
									cfdata = cfBooleanData.getcfBooleanData(cellValue.getBooleanValue());
									break;
								case Cell.CELL_TYPE_NUMERIC:
									cfdata = new cfNumberData(cellValue.getNumberValue());
									break;
								case Cell.CELL_TYPE_STRING:
									cfdata = new cfStringData(cellValue.getStringValue());
									break;

								default:
									cfdata = cfStringData.EMPTY_STRING;
									break;
							}
 
  					}else
  						cfdata = new cfStringData("");
  				}
  				
  				hm.put( columnLabels[c], cfdata );
  			}
  			
  			vResults.add( hm ); 			
  		}

		}else{
			
  		/*
  		 * Read __ALL__ the rows associated with this spreadsheet
  		 */
  		for ( int r=startRow; r < sheet.getLastRowNum()+1; r++ ){
  			Map<String, cfData> hm = new FastMap<String, cfData>();
  			
  			if ( (row=sheet.getRow(r)) == null ) 
  				continue;
  			
  			for ( int c=0; c < columnsToUse.length; c++ ){
  				cell	= row.getCell( columnsToUse[c] );
  				if ( cell == null )
  					cfdata	= new cfStringData("");
  				else{
  					if ( cell.getCellType() == Cell.CELL_TYPE_BOOLEAN )
  						cfdata = cfBooleanData.getcfBooleanData( cell.getBooleanCellValue() );
  					else if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC )
  						cfdata = new cfNumberData( cell.getNumericCellValue() );
  					else if ( cell.getCellType() == Cell.CELL_TYPE_BLANK )
  						cfdata = new cfStringData("");
  					else if ( cell.getCellType() == Cell.CELL_TYPE_STRING )
  						cfdata = new cfStringData( cell.getStringCellValue() );
  					else if ( cell.getCellType() == Cell.CELL_TYPE_FORMULA ){
  						CellValue cellValue = evaluator.evaluate(cell);
  						
							switch (cellValue.getCellType()) {
								case Cell.CELL_TYPE_BOOLEAN:
									cfdata = cfBooleanData.getcfBooleanData(cellValue.getBooleanValue());
									break;
								case Cell.CELL_TYPE_NUMERIC:
									cfdata = new cfNumberData(cellValue.getNumberValue());
									break;
								case Cell.CELL_TYPE_STRING:
									cfdata = new cfStringData(cellValue.getStringValue());
									break;

								default:
									cfdata = cfStringData.EMPTY_STRING;
									break;
							}
 
  					} else
  						cfdata = new cfStringData("");
  				}
  				
  				hm.put( columnLabels[c], cfdata );
  			}
  			
  			vResults.add( hm );
  		}
  		
		}
		
  	queryData.populateQuery( vResults );
  	return queryData;
  }
}
