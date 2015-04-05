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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetSetRowWidth extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetSetRowWidth(){  min = 3;  max = 3; }
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"row no",
			"row width - pixels"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Sets width of the row", 
				ReturnType.BOOLEAN );
	}	
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	cfSpreadSheetData	spreadsheet = null;
  	int rowNo, rowHeight;
  	
  	/*
  	 * Collect up the parameters
  	 */
		spreadsheet	= (cfSpreadSheetData)parameters.get(2);
		rowNo				= parameters.get(1).getInt() - 1;
		rowHeight		= parameters.get(0).getInt();

		if ( rowNo < 0 )
			throwException(_session, "row must be 1 or greater (" + rowNo + ")");

		Sheet	sheet = spreadsheet.getActiveSheet();
		Row row	= sheet.getRow( rowNo );
		if ( row == null )
			row	= sheet.createRow( rowNo );
		
		row.setHeightInPoints( rowHeight );

  	return cfBooleanData.TRUE;
  }
}
