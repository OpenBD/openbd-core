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
 *  
 *  $Id: CollectionStatus.java 1662 2011-09-09 09:49:21Z alan $
 */


package com.bluedragon.search.collection;

import java.io.IOException;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class CollectionStatus extends functionBase {
	private static final long	serialVersionUID	= 1L;
	
	public CollectionStatus(){
		min = 1;
		max = 1;
		setNamedParams( new String[]{ "collection" } );
	}
	
	
  public String[] getParamInfo(){
		return new String[]{
			"the name of the collection"
		};
  }
	
	public java.util.Map getInfo(){
		return makeInfo(
				"search", 
				"Returns back all the information for a given collection", 
				ReturnType.QUERY );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		String name	= getNamedStringParam(argStruct, "collection", null );
		if ( name == null )
			throwException(_session, "please specifiy the 'collection' attribute" );
		
		Collection col	= CollectionFactory.getCollection( name );
		if ( col == null )
			throwException(_session, "invalid '" + name + "' collection" );
		
		cfQueryResultData queryResultData	=  new cfQueryResultData( new String[] { 
				"EXTERNAL", "LANGUAGE", "MAPPED", "NAME", "ONLINE", "PATH", "REGISTERED",
    		"CATEGORIES", "SIZE", "DOCCOUNT", "LASTMODIFIED", "CREATED", "STOREDBODY" }, "SEARCH" );
		
		try {
			col.getIndexSearcher();
		} catch (IOException e) {
			throwException(_session, "problem with '" + name + "' collection:" + e.getMessage() );
		}
		
		queryResultData.addRow(1);
		queryResultData.setCurrentRow( queryResultData.getSize() );
		
		queryResultData.setCell( 1, cfBooleanData.FALSE );
		queryResultData.setCell( 2, new cfStringData( col.getLanguage() ) );
		queryResultData.setCell( 3, cfBooleanData.TRUE );
		queryResultData.setCell( 4, new cfStringData( col.getName() ) );
		queryResultData.setCell( 5, cfBooleanData.TRUE );
		queryResultData.setCell( 6, new cfStringData( col.getPath() ) );
		queryResultData.setCell( 7, cfBooleanData.TRUE );
		queryResultData.setCell( 8, cfBooleanData.TRUE );
		
		queryResultData.setCell( 9, new cfNumberData( col.size()/1024 ) );
		queryResultData.setCell(10, new cfNumberData( col.getTotalDocs() ) );
		queryResultData.setCell(11, new cfDateData( col.getLastModified() ) );
		queryResultData.setCell(12, new cfDateData( col.getCreated() ) );
		queryResultData.setCell(13, cfBooleanData.getcfBooleanData( col.bStoreBody() ) );
		
		queryResultData.reset();
		
		return queryResultData;
	}
}
