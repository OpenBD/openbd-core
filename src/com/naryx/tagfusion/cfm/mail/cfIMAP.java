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

import java.io.Serializable;
import java.util.List;

import javax.mail.Flags;

import com.nary.io.FileUtils;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

/*
 * This class provides the implementation of the CFIMAP tag.  It is a wrapper for the 
 * underlying JavaMail API
 */

public class cfIMAP extends cfTag implements Serializable{

  static final long serialVersionUID = 1;

	private static imapManager	iManager;

	//--------------------------------------------------------------
	//--[ Listener Management
	//--------------------------------------------------------------

  public static void init( xmlCFML configFile ) {
		iManager = new imapManager();
	}

	//--------------------------------------------------------------
	
  protected void defaultParameters( String _tag ) throws cfmBadFileException {
    defaultAttribute( "SERVICE", "IMAP" );
    parseTagHeader( _tag );

		if ( !containsAttribute("ACTION") )
    	throw newBadFileException( "Missing ACTION", "You need to provide a ACTION" );
	}


	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
	
		//--[ Need to get a connection sorted out
		String ACTION	= getConstant( "ACTION" );
		cfImapConnection imapConnection;
		
		if ( ACTION.equalsIgnoreCase("OPEN") ){
			imapConnection = openConnection( _Session );
			
			//--[ If the CONNECTION has been specified then cache it
			if ( containsAttribute("CONNECTION") )
				iManager.cacheConnection( getDynamic( _Session, "CONNECTION" ).getString(), imapConnection );
			
			_Session.setData( "IMAP", imapConnection );

		} else if ( ACTION.equalsIgnoreCase("CLOSE") ){

			if ( containsAttribute("CONNECTION") )
				iManager.closeCachedConnection( getDynamic( _Session, "CONNECTION" ).getString() );

		} else {
		
			imapConnection = getConnection( _Session );
			
			if ( ACTION.equalsIgnoreCase("LISTFOLDER") ){
				listFolder( _Session, imapConnection );
			} else if ( ACTION.equalsIgnoreCase("LISTALLFOLDERS") ){
				listAllFolders( _Session, imapConnection );
			} else if ( ACTION.equalsIgnoreCase("LISTMAIL") ){
				listFolderMessages( _Session, imapConnection );
			} else if ( ACTION.equalsIgnoreCase("READMAIL") ){
				readMessage( _Session, imapConnection );
			} else if ( ACTION.equalsIgnoreCase("MARKREAD") ){
				markMessagesRead( _Session, imapConnection );
			} else if ( ACTION.equalsIgnoreCase("DELETEMAIL") ){
				markMessagesDelete( _Session, imapConnection );
			} else if ( ACTION.equalsIgnoreCase("MOVEMAIL") ){
				moveMessages( _Session, imapConnection );
			} else if ( ACTION.equalsIgnoreCase("CREATEFOLDER") ){
				createFolder( _Session, imapConnection );
			} else if ( ACTION.equalsIgnoreCase("DELETEFOLDER") ){
				deleteFolder( _Session, imapConnection );
			} else if ( ACTION.equalsIgnoreCase("RENAMEFOLDER") ){
				renameFolder( _Session, imapConnection );
      } else if ( ACTION.equalsIgnoreCase("SETFLAGS") ){
        setFlags( _Session, imapConnection );
      }

			_Session.setData( "IMAP", imapConnection );
		}
		
