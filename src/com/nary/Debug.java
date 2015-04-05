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

package com.nary;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.servlet.ServletContext;

import org.apache.commons.vfs.FileObject;

import com.naryx.tagfusion.cfm.engine.cfEngine;

/**
 * This is a simple Debugging class that can be used to generate a log file of all
 * errors.  Currently it logs to a file called debug.txt, from where ever the Java
 * application is run from.
 */

public final class Debug extends Object implements Serializable{
	private static final long serialVersionUID = 1L;
	private RandomAccessFile OutFile 				= null;
  private boolean bOn			  							= true;
  private boolean bSystemOut							= true;
  private boolean bFile      							= true;
  private String filename 								= "debug.txt";
  private static Debug newInstance 				= null;
  private ServletContext	servletContext 	= null;
  private long logFileSize								= 0;
  private long maxLogFileSize							= 25000000;		//- default to 25MB

  private Debug(){}

  /**
   * This method sets file name to a specifed file name.
   *
   * @param _file The specified file name
   */

  public static void setFilename( String _file ){
    checkCurrentInstance();
    newInstance.intSetFilename( _file );
  }

  private void intSetFilename( String _file ){
    filename  = _file;
    try{
      if ( OutFile != null )
        OutFile.close();
    }catch(Exception E){}
    OutFile = null;
  }

	/**
	 * Sets a flag to true to indecate a active status.
	 */

	public static void On(){
		checkCurrentInstance();
		newInstance.bOn=true;
	}
	
	/**
	 * Sets a flag to false to indecate a idel status.
	 */

	public static void Off(){
		checkCurrentInstance();
		newInstance.bOn=false;
	}

	/**
	 * Sets flag to true to show that the system is active.
	 */

	public static void SystemOn(){
		checkCurrentInstance();
		newInstance.bSystemOut=true;
	}

	/**
	 * Sets the flag to false to show the system is closed.
	 */

	public static void SystemOff(){
		checkCurrentInstance();
		newInstance.bSystemOut=false;
	}


	/**
   * Set the flag to true to show the file is open or exists.
   */
  public static void FileOn(){
    checkCurrentInstance();
    newInstance.bFile = true;
  }

  /**
   * Sets the flag to false to show that the file is closed or not exist.
   */
  public static void FileOff(){
    checkCurrentInstance();
    newInstance.bFile = false;
  }

	public static void setServletLogging( ServletContext _servletContext ){
		checkCurrentInstance();
		newInstance.servletContext = _servletContext;
	}

	public static void setRotationSize( long maxLogFileSize ){
		checkCurrentInstance();
		newInstance.maxLogFileSize	= maxLogFileSize;
	}
	
  /**
   * This method open the system for printing, after finish printing, set system to close status.
   *
   * @param t
   * @param Line
   */

  public static void println( boolean t, String Line ){
    checkCurrentInstance();
    newInstance.intPrintln( t, Line );
  }

  private void intPrintln( boolean t, String Line ){
    bSystemOut = true;
    intPrintln( Line );
    bSystemOut = false;
  }

  public static void println( boolean t, Exception E ){
    checkCurrentInstance();
    newInstance.intPrintln( t, E );
  }

  private void intPrintln( boolean t, Exception E ){
    bSystemOut = true;
    intPrintln( E.toString() );
    bSystemOut = false;
  }

  /**
   * print out error message.
   *
   * @param E
   */

  public static void println( Exception E ){
    checkCurrentInstance();
    newInstance.intPrintln( E.toString() );
  }


	/**
   * This method takes in a Throwable object, and print out stack trace for this object.
   *
   * @param E The specified object
   */

  public static void printStackTrace( Throwable t ){
    checkCurrentInstance();
    newInstance.intPrintStackTrace( t );
  }

	public static String getStackTraceAsString( Throwable t ){
    StringWriter sw = new StringWriter();
	  PrintWriter pw = new PrintWriter(sw);
	  t.printStackTrace(pw);
	  pw.close();
  	return sw.toString();
	}


	/**
	 * This method generates and prints a stack trace.
	 */

	public static void printStackTrace( ){
		try{
			throw new Exception();
		}catch(Exception e ){
			printStackTrace( e );
		}
	}


  private void intPrintStackTrace( Throwable E ){
    StringWriter sw = new StringWriter();
	  PrintWriter pw = new PrintWriter(sw);
	  E.printStackTrace(pw);
	  pw.close();
  	intPrintln( sw.toString() );
  }

