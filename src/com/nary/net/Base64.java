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

package com.nary.net;

import java.io.ByteArrayOutputStream;
import java.util.BitSet;

/**
 * This class collects various encoders and decoders.
 */

public class Base64 {

	private static BitSet	BoundChar;
	private static BitSet	EBCDICUnsafeChar;

	private static byte[]	Base64EncMap, Base64DecMap;
	private static char[]	UUEncMap;
	private static byte[]	UUDecMap;

	// Class Initializer

	static {
		// rfc-2046 & rfc-2045: (bcharsnospace & token)
		// used for multipart codings
		BoundChar = new BitSet( 256 );
		for (int ch = '0'; ch <= '9'; ch++)
			BoundChar.set( ch );
		for (int ch = 'A'; ch <= 'Z'; ch++)
			BoundChar.set( ch );
		for (int ch = 'a'; ch <= 'z'; ch++)
			BoundChar.set( ch );
		BoundChar.set( '+' );
		BoundChar.set( '_' );
		BoundChar.set( '-' );
		BoundChar.set( '.' );

		// EBCDIC unsafe characters to be quoted in quoted-printable
		// See first NOTE in section 6.7 of rfc-2045
		EBCDICUnsafeChar = new BitSet( 256 );
		EBCDICUnsafeChar.set( '!' );
		EBCDICUnsafeChar.set( '"' );
		EBCDICUnsafeChar.set( '#' );
		EBCDICUnsafeChar.set( '$' );
		EBCDICUnsafeChar.set( '@' );
		EBCDICUnsafeChar.set( '[' );
		EBCDICUnsafeChar.set( '\\' );
		EBCDICUnsafeChar.set( ']' );
		EBCDICUnsafeChar.set( '^' );
		EBCDICUnsafeChar.set( '`' );
		EBCDICUnsafeChar.set( '{' );
		EBCDICUnsafeChar.set( '|' );
		EBCDICUnsafeChar.set( '}' );
		EBCDICUnsafeChar.set( '~' );

		// rfc-2045: Base64 Alphabet
		byte[] map = { (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E',
			(byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K',
			(byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q',
			(byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W',
			(byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b', (byte) 'c',
			(byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i',
			(byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o',
			(byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u',
			(byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z', (byte) '0',
			(byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6',
			(byte) '7', (byte) '8', (byte) '9', (byte) '+', (byte) '/' };
		Base64EncMap = map;
		Base64DecMap = new byte[128];
		for (int idx = 0; idx < Base64EncMap.length; idx++)
			Base64DecMap[Base64EncMap[idx]] = (byte) idx;

		// browsers convert '+' to ' ' when storing Base64-encoded strings in
		// cookies;
		// therefore, treat ' ' as '+' when decoding
		Base64DecMap[' '] = Base64DecMap['+'];

		// uuencode'ing maps
		UUEncMap = new char[64];
		for (int idx = 0; idx < UUEncMap.length; idx++)
			UUEncMap[idx] = (char) ( idx + 0x20 );
		UUDecMap = new byte[128];
		for (int idx = 0; idx < UUEncMap.length; idx++)
			UUDecMap[UUEncMap[idx]] = (byte) idx;
	}

	// Constructors

	/**
	 * This class isn't meant to be instantiated.
	 */
	private Base64() {
	}

	// Methods

	/**
	 * This method encodes the given string using the base64-encoding specified in
	 * RFC-2045 (Section 6.8). It's used for example in the "Basic" authorization
	 * scheme.
	 * 
	 * @param str
	 *          the string
	 * @return the base64-encoded <var>str</var>
	 */
	@SuppressWarnings("deprecation")
	public final static String base64Encode( String str ) {
		if ( str == null )
			return null;

		byte data[] = new byte[str.length()];
		str.getBytes( 0, str.length(), data, 0 );

		return new String( base64Encode( data ), 0 );
	}

	/**
	 * This method encodes the given byte[] using the base64-encoding specified in
	 * RFC-2045 (Section 6.8).
	 * 
	 * @param data
	 *          the data
	 * @return the base64-encoded <var>data</var>
	 */
	public final static byte[] base64Encode( byte[] data ) {
		if ( data == null )
			return null;

		int sidx, didx;
		byte dest[] = new byte[( ( data.length + 2 ) / 3 ) * 4];

		// 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
		for (sidx = 0, didx = 0; sidx < data.length - 2; sidx += 3) {
			dest[didx++] = Base64EncMap[( data[sidx] >>> 2 ) & 077];
			dest[didx++] = Base64EncMap[( data[sidx + 1] >>> 4 ) & 017
					| ( data[sidx] << 4 ) & 077];
			dest[didx++] = Base64EncMap[( data[sidx + 2] >>> 6 ) & 003
					| ( data[sidx + 1] << 2 ) & 077];
			dest[didx++] = Base64EncMap[data[sidx + 2] & 077];
		}
		if ( sidx < data.length ) {
			dest[didx++] = Base64EncMap[( data[sidx] >>> 2 ) & 077];
			if ( sidx < data.length - 1 ) {
				dest[didx++] = Base64EncMap[( data[sidx + 1] >>> 4 ) & 017
						| ( data[sidx] << 4 ) & 077];
				dest[didx++] = Base64EncMap[( data[sidx + 1] << 2 ) & 077];
			} else
				dest[didx++] = Base64EncMap[( data[sidx] << 4 ) & 077];
		}

		// add padding
		for (; didx < dest.length; didx++)
			dest[didx] = (byte) '=';

		return dest;
	}

	/**
	 * This method decodes the given string using the base64-encoding specified in
	 * RFC-2045 (Section 6.8).
	 * 
	 * @param str
	 *          the base64-encoded string.
	 * @return the decoded <var>str</var>.
	 */
	@SuppressWarnings("deprecation")
	public final static String base64Decode( String str ) {
		if ( str == null )
			return null;

		byte data[] = new byte[str.length()];
		str.getBytes( 0, str.length(), data, 0 );

		return new String( base64Decode( data ), 0 );
	}

	/**
	 * This method decodes the given byte[] using the base64-encoding specified in
	 * RFC-2045 (Section 6.8).
	 * 
	 * @param data
	 *          the base64-encoded data.
	 * @return the decoded <var>data</var>.
	 */
	public final static byte[] base64Decode( byte[] data ) {
		if ( data == null )
			return null;
		if ( data.length == 0 )
			return data;

		int tail = data.length;
		while (data[tail - 1] == '=')
			tail--;

		byte dest[] = new byte[tail - data.length / 4];

		// ascii printable to 0-63 conversion
		for (int idx = 0; idx < data.length; idx++)
			data[idx] = Base64DecMap[data[idx]];

		// 4-byte to 3-byte conversion
		int sidx, didx;
		for (sidx = 0, didx = 0; didx < dest.length - 2; sidx += 4, didx += 3) {
			dest[didx] = (byte) ( ( ( data[sidx] << 2 ) & 255 ) | ( ( data[sidx + 1] >>> 4 ) & 003 ) );
			dest[didx + 1] = (byte) ( ( ( data[sidx + 1] << 4 ) & 255 ) | ( ( data[sidx + 2] >>> 2 ) & 017 ) );
			dest[didx + 2] = (byte) ( ( ( data[sidx + 2] << 6 ) & 255 ) | ( data[sidx + 3] & 077 ) );
		}
		if ( didx < dest.length )
			dest[didx] = (byte) ( ( ( data[sidx] << 2 ) & 255 ) | ( ( data[sidx + 1] >>> 4 ) & 003 ) );
		if ( ++didx < dest.length )
			dest[didx] = (byte) ( ( ( data[sidx + 1] << 4 ) & 255 ) | ( ( data[sidx + 2] >>> 2 ) & 017 ) );

		return dest;
	}

	/**
	 * This method encodes the given byte[] using the unix uuencode encding. The
	 * output is split into lines starting with the encoded number of encoded
	 * octets in the line and ending with a newline. No line is longer than 45
	 * octets (60 characters), not including length and newline.
	 * 
	 * <P>
	 * <em>Note:</em> just the raw data is encoded; no 'begin' and 'end' lines
	 * are added as is done by the unix <code>uuencode</code> utility.
	 * 
	 * @param data
	 *          the data
	 * @return the uuencoded <var>data</var>
	 */
	public final static char[] uuencode( byte[] data ) {
		if ( data == null )
			return null;
		if ( data.length == 0 )
			return new char[0];

		int line_len = 45; // line length, in octets

		int sidx, didx;
		char nl[] = new char[]{ '\n' }, dest[] = new char[( data.length + 2 )
				/ 3
				* 4
				+ ( ( data.length + line_len - 1 ) / line_len )
				* ( nl.length + 1 )];

		// split into lines, adding line-length and line terminator
		for (sidx = 0, didx = 0; sidx + line_len < data.length;) {
			// line length
			dest[didx++] = UUEncMap[line_len];

			// 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
			for (int end = sidx + line_len; sidx < end; sidx += 3) {
				dest[didx++] = UUEncMap[( data[sidx] >>> 2 ) & 077];
				dest[didx++] = UUEncMap[( data[sidx + 1] >>> 4 ) & 017
						| ( data[sidx] << 4 ) & 077];
				dest[didx++] = UUEncMap[( data[sidx + 2] >>> 6 ) & 003
						| ( data[sidx + 1] << 2 ) & 077];
				dest[didx++] = UUEncMap[data[sidx + 2] & 077];
			}

			// line terminator
			for (int idx = 0; idx < nl.length; idx++)
				dest[didx++] = nl[idx];
		}

		// last line

		// line length
		dest[didx++] = UUEncMap[data.length - sidx];

		// 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
		for (; sidx + 2 < data.length; sidx += 3) {
			dest[didx++] = UUEncMap[( data[sidx] >>> 2 ) & 077];
			dest[didx++] = UUEncMap[( data[sidx + 1] >>> 4 ) & 017
					| ( data[sidx] << 4 ) & 077];
			dest[didx++] = UUEncMap[( data[sidx + 2] >>> 6 ) & 003
					| ( data[sidx + 1] << 2 ) & 077];
			dest[didx++] = UUEncMap[data[sidx + 2] & 077];
		}

		if ( sidx < data.length - 1 ) {
			dest[didx++] = UUEncMap[( data[sidx] >>> 2 ) & 077];
			dest[didx++] = UUEncMap[( data[sidx + 1] >>> 4 ) & 017
					| ( data[sidx] << 4 ) & 077];
			dest[didx++] = UUEncMap[( data[sidx + 1] << 2 ) & 077];
			dest[didx++] = UUEncMap[0];
		} else if ( sidx < data.length ) {
			dest[didx++] = UUEncMap[( data[sidx] >>> 2 ) & 077];
			dest[didx++] = UUEncMap[( data[sidx] << 4 ) & 077];
			dest[didx++] = UUEncMap[0];
			dest[didx++] = UUEncMap[0];
		}

		// line terminator
		for (int idx = 0; idx < nl.length; idx++)
			dest[didx++] = nl[idx];

		// sanity check
		if ( didx != dest.length )
			throw new Error( "Calculated " + dest.length + " chars but wrote " + didx
					+ " chars!" );

		return dest;
	}

	/**
	 * This method decodes the given uuencoded char[].
	 * 
	 * <P>
	 * <em>Note:</em> just the actual data is decoded; any 'begin' and 'end'
	 * lines such as those generated by the unix <code>uuencode</code> utility
	 * must not be included.
	 * 
	 * @param data
	 *          the uuencode-encoded data.
	 * @return the decoded <var>data</var>.
	 */
	public final static byte[] uudecode( char[] data ) {
		if ( data == null )
			return null;

		int sidx, didx;
		ByteArrayOutputStream bos = new ByteArrayOutputStream( data.length );

		for (sidx = 0, didx = 0; sidx < data.length;) {
			// get line length (in number of encoded octets)
			int len = UUDecMap[data[sidx++]];

			// ascii printable to 0-63 and 4-byte to 3-byte conversion
			int end = didx + len;
			for (; didx < end - 2; sidx += 4) {
				byte A = UUDecMap[data[sidx]], B = UUDecMap[data[sidx + 1]], C = UUDecMap[data[sidx + 2]], D = UUDecMap[data[sidx + 3]];
				bos.write( (byte) ( ( ( A << 2 ) & 255 ) | ( ( B >>> 4 ) & 003 ) ) );
				bos.write( (byte) ( ( ( B << 4 ) & 255 ) | ( ( C >>> 2 ) & 017 ) ) );
				bos.write( (byte) ( ( ( C << 6 ) & 255 ) | ( D & 077 ) ) );
				didx+=3;
			}

			if ( didx < end ) {
				byte A = UUDecMap[data[sidx]], B = UUDecMap[data[sidx + 1]];
				bos.write( (byte) ( ( ( A << 2 ) & 255 ) | ( ( B >>> 4 ) & 003 ) ) );
				didx++;
			}
			if ( didx < end ) {
				byte B = UUDecMap[data[sidx + 1]], C = UUDecMap[data[sidx + 2]];
				bos.write( (byte) ( ( ( B << 4 ) & 255 ) | ( ( C >>> 2 ) & 017 ) ) );
				didx++;
			} 

			// skip padding
			while (sidx < data.length && data[sidx] != '\n' && data[sidx] != '\r')
				sidx++;

			// skip end of line
			while (sidx < data.length && ( data[sidx] == '\n' || data[sidx] == '\r' ))
				sidx++;
		}

		return bos.toByteArray();
	}

}
