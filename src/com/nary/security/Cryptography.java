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

package com.nary.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.nary.net.Base64;

public class Cryptography {

	public static int ENCRYPT_MODE = 0, DECRYPT_MODE = 1;
	
	private static SecureRandom secureRandom = new SecureRandom();
	
  public static byte [] doCipher( int _mode, byte [] _data, String _fullAlgorithm, byte [] _key, byte [] _ivOrSalt, int _iterations ) throws GeneralSecurityException{
  	String algorithm;
  	int mode = ( _mode == ENCRYPT_MODE ? javax.crypto.Cipher.ENCRYPT_MODE : javax.crypto.Cipher.DECRYPT_MODE );
  	
  	String feedbackMode = "ecb";
  	int delimIndex = _fullAlgorithm.indexOf( '/' );
  	
  	if ( delimIndex != -1 ){
  		algorithm = _fullAlgorithm.substring( 0, delimIndex ).toLowerCase();
  		int secondDelim = _fullAlgorithm.indexOf( '/', delimIndex+1 );
  		if ( secondDelim == -1 ){
  			feedbackMode = _fullAlgorithm.substring( delimIndex+1 ).toLowerCase();
  		}else{
  			feedbackMode = _fullAlgorithm.substring( delimIndex+1, secondDelim ).toLowerCase();
  		}
  	}else{
  		algorithm = _fullAlgorithm.toLowerCase();
  	}
  	

  	byte [] ivOrSalt = _ivOrSalt;
  	byte [] data = _data;
  	boolean prependIV = false;
  	boolean ecbMode = feedbackMode.equals( "ecb" );
  	boolean isPBE = algorithm.startsWith( "pbe" );
  	
  	SecretKey secretKey =	new SecretKeySpec( Base64.base64Decode( _key ), algorithm ); 
  	
		Cipher cipher = Cipher.getInstance(_fullAlgorithm);

  	int ivSize = cipher.getBlockSize();

  	//-- work out the IV (or Salt) value 
  	if ( _ivOrSalt != null ){
    	ivOrSalt = _ivOrSalt;
    
    // if we're decrypting and the mode requires an IV and no IV has been specified we need to obtain it from the data
  	}else if ( mode == javax.crypto.Cipher.DECRYPT_MODE && ( isPBE || !ecbMode ) ){
  		ivOrSalt = new byte[ivSize];
  		data = new byte[ data.length - ivSize ];
  		System.arraycopy( _data, 0, ivOrSalt, 0, ivSize );
  		System.arraycopy( _data, ivSize, data, 0, data.length );

    // otherwise generate it. This should be prepended to the encrypted data
  	}else if ( isPBE || !ecbMode ){
  		ivOrSalt = new byte[ ivSize ];
	  	secureRandom.nextBytes( ivOrSalt );
  		prependIV = true;
  	}

		AlgorithmParameterSpec paramSpec = null;
		
		if ( isPBE ){ // i.e. this is Password Based Encryption alg
			paramSpec = new PBEParameterSpec( ivOrSalt, _iterations );
		}else if ( !ecbMode ){
			paramSpec = new IvParameterSpec( ivOrSalt );
		}
		
	  if ( paramSpec != null ){
			cipher.init( mode, secretKey,  paramSpec );
		}else{
			cipher.init( mode, secretKey );
		}
		
	  // if we're encrypting and had to generate the IV then prepend it to the encrypted data
	  if ( prependIV && ( isPBE || !ecbMode ) ){ //TODO: simplify
	  	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	  	try {
				bos.write( ivOrSalt );
		  	bos.write( cipher.doFinal( data ) );
			} catch (IOException e) { // not going to happen
				com.nary.Debug.printStackTrace(e);
			}
	  	return bos.toByteArray();
	  }else{
	  	return cipher.doFinal( data );
	  }
		  
	}
	
}
