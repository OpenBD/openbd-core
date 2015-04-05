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
 *  $Id: CrawlFactory.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.bluedragon.search.index.crawl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bluedragon.search.DocumentWrap;
import com.bluedragon.search.index.crawl.handler.AbstractFileHandler;
import com.bluedragon.search.index.crawl.handler.CrawlException;
import com.bluedragon.search.index.crawl.handler.FileHandlerHTMLImpl;
import com.bluedragon.search.index.crawl.handler.FileHandlerJPGImpl;
import com.bluedragon.search.index.crawl.handler.FileHandlerMP3Impl;
import com.bluedragon.search.index.crawl.handler.FileHandlerMSOfficeImpl;
import com.bluedragon.search.index.crawl.handler.FileHandlerOpenOfficeImpl;
import com.bluedragon.search.index.crawl.handler.FileHandlerPDFImpl;
import com.bluedragon.search.index.crawl.handler.FileHandlerTextImpl;
import com.nary.util.string;


public class CrawlFactory extends Object {

	protected Map<String, AbstractFileHandler>	extHandlers;
	
	public CrawlFactory(boolean bStoreBody){
		extHandlers	= new HashMap<String, AbstractFileHandler>();
		
		addHandler( new FileHandlerMSOfficeImpl(bStoreBody) );
		addHandler( new FileHandlerOpenOfficeImpl(bStoreBody) );
		addHandler( new FileHandlerTextImpl(bStoreBody) );
		addHandler( new FileHandlerPDFImpl(bStoreBody) );
		addHandler( new FileHandlerHTMLImpl(bStoreBody) );
		addHandler( new FileHandlerMP3Impl(false) );
		addHandler( new FileHandlerJPGImpl(false) );
	}
	
	
	private void addHandler(AbstractFileHandler	fH){
		Iterator<String> it	= fH.getExtensions().iterator();
		while ( it.hasNext() )
			extHandlers.put( it.next(), fH);

		it	= fH.getMimeTypes().iterator();
		while ( it.hasNext() )
			extHandlers.put( it.next(), fH);
	}

	/**
	 * Crawls the file given.  If the file cannot be handled then a null is returned 
	 * 
	 * @param urlroot
	 * @param file
	 * @return
	 * @throws CrawlException
	 */
	public DocumentWrap	crawlFile( String urlroot, File file ) {
		if ( !file.exists() || !file.isFile() )
			return null;
		
		String ext	= org.apache.commons.io.FilenameUtils.getExtension( file.getName().toLowerCase() );
		
		if ( extHandlers.containsKey(ext) ){
			try {
				return extHandlers.get(ext).crawl( urlroot, file);
			} catch (CrawlException e) {}
		}
		
		return null;
	}
	

	/**
	 * Gets the list of files to crawl
	 * 
	 * @param dir
	 * @param exts
	 * @param bRecurse
	 * @return
	 * @throws IOException
	 */
	public Set<String>	getFilesToCrawl(File dir, String exts, boolean bRecurse ) throws IOException {
		ConfigurableFileFilter	filter	= new ConfigurableFileFilter( getExtensions(exts), bRecurse );
		return recursePath( dir, filter );
	}
	
	
	private Set<String> recursePath(File dir, FileFilter filter) throws IOException {
		Set<String> set = new HashSet<String>();
		File[] files = dir.listFiles(filter);
		
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory())
				set.addAll(recursePath(files[i], filter));
			else
				set.add(files[i].getCanonicalPath());
		}

		return set;
	}

	
	private Set<String> getExtensions(String exts) {
		Set<String> extensions = new HashSet<String>();
		
		if ( exts != null ) {
			List<String> tokens = string.split( exts, " ,:;");
			String token = null;
			for (int i = 0; i < tokens.size(); i++) {
				token = tokens.get(i).toLowerCase();
				if (token.indexOf(".") == -1)
					token = "." + token;
				extensions.add(token);
			}
		} else {
			extensions.add(".htm");
			extensions.add(".html");
			extensions.add(".cfm");
			extensions.add(".cfml");
			extensions.add(".dbm");
			extensions.add(".dbml");
		}
		return extensions;
	}
	
	
	public void close() {}
}
