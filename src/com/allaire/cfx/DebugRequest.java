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

public class DebugRequest extends Object implements Request {

	Query query;
	Hashtable attributes, settings;
	
	public DebugRequest( Hashtable attributes ){
		this.attributes	= attributes;
		this.settings 	= new Hashtable();
		this.query			= null;
	}

	public DebugRequest( Hashtable attributes, Query query ){
		this.attributes	= attributes;
		this.settings 	= new Hashtable();
		this.query			= query;
	}
	
	public DebugRequest( Hashtable attributes, Query query, Hashtable settings ){
		this.attributes	= attributes;
		this.settings 	= settings;
		this.query			= query;
	}
	
	public boolean 	attributeExists(String name){
		if ( attributes == null )
			return false;
		else
			return attributes.containsKey( name );
	}
	
	public String getAttribute(String name){
		if ( attributes == null )
			return null;
		else
			return (String)attributes.get( name );
	}
	
	public String getAttribute(String name, String _default ){
		if ( attributes == null )
			return _default;
		else
			return (attributes.containsKey(name)) ? (String)attributes.get(name) : _default;	
	}
	
	public int getIntAttribute(String name) throws NumberFormatException{
		if ( attributes == null || (attributes.containsKey(name)) )
			throw new NumberFormatException();
		else 
			return java.lang.Integer.parseInt( (String)attributes.get(name) );	
	}
	
	public int getIntAttribute(String name, int _default) throws NumberFormatException{
		if ( attributes == null || attributes.containsKey(name) )
			return _default;
		else 
			return java.lang.Integer.parseInt( (String)attributes.get(name) );	
	}
	
	public String[] getAttributeList(){
		Enumeration E	= attributes.keys();
		String keys[]	= new String[ attributes.size() ];
		int		indx		= 0;
		while ( E.hasMoreElements() )
			keys[indx++]	= (String)E.nextElement();
			
		return keys;
	}

	public String 	getSetting(String name){
		if ( settings == null )
			return null;
		else
			return (String)settings.get( name );
	}

	public Query 		getQuery(){ 	return query; }
	public boolean	debug(){	return true;	}
}
