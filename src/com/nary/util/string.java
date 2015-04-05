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

/**
 * A general String utility class for performing various
 * operations on strings.
 */

package com.nary.util;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;

public class string extends java.lang.Object {

	public static final String EMPTY_STRING = new String("");

	private static final String INT_MAX = "2147483647"; // equivalent to
																											// java.lang.Long.MAX_VALUE

	private static final String INT_MIN = "-2147483648"; // equivalent to
																												// java.lang.Long.MIN_VALUE

	private static final int INT_MAX_LEN = INT_MAX.length();

	public static byte CENTER = 0, RIGHT = 1, LEFT = 2;

	/**
	 * This method takes in a URL address as an argument, and mark it up in HTML
	 * format to show this is a hyperlink.
	 * 
	 * @param _Line
	 *          A string of URL address
	 * @return Returns a tagged hyperlink
	 */

	// --------------------------------

	public static java.lang.String encodeHTML(java.lang.String _Line) {
		if (_Line == null)
			return null;

		java.lang.String urlString = "";
		int c2 = -1, len = 0, EOL = -1;

		_Line = stripLinks(_Line);
		int c1 = _Line.indexOf("http://");

		while (c1 != -1) {
			c2 = _Line.indexOf(" ", c1);
			if (c2 != -1)
				urlString = _Line.substring(c1, c2);
			else {
				urlString = _Line.substring(c1);
				EOL = urlString.indexOf("\r");
				if (EOL != -1)
					urlString = urlString.substring(0, EOL);
				EOL = urlString.indexOf("\n");
				if (EOL != -1)
					urlString = urlString.substring(0, EOL);
			}
			len = urlString.length();
			_Line = _Line.substring(0, c1) + "<A HREF=\"" + urlString + "\">" + urlString + "</A>" + _Line.substring(c1 + len);
			c1 = _Line.indexOf("http://", c1 + 15 + (2 * len));
		}
		return _Line;
	}

	/**
	 * This method makes up a hyperlink in HTLM format with a point - Target to
	 * jump to.
	 * 
	 * @param _Line
	 *          A URL address
	 * @param _Target
	 *          A position in the path.
	 * @return A tagged hyperlink with a point to jump to.
	 */

	public static java.lang.String encodeHTML(java.lang.String _Line, java.lang.String _Target) {
		if (_Line == null)
			return null;
		if (_Target == null || _Target == "")
			return encodeHTML(_Line);

		java.lang.String urlString = "";
		int c2 = -1, len = 0, EOL = -1;

		_Line = stripLinks(_Line);

		int c1 = _Line.indexOf("http://");

		while (c1 != -1) {
			c2 = _Line.indexOf(" ", c1);
			if (c2 != -1)
				urlString = _Line.substring(c1, c2);
			else {
				urlString = _Line.substring(c1);
				EOL = urlString.indexOf("\r");
				if (EOL != -1)
					urlString = urlString.substring(0, EOL);
				EOL = urlString.indexOf("\n");
				if (EOL != -1)
					urlString = urlString.substring(0, EOL);
			}
			len = urlString.length();
			_Line = _Line.substring(0, c1) + "<A HREF=\"" + urlString + "\" TARGET=\"" + _Target + "\">" + urlString + "</A>" + _Line.substring(c1 + len);
			c1 = _Line.indexOf("http://", c1 + 24 + (2 * len) + _Target.length());
		}
		return _Line;
	}

	/**
	 * This method takes in a tagged hyperlink and removes the tag, returns a URL
	 * string.
	 * 
	 * @param _Line
	 *          A tagged hyperlink
	 * @return A string of URL address
	 */

	public static java.lang.String stripHTML(java.lang.String _Line) {
		if (_Line == null)
			return null;

		int c2 = -1, c3 = -1, temp = 0;
		int c1 = _Line.indexOf("<");

		while (c1 != -1) {

			c2 = _Line.indexOf(">", c1);

			if (c2 != -1) {

				c3 = _Line.indexOf("<", c1 + 1);

				while (c3 < c2 && c3 != -1) {
					temp = c3;
					c3 = _Line.indexOf("<", c3 + 1);
				}

				if (c2 < _Line.length() - 1 && temp != 0)
					_Line = _Line.substring(0, temp) + _Line.substring(c2 + 1);
				else if (c2 < _Line.length() - 1 && temp == 0)
					_Line = _Line.substring(0, c1) + _Line.substring(c2 + 1);
				else if (temp == 0)
					_Line = _Line.substring(0, c1);
				else
					_Line = _Line.substring(0, temp);

			} else
				break; // end of the line

			if (temp != 0)
				c1 = _Line.indexOf("<", temp);
			else
				c1 = _Line.indexOf("<", c1);

			temp = 0;
		}

		return _Line;
	}

	/**
	 * Takes in a html tag, returns the first parameter in the tag.
	 * 
	 * @param _tag
	 *          The html tag which going to be parsed.
	 */

	public static java.lang.String HtmlTagName(java.lang.String _tag) {
		String tag = null;
		int c1 = _tag.indexOf(" ");
		int c2 = _tag.indexOf(">");
		if (c1 == -1)
			tag = _tag.substring(1, c2);
		else
			tag = _tag.substring(1, c1);

		return tag;
	}

