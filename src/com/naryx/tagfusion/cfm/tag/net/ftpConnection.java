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

package com.naryx.tagfusion.cfm.tag.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfEngine;

/**
 * The following class is an implementation of an ftp client.
 * Creates a connection to a given ftp server and allows for the following
 * operations: ..........
 */

public class ftpConnection extends Object{
  private static final int WINDOWS_NT = 0;
  private static final int MVS = 1;
  private static final int VM = 2;
  private static final int OS400 = 3;
  private static final int NETWARE = 4;
  private static final int UNIX = 5;

  Socket socketConnection;
  BufferedReader controlInputStream;
  PrintStream controlOutputStream;

  InetAddress localHost;
  boolean passiveMode = true;
  boolean forceNLST = false;
  int serverOS;

  String hostname;
  int hostport;
  String username;
  String password;

  String type = "ASCII";

  boolean isOpen = false;

  private final static int chunkSize = 2048;

  response lastResponse;


  public ftpConnection( String _server, int _port, String _username, String _password ){
    hostname = _server;
    hostport = _port;
    username = _username;
    password = _password;
  }//ftpConnection()


  // [-- setup and close

  /**
   * attempts to open the ftp connection.
   * returns the response from the server.
   * returns null if the attempt to connect to the server failed.
   */

  /**
   * This method now accepts a timeout parameter which is part of the fix for NA #3301
   */
  public response open(int timeout){
    try{
      socketConnection = new Socket( hostname, hostport );
      socketConnection.setSoTimeout(timeout);
      localHost = socketConnection.getLocalAddress();
      controlInputStream = new BufferedReader( new InputStreamReader( socketConnection.getInputStream() ) );
      controlOutputStream = new PrintStream( socketConnection.getOutputStream() );
      checkResponse();
      response responseToLogin = login();
      if ( responseToLogin.getResponse() == 530 ){
        return responseToLogin;
      }
      isOpen = true;
      return responseToLogin;//checkResponse();
     }catch(IOException e){
      return null;
    }
  }//open()


  private response login() {
    executeCommand("USER " + username);
    response resp = executeCommand("PASS " + password );

    if (resp.getResponse() != 230){
   	  return resp; // bad password
    }
    setSystem();
    return resp;
  }// login()


  public int setTransferMode( String _transferMode ){
     response resp;
     if ( _transferMode.equalsIgnoreCase( "ascii" ) ){
       resp = executeCommand( "TYPE A");
     }else{
       resp = executeCommand("TYPE I");
     }
     if ( resp.getResponse() == 200 ){
       type = _transferMode.toUpperCase();
     }
     return resp.getResponse();
  }//setTransferMode()


  public String getServer(){
    return hostname;
  }//getServer()


  public int getServerPort(){
    return hostport;
  }//getServerPort()


  public String getUsername(){
    return username;
  }// getUsername()


  public String getPassword(){
    return password;
  }//getPassword()


  public void setPassiveMode( boolean _passive ){
    passiveMode = _passive;
  }

  public boolean isOpen(){
    return isOpen; //( controlInputStream == null || controlOutputStream == null );
  }//isConnected()


  public void disconnect(){
    try{
      isOpen = false;
      executeCommand( "QUIT" );
      controlOutputStream.close();
      controlInputStream.close();
      socketConnection.close();
    }catch(Exception ignored){}
  }//disconnect()


  // [-- commands
  public response changeDir( String _dir ){
    return executeCommand( "CWD " + _dir );
  }// changeDir()


  public response createDir( String _dir ){
    return executeCommand( "MKD " + _dir );
  }//createDir()


  public String getCurrentDir(){
    String resp = executeCommand( "PWD" ).getStringResponse();
    int firstQuote = resp.indexOf( "\"" );
    int lastQuote = resp.lastIndexOf( "\"" );
    if ( lastQuote != -1 && lastQuote != firstQuote ){
      return resp.substring( firstQuote + 1, lastQuote );
    }

    return null;
  }//getCurrentDir()



