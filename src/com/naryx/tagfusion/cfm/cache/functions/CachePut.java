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
 *  $Id: CachePut.java 2179 2012-07-12 23:43:12Z alan $
 */
package com.naryx.tagfusion.cfm.cache.functions;


import ucar.unidata.util.DateUtil;

import com.naryx.tagfusion.cfm.cache.CacheFactory;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class CachePut extends functionBase {
	
	private static final long serialVersionUID = 1;
    
	public CachePut() { 
		min = 2; max = 6;
		setNamedParams( new String[]{ "id", "value", "timespan", "idlespan", "region", "throwonerror" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"the unique ID of the object/string in the cache",
			"the object to store into the cache",
			"the time span that this object should live for in the cache; use CacheTimeSpan() as it is a decimal of 1 day",
			"the time span this this object will be removed from the cache is not used",
			"the region engine to use - default 'DEFAULT'",
			"if the region does not exist, throw an exception (default false)"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"cache", 
				"Places the object into the cache", 
				ReturnType.BOOLEAN );
	} 

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String id =  getNamedStringParam( argStruct, "id", null);
		if ( id == null )
			throwException( _session, "please specify the id" );

		cfData value = getNamedParam( argStruct, "value", null);
		if ( value == null )
			throwException( _session, "please specify the value" );

		
		/* Make sure the cache factory is actually enabled */
		String region = getNamedStringParam( argStruct, "region", "default" );
		if ( !CacheFactory.isCacheEnabled(region) ){
			if ( getNamedBooleanParam(argStruct, "throwonerror", false) )
				throwException( _session, "'" + region + "' is an invalid cache region" );
			
			return cfBooleanData.FALSE;
		}
		
		/* Time considerations */
		double timespan	= getNamedDoubleParam(argStruct, "timespan", -1 );
		long ageMs	= (long)(timespan * (double)DateUtil.MILLIS_DAY);

		double idlespan	= getNamedDoubleParam(argStruct, "idlespan", -1 );
		long idleTime	= (long)(idlespan * (double)DateUtil.MILLIS_DAY);

		CacheFactory.getCacheEngine(region).set(id, value, ageMs, idleTime);

		/* Retrieve the Cached value */
		return cfBooleanData.TRUE;
	}

}