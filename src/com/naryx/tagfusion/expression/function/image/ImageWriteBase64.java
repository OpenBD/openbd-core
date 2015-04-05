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
 *  $Id: ImageWriteBase64.java 1820 2011-11-24 07:14:19Z alan $
 */


package com.naryx.tagfusion.expression.function.image;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.aw20.io.ByteArrayOutputStreamRaw;

import com.nary.net.Base64;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.vfs.cfVFSData;

public class ImageWriteBase64 extends ImageInfo {
	private static final long serialVersionUID = 1L;

	public ImageWriteBase64() {
		min = 3; max = 6;
		setNamedParams( new String[]{ "name", "destination", "format", "inhtmlformat", "overwrite", "quality" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"the image object derived from ImageNew() or ImageRead()",
			"the file path to where the image will be written; optional",
			"flag to control where the destination file is overwritten if using the file",
			"the format of the output; gif, png, jpg",
			"whether or not to include the HTML format data for using inline with HTML img tags",
			"quality of the image if its a jpg; default 0.75"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Converts the image to Base64 before writing", 
				ReturnType.STRING );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		cfImageData im	= getImage( _session, argStruct );
		
		// Determine where we want to save this
		String destination	= getNamedStringParam(argStruct, "destination", null);
		if ( destination != null && destination.length() == 0 ){
			destination = null;
		}

	
		// Get the blob
		try{
			String format = getNamedStringParam(argStruct, "format", "jpg");
			ByteArrayOutputStreamRaw	baos = new ByteArrayOutputStreamRaw(64000);
			if ( format.equals("jpg") || format.equals("jpeg") ){
				float compression = 0.75f;
				
				cfData qD = getNamedParam(argStruct, "quality");
				if ( qD != null && qD.getDataType() == cfData.CFNUMBERDATA )
					compression	= (float)((cfNumberData)qD).getDouble();
	
				writeImageJpg( im, baos, compression );
			}else if ( format.equals("png") ) {
				writeImagePng( im, baos );
			}else if ( format.equals("gif") ) {
				writeImageGif( im, baos );
			}else{
				throwException(_session, "output image format: " + format + " is not supported" );
			}
			
			
			// Get the base64 string
			String base64	= new String( Base64.base64Encode(baos.getByteArray()));
		
			if ( getNamedBooleanParam(argStruct, "inhtmlformat", false ) ){
				base64 = "data:image/" + format + ";base64," + base64;
			}
			
			
			if ( destination != null ){
				writeFile( _session, destination, base64, getNamedBooleanParam(argStruct, "overwrite", false )  );
			}
			
		
			return new cfStringData( base64 );
		}catch(Exception e){
			throwException(_session, e.getMessage() );
		}
		

		return cfBooleanData.TRUE;
	}
	
	private void writeFile(cfSession _session, String destination, String base64, boolean overrite)  throws cfmRunTimeException {
		// Get the outgoing image
		cfVFSData	fileObj = null;
		try {
			
			// open for reading to see if it exists
			fileObj = new cfVFSData( destination );
		
			// Overwrite flag
			if ( !overrite ){
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
			fileObj.write( base64 );

		} catch (Exception e) {
			throwException(_session, e.getMessage() );
		} finally {
			if ( fileObj != null ){
				try {
					fileObj.close();
				} catch (Exception e) {}
			}
		}
		
	}

	private void writeImageGif(cfImageData im, OutputStream out ) throws Exception {
		ImageIO.write( im.getImage(), "gif", out );
	}
	
	private void writeImagePng(cfImageData im, OutputStream out) throws Exception {
		ImageIO.write( im.getImage(), "png", out );
	}
	
	private void writeImageJpg(cfImageData im, OutputStream out, float compression ) throws Exception {
		ImageWriteParam iwparam = new ImageWriteParam(); 
		iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ; 
		iwparam.setCompressionQuality( compression );
		iwparam.setProgressiveMode( ImageWriteParam.MODE_DEFAULT );
		
		ImageWriter writer = null; 
		Iterator iter = ImageIO.getImageWritersByFormatName("jpg"); 
		if (iter.hasNext()) { 
			writer = (ImageWriter)iter.next(); 
		} 
		
		ImageOutputStream ios = ImageIO.createImageOutputStream(out); 
		writer.setOutput(ios); 
		writer.write(null, new IIOImage( im.getImage(), null, null), iwparam); 
		writer.dispose(); 
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