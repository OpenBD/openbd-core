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

import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Generic utilities
 */
public class NumberUtils {
	  
	public static String getString( double d ) {
		if ( d - (int) d == 0 ){ // if this has just an integer part
			return String.format( "%1d", (int) d );
		}else{
			String formatted = String.format( "%1.12G", d );
			int expIndex = formatted.indexOf( 'E' );
			int decIndex = formatted.indexOf( '.' );
			
			// if there are trailing 0's remove them
			if ( expIndex == -1 && decIndex != -1 && formatted.endsWith( "0" ) ){
				StringBuilder sb = new StringBuilder( formatted );
				int count = sb.length() - 1; // skip the last char, we already know it's a 0
				while ( sb.charAt( count-1  ) == '0' ){
					count--;
				}
				if ( sb.charAt( count-1  ) == '.' ){
					count--;
				}
				sb.delete( count, sb.length() );
				return sb.toString();
				
			// trailing 0s in the scientific notation e.g. 4.90000000E12
			}else if ( expIndex != -1 && decIndex != -1 &&  formatted.charAt( expIndex-1 ) == '0' ){
				StringBuilder sb = new StringBuilder( formatted );
				int count = expIndex - 1; // skip the last char, we already know it's a 0
				while ( sb.charAt( count-1  ) == '0' ){
					count--;
				}
				if ( sb.charAt( count-1  ) == '.' ){
					count--;
				}
				sb.delete( count, expIndex );
				return sb.toString();
				
			}else{
				return formatted;
			}
		}
	}

  public static String decimalFormat( double d ) {
    java.text.DecimalFormat DF = new java.text.DecimalFormat("#,##0.00", new DecimalFormatSymbols( Locale.US ) );
    return DF.format( d );
  }
}
