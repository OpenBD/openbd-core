/* 
 *  Copyright (C) 2012 TagServlet Ltd
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
 *  http://openbd.rg/
 *  $Id: cfFTPData.java 2275 2012-09-04 00:20:26Z alan $
 */
package com.naryx.tagfusion.cfm.tag.net.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructReadOnlyData;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

public class cfFTPData extends cfStructReadOnlyData {
	private static final long serialVersionUID = 1L;

	private FTPClient	ftpclient = null;

	private boolean succeeded = true;
  private int     errorCode = 0;
  private String  errorText = "";
	
	private	String 	username, password;
	
  private int     inUse = 0;
  private Object  semaphore;

	
	public cfFTPData( String _Server, int _Port, String _Username, String _Password ){
		
		setPrivateData( "server", new cfStringData(_Server) );
		setPrivateData( "port", 	new cfNumberData(_Port) );

		this.username = _Username;
		this.password	= _Password;
		
		ftpclient			= new FTPClient();
	}
	
	public cfFTPData(boolean b, int i, String string) {
		succeeded = b;
		errorCode = i;
		errorText	= string;
	}

	public void finalize() throws Throwable { 
    close();
  }
	
  public synchronized void lock(){
    while ( inUse != 0 )
      try{ semaphore.wait(); }catch(Exception E){}

    inUse++;
  }

  public synchronized void unlock(){
    inUse       -= 1;
    if ( inUse < 0 ) inUse = 0;
    try{ semaphore.notify(); }catch(Exception E){}
  }
	
	public void setTimeout( int timeoutMS ){
		ftpclient.setDataTimeout(timeoutMS);
	}
	
	private void setStatusData(){
		setPrivateData( "succeeded", cfBooleanData.getcfBooleanData(succeeded) );
		setPrivateData( "connected", cfBooleanData.getcfBooleanData(ftpclient.isConnected()) );
		setPrivateData( "errorcode", new cfNumberData(errorCode) );
		setPrivateData( "errortext", new cfStringData(errorText) );
	}
	
	public boolean isOpen(){
		return ftpclient.isConnected();
	}
	
	public void close(){
		if ( ftpclient.isConnected() ){
			try {
				ftpclient.disconnect();
			} catch (IOException e) {}
		}
	}
	
	public void open(){
		if ( !ftpclient.isConnected() ){
			try {
				ftpclient.connect( getData("server").getString(),  getData("port").getInt() );
				errorCode = ftpclient.getReplyCode();
				
				if (!FTPReply.isPositiveCompletion(errorCode))
					throw new Exception( ftpclient.getReplyString() );

				succeeded = ftpclient.login(username, password);
				errorCode = ftpclient.getReplyCode();
				
			} catch (Exception e) {
				succeeded = false;
				errorText	= e.getMessage();
			}
			setStatusData();
		}
	}

