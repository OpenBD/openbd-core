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

package com.naryx.tagfusion.cfm.cfform;

import java.io.Serializable;
import java.util.Set;

import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.engineListener;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.xmlConfig.xmlCFML;
 
public class cfFORM extends cfAbstractFormTag implements Serializable
{
  static final long serialVersionUID = 1;

	private static long nameID	= System.currentTimeMillis();
	
	public static final String DATA_BIN_KEY = "CFFORM_DATA";

	private static String defaultScriptSrc;
	
	public static void init( xmlCFML configFile ) {
		defaultScriptSrc = configFile.getString( "server.system.scriptsrc", cfEngine.DEFAULT_SCRIPTSRC );
		cfEngine.registerEngineListener( new EngineListener() );
	}
	
  protected void defaultParameters( String _tag ) throws cfmBadFileException {
    defaultAttribute( "NAME", 		"BD" + (nameID++) );
    defaultAttribute( "METHOD", 	"POST" );
    
    parseTagHeader( _tag );
  }
  
  public String getEndMarker(){	return "</CFFORM>"; }
  
  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
  	
  	cfFormInputData form = (cfFormInputData)_Session.getDataBin( DATA_BIN_KEY );
  	if ( form == null ){
  	 	form = new cfFormInputData();
 	  	_Session.setDataBin( DATA_BIN_KEY, form );
 	  	String scriptSrc = null;
 	  	if ( containsAttribute( "SCRIPTSRC" ) ){
 	  		scriptSrc = getDynamic( _Session, "SCRIPTSRC" ).getString();
 	  	}else{
 	  		scriptSrc = defaultScriptSrc;
 	  	}
 	  	
 	  	// add trailing / if one not present
 	  	if ( !scriptSrc.endsWith( "/" ) ){
 	  		scriptSrc = scriptSrc + "/";
 	  	}

 	  	if ( containsAttribute( "PRESERVEDATA" ) ){
 	  		form.setPreserveData( getDynamic( _Session, "PRESERVEDATA" ).getBoolean() );
 	  	}

 	  	_Session.setHeadElement( "<script type=\"text/javascript\" src=\"" + _Session.REQ.getContextPath() + scriptSrc + "cfform.js\"></script>\r\n", true );
 	  	_Session.setHeadElement( "<script type=\"text/javascript\" src=\"" + _Session.REQ.getContextPath() + scriptSrc + "mask.js\"></script>\r\n", true );

  	} else {
 	  	form.reset();
 	  }
 	  
 	  form.setFormName( getDynamic(_Session, "NAME").getString() );

 	  // append all the attributes
 	 StringBuilder attribs = new StringBuilder();
		appendAttributes( _Session, attribs, getIgnoreKeys() );
 	  
		if ( containsAttribute("PASSTHROUGH") ){
			attribs.append( " " );
			attribs.append( getDynamic(_Session, "PASSTHROUGH").getString() );
			attribs.append( " " );
    }

		if ( containsAttribute( "ARCHIVE" ) ){
			form.setArchive( getDynamic(_Session, "ARCHIVE").getString() );		
		}else{
			form.setArchive( _Session.REQ.getContextPath() + "/bluedragon/jars/bluedragonapplets.jar" );
		}
		
  	form.setFormTag( attribs.toString(), getValue( _Session, "ONSUBMIT" ) );
  
		//--[ Run the inside of the tag
		String buffer = renderToString( _Session ).getOutput();
		form.closeScript();
		
  	//--[ Output the Form tag
    boolean processingCfOutput = _Session.setProcessingCfOutput( true );
    boolean suppressWhiteSpace = _Session.setSuppressWhiteSpace( false );
    
  	_Session.setHeadElement( form.getJavaScript(), true );
    		
		_Session.write( form.outputFORMtag() );
		
		_Session.write( buffer );
		
		_Session.write( form.closeFORMTag() );
		_Session.setSuppressWhiteSpace( suppressWhiteSpace );
    _Session.setProcessingCfOutput( processingCfOutput );
    
    return cfTagReturnType.NORMAL;
  }
  
  protected Set<String> getIgnoreKeys(){
  	Set<String> ignoreKeys = super.getIgnoreKeys();
  	ignoreKeys.remove("NAME" );
  	ignoreKeys.add( "ARCHIVE" );
  	ignoreKeys.add( "SCRIPTSRC" );
  	ignoreKeys.add( "ONSUBMIT" );
  	return ignoreKeys;
  }
  
	private static class EngineListener implements engineListener{
		
		public void engineAdminUpdate( xmlCFML config ) {
			defaultScriptSrc = config.getString( "server.system.scriptsrc", cfEngine.DEFAULT_SCRIPTSRC );
		}
		
		public void engineShutdown() {}	// do nothing
	}

}
