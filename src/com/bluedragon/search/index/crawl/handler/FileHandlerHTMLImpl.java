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
 *  $Id: FileHandlerHTMLImpl.java 1638 2011-07-31 16:08:50Z alan $
 */

package com.bluedragon.search.index.crawl.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bluedragon.search.DocumentWrap;


public class FileHandlerHTMLImpl extends AbstractFileHandler {

	private static Set<String> extensions = new HashSet<String>( Arrays.asList("htm","html","cfm","cfml","cfc","asp","aspx","php","jsp","jspx") );
	private static Set<String> mimetypes = new HashSet<String>( Arrays.asList("text/html") );

	private Set<String>	anchors;
	
	public FileHandlerHTMLImpl(boolean bStoreBody) {
		super(bStoreBody);
		
		anchors	= new HashSet<String>();
	}

	public Set<String> getExtensions() {
		return extensions;
	}

	public Set<String> getMimeTypes() {
		return mimetypes;
	}

	public Object getExtra(){
		return anchors;
	}
	
	
	public DocumentWrap crawl(String uriroot, File file) throws CrawlException {
		DocumentWrap	document	= new DocumentWrap();
		
		try{
			// Get the body from the WORD
			String htmlBody = FileUtils.readFileToString(file);

			Document doc = Jsoup.parse(htmlBody);
			
			if ( uriroot != null )
				doc.setBaseUri(uriroot);
			
			setAnchors(doc, uriroot);
			
			// Setup the document
	    document.setContent( doc.text(), bStoreBody );
	    document.setSize( (int)file.length() );
	    document.setType( "text/html" );
	    
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
	
	
	/**
	 * Runs around all the internal links and pulls out all the URLs
	 * @param doc
	 * @param baseUri
	 */
	private void setAnchors( Document doc, String baseUri ){
		Elements links = doc.select("a[href]");
		for (Element link : links) {
			if ( baseUri != null )
				link.setBaseUri(baseUri);
			
			String newLink = link.attr("abs:href");
			if ( newLink.indexOf("#") != -1 )
				newLink	= newLink.substring( 0, newLink.indexOf("#") );

			anchors.add( newLink );
		}
	}
	
}
