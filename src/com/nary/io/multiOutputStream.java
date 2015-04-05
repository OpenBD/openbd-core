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

package com.nary.io;


/**
 * This class wraps up a number of outputStreams so that one operation
 * on an outputStream can be duplicated to a set of outputStreams
 */

import java.io.OutputStream;

public class multiOutputStream extends OutputStream{
 
  OutputStream [] streams;

  public multiOutputStream ( OutputStream out1, OutputStream out2 ){
    streams = new OutputStream[2];
    streams[0] = out1;
    streams[1] = out2;
  }// multiOutputStream

 
  public multiOutputStream ( OutputStream outs[] ){
    streams = new OutputStream[outs.length];
    for (int i = 0; i < streams.length; i++){
      streams[i] = outs[i];
    }
  }// multiOutputStream


  public void write(byte[] b) throws java.io.IOException{
    for (int i = 0; i < streams.length; i++){
      streams[i].write(b);
    }
  
  }// write()


  public void write(byte[] b, int off, int len) throws java.io.IOException{
    for (int i = 0; i < streams.length; i++){
      streams[i].write(b, off, len);
    }
  
  }// write()

  
  public void write(int b) throws java.io.IOException{
    for (int i = 0; i < streams.length; i++){
      streams[i].write(b);
    }

  }// write()


  public void close() throws java.io.IOException{
    for (int i = 0; i < streams.length; i++){
      streams[i].close();
    }

  }// close()


  public void flush() throws java.io.IOException{
    for (int i = 0; i < streams.length; i++){
      streams[i].flush();
    }

  }// flush()


}// multiOutputStream

