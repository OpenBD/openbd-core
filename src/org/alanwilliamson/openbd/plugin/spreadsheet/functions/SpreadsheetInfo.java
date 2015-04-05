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
import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetInfo extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetInfo(){  min = 1;  max = 1; }
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object"	
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Reads the meta information associated with the spreadsheet", 
				ReturnType.STRUCTURE );
	}
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	cfSpreadSheetData	spreadsheet = null;
  	spreadsheet		= (cfSpreadSheetData)parameters.get(0);
  	
  	Workbook	workbook	= spreadsheet.getWorkBook();
  	cfStructData	s	= new cfStructData();
  	
  	
  	/*
  	 * Sheet details
  	 */
  	s.setData( "sheets", new cfNumberData( workbook.getNumberOfSheets() ) );
  	cfArrayData	sheetArr	= cfArrayData.createArray(1);
  	for ( int x=0; x < workbook.getNumberOfSheets(); x++ ){
  		String name	= workbook.getSheetName(x);
  		if ( name == null )
  			name = "";
  		
  		sheetArr.addElement( new cfStringData(name) );
  	}
  	s.setData( "sheetnames", sheetArr );
  	
  	
  	/*
  	 * Workbook type
  	 */
  	if ( workbook instanceof XSSFWorkbook ){
  		s.setData( "spreadsheettype", new cfStringData("xlsx") );
  	}else{
  		s.setData( "spreadsheettype", new cfStringData("xls") );
  	}
  	
  	
  	/*
  	 * XSSFWorkbook
  	 */
  	if ( workbook instanceof XSSFWorkbook ){
  		XSSFWorkbook xSSFWorkbook = (XSSFWorkbook)workbook;
  		
  		CoreProperties cP = xSSFWorkbook.getProperties().getCoreProperties();
  		s.setData( "category", 		new cfStringData( cP.getCategory() ) );
  		s.setData( "subject", 		new cfStringData( cP.getSubject() ) );
  		s.setData( "title", 			new cfStringData( cP.getTitle() ) );
  		s.setData( "revision", 		new cfStringData( cP.getRevision() ) );
  		s.setData( "author", 			new cfStringData( cP.getCreator() ) );
  		s.setData( "description",	new cfStringData( cP.getDescription() ) );
  		
  		if ( cP.getLastPrinted() != null )
   			s.setData( "lastprinted", 	new cfDateData( cP.getLastPrinted() ) );
  		if ( cP.getModified() != null )
   			s.setData( "lastsaved", 	new cfDateData( cP.getModified() ) );
  		if ( cP.getCreated() != null )
   			s.setData( "creationdate", 	new cfDateData( cP.getCreated() ) );
  		 		
  	}else{
  		HSSFWorkbook hSSFWorkbook = (HSSFWorkbook)workbook;
  		DocumentSummaryInformation dSummary = hSSFWorkbook.getDocumentSummaryInformation();
  		
  		if ( dSummary == null ){
    		s.setData( "category", 		cfStringData.EMPTY_STRING );
    		s.setData( "company", 		cfStringData.EMPTY_STRING );
    		s.setData( "manager", 		cfStringData.EMPTY_STRING );
  		}else{
    		s.setData( "category", 		new cfStringData( dSummary.getCategory() ) );
    		s.setData( "company", 		new cfStringData( dSummary.getCompany() ) );
    		s.setData( "manager", 		new cfStringData( dSummary.getManager() ) );
  		}
  		
  		
  		SummaryInformation sInformation = hSSFWorkbook.getSummaryInformation();
  		if ( sInformation == null ){
  			
    		s.setData( "author", 		cfStringData.EMPTY_STRING );
    		s.setData( "comments",	cfStringData.EMPTY_STRING );
     		s.setData( "keywords",	cfStringData.EMPTY_STRING );
     		s.setData( "lastauthor",cfStringData.EMPTY_STRING );
     		s.setData( "title",			cfStringData.EMPTY_STRING );
     		s.setData( "subject",		cfStringData.EMPTY_STRING );
  			
  		}else{
  			
    		s.setData( "author", 		new cfStringData( sInformation.getAuthor() ) );
    		s.setData( "comments",	new cfStringData( sInformation.getComments() ) );
     		s.setData( "keywords",	new cfStringData( sInformation.getKeywords() ) );
     		s.setData( "lastauthor",new cfStringData( sInformation.getLastAuthor() ) );
     		s.setData( "title",			new cfStringData( sInformation.getTitle() ) );
     		s.setData( "subject",		new cfStringData( sInformation.getSubject() ) );

     		if ( sInformation.getCreateDateTime() != null )
     			s.setData( "creationdate", 	new cfDateData( sInformation.getCreateDateTime() ) );
     		
     		if ( sInformation.getLastSaveDateTime() != null )
     			s.setData( "lastsaved", 		new cfDateData( sInformation.getLastSaveDateTime() ) );

     		if ( sInformation.getLastPrinted() != null )
     			s.setData( "lastprinted", 	new cfDateData( sInformation.getLastPrinted() ) );
  			
  		}

  	}
  	
  	return s;
  }
}
