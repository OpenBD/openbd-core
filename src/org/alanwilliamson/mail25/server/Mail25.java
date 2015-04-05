/* 
 *  Copyright (C) 1996 - 2008 Alan Williamson
 *
 *  This file is part of Mail25 Mailet Container.
 *  
 *  Mail25 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  Free Software Foundation,version 3.
 *  
 *  Mail25 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with Mail25.  If not, see http://www.gnu.org/licenses/
 *  
 *  http://alan.blog-city.com/
 *  
 *  $Id: Mail25.java 1720 2011-10-07 19:39:14Z alan $
 */


/**
 * Mail25 - Apache Mailet Container 
 * 
 * Properties ____
 * 
 * mail25.bindaddress=[ip you want to bind this process to]
 * mail25.maxconnections=[max number of connections]
 *  
 * mail25.deliveryqueue=[directory for mails that are to be delivered]
 * mail25.spooldir=[directory for large incoming emails]
 * 
 * mail25.port=[port to listen on, defaults to 25]
 * mail25.logfile=[path to the log file, defaults to mailcatcher.log]
 * 
 * If you are finding the resolving of MX records empty you can pass in the DNS server as a
 * JVM parameter:  -Ddns.server=dns1,dns2
 * 
*/
package org.alanwilliamson.mail25.server;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.internet.InternetAddress;

import org.alanwilliamson.mail25.chain.Chain;
import org.alanwilliamson.mail25.mailet.CaughtMail;
import org.alanwilliamson.mail25.mailet.Mail25Context;
import org.alanwilliamson.mail25.mailet.PreMailFilterInterface;
import org.apache.mailet.Mail;
import org.apache.mailet.MailetContext;
import org.quickserver.net.server.QuickServer;



public class Mail25 extends Object {
  private QuickServer mailServer;
  private File spoolDirectoryIn;
  private Mail25Context mailContext;
  private HashMap chainManagers;
  private PreMailFilterInterface preFilterMail;
  
  private boolean bDebugMode = false;

  public Mail25( String name, Properties config, String banner ) throws Exception {
    // Set up the QuickServer details for this Mail
    int portNo = Integer.parseInt( config.getProperty("mail25.port", "25") );
    mailServer = new QuickServer( "org.alanwilliamson.mail25.server.MailHandler",  portNo  );
    mailServer.setClientData( "org.alanwilliamson.mail25.server.MailData" );
    mailServer.setClientEventHandler( "org.alanwilliamson.mail25.server.MailHandler" );
    mailServer.setStoreObjects( new Object[]{ this, banner } );
    
    if ( config.getProperty("mail25.bindaddress") != null )
      mailServer.setBindAddr( config.getProperty("mail25.bindaddress") );
    
    mailServer.setMaxConnection( Integer.parseInt( config.getProperty("mail25.maxconnections", "5") ) );
    mailServer.setMaxConnectionMsg( "Mail25 experiencing high load, please try again later" );
    mailServer.setTimeout( 120 * 1000 );  //- 2 minutes socket timeout

    //-- Setup the spool directory for large emails
    spoolDirectoryIn  = new File( config.getProperty("mail25.spooldir", "./spool_in_for_" + name ) );
    if ( !spoolDirectoryIn.isDirectory() ){
      spoolDirectoryIn.mkdirs();
    }

    //-- Let us attempt to create the Matcher class
    try{
      if ( config.containsKey("prefiltermail.class") ){
        Class c       = Class.forName( config.getProperty( "mail25.prefiltermail.class" ) );
        preFilterMail = (PreMailFilterInterface)c.newInstance();
      }
    }catch(Exception e){
      throw new Exception( "PreMailFilterInterface: [mail25.prefiltermail.class=" + config.getProperty( "mail25.prefiltermail.class" ) + "] " + e.getMessage() );
    }
    
    //- Create the Mail Context, one per server
    mailContext = new Mail25Context( config );

    //- Create the Chain Managers
    chainManagers = new HashMap();
    initChainManagers( config );

    if ( chainManagers.size() == 0 || !chainManagers.containsKey( Mail.DEFAULT.toUpperCase() ) )
      throw new Exception("No [" + Mail.DEFAULT + "] chain defined");

    log( "Mail25: Listening on port#" + portNo + "; IP=" + (config.getProperty("mail25.bindaddress")==null? "*":config.getProperty("mail25.bindaddress")) );
    log( "Mail25: Incoming Spool Dir=" + spoolDirectoryIn.getAbsolutePath() );

   	mailServer.setServerBanner( "Mail25 Mailet Container @alanwilliamson.org v1.2" );
    mailServer.setAppLogger( null );

    try{
      mailServer.startServer();
    }catch(Exception e){
      System.out.println( "Failed to create server: " + e.getMessage() );
      shutdown();
    }
  }

  
  private void initChainManagers(Properties config) throws Exception {
    String chainList  = config.getProperty( "spoolchain.name" );
    if ( chainList == null || chainList.length() == 0 )
      return;
    
    StringTokenizer st = new StringTokenizer( chainList, "," );
    while ( st.hasMoreTokens() ){
      String chainName  = st.nextToken();
      
      chainManagers.put( chainName.toUpperCase(), new Chain( chainName, config, this ) );
    }
  }

