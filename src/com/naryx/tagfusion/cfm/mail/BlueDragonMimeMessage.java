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

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;


/*
 * Specific handling to retain the custom Message-ID that is sent
 * 
 * We look for it being set by overriding the setHeader() method
 * and if we spot it, we keep it, and then look for the updateMessageID()
 * call to re-establish it.
 * 
 * Clunky? yes.  But sadly this is way we have to do it for JavaMail
 */
public class BlueDragonMimeMessage extends MimeMessage {
	private String customMessageID	= null;
	
	public BlueDragonMimeMessage(Session session) {
		super(session);
	}

	public void setHeader(String name,String value) throws MessagingException{
		if ( name.equalsIgnoreCase("message-id") ){
			if ( customMessageID == null )
				customMessageID	= value;
			else
				return;
		}
		
		super.setHeader( name, value );
	}
	
	protected void updateMessageID() throws MessagingException {
		if ( customMessageID != null )
			setHeader("Message-ID", customMessageID );
		else
			super.updateMessageID();
	}
}
