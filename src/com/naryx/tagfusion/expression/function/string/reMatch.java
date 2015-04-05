/* 
 *  Copyright (C) 2000 - 2009 TagServlet Ltd
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

package com.naryx.tagfusion.expression.function.string;

/**
 * Implements the reFindNoCase function. Note that it 
 * just executes an instance of reFind but sets caseSensitive to false first
 */

import java.util.HashSet;

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
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class reMatch extends functionBase {

	private static final long serialVersionUID = 1L;
	protected boolean caseSensitiveMatch = true;
	
	public reMatch() {
		min = 2; max = 3;
		setNamedParams( new String[]{ "regular", "string","unique"} );
	}

	public String[] getParamInfo(){
		return new String[]{
			"regular expression",
			"string to search",
			"unique only - default false.  Returns only unique finds, repeated results are not returned"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"string", 
				"Returns an array of strings of all the matches that the given regular expression found performing a case-sensitive match", 
				ReturnType.ARRAY );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String regexp = getNamedStringParam(argStruct, "regular", "");
		String strToSearch = getNamedStringParam(argStruct, "string", "");
		boolean bUnique = getNamedBooleanParam(argStruct, "unique",false);
		
		HashSet<String>	uniqueTrack = null;
		if ( bUnique ){
			uniqueTrack	= new HashSet<String>();
		}


		/* Setup the RegEx */
		PatternCompiler compiler = new Perl5Compiler();
		Pattern pattern;
		
		try {
			if (caseSensitiveMatch) {
				pattern = compiler.compile(regexp, Perl5Compiler.SINGLELINE_MASK);
			} else {
				pattern = compiler.compile(regexp, Perl5Compiler.CASE_INSENSITIVE_MASK | Perl5Compiler.SINGLELINE_MASK);
			}
		} catch (MalformedPatternException e) {
			cfCatchData catchD = new cfCatchData();
			catchD.setType("Function");
			catchD.setMessage("REMatch - invalid parameter");
			catchD.setDetail("Invalid regular expression ( " + regexp + " )");
			throw new cfmRunTimeException(catchD);
		}

		/* Perform the search */
		cfArrayData	array	= cfArrayData.createArray(1);
		PatternMatcher matcher = new Perl5Matcher();
		MatchResult result;
		PatternMatcherInput input = new PatternMatcherInput( strToSearch );
		while ( matcher.contains(input, pattern) ) {
			result = matcher.getMatch();
			
			String strResult = result.toString();
			if ( bUnique ){
				if ( !uniqueTrack.contains( strResult ) ){
					array.addElement( new cfStringData( strResult ) );
					uniqueTrack.add( strResult );
				}
			}else		
				array.addElement( new cfStringData( strResult ) );
		}
		
		return array;
	}
}