		return cfTagReturnType.NORMAL;
	}
	
	
	public static cfImapConnection getCachedConnection( String connectionKey ){
		return iManager.getCacheConnection( connectionKey );
	}


	private cfImapConnection getConnection( cfSession _Session ) throws cfmRunTimeException {
		if ( !containsAttribute("CONNECTION") )
			return openConnection( _Session );
		else{
			String connectionKey 			= getDynamic( _Session, "CONNECTION" ).getString();
			cfImapConnection imapCon 	= iManager.getCacheConnection( connectionKey );
			if ( imapCon == null ){
				imapCon = openConnection( _Session );
				iManager.cacheConnection( connectionKey, imapCon );
			}
			
			return imapCon;
		}
	}
	
	
	private cfImapConnection openConnection( cfSession _Session ) throws cfmRunTimeException {
		
		//--[ Check for the necessary parameters
		if ( !containsAttribute("SERVER") )
			throw newRunTimeException( "Missing the SERVER attribute" );
		if ( !containsAttribute("USERNAME") )
			throw newRunTimeException( "Missing the USERNAME attribute" );
		if ( !containsAttribute("PASSWORD") )
			throw newRunTimeException( "Missing the PASSWORD attribute" );
			
		//--[ Attempt to create the parameter
		return new cfImapConnection( getDynamic( _Session, "SERVER" ),
                              	 getDynamic( _Session, "SERVICE" ),
																 getDynamic( _Session, "USERNAME" ),
																 getDynamic( _Session, "PASSWORD" ) );
	}
	
	
	private void listFolder( cfSession _Session, cfImapConnection imapConnection ) throws cfmRunTimeException {

		if ( !containsAttribute("NAME") )
			throw newRunTimeException( "Missing the NAME attribute" );

		String rootFolder	= null;
		if ( containsAttribute("FOLDER") )
			rootFolder = getDynamic( _Session, "FOLDER" ).getString();
		
		String name				= getDynamic( _Session, "NAME" ).getString();
		cfMailFolderData	cfMFD	= new cfMailFolderData();
		_Session.setData( name, cfMFD.listFolder( imapConnection, rootFolder ) );
	}


	private void listAllFolders( cfSession _Session, cfImapConnection imapConnection ) throws cfmRunTimeException {

		if ( !containsAttribute("NAME") )
			throw newRunTimeException( "Missing the NAME attribute" );

		String name				= getDynamic( _Session, "NAME" ).getString();
		cfMailFolderData	cfMFD	= new cfMailFolderData();
		_Session.setData( name, cfMFD.listAllFolders( imapConnection ) );
	}


	
	private void listFolderMessages( cfSession _Session, cfImapConnection imapConnection ) throws cfmRunTimeException {

		if ( !containsAttribute("NAME") )
			throw newRunTimeException( "Missing the NAME attribute" );

		if ( !containsAttribute("FOLDER") )
			throw newRunTimeException( "Missing the FOLDER attribute" );

		String name			= getDynamic( _Session, "NAME" ).getString();
		String rootFolder	= getDynamic( _Session, "FOLDER" ).getString();
	
		int startRows = -1, maxRows = 0;
		if ( containsAttribute( "STARTROW" ) && containsAttribute( "MAXROWS" ) ){
			startRows = getDynamic( _Session, "STARTROW" ).getInt();
			maxRows 	= getDynamic( _Session, "MAXROWS" ).getInt();
		}
	
		boolean reverseOrder = false;
		if ( containsAttribute( "REVERSE" ) ){
			reverseOrder = getDynamic( _Session, "REVERSE" ).getBoolean();
		}
		
		cfMailFolderMessagesData	cfMFD	= new cfMailFolderMessagesData();
		_Session.setData( name, cfMFD.listFolderMessages( imapConnection, rootFolder, startRows, maxRows, reverseOrder ) );
	}

	
	
	
	private void readMessage( cfSession _Session, cfImapConnection imapConnection ) throws cfmRunTimeException {
		if ( !containsAttribute("NAME") )
			throw newRunTimeException( "Missing the NAME attribute" );

		if ( !containsAttribute("MESSAGEID") )
			throw newRunTimeException( "Missing the MESSAGEID attribute" );

		String name				= getDynamic( _Session, "NAME" ).getString();
		String rootFolder	= null;
		if ( containsAttribute("FOLDER") )
			rootFolder = getDynamic( _Session, "FOLDER" ).getString();

		String attachURI  = null, attachDIR = null;
		if ( containsAttribute("ATTACHMENTSURI") ){
			attachURI = getDynamic( _Session, "ATTACHMENTSURI" ).getString();
			attachDIR = FileUtils.getRealPath( _Session.REQ, attachURI );

			if ( ( attachDIR == null ) || !new java.io.File( attachDIR ).exists() ) {
				throw newRunTimeException( "The directory specified via the ATTACHMENTSURI attribute does not exist." );
			}else if ( attachDIR.charAt( attachDIR.length()-1 ) != java.io.File.separatorChar ){
				attachDIR	+= java.io.File.separator;
			}
		}

		long messageID	= getDynamic( _Session, "MESSAGEID" ).getInt();

		cfMailMessageData	cfMMD = new cfMailMessageData( _Session );
		cfMMD.getMessage( imapConnection, rootFolder, messageID, attachURI, attachDIR );

		if ( imapConnection.didSucceed() )
			_Session.setData( name, cfMMD );
	}
	
	private void markMessagesRead( cfSession _Session, cfImapConnection imapConnection ) throws cfmRunTimeException {
	
		if ( !containsAttribute("MESSAGELIST") )
			throw newRunTimeException( "Missing the MESSAGELIST attribute" );

		if ( !containsAttribute("FOLDER") )
			throw newRunTimeException( "Missing the FOLDER attribute" );

    long mailIDs[] = makeArray( getDynamic( _Session, "MESSAGELIST" ).getString() );
  	String rootFolder = getDynamic( _Session, "FOLDER" ).getString();
  	imapConnection.changeStatus( rootFolder, mailIDs, Flags.Flag.SEEN, true );
	}
	
	private void markMessagesDelete( cfSession _Session, cfImapConnection imapConnection ) throws cfmRunTimeException {
	
		if ( !containsAttribute("MESSAGELIST") )
			throw newRunTimeException( "Missing the MESSAGELIST attribute" );

		if ( !containsAttribute("FOLDER") )
			throw newRunTimeException( "Missing the FOLDER attribute" );

    long mailIDs[] = makeArray( getDynamic( _Session, "MESSAGELIST" ).getString() );
  	String rootFolder = getDynamic( _Session, "FOLDER" ).getString();
  	imapConnection.changeStatus( rootFolder, mailIDs, Flags.Flag.DELETED, true );
	}

  private void setFlags( cfSession _Session, cfImapConnection imapConnection ) throws cfmRunTimeException {
    if ( !containsAttribute("MESSAGEID") )
      throw newRunTimeException( "Missing the MESSAGEID attribute" );

    if ( !containsAttribute("FOLDER") )
      throw newRunTimeException( "Missing the FOLDER attribute" );
    
    String rootFolder = getDynamic( _Session, "FOLDER" ).getString();
    
    long [] messageID = new long [] { getDynamic( _Session, "MESSAGEID" ).getLong() };
    if ( containsAttribute("ANSWERED") )
      imapConnection.changeStatus( rootFolder, messageID, Flags.Flag.ANSWERED, getDynamic( _Session, "ANSWERED" ).getBoolean() );

    if ( containsAttribute("DELETED") )
      imapConnection.changeStatus( rootFolder, messageID, Flags.Flag.DELETED, getDynamic( _Session, "DELETED" ).getBoolean() );

    if ( containsAttribute("DRAFT") )
      imapConnection.changeStatus( rootFolder, messageID, Flags.Flag.DRAFT, getDynamic( _Session, "DRAFT" ).getBoolean() );

    if ( containsAttribute("FLAGGED") )
      imapConnection.changeStatus( rootFolder, messageID, Flags.Flag.FLAGGED, getDynamic( _Session, "FLAGGED" ).getBoolean() );

    if ( containsAttribute("RECENT") )
      imapConnection.changeStatus( rootFolder, messageID, Flags.Flag.RECENT, getDynamic( _Session, "RECENT" ).getBoolean() );

    if ( containsAttribute("SEEN") )
      imapConnection.changeStatus( rootFolder, messageID, Flags.Flag.SEEN, getDynamic( _Session, "SEEN" ).getBoolean() );
    
    
  }
  
	private void moveMessages( cfSession _Session, cfImapConnection imapConnection ) throws cfmRunTimeException {
	
		if ( !containsAttribute("MESSAGELIST") )
			throw newRunTimeException( "Missing the MESSAGELIST attribute" );

		if ( !containsAttribute("FOLDER") )
			throw newRunTimeException( "Missing the FOLDER attribute" );

		if ( !containsAttribute("DESTFOLDER") )
			throw newRunTimeException( "CFIMAP: Missing the DESTFOLDER attribute" );

    long mailIDs[] = makeArray( getDynamic( _Session, "MESSAGELIST" ).getString() );
  	String rootFolder = getDynamic( _Session, "FOLDER" ).getString();
  	String destFolder = getDynamic( _Session, "DESTFOLDER" ).getString();
  	
  	imapConnection.moveMessages( rootFolder, destFolder, mailIDs );
	}

	
	
	private static long[] makeArray( String list ){
	  List<String> tokens = string.split( list, "," );
	  long array[] = new long[ tokens.size() ];
	  int x=0,badItems = 0;
	  for ( int i = 0; i < tokens.size(); i++ ){
	    array[x++] = com.nary.util.string.convertToLong( tokens.get(i).toString(), -1 );
	    if ( array[x-1] == -1 )
	      badItems++;
	  }
	  
	  if ( badItems > 0 ){
	    long newArray[]  = new long[ array.length - badItems ];
	    int y=0;
	    for ( int xx=0; xx < array.length; xx++ )
	      if ( array[xx] != -1 )
	        newArray[y++] = array[xx];
	        
	    return newArray;
	  }else
  	  return array;
	}
	
	//--------------------------------------------------------------
	//--[ Folder Management routines
	//--------------------------------------------------------------
	
	private void createFolder( cfSession _Session, cfImapConnection imapConnection ) throws cfmRunTimeException {

		if ( !containsAttribute("FOLDER") )
			throw newRunTimeException( "Missing the FOLDER attribute" );

		imapConnection.createFolder( getDynamic( _Session, "FOLDER" ).getString() );
	}
	
	
	private void deleteFolder( cfSession _Session, cfImapConnection imapConnection ) throws cfmRunTimeException {

		if ( !containsAttribute("FOLDER") )
			throw newRunTimeException( "Missing the FOLDER attribute" );

		imapConnection.deleteFolder( getDynamic( _Session, "FOLDER" ).getString() );
	}
	
	
	private void renameFolder( cfSession _Session, cfImapConnection imapConnection ) throws cfmRunTimeException {

		if ( !containsAttribute("OLDFOLDER") )
			throw newRunTimeException( "Missing the OLDFOLDER attribute" );

		if ( !containsAttribute("NEWFOLDER") )
			throw newRunTimeException( "Missing the NEWFOLDER attribute" );

		imapConnection.renameFolder( getDynamic( _Session, "OLDFOLDER" ).getString(), getDynamic( _Session, "NEWFOLDER" ).getString() );
	}
}
