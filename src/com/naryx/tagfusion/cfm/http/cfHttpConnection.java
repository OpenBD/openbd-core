/*
 *  Copyright (C) 2000 - 2014 TagServlet Ltd
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
 *  $Id: cfHttpConnection.java 2526 2015-02-26 15:58:34Z alan $
 */

package com.naryx.tagfusion.cfm.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.ContentEncodingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.aw20.io.StreamUtil;
import org.aw20.util.StringUtil;

import com.nary.net.tagFilterReader;
import com.nary.net.http.urlEncoder;
import com.nary.net.http.urlResolver;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.fileDescriptor;

/*
 * Created on each render of a CFHTTP tag
 */

@SuppressWarnings("deprecation")
public class cfHttpConnection implements cfHttpConnectionI {

	private static Scheme					https;
	private static SchemeRegistry	scheme;
	static {
		try {
			X509HostnameVerifier hostVerifier = SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;

			if ( System.getProperty( "com.naryx.cfm.http.X509HostnameVerifier" ) != null ) {
				String hostnameVerifier = System.getProperty( "com.naryx.cfm.http.X509HostnameVerifier" );
				cfEngine.log( "-] Using alternative CFHTTP hostname verifier: " + hostnameVerifier );
				hostVerifier = ( X509HostnameVerifier )Class.forName( hostnameVerifier ).newInstance();
			}

			SSLContext sslcontext = SSLContext.getDefault();
			SSLSocketFactory socketFactory = new SSLSocketFactory( sslcontext, hostVerifier );

			https = new Scheme( "https", 443, socketFactory );
			scheme = new SchemeRegistry();
			scheme.register( https );

		} catch ( Exception e ) {
			cfEngine.log( "-] Failed due to " + e.getClass().getName() + ": " + e.getMessage() );
		}
	}

	private DefaultHttpClient			client;
	private HttpUriRequest				message;
	private boolean								isMultipart;

	private MultipartEntityBuilder	multipartEntityBuilder = null;

	public cfHttpConnection( cfSession _session, cfHttpData _httpData ) throws cfmRunTimeException {
		this( _session, _httpData, null, null );
	}



	public cfHttpConnection( cfSession _session, cfHttpData _httpData, File _clientCert, String _clientPassword ) throws cfmRunTimeException {
		init( _session, _httpData );
		if ( _clientCert != null ) {
			try {
				client = getHttpClient( _clientCert, _clientPassword );
			} catch ( Exception e ) {
				throw newRunTimeException( "Failed to instantiate http client due to certificate issue. " + e.getClass().getName() + " was thrown: " + e.getMessage() );
			}
		} else {
			client = new ContentEncodingHttpClient();
		}
		client.getParams().setBooleanParameter( ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true );
		resolveLinks = false;
	}



	@Override
	public void setMethod( String _method, boolean _multipart ) throws cfmRunTimeException {
		message = resolveMethod( _method, _multipart );
		isMultipart = _multipart;
	}



	@Override
	public void setURL( String _url, int _port ) throws cfmRunTimeException {
		port = _port;
		url = _url;

		if ( _url.startsWith( "/" ) ) {
			url = "http://" + session.REQ.getServerName() + ":" + session.REQ.getServerPort() + session.REQ.getContextPath() + url;
		}

		try {
			( ( HttpRequestBase )message ).setURI( new URI( url ) );
		} catch ( URISyntaxException ue ) {
			throw newRunTimeException( "Failed to set URL:" + ue.getReason() );
		}

		int uriPort = message.getURI().getPort();
		if ( port == -1 && uriPort == -1 ) {
			// use default port for http/https
		} else if ( uriPort != -1 ) {
			// use specified port
			port = uriPort;
		}
	}



	@Override
	public void setProxyServer( String _proxyServer, int _proxyPort ) {
		proxyServer = _proxyServer;
		proxyPort = _proxyPort;
		HttpHost proxy = new HttpHost( proxyServer, proxyPort );
		client.getParams().setParameter( ConnRoutePNames.DEFAULT_PROXY, proxy );
	}



	@Override
	public void authenticate( String _user, String _password ) {
		client.getCredentialsProvider().setCredentials( AuthScope.ANY, new UsernamePasswordCredentials( _user, _password ) );
	}



	@Override
	public void authenticateProxy( String _user, String _password ) {
		client.getCredentialsProvider().setCredentials( new AuthScope( proxyServer, proxyPort ), new UsernamePasswordCredentials( _user, _password ) );
	}



