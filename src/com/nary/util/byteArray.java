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

import java.io.File;
import java.io.FileInputStream;

/**
 * byteArray
 *
 * Provides methods for operating on byte arrays
 */

public class byteArray {

	private static byte capsToSmall = 'A' - 'a';

	/**
	 * returns true if the 2 arrays given are equals
	 */

	public static boolean equals(byte[] array1, byte[] array2) {
		if (array1.length != array2.length) {
			return false;
		}

		for (int i = 0; i < array1.length; i++) {
			if (array1[i] != array2[i]) {
				return false;
			}
		}
		return true;
	}// equals

	public static boolean equalsIgnoreCase(byte[] array1, byte[] array2) {
		// System.out.println("Comparing : " + new String(array1) + ", " + new
		// String(array2));
		if (array1.length != array2.length) {
			return false;
		}

		for (int i = 0; i < array1.length; i++) {
			if (convertToSmall(array1[i]) != convertToSmall(array2[i])) {
				return false;
			}
		}
		return true;

	}// equalsIgnoreCase()

	/**
	 * converts given character to a small char
	 */

	public static byte convertToSmall(byte in) {
		if (in >= 'A' && in <= 'Z') {
			return (byte) (in - capsToSmall);
		} else {
			return in;
		}
	}

	/**
	 * converts all the given characters in the byte array to small chars
	 */

	public static void convertToSmall(byte[] in) {
		for (int i = 0; i < in.length; i++) {
			in[i] = convertToSmall(in[i]);
		}

	}// convertToSmall

	/**
	 * returns true if the byte array passed in is a set of numeric chars
	 */
	public static boolean isInt(byte[] in) {
		if (in.length == 0) {
			return false;
		}
		for (int i = 0; i < in.length; i++) {
			if (in[i] < '0' || in[i] > '9') {
				return false;
			}
		}
		return true;
	}

	/**
	 * takes a byte array containing chars that should represent an integer, and
	 * returns the int value it represents
	 */

	public static int intValue(byte[] in) {
		int power = in.length - 1;
		int value = 0; // the integer value to return

		for (int i = 0; i < in.length; i++) {
			value += ((int) Math.pow(10, power)) * (in[i] - '0');
			power--;
		}

		return value;

	}// intValue()

	/**
	 * Taken from ServletExec's FileHelper class
	 * @param f
	 * @return
	 */
	public static byte[] convertToByteArray(File f)
	{
		byte[] file = null;
		FileInputStream in = null;

		try
		{
			if (f.length() >= 0)
			{
				// Read the file
				int	len	 = (int)f.length();
				in 		 = new FileInputStream(f);
		  		file	 = new byte[ len ];

				int left  = len;
				int num   = 0;
				while (left > 0)
				{
					num = in.read( file, len - left, left );
					if (num == -1)
						break;

					left -= num;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			try
			{
				//close the file input stream
				in.close();
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
		}

		return file;
	}

}// byteArray
