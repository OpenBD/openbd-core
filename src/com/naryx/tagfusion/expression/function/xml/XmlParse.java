/*
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: XmlParse.java 2506 2015-02-08 22:25:59Z alan $
 */

package com.naryx.tagfusion.expression.function.xml;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.expression.function.functionBase;

public class XmlParse extends functionBase {
	private static final long serialVersionUID = 1L;

	public XmlParse() {
		min = 1;
		max = 3;
	}


  public String[] getParamInfo(){
		return new String[]{
			"xmlstring - Can be a string, URL to a remote XML, or path to a file",
			"case sensitive - defaults to false",
			"validation xml - which document to validate against, same format as xmlstring"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"xml", 
				"Determines if the object passed in is an XML node", 
				ReturnType.BOOLEAN );
	}

		
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		String str = null;
		String validator = null;
		boolean caseSensitive = false;
		
		if (parameters.size() == 3) {
			str = parameters.get(2).getString().trim();
			caseSensitive = parameters.get(1).getBoolean();
			validator = parameters.get(0).getString().trim();
		} else if (parameters.size() == 2) {
			str = parameters.get(1).getString().trim();
			caseSensitive = parameters.get(0).getBoolean();
		} else {
			str = parameters.get(0).getString().trim();
		}

		return cfXmlData.parseXml( cfXmlData.interpretString(_session, str), caseSensitive, cfXmlData.interpretString(_session, validator));
	}

}
