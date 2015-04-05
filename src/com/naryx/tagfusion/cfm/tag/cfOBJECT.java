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

/**
 * CFObject
 * - this currently only supports Java objects and CFCs
 */

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfWSObjectData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;


public class cfOBJECT extends cfTag implements Serializable
{
	static final long serialVersionUID = 1;

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseTagHeader(_tag);
		
		if (!containsAttribute("COMPONENT") && !containsAttribute("WEBSERVICE")) {
			if (!containsAttribute("TYPE"))
				throw newBadFileException("Missing Attribute", "This tag requires the TYPE attribute to be specified.");
			if (!containsAttribute("CLASS"))
				throw newBadFileException("Missing Attribute", "This tag requires the CLASS attribute to be specified.");
		}
		if (!containsAttribute("NAME"))
			throw newBadFileException("Missing Attribute", "This tag requires the NAME attribute to be specified. This is the name that will be used to access the object.");
	}

	public String getEndMarker() {
		return null;
	}

	public cfTagReturnType render(cfSession session) throws cfmRunTimeException {
		String attribName = getDynamic(session, "NAME").getString();

		// TODO: for performance, the TYPE attribute can be converted to an integer within defaultParameters()
		if (containsAttribute("TYPE") && getConstant("TYPE").equalsIgnoreCase("JAVA") ) {
			String attribClass = getDynamic(session, "CLASS").getString();
			Class<?> c = null;

			// does the class exist?
			try {
				c = Class.forName(attribClass);
			}
			catch (ClassNotFoundException e) {
				cfCatchData catchData = new cfCatchData(session);
				catchData.setType(cfCatchData.TYPE_TEMPLATE);
				catchData.setDetail("CFOBJECT");
				catchData.setMessage("Failed to load class, " + attribClass);
				throw new cfmRunTimeException(catchData);
			}
			catch ( NoClassDefFoundError e ) {
				cfCatchData catchData = new cfCatchData(session);
				catchData.setType(cfCatchData.TYPE_TEMPLATE);
				catchData.setDetail("CFOBJECT");
				catchData.setMessage("Failed to load class, " + e.getMessage());
				throw new cfmRunTimeException(catchData);
			}

			// create a new cfJavaObjectData and stick it in the session with the name provided
			session.setData(attribName, new cfJavaObjectData(session, c));
		}
		else if (containsAttribute("COMPONENT")) {
			String attribComponent = getDynamic(session, "COMPONENT").getString();
			cfComponentData component = new cfComponentData(session, attribComponent);
			session.setData(attribName, component);
		}
		else if (containsAttribute("WEBSERVICE")) {
			String wsdlURL = getDynamic(session, "WEBSERVICE").getString();
			cfWSObjectData ws = new cfWSObjectData(session, wsdlURL);
			session.setData(attribName, ws);
		}
		else {
			cfCatchData catchData = new cfCatchData(session);
			catchData.setType( cfCatchData.TYPE_TEMPLATE );
			catchData.setMessage( "Invalid CFOBJECT attributes" );
			catchData.setDetail( "Must specify one of the following attributes: COMPONENT, WEBSERVICE, or TYPE=\"JAVA\"" );
			throw new cfmRunTimeException( catchData );
		}
		
		return cfTagReturnType.NORMAL;
	}
}
