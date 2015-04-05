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
 *  $Id: AbstractFileHandler.java 1638 2011-07-31 16:08:50Z alan $
 */


package com.bluedragon.search.index.crawl.handler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import com.bluedragon.search.DocumentWrap;


public abstract class AbstractFileHandler {
	protected	boolean bStoreBody;

	protected AbstractFileHandler( boolean bStoreBody ){
		this.bStoreBody = bStoreBody;
	}
	
	public Object getExtra(){
		return null;
	}
	
	public abstract DocumentWrap crawl(  String uriroot, File file ) throws CrawlException;
	
	protected String getUrl(String uriroot, File file) {
		if ( uriroot.endsWith("/") )
			return uriroot + file.getName();
		else
			return uriroot + "/" + file.getName();
	}

	public abstract Set<String> getExtensions();
	public abstract Set<String> getMimeTypes();

	private InputStream	fis = null;
	
	protected void openFile( File file ) throws FileNotFoundException{
		fis	= new BufferedInputStream( new FileInputStream(file) );
	}
	
	protected InputStream	getFileStream(){
		return fis;
	}
	
	protected void closeFile(){
		if ( fis != null ){
			try {
				fis.close();
			} catch (IOException e) {}
		}
	}
}
