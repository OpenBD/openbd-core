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
 *  $Id: CacheRemove.java 2131 2012-06-27 19:02:26Z alan $
 */
package com.naryx.tagfusion.cfm.cache.functions;


import com.naryx.tagfusion.cfm.cache.CacheFactory;
import com.naryx.tagfusion.cfm.cache.CacheInterface;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class CacheRemove extends functionBase {
	
	private static final long serialVersionUID = 1;

	public CacheRemove() { 
		min = 1; max = 4;
		setNamedParams( new String[]{ "id", "region", "throwonerror", "exact" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"one or a list of IDs to delete",
			"the region engine to use - default 'DEFAULT'",
			"flag to control if an exception is thrown if the region does not exist",
			"whether the key should be exact - default TRUE (note not all caching engines support this)"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"cache", 
				"Removes one or more keys", 
				ReturnType.BOOLEAN );
	} 

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String ids =  getNamedStringParam( argStruct, "id", null);
		if ( ids == null )
			throwException( _session, "please specify the id" );

		
		String region = getNamedStringParam( argStruct, "region", "DEFAULT");
		if ( !CacheFactory.isCacheEnabled(region) ){
			if ( getNamedBooleanParam(argStruct, "throwonerror", false) )
				throwException( _session, "\"" + region + "\" is an invalid cache region" );
			
			return cfBooleanData.FALSE;
		}

		boolean exact = getNamedBooleanParam(argStruct, "exact", true);
		
		/* Retrieve the Cached value */
		String[]	idArr	= ids.split(",");
		CacheInterface ci = CacheFactory.getCacheEngine(region);
		for ( int x=0; x < idArr.length; x++ ){
			ci.delete(idArr[x], exact);
		}

		return cfBooleanData.TRUE;
	}
}
