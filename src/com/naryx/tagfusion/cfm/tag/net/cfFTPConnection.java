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

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;

public class cfFTPConnection extends cfData implements java.io.Serializable{

  static final long serialVersionUID = 1;

  private ftpConnection connection;
  private int     inUse = 0;
  private Object  semaphore;

  private int     timeout     = 30000;  //-- specified in milliseconds


  //--[ Error codes
  private boolean stopOnError = false;
  private boolean succeeded = true;
  private int     errorCode = 0;
  private String  errorText = "";
  private cfData  returnValue = null;

  public cfFTPConnection( boolean _bSucceeded, int _errorCode, String _errorText ) {
    succeeded = _bSucceeded;
    errorCode = _errorCode;
    errorText = _errorText;
  }

  public cfFTPConnection( String _Server, int _Port, String _Username, String _Password ) {
    //- Simply create the underlying instance
    connection  = new ftpConnection( _Server, _Port, _Username, _Password );
    semaphore   = new Object();
  }


  /**
   *
   * @param _timeout #of seconds to all for all operations
   */
  public void setTimeout( int _timeout ){ timeout  = _timeout * 1000; }
  public void setStopOnError( boolean _stopOnError ){ stopOnError  = _stopOnError; }
  public void setPassive( boolean _passive ){ if ( connection != null ) connection.setPassiveMode( _passive ); }
  public void setReturnValue( cfData _returnValue ){ returnValue  = _returnValue; }

  public boolean stopOnError(){ return stopOnError; }
  public boolean didSucceed(){ return succeeded; }
  public int     getErrorCode(){ return errorCode; }
  public String  getErrorText(){ return errorText; }
  public cfData  getReturnValue(){ return returnValue; }

  public String getServer(){
    return connection.getServer();
  }

  public int getPort(){
    return connection.getServerPort();
  }

  public void resetError(){
    succeeded = true;
    errorCode = 0;
    errorText = "";
    inUse     = 0;
  }

  public void finalize() throws Throwable {   //--[ Since this in a session it will die naturally
    close();
  }

  public synchronized void lock(){
    while ( inUse != 0 )
      try{ semaphore.wait(); }catch(Exception E){}

    inUse++;
  }

  public synchronized void unlock(){
    inUse       -= 0;
    if ( inUse < 0 ) inUse = 0;
    try{ semaphore.notify(); }catch(Exception E){}
  }


  public boolean isConnectionOpen(){
    return connection.isOpen();
  }


  public void open() {
    //--[ Open the connection up. Assume that a close() has been done already if its done.
    //--[ If something goes wrong, but fill in the error codes
    resetError();

    response resp = connection.open(timeout);
    if ( resp == null ){
      succeeded = false;
      errorCode = 15;
      errorText = "No response from server.";

    }else if ( resp.getResponse() != 230){
      succeeded = false;
      errorCode = resp.getResponse();
      errorText = resp.getStringResponse();

    }
  }//open()


  public void close(){
    //--[
    resetError();
    connection.disconnect();
  }

  public void actionChangeDir( String DIRECTORY ){
    //--[
    resetError();
    response resp = connection.changeDir( DIRECTORY );
    setResponse( resp, 250 );
  }


  public void actionCreateDir( String DIRECTORY ){
    //--[
    resetError();
    response resp = connection.createDir( DIRECTORY );
    setResponse( resp, 257 );
  }

  public void actionRemoveDir( String DIRECTORY ){
    //--[
    resetError();
    response resp = connection.removeDir( DIRECTORY );
    setResponse( resp, 250 );
  }

  public boolean actionDirExists( String DIRECTORY ){
    //--[ This could be more efficient
    resetError();
		String currentDir = actionGetCurrentDir();
    actionChangeDir( DIRECTORY );
    boolean exists = succeeded;
   	actionChangeDir( currentDir );
    setResponse( connection.getLastResponse(), 250 );
    return exists;
  }

  public String actionGetCurrentDir(){
    //--[
    resetError();
    String currentDir = connection.getCurrentDir();
    response resp = connection.getLastResponse();
    setResponse( resp, 257 );
    if ( currentDir == null ){
      succeeded = false;
      return "";
    }
    return currentDir;
  }

  public String actionGetCurrentURL(){
    //--[
    resetError();
    String currDir = actionGetCurrentDir().replace('\\','/');
    setResponse( connection.getLastResponse(), 257 );
    return currDir;
  }

  public boolean actionFileExists( String REMOTEFILE ) throws IOException{
    //--[
    resetError();
    List<fileInfo> result = actionListDir( REMOTEFILE );
    if ( result == null || result.size() == 0 ){
      return false;
    }else{
      fileInfo fi = (fileInfo) result.get(0);
      setResponse( connection.getLastResponse(), 226 );
      if ( fi.isDirectory() ){
        return false;
      }else{
        return true;
      }
    }
  }

  public void actionRemove( String ITEM ){
    //--[ You need to fill in the necessary error codes for this operation
    resetError();
    response resp = connection.remove( ITEM );
    setResponse( resp, 250 );
  }

  public void actionRename( String REMOTEFROM, String REMOTETO ){
    //--[ You need to fill in the necessary error codes for this operation
    resetError();
    response resp = connection.rename( REMOTEFROM, REMOTETO );
    setResponse( resp, 250 );
  }

  public void actionPutFile( File LOCALFILE, String REMOTEFILE, String TRANSFERMODE ) throws IOException{
    //--[ You need to fill in the necessary error codes for this operation
    resetError();
    connection.setTransferMode( TRANSFERMODE );
    response resp = connection.putFile( LOCALFILE, REMOTEFILE );
    setResponse( resp, 226 );
  }

  public void actionGetFile( File LOCALFILE, String REMOTEFILE, String TRANSFERMODE ) throws IOException{
    //--[ You need to fill in the necessary error codes for this operation
    resetError();
    connection.setTransferMode( TRANSFERMODE );
    response resp = connection.getFile( REMOTEFILE, LOCALFILE );
    setResponse( resp, 226 );
  }

  public List<fileInfo> actionListDir( String REMOTEFILE ) throws IOException{
    //--[ You need to fill in the necessary error codes for this operation
    resetError();
    List<fileInfo> list   = connection.listDir( REMOTEFILE );
    response resp = connection.getLastResponse();

    // CFMX treats a 550 as succeeded so BD should too.
    if ( ( ( resp.getResponse() ) >= 400 ) && ( resp.getResponse() != 550 ) ) {
      succeeded = false;
    }

    // Always update the erroCode and errorText values
    errorCode = resp.getResponse();
    errorText = resp.getStringResponse();
    return list;
  }

  private void setResponse( response _resp, int _successCode ){
    if ( _resp.getResponse() != _successCode ){
      succeeded = false;
    }

    // Always update the erroCode and errorText values
    errorCode = _resp.getResponse();
    errorText = _resp.getStringResponse();
  }
}
