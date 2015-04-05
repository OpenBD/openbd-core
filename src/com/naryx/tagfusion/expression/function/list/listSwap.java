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

package com.naryx.tagfusion.expression.function.list;

import java.util.ArrayList;
import java.util.List;

import com.nary.util.stringtokenizer;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class listSwap extends functionBase {
	private static final long serialVersionUID = 1L;

	public listSwap() {
		min = 3;
		max = 4;
		setNamedParams( new String[]{ "list","position1","position2","delimiter"} );
	}

	public String[] getParamInfo(){
		return new String[]{
			"list",
			"position1",
			"position2",
			"delimiter - default comma (,)"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"list", 
				"Swaps the positions of two elements within the list, returning back the new list", 
				ReturnType.STRING );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String list = getNamedStringParam( argStruct, "list" , "" );
		String delimiter = getNamedStringParam( argStruct, "delimiter" , "," );
		int position1 = getNamedIntParam( argStruct, "position1" ,0 );
		int position2 = getNamedIntParam( argStruct, "position2" ,0 );

		if (delimiter.length() == 0) {
			throwException(_session, "Invalid delimiter specified. The delimiter value must contain at least one character.");
		}

		if (position1 <= 0) {
			throwException(_session, "Invalid list index(1): " + position1);
		}
		if (position2 <= 0) {
			throwException(_session, "Invalid list index(2): " + position2);
		}

		// Note that we can't use java.util.StringTokenizer nor
		// com.nary.util.stringtokenizer
		// here because we need to maintain the list delimiters used in the original
		// list
		// and there may be more than one delimiter char.
		StringBuilder newList = new StringBuilder( list.length() );

		List<String>	listArr	= new ArrayList<String>();
		stringtokenizer st = new stringtokenizer(list, delimiter);
		while (st.hasMoreTokens()) {
			listArr.add( st.nextToken() );
		}
		
		if ( listArr.size() <= position1 )
			throwException(_session, "Invalid list index(1): " + position1);

		if ( listArr.size() <= ( position2 - 1 ))
			throwException(_session, "Invalid list index(2): " + position2);
		
		String el1	= listArr.get( position1 - 1 );
		String el2	= listArr.get( position2 - 1 );
		
		listArr.set(  position1 - 1, el2 );
		listArr.set(  position2 - 1, el1 );
		
		for ( int x=0; x < listArr.size(); x++ ){
			newList.append( listArr.get(x) );
			if ( x < listArr.size()-1 )
				newList.append( delimiter );
		}

		return new cfStringData( newList.toString() );
	}
}
