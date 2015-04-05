/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  $Id: cfHEADER.java 2390 2013-06-23 16:34:12Z alan $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfHEADER extends cfTag implements Serializable {
	static final long serialVersionUID = 1;


  @SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
  	return createInfo("output", "Sets the HTTP header of the outgoing request");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
   			createAttInfo( "NAME", "The name of the HTTP value", 	"", true ),
   			createAttInfo( "VALUE", "The VALUE of the of the response", 	"", false ),
   			createAttInfo( "CHARSET", "The character set of the outgoing request", 	"", false ),
   			createAttInfo( "STATUSCODE", "The number of the response code", 	"", false ),
   			createAttInfo( "STATUSTEXT", "The status text", 	"", false )
  	};
  }
	
	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseTagHeader(_tag);
		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;
		
		if (!containsAttribute("NAME") && !containsAttribute("STATUSCODE"))
			throw newBadFileException("Missing Attributes", "You need to provide either NAME/VALUE or STATUSCODE");

		if (containsAttribute("NAME") && !containsAttribute("VALUE"))
			throw newBadFileException("Missing Attributes", "You need to provide both NAME and VALUE attributes");

		if (containsAttribute("NAME") && containsAttribute("STATUSCODE"))
			throw newBadFileException("Too many attributes", "Only specify one pairing at a time. NAME/VALUE or STATUSCODE/STATUSTEXT");

		setFlushable(false);
	}
  
	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if (!containsAttribute(attributes,"NAME") && !containsAttribute(attributes,"STATUSCODE"))
			throw newBadFileException("Missing Attributes", "You need to provide either NAME/VALUE or STATUSCODE");

		if (containsAttribute(attributes,"NAME") && !containsAttribute(attributes,"VALUE"))
			throw newBadFileException("Missing Attributes", "You need to provide both NAME and VALUE attributes");

		if (containsAttribute(attributes,"NAME") && containsAttribute(attributes,"STATUSCODE"))
			throw newBadFileException("Too many attributes", "Only specify one pairing at a time. NAME/VALUE or STATUSCODE/STATUSTEXT");
    return	attributes;
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
  	cfStructData attributes = setAttributeCollection(_Session);

		if (containsAttribute(attributes,"NAME")) {
			String name = getDynamic(attributes,_Session, "NAME").getString();
			String value = null;
			
			if (containsAttribute(attributes,"CHARSET")) {
				String charset = getDynamic(attributes,_Session, "CHARSET").getString();
				try {
					value = new String(getDynamic(attributes,_Session, "VALUE").getString().getBytes(), charset);
				} catch (UnsupportedEncodingException u) {
					throw newRunTimeException("Unsupported CHARSET specified [" + charset + "]");
				}
			} else {
				value = getDynamic(attributes,_Session, "VALUE").getString();
			}

			// Apache 2.0.x on Linux will not allow setting the Content-Type header
			// via setHeader(),
			// so this check allows setting of Content-Type via CFHEADER
			// Using case-insensitive test here, is what fixes bug #2335
			if ("Content-Type".equalsIgnoreCase(name)) {
				_Session.setContentType(value);
			} else {
				_Session.setHeader(name, value);
			}
		} else {
			int code = getDynamic(attributes,_Session, "STATUSCODE").getInt();
			if (containsAttribute(attributes,"STATUSTEXT")) {
				String text = getDynamic(attributes,_Session, "STATUSTEXT").getString();
				_Session.setStatus(code, text);
			} else {
				_Session.setStatus(code);
			}
		}
		return cfTagReturnType.NORMAL;
	}
}
