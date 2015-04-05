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

package com.naryx.tagfusion.cfm.tag.awt;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nary.util.LogFile;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfcMethodData;
import com.naryx.tagfusion.cfm.engine.engineListener;
import com.naryx.tagfusion.cfm.wddx.cfWDDX;
import com.naryx.tagfusion.util.dummyServletRequest;
import com.naryx.tagfusion.util.dummyServletResponse;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class MultiCastManager extends Object implements engineListener {

  private String    address;
  private InetAddress bindAddress;
  private InetAddress groupAddr;
	
  private int       port;
  private TxdThread txdThread       = null;
  private RxdThread rxdThread       = null;
  private cfcRunnerThread cfcThread = null; 
  
  private String    cfc, cfcMethod, webRoot;
  
  private Vector    outList, inList;
  
  private boolean   bRunning, bReceiving;
  private int messages_rxd = 0, messages_txd = 0;
  private long bytes_rxd = 0, bytes_txd = 0;
  
  public MultiCastManager(String bindaddress, String address, int port) throws UnknownHostException{
    
    cfEngine.registerEngineListener( this );
    
    try {
      bindAddress = InetAddress.getByName( bindaddress );
    } catch (UnknownHostException e) {
      try {
        bindAddress = InetAddress.getLocalHost();
      } catch (UnknownHostException e1) {
        log("-] MultiCastManager cannot start: " + e1.getMessage() );
        return;
      }
    }
    
    this.address = address;
    this.port = port;
   	groupAddr = InetAddress.getByName( address );
    bRunning    = true;
    bReceiving  = false;

    outList = new Vector();
    inList  = new Vector();

    rxdThread = null;
    txdThread = new TxdThread();

    log("-] MultiCastManager started $Revision: 2374 $; bindAddr:" + bindAddress );
  }

  public void log(String logLine) {
    LogFile.println("MultiCast", address +"#" + port + ": " + logLine);
  }

  public int getMessagesTxd(){
    return messages_txd;
  }
  
  public int getMessagesRxd(){
    return messages_rxd;
  }

  public long getBytesTxd(){
    return bytes_txd;
  }

  public long getBytesRxd(){
    return bytes_rxd;
  }
  
  public void broadcastData(String data) {
    outList.add(data);
    synchronized(txdThread){
      txdThread.notify();
    }
  }

  public void stopReceiving(){
    if (rxdThread != null){
      bReceiving = false;
      rxdThread.shutdown();
      rxdThread.interrupt();
      rxdThread = null;
    }
    if ( cfcThread != null ){
    	cfcThread.interrupt();
    	try {
				cfcThread.join( 1000 );
			} catch (InterruptedException e) {
	    	cfcThread.interrupt();
			}
			cfcThread = null;
    }
  }
  
  public synchronized void registerReceiver(String cfc, String cfcMethod, String webServerRoot) {
    this.cfc = cfc;
    this.cfcMethod = cfcMethod;
    this.webRoot = webServerRoot;

    if (rxdThread == null)
      rxdThread = new RxdThread();
  }


  public void engineAdminUpdate(xmlCFML config) {
  }

  public void engineShutdown() {
    bRunning    = false;
    bReceiving  = false;
    
    if ( rxdThread != null ){
    	rxdThread.shutdown();
      rxdThread.interrupt();
    }
    
    if ( txdThread != null ){
      txdThread.interrupt();
    }
    
    if ( cfcThread != null ){
      cfcThread.interrupt();
    }
    
    log("Shutdown");
  }
  
  // -------------------------------------
  // -- Helper Threads
  // -------------------------------------

  class TxdThread extends Thread {

  	
    public TxdThread() {
      super("MultiCastManager.TxdThread." + address + "#" + port);
      setDaemon(true);
      start();
    }

    public void run() {
      while (bRunning) {
        while (outList.size() == 0) {
          try {
            synchronized (this){
              wait(1000);
            }
          } catch (InterruptedException e) {
            log( "TxdThread.run.InterruptedException:" + e.getMessage() );
          }
        }

        String outData = (String) outList.remove(0);
        if ( outData != null )
          sendData(outData);
      }
    }

    private void sendData(String data) {
      MulticastSocket msocket = null;
      
      try {
        
        msocket = new MulticastSocket( port );
        msocket.setInterface( bindAddress );
        DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), groupAddr, port);
        msocket.send(packet);
        
        messages_txd++;
        bytes_txd += data.length();
        
      } catch (SocketException e) {
        log( "TxdThread.sendData.SocketException: " + e.getMessage() );
      } catch (IOException e) {
		log("TxdThread.sendData.IOException: " + e.getMessage());
	  } catch (SecurityException e) {
		  log("TxdThread.sendData.SecurityException: CFMULTICAST is not supported when the SecurityPermission UnmanagedCode attribute is not set.");
		  log("TxdThread.sendData.SecurityException: " + e.getMessage());
	  } finally {
        try{ msocket.close(); }catch(Exception ignore){}
      }
    }
  }

  // ----------------------------------------------------

  class RxdThread extends Thread {
    private MulticastSocket msocket;

    public RxdThread() {
      super("MultiCastManager.RxdThread." + address + "#" + port);
      setDaemon(true);
      
      bReceiving  = true;
      
      try {
        msocket = new MulticastSocket( port );
        msocket.setInterface( bindAddress );
        msocket.joinGroup(groupAddr);
        start();
        
        cfcThread = new cfcRunnerThread();
        
      } catch (IOException e) {
        log( "RxdThread.IOException:" + e.getMessage() );
      }
    }

    public void shutdown(){
    	bReceiving = false;
    	msocket.close();
    }
    
    public void run() {
      byte inBuffer[] = new byte[64000];

      while (bReceiving) {
        try {
          DatagramPacket packet = new DatagramPacket(inBuffer, inBuffer.length);

          //- Wait for the datagram packet
          msocket.receive(packet);

          messages_rxd++;
          bytes_rxd += packet.getLength();
          
          //- Queue the message in
          HashMap h = new HashMap();
          h.put( "data", new String(inBuffer, 0, packet.getLength() ) );
          h.put( "from", packet.getAddress().getHostAddress() );
          inList.add( h );
          
          synchronized (inList){
            inList.notify();
          }
          
          log( "Mess In: From=" + packet.getAddress() + "; Size=" + packet.getLength() );

        } catch (IOException e) {
        	if ( bReceiving )
        		log( "RxdThread.run.IOException:" + e.getMessage() );
        }
      }
      
      msocket.close();
    }
  }

  // ----------------------------------------------------

  class cfcRunnerThread extends Thread {
    
    public cfcRunnerThread(){
      super("MultiCastManager.cfcRunnerThread." + cfc + "." + cfcMethod );
      setDaemon(true);
      start();
    }
    
    public void run(){
      while (bReceiving) {
        while (inList.size() == 0) {
          try {
            synchronized (inList){
              inList.wait(1000);
            }
          } catch (InterruptedException e) {
          	if ( bReceiving )
          		log( "cfcRunnerThread.run.InterruptedException:" + e.getMessage() );
            return;
          }
        }

        HashMap cfcData = (HashMap)inList.remove(0);
        if ( cfcData != null )
          runCFC( cfcData );
      }
    }

    private void runCFC(HashMap cfcData ){
      cfSession tmpSession = null;
      try {
        long startTime  = System.currentTimeMillis();

        HttpServletRequest REQ  = new dummyServletRequest(webRoot);
        HttpServletResponse RES = new dummyServletResponse();

        tmpSession = new cfSession(REQ, RES, cfEngine.thisServletContext);

        cfStructData mess = new cfStructData();

        //- set the data
        String data = (String)cfcData.get("data");
        if ( data.startsWith("<wddxPacket version='1.0'><header></header><data>") ){
          mess.setData("cfmldata", cfWDDX.wddx2Cfml( data, tmpSession) );
        }else{
          mess.setData("textdata", new cfStringData( data ) );
        }

        //- set the from field
        mess.setData( "from", new cfStringData( (String)cfcData.get("from") ) );

        //- Call the CFC
        cfArgStructData args = new cfArgStructData();
        args.setData( "data", mess );

        cfComponentData component = new cfComponentData(tmpSession, cfc);
        cfcMethodData invocationData = new cfcMethodData(tmpSession, cfcMethod, args);
        cfData returnData = component.invokeComponentFunction(tmpSession, invocationData);

        if ( returnData.getDataType() == cfData.CFSTRINGDATA || returnData.getDataType() == cfData.CFNUMBERDATA 
        		|| returnData.getDataType() == cfData.CFBOOLEANDATA || returnData.getDataType() == cfData.CFDATEDATA ){
          log( "cfcRan: Time=" + (System.currentTimeMillis()-startTime) + "ms. Returned " + returnData.getString() );
        }else{
        	log( "cfcRan: Time=" + (System.currentTimeMillis()-startTime) + "ms" );
        }
      } catch (Exception E) {
        log( cfc + "." + cfcMethod + ".runCFC: " + E.getMessage());
      } finally {
    	// Make sure per request connections are closed (bug NA#3174)
    	if ( tmpSession != null ) tmpSession.sessionEnd();
      }
    }
  }
}
