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

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.alanwilliamson.mail25.server.MailData;
import org.apache.mailet.Mail;
import org.apache.mailet.MailAddress;

public class CaughtMail implements Mail, Serializable {
	private static final long serialVersionUID = 1L;
	private MailData    origEmail;
  private String      errorMess = null;
  private MimeMessage mimeMessage = null;
  private HashMap     attributes;
  private String      mailState;
  
  public CaughtMail( MailData in ){
    origEmail   = in;
    attributes  = new HashMap();

    attributes.put("session", in.getMailSession() );
    mailState   = Mail.DEFAULT;
  }


  public void setMessage(MimeMessage newMimeMessage) {
    if (  mimeMessage != null ){
      //- Previous instance is still available, remove it
      
    }

    mimeMessage = newMimeMessage;
  }
  
  public MimeMessage getMessage() throws MessagingException {
    if ( mimeMessage == null ){
      try {
        mimeMessage = new MimeMessage( Session.getInstance( new Properties() ), origEmail.getInputStream() );
      } catch (FileNotFoundException e) {
        throw new MessagingException("Spool file no longer available");
      }
    }

    return mimeMessage;
  }

  public Collection getRecipients() {
    return origEmail.getToList();
  }

  public MailAddress getSender() {
    return (MailAddress)origEmail.getFromList().get(0);
  }

  public String getState() {
    return mailState;
  }

  public String getRemoteHost() {
    return origEmail.getClientAddress();
  }

  public String getRemoteAddr() {
    return origEmail.getClientAddress();
  }

  public String getErrorMessage() {
    return errorMess;
  }

  public void setErrorMessage(String _errorMess) {
    errorMess = _errorMess;
  }

  public void setState(String newMailState) {
    mailState = newMailState;
  }

  public Serializable getAttribute(String key) {
    return (Serializable)attributes.get(key);
  }

  public Iterator getAttributeNames() {
    return attributes.keySet().iterator();
  }

  public boolean hasAttributes() {
    return attributes.size() != 0;
  }

  public Serializable removeAttribute(String key) {
    return (Serializable)attributes.remove( key );
  }

  public void removeAllAttributes() {
    attributes.clear();
  }

  public Serializable setAttribute(String key, Serializable data) {
    attributes.put( key, data );
    return data;
  }

  public void setRecipients(Collection recipients) {
    origEmail.setRecipients( recipients );
  }
}
