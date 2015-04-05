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

package com.naryx.tagfusion.util;


/**
 * This class is for collating and dumping all the information
 * related to exceptions as recorded when there is debug output enabled
 */
 
import java.util.ArrayList;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.tag.cfTag;

class debugExceptions{
  
  private List<exceptionWrapper> exceptions;
  
  public debugExceptions(){
    exceptions = new ArrayList<exceptionWrapper>();
  }
  
	
	/**
	 * adds the given exception to the list of exceptions
	 */
	 
  public void add( cfmRunTimeException exception, cfFile originF, cfTag originT ){
		cfCatchData catchD = exception.getCatchData();
		exceptionWrapper ew = new exceptionWrapper();
		ew.timestamp 	= System.currentTimeMillis(); 
		ew.message 		= getExceptionMessage( catchD );
		if ( originF != null ) {
			ew.template 	= originF.getName(); //catchD.getString( "template" );
		}
	    if ( originT != null ) {
		    ew.line 	= String.valueOf( originT.posLine );
	    }
		ew.type 	= catchD.getString( "type" );
		exceptions.add( ew );  
	}// addException()
  
  private static String getExceptionMessage( cfCatchData catchData ) {
  	StringBuilder sb = new StringBuilder().append( catchData.getMessage() );
	  String temp = catchData.getDetail();
	  if ( temp.length() > 0 ) {
		  sb.append( "; " + temp );
		  temp = catchData.getExtendedInfo();
		  if ( temp.length() > 0 ) {
			  sb.append( "; " + temp );
		  }
	  }
	  return sb.toString();
  }

  
	public void dump( cfSession session ){
		if ( exceptions.size() > 0 ){
		
			session.write( "<HR><b><div class=\"debughdr\">Exceptions</div></b>\n" );
			
			for ( int i = 0; i < exceptions.size(); i++ ){
				exceptionWrapper next = (exceptionWrapper) exceptions.get(i);
				session.write( "<p><div class=\"debug\"><b>[" );
				session.write( com.nary.util.Date.formatDate( next.timestamp, "HH:mm:ss" ) );
				session.write( "]&nbsp;" );
				session.write( next.type );
				session.write( " Exception in : " );
				session.write( next.template );
				session.write( " Line: " );
				session.write( next.line );
				session.write( "</b><p>" );
				session.write( next.message );
				session.write( "</p></div></p>" );
			}
		}
	}
  
	class exceptionWrapper{
		public long 	timestamp;
		public String message;
		public String type;
		public String template;
		public String line;		
		
	}
  
}
