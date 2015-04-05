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

package com.naryx.tagfusion.cfm.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;

import javax.activation.MimeType;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import org.apache.commons.io.IOUtils;

import com.nary.util.UTF7Converter;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

 
public class cfMailMessageData extends cfStructData implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	public cfMailMessageData( cfSession _Session ){	super();	}

	public void getMessage( cfImapConnection imapConnection, String rootFolder, long messageID, String _attachURI, String _attachDIR ) throws cfmRunTimeException {
		try{
			Folder	folderToList;
	
			if ( rootFolder == null || rootFolder.length() == 0 )
				folderToList = imapConnection.mailStore.getDefaultFolder();
			else
				folderToList = imapConnection.mailStore.getFolder(rootFolder);
				
			if ( (folderToList.getType() & Folder.HOLDS_MESSAGES) != 0){
			
				if ( !folderToList.isOpen() )
					folderToList.open( Folder.READ_ONLY );

				boolean bResult = false;
				if ( folderToList instanceof UIDFolder )	
					bResult = extractMessage( ((UIDFolder)folderToList).getMessageByUID( messageID ), messageID, _attachURI, _attachDIR );
				else
					bResult = extractMessage( folderToList.getMessage( (int)messageID ), messageID, _attachURI, _attachDIR );
				
				if ( !bResult )	imapConnection.setStatus( false, "Message does not exist" );
				else imapConnection.setStatus( true, "" );
				
				folderToList.close(false);
			}

		}catch(Exception E){
			imapConnection.setStatus( false, E.getMessage() );
		}
	}
  
  private static cfArrayData extractAddresses( Address[] addresses ) throws cfmRunTimeException {
	  if ( addresses == null || addresses.length == 0 )
	  	return null;
	  
	  cfArrayData	AD	= cfArrayData.createArray(1);
	  
	  for ( int x=0; x < addresses.length; x++ ){
			cfStructData sdFrom	= new cfStructData();
			String name =  ((InternetAddress)addresses[x]).getPersonal();
			if ( name != null )
  			sdFrom.setData( "name", new cfStringData( name ) );
  			
			sdFrom.setData( "email", new cfStringData( ((InternetAddress)addresses[x]).getAddress() ) );
			AD.addElement( sdFrom );
	  }
	 	return AD;
  }
  
  private boolean extractMessage( Message Mess, long messageID, String attachURI, String attachDIR ){
		cfArrayData ADD	= cfArrayData.createArray(1);

		try{
		
			setData( "subject", 	new cfStringData( Mess.getSubject() ) );
			setData( "id",				new cfNumberData( messageID ) );
			
			//--- Pull out all the headers
			cfStructData	headers	= new cfStructData();
			Enumeration<Header> eH	= Mess.getAllHeaders();
			String headerKey;
			while (eH.hasMoreElements()){
				Header hdr	= eH.nextElement();
				
				headerKey = hdr.getName().replace('-','_').toLowerCase();
				if ( headers.containsKey(headerKey) ){
					headers.setData( headerKey, new cfStringData( headers.getData(headerKey).toString() + ";" + hdr.getValue() ) );
				}else
					headers.setData( headerKey, new cfStringData( hdr.getValue() ) );
			}
			
			setData( "headers",  headers );

			// Get the Date
      Date DD = Mess.getReceivedDate();
      if ( DD == null )
  			setData( "rxddate", 	new cfDateData( System.currentTimeMillis() ) );
  	  else
  			setData( "rxddate", 	new cfDateData( DD.getTime() ) );


      DD = Mess.getSentDate();
      if ( DD == null )
  			setData( "sentdate", 	new cfDateData( System.currentTimeMillis() ) );
  	  else
  			setData( "sentdate", 	new cfDateData( DD.getTime() ) );
			
			// Get the FROM field
			Address[] from	= Mess.getFrom();
			if ( from != null && from.length > 0 ){
				cfStructData sdFrom	= new cfStructData();
				String name =  ((InternetAddress)from[0]).getPersonal();
				if ( name != null )
  				sdFrom.setData( "name", new cfStringData(name) );
  				
				sdFrom.setData( "email", new cfStringData( ((InternetAddress)from[0]).getAddress() ) );
				setData( "from", sdFrom );
			}
			
			//--[ Get the TO/CC/BCC field
			cfArrayData	AD	= extractAddresses( Mess.getRecipients(Message.RecipientType.TO) );
			if ( AD != null )
				setData( "to", AD );
		
			AD	= extractAddresses( Mess.getRecipients(Message.RecipientType.CC) );
			if ( AD != null )
				setData( "cc", AD );		

			AD	= extractAddresses( Mess.getRecipients(Message.RecipientType.BCC) );
			if ( AD != null )
				setData( "bcc", AD );
				
			AD	= extractAddresses( Mess.getReplyTo() );
			if ( AD != null )
				setData( "replyto", AD );
				
			//--[ Set the flags
			setData( "answered",	cfBooleanData.getcfBooleanData( Mess.isSet( Flags.Flag.ANSWERED ) ) );
			setData( "deleted",		cfBooleanData.getcfBooleanData( Mess.isSet( Flags.Flag.DELETED ) ) );
			setData( "draft",			cfBooleanData.getcfBooleanData( Mess.isSet( Flags.Flag.DRAFT ) ) );
			setData( "flagged",		cfBooleanData.getcfBooleanData( Mess.isSet( Flags.Flag.FLAGGED ) ) );
			setData( "recent",		cfBooleanData.getcfBooleanData( Mess.isSet( Flags.Flag.RECENT ) ) );
			setData( "seen",			cfBooleanData.getcfBooleanData( Mess.isSet( Flags.Flag.SEEN ) ) );

			setData( "size",			new cfNumberData( Mess.getSize() ) );
				setData( "lines",			new cfNumberData( Mess.getLineCount() ) );
			
			String tmp	= Mess.getContentType();
			if ( tmp.indexOf(";") != -1 )
				tmp	= tmp.substring( 0, tmp.indexOf(";") );

			setData( "mimetype", new cfStringData( tmp ) );
			
			// Get the body of the email
			extractBody( Mess, ADD, attachURI, attachDIR );
		
		}catch(Exception E){
			return false;
		}
  	
		setData( "body",	ADD );
		return true;
  }
  
  
 	private void extractBody( Part Mess, cfArrayData AD, String attachURI, String attachDIR  ) throws Exception {
		
		if ( Mess.isMimeType("multipart/*") ){
		
			Multipart mp 	= (Multipart)Mess.getContent();
		 	int count 		= mp.getCount();
	    for (int i = 0; i < count; i++)
				extractBody( mp.getBodyPart(i), AD, attachURI, attachDIR );
		
		} else {
		
			cfStructData sd	= new cfStructData();

			String tmp	= Mess.getContentType();
			if ( tmp.indexOf(";") != -1 )
				tmp	= tmp.substring( 0, tmp.indexOf(";") );

			sd.setData( "mimetype",	new cfStringData( tmp ) );

			String filename = getFilename( Mess );
			String dispos   = getDisposition( Mess );
			
      MimeType messtype = new MimeType( tmp ); 
      // Note that we can't use Mess.isMimeType() here due to bug #2080
			if ( ( dispos == null || dispos.equalsIgnoreCase(Part.INLINE) ) && messtype.match("text/*") ) {
				
        Object content;
        String contentType = Mess.getContentType().toLowerCase();
        // support aliases of UTF-7 - UTF7, UNICODE-1-1-UTF-7, csUnicode11UTF7, UNICODE-2-0-UTF-7
        if ( contentType.indexOf( "utf-7") != -1 || contentType.indexOf( "utf7") != -1){
          InputStream ins = Mess.getInputStream();
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          IOUtils.copy(ins,bos);
          content = new String( UTF7Converter.convert( bos.toByteArray() ) );
        }else{
        	try{
        		content = Mess.getContent();
        	}catch( UnsupportedEncodingException e ){
        		content = Mess.getInputStream();
        	}catch( IOException ioe ){
        		// This will happen on BD/Java when the attachment has no content
        		// NOTE: this is the fix for bug NA#3198.
        		content = "";
        	}
        }
        
        if ( content instanceof InputStream ){
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          IOUtils.copy((InputStream) content,bos);
          sd.setData( "content",  new cfStringData( new String( bos.toByteArray() ) ) );
        }else{
          sd.setData( "content",  new cfStringData( content.toString() ) );
        }

        sd.setData( "file",			cfBooleanData.FALSE );
				sd.setData( "filename", new cfStringData( filename == null ? "" : filename ) );
				
			} else if ( attachDIR != null ){

				sd.setData( "content",	new cfStringData("") );
				
				if ( filename == null || filename.length() == 0 )
				  filename = "unknownfile";
				  
				filename = getAttachedFilename( attachDIR, filename );
				
				//--[ An attachment, save it out to disk
				try{
					BufferedInputStream	in  = new BufferedInputStream( Mess.getInputStream() );
					BufferedOutputStream	out	= new BufferedOutputStream( cfEngine.thisPlatform.getFileIO().getFileOutputStream( new File(attachDIR + filename)  ) );
			        IOUtils.copy(in,out);

					out.flush();
					out.close();
					in.close();

					sd.setData( "file",			cfBooleanData.TRUE );
					sd.setData( "filename", new cfStringData( filename ) );
					if ( attachURI.charAt( attachURI.length()-1 ) != '/' )
						sd.setData( "url",      new cfStringData( attachURI + '/' + filename ) );
					else
						sd.setData( "url",      new cfStringData( attachURI + filename ) );
					sd.setData( "size",     new cfNumberData( (int)new File( attachDIR + filename ).length() ) );

				}catch(Exception ignoreException){
					// NOTE: this could happen when we don't have permission to write to the specified directory
					//       so let's log an error message to make this easier to debug.
					cfEngine.log( "-] Failed to save attachment to " + attachDIR + filename + ", exception=[" + ignoreException.toString() + "]" );
				}

			} else {
  			sd.setData( "file",			cfBooleanData.FALSE );
  			sd.setData( "content",	new cfStringData("") );
			}

			AD.addElement( sd );
		}
	}
 	
 	
 	public static String getDisposition( Part _mess ) throws MessagingException{
 		String dispos = _mess.getDisposition();
		// getDisposition isn't 100% reliable 
 		if ( dispos == null ){
 			String [] disposHdr = _mess.getHeader( "Content-Disposition" );
 			if ( disposHdr != null && disposHdr.length > 0 && disposHdr[0].startsWith( Part.ATTACHMENT ) ){
 				return Part.ATTACHMENT;
 			}
 		}
 		
 		return dispos;
 	}
 	
 	
  public static String getFilename( Part Mess ) throws MessagingException{
		// Part.getFileName() doesn't take into account any encoding that may have been 
		// applied to the filename so in order to obtain the correct filename we
		// need to retrieve it from the Content-Disposition

		String [] contentType = Mess.getHeader( "Content-Disposition" );
  	if ( contentType != null && contentType.length > 0 ){
  		int nameStartIndx = contentType[0].indexOf( "filename=\"" );
  		if ( nameStartIndx != -1 ){
  			String filename = contentType[0].substring( nameStartIndx+10, contentType[0].indexOf( '\"', nameStartIndx+10 ) );
  			try {
					filename = MimeUtility.decodeText( filename );
					return filename;
				} catch (UnsupportedEncodingException e) {}
  		}  		
  	}

  	// couldn't determine it using the above, so fall back to more reliable but 
		// less correct option
  	return Mess.getFileName();
  }

	
	private static String getAttachedFilename( String attachDIR, String filename ){
		try {
			filename = MimeUtility.decodeText( filename );
		} catch (UnsupportedEncodingException e) {}

		filename  = filename.replace(' ', '_').replace('/','_');
	  File fileN  = new File( attachDIR, filename );
	  
	  int x = 1;
	  while ( fileN.exists() )
	    fileN  = new File( attachDIR, (x++) + "_" + filename );
	  
	  return fileN.getName();
	}
}
