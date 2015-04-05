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
 *  $Id: cfResourceServlet.java 2122 2012-06-22 10:59:34Z alan $
 */
package com.naryx.tagfusion.cfm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;

public class cfResourceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static ServletContext thisServletContext;
	
	private static HashMap mimeTypes;
	
	public void init(ServletConfig config) throws ServletException{
		super.init(config);
		thisServletContext = config.getServletContext();
		
		mimeTypes	= new HashMap();
		mimeTypes.put( "txt", 	"text/plain" );
		mimeTypes.put( "html", 	"text/html" );
		mimeTypes.put( "htm", 	"text/html" );
		mimeTypes.put( "css", 	"text/css" );
		mimeTypes.put( "gif", 	"image/gif" );
		mimeTypes.put( "jpg", 	"image/jpg" );
		mimeTypes.put( "jpeg", 	"image/jpg" );
		mimeTypes.put( "png", 	"image/png" );
		mimeTypes.put( "swf", 	"application/x-shockwave-flash" );
		mimeTypes.put( "js", 		"application/x-javascript" );
	}

	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if ( request.getServletPath().indexOf("cfmlbug.cfres") != -1 ){
			onCfmlBugRequest( request, response );
			return;
		}


		/*
		 * We handle the following types; 
		 * f= is the file within the resources, 
		 * t= is for temporary files we create
		 * js= is for the cfjavascript
		 * css= is for the cfstylesheet
		 */
		String fileReq = null, prefix = "";
		boolean bRequestForTempFile = false;
		
		if ( (fileReq = request.getParameter("f")) != null ){
			prefix = cfEngine.getWebResourcePath();
			
			if ( !fileReq.startsWith("/") )
				fileReq	= "/" + fileReq;
			
			if ( request.getServletPath().indexOf("cfmlbug-static") > 0 )
				fileReq	= "/cfmlbug" + fileReq;
			
		}else if ( (fileReq = request.getParameter("js")) != null ){
			
			bRequestForTempFile = true;
			fileReq = File.separator + "cfjavascript" + File.separator + fileReq;
			
		}else if ( (fileReq = request.getParameter("css")) != null ){
			
			bRequestForTempFile = true;
			fileReq = File.separator + "cfstylesheet" + File.separator + fileReq;
			
		}else if ( (fileReq = request.getParameter("t")) != null ){
			
			bRequestForTempFile = true;
			fileReq = File.separator + fileReq;
			
		}
		
		
		/* Make sure we have a URL, and also make sure no one is being naughty trying to serve content from within the root WEB-INF */
		if ( fileReq == null || fileReq.indexOf("..") != -1 ){
			response.setStatus(404);
			return;
		}
		
		setMimeType( fileReq, response );

		/* Primary Client Side Caching using ETAG */
		String serverETag = getHash( fileReq );
		String clientETag = request.getHeader("If-None-Match");
		if ( clientETag != null && clientETag.equals(serverETag) ){
			response.setStatus( 304 );
			return;
		}else{
			response.setHeader( "Etag", serverETag );	
		}

		/* Make sure we have it */
		File fileToServe;
		if ( bRequestForTempFile ){
			fileToServe = new File( cfEngine.thisPlatform.getFileIO().getTempDirectory(), fileReq );
		}else if ( prefix.length() != 0 ){
			fileToServe = new File( prefix, fileReq );
		}else{
			fileToServe = new File( FileUtils.getRealPath( prefix + fileReq ) );	
		}
		
		if ( !fileToServe.exists() ){
			response.setStatus( 404 );
			return;
		}

		/* Set some expires headers; this is content once delivered we don't want to keep giving out */
		response.setHeader( "Expires", "Sun, 9 Dec 2038 09:00:00 GMT" );
		response.setHeader( "Cache-Control", "max-age=311040000" );
		sendFile( fileToServe, response );
	}
	
	private void sendFile( File fileToServe, HttpServletResponse response) throws IOException {
		/* Read in the resource we want to consume */
		response.setStatus(200);
		response.setContentLength( (int)fileToServe.length() );

		InputStream inFile = null;
		BufferedInputStream in = null;
		try{
			inFile 	= new FileInputStream( fileToServe );
			in 			= new BufferedInputStream( inFile );

			OutputStream	out = response.getOutputStream();
			
			byte[] buf = new byte[ 128000 ];
			int c;
			while ( (c=in.read(buf,0,128000)) != -1 )
				out.write( buf, 0, c );

			out.flush();

		}catch(Exception e){
			e.printStackTrace();
			response.setStatus(404);
		}finally{
			if ( inFile != null ){
				inFile.close();
			}
			
			if ( in != null ){
				in.close();
			}
		}
	}
	
	
	private String getHash( String t ){
		int k = t.hashCode();
		return ( k < 0 ) ? String.valueOf( k * -1 ) : String.valueOf(k);
	}
	
	
	private void setMimeType( String file, HttpServletResponse response) throws IOException {
		int c = file.lastIndexOf(".");
		if ( c == -1 ){
			response.setContentType( "application/octet-stream" );
			return;
		}

		String mimeType = (String)mimeTypes.get( file.substring(c+1).toLowerCase() );
		if ( mimeType == null ){
			response.setContentType( "application/octet-stream" );
			return;
		}else{
			response.setContentType( mimeType );
			return;
		}
	}
	
	

	/**
	 * Handling the response requests
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void onCfmlBugRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		// Check to see we have the debug IP's
		if ( !cfSession.checkDebugIP( request.getRemoteAddr() ) ){
			response.setStatus(401);
			return;
		}
			
		
		String f	= request.getParameter("_f");
		if ( f == null )
			f = "index.cfm";
		
		
		// Stop from being naughty and creeping around the system
		if ( f.indexOf("..") != -1 ){
			response.setStatus(404);
			return;
		}
		
		
		// Forward now onto the main cfEngine
		RequestDispatcher rd = request.getRequestDispatcher( "/WEB-INF/webresources/cfmlbug/" + f );
		try {
			rd.forward( request, response );
		} catch (ServletException e) {
			e.printStackTrace();
		}
		
	}
}