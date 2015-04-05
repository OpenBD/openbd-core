/* 
 *  Copyright (C) 2012 TagServlet Ltd
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
 *  $Id: SecureUtils.java 2151 2012-07-04 13:46:43Z alan $
 */
package com.naryx.tagfusion.cfm.file.mapping;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import org.aw20.util.Base64;

public class SecureUtils extends Object {
	
	private static final String		ALGORITHM				= "AES";
	private static final String		TRANSFORMATION	= "AES/ECB/PKCS5Padding";
	public  static final String 	DEFAULT_KEY 		= "9QXxj+FB261fFeJWwwi1QQ==";
	
	
	public static byte[]	encryptData(String key, byte[] data) throws Exception{
		Cipher cipher = getEncryptCipher( key );
		return encryptData( cipher, data );
	}
	
	
	private static byte[]	encryptData(Cipher cf, byte[] data) throws Exception{
		ByteArrayOutputStream baos	= new ByteArrayOutputStream( data.length + 2048 );

		OutputStream os = new EncryptedOutputStream( cf, baos );
		os.write( data );
		os.flush();
		os.close();

		return baos.toByteArray();
	}
	

	public static byte[]	decryptData(String key, byte[] data) throws Exception{
		Cipher cipher = getDecryptCipher( key );
		return decryptData( cipher, data );
	}
	
	
	public static byte[]	decryptData( Cipher cf, byte[] data ) throws Exception {
		InputStream bis 					= new ByteArrayInputStream(data);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);

		int bufSize = cf.getBlockSize();
		byte[] buf = new byte[bufSize];
		byte[] out = null;

		int n = bis.read(buf, 0, bufSize);
		while (n != -1) {
			out = cf.update(buf, 0, n);
			bos.write(out);
			n = bis.read(buf, 0, bufSize);
		}
		out = cf.doFinal();
		bos.write(out);
		bos.flush();
		return bos.toByteArray();
	}

	
	private static Cipher getDecryptCipher( String cipher ) throws Exception {
		Key skey;
		skey = getBase64DecodeKey( cipher, ALGORITHM );
		Cipher cf = Cipher.getInstance(TRANSFORMATION);
		cf.init(Cipher.DECRYPT_MODE, skey);
		return cf;
	}

	
	
	private static Cipher getEncryptCipher( String cipher ) throws Exception {
		Key skey;
		skey = getBase64DecodeKey( cipher, ALGORITHM );
		Cipher cf = Cipher.getInstance(TRANSFORMATION);
		cf.init(Cipher.ENCRYPT_MODE, skey);
		return cf;
	}

	
	
	/**
	 * Generates a new key
	 * 
	 * @param algorithm
	 * @return
	 * @throws Exception
	 */
	public static String	generateKey() throws Exception {
		KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM);
		Key skey = generator.generateKey();
		return new String( Base64.encodeBytes( skey.getEncoded() ) );
	}
	
	

	
	/**
	 * Gets the Key object for a given string
	 * 
	 * @param key
	 * @param algorithm
	 * @return
	 * @throws Exception
	 */
	private static Key getBase64DecodeKey(String key, String algorithm) throws Exception{
		byte[] bkey = null;
		
		try {
			bkey = Base64.decode(key.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new Exception(e);
		}
		
		return new SecretKeySpec(bkey, algorithm);
	}
	
}