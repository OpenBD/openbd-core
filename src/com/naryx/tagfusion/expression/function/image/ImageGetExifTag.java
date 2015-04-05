/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  $Id: ImageGetExifTag.java 2404 2013-09-22 21:51:40Z alan $
 */

package com.naryx.tagfusion.expression.function.image;

import java.util.Iterator;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.vfs.cfVFSData;

public class ImageGetExifTag extends ImageGetExifMetaData {
	private static final long serialVersionUID = 1L;

	public ImageGetExifTag() {
		min = 2; max = 2;
		setNamedParams( new String[]{ "name", "tag" } );
	}

	public String[] getParamInfo() {
		return new String[] { "the image object", "the name of the tag to retrieve" };
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"image", 
				"Reads the given EXIF/IPCT tag information for a given file.  If the image does not support it, an exception will be thrown", 
				ReturnType.STRING );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		cfImageData im	= getImage( _session, argStruct );
		
		String tag	= getNamedStringParam(argStruct, "tag", null );
		if ( tag == null )
			throwException(_session, "missing tag parameter");
		
		cfStructData	cfMetaData	= im.getMetaData();
		if ( cfMetaData == null ){
			loadMetaData(_session, im);
			cfMetaData	= im.getMetaData();
		}
		
		// We need to load it
		if ( cfMetaData.containsKey( tag ) )
			return cfMetaData.getData(tag);
		else
			return cfStringData.EMPTY_STRING;
	}
	
	protected cfStructData loadMetaData(cfSession _session, cfImageData im) throws cfmRunTimeException {
		String src	= im.getSrc();
		if ( src == null )
			throwException(_session,"this image does not support EXIF metadata");
		
		cfStructData cfMetaData	= new cfStructData();
		
		cfVFSData fileObj = null;
		try {
			fileObj = new cfVFSData( src, "readbinary", null );
			Metadata metadata = ImageMetadataReader.readMetadata(fileObj.getStreamReader(), true);
			
			Iterator<Directory> directories = metadata.getDirectories().iterator();
			while (directories.hasNext()) {
		    Directory directory = (Directory)directories.next();
		    Iterator<Tag> tags = directory.getTags().iterator();
		    
		    while (tags.hasNext()) {
		    	Tag tag = (Tag)tags.next();
		    	cfMetaData.setData( tag.getTagName().replace(' ', '_'), new cfStringData( tag.getDescription()) );
		    }
			}

			im.setMetaData( cfMetaData );
			
		} catch (Exception e) {
			throwException(_session, e.getMessage() );
		} finally {
			if ( fileObj != null ){ try {	fileObj.close();} catch (Exception e) {} }
		}
		
		return cfMetaData;
	}
}