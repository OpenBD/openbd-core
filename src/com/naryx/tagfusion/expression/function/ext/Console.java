/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: Console.java 1796 2011-11-10 08:12:58Z alan $
 */

package com.naryx.tagfusion.expression.function.ext;

import java.util.List;
import java.util.ListIterator;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.expression.function.functionBase;


public class Console extends functionBase {
	private static final long serialVersionUID = 1L;

	protected static boolean	bConsoleOn = false;
	
	public Console() {
		min = 1;
		max = 10;
	}

	public String[] getParamInfo(){
		return new String[]{
			"object"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"debugging", 
				"If the console has been turned on, or the request is coming from 127.0.0.1/localhost (local), will output the variable(s) to the engine console", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		if ( bConsoleOn || isLocalIP(_session.REQ.getRemoteAddr()) ){
			List<cfData> dataParams = parameters;

			ListIterator<cfData> li = dataParams.listIterator(dataParams.size());
			
			// Not sure why, but it seems the items get here in reverse order
			while(li.hasPrevious()) {
				// moving cursor to previous element
	            cfData data = (cfData)li.previous();
	            
				if ( cfData.isSimpleValue(data) ){
					System.out.println( data.getString() );
				} else if ( data.getDataType() == cfData.CFQUERYRESULTDATA ){
					dumpQuery( (cfQueryResultData)data );
				} else if ( data.getDataType() == cfData.CFARRAYDATA ){
					dumpArray( (cfArrayData)data );
				} else if ( data.getDataType() == cfData.CFSTRUCTDATA ){
					dumpStruct( (cfStructData)data );
				} else {
					System.out.println( getString(data) );
				}
			}
			
		}

		return cfBooleanData.TRUE;
	}

	
	private boolean isLocalIP( String ip ){
		if ( ip != null && ip.equals("127.0.0.1") )
			return true;
		else
			return false;
	}
	
	
	private void dumpStruct(cfStructData data) throws dataNotSupportedException {
		StringBuilder s = new StringBuilder( 1024 );
		
		// Get the max width -----------------------
		Object[] keys = data.keys();
		int maxWidth = 0;
		for ( int c=0; c<keys.length;c++){
			if ( keys[c].toString().length() > maxWidth )
				maxWidth = keys[c].toString().length();
		}
		
		String strapLine = "+-";
		for ( int c=0; c<maxWidth; c++ )
			strapLine += "-";
		
		strapLine += "-+--";
		s.append( strapLine + "\r\n" );

		for ( int c=0; c<keys.length;c++){
			s.append("| ");
			
			String ct = keys[c].toString();
			for ( int p=0; p < (maxWidth-ct.length()); p++ )
				s.append( " " );
			
			s.append( ct );
			s.append(" | ");
			
			String tmp = getString( data.getData(keys[c].toString()) );
			
			if ( tmp.length() > (116-maxWidth) )
				tmp = tmp.substring(0,116-maxWidth);
			
			s.append( tmp );
			
			s.append( "\r\n" );

			
			
		}
		s.append( strapLine );
		
		System.out.println(s);
	}

	
	
	private void dumpArray(cfArrayData data) throws dataNotSupportedException, cfmRunTimeException {
		StringBuilder s = new StringBuilder( 1024 );
		
		int maxWidth	= String.valueOf(data.size()).length();
		String strapLine = "+-";
		for ( int c=0; c<maxWidth; c++ )
			strapLine += "-";
		
		strapLine += "-+--";
		
		s.append( strapLine + "\r\n" );
		
		for ( int r=0; r<data.size(); r++ ){
			s.append("| ");
			
			String ct = String.valueOf( r+1 );
			for ( int p=0; p < (maxWidth-ct.length()); p++ )
				s.append( " " );
			
			s.append( ct );
			s.append(" | ");
			
			String tmp = getString(data.getData(r+1));
			
			if ( tmp.length() > (116-maxWidth) )
				tmp = tmp.substring(0,116-maxWidth);
			
			s.append( tmp );
			
			s.append( "\r\n" );
		}
		
		s.append( strapLine );
		
		System.out.println(s);
	}

	
	private String getString( cfData d ) throws dataNotSupportedException{
		if ( cfData.isSimpleValue(d) ){
			return d.getString();
		}else{
			return "{CFML Type::" + d.getDataTypeName() + "}";
		}
	}
	
	
	private void dumpQuery(cfQueryResultData data) throws dataNotSupportedException {
		StringBuilder s = new StringBuilder( 1024 );
		
		String[] columns = data.getColumnNames();
		int[]	widths	= new int[columns.length];
		
		// Get the maximum widths ---------------------
		for ( int c=0; c < columns.length; c++ ){
			widths[c] = columns[c].length();
		}
		
		for ( int r=0; r < data.getSize(); r++ ){
			for ( int c=0; c < columns.length; c++ ){
				String out	= data.getCell( r+1, c+1, true ).getString();
				if ( widths[c] < out.length() )
					widths[c] = out.length();
			}
		}
		
		// Snip the widths -------------------------
		int totalWidth = 0;
		for ( int c=0; c < widths.length; c++ ){
			totalWidth += widths[c];
		}
		
		if ( totalWidth > 120 ){
			for ( int c=0; c < widths.length; c++ ){
				if ( widths[c] > 32 )
					widths[c] = 32;
			}
		}
				
		
		// Do the columns ---------------------
		String strapLine = "";
		for ( int x=0; x < columns.length; x++ ){
			strapLine	+= "+-";
			s.append( "| " );
			s.append( columns[x] );
			
			int pad = widths[x] - columns[x].length();
			for ( int p=0; p < pad; p++ )
				s.append( " " );

			s.append( " " );

			for ( int c1=0; c1 < widths[x]; c1++ )
				strapLine += "-";
			
			strapLine	+= "-";
		}
		s.append( "|\r\n" );
		strapLine	+= "+\r\n";
		
		s.insert( 0, strapLine );
		s.append( strapLine );
		
		
		// Do the rows ----------------------
		for ( int r=0; r < data.getSize(); r++ ){
			for ( int c=0; c < columns.length; c++ ){
				s.append( "| " );
				
				String out = data.getCell( r+1, c+1, true ).getString();
				if ( out.length() > widths[c] ){
					s.append( out.substring(0,widths[c]) );
				}else{
					s.append( out );
					int pad = widths[c] - out.length();
					for ( int p=0; p < pad; p++ )
						s.append( " " );
				}

				s.append( " " );
			}
			s.append( "|\r\n" );
		}
		
		s.append( strapLine );
		s.append( data.getSize() + " rows; " + data.getQuerySource() );
		
		System.out.println(s);
	}
}
