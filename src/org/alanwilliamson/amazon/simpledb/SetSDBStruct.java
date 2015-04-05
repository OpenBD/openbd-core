/* 
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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

import java.util.HashMap;
import java.util.Iterator;

import org.aw20.amazon.SimpleDB;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SetSDBStruct  extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public SetSDBStruct(){ min = 4; max = 4; 
		setNamedParams( new String[]{ "datasource", "domain", "itemname", "data" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"Amazon SimpleDB Datasource",
			"Amazon Domain to which this attribute is set",
			"The SimpleDB 'itemName' or key of the main document record",
			"data - structure"
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"amazon", 
				"Amazon SimpleDB: Sets all the attributes in data to the ItemName in domain", 
				ReturnType.BOOLEAN );
	}
	
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
 		String dsName				= getNamedStringParam(argStruct, "datasource", "");
  	String domainName		= getNamedStringParam(argStruct, "domain", "");
  	String itemName			= getNamedStringParam(argStruct, "itemname", "");
  	cfData data					= getNamedParam(argStruct, "data");

  	SimpleDB	sdb = SimpleDBFactory.getDS(dsName);
  	if ( sdb == null )
  		throwException(_session, "Invalid named Amazon Datasource");

  	if ( !data.isStruct() ){
      throwException(_session, "Invalid argument. Last argument provided was not a struct.");
    }
  	cfStructData	struct = (cfStructData)data;
		HashMap map = new HashMap();

  	try {
  		//Collect the params
  		Iterator it = struct.keySet().iterator();
  		while ( it.hasNext() ){
  			String key	= (String)it.next();
  			map.put( key, struct.getData(key).getString() );
  		}

  		//Call out to Amazon
  		sdb.putAttributes( domainName, itemName, map, map.keySet() );

			return cfBooleanData.TRUE;
		} catch (Exception e) {
			throwException(_session, "AmazonSimpleDB: " + e.getMessage() );
			return cfBooleanData.FALSE;
		}
  }
}
