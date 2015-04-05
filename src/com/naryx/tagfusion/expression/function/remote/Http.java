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
 *  $Id: Http.java 2294 2012-11-05 12:43:19Z alan $
 */

package com.naryx.tagfusion.expression.function.remote;

import java.io.File;
import java.io.IOException;

import javax.activation.MimetypesFileTypeMap;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.file.fileDescriptor;
import com.naryx.tagfusion.cfm.http.cfHttpConnection;
import com.naryx.tagfusion.cfm.http.cfHttpConnectionI;
import com.naryx.tagfusion.cfm.http.cfHttpData;
import com.naryx.tagfusion.expression.function.functionBase;

public class Http extends functionBase {

	private static final long	serialVersionUID	= 1L;

	/**
	 * Setting mandatory and optional parameters name and count
	 */
	public Http() {
		min = 1;
		max = 28;
		setNamedParams( new String[] { 
				"url", "resolveurl", "redirect", "getasbinary", 
				"throwonerror", "port", "useragent", "method", 
				"multipart", "timeout", "proxyserver", "proxyport", 
				"proxyuser", "proxypassword", "user", "password", 
				"file", "path", "uridirectory", "charset", "result", 
				"name", "firstrowasheaders", "columns", "textqualifier", 
				"delimiter", "httpparams", "clientcert", "clientcertpassword" } );
	}


	/*
	 * (non-Javadoc)
	 * @see com.naryx.tagfusion.expression.function.functionBase#getParamInfo()
	 * Describing functionality of each parameters
	 */
	public String[] getParamInfo() {
		return new String[] { 
				"The full URL to request", 
				"A boolean to indicate whether internal links are resolved", 
				"A boolean to indicate whether to automatically follow the redirects from the remote server", 
				"A boolean to indicate whether to return the body back as a binary object - valid values are AUTO, YES and NO", 
				"A boolean to indicate whether an exception should be thrown if an error occurs", 
				"Set the port to connect to. Defaults to 80.", 
				"The UserAgent value to use for the request", 
				"The HTTP method to use. Defaults to GET", 
				"A boolean to indicate whether this a multipart request in the case where the method is POST", 
				"The number of seconds to wait before timing out the request", 
				"The proxy server to use", 
				"The port of the proxy server to use", 
				"The username for authentication against the proxy server if required", 
				"The password for authentication against the proxy server if required", 
				"The username if basic authentication is required", 
				"The password if basic authentication is required", 
				"If specified, the response will be written to the file for the given path", 
				"The path to the file to upload with a POST", 
				"A boolean to indicate whether the file path is relative to the web root", 
				"The character set of the response", 
				"The name of the variable to set the result", 
				"The name of the variable to set the query if the response should be treated as CSV", 
				"If reading the response as CSV, this boolean indicates whether the first line contain the headers", 
				"If reading the response as CSV, the list of columns from the result CSV. The column count has to be the same as the column count in the original data", 
				"If reading the response as CSV, the text qualifier to use in parsing it", 
				"If reading the response as CSV, the text delimiter to use in parsing it", 
				"Array of structures {type, value, encoded, url, formfield, cgi, header, cookie, file, mimetype} representing the attributes of CFHTTPPARAM",
				"The file path to the client certificate to use for a secure connection",
				"The password to the client certificate file"
		};
	}


	public java.util.Map getInfo() {
		return makeInfo( "remote", 
				"The Http function allows you to interact with remote HTTP sites, " +
				"from simply downloading a file, to submitting form data.  Returns back the CFHTTP structure", 
				ReturnType.STRUCTURE );
	}


	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String charset = getNamedStringParam( argStruct, "charset", null );

