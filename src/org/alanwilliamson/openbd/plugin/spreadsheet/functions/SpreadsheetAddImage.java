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
package org.alanwilliamson.openbd.plugin.spreadsheet.functions;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.alanwilliamson.openbd.plugin.spreadsheet.cfSpreadSheetData;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.ss.usermodel.Workbook;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SpreadsheetAddImage extends functionBase {
	private static final long serialVersionUID = 1L;

	public SpreadsheetAddImage(){  min = 3;  max = 3; }
  
	public String[] getParamInfo(){
		return new String[]{
			"spreadsheet object",
			"image file path",
			"anchor - either 4 numbers (startRow,startCol,endRow,endCol), or 8 numbers (startXPos,startYPos,endXPos,endYPos,startRow,startCol,endRow,endCol)"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"spreadsheet-plugin", 
				"Adds an image to the current spreadsheet object", 
				ReturnType.BOOLEAN );
	}
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
  	cfSpreadSheetData	spreadsheet	= (cfSpreadSheetData)parameters.get(2);
  	String imgPath	= parameters.get(1).getString();
  	String[] anchor	= parameters.get(0).getString().split(",");
  	
  	// Check the anchor
  	if ( anchor.length != 4 && anchor.length != 8 )
  		throwException(_session, "Invalid Anchor parameter. Must be 4 or 8 comma separated numbers" );
  	
  	
  	// Determine the file type
  	String fileExt	= imgPath.substring( imgPath.lastIndexOf(".")+1 ).toLowerCase();
  	int imgTypeIndex = 0;
  	if ( fileExt.equals("dib") ){
  		imgTypeIndex = Workbook.PICTURE_TYPE_DIB;
  	}else if ( fileExt.equals("jpg") ){
  		imgTypeIndex = Workbook.PICTURE_TYPE_JPEG;
  	}else if ( fileExt.equals("emf") ){
  		imgTypeIndex = Workbook.PICTURE_TYPE_EMF;
  	}else if ( fileExt.equals("pict") ){
  		imgTypeIndex = Workbook.PICTURE_TYPE_PICT;
  	}else if ( fileExt.equals("png") ){
  		imgTypeIndex = Workbook.PICTURE_TYPE_PNG;
  	}else if ( fileExt.equals("wmf") ){
  		imgTypeIndex = Workbook.PICTURE_TYPE_WMF;
  	}else
  		throwException(_session, "Unknown file type: " + imgPath );
  	
  	
  	// Read the file
  	FileInputStream fs = null;
  	byte[] fileBuffer = null;
  	try{
  		fs	= new FileInputStream( imgPath );
  		fileBuffer = org.apache.poi.util.IOUtils.toByteArray(fs);
  	}catch(Exception fe){
  		throwException(_session, "Unable to read file: " + imgPath );
  	}finally{
  		try {fs.close();} catch (IOException e) {}
  	}
  	
  	
  	// Add the picture
  	int imageIndex	= spreadsheet.getWorkBook().addPicture(fileBuffer, imgTypeIndex);
  	
  	HSSFClientAnchor	clientAnchor	= new HSSFClientAnchor();
  	if ( anchor.length == 4 ){
  		clientAnchor.setRow1( Integer.valueOf( anchor[0] ) - 1 );
  		clientAnchor.setCol1( Integer.valueOf( anchor[1] ) - 1 );
  		clientAnchor.setRow2( Integer.valueOf( anchor[2] ) - 1 );
  		clientAnchor.setCol2( Integer.valueOf( anchor[3] ) - 1 );
  	}else{
  		clientAnchor.setDx1( Integer.valueOf( anchor[0] ) - 1 );
  		clientAnchor.setDy1( Integer.valueOf( anchor[1] ) - 1 );
  		clientAnchor.setDx2( Integer.valueOf( anchor[2] ) - 1 );
  		clientAnchor.setDy2( Integer.valueOf( anchor[3] ) - 1 );
  		
  		clientAnchor.setRow1( Integer.valueOf( anchor[4] ) - 1 );
  		clientAnchor.setCol1( Integer.valueOf( anchor[5] ) - 1 );
  		clientAnchor.setRow2( Integer.valueOf( anchor[6] ) - 1 );
  		clientAnchor.setCol2( Integer.valueOf( anchor[7] ) - 1 );
  	}
  	
  	
  	// finalise the image to the sheet
  	spreadsheet.getActiveSheet().createDrawingPatriarch().createPicture(clientAnchor, imageIndex);
  	
  	return cfBooleanData.TRUE;
  }
}