	/**
	 * This method takes in a line, if the line equals null, returns a empty
	 * vector, else returns the vector of interge elements which shows the
	 * position of the tags in the line.
	 * 
	 * @param _Line
	 *          A tagged line
	 * @return Returns a vector
	 */

	public static Vector<int[]> findHTML(java.lang.String _Line) {

		Vector<int[]> v = new Vector<int[]>();

		if (_Line == null)
			return v;

		int c2 = -1, c3 = -1, temp = 0;
		int c1 = _Line.indexOf("<");

		if (c1 == -1)
			return v;
		while (c1 != -1) {

			int[] limits = new int[2];

			c2 = _Line.indexOf(">", c1);

			if (c2 != -1) {

				c3 = _Line.indexOf("<", c1 + 1);

				while (c3 < c2 && c3 != -1) {
					temp = c3;
					c3 = _Line.indexOf("<", c3 + 1);
				}

				if (c2 < _Line.length() - 1 && temp != 0) {
					limits[0] = temp;
					limits[1] = c2;
					v.addElement(limits);
				} else if (c2 < _Line.length() - 1 && temp == 0) {
					limits[0] = c1;
					limits[1] = c2;
					v.addElement(limits);
				} else if (temp == 0) {
					limits[0] = c1;
					limits[1] = _Line.length() - 1;
					v.addElement(limits);
				} else {
					limits[0] = temp;
					limits[1] = _Line.length() - 1;
					v.addElement(limits);
				}

			} else
				break; // end of the line

			if (temp != 0)
				c1 = _Line.indexOf("<", temp + 1);
			else
				c1 = _Line.indexOf("<", c1 + 1);

			temp = 0;
		}

		return v;
	}

	/**
	 * The method which takes in a string _Line, do removing all hyperlink tags
	 * within the _Line and return the _Line.
	 * 
	 * @param _Line
	 *          the line of string which going to be checked.
	 * @return the string without hyperlink tags within it.
	 */

	public static java.lang.String stripLinks(java.lang.String _Line) {
		if (_Line == null)
			return null;

		int c2 = -1, c3 = -1;

		java.lang.String ucase = _Line.toUpperCase();
		int c1 = ucase.indexOf("<A");

		while (c1 != -1) {

			c2 = ucase.indexOf(">", c1);

			if (c2 != -1) {
				_Line = _Line.substring(0, c1) + _Line.substring(c2 + 1);
				c3 = ucase.indexOf("</A>", c2 + 1);
				if (c3 == -1) {
					ucase = _Line.toUpperCase();
					c1 = ucase.indexOf("<A", c1);
				} else {
					_Line = _Line.substring(0, c3 - ((c2 + 1) - c1)) + _Line.substring(c3 - ((c2 + 1) - c1) + 4);
					ucase = _Line.toUpperCase();
					c1 = ucase.indexOf("<A", c3 - ((c2 + 1) - c1));

				}
			} else
				break; // end of the line
		}
		return _Line;
	}

	/**
	 * The method takes a string _Line in, just check to see if there is a "=" at
	 * the end, then remove it and return the _Line. else simply return the _Line.
	 * 
	 * @param _Line
	 *          A line of string
	 * @return Returns a line without a "=" at the end of line
	 */

	public static java.lang.String stripEquals(java.lang.String _Line) {
		if (_Line == null)
			return null;

		if (_Line.endsWith("=")) {
			_Line = _Line.substring(0, _Line.lastIndexOf("="));
		}
		return _Line;
	}

	/**
	 * Takes in three string values: _Line, _old and _new. Keep searching the
	 * _Line, if string _old is found, then replace it with string _new, until no
	 * more _old strings in the _Line. Returns the _Line.
	 * 
	 * @param _Line
	 *          the string which needs to be checked.
	 * @param _old
	 *          the specified string which going to be replaced.
	 * @param _new
	 *          the specified string which going to replace the _old value.
	 * @return Returns a string
	 */

	public static java.lang.String replaceString(java.lang.String _Line, java.lang.String _old, java.lang.String _new) {
		return replaceString(_Line, _old, _new, true);
	}

	/**
	 * Takes in three string values: _Line, _old and _new. Keep searching the
	 * _Line, if find string _old, then replace it with string _new, until no more
	 * _old value in the _Line. Returns the _Line. if the boolean _scope is false
	 * only the first occurence of _old is replaced.
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
	 * @return Returns a string
	 */

	public static java.lang.String replaceString(java.lang.String _Line, java.lang.String _old, java.lang.String _new, boolean _scope) {
		return replaceString(_Line, _old, _new, _scope, 0);
	}

	/**
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
	 * @param -offset the offset within the string from which to start the replace
	 *        from.
	 * @return Returns a string
	 */

