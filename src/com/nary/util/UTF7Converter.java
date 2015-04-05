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

package com.nary.util;

import java.io.CharArrayWriter;

public class UTF7Converter {

	public static char[] convert(byte[] _bytes) throws Exception {

		boolean b64Context = false;
		int currentB64Off = 0;
		char currentChar = 0;
		CharArrayWriter caw = new CharArrayWriter();

		for (int i = 0; i < _bytes.length; i++) {
			if (b64Context) {
				if (_bytes[i] == '-') {
					if (currentB64Off != 0 && currentChar > 0) {
						caw.write(currentChar);
					}
					b64Context = false;
					continue;
				}
				int part = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz0123456789+/").indexOf(_bytes[i]);
				if (part == -1) {
					throw new Exception("Invalid UTF-7 code: " + (char) _bytes[i]);
				}

				switch (currentB64Off) {
				case 0:
					currentChar = (char) (part << 10);
					break;
				case 1:
					currentChar |= (char) (part << 4);
					break;
				case 2:
					currentChar |= (char) (part >> 2);
					caw.write(currentChar);
					currentChar = (char) ((part & 0x03) << 14);
					break;
				case 3:
					currentChar |= (char) (part << 8);
					break;
				case 4:
					currentChar |= (char) (part << 2);
					break;
				case 5:
					currentChar |= (char) (part >> 4);
					caw.write(currentChar);
					currentChar = (char) ((part & 0x0f) << 12);
					break;
				case 6:
					currentChar |= (char) (part << 6);
					break;
				case 7:
					currentChar |= (char) part;
					caw.write(currentChar);
					break;
				default:
					break;
				}
				currentB64Off = (currentB64Off + 1) % 8;
				continue;
			}

			if (_bytes[i] == '+') {
				// shift character
				// This is start of the Base64 sequence.
				b64Context = true;
				currentB64Off = 0;
				continue;
			}
			caw.write((char) _bytes[i]);
		}
		return caw.toCharArray();
	}

}
