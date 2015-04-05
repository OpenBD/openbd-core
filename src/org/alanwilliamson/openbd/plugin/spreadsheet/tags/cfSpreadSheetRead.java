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
 *  $Id: cfSpreadSheetRead.java 1718 2011-10-07 19:22:20Z alan $
 */
package org.alanwilliamson.openbd.plugin.spreadsheet.tags;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alanwilliamson.openbd.plugin.spreadsheet.SheetUtility;
import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;
import com.naryx.tagfusion.expression.function.ext.ToCSV;
import com.naryx.tagfusion.expression.function.ext.ToHTML;

public class cfSpreadSheetRead implements Serializable {
  static final long serialVersionUID = 1;

	
  public void render( cfSpreadSheet tag, cfSession _Session ) throws cfmRunTimeException {
  
  	if ( tag.containsAttribute("NAME") ){

  		if ( !tag.containsAttribute("SRC") )
  			throw tag.newRunTimeException( "No SRC attribute given" );

  		cfSpreadSheetData	spreadsheet =	getSpreadSheetData( tag, _Session );
  		String name	= tag.getDynamic(_Session, "NAME").getString();
  		
  		if ( !tag.containsAttribute("FORMAT") ){

    		//Read in the spreadsheet object
    		_Session.setData( name, spreadsheet );
  			
  		}else{
  			
  			String format	= tag.getDynamic(_Session, "FORMAT" ).getString();
  			if ( format.equalsIgnoreCase("csv") ){
      		_Session.setData( name, new cfStringData( ToCSV.convertQueryToCsv( getQueryData(tag,_Session), ",") ) );
  			}else if ( format.equalsIgnoreCase("html") ){
        	_Session.setData( name, new cfStringData( ToHTML.convertQueryToHtml( getQueryData(tag,_Session)) ) );
  			}else{
  				throw tag.newRunTimeException( "The FORMAT (" + format + ") is not supported" );
  			}
  			
  		}
  		
  	}else if ( tag.containsAttribute("QUERY") ){
  		
  		String query			= tag.getDynamic(_Session, "QUERY").getString();
  		_Session.setData( query, getQueryData(tag,_Session) );
  		
  	}else
  		throw tag.newRunTimeException("Specify a NAME or QUERY or NAME+FORMAT attribute");
  
  }
  
  
  /*
   * Creates a QUERY object from the data; utilising all the control attributes
   */
  private cfQueryResultData	getQueryData(	 cfSpreadSheet tag, cfSession _Session ) throws cfmRunTimeException{
  	cfSpreadSheetData	spreadsheet =	getSpreadSheetData( tag, _Session );
  	

  	//How many columns are we using
  	int[]	columnsToUse;
  	if ( tag.containsAttribute("COLUMNS") ){
  		int x = 0;
  		List<Integer>	numbers	= tagUtils.getNumberListSorted( tag.getDynamic(_Session, "COLUMNS" ).getString() );
  		columnsToUse	= new int[ numbers.size() ];
  		Iterator<Integer> numbersIT = numbers.iterator();
  		while ( numbersIT.hasNext() )
  			columnsToUse[x++]	= (numbersIT.next() - 1);
  		
  	}else{
  		int maxColumns	= SheetUtility.getMaxColumn( spreadsheet.getActiveSheet() );
  		columnsToUse	= new int[ maxColumns ];
  		for ( int x=0; x<maxColumns; x++ )
  			columnsToUse[x]	= x;
  	}
  	
  	
  	//Figure out the columns
  	String columnLabels[] = null;
  	int startRow	 = 0;
  	if ( tag.containsAttribute("COLUMNNAMES") ){
  		columnLabels	= tag.getDynamic(_Session, "COLUMNNAMES" ).getString().split(",");
  		if ( columnLabels.length != columnsToUse.length )
  			throw tag.newRunTimeException( "The COLUMNNAMES does not match the number of columns" );
  	} else if ( tag.containsAttribute("HEADERROW") ){
  		
  		int headerRow = tag.getDynamic(_Session, "HEADERROW" ).getInt()-1;
  		Row row	= spreadsheet.getActiveSheet().getRow( headerRow );
  		if ( row == null )
  			throw tag.newRunTimeException( "The HEADERROW does not exist" );
  		
  		columnLabels	= new String[ columnsToUse.length ];
  		
  		for ( int c=0; c < columnsToUse.length; c++ ){
  			Cell cell	= row.getCell( columnsToUse[c] );
  			if ( cell == null )
  				columnLabels[c]	= "";
  			else
  				columnLabels[c] = cell.toString();
  		}
  		
  		startRow	= headerRow + 1;
  		
  	} else {
  		columnLabels	= new String[ columnsToUse.length ];
  		for ( int x=0; x < columnLabels.length; x++ ){
  			columnLabels[x] = "Column " + (columnsToUse[x] + 1);
  		}
  	}

  	//Create the query
  	cfQueryResultData queryData = new cfQueryResultData( columnLabels, "SpreadSheet" );
  	List<Map<String, cfData>> vResults = new ArrayList<Map<String, cfData>>();
  	
		Sheet sheet	= spreadsheet.getActiveSheet();
		Row row;
		Cell cell;
		cfData cfdata;

  	
  	if ( tag.containsAttribute("ROWS") ){
  		List<Integer>	rows	= tagUtils.getNumberListSorted( tag.getDynamic(_Session, "ROWS" ).getString() );
  		Iterator<Integer> rowsIT	= rows.iterator();
  		while ( rowsIT.hasNext() ){
  			int r = rowsIT.next() - 1;
  			
  			Map<String, cfData> hm = new FastMap<String, cfData>();
  			
  			if ( (row=sheet.getRow(r)) == null ) 
  				continue;
  			
  			for ( int c=0; c < columnsToUse.length; c++ ){
  				cell	= row.getCell( columnsToUse[c] );
  				if ( cell == null )
  					cfdata	= new cfStringData("");
  				else{
  					if ( cell.getCellType() == Cell.CELL_TYPE_BOOLEAN )
  						cfdata = cfBooleanData.getcfBooleanData( cell.getBooleanCellValue() );
  					else if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC )
  						cfdata = new cfNumberData( cell.getNumericCellValue() );
  					else if ( cell.getCellType() == Cell.CELL_TYPE_BLANK )
  						cfdata = new cfStringData("");
  					else if ( cell.getCellType() == Cell.CELL_TYPE_STRING )
  						cfdata = new cfStringData( cell.getStringCellValue() );
  					else
  						cfdata = new cfStringData("");
  				}
  				
  				hm.put( columnLabels[c], cfdata );
  			}
  			
  			vResults.add( hm ); 			
  		}
  		
  	}else{
  		/*
  		 * Read __ALL__ the rows associated with this spreadsheet
  		 */
  		for ( int r=startRow; r < sheet.getLastRowNum()+1; r++ ){
  			Map<String, cfData> hm = new FastMap<String, cfData>();
  			
  			if ( (row=sheet.getRow(r)) == null ) 
  				continue;
  			
  			for ( int c=0; c < columnsToUse.length; c++ ){
  				cell	= row.getCell( columnsToUse[c] );
  				if ( cell == null )
  					cfdata	= new cfStringData("");
  				else{
  					if ( cell.getCellType() == Cell.CELL_TYPE_BOOLEAN )
  						cfdata = cfBooleanData.getcfBooleanData( cell.getBooleanCellValue() );
  					else if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC )
  						cfdata = new cfNumberData( cell.getNumericCellValue() );
  					else if ( cell.getCellType() == Cell.CELL_TYPE_BLANK )
  						cfdata = new cfStringData("");
  					else if ( cell.getCellType() == Cell.CELL_TYPE_STRING )
  						cfdata = new cfStringData( cell.getStringCellValue() );
  					else
  						cfdata = new cfStringData("");
  				}
  				
  				hm.put( columnLabels[c], cfdata );
  			}
  			
  			vResults.add( hm );
  		}
  		
  		
  	}
  	queryData.populateQuery( vResults );
  	
  	return queryData;
  }
  
  
  
  /*
   * Creates the spreadsheet object
   */
  private cfSpreadSheetData	getSpreadSheetData(	 cfSpreadSheet tag, cfSession _Session ) throws cfmRunTimeException{
		File fileToRead	= new File( tag.getDynamic(_Session, "SRC").getString() );
		if ( !fileToRead.exists() )
			tag.newRunTimeException( fileToRead + " does not exist" );

		cfSpreadSheetData	spreadsheet = null;
		try {
			spreadsheet	= new cfSpreadSheetData( fileToRead );
		} catch (IOException e) {
			throw tag.newRunTimeException( "Failed to read XLS file (" + e.getMessage() + ")" );
		}
		
		//Set the active sheetname or sheetno
		if ( tag.containsAttribute("SHEET") ){
			spreadsheet.setActiveSheet( tag.getDynamic(_Session, "SHEET").getInt() - 1 );
		}else if ( tag.containsAttribute("SHEETNAME") ){
			spreadsheet.setActiveSheet( tag.getDynamic(_Session, "SHEETNAME").getString() );
		}
		
		return spreadsheet;
  }
}