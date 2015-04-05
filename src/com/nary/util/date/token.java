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
 * A class for representing a token of a date
 *  - number, separator, space, monthLabel, colon, am/pm
 *
 * Copyright:    Copyright (c) 2001
 * Company:      n-ary
 */

class token{
  static final byte SPACE=0, SEPARATOR=1, MONTH=2, TIME=3, NUMBER=4, DAY=5;
  byte type;
  char[] value;

  token(byte _type, char[] _value){
    type = _type;
    value = _value;
  }// Token()

  token(byte _type){
    this(_type, null);
  }// Token()


  public byte getType(){
    return type;
  }// getType()

  public char [] getValue(){
    return value;
  }// getValue()

  public String toString(){
    return type + "\t" + ((value != null)? new String(value) : "");
  }// toString()
    
}// token
