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
 *  $Id: reFind.java 1597 2011-06-22 02:17:29Z alan $
 */

package com.naryx.tagfusion.expression.function.string;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class reFind extends functionBase {
	private static final long serialVersionUID = 1L;

	private boolean caseSensitiveMatch;

	public reFind() {
		min = 2;
		max = 4;
		caseSensitiveMatch = true;
		setNamedParams( new String[]{ "regular", "string","start","subexpression"} );
	}

	protected void setCaseSensitive(boolean _bool) {
		caseSensitiveMatch = _bool;
	}

	public String[] getParamInfo(){
		return new String[]{
			"regular expression",
			"string to search",
			"start position - default 1",
			"subexpression flag - default false; determines if a structure of the position is returned, or just the position"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"string", 
				"Performs a case-sensitive regular expression match to the given string.  If subexpression=true then it returns a structure (pos,len)", 
				ReturnType.STRUCTURE );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String regexp 			= getNamedStringParam(argStruct, "regular", "");
		String strToSearch 	= getNamedStringParam(argStruct, "string", "");
		int startingPos 		= getNamedIntParam(argStruct, "start", 1);
		boolean subexps 		= getNamedBooleanParam(argStruct, "subexpression",false);

		if (regexp.length() == 0 || strToSearch.length() == 0) {
			int pos;
			if (strToSearch.length() == 0) {
				pos = 0;
			} else {
				pos = startingPos;
			}

			
			if (subexps) {
				cfArrayData posArray = cfArrayData.createArray(1);
				posArray.addElement(new cfNumberData(pos));
				cfArrayData lenArray = cfArrayData.createArray(1);
				lenArray.addElement(new cfNumberData(0));
				return createStruct(lenArray, posArray);
			} else {
				return new cfNumberData(pos);
			}
		}

		// Make sure the startingPosition is not beyond what we need to search
		if (startingPos < 1) {
			startingPos = 1;
		}
		
		if (subexps) {
			// returns cfStructData
			return execWithSubExpressions(regexp, strToSearch, startingPos);
		} else {
			// returns cfNumberData
			return execNoSubExpressions(regexp, strToSearch, startingPos);
		}

	}// execute()

	
	// Note offset and index returned are in the range 1+. Except if no match is found 0 is returned.
	private cfNumberData execNoSubExpressions(String _regexp, String _strToSearch, int _offset) throws cfmRunTimeException {

		PatternMatcher matcher;
		PatternCompiler compiler;
		Pattern pattern;
		PatternMatcherInput input;
		MatchResult result;

		compiler = new Perl5Compiler();
		matcher = new Perl5Matcher();

		try {
			if (caseSensitiveMatch) {
				pattern = compiler.compile(_regexp, Perl5Compiler.SINGLELINE_MASK);
			} else {
				pattern = compiler.compile(_regexp, Perl5Compiler.CASE_INSENSITIVE_MASK | Perl5Compiler.SINGLELINE_MASK);
			}
		} catch (MalformedPatternException e) {
			cfCatchData catchD = new cfCatchData();
			catchD.setType("Function");
			catchD.setMessage("REFind - invalid parameter");
			catchD.setDetail("Invalid regular expression ( " + _regexp + " )");
			throw new cfmRunTimeException(catchD);
		}

		input = new PatternMatcherInput(_strToSearch);
		input.setCurrentOffset(_offset - 1);

		if (matcher.contains(input, pattern)) {
			result = matcher.getMatch();

			return new cfNumberData(result.beginOffset(0) + 1);

		} else {
			return new cfNumberData(0);
		}

	}

	private cfStructData execWithSubExpressions(String _regexp, String _strToSearch, int _offset) throws cfmRunTimeException {
		cfArrayData length = cfArrayData.createArray(1);
		cfArrayData pos = cfArrayData.createArray(1);

		int groups;
		PatternMatcher matcher;
		PatternCompiler compiler;
		Pattern pattern;
		PatternMatcherInput input;
		MatchResult result;

		compiler = new Perl5Compiler();
		matcher = new Perl5Matcher();

		try {
			if (caseSensitiveMatch) {
				pattern = compiler.compile(_regexp, Perl5Compiler.SINGLELINE_MASK);
			} else {
				pattern = compiler.compile(_regexp, Perl5Compiler.CASE_INSENSITIVE_MASK | Perl5Compiler.SINGLELINE_MASK);
			}
		} catch (MalformedPatternException e) {
			cfCatchData catchD = new cfCatchData();
			catchD.setType("Function");
			catchD.setMessage("REFind - invalid parameter");
			catchD.setDetail("Invalid regular expression ( " + _regexp + " )");
			throw new cfmRunTimeException(catchD);
		}

		input = new PatternMatcherInput(_strToSearch);
		input.setCurrentOffset(_offset - 1);

		if (matcher.contains(input, pattern)) {
			result = matcher.getMatch();

			int startInside = result.beginOffset(0);
			int lenInside = result.length();

			length.setData(1, new cfNumberData(lenInside));
			pos.setData(1, new cfNumberData(startInside + 1));

			groups = result.groups();

			// Start at 1 because we just printed out group 0
			for (int group = 1; group < groups; group++) {
				length.setData(group + 1, new cfNumberData(result.end(group) - result.begin(group)));
				pos.setData(group + 1, new cfNumberData(result.beginOffset(group) + 1));

				/*
				 * System.out.println(group + ": " + result.group(group));
				 * System.out.println("Begin: " + result.beginOffset(group));
				 * System.out.println("End: " + result.end(group) - result.begin(group)
				 * );
				 */
			}

			/*
			 * int index = 0; int startInside = r.getParenStart( index ); int
			 * lenInside = r.getParenLength( index );
			 * 
			 * // note cfArrayData indexes start from 1 but getParenStart(), //
			 * getParenLength() and the index returned from getParenStart // start
			 * from 0 so +1's are done accordingly while ( startInside != -1 ){
			 * length.setData( index + 1, new cfNumberData( lenInside ) );
			 * pos.setData( index + 1, new cfNumberData( startInside + 1 ) ); index++;
			 * startInside = r.getParenStart( index ); lenInside = r.getParenLength(
			 * index ); }
			 */

		} else {
			length.setData(1, new cfNumberData(0));
			pos.setData(1, new cfNumberData(0));
		}

		// create the struct containing the pos, len arrays as fields
		return createStruct(length, pos);
	}

	
	private static cfStructData createStruct(cfArrayData _len, cfArrayData _pos) {
		cfStructData struct = new cfStructData();
		struct.setData("pos", _pos);
		struct.setData("len", _len);
		return struct;
	}

}