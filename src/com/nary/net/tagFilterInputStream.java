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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;


/**
 * tagFilterInputStream
 *
 * this class filters an inputStream containing html.
 * This works by allowing a tagListener class to register with this class.
 * Upon finding a tag, it will call the tagListener passing in the tag and
 * replacing it with the value returned by the tagListener. 
 *
 */


public class tagFilterInputStream {

  private InputStream in;
  private tagListener listener;
  private byte[] buffer = null;
  private int bufferAt=0;
  private boolean inTag;
  
  private ByteArrayOutputStream byteOutputStream;

  public tagFilterInputStream( InputStream _inputStream ){
    in = _inputStream;
    byteOutputStream = new ByteArrayOutputStream();
    inTag = false;
  }// tagFilterInputStream()


  public void registerTagListener( tagListener _Listener ){
    listener = _Listener;
  }// registerTagListener()


  public int readChar() throws java.io.IOException{
    if ( buffer == null ){

      int nextChar = in.read();

      // if still inside a tag when doc has finished
      if ( inTag && nextChar == -1 ){
        throw new java.io.IOException( "ill formatted page: unclosed tag" );
     

      // if not currently looking at a tag and char is not the start of a tag
      }else if ( !inTag & ( nextChar != '<' ) ){
        return nextChar;
      
      // if at the start of a tag and not already in one
      }else if ( !inTag & ( nextChar == '<' ) ){
        byteOutputStream.write( nextChar );
        inTag = true;
        return readChar();
      
      // if in a tag and have reached the end
      }else if ( inTag && ( nextChar == '>' ) ){
        byteOutputStream.write(nextChar);
        inTag = false;

        // copy the read in tag to the buffer, ready to be checked
        buffer = byteOutputStream.toByteArray();

        // check the tag, getting the resolved tag back
        if ( listener != null ){
          buffer = listener.processTag( buffer );
        }

        bufferAt = 1;
        byteOutputStream.reset();

        return buffer[0];
      
      }else {//if (inTag && nextChar != '>'){
        byteOutputStream.write(nextChar);

        return readChar();
      }

    }else{ /* buffer not null */
      int bufferChar = buffer[bufferAt];
      bufferAt ++;
      if (bufferAt >= buffer.length)
        buffer = null;

      return bufferChar;
    }

  }//readChar()
	
	

}// tagFilterInputStream
