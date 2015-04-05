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

import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/*
 * This is the class that holds a reference to the IMAP server
 * it handles all the high level functionality including the connection
 * and closure of connections.
 */

public class cfImapConnection extends cfStructData {

	private static final long serialVersionUID = 1L;
	
	private long lastUsed;
	public Store mailStore = null;


	public cfImapConnection( cfData _SERVER, cfData _SERVICE, cfData _USERNAME, cfData _PASSWORD ){
		super();
		setData( "server", 		_SERVER );
		setData( "username", 	_USERNAME );
		setData( "password", 	_PASSWORD );
		setData( "service", 	_SERVICE );

		setStatus( false, "" );
		
		lastUsed	= System.currentTimeMillis();
		
		openConnection();
	}

	public void setStatus( boolean _succeeded, String _errorText ){
		setData( "errortext", 	new cfStringData( _errorText ) );
		setData( "succeeded", 	cfBooleanData.getcfBooleanData( _succeeded ) );
		lastUsed	= System.currentTimeMillis();
	}
	
	public long getTimeSinceLastUsed(){
		return System.currentTimeMillis() - lastUsed;
	}
	
	public void setTimeUsed(){
		lastUsed	= System.currentTimeMillis();
	}
	
	public boolean isClosed(){
		return (mailStore == null);
	}
	
	public boolean didSucceed() throws cfmRunTimeException {
		return getData( "succeeded" ).getBoolean();
	}
	
	public void setTotalMessages( int total ){
		setData( "totalmessages", new cfNumberData( total ) );
	}
	
	private void openConnection(){
		
		try{
		
			Properties props 	= System.getProperties();
			props.put( "mail.imap.partialfetch", "false" );
			Session session 	= Session.getInstance(props, null);

			mailStore					= session.getStore( getData("service").getString().toLowerCase() );
			mailStore.connect( getData("server").getString(), getData("username").getString(), getData("password").getString() );

			setData( "succeeded", 	cfBooleanData.TRUE );
			
		}catch(AuthenticationFailedException A){
			setData( "errortext", 	new cfStringData( A.getMessage() ) );
		}catch(MessagingException M){
			setData( "errortext", 	new cfStringData( M.getMessage() ) );
		}catch(SecurityException SE){
			setData("errortext", new cfStringData("CFIMAP is not supported if SocketPermission is not enabled for the IMAP server. (" + SE.getMessage() + ")"));
		}catch(Exception E){
			setData( "errortext", 	new cfStringData( E.getMessage() ) );
		}
	}

	public void closeConnection(){
		try{
			mailStore.close();
			mailStore = null;
			lastUsed	= System.currentTimeMillis();
		}catch(Exception E){}
	}
	
	public void changeStatus( String folderName, long mailIDs[], Flags.Flag newFlag, boolean value ){
    try{
    
      Folder folderToUse = mailStore.getFolder(folderName);
      folderToUse.open( Folder.READ_WRITE );
      
      Flags f = new Flags();
      f.add( newFlag );
			
			Message mlist[];
			if ( folderToUse instanceof UIDFolder )
				mlist = ((UIDFolder)folderToUse).getMessagesByUID( mailIDs );
			else
				mlist = folderToUse.getMessages( returnToInts(mailIDs) );
			
			for ( int x=0; x < mlist.length; x++ )
				mlist[x].setFlags( f, value );
      
      folderToUse.close( true );
			setData( "succeeded", 	cfBooleanData.TRUE );
    
    } catch (Exception E ){
			setData( "errortext", 	new cfStringData( E.getMessage() ) );
			setData( "succeeded", 	cfBooleanData.FALSE );
    }
  }
  
	private static int[] returnToInts( long t[] ){
		int tt[]	= new int[ t.length ];
		for ( int x=0; x < t.length; x++ )
			tt[x] = (int)t[x];
			
		return tt;
	}
	
  public void moveMessages( String rootFolder, String destFolder, long mailIDs[]){
    try{
    
      Folder folderToUse = mailStore.getFolder(rootFolder);
      Folder folderToRXD = mailStore.getFolder(destFolder);
      folderToUse.open( Folder.READ_WRITE );
			
			Message mlist[] = ((UIDFolder)folderToUse).getMessagesByUID( mailIDs );
     
      try{ 
        folderToUse.copyMessages( mlist, folderToRXD );
        Flags f = new Flags();
        f.add( Flags.Flag.DELETED );

				for ( int x=0; x < mlist.length; x++ )
					mlist[x].setFlags( f, true );
				       
      }catch(Exception E){}
      
      folderToUse.close( true );
      setData( "succeeded", 	cfBooleanData.TRUE );
			
    } catch (Exception E ){
      setData( "errortext", 	new cfStringData( E.getMessage() ) );
      setData( "succeeded", 	cfBooleanData.FALSE );
    }
  }  
  
  public void copyMessages( String destFolder, Message msg ){
    try{
    
      Folder folderToUse = mailStore.getFolder(destFolder);
      folderToUse.open( Folder.READ_WRITE );

      Message[] list  = new Message[1];
      list[0] = msg;
      
      folderToUse.appendMessages( list );
      folderToUse.close(false);
      setData( "succeeded", 	cfBooleanData.TRUE );
      
	  	lastUsed	= System.currentTimeMillis();
    
    } catch (Exception E ){
      setData( "errortext", 	new cfStringData( E.getMessage() ) );
      setData( "succeeded", 	cfBooleanData.FALSE );
    }
  }
	
	//---------------------------------------------------------------------
	//--[ Folder Management routines
	//---------------------------------------------------------------------

	public void createFolder( String newFolder ){
		if ( newFolder.length() == 0 )
			return;
		
    try{
    
      Folder folderToUse = mailStore.getFolder( newFolder );
			
			if ( !folderToUse.exists() ){
				folderToUse.create( Folder.HOLDS_FOLDERS | Folder.HOLDS_MESSAGES );
			}
			
      setData( "succeeded", 	cfBooleanData.TRUE );
    
    } catch (Exception E ){
      setData( "errortext", 	new cfStringData( E.getMessage() ) );
      setData( "succeeded", 	cfBooleanData.FALSE );
    }
		
		setTotalMessages( 0 );
	}
	
	
	
	public void deleteFolder( String folderToDelete ){
		if ( folderToDelete.length() == 0 )
			return;

    try{
    
      Folder folderToUse = mailStore.getFolder( folderToDelete );
			
			if ( folderToUse.exists() ){
				folderToUse.delete( true );
			}
			
      setData( "succeeded", 	cfBooleanData.TRUE );
      
    } catch (Exception E ){
      setData( "errortext", 	new cfStringData( E.getMessage() ) );
      setData( "succeeded", 	cfBooleanData.FALSE );
    }
		
		setTotalMessages( 0 );
	}

	
	
	public void renameFolder( String oldFolder, String newFolder ){
		
		if ( oldFolder.length() == 0 || newFolder.length() == 0 )
			return;
		
    try{
    
      Folder folderToUse 		= mailStore.getFolder( oldFolder );
			Folder newfolderToUse = mailStore.getFolder( newFolder );
			
			if ( folderToUse.exists() && !newfolderToUse.exists() ){
				folderToUse.renameTo( newfolderToUse );
			}
			
      setData( "succeeded", 	cfBooleanData.TRUE );
    
    } catch (Exception E ){
      setData( "errortext", 	new cfStringData( E.getMessage() ) );
      setData( "succeeded", 	cfBooleanData.FALSE );
    }
		
		setTotalMessages( 0 );
	}
}
