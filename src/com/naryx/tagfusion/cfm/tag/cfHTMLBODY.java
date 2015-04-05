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

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfHTMLBODY extends cfTag implements Serializable {
	static final long serialVersionUID = 1;
	private String END_MARKER = null;

  public java.util.Map getInfo(){
  	return createInfo("output", "Allows you to insert content into the HTML <body></body> area of the page.  The end tag is optional.  If omitted you may build content directly inside the tag.");
  }
  
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   		createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
 			createAttInfo("TEXT", 		"The content you wish to add to the body.  Omit if you wish to use the end marker tag", "", false ),
 			createAttInfo("POSITION", "APPEND|PREPEND controls where you wish the body text to appear within the body area", "APPEND", false ),
  	};
  }
	
	
	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseTagHeader(_tag);

		setFlushable(false);
		
		if ( !containsAttribute("TEXT") ){
			END_MARKER	= "</CFHTMLBODY>";
		}
	}

	public String getEndMarker(){
  	return END_MARKER;
  }
	
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
  	cfStructData attributes = setAttributeCollection(_Session);

		
		String htmlHeader;
		boolean append = true;
		
		if ( containsAttribute(attributes, "POSITION") ){
			if ( getDynamic(attributes, _Session,"POSITION").getString().equalsIgnoreCase("PREPEND") )
				append = false;
		}
		
		if ( END_MARKER == null ){
			htmlHeader	= getDynamic(attributes, _Session, "TEXT").getString().trim();
		}else{
			htmlHeader	= renderToString( _Session, cfTag.HONOR_CF_SETTING ).getOutput().trim();
		}
		
		_Session.setBodyElement( htmlHeader, append );
		return cfTagReturnType.NORMAL;
	}
}
