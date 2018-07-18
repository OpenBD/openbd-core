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
 *  
 *  Modifications by Marcus Fernstrom, May 2018. Contributed code given freely to the OpenBD project.
 * 
 *  PBKDF2 code is based on https://www.owasp.org/index.php/Hashing_Java (Licensed under https://creativecommons.org/licenses/by-sa/4.0/) and modified for OpenBD.
 */

package com.naryx.tagfusion.expression.function;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

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
		max = 7;
		setNamedParams( new String[]{ "data", "algorithm", "encoding", "iterations", "salt", "sha", "keylength" } );
	}

	@Override
	public String[] getParamInfo() {
		return new String[] { 
			"string/binary",
			"algorithm ('MD5', 'CFMX_COMPAT' +other Java supported)",
			"encoding",
			"iterations",
			"salt",
			"sha",
			"keylength"
		};
	}

	@SuppressWarnings("rawtypes")
	@Override
	public java.util.Map getInfo() {
		return makeInfo(
				"conversion", 
				"Determines the hash of the given string (or binary object) using the current encoding and algorithm", 
				ReturnType.STRING);
	}

	@Override
	public cfData execute(final cfSession _session, final cfArgStructData argStruct ) throws cfmRunTimeException {
		String algorithm 					= getNamedStringParam( argStruct, "algorithm", "MD5" ) ;
		final String salt 				= getNamedStringParam( argStruct, "salt", "" );
		final Integer iterations 	= getNamedIntParam( argStruct, "iterations", 1 );
		final Integer keylength 	= getNamedIntParam( argStruct, "keylength", 256 );
		final String sha 					= getNamedStringParam( argStruct, "sha", "SHA512" );
		String encoding 					= cfEngine.getDefaultCharset();
		final cfData data 				= getNamedParam(argStruct, "data");
		
		if( algorithm.equalsIgnoreCase("pbkdf2") ) {
			try {
				String stringToHash = null;
				if ( data.getDataType() == cfData.CFBINARYDATA ){
					stringToHash = new String( ((cfBinaryData)data).getByteArray() , "UTF-8");
				} else {
					stringToHash = data.toNormalString();
				}
				
				return new cfStringData( pbkdf2(_session, stringToHash, salt, sha, iterations, keylength) );
				
			} catch( Exception e ) {
				throwException(_session, e.getMessage());
			}
			
		} else {
			final boolean cfmx_compat = algorithm.equalsIgnoreCase("CFMX_COMPAT");
			// if encoding specified and the algorithm isn't CFMX_COMPAT
			if ( !cfmx_compat ) {
				encoding =  getNamedStringParam( argStruct, "encoding", encoding);
			}

			if (cfmx_compat) {
				algorithm = "MD5";
			}
			
			try {
				final MessageDigest md = MessageDigest.getInstance(algorithm);
				byte[] output;
				
				if ( data.getDataType() == cfData.CFBINARYDATA ){
					output = md.digest( ((cfBinaryData)data).getByteArray() );
				} else {
					output = md.digest( data.getString().getBytes(encoding) );
				}

				String outString = BinaryEncode.encode(BinaryEncode.HEX, output);
				
				if( iterations > 1 ) {
					for( Integer i = 1; i <= iterations -1; i++ ) {
						output = md.digest( outString.getBytes(encoding) );
						outString = BinaryEncode.encode(BinaryEncode.HEX, output);
					}
				}
				
				return new cfStringData(BinaryEncode.encode(BinaryEncode.HEX, output));
				
			} catch (NoSuchAlgorithmException e) {
				throwException(_session, "No such algorithm: " + algorithm);
				
			} catch (Exception e) {
				throwException(_session, e.getMessage());
			}
			
		}
		
		return null; // keep compiler happy
	}
	
	
	private String pbkdf2( final cfSession _session, final String data, final String salt, final String sha, final Integer iterations, final Integer keylength ) throws cfmRunTimeException, NoSuchAlgorithmException, InvalidKeySpecException {
		if( salt.length() == 0 ) {
			throwException( _session, "You have to specify a salt when using PBKDF2" );
		}
		
		final SecretKeyFactory skf 	= SecretKeyFactory.getInstance( "PBKDF2WithHmac" + sha );
		final char[] charArray 			= data.toCharArray();
		final byte[] saltArray 			= salt.getBytes();
		final PBEKeySpec spec 			= new PBEKeySpec( charArray, saltArray, iterations, keylength );
		final byte[] hash 					= skf.generateSecret( spec ).getEncoded();
		
		return BinaryEncode.encode(BinaryEncode.HEX, hash);
	}
}
