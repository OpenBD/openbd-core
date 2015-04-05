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

package com.naryx.tagfusion.expression.function;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class urlEncodedFormat extends functionBase {

	private static final long serialVersionUID = 1L;

	public urlEncodedFormat() {
		min = 1;
		max = 2;
	}

  public String[] getParamInfo(){
		return new String[]{
			"string",
			"encoding - default encoding of the URL/FORM"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"format", 
				"Encodes the string for use within a URL parameter, using the given encoding", 
				ReturnType.STRING );
	}

	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		boolean charsetSpecified = parameters.size() == 2;
		int dataIndex = charsetSpecified ? 1 : 0;

		String encoding;
		String data = parameters.get(dataIndex).getString();

		if (charsetSpecified) {
			encoding = parameters.get(0).getString();
		} else {
			encoding = _session.RES.getCharacterEncoding();
		}

		try {
			String s1 = com.nary.net.http.urlEncoder.encode(data, encoding);
			return new cfStringData(s1);
		} catch (UnsupportedEncodingException u) {
			throwException(_session, "Unsupported charset specified [" + encoding + "].");
		}
		return null; // keeps compiler happy
	}
}
