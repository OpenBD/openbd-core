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
import java.util.regex.Pattern;

import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetFindCell extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetFindCell(){  min = 2;  max = 2; }

	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"value - discreet value or a regular expression"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Looks for all cells in the active sheet that match the expression.  An array of structs (row,column,value) is returned.", 
				ReturnType.ARRAY );
	}
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	cfSpreadSheetData	spreadsheet = (cfSpreadSheetData)parameters.get(1);
  	Pattern pattern = Pattern.compile( parameters.get(0).getString() );

  	cfArrayData	arr	= cfArrayData.createArray(1);

  	Iterator<Row>	rowIT	= spreadsheet.getActiveSheet().rowIterator();
  	while ( rowIT.hasNext() ){
  		Row row	= rowIT.next();
  		
  		Iterator<Cell>	cellIT	= row.cellIterator();
  		while ( cellIT.hasNext() ){
  			Cell cell	= cellIT.next();
  			String cellValue = null;
  			
  			if ( cell.getCellType() == Cell.CELL_TYPE_STRING )
  				cellValue	= cell.getStringCellValue();
  			else if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC )
  				cellValue	= String.valueOf( cell.getNumericCellValue() );
  			else
  				cellValue	= cell.toString();

  			if ( pattern.matcher( cellValue ).find() ){
  				cfStructData	s	= new cfStructData();
  				s.setData( "row", 		new cfNumberData( cell.getRowIndex() + 1 ) );
  				s.setData( "column", 	new cfNumberData( cell.getColumnIndex() + 1 ) );
  				s.setData( "value", 	new cfStringData( cellValue ) );
  				arr.addElement( s );
  			}
  		}
  	}

  	return arr;
  }
}
