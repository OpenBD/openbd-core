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

import javax.mail.Folder;
import javax.mail.MessagingException;

import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
 
public class cfMailFolderData extends Object {
	
	public cfMailFolderData(){}

	public cfQueryResultData listFolder( cfImapConnection imapConnection, String rootFolder) throws cfmRunTimeException {
		
		cfQueryResultData query	= new cfQueryResultData( new String[]{"fullname","name","totalmessages","unread","new"},"CFIMAP" );						

		try{
		
			Folder	folderToList;
	
			if ( rootFolder == null || rootFolder.length() == 0 )
				folderToList = imapConnection.mailStore.getDefaultFolder();
			else
				folderToList = imapConnection.mailStore.getFolder(rootFolder);
				
			if ( (folderToList.getType() & Folder.HOLDS_FOLDERS) != 0){
				Folder[] folderArray = folderToList.list();
				
				query.addRow( folderArray.length );				
				
				for (int i = 0; i < folderArray.length; i++)
					folderRow( query, i+1, folderArray[i] );
			}

		}catch(Exception E){}
		
		return query;
	}
  
	public cfQueryResultData listAllFolders( cfImapConnection imapConnection) throws cfmRunTimeException {
		cfQueryResultData query	= new cfQueryResultData( new String[]{"fullname","name","totalmessages","unread","new"},"CFIMAP" );						
		
		try{
			Folder	folderToList;
			folderToList = imapConnection.mailStore.getDefaultFolder();

			if ( (folderToList.getType() & Folder.HOLDS_FOLDERS) != 0){
				Folder[] folderArray = folderToList.list("*");
				query.addRow( folderArray.length );				
				
				for (int i = 0; i < folderArray.length; i++)
					folderRow( query, i+1, folderArray[i] );
			}

		}catch(Exception E){}
		
		return query;
	}
  	
 	private static void folderRow( cfQueryResultData query, int Row, Folder thisFolder ) throws MessagingException {
		query.setCell( Row, 1, new cfStringData( thisFolder.getFullName() ) );
		query.setCell( Row, 2, new cfStringData( thisFolder.getName()  ) );

		// Invoking the getXXXCount() messages on a closed folder can be an expensive
		// operation so if the folder is closed then open it read only.
		// NOTE: refer to the API docs for javax.mail.Folder
		if ( !thisFolder.isOpen() )
			thisFolder.open( Folder.READ_ONLY );
		
		try{
			query.setCell( Row, 3, new cfNumberData( thisFolder.getMessageCount() ) );
		}catch(Exception E){
			query.setCell( Row, 3, new cfNumberData( -1 ) );
		}
		
		try{
			query.setCell( Row, 4, new cfNumberData( thisFolder.getUnreadMessageCount() ) );
		}catch(Exception E){
			query.setCell( Row, 4, new cfNumberData( -1 ) );
		}
		
		try{
			query.setCell( Row, 5, new cfNumberData( thisFolder.getNewMessageCount() ) );
		}catch(Exception E){
			query.setCell( Row, 5, new cfNumberData( -1 ) );
		}

		// Close the folder
		thisFolder.close( false );
	}

}

