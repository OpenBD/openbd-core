/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

package com.naryx.tagfusion.expression.function.list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.nary.util.stringtokenizer;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class listCompact extends functionBase {
	private static final long serialVersionUID = 1L;

	public listCompact() {
		min = 1;
		max = 2;
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
				"Returns back a new list, with all the empty items removed from the start and end of the list", 
				ReturnType.STRING );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		String list = getNamedStringParam( argStruct, "list" ,"" );
		String delimiter = getNamedStringParam( argStruct, "delimiter" ,"," );

		// Tokenize the list to an array
		stringtokenizer st = new stringtokenizer(list, delimiter);
		List<String> array	= new ArrayList<String>();
		while (st.hasMoreTokens()) {
			array.add( st.nextToken() );
		}

		// Trim the array elements
		Iterator<String> it	= array.iterator();
		while ( it.hasNext() ){
			if ( it.next().length() == 0 )
				it.remove();
			else
				break;
		}
		
		// Trim from the end of the array
		while ( array.size() > 0 && array.get( array.size()-1 ).length() == 0 ){
			array.remove( array.size()-1 );
		}
		

		StringBuilder newList = new StringBuilder();
		it	= array.iterator();
		while ( it.hasNext() ){
			newList.append( it.next() );
			newList.append( delimiter );
		}
		if( newList.length() >0 )
			return new cfStringData( newList.toString().substring( 0, newList.length() - delimiter.length() ) );
		else
			return new cfStringData( newList.toString());
	}
}
