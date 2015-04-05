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

/*
 * Wrapper for the Mail object that will be passed into the CFC
 * for processing an incoming email via CFSMTP
 * 
 * The idea is not to duplicate any data, but make a call down into the
 * underlying Mail object for the data
 * 
 */
package org.alanwilliamson.openbd.plugin.cfsmtp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.mail.Address;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.mailet.Mail;
import org.apache.mailet.MailAddress;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;

public class BlueDragonMailWrapper extends cfStructData {
	private static final long serialVersionUID = 1L;
	private Mail mail;
  private MimeMessage message;
  
  public BlueDragonMailWrapper( Mail mail ) throws Exception {
    this.mail     = mail;   
    this.message  = mail.getMessage(); 
  }

  public BlueDragonMailWrapper( MimeMessage message ) throws Exception {
    this.mail     = null;   
    this.message  = message; 
  }

  
  
  public void dumpLong( java.io.PrintWriter out, String _lbl, int _top ){
    dump( out, _lbl, _top );
  }
  
  public void dump( java.io.PrintWriter out, String _lbl, int _top ) {
    out.write( "<table class='cfdump_table_struct'><tr><th>mail</th></tr><tr><td><ul>" );
    out.write( "<li><b>getAllBodyParts()</b> returns Array of ALL Parts including the inner ones</li>");
    out.write( "<li><b>getBodyParts()</b> returns Array of Parts; as per email</li>");
    out.write( "<li><b>getCcList()</b> returns Array of Structs (name/email) of all the people addressed in the Cc field</li>");
    out.write( "<li><b>getFromList()</b> returns Array of Structs (name/email) of all the people addressed in the From field</li>");
    out.write( "<li><b>getHeaders()</b> returns Array of all the email headers</li>");
    out.write( "<li><b>getHeader( string )</b> returns the value of the given header</li>");
    
    if ( mail != null )
    	out.write( "<li><b>getIP()</b> returns the IP address of the delivery agent</li>");
    
    out.write( "<li><b>getMessageId()</b> returns the email message id</li>");
    out.write( "<li><b>getMailFrom()</b> returns email address that was in the MAIL FROM</li>");
    out.write( "<li><b>getReplyToList()</b> returns Array of Structs (name/email) of all the people addressed in the Reply-To field</li>");
    out.write( "<li><b>getSentDate()</b> returns the date the message was sent</li>");
    out.write( "<li><b>getReceivedDate()</b> returns the date the message was received</li>");
    
    if ( mail != null )
    	out.write( "<li><b>getRecipients()</b> returns Array of email addresses; these are the emails to which the email was delivered to via RCPT TO</li>");
    
    out.write( "<li><b>getSubject()</b> returns the email subject</li>");
    out.write( "<li><b>getSize()</b> returns the size of the email</li>");
    out.write( "<li><b>getToList()</b> returns Array of Structs (name/email) of all the people addressed in the To field</li>");
    out.write( "<li><b>spoolMailToDir(dir)</b> saves the mail to disk and returns the full path of the saved file</li>");
    out.write( "</ul></td><tr><table>" );
  }
    
  
  public cfStringData	spoolMailToDir( String directory ){
  	
  	File fileDir	= new File( directory );
  	if ( !fileDir.isDirectory() ){
  		fileDir.mkdirs();
  	}

  	
  	FileOutputStream	outWriter = null;
  	
  	try{
  		String id = this.message.getMessageID();
  		id	= id.replace('>', '0');
  		id	= id.replace('<', '0');
  		id	= id.replace('@', '-');
  		id	= id.replace('.', '-');
  		
  		/* Make sure its not too long, lets chop it */
  		if ( id.length() > 64 ){
  			id	= id.substring( 0, 32 ) + id.substring( id.length() - 32 );
  		}
  		id	= id + ".mail";


  		/* Make sure the file is unique */
  		int x = 0;
  		File	outFile	= new File( fileDir, id );
  		while ( outFile.isFile() ){
  			outFile	= new File( directory, (x++) + "" + id );
  		}
  		
  		/* Write out the message */
  		outWriter	= new FileOutputStream( outFile );
  		this.message.writeTo( outWriter );
  		outWriter.flush();
  		outWriter.close();

  		return new cfStringData( outFile.toString() );
  		
  	}catch(Exception e){
      return cfStringData.EMPTY_STRING;
    }finally{
    	if ( outWriter != null ){
    		try {
					outWriter.close();
				} catch (IOException ignoreCloseException) {}
    	}
    }
  }
  
  
  public cfArrayData getBodyParts(){
    try{
      cfArrayData arr = cfArrayData.createArray( 1 );

      if ( message.isMimeType("multipart/*") ){

        Multipart mp  = (Multipart)message.getContent();
        int count     = mp.getCount();
        for (int i = 0; i < count; i++)
          arr.addElement( new BlueDragonPartWrapper(mp.getBodyPart(i)) );

      }else{
        arr.addElement( new BlueDragonPartWrapper( (Part)message ) );
      }

      return arr;
    }catch(Exception e){
      return cfArrayData.createArray( 1 );
    }
  }
  
  
  
