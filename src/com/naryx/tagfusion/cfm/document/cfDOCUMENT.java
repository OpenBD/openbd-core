/*
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

package com.naryx.tagfusion.cfm.document;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.aw20.io.StreamUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.DefaultPDFCreationListener;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.PDFEncryption;
import org.xml.sax.InputSource;

import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;
import com.nary.io.FileUtils;
import com.nary.io.multiOutputStream;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.tag.cfOptionalBodyTag;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.cfm.tag.tagLocator;
import com.naryx.tagfusion.cfm.tag.tagReader;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.cfm.xml.parse.NoValidationResolver;
import com.naryx.tagfusion.xmlConfig.xmlCFML;


public class cfDOCUMENT extends cfTag implements cfOptionalBodyTag, Serializable {

	private static final long serialVersionUID = 1;

  private static final String TAG_NAME = "CFDOCUMENT";
  private String endMarker = null;

  public static final String CFDOCUMENT_KEY = "CFDOCUMENT";
  
	private static String[] defaultWindowsFontDirs = { "C:\\Windows\\Fonts", "C:\\WINNT\\Fonts" };
	private static String[] defaultOtherFontDirs = { "/usr/X/lib/X11/fonts/TrueType", "/usr/openwin/lib/X11/fonts/TrueType",
													 "/usr/share/fonts/default/TrueType", "/usr/X11R6/lib/X11/fonts/ttf",
													 "/usr/X11R6/lib/X11/fonts/truetype", "/usr/X11R6/lib/X11/fonts/TTF" };

	private static String [] defaultFontDirs;

	public static void init( xmlCFML configFile ) {
		String fontDirs = cfEngine.getConfig().getString( "server.fonts.dirs", "" );
		
		if ( fontDirs.length() == 0 ) { // no fonts configured, set defaults
			StringBuilder defaultFontDirsList = new StringBuilder();
			if ( cfEngine.WINDOWS ) {
				for ( int i = 0; i < defaultWindowsFontDirs.length; i++ ) {
					if ( FileUtils.exists( defaultWindowsFontDirs[ i ] ) ) {
						if ( defaultFontDirsList.length() > 0 ) { // not the first
							defaultFontDirsList.append( ',' );
						}
						defaultFontDirsList.append( defaultWindowsFontDirs[ i ] );
					}
				}
			} else {
				for ( int i = 0; i < defaultOtherFontDirs.length; i++ ) {
					if ( FileUtils.exists( defaultOtherFontDirs[ i ] ) ) {
						if ( defaultFontDirsList.length() > 0 ) { // not the first
							defaultFontDirsList.append( ',' );
						}
						defaultFontDirsList.append( defaultOtherFontDirs[ i ] );
					}
				}
			}
			
			if ( defaultFontDirsList.length() > 0 ) {
				cfEngine.getConfig().setData( "server.fonts.dirs", defaultFontDirsList.toString() );
			}
			fontDirs = defaultFontDirsList.toString();
		}
		
		
		defaultFontDirs = fontDirs.split(",");
		for ( int i = 0; i < defaultFontDirs.length; i++ ){
			FontFactory.registerDirectory( defaultFontDirs[i].toString() );
		}
		
	}

	public boolean doesTagHaveEmbeddedPoundSigns(){
		return false;
	}

	public String getEndMarker(){
		return endMarker;  
	}
	
  public void setEndTag() {
    endMarker = null;
  }

  public void lookAheadForEndTag(tagReader inFile) {
    endMarker = (new tagLocator(TAG_NAME, inFile)).findEndMarker();
  }

	/*
	TODO: these attributes are still to be supported
	fontEmbed = "yes|no|selective" // use ITextResolver
	bookmark = "yes|no"
	scale = "percentage less than 100"
	localUrl = "yes|no"
	*/
	
	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute( "ENCRYPTION", "none" );
		defaultAttribute( "FORMAT", "PDF" );
		defaultAttribute( "FONTEMBED", "true" );
		defaultAttribute( "MIMETYPE", "text/html" );
		defaultAttribute( "ORIENTATION", "PORTRAIT" );
		defaultAttribute( "OVERWRITE", "false" );		
		defaultAttribute( "PAGETYPE", "letter" );
		defaultAttribute( "UNIT", "IN" );
		defaultAttribute( "USERAGENT", "OpenBD" );
		defaultAttribute( "BACKGROUNDVISIBLE", "true" );
		
		parseTagHeader( _tag );
	}
	

	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {		
		
		if ( !getDynamic( _Session, "FORMAT" ).getString().equalsIgnoreCase( "PDF" ) ){
			throw newRunTimeException( "Invalid FORMAT value. Only \"PDF\" is supported." );
		}
		
		if ( containsAttribute( "SRC" ) && containsAttribute( "SRCFILE" ) ){
			throw newRunTimeException( "Invalid attribute combination. Either the SRC or SRCFILE attribute must be specified but not both" );
		}
		
		ITextRenderer renderer = new ITextRenderer();
		CreationListener listener = new CreationListener(getDynamic(_Session, "AUTHOR"),getDynamic(_Session, "TITLE"),getDynamic(_Session, "SUBJECT"),getDynamic(_Session, "KEYWORDS"));
		renderer.setListener(listener);
		resolveFonts( _Session, renderer );
		
		if ( _Session.getDataBin( CFDOCUMENT_KEY ) != null ){
			throw newRunTimeException( "CFDOCUMENT cannot be embedded within another CFDOCUMENT tag" );
		}
		
		_Session.setDataBin( CFDOCUMENT_KEY, new DocumentContainer() );

		String renderedBody = renderToString( _Session ).getOutput();
		try{
			DocumentContainer container = (DocumentContainer) _Session.getDataBin( CFDOCUMENT_KEY );
			
			List<DocumentSection> sections = container.getSections(); 
			if ( sections.size() == 0 ){
				// if no sections are specified then construct one from this tag
				DocumentSection section = new DocumentSection();
				section.setHeader( container.getMainHeader(), container.getMainHeaderAlign() );
				section.setFooter( container.getMainFooter(), container.getMainFooterAlign() );
				
				if ( renderedBody.length() == 0 && !( containsAttribute( "SRC" ) || containsAttribute( "SRCFILE" ) ) ){
					throw newRunTimeException( "Cannot create a PDF from an empty document!" );	
				}
				
				String src = containsAttribute( "SRC" ) ? getDynamic( _Session, "SRC" ).getString() : null;
				String srcFile = containsAttribute( "SRCFILE" ) ? getDynamic( _Session, "SRCFILE" ).getString() : null;
				
				section.setSources( src, srcFile, renderedBody );
				appendSectionAttributes( _Session, section );
				
				sections.add( section );
			}

			DocumentSettings settings = getDocumentSettings( _Session, container );

			// If there is more than 1 section and page counters are used that need special
			// processing then we need to do an initial conversion of the HTML to PDF to
			// determine how many pages are created per section and how many pages are created total.
			if ((sections.size() > 1) && (container.usesTotalPageCounters()))
				preparePageCounters( _Session, renderer, sections, settings );

			preparePDF( _Session, renderer, sections, settings );
	
			return cfTagReturnType.NORMAL;
		}finally{
			_Session.deleteDataBin( CFDOCUMENT_KEY );
		}

	}
	
	
	private void resolveFonts( cfSession _Session, ITextRenderer _renderer ) throws dataNotSupportedException, cfmRunTimeException{
		ITextFontResolver resolver = _renderer.getFontResolver();
		
		boolean embed = getDynamic( _Session, "FONTEMBED" ).getBoolean();
		for ( int i = 0; i < defaultFontDirs.length; i++ ){
			File nextFontDir = new File( defaultFontDirs[i] );
			File[] fontFiles = nextFontDir.listFiles( new FilenameFilter() {
				public boolean accept( File _dir, String _name ){
					String name = _name.toLowerCase();
					return name.endsWith( ".otf" ) || name.endsWith( ".ttf" );
				}
      });
			if ( fontFiles != null ){
	      for ( int f = 0; f < fontFiles.length; f++ ){
	      	try{
	      		resolver.addFont( fontFiles[f].getAbsolutePath(), BaseFont.IDENTITY_H,
	  	          embed );
	      	}catch( Exception ignored ){} // ignore fonts that can't be added
	      }
			}
		}
	}
	
	/*
	 * preparePageCounters
	 * 
	 * This method is expensive because it requires us to convert the HTML to PDF to
	 * determine the total page counters. This method should only be called if there is more
	 * than one section and one of the following page counter variables is being used:
	 * 
	 *   1. TotalPageCount
	 *   2. TotalSectionPageCount
	 */
	private void preparePageCounters( cfSession _Session, ITextRenderer _renderer, List<DocumentSection> _sections, DocumentSettings _settings ) throws cfmRunTimeException{
		OutputStream pdfOut = null;
		try{
			pdfOut = new NullOutputStream();
			
			DocumentSection nextSection = _sections.get( 0 );
			if (nextSection.pageCounterConflict())
				throw newRunTimeException("OpenBD doesn't support currentpagenumber and currentsectionpagenumber in same section.");		
			String renderedBody = getRenderedBody( _Session, nextSection, _settings, _sections.size() );
			_renderer.setDocument( getDocument( renderedBody ), nextSection.getBaseUrl( _Session ) );
			_renderer.layout();
			_renderer.createPDF( pdfOut, false );
			
			int currentPageNumber = _renderer.getWriter().getCurrentPageNumber();
			nextSection.setTotalSectionPageCount(currentPageNumber);
			int totalPageCount = currentPageNumber;

			for ( int i = 1; i < _sections.size(); i++ ){
				nextSection = _sections.get( i );
				if (nextSection.pageCounterConflict())
					throw newRunTimeException("OpenBD doesn't support currentpagenumber and currentsectionpagenumber in same section.");
				renderedBody = getRenderedBody( _Session, nextSection, _settings, _sections.size() );
				_renderer.setDocument( getDocument( renderedBody ), nextSection.getBaseUrl( _Session ) );
				_renderer.layout();
				_renderer.writeNextDocument( _renderer.getWriter().getCurrentPageNumber()+1 );
				currentPageNumber = _renderer.getWriter().getCurrentPageNumber();
				nextSection.setTotalSectionPageCount(currentPageNumber-totalPageCount);
				totalPageCount = currentPageNumber;
			}
			
			for ( int i = 0; i < _sections.size(); i++ ){
				nextSection = _sections.get( i );
				nextSection.setTotalPageCount(totalPageCount);
			}
		
		} catch (DocumentException e) {
			throw newRunTimeException( "Failed to create PDF due to DocumentException: " + e.getMessage() );
		}finally{
			if ( pdfOut != null )try{ pdfOut.close(); }catch( IOException ignored ){}
		}
	}

	private void preparePDF( cfSession _Session, ITextRenderer _renderer, List<DocumentSection> _sections, DocumentSettings _settings ) throws cfmRunTimeException{
		OutputStream pdfOut = null;
		try{
			ByteArrayOutputStream bos = null;
			
			if ( containsAttribute( "FILENAME" ) ){
				File pdfFile = new File( getDynamic( _Session, "FILENAME" ).getString() );
				if ( pdfFile.exists() && !getDynamic( _Session, "OVERWRITE" ).getBoolean() ){
					throw newRunTimeException( "PDF file already exists and overwrite is disabled." );
				}
				pdfOut = cfEngine.thisPlatform.getFileIO().getFileOutputStream(pdfFile);

				if ( containsAttribute( "NAME" ) ){
					bos = new ByteArrayOutputStream();
					pdfOut = new multiOutputStream( pdfOut, bos );
				}
			}else if ( containsAttribute( "NAME" ) ){
				pdfOut = new ByteArrayOutputStream();

			}else{
				_Session.resetBuffer();

				// The SAVEASNAME attribute as been tested with the following:
				//
				//    IE8 - Page/Save As : FAILS
				//    IE8 - PDF plugin save image : FAILS
				//    Firefox 3.5.8 - File/Save Page As : WORKS
				//    Firefox 3.5.8 - PDF plugin save image : FAILS
				//
				String saveAsName;
				if ( containsAttribute( "SAVEASNAME" ) ){
					saveAsName = getDynamic( _Session, "SAVEASNAME" ).toString();
				} else {
					// Extract the filename from the path and use it as the save as name
					saveAsName = _Session.REQ.getServletPath();
					int slash = saveAsName.lastIndexOf('/');
					if ( slash != -1 )
						saveAsName = saveAsName.substring(slash+1);
					int dot = saveAsName.lastIndexOf('.');
					if ( dot != -1 )
						saveAsName = saveAsName.substring(0,dot) + ".pdf";
				}
				
				pdfOut = new SessionOutputStream( _Session, saveAsName );					
			}
			
			// handle encryption/password if attributes are set
			if ( containsAttribute("OWNERPASSWORD") || containsAttribute("USERPASSWORD") || containsAttribute("PERMISSIONS") ||
			     !getDynamic( _Session, "ENCRYPTION" ).getString().equalsIgnoreCase("none")){
				PDFEncryption mEnc = new PDFEncryption();
				setPermissions( _Session, mEnc );
				_renderer.setPDFEncryption( mEnc );
			}
			
			DocumentSection nextSection = _sections.get( 0 );
			String renderedBody = getRenderedBody( _Session, nextSection, _settings, _sections.size() );
			_renderer.setDocument( getDocument( renderedBody ), nextSection.getBaseUrl( _Session ) );
			_renderer.layout();
			_renderer.createPDF( pdfOut, false );
			
			for ( int i = 1; i < _sections.size(); i++ ){
				nextSection = _sections.get( i );
				renderedBody = getRenderedBody( _Session, nextSection, _settings, _sections.size() );
				_renderer.setDocument( getDocument( renderedBody ), nextSection.getBaseUrl( _Session ) );
				_renderer.layout();
				if ( nextSection.usesCurrentPageNumber() ) {
					// uses currentpagenumber so start page numbering with current page number + 1
					_renderer.writeNextDocument( _renderer.getWriter().getCurrentPageNumber() + 1 );
				} else {
					// uses currentsectionpagenumber so start page numbering with 1
					_renderer.writeNextDocument( 1 );
				}
			}
			
			_renderer.finishPDF();
			
			if ( pdfOut instanceof SessionOutputStream ){
				if( ( (SessionOutputStream) pdfOut).getException() != null ){
					throw ( (SessionOutputStream) pdfOut).getException();
				}
				_Session.pageFlush();
				_Session.abortPageProcessing();
			}
			
			// Add the data to our session
			if ( containsAttribute( "NAME" ) )
			{
				if ( bos != null )
					_Session.setData(getDynamic(_Session, "NAME").toString(), new cfBinaryData(bos.toByteArray()));
				else
					_Session.setData(getDynamic(_Session, "NAME").toString(), new cfBinaryData(((ByteArrayOutputStream)pdfOut).toByteArray()));
			}
			
		} catch (DocumentException e) {
			throw newRunTimeException( "Failed to create PDF due to DocumentException: " + e.getMessage() );
		} catch ( IOException e ) {
			throw newRunTimeException( "Error writing PDF to file. Check the file exists and can be written to." );
		}finally{
			if ( pdfOut != null )try{ pdfOut.close(); }catch( IOException ignored ){}
		}
	}
	
	
	private String getRenderedBody( cfSession _Session, DocumentSection _section, DocumentSettings _settings, int _numSections ) throws cfmRunTimeException{
		String renderedBody = _section.getBody();
		if ( renderedBody.length() != 0 ){
			// If the section had a mimetype specified and it's text/plain or if it didn't have a mimetype
			// specified and the document mimetype was set to text/plain then treat the body as plain text.
			if ((( _section.getMimeType() != null ) && (_section.getMimeType().equalsIgnoreCase("text/plain"))) ||
			    (( _section.getMimeType() == null ) && (getDynamic(_Session,"MIMETYPE").getString().equalsIgnoreCase("text/plain"))))
				renderedBody = getXHTML( "<pre>" + escapeHtmlChars(renderedBody) + "</pre>" );
			else
				renderedBody = getXHTML( renderedBody );
		}else{
			renderedBody = getXHTML( retrieveDocument( _Session, _section, _settings ) );
		}
		renderedBody = insertStyles( _Session, renderedBody, _section, _settings, getDynamic(_Session, "BACKGROUNDVISIBLE").getBoolean(), _numSections );

		return renderedBody;
	}
	
	public Document getDocument( String _renderedBody ) throws cfmRunTimeException{
		try{
			DocumentBuilder builder;
			InputSource is = new InputSource( new StringReader( _renderedBody ) );
			Document doc;
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();			
			builderFactory.setValidating( false );
			builder = builderFactory.newDocumentBuilder();
			builder.setEntityResolver( new NoValidationResolver() );
			doc = builder.parse( is );
			return doc;
		} catch (Exception e) {
			throw newRunTimeException( "Failed to create valid xhtml document due to " + e.getClass().getName() + ": " + e.getMessage() );
		}
	}

	
	private String getXHTML( String _html ){
		Tidy tidy = new Tidy();
		tidy.setQuiet( true );
		tidy.setNumEntities( true );
		tidy.setShowWarnings( false );
		StringWriter result = new StringWriter();
		tidy.setMakeClean( true );
		tidy.setXHTML( true );
		tidy.parse( new StringReader( _html ), result );

		return result.toString();
	}

	
	private String retrieveDocument( cfSession _Session, DocumentSection _section, DocumentSettings _settings ) throws dataNotSupportedException, cfmRunTimeException{
		String src = _section.getSrc();
		if ( src != null ){
			// if this is a file:// url then just handle it as a SRCFILE rather
			// than pass it through HttpClient
			if ( src.startsWith( "file://" ) ){
				return retrieveLocalFile( _Session, src.substring( 8 ) );
			}else if ( src.startsWith( "http://") || src.startsWith( "https://") ){
				return retrieveHttp( _Session, src, _section, _settings );
			}else{
				return retrieveHttp( _Session, makeAbsoluteUrl(src, _Session), _section, _settings );
			}
		}else{
			return retrieveLocalFile( _Session, _section.getSrcFile() );
		}
	}
	
	private static String makeAbsoluteUrl(String relativeUrl, cfSession _Session)
	{
		boolean serverRelative = false;
		if (relativeUrl.startsWith("/"))
			serverRelative = true;
		StringBuffer base = new StringBuffer(_Session.REQ.getScheme());
		base.append("://");
		base.append(_Session.REQ.getServerName());
		if (_Session.REQ.getServerPort() != 80)
			base.append(":" + _Session.REQ.getServerPort());
		if (serverRelative)
		{
			base.append(relativeUrl);
		}
		else
		{
			if (_Session.REQ.getContextPath().equals(""))
				base.append("/");
			base.append(_Session.REQ.getContextPath());
			base.append(_Session.REQ.getServletPath().substring(
					0, _Session.REQ.getServletPath().lastIndexOf("/")));
			base.append("/" + relativeUrl);
		}
		return base.toString();		
	}

	private String retrieveLocalFile( cfSession _Session, String _filepath ) throws cfmRunTimeException{
		File file = new File( _filepath );
		if (!file.exists())
			file = new File(_Session.getPresentDirectory(), _filepath);
		FileInputStream fin = null;
		try{
			fin = new FileInputStream( file );
			return handleDocument( _Session, fin, null );
		} catch (FileNotFoundException e) {
			throw newRunTimeException( "Invalid file specified. " + _filepath + " could not be found" );
		} catch (IOException e) {
			throw newRunTimeException( "Failed to read specified file " + _filepath + ". Check the sufficient permissions have been set to permit reading of this file." );
		}finally{
			if ( fin != null )try{ fin.close(); }catch( Exception ignored ){}
		}
	}

	
	private String retrieveHttp( cfSession _Session, String _src, DocumentSection _section, DocumentSettings _defaultSettings ) throws dataNotSupportedException, cfmRunTimeException{
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet method = new HttpGet();
		
		try {
			method.setURI( new URI( _src ) );
			if ( _section.getUserAgent() != null ){
				method.setHeader( "User-Agent", _section.getUserAgent() );
			}else{
				method.setHeader( "User-Agent", _defaultSettings.getUserAgent() );
			}

			// HTTP basic authentication
			if ( _section.getAuthPassword() != null ){ 
				httpClient.getCredentialsProvider().setCredentials( AuthScope.ANY, new UsernamePasswordCredentials( _section.getAuthUser(), _section.getAuthPassword() ) );
			}

			// proxy support
			if ( _defaultSettings.getProxyHost() != null ){
				HttpHost proxy = new HttpHost( _defaultSettings.getProxyHost() , _defaultSettings.getProxyPort() );
				httpClient.getParams().setParameter( ConnRoutePNames.DEFAULT_PROXY, proxy );

				if ( _defaultSettings.getProxyUser() != null ){
					httpClient.getCredentialsProvider().setCredentials( new AuthScope( _defaultSettings.getProxyHost() , _defaultSettings.getProxyPort() ), 
							new UsernamePasswordCredentials( _defaultSettings.getProxyUser(),  _defaultSettings.getProxyPassword() ) );
				}
			}

			HttpResponse response;
			response = httpClient.execute( method );
		
			if ( response.getStatusLine().getStatusCode() == 200 ){
				String charset = null;
				Header contentType = response.getFirstHeader( "Content-type" );
				if ( contentType != null ){
					String value = contentType.getValue();
					int indx = value.indexOf( "charset=" );
					if ( indx > 0 ){
						charset = value.substring( indx+8 ).trim();
					}
				}
				return handleDocument( _Session, response.getEntity().getContent(), charset );
			}else{
				throw newRunTimeException( "Failed to retrieve document from source. HTTP status code " + response.getStatusLine().getStatusCode() + " was returned" );
			}
			
		//	throw newRunTimeException( "Failed to retrieve document from " + _src + " due to HttpException: " + e.getMessage() );
		} catch (URISyntaxException e) {
			throw newRunTimeException( "Error retrieving document via http: " + e.getMessage() );
			
		} catch (IOException e) {
			throw newRunTimeException( "Error retrieving document via http: " + e.getMessage() );
		}	

	}

	private DocumentSettings getDocumentSettings( cfSession _Session, DocumentContainer _container ) throws dataNotSupportedException, cfmRunTimeException{
		DocumentSettings settings = new DocumentSettings();
		
		// get UNIT value 
		String unit = getDynamic( _Session, "UNIT" ).getString().toLowerCase();
		if ( !unit.equals( "in" ) && !unit.equals( "cm" ) ){
			throw newRunTimeException( "Invalid UNIT value. Valid values include \"IN\" and \"CM\"." );
		}
		settings.setUnit( unit );

		// set margins
		if ( containsAttribute( "MARGINTOP" ) ){
			settings.setMarginTop( getDynamic( _Session, "MARGINTOP" ).getString() );
		}
		if ( containsAttribute( "MARGINLEFT" ) ){
			settings.setMarginLeft( getDynamic( _Session, "MARGINLEFT" ).getString() );
		}
		if ( containsAttribute( "MARGINRIGHT" ) ){
			settings.setMarginRight( getDynamic( _Session, "MARGINRIGHT" ).getString() );
		}
		if ( containsAttribute( "MARGINBOTTOM" ) ){
			settings.setMarginBottom( getDynamic( _Session, "MARGINBOTTOM" ).getString() );
		}
		
		// page type and size
		String pageType = getDynamic( _Session, "PAGETYPE" ).getString().toLowerCase();
		String pageSize = "a4";
		
		boolean landscape = false; // default to portrait
		if ( containsAttribute( "ORIENTATION" ) ){
			String orientStr = getDynamic( _Session, "ORIENTATION" ).getString();
			if ( orientStr.equalsIgnoreCase( "LANDSCAPE" ) ){
				landscape = true;
			}else if ( !orientStr.equalsIgnoreCase( "PORTRAIT" ) ){
				throw newRunTimeException( "Invalid ORIENTATION value. Valid values include \"PORTRAIT\" and \"LANDSCAPE\"." );
			}
		}
		
		if ( pageType.equals( "a4" ) || pageType.equals( "a5" ) || pageType.equals( "b4" ) || pageType.equals( "legal" ) || pageType.endsWith( "letter" )){
			pageSize = pageType;
			if ( landscape ){
				pageSize += " landscape";
			}
		}else{
			String width = null;
			String height = null;
			if ( pageType.equals( "b5" ) ){
				width = "7in";
				height = "9.88in";
			}else if ( pageType.equals( "b5-jis" ) ){
				width = "7.19in";
				height = "10.13in";
			}else if ( pageType.equals( "b4-jis" ) ){
				width = "10.13in";
				height = "14.31in";
			}else if ( pageType.equals( "custom" ) ){
				if ( !containsAttribute( "PAGEHEIGHT" ) || !containsAttribute( "PAGEWIDTH" ) ){
					throw newRunTimeException( "Missing PAGEHEIGHT/PAGEWIDTH attribute(s). Both must be specified when specifying a CUSTOM page size" );
				}

				width = getDynamic( _Session, "PAGEWIDTH" ).getString() + unit;
				height = getDynamic( _Session, "PAGEHEIGHT" ).getString() + unit;
				
			}else{
				throw newRunTimeException( "Invalid PAGETYPE value." );
			}
			
			if ( landscape ){
				String tmp = height;
				height = width;
				width = tmp;
			}
			
			pageSize = width + " " + height;
		}
			
		settings.setPageSize( pageSize );
		
		// proxy details
		if ( containsAttribute( "PROXYHOST" ) ){
			String proxyHost = getDynamic( _Session, "PROXYHOST" ).getString();
			int proxyPort = 80;
			if ( containsAttribute( "PROXYPORT" ) ){
				proxyPort = getDynamic( _Session, "PROXYPORT" ).getInt();
			}
			String proxyUser = null;
			String proxyPassword = null;
			if ( containsAttribute( "PROXYUSER" ) ){
				proxyUser = getDynamic( _Session, "PROXYUSER" ).getString(); 
				proxyPassword =	getDynamic( _Session, "PROXYPASSWORD" ).getString();
			}
			
			settings.setProxyDetails( proxyHost, proxyPort, proxyUser, proxyPassword );
		}
		
		return settings;
	}
	
	@SuppressWarnings("deprecation")
	private String handleDocument( cfSession _Session, InputStream _in, String _charset ) throws IOException, dataNotSupportedException, cfmRunTimeException{
		String mimeType = getDynamic( _Session, "MIMETYPE" ).getString().toLowerCase();
		String charset = _charset;
		if ( charset == null ){
			charset = "ISO-8859-1"; 
		}

		if ( mimeType.equals( "text/html" ) ){
			return IOUtils.toString( _in, charset );
		}else if ( mimeType.equals( "text/plain" ) ){
			String plainTxt = IOUtils.toString( _in, charset );
			return "<pre>" + escapeHtmlChars(plainTxt) + "</pre>";
			
		}else if ( mimeType.startsWith( "image/" ) ){ 
			File tmpFile = File.createTempFile( "cfdoc", '.' + mimeType.substring( mimeType.indexOf( '/' )+1 ) );
			OutputStream fout = cfEngine.thisPlatform.getFileIO().getFileOutputStream( tmpFile );
			StreamUtil.copyTo( _in, fout );
			return "<img src=\"" + tmpFile.toURL() + "\"/>";
		}else{
			throw newRunTimeException( "Invalid MIMETYPE value. Supported values include text/html, text/plain, image/jpg, image/gif, image/png and image/bmp" );
		}

	}
	
	/*
	 * escapeHTMLChars
	 * 
	 * This is used to escape the HTML chars in a plain text file so
	 * that JTidy and flying saucer ignore them.
	 */
    private static String escapeHtmlChars(String content)
    {
        char[] old = new char[] { '&', '<', '>', '\"' };
        String[] replacements = new String[] { "&amp;", "&lt;", "&gt;", "&quot;" };

        int strLen = content.length();
        int charsLen = old.length;
        StringBuilder buffer = new StringBuilder(content);
        StringBuilder writer = new StringBuilder(strLen);
        char nextChar;
        boolean foundCh;

        for (int i = 0; i < strLen; i++)
        {
            nextChar = buffer.charAt(i);
            foundCh = false;

            for (int j = 0; j < charsLen; j++)
            {
                if (nextChar == old[j])
                {
                    writer.append(replacements[j]);
                    foundCh = true;
                }
            }
            if (!foundCh)
            {
                writer.append(nextChar);
            }
        }

        return writer.toString();//.Replace("\n", "<br>");
    }
	
	private void setPermissions( cfSession _Session, PDFEncryption _pdfEnc ) throws cfmRunTimeException{

		// apply encryption 
		String encryption = getDynamic( _Session, "ENCRYPTION" ).getString().toLowerCase(); 
		if ( encryption.equals( "40" ) || encryption.equals( "40-bit" ) ){
			_pdfEnc.setEncryptionType( PdfWriter.STANDARD_ENCRYPTION_40);
		}else if ( encryption.equals( "128" ) || encryption.equals( "128-bit" ) ){
			_pdfEnc.setEncryptionType( PdfWriter.STANDARD_ENCRYPTION_128);
		}else if ( encryption.equals( "aes" ) ){
			_pdfEnc.setEncryptionType( PdfWriter.ENCRYPTION_AES_128);
		}else if ( !encryption.equals( "none" ) ){
			throw newRunTimeException( "Invalid ENCRYPTION value. Supported values include \"40-bit\", \"128-bit\", \"AES\" and \"none\"" );
		}

		// Default to no permissions
		int permissionsMask = 0;

		if ( containsAttribute( "PERMISSIONS" ) ){
	
			String [] permissions = getDynamic( _Session, "PERMISSIONS" ).getString().toLowerCase().split( "," );
			if ( permissions.length > 0 ){
			
				for ( int i = 0; i < permissions.length; i++ ){
					String nextPermission = permissions[i];
					if ( nextPermission.equals( "allowprinting" ) ){
						permissionsMask |= PdfWriter.ALLOW_PRINTING;
					}else if ( nextPermission.equals( "allowmodifycontents" ) ){
						permissionsMask |= PdfWriter.ALLOW_MODIFY_CONTENTS;
					}else if ( nextPermission.equals( "allowcopy" ) ){
						permissionsMask |= PdfWriter.ALLOW_COPY;
					}else if ( nextPermission.equals( "allowmodifyannotations" ) ){
						permissionsMask |= PdfWriter.ALLOW_MODIFY_ANNOTATIONS;
					}else if ( nextPermission.equals( "allowscreenreaders" ) ){
						if (_pdfEnc.getEncryptionType() == PdfWriter.STANDARD_ENCRYPTION_40)
							throw newRunTimeException("AllowScreenReaders is not valid with 40-bit encryption");
						permissionsMask |= PdfWriter.ALLOW_SCREENREADERS;
					}else if ( nextPermission.equals( "allowassembly" ) ){
						if (_pdfEnc.getEncryptionType() == PdfWriter.STANDARD_ENCRYPTION_40)
							throw newRunTimeException("AllowAssembly is not valid with 40-bit encryption");
						permissionsMask |= PdfWriter.ALLOW_ASSEMBLY;
					}else if ( nextPermission.equals( "allowdegradedprinting" ) ){
						if (_pdfEnc.getEncryptionType() == PdfWriter.STANDARD_ENCRYPTION_40)
							throw newRunTimeException("AllowDegradedPrinting is not valid with 40-bit encryption");
						permissionsMask |= PdfWriter.ALLOW_DEGRADED_PRINTING;
					}else if ( nextPermission.equals( "allowfillin" ) ){
						if (_pdfEnc.getEncryptionType() == PdfWriter.STANDARD_ENCRYPTION_40)
							throw newRunTimeException("AllowFillIn is not valid with 40-bit encryption");
						permissionsMask |= PdfWriter.ALLOW_FILL_IN;
					}else{
						throw newRunTimeException( "Invalid permissions value: " + nextPermission );	
					}
				}
			}
		}
		
		// Set the allowed permissions
		_pdfEnc.setAllowedPrivileges(permissionsMask);
		
		if ( containsAttribute("OWNERPASSWORD") )
			_pdfEnc.setOwnerPassword( getDynamic( _Session, "OWNERPASSWORD" ).getString().getBytes() );
		
		if ( containsAttribute("USERPASSWORD") )
			_pdfEnc.setUserPassword( getDynamic( _Session, "USERPASSWORD" ).getString().getBytes() );
		
	}
	
	/*
	 * removeBackground
	 * 
	 * Removes all of the background items like the 'bgcolor' attribute from
	 * the XHTML body.
	 */
	private String removeBackground(String _body)
	{
		try
		{
			// Parse the XHTML body into an XML object
			cfXmlData content = cfXmlData.parseXml(_body, true, null);
			
			// Remove the background items from the top node and all of its children
			Node node = content.getXMLNode();
			removeBackground(node);
			
			// Convert the XML object back into a string
			_body = content.toString();
		}
		catch (Exception e)
		{
		}
		
		return _body;
	}
	
	/*
	 * removeBackground
	 * 
	 * Removes all of the background items like the 'bgcolor' attribute from
	 * the XML node and recursively calls itself to remove them from all child
	 * nodes too.
	 */
	private void removeBackground(Node _node)
	{
		// Remove any background items from the node.
		// For now we only remove the 'bgcolor' attribute.
		NamedNodeMap attributes = _node.getAttributes();
		if ( (attributes != null) && (attributes.getNamedItem("bgcolor") != null) )
			attributes.removeNamedItem("bgcolor");
	
		// If the node has children then make recursive calls to remove the
		// background items from the children too.
		if (_node.hasChildNodes())
		{
			NodeList children =_node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++)
				removeBackground(children.item(i));
		}
	}

	/*
	 * extractTagAttributes
	 * 
	 * Extracts the attributes for the tag with the specified name.
	 */
	private HashMap<String, String> extractTagAttributes( String _tagName, String _xhtml )
	{
		HashMap<String, String> attributes = new HashMap<String, String>();
		
		try
		{
			// Parse the XHTML body into an XML object
			cfXmlData content = cfXmlData.parseXml(_xhtml, true, null);
			
			// Find the node for the specified tag
			Node node = findNode(content.getXMLNode(), _tagName);
			if ( node != null)
			{
				// Copy the node's attributes (if any) into the HashMap
				NamedNodeMap nodeAttributes = node.getAttributes();
				if (nodeAttributes != null)
				{
					for (int i=0; i < nodeAttributes.getLength(); i++)
						attributes.put(nodeAttributes.item(i).getNodeName(), nodeAttributes.item(i).getNodeValue());
				}
			}
		}
		catch (Exception e)
		{
		}
		
		return attributes;
	}
	
	/*
	 * findNode
	 * 
	 * Search the node and all child nodes for the node with the specified name.
	 */
	private Node findNode(Node _node, String _tagName)
	{
		String name = _node.getNodeName();
		if ((name != null) && name.equals(_tagName))
			return _node;
		
		if (_node.hasChildNodes())
		{
			NodeList children =_node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++)
			{
				Node n = findNode(children.item(i), _tagName);
				if ( n != null)
					return n;
			}
		}

		return null;
	}
	
	private String insertStyles( cfSession _Session, String _body, DocumentSection _section, DocumentSettings _settings, boolean _backgroundVisible, int _numSections ) throws cfmRunTimeException{
		
		int headStart = _body.indexOf( "<head>" );
		if ( headStart < 0 ){
			return _body;
		}
		
		HashMap<String, String> bodyTagAttributes = null;
		if ( !_backgroundVisible )
		{
			// The background is set to not visible so remove all
			// the background items like the 'bgcolor' attribute.
			_body = removeBackground(_body);
			headStart = _body.indexOf( "<head>" );
		}
		else
		{		
			// For some reason the BODY bgcolor attribute is being ignored so let's extract
			// it and set the background color using CSS. We'll extract all the body tag's
			// attributes since we might need them in the future.
			bodyTagAttributes = extractTagAttributes("body", _body);
		}

		StringBuilder styleBlock = new StringBuilder();
		styleBlock.append( "<style>\n" );
		styleBlock.append( "@page{\n" );
		styleBlock.append( getMargins( _Session, _section, _settings ) );
		String header = replaceHeaderFooterVariables(_section, _section.getHeader(), _numSections);
		String footer = replaceHeaderFooterVariables(_section, _section.getFooter(), _numSections);
		insertHeaderFooter( _Session, styleBlock, "top", _section.getHeaderAlign(), header, _backgroundVisible, _section.getMimeType() );
		insertHeaderFooter( _Session, styleBlock, "bottom", _section.getFooterAlign(), footer, _backgroundVisible, _section.getMimeType() );
		styleBlock.append( "size: " );
		styleBlock.append( _settings.getPageSize() );
		styleBlock.append( ";\n" );
		styleBlock.append( "}\n" );

		if ((bodyTagAttributes != null) && ( bodyTagAttributes.containsKey("bgcolor")))
		{
			styleBlock.append( "body { background-color: " );
			styleBlock.append( bodyTagAttributes.get("bgcolor") );
			styleBlock.append( "; }\n");
		}

		styleBlock.append( "</style>\n" );
		
		return _body.substring( 0, headStart+6 ) + styleBlock + _body.substring( headStart+6 ); 
	}
	
	/*
	 * replaceHeaderFooterVariables
	 * 
	 * Replace the header/footer page count variables with the appropriate values.
	 */
	private String replaceHeaderFooterVariables(DocumentSection _section, String _content, int _numSections)
	{
		if (_content == null)
			return null;

		_content = _content.replaceAll("BD:CURRENTPAGENUMBER", "\" counter(page) \"");
		_content = _content.replaceAll("BD:CURRENTSECTIONPAGENUMBER", "\" counter(page) \"");
		
		if ( _numSections == 1 ){
			// There's only 1 section so we can use the page and pages counters.
			_content = _content.replaceAll("BD:TOTALPAGECOUNT", "\" counter(pages) \"");
			_content = _content.replaceAll("BD:TOTALSECTIONPAGECOUNT", "\" counter(pages) \"");
		} else {
			// There's more than 1 section so we need to use the values calculated by preparePageCounters().
			_content = _content.replaceAll("BD:TOTALPAGECOUNT", Integer.toString(_section.getTotalPageCount()));
			_content = _content.replaceAll("BD:TOTALSECTIONPAGECOUNT", Integer.toString(_section.getTotalSectionPageCount()));
		}

		return _content;
	}
		
	private void insertHeaderFooter( cfSession _Session, StringBuilder _sb, String _position, String _align, String _content, boolean _backgroundVisible, String _sectionMimeType ) throws cfmRunTimeException{
		if ( _content != null ){		
			_sb.append( "@" );
			_sb.append( _position );
			_sb.append( "-" );
			_sb.append( _align );
			_sb.append( "{\nwhite-space: pre;\n" ); // this is necessary so escaped newlines in the content are displayed properly
			
			// If the section had a mimetype specified and it's text/plain or if it didn't have a mimetype
			// specified and the document mimetype was set to text/plain then treat the body as plain text.
			if ((( _sectionMimeType != null ) && (_sectionMimeType.equalsIgnoreCase("text/plain"))) ||
			    (( _sectionMimeType == null ) && (getDynamic(_Session,"MIMETYPE").getString().equalsIgnoreCase("text/plain"))))
			{
				_sb.append( "content: " );
				_sb.append( convertPlainTextToCSSContent(_content) );
			}
			else
			{
				HashMap<String, String> properties = new HashMap<String,String>();
				String xhtml = getHeaderFooterXHTML(_content, properties);
				if ( _backgroundVisible && properties.containsKey("background-color"))
				{
					_sb.append( "background-color: " );
					_sb.append( properties.get("background-color") );
					_sb.append( ";\n" );
				}
				_sb.append( "content: " );
				_sb.append( xhtml );
			}
			_sb.append( ";\n}\n" );
		}
	}
	
	private String getHeaderFooterXHTML(String _content, HashMap<String,String> _properties)
	{
		// Convert to XHTML
		String xhtml = getXHTML(_content);
		
		// Find the beginning of the body tag and body text (starts with double quotes)
		int beginBody = xhtml.indexOf("<body");
		if ( beginBody >= 0 )
		{
			beginBody = xhtml.indexOf('>',beginBody);
			while ( xhtml.charAt(beginBody) != '"' )
				beginBody++;
		}
		else
		{
			beginBody = 0;
		}
		
		// Find the end of the body text (ends with double quotes) and extract the body
		String body;
		int endPos = xhtml.indexOf("</body>");
		if ( endPos > 0)
		{
			while ( xhtml.charAt(endPos) != '"' )
				endPos--;
			body = xhtml.substring(beginBody, endPos+1);
		}
		else
		{
			body = xhtml.substring(beginBody);
		}

		// Convert the body to an appropriate format for CSS content
		String cssContent = convertHTMLToCSSContent(body);
		
		// Now see if there are any HTML attributes that need to be returned
		// as CSS properties.
		HashMap<String, String> attributes = extractTagAttributes("body", xhtml);
		if ( attributes.containsKey("bgcolor"))
			_properties.put("background-color", attributes.get("bgcolor"));
		
		return cssContent;
	}
	
	/*
	 * convertPlainTextToCSSContent
	 */
	private static String convertPlainTextToCSSContent(String _text)
	{
		StringBuilder sb = new StringBuilder();
		
		// First escape all HTML special characters except for the surrounding double quotes
		_text = '"' + escapeHtmlChars(_text.substring(1,_text.length()-1)) + '"';
		
		// Now escape all newline characters and HTML escaped double quotes
		for ( int i = 0; i < _text.length(); i++ )
		{
			char ch = _text.charAt(i);
			switch (ch)
			{
				case '\r':
				case '\n':
					sb.append("\\A ");	// escape sequence for newline
					if ( (ch == '\r') && (i+1 < _text.length()) && (_text.charAt(i+1) == '\n') )
						i++;
					break;
					
				case '&':
					if ( _text.startsWith("&quot;",i) )
					{
						if ( _text.startsWith("&quot; counter(page) &quot;",i) )
						{
							sb.append("\" counter(page) \""); // put back as unescaped
							i += "&quot; counter(page) &quot;".length() - 1; // move to the end
						}
						else if ( _text.startsWith("&quot; counter(pages) &quot;",i) )
						{
							sb.append("\" counter(pages) \""); // put back as unescaped
							i += "&quot; counter(pages) &quot;".length() - 1; // move to the end
						}
						else
						{
							sb.append("\\22 "); // escape sequence for double quote
							i += "&quot;".length() - 1; // move to the end
						}
					}
					else
					{
						sb.append(ch);
					}
					break;
					
				default:
					sb.append(ch);
					break;
			}
		}
		
		return sb.toString();
	}
	
	/*
	 * convertHTMLToCSSContent
	 */
	private static String convertHTMLToCSSContent(String _html)
	{
		StringBuilder sb = new StringBuilder();
		
		for ( int i = 0; i < _html.length(); i++ )
		{
			char ch = _html.charAt(i);
			switch (ch)
			{
				case '\r':
				case '\n':
					// Do nothing so these characters are removed.
					break;
				
				case '<':
					int endTag = _html.indexOf('>',i);			
					if ( endTag > i )
					{
						String htmlTag = _html.substring(i, endTag+1);
						
						// Currently we only handle the <br> tag.
						if ( htmlTag.equals("<br />"))
							sb.append("\\A ");	// escape sequence for newline
						
						i = endTag;
					}
					else
					{
						sb.append(ch);
					}
					break;
					
				default:
					sb.append(ch);
					break;
			}
		}
		
		return sb.toString();
	}
	 
	private String getMargins( cfSession _Session, DocumentSection _section, DocumentSettings _settings ) throws dataNotSupportedException, cfmRunTimeException{
		StringBuilder sb = new StringBuilder();
		
		String marginTop = _section.getMarginTop();
		if ( _section.getMarginTop() == null ){
			marginTop = _settings.getMarginTop();
		}
		if ( marginTop != null ){
			sb.append( "margin-top: " );
			sb.append( marginTop );
			sb.append( _settings.getUnit() );
			sb.append( ";\n" );
		}
		
		String marginBottom = _section.getMarginBottom();
		if ( _section.getMarginBottom() == null ){
			marginBottom = _settings.getMarginBottom();
		}
		if ( marginBottom != null ){
			sb.append( "margin-bottom: " );
			sb.append( marginBottom );
			sb.append( _settings.getUnit() );
			sb.append( ";\n" );
		}

		String marginLeft = _section.getMarginLeft();
		if ( _section.getMarginLeft() == null ){
			marginLeft = _settings.getMarginLeft();
		}
		if ( marginLeft != null ){
			sb.append( "margin-left: " );
			sb.append( marginLeft );
			sb.append( _settings.getUnit() );
			sb.append( ";\n" );
		}

		String marginRight = _section.getMarginRight();
		if ( _section.getMarginRight() == null ){
			marginRight = _settings.getMarginRight();
		}
		if ( marginRight != null ){
			sb.append( "margin-right: " );
			sb.append( marginRight );
			sb.append( _settings.getUnit() );
			sb.append( ";\n" );
		}
		
		return sb.toString();
	}
	

	private void appendSectionAttributes( cfSession _Session, DocumentSection _section ) throws cfmRunTimeException{
		if ( containsAttribute( "MIMETYPE" ) )
			_section.setMimeType( getDynamic( _Session, "MIMETYPE" ).toString() );

		if ( containsAttribute( "NAME" ) )
			_section.setName( getDynamic( _Session, "NAME" ).toString() );

		//TODO: validate values?
		if ( containsAttribute( "MARGINTOP" ) )
			_section.setMarginTop( getDynamic( _Session, "MARGINTOP" ).toString() );
		if ( containsAttribute( "MARGINBOTTOM" ) )
			_section.setMarginBottom( getDynamic( _Session, "MARGINBOTTOM" ).toString() );
		if ( containsAttribute( "MARGINLEFT" ) )
			_section.setMarginLeft( getDynamic( _Session, "MARGINLEFT" ).toString() );
		if ( containsAttribute( "MARGINRIGHT" ) )
			_section.setMarginRight( getDynamic( _Session, "MARGINRIGHT" ).toString() );
		
		_section.setUserAgent( getDynamic( _Session, "USERAGENT" ).getString() );
		
		if ( containsAttribute( "AUTHPASSWORD" ) && containsAttribute( "AUTHUSER" ) ){
			_section.setAuthentication( getDynamic( _Session, "AUTHUSER" ).getString(), getDynamic( _Session, "AUTHPASSWORD" ).getString() );
		}

	}

	private class SessionOutputStream extends java.io.OutputStream{

		private cfSession session;
		private cfmRunTimeException exception;
		private boolean firstWrite = true;
		private String saveAsName;
		
		SessionOutputStream( cfSession _session, String _saveAsName ){
			session = _session;
			saveAsName = _saveAsName;
		}
		
		public cfmRunTimeException getException(){
			return exception;
		}
		
		@Override
		public void write( byte[] b, int off, int len ) throws IOException {
			if ( exception == null ){ // only attempt to write further if exception hasn't occurred
				try {			
					// If this is the first write then set the content type and appropriate headers.
					// We wait to set these here so that any exceptions that occur before this will
					// be displayed properly in the browser.
					if ( firstWrite ){
						firstWrite = false;
						session.setContentType( "application/pdf" );
						session.setHeader("Content-Disposition", "inline; filename=" + saveAsName);
					}
					session.write( b, off, len );
				} catch (cfmRunTimeException e) {
					exception = e;
				}
			}
		}

		@Override
		public void write( byte[] b ) throws IOException {
			this.write( b, 0, b.length );
		}

		@Override
		public void write( int arg0 ) throws IOException {
			this.write( new byte[]{ (byte) arg0 }, 0, 1 );
		}
		
	} 

	private class NullOutputStream extends java.io.OutputStream{
		
		NullOutputStream(){
		}
		
		@Override
		public void write( byte[] b, int off, int len ) throws IOException {
		}

		@Override
		public void write( byte[] b ) throws IOException {
		}

		@Override
		public void write( int arg0 ) throws IOException {
		}	
	} 
	
	private class CreationListener extends DefaultPDFCreationListener
	{
		private PdfString author;
		private PdfString title;
		private PdfString subject;
		private PdfString keywords;
		
		public CreationListener(cfData _author, cfData _title, cfData _subject, cfData _keywords)
		{
			if ( _author != null )
				author = new PdfString(_author.toString());
			if ( _title != null )
				title = new PdfString(_title.toString());
			if ( _subject != null )
				subject = new PdfString(_subject.toString());
			if ( _keywords != null )
				keywords = new PdfString(_keywords.toString());
		}

		//public void preOpen(ITextRenderer renderer) 
		public void onClose(ITextRenderer renderer) 
		{
			PdfString creator = new PdfString("OpenBD " + cfEngine.PRODUCT_VERSION + " (" + cfEngine.BUILD_ISSUE + ")");
			renderer.getOutputDevice().getWriter().getInfo().put(PdfName.CREATOR,creator);  
			
			if (author != null)
				renderer.getOutputDevice().getWriter().getInfo().put(PdfName.AUTHOR,author);  
			if (title != null)
				renderer.getOutputDevice().getWriter().getInfo().put(PdfName.TITLE,title);  
			if (subject != null)
				renderer.getOutputDevice().getWriter().getInfo().put(PdfName.SUBJECT,subject);  
			if (keywords != null)
				renderer.getOutputDevice().getWriter().getInfo().put(PdfName.KEYWORDS,keywords);  
		}
	}

}
