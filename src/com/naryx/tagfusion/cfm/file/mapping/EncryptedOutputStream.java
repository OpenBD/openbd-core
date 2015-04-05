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
 *  $Id: EncryptedOutputStream.java 2151 2012-07-04 13:46:43Z alan $
 */
package com.naryx.tagfusion.cfm.file.mapping;

import java.io.IOException;
import java.io.OutputStream;

import javax.crypto.Cipher;


/**
 * Class that provides the wrapper to the encryption for outgoing streams
 * 
 * It utilises the users established cipher key
 * 
 * @author alan
 *
 */
public class EncryptedOutputStream extends OutputStream {
	private OutputStream	os;
	
	private Cipher cf;
	private int bufSize;
	private byte[] buffer;
	private int bufPos = 0;
	
	public EncryptedOutputStream( Cipher cf, OutputStream os ) throws Exception {
		this.os	= os;
		this.cf	= cf;
		bufSize = cf.getBlockSize();
		buffer 	= new byte[bufSize];
	}
	
	public void write(int b) throws IOException {
		buffer[ bufPos++ ]	= (byte)b;
		if ( bufPos < bufSize ){
			return;
		}

		// Encrypt it and send it out
		byte[] out = cf.update(buffer, 0, bufSize);
		os.write(out);
		bufPos = 0;
	}
	
	public void flush() throws IOException {
		
		byte[] out;
		if ( bufPos > 0 ){
			out = cf.update(buffer, 0, bufPos);
			os.write(out);
		}
		
		try {
			out = cf.doFinal();
			os.write(out);
			os.flush();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
