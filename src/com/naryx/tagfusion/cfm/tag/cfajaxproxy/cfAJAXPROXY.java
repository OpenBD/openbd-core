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
 *  $Id: cfAJAXPROXY.java 2009 2012-04-02 15:46:46Z alan $
 */

package com.naryx.tagfusion.cfm.tag.cfajaxproxy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

import com.nary.io.FileUtils;
import com.nary.io.StreamUtils;
import com.naryx.tagfusion.cfm.engine.cfArrayListData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.cfm.tag.ext.cfJAVASCRIPT;

public class cfAJAXPROXY extends cfTag implements java.io.Serializable {
	static final long serialVersionUID = 1;
  
	private static String	jsBaseTemplate = null, jsObjectTemplate = "";
	private static File	jsTempDirectory;

	public java.util.Map getInfo() {
		return createInfo("output", "This tag provides a Javascript API to the remote CFC making it very easy to utilise server side calls.  Only CFC methods that are marked as 'remote' can be exposed through this manner.  The resulting Javascript is independent of any popular Javascript library.  You can pass rich objects such as arrays and structures to the remote CFC with no problems.  This function will do the necessary encoding and decoding on the fly.");
	}

	public java.util.Map[] getAttInfo() {
		return new java.util.Map[] { 
				createAttInfo("ATTRIBUTECOLLECTION", 	"A structure containing the tag attributes", 	"", false ),
				createAttInfo("CFC", 									"The path of where the CFC you wish to call is, as seen by the client (for example browser), not the server.  Functions marked as 'remote' are exposed as Javascript functions", "", true), 
				createAttInfo("JSCLASSNAME", 					"The name of the Javascript object that will be available in the page", "", true),
				createAttInfo("INLINE", 							"Do you wish this Javacript block to be rendered inline, or as a remote URL resource", "false", false)
		};
	}

	
	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		if ( jsBaseTemplate == null ){
			try {
				jsBaseTemplate 		= StreamUtils.readToString( this.getClass().getResourceAsStream("blueDragonAjaxLibrary.js") );
				jsObjectTemplate 	= StreamUtils.readToString( this.getClass().getResourceAsStream("object-template.js") );
				
				// Minimize this chap
				jsBaseTemplate		= cfJAVASCRIPT.minimiseJavascript( jsBaseTemplate, true );
			} catch (Exception e) {
				throw newBadFileException("Invalid Setup", "CFAJAXPROXY is having difficult starting up (" + e.getMessage() + ")");
			}
			
			try{
				if (jsTempDirectory == null){
					jsTempDirectory = FileUtils.checkAndCreateDirectory( cfEngine.thisPlatform.getFileIO().getTempDirectory(), cfJAVASCRIPT.JS_TMP_DIR, true );
				}
			}catch(Exception fse){
				throw newBadFileException("Failed Startup",  "CFJAVASCRIPT had a problem: " + fse.getMessage() );
			}
		}
		
		parseTagHeader( _tag );
	
		if ( !containsAttribute("CFC") )
			throw newBadFileException("Missing Attribute", "You must provide either a CFC");
				
