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
 *  
 *  $Id: ImageWrite.java 1692 2011-09-28 11:03:08Z alan $
 */


package com.naryx.tagfusion.expression.function.image;

import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.vfs.cfVFSData;

public class ImageWrite extends ImageInfo {
	private static final long serialVersionUID = 1L;

	public ImageWrite() {
		min = 1; max = 4;
		setNamedParams( new String[]{ "name", "destination", "overwrite", "quality" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"the image object derived from ImageNew() or ImageRead()",
			"the file path to where the image will be written",
			"flag to control where the destination file is overwritten",
			"if the output is a JPG then this is a quality factor between 0 and 1. Default 0.75"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Writes the given image out to file", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		cfImageData im	= getImage( _session, argStruct );
		
		// Determine where we want to save this
		String destination	= getNamedStringParam(argStruct, "destination", null);
		if ( destination == null ){
			cfData	fD	= im.getData("source");
			if ( fD == null ){
				throwException(_session, "destination has not been specified");
			}else
				destination = fD.getString();
		}

		
		// Get the outgoing image
		cfVFSData	fileObj = null;
		try {
			
			// open for reading to see if it exists
			fileObj = new cfVFSData( destination );
		
			// Overwrite flag
			if ( !getNamedBooleanParam(argStruct, "overwrite", false ) ){
				// Do not overwrite
				if ( fileObj.exists() ){
					throwException(_session, "destination already exists, and the overwrite flag has been disabled");
				}
			}else{
				// Otherwise delete this file and re-open it
				fileObj.delete();
			}
			fileObj.close();
			
			
			// now open it for writing
			fileObj = new cfVFSData( destination, "write", null );
			
			// Determine if this is a JPG
			String ext = destination.substring( destination.lastIndexOf(".")+1 ).toLowerCase();

			if ( ext.equals("jpg") || ext.equals("jpeg") ){
				float compression = 0.75f;
				
				cfData qD = getNamedParam(argStruct, "quality");
				if ( qD != null && qD.getDataType() == cfData.CFNUMBERDATA )
					compression	= (float)((cfNumberData)qD).getDouble();

				writeImageJpg( im, fileObj, compression );
			}else if ( ext.equals("png") ) {
				writeImagePng( im, fileObj );
			}else if ( ext.equals("gif") ) {
				writeImageGif( im, fileObj );
			}else{
				throwException(_session, "output image format: " + ext + " is not supported" );
			}

		} catch (Exception e) {
			throwException(_session, e.getMessage() );
		} finally {
			if ( fileObj != null ){
				try {
					fileObj.close();
				} catch (Exception e) {}
			}
		}

		return cfBooleanData.TRUE;
	}
	
	private void writeImageGif(cfImageData im, cfVFSData	fileObj) throws Exception {
		ImageIO.write( im.getImage(), "gif", fileObj.getStreamWriter() );
		fileObj.close();
	}
	
	private void writeImagePng(cfImageData im, cfVFSData	fileObj) throws Exception {
		ImageIO.write( im.getImage(), "png", fileObj.getStreamWriter() );
		fileObj.close();
	}
	
	private void writeImageJpg(cfImageData im, cfVFSData fileObj, float compression ) throws Exception {
		ImageWriteParam iwparam = new ImageWriteParam(); 
		iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ; 
		iwparam.setCompressionQuality( compression );
		iwparam.setProgressiveMode( ImageWriteParam.MODE_DEFAULT );
		
		ImageWriter writer = null; 
		Iterator iter = ImageIO.getImageWritersByFormatName("jpg"); 
		if (iter.hasNext()) { 
			writer = (ImageWriter)iter.next(); 
		} 
		
		ImageOutputStream ios = ImageIO.createImageOutputStream(fileObj.getStreamWriter()); 
		writer.setOutput(ios); 
		writer.write(null, new IIOImage( im.getImage(), null, null), iwparam); 

		ios.flush(); 
		writer.dispose(); 
		ios.close(); 
	}
	
	private static class ImageWriteParam extends JPEGImageWriteParam { 
		public ImageWriteParam() { super(Locale.getDefault()); }  
		
		public void setCompressionQuality(float quality) { 
			if (quality < 0.0F || quality > 1.0F) { 
				throw new IllegalArgumentException("Quality out-of-bounds!"); 
			} 
			this.compressionQuality = 256 - (quality * 256); 
		} 
	}
}