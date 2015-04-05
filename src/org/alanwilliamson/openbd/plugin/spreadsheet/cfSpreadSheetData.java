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
package org.alanwilliamson.openbd.plugin.spreadsheet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

public class cfSpreadSheetData extends cfData implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	private	Workbook 	workbook;
	private String		activeSheetName = null;
	private Sheet			activeSheetObj = null;
	
	public cfSpreadSheetData( File inputFile ) throws IOException {
		FileInputStream in = new FileInputStream( inputFile );
		
		try{
			if ( inputFile.getName().toLowerCase().endsWith(".xls") ){
				workbook	= new HSSFWorkbook( in );
			}else if ( inputFile.getName().toLowerCase().endsWith(".xlsx") ){
				workbook	= new XSSFWorkbook( in );
			}else
				throw new IOException( "file format not supported (.xls/.xlsx only)" );
		}finally{
			if ( in != null ){
				try{
					in.close();
				}catch(Exception e){}
			}
		}
		
		activeSheetObj	= workbook.getSheetAt( 0 );
		activeSheetName	= activeSheetObj.getSheetName();
	}
	
	public cfSpreadSheetData( boolean bXLSX, String _activeSheet ){
		workbook 		= bXLSX ? new XSSFWorkbook() : new HSSFWorkbook();
		activeSheetName	= _activeSheet;

		activeSheetObj = workbook.createSheet(activeSheetName);
		activeSheetObj.setPrintGridlines( true );
		activeSheetObj.setDisplayGridlines( true );
    workbook.setActiveSheet( 0 );
    workbook.setSelectedTab( 0 );
	}
	
	public Workbook	getWorkBook(){
		return workbook;
	}
	
	public Sheet getActiveSheet(){
		return activeSheetObj;
	}

	public String getActiveSheetName(){
		return activeSheetName;
	}

	public void deleteSheet( String sheetName ){
		if ( workbook.getNumberOfSheets() == 1 )
			return;
		
		Sheet sheet	= workbook.getSheet( sheetName );
		if ( sheet != null ){
			workbook.removeSheetAt( workbook.getSheetIndex(sheet) );
			activeSheetObj = workbook.getSheetAt(0);
			if ( activeSheetObj != null ){
				activeSheetName	= activeSheetObj.getSheetName();
			}
		}
	}
	
	public void setActiveSheet( int sheetNo ){
		Sheet sheet	= workbook.getSheetAt( sheetNo );
		if ( sheet != null ){
			workbook.setActiveSheet( sheetNo );
			workbook.setSelectedTab( sheetNo );
			activeSheetName	= sheet.getSheetName();
			activeSheetObj	= sheet;
		}
	}


	public void setActiveSheet( String sheetName ){
		Sheet sheet	= workbook.getSheet( sheetName );
		if ( sheet != null ){
			workbook.setActiveSheet( workbook.getSheetIndex(sheet) );
			workbook.setSelectedTab( workbook.getSheetIndex(sheet) );
			activeSheetName	= sheetName;
			activeSheetObj	= sheet;
		}
	}
	
	
	/*
	 * Writes the current workbook out to the file system
	 */
	public void write( File destinationFile, String password ) throws IOException {
		if ( password != null && password.length() > 0 && workbook instanceof HSSFWorkbook ){
			((HSSFWorkbook)workbook).writeProtectWorkbook(password, "");
		}

		FileOutputStream out = null;
		try{
			out = new FileOutputStream(destinationFile);
			workbook.write(out);
		}finally{
			if ( out != null ){
				try{out.close();}catch(IOException ioe){}
			}
		}
	}
	
	
	/*
	 * Writes the spreadsheet object to a byte array
	 */
	public byte[] write() throws IOException {
		ByteArrayOutputStream	out	= new ByteArrayOutputStream( 32000 );
		workbook.write(out);
		return out.toByteArray();
	}
	
	
	/* --------------------------------------------
	 * Methods from the cfData class
	 */
	public byte 	getDataType(){ return CFJAVAOBJECTDATA; }
	public String	getDataTypeName() { return "spreadsheet"; }
    
	public String getString() throws dataNotSupportedException {
		throw new dataNotSupportedException( "Cannot convert spreadsheet data to string" );
	}

	public void dump( java.io.PrintWriter out ){
    dump( out, "", -1 );
  }

	public void dump( java.io.PrintWriter out, String _label, int _top ){
		out.write( "<table class='cfdump_table_struct'><tr><th class='cfdump_th_struct' colspan='2'>" );
		if ( _label.length() > 0 ) out.write( _label + " - " );
		out.write( "spreadsheet</th></tr>" );
		
		out.write( "<tr><td class='cfdump_td_struct'>XLSX</td><td class='cfdump_td_value'>" );
		out.write( String.valueOf( workbook instanceof XSSFWorkbook ) );
		out.write( "</td></tr>" );
		out.write( "<tr><td class='cfdump_td_struct'>Active Sheet</td><td class='cfdump_td_value'>" );
		out.write( activeSheetName );
		out.write( "</td></tr>" );
		
		out.write( "</table>" );
	}
	
	public boolean equals( Object o ){
		if ( o instanceof cfSpreadSheetData ){
			return true;
		}else
			return false;
	}

}