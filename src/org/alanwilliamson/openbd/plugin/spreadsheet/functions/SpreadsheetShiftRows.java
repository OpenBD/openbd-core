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
import org.apache.poi.ss.usermodel.Sheet;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetShiftRows extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetShiftRows(){  min = 2;  max = 4; }
	
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"start row",
			"end row",
			"count"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Moves rows around", 
				ReturnType.BOOLEAN );
	}
	

  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	cfSpreadSheetData	spreadsheet = null;
  	int start=0, end=0, rows = 1;
  	
  	
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
  		rows				= parameters.get(0).getInt() - 1;
  	}else if ( parameters.size() == 4 ){
  		spreadsheet	= (cfSpreadSheetData)parameters.get(3);
  		start				= parameters.get(2).getInt() - 1;
  		end					= parameters.get(1).getInt() - 1;
  		rows				= parameters.get(0).getInt() - 1;
  	}  	
  	
  	/*
  	 * Validate parameters
  	 */
  	if ( start < 0 )
  		throwException(_session, "column must be 1 or greater (" + start + ")");
  	
  	Sheet	sheet = spreadsheet.getActiveSheet();
  	sheet.shiftRows( start, end, rows, true, true );

  	return cfBooleanData.TRUE;
 	}
	
}