	public boolean isSucceeded() {
		return succeeded;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorText() {
		return errorCode + " " + ec.get( String.valueOf(errorCode) ) + " " + errorText;
	}

	public void setPassive(boolean passive) {
		if ( passive )
			ftpclient.enterLocalPassiveMode();
		else
			ftpclient.enterLocalActiveMode();
	}
	
	public FTPFile[]	listFiles(String directory) {
		if ( !ftpclient.isConnected() ){
			errorText = "not connected";
			succeeded	= false;
			return null;
		}
		
		FTPFile[]	files = null;
		
		try{
			files			= ftpclient.listFiles(directory);
			errorCode = ftpclient.getReplyCode();
			succeeded	= FTPReply.isPositiveCompletion(errorCode);
		}catch(Exception e){
			errorCode = ftpclient.getReplyCode();
			errorText	= e.getMessage();
		}finally{
			setStatusData();
		}
		
		return files;
	}

	public String getServer() throws dataNotSupportedException {
		return getData("server").getString();
	}

	public String getCurrentDirectory() {
		if ( !ftpclient.isConnected() ){
			errorText = "not connected";
			succeeded	= false;
			return null;
		}
		
		String curdir = null;
		try{
			curdir		= ftpclient.printWorkingDirectory();
			errorCode = ftpclient.getReplyCode();
			succeeded	= FTPReply.isPositiveCompletion(errorCode);
		}catch(Exception e){
			errorCode = ftpclient.getReplyCode();
			errorText	= e.getMessage();
		}finally{
			setStatusData();
		}
		
		return curdir;
	}

	public boolean setCurrentDirectory(String directory) {
		if ( !ftpclient.isConnected() ){
			errorText = "not connected";
			succeeded	= false;
			return false;
		}
		
		boolean bResult = false;
		try{
			bResult 	= ftpclient.changeWorkingDirectory(directory);
			errorCode = ftpclient.getReplyCode();
			succeeded	= FTPReply.isPositiveCompletion(errorCode);
		}catch(Exception e){
			errorCode = ftpclient.getReplyCode();
			errorText	= e.getMessage();
		}finally{
			setStatusData();
		}
		
		return bResult;
	}

	public boolean makeDirectory(String file) {
		if ( !ftpclient.isConnected() ){
			errorText = "not connected";
			succeeded	= false;
			return false;
		}
		
		boolean bResult = false;
		try{
			bResult 	= ftpclient.makeDirectory(file);
			errorCode = ftpclient.getReplyCode();
			succeeded	= FTPReply.isPositiveCompletion(errorCode);
		}catch(Exception e){
			errorCode = ftpclient.getReplyCode();
			errorText	= e.getMessage();
		}finally{
			setStatusData();
		}
		
		return bResult;
	}

	public boolean removeDirectory(String file) {
		if ( !ftpclient.isConnected() ){
			errorText = "not connected";
			succeeded	= false;
			return false;
		}
		
		boolean bResult = false;
		try{
			bResult 	= ftpclient.removeDirectory(file);
			errorCode = ftpclient.getReplyCode();
			succeeded	= FTPReply.isPositiveCompletion(errorCode);
		}catch(Exception e){
			errorCode = ftpclient.getReplyCode();
			errorText	= e.getMessage();
		}finally{
			setStatusData();
		}
		
		return bResult;
	}

	public boolean removeFile(String file) {
		if ( !ftpclient.isConnected() ){
			errorText = "not connected";
			succeeded	= false;
			return false;
		}
		
		boolean bResult = false;
		try{
			bResult 	= ftpclient.deleteFile(file);
			errorCode = ftpclient.getReplyCode();
			succeeded	= FTPReply.isPositiveCompletion(errorCode);
		}catch(Exception e){
			errorCode = ftpclient.getReplyCode();
			errorText	= e.getMessage();
		}finally{
			setStatusData();
		}
		
		return bResult;
	}

	public boolean renameFile(String oldfile, String newfile) {
		if ( !ftpclient.isConnected() ){
			errorText = "not connected";
			succeeded	= false;
			return false;
		}
		
		boolean bResult = false;
		try{
			bResult 	= ftpclient.rename(oldfile, newfile);
			errorCode = ftpclient.getReplyCode();
			succeeded	= FTPReply.isPositiveCompletion(errorCode);
		}catch(Exception e){
			errorCode = ftpclient.getReplyCode();
			errorText	= e.getMessage();
		}finally{
			setStatusData();
		}
		
		return bResult;
	}

	public boolean setTransferMode(int transferMode) {
		try {
			ftpclient.setFileTransferMode(transferMode);
			ftpclient.setFileType(transferMode);			
			return true;
		} catch (IOException e) {
			errorCode = ftpclient.getReplyCode();
			errorText	= e.getMessage();
			return false;
		}finally{
			setStatusData();
		}
	}

	
	public void downloadFile(File fileLocal, String remoteFile) {
		if ( !ftpclient.isConnected() ){
			errorText = "not connected";
			succeeded	= false;
			return;
		}
		
		FileOutputStream	fos = null;
		BufferedOutputStream	bos = null;
		try{

			ftpclient.setAutodetectUTF8(true);

			fos	= new FileOutputStream( fileLocal );
			bos	= new BufferedOutputStream(fos);
			ftpclient.retrieveFile(remoteFile, bos);
			bos.flush();
			bos.close();
			
			errorCode = ftpclient.getReplyCode();
			succeeded	= FTPReply.isPositiveCompletion(errorCode);

		}catch(Exception e){
			errorCode = ftpclient.getReplyCode();
			errorText	= e.getMessage();
		}finally{
			if ( fos != null ){
				try {
					fos.close();
				} catch (IOException e) {}
			}
			setStatusData();
		}
	}

	public void uploadFile(File fileLocal, String remoteFile) {
		if ( !ftpclient.isConnected() ){
			errorText = "not connected";
			succeeded	= false;
			return;
		}
		
		FileInputStream	fis = null;
		BufferedInputStream	bis = null;
		try{

			ftpclient.setAutodetectUTF8(true);

			fis	= new FileInputStream( fileLocal );
			bis	= new BufferedInputStream(fis);
			ftpclient.storeFile(remoteFile, bis);
			bis.close();

			errorCode = ftpclient.getReplyCode();
			succeeded	= FTPReply.isPositiveCompletion(errorCode);

		}catch(Exception e){
			errorCode = ftpclient.getReplyCode();
			errorText	= e.getMessage();
		}finally{
			if ( fis != null ){
				try {
					fis.close();
				} catch (IOException e) {}
			}
			setStatusData();
		}
	}

	public boolean siteCmd(String cmd) {
		if ( !ftpclient.isConnected() ){
			errorText = "not connected";
			succeeded	= false;
			return false;
		}
		
		boolean bResult = false;
		try{
			bResult 	= ftpclient.sendSiteCommand(cmd);
			errorCode = ftpclient.getReplyCode();
			succeeded	= FTPReply.isPositiveCompletion(errorCode);
		}catch(Exception e){
			errorCode = ftpclient.getReplyCode();
			errorText	= e.getMessage();
		}finally{
			setStatusData();
		}
		
		return bResult;
	}


	public String cmd(String cmd) {
		if ( !ftpclient.isConnected() ){
			errorText = "not connected";
			succeeded	= false;
			return "";
		}
		
		String params = "";
		if ( cmd.indexOf(" ") != -1){
			params	= cmd.substring( cmd.indexOf(" ") + 1 );
			cmd	= cmd.substring( 0, cmd.indexOf(" ") );
		}
		
		String bResult = "";
		try{
			ftpclient.doCommand(cmd, params);
			bResult	= ftpclient.getReplyString();
			errorCode = ftpclient.getReplyCode();
			succeeded	= FTPReply.isPositiveCompletion(errorCode);
		}catch(Exception e){
			errorCode = ftpclient.getReplyCode();
			errorText	= e.getMessage();
		}finally{
			setStatusData();
		}
		
		return bResult;
	}

	
	private static HashMap<String,String> ec;
	static{
		ec = new HashMap<String,String>();
		ec.put("100","Series: The requested action is being initiated, expect another reply before proceeding with a new command.");
		ec.put("110","Restart marker replay . In this case, the text is exact and not left to the particular implementation; it must read: MARK yyyy = mmmm where yyyy is User-process data stream marker, and mmmm server's equivalent marker (note the spaces between markers and =).");
		ec.put("120","Service ready in nnn minutes.");
		ec.put("125","Data connection already open; transfer starting.");
		ec.put("150","File status okay; about to open data connection.");
		ec.put("200","Command okay.");
		ec.put("202","Command not implemented, superfluous at this site.");
		ec.put("211","System status, or system help reply.");
		ec.put("212","Directory status.");
		ec.put("213","File status.");
		ec.put("214","Help message.On how to use the server or the meaning of a particular non-standard command. This reply is useful only to the human user.");
		ec.put("215","NAME system type. Where NAME is an official system name from the registry kept by IANA.");
		ec.put("220","Service ready for new user.");
		ec.put("221","Service closing control connection.");
		ec.put("225","Data connection open; no transfer in progress.");
		ec.put("226","Closing data connection. Requested file action successful (for example, file transfer or file abort).");
		ec.put("227","Entering Passive Mode (h1,h2,h3,h4,p1,p2).");
		ec.put("228","Entering Long Passive Mode (long address, port).");
		ec.put("229","Entering Extended Passive Mode (|||port|).");
		ec.put("230","User logged in, proceed. Logged out if appropriate.");
		ec.put("231","User logged out; service terminated.");
		ec.put("232","Logout command noted, will complete when transfer done.");
		ec.put("250","Requested file action okay, completed.");
		ec.put("257","\"PATHNAME\" created.");
		ec.put("331","User name okay, need password.");
		ec.put("332","Need account for login.");
		ec.put("350","Requested file action pending further information");
		ec.put("421","Service not available, closing control connection. This may be a reply to any command if the service knows it must shut down.");
		ec.put("425","Can't open data connection.");
		ec.put("426","Connection closed; transfer aborted.");
		ec.put("430","Invalid username or password");
		ec.put("434","Requested host unavailable.");
		ec.put("450","Requested file action not taken.");
		ec.put("451","Requested action aborted. Local error in processing.");
		ec.put("452","Requested action not taken. Insufficient storage space in system.File unavailable (e.g., file busy).");
		ec.put("500","Syntax error, command unrecognized. This may include errors such as command line too long.");
		ec.put("501","Syntax error in parameters or arguments.");
		ec.put("502","Command not implemented.");
		ec.put("503","Bad sequence of commands.");
		ec.put("504","Command not implemented for that parameter.");
		ec.put("530","Not logged in.");
		ec.put("532","Need account for storing files.");
		ec.put("550","Requested action not taken. File unavailable (e.g., file not found, no access).");
		ec.put("551","Requested action aborted. Page type unknown.");
		ec.put("552","Requested file action aborted. Exceeded storage allocation (for current directory or dataset).");
		ec.put("553","Requested action not taken. File name not allowed.");
		ec.put("631","Integrity protected reply.");
		ec.put("632","Confidentiality and integrity protected reply.");
		ec.put("633","Confidentiality protected reply.");
	}
	
}
