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
 *  
 *  $Id: FileHandlerTextImpl.java 1658 2011-09-06 10:40:00Z alan $
 */

package com.bluedragon.search.index.crawl.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.bluedragon.search.DocumentWrap;


public class FileHandlerTextImpl extends AbstractFileHandler {

	private static Set<String> extensions = new HashSet<String>( Arrays.asList("txt","text","log","java","cpp","php","inc") );
	private static Set<String> mimetypes = new HashSet<String>( Arrays.asList("text/plain") );

	public FileHandlerTextImpl(boolean bStoreBody) {
		super(bStoreBody);
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
			// Get the body from the WORD
			String bodyText = FileUtils.readFileToString(file);

      // Setup the document
	    document.setContent( bodyText, bStoreBody );
	    document.setSize( (int)file.length() );
	    document.setType( "text/plain" );
	    
    	document.setId( file.getCanonicalPath() );
    	
	    if ( uriroot != null )
	    	document.setURL( getUrl( uriroot, file ) );

		} catch (FileNotFoundException e) {
			throw new CrawlException("File not found: " + file, e);
		} catch (IOException e) {
			throw new CrawlException("File: " + file, e);
		} catch (Exception e) {
			throw new CrawlException("File: " + file, e);
		}
		
    return document;
	}
}
