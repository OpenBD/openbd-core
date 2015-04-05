/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  
 *  $Id: $
 */

package com.naryx.tagfusion.cfm.xml;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfXML extends cfTag implements Serializable {

	static final long	serialVersionUID	= 1;



	public cfXML() {
	}



	public String getEndMarker() {
		return "</CFXML>";
	}



	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute("CASESENSITIVE", "no");
		parseTagHeader(_tag);
		if (!containsAttribute("VARIABLE"))
			throw newBadFileException("Missing Attribute", "You must provide a VARIABLE");
		else
			return;
	}



	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		String vName = getDynamic(_Session, "VARIABLE").getString();
		boolean caseSensitive = cfBooleanData.getcfBooleanData(getDynamic(_Session, "CASESENSITIVE").getString()).getBoolean();
		cfXmlData content = cfXmlData.parseXml(renderToString(_Session, cfTag.HONOR_CF_SETTING).getOutput().trim(), caseSensitive, null);
		_Session.setData(vName, content);

		return cfTagReturnType.NORMAL;
	}
}