  public response getFile( String _fileName, File _localFile ) throws IOException{
    // in ASCII mode, we have to do the following:
    // in getFile, we have to use String System.getProperty("line.separator"). If the String returned by this method
    // contains a "\r\n" (13, 10), that is, we're writing to a DOS file system, we can store everything as is, as the server
    // side has already inserted the necessary \r chars (because we've set it to ASCII previously) according to the standard
    // NVT-ASCII format of ASCII transfers. However, if it only contains a "\n" (10) (e.g. we're writing to a local Unix
    // file system), we can't store it without removing the unnecessary \r's (13) first. We also should check for "\r" (Macs; 13) and
    // remove \n's before writing to the local file system. Fortunately, println() does the right conversion when writing to the
    // local file system so we don't have to do any conversion by hand, just to make sure we use println().

    // In ASCII mode putFile(), if the source system is \r\n, we don't have to do anything, either. If it's either \r or \n,
    // we have to insert the companion char to be NVT-ASCII compatible.

    Socket dataSocket = null;
    DataInputStream dInputstream = null;
    PrintStream ps = null;

    try{
      if ( passiveMode == false ){
        dataSocket = createPassiveSocket( "RETR " + _fileName );
      }else{ // passiveMode == true
        dataSocket = createListenerPortOnTheServer( "RETR " + _fileName );
      }

      if ( lastResponse.getResponse()/100 >= 4 ){ // check last response was good
        return lastResponse;
      }
      if ( dataSocket == null ) return lastResponse;  // both createPassiveSocket and createListenerPortOnTheServer returns 550

      dInputstream = new DataInputStream( new BufferedInputStream( dataSocket.getInputStream() ) );
      ps = new PrintStream( new BufferedOutputStream( new FileOutputStream( _localFile ) ) );

      // ascii?
      if ( type.equalsIgnoreCase( "ASCII" ) ) {
        // ascii
        String sep = System.getProperty( "line.separator" );
        int i;
        while ( ( i = dInputstream.read() ) != -1 ) {
          if ( i == '\r' ){ // now for the actual printout
            if ( ( i = dInputstream.read() ) == '\n' ) // next char - a \r\n seq?
            	// if yes, we print out the local line.separator
              ps.print( sep );
            else{// a stale \r with some other char - we print both
	            ps.print( '\r' );
              ps.print( (char) i );
            }
          }else{
            ps.print( (char) i );
          }

        } // while

      }else{ // binary
        byte datachunk[] = new byte[ chunkSize ]; // We use a buffer with alternating size
        int datalength = 0;

        while ( datalength != -1 ){
          datalength = dInputstream.read( datachunk );
          if ( datalength == -1 ){
            break;
          }
          ps.write( datachunk, 0, datalength );
        } // while (datalength != -1)
      }

      ps.flush();
      return checkResponse();

    }finally {
      if ( ps != null ) ps.close();   // we only close the output stream if it goes to a file because it's possible that getFile() was called by mget().
      if ( dInputstream != null ) dInputstream.close();
      if ( dataSocket != null ) dataSocket.close();

    }
  } // getFile()


  public List<fileInfo> listDir( String _dir ) throws IOException{
    // listing
		if ( _dir.equals( "." ) ){
			return fileList( null );
		}

		return fileList( _dir );
  }//listDir()


