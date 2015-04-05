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
 *  
 *  $Id: cfSpreadSheet.java 1718 2011-10-07 19:22:20Z alan $
 */
package org.alanwilliamson.openbd.plugin.spreadsheet.tags;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfSpreadSheet extends cfTag implements Serializable {
  static final long serialVersionUID = 1;

  public java.util.Map getInfo(){
  	return createInfo("spreadsheet-plugin", "Read, write and update spreadsheet files");
  }
  
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
 			createAttInfo("ACTION=READ", 		"Reads a spreadsheet file into a spreadsheet object or query", "", true ),
 			createAttInfo("ACTION=WRITE", 	"Writes data to a spreadsheet file", "", true ),
			createAttInfo("ACTION=UPDATE",	"Updates an existing spreadsheet file", "", true ),
			
 			createAttInfo("SHEET", 				"The Sheet number to be active (only SHEET or SHEETNAME must be present)", "", false ),
 			createAttInfo("SHEETNAME", 		"The Sheet name to be active (only SHEET or SHEETNAME must be present)", "", false ),
 			
 			createAttInfo("HEADERROW", 		"The row in the spreadsheet that is to be considered the header for the query (only HEADERROW or COLUMNNAMES must be present)", "", false ),
 			createAttInfo("COLUMNNAMES",	"The comma-separated names for the column names (only HEADERROW or COLUMNNAMES must be present)", "", false ),

 			createAttInfo("SRC",					"The source for the file, expressed as full path", "", false ),
 			createAttInfo("FORMAT",				"The format for the (csv,html) for the resulting object; if not present then NAME is the spreadsheet object", "", false ),
 			createAttInfo("NAME",					"The name of the variable that will receive/extract the data", "", false ),

 			createAttInfo("QUERY",				"The query variable that will receive/extract the data in CFML Query object", "", false ),
 			
 			createAttInfo("COLUMNS",			"The columns to be used in the format (1,2,3,5-6)", "all", false ),
 			createAttInfo("ROWS",					"The rows to be used in the format (1,2,3,5-6)", "all", false ),
 			
 			createAttInfo("FILENAME",			"The filename to use when writing or updating", "", false ),
 			createAttInfo("OVERWRITE",		"Boolean flag to overwrite the file", "false", false )
  	};
  }
  
  protected void defaultParameters( String _tag ) throws cfmBadFileException {
		parseTagHeader( _tag );
		
		if ( !containsAttribute("ACTION") )
			throw newBadFileException( "Missing ACTION", "You need to provide an ACTION (READ|UPDATE|WRITE)" );
		
		if ( containsAttribute("SHEET") && containsAttribute("SHEETNAME") )
			throw newBadFileException( "Invalid Attribute", "You must only provide SHEET _OR_ SHEETNAME, but not both" );

		if ( containsAttribute("HEADERROW") && containsAttribute("COLUMNNAMES") )
			throw newBadFileException( "Invalid Attribute", "You must only provide HEADERROW _OR_ COLUMNNAMES, but not both" );
	}

	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {

		String ACTION = getConstant("ACTION");
		if ( ACTION.equalsIgnoreCase("read") ){
			new cfSpreadSheetRead().render( this, _Session );
		}else if ( ACTION.equalsIgnoreCase("write") ){
			new cfSpreadSheetWrite().render( this, _Session );
		}else if ( ACTION.equalsIgnoreCase("update") ){
			new cfSpreadSheetUpdate().render( this, _Session );
		}else
			throw newRunTimeException( "Invalid attribute for ACTION (READ|UPDATE|WRITE)" );

		return cfTagReturnType.NORMAL;
	}
}