		cfHttpData httpData = new cfHttpData( charset == null ? "UTF-8" : charset );
		String result = getNamedStringParam( argStruct, "result", null );
		cfHttpConnectionI httpConnection = null;
		try {

			// get the client cert and password options if they've been passed in
			String clientCert = getNamedStringParam( argStruct, "clientcert", null );
			String clientCertPassword = getNamedStringParam( argStruct, "clientcertpassword", null );
			File clientCertFile = null;
			if ( clientCert != null ){
				if ( clientCertPassword == null ){
					throwException( _session, "The CLIENTCERTPASSWORD must be provided when the CLIENTCERT has a value." );
				}
				clientCertFile = new File( clientCert );
				if ( !clientCertFile.exists() || !clientCertFile.canRead() ){
					throwException( _session, "Failed to read clientcert file '" + clientCert + "'. Check the file path exists and has the appropriate read permissions.");
				}
			}
			
			httpConnection = new cfHttpConnection( _session, httpData, clientCertFile, clientCertPassword );
			
			String url = getNamedStringParam( argStruct, "url", null );
			httpConnection.setMethod( getNamedStringParam( argStruct, "method", "GET" ), getNamedBooleanParam( argStruct, "multipart", false ) );
			httpConnection.setURL( url, getNamedIntParam( argStruct, "port", -1 ) );
			
			if ( charset != null ){
				httpConnection.setCharset( charset );
			}

			// authentication
			String user = getNamedStringParam( argStruct, "user", null );
			if ( user != null ){
				httpConnection.authenticate( user, getNamedStringParam( argStruct, "password", "" ) );
			}

			// proxy related
			String proxyserver = getNamedStringParam( argStruct, "proxyserver", null );
			if( proxyserver != null ){
				httpConnection.setProxyServer( proxyserver, getNamedIntParam( argStruct, "proxyport", -1 ) );

				String proxyuser = getNamedStringParam( argStruct, "proxyuser", null );
				if( proxyuser != null ){
					String proxypassword = getNamedStringParam( argStruct, "proxypassword", "" );
					httpConnection.authenticateProxy( proxyuser, proxypassword );
				}
			}


			// saving result as a query?
			String name = getNamedStringParam( argStruct, "name", null );
			if( name != null ){
				boolean firstrowasheader = getNamedBooleanParam( argStruct, "firstrowasheaders", true );
				String columns = getNamedStringParam( argStruct, "columns", null );
				String textqualifier = getNamedStringParam( argStruct, "textqualifier", "\"" );
				String delimiter = getNamedStringParam( argStruct, "delimiter", "," );
				httpConnection.setQueryDetails( name, columns, textqualifier, delimiter, firstrowasheader );
			}
			
			setTimeout( _session, httpConnection, getNamedIntParam( argStruct, "timeout", 9999 ) );
			httpConnection.setFollowRedirects( getNamedBooleanParam( argStruct, "redirect", true ) );
			httpConnection.setThrowOnError( getNamedBooleanParam( argStruct, "throwonerror", false ) );
			httpConnection.setGetAsBinary( getNamedStringParam( argStruct, "getasbinary", "no" ).toLowerCase() );
			httpConnection.setResolveLinks( getNamedBooleanParam( argStruct, "resolveurl", false ) );
			httpConnection.setUserAgent( getNamedStringParam( argStruct, "useragent", "OpenBD" ) );
			
			String path = getNamedStringParam( argStruct, "path", null );
			if( path != null )
				httpConnection.setFile( getFile( _session, url, argStruct ) );
			

			cfData paramData = getNamedParam( argStruct, "httpparams", null );
			if ( paramData != null ) {
				cfArrayData httpParams = ( cfArrayData )paramData;
				for ( int x = 0; x < httpParams.size(); x++ ) {
					cfData data = httpParams.getElement( x + 1 );
					if ( data.getDataType() != cfData.CFSTRUCTDATA )
						throwException( _session, "params must be an array of structures; " + ( x + 1 ) + " element was not a structure" );

					cfStructData sdata = ( cfStructData )data;

					// Extract all parameters
					String paramDataType = null;
					boolean paramDataEncoded = false;

					if ( sdata.containsKey( "type" ) ) {
						paramDataType = sdata.getData( "type" ).getString();

						// URL
						if ( paramDataType.equalsIgnoreCase("url" ) ) {
							String urlName = getName( sdata, _session, paramDataType );
							String urlValue = getValue( sdata, _session, paramDataType );
							httpData.addURLData( urlName, urlValue );

							// FORMFIELD
						} else if ( paramDataType.equalsIgnoreCase( "formfield" ) ) {
							if ( httpData.isBodySet() ) {
								throwException( _session, "You cannot combine CFHTTPPARAM values of type FILE/FORMFIELD with those of type BODY/XML." );
							}
							String formFiledName = getName( sdata, _session, paramDataType );
							if ( formFiledName.length() != 0 ) {
								String formFieldValue = getValue( sdata, _session, paramDataType );
								if ( sdata.containsKey( "encoded" ) ) {
									paramDataEncoded = sdata.getData( "encoded" ).getBoolean();
									httpData.addFormData( formFiledName, formFieldValue, paramDataEncoded );
								} else {
									httpData.addFormData( formFiledName, formFieldValue, paramDataEncoded );
								}
							}

							// CGI
						} else if ( paramDataType.equalsIgnoreCase( "cgi" ) ) {
							String cgiName = getName( sdata, _session, paramDataType );
							if ( cgiName.length() != 0 ) {
								String cgiValue = getValue( sdata, _session, paramDataType );
								if ( sdata.containsKey( "encoded" ) ) {
									paramDataEncoded = sdata.getData( "encoded" ).getBoolean();
									httpData.addHeader( cgiName, cgiValue, paramDataEncoded );
								} else {
									httpData.addHeader( cgiName, cgiValue, paramDataEncoded );// only url encode the data if ENCODED = YES
								}
							}

							// BODY/XML
						} else if ( paramDataType.equalsIgnoreCase( "body" ) || paramDataType.equalsIgnoreCase( "xml" ) ) {
							if ( httpData.isBodySet() ) {
								throwException( _session, "You can only use one CFHTTPPARAM of type XML or BODY." );
							} else if ( httpData.getFiles().size() > 0 || httpData.getFormData().size() > 0 ) {
								throwException( _session, "You cannot combine CFHTTPPARAM values of type FILE/FORMFIELD with those of type BODY/XML." );
							}
							if ( paramDataType.equalsIgnoreCase( "xml" ) ) {
								String xmlValue = getValue( sdata, _session, paramDataType );
								httpData.setBody( xmlValue, "text/xml" );
							} else {
								String bodyValue = getValue( sdata, _session, paramDataType );
								httpData.setBody( bodyValue, "application/octet-stream" );
							}

							// HEADER
						} else if ( paramDataType.equalsIgnoreCase( "header" ) ) {
							String headerName = getName( sdata, _session, paramDataType );
							if ( headerName.length() != 0 ) { // different from cgi in that data is not urlencoded
								String headerValue = getValue( sdata, _session, paramDataType );
								httpData.addHeader( headerName, headerValue, false );
							}

							// COOKIE
						} else if ( paramDataType.equalsIgnoreCase( "cookie" ) ) {
							String cookieName = getName( sdata, _session, paramDataType );
							if ( cookieName.length() != 0 ) {
								String cookieValue = getValue( sdata, _session, paramDataType );
								if ( sdata.containsKey( "encoded" ) ) {
									paramDataEncoded = sdata.getData( "encoded" ).getBoolean();
									httpData.addCookie( cookieName, cookieValue, paramDataEncoded );
								} else {
									httpData.addCookie( cookieName, cookieValue, paramDataEncoded );
								}
							}

							// FILE
						} else if ( paramDataType.equalsIgnoreCase( "file" ) ) {
							if ( httpData.isBodySet() ) {
								throwException( _session, "You cannot combine CFHTTPPARAM values of type FILE/FORMFIELD with those of type BODY/XML." );
							}
							String fileName = getName( sdata, _session, paramDataType );
							String fileMimeType;
							if ( sdata.containsKey( "file" ) ) {
								String filePath = sdata.getData( "file" ).getString();
								if ( sdata.containsKey( "mimetype" ) ) {
									fileMimeType = sdata.getData( "mimetype" ).getString();
								} else {
									MimetypesFileTypeMap mimemap = new MimetypesFileTypeMap();
									fileMimeType = mimemap.getContentType( filePath );
								}
								if ( filePath.length() != 0 ) {
									httpData.addFile( new fileDescriptor( fileName, new File( filePath ), fileMimeType ) );
								}
							}

						}

					} else {
						throwException( _session, "Missing the TYPE attribute for http parameter." );
					}
				}
			}
			
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
 		}
		
