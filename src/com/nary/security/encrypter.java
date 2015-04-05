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

/**
 * This class encrypts and decrypts Strings and uses Base64 encoding to keep them below 128
 *
 */
import com.nary.net.Base64;

public class encrypter extends Object {

	/*
	 * encryptDBpassword
	 * 
	 * In older versions of BD the DB passwords weren't encrypted. To support
	 * backwards compatibility, we prefix a DB password with "enc-" so the decryptDBpassword
	 * can determine if it is a new encrypted password or old unencrypted password.
	 */
	public static String encryptDBpassword( String _inString, String _key ){
		if ( ( _inString == null ) || ( _inString.length() == 0 ) )
			return _inString;

		return encrypt( "enc-" + _inString, _key );
	}

	public static String decryptDBpassword( String _inString, String _key ){
		if ( ( _inString == null ) || ( _inString.length() == 0 ) )
			return _inString;

		try {
			String pwd = decrypt( _inString, _key );
			if ( pwd.startsWith("enc-") )
				return pwd.substring("enc-".length());
		} catch ( Exception exc ) {
			// ignore
		}
	
		// Must be an old unencrypted password so just return the passed in string
		return _inString;
	}
	
  /**
   *	encrypt
   *
   *	Encrypt a string value using the given key:
   *
   *		1. generate a 32-bit int key from the key string
   *		2. mask (XOR) the string using the int key
   *		3. Base64-encode the masked string
   */
  public static String encrypt( String _inString, String _key ){
    return Base64.base64Encode( mask( _inString, generateKey( _key  ) ) );
  }
  
  public static byte [] encrypt( byte [] _inbytes, String _key ){
  	return mask( _inbytes, generateKey( _key ) );
  }

  /**
   *	decrypt
   *
   *	Decrypt a string; must use the same key used to encrypt:
   *
   *		1. Un-Base64-encode the string
   *		2. generate a 32-bit int key from the key string
   *		3. unmask (XOR) the string using the int key
   */
  public static String decrypt( String _inString, String _key ) throws Exception{
  
    return mask( Base64.base64Decode( _inString ), generateKey( _key ) );
  }

  public static byte [] decrypt( byte [] _inbytes, String _key ){
  	return mask( _inbytes, generateKey( _key ) );
  }

  /**
   *	generateKey
   *
   *	Give an key string, generate a 32-bit int key by adding up the numeric
   *	values of the bytes in the key string and using that as a seed to create
   *	a psuedo-random int.
   */
  private static int generateKey( String _key ) {
  
   	byte[] keyBytes = _key.getBytes();
      
    long seed = 0;
      
    for ( int i = 0; i < keyBytes.length; i++ ) {
      seed += keyBytes[ i ];
    }
    
    return new java.util.Random( seed ).nextInt();
  }
  
  /**
   *	mask
   *
   *	Mask a string using a 32-bit int mask by applying successive bytes
   *	from the mask to the characters in the string. So character 0 of the
   *	string gets masked by byte 0, character 1 by byte 1, etc., until
   *	character 4 gets masekd by byte 0 again.
   */
  private static String mask( String s, int mask ) {
  
  	char[] maskBytes = new char[ 4 ];
  	
  	maskBytes[ 0 ] = (char)(mask & 0x00FF);
  	maskBytes[ 1 ] = (char)((mask >> 8) & 0x00FF);
  	maskBytes[ 2 ] = (char)((mask >> 16) & 0x00FF);
  	maskBytes[ 3 ] = (char)((mask >> 24) & 0x00FF);
  
  	char[] c = s.toCharArray();
  	
  	for ( int i = 0; i < c.length; i ++ ) {
  		c[ i ] = (char)(c[ i ] ^ maskBytes[ i % 4 ]);
  	}
  	
  	return new String( c );
  }

  private static byte [] mask( byte [] b, int mask ) {
    
  	byte[] maskBytes = new byte[ 4 ];
  	
  	maskBytes[ 0 ] = (byte)(mask & 0x00FF);
  	maskBytes[ 1 ] = (byte)((mask >> 8) & 0x00FF);
  	maskBytes[ 2 ] = (byte)((mask >> 16) & 0x00FF);
  	maskBytes[ 3 ] = (byte)((mask >> 24) & 0x00FF);
  
  	
  	byte [] newb = new byte[b.length];
  	for ( int i = 0; i < b.length; i ++ ) {
  		newb[ i ] = (byte) ( b[ i ] ^ maskBytes[ i % 4 ] );
  	}
  	
  	return newb;
  }


  /**
   *	encrypt-old
   *
   *	Encrypt a string value using the given key:
   *
   *	!* old algorithm, restored to support previous installations;
   *		although, this is not used since only decryption is necessary.
   */
  public static String encrypt_old( String _inString, String _key )
  {
   
    char [] key = _key.toCharArray();
    int k = 0;

    for ( int x = 0; x < key.length; x++ ){
		k += key[x];
    }
    
    char [] value = _inString.toCharArray();

    for ( int x = 0; x < value.length; x++ ){
		value[x] += k;
    }
     
    return com.nary.net.Base64.base64Encode( new String( value ) );

  }


  /**
   *	decrypt-old
   *
   *	Decrypt a string; must use the same key used to encrypt:
   *
   *	!* old algorithm, restored to support previous installations.
   */
  public static String decrypt_old( String _inString, String _key ) throws Exception
  {
    String inString = "";
	try{
		inString = com.nary.net.Base64.base64Decode( _inString );
	}catch(Exception e){ throw new Exception(); }
    
    char [] key = _key.toCharArray();
    
    int k = 0;
    
    for ( int x = 0; x < key.length; x++ ){
		k += key[x];
    }
    
    char [] value = inString.toCharArray();
    
    for ( int x = 0; x < value.length; x++ ){
		value[x] -= k;
    }

    return new String( value );
  }


}
