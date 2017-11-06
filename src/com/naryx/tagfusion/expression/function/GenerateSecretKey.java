/* 
 *  Copyright (C) 2000 - 2014 TagServlet Ltd
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
 *  $Id: GenerateSecretKey.java 2430 2014-03-30 19:50:30Z alan $
 */

package com.naryx.tagfusion.expression.function;

import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.nary.net.Base64;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;


public class GenerateSecretKey extends functionBase {

	private static final long serialVersionUID = 1L;


	public GenerateSecretKey() {
		max = 2;
		min = 1;
		setNamedParams( new String[] { "algorithm", "keysize" } );
	}


	public String[] getParamInfo() {
		return new String[] {
				"Key Algorithm to use valid values: 'des', 'desede', 'aes', 'blowfish'",
				"Keysize - defaults to 56 for DES, 168 for DESEDE, or 128 for BLOWFISH and AES"
		};
	}


	public java.util.Map getInfo() {
		return makeInfo(
				"security",
				"Generates a new secret key based on the algorithm",
				ReturnType.STRING );
	}


	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String algorithm = getNamedStringParam( argStruct, "algorithm", null );
		if ( algorithm == null )
			throwException( _session, "please provide the algorithm parameter" );

		algorithm = algorithm.toUpperCase();

		int keysize = getNamedIntParam( argStruct, "keysize", -1 );
		if ( keysize == -1 ) {
			if ( algorithm.equals( "DES" ) ) {
				keysize = 56;
			} else if ( algorithm.equals( "DESEDE" ) ) {
				keysize = 168;
			} else
				keysize = 128;
		}

		// else use the default - AES, BLOWFISH
		try {
			KeyGenerator kg = KeyGenerator.getInstance( algorithm );
			kg.init( keysize, new SecureRandom() );
			SecretKey key = kg.generateKey();
			return new cfStringData( new String( Base64.base64Encode( key.getEncoded() ), "latin1" ) );
		} catch ( Exception e ) {
			throwException( _session, "Failed to generate key. " + e.getMessage() );
		}

		return null; // keep compiler happy
	}

}
