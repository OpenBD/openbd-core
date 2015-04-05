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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.nary.util.date.dateTimeTokenizer;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.dateFormat;
import com.naryx.tagfusion.expression.function.functionBase;

public class LSDateFormat extends functionBase {
  
  private static final long serialVersionUID = 1L;
	
  public LSDateFormat(){
    min = 1; max = 2; 
  }
  
  public String[] getParamInfo(){
		return new String[]{
			"date1",
			"format string; short cuts ('short', 'medium', 'long', 'full') or customized string"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"locale", 
				"Formats a date string to a given output using the current sessions locale", 
				ReturnType.STRING );
	}
  
  
  public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException{
    int dateIndex = parameters.size() != 2 ? 0 : 1;
    
    try{
      cfData theDate = parameters.get(dateIndex);
      
      Date date;
      
      if ( dateFormat.isEmptyString( theDate ) ){
      	return new cfStringData("");
      }
      
      if( theDate.getDataType() == cfData.CFDATEDATA )
        date = new Date( theDate.getLong() );
      else if ( theDate.getDataType() == cfData.CFSTRINGDATA && theDate.getString().length() == 0 ){
      	return cfStringData.EMPTY_STRING;
      }else if ( theDate.getDataType() == cfData.CFNUMBERDATA ){
      	date = theDate.getDateData().getCalendar().getTime();
      }else
        date = dateTimeTokenizer.getDate( theDate.getString(), _session.getLocale());
      
      if(date == null){
        date = dateTimeTokenizer.getNeutralDate( theDate.getString() ); 
        if ( date == null ){
          cfCatchData catchData = new cfCatchData(_session);
          catchData.setType("Function");
          catchData.setDetail("LSDateFormat");
          catchData.setMessage("Invalid date.");
          throw new cfmRunTimeException(catchData);
        }
      }
      
      SimpleDateFormat dateformat;
      if(parameters.size() == 2){
        String format = parameters.get(0).getString();
        format = format.toLowerCase();
        if(format.equals("full"))
          dateformat = (SimpleDateFormat)DateFormat.getDateInstance( SimpleDateFormat.FULL, _session.getLocale());
        else if(format.equals("long"))
          dateformat = (SimpleDateFormat)DateFormat.getDateInstance( SimpleDateFormat.LONG, _session.getLocale());
        else if(format.equals("medium"))
          dateformat = (SimpleDateFormat)DateFormat.getDateInstance( SimpleDateFormat.MEDIUM, _session.getLocale());
        else if(format.equals("short")){
          dateformat = (SimpleDateFormat)DateFormat.getDateInstance( SimpleDateFormat.SHORT, _session.getLocale());
        } else{
          // A custom format string was specified.
          return new cfStringData(com.nary.util.Date.cfmlFormatDate( date.getTime(), format, _session.getLocale() ));
        }
      
      }else{
        dateformat = (SimpleDateFormat)DateFormat.getDateInstance( SimpleDateFormat.MEDIUM, _session.getLocale());
      }
      return new cfStringData(dateformat.format(date));
    }
    catch(IllegalArgumentException e){
      cfCatchData catchData = new cfCatchData(_session);
      catchData.setType("Function");
      catchData.setDetail("LSDateFormat");
      catchData.setMessage("Invalid date mask.");
      throw new cfmRunTimeException(catchData);
    }
        
  }

}
