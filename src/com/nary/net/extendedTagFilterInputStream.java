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

package com.nary.net;

/**
 * extendedTagFilterInputStream
 *
 * extends tagFilterInputStream providing information
 * about the current position in the inputstream. 
 *
 */



import java.io.InputStream;

public class extendedTagFilterInputStream extends tagFilterInputStream{ 

  private int lineCount;
  private int colCount;

  public extendedTagFilterInputStream( InputStream _inputStream ){
    super( _inputStream );
    lineCount = 1;
    colCount = 1;
  }// extendedTagFilterInputStream 


  public int readChar() throws java.io.IOException{
    int ch = super.readChar();

    if ( ch == '\n' ){
      lineCount ++;
      colCount = 1;
    }else{
      colCount ++;
    }

    return ch;
  }// readChar()

  
  public int getLine(){
    return lineCount;
  }//getLine()


  public int getColumn(){
    return colCount;
  }//getColumn()


}// extendedTagFilterInputStream 
