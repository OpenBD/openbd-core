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
 *  http://www.openbluedragon.org/
 *  
 *  $Id: SpreadsheetSetColumnWidth.java 1718 2011-10-07 19:22:20Z alan $
 */
package org.alanwilliamson.openbd.plugin.spreadsheet.functions;

import java.util.List;

import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetSetColumnWidth extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetSetColumnWidth(){  min = 3;  max = 3; }
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"column number (1+)",
			"column width in points; (try 1000)"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Sets width of the given column", 
				ReturnType.BOOLEAN );
	}
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	cfSpreadSheetData	spreadsheet = null;
  	int columnWidth, columnNo;
  	
  	/*
  	 * Collect up the parameters
  	 */
		spreadsheet	= (cfSpreadSheetData)parameters.get(2);
		columnNo		= parameters.get(1).getInt() - 1;
		columnWidth	= parameters.get(0).getInt();

		if ( columnNo < 0 )
			throwException(_session, "column must be 1 or greater (" + columnNo + ")");

		spreadsheet.getActiveSheet().setColumnWidth( columnNo, columnWidth );

  	return cfBooleanData.TRUE;
  }
}
