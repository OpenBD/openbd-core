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
 */

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfTREE extends cfTag implements Serializable
{
  
  static final long serialVersionUID = 1;
  
  public static final String DATA_BIN_KEY = "CFTREE_DATA";
 
  protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute( "HEIGHT", 				320 );  
		defaultAttribute( "WIDTH", 					200 );
		defaultAttribute( "MESSAGE", 				"Please select a tree value" );
		defaultAttribute( "ONERROR", 				"tf_on_error" );
		defaultAttribute( "DELIMITER", 			"\\" );
		defaultAttribute( "COMPLETEPATH",		"false" );
		defaultAttribute( "APPENDKEY", 			"true" );
		defaultAttribute( "REQUIRED", 			"false" );
		defaultAttribute( "HIGHLIGHTHREF", 	"true" );
		defaultAttribute( "BORDER", 				"0" );
  
    parseTagHeader( _tag );

    if ( !containsAttribute( "NAME" ) )
     	throw newBadFileException( "Missing NAME", "You need to provide a NAME" );
  }
  
  public String getEndMarker(){	return "</CFTREE>"; }
  
  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {

	  cfFormInputData formData;
	  
		//--[ Is this Tag running inside a CFFORM		
  	formData = (cfFormInputData)_Session.getDataBin( cfFORM.DATA_BIN_KEY );
  	if ( formData == null )
  		throw newRunTimeException( "There is no CFFORM tag" );


		//--[ Determine the browser type
		boolean isBrowserIE = true;
		cfData tmpData	= _Session.getQualifiedData( variableStore.CGI_SCOPE ).getData( "http_user_agent" );
		if ( tmpData == null || tmpData.getString().indexOf("MSIE") == -1 )
			isBrowserIE = false;


		//--[ Setup up the intermediate communicating class			
    cfTreeData treeData = new cfTreeData();
    _Session.setDataBin( DATA_BIN_KEY, treeData );

    
  	String name = "__CFFORM__" + getDynamic( _Session, "NAME" ).getString().toUpperCase().trim();
  	int height 	= getDynamic( _Session, "HEIGHT" ).getInt();
  	int width 	= getDynamic( _Session, "WIDTH" ).getInt();
    
    super.render( _Session );
    
    if( containsAttribute( "REQUIRED" ) ){
      if ( getDynamic(_Session,"REQUIRED").getBoolean() )
			  formData.formTagRequired( name, "_TREE", getDynamic(_Session, "MESSAGE").getString(), getDynamic(_Session, "ONERROR").getString() );
		}

		if ( containsAttribute("ONVALIDATE") )
		  formData.addValidateCheck( name, getDynamic(_Session, "ONVALIDATE").getString(), null, getDynamic(_Session, "MESSAGE").getString(), getDynamic(_Session, "ONERROR").getString() );


		String notSupported = "Java is not installed";
		if ( containsAttribute( "NOTSUPPORTED" ) )
    	notSupported = getDynamic( _Session, "NOTSUPPORTED" ).getString();  
		
		
		//-- Write out the first part of the tag
		if ( isBrowserIE ){
			_Session.write( "<OBJECT CLASSID=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\" WIDTH=\"" + width + "\" HEIGHT=\"" + height + "\"" );
			_Session.write( getAlignments( _Session ) );			
			_Session.write( " STANDBY=\"First use of this control. Configuring browser. Please wait...\">" );
  	  _Session.write( "<PARAM NAME=\"CODE\" VALUE=\"com.bluedragon.browser.thinlet.AppletLauncher\">" );
    	_Session.write( "<PARAM NAME=\"ARCHIVE\" VALUE=\"" + formData.getArchive() + "\">" );
			_Session.write( "<PARAM NAME=\"MAYSCRIPT\" VALUE=\"true\">");
			_Session.write( "<PARAM NAME=\"class\" VALUE=\"com.bluedragon.browser.TreeApplet\">");
			_Session.write( getParameters( _Session, formData, true ) );
			
			String data = treeData.getTreeModelString();
			int loops	= ((int)data.length() / (int)100) + 1;
			int c1 = 0;
			for ( int x=0; x < loops; x++ ){
				int e1 = c1 + 100;
				if ( e1 > data.length() )
					e1 = data.length();
					
				_Session.write( "<PARAM NAME=\"treedata"+ x +"\" VALUE=\"["+ data.substring(c1,e1) +"]\">" );
				c1 += 100;
			}
			
			_Session.write( notSupported );
	    _Session.write( "</OBJECT>" );  
		}else{
	    _Session.write( "<applet WIDTH=\"" + width + "\" HEIGHT=\"" + height + "\" TYPE=\"application/x-java-applet;jpi-version=1.3.0_01\" PLUGINSPAGE=\"http://java.sun.com/j2se/1.3/jre/index.html\" CODE=\"com.bluedragon.browser.thinlet.AppletLauncher\" ARCHIVE=\"" + formData.getArchive() + "\" MAYSCRIPT=\"true\" ");
			_Session.write( getAlignments( _Session ) );
			_Session.write( "><PARAM NAME=\"class\" VALUE=\"com.bluedragon.browser.TreeApplet\">");
			_Session.write( getParameters( _Session, formData, true ) );
			
			String data = treeData.getTreeModelString();
			int loops	= ((int)data.length() / (int)100) + 1;
			int c1 = 0;
			for ( int x=0; x < loops; x++ ){
				int e1 = c1 + 100;
				if ( e1 > data.length() )
					e1 = data.length();
					
				_Session.write( "<PARAM NAME=\"treedata"+ x +"\" VALUE=\"["+ data.substring(c1,e1) +"]\">" );
				c1 += 100;
			}
			
    	_Session.write( "<noembed>" + notSupported + "</noembed></applet>" );
		}
		
		//--[ Write out the name
    _Session.write( "<INPUT TYPE=\"HIDDEN\" NAME=\"" + name + "\">" );
	 
  	_Session.deleteDataBin( DATA_BIN_KEY );
  	
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
	
	
	private String getParameters( cfSession _Session, cfFormInputData formData, boolean bParam ) throws cfmRunTimeException {
		StringBuilder tmp = new StringBuilder(256);		
		
		tmp.append( wrapParam( bParam, "FORMNAME", 			formData.getFormName() ) );
		tmp.append( wrapParam( bParam, "DELIMITER", 		getDynamic( _Session, "DELIMITER" ).getString() ) );
		tmp.append( wrapParam( bParam, "APPENDKEY", 		"" + getDynamic( _Session, "APPENDKEY" ).getBoolean() ) );
		tmp.append( wrapParam( bParam, "HIGHLIGHTHREF", 	"" + getDynamic( _Session, "HIGHLIGHTHREF" ).getBoolean() ) );
   	tmp.append( wrapParam( bParam, "COMPLETEPATH", 	"" + getDynamic( _Session, "COMPLETEPATH" ).getBoolean()) );
		tmp.append( wrapParam( bParam, "BORDER", 				getDynamic( _Session, "BORDER" ).getString() ));
		
	  if ( containsAttribute( "FONT" ) )
      tmp.append( wrapParam( bParam, "FONT", getDynamic( _Session, "FONT" ).getString() ));
   
    if ( containsAttribute( "NAME" ) )
			tmp.append( wrapParam( bParam, "OBJECTNAME", "__CFFORM__" + getDynamic( _Session, "NAME" ).getString().toUpperCase().trim()) );

    if ( containsAttribute( "FONTSIZE" ) )
      tmp.append( wrapParam( bParam, "FONTSIZE", getDynamic( _Session, "FONTSIZE" ).getString()) );
    
    if ( containsAttribute( "ITALIC" ) )
      tmp.append( wrapParam( bParam, "ITALIC", "" + getDynamic( _Session, "ITALIC" ).getBoolean() ));

    if ( containsAttribute( "BOLD" ) )
    	tmp.append( wrapParam( bParam, "BOLD", "" + getDynamic( _Session, "BOLD" ).getBoolean()) );

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
