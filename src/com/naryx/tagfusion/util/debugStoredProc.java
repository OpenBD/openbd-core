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
 * related to stored procedures as recorded when there is debug output enabled
 *
 */
 
import java.util.ArrayList;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.sql.preparedData;
import com.naryx.tagfusion.cfm.sql.resultSetHolder;

class debugStoredProc{
  
  // params - type, CFSQLTYPE, vale, variable, dbVarName
  // resultsets - name, resultset
  
  private List<storedProcInfo> storedprocs;
  
  public debugStoredProc(){
    storedprocs = new ArrayList<storedProcInfo>();
  }
  
  
  public boolean hasQueries(){
    return storedprocs.size() > 0;
  }
  
	
	/**
	 * adds the given storedproc
	 * to the list, extracting all the relevent data
	 */
	 
  public void add( String _template, String _datasrc, String name, long _time, 
										List<preparedData> _params, List<resultSetHolder> _resultsets ){
		
		storedProcInfo newInfo = new storedProcInfo();	
		newInfo.template = _template;
		newInfo.name = name;
		newInfo.datasource = _datasrc;
		newInfo.execTime = _time;
		
		newInfo.params = _params;
		newInfo.results = _resultsets;
		
		newInfo.timeStamp = System.currentTimeMillis();
		storedprocs.add( newInfo );
    
  }// addQuery()
  
	
	public void dump( cfSession session ){
        String fontSize = ( session.isWindowsOrMacUser() ? "10pt" : "12pt" );
		if ( storedprocs.size() > 0 ){
			session.write( "<style type=\"text/css\">\n" );
			session.write( ".storedproc\n{" );
			session.write( "    color:black;\n" );  
			session.write( "    background-color:white;\n" ); 
			session.write( "    font-family:\"courier\", arial, serif;\n" ); 
			session.write( "    font-size: " + fontSize + ";\n}\n" );
			session.write( ".sp_tablename\n{" );
			session.write( "    color: #CCCCCC;\n" );  
			session.write( "    background-color:#000099;\n" );
			session.write( "    padding:5px;\n" );
			session.write( "    border: 1px solid black;\n" );
			session.write( "    font-family: Verdana, Helvetica, Arial, sans-serif;\n" );
			session.write( "    font-weight: bold;\n" );
			session.write( "    font-size: " + fontSize + ";\n}\n" );
			session.write( ".sp_tableheader\n{" );
			session.write( "    color:black;\n" );  
			session.write( "    background-color:white;\n" );
			session.write( "    padding:5px;\n" );
			session.write( "    border: 1px solid black;\n" );
			session.write( "    font-family:\"courier\", arial, serif;\n" );
			session.write( "    font-weight: bold;\n" );
			session.write( "    font-size: " + fontSize + ";\n}\n" );
			session.write( ".sp_tabledata\n{" );
			session.write( "    color:black;\n" );  
			session.write( "    background-color:white;\n" );
			session.write( "    border: 1px solid black;\n" );
			session.write( "    padding:5px;\n" );
			session.write( "    font-family: courier, arial, serif;\n" );
			session.write( "    font-size: " + fontSize + ";\n}\n" );
			
			session.write( "</style>\n\n" );
			
			session.write( "<HR><p><b><div class=\"debughdr\">Stored Procedures</div></B></p>" );
		
			storedProcInfo next;
			for ( int i = 0; i < storedprocs.size(); i++ ){
				next = (storedProcInfo) storedprocs.get(i);
				session.write( "<div class=\"storedproc\"><p><b>" );
				session.write( next.name );
				session.write( "</b> (Datasource=" );
				session.write( next.datasource );
				session.write( ", Time=" );
				session.write( String.valueOf( next.execTime ) );
				session.write( "ms) in " );
				session.write( next.template );
				session.write( " @ " );
				session.write( com.nary.util.Date.formatDate( next.timeStamp, "HH:mm:ss" ) );
				session.write( "</p>" );

				// write out parameters				
				session.write( "<p><table border=0 cellpadding=0 cellspacing=0>\n<tr><td colspan=4 class=\"sp_tablename\">parameters</td></tr>\n" );
				session.write( "<tr><td class=\"sp_tableheader\">type</td><td class=\"sp_tableheader\">CFSQLType</td>" );
				session.write( "<td class=\"sp_tableheader\">value</td><td class=\"sp_tableheader\">variable</td></tr>\n" );
				
				List<preparedData> qParams = next.params;
				if ( qParams != null ){
					for ( int j = 0; j < qParams.size(); j++ ){
						session.write( "<TR>" );
						preparedData nextParam = qParams.get(j);
						
						// write out type
						if ( nextParam.isIN() && nextParam.isOUT() ){
							session.write( "<TD class=\"sp_tabledata\">IN-OUT</TD>" );	
						}else if ( nextParam.isIN() ){
							session.write( "<TD class=\"sp_tabledata\">IN</TD>" );
						}else{
							session.write( "<TD class=\"sp_tabledata\">OUT</TD>" );
						}
						
						// CFSQLType
						session.write( "<TD class=\"sp_tabledata\">" );
						session.write( nextParam.getSQLType() );
						session.write( "</TD>" );
						
						// value
						session.write( "<TD class=\"sp_tabledata\">" );
						
						try{
							String val = nextParam.getDataAsString();
							if ( ( val != null ) && ( val.length() > 0 ) ) {
								session.write( val );
							}else{
								session.write( "&nbsp;" );
							}
						}catch( Exception ignored ){} // catches NullPointerException too 
						session.write( " </TD>" );
						
						// variable
						session.write( "<TD class=\"sp_tabledata\">" );
						if ( nextParam.getOutVariable() != null ){
							session.write( nextParam.getOutVariable() );
						}else{
							session.write( "&nbsp;" );
						}
						session.write( "</TD>" );
						session.write( "</TR>" );
					}
					session.write( "</table></p>\n" );
					session.write( "</div>\n\n" );
					
				}
				
				// write out resultsets				
				session.write( "<p><table border=0 cellpadding=0 cellspacing=0>\n<tr><td class=\"sp_tablename\" colspan=\"2\">resultsets</td></tr>\n" );
				session.write( "<tr><td class=\"sp_tableheader\">name</td><td class=\"sp_tableheader\">resultset</td></tr>\n" );
				
				List<resultSetHolder> qResults = next.results;
				if ( qResults != null ){
					for ( int j = 0; j < qResults.size(); j++ ){
						session.write( "<TR>" );
						resultSetHolder nextRS = qResults.get(j);
							
						// write out name
						session.write( "<TD class=\"sp_tabledata\">" );
						session.write( nextRS.name );
						session.write( "</TD>" );
						
						// resultset
						session.write( "<TD class=\"sp_tabledata\">" );
						session.write( String.valueOf( nextRS.resultIndex ) );
						session.write( "</TD>" );
							
						session.write( "</TR>" );
					}
					session.write( "</table></p>\n" );
					session.write( "</div>\n\n" );
				}
			
			}
		}
	}
  
	
  class storedProcInfo{
  
    public String name;
    public String datasource;
    public String template;
		
		public long   execTime;
		
		public List<preparedData> 	params 	= null;
		public List<resultSetHolder> 	results = null;
		
    public long   timeStamp;
    
    
  }
  


}