  public response putFile( File _localFile, String _remoteFileName ) throws IOException{

    Socket dataSocket = null;

    // now for the main transfer
    DataInputStream dInputstream = null;
    PrintStream ps = null;

    try{
      if (passiveMode == false){
        dataSocket = createPassiveSocket( "STOR " + _remoteFileName );
      }else{ // passiveMode == true
        dataSocket = createListenerPortOnTheServer( "STOR " + _remoteFileName );
      }

      if ( lastResponse.getResponse()/100 >= 4 ){ // check last response was good
        return lastResponse;
      }
      dInputstream = new DataInputStream ( new BufferedInputStream( new FileInputStream( _localFile ) ) );
      ps = new PrintStream ( dataSocket.getOutputStream() );

      if ( type.equalsIgnoreCase( "ASCII" ) ){
        // ascii
        // we can't use readLine() to read from the input b/c of the last line.separator. Therefore, we read it byte-by-byte
        int i;
        String sep = System.getProperty( "line.separator" );
        StringBuilder asciiBuf = new StringBuilder( chunkSize );

        while ( ( i = dInputstream.read() ) != -1 ){

          switch (i){
          case '\r' :
            // if the local line.separator is a simple \r, we explicitly
            // print out \r\n (13 10) (simple println() doesn't work, ofcourse!)
            if ( sep.equals( "\r" ) ){
              asciiBuf.append( "\r\n" );
            }else if ( sep.equals( "\n" ) ){ // we just print out \r as is
              asciiBuf.append( '\r' );
            }else if ( sep.equals( "\r\n" ) ){ // we have to handle stale \r's so we read the next char from the input
              if ( ( i = dInputstream.read() ) == '\n' ){ // next char - a \r\n seq?
                asciiBuf.append( "\r\n" );
              }else{ // print out both chars
                asciiBuf.append( '\r' );
                asciiBuf.append( (char) i );
              }
            }
            break; // next while iteration

          // we only get here if the local line.separator
          // is \n (that is, there're no \r's in the input).
          // Now, we can be sure that the source contained
          // a newline so we send it out too.
          case '\n' :
            asciiBuf.append( "\r\n" );
            break;

          default :
            if ( i > 127 ){
            	cfEngine.log( "WARNING: non-ascii character value '" + i + "' in " + _localFile.getName() );
            }
            asciiBuf.append( (char) i );
            break;
          } // switch (i)

          if ( asciiBuf.length() >= chunkSize ){
            ps.print( asciiBuf.toString() );
            asciiBuf.setLength( 0 ); //zero out our StringBuffer
          }
        } // while

        // print out the remaining data that we buffered
        if ( asciiBuf.length() > 0 ){
          ps.print( asciiBuf );
        }

      }else{ // binary
        // We use a buffer with alternating size
        byte datachunk[] = new byte[ chunkSize ];
        int datalength = 0;

        while ( ( datalength = dInputstream.read( datachunk ) ) != -1 ){
          ps.write( datachunk, 0, datalength );
        } // while (datalength != -1)
      }

      ps.flush();
      ps.close(); // it's very important that we close ps here (unlike in getFile(), where we could put all the closing stuff in the finally block). The explanation is very simple: the server only sees the end of the transfer when we close the stream that goes towards the server. If we don't close it, the server will wait forever and won't send anything back in the control channel.
      dInputstream.close();
      dataSocket.close();
    }finally{
      if ( ps != null ) ps.close(); // it's very important that we close ps here (unlike in getFile(), where we could put all the closing stuff in the finally block). The explanation is very simple: the server only sees the end of the transfer when we close the stream that goes towards the server. If we don't close it, the server will wait forever and won't send anything back in the control channel.
      if ( dInputstream != null ) dInputstream.close();
      if ( dataSocket != null ) dataSocket.close();
    }

    return checkResponse();
  }// putFile()


  public response remove( String _file ){
    return executeCommand( "DELE " + _file );
  }//remove()


  public response removeDir( String _dir ){
    return executeCommand( "RMD " + _dir );
  }//removeDir()

  /**
   * Renames the file specified by the first argument to the name given by second argument.
   *
   * @param oldName name of the file to be renamed.
   * @param newName new name of the file to be renamed.
   */

  public response rename( String _oldFilename, String _newFilename ){
  	response resp = executeCommand("RNFR " + _oldFilename);
    return ( resp.getResponse() == 550 ? resp : ( executeCommand("RNTO " + _newFilename) ) );
  }//rename()


  // required for getCurrentDir where want to return the current directory but may also want the response
  public response getLastResponse(){
    return lastResponse;
  }//getLastResponse()

  //[-- private methods


  private response executeCommand( String _command ){
    try{
      controlOutputStream.print( _command + "\r\n" );
      return checkResponse();
    }catch (NullPointerException e){
      response resp = new response();
      resp.setResponse( 999 );
      resp.setStringResponse( "Error. Failed to execute command. Connection is closed." );
      return resp;
    }
  }// executeMethod()


  private response checkResponse(){
    try{
      String reply = controlInputStream.readLine();
      if ( reply == null ){
        lastResponse = new response();
        lastResponse.setResponse( 999 );
        lastResponse.setStringResponse( "Error. Connection was closed." );
        return lastResponse;
      }

      String line = reply;
      boolean firstLine = true;

      while ( !( line != null && line.length() >= 4
        && Character.isDigit( line.charAt(0) )
        && Character.isDigit( line.charAt(1) )
        && Character.isDigit( line.charAt(2) )
        && (line.charAt(3) == ' ' )  ) ){

        if ( firstLine ){
          reply += "\n";
          firstLine = false;
        }
        line = controlInputStream.readLine();
        reply += line + "\n";
      }
      lastResponse = new response();
      lastResponse.setResponse( Integer.parseInt( reply.substring( 0,3 ) ) );
      lastResponse.setStringResponse( reply.substring(4) );
      return lastResponse;
    }catch(Exception e){
			lastResponse = new response();
      lastResponse.setResponse( 999 );
      lastResponse.setStringResponse( "Error. Connection was closed." );
      return lastResponse;
    }

  }//checkResponse()


