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
 *  $Id: hmac.java 2385 2013-06-17 01:37:40Z alan $
 */
package com.naryx.tagfusion.expression.function;

import java.security.NoSuchAlgorithmException;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class hmac extends functionBase {
	private static final long serialVersionUID = 1L;

	public hmac() {
		min = 2;
		max = 4;
		setNamedParams( new String[]{ "message", "key", "algorithm", "encoding" } );
	}

	public String[] getParamInfo() {
		return new String[] { 
			"the message to encode",
			"the key to use for the encoding",
			"algorithm ('HmacMD5','HmacSHA1','HmacSHA256','HmacSHA384','HmacSHA512') defaults to HmacSHA1",
			"encoding"
		};
	}

	public java.util.Map getInfo() {
		return makeInfo(
				"conversion", 
				"Creates hash-based Message Authentication Code (HMAC) for the given string based on the algorithm and encoding, returning back the Base64 encoding", 
				ReturnType.STRING);
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		cfData messageData 	= getNamedParam(argStruct, "message"); 
		cfData keyData 			= getNamedParam(argStruct, "key"); 
		String algorithm 		= getNamedStringParam( argStruct, "algorithm", "HmacSHA1" ) ;
		String encoding 		= getNamedStringParam( argStruct, "encoding", cfEngine.getDefaultCharset() );

		try{
			javax.crypto.spec.SecretKeySpec	key;
			javax.crypto.Mac	mac	= javax.crypto.Mac.getInstance(algorithm);
			
			// Setup the key
			if ( keyData.getDataType() == cfData.CFBINARYDATA )
				key	= new javax.crypto.spec.SecretKeySpec( ((cfBinaryData)keyData).getByteArray(), algorithm );
			else
				key	= new javax.crypto.spec.SecretKeySpec( keyData.getString().getBytes(encoding), algorithm );
			
			mac.init(key);
			
			// Encode the message
			byte[] resultBytes;
			if ( messageData.getDataType() == cfData.CFBINARYDATA )
				resultBytes	= mac.doFinal( ((cfBinaryData)messageData).getByteArray() );
			else
				resultBytes	= mac.doFinal( messageData.getString().getBytes(encoding) );
			
			return new cfStringData(BinaryEncode.encode(BinaryEncode.BASE64, resultBytes));
			
		} catch (NoSuchAlgorithmException e) {
			throwException(_session, "No such algorithm: " + algorithm);
		} catch (Exception e) {
			throwException(_session, e.getMessage());
		}

		return null; // keep compiler happy
	}
}
