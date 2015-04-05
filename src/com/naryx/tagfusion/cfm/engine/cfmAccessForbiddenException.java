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

package com.naryx.tagfusion.cfm.engine;

import com.naryx.tagfusion.cfm.file.cfFile;

/**
 * This class and it's use was added when enforcment of Allowed IPs on a per-BDA basis was implemented.
 * Some relevant areas of code include:
 * com.naryx.tagfusion.cfm.file.cfmlFileCache.loadRepositories()
 * ------------------------------------------.repositoryBDAs()
 * ------------------------------------------.getCfmFile() [which now takes REQ as a parameter and sends it down to bdFileArchive.getFile()].
 * 
 * getFile() will throw this Exception type, so all callers had to be updated to bubble that Exception on up to cfEngine.service().
 *
 */

public class cfmAccessForbiddenException extends cfmBadFileException
{
  private static final long serialVersionUID = 1L;

  public cfmAccessForbiddenException(cfCatchData _cfData)
  {
	super(_cfData);
  }

  public void handleException( cfFile svrFile, cfSession _Session ) {
	  handleException( _Session );
  }
  
  public void handleException(cfSession _Session)
  {
  	_Session.clearCfSettings();
	_Session.reset();
	_Session.setSuppressWhiteSpace(false);
		
	try
	{
    	_Session.setStatus( 403, "Access Forbidden" );
    }
    catch(cfmRunTimeException ignored){}		
    
    StringBuilder	buffer	= new StringBuilder( 1024 );
    String title = "Access Forbidden";
		
  	buffer.append( "\r\n<P><HTML><TITLE>" );
	buffer.append( title + "</TITLE><BODY>" );
        
    String fontSize = ( _Session.isWindowsOrMacUser() ? "11px" : "13px" );
      
	buffer.append( "<STYLE><!--\r\nTD {font-family :  Verdana, Geneva, Arial, Helvetica, sans-serif; font-size : " + fontSize + "; } //--></STYLE>" );
  	
	buffer.append( "<TABLE WIDTH='100%' BORDER='1' CELLSPACING='0' CELLPADDING='5' BGCOLOR='White'>");
    buffer.append( "<TR BGCOLOR='ORANGE'><TD COLSPAN='2' ALIGN='LEFT'><B><FONT SIZE='+1'>" );
	buffer.append( title + "</B></FONT></TD></TR><TR><TD WIDTH='1%' NOWRAP>Request</TD><TD>" );
	buffer.append( _Session.getRequestURI() ); 
	buffer.append( "</TD></TR>" );
			
	buffer.append( "</TABLE><P>" );

	buffer.append( "<TABLE WIDTH='100%' BORDER='1' CELLSPACING='0' CELLPADDING='5' BGCOLOR='White'>");
    buffer.append( "<TR BGCOLOR='ORANGE'><TD ALIGN='LEFT'>" );
	buffer.append( cfEngine.PRODUCT_NAME );
	buffer.append( " Time @ Server: " );
	buffer.append( com.nary.util.Date.formatNow( "kk:mm:ss.SSS   EEEE, d MMMM yyyy " ) );
	buffer.append( "</TD></TR></TABLE><P></BODY></HTML>\r\n\r\n" );
				
    _Session.write( buffer.toString() );
    
	try
	{
		_Session.pageEnd();
	}
	catch(Exception ignoreE){}
  } 
}
  
