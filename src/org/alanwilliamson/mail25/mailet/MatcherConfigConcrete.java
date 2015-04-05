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

import org.apache.mailet.MailetContext;
import org.apache.mailet.MatcherConfig;

public class MatcherConfigConcrete implements MatcherConfig {

  private String name;
  private String condition;
  private MailetContext mailContext;
  
  public MatcherConfigConcrete( String name, String condition, MailetContext mailContext ){
    this.name = name;
    this.condition = condition;
    this.mailContext = mailContext;
  }
  
  public String getCondition() {
    return condition;
  }

  public MailetContext getMailetContext() {
    return mailContext;
  }

  public String getMatcherName() {
    return name;
  }
}