  /**
   * Instead print debug message to the debug.txt file, this method allows to print out debug message
   * to a specified file.
   *
   * @ _logFile The file name that you want to print message to.
   * @ _Line The message that you want to print to the file.
   */

  public static void println( Object _object ){
    checkCurrentInstance();
    if ( _object != null )
		newInstance.intPrintln( _object.toString() );
  }

  /**
   * This method prints all the elements in a Hashtable printing each
   * key:value pair on a new line
   *
   * @param _hashtable The Hashtable to go through
   */

  public static void println( Hashtable _hashtable ){
    checkCurrentInstance();
    if ( _hashtable == null )
      return;

    Enumeration E = _hashtable.keys();

    while ( E.hasMoreElements() ){
      String key = (String)E.nextElement();
      Debug.println( "KEY=[" + key + "] DATA=[" + _hashtable.get( key ) + "]" );
    }
  }

  /**
   * This method prints all the elements in a Vector marking each with
   * the index it's at.
   *
   * @param _vector The Vector to go through
   */

  public static void println( Vector<Object> _vector ){
    checkCurrentInstance();
    if ( _vector == null )
      return;

    Enumeration<Object> E = _vector.elements();
    int x = 0;

    while ( E.hasMoreElements() ){
      newInstance.intPrintln( "element[" + (x++) + "]=" + E.nextElement() );
    }
  }

  /**
   * This method prints an array of ints, marking each array element with
   * the index it's at.
   *
   * @param _array The array of ints to print
   */

  public static void println( int [] _array ){
    checkCurrentInstance();
    if ( _array == null )
      return;

    for ( int x = 0; x < _array.length; x++ )
      newInstance.intPrintln("index[" + x + "]=" + _array[x] );
  }

  /**
   * This method prints an array of chars, marking each array element with
   * the index it's at.
   *
   * @param _array The array of chars to print
   */

  public static void println( char [] _array ){
    checkCurrentInstance();
    if ( _array == null )
      return;

    for ( int x = 0; x < _array.length; x++ )
      newInstance.intPrintln("index[" + x + "]:" + _array[x] );
  }

  /**
   * This method prints an array of bytes, marking each array element with
   * the index it's at.
   *
   * @param _array The array of bytes to print
   */

  public static void println( byte [] _array ){
    checkCurrentInstance();
    if ( _array == null )
      return;

    for ( int x = 0; x < _array.length; x++ )
      newInstance.intPrintln("index[" + x + "]=" + _array[x] );
  }

  /**
   * This method print debug record to consoel, speecifed file and to client.
   * All of the above methods eventually call this method
   *
   * @param Line The string needs to be print out.
   */

  public synchronized static void println( String Line ){
    checkCurrentInstance();
    newInstance.intPrintln( Line );
  }

  private void intPrintln( String Line ){
    if ( !bOn )
      return;

    String D = com.nary.util.Date.formatNow( "dd/MM/yy HH:mm.ss: " ) + Line;

		//--[ Write it out to the System Console
		if ( bSystemOut )
			System.out.println( D );

		//--[ Write it out to the Servlet Log
		if ( servletContext != null )
			servletContext.log( Line );

    D += "\r\n";

		//--[ Write it out to file
    if ( bFile ){
      try{
        
      	if ( OutFile == null ){
      	  //-- Log file is closed; lets open it up
      	  if ( filename == null ) {
      		return; // we're screwed--give up
      	  }
          OutFile 		= new RandomAccessFile( filename, "rw" );
          logFileSize	= OutFile.length();
          OutFile.seek( logFileSize );
          OutFile.writeBytes( "\r\n]--- Logging Started ------[\r\n" );
        }
        
      	//-- Write out the line to the logfile
        OutFile.writeBytes( D );
        logFileSize += D.length();
        
        if ( logFileSize > maxLogFileSize )
        	rotateLogFile();
        
      }catch(IOException E){
      	if ( OutFile != null ){ try{ OutFile.close(); } catch(IOException ignore){} }
      	OutFile	= null;
      }
    }
  }

