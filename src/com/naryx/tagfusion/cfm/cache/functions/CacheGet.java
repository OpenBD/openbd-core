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
 *  $Id: CacheGet.java 2179 2012-07-12 23:43:12Z alan $
 */
package com.naryx.tagfusion.cfm.cache.functions;


import com.naryx.tagfusion.cfm.cache.CacheFactory;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class CacheGet extends functionBase {
	
	private static final long serialVersionUID = 1;
    
	public CacheGet() { 
		min = 1; max = 2;
		setNamedParams( new String[]{ "id", "region" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"the unique ID of the object/string in the cache",
			"the region engine to use - default 'DEFAULT'"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"cache", 
				"Returns the data addressed by the id; returns a null if the object does not exist (use IsNull() to test)", 
				ReturnType.OBJECT );
	} 

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String id =  getNamedStringParam( argStruct, "id", null);
		if ( id == null )
			throwException( _session, "please specify the id" );

		String region = getNamedStringParam( argStruct, "region", "default");

		/* Make sure the cache factory is actually enabled */
		if ( !CacheFactory.isCacheEnabled(region) )
			throwException( _session, "'" + region + "' is an invalid cache region" );

		/* Retreive the cached value */
		cfData d = CacheFactory.getCacheEngine(region).get( id );
		return ( d == null ) ? cfNullData.NULL : d;
	}
}
