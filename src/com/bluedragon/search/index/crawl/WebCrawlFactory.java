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
 *  $Id: WebCrawlFactory.java 1638 2011-07-31 16:08:50Z alan $
 */

package com.bluedragon.search.index.crawl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.bluedragon.search.DocumentWrap;
import com.bluedragon.search.index.DocumentWriter;
import com.bluedragon.search.index.crawl.handler.AbstractFileHandler;
import com.bluedragon.search.index.crawl.handler.FileHandlerHTMLImpl;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;


public class WebCrawlFactory extends CrawlFactory {
	public static final int	MAX_DOCS	= 100;
	
	private DocumentWriter docWriter;
	private String categoryTree;
	private String[] category;
	private cfStructData custommap;
	private String originalHost;
	
	private cfArrayData	badUrls;
	private Set<String>	visitedUrls;
	private List<URL>	urlsToCrawl;
	private AbstractFileHandler	activeHandler;
	private int	totalDocs = 0;
	
	public WebCrawlFactory(boolean bStoreBody, URL weburl, DocumentWriter docWriter, String categoryTree, String[] category, cfStructData custommap ) {
		super(bStoreBody);

		originalHost			= weburl.getHost();
		this.docWriter		= docWriter;
		this.categoryTree = categoryTree;
		this.category			= category;
		this.custommap		= custommap;
		
		badUrls			= cfArrayData.createArray(1);
		visitedUrls	= new HashSet<String>();
		urlsToCrawl	= new ArrayList<URL>();
		
		urlsToCrawl.add( weburl );
	}

	
	public void close() {
		try {
			docWriter.commit();
		} catch (Exception e) {}
	}
	
	
	public cfData crawl() {
		
		while ( !urlsToCrawl.isEmpty() ){
			if ( totalDocs+badUrls.size() == MAX_DOCS )
				break;
			
			URL toCrawlUrl	= urlsToCrawl.remove(0);

			File	pageFile	= downloadPage( toCrawlUrl );
			if ( pageFile == null )
				continue;

			try{
				if ( !(activeHandler instanceof FileHandlerHTMLImpl) )
					continue;

				DocumentWrap	doc	= activeHandler.crawl( toCrawlUrl.toString(), pageFile );
				
				doc.setCategories(category);
				doc.setCategoryTree(categoryTree);
				
				doc.deleteField( DocumentWrap.URL );
				doc.deleteField( DocumentWrap.ID );
				doc.setId( toCrawlUrl.toString() );
				
				if ( custommap != null ){
					Iterator<String> it = custommap.keySet().iterator();
					while ( it.hasNext() ){
						String k = it.next();
						String v = custommap.getData(k).getString();
						doc.setAttribute(k, v);
					}
				}
				
				// Add this to our collection
				totalDocs++;
				docWriter.add(doc);
				
				// Catch any other url's; only add if inside same domain and we haven't seen them before
				Set<String>	anchors = (Set<String>)activeHandler.getExtra();
				if ( anchors != null ){
					Iterator<String>	is = anchors.iterator();
					while ( is.hasNext() ){
						String urlS = is.next();
						if ( urlS.length() > 0 && !visitedUrls.contains(urlS) ){
							URL url = new URL(is.next());
							if ( url.getHost().equalsIgnoreCase(originalHost) )
								urlsToCrawl.add( url );
						}
					}
				}

			}catch(Exception e){
				
			}finally{
				activeHandler	= null;
				pageFile.delete();
			}
			
		}

		// Set the status
		cfStructData sd = new cfStructData();
		sd.setData("inserted", new cfNumberData(totalDocs) );
		sd.setData("invalid", new cfNumberData(badUrls.size()) );
		sd.setData("badkeys", badUrls );
		return sd;
	}

	
	private File	downloadPage( URL url ){
		if ( visitedUrls.contains(url.toString()) )
			return null;
		
		activeHandler = null;
		visitedUrls.add( url.toString() );
		
		File tmpFile = null;
		
		long	startTime = System.currentTimeMillis();
		int size = 0;
		
		try{
			tmpFile = File.createTempFile("OpenBD-CollectionIndexWeb-", ".html");
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestMethod("GET");
			conn.setReadTimeout( 30000 );
			
		   // Connect and read the response
	    InputStream in = null;
	    String detectedContentType = null;
	    try {
	      conn.connect();
	      
	      if (conn.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN || conn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
	        throw new Exception("Skipping url: " + url + ". It is protected.");
	      
	      if (conn.getResponseCode() != -1 && conn.getResponseCode() != HttpURLConnection.HTTP_OK)
	        throw new Exception("Cannot read page: " + url + " response code is: " + conn.getResponseCode());
	      
	      in = conn.getInputStream();
	      detectedContentType = URLConnection.guessContentTypeFromStream(in);
	      size = readData( tmpFile, in );
	    } finally {
	      if (in != null)
	        in.close();
	      
	      conn.disconnect();
	    }

	    // Log the fact we made a crawl
	    cfEngine.log( "CollectionIndexWeb: Time=" + (System.currentTimeMillis()-startTime) + "; Size=" + size + "; " + url );
	    
	    // Determine the contentType
	    String specifiedContentType = conn.getContentType();
	    if (specifiedContentType == null || specifiedContentType.equals(""))
	      specifiedContentType = detectedContentType;
	    if (specifiedContentType == null || specifiedContentType.equals(""))
	      specifiedContentType = "text/plain";

	    specifiedContentType = validateMimeType( specifiedContentType ); 
	    
	    activeHandler	= extHandlers.get( specifiedContentType );
	    if ( activeHandler == null ){
	    	badUrls.addElement( new cfStringData( url.toString() ) );
	    	tmpFile.delete();
	    	return null;
	    }else
	    	return tmpFile;
	    
		}catch(Exception e){
			try {
				badUrls.addElement( new cfStringData( url.toString() ) );
			} catch (cfmRunTimeException e1) {}
			
			if ( tmpFile != null ) 
				tmpFile.delete();
			
			return null;
		}
	}
	
	
	
	private String validateMimeType(String mt) {
		// The Java VM returns null when it can't determine the content-type so use the default mime type.
		if (mt == null)
			return "text/plain";

		// Remove any subtypes from the content-type
		if (mt.indexOf(";") != -1)
			mt = mt.substring(0, mt.indexOf(";")).trim();
		
		return mt;
	}
	
	
	
	private int readData(File tmpFile, InputStream in) throws IOException {
		OutputStream out = null;
		int size = 0;

		try {
			out = new BufferedOutputStream(cfEngine.thisPlatform.getFileIO().getFileOutputStream(tmpFile));
			int read = -1;
			
			byte[] buf = new byte[1024];
			while ((read = in.read(buf, 0, buf.length)) != -1) {
				out.write(buf, 0, read);
				size += read;
			}
			out.flush();
		} finally {
			if (out != null)
				out.close();
		}
		
		return size;
	}

}
