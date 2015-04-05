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

package com.naryx.tagfusion.cfm.mail;

import java.io.File;
import java.io.Serializable;

import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfMAILPARAM extends cfTag implements Serializable{
  
  static final long serialVersionUID = 1;


  @SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
  	return createInfo("remote", "Attach files or set attributes to outgoing email.  Must be inside a CFMAIL");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
   			createAttInfo( "FILE", "The path to the file to be attached", 	"", true ),
   			createAttInfo( "NAME", "The name of the mail attribute.  If specified then FILE is invalid", 	"", false ),
   			createAttInfo( "VALUE", "The value of the mail attribute.  If specified must be with NAME", 	"", false ),
   			createAttInfo( "DISPOSITION", "How to treat the file; either as INLINE or ATTACHMENT", 	"attachment", false ),
   			createAttInfo( "CONTENTID", "If DISPOSITION=INLINE then this is the content-id", 	"", false ),
   			createAttInfo( "TYPE", "If file, this is the MIME-TYPE of the file", 	"", false ),
   			createAttInfo( "URIDIRECTORY", "Is the path to the file relative to the document root", 	"false", false ),
  	};
  }
  
  protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute( "URIDIRECTORY", "NO" );
    defaultAttribute( "DISPOSITION", "ATTACHMENT" );
    parseTagHeader( _tag );

		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;
    
		if ( !containsAttribute("FILE") && ( !containsAttribute("VALUE") && !containsAttribute("NAME") )  )
    	throw newBadFileException( "Missing attribute", "Does not contain the necessary parameters" );
  }

  
	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if ( !containsAttribute(attributes,"FILE") && ( !containsAttribute(attributes,"VALUE") && !containsAttribute(attributes,"NAME") )  )
    	throw newBadFileException( "Missing attribute", "Does not contain the necessary parameters" );
    return	attributes;
	}

  
  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
  	cfStructData attributes = setAttributeCollection(_Session);
  	
  	//--[ Get the mail data to allow us to pass data back to the CFMAIL tag
		cfMailData	mailData	= (cfMailData)_Session.getDataBin("MAILDATA");
		if ( mailData == null )
      throw newRunTimeException( "You must have a CFMAILPARAM tag within a CFMAIL block" ); 

		//--[ See if this tag is a FILE attach tag
		if ( containsAttribute(attributes,"FILE") ){
			
			String filePath	= getDynamic( attributes,_Session, "FILE" ).getString();
			if ( getDynamic( attributes,_Session, "URIDIRECTORY" ).getBoolean() )
				filePath = FileUtils.getRealPath( _Session.REQ, filePath );
			
      if ( ( filePath == null ) || !new File( filePath ).exists() )
        throw newRunTimeException( "The file, " + filePath + ", doesn't exist" ); 

      String type = null;
      if ( containsAttribute( attributes,"TYPE" ) ){
        type = getDynamic( attributes,_Session, "TYPE" ).getString();
      }
      
      String disposition = getDynamic(attributes, _Session, "DISPOSITION" ).getString().toUpperCase();
      
      
      if ( disposition.equals( "INLINE" ) ){
        if ( !containsAttribute( attributes,"CONTENTID" )){
          throw newRunTimeException( "When attaching an image INLINE you must specify a CONTENTID" ); 
        }else{
          String contentid = getDynamic( attributes,_Session, "CONTENTID" ).getString();
          mailData.addFile( filePath, type, disposition, contentid );
        }
      }else{
        mailData.addFile( filePath, type );
      }
      
      
		}
			
		//--[ See if this tag is a NAME-VALUE tag
		if ( containsAttribute(attributes,"VALUE") && containsAttribute(attributes,"NAME") ){
			mailData.addHeader( getDynamic( attributes,_Session, "NAME" ).getString(), 
                          getDynamic( attributes,_Session, "VALUE" ).getString() );
		}
		
		return cfTagReturnType.NORMAL;
  }
}
