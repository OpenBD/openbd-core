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

import java.util.Iterator;
import java.util.List;

import org.aw20.amazon.SimpleDB;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class ListSDBDomains extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public ListSDBDomains(){  min = 1; max = 1; setNamedParams( new String[]{ "datasource" } ); }


	public String[] getParamInfo(){
		return new String[]{
			"Amazon SimpleDB Datasource",
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"amazon", 
				"Amazon SimpleDB: Lists all the domains within this datasource", 
				ReturnType.QUERY );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
 		String dsName	= getNamedStringParam(argStruct, "datasource", "");
    	
  	SimpleDB	sdb = SimpleDBFactory.getDS(dsName);
  	if ( sdb == null )
  		throwException(_session, "Invalid named Amazon Datasource");

  	try {
			
  		//Call out to Amazon
  		List<String> domains = sdb.listDomains();

  		//Create the results
  		cfQueryResultData qD = new cfQueryResultData( new String[]{"domain"}, null );
  		qD.setQuerySource( "AmazonSimpleDB." + dsName );
  		  		
  		for ( Iterator<String> it = domains.iterator(); it.hasNext(); ){
  			String d = it.next();

  			qD.addRow(1);
  			qD.setCurrentRow( qD.getSize() );
  			qD.setCell( 1, new cfStringData( d ) );
  		}
  		
			return qD;
		} catch (Exception e) {
			throwException(_session, "AmazonSimpleDB: " + e.getMessage() );
			return cfBooleanData.FALSE;
		}
  }
}
