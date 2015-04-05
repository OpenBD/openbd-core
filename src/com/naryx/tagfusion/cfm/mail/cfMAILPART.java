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

/*
 * cfMAILPART is part of the CFMAIL tags
 * 
 */
package com.naryx.tagfusion.cfm.mail;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfMAILPART  extends cfTag implements Serializable {
  
	static final long serialVersionUID = 1;

	public String getEndMarker(){	return "</CFMAILPART>"; }


  @SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
  	return createInfo("remote", "Create MIME parts.  Must be inside a CFMAIL");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
   			createAttInfo( "TYPE", "The MIME type of this mail part", 	"", true ),
   			createAttInfo( "WRAP", "The column number to wrap this text", 	"", false )
  	};
  }
	
	
	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		parseTagHeader( _tag );
		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;

		if ( !containsAttribute("TYPE") )
			throw newBadFileException( "Missing attribute", "Does not contain TYPE attribute" );
	}
  
	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if ( !containsAttribute(attributes,"TYPE") )
			throw newBadFileException( "Missing attribute", "Does not contain TYPE attribute" );

		return	attributes;
	}

	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
  	cfStructData attributes = setAttributeCollection(_Session);
  
	  //--[ Get the mail data to allow us to pass data back to the CFMAIL tag
	  cfMailData	mailData	= (cfMailData)_Session.getDataBin("MAILDATA");
	  if ( mailData == null )
			throw newRunTimeException( "You must use a CFMAILPART tag within a CFMAIL block" ); 


		//-- Setup the mail data
		cfMailPartData	mailPartData = new cfMailPartData();
		
		mailPartData.setMimeType( getDynamic(attributes,_Session,"TYPE").getString() );
		if ( containsAttribute(attributes,"CHARSET") )
			mailPartData.setCharSet( getDynamic(attributes,_Session,"CHARSET").getString() );
			
		//-- Sort the content
		mailPartData.setContent( renderToString( _Session ).getOutput() );
		
		if ( containsAttribute(attributes,"WRAP") )
			mailPartData.wrapContent( getDynamic(attributes,_Session,"WRAP").getInt() );
			
		//-- Add the mail part
		mailData.addMailPart( mailPartData );
		
		return cfTagReturnType.NORMAL;
	}
}
