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

package com.nary.util.date;

import java.io.CharArrayWriter;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * A class for tokenizing a date and/or time string in to the
 * tokens - number, separator, space, month, time
 * Usage: Create a new dateTimeTokenizer with a date (and/or time) string.
 *        If wishing to ensure that this will convert correctly, call validateStructure()
 *        which will return true if the string is a correctly convertible dat/time.
 *        Call getDate() to get the date object equivalent to the date/time string.
 */

public class dateTimeTokenizer{ 

  String dateTimeStr;
  Object [] tokens;
  int numberCount = 0;
  int monthCount = 0;

  public static byte NEUTRAL=0, US = 1, UK = 2;
  private byte locale;
  private Locale realLocale;
  
  public dateTimeTokenizer(String _dateTimeString, byte _locale){
    locale = _locale;
		try {
        if ( _dateTimeString.startsWith( "{ts '" ) ){
          dateTimeStr = _dateTimeString.substring( _dateTimeString.indexOf( '\'' ) + 1, _dateTimeString.lastIndexOf( '\'' ) );
          locale = NEUTRAL;
        }else if ( _dateTimeString.startsWith( "{d '" ) ){
          dateTimeStr = _dateTimeString.substring( _dateTimeString.indexOf( '\'' ) + 1, _dateTimeString.lastIndexOf( '\'' ) );
          locale = NEUTRAL;      
        }else{
          dateTimeStr = _dateTimeString;
        }
        dateTimeStr = dateTimeStr.trim().replace(',','/').replace('-','/').replace('\t',' ').replace('\n',' ').replace('\r',' ').toLowerCase();
        tokens = tokenize();
    } catch ( RuntimeException e ) {
        // String.substring() can throw StringIndexOutOfBoundsException
    }
  }// DateTimeTokenizer()


  public dateTimeTokenizer(String _dateTimeString){
    this( _dateTimeString, US );
  }// dateTimeTokenizer()

	
  public Object[] getTokens(){
    return tokens;
  }// getTokens()

  public void setLocale(Locale l){
    realLocale = l;
  }

  public static Date getDate(String _date, Locale l){
    dateTimeTokenizer dtt;
    if(l.getCountry().equals("US"))
      dtt = new dateTimeTokenizer(_date, US);
    else
      dtt = new dateTimeTokenizer(_date, UK);
      
    if( dtt.tokens == null )
      return null;
      
    if ( !dtt.validateStructure() ) {
      dtt = new dateTimeTokenizer(_date, NEUTRAL);
      if ( !dtt.validateStructure() )
        return null;
    }
        
    dtt.setLocale(l);
    return dtt.getDate();
  }


	
	public static java.util.Date getUSDate( String _date ){
		return getDate( _date, US );
	}// getUSDate()
	
	// use for e.g 2001-2-1
	public static java.util.Date getNeutralDate( String _date ){
		return getDate( _date, NEUTRAL );
	}
	
	public static java.util.Date getUKDate( String _date ){
		return getDate( _date, UK );
	}
	
	private static java.util.Date getDate( String _date, byte _locale ){
		dateTimeTokenizer dtt = new dateTimeTokenizer( _date, _locale );
		if ( dtt.tokens != null && dtt.validateStructure() ){
			return dtt.getDate();
		}else{
			return null;
		}
	}
	
  // returns null if failed to tokenize
  private Object[] tokenize() {
    List<token> tokenList = new ArrayList<token>();

    char [] strBytes = dateTimeStr.toCharArray();
    charArrayParser dateTimeChars = new charArrayParser(strBytes);//dateTimeStr.getBytes());
    
    while (!dateTimeChars.endOfCharArray()){
      // note chars will be in lower case
      char nextChar = dateTimeChars.peakChar();
      
      if ((nextChar >= 'a' && nextChar <= 'z')){
        tokenList.add(dateTimeChars.getLabel());
      }else if (nextChar >= '0' && nextChar <= '9'){
        tokenList.add(dateTimeChars.getNumeric());
      }else if (nextChar == ' '){
        while (dateTimeChars.peakChar() == ' '){
          dateTimeChars.getChar();
        }
        tokenList.add(new token(token.SPACE));
      
      }else if (nextChar == '/' || nextChar == '.' ){
        tokenList.add(new token(token.SEPARATOR));
        dateTimeChars.getChar();
      
      }else{
        return null;
      }
    }
    return tokenList.toArray();
  }// tokenize()


