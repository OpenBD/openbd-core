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

package org.alanwilliamson.mail25.mailet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.mailet.Mail;
import org.apache.mailet.MailAddress;
import org.apache.mailet.MailetContext;

public class Mail25Context implements MailetContext {

  private long             MAXFILESIZE = 10000000L; //- Max 10MB log file
  private String           logFile;
  private RandomAccessFile logWriter;
  private long             logFileSize;
  private HashMap          attributes;
  private MailAddress      postMaster;
  
  public Mail25Context( Properties config ) throws Exception {
    this.logFile  = config.getProperty("mail25.logfile",   "mail25.log" );
    logWriter     = null;
    attributes    = new HashMap();
    
    //-- Copy over the attributes from config
    Iterator it = config.keySet().iterator();
    while ( it.hasNext() ){
      String key  = (String)it.next();
      attributes.put( key, config.get(key) );
    }

    postMaster    = new MailAddress( config.getProperty("mail25.postmaster", "root@localhost" ) );
  }

  public void shutdown(){
    closeLogFile();
  }
  
  private void closeLogFile(){
    try{
      if ( logWriter != null )
        logWriter.close();
    } catch(IOException ignore){} 
  }
  
  private void openLogFile() {
    try {
      logWriter   = new RandomAccessFile( this.logFile, "rw" );
      logFileSize = logWriter.length();
      logWriter.seek( logFileSize );
    } catch (Exception e) {
      logWriter = null;
    }
  }
  
  private void resetLogFile() {
    try{
      //- Find a new file name
      int f = 1;
      File newFile = new File( logFile + "." + f );
      while ( newFile.exists() ){
        newFile = new File( logFile + "." + (f++) );
      }

      //- Now that we have that file lets move and delete
      File oldFile = new File( logFile );
      oldFile.renameTo( newFile );
      oldFile.delete();
    }catch(Exception ignoreException){}
    logWriter = null;
  }
  
  public synchronized void log(String item) {
    if ( logWriter == null ){
      openLogFile();
    }
    
    if ( logWriter != null ){
      try {
        logWriter.writeBytes( new SimpleDateFormat( "MMM dd HH:mm.ss: ").format(new java.util.Date()) );
        logWriter.writeBytes( item );
        logWriter.writeBytes( "\r\n" );
        
        logFileSize += item.length() + 19;
        
        if ( logFileSize > MAXFILESIZE )
          resetLogFile();
        
      } catch (IOException e) {
        if ( logWriter != null ){ try{ logWriter.close(); } catch(IOException ignore){} }
        logWriter = null;
      }
    }
  }

  public void log(String item, Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    pw.close();
    log( item + ":" + sw.toString() );
  }


  public Iterator getSMTPHostAddresses(String host) {
    return getMailServers(host).iterator();
  }
  
  public MailAddress getPostmaster() {
    return postMaster;
  }

  public Object getAttribute(String key) {
    return attributes.get( key );
  }

  public Iterator getAttributeNames() {
    return attributes.keySet().iterator();
  }

  public int getMajorVersion() {
    return 0;
  }

  public int getMinorVersion() {
    return 3;
  }

  public String getServerInfo() {
    return "Mail25";
  }

  public void removeAttribute(String key) {
    attributes.remove( key );
  }
  
  public void setAttribute(String key, Object data) {
    attributes.put( key, data );
  }

  public void storeMail(MailAddress arg0, MailAddress arg1, MimeMessage arg2) throws MessagingException {
    throw new MessagingException("feature not supported");
  }

  public boolean isLocalUser(String arg0) {
    return false;
  }

	public void bounce(Mail arg0, String arg1) throws MessagingException {
	}

	public void bounce(Mail arg0, String arg1, MailAddress arg2) throws MessagingException {
	}

	public Collection getMailServers(String arg0) {
		return null;
	}

	public boolean isLocalServer(String arg0) {
		return false;
	}

	public void sendMail(MimeMessage arg0) throws MessagingException {
	}

	public void sendMail(Mail arg0) throws MessagingException {
	}

	public void sendMail(MailAddress arg0, Collection arg1, MimeMessage arg2) throws MessagingException {
	}

	public void sendMail(MailAddress arg0, Collection arg1, MimeMessage arg2, String arg3) throws MessagingException {
	}
}
