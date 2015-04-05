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

/**
 * This class operates in exactly the same manner as StringTokenizer, except it
 * works! It handles delimitors of variable length, and empty tokens.
 */

public class stringtokenizer extends Object {

	/**
	 * The delimiter used to separate tokens.
	 */

	/**
	 * The string to be parsed.
	 */

	private String Text, delim;

	/**
	 * The total number of tokens in the string.
	 */
	private int totalTokens, c1, currentToken;



	/**
	 * Constructs a string tokenizer for the specified string. The characters in
	 * the delim argument are the delimiters for separating tokens.
	 * 
	 * @param Text
	 *          a string to be parsed.
	 * @param delim
	 *          the delimiters.
	 */
	public stringtokenizer(String _Text, String _deliminator) {
		Text = _Text;
		delim = _deliminator;
		totalTokens = getTokens();
		currentToken = 0;
		c1 = 0;
	}

	/**
	 * Returns the number of tokens in the tokenizer's string.
	 */

	public int countTokens() {
		return totalTokens;
	}

	/**
	 * Check to see if there are more tokens available from this tokenizer's
	 * string.
	 * 
	 * @return If there are more tokens available from this string, return true,
	 *         else return false.
	 */

	public boolean hasMoreTokens() {
		if (currentToken < totalTokens && totalTokens != 0)
			return true;
		else
			return false;
	}

	/**
	 * returns the token at the specified position
	 * 
	 * @Param _index:
	 *          The index of the required token
	 */

	public String getTokenAt(int _index) {
		if (_index > totalTokens - 1)
			return null;

		// --[ search through tokens and return the one at the specified index
		int tokenCount = -1;
		String token = "";
		while ((token = nextToken()) != null) {
			tokenCount++;

			if (_index == tokenCount)
				break;
		}

		return token;
	}

	/**
	 * It is same as hasMoreTokens method.
	 */

	public boolean hasMoreElements() {
		return hasMoreTokens();
	}

	/**
	 * Returns the next token from this string tokenizer.
	 * 
	 * @return The next token from this string tokenizer.
	 */

	public String nextToken() {
		int c2 = 0;

		if (currentToken != 0)
			c2 = Text.indexOf(delim, c1) + delim.length();

		if (c2 != -1) {
			c1 = Text.indexOf(delim, c2);
			if (c1 == -1)
				c1 = Text.length();

			currentToken++;
			return Text.substring(c2, c1);
		} else {
			return null;
		}
	}

	/**
	 * Returns the same value as the nextToken method, except that its declared
	 * return value is Object rather than String.
	 * 
	 * @return the next token in the string.
	 */
	public Object nextElement() {
		return nextToken();
	}

	/**
	 * Calculates the number of tokens in the tokenizer's string.
	 * 
	 * @return The number of tokens in the string.
	 */

	public int getTokens() {
		if (Text.length() == 0)
			return 0;

		int noTokens = 1;
		int c1 = Text.indexOf(delim);
		while (c1 != -1) {
			noTokens++;
			c1 = Text.indexOf(delim, c1 + delim.length());
		}
		return noTokens;
	}

	public static void main(String arg[]) {
		stringtokenizer ST = new stringtokenizer("this||||is|", "|");
		while (ST.hasMoreTokens()) {
			System.out.println(ST.nextToken());
		}
	}

}
