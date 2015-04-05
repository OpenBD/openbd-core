/* 
 *  Copyright (C) 1996 - 2011 Alan Williamson
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
 *  
 *  $Id: MailHandler.java 1720 2011-10-07 19:39:14Z alan $
 */

package org.alanwilliamson.mail25.server;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.alanwilliamson.mail25.mailet.CaughtMail;
import org.quickserver.net.server.ClientCommandHandler;
import org.quickserver.net.server.ClientEventHandler;
import org.quickserver.net.server.ClientHandler;

public class MailHandler implements ClientCommandHandler, ClientEventHandler  {

  public void handleCommand(ClientHandler clientHandler, String lineIn ) throws SocketTimeoutException, IOException {
  	
    MailData  maildata = (MailData)clientHandler.getClientData();
    if ( maildata.isDataMode() ){
      maildata.dataRxd( lineIn );
    } else if ( lineIn.toUpperCase().startsWith("MAIL FROM" ) ){
      maildata.cmdMailFrom( lineIn );
    } else if ( lineIn.toUpperCase().startsWith("RCPT TO" ) ){
    	
    	if ( !maildata.hasFrom() ){
    		clientHandler.sendClientMsg( "500 No valid MAIL FROM" );
    		clientHandler.closeConnection();
    		return;
    	}else    	
    		maildata.cmdRcptTo( lineIn );
    	
    } else if ( lineIn.toUpperCase().startsWith("DATA" ) ){
    	
    	if ( !maildata.hasRpct() ){
    		clientHandler.sendClientMsg( "500 No valid RCPT TO" );
    		clientHandler.closeConnection();
    		return;
    	}else
    	  maildata.cmdData( lineIn );
    	
    } else if ( lineIn.toUpperCase().startsWith("HELO" ) || lineIn.toUpperCase().startsWith("EHLO" )){
      maildata.cmdHelo( lineIn );
    } else if ( lineIn.toUpperCase().startsWith("QUIT" ) ){
      maildata.cmdQuit( lineIn );
      clientHandler.sendClientMsg( maildata.getLastOutStatus() + " " + maildata.getLastStatusMsg() );
      clientHandler.closeConnection();
    } else {
      clientHandler.sendClientMsg( "500 Unknown Message" );
      return;
    }

    // Send out status to client
    if ( maildata.getLastOutStatus() != 0 ){
      clientHandler.sendClientMsg( maildata.getLastOutStatus() + " " + maildata.getLastStatusMsg() );
    }
    
    // If the mail has been received then pass it onto the Mailet container
    if ( maildata.isMailRxd() ){
      CaughtMail newMail = new CaughtMail(maildata);
      maildata.getMail25().log( "In#" + maildata.getId() + "; size=" + maildata.getSize() + "; time=" + maildata.getMailRxdTime() + "ms; from=" + newMail.getSender() + "; to=" + newMail.getRecipients() );
      maildata.getMail25().acceptMail( newMail );
      maildata.setMailProcessed();
    }
  }
  
  public void gotConnected(ClientHandler clientHandler) throws SocketTimeoutException, IOException {
    MailData  maildata = (MailData)clientHandler.getClientData();
    
    maildata.setMail25( (Mail25)clientHandler.getServer().getStoreObjects()[0] );
    
    String banner	= (String)clientHandler.getServer().getStoreObjects()[1];
    if ( banner != null)
    	clientHandler.sendClientMsg( "220 EMSTP " + banner );
    else
    	clientHandler.sendClientMsg( "220 ESMTP Mail25 OpenBD http://openbd.org/" );
    
    maildata.setClientAddress( clientHandler.getHostAddress() );

    maildata.getMail25().statsClientConnected();
    maildata.getMail25().log( "In#" + maildata.getId() + "; " + clientHandler.getHostAddress() + " connected" );
  }

  
  public void lostConnection(ClientHandler clientHandler) throws IOException {
    MailData  maildata = (MailData)clientHandler.getClientData();
    maildata.close();
    maildata.getMail25().statsLostConnection();
    maildata.getMail25().log( "In#" + maildata.getId() + "; " + clientHandler.getHostAddress() + " lost connection" );
  }

  
  public void closingConnection(ClientHandler clientHandler) throws IOException {
    MailData  maildata = (MailData)clientHandler.getClientData();
    maildata.close();
    maildata.getMail25().statsLostConnection();
    maildata.getMail25().log( "In#" + maildata.getId() + "; " + clientHandler.getHostAddress() + " closed; totalSize=" + maildata.getTotalSize() + "; totalMails=" + maildata.getTotalMails() );
  }
}