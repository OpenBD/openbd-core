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

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class monthConverter{

  public static int convertMonthToInt(char month[], DateFormatSymbols _dfs){
    String months[] = _dfs.getMonths();
    String shortmonths[] = _dfs.getShortMonths();
          
    for(int i = 0; i < months.length; i++)
      if(equal(month, months[i]) || equal(month, shortmonths[i]))
        return i;

    return -1;
  }

  /**
   * takes a month and returns the index of it - JAN => 1, FEB => 2...
   * assumes byte[] is in lower case
   * returns -1 if an invalid month.
   */
   
  public static int convertMonthToInt(char[] month){
    
    switch (month[0]){

      case 'j':
        if (equivalent(month, "january")){
          return Calendar.JANUARY;
        }
        else if (equivalent(month, "june")){
          return Calendar.JUNE;
        }
        else if (equivalent(month, "july")){
          return Calendar.JULY;
        }
        else
          return -1;
      
      case 'M':
      case 'm':
        if (equivalent(month, "may")){
          return Calendar.MAY;
        }
        else if (equivalent(month, "march")){
          return Calendar.MARCH;
        }
        else
          return -1;
      
      case 'A':
      case 'a':
        if (equivalent(month, "april")){
          return Calendar.APRIL;
        }
        else if (equivalent(month, "august")){
          return Calendar.AUGUST;
        }
        else
          return -1;
      
      case 'F':
      case 'f':
        if (equivalent(month, "february")){
          return Calendar.FEBRUARY;
        }
        else
          return -1;
          
      case 'D':
      case 'd':
        if (equivalent(month, "december")){
          return Calendar.DECEMBER;
        }
        else
          return -1;
          
      case 'S':
      case 's':
        if (equivalent(month, "september")){
          return Calendar.SEPTEMBER;
        }
        else
          return -1;
          
      case 'O':
      case 'o':
        if (equivalent(month, "october")){
          return Calendar.OCTOBER;
        }
        else
          return -1;
      
      case 'N':
      case 'n':
        if (equivalent(month, "november")){
          return Calendar.NOVEMBER;
        }
        else 
          return -1;
      
      default:
        return -1;
    }//switch
    
  }// convertMonthToInt
  
  
  
  /**
   * returns true if the testCase given is equivalent to 'equivTo'. Equivalent
   * in this case means that every byte in testCase is the same as the corresponding
   * byte in 'equivTo'. This is not case sensitive.
   * So if a byte [] containing "JAN" is equivalent to a byte [] containing "january".
   */
  
  private static  boolean equivalent (char [] testCase, String _equivTo){
    char [] equivTo = _equivTo.toCharArray();
    int testCaseLen;
    if (testCase[(testCase.length - 1)] == '.'){
      testCaseLen = testCase.length - 1;
    }
    else{
      testCaseLen = testCase.length;
    }

    // if testCase is longer than the equivTo array then can't be equivalent
    if (testCaseLen > equivTo.length || testCaseLen <= 2){
      return false; 
    }
    
    // loop thru' testCase bytes 
    // INVARIANT testCase.length <= equivTo.length
    for (int i = 0; i < testCaseLen; i++){
      if (testCase[i] != equivTo[i]){
	      return false;
      }
    }  
    
    return true;
  }// equivalent()
  
  private static boolean equal(char testCase[], String _equivTo){
    char equivTo[] = _equivTo.toCharArray();
    int testCaseLen = testCase.length;
    if(testCaseLen != _equivTo.length())
      return false;
    for(int i = 0; i < testCaseLen; i++)
      if(testCase[i] != equivTo[i])
        return false;

    return true;
  }


}// monthConverter
