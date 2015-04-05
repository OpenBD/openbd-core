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
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class GetEngineFunctionInfo extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public GetEngineFunctionInfo(){  min = 1;  max = 1; }
  
	public String[] getParamInfo(){
		return new String[]{
			"function name"	
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"engine", 
				"For the given core function name, returns back the metadata (summary, category, returntype, param array, min and max params, namedparamsupport, namedparams)", 
				ReturnType.STRUCTURE );
	}
	
	public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException {
		String functionName	= parameters.get(0).getString().toLowerCase();
		
		Map<String, String> hm = com.naryx.tagfusion.expression.compile.expressionEngine.getFunctions();
		if ( !hm.containsKey( functionName ) )
			throwException(_session, "No such function exists: " + functionName );
		
		cfStructData	s	= new cfStructData();
		
		functionBase	function	= com.naryx.tagfusion.expression.compile.expressionEngine.getFunction( functionName );
		
		Map<String,String>	metainfo	= function.getInfo();
		Iterator<String>	keys	= metainfo.keySet().iterator();
		while ( keys.hasNext() ){
			String key = keys.next();
			s.setData( key, new cfStringData( metainfo.get(key) ) );
		}
		
		s.setData("min", new cfNumberData( function.getMin() ) );
		s.setData("max", new cfNumberData( function.getMax() ) );
		
		s.setData("namedparamsupport", cfBooleanData.getcfBooleanData( function.supportNamedParams() ) );
		if ( function.supportNamedParams() ){
			cfArrayData	array	= cfArrayData.createArray(1);
			List<String>	params	= function.getFormals();
			for ( int x=0; x < params.size(); x++ )
				array.addElement( new cfStringData( params.get(x) ) );
			
			s.setData("namedparams", array );
		}
		
		cfArrayData	array	= cfArrayData.createArray(1);
		String[]	params	= function.getParamInfo();
		for ( int x=0; x < params.length; x++ )
			array.addElement( new cfStringData( params[x] ) );
		
		s.setData("params", array );
		return s;
	}
}