	@Override
	public void setFollowRedirects( boolean _follow ) {
		followRedirects = _follow;
		client.getParams().setParameter( "http.protocol.handle-redirects", followRedirects );
	}



	@Override
	public void setTimeout( int _timeout ) {
		client.getParams().setParameter( "http.connection.timeout", _timeout );
		client.getParams().setParameter( "http.socket.timeout", _timeout );
	}



	private DefaultHttpClient getHttpClient( File pKeyFile, String pKeyPassword ) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance( "SunX509" );
		KeyStore keyStore = KeyStore.getInstance( "PKCS12" );

		InputStream keyInput = null;
		try {
			keyInput = new FileInputStream( pKeyFile );
			keyStore.load( keyInput, pKeyPassword.toCharArray() );
		} finally {
			if ( keyInput != null )
				try {
					keyInput.close();
				} catch ( IOException ignored ) {
				}
		}

		keyManagerFactory.init( keyStore, pKeyPassword.toCharArray() );

		SSLSocketFactory sf = new SSLSocketFactory( keyStore, pKeyPassword );
		sf.setHostnameVerifier( SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );

		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion( params, HttpVersion.HTTP_1_1 );
		HttpProtocolParams.setContentCharset( params, HTTP.UTF_8 );

		SchemeRegistry registry = new SchemeRegistry();
		registry.register( new Scheme( "http", PlainSocketFactory.getSocketFactory(), 80 ) );
		registry.register( new Scheme( "https", sf, 443 ) );

		ClientConnectionManager ccm = new ThreadSafeClientConnManager( params, registry );

