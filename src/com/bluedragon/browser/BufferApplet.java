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

package com.bluedragon.browser;

import java.awt.Color;
import java.io.StringWriter;
import java.util.Hashtable;

import com.nary.util.stringtokenizer;

public class BufferApplet extends Object {

	private Hashtable<String, String> validColor, validAlign, validType, validInput;

	public String determineAlign(String _thisAlign) {

		if (_thisAlign != null)
			_thisAlign = _thisAlign.toLowerCase();
		else
			_thisAlign = "";

		if (validAlign.containsKey(_thisAlign))
			return _thisAlign; // whats the default?
		else
			return "";
	}

	public BufferApplet() {

		validColor = new Hashtable<String, String>();
		validAlign = new Hashtable<String, String>();
		validType = new Hashtable<String, String>();
		validInput = new Hashtable<String, String>();

		validColor.put("lightgray", "211,211,211");
		validColor.put("green", "0,255,0");
		validColor.put("red", "255,0,0");
		validColor.put("yellow", "255,255,0");
		validColor.put("pink", "255,192,203");
		validColor.put("gray", "128,128,128");
		validColor.put("white", "255,255,255");
		validColor.put("darkgray", "47,79,79");
		validColor.put("orange", "255,165,0");
		validColor.put("magenta", "255,0,255");
		validColor.put("black", "0,0,0");
		validColor.put("cyan", "0,255,255");
		validColor.put("silver", "192,192,192");
		validColor.put("blue", "0,0,255");

		validAlign.put("top", "");
		validAlign.put("middle", "");
		validAlign.put("bottom", "");
		validAlign.put("left", "");
		validAlign.put("right", "");
		validAlign.put("baseline", "");
		validAlign.put("texttop", "");
		validAlign.put("absbottom", "");
		validAlign.put("absmiddle", "");

		validType.put("date", "4");
		validType.put("eurodate", "3");
		validType.put("time", "2");
		validType.put("float", "6");
		validType.put("integer", "5");
		validType.put("telephone", "9");
		validType.put("zipcode", "7");
		validType.put("creditcard", "1");
		validType.put("social_security_number", "8");

		validInput.put("text", "");
		validInput.put("checkbox", "");
		validInput.put("radio", "");
		validInput.put("password", "");
	}

	public int checkValidateType(String _thisType) {

		if (_thisType == null)
			_thisType = "";

		_thisType = _thisType.toLowerCase();

		if (validType.containsKey(_thisType)) {
			String temp = validType.get(_thisType);
			return convertToInteger(temp, 0);
		} else
			return 0;
	}

	private String checkColor(String _thisColor) {

		if (_thisColor == null)
			_thisColor = "";

		_thisColor = _thisColor.toLowerCase();

		if (validColor.containsKey(_thisColor))
			return validColor.get(_thisColor);
		else
			return "";
	}

	public Color decodeColor(String _thisC, Color def) {
		int array[];

		if (_thisC == null)
			_thisC = "lightgray";

		if (checkColor(_thisC).equals(""))
			array = hexToIntArray(replaceString(_thisC, "#", "", true, 0));
		else
			array = stringToIntArray(checkColor(_thisC), ",");

		// if ( array.length == 3)
		if (array[0] != -1)
			return new Color(array[0], array[1], array[2]);
		else
			return def;
	}

	// altered to acommadate 'yes' as a true value
	// altered to ignore case
	public boolean decodeBoolean(String _boolean, boolean _default) {
		if (_boolean != null)
			if (_boolean.toString().equalsIgnoreCase("true") || _boolean.toString().equalsIgnoreCase("yes"))
				return true;
		return _default;
	}

	// converts string list to integer array
	public int[] stringToIntArray(String thisString, String delim) {
		int array[] = new int[3];

		if (thisString != null) {
			stringtokenizer st = new com.nary.util.stringtokenizer(thisString, delim);

			array = new int[st.countTokens()];

			String token;
			int x = 0;
			while (st.hasMoreTokens()) {
				token = st.nextToken();
				array[x++] = convertToInteger(token, 0);
			} // loopend while
			return array;
		} else {
			array[0] = -1;
			return array;
		} // endif
	}

