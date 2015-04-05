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

/*
 * <CFSMTP> - Accepting Email instantly
 * 
 * Provides instance incoming email processing, by utilising the open source
 * MailCatcher Project.   When an email is presented to the system then a CFC
 * is called.
 * 
 *  <cfsmtp action="start" port="25" cfcmail="cfcMail" cfcfilter="cfcMail">
 *  This action starts the server on the given port.  The CFCMAIL attribute defines the CFC
 *  that will be triggered when a new email comes in.  It must have the following signature:
 *   
 *  <cffunction name="onmailaccept" access="public"> <cfargument name="mail" required="yes">
 *  
 *  For the CFCFILTER attribute, this is called when the connection is in process and allows you
 *  to stop the acceptance of an email very quickly.  It must have the following signatures:
 *  
 *  <cffunction name="onmailfrom" access="public" returntype="boolean"><cfargument name="email" required="yes">
 *  <cffunction name="onmailto" access="public" returntype="boolean"><cfargument name="email" required="yes">
 *  
 *  The default action in the filter is to accept all email, which will then be passed to the CFCMAIL
 *  
 *  If you CFDUMP the mail and email parameters, you will see a list of the methods you can call with help
 *   
 *  
 *  <cfsmtp action="stop">
 *  This shutdowns the server
 *  
 *  <cfsmtp action="status" resultvar="cfsmtp">
 *  This returns a structure named at RESULTVAR with some stats about the servers progress
 * 
 */
package org.alanwilliamson.openbd.plugin.cfsmtp;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfSMTP extends cfTag  implements Serializable {
  static final long serialVersionUID  = 1;
  

  public java.util.Map getInfo(){
  	return createInfo("cfsmtp-plugin", "Turn OpenBD into a full incoming SMTP agent, accepting emails and process them using a standard CFC object.  Full logs are created in the OpenBD working directory.  Any errors are also logged here.");
  }
  
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
 			createAttInfo("ACTION=START", 	"Starts up the SMTP server", "", true ),
 			createAttInfo("ACTION=STOP", 		"Shuts down the current SMTP server.  All subsequent emails will be denied", "", true ),
			createAttInfo("ACTION=STATUS",	"Writes the status of the current SMTP server into the variable named in RESULTVAR.  Returns a structure of: running [true or false], totalmails, totalconnections", "", true ),
			
 			createAttInfo("PORT", 				"The port number to which to listen for incoming SMTP requests from", "25", false ),
 			createAttInfo("IPADDRESS", 		"The IP address to listen for incoming requests", "[all]", false ),
 			
 			createAttInfo("APPLICATION", 	"The name of the CFAPPLICATION that is to be present when executing a CFC inside a mail request", "", false ),
 			
 			createAttInfo("CFCMAIL",			"The name of the CFC that will be created for each email that comes in.  The method onMailAccept( mail ) must be present.  A new instance is created for each email", "", true ),
 			createAttInfo("CFCFILTER",		"The name of the CFC that will be created to handle the filtering of the email.  You can opt to receive an email or not very early on, saving lots of bandwidth by implementing the method onMailTo(email,ip) and onMailFrom(email,ip) returning back true or false depending on if you wish to receive this email", "", false ),
 			
 			createAttInfo("RESULTVAR",		"Used for ACTION=STATUS to receive the status update", "cfsmtp", false )
  	};
  }
 
  
  protected void defaultParameters( String _tag ) throws cfmBadFileException {
    defaultAttribute( "ACTION",     "start" );
    defaultAttribute( "RESULTVAR",  "cfsmtp" );

    parseTagHeader( _tag );
  }
  
  public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
    
    String action = getDynamic(_Session, "ACTION").getString();
    
    if ( action.equalsIgnoreCase("start") ){
      
    	cfArgStructData functionArgs = new cfArgStructData(true);
    	functionArgs.setData("name", new cfStringData("default") );
      if ( !containsAttribute("CFCMAIL") )
        throw newRunTimeException("Please specify the cfcmail attribute");
      
      functionArgs.setData("cfcmail", getDynamic(_Session, "CFCMAIL") );
      
      if ( containsAttribute("PORT") )
      	functionArgs.setData("port", getDynamic(_Session, "PORT") );
      
      if ( containsAttribute("APPLICATION") )
      	functionArgs.setData("application", getDynamic(_Session, "APPLICATION") );

      if ( containsAttribute("IPADDRESS") )
      	functionArgs.setData("ip", getDynamic(_Session, "IPADDRESS") );

      if ( containsAttribute("CFCFILTER") )
      	functionArgs.setData("cfcfilter", getDynamic(_Session, "CFCFILTER") );

      SmtpStartFunction func = new SmtpStartFunction();
      func.execute(_Session, functionArgs );

    } else if ( action.equalsIgnoreCase("stop") ){

    	cfArgStructData functionArgs = new cfArgStructData(true);
    	functionArgs.setData("name", new cfStringData("default") );
    	SmtpStopFunction func = new SmtpStopFunction();
    	func.execute(_Session, functionArgs );

    } else if ( action.equalsIgnoreCase("status") ){

    	cfArgStructData functionArgs = new cfArgStructData(true);
    	functionArgs.setData("name", new cfStringData("default") );
    	SmtpStatusFunction func = new SmtpStatusFunction();
    	_Session.setData( getDynamic(_Session,"RESULTVAR").getString(), func.execute(_Session, functionArgs ) );

    }

    return cfTagReturnType.NORMAL;
  }
  
}