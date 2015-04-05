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

package com.naryx.tagfusion.cfm.tag;

import java.util.Map;

/*
 * This class is used to decode the tag attributes into a hashtable.
 * The data for parameters in the tag can be delimited by spaces, tabs, quotes or single quotes.
 */

public final class tagParameters extends Object {

	Map 		params;
	char[] 	expArray;
	int			ptrIndex;

	private static char SPACE					= ' ';
  private static char TAB           = '\t';
	private static char EQUALS				= '=';
	private static char QUOTE					= '"';
	private static char SINGLEQUOTE		= '\'';
	private static char NEWLINE					= '\n';
	private static char CARRIAGERETURN	= '\r';
  private static char HASH  = '#';

	public tagParameters( String _tag, boolean onlyOne, Map _params ) throws Exception {
		params	= _params;
		
		int min = 9999;
    int c1 = _tag.indexOf(" ");
    if ( c1 != -1 && c1 < min )
    	min = c1;
    	
    c1 = _tag.indexOf(TAB);
    if ( c1 != -1 && c1 < min )
    	min = c1;

    c1 = _tag.indexOf( NEWLINE );
    if ( c1 != -1 && c1 < min )
    	min = c1;

    c1 = _tag.indexOf( CARRIAGERETURN );
    if ( c1 != -1 && c1 < min )
    	min = c1;
    
    if ( min == 9999 ) return;
		
		String tag	= _tag.substring( min, _tag.length()-1 );
		
		if ( onlyOne ){
			
			c1	= tag.indexOf("=");
			if ( c1 == -1 )
				throw new Exception();

			String key	 = tag.substring( 0, c1 ).toUpperCase().trim();
			String value = tag.substring( c1+1 ).trim();
			params.put( key, value );
			
		} else {
			
			expArray		= new char[ tag.length() ];
			ptrIndex		= 0;
			tag.getChars( 0, tag.length(), expArray, 0 );
			parseArray();
			
		}
	}// tagParameters()
	

	private void parseArray() throws Exception {
		String key = null,data = null;
	
		while ( ptrIndex < expArray.length ){
			
			if ( key != null && Character.isJavaIdentifierStart( expArray[ ptrIndex ] ) ){

				params.put( key.toUpperCase(), "true" );
				key 	= readKey();

			} else if ( key == null && Character.isJavaIdentifierStart( expArray[ ptrIndex ] ) ){

				key 	= readKey();

			} else if ( key != null && expArray[ ptrIndex ] == EQUALS ) {

				ptrIndex++;
				data	= readData();
				params.put( key.toUpperCase(), data );
				key 	= data = null;

			} else
				ptrIndex++;
				
		}
		
		//--[ If there was a final attribute at the end
		if ( key != null )
			params.put( key.toUpperCase(), "true" );

  }// parseArray()
  

	private String readData() throws Exception {

		//--[ Skip white space
		while ( (ptrIndex < expArray.length) && (expArray[ ptrIndex ] == SPACE 
				|| expArray[ ptrIndex ] == TAB || expArray[ ptrIndex ] == NEWLINE 
				|| expArray[ ptrIndex ] == CARRIAGERETURN))
			ptrIndex++;

		StringBuilder data = new StringBuilder();
		char  delimitor   = ' ';

		if ( expArray[ ptrIndex ] == QUOTE || expArray[ ptrIndex ] == SINGLEQUOTE || expArray[ ptrIndex ] == HASH ){
			data.append( expArray[ ptrIndex ] );
			delimitor = expArray[ ptrIndex ]; 
			ptrIndex++;
			StringBuilder signChars = new StringBuilder();
			signChars.append( delimitor );
  
			while ( ptrIndex < expArray.length ){
				// check for escape sequence first
				if ( expArray[ ptrIndex ] == delimitor 
						&& ((ptrIndex + 1) < expArray.length) && (expArray[ ptrIndex + 1 ] == delimitor)){
					data.append( expArray[ ptrIndex ] );
					ptrIndex++; // if escape sequence then advance past the first char of the escape seq. The chars that apply are " and '
				}else if ( expArray[ ptrIndex ] == QUOTE || expArray[ ptrIndex ] == SINGLEQUOTE || expArray[ ptrIndex ] == HASH ){
					char lastSignChar = signChars.charAt( signChars.length()-1 );

					if ( expArray[ptrIndex] == lastSignChar ){
						if ( signChars.length() == 1 ){
							// we have reached the last char
							data.append( expArray[ ptrIndex ] );
							break;
						}else{
							signChars.deleteCharAt( signChars.length()-1 ); // remove last significant char
						}
					// NOT (if this is a single quote within a double quote delimited string or vice versa)
					}else if ( !( ( expArray[ ptrIndex ] == QUOTE && lastSignChar == SINGLEQUOTE ) 
							|| ( expArray[ ptrIndex ] == SINGLEQUOTE && lastSignChar == QUOTE ) ) ){
						signChars.append( expArray[ptrIndex] );
					}
				}

				data.append( expArray[ ptrIndex ] );
				ptrIndex++;
			}

			// if not delimited by quotes or #, then we can simplify the logic for parsing this
		}else{
			// write char to data and move pointer on 
			data.append( expArray[ ptrIndex++ ] );

			while ( ptrIndex < expArray.length ){
				if ( expArray[ ptrIndex ] == SPACE || expArray[ ptrIndex ] == TAB 
						|| expArray[ ptrIndex ] == CARRIAGERETURN || expArray[ ptrIndex ] == NEWLINE ){
					break;
				}else if ( expArray[ ptrIndex ] == HASH || expArray[ ptrIndex ] == QUOTE || expArray[ ptrIndex ] == SINGLEQUOTE ){
					throw new Exception( "Unexpected character found: " + expArray[ptrIndex] );
				}else{
					data.append( expArray[ ptrIndex++ ] );
				}
			}
  
		}
		return data.toString().trim();
	}


	private String readKey() throws Exception {
		StringBuilder identifier = new StringBuilder();
		identifier.append( expArray[ ptrIndex ] );
		int sqBracket = 0;

		ptrIndex++;
		while ( ptrIndex < expArray.length ){
			if ( expArray[ ptrIndex ] == '[' )
				sqBracket++;
			else if ( expArray[ ptrIndex ] == ']' )
				--sqBracket;
			else if ( !(Character.isJavaIdentifierPart(expArray[ ptrIndex ]) || expArray[ ptrIndex ] == ':' ) && sqBracket == 0 && expArray[ ptrIndex ] != '.' )
				break;								
			
			identifier.append( expArray[ ptrIndex ] );
			ptrIndex++;
		}
		
		return identifier.toString();
	}

}