  /** 
   * returns false if the tokenised date/time doesn't fit the rules 
   * given by this method. If this returns false, it is unsafe to use
   * getDate().
   */

  public boolean validateStructure(){

    if ( ( tokens == null ) || ( tokens.length == 0 ) )
		return false;

    int lastTokenIndex = tokens.length - 1;

    if (((token)tokens[0]).getType() == token.SEPARATOR
      || ((token)tokens[lastTokenIndex]).getType() == token.SEPARATOR){
        return false;
    }

    int timeCount = 0;

    for (int i = 0; i <= lastTokenIndex; i++){
      token currToken = (token)tokens[i];
      token nextToken;

      switch (currToken.getType()){
      
      case token.SEPARATOR:
        // invariant :- last token is not a separator nor space so safe to look at next token
        nextToken = (token)tokens[(i+1)];
        if (nextToken.getType() == token.SEPARATOR
          || ( nextToken.getType() == token.SPACE && ( ((token)tokens[(i+2)]).getType() == token.SEPARATOR) ) ){
          
          return false;
        }
        break;

      case token.TIME:
        timeCount++;
        if (i == 0 && (i+1) <= lastTokenIndex && ((token)tokens[1]).getType() == token.SEPARATOR){
          return false;
        
        }else if (i == (lastTokenIndex) && (i-1) > 0 && 
          ((token)tokens[i-1]).getType() == token.SEPARATOR) {
          return false;
        }
        break;

      case token.MONTH:
        // if month is immediately followed by a number
        if (i < lastTokenIndex){
          nextToken = (token)tokens[(i+1)];
          if (nextToken.getType() == token.NUMBER){
            return false;
          }
        }
        monthCount++;
        break;

      case token.NUMBER:
        // if number is immediately followed by a month
        if (i < lastTokenIndex){
          nextToken = (token)tokens[(i+1)];
          if (nextToken.getType() == token.MONTH){
            return false;
          }
        }
        numberCount++;
        break;
        
      case token.DAY:
    	// day must be at the beginning of the string;
    	// if only the day is specified, then it's not a valid date (bug #2962)
      	if ( ( i != 0 ) || ( tokens.length == 1 ) ) {
      		return false;
      	}
      	break;
      	
	  default:
		break;
	  }
    }

    // check the counts AND check the TIME is at the start or end of the string
    // you can't have more than one time, more than one month, must have 2 numbers if a month is specified AND 0 or 3 numbers if a month isn't specified
    // // if a time is specified then it must be at the start or end of the string
    if (timeCount > 1
        || monthCount > 1
        || (monthCount == 1 && ( numberCount == 0 || numberCount > 2 )) // valid number counts 1, 2
        || (monthCount == 0 && (numberCount == 1 || numberCount > 3 ) ) // valid number counts 0, 2, 3
        || (timeCount == 1 && ((token)tokens[0]).getType() != token.TIME && ((token)tokens[tokens.length - 1]).getType() != token.TIME))
    {
      return false;    
    }

    return true;

  }// validateStructure()


