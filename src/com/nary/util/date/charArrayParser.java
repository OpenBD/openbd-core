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

/**
 * This class represents the session between the client and the server for one
 * request.  This is not the session as in the CFML session scope, nor is it a 
 * session in the Servlet context.
 *
 */

import java.io.CharArrayWriter;
import java.util.HashSet;
import java.util.Set;

public class charArrayParser {

  char [] charArray;
  int index;
  int marker;
  CharArrayWriter outputStream;

  private static Set days;
  
  static{
  	days = new HashSet();
  	days.add( "mon" );
  	days.add( "tue" );
  	days.add( "wed" );
  	days.add( "thu" );
  	days.add( "fri" );
  	days.add( "sat" );
  	days.add( "sun" );
  	days.add( "monday" );
  	days.add( "tuesday" );
  	days.add( "wednesday" );
  	days.add( "thursday" );
  	days.add( "friday" );
  	days.add( "saturday" );
  	days.add( "sunday" );
  }
  
  public charArrayParser(char [] _charArray){
    charArray = _charArray;
    index = 0;
    marker = 0;
    outputStream = new CharArrayWriter();
  }// charArrayParser


  public token getLabel(){
    outputStream.reset();
     
    while (!endOfCharArray() && (isAlpha(peakChar()) || peakChar() == '.')){
      outputStream.write(getChar());
    }
    
    if ( days.contains( outputStream.toString().toLowerCase() ) ){
      return new token(token.DAY, outputStream.toCharArray());
    }
    return new token(token.MONTH, outputStream.toCharArray());
  }// getLabel()


  public token getNumeric(){
    outputStream.reset();

    // is it a time?
    int tempIndex = index;
    while(tempIndex < charArray.length && isNumeric(charArray[tempIndex])){
      tempIndex++;
    }

    if (tempIndex < charArray.length){
    
      // skip whitespace. Note invariant that there is no whitespace at end of the bytearray
      while(charArray[tempIndex] == ' '){
        tempIndex++;
      }
    
      if (charArray[tempIndex] == ':' && (tempIndex + 1) < charArray.length){
        tempIndex++;
        // skip whitespace. Note invariant that there is no whitespace at end of the bytearray
        while(charArray[tempIndex] == ' '){
          tempIndex++;
        }

        if (isNumeric( charArray[tempIndex] ) ){
          return getTime();
        }
      }else if ( ( charArray[tempIndex] == 'a' || charArray[tempIndex] == 'p' )
          && ( ( (tempIndex + 1) < charArray.length 
          && ( charArray[( tempIndex+1)] == 'm' || charArray[(tempIndex+1)] == ' '  
          || isNumeric( charArray[( tempIndex+1)] ) ) )
          || tempIndex + 1 == charArray.length ) ){
        return getTime();
      }
    }

    // not a time so just return the number token
    while (!endOfCharArray() && isNumeric(peakChar())){
      outputStream.write(getChar());
    }

    return new token(token.NUMBER, outputStream.toCharArray());

  }// getNumeric()


  private token getTime(){
    //getnumber  
    while (!endOfCharArray() && isNumeric(peakChar())){
      outputStream.write(getChar());
    }

    while (peakChar() == ' '){getChar();}    // skip whitespace
  
    // while next isn't a space 
    while (!endOfCharArray() && peakChar() != ' ' ){
      if (peakChar() == ':' || peakChar() == '.'){
        outputStream.write(getChar());
        
        while (peakChar() == ' '){getChar();} // skip whitespace

        while (!endOfCharArray() && isNumeric(peakChar())){
          outputStream.write(getChar());
        }
        if (endOfCharArray()){
          return new token(token.TIME, outputStream.toCharArray());
        }

        mark();
        while (peakChar() == ' '){getChar();}    // skip whitespace
        if (!(peakChar() == 'a' || peakChar() == 'p' || peakChar() == ':' || peakChar() == '.')){
          reset();
          return new token(token.TIME, outputStream.toCharArray());
        }

      }else if (peakChar() == 'a' || peakChar() == 'p' ){
        if (index+1 < charArray.length){
          if (charArray[index+1] == 'm'){
            outputStream.write(getChar()); // write the a/p
            getChar(); // get the m but don't need to store it
            return new token(token.TIME, outputStream.toCharArray());
          }else if ((charArray[index+1] == ' ') || (charArray[index+1] >= '0' && charArray[index+1] <= '9')){
            outputStream.write(getChar()); // write the a/p
            return new token(token.TIME, outputStream.toCharArray());     
          }else{
            // don't write the a/p, it will be caught later as a bad label
            return new token(token.TIME, outputStream.toCharArray());
          }
        }else{
          outputStream.write(getChar());
          return new token(token.TIME, outputStream.toCharArray());     
        }
      }else if (isNumeric(peakChar())){
        outputStream.write(getChar());
        while (isNumeric(peakChar())){
          outputStream.write(getChar());        
        }
        if (endOfCharArray()){
          return new token(token.TIME, outputStream.toCharArray());
        }
        // look ahead past whitespace. If next is a continuation of the time then carry on
        // else return the time now
        mark();
        while (peakChar() == ' '){getChar();}    // skip whitespace
        if (!(peakChar() == 'a' || peakChar() == 'p' || peakChar() == ':')){
          reset();
          return new token(token.TIME, outputStream.toCharArray());
        }
      }
    }
    return new token(token.TIME, outputStream.toCharArray());     

  }// getTime()

  
  private void mark(){
    marker = index;
  }// mark()

  private void reset(){
    index = marker;
  }// reset()


  public boolean isAlpha(char b){
    return ((b >= 'A' && b <='Z')
             || (b >= 'a' && b <= 'z'));
  }// isAlphaNumeric()


  public boolean isNumeric(char b){
    return (b >= '0' && b <= '9');
  }// isNumeric()


  public char getChar(){
    char ch = charArray[index];
    index++;
    return ch;
  }// getNextChar()


  public char peakChar(){
    return charArray[index];
  }// peakChar()

  public boolean endOfCharArray(){
    return (index == charArray.length);
  }

}
