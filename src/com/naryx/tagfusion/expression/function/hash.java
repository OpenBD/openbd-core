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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class hash extends functionBase {
	private static final long serialVersionUID = 1L;

	public hash() {
		min = 1;
		max = 3;
		setNamedParams( new String[]{ "data", "algorithm", "encoding" } );
	}

	public String[] getParamInfo() {
		return new String[] { 
			"string/binary",
			"algorithm ('MD5', 'CFMX_COMPAT' +other Java supported)",
			"encoding"
		};
	}

	public java.util.Map getInfo() {
		return makeInfo(
				"conversion", 
				"Determines the hash of the given string (or binary object) using the current encoding and algorithm", 
				ReturnType.STRING);
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		String algorithm = getNamedStringParam( argStruct, "algorithm", "MD5" ) ;
		String encoding = cfEngine.getDefaultCharset();
		cfData data = getNamedParam(argStruct, "data"); 

		boolean cfmx_compat = algorithm.equalsIgnoreCase("CFMX_COMPAT");
		// if encoding specified and the algorithm isn't CFMX_COMPAT
		if ( !cfmx_compat ) {
			encoding =  getNamedStringParam( argStruct, "encoding", encoding);
		}

		if (cfmx_compat) {
			algorithm = "MD5";
		}

		
		if ( data.getDataType() == cfData.CFBINARYDATA ){

			try{
				MessageDigest md = MessageDigest.getInstance(algorithm);
				byte[] output = md.digest( ((cfBinaryData)data).getByteArray() );
				return new cfStringData(BinaryEncode.encode(BinaryEncode.HEX, output));
			} catch (NoSuchAlgorithmException e) {
				throwException(_session, "No such algorithm: " + algorithm);
			} catch (Exception e) {
				throwException(_session, e.getMessage());
			}

		}else{
		
			try {
				MessageDigest md = MessageDigest.getInstance(algorithm);
				byte[] output = md.digest(data.getString().getBytes(encoding));
				return new cfStringData(BinaryEncode.encode(BinaryEncode.HEX, output));
	
			} catch (NoSuchAlgorithmException e) {
				throwException(_session, "No such algorithm: " + algorithm);
			} catch (UnsupportedEncodingException e) {
				throwException(_session, "Unsupported ENCODING: " + encoding);
			}

		}
		
		return null; // keep compiler happy
	}
}
