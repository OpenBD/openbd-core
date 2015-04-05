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
 *  $Id: MailDeliverFunction.java 1755 2011-11-01 10:04:32Z alan $
 */
package org.alanwilliamson.openbd.plugin.cfsmtp;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.bluedragon.platform.SmtpInterface;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.mail.cfMAIL;
import com.naryx.tagfusion.expression.function.functionBase;


public class MailDeliverFunction extends functionBase {
	private static final long	serialVersionUID	= 1L;

	public MailDeliverFunction() {
		min = 1; max = 8;
		setNamedParams( new String[]{ "mail", "server", "usetls", "usessl", "from", "to", "cc", "bcc" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"Mail object",
			"The server to which the mail will be delivered to. Can be in the format username:password@server:port",
			"Flag to determine if TLS should be used for delivery",
			"Flag to determine if SSL should be used for delivery",
			"Override the existing FROM field in the original mail object",
			"Override the existing TO field in the original mail object",
			"Override the existing CC field in the original mail object",
			"Override the existing BCC field in the original mail object"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"cfsmtp-plugin", 
				"This function writes the email out to disk", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		cfData	obj	= getNamedParam(argStruct, "mail", null );
		if ( obj == null )
			throwException(_session, "missing parameter 'mail'");
		
		if ( !(obj instanceof BlueDragonMailWrapper) )
			throwException(_session, "not the required type");
			
		BlueDragonMailWrapper	mailWrapper	= (BlueDragonMailWrapper)obj;

		String	from		= getNamedStringParam( argStruct, "from", null );
		String	server	= getNamedStringParam( argStruct, "server", null );
		String	to			= getNamedStringParam( argStruct, "to", null );
		String	cc			= getNamedStringParam( argStruct, "cc", null );
		String	bcc			= getNamedStringParam( argStruct, "bcc", null );
		
		MimeMessage	msg	= mailWrapper.getMessage();
		
		try{

			if ( from != null ){
				InternetAddress [] fromAddr = cfMAIL.getAddresses( from, SmtpInterface.DEFAULT_CHARSET );
				if ( fromAddr != null && fromAddr.length > 0 ){
		    	msg.setFrom( fromAddr[0] );	
		    }
			}
			
			
			if ( to != null ){
				Address[]	addrs	= cfMAIL.getAddresses( to, SmtpInterface.DEFAULT_CHARSET );
	      if ( addrs != null )
	      	msg.setRecipients( Message.RecipientType.TO, addrs );	
			}
			
			
			if ( cc != null ){
				Address[]	addrs	= cfMAIL.getAddresses( cc, SmtpInterface.DEFAULT_CHARSET );
	      if ( addrs != null )
	      	msg.setRecipients( Message.RecipientType.CC, addrs );	
			}
			
			
			if ( bcc != null ){
				Address[]	addrs	= cfMAIL.getAddresses( bcc, SmtpInterface.DEFAULT_CHARSET );
	      if ( addrs != null )
	      	msg.setRecipients( Message.RecipientType.BCC, addrs );	
			}

			
			if ( server != null ){
				// ssl/tls settings
				boolean useSSL = getNamedBooleanParam(argStruct, "usessl", false );
				boolean useTLS = getNamedBooleanParam(argStruct, "usetls", false );

				String ssl = useTLS ? "tls" : ( useSSL ? "ssl" : "none" );
				msg.setHeader("X-BlueDragon-SSL", ssl );
				
				String defaultPort = SmtpInterface.DEFAULT_SMTP_PORT;
				if ( useSSL ){
					defaultPort = SmtpInterface.DEFAULT_SMTPS_PORT;
				}
				
				msg.addHeader("X-BlueDragon-Server", getServerHeader(server,defaultPort) );
				
			}else{
				String ssl = cfEngine.thisPlatform.getSmtp().isUseTLS() ? "tls" : ( cfEngine.thisPlatform.getSmtp().isUseSSL() ? "ssl" : "none" );
				msg.setHeader("X-BlueDragon-SSL", ssl );
			}
			
			msg.setHeader("X-BlueDragon-timeout", String.valueOf( cfEngine.thisPlatform.getSmtp().getDefaultTimeout()*1000 )  );
			
			// Message construction done; lets pass it to the outgoing mail server
			cfEngine.thisPlatform.getSmtp().send( msg );

		} catch ( MessagingException e){
			throwException(_session, e.getMessage() );
		}

		return cfBooleanData.TRUE;
	}
	
	
	private String getServerHeader(String serverString, String defaultPort){
		String server, username, password, port;
		
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
			username	= "";
			password	= "";
			c1 = serverString.indexOf(":");
			if ( c1 == -1 ){
				server	= serverString;
				port		= defaultPort;
			}else{
				server	= serverString.substring(0,c1);
				port		= serverString.substring(c1+1);
			}
		}

		return username + ";" + password + ";" + server + ";" + port;
	}
}
