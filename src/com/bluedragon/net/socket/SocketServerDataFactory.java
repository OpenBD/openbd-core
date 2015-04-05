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
 *  http://openbd.org/
 *  $Id: SocketServerDataFactory.java 1903 2011-12-30 20:46:20Z alan $
 */

package com.bluedragon.net.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.bluedragon.plugin.ObjectCFC;
import com.bluedragon.plugin.PluginManager;
import com.naryx.tagfusion.cfm.application.cfAPPLICATION;
import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;

/**
 * Manages the Server Sockets for the ServerSocketXXX() functionality.
 * 
 * This is a singleton class that wraps the threads for handling the servers; they are all destroyed on the close down of the main server
 *
 */
public class SocketServerDataFactory {

	public static SocketServerDataFactory	thisInst = new SocketServerDataFactory();
	
	private Map<Integer, SocketServerAccept>	servers = null;
	
	private SocketServerDataFactory(){}
	
	public void close(){
		if ( servers == null )
			return;
		
		Iterator<SocketServerAccept>	it	= servers.values().iterator();
		while ( it.hasNext() )
			it.next().close();
	}
	
	public synchronized	boolean	addServer( String ip, int port, String cfc, String appname ) throws Exception{
		if ( servers == null )
			servers	= new HashMap<Integer, SocketServerAccept>();
		
		if ( servers.containsKey(port) )
			return false;
		
		// Test the CFC is ok; this will throw an error if wrong
		PluginManager.getPlugInManager().createCFC( PluginManager.getPlugInManager().createBlankSession(), cfc );
		
		// All is well, so let us move forward
		servers.put( port, new SocketServerAccept(ip,port,cfc,appname) );
		return true;
	}
	
	
	public synchronized void closeServer( int port ){
		if ( servers == null )
			return;
		
		SocketServerAccept	ssa	= servers.get(port);
		if (ssa != null){
			ssa.close();
			servers.remove(port);
		}
	}
	
	
	public synchronized cfArrayData getClients( int port ) throws cfmRunTimeException{
		if ( servers == null )
			return null;
		
		SocketServerAccept	ssa	= servers.get(port);
		if (ssa != null){
			return ssa.getClients();
		}else
			return null;
	}
	
	
	/**
	 * This is the thread that runs in response
	 *
	 */
	private class SocketServerAccept extends Thread {
		
		private ServerSocket	serversocket;
		private String cfc, appname;
		private List<remoteclient>	connectedClients;
		
		
		SocketServerAccept( String ip, int port, String cfc, String appname ) throws IOException{
			super("SocketServerAccept#" + port );
			
			this.cfc	= cfc;
			this.appname	= appname;
			
			connectedClients	= new ArrayList<remoteclient>();
			
			if ( ip == null )
				serversocket	= new ServerSocket( port, 20 );
			else
				serversocket	= new ServerSocket( port, 20, InetAddress.getByName(ip) );

			start();
		}
		
		public cfArrayData	getClients() throws cfmRunTimeException{
			cfArrayData	array	= cfArrayData.createArray(1);
			
			Iterator<remoteclient>	it	= connectedClients.iterator();
			while ( it.hasNext() ){
				array.addElement( it.next().getCFC().getComponentCFC() );
			}
			
			return array;
		}
		
		public void close(){
			interrupt();
			
			Iterator<remoteclient>	it	= connectedClients.iterator();
			while ( it.hasNext() ){
				it.next().close();
			}
		}
		
		public void disconnectClient( remoteclient client ){
			connectedClients.remove(client);
		}
		
		public void run(){
			cfEngine.log("Server #" + serversocket.getLocalPort() + ": Started");
			
			for (;;){
				Socket socket = null;
				
				try {
					socket	= serversocket.accept();
				} catch (IOException e) {
					break;
				}
				
				startCFC( socket );
			}
			
			// this server has stopped
			servers.remove( serversocket.getLocalPort() );
			
			cfEngine.log("Server #" + serversocket.getLocalPort() + ": Stopped");
		}
		
		
		private void startCFC(Socket socket){
			
			try {
				SocketData	socketdata	= new SocketData(socket, true);
				cfSession tmpSession 		= PluginManager.getPlugInManager().createBlankSession();

				if ( appname != null ){
		    	cfApplicationData appData = cfAPPLICATION.getAppManager().getAppData( tmpSession, appname );
		    	tmpSession.setQualifiedData( variableStore.APPLICATION_SCOPE, appData );
		    }
				
				ObjectCFC cfcObj 	= PluginManager.getPlugInManager().createCFC( tmpSession, cfc );
				connectedClients.add( new remoteclient(this, tmpSession, cfcObj, socketdata ) );

			} catch (Exception e) {
				cfEngine.log("SocketServer.startCFC(" + cfc + ") " + e.getMessage() );
			}
		}

	}
	
	
	/**
	 * Handles the remote client
	 * 
	 * @author alan
	 *
	 */
	class remoteclient extends Thread {
		private SocketServerAccept server;
		private cfSession session;
		private ObjectCFC cfcobj;
		private SocketData	socketdata;
		
		public remoteclient(SocketServerAccept server, cfSession tmpSession, ObjectCFC cfcObj, SocketData	socketdata ){
			this.server 		= server;
			this.session 		= tmpSession;
			this.cfcobj 		= cfcObj;
			this.socketdata	= socketdata;
			start();
		}
		
		public ObjectCFC getCFC(){
			return cfcobj;
		}
		
		public void close(){
			socketdata.disconnect();
			interrupt();
		}
		
		public void run(){
			// Call the CFC
			onConnect();
			
			while ( socketdata.isConnected() ){
				String lineIn;
				try {
					lineIn = socketdata.readLine( 30000 );
					if ( lineIn == null )
						continue;
					
					onReadLine(lineIn);
					
				} catch (Exception e) {
					if ( "Read timed out".equals( e.getMessage() ) )
						continue;

					socketdata.disconnect();
					break;
				}
			}
			
			// this one is finished with
			onDisconnect();
			server.disconnectClient(this);
		}
		
		
		private synchronized void onReadLine(String line){
			cfcobj.clearArguments();
			cfcobj.addArgument( "socketdata", socketdata );
			cfcobj.addArgument("line", new cfStringData(line) );
			try {
				cfcobj.runMethodReturnBoolean( session, "onreadline" );
			} catch (Exception e) {
				cfEngine.log("SocketServer.RemoteClient.onReadLine(): " + e.getMessage() );
			}
		}
		
		
		private synchronized void onConnect(){
			cfcobj.clearArguments();
			cfcobj.addArgument( "socketdata", socketdata );
			try {
				cfcobj.runMethodReturnBoolean( session, "onconnect" );
			} catch (Exception e) {
				cfEngine.log("SocketServer.RemoteClient.onConnect(): " + e.getMessage() );
			}
		}
		
		
		private synchronized void onDisconnect(){
			cfcobj.clearArguments();
			cfcobj.addArgument( "socketdata", socketdata );
			try {
				cfcobj.runMethodReturnBoolean( session, "ondisconnect" );
			} catch (Exception e) {
				cfEngine.log("SocketServer.RemoteClient.onDisconnect(): " + e.getMessage() );
			}
		}
	}
}
