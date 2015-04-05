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

package com.naryx.tagfusion.cfx;

import java.util.Iterator;
import java.util.Map;

import com.allaire.cfx.Query;
import com.allaire.cfx.Request;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.parser.runTime;

public class sessionRequest extends Object implements Request {

	private cfSession	session;
	private Map			properties;
	private boolean		debugOn;

	public sessionRequest( cfSession _session, Map _properties, boolean _debugOn ){
		session		= _session;
		properties	= _properties;
		debugOn		= _debugOn;
	}

	public boolean 	attributeExists(String name){
		return properties.containsKey( name );
	}

	public String 	getAttribute(String name){
		return getAttribute( name, "" );
	}
	
	public String 	getAttribute(String name, String _default){
		String V = (String)properties.get( name );
		if ( V != null )
			return V;
		else
			return _default;
	}
	
	public int getIntAttribute(String name) throws NumberFormatException{
		return getIntAttribute( name, -1 );
	}

	public int getIntAttribute(String name, int _default ) throws NumberFormatException{
		return com.nary.util.string.convertToInteger( (String)properties.get( name ), _default );
	}
	
	public String[] getAttributeList(){
		String keys[] = new String[ properties.size() ];
		Iterator iter = properties.keySet().iterator();
		for ( int indx = 0; iter.hasNext(); indx++ ) {
			keys[ indx ] = (String)iter.next();
		}
		return keys;
	}
	
	public Query 		getQuery(){
		if ( !attributeExists("QUERY") )
			return null;
		
		try{
  		cfQueryResultData	queryData	= (cfQueryResultData)runTime.runExpression( session, (String)properties.get("QUERY") );
  		return new sessionQuery( (String)properties.get("QUERY"), queryData );
  	}catch(Exception E){
			return null;
  	}
	}
	
	public String 	getSetting(String name){
		return getAttribute( name );
	}
	
	public boolean debug() {
		return debugOn;	
	}
}
