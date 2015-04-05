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
 */

package org.alanwilliamson.mail25.mailet;

import javax.mail.MessagingException;

import org.apache.mailet.Mail;
import org.apache.mailet.Mailet;
import org.apache.mailet.MailetConfig;


/**
 * Purpose of this Mailet is to do nothing, just stop processing the mail
 * 
 * @author Alan Williamson
 *
 */
public class NullMailet implements Mailet {

  MailetConfig config;
  
  public void destroy() {}

  public String getMailetInfo() {
    return null;
  }

  public MailetConfig getMailetConfig() {
    return config;
  }

  public void init(MailetConfig config) throws MessagingException {
    this.config = config;
  }

  public void service(Mail mail) throws MessagingException {
    mail.setState( Mail.GHOST );
  }
}