		return new DefaultHttpClient( ccm, params );
	}



	@Override
	public void connect() throws cfmRunTimeException {

		// Collect up the request
		addHeaders();
		addCookies();
		addURLData();
		addFormData();
		addFiles();
		setBody();
		setDefaultProxy();

		// if we are building up a multipart then we should add this to the message
		if ( multipartEntityBuilder != null ){
			HttpEntity reqEntity	= multipartEntityBuilder.build();
			((HttpPost)message).setEntity( reqEntity );
		}



		// Execute the method.
		int statusCode = -1;
		int redirectLimit = 5;
		HttpResponse response;
		try {
			client.getParams().setParameter( "http.protocol.max-redirects", redirectLimit );

			response = client.execute( message );
			statusCode = response.getStatusLine().getStatusCode();

		} catch ( ConnectTimeoutException ce ) {
			if ( !throwOnError ) {
				handleError( "Connect Exception: Connection timed out.", "Connection Failed" );
			} else {
				throw newRunTimeException( "Connect Exception: Connection timed out." );
			}
			return;
		} catch ( ClientProtocolException e ) {
			if ( !throwOnError ) {
				handleError( "Connect Exception: " + e.getMessage(), "Connection Failed" );
			} else {
				throw newRunTimeException( "Failed due to invalid Protocol: " + e.getMessage() );
			}
			return;
		} catch ( IOException e ) {
			if ( !throwOnError ) {
				handleError( "Connect Exception: " + e.getMessage(), "Connection Failed" );
			} else {
				throw newRunTimeException( "Connect Exception: " + e.getMessage() );
			}
			return;
		}

		// if status code is -1
		if ( statusCode != -1 )
			handleResponse( response, statusCode );

	}



	private void setDefaultProxy() {
		HttpHost currentProxy = ( HttpHost )client.getParams().getParameter( ConnRoutePNames.DEFAULT_PROXY );

		if ( currentProxy == null ) {
			ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner( client.getConnectionManager().getSchemeRegistry(), ProxySelector.getDefault() );
			client.setRoutePlanner( routePlanner );
		}
	}



	private void handleResponse( HttpResponse response, int _statusCode ) throws cfmRunTimeException {
		// if the status code is not 2xx and throwOnError is true then throw an exception
		if ( throwOnError && ( _statusCode < 200 || _statusCode > 299 ) ) {
			throw newRunTimeException( _statusCode + " " + response.getStatusLine().getReasonPhrase() );
		}

		// Read response headers
		Header[] respHeaders = response.getAllHeaders();
		cfStructData responseHeaders = new cfStructData();
		StringBuilder allHeaders = new StringBuilder();
		String mimeType = "";
		Header nextHeader;

		// loop through the response headers creating the necessary cfStructData
		// if there are any duplicate headers then an array of the values is created
		for ( int i = 0; i < respHeaders.length; i++ ) {
			nextHeader = respHeaders[i];
			if ( responseHeaders.containsKey( nextHeader.getName() ) ) {
				cfData headerVals = responseHeaders.getData( nextHeader.getName() );

				if ( headerVals.getDataType() == cfData.CFSTRUCTDATA ) {
					( ( cfStructData )headerVals ).setData( String.valueOf( ( ( cfStructData )headerVals ).size() + 1 ), new cfStringData( nextHeader.getValue() ) );
				} else {
					cfStructData newHeaderVals = new cfStructData();
					newHeaderVals.setData( "1", headerVals );
					newHeaderVals.setData( "2", new cfStringData( nextHeader.getValue() ) );
					responseHeaders.setData( nextHeader.getName(), newHeaderVals );
				}
			} else {
				responseHeaders.setData( nextHeader.getName(), new cfStringData( nextHeader.getValue() ) );
			}

			allHeaders.append( nextHeader );
			if ( nextHeader.getName().toLowerCase().equals( "content-type" ) )
				mimeType = nextHeader.getValue();
		}
		httpData.setData( "responseheader", responseHeaders );

		StatusLine httpStatus = response.getStatusLine();
		responseHeaders.setData( "HTTP_VERSION", new cfStringData( httpStatus.getProtocolVersion().toString() ) );
		responseHeaders.setData( "EXPLANATION", new cfStringData( httpStatus.getReasonPhrase() ) );
		responseHeaders.setData( "STATUS_CODE", new cfNumberData( _statusCode ) );

		httpData.setData( "header", new cfStringData( httpStatus.toString() + " " + allHeaders.toString() ) );

		httpData.setData( "mimetype", new cfStringData( mimeType ) );

		int charsetIndex = mimeType.toLowerCase().indexOf( "charset" );
		String charsetFromHeader = null;
		if ( charsetIndex != -1 ) {
			charsetFromHeader = mimeType.substring( charsetIndex + 8 ).trim();
			httpData.setData( "charset", new cfStringData( charsetFromHeader ) );
		} else {
			httpData.setData( "charset", new cfStringData( "" ) );
		}

		byte[] responseBody = getResponseBody( response, outFile );
		if ( responseBody == null ) {
			throw newRunTimeException( "Failed to read response body." );
		}

		cfData fileContent = null;
		byte resultType = getResultType( mimeType );
		switch (resultType) {
			case OBJECT:
				if ( outFile == null ) {
					java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
					bos.write( responseBody, 0, responseBody.length );
					fileContent = new cfJavaObjectData( bos );
					break;
				}
			case BINARY:
				if ( outFile == null )
					fileContent = new cfBinaryData( responseBody );
				else
					fileContent = new cfStringData( new String( responseBody ) );

				break;
			case STRING:
				String fileContentStr = getAsString( responseBody, charsetFromHeader );
				if ( resolveLinks ) {
					fileContentStr = resolveLinks( fileContentStr );
				}
				fileContent = new cfStringData( fileContentStr );
				break;
		}

		httpData.setData( "filecontent", fileContent );

		// if binary data then we ignore
		if ( query != null && fileContent.getDataType() == cfData.CFSTRINGDATA ) {
			handleQuery( fileContent.getString() );
		}

		httpData.setData( "statuscode", new cfStringData( _statusCode + " " + httpStatus.getReasonPhrase() ) );

		httpData.setData( "errordetail", new cfStringData( "" ) );
		httpData.setData( "text", fileContent.getDataType() == cfData.CFSTRINGDATA ? cfBooleanData.TRUE : cfBooleanData.FALSE );

		cleanHttpData();
	}



	@Override
	public void close() throws IOException {
		client.getConnectionManager().shutdown();
	}



	// Private Methods
	private HttpUriRequest resolveMethod( String _method, boolean _multipart ) throws cfmRunTimeException {
		String method = _method.toUpperCase();
		if ( method.equals( "GET" ) ) {
			return new HttpGet();
		} else if ( method.equals( "POST" ) ) {
			return new HttpPost();
		} else if ( method.equals( "HEAD" ) ) {
			return new HttpHead();
		} else if ( method.equals( "TRACE" ) ) {
			return new HttpTrace();
		} else if ( method.equals( "DELETE" ) ) {
			return new HttpDelete();
		} else if ( method.equals( "OPTIONS" ) ) {
			return new HttpOptions();
		} else if ( method.equals( "PUT" ) ) {
			return new HttpPut();
		} else if ( method.equals( "PATCH" ) ) {
			return new HttpPatch();
		}
		throw newRunTimeException( "Unsupported METHOD value [" + method + "]. Valid METHOD values are GET, POST, HEAD, TRACE, DELETE, OPTIONS, PATCH and PUT." );
	}



	private void addURLData() throws cfmRunTimeException {
		addQueryStringData( httpData.getURLData(), httpData.getCharset() );
	}



	private void addQueryStringData( Map<String, String> _data, String _charset ) throws cfmRunTimeException {

		if ( _data.size() > 0 ) { // don't need to do anything if there's no url data
															// to add
			StringBuilder queryString = new StringBuilder(); // method.getQueryString()
																												// );
			Iterator<String> keys = _data.keySet().iterator();
			while ( keys.hasNext() ) {
				String nextKey = keys.next();
				try {
					queryString.append( urlEncoder.encode( nextKey, _charset ) );
				} catch ( UnsupportedEncodingException e1 ) {
					queryString.append( urlEncoder.encode( nextKey ) );
				}
				queryString.append( '=' );
				try {
					queryString.append( urlEncoder.encode( _data.get( nextKey ), _charset ) );
				} catch ( UnsupportedEncodingException e ) {
					queryString.append( urlEncoder.encode( _data.get( nextKey ) ) );
				}
				queryString.append( "&" );
			}
			// remove last &. We know there is at least one url param
			queryString = queryString.deleteCharAt( queryString.length() - 1 );
			String currentQStr = message.getURI().getQuery();
			if ( currentQStr == null )
				currentQStr = "";
			try {
				URI uri = message.getURI();
				String schemeName = uri.getScheme();
				String hostName = uri.getHost();
				int port = uri.getPort();
				String fragment = uri.getFragment();
				String path = uri.getPath();
				String queryStr = queryString.toString();

				if ( currentQStr.length() > 0 ) {
					uri = URIUtils.createURI( schemeName, hostName, port, path, currentQStr + '&' + queryStr, fragment );
				} else {
					uri = URIUtils.createURI( schemeName, hostName, port, path, queryStr, fragment );
				}

				( ( HttpRequestBase )message ).setURI( uri );

			} catch ( URISyntaxException e ) {
				throw newRunTimeException( "Failed due to URI Syntax Error: " + e.getMessage() );
			}

		}

	}



	private void addFormData() throws cfmRunTimeException {
		Map<String, String> formData = httpData.getFormData();
		Iterator<String> keys = formData.keySet().iterator();
		String nextKey;

		if ( message.getMethod().equalsIgnoreCase( "POST" ) || message.getMethod().equalsIgnoreCase( "PATCH" ) ) {

			if ( isMultipart || httpData.getFiles().size() > 0 ) {

				if ( multipartEntityBuilder == null )
					multipartEntityBuilder	= MultipartEntityBuilder.create().setCharset( charset );

				while ( keys.hasNext() ) {
					nextKey = keys.next();
					multipartEntityBuilder.addPart( nextKey, new StringBody( formData.get( nextKey ), ContentType.TEXT_PLAIN ) );
				}

			} else {

				Header contentType = message.getFirstHeader( "Content-type" );
				if ( contentType == null ) {
					// otherwise it's been set manually so don't override it
					message.setHeader( "Content-type", "application/x-www-form-urlencoded" );
				}

				StringBuilder paramStr = new StringBuilder();
				while ( keys.hasNext() ) {
					nextKey = keys.next();
					paramStr.append( nextKey );
					paramStr.append( '=' );
					paramStr.append( formData.get( nextKey ) );
					paramStr.append( '&' );
				}
				if ( paramStr.length() > 0 ) {
					paramStr.deleteCharAt( paramStr.length() - 1 ); // remove last &
					( ( HttpPost )message ).setEntity( new StringEntity( paramStr.toString(), this.charset ) );
				}

			}
		}
	}



	private void addFiles() throws cfmRunTimeException {
		List<fileDescriptor> files = httpData.getFiles();

		if ( files.size() > 0 ) {

			if ( message instanceof HttpPost && ( isMultipart || httpData.getFiles().size() > 0 ) ) {

				if ( multipartEntityBuilder == null )
					multipartEntityBuilder	= MultipartEntityBuilder.create().setCharset( charset );

				for ( int i = 0; i < files.size(); i++ ) {
					fileDescriptor nextFile = files.get( i );
					multipartEntityBuilder.addPart( nextFile.getName(), new FileBody( nextFile.getFile(), ContentType.create( nextFile.getMimeType() ), nextFile.getFile().getName() ) );
				}

			} else if ( message instanceof HttpPut ) {
				fileDescriptor nextFile = files.get( 0 ); // just use the first file specified
				try {
					FileInputStream fileIn = new FileInputStream( nextFile.getFile() );
					InputStreamEntity entity = new InputStreamEntity( fileIn, nextFile.getFile().length(), ContentType.create( nextFile.getMimeType() ) );
					( ( HttpPut )message ).setEntity( entity );
				} catch ( FileNotFoundException e ) {
					throw newRunTimeException( "Failed to locate file " + nextFile.getFile().getAbsolutePath() );
				}

			}  else if ( message instanceof HttpPatch ) {
				fileDescriptor nextFile = files.get( 0 ); // just use the first file specified
				try {
					FileInputStream fileIn = new FileInputStream( nextFile.getFile() );
					InputStreamEntity entity = new InputStreamEntity( fileIn, nextFile.getFile().length(), ContentType.create( nextFile.getMimeType() ) );
					( ( HttpPatch )message ).setEntity( entity );
				} catch ( FileNotFoundException e ) {
					throw newRunTimeException( "Failed to locate file " + nextFile.getFile().getAbsolutePath() );
				}

			}
		}
	}



	/**
	 * Reads the response from the remote site
	 *
	 * @param _response
	 * @param gzip
	 * @param file
	 * @return
	 */
	private byte[] getResponseBody( HttpResponse _response, File file ) {
		Header contentLenHdr = _response.getFirstHeader( "Content-Length" );

		int expected = StringUtil.toInteger( (contentLenHdr != null ) ? contentLenHdr.getValue() : null, 4096 );

		try {
			if ( _response.getEntity() != null ) { // response body might be null if there is no body e.g. HEAD method used
				InputStream in = _response.getEntity().getContent();

				if ( file != null ) {
					FileOutputStream out = new FileOutputStream( file );
					try {
						StreamUtil.copyTo( in, out );
					} finally {
						out.close();
					}
					return new String( file.getAbsolutePath() ).getBytes();
				} else {
					ByteArrayOutputStream out = new ByteArrayOutputStream( expected );
					StreamUtil.copyTo( in, out );
					return out.toByteArray();
				}

			} else {
				return new byte[0];
			}
		} catch ( IOException ioe ) {
			return null;
		}
	}



	private void addHeaders() {

		// There is no need to add HOST header since this is done by the
		// "Jakarta Commons-HttpClient" HttpClient class.

		message.addHeader( "Connection", "close" );

		Map<String, String> headers = httpData.getHeaders();
		Iterator<String> keys = headers.keySet().iterator();
		boolean addUserAgent = true;

		while ( keys.hasNext() ) {
			String nextKey = keys.next();
			message.addHeader( nextKey, headers.get( nextKey ) );
			if ( nextKey.equalsIgnoreCase( "User-Agent" ) ) {
				addUserAgent = false;
			}
		}

		if ( addUserAgent )
			message.addHeader( "User-agent", useragent );

		// only set the content-type if it hasn't already been set, thus allowing for it to be overridden
		if ( httpData.isBodySet() && message.getLastHeader( "Content-type" ) == null )
			message.setHeader( "Content-type", httpData.getContentType() );
	}



	private void addCookies() {

		Map<String, List<String>> cookies = httpData.getCookies();
		Iterator<String> keys = cookies.keySet().iterator();
		String domain = "";
		domain = message.getURI().getHost();
		CookieStore cookieStore = new BasicCookieStore();

		HttpContext localContext = new BasicHttpContext();

		// Bind custom cookie store to the local context
		localContext.setAttribute( ClientContext.COOKIE_STORE, cookieStore );
		client.getParams().setParameter( ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY );

		while ( keys.hasNext() ) {
			String nextKey = keys.next();
			List<String> values = cookies.get( nextKey );
			Date date = new Date( 2038, 1, 1 );
			for ( int i = 0; i < values.size(); i++ ) {
				BasicClientCookie cookie = new BasicClientCookie( nextKey, values.get( i ) );
				cookieStore.addCookie( cookie );
				cookie.setVersion( 1 );
				cookie.setDomain( domain );
				cookie.setPath( "/" );
				cookie.setExpiryDate( date );
				cookie.setSecure( false );
			}
		}
		client.setCookieStore( cookieStore );
	}



	public void setBody() throws cfmRunTimeException {
		// if there is a request body set and this is a PUT, PATCH or POST
		if ( httpData.isBodySet() ) {
			try {
				if ( message instanceof HttpPost ) {
					if ( this.charset == null ) {
						( (HttpPost) message ).setEntity( new StringEntity( httpData.getBody() ) );
					} else {
						( (HttpPost) message ).setEntity( new StringEntity( httpData.getBody(), this.charset ) );
					}

				} else if ( message instanceof HttpPut ) {
					if ( this.charset == null ) {
						( (HttpPut) message ).setEntity( new StringEntity( httpData.getBody() ) );
					} else {
						( (HttpPut) message ).setEntity( new StringEntity( httpData.getBody(), this.charset ) );
					}

				}
				 else if ( message instanceof HttpPatch ) {
						if ( this.charset == null ) {
							( (HttpPatch) message ).setEntity( new StringEntity( httpData.getBody() ) );
						} else {
							( (HttpPatch) message ).setEntity( new StringEntity( httpData.getBody(), this.charset ) );
						}

					}
			} catch ( UnsupportedEncodingException | UnsupportedCharsetException e ) {
				throw newRunTimeException( "Failed due to UnsupportedEncoding while setting body: " + e.getMessage() );
			}
		}
	}

	protected String				url;

	protected int						port;

	protected Charset				charset;

	protected boolean				resolveLinks;

	protected boolean				followRedirects;

	protected String				useragent;

	protected cfSession			session;

	protected boolean				throwOnError;

	protected String				getAsBinary;

	protected File					outFile	= null;

	protected queryDetails	query		= null;

	protected cfHttpData		httpData;

	protected String				proxyServer;

	protected int						proxyPort;

	protected final static byte	STRING	= 0, BINARY = 1, OBJECT = 2;



	protected void init( cfSession _session, cfHttpData _httpData ) {
		session = _session;
		httpData = _httpData;
	}



	protected void handleQuery( String _page ) throws cfmRunTimeException {
		if ( query == null )
			return; // shouldn't happen but check anyway

		if ( query.columns == null && _page.trim().length() == 0 ) {
			throw newRunTimeException( "Cannot create Query. Empty file returned in HTTP call with no COLUMNS specified." );
		}

		if ( query.name.length() == 0 ) { // no need to create query with no name
			return;
		}

		if ( query.delimiter.length() != 1 ) {
			throw newRunTimeException( "Invalid delimiter value. Delimiter must be one character in length." );
		}
		char delimiter = query.delimiter.charAt( 0 );

		if ( query.textqualifier.length() > 1 ) {
			throw newRunTimeException( "Invalid textQualifier set. TextQualifier must be 0 or 1 character in length." );
		}

		// get columns and create the query

		// columns are given in the tag as COLUMNS parameter
		new cfHttpQueryData( session, _page, query.columns, query.name, delimiter, query.textqualifier, false, query.firstRowAsHeaders );
	}



	protected void saveToFile( cfData _filecontent ) throws cfmRunTimeException {
		try {
			FileOutputStream fout = new FileOutputStream( outFile );
			if ( _filecontent.getDataType() == cfData.CFSTRINGDATA ) {
				fout.write( _filecontent.getString().getBytes() );
			} else if ( _filecontent.getDataType() == cfData.CFBINARYDATA ) {
				fout.write( ( ( cfBinaryData )_filecontent ).getByteArray() );
			}
			fout.flush();
			fout.close();
		} catch ( IOException ioe ) {
			throw newRunTimeException( "Failed to save filecontent to file. IOException: " + ioe );
		}
	}



	protected String resolveLinks( String _content ) {
		tagFilterReader reader = new tagFilterReader( new StringReader( _content ) );
		reader.registerTagFilter( new urlResolver( url, port ) );
		java.io.StringWriter writer = new java.io.StringWriter();
		try {

			int ch = reader.readChar();
			while ( ch != -1 ) {
				writer.write( ch );
				ch = reader.readChar();
			}
			return writer.toString();
		} catch ( IOException ignored ) {
		} // won't happen since the source is a String

		return _content;
	}



	protected cfmRunTimeException newRunTimeException( String _msg ) {
		cfCatchData catchData = new cfCatchData( session );
		catchData.setMessage( _msg );
		return new cfmRunTimeException( catchData );
	}

	private class queryDetails {

		String	name;

		String	columns;

		String	textqualifier;

		String	delimiter;

		boolean	firstRowAsHeaders;



		public queryDetails( String _name, String _cols, String _tq, String _delim, boolean _first ) {
			name = _name;
			columns = _cols;
			textqualifier = _tq;
			delimiter = _delim;
			firstRowAsHeaders = _first;
		}
	}



	@Override
	public void setQueryDetails( String _name, String _cols, String _textqualifier, String _delimiter, boolean _first ) {
		query = new queryDetails( _name, _cols, _textqualifier, _delimiter, _first );
	}



	@Override
	public void setThrowOnError( boolean b ) {
		throwOnError = b;
	}



	@Override
	public void setFile( File _out ) {
		outFile = _out;
	}



	@Override
	public void setCharset( String _charset ) throws cfmRunTimeException {
		try {
			charset = Charset.forName( _charset );
		} catch ( java.nio.charset.IllegalCharsetNameException e ) {
			throw newRunTimeException( "Invalid CHARSET value specified: " + _charset + "." );
		}
	}



	@Override
	public void setUserAgent( String _ua ) {
		useragent = _ua;
	}



	protected void cleanHttpData() {
		// remove headers, data, files
		httpData.removeData();
		httpData.removeHeaders();
		httpData.removeFiles();
		httpData.removeCookies();
	}



	protected static boolean isText( String _contentType ) {
		if ( _contentType.equals( "" ) || _contentType.startsWith( "text" ) || _contentType.startsWith( "message" ) || _contentType.equals( "application/octet-stream" ) || _contentType.equals( "application/xml" ) || _contentType.equals( "application/json" ) ) {
			return true;
		} else {
			return false;
		}
	}



	protected void handleError( String _error, String _content ) {
		httpData.setData( "charset", new cfStringData( "" ) );
		httpData.setData( "errordetail", new cfStringData( _error ) );
		httpData.setData( "filecontent", new cfStringData( _content ) );
		httpData.setData( "header", new cfStructData() );
		httpData.setData( "mimetype", new cfStringData( "Unable to determine MIME type of file" ) );
		httpData.setData( "responseheader", new cfStructData() );
		httpData.setData( "statuscode", new cfStringData( "Status code unavailable" ) );
		httpData.setData( "text", cfBooleanData.TRUE );
	}



	protected byte getResultType( String _contentType ) throws cfmRunTimeException {
		if ( getAsBinary.equals( "yes" ) ) {
			return BINARY;
		} else if ( getAsBinary.equals( "no" ) ) {
			if ( isText( cleanContentType( _contentType ) ) ) {
				return STRING;
			} else {
				return OBJECT;
			}
		} else if ( getAsBinary.equals( "auto" ) ) {
			if ( isText( cleanContentType( _contentType ) ) ) {
				return STRING;
			} else {
				return BINARY;
			}
		} else {
			throw newRunTimeException( "Invalid GETASBINARY value specified: " + getAsBinary + ". Supported values are NO, YES and AUTO." );
		}
	}



	private static String cleanContentType( String _contentType ) {
		int semiColonIndx = _contentType.indexOf( ';' );
		if ( semiColonIndx != -1 ) {
			return _contentType.substring( 0, semiColonIndx );
		}
		return _contentType;
	}



	@Override
	public void setGetAsBinary( String _gab ) {
		getAsBinary = _gab;
	}



	@Override
	public void setResolveLinks( boolean _resolve ) {
		resolveLinks = _resolve;
	}



	private String getAsString( byte[] _body, String _charsetFromHeader ) throws cfmRunTimeException {
		if ( _body == null ) {
			return "";
		}

		String useCharset;
		if ( charset == null ) {
			useCharset = _charsetFromHeader;
		} else {
			useCharset = charset.toString();
		}

		String fileContentStr;
		if ( useCharset != null ) {
			try {
				fileContentStr = new String( _body, com.nary.util.Localization.convertCharSetToCharEncoding( useCharset ) );
			} catch ( UnsupportedEncodingException ue ) {
				throw newRunTimeException( "Unsupported charset specified: " + useCharset );
			}
		} else {
			fileContentStr = new String( _body );
		}
		return fileContentStr;
	}

}
