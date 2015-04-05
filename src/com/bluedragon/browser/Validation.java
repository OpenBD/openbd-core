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

package com.bluedragon.browser;

import java.text.SimpleDateFormat;

public class Validation extends Object{
  
  /*Validate Credit Card
  **params a string representing a credit card number XXXX-XXXX-XXXX-XXXX
  **the numbers may be separated by a space,"-","/"or":"
  **
  **returns true if the number conforms to the mod 10 algorithm
  */
  public static boolean validateCreditCard(String _CC){
    if(isString(_CC)){
      _CC = removeDelims(_CC);
    
      if(_CC.length()>17 || _CC.length()<13)
        return false;
      
      char [] car = _CC.toCharArray();  
    
      int sum   = 0;
      int count = 1;
    
      for(int i=car.length-1;i>=0;i--){
        if(!Character.isDigit(car[i]) )
          return false;
      
        if(count%2 == 0){
          int t = ((Character.getNumericValue(car[i]))*2);
          sum += t%10;
          if(t/10>=1)
            sum +=1;  
        }
        else
          sum+= Character.getNumericValue(car[i]);
        count++;
      }
      if(sum%10 == 0) 
        return true;
    }
    return false; 
  }
  
  /*checkInteger
  **params aString
  **returns true if the string can be converted to an integer, else false
  */
  public static boolean checkInteger(String _s){
    try{
      new Integer(_s);
      return true;
    }catch(NumberFormatException e){
      return false;
    }
  
  }
  /*checkFloat
  **params aString
  **returns true if the string can be converted to a float, else false
  */
  public static boolean checkFloat(String _s){
    try{
      new Float(_s);
      return true;
    }catch(NumberFormatException e){
      return false;
    }
  
  }
  /*checkTelNo
  **checks that a telephone number is supplied (in US format) correctly
  **params a string representation of the telephone number
  **returns true or false
  */
  public static boolean checkTelNo(String _s){
    if(isString(_s)){
      _s = removeDelims(_s);
      if(isNumber(_s)){
        char [] car = _s.toCharArray();
        if(Character.getNumericValue(car[0]) != 0 && Character.getNumericValue(car[3]) != 0 && car.length == 10)
          return true;
      }
    }
    return false;
  }
  
  
  public static boolean checkEUDate(String _s){
    SimpleDateFormat eudate = new SimpleDateFormat ("dd/MM/yy");
    eudate.setLenient(false);
    try{
      eudate.parse(_s);
      return true;
    }catch(Exception e){
      return false;
    }
  }
  
    
  public static boolean checkUSDate(String _s){
    SimpleDateFormat usdate = new SimpleDateFormat ("MM/dd/yy");
    usdate.setLenient(false);
    try{
      usdate.parse(_s);
      return true;
    }catch(Exception e){
      return false;
    }
  }
  
  public static boolean checkTime(String _s){
    SimpleDateFormat time = new SimpleDateFormat ("hh:mm:ss");
    time.setLenient(false);
    try{
      time.parse(_s);
      return true;
    }catch(Exception e){
      return false;
    }
  }
  
  //-- Altered 12/10/2000 to accomodate UK variations
  //-- US can be 5 or 9, UK can be 7 or 8
  public static boolean checkZipCode( String _s ){
    if(isString(_s)){
      _s = removeDelims(_s);
      //--if s length is 5 or 9 check US
      if(_s.length() == 5 || _s.length() == 9){
        if(isNumber(_s)){
          return true;
      	}else{
      		return false;
      	}
      }else{
      	//-- Check UK - gives 90% validation
      	if(_s.length() == 6 || _s.length() == 7){
      		if(	Character.isLetter( _s.charAt(0) ) 
      				&& Character.isLetter( _s.charAt(_s.length()-2) )
      		 		&& Character.isLetter( _s.charAt(_s.length()-1) )) 
      			if (isNumber(_s.substring(_s.length()-5,_s.length()-2)))
      				return true;
      	}
      }
    }  
    return false;
  }
  
  //-- Altered 12/10/2000 to accomodate UK variations
  public static boolean checkSocialSecNo(String _s){
    if(isString(_s)){
      _s = removeDelims(_s);
      if(_s.length() == 9){
      	// check US
      	if(isNumber(_s)) 
          return true; 
        else{
        	// Check if UK  
      		if(	Character.isLetter( _s.charAt(0) ) 
      				&& Character.isLetter( _s.charAt(1) )
      				&& Character.isLetter( _s.charAt(8) )){
      			if (isNumber(_s.substring(2,8)))
      				return true;
      			}
      	}
      }
    }
    return false;
  }
  
  //removes a specified delimiter  
  public static String removeDelim(String _delim, String _s){
    if (_s == null)
      return null;
    
    _s.trim();
    
    int c1 = _s.indexOf(_delim);

    if ( c1 == -1)
      return _s;

    while ( c1 != -1 ){
      _s = _s.substring(0, c1) + _s.substring(c1+1);
      c1 = _s.indexOf(_delim, c1 );
    }
    return _s;
  }
  
  //removes a common set of delimiters " ","/","-",":"
  public static String removeDelims(String _s){
    _s = removeDelim(" ",_s);
    _s = removeDelim("-",_s);
    _s = removeDelim("/",_s);
    _s = removeDelim(":",_s);
    return _s;
  }
  
  public static boolean isNumber(String _s){
    try{
      new Long(_s);
      return true;
    }catch(Exception e){
      return false;
    }
  }
  
  public static boolean isString(String _s){
    
    if(_s != null && _s.length() != 0)
      return true;
    
    return false;
  }
}
