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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

 
public class LSParseCurrency extends functionBase {
	
  private static final long serialVersionUID = 1L;
	
  public LSParseCurrency(){  min =  max = 1; }
  
  public String[] getParamInfo(){
		return new String[]{
			"currency string"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"locale", 
				"Attempts to convert the current represented in the string to a number using the current sessions locale", 
				ReturnType.NUMERIC );
	} 
  
  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
    String number = parameters.get(0).getString();
    Number returnNum;
    NumberFormat numFormat = DecimalFormat.getInstance( _session.getLocale() );
    
    // try none, local, international
    try{
      returnNum = numFormat.parse(number);
      return cfNumberData.getNumber( returnNum );
    }catch( java.text.ParseException ignored ){}
    
    try{  
      numFormat = DecimalFormat.getCurrencyInstance( _session.getLocale() );
      returnNum = numFormat.parse(number);
      return cfNumberData.getNumber( returnNum );
      
    }catch( java.text.ParseException ignored ){}
    
    // international: can't find a nicer way to do this
    try{
      // we substitute the international currency with the regular currency symbol first
      String intCurrSymbol = new DecimalFormatSymbols( _session.getLocale() ).getInternationalCurrencySymbol();
      String currSymbol = new DecimalFormatSymbols( _session.getLocale() ).getCurrencySymbol();
      number = string.replaceString( number, intCurrSymbol, currSymbol );
      numFormat = DecimalFormat.getCurrencyInstance( _session.getLocale() );
      returnNum = numFormat.parse( number );
      return cfNumberData.getNumber( returnNum );
    }catch( java.text.ParseException ignored ){}
    
    throwException( _session, "The value '" + number + "' is not a valid currency in the current locale." );
    
    // keep the compiler happy
    return new cfStringData("");
  }
}
