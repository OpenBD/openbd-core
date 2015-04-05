/*
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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
 */

/*
 * Created on 21-May-2004 by Alan Williamson
 *
 * Implements the CFCACHE tag
 *
 * Due to the way we retrieve the content we do not need USERNAME, PASSWORD, HTTP, PORT attributes
 * of the CFCACHE tag.
 *
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletResponse;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;

import com.nary.util.Lock;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.servlet.jsp.cfIncludeHttpServletResponseWrapper;

public class cfCACHE extends cfTag implements Serializable, cfOptionalBodyTag
{

	static final long serialVersionUID = 1;

  private static final int  ACTION_CACHE        = 0;
  private static final int  ACTION_FLUSH        = 1;
  private static final int  ACTION_CLIENTCACHE  = 2;
  private static final int  ACTION_SERVERCACHE  = 4;
  private static int	totalCacheHit = 0;	// total server cache hit (doesn't include client cache hits)

  private int actionType = ACTION_CACHE;

  private static Lock cacheLock = new Lock();

	private String endMarker = null;

	public static int getTotalHits(){
		return totalCacheHit;
	}

  public String getEndMarker() {
		return endMarker;
  }

  public void setEndTag() {
		//--[ This is called once from the cfParseTag class.  its to handle <CFMODULE/> which is to trigger double execution
		endMarker = "";
  }

  public void lookAheadForEndTag(tagReader inFile) {
		endMarker = new tagLocator("CFCACHE", inFile).findEndMarker();
  }

  //------------------------------------
  //------------------------------------

  protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute( "ACTION",       "cache" );
		defaultAttribute( "PROTOCOL",     "http://" );
		defaultAttribute( "PORT",     		"80" );
		defaultAttribute( "EXPIREURL", 		"" );

		parseTagHeader( _tag );

    //--[ Get the ACTION property out
    String action = getConstant( "ACTION" ).toLowerCase();
    if ( action.equals("cache") || action.equals("optimal") )
      actionType = ACTION_CACHE;
    else if ( action.equals("flush") )
      actionType = ACTION_FLUSH;
		else if ( action.equals("clientcache") )
	  	actionType = ACTION_CLIENTCACHE;
		else if ( action.equals("servercache") )
	   actionType = ACTION_SERVERCACHE;

		if ( containsAttribute( "TIMEOUT" ) ){
			throw newBadFileException("Unsupported Attribute", "The TIMEOUT attribute is not supported. Use TIMESPAN instead." );
		}
		//--[ No longer required so lets remove it
    removeAttribute( "ACTION" );
  }


	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {

		if ( !containsAttribute( "DIRECTORY" ) ) {
	        defaultAttribute( "DIRECTORY", new File( cfEngine.thisPlatform.getFileIO().getWorkingDirectory(), "cfcache" ).toString() );
	    }
		
		if ( actionType == ACTION_CACHE ){

			//-- Check to see that this execution run isn't in response to a CFCACHE call
			//-- If it is, then simply ignore this execution
			if ( _Session.REQ.getAttribute("bdcache") != null )
				return cfTagReturnType.NORMAL;

			//-- Determine if we are to send back the page we have in cache
			//-- or create a new one
			long browserDate	= getBrowserDate( _Session.REQ.getHeader("If-Modified-Since") );
			File cacheName		= new File( getDirectory(_Session), generateFilename( _Session ) );
			String lockName = cacheName.getAbsolutePath();

			// First check the client cache
			if ( !isClientCachedFileExpired(_Session, cacheName, browserDate, _Session.getCurrentFile().lastModified()) ){
				// It's not expired in the client cache so return not modified and abort processing
				_Session.setStatus( 304, "Not Modified" );
				_Session.abortPageProcessing();
			}

			synchronized( cacheLock.getLock( lockName ) ){
				try{

					// Now check the server cache
					if ( isServerCachedFileExpired(_Session, cacheName, _Session.getCurrentFile().lastModified() ) ){
						// It's expired in the client and server cache so send last-modified to cache it in the
						// client cache and call makeCacheFile() to cache it in the server cache.
						_Session.setHeader( "Last-Modified", com.nary.util.Date.formatNow( "EEE, dd MMM yyyy HH:mm:ss" ) + " GMT" );

						String errorMsg = makeCacheFile(_Session, cacheName);
						if ( errorMsg != null )
							throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.runtimeError","runtime.general", new String[] {"Failed to cache " + _Session.getRequestURI() + " (" + errorMsg + ")"}));
					}else{
						//-- The server cache is good
						totalCacheHit++;
					}

					//-- Read the cache from file and send it to the client
					sendCacheFile( _Session, cacheName );
				}finally{
					cacheLock.removeLock( lockName );
				}
			}

			//-- No point in continuing any further
			_Session.abortPageProcessing( true );

		} else if ( actionType == ACTION_SERVERCACHE ){

			//-- Check to see that this execution run isn't in response to a CFCACHE call
			//-- If it is, then simply ignore this execution
			if ( _Session.REQ.getAttribute("bdcache") != null )
				return cfTagReturnType.NORMAL;

			File cacheName	= new File( getDirectory(_Session), generateFilename( _Session ) );
			String lockName = cacheName.getAbsolutePath();

			synchronized( cacheLock.getLock( lockName ) ){
				try{
					if ( isServerCachedFileExpired(_Session, cacheName, _Session.getCurrentFile().lastModified() ) ){
						String errorMsg = makeCacheFile(_Session, cacheName);
						if ( errorMsg != null )
							throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.runtimeError","runtime.general", new String[] {"Failed to cache " + _Session.getRequestURI() + " (" + errorMsg + ")"}));
					}else{
						//-- The server cache is good
						totalCacheHit++;
					}

					//-- Read the cache from file and send it to the client
					sendCacheFile( _Session, cacheName );
				}finally{
					cacheLock.removeLock( lockName );
				}
			}

			//-- No point in continuing any further
			_Session.abortPageProcessing();

		} else if ( actionType == ACTION_CLIENTCACHE ){

			long browserDate	= getBrowserDate( _Session.REQ.getHeader("If-Modified-Since") );
			File cacheName		= new File( getDirectory(_Session), generateFilename( _Session ) );

			if ( isClientCachedFileExpired(_Session, cacheName, browserDate, _Session.getCurrentFile().lastModified()) ){
				// When only the client cache is being used, we create an empty file
				// in the server cache so we can detect a flush action.
				touchLocalFile( _Session, cacheName );
				_Session.setHeader( "Last-Modified", com.nary.util.Date.formatNow( "EEE, dd MMM yyyy HH:mm:ss" ) + " GMT" );

			}else{
				_Session.setStatus( 304, "Not Modified" );
				_Session.abortPageProcessing();
			}

		}else if ( actionType == ACTION_FLUSH ){
			expireFiles( getDirectory(_Session), getDynamic(_Session,"EXPIREURL").getString(), _Session.REQ.getServerName().toLowerCase() );
		}

		return cfTagReturnType.NORMAL;
	}

  private static long getBrowserDate(String date){
  	if ( date == null || date.length() == 0 )
  		return Long.MAX_VALUE;

		date	= date.substring( date.indexOf(",")+1 ).trim();
		date	= date.substring( 0, date.lastIndexOf(" "));

		java.util.Date dd = com.nary.util.date.dateTimeTokenizer.getUKDate( date );
		if ( dd != null )
			return dd.getTime();
		else
			return Long.MAX_VALUE;
  }

  private File getDirectory(cfSession _Session ) throws cfmRunTimeException {
  	File dir	= new File(getDynamic(_Session,"DIRECTORY").getString());
  	if ( !dir.isDirectory() )
  		dir.mkdirs();

  	return dir;
  }

  private static void touchLocalFile(cfSession _Session, File localFile ){
  	BufferedWriter out = null;
  	try{
			String server = _Session.REQ.getServerName().toLowerCase();
  		String uri		= _Session.getRequestURI();
  		String queryStr	= _Session.REQ.getQueryString();

  		out	= new BufferedWriter( cfEngine.thisPlatform.getFileIO().getFileWriter(localFile) );

  		if ( queryStr != null )
				out.write( "<!-- " + server + uri + "?" + queryStr + " -->\r\n" );
			else
				out.write( "<!-- " + server + uri + " -->\r\n" );

  	}catch(Exception ignore){
  	}finally{
		// Make sure the writer is closed so we'll be able to delete the file for a flush action.
		try{if ( out != null ) out.close();}catch(Exception ignoreClose){}
  	}
 }

  private static String generateFilename( cfSession _Session ) {
		String 	server 		= _Session.REQ.getServerName().toLowerCase();
  	String	queryStr	= _Session.REQ.getQueryString();
  	String	filename	= server + _Session.getRequestURI();
  	if ( queryStr != null )
  		filename	+= "?" + queryStr;

  	return "cfcache_" + com.nary.util.string.hashCode(filename) + ".htm";
  }

	private boolean isServerCachedFileExpired(cfSession _Session, File cacheName, long _pageLastModified )
	  	throws cfmRunTimeException{
	// If the file isn't cached on the server then return true to indicate it expired.
	if ( !cacheName.exists() )
		return true;

  	// if the cfm page has been modified since it was last cached
  	if ( _pageLastModified > cacheName.lastModified() ){
  		return true;
  	}

	// If a timespan was specified and the current time is greater than the cached file's
	// last modified time plus the timespan then return true to indicate it expired.
	if ( containsAttribute("TIMESPAN") ){
	  double timespan = getDynamic(_Session,"TIMESPAN").getDouble();

	  // Convert the timespan to milliseconds
	  long timespanMillis	= (long)((double)86400000 * timespan);

	  // Check if it expired
	  if ( System.currentTimeMillis() > cacheName.lastModified() + timespanMillis )
		  return true;
	}

	return false;
  }

  private boolean isClientCachedFileExpired(cfSession _Session, File cacheName, long browserDate, long _codeLastModified )
  	throws cfmRunTimeException
  {
	// When only the client cache is being used, we create an empty file in the server cache so we can
	// detect a flush action.  When both the client and server cache are being used then the file in the
	// server cache will actually contain the last cached results.  In either case, if the file doesn't
	// exist then we now it's been flushed so return true to indicate it expired.
	if ( !cacheName.exists() )
		return true;

	// If the file doesn't exist in the client cache then return true to indicate it expired.
	// NOTE:  This is detected by the absence of the "If-Modified-Since" request header.
	if ( browserDate == Long.MAX_VALUE )
		return true;

	if ( _codeLastModified > cacheName.lastModified() ){
		return true;
	}

	// If a timespan was specified and the current time is greater than the browserDate plus
	// the timespan then return true to indicate it expired.
	if ( containsAttribute("TIMESPAN") ){
		double timespan = getDynamic(_Session,"TIMESPAN").getDouble();

  		// Convert the timespan to milliseconds
		long timespanMillis	= (long)((double)86400000 * timespan);

		// Check if it expired
		if ( System.currentTimeMillis() > browserDate + timespanMillis )
			return true;
	}

	return false;
  }


  private static String makeCacheFile( cfSession _session, File cacheName ){

  	String uri = _session.getRequestURI();

  	RequestDispatcher rd = _session.REQ.getRequestDispatcher( uri );
		if ( rd == null ) {
			return "Failed to get RequestDispatcher";
		}


  	//---[ Now that the servlet has been found, trigger its execution
		cfIncludeHttpServletResponseWrapper servletOutput = new cfIncludeHttpServletResponseWrapper( _session.RES );
		_session.REQ.setAttribute("bdcache", "");

		try {
			rd.include( _session.REQ, servletOutput );
		} catch ( Exception exc ) {
			//-- This page had an error with it; therefore, we don't cache the page, but continue to
			//-- execute it as a whole so the user can see the *real* error
			return "Requested page threw an exception - " + exc.toString();
		}

		//-- Lets make sure all is well
		if ( servletOutput.getStatusCode() == HttpServletResponse.SC_TEMPORARY_REDIRECT ){
			//- The page we were attempting to cache issued a redirect via CFLOCATION.
			//- We'll honour it here so the page isn't run twice which may cause problems
			try{
				_session.sendRedirect( servletOutput.getRedirectURI() );
			}catch(Exception ignore){}
			return "Failed to redirect";
		} else if ( servletOutput.getStatusCode() != HttpServletResponse.SC_OK ){
			return "Received status code - " + servletOutput.getStatusCode();
		}


		//-- Write output to File
		BufferedWriter	out = null;
		OutputStream fout = null;
		OutputStreamWriter osw = null;
		try{
			String 	server = _session.REQ.getServerName().toLowerCase();
			String	queryStr = _session.REQ.getQueryString();
			fout = cfEngine.thisPlatform.getFileIO().getFileOutputStream(cacheName);
			osw = new OutputStreamWriter( fout, "utf-8" );
			out	= new BufferedWriter( osw );

			if ( queryStr != null )
				out.write( "<!-- " + server + uri + "?" + queryStr + " -->\r\n" );
			else
				out.write( "<!-- " + server + uri + " -->\r\n" );

			String enc = com.nary.util.Localization.convertCharSetToCharEncoding( servletOutput.getCharacterEncoding() );
			String s = new String( servletOutput.getByteArray(), enc );
			out.write( s );
			out.flush();
		}catch(Exception E){
			//-- Something went wrong with the cache output; flag this as an invalid cache creation
			return "Failed to write file to cache - " + E.toString();
		}finally{
			// Make sure the writer is closed so we'll be able to delete the file for a flush action.
			try{if ( out != null ) out.close();}catch(Exception ignoreClose){}
		}

		return null;
  }

  private static void sendCacheFile( cfSession _Session, File cacheName ) throws cfmRunTimeException {
		try{
			sendCacheFile( _Session, cacheName, false );
		}catch( IOException e ){
			String errorMsg = makeCacheFile(_Session, cacheName);
			if ( errorMsg != null )
				throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.runtimeError","runtime.general", new String[] {"Failed to cache " + _Session.getRequestURI() + " (" + errorMsg + ")"}));
			try {
	      sendCacheFile( _Session, cacheName, true );
      } catch ( IOException e1 ) { // shouldn't happen but log it in any case
      	cfEngine.log( "Unexpected exception. IOException should be masked" );
      }
		}
  }

	private static void sendCacheFile( cfSession _Session, File cacheName, boolean _maskIOException ) throws IOException, cfmRunTimeException {
		BufferedReader	in	= null;
		FileInputStream fis = null;
		InputStreamReader isr = null;
		try{
			fis = new FileInputStream( cacheName );
			isr = new InputStreamReader( fis, com.nary.util.Localization.convertCharSetToCharEncoding( "utf-8" ) );
			in	= new BufferedReader( isr );
			String lineIn = in.readLine();	//-- Read the first line; its BlueDragon related
			while ( (lineIn=in.readLine()) != null ){
				_Session.write( lineIn );
				_Session.write( "\r\n" );
			}

		}catch(Exception E){
			if ( ! _maskIOException && E instanceof IOException ){
				throw (IOException) E;
			}
			throw new cfmRunTimeException( catchDataFactory.extendedException( "errorCode.runtimeError",
					"cfcache.fromdisk",
					new String[]{cacheName.toString()},
					E.getMessage()) );
		}finally{
			// Make sure the reader is closed so we'll be able to delete the file for a flush action.
			try{if ( in != null ) in.close();}catch(Exception ignoreClose){}
		}
	}

	private static class cfCACHEFileFilter implements FileFilter
  	{
	  public boolean accept(File pathname)
	  {
	    if(pathname.getName().matches("cfcache_-?[0-9]+\\.htm"))
	    	return true;
	    else
	    	return false;
	  }
	}

  	private static cfCACHEFileFilter cfCacheFileFilter = new cfCACHEFileFilter();

	private void expireFiles( File directory, String expireURL, String virtualServer ) throws cfmRunTimeException {
		File[] listOfFiles = directory.listFiles(cfCacheFileFilter);

		boolean	deleteAll	= (expireURL.equals("*") || expireURL.length() == 0);

		//use of this var is the fix for NA bug #3308
	  	boolean ignoreHost = (! deleteAll && expireURL.startsWith("*"));

		File thisFile;
		String firstline;

		Perl5Compiler 			compiler 	= new Perl5Compiler();
		Perl5Matcher				matcher 	= new Perl5Matcher();
		Pattern 						pattern 	= null;

		if ( !deleteAll ){
			try {
				if(!ignoreHost)
				{
					/* The string in the cache file is always <servername>/<uri>; we need to add in the servername and adjust the expireURL accordingly */
					if ( expireURL.startsWith("/") )
						expireURL = virtualServer + expireURL;
					else
						expireURL = virtualServer + "/" + expireURL;
				}

				pattern		= compiler.compile( escapeExpireUrl( expireURL ) );
			} catch (MalformedPatternException e) {
				throw new cfmRunTimeException( catchDataFactory.extendedException( "errorCode.runtimeError",
						 "cfcache.expireUrl",
						 new String[]{expireURL},
						 e.getMessage()) );
			}
		}

  	for ( int x=0; x < listOfFiles.length; x++ ){
  		thisFile	= listOfFiles[x];
  		if ( deleteAll ) {
  			boolean success = false;
  			int tries = 0;
  			for ( ; (tries < 10) && (!success); tries++ )
  			{
  				if ( deleteCachedFile( thisFile ) )
  					success = true;
 			}
			if ( !success ) {
				throw newRunTimeException( "Failed to delete cache file: " + thisFile );
			}
  		}
  		else{
  			firstline	= getURIFromFile( thisFile );
  			if ( firstline != null ){

  				if( pattern != null && matcher.contains( new PatternMatcherInput( firstline ), pattern ) )
  					deleteCachedFile( thisFile );

  			}
  		}
  	}
  }

  private boolean deleteCachedFile( File _f ){
  	synchronized( cacheLock.getLock( _f.getAbsolutePath() ) ){
  		try{
  			return _f.delete();
  		}finally{
  			cacheLock.removeLock( _f.getAbsolutePath() );
  		}
  	}
  }

  private static String escapeExpireUrl(String expireURL){
		Perl5Compiler 			compiler 	= new Perl5Compiler();
		Perl5Matcher				matcher 	= new Perl5Matcher();
		Pattern 						pattern 	= null;

		try{
			pattern		= compiler.compile( "([+?.])" );
		  expireURL = Util.substitute(matcher, pattern, new Perl5Substitution( "\\\\$1" ), expireURL, Util.SUBSTITUTE_ALL );
		  return com.nary.util.string.replaceString(expireURL,"*",".*");
		}catch(Exception E){
			return null;
		}
  }


  private static String getURIFromFile(File file){
  	try{
  		BufferedReader	in	= new BufferedReader( new FileReader(file) );

  		String lineIn  = in.readLine();
  		in.close();

  		int c1 = lineIn.indexOf("<!--");
  		if ( c1 == -1 )	return null;

  		return lineIn.substring( c1+4, lineIn.indexOf("-->")-1 );

  	}catch(Exception E){
  		return null;
  	}
  }

}