  public java.util.Date getDate() {
    java.util.GregorianCalendar cal = (GregorianCalendar)java.util.GregorianCalendar.getInstance();
    cal.setLenient(false);
    // initialise time of calendar
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
    cal.set(java.util.Calendar.MINUTE, 0);
    cal.set(java.util.Calendar.SECOND, 0);
    cal.set(java.util.Calendar.MILLISECOND, 0);      

    
    int timeIndex = -1;
    int lastTokenIndex = tokens.length - 1;
    // invariant: there is only one time which should be the first or last
    if (((token)tokens[0]).getType() == token.TIME){
      timeIndex = 0;
    
    }else if ( (((token)tokens[lastTokenIndex]).getType() == token.TIME)){
      timeIndex = tokens.length - 1;
    
    }

    if (timeIndex != -1){
      // convert time to 
      if (!processTime(cal, ((token)tokens[timeIndex]).getValue())){
        return null;
      }
    }

    // if there is a date 
    if (numberCount > 0){// i.e. numberCount == 2 or 3
      // convert the date 
      if (monthCount == 1){ // numberCount == 2
        // if there's a month then any of the other 2 are year and day in any order
        char[] month = null;
        int part1 = -1;
        int part2 = -1;
        
        for (int i = 0; i < tokens.length; i++){ 
          if (((token)tokens[i]).getType() == token.MONTH){
            month = ((token)tokens[i]).getValue();

          }else if (((token)tokens[i]).getType() == token.NUMBER){
            if (part1 == -1){
              part1 = intValue(((token)tokens[i]).getValue());
              
            }else{
              part2 = intValue(((token)tokens[i]).getValue());
            }
          }
        }
        
        if ( numberCount == 1 ){
        	if ( !processDate1Digit( cal, month, part1 ) ){
        		return null;
        	}
        }else if (!processDate(cal, month, part1, part2)){
          return null;
        }
          
      }else{ // monthCount == 0, numberCount == 3
        int part1 = -1;
        int part2 = -1;
        int part3 = -1;

        for (int i = 0; i < tokens.length; i++){ 
          if (((token)tokens[i]).getType() == token.NUMBER){
            if (part1 == -1){
              part1 = intValue(((token)tokens[i]).getValue());
              
            }else if (part2  == -1){
              part2 = intValue(((token)tokens[i]).getValue());
            
            }else{
              part3 = intValue(((token)tokens[i]).getValue());
            }
          }
        }
        
        if ( numberCount == 2 ){
        	if ( !processDate2Digit( cal, part1, part2 ) ){
        		return null;
        	}
        }else if (!processDate(cal, part1,part2,part3)){
          return null;
        }
      }
    
    }

    try
    {
    	return cal.getTime();
    }
    catch ( IllegalArgumentException exc )
    {
    	// With some VMs if the hour is 2am on the day that daylight savings begins then
    	// an IllegalArgumentException will be thrown with a message of "HOUR_OF_DAY".
    	// In this case set lenient to true and call getTime() again so it will work.
    	// NOTE: In this case it returns the hour as 3am instead of 2am. In VMs that behave
    	//       properly the hour is returned as 2am.
    	// NOTE: We know this problem exists with the following VMs: 1.5.0_07, 1.6.0 and 1.6.0_01.
    	//       But doesn't exist with these VMs: 1.5.0_01 and 1.6.0_12.
    	// NOTE: This is the fix for bug NA#3187.
    	cal.setLenient(true);
    	if ( exc.getMessage().equals("HOUR_OF_DAY") && (cal.get(java.util.Calendar.HOUR_OF_DAY)==3) )
     	    return cal.getTime();
    	else
    		throw exc;
    }
  }// getDate()

  

  // attempts to create date using the parameters trying parts as day, year then year, day
  // returns false if failed to process it
  private boolean processDate(java.util.GregorianCalendar _calendar, char [] _month, int _part1, int _part2) {
    
    int month;
    if(realLocale == null)
      month = monthConverter.convertMonthToInt(_month);// returns month byte[] as an int index from 0;
    else
      month = monthConverter.convertMonthToInt(_month, new DateFormatSymbols(realLocale));

    if ( month == -1 ) return false;
    
    // note: calendar.set() takes params year, month, day
    // whilst prelimDateCheck takes them in the order day, month, year

    // basically, this works out which comes first, the day or the year but checks
    // them in the order of priority, hence the number of if statements
    if (prelimDateCheck(_calendar, month + 1, _part1, _part2)){ // month/day/year
    	return setCalendar(_calendar, _part2, month, _part1); 
    }

    if (prelimDateCheck(_calendar, _part1, month + 1, _part2)){ // day/month/year
    	return setCalendar(_calendar, _part2, month, _part1);
    }

    if (prelimDateCheck(_calendar, _part2, month + 1, _part1)){ // year/month/day
    	return setCalendar(_calendar, _part1, month, _part2);
    }

    if (prelimDateCheck(_calendar, month + 1, _part1, _part2)){ // month/year/day
    	return setCalendar(_calendar, _part1, month, _part2);
    }

    if (prelimDateCheck(_calendar, _part1, _part2, month + 1)){ // day/year/month
    	return setCalendar(_calendar, _part2, month, _part1);
    }

    if (prelimDateCheck(_calendar, _part2, _part1, month + 1)){ // year/day/month
    	return setCalendar(_calendar, _part1, month, _part2);
      
    }

    return false;
  }// processTime()

