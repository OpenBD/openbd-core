/* 
 *  Copyright (C) 1996 - 2008 Alan Williamson
 *
 *  This file is part of Mail25 Mailet Container.
 *  
 *  Mail25 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  Free Software Foundation,version 3.
 *  
 *  Mail25 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with Mail25.  If not, see http://www.gnu.org/licenses/
 *  
 *  http://alan.blog-city.com/
 */

package org.alanwilliamson.mail25.server;

/*
 * Stores the current session data for a given client transaction.
 * 
 * If the current mail being received is shaping up to be more than
 * MEMORY_SIZE (64KB) then the in-memory buffering will be stopped
 * and instead all data will be paged straight out to disk.  This allows
 * us to handle very large emails.
 * 
 */

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.mail.internet.InternetAddress;

import org.apache.mailet.MailAddress;
import org.quickserver.net.server.ClientData;

public class MailData implements ClientData, Serializable {
	private static final long serialVersionUID = 1L;
	private static int lastId = 0;
  private int thisId;
  private int sizeRxd = 0;
  private long startTime = 0, rxdTime = 0;
  
  private int totalRxd = 0, totalMails = 0;
  
  private int MEMORY_SIZE = 64000;
  
  private boolean mailRxd = false;
  private int lastOutStatus = 0;
  private String lastOutMsg = "";
  private boolean dataMode = false;

  private ByteArrayOutputStream byteArray;
  private FileInputStream fileInputStream = null;
  private FileWriter fileWriter = null;
  private PrintWriter printWriter = null;
  private File fileSpool = null;
  
  private String clientAddress = null;
  
  private List mailFromList = null;
  private Collection mailToList = null;
  private Mail25 mail25;
  private cfMailSession	cfmailsession;
  
  public MailData(){
    thisId = lastId++;
    cfmailsession	= new cfMailSession();
  }

  public void setMail25( Mail25 mail25 ){
    this.mail25 = mail25;
  }

  public Mail25 getMail25(){
  	return this.mail25;
  }
  
  public int getId(){
    return thisId;
  }
  
  public int getLastOutStatus(){return lastOutStatus;}
  public String getLastStatusMsg(){return lastOutMsg;}
  public boolean isDataMode(){ return dataMode; }
  public boolean isMailRxd(){ return mailRxd; }
  public int getSize(){ return sizeRxd; }
  public long getMailRxdTime(){ return rxdTime; }
  public int getTotalSize(){ return totalRxd; }
  public int getTotalMails(){ return totalMails; }

  public void setRecipients(Collection recipients) {
    mailToList  = recipients;
  }

  
  public InputStream getInputStream() throws FileNotFoundException {
    if ( fileSpool == null )
      return new BufferedInputStream( new ByteArrayInputStream( byteArray.toByteArray() ) );
    else{
      fileInputStream = new FileInputStream( fileSpool );
      return new BufferedInputStream( fileInputStream );
    }
  }
  
  public void setMailProcessed(){
    mailRxd = false;
  }
  
  private void reset(){
    mailFromList  = new ArrayList();
    mailToList    = new ArrayList();
    
    lastOutStatus = 0;
    lastOutMsg    = "";
    mailRxd       = false;
    dataMode      = false;
    sizeRxd       = 0;
    closeAndDeleteFile();
  }
  
  public List getFromList(){
    return mailFromList;
  }
  
  public void setClientAddress( String ip ){
    clientAddress = ip;
  }
  
  public String getClientAddress(){
    return clientAddress;
  }
  
  public Collection getToList(){
    return mailToList;
  }
  
  public void cmdHelo(String lineIn) {
    lastOutStatus = 250;
    lastOutMsg    = "Welcome";
    sizeRxd += lineIn.length();
  }

  public void cmdQuit(String lineIn) {
    lastOutStatus = 221;
    lastOutMsg    = "Bye";
    
    //- If this message was spooled out to disk, the nuke it
    closeAndDeleteFile();
  }

  public void cmdMailFrom(String lineIn) {
    startTime = System.currentTimeMillis();
    totalMails++;
    
    sizeRxd  = lineIn.length();
    totalRxd += lineIn.length();

    reset();
    
    try{
      InternetAddress[] address = InternetAddress.parse( lineIn.substring( lineIn.indexOf(":")+1 ) );
      
      boolean bAccept = false;
      for (int x=0; x<address.length;x++){
        bAccept = mail25.acceptMailFrom( cfmailsession, address[x], getClientAddress() );
        if ( bAccept ){
          mailFromList.add( new MailAddress( address[x] ) );
        }
      }

      if ( bAccept ){
        lastOutStatus = 250;
        lastOutMsg    = "Ok";
      }else{
        lastOutStatus = 501;
        lastOutMsg    = "Bad address";
      }
        
    }catch (Exception e){
      lastOutStatus = 500;
      lastOutMsg    = "Bad Syntax (" + e.getMessage() + ")";
    }
  }