		if ( !containsAttribute("JSCLASSNAME") )
			throw newBadFileException("Missing Attribute", "You must provide JSCLASSNAME when using a CFC");
	}
  
	
	
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);
		
		boolean inline = false;
		if ( containsAttribute(attributes, "INLINE") )
			inline	= getDynamic(attributes, _Session, "INLINE").getBoolean();
		
		manageCoreJavascriptLibrary( _Session, inline );
		
		String CFC 			=	getDynamic(attributes, _Session, "CFC" ).getString();
		String JSNAME 	= getDynamic(attributes, _Session, "JSCLASSNAME" ).getString();
		
		cfSession offlineSession 	= new cfSession( _Session, false );
		cfComponentData component = new cfComponentData( offlineSession, CFC );

		String fileJS = "cfc" + getHashCode( component ) + ".js";
		File tmpFile 	= new File( jsTempDirectory, fileJS );
		
		if ( !inline ){
			if ( tmpFile.exists() && _Session.getDataBin( fileJS ) == null ){
				_Session.write( "<script src=\"load.cfres?js=" + fileJS + "\"></script>" );
				_Session.setDataBin( fileJS, "" );
				return cfTagReturnType.NORMAL;
			}
		}
		
		/* Build up the header of the file */
		StringBuilder	javascript = new StringBuilder( 16000 );
		javascript.append( JSNAME );
		javascript.append( " = function(){\r\n");
		javascript.append( " this.remoteCFC = '" );
		javascript.append( CFC.replace('.', '/') );
		javascript.append( ".cfc';\r\n" );

		cfStructData metaData 				= component.getMetaData();
		cfArrayListData functionArray = (cfArrayListData)metaData.getData("FUNCTIONS");
		
		/* For each method in the CFC we need to kick out the necessary value */
		for ( int x=0; x < functionArray.size(); x++ ){
			cfStructData functionData = (cfStructData)functionArray.getElement( x+1 );
			
			/* Only functions declared as REMOTE can be called */
			if ( !functionData.containsKey("ACCESS") || !functionData.getData("ACCESS").toString().equalsIgnoreCase("remote") ){
				continue;
			}

			javascript.append( "\r\n this." + functionData.getData("NAME") + " = function(");
			
			cfArrayListData paramArray = (cfArrayListData)functionData.getData("PARAMETERS");
			StringBuilder paramBuffer = new StringBuilder(1000);
			
			for ( int p=0; p < paramArray.size(); p++ ){
				cfStructData paramData = (cfStructData)paramArray.getElement( p+1 );
				String paramName = paramData.get("NAME").toString();
				
				
				javascript.append( paramName );
				if ( p < paramArray.size()-1 )
					javascript.append(",");
				
				paramBuffer.append( "\r\n  if (" + paramName + " != undefined){params['" + paramName + "']=JSON.stringify(" + paramName + ");}" );
			}

			javascript.append( "){" );
			javascript.append( "\r\n  var params = {method:'" );
			javascript.append( functionData.getData("NAME") );
			javascript.append( "'};" );
			javascript.append( paramBuffer );
			javascript.append( "\r\n  return this.remoteCall( params );");
			javascript.append( "\r\n };\r\n" );
		}
		
		javascript.append( "\r\n" );
		javascript.append( jsObjectTemplate );
		javascript.append( "};" );

		manageComponentJavascriptLibrary(_Session, javascript.toString(), fileJS, tmpFile, inline );
		
		return cfTagReturnType.NORMAL;
	}
	
	
	
	/*
	 * Calculates a hash value for all the components in this component tree.
	 * The value will be the same if the none of the files have changed.
	 * This will allow us to cache the javascript that is produced and not have
	 * to redo it every time
	 */
	private long getHashCode( cfComponentData component ){
		long hk = component.getComponentHash();
		cfComponentData superComponent = component.getSuperComponent();
		while ( superComponent != null ){
			hk += superComponent.getComponentHash();
			superComponent = superComponent.getSuperComponent();
		}
		return hk;
	}
	
	
	/*
	 * Writes up the core Javascript file that is required for the proxy class
	 */
	private void manageComponentJavascriptLibrary( cfSession _Session, String buffer, String fileJS, File tmpFile, boolean inline ) throws cfmRunTimeException {
		
		if ( inline ){
			try {
				_Session.write( "<script>" + cfJAVASCRIPT.minimiseJavascript( buffer, false ) + "</script>" );
			} catch (Exception e) {
				throw newRunTimeException( "Failed to minimize: " + fileJS );
			}
		}else{
			if ( !tmpFile.exists() ){
				try {
					writeFile( tmpFile, cfJAVASCRIPT.minimiseJavascript( buffer, false ) );
				} catch (Exception e) {
					throw newRunTimeException( "Failed to create the temporary file: " + fileJS );
				}
			}
			_Session.write( "<script src=\"load.cfres?js=" + fileJS + "\"></script>" );
		}
		
		_Session.setDataBin( fileJS, "" );
	}

	
	
	/*
	 * Writes up the core Javascript file that is required for the proxy class
	 */
	private void manageCoreJavascriptLibrary( cfSession _Session, boolean inline ) throws cfmRunTimeException {
		if ( _Session.getDataBin("cfajaxproxy.js") != null ) return;
		
		if ( inline ){
			_Session.write( "<script>" + jsBaseTemplate + "</script>" );
		}else{
			String fileJS = "cfajaxproxy01.js";
			File tmpFile = new File( jsTempDirectory, fileJS );

			if ( !jsTempDirectory.exists() )
				jsTempDirectory.mkdirs();
			
			if ( !tmpFile.exists() ){
				try {
					writeFile( tmpFile, jsBaseTemplate );
				} catch (Exception e) {
					throw newRunTimeException( "Failed to create core temporary file: " + fileJS );
				}
			}

			_Session.write( "<script src=\"load.cfres?js=" + fileJS + "\"></script>" );
		}
		_Session.setDataBin("cfajaxproxy.js", "" );
	}
	
	
	private void writeFile( File _file, String _contents ) throws cfmRunTimeException {
  	BufferedWriter	writer = null;
  	Writer	fwriter = null;
  	
		if ( !jsTempDirectory.exists() )
			jsTempDirectory.mkdirs();

  	
  	try{
  		fwriter	= cfEngine.thisPlatform.getFileIO().getFileWriter(_file);
  		writer	= new BufferedWriter( fwriter );
  		
  		writer.write( _contents );
  		writer.flush();
  	}catch(IOException ioe){
			throw newRunTimeException( "Failed to create the temporary file: " + _file );
  	}finally{
  		if ( writer != null )try{ writer.close(); }catch( IOException ignored ){}
  		if ( fwriter != null )try{ fwriter.close(); }catch( IOException ignored ){}
  	}
	}

}