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

package com.allaire.cfx;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This is a class used for debugging CFX
 */


public class DebugResponse implements Response {
	StringBuilder	outputBuffer, debugBuffer;
	Hashtable			variables;
	
	public DebugResponse(){
		outputBuffer	= new StringBuilder(128);
		debugBuffer		= new StringBuilder(128);
		variables			= new Hashtable();
	}
	
	public void 	write(String output){
		outputBuffer.append( output );
	}
	
	public void 	setVariable(String name, String value) throws IllegalArgumentException{
		variables.put( name, value );
	}
	
	public Query 	addQuery(String name,String[] columns) throws IllegalArgumentException{
		return new DebugQuery( name, columns );
	}
	
	public void 	writeDebug(String output){
		debugBuffer.append( output );
	}
	
	public void printResults(){
		System.out.println( "-] Debug Output --------------" );
		System.out.println( "-] OutputBuffer:" );
		System.out.println( outputBuffer.toString() );
		
		System.out.println( "" );
		System.out.println( "-] DebugBuffer:" );
		System.out.println( debugBuffer.toString() );

		System.out.println( "" );
		System.out.println( "-] variables:" );
		Enumeration E = variables.keys();
		while ( E.hasMoreElements() ){
			String key = (String)E.nextElement();
			System.out.println( "Key=[" + key + "] Data=[" + (String)variables.get(key) + "]" );
		}
				
		System.out.println( "-]----------------------------" );
	}
}
