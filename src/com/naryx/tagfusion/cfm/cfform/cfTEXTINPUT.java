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

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfFormData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfTEXTINPUT extends cfINPUT implements Serializable
{

  static final long serialVersionUID = 1;

  protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute( "HEIGHT", 50 );
		defaultAttribute( "WIDTH", 220 );
    defaultAttribute( "REQUIRED", "NO" );
    defaultAttribute( "MESSAGE", 	"Please provide a valid value" );
		defaultAttribute( "ONERROR", 	"tf_on_error" );
    parseTagHeader( _tag );
    
    if ( !containsAttribute( "NAME" ) )
    	throw newBadFileException( "Missing NAME", "You need to provide a NAME" );
  }


  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
	  String name	=  getDynamic(_Session, "NAME").getString();
    
    cfFormInputData form;
  
  	form = (cfFormInputData)_Session.getDataBin( cfFORM.DATA_BIN_KEY );
  	if ( form == null )
  		throw newRunTimeException( "There is no CFFORM tag" );

 		if ( getDynamic(_Session, "REQUIRED").getBoolean() )
			form.formTagRequired( name, "_TEXT", getDynamic(_Session, "MESSAGE").getString(), getConstant("ONERROR") );
			
  	  
  	_Session.write( "<APPLET MAYSCRIPT NAME=\"" + getDynamic( _Session, "NAME" ).getString() + "\" CODE=\"com.bluedragon.browser.TextApplet\"  WIDTH=\"" + getDynamic( _Session, "WIDTH" ).getInt() + "\" HEIGHT=\"" + getDynamic( _Session, "HEIGHT" ).getInt() + "\" ARCHIVE=\""+ form.getArchive() +"\">" );
    _Session.write( "<PARAM NAME=\"MAYSCRIPT\" VALUE=\"true\">");
    _Session.write( "<PARAM NAME=\"ARCHIVE\" VALUE=\"" + form.getArchive() + "\">" );
   	_Session.write( "<PARAM NAME=\"FORMNAME\" VALUE=\"" + form.getFormName() + "\" >" );
	
  	_Session.write( setParams( _Session, form ) );
  	
  	_Session.write( "</APPLET>" );

  	_Session.write( "<INPUT TYPE=\"hidden\" NAME=\"" );
  	_Session.write( name );
  	_Session.write( "\"" );
  	
  	appendValue( _Session, form, name );

     _Session.write( ">" );
   
   	applyValidation( _Session, form, name, "TEXT", new StringBuilder() );
	
		return cfTagReturnType.NORMAL;
  }


  private void appendValue( cfSession _Session, cfFormInputData _formData, String _name ) throws cfmRunTimeException{
  	
		cfData value = null;
		// if we're preserving data then look to see if a value is already in the form scope
		if ( _formData.isPreserveData() ){
			cfFormData formdata = (cfFormData)_Session.getQualifiedData( variableStore.FORM_SCOPE ); 
			value = formdata.getData( _name );
		}
		
		// if we haven't already retrieved the value from the form scope and the VALUE is specified
		if ( value == null && containsAttribute( "VALUE" ) ){
			value = getDynamic( _Session, "VALUE" );
		}

		if ( value != null ){
      _Session.write( " VALUE=\"" );
      _Session.write( com.nary.util.string.escapeHtml( value.getString() ) );
      _Session.write( "\"" );

		}
  }

  private String setParams( cfSession _Session, cfFormInputData form )throws cfmRunTimeException{
  	StringBuilder parameters = new StringBuilder();

    if ( containsAttribute( "NAME" ) )
        _Session.write( "<PARAM NAME=\"OBJECTNAME\" VALUE=\"" + getDynamic( _Session, "NAME" ).getString() + "\">" );
      
    if ( containsAttribute( "SIZE" ) )
      parameters.append( "<PARAM NAME=\"SIZE\" VALUE=\"" + getDynamic( _Session, "SIZE" ).getString() + "\">" );
    
    if ( containsAttribute( "REQUIRED" ) )
      parameters.append( "<PARAM NAME=\"REQUIRED\" VALUE=\"" + getDynamic( _Session, "REQUIRED" ).getString() + "\">" );
      
    if ( containsAttribute( "VALIDATE" ) )
      parameters.append( "<PARAM NAME=\"VALIDATE\" VALUE=\"" + getDynamic( _Session, "VALIDATE" ).getString() + "\">" );  
      
    if ( containsAttribute( "ONVALIDATE" ) )
      parameters.append( "<PARAM NAME=\"ONVALIDATE\" VALUE=\"" + getDynamic( _Session, "ONVALIDATE" ).getString() + "\">" );
     
    if ( containsAttribute( "RANGE" ) )
      parameters.append( "<PARAM NAME=\"RANGE\" VALUE=\"" + getDynamic( _Session, "RANGE" ).getString() + "\">" );
    
    if ( containsAttribute( "ONERROR" ) )
      parameters.append( "<PARAM NAME=\"ONERROR\" VALUE=\"" + getDynamic( _Session, "ONERROR" ).getString() + "\">" );
    
    if ( containsAttribute( "VALUE" ) )
      parameters.append( "<PARAM NAME=\"VALUE\" VALUE=\"" + getDynamic( _Session, "VALUE" ).getString() + "\">" );
     
    if ( containsAttribute( "HEIGHT" ) )
      parameters.append( "<PARAM NAME=\"HEIGHT\" VALUE=\"" + getDynamic( _Session, "HEIGHT" ).getString() + "\">" );
    
    if ( containsAttribute( "WIDTH" ) )
      parameters.append( "<PARAM NAME=\"WIDTH\" VALUE=\"" + getDynamic( _Session, "WIDTH" ).getString() + "\">" );
      
    if ( containsAttribute( "MESSAGE" ) )
      parameters.append( "<PARAM NAME=\"MESSAGE\" VALUE=\"" + getDynamic( _Session, "MESSAGE" ).getString() + "\">" );
      
    if ( containsAttribute( "VSPACE" ) )
      parameters.append( "<PARAM NAME=\"VSPACE\" VALUE=\"" + getDynamic( _Session, "VSPACE" ).getString() + "\">" );
    
    if ( containsAttribute( "HSPACE" ) )
      parameters.append( "<PARAM NAME=\"HSPACE\" VALUE=\"" + getDynamic( _Session, "HSPACE" ).getString() + "\">" );
      
    if ( containsAttribute( "ALIGN" ) )
      parameters.append( "<PARAM NAME=\"ALIGN\" VALUE=\"" + getDynamic( _Session, "ALIGN" ).getString() + "\">" );
      
    if ( containsAttribute( "GROOVECOLOR" ) )
      parameters.append( "<PARAM NAME=\"GROOVECOLOR\" VALUE=\"" + getDynamic( _Session, "GROOVECOLOR" ).getString() + "\">" );
    
    if ( containsAttribute( "BGCOLOR" ) )
      parameters.append( "<PARAM NAME=\"BGCOLOR\" VALUE=\"" + getDynamic( _Session, "BGCOLOR" ).getString() + "\">" );
    
    if ( containsAttribute( "TEXTCOLOR" ) )
      parameters.append( "<PARAM NAME=\"TEXTCOLOR\" VALUE=\"" + getDynamic( _Session, "TEXTCOLOR" ).getString() + "\">" );
      
    if ( containsAttribute( "FONT" ) )
      parameters.append( "<PARAM NAME=\"FONT\" VALUE=\"" + getDynamic( _Session, "FONT" ).getString() + "\">" );
    
    if ( containsAttribute( "FONTSIZE" ) )
      parameters.append( "<PARAM NAME=\"FONTSIZE\" VALUE=\"" + getDynamic( _Session, "FONTSIZE" ).getString() + "\">" );    
    
    if ( containsAttribute( "ITALIC" ) )
      parameters.append( "<PARAM NAME=\"ITALIC\" VALUE=\"" + getDynamic( _Session, "ITALIC" ).getString() + "\">" );
    
    if ( containsAttribute( "BOLD" ) )
      parameters.append( "<PARAM NAME=\"BOLD\" VALUE=\"" + getDynamic( _Session, "BOLD" ).getString() + "\">" );    
      
    if ( containsAttribute( "MAXLENGTH" ) )
      parameters.append( "<PARAM NAME=\"MAXLENGTH\" VALUE=\"" + getDynamic( _Session, "MAXLENGTH" ).getString() + "\">" );
    
    if ( containsAttribute( "NOTSUPPORTED" ) )
      parameters.append( "<PARAM NAME=\"NOTSUPPORTED\" VALUE=\"" + getDynamic( _Session, "NOTSUPPORTED" ).getString() + "\">" );    
      
    return parameters.toString();
  }


}
