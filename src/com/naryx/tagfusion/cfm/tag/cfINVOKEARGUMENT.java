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
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfINVOKEARGUMENT extends cfTag implements Serializable {

	static final long serialVersionUID = 1;

	public String getEndMarker() {
		return null;
	}

	public java.util.Map getInfo() {
		return createInfo("remote", "For use within the CFINVOKE tag.  This tag allows you to associate a parameter with the remote web services call.");
	}

	public java.util.Map[] getAttInfo() {
		return new java.util.Map[] { 
				createAttInfo("NAME", "The name of the variable you wish to set in this web services argument", "", true),
				createAttInfo("VALUE", "The value of the variable you wish to set in this web services argument", "", true)
		};
	}
	
	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseTagHeader(_tag);
		if (!containsAttribute("NAME"))
			throw newBadFileException("Missing Attribute", "You must specify a NAME attribute for CFINVOKEARGUMENT");
		if (!containsAttribute("VALUE"))
			throw newBadFileException("Missing Attribute", "You must specify a VALUE attribute for CFINVOKEARGUMENT");
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		Map<cfData, cfData[]> invokeArgs = (Map<cfData, cfData[]>) _Session.getDataBin(cfINVOKECommon.DATA_BIN_KEY);
		if (invokeArgs == null) {
			throw newRunTimeException("CFINVOKEARGUMENT must be nested within a CFINVOKE tag");
		}
		cfBooleanData omitted = ((containsAttribute("OMIT") && getDynamic(_Session, "OMIT").getBoolean()) ? cfBooleanData.TRUE : cfBooleanData.FALSE);
		invokeArgs.put(getDynamic(_Session, "NAME"), new cfData[] { getDynamic(_Session, "VALUE"), omitted });

		return cfTagReturnType.NORMAL;
	}

}
