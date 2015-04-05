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
 *  $Id: CacheRemoveAll.java 2131 2012-06-27 19:02:26Z alan $
 */
package com.naryx.tagfusion.cfm.cache.functions;


import com.naryx.tagfusion.cfm.cache.CacheFactory;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class CacheRemoveAll extends functionBase {
	
	private static final long serialVersionUID = 1;

	public CacheRemoveAll() { 
		min = 0; max = 1;
		setNamedParams( new String[]{ "region" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"the region engine to use - default 'DEFAULT'"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"cache", 
				"Removes all the data in the cache", 
				ReturnType.BOOLEAN );
	} 

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String region = getNamedStringParam( argStruct, "region", "DEFAULT");

		/* Make sure the cache factory is actually enabled */
		if ( !CacheFactory.isCacheEnabled(region) )
			throwException( _session, "\"" + region + "\" is an invalid cache region" );

		/* Retrieve the Cached value */
		CacheFactory.getCacheEngine(region).deleteAll();
		
		return cfBooleanData.TRUE;
	}
}
