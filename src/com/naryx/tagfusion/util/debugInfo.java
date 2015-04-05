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
 * This class is for collating and dumping all the debug info
 */
 
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;

class debugInfo{
  
	
	public void dump( cfSession session ){
		//--] Collate Debug data and display it in a <table>
		try{
			cfStructData serverscope = (cfStructData) session.getQualifiedData( variableStore.SERVER_SCOPE ); 
			String prodVersion = serverscope.getData( "coldfusion" ).getData( "productversion" ).getString();
			String prodLevel   = cfEngine.PRODUCT_NAME + " " + serverscope.getData( "coldfusion" ).getData( "productlevel" ).getString();
			cfStructData cgiscope = (cfStructData) session.getQualifiedData( variableStore.CGI_SCOPE );   
			String template  = cgiscope.getData( "script_name" ).getString();
			String userAgent = cgiscope.getData( "http_user_agent" ).getString();
			String remoteIP  = cgiscope.getData( "remote_addr" ).getString();
			String hostname  = cgiscope.getData( "remote_host" ).getString();
			String timestamp = com.nary.util.Date.formatNow( "dd-MMM-yy hh:mm a" );
			
			session.write( "<HR><b><div class=\"debughdr\">Debugging Information</div></b>\n" );
			session.write( "<table class=\"debug\">\n" );
			session.write( "<tr>\n" );
			session.write( "			<td class=\"debug\" nowrap> " + prodLevel + " </td>\n" );
			session.write( "      <td class=\"debug\">" + prodVersion + "</td>\n" );
			session.write( "</tr>\n" );
			session.write( "<tr>\n" );
			session.write( "			<td class=\"debug\" nowrap> Template </td>\n" );
			session.write( "    	<td class=\"debug\">" + template + "</td>\n" );
			session.write( "</tr>\n" );
			session.write( "<tr>\n" );
			session.write( "			<td class=\"debug\" nowrap> Time stamp </td>\n" );
			session.write( "			<td class=\"debug\" nowrap>" + timestamp + "</td>\n" );
			session.write( "</tr>\n" );
			session.write( "<tr>\n" );
			session.write( "			<td class=\"debug\" nowrap> Locale </td>\n" );
			session.write( "			<td class=\"debug\" nowrap>" + session.getLocaleDisplayName() + "</td>\n" );
			session.write( "</tr>\n" );
			session.write( "<tr>\n" );
			session.write( "			<td class=\"debug\" nowrap> User Agent </td>\n" );
			session.write( "			<td class=\"debug\" nowrap>" + userAgent +  "</td>\n" );
			session.write( "</tr>\n" );
			session.write( "<tr>\n" );
			session.write( "			<td class=\"debug\" nowrap> Remote IP </td>\n" );
			session.write( "			<td class=\"debug\" nowrap>" + remoteIP + "</td>\n" );
			session.write( "</tr>\n" );
			
			session.write( "<tr>\n" );
			session.write( "			<td class=\"debug\" nowrap> Host Name </td>\n" );
			session.write( "			<td class=\"debug\" nowrap>" + hostname + "</td>\n" );
			session.write( "</tr>\n" );
			
			
			session.write( "</table>\n" );
		}catch( cfmRunTimeException e ){}
	}
 


}
