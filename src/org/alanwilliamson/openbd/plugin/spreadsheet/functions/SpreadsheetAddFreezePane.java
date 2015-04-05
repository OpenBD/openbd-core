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

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetAddFreezePane extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetAddFreezePane(){  min = 2;  max = 5; }
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"freeze rowNo",
			"freeze columnNo",
			"rowNo - default at the end",
			"columnNo - default at the right most"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Creates a freeze panel within the current spreadsheet", 
				ReturnType.BOOLEAN );
	}
	
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	cfSpreadSheetData	spreadsheet = null;
  	
  	/*
  	 * Collect up the parameters
  	 */
  	if ( parameters.size() == 3 ){
  		spreadsheet		= (cfSpreadSheetData)parameters.get(2);
  		int freezecol	= parameters.get(1).getInt()-1;
  		int freezerow	= parameters.get(0).getInt()-1;
  		
    	if ( freezecol < 0 )
    		throwException(_session, "freezecol must be 1 or greater (" + freezecol + ")");
    	if ( freezerow < 0 )
    		throwException(_session, "freezerow must be 1 or greater (" + freezerow + ")");

    	spreadsheet.getActiveSheet().createFreezePane( freezecol, freezerow );
  		
  	}else if ( parameters.size() == 5 ){
  		spreadsheet		= (cfSpreadSheetData)parameters.get(4);
  		int freezecol	= parameters.get(3).getInt()-1;
  		int freezerow	= parameters.get(2).getInt()-1;
  		int col				= parameters.get(1).getInt()-1;
  		int row				= parameters.get(0).getInt()-1;

  		if ( freezecol < 0 )
    		throwException(_session, "freezecol must be 1 or greater (" + freezecol + ")");
    	if ( freezerow < 0 )
    		throwException(_session, "freezerow must be 1 or greater (" + freezerow + ")");
  		if ( col < 0 )
    		throwException(_session, "column must be 1 or greater (" + col + ")");
    	if ( row < 0 )
    		throwException(_session, "row must be 1 or greater (" + row + ")");

    	spreadsheet.getActiveSheet().createFreezePane( freezecol, freezerow, col, row );
  	
  	}else{
  		throwException(_session, "You must supply row and column");
  		return null;
  	}
  	
  	return cfBooleanData.TRUE;
  }
  
}
