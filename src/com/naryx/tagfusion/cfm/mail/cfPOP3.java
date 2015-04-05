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
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeUtility;

import org.apache.commons.io.IOUtils;

import com.nary.io.FileUtils;
import com.nary.util.UTF7Converter;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfPOP3 extends cfTag implements Serializable{
	
  static final long serialVersionUID = 1;
  
	private static String[] header={ "date", "from", "messagenumber", "replyto", "subject", "cc", "to", "messageid", "uid", "header"  };
	private static String[] headerAndBody={ "date", "from", "messagenumber", "replyto", "subject", "cc", "to", "messageid", "uid", "header", "body", "attachments", "attachmentfiles", "htmlbody", "textbody" };
	
	public void defaultParameters( String _tag ) throws cfmBadFileException {
		
		defaultAttribute("USERNAME",								"anonymous");
		defaultAttribute("PASSWORD",								"anonymous");
		defaultAttribute("ACTION", 									"GetHeaderOnly");
		defaultAttribute("TIMEOUT", 								60);
		defaultAttribute("STARTROW",								1);
		defaultAttribute("GENERATEUNIQUEFILENAMES", "NO");
		defaultAttribute("URIDIRECTORY", 						"NO");
		defaultAttribute("MESSAGENUMBER", 					"");
				
		parseTagHeader( _tag );		
    
    // ensure server has been specified
    if ( !containsAttribute( "SERVER") )
	    throw newBadFileException( "Missing Attribute", "You need to provide a SERVER" );
	}


  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		String action	= getDynamic( _Session, "ACTION" ).getString().toLowerCase();
		
		if ( action.equals("getheaderonly") )
			getMessagesFromServer( _Session, false );
		else if ( action.equals("getall") )
			getMessagesFromServer( _Session, true );
		else if ( action.equals("delete") )
			deleteMessagesFromServer( _Session );
		else
			throw newRunTimeException( "Invalid ACTION attribute:" + action );
		
		return cfTagReturnType.NORMAL;
	}
	
	//----------------------------------------------------------------
	
	private void getMessagesFromServer( cfSession _Session, boolean GetAll ) throws cfmRunTimeException {
		
  	if ( !containsAttribute( "NAME" ) ){
	    throw newRunTimeException( "Missing NAME attribute. You need to name this transaction" );
		}

		//--[ See if we are pulling attachments down
		File	attachmentDir = null;
		if ( containsAttribute("ATTACHMENTPATH") ){
			String attachment	= getDynamic( _Session, "ATTACHMENTPATH" ).getString();
			if ( getDynamic( _Session, "URIDIRECTORY" ).getBoolean() )
				attachmentDir = new File( FileUtils.getRealPath( _Session.REQ, attachment ) );
			else
				attachmentDir = new File( attachment );
			
			// Fixes issue #304 where attachment path should create directories if they do not exist
			if (!attachmentDir.exists())
				attachmentDir.mkdirs();
		}
		
		int startRow = getDynamic( _Session, "STARTROW" ).getInt();
		int maxRows = -1;
		if ( containsAttribute( "MAXROWS" ) ){
			maxRows = getDynamic( _Session, "MAXROWS" ).getInt();
	  	if ( maxRows < 1 ){
	  		throw newRunTimeException( "The value of MAXROWS must be 1 or greater." );
	  	}
		}
		
  	if ( startRow < 1 ){
  		throw newRunTimeException( "The value of STARTROW must be 1 or greater." );
  	}
		
		//--[ Get Message Store
		Store popStore = null;
		Folder popFolder = null;
		try
		{
			popStore = openConnection( _Session );
			
			//--[ Setup the query data
			cfQueryResultData		popData;
			if ( GetAll )
				popData	= new cfQueryResultData( headerAndBody, "CFPOP" );
			else
				popData	= new cfQueryResultData( header, "CFPOP" );
				
			//--[ Open up the Folder:INBOX and retrieve the headers
			popFolder = openFolder( _Session, popStore );
			
			//--[ Run through the Messages and populate the query
			readMessages( _Session, popFolder, popData, startRow, maxRows, GetAll, attachmentDir );		
			
			//--[ Add the query to the session
			_Session.setData( getDynamic(_Session,"NAME").getString(), popData );
		}		
		finally
		{
			closeFolder( popFolder );
			closeConnection( popStore );
		}
	}
	
	private void deleteMessagesFromServer( cfSession _Session ) throws cfmRunTimeException {

		//--[ Get Message Store
		Store popStore			= openConnection( _Session );

		//--[ Open up the Folder:INBOX and retrieve the headers
		Folder popFolder 	= openFolder( _Session, popStore );

    try{
      Message[] listOfMessages  = popFolder.getMessages();
      FetchProfile fProfile = new FetchProfile();
      fProfile.add( FetchProfile.Item.ENVELOPE );
      
      if ( containsAttribute( "UID" ) ){
        String[] messageUIDList = getMessageUIDList( getDynamic(_Session,"UID").getString() );
        fProfile.add(UIDFolder.FetchProfileItem.UID);
        popFolder.fetch( listOfMessages, fProfile );
  
        for ( int x=0; x < listOfMessages.length; x++ ){
          if ( messageUIDValid( messageUIDList, getMessageUID( popFolder, listOfMessages[x] ) ) ){
            listOfMessages[x].setFlag( Flags.Flag.DELETED, true );
          }
        }
        
      }else if ( containsAttribute( "MESSAGENUMBER" ) ){
    		int[] messageList	= getMessageList( getDynamic(_Session,"MESSAGENUMBER").getString() );
  			popFolder.fetch( listOfMessages, fProfile );
    
   			for ( int x=0; x < listOfMessages.length; x++ ){
   				if ( messageIDValid(messageList, listOfMessages[x].getMessageNumber() ) ){
   					listOfMessages[x].setFlag( Flags.Flag.DELETED, true );
   				}
   			}
   			
      }else{
  	    throw newRunTimeException( "Either MESSAGENUMBER or UID attribute must be specified when ACTION=DELETE" );
      }
    }catch(Exception ignore){}

    //--[ Close off the folder		
		closeFolder( popFolder );
		closeConnection( popStore );
	}
	
	//----------------------------------------------------------------

	private int getPort( cfSession _Session, boolean _secure ) throws dataNotSupportedException, cfmRunTimeException{
		int port;
		if ( containsAttribute( "PORT" ) ){
			port = getDynamic( _Session, "PORT" ).getInt();
		}else if ( _secure ){
			port = 995;
		}else{
			port = 110;
		}
		return port;
	}
	
	private boolean getSecure( cfSession _Session ) throws dataNotSupportedException, cfmRunTimeException{
		if ( containsAttribute( "SECURE" ) ){
			return getDynamic( _Session, "SECURE" ).getBoolean();
		}else{
			return false;
		}
	}
	//----------------------------------------------------------------
	
	private Store openConnection( cfSession _Session ) throws cfmRunTimeException	{
		String server	 = getDynamic( _Session, "SERVER" ).getString();
		boolean secure = getSecure( _Session );
		int	port			 = getPort( _Session, secure );
		String user    = getDynamic( _Session, "USERNAME" ).getString();
		String pass    = getDynamic( _Session, "PASSWORD" ).getString();
		
		Properties props = new Properties();
		String protocol = "pop3";
		if ( secure ){
			protocol = "pop3s";
		}
		
		props.put( "mail.transport.protocol", protocol );
		props.put( "mail." + protocol + ".port", String.valueOf( port ) );
		
		// This is the fix for bug NA#3156
  	    props.put("mail.mime.address.strict", "false");
		
		// With WebLogic Server 8.1sp4 and an IMAIL server, we're seeing that sometimes the first
		// attempt to connect after messages have been deleted fails with either a MessagingException 
		// of "Connection reset" or an AuthenticationFailedException of "EOF on socket".  To work
		// around this let's try 3 attempts to connect before giving up.
		for ( int numAttempts = 1; numAttempts < 4; numAttempts++ )
		{
			try{
				Session session 	= Session.getInstance( props );
				Store mailStore		= session.getStore( protocol );
				mailStore.connect( server, user, pass );
				return mailStore;
			}catch(Exception E){
				if ( numAttempts == 3 )
					throw newRunTimeException( E.getMessage() );
			}
		}
		
		// The code in the for loop should either return or throw an exception
		// so this code should never get hit.
		return null;
	}
	
	private static void closeConnection( Store store ) {
		try{
			if ( store != null ) store.close();
		}catch(Exception ignoreE){}
	}
	
	//----------------------------------------------------------------

	private Folder openFolder( cfSession _Session, Store popStore )  throws cfmRunTimeException	{
		try{
			Folder folder = popStore.getDefaultFolder();
			Folder popFolder = folder.getFolder("INBOX");
			popFolder.open( Folder.READ_WRITE );
			return popFolder;
		}catch(Exception E){
			throw newRunTimeException( E.getMessage() );
		}
	}

	private static void closeFolder( Folder popFolder ) {
		try{
			if ( popFolder != null ) popFolder.close( true );
		}catch(Exception ignoreE){}
	}

	//----------------------------------------------------------------
	
	private void readMessages( cfSession _Session, Folder popFolder, cfQueryResultData popData, int _start, int _max, boolean GetAll, File attachmentDir )  throws cfmRunTimeException	{
    try{
    	int maxRows = _max;
    	int startRow = _start;
    	String messageNumber = getDynamic(_Session,"MESSAGENUMBER").getString();
    	boolean containsUID = containsAttribute( "UID" );
    	boolean usingMessageNumber = messageNumber.length() > 0;
    	int msgCount = popFolder.getMessageCount();
    	
    	// if MAXROWS is not specified, or UID or MESSAGENUMBER is, then we want to get all the messages
    	if ( _max == -1 || containsUID || usingMessageNumber ){
    		maxRows = msgCount;
    	}
    	if ( containsUID || usingMessageNumber ){
    		startRow = 1;
    	}
    	
    	if ( msgCount != 0 && startRow > msgCount ){
    		throw newRunTimeException( "The value of STARTROW must not be greater than the total number of messages in the folder, " + popFolder.getMessageCount() + "." );
    	}
    	
    	Message[] listOfMessages;
    	if ( !usingMessageNumber ){
    		listOfMessages = popFolder.getMessages();
    	}else{
    		listOfMessages = popFolder.getMessages( getMessageList( messageNumber ) );
    	}
    	
      FetchProfile fProfile = new FetchProfile();
      fProfile.add( FetchProfile.Item.ENVELOPE );
      fProfile.add(UIDFolder.FetchProfileItem.UID);
      popFolder.fetch( listOfMessages, fProfile );
      
      if ( containsUID ){
        String[] messageUIDList = getMessageUIDList( getDynamic(_Session,"UID").getString() );
      
        for ( int x=0; x < listOfMessages.length; x++ ){
          if ( messageUIDList.length == 0 || messageUIDValid( messageUIDList, getMessageUID( popFolder, listOfMessages[x] ) ) ){
            populateMessage( _Session, listOfMessages[x], popData, GetAll, attachmentDir, popFolder );
          }
        }
      }else{
  			popFolder.fetch( listOfMessages, fProfile );
  			int end = startRow -1 + maxRows;
  			if ( end > listOfMessages.length ){
  				end = listOfMessages.length;
  			}
  			for ( int x=startRow-1; x < end; x++ ){
 					populateMessage( _Session, listOfMessages[x], popData, GetAll, attachmentDir, popFolder );
  			}
      }
    }catch(Exception E){
      if ( E.getMessage() != null )
          throw newRunTimeException( E.getMessage() );
      else
    	  throw newRunTimeException( E.toString() );
    }

  }

	private void populateMessage( cfSession _Session, Message thisMessage, cfQueryResultData popData, boolean GetAll, File attachmentDir, Folder _parent ) throws Exception {
    popData.addRow( 1 );
    int Row = popData.getNoRows();

    Date date = thisMessage.getSentDate();
		if ( date != null ){
      cfDateData cfdate = new cfDateData( date );
      cfdate.setPOPDate();
      popData.setCell( Row, 1, cfdate );    
    }else{
      popData.setCell( Row, 1, new cfStringData("") );    
    }

		popData.setCell( Row, 2, new cfStringData( formatAddress( thisMessage.getFrom() ) ) );		
		popData.setCell( Row, 3, new cfNumberData( thisMessage.getMessageNumber() ) );		
		popData.setCell( Row, 4, new cfStringData( formatAddress( thisMessage.getReplyTo() ) ) );		
		popData.setCell( Row, 5, new cfStringData( thisMessage.getSubject() ) );		
		popData.setCell( Row, 6, new cfStringData( formatAddress( thisMessage.getRecipients(Message.RecipientType.CC) ) ) );		
		popData.setCell( Row, 7, new cfStringData( formatAddress( thisMessage.getRecipients(Message.RecipientType.TO) ) ) );
		String [] msgid = thisMessage.getHeader( "Message-ID" );
    popData.setCell( Row, 8, new cfStringData( msgid != null ? msgid[0] : "" ) );
    popData.setCell( Row, 9, new cfStringData( getMessageUID( _parent, thisMessage ) ) );
    popData.setCell( Row, 10, new cfStringData( formatHeader( thisMessage ) ) );
    
		if ( GetAll ){
			retrieveBody( _Session, thisMessage, popData, Row, attachmentDir );
		}
	}

	private static String formatHeader( Message thisMessage ) throws Exception {
		Enumeration<Header> E	= thisMessage.getAllHeaders();
		StringBuilder	tmp	= new StringBuilder(128);
		while (E.hasMoreElements()){
			Header hdr = E.nextElement();
			tmp.append( hdr.getName() );
			tmp.append( ": " );
			tmp.append( hdr.getValue() );
			tmp.append( "\r\n" );
		}
		
		return tmp.toString();
	}
	
	private static String formatAddress( Address[] addList ){
		if ( addList == null || addList.length == 0 )
			return "";
		
		try {
			return MimeUtility.decodeText( javax.mail.internet.InternetAddress.toString( addList ) ).trim();
		} catch (UnsupportedEncodingException e) {
			return javax.mail.internet.InternetAddress.toString( addList );
		}
	}
	
	private void retrieveBody( cfSession _Session, Part Mess, cfQueryResultData popData, int Row, File attachmentDir ) throws Exception {
		
		if ( Mess.isMimeType("multipart/*") ){
			Multipart mp 	= (Multipart)Mess.getContent();
		 	int count 		= mp.getCount();
	    for (int i = 0; i < count; i++)
				retrieveBody( _Session, mp.getBodyPart(i), popData, Row, attachmentDir );
				
		}else{
			String filename = cfMailMessageData.getFilename( Mess );
			String dispos   = Mess.getDisposition();

			// note: text/enriched shouldn't be treated as a text part of the email (see bug #2227)
			if ( ( dispos == null || dispos.equalsIgnoreCase( Part.INLINE ) ) && Mess.isMimeType("text/*") && !Mess.isMimeType( "text/enriched" ) ) {
        String content;
        String contentType = Mess.getContentType().toLowerCase();
        // support aliases of UTF-7 - UTF7, UNICODE-1-1-UTF-7, csUnicode11UTF7, UNICODE-2-0-UTF-7
        if ( contentType.indexOf( "utf-7") != -1 || contentType.indexOf( "utf7") != -1){
          content = new String( UTF7Converter.convert( readInputStream( Mess ) ) );
        }else{
          try{
            content = (String) Mess.getContent();
          }catch( UnsupportedEncodingException e ){
            content = "Unable to retrieve message body due to UnsupportedEncodingException:" + e.getMessage();
          }catch( ClassCastException e ){
            // shouldn't happen but handle it gracefully
            content = new String( readInputStream( Mess ) );  
          }
        }
        
        if ( Mess.isMimeType( "text/html" ) ){
          popData.setCell( Row, 14, new cfStringData( content ) );
        }else if ( Mess.isMimeType( "text/plain" ) ){
          popData.setCell( Row, 15, new cfStringData( content ) );
        }
        popData.setCell( Row, 11, new cfStringData( content ) );
								
			} else if ( attachmentDir != null ){
				
				File outFile;
				if ( filename == null )
				  filename = "unknownfile";
				  
				outFile = getAttachedFilename( attachmentDir, filename, getDynamic( _Session, "GENERATEUNIQUEFILENAMES" ).getBoolean() );
				try{
					BufferedInputStream		in 	= new BufferedInputStream( Mess.getInputStream() );
					BufferedOutputStream	out	= new BufferedOutputStream( cfEngine.thisPlatform.getFileIO().getFileOutputStream( outFile ) );
					IOUtils.copy(in,out);

					out.flush();
					out.close();
					in.close();
					
					//--[ Update the fields
					cfStringData cell	= (cfStringData)popData.getCell( Row, 12 );
					if ( cell.getString().length() == 0 ) 
						cell = new cfStringData( filename );
					else
						cell = new cfStringData( cell.getString() + "," + filename );
					
					popData.setCell( Row, 12, cell );
					
					cell	= (cfStringData)popData.getCell( Row, 13 );
					if ( cell.getString().length() == 0 ) 
						cell = new cfStringData( outFile.toString() );
					else
						cell = new cfStringData( cell.getString() + "," + outFile.toString() );
					
					popData.setCell( Row, 13, cell );
					
				}catch(Exception ignoreException){}
			}
		}
	}
	
  private static byte[] readInputStream( Part _part ) throws IOException, MessagingException{
    InputStream ins = null;
    ByteArrayOutputStream bos = null;
    try{
      ins = _part.getInputStream();
      bos = new ByteArrayOutputStream();
      byte [] buffer = new byte[2048];
      int read;
      while ( (read = ins.read(buffer)) != -1 ){
        bos.write( buffer, 0, read );
      }
      return bos.toByteArray();
    }finally{
      if ( ins != null ) try{ ins.close(); }catch( IOException ignored ){}
      if ( bos != null ) try{ bos.close(); }catch( IOException ignored ){}
    }
  }
  
	private static File getAttachedFilename( File attachDIR, String filename, boolean unique ){
  	filename  = filename.replace(' ', '_').replace('/','_');
	  File fileN  = new File( attachDIR, filename );
	  
		if ( unique ){
	  	int x = 1;
	  	while ( fileN.exists() )
	    	fileN  = new File( attachDIR, (x++) + "_" + filename );
		}
	  
	  return fileN;
	}
	
	private static int[] getMessageList( String line ){
		if ( line == null )	return new int[0];
		
		List<String> tokens = string.split( line, "," );
		int [] list      		= new int[ tokens.size() ];
				
		int indx						= 0;
		for ( int i = 0; i < tokens.size(); i++ )
			list[ indx++ ]	= com.nary.util.string.convertToInteger( tokens.get(i).toString().trim(), 0 );

    return list;
	}

  private static String[] getMessageUIDList( String line ){
    if ( line == null ) return new String[0];
    
    List<String> tokens = string.split( line, "," );
		String [] list         = new String[ tokens.size() ];
        
    int indx            = 0;
    for ( int i = 0; i < tokens.size(); i++ )
      list[ indx++ ] = tokens.get(i).toString().trim();

    return list;
  }

	private static boolean messageIDValid( int[] list, int id ){
		for ( int x=0; x < list.length; x++ )
			if ( id == list[x] ) return true;
			
		return false;
	}

  private static boolean messageUIDValid( String[] list, String uid ){
    if ( uid.length() != 0 ){  
      for ( int x=0; x < list.length; x++ )
        if ( uid.equals(list[x]) ) return true;
    }  
    return false;
  }
  
  private static String getMessageUID( Folder _parent, Message _msg ) throws MessagingException{
	    String uid = null;
	    if ( _parent instanceof com.sun.mail.pop3.POP3Folder ) {
	      uid = ( (com.sun.mail.pop3.POP3Folder) _parent ).getUID( _msg );
	    }
	    
	    if ( uid == null ){
	      uid = "";
	    }
	    return uid;
	  }

}	