  private Vector<fileInfo> fileList( String _filesToList ) throws IOException{
    Vector<fileInfo> filelist = null;
    Socket dataSocket = null;
    Vector<String> fullReplies;
    String filesToList;
    BufferedReader bInputstream = null;

    String oldType = null;
    if (!type.equalsIgnoreCase("ASCII")) {
  	  oldType = type;
 	    setTransferMode("ASCII");
    }


    try{
      filesToList = ( _filesToList == null ? "" : ( _filesToList ) );
      if ( !passiveMode ){
        dataSocket = createPassiveSocket( "LIST " + filesToList );
      }else{ // passiveMode == true
        dataSocket = createListenerPortOnTheServer( "LIST " + filesToList );
      }

      if ( lastResponse.getResponse()/100 >= 4 ){ // check last response was good
        return new Vector<fileInfo>();
      }

      // read in all the lines from the data socket
      bInputstream = new BufferedReader( new InputStreamReader( dataSocket.getInputStream() ) );
      fullReplies = new Vector<String> ();
      String line;
      while ((line= bInputstream.readLine()) != null){
        fullReplies.addElement(line);
      }

    }finally{
      if ( bInputstream != null ) bInputstream.close();
      if ( dataSocket != null ) dataSocket.close();
    }

    checkResponse(); // we force message 226 to disappear from the command connection's queue

    if (fullReplies.size() > 0){ // if no replies from list no point doing the rest of this section

    	/* check if it was a file */
    	/*
    	    The following code doesn't appear to be necessary and it breaks the CFFTP action
    	    existsFile with NetWare FTP servers so we'll comment it out for now. If it turns
    	    out that this code is necessary then we should only run it if serverOS != NETWARE.

			if ( fullReplies.size() == 1 ) {
				Vector tempFileList = setFileListDescription( fullReplies, filesToList );
				if (tempFileList.size() == 1 )
				{
					fileInfo f = (fileInfo) tempFileList.elementAt(0);
					if ( f.getName().trim().equals( filesToList.trim() ) ){
						lastResponse.setResponse( 999 );
						lastResponse.setStringResponse( "Error. Connection was closed.!!!!" );
						return new Vector();
					}
				}
			}
		*/

			if ( _filesToList == null ){
				filesToList = ".";
			}
      filelist=setFileListDescription( fullReplies, filesToList );

			// we filter out the .. / . / empty names - see PTR 6524
      int i = 0;
			while ( i < filelist.size() ){
        if ( filelist.elementAt(i).getName().equals( ".." )
        	|| filelist.elementAt(i).getName().equals( "." )
       		|| filelist.elementAt(i).getName().equals( "" ) ){
          filelist.removeElementAt(i);
        }else{
					i++;
				}
      }
    }else{
			filelist = new Vector<fileInfo>();
		}

    if ( oldType != null ) {
      setTransferMode( oldType ); // it was binary, so we change the type back
    }
    return filelist;

  }//fileList()


  private java.util.Vector<fileInfo> setFileListDescription( Vector<String> full, String _path /*Vector namesOnly*/ ) {
    Vector<fileInfo> fileList = new Vector<fileInfo>();
    fileInfo fInfo = null;
    String reply;
    boolean isFileOrDirEntry;

    for ( int i = 0; i < full.size(); i++ ){

      reply = full.elementAt(i);
      fInfo = new fileInfo();
      switch ( serverOS ){
      case WINDOWS_NT: isFileOrDirEntry = fInfo.setRemoteDescriptionWinNT ( reply ); break;
      case VM: isFileOrDirEntry = fInfo.setRemoteDescriptionVM ( reply ); break;
      case MVS: isFileOrDirEntry = fInfo.setRemoteDescriptionMVS ( reply ); break;
      case OS400: isFileOrDirEntry = fInfo.setRemoteDescriptionOS400 ( reply ); break;
      case NETWARE: isFileOrDirEntry = fInfo.setRemoteDescriptionNetware ( reply ); break;
      default: isFileOrDirEntry = fInfo.setRemoteDescriptionUNIX( reply ); break;
      }

      // If it isn't a file or directory entry then don't add it to the fileList vector.
      // NOTE: this was seen with the ftp site agora.rdrop.com which returns "total N" before the list.
      if ( isFileOrDirEntry ){
	      if ( _path.equals("/") ){
	        fInfo.setFilepath( "/" + fInfo.getName() );
	      }else{
	        fInfo.setFilepath( _path + ( _path.endsWith( "/" ) ? "" : "/" ) + fInfo.getName() );
	      }
	      fileList.addElement(fInfo);
      }
    } // for
    return fileList;
  }// setFileListDescription()


