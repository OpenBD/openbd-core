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
 *  $Id: ImageRead.java 2312 2013-01-26 12:03:14Z alan $
 */

package com.naryx.tagfusion.expression.function.image;

import java.io.InputStream;

import javax.imageio.ImageIO;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.vfs.cfVFSData;
import com.naryx.tagfusion.expression.function.functionBase;

public class ImageRead extends functionBase {
	private static final long serialVersionUID = 1L;

	public ImageRead() {
		min = max = 1;
		setNamedParams( new String[]{ "src" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"Path to the image to load (can be a url, file path or memory path)"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Loads an image", 
				ReturnType.IMAGE );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
  	String src	= getNamedStringParam(argStruct, "src", null );
		return loadImage( _session, src );
	}
	
	
	
	protected cfImageData	loadImage( cfSession _session, String src ) throws cfmRunTimeException{
		cfVFSData	fileObj = null;
		
		try {
			cfImageData	im	= new cfImageData();
			
			if ( src.startsWith("http") ){
				readImgFromUrl( src, im );
			}else{
				fileObj	= new cfVFSData( src, "readbinary", null );	
				im.setImage( ImageIO.read(fileObj.getStreamReader()) );
				im.setSize( fileObj.size() );
			}

			im.setSrc(src);
			
			return im;
		} catch (Exception e) {
			throwException(_session, e.getMessage() );
			return null;
		} finally {
			if ( fileObj != null ){ try {	fileObj.close();} catch (Exception e) {} }
		}
	}

	
	
	private void readImgFromUrl(String src, cfImageData im) throws Exception {
		org.aw20.net.HttpResult res = org.aw20.net.HttpGet.doGet(src);

		if ( res.getResponseCode() != 200 )
			throw new Exception( res.getResponseCode() + " " + res.getResponseMessage() + "; " + src );
		
		InputStream	is	= new java.io.ByteArrayInputStream( res.getBody() );
		im.setImage( ImageIO.read(is) );
		im.setSize( res.getContentLength() );
	}
}