/*
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

package com.naryx.tagfusion.cfm.tag.ext;

import java.io.File;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import com.yahoo.platform.yui.compressor.CssCompressor;

/*
 * A simple drop-in replacement for <link rel="stylesheet"> that takes CSS
 * files and minimises them.
 */

public class cfSTYLESHEET extends cfJAVASCRIPT implements Serializable {
	static final long serialVersionUID = 1;
	private static File	cssTempDirectory = null;
	public static String CSS_TMP_DIR = "cfstylesheet";
	
  public java.util.Map getInfo(){
  	return createInfo("output",
  			"Intelligently manage CSS files by collecting multiple files together and on-the-fly optimizing them to reduce their size and readability.  It will only include CSS files once per page request, even if they are specified multiple times in multiple tags throughout the page request.");
  }
  
  
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
 			createAttInfo("SRC", 			"A comma-separated list or array of CSS files to manage.  If omitted then use a closing tag, and place the CSS inside the tag", "", false ),
 			createAttInfo("HREF", 		"Same as the SRC tag - there for drop-in speed", "", false ),
 			createAttInfo("MEDIA", 		"The media attribute for the underlying link tag", "ALL", false ),
 			createAttInfo("MINIMIZE", "Flag to control whether the resulting CSS block is to be optimized removing all whitespace, comments and line breaks","true",false),
 			createAttInfo("OUTPUT", 	"Controls where the resulting CSS gets placed in the HTML file.  Values are 'INLINE', 'HEAD' or 'BODY'.  This lets you put the result in the HEAD of the page, or at the end of the page for faster loading","INLINE", false),
 			createAttInfo("PATH", 		"The path for the link to be seen to be coming from","current path", false)
  	};
  }

	
	
	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		
		try{
			if (cssTempDirectory == null){
				cssTempDirectory = FileUtils.checkAndCreateDirectory( cfEngine.thisPlatform.getFileIO().getTempDirectory(), CSS_TMP_DIR, true );
			}
		}catch(Exception fse){
			throw newBadFileException("Failed Startup",  "CFSTYLESHEET had a problem: " + fse.getMessage() );
		}
		
		
		defaultAttribute("MEDIA", 		"all");
		defaultAttribute("MINIMIZE", 	"true");
		defaultAttribute("OUTPUT", 		"inline");
		parseTagHeader( _tag );

		if (!containsAttribute("SRC") && !containsAttribute("HREF"))
			END_MARKER	= "</CFSTYLESHEET>";
		
		output	= OutputTypeEnum.INLINE;
		if ( getConstant("OUTPUT").equalsIgnoreCase("HEAD") ){
			output	= OutputTypeEnum.HTMLHEAD;
		} else if ( getConstant("OUTPUT").equalsIgnoreCase("BODY") ){
			output	= OutputTypeEnum.HTMLBODY;
		}
	}
	
	
  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
  	if ( END_MARKER == null )
  		return renderSrc( _Session );
  	else
  		return renderInline( _Session );
  }


  private cfTagReturnType renderInline( cfSession _Session ) throws cfmRunTimeException {
  	String cssBlock = renderToString( _Session, cfTag.HONOR_CF_SETTING ).getOutput();

		/* Parameters */
		boolean bMinimize = getDynamic(_Session, "MINIMIZE" ).getBoolean();

		if ( bMinimize ){
			try{
				cssBlock = minimiseCSS( cssBlock );
			} catch (Exception e) {
				throw newRunTimeException( "Problem with minimizing CSS: "  +  e.getMessage() );
			}
		}


		/* Output the results */
		if ( output == OutputTypeEnum.INLINE )
			_Session.write( "<style>" + cssBlock + "</style>" );
		else if ( output == OutputTypeEnum.HTMLHEAD )
			_Session.setHeadElement( "<style>" + cssBlock + "</style>", true );
		else if ( output == OutputTypeEnum.HTMLBODY )
			_Session.setBodyElement( "<style>" + cssBlock + "</style>", true );

  	return cfTagReturnType.NORMAL;
  }


	private cfTagReturnType renderSrc( cfSession _Session ) throws cfmRunTimeException {
		if ( !containsAttribute("SRC") && !containsAttribute("HREF") )
			throw newRunTimeException("Attribte SRC (or HREF) is missing");

		/* Get the list of files */
		List<String>	srcUrls = new ArrayList<String>();
		String fileCSS = null;
		
		try {
		
			cfData srcData;
			if ( containsAttribute("SRC") )
				srcData = getDynamic(_Session, "SRC" );
			else
				srcData = getDynamic(_Session, "HREF" );
	
			if ( srcData.getDataType() == cfData.CFSTRINGDATA ){
				String [] srcArr = srcData.getString().split( "," );
				for ( String nextSrc : srcArr ){
					srcUrls.add( expandPath.expand(_Session, nextSrc.trim() ) );
				}
			}else if ( srcData.getDataType() == cfData.CFARRAYDATA ){
				cfArrayListData cfArr = (cfArrayListData)srcData;
				for ( int x=0; x < cfArr.size(); x++ ){
					srcUrls.add( expandPath.expand(_Session, cfArr.getElement(x+1).getString() ) );
				}
			}else{
				throw newRunTimeException( "The SRC attribute must be either a single URL or an Array of URLs" );
			}
	
			/* Parameters */
			boolean bMinimize = getDynamic(_Session, "MINIMIZE" ).getBoolean();
	
	
			/* Let us make sure we haven't already built this, with all the options, and the files haven't changed on the file system */
			long timeCheck = 0;
			int		orderCheck = 1;
			Iterator<String> it = srcUrls.iterator();
			while ( it.hasNext() ){
				File file = new File( it.next() );
				if ( !file.exists() )
					throw newRunTimeException( "The file: " + file + " does not exist" );
	
				timeCheck += ( (file.lastModified() + file.length()) << (orderCheck++) );
			}
	
	
			if ( !cssTempDirectory.exists() )
				cssTempDirectory.mkdirs();
			
			fileCSS = "cfcss_" + getMD5asHex( String.valueOf(timeCheck).getBytes() ) + ".css";
					
			File tmpFile = new File( cssTempDirectory, fileCSS );
			if ( !tmpFile.exists() ){
				/* Let us now run through these files */
				StringBuilder	totalFileBuffer = new StringBuilder( 128000 );
	
				it = srcUrls.iterator();
				while ( it.hasNext() ){
					FileUtils.readFile( new File(it.next()), totalFileBuffer );
					totalFileBuffer.append( "\r\n" );
				}
	
				/* Write out the file */
				if ( bMinimize )
					FileUtils.writeFile( tmpFile, minimiseCSS( totalFileBuffer.toString() ) );
				else
					FileUtils.writeFile( tmpFile, totalFileBuffer.toString() );
			}

		} catch (Exception fse) {
			throw newRunTimeException( "CFSTYLESHEET: " + fse.getMessage() );
		}

		
		/* We should put this file at the same level as the current request */
		String relativePath = "";
		if ( containsAttribute("PATH") ){
			relativePath = getDynamic(_Session, "PATH" ).getString();
			if ( !relativePath.endsWith("/") )
				relativePath += "/";
		}

		
		/* Output the results */
		String t = "<link rel=\"stylesheet\" type=\"text/css\" media=\"" + getDynamic(_Session, "MEDIA").getString() + "\" href=\"" + relativePath + "load.cfres?css=" + fileCSS + "\"/>";
		
		if ( output == OutputTypeEnum.INLINE )
			_Session.write( t );
		else if ( output == OutputTypeEnum.HTMLHEAD )
			_Session.setHeadElement( t, true );
		else if ( output == OutputTypeEnum.HTMLBODY )
			_Session.setBodyElement( t, true );
		
		return cfTagReturnType.NORMAL;
	}



	private String minimiseCSS( String cssContents ) throws cfmRunTimeException {
		StringReader	stringReader = new StringReader( cssContents );

		try{
			CssCompressor compressor = new CssCompressor( stringReader );

			StringWriter out = new StringWriter();
			compressor.compress(out, -1);
			return out.toString();

		}catch(Exception e){
			throw newRunTimeException( "CSS Minimizer: failed: " + e.getMessage() );
		}
	}

}