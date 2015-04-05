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
 *  http://openbd.org/
 *  $Id: cfmlURI.java 2375 2013-06-10 22:42:09Z alan $
 */

package com.naryx.tagfusion.cfm.file;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import com.nary.io.FileUtils;

/**
 * This class enscapsulates all the addressing information for a resource
 * 
 * Note that although this implements Serializable, it may fail if request != null.
 * It will be null when we require it to be serialized when creating a BDA.
 */
public class cfmlURI implements Serializable {
	static final long serialVersionUID = 1;

	private String host, context, uri, key = null;
	private int port = 0;
	private String realFilePath = null;
	private String componentName = null; // for CFCs
	private String archiveFile = null;
	private boolean	bMapLookup = false;
	
	private cfmlURI() {
		// for making copies
	}

	// copies all fields except request
	public cfmlURI copy() {
		cfmlURI copy 				= new cfmlURI();
		copy.host 					= this.getHost(null);
		copy.context 				= this.context;
		copy.uri 						= this.uri;
		copy.port 					= this.getPort(null);
		copy.realFilePath 	= this.realFilePath;
		copy.componentName 	= this.componentName;
		copy.archiveFile 		= this.archiveFile;
		return copy;
	}

	public cfmlURI(HttpServletRequest _req, String _uri) {
		// defer looking up host (see comments on getHost() method, above)
		context 			= _req.getContextPath();
		
		// defer looking up port (see comment on getPort() method, above)
		uri 					= _uri;
		realFilePath 	= null;
		bMapLookup 		= true;
	}

	public cfmlURI(File file) {
		host = "{file}";
		context = "";
		port = 0;
		uri = file.toString();
		try {
			// Store the canonical path
			realFilePath = file.getCanonicalPath();
		} catch (IOException e) {
			// If for some reason getCanonicalPath() fails then just use toString()
			realFilePath = file.toString();
		}
	}

	public cfmlURI(String fullpath) {
		this(fullpath, false);
	}

	/**
	 * if realPath is true, then fullpath is a full physical path; if realPath is false, then we don't know if it's a full physical path or a URI path
	 */
	public cfmlURI(String fullpath, boolean realPath) {
		if ( fullpath != null && fullpath.startsWith("openbd://") ){
			initProtocolOpenBD(fullpath);
			return;
		}
		
		String directory = "", fileName = "";

		if (fullpath != null) {
			String processedFullpath = fullpath.replace('\\', '/');

			int lastSlash = processedFullpath.lastIndexOf('/');
			if (lastSlash == -1) {
				directory = "";
				fileName 	= processedFullpath;
			} else if (lastSlash == 0) {
				directory = "/";
				fileName 	= (processedFullpath.length() > 1 ? processedFullpath.substring(1) : "");
			} else {
				directory = processedFullpath.substring(0, lastSlash);
				fileName 	= processedFullpath.substring(lastSlash + 1);
			}
		}
		init(directory, fileName, realPath);
	}

	
	private void initProtocolOpenBD(String fullpath) {
		int c1			= fullpath.indexOf("@");
		archiveFile	=	fullpath.substring( 9, c1 );
		uri					= fullpath.substring( c1 + 1 );
		
		host 				= "{openbd}";
		context 		= "";
		port 				= 0;
	}
	

	public cfmlURI(String directory, String fileName) {
		init(directory, fileName, false);
	}

	/**
	 * if realPath is true, then directory is a full physical path; if realPath is false, then we don't know if it's a full physical path or a URI path
	 */
	private void init(String directory, String fileName, boolean realPath) {
		if (!realPath && (directory.length() > 0) && (directory.charAt(0) == '/') && ((directory.length() < 2) || (directory.charAt(1) != '/'))) { // make sure not Windows UNC network path

			// URL path
			uri = directory;
			if (fileName.length() > 0 && !uri.endsWith("/"))
				uri += "/";
			if (fileName.length() > 1 && fileName.charAt(0) == '/')
				fileName = fileName.substring(1);

			uri += fileName;
			realFilePath = null;

		} else {
			// Real path
			if (directory.length() > 0 && directory.charAt(0) == '$') {
				realFilePath = FileUtils.getCanonicalPath(directory.substring(1), fileName);
			} else {
				realFilePath = FileUtils.getCanonicalPath(directory, fileName);
			}

			host = "{file}";
			context = "";
			port = 0;
			uri = realFilePath;
		}
	}

	public String getRealPath(HttpServletRequest request) {
		if (realFilePath != null) {
			return realFilePath;
		}

		if ((request == null) || (uri == null)) {
			return null;
		}

		// normalize path since getRealPath() isn't guaranteed to return a path that uses the OS separator
		realFilePath = FileUtils.getCanonicalPath(FileUtils.getRealPath(request, uri));
		return realFilePath;
	}

	public boolean isArchiveFile(){
		return (archiveFile != null);
	}
	
	public boolean isRealFile() {
		return (realFilePath != null);
	}

	public String getRealPath() {
		return realFilePath;
	}

	public File getFile() {
		return new File(realFilePath);
	}
	
	public String getArchiveFile(){
		return archiveFile;
	}

	public String getURI() {
		return uri;
	}
	
	public boolean isMapSearch(){
		return bMapLookup;
	}
	
	public String getCacheKey(){
		if ( realFilePath != null )
			return "{f}" + realFilePath;
		else if ( archiveFile != null )
			return "{openbd}" + uri;
		else
			return uri;
	}

	public String getParentURI() {
		String t = uri.substring(0, uri.lastIndexOf("/"));
		return (t.length() == 0) ? "/" : t;
	}

	public String getChildURI() {
		int slashPos = uri.lastIndexOf("/");
		if ((slashPos >= 0) && slashPos < uri.length()) {
			return uri.substring(slashPos + 1);
		}else
			return "";
	}

	public String getKey(HttpServletRequest request) {
		if (key == null) {
			key = getHost(request) + ":" + getPort(request) + context + uri;
		}
		return key;
	}


	public String getComponentName() {
		return componentName;
	}

	public cfmlURI setComponentName(String _name) {
		componentName = _name;
		return this;
	}

	
	// for performance reasons we're going to defer making the call to
	// request.getServerName() until we need it; therefore, don't read
	// the "host" attribute directly, but always via the getHost() method
	private String getHost(HttpServletRequest request) {
		if ((host == null) && (request != null)) {
			host = request.getServerName();
		}
		return host;
	}
	

	// for performance reasons we're going to defer making the call to
	// request.getServerPort() until we need it; therefore, don't read
	// the "port" attribute directly, but always via the getPort() method
	private int getPort(HttpServletRequest request) {
		if ((port == 0) && (request != null)) {
			port = request.getServerPort();
		}
		return port;
	}

	
	public String toString() {
		return "\r\nhost    = " + getHost(null) 
				+ "\r\nport    = " + getPort(null) 
				+ "\r\ncontext = " + context 
				+ "\r\nuri     = " + uri 
				+ "\r\nrealfile= " + realFilePath 
				+ "\r\nacrhive = " + archiveFile
				+ "\r\ncomponentName = " + componentName;
	}
}