  private Socket createPassiveSocket( String commandToSend ) throws IOException {

    ServerSocket serversocket = null;
    //int socketTimeout;
    try{

      byte localInetAddress[] = InetAddress.getLocalHost().getAddress();
      serversocket = new ServerSocket(0); 	// 0: any free port
  	  //serversocket.setSoTimeout( socketTimeout );

      StringBuilder stringToSend = new StringBuilder( "PORT " );
      for ( int i = 0; i < localInetAddress.length; i++ ){
 	      stringToSend.append( localInetAddress[i] & 0xff );
        stringToSend.append( "," );
  	  }// for

      stringToSend.append( serversocket.getLocalPort() >>> 8 & 0xff );
 	    stringToSend.append( "," );
	    stringToSend.append( serversocket.getLocalPort() & 0xff );
  	  executeCommand( new String(stringToSend) ); // PORT; returns 200 PORT Command successful. in lastResponse
      if ( lastResponse.getResponse() / 100 != 2 ) {
        return null;
      }
      executeCommand( commandToSend ); // with getFile, either 150 (150 Opening BINARY mode data connection for <file> (<size>)) or 550 (550 <file>: No such file or directory.)
	    if ( lastResponse.getResponse() / 100 == 5 ) {
        return null;
      } // there is no file, so we shoulnd't create a Socket out of the server socket because nothing will connect to it. This means we don't even have to catch InterruptedIOException in this case. If we didn't jump out, then we would have to catch InterruptedIOException in order to make the program run further.
      Socket dataSocket = serversocket.accept(); // the file exists, go on
      //dataSocket.setSoTimeout(parent.getTimeout());
      return dataSocket;
    }finally  {
      try {
        serversocket.close();
      } catch (IOException ignored) {}
    }
  } // createPassiveSocket()


  private Socket createListenerPortOnTheServer(String commandToSend) throws IOException{

	  response resp = executeCommand( "PASV" );
	  if ( resp.getResponse()/100 == 2 ) {
   		//the passive command was successful
	  	List<String> tokens = string.split( resp.getStringResponse(), ",)\r\n" );
	    int highOrderPort = Integer.parseInt( tokens.get(4).toString() );
	    int lowOrderPort = Integer.parseInt( tokens.get(5).toString() );
	    // the server first opens a ServerSocket and waits until we connect to it. It doesn't send any response until we do this. This is why executeCommand() is executed so late.
 	      Socket s = new Socket( hostname,  highOrderPort * 256 + lowOrderPort );
 	      //s.setSoTimeout(parent.getTimeout());
		    executeCommand( commandToSend );
	      if ( lastResponse.getResponse() / 100 == 5 ) { return null; }; // there is no file, so we shoulnd't create a Socket out of the server socket because nothing will connect to it. This means we don't even have to catch InterruptedIOException in this case. If we didn't jump out, then we would have to catch InterruptedIOException in order to make the program run further; added on 11/06/2001 to avoid hang in getFile() 550.
        return s;
	  }else{
      lastResponse = resp;
    }
	  return null;
  }// createListenerPortOnTheServer()


  private int setSystem() {
    response resp;
    resp = executeCommand("SYST");
    String system = resp.getStringResponse().toUpperCase();
    if (system.indexOf("WINDOWS_NT") >= 0 )
    	serverOS = WINDOWS_NT;
    else if (system.indexOf("MVS") >= 0 )
    	serverOS = MVS;
    else if (system.indexOf("VM") >= 0 )
    	serverOS = VM;
    else if (system.indexOf("OS/400") >= 0 )
    	serverOS = OS400;
    else if (system.indexOf("NETWARE") >= 0 )
    	serverOS = NETWARE;
    else
    	serverOS = UNIX;
    return resp.getResponse();
  }// setSystem()

}//ftpConnection
