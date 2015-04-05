/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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

package org.alanwilliamson.amazon.simpledb;

import java.util.Iterator;
import java.util.Map;

import org.aw20.amazon.SimpleDB;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class GetSDBAttributes extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public GetSDBAttributes(){  min = 3; max = 4; setNamedParams( new String[]{ "datasource", "domain", "itemname", "consistentread" } ); }

	public String[] getParamInfo(){
		return new String[]{
			"Amazon SimpleDB Datasource",
			"domain",
			"The itemName name",
			"Defaults to false.  This is a flag that controls whether or not SimpleDB should wait for all the replication nodes to return before failing"
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"amazon", 
				"Amazon SimpleDB: Gets all the attributes for the given domain and ItemName", 
				ReturnType.STRUCTURE );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
 		String dsName			= getNamedStringParam(argStruct, "datasource", "");
  	String domainName	= getNamedStringParam(argStruct, "domain", "");
  	String itemName		= getNamedStringParam(argStruct, "itemname", "");
  	boolean cRead			= getNamedBooleanParam(argStruct, "consistentread", false );
  	
  	SimpleDB	sdb = SimpleDBFactory.getDS(dsName);
  	if ( sdb == null )
  		throwException(_session, "Invalid named Amazon Datasource");

  	try {
			
  		//Call out to Amazon
  		Map map = sdb.getAttributes(domainName, itemName, null, cRead );
  		cfStructData cfMap	= new cfStructData();
  		
  		for( Iterator<String> kit = map.keySet().iterator(); kit.hasNext(); ){
  			String key = kit.next();
  			cfMap.setData( key, new cfStringData( (String)((String[])map.get( key ))[0] ) );
  		}
  		
			return cfMap;
		} catch (Exception e) {
			throwException(_session, "AmazonSimpleDB: " + e.getMessage() );
			return cfBooleanData.FALSE;
		}
  }
}
