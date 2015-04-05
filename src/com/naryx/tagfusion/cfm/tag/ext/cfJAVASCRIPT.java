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
 *	$Id: cfJAVASCRIPT.java 1729 2011-10-10 19:25:28Z alan $
 *
 *  http://www.openbluedragon.org/
 */

package com.naryx.tagfusion.cfm.tag.ext;

import java.io.File;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.engine.cfArrayListData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.expression.function.expandPath;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/*
 * A simple drop-in replacement for <script> that takes JS files
 * and minimizes them.  It will also reduce a number of JS files
 * into one file
 *
 * Simply change <script src="">
 */

public class cfJAVASCRIPT extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

	private static File	jsTempDirectory = null;
	public static String JS_TMP_DIR = "cfjavascript";
	
	protected String END_MARKER = null;
	protected OutputTypeEnum output;
	protected enum OutputTypeEnum { INLINE,	HTMLHEAD, HTMLBODY };

  public java.util.Map getInfo(){
  	return createInfo("output",
  			"Intelligently manage Javascript files by collecting multiple files together and on-the-fly optimizing them to reduce their size and readability.  Either compile Javascript inline or as a <script src=''> tag.  It will only include Javascript files once per page request, even if they are specified multiple times in multiple tags throughout the page request.");
  }

  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
 			createAttInfo("SRC", 			"A comma-separated list or array of Javascript files to manage.  If omitted then use a closing tag, and place the Javascript inside the tag", "", false ),
 			createAttInfo("MUNGE", 		"Flag to control whether or not to rename the Javascript variables and function names to reduce the readability of the resulting block", "true", false ),
 			createAttInfo("MINIMIZE", "Flag to control whether the resulting Javascript block is to be optimized removing all whitespace, comments and line breaks","true",false),
 			createAttInfo("OUTPUT", 	"Controls where the resulting Javascript gets placed in the HTML file.  Values are 'INLINE', 'HEAD' or 'BODY'.  This lets you put the result in the HEAD of the page, or at the end of the page for faster loading","INLINE", false),
 			createAttInfo("CHARSET", 	"The character set of the Javascript files","utf-8", false),
 			createAttInfo("PATH", 		"The path for the link to be seen to be coming from","current path", false)
  	};
  }


	protected void defaultParameters( String _tag ) throws cfmBadFileException {

		/* Initialise its working directory; only once */
		try{
			if (jsTempDirectory == null){
				jsTempDirectory = FileUtils.checkAndCreateDirectory( cfEngine.thisPlatform.getFileIO().getTempDirectory(), JS_TMP_DIR, true );
			}
		}catch(Exception fse){
			throw newBadFileException("Failed Startup",  "CFJAVASCRIPT had a problem: " + fse.getMessage() );
		}

		defaultAttribute("CHARSET", 	"utf-8");
		defaultAttribute("MUNGE", 		"true");
		defaultAttribute("MINIMIZE", 	"true");
		defaultAttribute("OUTPUT", 		"inline");
		parseTagHeader( _tag );

		if ( !containsAttribute("SRC") ){
			END_MARKER	= "</CFJAVASCRIPT>";
		}

		output	= OutputTypeEnum.INLINE;
		if ( getConstant("OUTPUT").equalsIgnoreCase("HEAD") ){
			output	= OutputTypeEnum.HTMLHEAD;
		} else if ( getConstant("OUTPUT").equalsIgnoreCase("BODY") ){
			output	= OutputTypeEnum.HTMLBODY;
		}
	}

  public String getEndMarker(){
  	return END_MARKER;
  }

  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
  	if ( END_MARKER == null )
  		return renderSrc( _Session );
  	else
  		return renderInline( _Session );
  }


  private cfTagReturnType renderInline( cfSession _Session ) throws cfmRunTimeException {
  	String jsBlock = renderToString( _Session, cfTag.HONOR_CF_SETTING ).getOutput();

		/* Parameters */
		boolean bMinimize = getDynamic(_Session, "MINIMIZE" ).getBoolean();

		if ( bMinimize ){
			boolean bMunge = getDynamic(_Session, "MUNGE" ).getBoolean();
			try{
				jsBlock = minimiseJavascript( jsBlock, bMunge );
			} catch (Exception e) {
				throw newRunTimeException( "Problem with minimizing Javascript: "  +  e.getMessage() );
			}
		}


		/* Output the results */
		if ( output == OutputTypeEnum.INLINE )
			_Session.write( "<script type=\"text/javascript\">" + jsBlock + "</script>" );
		else if ( output == OutputTypeEnum.HTMLHEAD )
			_Session.setHeadElement( "<script type=\"text/javascript\">" + jsBlock + "</script>", true );
		else if ( output == OutputTypeEnum.HTMLBODY )
			_Session.setBodyElement( "<script type=\"text/javascript\">" + jsBlock + "</script>", true );

  	return cfTagReturnType.NORMAL;
  }



  private cfTagReturnType renderSrc( cfSession _Session ) throws cfmRunTimeException {
		if ( !containsAttribute("SRC") )
			throw newRunTimeException("Attribte SRC is missing");

		/* Insert only unique files */
		HashSet<String>	uniqueJSFiles	= (HashSet)_Session.getDataBin("cfjavascript");
		if ( uniqueJSFiles == null ){
			uniqueJSFiles	= new HashSet<String>();
			_Session.setDataBin("cfjavascript", uniqueJSFiles );
		}

		String fileJS = null;
		
		try {

			/* Get the list of files */
			List<String>	srcUrls = new ArrayList<String>();
			cfData srcData = getDynamic(_Session, "SRC" );
			if ( srcData.getDataType() == cfData.CFSTRINGDATA ){
	
				String[]	arrayList	= srcData.getString().split(",");
				for ( int x=0; x < arrayList.length; x++ ){
					if ( uniqueJSFiles.contains(arrayList[x]) )
						continue;
	
					srcUrls.add( expandPath.expand(_Session, arrayList[x].trim() ) );
					uniqueJSFiles.add( arrayList[x] );
				}
	
			}else if ( srcData.getDataType() == cfData.CFARRAYDATA ){
	
				cfArrayListData cfArr = (cfArrayListData)srcData;
				for ( int x=0; x < cfArr.size(); x++ ){
					String jsFile	 = cfArr.getElement(x+1).getString();
					if ( uniqueJSFiles.contains( jsFile ) )
						continue;
	
					srcUrls.add( expandPath.expand(_Session, jsFile ) );
					uniqueJSFiles.add( jsFile );
				}
	
			}else{
				throw newRunTimeException( "The SRC attribute must be either a single URL or an Array of URLs" );
			}


			/* Parameters */
			boolean bMunge 		= getDynamic(_Session, "MUNGE" ).getBoolean();
			boolean bMinimize = getDynamic(_Session, "MINIMIZE" ).getBoolean();
	
	
			/* Let us make sure we haven't already built this, with all the options, and the files haven't changed on the file system */
			long timeCheck = 0;
			int		orderCheck = 1;
			if ( bMunge ) timeCheck++;
			if ( bMinimize ) timeCheck++;
	
			Iterator<String> it = srcUrls.iterator();
			StringBuilder	fileList = new StringBuilder( 256 );
			fileList.append("$Id: cfJAVASCRIPT.java 1729 2011-10-10 19:25:28Z alan $");
	
			while ( it.hasNext() ){
				File file = new File( it.next() );
				if ( !file.exists() )
					throw newRunTimeException( "The file: " + file + " does not exist" );
	
				fileList.append( file.toString() );
				timeCheck += (( (file.lastModified()+file.length()) << (orderCheck++) ) );
			}
	
			
			// The file name that will be used to save the file and appear in the webpage
			fileJS = "cfjs_" + getMD5asHex( String.valueOf( fileList.toString() + timeCheck ).getBytes() ) + ".js";

			if ( !jsTempDirectory.exists() )
				jsTempDirectory.mkdirs();
			
			File tmpFile = new File( jsTempDirectory, fileJS );
			if ( !tmpFile.exists() ){
				/* Let us now run through these files */
				StringBuilder	totalFileBuffer = new StringBuilder( 128000 );

				it = srcUrls.iterator();
				while ( it.hasNext() ){
					FileUtils.readFile( new File( it.next() ), totalFileBuffer );
					totalFileBuffer.append( ";\r\n" );
				}

				/* Write out the file */
				if ( bMinimize ){
					try {
						FileUtils.writeFile( tmpFile, minimiseJavascript( totalFileBuffer.toString(), bMunge) );
					} catch (Exception e) {
						throw newRunTimeException( "Problem with minimizing Javascript: "  +  e.getMessage() );
					}
				}else{
					FileUtils.writeFile( tmpFile, totalFileBuffer.toString() );
				}
			}
			
		} catch (Exception fse) {
			throw newRunTimeException( "CFJAVASCRIPT: " + fse.getMessage() );
		}

		
		/* We should put this file at the same level as the current request */
		String relativePath = "";
		if ( containsAttribute("PATH") ){
			relativePath = getDynamic(_Session, "PATH" ).getString();
			if ( !relativePath.endsWith("/") )
				relativePath += "/";
		}
		

		/* Output the results */
		if ( output == OutputTypeEnum.INLINE )
			_Session.write( "<script type=\"text/javascript\" src=\"" + relativePath + "load.cfres?js=" + fileJS + "\"></script>" );
		else if ( output == OutputTypeEnum.HTMLHEAD )
			_Session.setHeadElement( "<script type=\"text/javascript\" src=\"" + relativePath + "load.cfres?js=" + fileJS + "\"></script>", true );
		else if ( output == OutputTypeEnum.HTMLBODY )
			_Session.setBodyElement( "<script type=\"text/javascript\" src=\"" + relativePath + "load.cfres?js=" + fileJS + "\"></script>", true );

		return cfTagReturnType.NORMAL;
	}



	public static String minimiseJavascript( String jsContents, boolean munge ) throws Exception {
		StringReader	stringReader = new StringReader( jsContents );

		try{
			JavaScriptCompressor compressor = new JavaScriptCompressor( stringReader, new ErrorReporter() {
	      public void warning(String message, String sourceName,int line, String lineSource, int lineOffset) {}
	      public void error(String message, String sourceName,int line, String lineSource, int lineOffset) {}
	      public EvaluatorException runtimeError(String message, String sourceName,int line, String lineSource, int lineOffset) {
          error(message, sourceName, line, lineSource, lineOffset);
          return new EvaluatorException(message);
	      }
			} );


			StringWriter out = new StringWriter();
			boolean preserveAllSemiColons = true;
			boolean disableOptimizations 	= false;
			compressor.compress(out, -1, munge, false, preserveAllSemiColons, disableOptimizations);
			return out.toString();

		}catch(Exception e){
			cfEngine.log( jsContents );
			cfEngine.log( com.nary.Debug.getStackTraceAsString(e) );
			throw new Exception( "Javascript Minimizer: failed: " + e.getMessage() );
		}
	}

	
	private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', };

	protected String getMD5asHex(byte _b[]) {
		try {
			byte[] hash = MessageDigest.getInstance("MD5").digest( _b );

			char buf[] = new char[hash.length * 2];
			for (int i = 0, x = 0; i < hash.length; i++) {
				buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
				buf[x++] = HEX_CHARS[hash[i] & 0xf];
			}
			return new String(buf);

		} catch (NoSuchAlgorithmException e) {}
		return null;
	}
}