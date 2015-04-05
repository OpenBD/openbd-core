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

package com.naryx.tagfusion.cfm.engine;

import java.io.CharArrayWriter;

import com.nary.util.charVector;
import com.naryx.tagfusion.cfm.parser.runTime;

/**
 * 
 * This class is used to look into the flow of bytes going back out should the
 * client require the #...# to be parsed out. Commonly used by the CFOUTPUT tag.
 */

public class cfOutputFilter extends Object {

	private cfSession session;

	private StringBuilder expression = null;

	private boolean ignoreRepeated;

	private int bracketCount = 0;

	private int sqBracketCount = 0;

	private charVector endMarkers;

	private char lastImpChar;

	private boolean possibleEscape;

	private boolean inString;

	public cfOutputFilter(cfSession _session, boolean _ignoreRepeated) {
		session = _session;
		ignoreRepeated = _ignoreRepeated;
		endMarkers = new charVector(5);
	}

	public cfOutputFilter() {
		// ---[ This constructor is for the cfParseTag for pulling out expressions
		// at run time
		endMarkers = new charVector(5);
		session = null;
	}

	public String getExpression() {
		return expression.toString();
	}

	public boolean shouldWeIgnore() {
		return ignoreRepeated;
	}

	public void endFilter(cfSession _session) throws cfmRunTimeException {
		if (expression != null) {
			cfCatchData catchData = new cfCatchData(_session);
			catchData.setType("Expression");
			catchData.setDetail("Unclosed expression.");
			catchData.setMessage("Check you have closed all #, \", and ' expressions.");
			throw new cfmRunTimeException(catchData);
		}
	}

	public boolean write(int ch, CharArrayWriter _out) throws cfmRunTimeException {

		// --[ Look for the start of a possible expression
		if (ch == '#' && bracketCount == 0 && sqBracketCount == 0 && endMarkers.size() == 0) {

			if (expression == null) {

				// --[ The expression string is just starting, so lets save the bytes
				expression = new StringBuilder();
				return false;

			} else {

				if (session != null) {
					if (expression.length() == 0)
						_out.write('#');
					else {
						cfData token = runTime.runExpression(session, expression.toString());
						if (token != null) {
							String tokenStr = token.getString();
							_out.write(tokenStr.toCharArray(), 0, tokenStr.length());
						}
						lastImpChar = (char) -1;
					}
				} else if (session == null)
					return true;

				expression = null;
				bracketCount = 0;
			}

		} else if (expression != null) {

			// update the variables so we know when the expression has finished
			if (possibleEscape && ch == lastImpChar) { // was an escape char
				inString = true;
				if (ch != '#') {
					endMarkers.add((char) ch);
				}
				possibleEscape = false;
			} else if (inString) {
				possibleEscape = false;
				if (ch == '#') {
					lastImpChar = '#';
					inString = false;
					possibleEscape = true;
				} else if ((endMarkers.size() > 0) && (ch == endMarkers.getLast())) {
					// assume this is the end of the string but store
					// the char in case next char is the escape char for
					// this single/double quote
					lastImpChar = (char) ch;
					inString = false;
					possibleEscape = true;
					endMarkers.removeLast();
				}
				// else, don't do anything
			} else {
				possibleEscape = false;
				if (ch == '#') {
					inString = true;
				} else if (ch == '(') {
					bracketCount += 1;
				} else if (ch == ')') {
					if (bracketCount == 0) {
						cfCatchData catchData = new cfCatchData();
						catchData.setMessage("Expression error: closing parenthesis without opening parenthesis");
						throw new cfmBadFileException(catchData);
					}
					bracketCount -= 1;
				} else if (ch == '[') {
					sqBracketCount += 1;
				} else if (ch == ']') {
					if (sqBracketCount == 0) {
						cfCatchData catchData = new cfCatchData();
						catchData.setMessage("Expression error: closing bracket without opening bracket");
						throw new cfmBadFileException(catchData);
					}
					sqBracketCount -= 1;
				} else if (ch == '"' || ch == '\'') {
					inString = true;
					endMarkers.add((char) ch);
				}
			}
			expression.append((char) ch);

			// not in an expression, nor at the start of one.
		} else {
			_out.write(ch);
		}

		return false;
	}

}