	// converts 6 digit hex to integer array representing RGB values
	public int[] hexToIntArray(String thisHex) {
		int array[] = new int[3];
		try {

			int pos = 0;
			for (int i = 0; i < thisHex.length(); i += 2)
				array[pos++] = convertHexToInt(thisHex.substring(i, i + 2), -1);
			return array;
		} catch (Exception e) {
			array[0] = -1;
			return array;
		}
	}

	/**
	 * 
	 * THIS METHOD WAS COPIED FROM com.nary.util.string.
	 * 
	 * This method takes a string and a integer _default in, if string equals
	 * null, then returns integer _default, else converts string to a integer and
	 * returns it.
	 * 
	 * @param _value
	 *          The string which wants to be converted
	 * @param _default
	 *          The integer which is returned when string equals null
	 * @return Returns a integer
	 */

	public static int convertToInteger(java.lang.String _value, int _default) {
		if (_value == null)
			return _default;

		try {
			return Integer.parseInt(_value);

		} catch (Exception E) {
			return _default;
		}
	}

	/**
	 * 
	 * THIS METHOD WAS COPIED FROM com.nary.util.string.
	 * 
	 * Takes in three string values: _Line, _old and _new. Keep searching the
	 * _Line, if find string _old, then replace it with string _new, until no more
	 * _old value in the _Line. Returns the _Line. if the boolean _scope is false
	 * only the first occurence of _old is replaced. If offset is 0, it is
	 * equivalent to calling replaceString( _Line, _old, _new, _scope )
	 * 
	 * @param _Line
	 *          the string which needs to be checked.
	 * @param _old
	 *          the specified string which going to be replaced.
	 * @param _new
	 *          the specified string which going to replace the _old value.
	 * @param _scope
	 *          the boolean which indicates whether to replace all cases or the
	 *          first occurence of _old with _new.
	 * @param -offset
	 *          the offset within the string from which to start the replace from.
	 * @return Returns a string
	 */

	private static java.lang.String replaceString(java.lang.String _Line, java.lang.String _old, java.lang.String _new, boolean _scope, int _offset) {

		String line = _Line;

		if (line == null)
			return null;
		int oldLen = 0;

		if (_old == null || _old.length() == 0)
			return line;
		else
			oldLen = _old.length();

		if (_new == null)
			_new = "";

		int c1 = line.indexOf(_old, _offset);

		if (c1 == -1)
			return line;

		if (_scope) {
			StringWriter writer = new StringWriter(_Line.length());
			int start = 0;
			while (c1 != -1) {
				writer.write(line.substring(start, c1));
				writer.write(_new);
				start = c1 + oldLen;
				c1 = line.indexOf(_old, start);
			}
			writer.write(line.substring(start));
			line = writer.toString();
		} else if (c1 != -1) {
			line = line.substring(0, c1) + _new + line.substring(c1 + oldLen);
		}

		return line;
	}

	/**
	 * 
	 * THIS METHOD WAS COPIED FROM com.nary.util.string.
	 * 
	 * This method takes a string representation of a Hex number and a integer
	 * _default in, if string equals null, then returns integer _default, else
	 * converts string to an integer and returns it.
	 * 
	 * @param _value
	 *          The string which wants to be converted
	 * @param _default
	 *          The integer which is returned when string equals null
	 * @return Returns a integer
	 */

	private static int convertHexToInt(java.lang.String _value, int _default) {
		if (_value == null || _value.length() == 0)
			return _default;

		try {
			if (_value.indexOf("0x") == -1)
				_value = "0x" + _value;
			return Integer.decode(_value).intValue();
		} catch (Exception E) {
			return _default;
		}
	}
}
