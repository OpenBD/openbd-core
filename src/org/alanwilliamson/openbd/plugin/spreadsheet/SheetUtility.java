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
package org.alanwilliamson.openbd.plugin.spreadsheet;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;


/*
 * Helper functions to aid in the management of sheets
 */
public class SheetUtility extends Object {

	
	/*
	 * Returns the maximum column count this sheet represents
	 */
	public static int getMaxColumn( Sheet sheet ){
		int maxColumn = 0;
		for ( int r=0; r < sheet.getLastRowNum()+1; r++ ){
			Row	row	= sheet.getRow( r );
			
			// if no row exists here; then nothing to do; next!
			if ( row == null )
				continue;

			int lastColumn = row.getLastCellNum();
			if ( lastColumn > maxColumn )
				maxColumn = lastColumn;
		}
		return maxColumn;
	}
	
	
	
	/**
	 * Given a sheet, this method inserts a row to a sheet and moves
	 * all the rows to the bottom down one
	 * 
	 * Note, this method will not update any formula references.
	 * 
	 * @param sheet
	 * @param rowPosition
	 */
	public static void insertRow( Sheet sheet, int rowPosition ){

		//Row Position maybe beyond the last
		if ( rowPosition > sheet.getLastRowNum() ){
			sheet.createRow( rowPosition );
			return;
		}

		
		//Create a new Row at the end
		sheet.createRow( sheet.getLastRowNum()+1 );
		Row row;
		
		for ( int r=sheet.getLastRowNum(); r > rowPosition; r-- ){
			row	= sheet.getRow(r);
			if ( row == null )
				row = sheet.createRow( r );
			
			//Clear the row
			for ( int c=0; c < row.getLastCellNum(); c++ ){
				Cell cell	= row.getCell( c );
				if ( cell != null )
					row.removeCell( cell );
			}
			
			//Move the row
			Row previousRow	= sheet.getRow(r-1);
			if ( previousRow == null ){
				sheet.createRow( r-1 );
				continue;
			}
			
			for ( int c=0; c < previousRow.getLastCellNum(); c++ ){
				Cell cell	= previousRow.getCell( c );
				if ( cell != null ){
					Cell newCell	= row.createCell( c, cell.getCellType() );
					cloneCell( newCell, cell );
				}
			}
		}
		
		
		//Clear the newly inserted row
		row = sheet.getRow( rowPosition );
		for ( int c=0; c < row.getLastCellNum(); c++ ){
			Cell cell	= row.getCell( c );
			if ( cell != null )
				row.removeCell( cell );
		}
	}
	
	
	/**
	 * Given a sheet, this method deletes a column from a sheet and moves
	 * all the columns to the right of it to the left one cell.
	 * 
	 * Note, this method will not update any formula references.
	 * 
	 * @param sheet
	 * @param column
	 */
	public static void deleteColumn( Sheet sheet, int columnToDelete ){
		int maxColumn = 0;
		for ( int r=0; r < sheet.getLastRowNum()+1; r++ ){
			Row	row	= sheet.getRow( r );
			
			// if no row exists here; then nothing to do; next!
			if ( row == null )
				continue;
			
			int lastColumn = row.getLastCellNum();
			if ( lastColumn > maxColumn )
				maxColumn = lastColumn;
			
			// if the row doesn't have this many columns then we are good; next!
			if ( lastColumn < columnToDelete )
				continue;
			
			for ( int x=columnToDelete+1; x < lastColumn + 1; x++ ){
				Cell oldCell	= row.getCell(x-1);
				if ( oldCell != null )
					row.removeCell( oldCell );
				
				Cell nextCell	= row.getCell( x );
				if ( nextCell != null ){
					Cell newCell	= row.createCell( x-1, nextCell.getCellType() );
					cloneCell(newCell, nextCell);
				}
			}
		}

		
		// Adjust the column widths
		for ( int c=0; c < maxColumn; c++ ){
			sheet.setColumnWidth( c, sheet.getColumnWidth(c+1) );
		}
	}
	
	
	/*
	 * Takes an existing Cell and merges all the styles and forumla
	 * into the new one
	 */
	public static void cloneCell( Cell cNew, Cell cOld ){
		cNew.setCellComment( cOld.getCellComment() );
		cNew.setCellStyle( cOld.getCellStyle() );
		cNew.setCellType( cOld.getCellType() );
		
		switch ( cNew.getCellType() ){
			case Cell.CELL_TYPE_BOOLEAN:{
				cNew.setCellValue( cOld.getBooleanCellValue() );
				break;
			}
			case Cell.CELL_TYPE_NUMERIC:{
				cNew.setCellValue( cOld.getNumericCellValue() );
				break;
			}
			case Cell.CELL_TYPE_STRING:{
				cNew.setCellValue( cOld.getStringCellValue() );
				break;
			}
			case Cell.CELL_TYPE_ERROR:{
				cNew.setCellValue( cOld.getErrorCellValue() );
				break;
			}
			case Cell.CELL_TYPE_FORMULA:{
				cNew.setCellFormula( cOld.getCellFormula() );
				break;
			}
			case Cell.CELL_TYPE_BLANK:{
				cNew.setCellValue( cOld.getNumericCellValue() );
				break;
			}
				
		}
		
	}


	/**
	 * Shifts all the cells from the specified column to the right
	 * @param row
	 * @param column
	 */
	public static void shiftCellRight(Row row, int column) {
		int lastColumnCell	=  row.getLastCellNum();

		if ( column > lastColumnCell )
			return;

		for ( int x=lastColumnCell; x > column; --x ){
			Cell	cell	= row.getCell(x-1);
			if ( cell == null )
				continue;
		
			Cell newCell = row.createCell( x, cell.getCellType() );
			cloneCell( newCell, cell );
			row.removeCell( cell );
		}
	}
	
	
	/**
	 * Finds the best fit for a cell to be set
	 * 
	 * @param cell
	 * @param data
	 * @throws dataNotSupportedException  
	 */
	public static void setCell( Cell cell, cfData value ) throws dataNotSupportedException {
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