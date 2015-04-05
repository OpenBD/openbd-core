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

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfLOCATION extends cfTag implements Serializable{
    static final long serialVersionUID = 1;
  
	public java.util.Map getInfo(){
		return createInfo("network", "This tag can stop execution of the current page and navigate to a new CFML/HTML page at the given URL.");
	}
  
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
				createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
				createAttInfo("URL", "The URL of a CFML/HTML page to open.", "", true ),
				createAttInfo("STATUSCODE", "The status code to indicate the reason for redirection. Valid options include: \"300\", \"301\", \"302\", \"303\", \"304\", \"305\", \"306\" or \"307\".", "302", false ),
				createAttInfo("ADDTOKEN", "If set to true then the current URL tokens will be appended to the URL of the new page that is opened.", "YES", false ),
				createAttInfo("ABORT", "If set to true then the current page processing will be stopped immmediatly, otherwise the page will be loaded before the redirection occurs.", "YES", false )
		};
	}

  protected void defaultParameters( String _tag ) throws cfmBadFileException {
    defaultAttribute( "ADDTOKEN", "YES" );
    defaultAttribute( "ABORT", "YES" );
		setFlushable( false );
    
    parseTagHeader( _tag );
    
		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;

		if ( !containsAttribute("URL") )
    	throw newBadFileException( "Missing URL", "You need to provide a URL" );
	}  
  
	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if ( !containsAttribute(attributes,"URL") )
    	throw newBadFileException( "Missing URL", "You need to provide a URL" );

		return	attributes;
	}

  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
  	cfStructData attributes = setAttributeCollection(_Session);
  	
  	String absURL	= getDynamic( attributes, _Session, "URL" ).getString();
  	if ((absURL.indexOf('?') == 0) || (absURL.indexOf('#') == 0)) { // fix for bug #3290
  		absURL = _Session.REQ.getRequestURI() + absURL;
    }

  	// Check to see if the current tokens is to be added on
  	if ( getDynamic( attributes, _Session, "ADDTOKEN" ).getBoolean() )
			absURL	= _Session.encodeURL( absURL );
  	
  	//Could work but only seems to affect mailto links? Servlet23.jar seems to always send 302 for web redirects?
  	int statusCode	= 302;
  	if ( containsAttribute(attributes, "STATUSCODE") ){
  		statusCode = getDynamic( _Session, "STATUSCODE" ).getInt();
  		if ((statusCode < 300) || (statusCode > 307)) {
    		throw newRunTimeException("Invalid STATUSCODE, valid values are \"300\", \"301\", \"302\", \"303\", \"304\", \"305\", \"306\" or \"307\".");
    	}
  	}
  	
  	_Session.sendRedirect( absURL, getDynamic( attributes, _Session, "ABORT" ).getBoolean(), statusCode );
		
  	return cfTagReturnType.NORMAL;
  }
}
