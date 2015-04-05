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

package com.naryx.tagfusion.cfm.http;

import java.io.File;
import java.io.Serializable;

import javax.activation.MimetypesFileTypeMap;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.fileDescriptor;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfHTTPPARAM extends cfTag implements Serializable{
  static final long serialVersionUID = 1;

  @SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
  	return createInfo("remote", "Specifies a parameter to use with CFHTTP");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
   			createAttInfo( "TYPE", "The type of this parameter: url, formfield, cgi, body, xml, header, cookie, file", 	"", true ),
   			createAttInfo( "ENCODED", "Is the parameter value encoded", 	"", false ),
   			createAttInfo( "VALUE", "The value for the type", 	"", false ),
   			createAttInfo( "URL", "If TYPE=URL, then this is the value for the URL", 	"", false ),
   			createAttInfo( "FORMFIELD", "If TYPE=FORMFIELD, then this is the name of the field", 	"", false ),
   			createAttInfo( "CGI", "If TYPE=CGI, then this is the name of the CGI field", 	"", false ),
   			createAttInfo( "HEADER", "If TYPE=HEADER, then this is the name for the header field", 	"", false ),
   			createAttInfo( "COOKIE", "If TYPE=COOKIE, then this is the name of the cookie", 	"", false ),
   			createAttInfo( "FILE", "If TYPE=FILE, then this is the path of the file", 	"", false ),
   			createAttInfo( "MIMETYPE", "If TYPE=FILE, then this is the mime type of the file", 	"", false ),
  	};
  }
  
  
  protected void defaultParameters( String _tag ) throws cfmBadFileException {
    defaultAttribute( "ENCODED", "true" );
    parseTagHeader( _tag );
  
    if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;

    if (!containsAttribute("TYPE")){
      throw newBadFileException("Missing Attribute", "This tag requires a TYPE attribute");
    }
  }

	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

    if (!containsAttribute(attributes,"TYPE"))
      throw newBadFileException("Missing Attribute", "This tag requires a TYPE attribute");
    
		return	attributes;
	}

   
  public String getEndMarker(){
    return null;
  }


  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
  	cfStructData attributes = setAttributeCollection(_Session);
  	
    //--[ Get the mail data to allow us to pass data back to the CFMAIL tag
    cfHttpData  httpData  = (cfHttpData)_Session.getDataBin("CFHTTP");
    if ( httpData == null ){
      throw newRunTimeException("The CFHTTPPARAM tag must be nested inside a CFHTTP tag.");
    }

    // notify httpData that at least one CFHTTPARAM has been processed  - POSTs req at least one
    httpData.setParamsUsed(true);

    String type = getDynamic(attributes,_Session, "TYPE").getString().toLowerCase();
    
    if ( type.equals( "url" ) ) {
      String name = getName( attributes, _Session, "URL" );
      if (name.length() != 0){
        httpData.addURLData(name, getValue(attributes, _Session, "URL" ) );
      }
      
    } else if ( type.equals( "formfield" ) ) {
      if ( httpData.isBodySet() ){
        throw newRunTimeException( "You cannot combine CFHTTPPARAM values of type FILE/FORMFIELD with those of type BODY/XML." );
      }

      String name = getName( attributes,_Session, "FORMFIELD" );
      if (name.length() != 0){
        httpData.addFormData(name, getValue( attributes,_Session, "FORMFIELD" ), getDynamic( attributes,_Session, "ENCODED" ).getBoolean() );
      }

    } else if ( type.equals( "cgi" ) ) {
      String name = getName( attributes,_Session, "CGI" );
      if (name.length() != 0){
        // only url encode the data if ENCODED = YES
        httpData.addHeader( name, getValue(attributes, _Session, "CGI" ), getDynamic(attributes, _Session, "ENCODED" ).getBoolean() );
        
      }
    
    } else if ( type.equals( "body" ) || type.equals( "xml" ) ){
      if ( httpData.isBodySet() ){
        throw newRunTimeException( "You can only use one CFHTTPPARAM of type XML or BODY." );
      }else if ( httpData.getFiles().size() > 0 || httpData.getFormData().size() > 0 ){
        throw newRunTimeException( "You cannot combine CFHTTPPARAM values of type FILE/FORMFIELD with those of type BODY/XML." );
      }
      
      String value = getValue(attributes, _Session, type.toUpperCase() ); 
      if ( type.equals( "xml" ) ){   
        httpData.setBody( value, "text/xml" );
      }else{
        httpData.setBody( value, "application/octet-stream" );
      }
      
    
    }else if ( type.equals( "header" ) ) {
      String name = getName( attributes,_Session, "HEADER" );
      if (name.length() != 0){ // different from cgi in that data is not urlencoded
        httpData.addHeader(name, getValue( attributes,_Session, "HEADER" ), false );
      }
  
    } else if (type.equals("cookie")){
      String name = getName(attributes, _Session, "COOKIE" );
      if (name.length() != 0){
        if (containsAttribute(attributes,"VALUE")){
          httpData.addCookie( name, getValue(attributes, _Session, "COOKIE" ), getDynamic(attributes,_Session, "ENCODED" ).getBoolean()  );
        }
        else{
          throw newRunTimeException("The VALUE attribute must be set when using a CFHTTPPARAM of type COOKIE");
        }
      }
    }
    else if (type.equals("file")){
      if ( httpData.isBodySet() ){
        throw newRunTimeException( "You cannot combine CFHTTPPARAM values of type FILE/FORMFIELD with those of type BODY/XML." );
      }
      
      String name = getName( attributes,_Session, "FILE" );
      
      if ( containsAttribute(attributes,"FILE") ){
        String file = getDynamic(attributes,_Session, "FILE").getString();
        String mimetype = null;
        if ( containsAttribute(attributes, "MIMETYPE" ) ){
          mimetype = getDynamic(attributes, _Session, "MIMETYPE" ).getString();
        }else{
          MimetypesFileTypeMap mimemap = new MimetypesFileTypeMap();
          mimetype =  mimemap.getContentType( file );
        }
        if (file.length() != 0){
          httpData.addFile( new fileDescriptor( name, new File( file ), mimetype ) ); 
        }
      }
      else{
        throw newRunTimeException("The FILE attribute must be set when using a CFHTTPPARAM of type FILE");
      }
    }else{
      throw newRunTimeException("Invalid value specified for the TYPE attribute.");
    }
    
    return cfTagReturnType.NORMAL;
  }// render()


  private String getName( cfStructData attributes, cfSession _Session, String _type ) throws cfmRunTimeException{
    if (!containsAttribute("NAME")){
      throw newRunTimeException( "This tag requires a NAME attribute when TYPE is \"" + _type + "\"." );
    }
    return getDynamic(attributes,_Session, "NAME").getString();
  }


  private String getValue( cfStructData attributes, cfSession _Session, String _type ) throws cfmRunTimeException{
    if (!containsAttribute("VALUE")){
      throw newRunTimeException( "This tag requires a VALUE attribute when TYPE is \"" + _type + "\"." );
    }
    return getDynamic(attributes,_Session, "VALUE").getString();
  }

  
}// cfHTTPParam
