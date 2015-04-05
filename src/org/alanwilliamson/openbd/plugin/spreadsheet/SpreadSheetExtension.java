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
 *  http://openbd.org/
 *  $Id: SpreadSheetExtension.java 1891 2011-12-27 20:41:26Z alan $
 */

package org.alanwilliamson.openbd.plugin.spreadsheet;

import com.bluedragon.plugin.Plugin;
import com.bluedragon.plugin.PluginManagerInterface;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class SpreadSheetExtension implements Plugin {
	public String getPluginDescription() {
		return "SpreadSheetPlugin";
	}

	public String getPluginName() {
		return "SpreadSheet";
	}

	public String getPluginVersion() {
		return "1.2011.11.4";
	}
	
	public void pluginStop(PluginManagerInterface manager) {}
	
	public void pluginStart(PluginManagerInterface manager, xmlCFML systemParameters) {
		SpreadSheetFormatOptions.initialize();
		
		// Register the tags
		manager.registerTag( "cfspreadsheet", "org.alanwilliamson.openbd.plugin.spreadsheet.tags.cfSpreadSheet" );
		
		// Register the functions
		manager.registerFunction( "IsSpreadsheetObject",		"org.alanwilliamson.openbd.plugin.spreadsheet.functions.IsSpreadsheetObject" );
		manager.registerFunction( "SpreadsheetNew", 				"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetNew" );
		manager.registerFunction( "SpreadsheetWrite", 			"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetWrite" );
		manager.registerFunction( "SpreadsheetRead", 				"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetRead" );
		manager.registerFunction( "SpreadsheetReadBinary", 	"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetReadBinary" );
		
		manager.registerFunction( "SpreadsheetCreateSheet", 					"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetCreateSheet" );
		manager.registerFunction( "SpreadsheetDeleteSheet", 					"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetDeleteSheet" );
		manager.registerFunction( "SpreadsheetSetActiveSheet", 				"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetSetActiveSheet" );
		manager.registerFunction( "SpreadsheetSetActiveSheetNumber", 	"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetSetActiveSheetNumber" );
		
		manager.registerFunction( "SpreadsheetSetCellValue", 		"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetSetCellValue" );
		manager.registerFunction( "SpreadsheetSetCellFormula", 	"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetSetCellFormula" );
		
		manager.registerFunction( "SpreadsheetSetCellComment", 	"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetSetCellComment" );
		manager.registerFunction( "SpreadsheetGetCellComment", 	"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetGetCellComment" );
		
		manager.registerFunction( "SpreadsheetGetCellValue", 		"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetGetCellValue" );
		manager.registerFunction( "SpreadsheetGetCellFormula", 	"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetGetCellFormula" );
		
		manager.registerFunction( "SpreadsheetSetColumnWidth", 	"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetSetColumnWidth" );
		manager.registerFunction( "SpreadsheetSetRowWidth", 		"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetSetRowWidth" );

		manager.registerFunction( "SpreadsheetSetHeader", 			"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetSetHeader" );
		manager.registerFunction( "SpreadsheetSetFooter", 			"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetSetFooter" );
		
		manager.registerFunction( "SpreadsheetFormatCell", 			"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetFormatCell" );
		manager.registerFunction( "SpreadsheetFormatColumn", 		"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetFormatColumn" );
		manager.registerFunction( "SpreadsheetFormatColumns", 	"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetFormatColumn" );
		manager.registerFunction( "SpreadsheetFormatRow", 			"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetFormatRow" );
		manager.registerFunction( "SpreadsheetFormatRows", 			"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetFormatRow" );
		
		manager.registerFunction( "SpreadsheetDeleteRow", 			"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetDeleteRow" );
		manager.registerFunction( "SpreadsheetDeleteRows", 			"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetDeleteRow" );
		manager.registerFunction( "SpreadsheetDeleteColumn", 		"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetDeleteColumn" );
		manager.registerFunction( "SpreadsheetDeleteColumns", 	"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetDeleteColumn" );
		manager.registerFunction( "SpreadsheetRemoveColumn", 		"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetRemoveColumn" );
		manager.registerFunction( "SpreadsheetAddColumn", 			"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetAddColumn" );
		manager.registerFunction( "SpreadsheetShiftColumns", 		"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetShiftColumns" );

		manager.registerFunction( "SpreadsheetAddRow", 					"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetAddRow" );
		manager.registerFunction( "SpreadsheetAddRows", 				"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetAddRow" );
		manager.registerFunction( "SpreadsheetShiftRows", 			"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetShiftRows" );
		manager.registerFunction( "SpreadsheetAddFreezePane", 	"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetAddFreezePane" );
		manager.registerFunction( "SpreadsheetMergeCells", 			"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetMergeCells" );
		manager.registerFunction( "SpreadsheetAddSplitPane", 		"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetAddSplitPane" );

		manager.registerFunction( "SpreadsheetAddInfo", 				"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetAddInfo" );
		manager.registerFunction( "SpreadsheetInfo", 						"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetInfo" );

		manager.registerFunction( "SpreadsheetAddImage",				"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetAddImage" );

		manager.registerFunction( "SpreadsheetFindCell",				"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetFindCell" );
		
		manager.registerFunction( "SpreadsheetColumnFitToSize",	"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetColumnFitToSize" );
		manager.registerFunction( "SpreadsheetQueryRead",				"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetQueryRead" );
		manager.registerFunction( "SpreadsheetQueryWrite",			"org.alanwilliamson.openbd.plugin.spreadsheet.functions.SpreadsheetQueryWrite" );
	}

}
