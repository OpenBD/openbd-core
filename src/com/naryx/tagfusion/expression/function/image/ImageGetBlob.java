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
 *  http://www.openbluedragon.org/
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

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class ImageGetBlob extends ImageInfo {
	private static final long serialVersionUID = 1L;

	public ImageGetBlob() {
		min = 1; max = 2;
		setNamedParams( new String[]{ "name", "format" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"the image object derived from ImageNew() or ImageRead()",
			"the format of the binary blob. Values, png, jpg or gif.   Defaults to png"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Returns binary version of this image", 
				ReturnType.BINARY );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		cfImageData im	= getImage( _session, argStruct );
		String format	= getNamedStringParam(argStruct, "format", "png");
		
		ByteArrayOutputStreamRaw	baos = new ByteArrayOutputStreamRaw(64000);
		try {
			if ( format.equalsIgnoreCase("gif") ){
				ImageIO.write( im.getImage(), "gif", baos );
			}else if( format.equalsIgnoreCase("jpg") ){
				writeImageJpg( im, baos, 0.75f );
			}else{
				ImageIO.write(im.getImage(), "png", baos );
			}
			
		} catch (Exception e) {
			throwException(_session, "failed to get image blob: " + e.getMessage() );
		}
		
		return new cfBinaryData( baos.getByteArray() );
	}
	
	private void writeImageJpg(cfImageData im, OutputStream fileObj, float compression ) throws Exception {
		ImageWriteParam iwparam = new ImageWriteParam(); 
		iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ; 
		iwparam.setCompressionQuality( compression );
		iwparam.setProgressiveMode( ImageWriteParam.MODE_DEFAULT );
		
		ImageWriter writer = null; 
		Iterator iter = ImageIO.getImageWritersByFormatName("jpg"); 
		if (iter.hasNext()) { 
			writer = (ImageWriter)iter.next(); 
		} 
		
		ImageOutputStream ios = ImageIO.createImageOutputStream(fileObj); 
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