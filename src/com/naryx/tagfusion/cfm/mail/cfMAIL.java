/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  
 *  $Id: cfMAIL.java 2473 2015-01-12 10:52:50Z alan $
 */

package com.naryx.tagfusion.cfm.mail;

/*
 * This class handles the CFMAIL component of CFML.  It accepts a mail but doesn't
 * immediately deliver it.  Instead it passes it off to an email engine that will
 * come along and pick it up.
 */

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import com.bluedragon.platform.SmtpInterface;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfMAIL extends cfTag  implements Serializable{
  static final long serialVersionUID = 1;
 
	public boolean doesTagHaveEmbeddedPoundSigns() {
		return true;
	}

  @SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
  	return createInfo("remote", "Easily send MIME and PLAIN type emails");
  }
  
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
   			createAttInfo( "TO", "The email address to send the email to.  Multiple emails delimited by ; or ,. If using a query, this is the column name where the TO field is", 	"", true ),
   			createAttInfo( "CC", "The email address to cc send the email.  Multiple emails delimited by ; or ,. If using a query, this is the column name where the CC field is", 	"", false ),
   			createAttInfo( "BCC", "The email address to bcc send the email.  Multiple emails delimited by ; or ,. If using a query, this is the column name where the BCC field is", 	"", false ),
   			createAttInfo( "REPLYTO", "The email address for the REPLYTO field", 	"", false ),
   			createAttInfo( "FAILTO", "The email address for the Return-Path field", 	"", false ),
   			createAttInfo( "SUBJECT", "The subject of the email", 	"", true ),
   			createAttInfo( "MAILERID", "The X-MAILER value", 	"OpenBD", false ),
   			createAttInfo( "TYPE", "The type of email to send.  HTML or PLAIN", 	"plain", false ),
   			createAttInfo( "STARTROW", "If using a query, then this is the start of the row", 	"0", false ),
   			createAttInfo( "MAXROWS", "If using a query, then this is the maximum rows to send", 	"-all-", false ),
   			createAttInfo( "QUERY", "The query object containing the data.  For each row, an email will be generated", 	"", false ),
   			createAttInfo( "GROUP", "If using a query, then this is the GROUP'ing of the query", 	"", false ),
   			createAttInfo( "GROUPCASESENSITIVE", "If using a query, this is the GROUP sensitivity", 	"false", false ),
   			createAttInfo( "USESSL", "Set to true to send using SSL", 	"false", false ),
   			createAttInfo( "USETLS", "Set to true to send using TLS", 	"false", false ),
   			createAttInfo( "TIMEOUT", "How long to wait for the MTA to respond before returning an error", 	"", false ),
   			createAttInfo( "MIMEATTACH", "Attach a file to this email, using the file path", 	"", false ),
   			createAttInfo( "WRAPTEXT", "The number of columns to wrap the text to if using TYPE=PLAIN", 	"-do not-", false ),
   			createAttInfo( "SERVER", "The server list of MTA agents to deliver the email to. Can be in format: [user:password@]server[:port],[user:password@]server2[:port]", 	"", false ),
   			createAttInfo( "CHARSET", "The character set to use for this email", 	"", false ),
   			createAttInfo( "PRIORITY", "The priority value in the range 1-5 to use for this email", 	"", false ),
   			createAttInfo( "CALLBACK", "The name of the CFC that will have onMailSuccess/onMailFail callbacks", 	"", false ),
   			createAttInfo( "CALLBACKDATA", "A string that will be passed on through to the callback functions; can be any string", 	"", false ),
  	};
  }

	protected void defaultParameters( String _tag ) throws cfmBadFileException {
	  defaultAttribute( "MAILERID", cfEngine.PRODUCT_NAME + " <CFMAIL/CFIMAP> tag output" );
	  defaultAttribute( "TYPE",     "plain" );
 	  defaultAttribute( "STARTROW", 0 );
 	  defaultAttribute( "MAXROWS",  -1 );
 	  
 	  setFlushable( false );
 	 
		parseTagHeader( _tag );

		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;
		
		if ( !containsAttribute("TO") )
	    throw newBadFileException( "Missing TO", "Must contain at least a TO field" );
    else if (!containsAttribute("FROM") )
	    throw newBadFileException( "Missing FROM", "Must contain at least a FROM field" );	
    else if (!containsAttribute("SUBJECT") )
	    throw newBadFileException( "Missing SUBJECT", "Must contain at least a SUBJECT field" );
	}
  
	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

    if ( !containsAttribute(attributes,"TO") )
	    throw newBadFileException( "Missing TO", "Must contain at least a TO field" );	
    else if (!containsAttribute(attributes,"FROM") )
	    throw newBadFileException( "Missing FROM", "Must contain at least a FROM field" );	
    else if (!containsAttribute(attributes,"SUBJECT") )
	    throw newBadFileException( "Missing SUBJECT", "Must contain at least a SUBJECT field" );	
    
    return	attributes;
	}
	
  public String getEndMarker(){ 	return "</CFMAIL>";  }


	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);
		
    Properties props = new Properties();
 		props.put("mail.smtp.host", "127.0.0.1" );
 		props.put("mail.smtp.port", "25" );
 		String mailDomain = cfEngine.thisPlatform.getSmtp().getDomain(); 
 		if ( mailDomain != null && mailDomain.length() != 0 )
 			props.put("mail.host", mailDomain );
 		
    Session mailSession = Session.getInstance( props );
    
		if ( containsAttribute(attributes,"IMAPCONNECTION") ){
		
		  _Session.setDataBin( "MAILDATA", new cfMailData(_Session) );
		  MimeMessage msg = createMessage( attributes, _Session, mailSession, renderToString( _Session ).getOutput() );
		  _Session.deleteDataBin( "MAILDATA" );
		  
		  cfEngine.thisPlatform.getSmtp().send( msg );
		  saveMessageIMAPFolder( attributes, _Session, msg );
		
		} else if ( !containsAttribute(attributes,"QUERY") ){
			_Session.setDataBin( "MAILDATA", new cfMailData(_Session) );
			cfEngine.thisPlatform.getSmtp().send( createMessage( attributes, _Session, mailSession, renderToString( _Session ).getOutput() ) );
			_Session.deleteDataBin( "MAILDATA" );
		} else {
		
      String QUERY = getDynamic( attributes, _Session, "QUERY" ).getString();
			cfData queryDataTmp = runTime.runExpression( _Session, QUERY );
			cfQueryResultData queryData = null;
      	
			if ( queryDataTmp != null ){
				
				if ( queryDataTmp instanceof cfQueryResultData ){
				  queryData = (cfQueryResultData) queryDataTmp;
				}else{
					throw newRunTimeException( "The specified QUERY " + QUERY + " is not a valid query type." );
				}
				
				queryData.reset();
				
				int startRow	= getDynamic( attributes, _Session, "STARTROW" ).getInt();
				int maxRows		= getDynamic( attributes, _Session, "MAXROWS" ).getInt();
				int rowCount	= 0;
			
				_Session.pushQuery( queryData );
			
        // Check for the GROUPBY stuff
        if (containsAttribute(attributes,"GROUP")) {
          boolean groupCaseSensitive = false;
          if ( containsAttribute( attributes,"GROUPCASESENSITIVE" ) ){
            groupCaseSensitive = getDynamic(attributes, _Session, "GROUPCASESENSITIVE").getBoolean();
          }
          queryData.setGroupBy(getDynamic(attributes, _Session, "GROUP").getString(), groupCaseSensitive );
        }

				while ( queryData.nextRow() ){
					rowCount++;
				
					// Get ourselves up to the start
					if ( rowCount <= startRow )
						continue;
				
					// Send the data out
          _Session.setDataBin( "MAILDATA", new cfMailData(_Session) );
          cfEngine.thisPlatform.getSmtp().send( createMessage( attributes,_Session,mailSession, renderToString( _Session ).getOutput() ) );
          _Session.deleteDataBin("MAILDATA");
          
					// Determine if the maximum rows have been reached
					if ( maxRows != -1 && rowCount == maxRows )
						break;
				}
				
				queryData.finishQuery();
				_Session.popQuery();
  		}else{
				throw newRunTimeException( "The specified QUERY " + QUERY + " does not exist." );
			}
		}
		
		return cfTagReturnType.NORMAL;
 	} 

	protected MimeMessage createMessage( cfStructData attributes, cfSession _Session, Session mailSession, String _emailBody ) throws cfmRunTimeException {

		try{
			MimeMessage msg	= new BlueDragonMimeMessage( mailSession );

			// CHARSET defaults to the one specified in the admin. It can be overridden using
			// the CHARSET attribute
			String charset = SmtpInterface.DEFAULT_CHARSET;
			if ( containsAttribute( attributes, "CHARSET") ){
				charset = getDynamic( attributes, _Session, "CHARSET" ).getString();                      
			}else{
				cfEngine.thisPlatform.getSmtp();
				charset = cfEngine.getConfig().getString( "server.cfmail.charset", SmtpInterface.DEFAULT_CHARSET);
			}
      
			validateCharset( charset );

			boolean useGlobalServer = !containsAttribute( attributes, "SERVER" );
			
			// ssl/tls settings
			boolean useSSL = false;
			boolean useTLS = false;
			if ( useGlobalServer ){
				useSSL = cfEngine.thisPlatform.getSmtp().isUseSSL();
				useTLS = cfEngine.thisPlatform.getSmtp().isUseTLS();
			}else{
				if ( containsAttribute( attributes, "USESSL" ) ){
					useSSL = getDynamic( attributes, _Session, "USESSL" ).getBoolean();
				}
				if ( containsAttribute( attributes,"USETLS" ) ){
					useTLS = getDynamic( attributes, _Session, "USETLS" ).getBoolean();
				}
			}
			String ssl = useTLS ? "tls" : ( useSSL ? "ssl" : "none" );
			msg.setHeader("X-BlueDragon-SSL", ssl );

			// Do we have the call back
			if ( containsAttribute( attributes, "CALLBACK") ){
				msg.setHeader("X-BlueDragon-callback", getDynamic(attributes, _Session,"CALLBACK" ).getString() );
				
				if ( containsAttribute( attributes, "CALLBACKDATA") )
					msg.setHeader("X-BlueDragon-callbackdata", getDynamic(attributes, _Session,"CALLBACKDATA" ).getString() );
				
				cfApplicationData appData = _Session.getApplicationData();
				if ( appData != null )
					msg.setHeader("X-BlueDragon-appname", appData.getAppName() );
			}
				
			
			// Construct the server settings
			String defaultPort;
			if ( containsAttribute( attributes,"PORT" ) ){
				defaultPort	= getDynamic(attributes, _Session,"PORT" ).getString();
			}else if ( useGlobalServer ){
				defaultPort	= cfEngine.thisPlatform.getSmtp().getSmtpPort();
			}else if ( useSSL ){
				defaultPort = SmtpInterface.DEFAULT_SMTPS_PORT;
			}else{
				defaultPort = SmtpInterface.DEFAULT_SMTP_PORT;
			}
			 
			String defaultUser = "", defaultPassword = "";
			if ( containsAttribute( attributes,"USERNAME" ) && containsAttribute( attributes,"PASSWORD" ) ){
				defaultUser			= getDynamic(attributes, _Session,"USERNAME" ).getString();
				defaultPassword	= getDynamic(attributes, _Session,"PASSWORD" ).getString();					 
			}
			 
			
			List<String> servers;
			if ( !useGlobalServer )
				servers	= string.split( getDynamic( attributes, _Session,"SERVER" ).getString(), "," );
			else
				servers	= string.split( cfEngine.thisPlatform.getSmtp().getSmtpServer(), "," );
				
			for ( int i = 0; i < servers.size(); i++ ){
				String server, username, password, port;
				String serverString	= servers.get(i);
				
				int c1 = serverString.lastIndexOf("@");
				if ( c1 != -1 ){
					int c2 = serverString.indexOf(":", c1+1);
					if ( c2 == -1 ){ // no port specified
						server	= serverString.substring( c1+1 );
						port		= defaultPort;
					}else{
						server	= serverString.substring( c1+1, c2 );
						port		= serverString.substring( c2+1 );
					}
					c2	= serverString.indexOf(":");
					username	= serverString.substring( 0, c2 );
					password	= serverString.substring( c2+1, c1 );
				}else{
					username	= defaultUser;
					password	= defaultPassword;
					c1 = serverString.indexOf(":");
					if ( c1 == -1 ){
						server	= serverString;
						port		= defaultPort;
					}else{
						server	= serverString.substring(0,c1);
						port		= serverString.substring(c1+1);
					}
				}

				msg.addHeader("X-BlueDragon-Server",	username + ";" + password + ";" + server + ";" + port );
			}

			// set the timeout using the admin set default if the TIMEOUT attribute is not specified --------------------------------
			int timeout;
			if ( containsAttribute( attributes,"TIMEOUT" ) ){
				timeout = getDynamic(attributes, _Session,"TIMEOUT").getInt()*1000;
			}else{
				timeout = cfEngine.thisPlatform.getSmtp().getDefaultTimeout()*1000;
			}
      msg.setHeader("X-BlueDragon-timeout", String.valueOf( timeout )  );
      
      if ( containsAttribute( attributes, "PRIORITY" ) ){
      	int priority = getDynamic(attributes, _Session,"PRIORITY").getInt();
      	msg.setHeader( "X-Priority", String.valueOf( priority ) );
      }
      
      // Set the To, From, CC, BCC fields
      InternetAddress [] fromAddr = getAddresses( getDynamic( attributes, _Session, "FROM" ).getString(), charset );
  		
      // The JavaMail API only permits one valid FROM field so just send the first one
      if ( fromAddr != null && fromAddr.length > 0 ){
      	msg.setFrom( fromAddr[0] );	
      }
      
      
      // Set the TO, CC, BCC fields --------------------------------
      int emailSet = 0;
      Address[]	addrs;
      
    	if ( containsAttribute( attributes,"TO") ){
	      addrs	= getAddresses( getDynamic( attributes, _Session, "TO" ).getString().replace(';',','), charset );
	      if ( addrs != null ){
	      	msg.setRecipients( Message.RecipientType.TO, addrs );
	      	emailSet++;
	      }
    	}

     	if ( containsAttribute( attributes,"CC") ){
     		addrs	= getAddresses( getDynamic( attributes, _Session, "CC" ).getString().replace(';',','), charset );
     		if ( addrs != null ){
     			msg.setRecipients( Message.RecipientType.CC, addrs );
     			emailSet++;
     		}
     	}

     	if ( containsAttribute( attributes,"BCC") ){
     		addrs	= getAddresses( getDynamic( attributes, _Session, "BCC" ).getString().replace(';',','), charset );
     		if ( addrs != null ){
     			msg.setRecipients( Message.RecipientType.BCC, addrs );
     			emailSet++;
     		}
     	}

     	if ( emailSet == 0 ){
     		throw newRunTimeException( "Please specify at least one of the following TO, CC or BCC fields" );
     	}
     	
     	
     	//-- Set the subject fields, and header fields --------------------------------
      msg.setSubject( getDynamic( attributes, _Session, "SUBJECT" ).getString(), charset );
  		msg.setSentDate( new java.util.Date() );
  		
  		msg.setHeader( "X-Mailer", getDynamic( attributes, _Session, "MAILERID" ).getString() );
    	msg.setHeader( "X-Mailer-Version", cfEngine.PRODUCT_VERSION );
	    
      String TYPE = getDynamic( attributes, _Session, "TYPE" ).getString();

			// Set the REPLYTO
			if ( containsAttribute( attributes,"REPLYTO") )
				msg.setReplyTo( getAddresses( getDynamic( attributes, _Session, "REPLYTO" ).getString().replace(';',','), charset ) );

			// Set the FAILTO
			if ( containsAttribute( attributes,"FAILTO") ){
				InternetAddress [] failToAddr = getAddresses( getDynamic( attributes, _Session, "FAILTO" ).getString(), charset );
				if ( failToAddr != null && failToAddr.length > 0 ){
					msg.setHeader( "Return-Path",  failToAddr[0].toString() );
				}
			}

  		// Set the body
      cfMailData	mailData	= (cfMailData)_Session.getDataBin("MAILDATA");
			
			if ( containsAttribute( attributes,"MIMEATTACH" ) ){
				String filepath = getDynamic( attributes, _Session, "MIMEATTACH" ).getString();
				if ( !new File(filepath).exists() )
					throw newRunTimeException( "The file doesn't exist : " + filepath );
        String mimetype = FileTypeMap.getDefaultFileTypeMap().getContentType( filepath );
				mailData.addFile( filepath, mimetype );
			}

			int wrapColumn	= -1;
			if ( containsAttribute( attributes,"WRAPTEXT") ){
				wrapColumn	= getDynamic( attributes, _Session, "WRAPTEXT" ).getInt();
				if ( wrapColumn < 1 )
					wrapColumn = -1;
			}


			// If there is any multiparts then we render these instead			
      if ( mailData != null && (mailData.fileAttachmentListSize() > 0 || mailData.mailPartSize() > 0 || mailData.fileInlineListSize() > 0 ) ){

				MimeMultipart mailBody 		= new MimeMultipart("alternative");
        MimeBodyPart htmlPart = null;
        
				// Run through the mail parts
        Enumeration<cfMailPartData> MP	= mailData.getMailParts();
        
				while ( MP.hasMoreElements() ){
					MimeBodyPart mailPartBody = new MimeBodyPart();
					
					cfMailPartData	mailPart	= MP.nextElement();
					String multipartCharset = mailPart.getCharSet() != null ? mailPart.getCharSet() : charset;
					
					if ( mailPart.getMimeType().indexOf("html") != -1 ){
						mailPartBody.setContent( mailPart.getContent(-1), "text/html; charset=" + multipartCharset );
						if ( htmlPart == null ){ htmlPart = mailPartBody; }
          }else{
						mailPartBody.setText( mailPart.getContent(wrapColumn), multipartCharset );
          }
					mailBody.addBodyPart(mailPartBody);
        }
				
				// if there's only 1 multipart then the body of the tag should be
				// used as another multipart
				if ( mailData.mailPartSize() <= 1 ){
					MimeBodyPart mailPartBody = new MimeBodyPart();
					if ( TYPE.equalsIgnoreCase("html") ){
						mailPartBody.setContent( _emailBody, "text/html; charset=" + charset );
            if ( htmlPart == null ){ htmlPart = mailPartBody; }
          }else
	        	mailPartBody.setText( _emailBody, charset );
					mailBody.addBodyPart(mailPartBody);
				}
				
        // handle inline images
        // inline images are only useful when there is an html part
        if (mailData.fileInlineListSize() > 0 && htmlPart != null ){

          mailBody.removeBodyPart( htmlPart );
          
          Multipart related = new MimeMultipart("related");
          related.addBodyPart( htmlPart );
          
          addAttachments( mailData.getInlineFileList(), related, true );
          
          MimeBodyPart mbp = new MimeBodyPart();
          mbp.setContent( related );
          
          mailBody.addBodyPart( mbp );
        
        }

        // handle attachments
        if (mailData.fileAttachmentListSize() > 0){

					Multipart mixed = new MimeMultipart("mixed");
					
					if ( mailData.mailPartSize() > 0 ){
						MimeBodyPart wrap = new MimeBodyPart();
					  wrap.setContent( mailBody );
						mixed.addBodyPart(wrap);
        	}else{
						MimeBodyPart content = new MimeBodyPart();
						
						if ( TYPE.toLowerCase().endsWith("html") )
							content.setContent( _emailBody, "text/html; charset=" + charset );
						else
							content.setText( wrapText(_emailBody,wrapColumn), charset );
							
						mixed.addBodyPart( content );
        	}

					addAttachments( mailData.getFileAttachmentList(), mixed, false );
					msg.setContent(mixed);
				   
        }else
        	msg.setContent( mailBody );
        
      }else{
      	// Basic email      	
        if ( TYPE.toLowerCase().endsWith("html") )
          msg.setContent( _emailBody, "text/html; charset=" + charset );
        else{
					msg.setText( wrapText(_emailBody,wrapColumn), charset );
        }
      }
  		
  		// Set the necessary headers
  		if ( mailData != null && mailData.headerListSize() > 0 ){
 	  		Enumeration<String> FE	= mailData.getHeaderList();
   			while ( FE.hasMoreElements() ){
 		  		String header	= FE.nextElement();
 			  	String value	= header.substring( header.indexOf(",")+1 );
 				  header				= header.substring( 0, header.indexOf(",") );
   				msg.setHeader( header, value );
 	  		}
  		}
  		
  		return msg;
  		
    } catch (cfmRunTimeException RT ){
      throw RT;
    } catch( Exception EEE ){
			throw newRunTimeException( "CFMAIL Serious Error: " + EEE );
		}
	}
	
	
	private void addAttachments( Enumeration<fileAttachment> _attach, Multipart _parent, boolean _isInline ) throws MessagingException{
		while ( _attach.hasMoreElements() ){
			fileAttachment nextFile = _attach.nextElement();
			FileDataSource fds 			= new FileDataSource( nextFile.getFilepath() );
			String mimeType = nextFile.getMimetype();
			if (mimeType == null){
				// if mime type not supplied then auto detect
				mimeType = FileTypeMap.getDefaultFileTypeMap().getContentType(nextFile.getFilepath());
      }else{
				// since mime type is not null then it the mime type has been set manually therefore
				// we need to ensure that any call to the underlying FileDataSource.getFileTypeMap()
				// returns a FileTypeMap that will map to this type
				fds.setFileTypeMap(new CustomFileTypeMap(mimeType));
			}
			
			String filename = cleanName(fds.getName());
			try {
				// encode the filename to ensure that it contains US-ASCII characters only
				filename = MimeUtility.encodeText( filename, "utf-8", "b" );
			} catch (UnsupportedEncodingException e5) {
				// shouldn't occur
			}
		  MimeBodyPart mimeAttach	= new MimeBodyPart();
		  mimeAttach.setDataHandler( new DataHandler(fds) );
			mimeAttach.setFileName( filename );

			ContentType ct = new ContentType(mimeType);
			ct.setParameter("name", filename );
			
			mimeAttach.setHeader("Content-Type", ct.toString() );

			if ( _isInline ){
        mimeAttach.setDisposition( "inline" );
        mimeAttach.addHeader( "Content-id", "<" + nextFile.getContentid() + ">" );
			}
      
			_parent.addBodyPart(mimeAttach);
		}
	}
	
	private String wrapText(String text, int columnWidth){
		if ( columnWidth != -1 )
			return com.nary.util.string.wrap( text, columnWidth, false );
		else
			return text;
	}
	
	
	public static InternetAddress[] getAddresses( String addrStr, String charset ) {
		addrStr	= addrStr.replace( ';', ',' );
		InternetAddress addresses[];
		try{
			addresses = InternetAddress.parse( addrStr );
		}catch(Exception E){ return null; }
		
		if ( addresses.length == 0 )
			return null;
		else{
			try{
				for ( int i = 0; i < addresses.length; i++ ){
					addresses[i].setPersonal( addresses[i].getPersonal(), charset );
				}
			}catch( UnsupportedEncodingException e){} // just leave address as is
		}
		
		return addresses;
	}
	
	
	private String cleanName( String name ){
    name = name.replace( '\\','/' );
    int c1 = name.lastIndexOf( "/" );
    if ( c1 != -1 )
      return name.substring( c1+1 );
    else
      return name;
	}

	
  private void saveMessageIMAPFolder( cfStructData attributes, cfSession _Session, MimeMessage msg ) throws cfmRunTimeException {
  
    //--[ Get the IMAP connection
    cfImapConnection imapCon = cfIMAP.getCachedConnection( getDynamic( attributes, _Session, "IMAPCONNECTION" ).getString() );
    
    if ( imapCon != null ){
      imapCon.copyMessages( getDynamic( attributes, _Session, "IMAPFOLDER" ).getString(), msg );
    }
	}
  
  private void validateCharset( String _charset ) throws cfmRunTimeException{
    try{
		  String encoding = com.nary.util.Localization.convertCharSetToCharEncoding( _charset );
		  if ( encoding == null )
		  	throw newRunTimeException( "Invalid CHARSET specified [" + _charset + "]." );
		  else
		  	new String( new byte[0], encoding );
    }catch( UnsupportedEncodingException u ){
      throw newRunTimeException( "Invalid CHARSET specified [" + _charset + "]." );
    }
  }

	private class CustomFileTypeMap extends javax.activation.FileTypeMap {
		private String mimetype;

		public CustomFileTypeMap(String _type){
			mimetype = _type;
		}

		public String getContentType( File _f ){
			return mimetype;
		}

		public String getContentType( String _f ){
			return mimetype;
		}
	}
}