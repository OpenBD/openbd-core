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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfMailFolderMessagesData extends Object {
	
	public cfMailFolderMessagesData(){}

	public cfQueryResultData listFolderMessages( cfImapConnection imapConnection, String rootFolder, int startRow, int totalMessages, boolean reverseOrder ) {
		cfQueryResultData query = new cfQueryResultData( new String[]{"subject","id","rxddate","sentdate","from","to","cc","bcc","size","lines","answered","deleted","draft","flagged","recent","seen"}, "CFIMAP" );

		try{
			Folder	folderToList;
			if ( rootFolder == null || rootFolder.length() == 0 )
				folderToList = imapConnection.mailStore.getDefaultFolder();
			else
				folderToList = imapConnection.mailStore.getFolder(rootFolder);
			
			if ( (folderToList.getType() & Folder.HOLDS_MESSAGES) != 0){
			
				if ( !folderToList.isOpen() )
					folderToList.open( Folder.READ_ONLY );
				
				Message[] messageArray;
				if ( startRow != -1 ){
					
					int folderCount = folderToList.getMessageCount();
					int start, end;
					if ( !reverseOrder ){
						start = startRow;
						if ( folderCount < (startRow+totalMessages-1) ){
							start = startRow;
							end = folderCount; 
						}else{
							end = startRow + totalMessages - 1;
						}
					}else{
						end = folderCount - startRow + 1;
						if ( folderCount < (startRow+totalMessages-1) ){ 
							start = 1;
						}else{
							start = folderCount - startRow - totalMessages + 2;
						}
						
					}
					
					messageArray = folderToList.getMessages( start, end );
					imapConnection.setTotalMessages( folderCount );
					
					
				}else{
					messageArray = folderToList.getMessages();
					imapConnection.setTotalMessages( messageArray.length );
				}

				// To improve performance, pre-fetch all of the message items
				// used by the CFIMAP list action. This will retrieve all of the
				// items for all of the messages with one single FETCH command.
				FetchProfile fp = new FetchProfile();
				fp.add(FetchProfile.Item.ENVELOPE);
				fp.add(FetchProfile.Item.FLAGS);
				fp.add(FetchProfile.Item.CONTENT_INFO);
				folderToList.fetch(messageArray, fp);
				List<Map<String, cfData>>	vectorMessages = new ArrayList<Map<String, cfData>>(messageArray.length);
				
				if ( reverseOrder ){
					int msgIndex = messageArray.length-1;
					for (int i = 0; i < messageArray.length; i++)
						vectorMessages.add( extractMessage( messageArray[msgIndex--] ) );
					
				}else{
					for (int i = 0; i < messageArray.length; i++)
						vectorMessages.add( extractMessage( messageArray[i] ) );
				}
				
				folderToList.close(false);
				query.populateQuery( vectorMessages );
			}

		}catch(Exception E){
			cfEngine.log( E.getMessage() );
			imapConnection.setStatus( false, E.getMessage() );
		}
		
		return query;
	}
  
  private Map<String, cfData> extractMessage( Message Mess ){
	  	Map<String, cfData> HT	= new FastMap<String, cfData>();
  	
		try{
			HT.put( "subject", 	new cfStringData( Mess.getSubject() ) );
			
			Folder parentFolder	= Mess.getFolder();
			if ( parentFolder instanceof UIDFolder )
				HT.put( "id",	new cfNumberData( ((UIDFolder)parentFolder).getUID( Mess) ) );
			else
				HT.put( "id",	new cfNumberData( Mess.getMessageNumber() ) );
			
			//--[ Get the FROM field
			Address[] from	= Mess.getFrom();
			if ( from != null && from.length > 0 ){
				cfStructData sdFrom	= new cfStructData();
				
				String name = ((InternetAddress)from[0]).getPersonal();
				if ( name != null )
  				sdFrom.setData( "name", new cfStringData( name ) );
				
				sdFrom.setData( "email", new cfStringData( ((InternetAddress)from[0]).getAddress() ) );
				HT.put( "from", sdFrom );
			}
			
			//--[ Get the TO/CC/BCC field
			cfArrayData	AD	= extractAddresses( Mess.getRecipients(Message.RecipientType.TO) );
			if ( AD != null )
				HT.put( "to", AD );
      else
        HT.put( "to", cfNullData.NULL );
        
		
			AD	= extractAddresses( Mess.getRecipients(Message.RecipientType.CC) );
			if ( AD != null )
				HT.put( "cc", AD );		
      else
        HT.put( "cc", cfNullData.NULL );

			AD	= extractAddresses( Mess.getRecipients(Message.RecipientType.BCC) );
			if ( AD != null )
				HT.put( "bcc", AD );
      else
        HT.put( "bcc", cfNullData.NULL );

			//--[ Set the flags
			HT.put( "answered",	cfBooleanData.getcfBooleanData( Mess.isSet( Flags.Flag.ANSWERED ) ) );
			HT.put( "deleted",	cfBooleanData.getcfBooleanData( Mess.isSet( Flags.Flag.DELETED ) ) );
			HT.put( "draft",		cfBooleanData.getcfBooleanData( Mess.isSet( Flags.Flag.DRAFT ) ) );
			HT.put( "flagged",	cfBooleanData.getcfBooleanData( Mess.isSet( Flags.Flag.FLAGGED ) ) );
			HT.put( "recent",		cfBooleanData.getcfBooleanData( Mess.isSet( Flags.Flag.RECENT ) ) );
			HT.put( "seen",			cfBooleanData.getcfBooleanData( Mess.isSet( Flags.Flag.SEEN ) ) );

			HT.put( "size",			new cfNumberData( Mess.getSize() ) );
			HT.put( "lines",		new cfNumberData( Mess.getLineCount() ) );

      Date DD = Mess.getReceivedDate();
      if ( DD == null )
  			HT.put( "rxddate", 	new cfDateData( System.currentTimeMillis() ) );
  	  else
  			HT.put( "rxddate", 	new cfDateData( DD.getTime() ) );


      DD = Mess.getSentDate();
      if ( DD == null )
  			HT.put( "sentdate", 	new cfDateData( System.currentTimeMillis() ) );
  	  else
  			HT.put( "sentdate", 	new cfDateData( DD.getTime() ) );

		}catch(Exception E){
		  com.nary.Debug.printStackTrace( E );
		}
  	
		return HT;  	  
  }
  
  private static cfArrayData extractAddresses( Address[] addresses ) throws cfmRunTimeException{
	  if ( addresses == null || addresses.length == 0 )
	  	return null;
	  
	  cfArrayData	AD	= cfArrayData.createArray(1);
	  
	  for ( int x=0; x < addresses.length; x++ ){
			cfStructData sdFrom	= new cfStructData();
			String name = ((InternetAddress)addresses[x]).getPersonal();
			if ( name != null )
  			sdFrom.setData( "name", new cfStringData( name ) );
  			
			sdFrom.setData( "email", new cfStringData( ((InternetAddress)addresses[x]).getAddress() ) );
			AD.addElement( sdFrom );
	  }
	  
  	return AD;
  }
  
}
