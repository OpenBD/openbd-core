/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: CacheSetProperties.java 2131 2012-06-27 19:02:26Z alan $
 */
package com.naryx.tagfusion.cfm.cache.functions;


import com.naryx.tagfusion.cfm.cache.CacheFactory;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class CacheSetProperties extends functionBase {
	
	private static final long serialVersionUID = 1;

	public CacheSetProperties() { 
		min = 1; max = 2;
		setNamedParams( new String[]{ "region", "props" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"the region engine to call this",
			"the properties to pass to this (struct) See below for the possible values",
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"cache", 
				"Changes the properties of the cache engine if the underlying implementation supports it", 
				ReturnType.BOOLEAN );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String region = getNamedStringParam( argStruct, "region", null );
		if ( region == null ){
			throwException(_session, "Please specify the region");
		}

		cfStructData props = null;
		cfData	data	= getNamedParam( argStruct, "props", null );
		
		if ( data == null ){
			props	= new cfStructData();
		}else if ( !data.isStruct() )
			throwException(_session, "Please specify the props as a structure");
		else
			props	= (cfStructData)data;
		
		
		if ( CacheFactory.isCacheEnabled(region) ){
			try {
				CacheFactory.getCacheEngine(region).setProperties(region, props);
			} catch (Exception e) {
				throwException(_session, e.getMessage());
			}
		}

		return cfBooleanData.TRUE;
	}
}