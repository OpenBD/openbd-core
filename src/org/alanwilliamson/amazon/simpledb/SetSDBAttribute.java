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

import org.aw20.amazon.SimpleDB;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SetSDBAttribute extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public SetSDBAttribute(){  
		min = 5; max = 5; 
		setNamedParams( new String[]{ "datasource", "domain", "itemname", "attname", "attvalue" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"Amazon SimpleDB Datasource",
			"Amazon Domain to which this attribute is set",
			"The SimpleDB 'itemName' or key of the main document record",
			"The attribute name",
			"The attribute value"
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"amazon", 
				"Amazon SimpleDB: Sets the attribute (and optional value) to the ItemName inside the domain", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
 		String dsName	= getNamedStringParam(argStruct, "datasource", "");
  	String domainName	= getNamedStringParam(argStruct, "domain", "");
  	String itemName		= getNamedStringParam(argStruct, "itemname", "");
  	String attName		= getNamedStringParam(argStruct, "attname", "");
  	String attValue		= getNamedStringParam(argStruct, "attvalue", "");
		
  	SimpleDB	sdb = SimpleDBFactory.getDS(dsName);
  	if ( sdb == null )
  		throwException(_session, "Invalid named Amazon Datasource");

  	try {
			
  		//Call out to Amazon
  		HashMap map = new HashMap();
  		map.put( attName, attValue );
  		
  		sdb.putAttributes( domainName, itemName, map, map.keySet() );

			return cfBooleanData.TRUE;
		} catch (Exception e) {
			throwException(_session, "AmazonSimpleDB: " + e.getMessage() );
			return cfBooleanData.FALSE;
		}
  }
}