  public cfArrayData getAllBodyParts(){
    try{
      cfArrayData arr = cfArrayData.createArray( 1 );

      ArrayList<BlueDragonPartWrapper>	list = new ArrayList<BlueDragonPartWrapper>();
      getParts( message, list );
      
      Iterator<BlueDragonPartWrapper> it = list.iterator();
      while ( it.hasNext() )
      	arr.addElement( it.next() );

      return arr;
    }catch(Exception e){
      return cfArrayData.createArray( 1 );
    }
  }

  
  
  /*
   * This method flattens out all the parts and puts them in list
   * This method is also recursable
   */
  private void	getParts( Part part, List<BlueDragonPartWrapper> list ) throws MessagingException, IOException{
  	if ( part.isMimeType("multipart/*") ){

  		Multipart mp  = (Multipart)part.getContent();
      int count     = mp.getCount();
      for (int i = 0; i < count; i++)
      	getParts( mp.getBodyPart(i), list );

    }else{
      list.add( new BlueDragonPartWrapper( part ) );
    }
  }
  
  
  /*
   * This returns the actual email addresses this mail was delivered to;
   * it need not necessarily be the emails in the official headers.
   * 
   * For example [BCC] emails will be here
   */
  public cfArrayData getRecipients(){
    try{
      cfArrayData arr = cfArrayData.createArray( 1 );
 
      if ( mail != null ){
	      Iterator c = mail.getRecipients().iterator();
	      while ( c.hasNext() ){
	        MailAddress ma = (MailAddress)c.next();
	        
	        arr.addElement( new cfStringData( ma.toString() ) );
	      }      
      }
      
      return arr;
    }catch(Exception e){
      return cfArrayData.createArray( 1 );
    }
  }
  
  /*
   * This is the actual address the mail was delivered from
   * in the "MAIL FROM" exchange.  This may not be the same
   * as the "From" in the email headers
   */
  public cfStringData getMailFrom(){
    try{
    	if ( mail != null )
    		return new cfStringData( mail.getSender().toString() ); 
    }catch(Exception e){}
    return cfStringData.EMPTY_STRING;
  }
  
  /*
   * This is the IP address to which the client connected from
   */
  public cfStringData getIP(){
    try{
    	if ( mail != null )
    		return new cfStringData( mail.getRemoteAddr() ); 
    }catch(Exception e){}
    return cfStringData.EMPTY_STRING;
  }
  
  public cfArrayData getHeaders(){
    try{
      cfArrayData arr = cfArrayData.createArray( 1 );
      
      Enumeration e = message.getAllHeaders();
      while (e.hasMoreElements()){
        Header hdr = (Header)e.nextElement();
        arr.addElement( new cfStringData( hdr.getName() ) );
      }

      return arr;
    }catch(Exception e){
      return cfArrayData.createArray( 1 );
    }
  }
  
  public cfArrayData getToList(){
    try {
      return getAddresses( message.getRecipients( Message.RecipientType.TO ));
    } catch (MessagingException e) {
      return cfArrayData.createArray( 1 );
    }
  }
  
  public cfArrayData getFromList(){
    try {
      return getAddresses( message.getFrom() );
    } catch (MessagingException e) {
      return cfArrayData.createArray( 1 );
    }
  }
  
  public cfArrayData getReplyToList(){
    try {
      return getAddresses( message.getReplyTo() );
    } catch (MessagingException e) {
      return cfArrayData.createArray( 1 );
    }
  }
  
  public cfArrayData getCcList(){
    try {
      return getAddresses( message.getRecipients( Message.RecipientType.CC ));
    } catch (MessagingException e) {
      return cfArrayData.createArray( 1 );
    }
  }
  
  private cfArrayData getAddresses( Address[] a ){
    try{
      cfArrayData arr = cfArrayData.createArray( 1 );
      for ( int x=0; x < a.length; x++ ){
        InternetAddress address = (InternetAddress)a[x];
        cfStructData s = new cfStructData();
        s.setData("name", new cfStringData(address.getPersonal()));
        s.setData("email", new cfStringData(address.getAddress()));
        arr.addElement( s );
      }

      return arr;
    }catch(Exception e){
      return cfArrayData.createArray( 1 );
    }
  }
  
  public cfStringData getHeader( String hdr ){
    try{
      String[] values = message.getHeader(hdr);
      String value = "";
      for ( int x=0; x < values.length; x++ ){
        value += value + values[x];
        if ( x < values.length-1 )
          value += ";";
      }

      return new cfStringData( value );
    }catch(Exception e){
      return cfStringData.EMPTY_STRING;
    }
  }

  public cfDateData getSentDate(){
    try{
      return new cfDateData( message.getSentDate() );
    }catch(Exception e){
      return new cfDateData( new Date() );
    }
  }
  
  public cfDateData getReceivedDate(){
    try{
      return new cfDateData( message.getReceivedDate() );
    }catch(Exception e){
      return new cfDateData( new Date() );
    }
  }
  
  public cfStringData getSubject(){
    try{
      return new cfStringData( message.getSubject() );
    }catch(Exception e){
      return cfStringData.EMPTY_STRING;
    }
  }
  
  public cfStringData getMessageId(){
    try{
      return new cfStringData( message.getMessageID() );
    }catch(Exception e){
      return cfStringData.EMPTY_STRING;
    }
  }

  public cfNumberData getSize(){
    try{
      return new cfNumberData( message.getSize() );
    }catch(Exception e){
      return new cfNumberData(0);
    }
  }

	public MimeMessage getMessage() {
		return message;
	}
}
