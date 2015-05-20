/* 
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
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
 */

package com.naryx.tagfusion.expression.function.ext;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.expression.function.functionBase;

public class GetEngineTagCategories extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public GetEngineTagCategories(){  min = 0;  max = 0; }
  
	public Map<String,String> getInfo(){
		return makeInfo(
				"engine", 
				"Returns back an array of all the tag categories", 
				ReturnType.ARRAY );
	}

	public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
		Set<String>	categorySet	= new HashSet<String>();
		
		// Collect up all the categories
		List<String> HT = cfEngine.thisInstance.TagChecker.getSupportedTags();
		Iterator<String> it = HT.iterator();
		while ( it.hasNext() ){
			String tagName	= it.next(); 
			cfTag tag = null;
			try{
				Class<?> C = Class.forName( cfEngine.thisInstance.TagChecker.getClass( tagName ) ); 
				tag = (cfTag)C.newInstance();
				categorySet.add( (String)tag.getInfo().get("category") );
			}catch (Exception e){}
		}
		
		// Convert to an array 
		cfArrayData	array	= cfArrayData.createArray(1);
		Iterator<String> iter	= categorySet.iterator();
		while ( iter.hasNext() ){
			array.addElement( new cfStringData( iter.next() ) );
		}
		
		array.sortArray( "text", "asc" );
		return array;
	}
	
}
