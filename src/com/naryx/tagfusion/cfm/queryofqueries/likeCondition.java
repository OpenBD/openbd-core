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

package com.naryx.tagfusion.cfm.queryofqueries;

/**
 * This class represents a like condition.
 */
 

import java.util.List;
import java.util.Map;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
 
class likeCondition extends condition{

	expression str1, str2;
	boolean not;
	char escapeChar;
	
	boolean isStringComp; // indicates if the RHS of the LIKE expression is a stringExpression
	private String likeExpression; // used for caching the RHS of the LIKE expression if it's a stringExpression 
	
	private static String REGEXP_CHARS = "\\*+?|{[()^$.#";
	
	likeCondition( expression _s1, expression _s2, boolean _not, char _esc ){
		str1 = _s1;
		str2 = _s2;
		not = _not;
		escapeChar = _esc;
		isStringComp = str2 instanceof stringExpression;
	}
	
	
	boolean evaluate( rowContext _rowContext, List<cfData> _pData ) throws cfmRunTimeException{
		return xor( not, execLike( str1.evaluate( _rowContext, _pData ), str2.evaluate( _rowContext, _pData ) ) );
	}
	
	public boolean evaluate( ResultRow _row, List<cfData> data, Map<String, Integer> lookup ) throws cfmRunTimeException {
		return xor( not, execLike( str1.evaluate( _row, data, lookup ), str2.evaluate( _row, data, lookup ) ) );
	}
			
	boolean execLike( cfData _str1, cfData _str2 ) throws cfmRunTimeException{
		
		String strToSearch = _str1.getString();
 		String likeString = getLikeExpression( _str2 );
		
		PatternMatcher matcher;
 		PatternCompiler compiler;
 		Pattern pattern;
 		PatternMatcherInput input;

 		compiler = new Perl5Compiler();
 		matcher  = new Perl5Matcher();

		try {
			pattern = compiler.compile( likeString );
 		} catch( MalformedPatternException e ) {
   		return false;
		}
		
		input   = new PatternMatcherInput( strToSearch );
		
		if( matcher.matches( input, pattern ) ) {
   		return true;
		}else{
			return false;
		}
	}
	
	
	private String getLikeExpression( cfData _str2 ) throws dataNotSupportedException, cfmRunTimeException{
		if ( likeExpression != null && isStringComp ){
			return likeExpression;
		}
		
		String likeString = _str2.getString();
		
		if ( escapeChar != -1 ){
			StringBuilder strOut = new StringBuilder(); 

			for ( int i = 0; i < likeString.length(); i++ ){
				char nextCh = likeString.charAt( i );
				if ( nextCh == escapeChar && i+1 < likeString.length() ){
					i++;
					
					nextCh = likeString.charAt( i );
					if ( nextCh == '_' || nextCh == '%' ){
						strOut.append( nextCh );
					}else if ( REGEXP_CHARS.indexOf( nextCh ) != -1 ){
						strOut.append( escapeChar );
						strOut.append( '\\' ); // escape regex char
						strOut.append( nextCh );
						
					}else{
						strOut.append( escapeChar );
						strOut.append( nextCh );
					}

				}else if ( nextCh == '_' ){
					strOut.append( '.' );
				}else if ( nextCh == '%' ){
					strOut.append( "(.)*" );
				}else if ( REGEXP_CHARS.indexOf( nextCh ) != -1 ){
					strOut.append( '\\' ); // escape regex char
					strOut.append( nextCh );
				}else{
					strOut.append( nextCh );
				}
				
			}
			likeString = strOut.toString();
		}else{		
			likeString = regexEscape( likeString );
			likeString = com.nary.util.string.replaceString( likeString, "_", "." );
			likeString = com.nary.util.string.replaceString( likeString, "%", "(.)*" );
		}

		if ( isStringComp ){ // cache this string
			likeExpression = likeString;
		}
		
		return likeString;
	}
	
	private static final char [] escapeChars = new char[]{ '\\', '*', '+', '?', '|', '{', '[', '(', ')', '^', '$', '.', '#' };
	private static final String [] escapeStrings = new String[]{ "\\\\", "\\*", "\\+", "\\?", "\\|", "\\{", "\\[",
		"\\(", "\\)", "\\^", "\\$", "\\.", "\\#" };
  
    private static String regexEscape( String _in ) {
    	try {
    		return string.replaceChars( _in, escapeChars, escapeStrings );
    	} catch ( Exception e ) { // won't happen since both arrays match in size
    		cfEngine.log( "Unexpected exception in likeCondition.regexEscape: "
    									+ e.getClass().getName() + "(" + e.getMessage() + ")" );
    		return _in;
    	}
    }
}
