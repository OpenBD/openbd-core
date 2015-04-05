/* 
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
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
 *  $Id: hashBinary.java 2488 2015-01-28 01:35:58Z alan $
 */

package com.naryx.tagfusion.expression.function;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.aw20.io.StreamUtil;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class hashBinary extends functionBase {
	private static final long serialVersionUID = 1L;

	public hashBinary() {
		min = 1;
		max = 2;
		setNamedParams( new String[]{ "data", "algorithm" } );
	}

	public String[] getParamInfo() {
		return new String[] { 
			"data filename/binary object",
			"algorithm ('MD5', 'SHA-1', 'SHA-256')"
		};
	}

	public java.util.Map getInfo() {
		return makeInfo(
				"conversion", 
				"Determines the hash of the given binary object, or the file represented by the full path", 
				ReturnType.STRING);
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		
		String algorithm = getNamedStringParam( argStruct, "algorithm", "MD5" ) ;
		cfData data = getNamedParam(argStruct, "data"); 
		
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
			File	fileToRead	= new File( data.getString() );
			if ( !fileToRead.exists() )
				throwException(_session, "The file specified; " + fileToRead + "; does not exist" );
			
			try {
				return new cfStringData( getFileHash( fileToRead, algorithm ) );
			} catch (NoSuchAlgorithmException e) {
				throwException(_session, "No such algorithm: " + algorithm);
			} catch (Exception e) {
				throwException(_session, e.getMessage());
			}
		}
		
		return null; // keep compiler happy
	}

	
	/**
	 * Returns the hash code the given file
	 * 
	 * @param fileToRead
	 * @param algorithm
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public String getFileHash( File fileToRead, String algorithm ) throws NoSuchAlgorithmException, IOException{
		
		FileInputStream	inFile = null;
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			inFile	= new FileInputStream( fileToRead );
			
			DigestInputStream	digFile	= new DigestInputStream( inFile, md );

			byte[]	buffer	= new byte[4096];
			while ( digFile.read(buffer) != -1 );
			
			digFile.close();
		
			return BinaryEncode.encode(BinaryEncode.HEX, md.digest() );
		
		} finally {
			StreamUtil.closeStream(inFile);
		}
		
	}
}
