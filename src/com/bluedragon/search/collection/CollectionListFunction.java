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
 *  $Id: CollectionListFunction.java 1662 2011-09-09 09:49:21Z alan $
 */


package com.bluedragon.search.collection;

import java.io.IOException;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class CollectionListFunction extends functionBase {
	private static final long	serialVersionUID	= 1L;
	
  public String[] getParamInfo(){
		return new String[]{};
  }
	
  
	public java.util.Map getInfo(){
		return makeInfo(
				"search", 
				"Lists all the collections registered.  A query is returned detailing information about each collection", 
				ReturnType.QUERY );
	}

	
	public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
		String[]	collectionNames	= CollectionFactory.getCollectionNames();
		
		cfQueryResultData queryResultData	=  new cfQueryResultData( new String[] { 
				"EXTERNAL", "LANGUAGE", "MAPPED", "NAME", "ONLINE", "PATH", "REGISTERED",
    		"CATEGORIES", "SIZE", "DOCCOUNT", "LASTMODIFIED", "CREATED", "STOREDBODY" }, "SEARCH" );
		
		for ( int x=0; x < collectionNames.length; x++ ){
			Collection col	= CollectionFactory.getCollection( collectionNames[x] );
			if ( col == null )
				continue;
			
			try {
				col.getIndexSearcher();
			} catch (IOException e) {
				continue;
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
		}

		queryResultData.reset();
		return queryResultData;
	}
}