  public void cmdRcptTo(String lineIn) {
    sizeRxd += lineIn.length();
    totalRxd += lineIn.length();

    try{
      InternetAddress[] address = InternetAddress.parse( lineIn.substring( lineIn.indexOf(":")+1 ) );
      
      boolean bAccept = false;
      for (int x=0; x<address.length;x++){
        bAccept = mail25.acceptMailTo( cfmailsession, address[x], getClientAddress() );
        if ( bAccept ){
          mailToList.add( new MailAddress( address[x] ) );
        }
      }

      if ( bAccept ){
        lastOutStatus = 250;
        lastOutMsg    = "Ok";
      }else{
        lastOutStatus = 550;
        lastOutMsg    = "No one here";
      }

    }catch (Exception e){
      lastOutStatus = 500;
      lastOutMsg    = "Bad Syntax (" + e.getMessage() + ")";
    }
  }

  public void cmdData(String lineIn) {
    /*
     * Got to make sure they have put this in the right order
     */
    if ( mailToList.size() == 0 || mailFromList.size() == 0 ){
      lastOutStatus = 503;
      lastOutMsg    = "not valid here";
      return;
    }
    
    sizeRxd += lineIn.length();
    totalRxd += lineIn.length();

    lastOutStatus = 354;
    lastOutMsg    = "End data with <CR><LF>.<CR><LF>";
    dataMode      = true;
    
    byteArray     = new ByteArrayOutputStream( MEMORY_SIZE );
    printWriter   = new PrintWriter( new BufferedWriter( new OutputStreamWriter(byteArray) ) );
  }
  
  public void close(){
    //-- This makes sure all the buffers are closed
    if ( printWriter != null )
      printWriter.close();

    closeAndDeleteFile();
  }
  
  public void dataRxd(String lineIn) throws IOException {
    sizeRxd += lineIn.length();
    totalRxd += lineIn.length();

    if  ( lineIn.length() == 1 && lineIn.charAt(0) == '.' ){
      lastOutStatus = 250;
      lastOutMsg    = "Ok: mail accepted";
      dataMode      = false;
      mailRxd       = true;
      rxdTime       = System.currentTimeMillis() - startTime;
      
      printWriter.flush();
      printWriter.close();
      if ( fileWriter != null )
        fileWriter.close();
      
      fileWriter = null;
      printWriter = null;
    }else{
      //- check for the escape for the .
      if ( lineIn.length() >= 2 && lineIn.charAt(0) == '.' && lineIn.charAt(1) == '.'  ){
        lineIn  = lineIn.substring( 1 );
      }

      printWriter.println( lineIn );
      printWriter.flush();
      
      //- check to see we aren't handling too big a message. if we are
      //- then swing over the output buffers and page to disk instead.
      if ( byteArray != null && byteArray.size() > MEMORY_SIZE ){
        spoolToDisk();
      }

      lastOutStatus = 0;
    }
  }
  
  private void closeAndDeleteFile(){
    try{
      if ( fileWriter != null )
        fileWriter.close();
      
      if ( fileInputStream != null )
        fileInputStream.close();
      
      if ( fileSpool != null )
        fileSpool.delete();
    }catch(Exception ignoreTheClose){}
    
    fileWriter      = null;
    fileSpool       = null;
    fileInputStream = null;
  }
  
  private void spoolToDisk() throws IOException{
    fileSpool = mail25.getSpoolFile();

    //- Create the output
    fileWriter  = new FileWriter(fileSpool);
    printWriter = new PrintWriter( new BufferedWriter( fileWriter ) );

    //- Page out to disk what we already have collected
    String out = new String( byteArray.toByteArray() );
    printWriter.print( out );
    printWriter.flush();
    
    //-- Reset the internal buffer, and set it to null to let it be garbage collected
    byteArray.reset();
    byteArray = null;
  }

	public boolean hasRpct() {
		return (mailToList.size() > 0);
	}

	public boolean hasFrom() {
		return (mailFromList.size() > 0);
	}

	public Object getMailSession() {
		return cfmailsession;
	}
}
