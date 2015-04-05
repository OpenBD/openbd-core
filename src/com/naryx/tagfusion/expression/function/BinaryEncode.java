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
 */

package com.naryx.tagfusion.expression.function;


import com.nary.net.Base64;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class BinaryEncode extends functionBase {

	private static final long serialVersionUID = 1L;

	public final static byte HEX = 0, UU = 1, BASE64 = 2;

	public BinaryEncode() {
		min = max = 2;
		setNamedParams( new String[]{ "data", "encoding" } );		
	}

	public String[] getParamInfo(){
		return new String[]{
			"binary data to encode",
			"encoding format ('hex', 'uu' or 'base64')"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"string", 
				"Returns the binary object encoded as a string using the encoding method given", 
				ReturnType.STRING );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		String encoding = getNamedStringParam( argStruct, "encoding" , "");
		cfData binData 	= getNamedParam( argStruct, "data" );

		if (binData.getDataType() != cfData.CFBINARYDATA) {
			throwException(_session, "Invalid argument. The first argument to this function must be a binary object.");
		}

		try{
			return new cfStringData(encode(_session, encoding, ((cfBinaryData) binData).getByteArray()));
		}catch( IllegalArgumentException e ){
			throwException(_session, e.getMessage() );
		}
		
		return null;
	}

	public static String encode(cfSession _session, String _encoding, byte[] _bytes) throws cfmRunTimeException {

		byte encByte;
		if (_encoding.equalsIgnoreCase("hex")) {
			encByte = HEX;
		} else if (_encoding.equalsIgnoreCase("uu")) {
			encByte = UU;
		} else if (_encoding.equalsIgnoreCase("base64")) {
			encByte = BASE64;
		} else {
			cfCatchData catchData = new cfCatchData(_session);
			catchData.setType("Expression");
			catchData.setMessage("Invalid binary encoding: " + _encoding + ". Expected one of Hex, UU and Base64.");
			throw new cfmRunTimeException(catchData);
		}

		return encode(encByte, _bytes);
	}

	public static String encode(byte _encoding, byte[] _bytes) {
		switch (_encoding) {
		case HEX:
			return com.nary.util.string.toHex(_bytes);
		case UU:
			return new String(Base64.uuencode(_bytes));
		default:
			return new String(Base64.base64Encode(_bytes));
		}
	}
}
