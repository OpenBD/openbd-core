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

import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.mailet.MailetConfig;
import org.apache.mailet.MailetContext;

public class Mail25Config implements MailetConfig {

  private MailetContext mailetContext; 
  private String        mailetName;
  private HashMap       params;
  
  public Mail25Config(MailetContext mailetContext, String mailetName, String paramString){
    this.mailetContext  = mailetContext;
    this.mailetName     = mailetName;
    this.params         = new HashMap();
    
    //-- Parse up the parameters
    if ( paramString != null ){
      StringTokenizer st = new StringTokenizer( paramString, "," );
      while ( st.hasMoreTokens() ){
        String keyvaluepair = st.nextToken();
        
        int c1 = keyvaluepair.indexOf("=");
        if ( c1 != -1 && c1 < keyvaluepair.length() ){
          params.put( keyvaluepair.substring(0,c1), keyvaluepair.substring(c1+1) );
        }
      }
    }
  }

  public MailetContext getMailetContext() {
    return mailetContext;
  }

  public String getMailetName() {
    return mailetName;
  }
  
  public String getInitParameter(String key) {
    return (String)params.get(key);
  }

  public Iterator getInitParameterNames() {
    return params.keySet().iterator();
  }
}
