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

package org.alanwilliamson.mail25.matchers;

import java.util.Collection;

import javax.mail.MessagingException;

import org.apache.mailet.Mail;
import org.apache.mailet.Matcher;
import org.apache.mailet.MatcherConfig;

public class BaseMatcher implements Matcher {

  private MatcherConfig matcherConfig;
  
  public void destroy() {
  }

  public MatcherConfig getMatcherConfig() {
    return matcherConfig;
  }

  public String getMatcherInfo() {
    return "BaseMatcher";
  }

  public void init(MatcherConfig matcherConfig) throws MessagingException {
    this.matcherConfig = matcherConfig;
  }

  public Collection match(Mail inMail) throws MessagingException {
    return null;
  }
}
