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

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;

import com.nary.util.Localization;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class LSCurrencyFormat extends functionBase {
  
  private static final long serialVersionUID = 1L;
	
  public LSCurrencyFormat(){
    min = 1; max = 2; 
  }
  
	public String[] getParamInfo(){
		return new String[]{
			"number",
			"type - local, none, or international"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"locale", 
				"Returns back the formatted currency representation of the number in the current session locale", 
				ReturnType.STRING );
	}
  
  
  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
    double number;
    String type;
    String returnString = "";
    NumberFormat numFormat = NumberFormat.getCurrencyInstance( _session.getLocale() );
    int localeMinDecDigits = numFormat.getMinimumFractionDigits();
    int localeMaxDecDigits = numFormat.getMaximumFractionDigits();
    
    if ( parameters.size() == 2 ){
      number	= getDoubleWithChecks( _session, parameters.get(1) );
      type		= parameters.get(0).getString();
    } else {
      number	= getDoubleWithChecks( _session, parameters.get(0) );
      type		= "local";
    }
      
    if( type.equalsIgnoreCase( "local" ) ){
      returnString = numFormat.format(number);
    }

    else if( type.equalsIgnoreCase( "none" ) ){
      numFormat = NumberFormat.getInstance( _session.getLocale() );
      numFormat.setMinimumFractionDigits( localeMinDecDigits );
      numFormat.setMaximumFractionDigits( localeMaxDecDigits );
	  Localization.updateNoneCurrencyFormat( _session.getLocale(), numFormat );
      returnString = numFormat.format(number);
    }
      
    else if( type.equalsIgnoreCase( "international" ) ){
      numFormat = NumberFormat.getInstance( _session.getLocale() );
      numFormat.setMinimumFractionDigits( localeMinDecDigits );
      numFormat.setMaximumFractionDigits( localeMaxDecDigits );
      returnString = new DecimalFormatSymbols( _session.getLocale() ).getInternationalCurrencySymbol() + numFormat.format(number);
    }
    else
      throwException( _session,  " invalid  type: "+type+" in LSCurrencyFormat" );
    
    return new cfStringData( returnString );
	}
 }