  private boolean setCalendar(java.util.GregorianCalendar _calendar, int _year, int _month, int _day){
    _calendar.set( convertYear(_year), _month, _day );
    return true;
  }

  // returns false if failed to process it
  private boolean processDate(java.util.GregorianCalendar _calendar, int _part1, int _part2, int _part3) {
    // note: month index has to be converted

    if (locale == UK){
      if (prelimDateCheck(_calendar, _part1, _part2, _part3)){
				return setCalendar(_calendar, _part3, _part2 - 1, _part1); // day/month/year
         
      }
    }else if (locale == US){
      if (prelimDateCheck(_calendar, _part2, _part1, _part3)){
				return setCalendar(_calendar, _part3, _part1 - 1, _part2); // month/day/year
      }
    }else if (locale == NEUTRAL){
      if (prelimDateCheck(_calendar, _part3, _part2, _part1)){
				return setCalendar(_calendar, _part1, _part2 - 1, _part3); // year/month/day
      }
    }

    return false;

  }// processDate()

  // attempts to create date using the parameters trying parts as day, year then year, day
  // returns false if failed to process it
  private boolean processDate2Digit(java.util.GregorianCalendar _calendar, int _part1, int _part2) {
  	int defaultYear = _calendar.get( Calendar.YEAR );
  	if ( prelimDateCheck( _calendar, _part2, _part1, defaultYear ) ){ // month/day
  		return setCalendar(_calendar,  defaultYear, _part1 - 1, _part2); 
  	}else if ( prelimDateCheck( _calendar, _part1, _part2, defaultYear ) ){ // day/month
			return setCalendar(_calendar, defaultYear, _part2 - 1, _part1); 
  	}else if ( prelimDateCheck(_calendar, 1, _part1, _part2 ) ){ // month/year
  		return setCalendar(_calendar, _part2, _part1 - 1, 1 ); 
  	}else if ( prelimDateCheck(_calendar, 1, _part2, _part1 ) ){ // year/month
  		return setCalendar(_calendar,  _part1, _part2 - 1, 1 ); 
  	}
  	
  	return false;
  }
  
  private boolean processDate1Digit(java.util.GregorianCalendar _calendar, char[] _month, int _part1 ) {
  	
  	// convert the month characters to a numeric
    int month;
    if(realLocale == null)
      month = monthConverter.convertMonthToInt(_month);// returns month byte[] as an int index from 0;
    else
      month = monthConverter.convertMonthToInt(_month, new DateFormatSymbols(realLocale));

    if ( month == -1 ) return false;
    
    int defaultYear = _calendar.get( Calendar.YEAR );
    
    // now we need to figure out whether the numerical part represents the year or the day
    if (prelimDateCheck(_calendar, month + 1, _part1, defaultYear)){ // month/day
    	return setCalendar(_calendar, defaultYear, month, _part1); 
    }else if (prelimDateCheck(_calendar, 1, month + 1, _part1)){ // month/year
    	return setCalendar(_calendar, _part1, month, 1);
    }
  	return false;
  }

