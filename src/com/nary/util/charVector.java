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

package com.nary.util;

/**
 * A character vector class. Effectively a vector that holds char types. 
 *
 */

public class charVector{

	private char [] chars;
	private int actualSize;
	private int virtualSize;
	private int increment = 5; // size to increment by when array gets full
	
	public charVector( int _size ){
		actualSize = _size;
		virtualSize = 0;
		chars = new char[ actualSize ];
	}// charVector()
	
	
	public charVector(){
		this( 10 );
	}// charVector()
	
	
	public void add( char _ch ){
	 	if ( virtualSize == actualSize ){
			increaseSize();
		}
		chars[ virtualSize ] = _ch;
		virtualSize++;
	}// add()
	
	
	public void clear(){
		virtualSize = 0;
	}// clear
	
	
	/**
	 * returns the number of chars in the 
	 * character vector
	 */
	 
	public int size(){
		return virtualSize;
	}
	
	/**
	 * returns the last char in the vector
	 */
	 
	public char getLast(){
		if ( virtualSize > 0 ){
			return chars[ virtualSize - 1 ];
		}else{
			return (char) -1;
		}
	}// getLast()
	
	/**
	 * returns the last char in the array removing it
	 * Assumes that there is at least one char in the vector
	 */
	 
	public char removeLast(){
		virtualSize--;
		return chars[ virtualSize ];
	}

	
	private void increaseSize(){
		actualSize += increment;
		char [] temp = new char[ actualSize ];
		System.arraycopy( chars, 0, temp, 0, chars.length );
		chars = temp;
	}// increaseSize()

}// charVector
