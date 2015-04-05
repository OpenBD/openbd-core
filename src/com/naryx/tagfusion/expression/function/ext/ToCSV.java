/* 
 *  Copyright (C) 2000 - 2014 TagServlet Ltd
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
 *  $Id: ToCSV.java 2432 2014-03-30 20:09:10Z alan $
 */
package com.naryx.tagfusion.expression.function.ext;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.expression.function.functionBase;

public class ToCSV extends functionBase {
	private static final long serialVersionUID = 1;
	private static final String defaultLineDelimiter = "\r\n";
  
	public ToCSV() { min = 1; max = 4; setNamedParams( new String[]{ "query", "headerline", "delimiter", "lineDelimiter" } ); }
  
	public String[] getParamInfo(){
		return new String[]{
			"query",
			"header first line - contain the column names - default true",
			"delimiter - default comma (,) - use 'tab' string for tabs",
			"line delimiter - default CRLF (\\r\\n)"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"query", 
				"Transforms the query object into a Comma Separated Value (CSV) block", 
				ReturnType.STRING );
	} 

	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		if ( getNamedParam(argStruct, "query").getDataType() != cfData.CFQUERYRESULTDATA )
			throwException(_session, "Parameter must be of type query");

		cfQueryResultData queryData	 = (cfQueryResultData)getNamedParam(argStruct, "query");
		boolean firstLine	= getNamedBooleanParam( argStruct, "headerline", true );
		String delimitor	= getNamedStringParam( argStruct, "delimiter", "," );
		String lineDelimiter	= getNamedStringParam( argStruct, "lineDelimiter", defaultLineDelimiter );
		
		if ( delimitor.equalsIgnoreCase("tab"))
			delimitor	= "\t";
	
		return new cfStringData( convertQueryToCsv(queryData,firstLine,delimitor,lineDelimiter) );
	}
	
	public static	String convertQueryToCsv( cfQueryResultData queryData, String delimitor ) throws dataNotSupportedException{
		return convertQueryToCsv( queryData, true, delimitor, defaultLineDelimiter ); 
	}
	
	public static	String convertQueryToCsv( cfQueryResultData queryData, boolean bFirstLine, String delimitor, String lineDelimiter ) throws dataNotSupportedException{
		StringBuilder	bluffy	= new StringBuilder( 32000 );
		
		String cols[] = queryData.getColumnNames();
		
		//Columns
		if ( bFirstLine ){
			for ( int c=0; c < cols.length; c++ ){
				bluffy.append( escapeColumn( cols[c] ) );
				if ( c < cols.length-1 )
					bluffy.append( delimitor );
			}
			bluffy.append( lineDelimiter );
		}
		
		//Rows
		for ( int r=0; r < queryData.getSize(); r++ ){
			for ( int c=0; c < cols.length; c++ ){
				
				cfData cell	= queryData.getCell( r+1, c+1 );
				if ( cell.getDataType() == cfData.CFBOOLEANDATA )
					bluffy.append( cell.getBoolean() );
				else if ( cell.getDataType() == cfData.CFNUMBERDATA )
					bluffy.append( cell.getString() );
				else if ( cell.getDataType() == cfData.CFNULLDATA )
          bluffy.append( "NULL" );
				else 
					bluffy.append( escapeColumn( cell.getString() ) );
				
				if ( c < cols.length-1 )
					bluffy.append( delimitor );
			}
			bluffy.append( lineDelimiter );
		}

		return bluffy.toString();
	}
	
	private static String escapeColumn( String s ){
		s = s.replaceAll( "\"", "\"\"");
		return "\"" + s + "\"";
	}
	
}
