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

package com.naryx.tagfusion.expression.function.string;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;

import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;


public class reReplace extends reReplaceBase {
	
	private static final long serialVersionUID = 1L;
  
	protected String doRereplace( String _theString, String _theRE, String _theSubstr, boolean _casesensitive, boolean _replaceAll ) throws cfmRunTimeException{
		int replaceCount = _replaceAll ? Util.SUBSTITUTE_ALL : 1; 
		PatternMatcher matcher = new Perl5Matcher();
		Pattern pattern = null;
		PatternCompiler compiler = new Perl5Compiler();
    
		try {
			if ( _casesensitive ){
				pattern = compiler.compile( _theRE, Perl5Compiler.SINGLELINE_MASK );
			}else{
				pattern = compiler.compile( _theRE, Perl5Compiler.CASE_INSENSITIVE_MASK | Perl5Compiler.SINGLELINE_MASK );
			}
			
		} catch(MalformedPatternException e){ // definitely should happen since regexp is hardcoded
			cfCatchData catchD = new cfCatchData();
			catchD.setType( "Function" );
			catchD.setMessage( "Internal Error" );
			catchD.setDetail( "Invalid regular expression ( " + _theRE + " )" );
			throw new cfmRunTimeException( catchD );
		}

		// Perform substitution and print result.
		return Util.substitute(matcher, pattern, new Perl5Substitution( processSubstr( _theSubstr ) ), _theString, replaceCount );
	}//rereplace()

	// replaces back references in the format \1 with $1 making sure to
	// add escaping for the existing $'s and handle escaping \'s
	private String processSubstr( String _substr ){
		StringBuilder res = new StringBuilder( _substr );
		int i = 0;

		while ( i < res.length() )
		{
      
			switch( res.charAt(i) )
			{
				case '\\':
					if ( i+1 < res.length() )
					{
						char nextCh = res.charAt(i+1);
						if ( Character.isDigit( nextCh ) )
						{
							res.setCharAt( i, '$' );
						}
						else if ( ( nextCh == '\\' ) && ( i+2 < res.length() ) )
						{
							nextCh = res.charAt(i+2);
							if ( !isEsc( nextCh ) ) 
							{
								// It's not a \\U, \\L, etc. so escape the back slash
								res.insert( i+1, '\\' );
								i++; // extra increment
							}
						}
						else if ( !isEsc( nextCh ) )
						{
							// It's not a '\U', '\L', etc. so escape the back slash
							res.insert( i+1, '\\' );
							i++; // extra increment
						}
					}
					else
					{
						res.insert( i+1, '\\' );
						i++; // extra increment
					}
					i++;
					break;

        case '$':
          res.insert( i, '\\' );
          i+=2;
          break;
				default:
					i++;
					break;
			}
		}

		return res.toString();
	}// processSubStr()
  
	private boolean isEsc( char _c )
	{
		return _c == 'U' || _c == 'u' || _c == 'L' || _c == 'l' || _c == 'E';
	}
  
}// reReplace()