	public static java.lang.String replaceString(java.lang.String _Line, java.lang.String _old, java.lang.String _new, boolean _scope, int _offset) {

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
	 * Takes in three string values: _Line, _old and _new. Keep searching the
	 * _Line without considering case different, if find string _old, then replace
	 * it with string _new, until no more _old value in the _Line. Returns the
	 * _Line.
	 * 
	 * @param _Line
	 *          the string which going to be checked.
	 * @param _old
	 *          the string which going to replaced.
	 * @param _new
	 *          the string which going to replace the _old string.
	 * @return Returns a string
	 */

	public static java.lang.String replaceStringIgnoreCase(java.lang.String _Line, java.lang.String _old, java.lang.String _new) {
		return replaceStringIgnoreCase(_Line, _old, _new, true);
	}

	/**
	 * Takes in three string values: _Line, _old and _new. Keep searching the
	 * _Line without considering case different, if find string _old, then replace
	 * it with string _new, until no more _old value in the _Line. Returns the
	 * _Line.
	 * 
	 * @param _Line
	 *          the string which going to be checked.
	 * @param _old
	 *          the string which going to replaced.
	 * @param _new
	 *          the string which going to replace the _old string.
	 * @return Returns a string
	 */

	public static java.lang.String replaceStringIgnoreCase(java.lang.String _Line, java.lang.String _old, java.lang.String _new, boolean _scope) {
		if (_Line == null)
			return null;

		int oldLen = 0;

		if (_old == null)
			return _Line;
		else
			oldLen = _old.length();

		if (_new == null)
			_new = "";
		;

		BMPattern bmp = new BMPattern(_old, true);
		char[] chars = _Line.toCharArray();

		int c1 = bmp.matches(chars, 0, chars.length);

		if (c1 == -1)
			return _Line;

		if (_scope) {

			StringBuilder sb = new StringBuilder();
			int lastc1 = 0;
			while (c1 != -1) {
				sb.append(chars, lastc1, c1 - lastc1);
				sb.append(_new);
				lastc1 = c1 + oldLen;
				c1 = bmp.matches(chars, c1 + oldLen, chars.length);
			}
			sb.append(chars, lastc1, chars.length - lastc1);
			return sb.toString();

		} else if (c1 != -1) {
			_Line = _Line.substring(0, c1) + _new + _Line.substring(c1 + oldLen);
		}
		return _Line;
	}

	/**
	 * The method which takes in a line, keep searching the line, if find a space
	 * within the line, then removes the space, until no more space within the
	 * line. Returns the line.
	 * 
	 * @param _Line
	 *          A line of string
	 * @return Returns a string without extra space in it
	 */

	public static java.lang.String removeExtraSpace(java.lang.String _Line) {
		if (_Line == null)
			return null;
		int c1 = _Line.indexOf("  ");

		if (c1 == -1)
			return _Line;

		while (c1 != -1) {
			_Line = _Line.substring(0, c1) + _Line.substring(c1 + 1);
			c1 = _Line.indexOf("  ", c1);
		}
		return _Line;
	}

	/**
	 * Takes in a string and keep searching the string in forward order to see if
	 * there is a "\r\n" within the string, remove it, until no more "\r\n" in the
	 * string. Return the string.
	 * 
	 * @param _Line
	 *          A line of string
	 * @return Returns a line of string without CRLF at the front
	 */

	public static java.lang.String removeCRLF(java.lang.String _Line) {
		if (_Line == null)
			return null;
		int c1 = _Line.indexOf("\r\n");

		if (c1 == -1)
			return _Line;
		while (c1 != -1) {
			_Line = _Line.substring(0, c1) + _Line.substring(c1 + 2);
			c1 = _Line.indexOf("\r\n", c1);
		}

		return _Line;
	}

	/**
	 * Takes in a string and keep searching the string in backward order to see if
	 * there is a "\r\n" at end, if so, remove it, until no more "\r\n" in the
	 * string. Return the String.
	 * 
	 * 
	 * @param _Line
	 *          A line of string
	 * @return Returns a line of string without CRLF at the end
	 */
	public static java.lang.String removeCRLFAtEnd(java.lang.String _Line) {
		if (_Line == null)
			return null;

		int c1 = _Line.lastIndexOf("\r\n");

		if (c1 == -1)
			return _Line;

		while (c1 + 2 == _Line.length()) { // \r\n at end of line
			_Line = _Line.substring(0, c1);
			c1 = _Line.lastIndexOf("\r\n");
			if (c1 == -1)
				break;
		}

		return _Line;
	}

	/**
	 * The method is to get a string and not allow its value equals null, if is
	 * null then replace it with the default and return it, else just return it.
	 * 
	 * @param _l
	 *          A string which passed into the method
	 * @param _default
	 *          A string which is used to replace a null value
	 * @return Returns a string
	 */

	public static java.lang.String replaceTheString(java.lang.String _l, java.lang.String _default) {
		if (_l == null)
			return _default;
		else
			return _l;
	}

	/**
	 * The method is to get a string and not allow its value equals null, if is
	 * null then replace it with a space and return it, else just return it.
	 * 
	 * @param _l
	 *          A string which passed into the method
	 * @return Returns a string
	 */

	public static java.lang.String replaceTheString(java.lang.String _l) {
		if (_l == null)
			return "";
		else
			return _l;
	}

	/**
	 * This method takes in a string, and if the string is null, return a space
	 * instead. else if the string lenght great than specified length, then trim
	 * the string at the length of "_length -1".
	 * 
	 * @param _l
	 *          The string which going to trimed
	 * @param _length
	 *          A integer which definds the length of the trimed string is
	 *          "_length -1"
	 * @return Returns the trimed string
	 */

	public static java.lang.String trimString(java.lang.String _l, int _length) {
		java.lang.String N = _l;
		if (N == null)
			N = "";
		if (N.length() > _length)
			N = N.substring(0, _length - 1);

		return N;
	}

	/**
	 * This method takes a String, and returns a hash code using the MD5 algorithm
	 * for this string.
	 * 
	 * @param value
	 */

	public static String hashCodeMD5(java.lang.String value) {

		MessageDigest md;
		String hashString = "";

		try {

			md = MessageDigest.getInstance("MD5");
			byte[] output = md.digest(value.getBytes());
			StringBuilder sb = new StringBuilder(2 * output.length);

			for (int i = 0; i < output.length; ++i) {
				int k = output[i] & 0xFF;
				if (k < 0x10) {
					sb.append('0');
				}
				sb.append(Integer.toHexString(k));
			}

			hashString = sb.toString();

		} catch (Exception e) {
		}

		return hashString;
	}

	/**
	 * This method takes a string, and returns a unique hash code for this string.
	 * 
	 * @param value
	 */

	public static int hashCode(java.lang.String value) {
		int h = 0;
		int off = 0;
		char val[] = new char[value.length()];
		value.getChars(0, value.length(), val, 0);
		int len = value.length();

		for (int i = 0; i < len; i++)
			h = 31 * h + val[off++];

		return h;
	}

	/**
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

	public static int convertHexToInt(java.lang.String _value, int _default) {
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

	/**
	 * This method takes a string and a long value _default in, if string equals
	 * null, then returns long value _default, else converts string to a long
	 * value and returns it.
	 * 
	 * @param _value
	 *          The string which wants to be converted
	 * @param _default
	 *          The long integer which is returned when string equals null
	 * @return Returns a long integer
	 */
	public static long convertToLong(java.lang.String _value, long _default) {
		if (_value == null)
			return _default;

		try {
			return Long.parseLong(_value);
		} catch (Exception E) {
			return _default;
		}
	}

	/**
	 * this method takes a string and converts it to a list of strings by
	 * extracting the substrings delimited by the _delimiter char
	 * 
	 * @param _values
	 *          The String containing the substrings
	 * @param _delimiter
	 *          The character which separates substrings in _values
	 */

	public static String[] convertToList(String _values, char _delimiter) {
		List<String> valist = split(_values, _delimiter);
		String[] list = new String[valist.size()];
		for (int i = 0; i < list.length; i++) {
			list[i] = valist.get(i).trim();
		}
		return list;
	}// convertToList

	/**
	 * The method which takes in a string, converts its first letter of the string
	 * or the letter after a space within the string to uppercase, and other
	 * letters remain unchanged.
	 * 
	 * @param _a
	 *          The string which to be coverted
	 * @return Returns a string with only one capital letter at the beginning
	 */

	public static java.lang.String upperCase(java.lang.String _a) {
		java.lang.String a = _a.toLowerCase();

		char aw[] = new char[a.length()];

		for (int x = 0; x < a.length(); x++) {
			if (a.charAt(x) > 96 && a.charAt(x) < 123) {
				if (x == 0 || a.charAt(x - 1) == 32)
					aw[x] = (char) (a.charAt(x) - 32);
				else
					aw[x] = (char) a.charAt(x);
			} else
				aw[x] = (char) a.charAt(x);
		}

		return new java.lang.String(aw);
	}

	/**
	 * This method takes in a string and use this string to create a vector of
	 * lines.
	 * 
	 * @param _Body
	 *          the string which passed in
	 * @return A vector
	 */

	public static Vector<String> createVectorOfLines(java.lang.String _Body) {
		Vector<String> V = new Vector<String>(10, 5);

		try {
			java.io.BufferedReader bodyIn = new java.io.BufferedReader(new java.io.StringReader(_Body));
			java.lang.String LineIn = bodyIn.readLine();
			while (LineIn != null) {
				if (LineIn.length() == 0)
					LineIn = " ";
				V.addElement(LineIn);
				LineIn = bodyIn.readLine();
			}
		} catch (Exception E) {
		}

		return V;
	}

	/**
	 * This method is used to convert a long value to a decimal format.
	 * 
	 * @param number
	 *          The number to be formatted
	 * @return A string that is the formatted object
	 */
	public static String formatLongToCurrency(long number) {
		if (number == 0)
			return "0.00";

		java.text.DecimalFormat DF = new java.text.DecimalFormat("#0.00");
		return DF.format((double) number / (double) 100);
	}

	/**
	 * Convert the string value to the boolean value, if the string value is null,
	 * the default boolean value is returned.
	 */
	public static boolean convertToBoolean(String _value, boolean _defalut) {
		if ((_value == null) || (_value.length() == 0))
			return _defalut;
		else if (_value.equalsIgnoreCase("true"))
			return true;
		else if (_value.equalsIgnoreCase("1"))
			return true;
		else if (_value.equalsIgnoreCase("yes"))
			return true;
		else
			return false;
	}

	/**
	 * This method is used to tell if a character represents a number between 0-9
	 * 
	 * @param charIn
	 *          The char to compare
	 * @return True if the char is a number
	 */
	public static boolean isANumber(char charIn) {
		if (charIn == -1)
			return false;

		if ((int) charIn >= 48 && (int) charIn <= 57)
			return true;

		return false;
	}

	/**
	 * This method is used to find the index of a string inside a buffer. Very
	 * fast
	 * 
	 * @param buffer
	 *          The byte array to search
	 * @param text
	 *          The string to find
	 * @return -1 if not found, or the index where it was found
	 */
	public static int byteSearch(byte[] buffer, String text) {
		return byteSearch(buffer, text, 0);
	}

	/**
	 * This method is used to find the index of a string inside a buffer. Very
	 * fast
	 * 
	 * @param buffer
	 *          The byte array to search
	 * @param text
	 *          The string to find
	 * @param startIndex
	 *          The index to where to start in the string
	 * @return -1 if not found, or the index where it was found
	 */

	public static int byteSearch(byte[] buffer, String text, int startIndex) {

		if (text.length() == 0)
			return -1;

		int searchIndx = -1;
		int searchStart = -1;

		for (int x = startIndex; x < buffer.length; x++) {
			if (searchIndx == -1 && (char) buffer[x] == (char) text.charAt(0)) {
				searchIndx = 1;
				searchStart = x;
				if (text.length() == 1)
					return searchStart;
			} else if (searchIndx != -1 && (char) buffer[x] == (char) text.charAt(searchIndx)) {
				searchIndx++;
				if (searchIndx == text.length())
					return searchStart;
			} else
				searchIndx = -1;
		}

		return -1;
	}

	/**
	 * This method is used to find the index of a string inside a buffer. Very
	 * fast
	 * 
	 * @param buffer
	 *          The char array to search
	 * @param text
	 *          The string to find
	 * @param startIndex
	 *          The index to where to start in the string
	 * @return -1 if not found, or the index where it was found
	 */

	public static int charSearch(char[] buffer, String _text, int startIndex, int endIndex) {

		if (_text.length() == 0)
			return -1;

		int searchIndx = -1;
		int searchStart = -1;
		char[] toMatch = _text.toCharArray();
		int matchLength = _text.length();

		for (int x = startIndex; (x < buffer.length) && (x < endIndex); x++) {
			if (searchIndx == -1 && (buffer[x] == toMatch[0])) {
				searchIndx = 1;
				searchStart = x;
				if (matchLength == 1)
					return searchStart;
			} else if (searchIndx != -1 && buffer[x] == toMatch[searchIndx]) {
				searchIndx++;
				if (searchIndx == matchLength)
					return searchStart;
			} else
				searchIndx = -1;
		}

		return -1;
	}

	/**
	 * Coverts a string to a double value and return this value. If anything
	 * wrong, the default double value will be returned.
	 * 
	 * @param _value
	 *          the string which going to be converted.
	 * @param _default
	 *          the default double value.
	 */

	public static double convertToDouble(java.lang.String _value, double _default) {
		if (_value == null)
			return _default;
		try {
			return new Double(_value).doubleValue();
		} catch (Exception E) {
			return _default;
		}
	}

	public static byte[] stripCRLF(byte[] _b) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < _b.length; i++) {
			int temp = _b[i];
			if (temp == 10 || temp == 13) {
			} else
				out.write(temp);
		}
		return out.toByteArray();
	}

	/**
	 * Converts a String with hex coded characters (eg. %20) to a String with the
	 * actual characters inserted. e.g belle%20%26%20sebastian = belle & sebastian
	 */

	public static String decodeUrlString(String _str) {
		int c1 = _str.indexOf("%");
		String hex;
		int x;

		while (c1 != -1 && c1 + 3 < _str.length()) {
			hex = _str.substring(c1 + 1, c1 + 3);
			x = com.nary.util.string.convertHexToInt("0x" + hex, -1);

			if (x != -1)
				_str = com.nary.util.string.replaceString(_str, "%" + hex, String.valueOf((char) x));

			c1 = _str.indexOf("%", c1 + 1);
		}

		return _str;
	}

	/**
	 * Removes all non-alphanumeric characters from a String
	 */

	public static String removeNonAlphaNumeric(String _str) {
		if (_str == null)
			return null;

		int c1;
		byte[] buffer = new byte[_str.length()];
		int count = 0;

		for (int x = 0; x < _str.length(); x++) {
			c1 = _str.charAt(x);
			if ((c1 >= 97 && c1 <= 122) || c1 == 32 || (c1 >= 48 && c1 <= 57) || (c1 >= 65 && c1 <= 90))
				buffer[count++] = (byte) c1;
		}

		return new String(buffer, 0, count);
	}

	/**
	 * returns the number of times the character appears in the string
	 * 
	 * @param _str
	 *          - the String to be searched
	 * @param _ch
	 *          - the character that the occurences of in the string will be
	 *          counted
	 */

	public static int occurrenceCount(String _str, char _ch) {
		int count = 0;
		for (int i = 0; i < _str.length(); i++) {
			if (_str.charAt(i) == _ch) {
				count++;
			}
		}

		return count;
	}// occurrenceCount()

	/**
	 * returns whether the given string can be converted to a number without
	 * try..catching any expression e.g parseInt()
	 */

	public static boolean isNumber(String _str) {
		int index = 0;
		boolean dotUsed = false;
		int eUsed = -1; // index of 'E' in _str
		char nextCh;
		int strLen = _str.length();

		// If string is empty; or ends with a - then definitely not a number
		if (strLen == 0 || ( strLen > 1 && _str.charAt(strLen-1) == '-' ) )
			return false;

		
		nextCh = _str.charAt(0);
		if ( (nextCh == '-' || nextCh == '+') && strLen > 1 ){
			if ( _str.charAt(1) == '.' && strLen == 2 )
				return false;

			index++;
		} else if (Character.isDigit(nextCh)) {
			index++;
		} else if (nextCh == '.' && strLen > 1) {
			index++;
			dotUsed = true;
		} else {
			return false;
		}

		for (int i = index; i < strLen; i++) {
			nextCh = _str.charAt(index);
			if (nextCh == '.') {
				if (dotUsed) {
					return false;
				} else if (eUsed > 0) {
					return false; // can't have '.' after an E
				} else {
					dotUsed = true;
					index++;
				}
			} else if (nextCh == 'E' || nextCh == 'e') {
				if (eUsed > 0) {
					return false;
				} else if (index == (strLen - 1)) {
					return false; // E can't be last character
				} else {
					eUsed = index;
					index++;
				}
			} else if (nextCh == '-' || nextCh == '+') {
				if (eUsed != (index - 1)) {
					return false;
				}
				index++;
			} else if (Character.isDigit(nextCh)) {
				index++;
			} else {
				return false;
			}
		}

		return true;

	}// isNumber()

	/**
	 * returns whether the String is a valid int assumes that _s contains only
	 * digits or "E" with an optional -
	 */
	public static boolean isInt(String _s) {

		// If the string contains an "E" then return false
		if ((_s.indexOf('E') > 0) || (_s.indexOf('e') > 0))
			return false;

		// Determine if it's a negative or positive number
		boolean negative = false;
		if (_s.charAt(0) == '-') {
			negative = true;
			_s = _s.substring(1); // remove minus sign
		}

		// Extract the length of the string
		int strLen = _s.length();

		// If the length of the string is greater than the max. length of an int
		// then return false
		if (strLen > INT_MAX_LEN)
			return false;

		// If the length of the string is less than the max. length of an int then
		// return true
		if (strLen < INT_MAX_LEN)
			return true;

		// The length of the string is equal to the max. length of an int so compare
		// it to INT_MAX or INT_MIN
		String compareValue;
		if (negative)
			compareValue = INT_MIN.substring(1); // remove minus sign
		else
			compareValue = INT_MAX;

		// compare each char
		for (int i = 0; i < strLen; i++) {
			if (_s.charAt(i) > compareValue.charAt(i)) {
				return false;
			} else if (_s.charAt(i) != compareValue.charAt(i)) {
				return true;
			}
			// if equal then continue comparison
		}

		return true;
	}// isInt()

	public static String leftTrim(String value) {
		int c1 = -1;
		for (int x = 0; x < value.length(); x++) {
			if (!Character.isWhitespace(value.charAt(x)))
				break;
			else
				c1 = x;
		}

		return (c1 == -1) ? value : value.substring(c1 + 1);
	}

	public static String rightTrim(String value) {
		int c1 = -1;
		for (int x = value.length() - 1; x >= 0; --x) {
			if (!Character.isWhitespace(value.charAt(x)))
				break;
			else
				c1 = x;
		}

		return (c1 == -1) ? value : value.substring(0, c1);
	}

	/**
	 * Replaces all occurrences of specified characters in a String with the given
	 * String replacements
	 * 
	 * @param _src
	 *          The String in which the chars should be replaced
	 * @param _old
	 *          A list of characters to be replaced
	 * @param _new
	 *          A list of Strings to use as replacements. The first String will be
	 *          used when an occurence of the first char is found, and so on.
	 * @throws Exception
	 *           if the size of the _old and _new arrays don't match
	 * @return String with replacements inserted
	 */
	public static String replaceChars(String _src, char[] _old, String[] _new) throws Exception {
		if (_old.length != _new.length) {
			throw new Exception("Method misuse: _old.length != _new.length");
		}
		int strLen = _src.length();
		int charsLen = _old.length;
		StringBuilder buffer = new StringBuilder(_src);
		StringWriter writer = new StringWriter(strLen);
		char nextChar;
		boolean foundCh;

		for (int i = 0; i < strLen; i++) {
			nextChar = buffer.charAt(i);
			foundCh = false;

			for (int j = 0; j < charsLen; j++) {
				if (nextChar == _old[j]) {
					writer.write(_new[j]);
					foundCh = true;
				}
			}
			if (!foundCh) {
				writer.write(nextChar);
			}
		}

		return writer.toString();
	}

	public static String escapeHtml(String str) {
		try {
			PatternMatcher matcher = new Perl5Matcher();
			PatternCompiler compiler = new Perl5Compiler();

			Pattern pattern = compiler.compile("&(([a-z][a-zA-Z0-9]*)|(#\\d{2,6});)", Perl5Compiler.CASE_INSENSITIVE_MASK | Perl5Compiler.SINGLELINE_MASK);

			String tmp = Util.substitute(matcher, pattern, new Perl5Substitution("&amp;$1"), str, Util.SUBSTITUTE_ALL);

			return replaceChars(tmp, new char[] { '<', '>', '\"' }, new String[] { "&lt;", "&gt;", "&quot;" });
		} catch (Exception e) {
			return str;
		}// won't happen

	}

	/**
	 * Formats a string to a given column width, optionally ignoring the carriage
	 * returns that may exist in the original string.
	 * 
	 * @param inText
	 * @param colWidth
	 * @param ignoreCarriageReturns
	 * @return
	 */
	public static java.lang.String wrap(java.lang.String inText, int colWidth, boolean ignoreCarriageReturns) {
		if (inText == null)
			throw new RuntimeException("inText must be a valid String");

		int inTextLength = inText.length();

		// --[ Make sure the column width is sensible. If the string passed in is
		// less than the columnWidth then just return
		if (colWidth < 1)
			throw new RuntimeException("colWidth must be greater than 0");

		if (inTextLength <= colWidth)
			return inText;

		StringBuilder outBuffer = new StringBuilder(inTextLength);
		StringBuilder currentLine = new StringBuilder(colWidth);
		StringBuilder token = new StringBuilder(32);
		char c;

		for (int c1 = 0; c1 < inTextLength; c1++) {
			c = inText.charAt(c1);

			// -- Special check for the carriage returns.
			// -- If found, we want to move to reset the counters etc
			if (ignoreCarriageReturns && (c == '\r' || c == '\n'))
				c = ' ';
			else if (!ignoreCarriageReturns && (c == '\r' || c == '\n')) {
				if (token.length() + currentLine.length() <= colWidth) { // -- token is
																																	// okay to add
																																	// to the line
					currentLine.append(token);
					currentLine.append(c);
					outBuffer.append(currentLine);
					currentLine.setLength(0);
					token.setLength(0);
				} else {
					// -- Add the existing line to the outbuffer, and
					addToken(token, currentLine, outBuffer, c, colWidth);
				}

				if (c1 < inTextLength - 1 && ((c == '\r' && inText.charAt(c1 + 1) == '\n') || (c == '\n' && inText.charAt(c1 + 1) == '\r')))
					c1++;

				continue;
			}

			// -- Check for 'whitespace'
			if (c == ' ') {

				if (token.length() + currentLine.length() <= colWidth) { // -- token is
																																	// okay to add
																																	// to the line
					currentLine.append(token);
					currentLine.append(c);
					token.setLength(0);
				} else {
					// -- Add the existing line to the outbuffer, and
					addToken(token, currentLine, outBuffer, c, colWidth);
				}

			} else
				token.append(c);

		}

		// -- Deal with the tail end of the token
		if (token.length() > 0) {
			if (token.length() + currentLine.length() <= colWidth) {
				currentLine.append(token);
			} else
				addToken(token, currentLine, outBuffer, ' ', colWidth);
		}

		// -- The present line may not be ready
		if (currentLine.length() > 0)
			outBuffer.append(currentLine);

		return outBuffer.toString().substring(0, outBuffer.length());
	}

	private static void addToken(StringBuilder token, StringBuilder currentLine, StringBuilder outBuffer, char c, int colWidth) {
		// -- Internal method only used by wrap()

		if (token.length() > colWidth) {
			// -- May as well break it here on this line
			// -- Pickup whats left on this line
			int size = colWidth - currentLine.length();
			if (size > 0) {
				currentLine.append(token.toString().substring(0, size));
				token = token.delete(0, size);
			}
			outBuffer.append(currentLine);
			outBuffer.append("\r\n");

			currentLine.setLength(0);

			while (token.length() > 0) {
				if (token.length() >= colWidth) {
					currentLine.append(token.toString().substring(0, colWidth));
					outBuffer.append(currentLine);
					outBuffer.append("\r\n");
					currentLine.setLength(0);
					token = token.delete(0, colWidth);
				} else {
					currentLine.append(token);
					currentLine.append(c);
					token.setLength(0);
				}
			}

		} else {
			outBuffer.append(currentLine);
			outBuffer.append("\r\n");
			currentLine.setLength(0);
			currentLine.append(token);
			currentLine.append(c);
			token.setLength(0);
		}

	}

	/**
	 * Returns a byte array from a string of hexadecimal digits.
	 */
	public static byte[] hexFromString(String _hex) {
		int len = _hex.length();
		byte[] buf = new byte[((len + 1) / 2)];

		int i = 0, j = 0;
		if ((len % 2) == 1)
			buf[j++] = (byte) fromDigit(_hex.charAt(i++));

		while (i < len) {
			buf[j++] = (byte) ((fromDigit(_hex.charAt(i++)) << 4) | fromDigit(_hex.charAt(i++)));
		}

		return buf;
	}

	public static String toHex(byte[] _hex) {
		if (_hex == null || _hex.length <= 0)
			return "";

		StringBuilder sb = new StringBuilder(_hex.length * 2);

		String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

		for (int i = 0; i < _hex.length; i++) {
			// look up high nibble char
			sb.append(pseudo[(_hex[i] & 0xf0) >>> 4]);

			// look up low nibble char
			sb.append(pseudo[_hex[i] & 0x0f]);
		}

		return sb.toString();
	}

	private static int fromDigit(char _ch) {
		if (_ch >= '0' && _ch <= '9')
			return _ch - '0';
		else if (_ch >= 'A' && _ch <= 'F')
			return _ch - 'A' + 10;
		else if (_ch >= 'a' && _ch <= 'f')
			return _ch - 'a' + 10;

		throw new IllegalArgumentException("invalid hex digit '" + _ch + "'");
	}

	public static int indexOf(StringBuilder _sb, String _substr, int _start) {
		int sbLen = _sb.length();
		int start = _start;
		if (start >= sbLen) {
			return -1;
		} else if (start < 0) {
			start = 0;
		}

		char[] subStrChars = _substr.toCharArray();

		for (int i = start; i < sbLen; i++) {
			boolean found = true;
			for (int j = 0; j < subStrChars.length; j++) {
				if (_sb.charAt(i + j) != subStrChars[j]) {
					found = false;
					break;
				}
			}
			if (found) {
				return i;
			}
		}

		return -1; // not found
	}

	/**
	 * <p>
	 * Splits the provided text into an array, using whitespace as the separator.
	 * Whitespace is defined by {@link Character#isWhitespace(char)}.
	 * </p>
	 * 
	 * <p>
	 * The separator is not included in the returned String array. Adjacent
	 * separators are treated as one separator.
	 * </p>
	 * 
	 * <p>
	 * A <code>null</code> input String returns <code>null</code>.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.split(null)       = null
	 * StringUtils.split("")         = []
	 * StringUtils.split("abc def")  = ["abc", "def"]
	 * StringUtils.split("abc  def") = ["abc", "def"]
	 * StringUtils.split(" abc ")    = ["abc"]
	 * </pre>
	 * 
	 * @param str
	 *          the String to parse, may be null
	 * @return an array of parsed Strings, <code>null</code> if null String input
	 */
	public static List<String> split(String str, String separatorChars) {
		return split(str, separatorChars, -1);
	}

	public static List<String> split(String str) {
		return split(str, " \t\n\r\f"); // default separators match
																		// java.util.StringTokenizer delimiters
	}

	public static List<String> split(String str, char separatorChar) {
		return split(str, String.valueOf(separatorChar), -1);
	}

	/**
	 * <p>
	 * Splits the provided text into an array, separator specified. This is an
	 * alternative to using StringTokenizer.
	 * </p>
	 * 
	 * <p>
	 * The separator is not included in the returned String array. Adjacent
	 * separators are treated as one separator.
	 * </p>
	 * 
	 * <p>
	 * A <code>null</code> input String returns <code>null</code>.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.split(null, *)         = null
	 * StringUtils.split("", *)           = []
	 * StringUtils.split("a.b.c", '.')    = ["a", "b", "c"]
	 * StringUtils.split("a..b.c", '.')   = ["a", "b", "c"]
	 * StringUtils.split("a:b:c", '.')    = ["a:b:c"]
	 * StringUtils.split("a\tb\nc", null) = ["a", "b", "c"]
	 * StringUtils.split("a b c", ' ')    = ["a", "b", "c"]
	 * </pre>
	 * 
	 * @param str
	 *          the String to parse, may be null
	 * @param separatorChar
	 *          the character used as the delimiter, <code>null</code> splits on
	 *          whitespace
	 * @return an array of parsed Strings, <code>null</code> if null String input
	 * @since 2.0
	 */
	public static List<String> split(String str, String separatorChars, int max) {
		// Performance tuned for 2.0 (JDK1.4)
		// Direct code is quicker than StringTokenizer.
		// Also, StringTokenizer uses isSpace() not isWhitespace()
		List<String> list = new ArrayList<String>();

		if (str == null) {
			return list;
		}
		int len = str.length();
		if (len == 0) {
			return list;
		}

		int sizePlus1 = 1;
		int i = 0, start = 0;
		boolean match = false;
		if (separatorChars == null) {
			// Null separator means use whitespace
			while (i < len) {
				if (Character.isWhitespace(str.charAt(i))) {
					if (match) {
						if (sizePlus1++ == max) {
							i = len;
						}
						list.add(str.substring(start, i));
						match = false;
					}
					start = ++i;
					continue;
				}
				match = true;
				i++;
			}
		} else if (separatorChars.length() == 1) {
			// Optimise 1 character case
			char sep = separatorChars.charAt(0);
			while (i < len) {
				if (str.charAt(i) == sep) {
					if (match) {
						if (sizePlus1++ == max) {
							i = len;
						}
						list.add(str.substring(start, i));
						match = false;
					}
					start = ++i;
					continue;
				}
				match = true;
				i++;
			}
		} else {
			// standard case
			while (i < len) {
				if (separatorChars.indexOf(str.charAt(i)) >= 0) {
					if (match) {
						if (sizePlus1++ == max) {
							i = len;
						}
						list.add(str.substring(start, i));
						match = false;
					}
					start = ++i;
					continue;
				}
				match = true;
				i++;
			}
		}
		if (match) {
			list.add(str.substring(start, i));
		}
		return list;
	}

	public static String justify(byte _mode, int _space, String _value) {

		// center
		int padding;
		if (_mode == CENTER) {
			padding = (_space - _value.length()) >> 1;
		} else {
			padding = _space - _value.length();
		}
		StringBuilder newValue = new StringBuilder(_value);

		// StringBuffer padString = new StringBuffer();
		for (int x = 0; x < padding; x++) {
			if (_mode == CENTER || _mode == RIGHT) {
				newValue.insert(0, " ");
			}
			if (_mode == CENTER || _mode == LEFT) {
				newValue.append(" ");
			}
		}

		if (_mode == CENTER && newValue.length() != _space)
			newValue.append(" ");

		return newValue.toString();
	}

	public static boolean regexMatches(String str, String re) throws MalformedPatternException {
		PatternMatcher matcher = new Perl5Matcher();
		PatternCompiler compiler = new Perl5Compiler();
		PatternMatcherInput input = new PatternMatcherInput(str);

		Pattern pattern = compiler.compile(re, Perl5Compiler.SINGLELINE_MASK);
		return matcher.matches(input, pattern);
	}

}// string
