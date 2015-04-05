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
 *  http://openbd.org/
 *  $Id: cfHTTP.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.http;

import java.io.File;
import java.io.IOException;

import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.tag.cfOptionalBodyTag;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.cfm.tag.tagLocator;
import com.naryx.tagfusion.cfm.tag.tagReader;

public class cfHTTP extends cfTag implements cfOptionalBodyTag, java.io.Serializable {

	static final long						serialVersionUID	= 1;

	private static final String	TAG_NAME					= "CFHTTP";
	private static final String	DATA_BIN_KEY			= "CFHTTP";
	private String							endMarker					= null;



	@SuppressWarnings("unchecked")
	public java.util.Map getInfo() {
		return createInfo( "remote", "CFHTTP allows you to interact with remote HTTP sites, from simply downloading a file, to submitting form data.  " + "Using the CFHTTPPARAM you can control all aspects of the HTTP experience" );
	}



	@SuppressWarnings("unchecked")
	public java.util.Map[] getAttInfo() {
		return new java.util.Map[] { 
				createAttInfo( "ATTRIBUTECOLLECTION", 	"A structure containing the tag attributes", "", false ), 
				createAttInfo( "URL", 									"The full URL to use", "", true ), 
				createAttInfo( "RESOLVEURL", 						"Controls if the internal links are resolved", "false", false ), 
				createAttInfo( "REDIRECT", 							"Automatically follow the redirects from the remote server", "true", true ), 
				createAttInfo( "GETASBINARY", 					"Return the body back as a binary object", "false", false ), 
				createAttInfo( "THROWONERROR", 					"If an error occurs throw an exception", "false", false ), 
				createAttInfo( "PORT", 									"Override the port to connect to", "", false ), 
				createAttInfo( "USERAGENT", 						"The UserAgent string to use for the request", "BlueDragon", false ), 
				createAttInfo( "METHOD",								"The HTTP method to use", "GET", false ), 
				createAttInfo( "MULTIPART", 						"Is this a multipart request", "false", false ),
				createAttInfo( "PROXYSERVER", 					"The proxy server to use", "", false ), 
				createAttInfo( "PROXYPORT", 						"The port of the proxy server to use", "", false ), 
				createAttInfo( "PROXYUSER", 						"The user to user for authentication to the proxy server", "", false ), 
				createAttInfo( "PROXYPASSWORD", 				"The password to user for authentication to the proxy server", "", false ),
				createAttInfo( "USER", 									"The user to user for authentication", "", false ), 
				createAttInfo( "PASSWORD", 							"The password to user for authentication", "", false ),
				createAttInfo( "FILE", 									"The response will be written to the file if specified", "", false ),
				createAttInfo( "PATH", 									"The path to the file to attach to the POST", "", false ), 
				createAttInfo( "URIDIRECTORY", 					"Is the file relative to the web root", "false", false ), 
				createAttInfo( "CHARSET",	 							"The character set for the request", "", false ), 
				createAttInfo( "RESULT", 								"The name of the variable to get the result", "cfhttp", false ),
				createAttInfo( "NAME", 									"The name of the variable to receive the query", "", false ), 
				createAttInfo( "FIRSTROWASHEADERS", 		"If receiving a CSV file, does the first line contain the headers", "", false ), 
				createAttInfo( "COLUMNS", 							"List of columns from the result CSV", "", false ), 
				createAttInfo( "TEXTQUALIFIER", 				"The text qualifier for the CSV", "\"", false ), 
				createAttInfo( "DELIMITER", 						"The text delimiter for the CSV", ",", false ),

		};
	}



	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute( "RESOLVEURL", 		"false" );
		defaultAttribute( "GETASBINARY", 		"no" );
		defaultAttribute( "REDIRECT", 			"true" );
		defaultAttribute( "THROWONERROR", 	"no" );
		defaultAttribute( "PORT", 					-1 );
		defaultAttribute( "USERAGENT", 			"OpenBD" );
		defaultAttribute( "URIDIRECTORY", 	"no" );
		defaultAttribute( "METHOD", 				"GET" );
		defaultAttribute( "MULTIPART", 			"false" );
		defaultAttribute( "FIRSTROWASHEADERS", "true" );
		defaultAttribute( "TIMEOUT", 				"9999" );

		defaultAttribute( "PASSWORD", 			"" );
		defaultAttribute( "PROXYPASSWORD", 	"" );

		defaultAttribute( "DELIMITER", 			"," );
		defaultAttribute( "TEXTQUALIFIER", 	"\"\"" ); // need to do this so runExpression escapes it to a single double quote

