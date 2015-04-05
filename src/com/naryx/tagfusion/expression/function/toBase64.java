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

import com.nary.net.Base64;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * This takes in an object and attempts to convert it to Base64 for encoding.
 * Particularly useful in around dealing with binary objects.
 */

public class toBase64 extends functionBase {

	private static final long serialVersionUID = 1L;

	public toBase64() {
		min = 1;
		max = 2;
		setNamedParams( new String[]{ "string", "encoding" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"string/binary",
			"encoding"
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"conversion", 
				"This takes in a string/binary object and attempts to convert it to a Base64 string", 
				ReturnType.STRING );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		cfData data = getNamedParam(argStruct, "string");
		String encoding = getNamedStringParam( argStruct, "encoding", null );

		if (data.getDataType() == cfData.CFBINARYDATA) { // if encoding is
																											// specified, it is
																											// ignored for this type
																											// of data
				return new cfStringData(new String(Base64.base64Encode(((cfBinaryData) data).getByteArray())));
		} else if( encoding!=null ) {
			encoding = com.nary.util.Localization.convertCharSetToCharEncoding(encoding);
			try {
				return new cfStringData(new String(Base64.base64Encode(data.getString().getBytes(encoding))));
			} catch (UnsupportedEncodingException uee) {
				throwException(_session, "Unsupported encoding specified [" + encoding + "].");
				return null; // keeps compiler happy
			}
		} else {
			String encoded = Base64.base64Encode(data.getString());
			return new cfStringData(encoded);
		}
	}
}
