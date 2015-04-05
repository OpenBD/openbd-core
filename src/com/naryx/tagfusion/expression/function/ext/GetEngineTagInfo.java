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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.expression.function.functionBase;

public class GetEngineTagInfo extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public GetEngineTagInfo(){  min = 1;  max = 1; }
  
	public String[] getParamInfo(){
		return new String[]{
			"tag name"	
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"engine", 
				"For the given core TAG name, returns back the metadata (summary, category, attrubute array of structs)", 
				ReturnType.STRUCTURE );
	}
	
	public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
		String tagName	= parameters.get(0).getString().toUpperCase();
		
		String classPath	= com.naryx.tagfusion.cfm.engine.cfEngine.thisInstance.TagChecker.getClass( tagName );
		if ( classPath == null )
			throwException(_session, "No such tag exists: " + tagName );
		
		cfStructData	s	= new cfStructData();
		
		// Load the class
		cfTag tag = null;
		try{
			Class<?> C = Class.forName( classPath ); 
			tag = (cfTag)C.newInstance();
		}catch (Exception e){
			throwException(_session, "Failed to load tag: " + tagName );
		}
		
		Map<String,String>	metainfo	= tag.getInfo();
		Iterator<String>	keys	= metainfo.keySet().iterator();
		while ( keys.hasNext() ){
			String key = keys.next();
			s.setData( key, new cfStringData( metainfo.get(key) ) );
		}
		
		s.setData("endtag", tag.getEndMarker() == null ? cfBooleanData.FALSE : cfBooleanData.TRUE );
		
		cfArrayData	array	= cfArrayData.createArray(1);
		Map<String,String>	attributes[] = tag.getAttInfo();
		
		for ( int x=0; x < attributes.length; x++ ){
			Map<String,String>	att = attributes[x];	
			
			cfStructData	a	= new cfStructData();
			keys	= att.keySet().iterator();
			while ( keys.hasNext() ){
				String key = keys.next();
				a.setData( key, new cfStringData( att.get(key) ) );
			}
			
			array.addElement( a );
		}

		s.setData("attributes", array );
		return s;
	}
}
