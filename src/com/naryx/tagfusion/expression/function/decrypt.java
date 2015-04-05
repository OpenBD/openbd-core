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
 *  http://www.openbd.org/
 *  $Id: decrypt.java 2461 2014-12-17 11:48:57Z alan $
 */

package com.naryx.tagfusion.expression.function;

import java.util.List;

import com.nary.security.Cryptography;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;


public class decrypt extends encrypt {

	private static final long serialVersionUID = 1L;


	public java.util.Map<String,String> getInfo() {
		return makeInfo(
				"security",
				"Decrypts the given string with the optional parameters",
				ReturnType.STRING );
	}


	public String[] getParamInfo() {
		return new String[] {
				"string to decrypt",
				"encryption key",
				"encryption algorithm to be applied. If not specified, a default of BD_DEFAULT will be used. The CFMX_COMPAT algorithm option is not supported.",
				"the encoding - uu (default), hex or base64 are valid options",
				"the salt to be applied in encryption",
				"the number of iterations"
		};
	}


	public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
		// parameters - string, key[, algorithm[, encoding[, IVorSalt[,
		// iterations]]]]
		String data = parameters.get( parameters.size() - 1 ).getString();
		String key = parameters.get( parameters.size() - 2 ).getString();

		String fullAlgorithm = getAlgorithm( _session, parameters );

		if ( fullAlgorithm.equalsIgnoreCase( "BD_DEFAULT" ) ) {
			try {
				return new cfStringData( com.nary.security.encrypter.decrypt( data, key ) );
			} catch ( Exception e ) {
				cfCatchData catchData = new cfCatchData( _session );
				catchData.setType( "Add instance" );
				catchData.setDetail( "Decrypt" );
				catchData.setMessage( "Decryption failed. Is the String encrypted?; " + e );
				throw new cfmRunTimeException( catchData );
			}
		} else if ( fullAlgorithm.equalsIgnoreCase( "BDNET62_COMPAT" ) ) {
			try {
				return new cfStringData( com.nary.security.encrypter.decrypt( data, key ) );
			} catch ( Exception e ) {
				cfCatchData catchData = new cfCatchData( _session );
				catchData.setType( "Add instance" );
				catchData.setDetail( "Decrypt" );
				catchData.setMessage( "Decryption failed. Is the String encrypted?; " + e );
				throw new cfmRunTimeException( catchData );
			}
		}

		String encoding = getEncoding( _session, parameters );
		byte[] ivOrSalt = getIvOrSalt( _session, parameters, 4 );

		int iterations = getIterations( _session, parameters, 5 );
		try {
			byte[] decrypted = Cryptography.doCipher( Cryptography.DECRYPT_MODE, BinaryDecode.decode( _session, encoding, data ), fullAlgorithm, key.getBytes(), ivOrSalt, iterations );

			return new cfStringData( new String( decrypted ) );
		} catch ( Exception e ) {
			throwException( _session, e.getMessage() );
			return null; // keep compiler happy
		}
	}

}
