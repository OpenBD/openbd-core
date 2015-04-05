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

import org.aw20.amazon.SimpleDB;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.sql.cfQUERY;
import com.naryx.tagfusion.cfm.sql.cfQueryImplInterface;
import com.naryx.tagfusion.cfm.sql.cfSQLQueryData;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class cfQueryImplSimpleDB extends Object implements cfQueryImplInterface {

	public cfTagReturnType render(cfQUERY tag, cfSession _Session) throws cfmRunTimeException {

		// Any TOKEN passed in
		String token = null;
		if ( tag.containsAttribute("TOKEN") ){
			token = tag.getDynamic(_Session, "TOKEN").toString();
		}
		
		SimpleDB	sdb	= getDatasource( tag, _Session );
		
		cfSQLQueryData	queryData	= new cfSimpleDBResultData( sdb, token );
		queryData.setDataSource( cfAmazonDataSource.getDefault() );
		
		
		// Determine the inner SQL
		tag.renderInnerBody( queryData, _Session );
		
    //Setup any cache information for this
		tag.setupCache( queryData, _Session );

		//Execute the query
		tag.executeStatement( queryData, _Session );
		
		return cfTagReturnType.NORMAL;
	}

	
	
	private SimpleDB getDatasource(cfQUERY tag, cfSession _Session) throws cfmRunTimeException {
		if ( !tag.containsAttribute("DATASOURCE") ){
			throw tag.newRunTimeException( "You must specify the DATASOURCE attribute" );
		}
		
		SimpleDB sdb = SimpleDBFactory.getDS( tag.getDynamic(_Session, "DATASOURCE").getString() );
		if ( sdb == null ){
			throw tag.newRunTimeException( "You must create an Amazon datasource first via AmazonSimpleDB() function" );
		}
		
		return sdb;
	}



	public void init(xmlCFML configFile) {}

}
