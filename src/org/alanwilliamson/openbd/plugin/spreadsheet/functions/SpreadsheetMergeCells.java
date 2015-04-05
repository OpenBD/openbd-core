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
import org.apache.poi.ss.util.CellRangeAddress;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetMergeCells extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetMergeCells(){  min = 5;  max = 5; }
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"startrowNo",
			"end rowNo",
			"start columnNo",
			"end columnNo"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Merges the cell range into one on the active sheet", 
				ReturnType.BOOLEAN );
	}
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	cfSpreadSheetData	spreadsheet = null;
  	
  	/*
  	 * Collect up the parameters
  	 */
		spreadsheet		= (cfSpreadSheetData)parameters.get(4);
		int startRow	= parameters.get(3).getInt()-1;
		int endRow		= parameters.get(2).getInt()-1;
		int startCol	= parameters.get(1).getInt()-1;
		int endCol		= parameters.get(0).getInt()-1;

		if ( startRow < 0 )
  		throwException(_session, "startRow must be 1 or greater (" + startRow + ")");
  	if ( endRow < 0 )
  		throwException(_session, "endRow must be 1 or greater (" + endRow + ")");
		if ( startCol < 0 )
  		throwException(_session, "column must be 1 or greater (" + startCol + ")");
  	if ( endCol < 0 )
  		throwException(_session, "row must be 1 or greater (" + endCol + ")");

  	if ( endRow < startRow )
  		throwException(_session, "startrow must be smaller than endrow");
  	if ( endCol < startCol )
  		throwException(_session, "startcolumn must be smaller than endcolumn");
  	
  	//Perform the merge operation
   	spreadsheet.getActiveSheet().addMergedRegion( new CellRangeAddress( startRow, endRow, startCol, endCol ) );

  	return cfBooleanData.TRUE;
  }
  
}
