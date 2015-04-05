/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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
package com.naryx.tagfusion.expression.function.ext;

import java.io.IOException;
import java.io.StringReader;

import au.com.bytecode.opencsv.CSVReader;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class ReadCSV extends functionBase {
	private static final long serialVersionUID = 1;
  
	public ReadCSV() { min = 1; max = 4; setNamedParams( new String[]{ "string", "headerline", "delimiter", "qualifier" } ); }
  
	public String[] getParamInfo(){
		return new String[]{
			"string",
			"header first line - does the first contain the column names - default false",
			"delimiter - default comma (,)",
			"qualifier - default double quote (\")"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"query", 
				"Reads a CSV separated block and transform it to a Query object", 
				ReturnType.QUERY );
	} 

	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		String csvBlock		= getNamedStringParam( argStruct, "string", null ).trim();
		boolean firstLine	= getNamedBooleanParam( argStruct, "headerline", true );
		char delimiter	  = getAttributeAsChar( getNamedStringParam( argStruct, "delimiter", "," ) );
		char qualifier	  = getAttributeAsChar( getNamedStringParam( argStruct, "qualifier", "\"" ) );

		CSVReader reader = null;

		cfQueryResultData queryResult = null;
		String [] csvLine;
		
    try {
    	reader = new CSVReader( new StringReader(csvBlock), delimiter, qualifier );
    	
			while ((csvLine = reader.readNext()) != null) {
				if ( queryResult == null ){
					String columns[]	= new String[ csvLine.length ];
					
					if ( firstLine ){
						for ( int x=0; x<columns.length;x++){
							if ( csvLine[x].trim().length() == 0 )
								columns[x] = "Column" + (x+1);
							else
								columns[x] = csvLine[x];
						}
						
						queryResult	= new cfQueryResultData( columns, "FromCSV" );
						continue;
					}else{
						for ( int x=0; x<columns.length;x++)
							columns[x] = "Column" + (x+1);

						queryResult	= new cfQueryResultData( columns, "FromCSV" );
					}
				}

				// Add the row
				queryResult.addRow(1);
				queryResult.setCurrentRow( queryResult.getSize() );
				
				for ( int x=0; x<csvLine.length; x++ ){
					queryResult.setCell(x+1, new cfStringData( csvLine[x] ) );
				}
			}
			
		} catch (Exception e) {
			throwException(_session, e.getMessage());
		} finally {
			try {
				reader.close();
			} catch (IOException e) {}
		}

    queryResult.reset();
		return queryResult;
	}
	
	private char getAttributeAsChar( String _attributeValue ){
		char attribChar;
		if ( _attributeValue.equalsIgnoreCase("tab"))
			attribChar	= '\t';
		else
			attribChar	= _attributeValue.charAt(0);
		
		return attribChar;
	}
}