  private boolean processTime(java.util.GregorianCalendar _calendar, char [] _time) {
    int index = 0;
    int hours = 0;
    int mins = 0;
    int secs = 0;
    int ms = 0;
    
    CharArrayWriter number = new CharArrayWriter();

    // get hours
    while (_time[index] >= '0' && _time[index] <= '9'){
      number.write(_time[index]);
      index++;
    }
    hours = intValue(number.toCharArray());
    
    if (_time[index] == 'a' || _time[index] == 'p'){

      hours = convertHours(hours, _time[index]);
      if (hours >= 0 && hours <= 23){
        _calendar.set(java.util.Calendar.HOUR_OF_DAY, hours);
        return true;
      }else{
        return false;
      }
    }

    if ( _time[index] == '.' )
      return false;
    
    // get minutes
    number.reset();
    index++; // ignore the :
    while ((index < _time.length) && _time[index] >= '0' && _time[index] <= '9'){
      number.write(_time[index]);
      index++;
    }
    
    mins = intValue(number.toCharArray());

    if ( (index < _time.length) && _time[index] == '.' )
      return false;
    
    if (index == _time.length){
      if (prelimTimeCheck(hours, mins, 0)){
        _calendar.set(java.util.Calendar.HOUR_OF_DAY, hours);
        _calendar.set(java.util.Calendar.MINUTE, mins);
        return true;
      }else{
        return false;
      }
    }

    if (_time[index] == 'a' || _time[index] == 'p'){
      hours = convertHours(hours, _time[index]);
      if (prelimTimeCheck(hours, mins, 0)){
        _calendar.set(java.util.Calendar.HOUR_OF_DAY, hours);
        _calendar.set(java.util.Calendar.MINUTE, mins);
        return true;
      }else{
        return false;
      }
    }
    

    index++; // ignore the ':'

    // get secs
    number.reset();
    while (index < _time.length && _time[index] >= '0' && _time[index] <= '9'){
      number.write(_time[index]);
      index++;
    }

    secs = intValue(number.toCharArray());

    //  milliseconds
    number.reset();
    if ( index < _time.length && _time[index] == '.' ){
      index++;
      while (index < _time.length && _time[index] >= '0' && _time[index] <= '9'){
        number.write(_time[index]);
        index++;
      }
      if ( number.size() > 9 ){
        return false;
      }else if ( number.size() > 3 ){
        // only include the first 3 numbers since we can't set the millisecond value
        // to any higher precision
        char [] numberChars = number.toCharArray();
        ms = intValue( new char[]{ numberChars[0], numberChars[1], numberChars[2] });
      }else{
        ms = intValue( number.toCharArray() );
        ms = number.size() == 3 ? ms : (number.size() == 2 ? ms * 10 : ms * 100 );
      }
    }
    
    if (index < _time.length && (_time[index] == 'a' || _time[index] == 'p')){
      hours = convertHours(hours, _time[index]);
    }

    if (!prelimTimeCheck(hours, mins, secs)){
      return false;
    }
    _calendar.set(java.util.Calendar.HOUR_OF_DAY, hours);
    _calendar.set(java.util.Calendar.MINUTE, mins);
    _calendar.set(java.util.Calendar.SECOND, secs);
    _calendar.set(java.util.Calendar.MILLISECOND, ms);
    return true;
  }// processTime()


  public static int intValue(char [] in){
    int power = in.length - 1;
    int value = 0; // the integer value to return

    for (int i = 0; i < in.length; i++){
      value += ((int) Math.pow(10, power)) * (in[i] - '0');
      power--;
    }

    return value;

  }// intValue()
  
  private static boolean prelimTimeCheck(int _hours, int _mins, int _secs){
    return (_hours >= 0 && _hours <= 23 && _mins >= 0 && _mins <=59 && _secs >= 0 && _secs <=59);
  }// prelimTimeCheck()


  private boolean prelimDateCheck(java.util.GregorianCalendar _cal, int _day, int _month, int _year){
    if (_day > 0 && _day < 32 && _month > 0 && _month < 13 && _year >= 0 && _year < 10000 ){

      _cal.set(convertYear(_year), _month - 1, _day); // note must convert month back to java form
      try{
        _cal.getTime();
      }catch(IllegalArgumentException e){
        return false;
      }
      return true;
    }else{
      return false;
    }
  }// prelimDateCheck


  private static int convertHours(int _hours, char _period){
    if (_period == 'a' && _hours == 12){
      return 0;
      
    }else if (_period == 'p' && _hours < 12){
      return _hours + 12;
    
    }else{
      return _hours;
    }
    
  }// convertHours()

  private static int convertYear(int _year){
    return( _year < 100 ? ( _year < 30 ? _year + 2000 : _year + 1900 ) : _year );
  }// convertYear()

 

}// DateTimeTokenizer
