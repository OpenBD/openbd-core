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
 * Created on 13-Feb-2005
 * 
 * Implements the CFTIMER tag from CFMX7
 * 
 * Debugging must be enabled in the Administration for this tag to run
 * 
 * Additional Feature:
 * After each run through of this tag, the object 'cftimer' is set
 * that holds the number of milliseconds the run through happenned.
 * (Was noted in a blog entry that this was would be very useful)
 * 
 */
package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfTIMER extends cfTag implements Serializable {
  static final long serialVersionUID = 1;

  public java.util.Map getInfo(){
  	return createInfo("debugging", "Calculates the time it takes to execute a block of CFML code");
  }
  
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   		createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
 			createAttInfo("TYPE=DEBUG", 	"Outputs the execution time in the debugging panel", "DEBUG", false ),
 			createAttInfo("TYPE=OUTLINE", "Outputs the execution time in a FIELDSET tag", "", false ),
			createAttInfo("TYPE=INLINE", 	"Outputs the execution time at the current position as simple text", "", false ),
 			createAttInfo("TYPE=COMMENT", "Outputs the execution time as an HTML comment", "", false ),
 			createAttInfo("LABEL", "Optional label to associate with the output", "", false )
  	};
  }
  
  protected void defaultParameters( String _tag ) throws cfmBadFileException {
  	defaultAttribute( "LABEL", " " );
  	defaultAttribute( "TYPE",  "DEBUG" );
  	
    parseTagHeader( _tag );
  }

  public String getEndMarker() {
    return "</CFTIMER>";
  }
  
  public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
  	cfStructData attributes = setAttributeCollection(_Session);
    
    //-- Make sure the debugging is enabled; if not then simply execute and 
    //-- return as quickly and as little overhead as possible.
    if ( !_Session.isDebugEnabled() ){
      super.render( _Session );
      return cfTagReturnType.NORMAL;
    }
    
    
    String type	= getDynamic(attributes,_Session,"TYPE").getString().toLowerCase();
    
    if ( type.equals("outline") ){
      
      long sTime	= System.currentTimeMillis();
      String tagString = renderToString( _Session ).getOutput();
      sTime	= System.currentTimeMillis() - sTime;
      
      _Session.write( "<fieldset class=\"cftimer\"><legend>" + getDynamic(attributes,_Session,"LABEL").getString() + ": " + sTime + "ms</legend>" );
      _Session.write( tagString );
      _Session.write( "</fieldset>");
      
      _Session.setData( "cftimer", new cfNumberData( sTime ) );
      
    } else if ( type.equals("inline") ){
      
      long sTime	= System.currentTimeMillis();
      super.render( _Session );
      sTime	= System.currentTimeMillis() - sTime;
      _Session.write( getDynamic(attributes,_Session,"LABEL").getString() + ": " + sTime + "ms" );

      _Session.setData( "cftimer", new cfNumberData( sTime ) );
      
    } else if ( type.equals("comment") ){
      
      long sTime	= System.currentTimeMillis();
      super.render( _Session );
      sTime	= System.currentTimeMillis() - sTime;
      _Session.write( "<!-- " + getDynamic(attributes,_Session,"LABEL").getString() + ": " + sTime + "ms -->" );

      _Session.setData( "cftimer", new cfNumberData( sTime ) );

    } else if ( type.equals("debug") ){
      
      long sTime	= System.currentTimeMillis();
      super.render( _Session );
      sTime	= System.currentTimeMillis() - sTime;
      
      _Session.recordTimer( "[" + sTime + "ms] " + getDynamic(attributes,_Session,"LABEL").getString() );
    }
    
    return cfTagReturnType.NORMAL;
  }
}
