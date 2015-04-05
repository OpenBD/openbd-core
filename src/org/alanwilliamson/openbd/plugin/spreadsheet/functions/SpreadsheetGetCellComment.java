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


import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetGetCellComment extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetGetCellComment(){  min = 1;  max = 3; }
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"rowNo",
			"columnNo"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Returns the comment stucture for the given cell on the active sheet", 
				ReturnType.STRUCTURE );
	}
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	Collections.reverse( parameters );

  	if ( parameters.size() == 2 ){
  		throwException(_session,"please specify both a row and a column");  		
  	}
  	
  	cfSpreadSheetData	spreadsheet = (cfSpreadSheetData)parameters.get(0);
  	Sheet	sheet = spreadsheet.getActiveSheet();
  	
  	if ( parameters.size() == 3 ){
  		int rowNo				= parameters.get(1).getInt() - 1;
  		int columnNo		= parameters.get(0).getInt() - 1;
    		
  		if ( rowNo < 0 )
  			throwException(_session, "row must be 1 or greater (" + rowNo + ")");
  		if ( columnNo < 0 )
  			throwException(_session, "column must be 1 or greater (" + columnNo + ")");

  		cfStructData	sd	= new cfStructData();
  		
  		Row row	= sheet.getRow( rowNo );
  		if ( row != null ){
  			Cell cell	= row.getCell( columnNo );
  			if ( cell != null ){
  				Comment comment = cell.getCellComment();
  				if ( comment != null ){
	  				sd.setData("column", 	new cfNumberData(columnNo) );
	  				sd.setData("row", 		new cfNumberData(rowNo) );
	  				sd.setData("author", 	new cfStringData(comment.getAuthor()) );
	  				sd.setData("comment", new cfStringData(comment.getString().getString()) );
  				}
  			}
  		}
  		
  		return sd;
  	}else{
  		cfArrayData	arr = cfArrayData.createArray(1);
  		
  		Iterator<Row> rowIT	= sheet.rowIterator();
  		while ( rowIT.hasNext() ){
  			Row row	= rowIT.next();
  			
  			Iterator<Cell> cellIT = row.cellIterator();
  			while ( cellIT.hasNext() ){
  				Cell cell = cellIT.next();
  				Comment comment = cell.getCellComment();
  				if ( comment != null ){
  					cfStructData	sd	= new cfStructData();
	  				sd.setData("column", 	new cfNumberData( cell.getColumnIndex() + 1 ) );
	  				sd.setData("row", 		new cfNumberData( row.getRowNum()+1 ) );
	  				sd.setData("author", 	new cfStringData( comment.getAuthor() ) );
	  				sd.setData("comment", new cfStringData( comment.getString().getString() ) );
	  				arr.addElement( sd );
  				}
  			}
  		}
  		
  		return arr;
  	}

  }
}
