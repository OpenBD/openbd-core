/* 
 *  Copyright (C) 1996 - 2011 Alan Williamson
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
 *  $Id: Chain.java 1745 2011-10-25 15:43:15Z alan $
 */

package org.alanwilliamson.mail25.chain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.MessagingException;

import org.alanwilliamson.mail25.mailet.CaughtMail;
import org.alanwilliamson.mail25.mailet.Mail25Config;
import org.alanwilliamson.mail25.mailet.MatcherConfigConcrete;
import org.alanwilliamson.mail25.server.Mail25;
import org.apache.mailet.Mail;
import org.apache.mailet.MailAddress;
import org.apache.mailet.Mailet;
import org.apache.mailet.Matcher;

public class Chain extends Object {

  private ArrayList matcherList;
  private ArrayList mailetList;
  private Mail25 mail25;
  private String chainName;
  
  public Chain( String chainName, Properties config, Mail25 mail25 ) throws Exception{
    this.chainName  = chainName;
    this.mail25  		= mail25;
  
    //-- Setup the chain
    matcherList = new ArrayList();
    mailetList  = new ArrayList();

    //-- Need to run through looking for the parameters
    String matchers = config.getProperty( "spoolchain." + chainName + ".matchers" );
    if ( matchers == null )
      throw new Exception( "ChainCreationException: [spoolchain." + chainName + ".matchers] was not defined" );
    
    StringTokenizer st  = new StringTokenizer( matchers, "," );
    while ( st.hasMoreTokens() ){
      String matcherName  = st.nextToken();
      
      //- Pull out the matcher class
      String matcherClass = config.getProperty( "matcher." + matcherName );
      if ( matcherClass == null )
        throw new Exception( "ChainCreationException: [matcher." + matcherName + "] was not defined. Should be a class name" );
      
      //- Pull out the matcher condition, which can be null; thats valid
      String matcherCondition = config.getProperty( "matcher." + matcherName + ".condition" );
      
      //- Pull out the mailet details
      String mailetClass  = config.getProperty( "matcher." + matcherName + ".mailet" );
      if ( mailetClass == null )
        throw new Exception( "ChainCreationException: [matcher." + matcherName + ".mailet] was not defined. Should be a class name" );

      //- Pull out the mailet params; null params is okay
      String mailetParams = config.getProperty( "matcher." + matcherName + ".mailetparams" );
      
      
      //-- Let us attempt to create the Matcher class
      Matcher matcherInst;
      try{
        Class c     = Class.forName( matcherClass );
        matcherInst = (Matcher)c.newInstance();
        matcherInst.init( new MatcherConfigConcrete( matcherName, matcherCondition, mail25.getMailContext() ) );
      }catch(Exception e){
        throw new Exception( "ChainCreationException: [matcher." + matcherName + "] " + e.getMessage() );
      }
      
      
      //-- Let us attempt to create the Mailet class
      Mailet mailetClassInst;
      try{
        Class c         = Class.forName( mailetClass );
        mailetClassInst = (Mailet)c.newInstance();
        mailetClassInst.init( new Mail25Config( mail25.getMailContext(), matcherName + "Mailet", mailetParams ) );
      }catch(Exception e){
        throw new Exception( "ChainCreationException: [matcher." + matcherName + ".mailet] " + mailetClass + ":" + e.getMessage() );
      }
      
      //- Gets here, everything is good
      matcherList.add( matcherInst );
      mailetList.add( mailetClassInst );
    }
  }
  
  public String toString(){
    return "Chain." + chainName + ":";
  }
  
  public void shutdown(){
    for ( int x=0; x<matcherList.size(); x++ ){
      ((Matcher)matcherList.get(x)).destroy();
    }

    for ( int x=0; x<mailetList.size(); x++ ){
      ((Mailet)mailetList.get(x)).destroy();
    }

    matcherList = null;
    mailetList = null;
  }
  
  
  public void acceptMail( CaughtMail mail ){
    runMailThroughChain( mail );
  }
  
  //-------------------------------------------------------------------
  //-- Private Methods
  
  private void runMailThroughChain( CaughtMail mail ){
    
    for ( int x=0; x<matcherList.size(); x++ ){
      Matcher matcher = (Matcher)matcherList.get(x);
      
      Collection matchedRecipients;
      
      //- Get the list of matched recipients
      try {
        matchedRecipients = matcher.match( mail );

        //- No more recipents to match; its game over for this processing
        if ( matchedRecipients == null || matchedRecipients.size() == 0 )
          return;

        
        if ( !checkRecipientTypes(matchedRecipients) ){
        	mail25.log( toString() + matcher.getMatcherInfo() + ": returned invalid objects as recipients");
          return;
        }
        
      } catch (MessagingException e) {
      	mail25.log( toString() + matcher.getMatcherInfo() + ": MessagingException: " + e.getMessage() );
        return;
      }
      
      //- Set the mail recipents for this mail
      mail.setRecipients( matchedRecipients );

      
      //- Run the mailet associated
      Mailet  mailet = (Mailet)mailetList.get(x);
      String oldState = mail.getState();
      
      try {
        long runTime  = System.currentTimeMillis();
        
        mailet.service( mail );
        
        runTime = System.currentTimeMillis() - runTime;
        
        //- Set the attribute into the mailet incase it wants to see execution time
        mail.setAttribute( "MAILCATCHER.EXECTIME", String.valueOf(runTime) );

      } catch (Exception e) {
      	mail25.log( toString() + "[ERROR]=" + mailet.getMailetInfo() + "; from=" + mail.getSender() + "; Exception:" + e.getMessage() );
        return;
      }


      if ( mail.getState().equals( Mail.GHOST) ){
        //-- If the STATE is GHOST then we remove the mail from this process
        return;
      }else if ( mail.getState().equals( Mail.ERROR) ){
        //- The Mailet indicated an error with this email; so disregard and log it
      	mail25.log( toString() + "[STATE=ERROR]=" + mailet.getMailetInfo() + "; from=" + mail.getSender() + "; Error:" + mail.getErrorMessage() );
        return;
      } else if ( !mail.getState().equals(oldState) ){
        //- The mailet has decided it wants to move to another chain
        mail25.acceptMail( mail.getState(), mail );
        return;
      }
    }
  }
  
  private boolean checkRecipientTypes(Collection r){
    
    /* Since we are relying on a third party class to provide
     * us with the list of recipients of a mail message we
     * have to make sure they are giving us back the right
     * object types.
     */
    
    Iterator it = r.iterator();
    while (it.hasNext()){
      if ( ! (it.next() instanceof MailAddress) )
        return false;
    }
    return true;
  }
}
