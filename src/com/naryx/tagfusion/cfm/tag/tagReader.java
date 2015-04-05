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

package com.naryx.tagfusion.cfm.tag;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aw20.io.StreamUtil;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.file.cfmlURI;

 
public class tagReader extends java.io.Reader {

	public 	int	curLine, curColumn;
	private int lastChar;
	private List<cfTag>	tagStack;
	private List<cfCatchData>	caughtExceptions;
  char[]  data;

	int currentIndx = 0, peekIndx = 0;

	public tagReader( Reader _in ) throws IOException {
		curLine		= 1;
		curColumn	= 1;

    //--[ Read the stream into memory as we will be needing
    //--[ to do some look-ahead stuff later on with.		
		CharArrayWriter buffer = new CharArrayWriter();
		
		StreamUtil.copyTo(_in,buffer);
		  
		data  = buffer.toCharArray();
		
		tagStack			= new ArrayList<cfTag>();
		caughtExceptions	= new ArrayList<cfCatchData>();
	}
    
    public int getColumn() { return curColumn; }
    public int getLine() { return curLine; }

  //-------------------------------------------------------------------
  //--[ These methods are used to allow us to look-a-head in the 
	
	public void addException( cfCatchData	catchData ){
		caughtExceptions.add( catchData );
	}
	
	
	public int read() {
    if ( currentIndx >= data.length )
      return -1;

    lastChar = data[ currentIndx++ ];

   	if ( lastChar == (int)'\n' ){
   		curLine++;
   		curColumn = 1;
   	}else
   		curColumn++;
   		
   	return lastChar;
	}


  //-------------------------------------------------------------------
  //--[ These methods are used to allow us to look-a-head in the 
  //--[ stream.  mark() remembers where the input stream was.
  
  public void mark(){
    peekIndx  = currentIndx;
  }

	// resets the reader to the position when mark() was last called
  public void reset(){
    currentIndx = peekIndx;
  }

	public int peekRead() {
    if ( peekIndx == data.length )
      return -1;

    return data[ peekIndx++ ];
	}
	
	//-------------------------------------------------------------------
	//--[ These methods are for tracking when tags have not been closed
	public void pushStartTag( cfTag sTag ){	tagStack.add( sTag ); }
	public void popEndTag( cfTag sTag ){ tagStack.remove( sTag ); }

	public void close( cfmlURI _address ) throws cfmBadFileException {
		if ( tagStack.size() > 0 ){
			Iterator<cfTag> i	= tagStack.iterator();
			while ( i.hasNext() )
				addException( catchDataFactory.noEndTagException( i.next() ) );
		}
		
		if ( caughtExceptions.size() > 0 )
			throw new cfmBadFileException( new cfCatchData( _address, caughtExceptions ) );	
	}

	public int getPeekIndx() {
		return peekIndx;
	}


	public void setPeekIndx(int peekIndx) {
		this.peekIndx = peekIndx;
	}

  public void close() throws IOException {}

  public int read( char[] cbuf, int off, int len ) throws IOException {
    int nextCh = read();
    if ( nextCh == -1 ){
      return -1;
    }
    int count = 0;
    while( nextCh != -1 && count < len ){
      nextCh = read();
      cbuf[off+count] = (char) nextCh;
      count++;
    }
    return count;
  }
  
  public int nextIndexOf( String _s, int _offset ) {
  	if ( _offset >= data.length )
  		return -1;

    int searchIndx = -1;
    int searchStart = -1;
    char [] toMatch = _s.toUpperCase().toCharArray();
    int matchLength = _s.length();

    for ( int x = _offset; x < data.length; x++ ){     
      if ( searchIndx == -1 && (  Character.toUpperCase( data[x] ) == toMatch[0] ) ){
      	searchIndx  = 1;
        searchStart = x;
        if ( matchLength == 1 )
          return searchStart;
      }else if ( searchIndx != -1 && Character.toUpperCase( data[x] ) == toMatch[searchIndx] ){
        searchIndx++;
        if ( searchIndx == matchLength )
          return searchStart;
      }else
        searchIndx = -1;
    }

    return -1;
  }
  
}
