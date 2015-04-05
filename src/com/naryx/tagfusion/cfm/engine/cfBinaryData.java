/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  $Id: $
 */

package com.naryx.tagfusion.cfm.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * The cfBinaryData class represents binary data, usually a non text file such
 * as an image or an executable.
 */

public class cfBinaryData extends cfData implements java.io.Serializable {

	static final long serialVersionUID = 1;

	private byte[] bytes;

	public cfBinaryData(InputStream inStream) {
		try {
			ByteArrayOutputStream Buffer = new ByteArrayOutputStream();
			// Note: Can't wrap the InputStream in a BufferedInputStream because
			// jdbc-odbc bridge doesn't support available() which is required
			// by the BufferedInputStream
			// BufferedInputStream inFile = new BufferedInputStream( inStream );

			byte in[] = new byte[2048];
			int c1;

			while ((c1 = inStream.read(in, 0, 2048)) != -1)
				Buffer.write(in, 0, c1);

			bytes = Buffer.toByteArray();
		} catch (IOException e) {
			cfEngine.log(e.getMessage());
		}
	}

	public cfBinaryData(byte binary[], int length ) {
		bytes = new byte[length];
		for ( int x=0; x < length; x++ )
			bytes[x] = binary[x];
	}

	public cfBinaryData(byte binary[]) {
		bytes = binary;
	}

	public cfData duplicate() {
		byte[] copybytes = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			copybytes[i] = bytes[i];
		}
		return new cfBinaryData(copybytes);
	}

	public void appendByteArray(byte[] byteArray) {
		byte[]	newByteArray = new byte[ bytes.length + byteArray.length ];
		
		for (int i = 0; i < bytes.length; i++)
			newByteArray[i] = bytes[i];
		
		for (int i = 0; i < byteArray.length; i++)
			newByteArray[ bytes.length + i ] = byteArray[i];
		
		bytes = newByteArray;
	}

	public byte[] getByteArray() {
		return bytes;
	}

	public byte getByteAt( int x ){
		return bytes[x];
	}
	
	public byte getDataType() {
		return CFBINARYDATA;
	}

	public String getDataTypeName() {
		return "binary";
	}

	public String getString() throws dataNotSupportedException {
		throw new dataNotSupportedException("Cannot convert binary data to string");
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(bytes.length);
		for (int i = 0; (i < bytes.length) && (i < 256); i++) {
			sb.append(bytes[i]);
		}
		if (sb.length() < bytes.length) {
			sb.append("<br>[truncated]");
		}
		return sb.toString();
	}

	public int getLength() {
		return bytes.length;
	}

	public void dump(java.io.PrintWriter out) {
		dump(out, "", -1);
	}

	public void dump(java.io.PrintWriter out, String _label, int _top) {
		out.write("<table class='cfdump_table_binary'>");
		out.write("<th class='cfdump_th_binary' colspan='2'>");
		if (_label.length() > 0) {
			out.write(_label);
			out.write(" - ");
		}
		out.write("binary</th>");
		out.write("<tr><td class='cfdump_td_value'>");
		out.write(toString());
		out.write("</td></tr>");
		out.write("</table>");
	}

	public void dumpWDDX(int version, java.io.PrintWriter out) {
		if (version > 10)
			out.write("<b l='");
		else
			out.write("<binary length='");

		out.write(bytes.length + "");
		out.write("'>");
		out.write(new String(com.nary.net.Base64.base64Encode(bytes)));

		if (version > 10)
			out.write("</b>");
		else
			out.write("</binary>");
	}

	public boolean equals(Object o) {
		if (o instanceof cfBinaryData)
			return Arrays.equals(bytes, ((cfBinaryData) o).bytes);

		return false;
	}

}
