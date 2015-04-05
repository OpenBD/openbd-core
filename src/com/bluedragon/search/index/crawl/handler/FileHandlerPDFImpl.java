/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: FileHandlerPDFImpl.java 2020 2012-04-26 01:06:12Z alan $
 */

package com.bluedragon.search.index.crawl.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import com.bluedragon.search.DocumentWrap;


public class FileHandlerPDFImpl extends AbstractFileHandler {

	private static Set<String> extensions = new HashSet<String>( Arrays.asList("pdf") );
	private static Set<String> mimetypes = new HashSet<String>( Arrays.asList("application/pdf") );

	public FileHandlerPDFImpl(boolean bStoreBody) {
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
			openFile(file);
			
			ContentHandler 	textHandler 	= new BodyContentHandler();
			Metadata 				metadata 			= new Metadata();
			ParseContext		parseContext	= new ParseContext();
			
			Parser parser = new PDFParser();
			parser.parse( getFileStream(), textHandler, metadata, parseContext);

			
      // Setup the document
			document.setContent( textHandler.toString(), bStoreBody );
	    document.setSize( (int)file.length() );
	    document.setType( "application/pdf" );

      document.setAuthor( metadata.get( Metadata.AUTHOR ) );
      document.setName( metadata.get( Metadata.TITLE ) );
      document.setSummary( metadata.get( Metadata.SUBJECT ) );
      document.setAttribute( "keywords", metadata.get(Metadata.KEYWORDS) );
	    
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
