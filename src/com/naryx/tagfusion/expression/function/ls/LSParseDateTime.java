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
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.nary.util.date.dateTimeTokenizer;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class LSParseDateTime extends functionBase {

  private static final long serialVersionUID = 1L;

  public LSParseDateTime(){  min =  max = 1; }
  
  public String[] getParamInfo(){
		return new String[]{
			"date1",
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"locale", 
				"Formats a date string to a given output using the current session locale", 
				ReturnType.DATE );
	}

  
  public cfData execute( cfSession _session, List<cfData> parameters )
      throws cfmRunTimeException {
    String dateTimeStr = parameters.get(0).getString();
    Locale locale = _session.getLocale();
    
    cfDateData date = isDateTime( dateTimeStr, locale );
    if ( date == null ){
      throwException( _session, "Failed to parse date/time string: " + dateTimeStr );
    }
    
    return date;
  }
  
  public static cfDateData isDateTime( String _dateTimeStr, Locale _locale ){
    
    // we don't know what the format of the date will be so we loop through all the possibilities
    int [] dateformats = new int[]{ DateFormat.SHORT, DateFormat.MEDIUM, DateFormat.LONG, DateFormat.FULL};
    ParsePosition pp = new ParsePosition(0);
    Date parseResult = null;

    // date only
    for ( int i = 0; i < dateformats.length; i++ ){
    	pp.setIndex( 0 );
      DateFormat df = DateFormat.getDateInstance( dateformats[i], _locale );
      df.setLenient( false );
      parseResult = df.parse( _dateTimeStr, pp );
      if ( parseResult != null && pp.getIndex() == _dateTimeStr.length() )
        return new cfDateData( parseResult );
    }
    
    // date and time
    for ( int i = 0; i < dateformats.length; i++ ){
      for ( int j = 0; j < dateformats.length; j++ ){
      	pp.setIndex( 0 );
      	DateFormat df = DateFormat.getDateTimeInstance( dateformats[i], dateformats[j], _locale );
        df.setLenient( false );
        parseResult = df.parse( _dateTimeStr, pp );
        if ( parseResult != null && pp.getIndex() == _dateTimeStr.length() )
          return new cfDateData( parseResult );
      }

    }
    
    // time only
    for ( int i = 0; i < dateformats.length; i++ ){
    	pp.setIndex( 0 );
    	DateFormat df = DateFormat.getTimeInstance( dateformats[i], _locale );
      df.setLenient( false );
      parseResult = df.parse( _dateTimeStr, pp );
      if ( parseResult != null && pp.getIndex() == _dateTimeStr.length() ){
      	// note since only the time is set we need to adjust the date to the cf epoch instead of the java one
      	long adjustedTime = cfDateData.CF_EPOCH + parseResult.getTime();
        return new cfDateData( adjustedTime );
      }
    }
    
    
    java.util.Date d = dateTimeTokenizer.getDate( _dateTimeStr, _locale );
    if ( d != null )
      return new cfDateData(d.getTime());
    
    
    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss", _locale );
    String [] patterns = new String[] {  "yyyy-MM-dd hh:mm:ss a z",  "yyyy-MM-dd hh:mm:ss a", 
        "yyyy-MM-dd hh:mm a z", "yyyy-MM-dd hh:mm a", "yyyy-MM-dd HH:mm:ss", "HH:mm a z", "HH:mm:ss Z" };
    
    for ( int i = 0; i < patterns.length; i++ ){
      sdf.applyPattern( patterns[i] );
      parseResult = sdf.parse( _dateTimeStr, pp );
      if ( parseResult != null && pp.getIndex() == _dateTimeStr.length() )
        return new cfDateData( parseResult );
    }
    
    return null;
  }
  
}
