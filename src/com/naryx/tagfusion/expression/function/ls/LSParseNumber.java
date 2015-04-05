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

package com.naryx.tagfusion.expression.function.ls;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;
 
public class LSParseNumber extends functionBase {
  private static final long serialVersionUID = 1L;

  public LSParseNumber(){  min =  max = 1; }
  
  public String[] getParamInfo(){
		return new String[]{
			"string"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"locale", 
				"Attempts to convert the string represented to a number using the current sessions locale", 
				ReturnType.NUMERIC );
	} 
 
  
  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
    String number = parameters.get(0).getString();
    
    try{
      Number num = parseNumber( number, _session.getLocale() );
      return new cfNumberData( num.doubleValue() );
    }catch( ParseException pe ){
      throwException( _session,  "Failed to parse number: " + number );
      return null; // keep compiler happy
    }
    
  }
  
  protected static Number parseNumber( String _num, Locale _locale ) throws ParseException{
    NumberFormat numFormat = NumberFormat.getNumberInstance( _locale );
    return numFormat.parse( cleanNumber( _num ) );
  }

  
  private static String cleanNumber( String _number ){
  	StringBuilder number = new StringBuilder( _number );
    boolean isNegative = false;

    int index = 0;
    while ( index < number.length() ){
      
      char nextCh = number.charAt(index);
      
      switch( nextCh ){
        case '-':
          isNegative = true;
        case '+':
        case ' ':
          number = number.deleteCharAt( index );
          break;
        default:
          index++;
        
      }
      
    }
    
    if ( isNegative ){
      number.insert( 0, '-' );
    }
    return number.toString();
  }
  
}
