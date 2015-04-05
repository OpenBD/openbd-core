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
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfSAVECONTENT extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

	public String getEndMarker() {
		return "</CFSAVECONTENT>";
	}

	public java.util.Map getInfo() {
		return createInfo("output", "Allows you to capture complete blocks of output and instead of sending to the request, will catch them and store them into the variable provided");
	}

	public java.util.Map[] getAttInfo() {
		return new java.util.Map[] { 
   			createAttInfo("ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
				createAttInfo("VARIABLE", "The name of the variable that the content will be stored in", "", true),
				createAttInfo("TRIM", "Trim all the whitespace from the result content", "false", false)
		};
	}

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseTagHeader(_tag);
		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;

		if (!containsAttribute("VARIABLE"))
			throw newBadFileException("Missing Attribute", "You must provide a VARIABLE");
	}

	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if (!containsAttribute(attributes,"VARIABLE"))
			throw newBadFileException("Missing Attribute", "You must provide a VARIABLE");

		return	attributes;
	}

	
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);

		cfTagReturnType returnVar = renderToString(_Session, cfTag.HONOR_CF_SETTING);
		String body = returnVar.getOutput();
		
		if ( containsAttribute(attributes,"TRIM") && getDynamic(attributes,_Session, "TRIM").getBoolean() ){
			body	= body.trim();
		}
		
		_Session.setData(getDynamic(attributes,_Session, "VARIABLE").getString(), new cfStringData(body));

		return returnVar;
	}
}
