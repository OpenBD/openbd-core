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

import org.alanwilliamson.openbd.plugin.spreadsheet.SheetUtility;
import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetShiftColumns extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetShiftColumns(){  min = 2;  max = 4; }
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"start column",
			"end column",
			"count"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Moves the column around", 
				ReturnType.BOOLEAN );
	}
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	cfSpreadSheetData	spreadsheet = null;
  	int start=0, end=0, cols = 1;
  	
  	
  	/*
  	 * Collect up the parameters
  	 */
  	if ( parameters.size() == 2 ){
  		spreadsheet	= (cfSpreadSheetData)parameters.get(1);
  		start				= parameters.get(0).getInt() - 1;
  		end					= start;
  	}else if ( parameters.size() == 3 ){
  		spreadsheet	= (cfSpreadSheetData)parameters.get(2);
  		start				= parameters.get(1).getInt() - 1;
  		end					= start;
  		cols				= parameters.get(0).getInt();
  	}else if ( parameters.size() == 4 ){
  		spreadsheet	= (cfSpreadSheetData)parameters.get(3);
  		start				= parameters.get(2).getInt() - 1;
  		end					= parameters.get(1).getInt() - 1;
  		cols				= parameters.get(0).getInt();
  	}  	
  	
  	/*
  	 * Validate parameters
  	 */
  	if ( start < 0 )
  		throwException(_session, "start must be 1 or greater (" + start + ")");

  	if ( end < 0 )
  		throwException(_session, "end must be 1 or greater (" + end + ")");
  	
  	if ( start > end )
  		throwException(_session, "end must be greater that start");
  	
  	
  	Sheet	sheet = spreadsheet.getActiveSheet();
  	
		Iterator<Row> rowIT	= sheet.rowIterator();
		while ( rowIT.hasNext() ){
			Row row	= rowIT.next();

			if ( cols > 0 ){
				// Moving to the right
				
				for ( int x=0; x < (end-start)+1; x++ ){
					// Remove Cell that is there
					Cell cell	= row.getCell( end + cols - x );
					if ( cell != null )
						row.removeCell( cell );
					
					Cell thisCell	= row.getCell( end - x );
					if ( thisCell != null ){
						Cell newCell	= row.createCell( end + cols - x, thisCell.getCellType() );
						SheetUtility.cloneCell( newCell, thisCell );
						row.removeCell( thisCell );
					}
				}
				
			}else{
				// Moving to the left
				for ( int x=0; x < (end-start)+1; x++ ){
					// Remove Cell that is there
					if ( start + cols - x < 0 )
						continue;
						
					Cell cell	= row.getCell( start + cols - x );
					if ( cell != null )
						row.removeCell( cell );
					
					Cell thisCell	= row.getCell( start - x );
					if ( thisCell != null ){
						Cell newCell	= row.createCell( start + cols - x, thisCell.getCellType() );
						SheetUtility.cloneCell( newCell, thisCell );
						row.removeCell( thisCell );
					}
			
				}
				
			}
			
		}
  	
  	return cfBooleanData.TRUE;
 	}
	
}
