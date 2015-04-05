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

/*
 * Uses the com.bluedragon.browser.TreeApplet to utlimately render
 * a tree control. Based on the thinlet technology that is also
 * included in the com.bluedragon.browser.thinlet package.
 * 
 * The JAR for this class should be in a folder: 
 * /BlueDragon/jars/bluedragonapplets.jar 
 * 
 */

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfFormData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfSLIDER extends cfTag implements Serializable
{

  static final long serialVersionUID = 1;
  
  protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute( "HEIGHT", 50 );  
		defaultAttribute( "WIDTH", 220 );  
		defaultAttribute( "ONERROR", "tf_on_error" );
		
    parseTagHeader( _tag );

    if ( !containsAttribute( "NAME" ) )
     	throw newBadFileException( "Missing NAME", "You need to provide a NAME" );
  }

  
  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {

		//---[ Check for the CFFORM tag
  	cfFormInputData formData = (cfFormInputData)_Session.getDataBin( cfFORM.DATA_BIN_KEY );
  	if ( formData == null )
  		throw newRunTimeException( "There is no CFFORM tag" );

		//--[ Determine the browser type
		boolean isBrowserIE = true;
		cfData tmpData	= _Session.getQualifiedData( variableStore.CGI_SCOPE ).getData( "http_user_agent" );
		if ( tmpData == null || tmpData.getString().indexOf("MSIE") == -1 )
			isBrowserIE = false;


  	String name = getDynamic( _Session, "NAME" ).getString();
  	int height 	= getDynamic( _Session, "HEIGHT" ).getInt();
  	int width 	= getDynamic( _Session, "WIDTH" ).getInt();
    
		String notSupported = "Java is not installed";
		if ( containsAttribute( "NOTSUPPORTED" ) )
    	notSupported = getDynamic( _Session, "NOTSUPPORTED" ).getString();  
		
	if ( isBrowserIE ){
		_Session.write( "<OBJECT CLASSID=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\"  WIDTH=\"" + width + "\" HEIGHT=\"" + height + "\"" );
		_Session.write( getAlignments( _Session ) );			
		_Session.write( " STANDBY=\"First use of this control. Configuring browser. Please wait...\">" );
		_Session.write( "<PARAM NAME=\"CODE\" VALUE=\"com.bluedragon.browser.thinlet.AppletLauncher\">" );
		_Session.write( "<PARAM NAME=\"ARCHIVE\" VALUE=\"" + formData.getArchive() + "\">" );
		_Session.write( "<PARAM NAME=\"MAYSCRIPT\" VALUE=\"true\">");
		_Session.write( "<PARAM NAME=\"class\" VALUE=\"com.bluedragon.browser.SliderApplet\">");
		_Session.write( getParameters( _Session, formData, true, name ) );
		_Session.write( notSupported );
		_Session.write( "</OBJECT>" );  
	}else{
		_Session.write( "<applet WIDTH=\"" + width + "\" HEIGHT=\"" + height + "\" TYPE=\"application/x-java-applet;jpi-version=1.3.0_01\" PLUGINSPAGE=\"http://java.sun.com/j2se/1.3/jre/index.html\" CODE=\"com.bluedragon.browser.thinlet.AppletLauncher\" ARCHIVE=\"" + formData.getArchive() + "\" MAYSCRIPT=\"true\" ");
		_Session.write( getAlignments( _Session ) );
		_Session.write( "><PARAM NAME=\"class\" VALUE=\"com.bluedragon.browser.SliderApplet\">");
		_Session.write( getParameters( _Session, formData, true, name ) );
		_Session.write( "<noembed>" + notSupported + "</noembed></applet>" );
	}

	_Session.write( "<INPUT TYPE=\"HIDDEN\" NAME=\"" + name + "\">" );
	
	return cfTagReturnType.NORMAL;
  }

	
 	private String getAlignments( cfSession _Session ) throws cfmRunTimeException {
 		StringBuilder tmp = new StringBuilder(128);		
    if ( containsAttribute( "HSPACE" ) )
    	tmp.append( wrapParam( false, "HSPACE", getDynamic( _Session, "HSPACE" ).getString()) );
      
    if ( containsAttribute( "VSPACE" ) )
    	tmp.append( wrapParam( false, "VSPACE", getDynamic( _Session, "VSPACE" ).getString()) ); 
       
    if ( containsAttribute( "ALIGN" ) )
    	tmp.append( wrapParam( false, "ALIGN", getDynamic( _Session, "ALIGN" ).getString() ));
		 
		return tmp.toString();
	}
 
	private String getParameters( cfSession _Session, cfFormInputData formData, boolean bParam, String name ) throws cfmRunTimeException {
		StringBuilder tmp = new StringBuilder(128);		
		
		tmp.append( wrapParam( bParam, "FORMNAME", formData.getFormName() ) );
		
	  if ( containsAttribute( "LABEL" ) )
      tmp.append( wrapParam( bParam, "LABEL", getDynamic( _Session, "LABEL" ).getString() ));
   
		tmp.append( wrapParam( bParam, "OBJECTNAME", name ) );

		if ( containsAttribute( "RANGE" ) )
			tmp.append( wrapParam( bParam, "RANGE", getDynamic( _Session, "RANGE" ).getString()) );

		cfData value = null;
		// if we're preserving data then look to see if a value is already in the form scope
		if ( formData.isPreserveData() ){
			cfFormData formdata = (cfFormData)_Session.getQualifiedData( variableStore.FORM_SCOPE ); 
			value = formdata.getData( name );
		}
		
		// if we haven't already retrieved the value from the form scope and the VALUE is specified
		if ( value == null && containsAttribute( "VALUE" ) ){
			value = getDynamic( _Session, "VALUE" );
		}

		if ( value != null )
			tmp.append( wrapParam( bParam, "VALUE", value.getString() ) );

		if ( containsAttribute( "MESSAGE" ) )
			tmp.append( wrapParam( bParam, "MESSAGE", getDynamic( _Session, "MESSAGE" ).getString()) );

		if ( containsAttribute( "BGCOLOR" ) )
			tmp.append( wrapParam( bParam, "BGCOLOR", getDynamic( _Session, "BGCOLOR" ).getString()) );

		if ( containsAttribute( "TEXTCOLOR" ) )
			tmp.append( wrapParam( bParam, "TEXTCOLOR", getDynamic( _Session, "TEXTCOLOR" ).getString()) );

		if ( containsAttribute( "FONT" ) )
			tmp.append( wrapParam( bParam, "FONT", getDynamic( _Session, "FONT" ).getString()) );

		if ( containsAttribute( "FONTSIZE" ) )
			tmp.append( wrapParam( bParam, "FONTSIZE", getDynamic( _Session, "FONTSIZE" ).getString()) );

		if ( containsAttribute( "ITALIC" ) )
			tmp.append( wrapParam( bParam, "ITALIC", getDynamic( _Session, "ITALIC" ).getString()) );

		if ( containsAttribute( "BOLD" ) )
			tmp.append( wrapParam( bParam, "BOLD", getDynamic( _Session, "BOLD" ).getString()) );
			
		if ( containsAttribute( "VERTICAL" ) )
			tmp.append( wrapParam( bParam, "VERTICAL", getDynamic( _Session, "VERTICAL" ).getBoolean() + "") );
			
		return tmp.toString();
	}
 
	private static String wrapParam( boolean wrap, String name, String value ){
		StringBuilder t = new StringBuilder(32);
		if ( wrap ){
			t.append( "<PARAM NAME=\"" );
			t.append( name );
			t.append( "\" VALUE=\"" );
			t.append( value );
			t.append( "\">" );
		}else{
			t.append ( " " );
			t.append( name );
			t.append( "=\"" );
			t.append( value );
			t.append( "\" " );
		}
		return t.toString();
	}
}