  /**
   * This method will rotate the log file, renaming it to another 
   */
	private void rotateLogFile() {
		try{
			OutFile.writeBytes( "\r\n\r\n--- Log file rotated ---" );
			OutFile.close();
			
			//- rename the old file to a new one
			int x = 1;
			File newFile	= new File( filename + "." + x );
			while ( newFile.exists() ){
				newFile	= new File( filename + "." + (x++) );
			}
			
			//- Rename the old file
			new File( filename ).renameTo( newFile );
			
			//- Delete the old one
			new File( filename ).delete();
			
		}catch(IOException ignoreException){
			//- the rotation failed; so lets just reset the current file
		}
		OutFile = null;
	}

	
  public static void saveClass( String _filename, Object _class ){
    saveClass( _filename, _class, false );
  }

  public static void saveClass( String _filename, Object _class, boolean _compress ){
    BufferedOutputStream     FS = null;
    try{
      FS = new BufferedOutputStream( cfEngine.thisPlatform.getFileIO().getFileOutputStream( new File(_filename) ), 32000 );
      saveClass( FS, _class, _compress );
    }catch(Throwable E){
    }finally{
      try{ if ( FS != null ) FS.close(); }catch( IOException ignored ){}
    }
  }
  
  public static void saveClass( OutputStream _out, Object _class, boolean _compress ) throws IOException{
    if ( _compress ){
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream   OOS  = new ObjectOutputStream(bos);
      OOS.writeObject( _class );
    
      byte [] dataArray = bos.toByteArray();
      byte [] test = new byte[ dataArray.length ]; // this is where the byte array gets compressed to
      Deflater def = new Deflater( Deflater.BEST_COMPRESSION );
      def.setInput( dataArray );
      def.finish();
      def.deflate( test );
      _out.write( test, 0, def.getTotalOut() ); 
    }else{
      ObjectOutputStream   OS = new ObjectOutputStream(_out);
      OS.writeObject( _class );
    }
  }


  /**
   * This method read a specified class object from a specifed file
   * and return this object.
   *
   * @param _filename the spcifed file
   */

	public static Object loadClass(FileObject fileobject) {
		InputStream fis = null;
    try{
      fis = fileobject.getContent().getInputStream();
      return loadClass( fis, false );
    }catch(Exception E){
      return null;
    }finally{
      try{ if ( fis != null ) fis.close(); }catch( Exception ignored ){}
    }
	}

  
  public static Object loadClass( String _filename ){
  	return loadClass( _filename, false);
  }

  public static Object loadClass( String _filename, boolean _uncompress ){
    FileInputStream fis = null;
    try{
      fis = new FileInputStream( _filename );
      return loadClass( fis, _uncompress );
    }catch(Exception E){
      return null;
    }finally{
      try{ if ( fis != null ) fis.close(); }catch( Exception ignored ){}
    }
  }

  public static Object loadClass( InputStream _inStream ){
    return loadClass( _inStream, false );
  }


  public static Object loadClass( InputStream _inStream, boolean _uncompress ){
    ObjectInputStream ois;
    try{
      if ( _uncompress ){
        // we need to get the input as a byte [] so we can decompress (inflate) it.  
        Inflater inflater = new Inflater();
        ByteArrayOutputStream bos;
        int bytesAvail = _inStream.available();
        if ( bytesAvail > 0 ){
          bos = new ByteArrayOutputStream( bytesAvail );
        }else{
          bos = new ByteArrayOutputStream();
        }
        
        byte [] buffer = new byte[1024];
        int read = _inStream.read( buffer );
        while( read > 0 ){
          bos.write( buffer, 0, read );
          read = _inStream.read( buffer );
        }
        bos.flush();
        inflater.setInput( bos.toByteArray() );
        
        bos.reset();
        buffer = new byte[1024];
        int inflated = inflater.inflate( buffer );
        while( inflated > 0 ){
          bos.write( buffer, 0, inflated );
          inflated = inflater.inflate( buffer );
        }
        
        bos.flush();
        ByteArrayInputStream bis = new ByteArrayInputStream( bos.toByteArray() );
        
        ois = new ObjectInputStream( bis );
      
      }else{
        ois = new ObjectInputStream(_inStream);
      }
    
      return ois.readObject();
    }catch(Exception E){
      return null;
    }finally{
    	try{_inStream.close();}catch(Exception ioe){}
    }
  }


  private static void checkCurrentInstance(){
    if ( newInstance != null )
      return;

    newInstance = new Debug();
  }

}
