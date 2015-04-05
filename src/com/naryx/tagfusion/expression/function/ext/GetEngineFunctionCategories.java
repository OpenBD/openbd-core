/* 
 *  Copyright (C) 2000 - 2009 TagServlet Ltd
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

package com.naryx.tagfusion.expression.function.ext;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class GetEngineFunctionCategories extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public GetEngineFunctionCategories(){  min = 0;  max = 0; }
  
	public java.util.Map getInfo(){
		return makeInfo(
				"engine", 
				"Returns back an array of all the function categories", 
				ReturnType.ARRAY );
	}

	public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
		Set<String>	categorySet	= new HashSet<String>();
		
		// Collect up all the categories
		Map<String, String> hm = com.naryx.tagfusion.expression.compile.expressionEngine.getFunctions();
		Iterator<String> iter = hm.keySet().iterator();
		while ( iter.hasNext() ){
			functionBase	function	= com.naryx.tagfusion.expression.compile.expressionEngine.getFunction( iter.next() );
			Map<String,String>	metainfo	= function.getInfo();
			categorySet.add( metainfo.get("category") );
		}
		
		// Convert to an array 
		cfArrayData	array	= cfArrayData.createArray(1);
		iter	= categorySet.iterator();
		while ( iter.hasNext() ){
			array.addElement( new cfStringData( iter.next() ) );
		}
		
		array.sortArray( "text", "asc" );
		return array;
	}
	
}