		if (result != null ){
			_session.setData( result, httpData );
    }
		
		return httpData;
	}



	private void setTimeout( cfSession _Session, cfHttpConnectionI _connection, int timeout ) throws cfmRunTimeException {

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
	private File getFile( cfSession session, String url, cfArgStructData argStruct ) throws cfmRunTimeException {
		String file = getNamedStringParam( argStruct, "file", null );
		if ( file != null ) {
			return resolveToFullpath( session, file, argStruct );
		} else {
			return resolveToFullpath( session, getFilenameFromURL( url ),argStruct);
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



	private File resolveToFullpath( cfSession session, String filename, cfArgStructData argStruct ) throws cfmRunTimeException {
		String path = getNamedStringParam( argStruct, "path", null );
		File fPath;
		File fullPath = null; // the full path with filename at the end
		boolean uriDirectory = getNamedBooleanParam( argStruct, "uridirectory", false );
		if ( uriDirectory )
			fPath = com.nary.io.FileUtils.getRealFile( session.REQ, path );
		else
			fPath = new File( path );

		if ( fPath != null ) {
			fullPath = new File( fPath.toString() + File.separator + filename );
			if ( fPath.isDirectory() && !fullPath.isDirectory() ) {
				return fullPath;
			} else {
				return null;
			}
		} else {
			throwException( session, "The path provided is a bad path" );
			return null;
		}

	}// resolveToFullpath



	private String getName( cfStructData sdata, cfSession _Session, String _type ) throws cfmRunTimeException {
		if ( ! ( sdata.containsKey( "name" ) ) ) {
			throwException( _Session, "This tag requires a NAME attribute when TYPE is \"" + _type + "\"." );
		}
		return sdata.getData( "name" ).toString();
	}



	private String getValue( cfStructData sdata, cfSession _Session, String _type ) throws cfmRunTimeException {
		if ( ! ( sdata.containsKey( "value" ) ) ) {
			throwException( _Session, "This tag requires a VALUE attribute when TYPE is \"" + _type + "\"." );
		}
		return sdata.getData( "value" ).toString();
	}
}
