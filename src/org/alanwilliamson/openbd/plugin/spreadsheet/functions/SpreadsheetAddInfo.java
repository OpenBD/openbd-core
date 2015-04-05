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

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetAddInfo extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetAddInfo(){  min = 2;  max = 2; }
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"structure - items include (author, category, subject, title, revision, description, manager, company, comments, lastauthor)"			
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Updates or inserts spreadsheet level info", 
				ReturnType.BOOLEAN );
	}
	
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	if ( parameters.get(0).getDataType() != cfData.CFSTRUCTDATA )
  		throwException(_session, "parameter must be of type structure");
  	
  	cfSpreadSheetData	spreadsheet	= (cfSpreadSheetData)parameters.get(1);
  	cfStructData	s	= (cfStructData)parameters.get(0);
  	
  	Workbook	workbook	= spreadsheet.getWorkBook();
  	
  	/*
  	 * XSSFWorkbook
  	 */
  	if ( workbook instanceof XSSFWorkbook ){
  		XSSFWorkbook xSSFWorkbook = (XSSFWorkbook)workbook;
  		
  		CoreProperties cP = xSSFWorkbook.getProperties().getCoreProperties();
  		
  		if ( s.containsKey("author") )
  			cP.setCreator( s.getData("author").getString() );
  		if ( s.containsKey("category") )
  			cP.setCategory( s.getData("category").getString() );
  		if ( s.containsKey("subject") )
  			cP.setSubjectProperty( s.getData("subject").getString() );
  		if ( s.containsKey("title") )
  			cP.setTitle( s.getData("title").getString() );
  		if ( s.containsKey("revision") )
  			cP.setRevision( s.getData("revision").getString() );
  		if ( s.containsKey("description") )
  			cP.setDescription( s.getData("description").getString() );
  		 		
  	}else{
  		HSSFWorkbook hSSFWorkbook = (HSSFWorkbook)workbook;
  		DocumentSummaryInformation dSummary = hSSFWorkbook.getDocumentSummaryInformation();
  		
  		if ( dSummary == null ){
  			hSSFWorkbook.createInformationProperties();
  			dSummary = hSSFWorkbook.getDocumentSummaryInformation();
  		}
  		
  		if ( s.containsKey("category") )
  			dSummary.setCategory( s.getData("category").getString() );
  		if ( s.containsKey("manager") )
  			dSummary.setManager( s.getData("manager").getString() );
  		if ( s.containsKey("company") )
  			dSummary.setCompany( s.getData("company").getString() );

  		SummaryInformation sInformation = hSSFWorkbook.getSummaryInformation();
  		
  		if ( s.containsKey("title") )
  			sInformation.setTitle( s.getData("title").getString() );
  		if ( s.containsKey("subject") )
  			sInformation.setSubject( s.getData("subject").getString() );
  		if ( s.containsKey("author") )
  			sInformation.setAuthor( s.getData("author").getString() );
  		if ( s.containsKey("comments") )
  			sInformation.setComments( s.getData("comments").getString() );
  		if ( s.containsKey("keywords") )
  			sInformation.setKeywords( s.getData("keywords").getString() );
  		if ( s.containsKey("lastauthor") )
  			sInformation.setLastAuthor( s.getData("lastauthor").getString() );
  	}
  	
  	return cfBooleanData.TRUE;
  }
}
