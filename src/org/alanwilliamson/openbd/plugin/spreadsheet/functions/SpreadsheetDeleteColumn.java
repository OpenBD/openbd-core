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
import java.util.Set;

import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetDeleteColumn extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetDeleteColumn(){  min = 2;  max = 2; }
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"column no - expressed as either a list of numbers or a range (2,3,5-9)"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Deletes one or more columns from the active sheet; it will not shift any other columns", 
				ReturnType.BOOLEAN );
	}
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	cfSpreadSheetData	spreadsheet = null;
  	String cols;
  	
  	/*
  	 * Collect up the parameters
  	 */
		spreadsheet	= (cfSpreadSheetData)parameters.get(1);
		cols				= parameters.get(0).getString();
		
		Sheet	sheet = spreadsheet.getActiveSheet();
		
		Set<Integer>	numbers	= tagUtils.getNumberSet( cols );
		Iterator<Row> rowIT	= sheet.rowIterator();
		while ( rowIT.hasNext() ){
			Row row	= rowIT.next();
			
			Iterator<Integer>	columnIndx 	= numbers.iterator();
			while ( columnIndx.hasNext() ){
				Cell cell = row.getCell( columnIndx.next() - 1 );
				if ( cell != null ){
					row.removeCell( cell );
				}
			}
		}

  	return cfBooleanData.TRUE;
  }
}
