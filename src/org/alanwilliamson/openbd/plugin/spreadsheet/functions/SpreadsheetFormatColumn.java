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
 *  $Id: SpreadsheetFormatColumn.java 1718 2011-10-07 19:22:20Z alan $
 */
package org.alanwilliamson.openbd.plugin.spreadsheet.functions;


import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.alanwilliamson.openbd.plugin.spreadsheet.SpreadSheetFormatOptions;
import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetFormatColumn extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetFormatColumn(){  min = max = 3; }
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"format Structure. Keys: " + SpreadSheetFormatOptions.createCellStyleHelp(),
			"columnNo - expressed as single or a range (1,2,3,5-8)",
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Sets the default formatting properties for the given column(s).  It will not reformat any existing columns", 
				ReturnType.BOOLEAN );
	}
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	cfSpreadSheetData	spreadsheet = null;
  	cfData value;
  	cfData column;
  	
  	/*
  	 * Collect up the parameters
  	 */
		spreadsheet	= (cfSpreadSheetData)parameters.get(2);
		value				= parameters.get(1);
		column			= parameters.get(0);
		
		if ( value.getDataType() != cfData.CFSTRUCTDATA )
			throwException(_session, "format object must be a structure");
		

		/*
		 * Get the CellStyle
		 */
		CellStyle	cellstyle = null;
		try {
			cellstyle = SpreadSheetFormatOptions.createCellStyle( spreadsheet.getWorkBook(), (cfStructData)value );
		} catch (Exception e) {
			throwException(_session, e.getMessage() );
		}
			
		// Not a single number; lets try the string method
		Set<Integer>	numbers	= tagUtils.getNumberSet( column.getString() );
		Iterator<Integer> it	= numbers.iterator();
		while ( it.hasNext() ){
			formatColumn( spreadsheet.getActiveSheet(), cellstyle, it.next() - 1 );
		}
		
  	return cfBooleanData.TRUE;
  }
  
  private void formatColumn( Sheet	sheet, CellStyle style, int columnNo ){
  	sheet.setDefaultColumnStyle( columnNo, style );
  }
}
