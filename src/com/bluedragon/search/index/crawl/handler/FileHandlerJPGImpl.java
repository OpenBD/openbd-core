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
 *  http://openbd..org/
 *  
 *  $Id: FileHandlerJPGImpl.java 2404 2013-09-22 21:51:40Z alan $
 */

package com.bluedragon.search.index.crawl.handler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.bluedragon.search.DocumentWrap;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;


public class FileHandlerJPGImpl extends AbstractFileHandler {

	private static Set<String> extensions = new HashSet<String>( Arrays.asList("jpg","jpeg","tiff") );
	private static Set<String> mimetypes = new HashSet<String>( Arrays.asList("image/jpg","image/jpeg","image/tiff") );

	public FileHandlerJPGImpl(boolean bStoreBody) {
		super(false);
	}

	public Set<String> getExtensions() {
		return extensions;
	}

	public Set<String> getMimeTypes() {
		return mimetypes;
	}
	
	public DocumentWrap crawl(String uriroot, File file) throws CrawlException {
		DocumentWrap	document	= new DocumentWrap();
		
		try{
			openFile(file);
			
			Metadata metadata = ImageMetadataReader.readMetadata( new BufferedInputStream(getFileStream()), true );
			
			Iterator<Directory> directories = metadata.getDirectories().iterator();
			while (directories.hasNext()) {
		    Directory directory = (Directory)directories.next();
		    Iterator<Tag> tags = directory.getTags().iterator();
		    
		    while (tags.hasNext()) {
		    	Tag tag = (Tag)tags.next();
		    	
		    	if ( tag.getTagName().toLowerCase().startsWith("unknown_") )
		    		continue;

		    	document.setAttribute( tag.getTagName().replace(' ', '_'), tag.getDescription() );
		    }
			}
			
			// Setup the document
	    document.setSize( (int)file.length() );
	    document.setType( "image/jpg" );
    	document.setId( file.getCanonicalPath() );

	    if ( uriroot != null )
	    	document.setURL( getUrl( uriroot, file ) );

		} catch (FileNotFoundException e) {
			throw new CrawlException("File not found: " + file, e);
		} catch (IOException e) {
			throw new CrawlException("File: " + file, e);
		} catch (Exception e) {
			throw new CrawlException("File: " + file, e);
		} finally {
			closeFile();
		}
		
    return document;
	}
}