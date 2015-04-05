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
 * This is a BlueDragon ONLY expression.
 * 
 * It takes a list of values and removes the duplicates.
 * 
 * Usage:  ListRemoveDuplicates( list [,delimiters] )
 * 
 */
package com.naryx.tagfusion.expression.function.list;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nary.util.FastMap;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class listRemoveDuplicates extends functionBase {

	private static final long serialVersionUID = 1L;

	public listRemoveDuplicates(){
  	min = 1; max = 2;
  	setNamedParams( new String[]{ "list","delimiter"} );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"list",
			"delimiter - default comma (,)"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"list", 
				"Removes all duplicate values within the list, returning back the new list", 
				ReturnType.STRING );
	} 
  
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String list = getNamedStringParam( argStruct, "list" ,"" );
		String delimiter = getNamedStringParam( argStruct, "delimiter" ,"," );
  
		//-- Run through the list
		Map<String, Object>	keyList = new FastMap<String, Object>();
		List<String> tokens = string.split( list, delimiter );
		
		for ( int i = 0; i < tokens.size(); i++ ){
			keyList.put( tokens.get(i), null );
		}
  	
  	//-- If both lists are the same size, then no duplicates
  	if ( keyList.size() != tokens.size() ){
  		
  		StringBuilder buffer	= new StringBuilder( list.length() );
			Iterator<String> it	= keyList.keySet().iterator();
			int	count			= 1;
			
			while ( it.hasNext() ){
				buffer.append( it.next() );
				
				if ( count < keyList.size() )
					buffer.append( delimiter );
				
				count++;
			}
			
			list = buffer.toString();
  	}
  	
  	return new cfStringData( list );
	}

}
