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

import org.alanwilliamson.openbd.plugin.spreadsheet.SpreadSheetFormatOptions;
import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetSetCellComment extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetSetCellComment(){  min = 4;  max = 4; }
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"comment struct - keys of: anchor, author, bold, color, comment, fillcolor, font, horizontalalignment, italic, linestyle, linestylecolor, size, strikeout, underline, verticalalignment, visible",
			"rowNo",
			"columnNo"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Sets the comment properties of the cell", 
				ReturnType.BOOLEAN );
	}
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	if ( parameters.get(2).getDataType() != cfData.CFSTRUCTDATA )
  		throwException(_session, "parameter must be of type structure");

  	cfSpreadSheetData	spreadsheet = null;
  	cfStructData commentS = null;
  	int rowNo, columnNo;
  	
  	/*
  	 * Collect up the parameters
  	 */
		spreadsheet	= (cfSpreadSheetData)parameters.get(3);
		commentS		= (cfStructData)parameters.get(2);
		rowNo				= parameters.get(1).getInt() - 1;
		columnNo		= parameters.get(0).getInt() - 1;
  		
		if ( rowNo < 0 )
			throwException(_session, "row must be 1 or greater (" + rowNo + ")");
		if ( columnNo < 0 )
			throwException(_session, "column must be 1 or greater (" + columnNo + ")");
		
		
		/*
		 * Perform the insertion
		 */
		Sheet	sheet = spreadsheet.getActiveSheet();
		Row row	= sheet.getRow( rowNo );
		if ( row == null )
			row	= sheet.createRow( rowNo );

		Cell cell	= row.getCell( columnNo );
		if ( cell == null )
			cell = row.createCell( columnNo );
		
		
		// Create the anchor
		HSSFClientAnchor clientAnchor = new HSSFClientAnchor();
		if ( commentS.containsKey("anchor") ){
			String[] anchor	= commentS.getData("anchor").getString().split(",");
			if ( anchor.length != 4 )
				throwException(_session,"Invalid 'anchor' attribute, should be 4 numbers");
				
  		clientAnchor.setRow1( Integer.valueOf( anchor[0] ) - 1 );
  		clientAnchor.setCol1( Integer.valueOf( anchor[1] ) - 1 );
  		clientAnchor.setRow2( Integer.valueOf( anchor[2] ) - 1 );
  		clientAnchor.setCol2( Integer.valueOf( anchor[3] ) - 1 );
		}else{
  		clientAnchor.setRow1( rowNo );
  		clientAnchor.setCol1( columnNo );
  		clientAnchor.setRow2( rowNo + 2 );
  		clientAnchor.setCol2( columnNo + 2 );
		}
		
		// Create the comment
		Comment comment = spreadsheet.getActiveSheet().createDrawingPatriarch().createCellComment(clientAnchor);
		
		if ( commentS.containsKey("author") ){
			comment.setAuthor( commentS.getData("author").getString() );
		}
		
		if ( commentS.containsKey("visible") ){
			comment.setVisible( commentS.getData("visible").getBoolean() );
		}
		
		if ( commentS.containsKey("comment") ){
			HSSFRichTextString richText = new HSSFRichTextString( commentS.getData("comment").getString() );
			try {
				richText.applyFont( SpreadSheetFormatOptions.createCommentFont(spreadsheet.getWorkBook(), commentS) );
			} catch (Exception e) {
				throwException( _session, e.getMessage() );
			}
			
			comment.setString( richText );
		}
		
		cell.setCellComment( comment );
  	return cfBooleanData.TRUE;
  }
}