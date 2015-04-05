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
 */
package org.alanwilliamson.openbd.plugin.spreadsheet.functions;

import java.util.List;

import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetNew extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetNew(){  min = 0;  max = 2; }
  
	 
	public String[] getParamInfo(){
		return new String[]{
			"type - boolean flag; set to true to create a XLSX spreadsheet, and false for an XLS sheet (default)",
			"defaultsheet - The name of the default sheet"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Creates a new spreadsheet object", 
				ReturnType.SPREADSHEET );
	}
	
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	boolean	xlsx	= false;
  	String defaultSheet	= "sheet1";
  	
  	if ( parameters.size() == 2 ){
  		xlsx					= parameters.get(0).getBoolean();
  		defaultSheet	= parameters.get(1).getString();
  	}else if ( parameters.size() == 1 ){
  		cfData param = parameters.get(0);
  		try{
  			xlsx	= param.getBoolean();
  		}catch(Exception e){
  			defaultSheet = param.getString();
  		}
  	}
  	
  	return new cfSpreadSheetData( xlsx, defaultSheet );
  }
}
