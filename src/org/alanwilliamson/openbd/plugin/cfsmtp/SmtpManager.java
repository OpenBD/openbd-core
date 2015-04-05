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
 *  $Id: SmtpManager.java 1720 2011-10-07 19:39:14Z alan $
 */

/*
 * Handles the SMTP bridge between CFSMTP and Mail25 libraries
 */
package org.alanwilliamson.openbd.plugin.cfsmtp;

import java.io.File;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.alanwilliamson.mail25.mailet.PreMailFilterInterface;
import org.alanwilliamson.mail25.server.Mail25;
import org.alanwilliamson.mail25.server.cfMailSession;
import org.apache.mailet.Mail;

import com.bluedragon.plugin.ObjectCFC;
import com.bluedragon.plugin.PluginManager;
import com.naryx.tagfusion.cfm.application.cfAPPLICATION;
import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;


public class SmtpManager extends Object implements PreMailFilterInterface {

  private Mail25 mail25 = null;
  private String cfcMailet = null, cfcFilter = null;
  private String cfApplicationName = null;
  private String cfcMailetMethod = "onmailaccept", cfcFilterMethod = "onmail";

  public SmtpManager( String name, String ipaddress, int port, String cfcMailComponent, String cfcFilterComponent, String applicationName, int maxConnections, String banner ) throws Exception {

    /* Make sure the server can be started, shut down any previous instances */
    if ( mail25 != null )
      stopServer();

    /* Confirm the CFC references are good */
    this.cfcMailet 					= cfcMailComponent;
    this.cfcFilter 					= cfcFilterComponent;
    this.cfApplicationName	= applicationName;

    /* Now we can setup the MailCatcher */
    Properties config = new Properties();
    if ( ipaddress != null )
    	config.setProperty("mail25.bindaddress", ipaddress );

    config.setProperty("mail25.port",          	String.valueOf(port) );
    config.setProperty("mail25.maxconnections",	String.valueOf(maxConnections) );
    config.setProperty("mail25.spooldir",      	new File( cfEngine.thisPlatform.getFileIO().getWorkingDirectory(), name + "_smtp_in" ).getAbsolutePath() );
    config.setProperty("mail25.deliveryqueue", 	new File( cfEngine.thisPlatform.getFileIO().getWorkingDirectory(), name + "_smtp_out" ).getAbsolutePath() );
    config.setProperty("mail25.logfile",       	new File( cfEngine.thisPlatform.getFileIO().getWorkingDirectory(), name + "_smtp.log" ).getAbsolutePath() );

    config.setProperty("spoolchain.name",         "root" );
    config.setProperty("spoolchain.root.matchers","bd" );
    config.setProperty("matcher.bd",              "org.alanwilliamson.mail25.matchers.All" );
    config.setProperty("matcher.bd.condition",    "" );
    config.setProperty("matcher.bd.mailet",       "org.alanwilliamson.openbd.plugin.cfsmtp.BlueDragonMailet" );
    config.setProperty("matcher.bd.mailetparams", name );

    mail25 = new Mail25(name, config, banner);
    mail25.setPreFilterClass( this );
  }

  public void stopServer(){
    if ( mail25 != null ){
    	mail25.shutdown();
    	mail25 = null;
    }
  }

  public boolean isServerRunning(){
    return ( mail25 != null );
  }

  public int getClientsConnected(){
    if ( mail25 != null )
      return mail25.getClientsConnected();
    else
      return 0;
  }

  public int getMailetsRan(){
    if ( mail25 != null )
      return mail25.getMailetsRan();
    else
      return 0;
  }

  public void service(Mail mail) throws MessagingException {
    long startTime  = System.currentTimeMillis();

  	// Create the dummy session
    cfSession tmpSession = ((cfMailSession)(mail.getAttribute("session"))).getSession();

    try {
      // Setup the Application Data
      setApplicationData( tmpSession );

      // Create the CFC we want to call
      ObjectCFC cfc = PluginManager.getPlugInManager().createCFC( tmpSession, cfcMailet );
    	cfc.addArgument( "mail", new BlueDragonMailWrapper( mail ) );
    	cfc.runMethod( tmpSession, cfcMailetMethod );

    	mail25.log( "cfcRan." + cfcMailet + "; Time=" + (System.currentTimeMillis()-startTime) + "ms" );
    } catch (cfmRunTimeException rte) {
      rte.handleException( tmpSession );
      mail25.log( "cfcRan." + cfcMailet + "; Exception @ " + rte.getLogFile().toString() );
    } catch (Exception e) {
    	PluginManager.getPlugInManager().log( "SmtpManager.service.Exception:" + e.getMessage() );
    } finally {
    	tmpSession.pageEnd();
    	tmpSession.close();
    }
  }

  public boolean acceptMailFrom(cfMailSession cfmailsession, InternetAddress address, String ipAddress) {
    if ( this.cfcFilter != null ){
    	long startTime  = System.currentTimeMillis();

    	// Create the dummy session
      cfSession tmpSession = cfmailsession.getSession();

      try {
        // Setup the Application Data
        setApplicationData( tmpSession );

        // Create the CFC we want to call
        ObjectCFC cfc = PluginManager.getPlugInManager().createCFC( tmpSession, cfcFilter );

        cfc.addArgument( "email", address.getAddress() );
        cfc.addArgument( "ip", 		ipAddress );

        return cfc.runMethodReturnBoolean( tmpSession, cfcFilterMethod + "from" );
      } catch (cfmRunTimeException rte) {
        rte.handleException( tmpSession );
        return true;
      }catch (Exception e) {
      	PluginManager.getPlugInManager().log( "SmtpManager.onMailFrom.Exception:" + e.getMessage() );
        return true;
      } finally {
      	tmpSession.pageEnd();
      	tmpSession.close();
      	mail25.log( "onMailFrom=" + cfcFilter + "." + cfcFilterMethod + "from; Time=" + (System.currentTimeMillis()-startTime) + "ms" );
      }
    }else
      return true;
  }

  public boolean acceptMailTo(cfMailSession cfmailsession, InternetAddress address, String ipAddress) {
    if ( this.cfcFilter != null ){
      long startTime  = System.currentTimeMillis();

    	// Create the dummy session
      cfSession tmpSession = cfmailsession.getSession();

      try {
        // Setup the Application Data
        setApplicationData( tmpSession );

        // Create the CFC we want to call
        ObjectCFC cfc = PluginManager.getPlugInManager().createCFC( tmpSession, cfcFilter );

        cfc.addArgument( "email", address.getAddress() );
        cfc.addArgument( "ip", 		ipAddress );

        return cfc.runMethodReturnBoolean( tmpSession, cfcFilterMethod + "to" );
      } catch (cfmRunTimeException rte) {
        rte.handleException( tmpSession );
        return true;
      }catch (Exception e) {
      	PluginManager.getPlugInManager().log( "SmtpManager.onMailTo.Exception:" + e.getMessage() );
        return true;
      } finally {
      	tmpSession.pageEnd();
      	tmpSession.close();
      	mail25.log( "onMailTo=" + cfcFilter + "." + cfcFilterMethod + "to; Time=" + (System.currentTimeMillis()-startTime) + "ms" );
      }
    }else
      return true;
  }

  private void setApplicationData( cfSession tmpSession ){
    // Setup the Application Data
    if ( cfApplicationName != null ){
    	cfApplicationData appData = cfAPPLICATION.getAppManager().getAppData( tmpSession, cfApplicationName );
    	tmpSession.setQualifiedData( variableStore.APPLICATION_SCOPE, appData );
    }
  }
}
