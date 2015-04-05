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
 *  $Id: $
 */

package com.naryx.tagfusion.cfm.mail;

import java.util.Enumeration;
import java.util.Hashtable;

import org.aw20.util.SystemClockEvent;

import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.engineListener;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

/*
 * This class holds all the references to the connections that are presently
 * being used for the CFIMAP tag.  There is a timer on this class that will
 * run around and clean out dead and unused connections.  30 minutes is the time
 * out value for the connection.
 * 
 */

public class imapManager extends Object implements engineListener, SystemClockEvent {
	
	private Hashtable<String, cfImapConnection>	connectionCache;
	private long	notUsedTimeOut	= 30 * 60 * 1000;
	
	public imapManager(){
		cfEngine.registerEngineListener( this );
		connectionCache = new Hashtable<String, cfImapConnection>();
		
		cfEngine.thisPlatform.timerSetListenerMinute(this, 15);
		
		cfEngine.log("CFIMAP Caching Engine Initialised.");
	}
	
	public void engineAdminUpdate( xmlCFML config ){}

	public void engineShutdown(){
		Enumeration<cfImapConnection> E = connectionCache.elements();
		while ( E.hasMoreElements() ){
			E.nextElement().closeConnection();
		}
		
		cfEngine.log("CFIMAP Caching Engine closed open connections");
	}

	public void clockEvent(int type){
		Enumeration<String> E = connectionCache.keys();
		while ( E.hasMoreElements() ){
			String key	= E.nextElement();
			cfImapConnection	iMapCon	= connectionCache.get(key);
			if ( iMapCon.getTimeSinceLastUsed() > notUsedTimeOut || iMapCon.isClosed() ){
				iMapCon.closeConnection();
				connectionCache.remove(key);
			}
		}
	}
	
	public void cacheConnection( String name, cfImapConnection imapConnection ){
		imapConnection.setTimeUsed();
		connectionCache.put( name, imapConnection );
	}
	
	public void closeCachedConnection( String name ){
		if ( connectionCache.containsKey(name) ){
			connectionCache.get( name ).closeConnection();
			connectionCache.remove( name );
		}
	}
	
	public cfImapConnection getCacheConnection( String name ){
		cfImapConnection imapCon = connectionCache.get( name );
		if ( imapCon != null )
			imapCon.setTimeUsed();
			
		return imapCon;
	}
}