		parseTagHeader( _tag );
		setFlushable( false );

		if ( containsAttribute( "ATTRIBUTECOLLECTION" ) )
			return;

		if ( !containsAttribute( "URL" ) )
			throw newBadFileException( "Missing Attribute", "This tag requires the URL attribute" );
	}



	protected cfStructData setAttributeCollection( cfSession _Session ) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection( _Session );

		if ( !containsAttribute( attributes, "URL" ) )
			throw newBadFileException( "Missing Attribute", "This tag requires the URL attribute" );

		return attributes;
	}



	public String getEndMarker() {
		return endMarker;
	}



	public void setEndTag() {
		endMarker = null;
	}



	public void lookAheadForEndTag( tagReader inFile ) {
		endMarker = ( new tagLocator( TAG_NAME, inFile ) ).findEndMarker();
	}



	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection( _Session );

		String methodStr = getDynamic( attributes, _Session, "METHOD" ).getString();

  	cfHttpData httpData = null;
    if ( containsAttribute( attributes, "CHARSET" ) ){
      httpData = new cfHttpData( getDynamic( attributes, _Session, "CHARSET" ).getString() );
    }else{
    	httpData = new cfHttpData( "UTF-8" );
    }

		_Session.setDataBin( DATA_BIN_KEY, httpData );
		cfHttpConnectionI httpConnection = null;

		try {
			httpData.setUsesGet( methodStr.equalsIgnoreCase( "GET" ) );

			renderToString( _Session );

			File certFile = null;
			String certPassword = null;
			if ( containsAttribute( "CLIENTCERT" ) ){
				if ( !containsAttribute( "CLIENTCERTPASSWORD" ) ){
					throw newRunTimeException( "The CLIENTCERTPASSWORD must be provided when the CLIENTCERT has a value." );
				}
				certFile = new File( getDynamic( attributes, _Session, "CLIENTCERT" ).getString() );
				if ( !certFile.exists() || !certFile.canRead() ){
					throw newRunTimeException( "Failed to read clientcert file '" + getDynamic( attributes, _Session, "CLIENTCERT" ).getString() + "'. Check the file path exists and has the appropriate read permissions.");
				}
				certPassword = getDynamic( _Session, "CLIENTCERTPASSWORD" ).getString();
			}
			
			// httpConnection = new cfHttpConnection( _Session, httpData );
			httpConnection = new cfHttpConnection(_Session, httpData, certFile, certPassword );
			
			httpConnection.setThrowOnError( getDynamic( attributes, _Session, "THROWONERROR" ).getBoolean() );

			httpConnection.setMethod( methodStr, getDynamic( attributes, _Session, "MULTIPART" ).getBoolean() );

			String url = getDynamic( attributes, _Session, "URL" ).getString();
			int port = getDynamic( attributes, _Session, "PORT" ).getInt();
			httpConnection.setURL( url, port );

			// Optional proxy configuration
			if ( containsAttribute( attributes, "PROXYSERVER" ) ) {
				String proxyserver = getDynamic( attributes, _Session, "PROXYSERVER" ).getString();
				int proxyPort = 80;
				if ( containsAttribute( attributes, "PROXYPORT" ) ) {
					proxyPort = getDynamic( attributes, _Session, "PROXYPORT" ).getInt();
				}
				httpConnection.setProxyServer( proxyserver, proxyPort );
			}

			httpConnection.setUserAgent( getDynamic( attributes, _Session, "USERAGENT" ).getString() );

			if ( containsAttribute( attributes, "USERNAME" ) ) {
				httpConnection.authenticate( getDynamic( attributes, _Session, "USERNAME" ).getString(), getDynamic( attributes, _Session, "PASSWORD" ).getString() );
			}

			if ( containsAttribute( attributes, "PROXYUSER" ) ) {
				httpConnection.authenticateProxy( getDynamic( attributes, _Session, "PROXYUSER" ).getString(), getDynamic( attributes, _Session, "PROXYPASSWORD" ).getString() );
			}

			// set timeout and the option to FollowRedirects
			httpConnection.setFollowRedirects( getDynamic( attributes, _Session, "REDIRECT" ).getBoolean() );

			setTimeout( attributes, _Session, httpConnection );

			if ( containsAttribute( attributes, "PATH" ) ) {
				httpConnection.setFile( getFile( attributes, _Session, url ) );
			}

			if ( containsAttribute( attributes, "CHARSET" ) ) {
				String charsetVal = getDynamic( attributes, _Session, "CHARSET" ).getString();
				if ( charsetVal.trim().length() > 0 ){
					httpConnection.setCharset( charsetVal );
				}
			}

			if ( containsAttribute( attributes, "NAME" ) ) {
				String columns = null;
				if ( containsAttribute( attributes, "COLUMNS" ) ) {
					columns = getDynamic( attributes, _Session, "COLUMNS" ).getString();
				}

				httpConnection.setQueryDetails( getDynamic( attributes, _Session, "NAME" ).getString(), columns,
						getDynamic( attributes, _Session, "TEXTQUALIFIER" ).getString(),
						getDynamic( attributes, _Session, "DELIMITER" ).getString(),
						getDynamic( attributes, _Session, "FIRSTROWASHEADERS" ).getBoolean() );
			}

			httpConnection.setGetAsBinary( getDynamic( attributes, _Session, "GETASBINARY" ).getString().toLowerCase() );
			httpConnection.setResolveLinks( getDynamic( attributes, _Session, "RESOLVEURL" ).getBoolean() );
			httpConnection.connect();

		} finally {
			// Release the connection.
			try {
				if ( httpConnection != null )
					httpConnection.close();
			} catch ( IOException ioe ) {
				// Shouldn't be important if an exception is thrown at this stage but lets log it
				cfEngine.log( "CFHTTP exception when closing connection: " + ioe.getClass() + ioe.getMessage() );
			}

			_Session.deleteDataBin( DATA_BIN_KEY );
		}

		if ( containsAttribute( attributes, "RESULT" ) ) {
			_Session.setData( getDynamic( attributes, _Session, "RESULT" ).getString(), httpData );
		} else {
			_Session.setData( "CFHTTP", httpData );

		}

		return cfTagReturnType.NORMAL;
	}



	private void setTimeout( cfStructData attributes, cfSession _Session, cfHttpConnectionI _connection ) throws cfmRunTimeException {
		int timeout = getDynamic( attributes, _Session, "TIMEOUT" ).getInt();

		cfStructData urlData = ( cfStructData )_Session.getQualifiedData( variableStore.URL_SCOPE );
		if ( urlData.containsKey( "RequestTime" ) ) {
			int tmp = urlData.getData( "RequestTime" ).getInt();
			if ( tmp < timeout ) {
				timeout = tmp;
			}
		}

		_connection.setTimeout( timeout * 1000 );
	}



	// returns null if no file is specified, or file specified is invlaid
	private File getFile( cfStructData attributes, cfSession _Session, String _url ) throws cfmRunTimeException {
		if ( containsAttribute( attributes, "FILE" ) ) {
			return resolveToFullpath( attributes, _Session, getDynamic( attributes, _Session, "FILE" ).getString() );
		} else {
			return resolveToFullpath( attributes, _Session, getFilenameFromURL( _url ) );
		}

	}// getFile()



	private static String getFilenameFromURL( String _url ) {
		int anchorIndex = _url.indexOf( '#' );
		int dataIndex = _url.indexOf( '?' );
		// find the smallest substring index
		int smallestIndex = ( anchorIndex == -1 ) ? dataIndex : ( ( dataIndex == -1 ) ? anchorIndex : ( dataIndex < anchorIndex ? dataIndex : anchorIndex ) );

		String urlCopy = _url;

		if ( smallestIndex != -1 ) {
			urlCopy = urlCopy.substring( 0, smallestIndex );
		}

		urlCopy.replace( '\\', '/' );

		if ( urlCopy.endsWith( "/" ) ) {
			return urlCopy.substring( urlCopy.substring( 0, urlCopy.length() - 1 ).lastIndexOf( "/" ), urlCopy.length() - 1 );

		} else {
			return urlCopy.substring( urlCopy.lastIndexOf( "/" ), urlCopy.length() );
		}

	}// getFilenameFromURL()



	private File resolveToFullpath( cfStructData attributes, cfSession _Session, String _filename ) throws cfmRunTimeException {
		String pathStr = getDynamic( _Session, "PATH" ).getString();
		File path;
		File fullPath; // the full path with filename at the end

		boolean bURI = getDynamic( attributes, _Session, "URIDIRECTORY" ).getBoolean();
		if ( bURI )
			path = com.nary.io.FileUtils.getRealFile( _Session.REQ, pathStr );
		else
			path = new File( pathStr );

		if ( path != null ) {
			fullPath = new File( path.toString() + File.separator + _filename );
			if ( path.isDirectory() && !fullPath.isDirectory() ) {
				return fullPath;
			} else {
				// set fileContent to invalid path
				return null;
			}
		} else {
			throw newRunTimeException( "The file path provided is invalid" );
		}
	}

	// resolveToFullpath

}