  /**
   * Allows you to set the pre-filter class 
   */
  public void setPreFilterClass( PreMailFilterInterface preFilterMail ) {
    this.preFilterMail  = preFilterMail;
  }

  /**
   * Shut downs the server. 
   */
  public void shutdown() {
    try {
      mailServer.closeAllPools();
    } catch (Exception ignore) {}
      
    try{
      mailServer.stopServer();
    } catch (Exception ignore) {}


    //- close down the chain runners
    Iterator it = chainManagers.values().iterator();
    while ( it.hasNext() ){
      Chain c = (Chain)it.next();
      c.shutdown();
    }
    
    mailContext.shutdown();
  }
  
  /**
   * Creates a unique file in the spool directory.
   * 
   * @return java.io.File
   */
  public synchronized File getSpoolFile(){
    //- Get a unused file name
    File fileSpool = new File( spoolDirectoryIn, System.currentTimeMillis() + ".email" );
    while ( fileSpool.isFile() ){
      fileSpool = new File( spoolDirectoryIn, (System.currentTimeMillis()-4311231231L) + ".email" );
    }
    return fileSpool;
  }

  public void log( String item ){
    mailContext.log( item );
  }
  
  public void log( String item, Throwable t ){
    mailContext.log( item, t );
  }
  
  //------------------------------
  //- Methods for collecting stats
  //------------------------------
  
  private int stats_connectedclients = 0;
  private int stats_mailetsran = 0;
  private int stats_currentclients = 0;
  
  public void statsClientConnected(){
    stats_connectedclients++;
    stats_currentclients++;
  }
  
  /**
   * Returns the total number of clients that have connected to MailCatcher since startup
   * 
   * @return int
   */
  public int getClientsConnected(){
    return stats_connectedclients;
  }
  
  /**
   * Returns the number of times the mailet service() was triggered
   * 
   * @return int
   */
  public int getMailetsRan(){
    return stats_mailetsran;
  }
  
  public void statsLostConnection(){
    stats_currentclients--;
  }
  
  /**
   * Returns the number of clients that are presently connected to MailCatcher
   * 
   * @return int
   */
  public int getConnectedClients(){
    return stats_currentclients;
  }

  public MailetContext getMailContext() {
    return mailContext;
  }

  public void acceptMail(CaughtMail mail) {
    acceptMail( Mail.DEFAULT, mail );
  }

  public void acceptMail(String chainname, CaughtMail mail) {
    stats_mailetsran++;
    
    // Get the chain
    Chain c = (Chain)chainManagers.get( chainname.toUpperCase() );
    
    if ( c == null ){
      log( toString() + "[InvalidChain]=" + chainname );
    }else{
      c.acceptMail( mail );
    }
  }

  public boolean inDebugMode() {
    return bDebugMode;
  }
  
  public boolean acceptMailFrom( cfMailSession cfmailsession, InternetAddress from, String ipAddress ){
    if ( preFilterMail == null )
      return true;
    else
      return preFilterMail.acceptMailFrom(cfmailsession, from, ipAddress);
  }

  public boolean acceptMailTo( cfMailSession cfmailsession, InternetAddress to, String ipAddress ){
    if ( preFilterMail == null )
      return true;
    else
      return preFilterMail.acceptMailTo(cfmailsession, to, ipAddress);
  }
}
