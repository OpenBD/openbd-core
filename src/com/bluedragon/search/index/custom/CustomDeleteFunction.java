/* 
 * Copyright (C) 2000 - 2008 TagServlet Ltd
 *
 * This file is part of the BlueDragon Java Open Source Project.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.bluedragon.search.index.custom;

import java.io.IOException;

import com.bluedragon.search.DocumentWrap;
import com.bluedragon.search.collection.Collection;
import com.bluedragon.search.collection.CollectionFactory;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;


public class CustomDeleteFunction extends CustomFunction {
	private static final long	serialVersionUID	= 1L;

	public CustomDeleteFunction(){
		min = 2;
		max = 3;
		setNamedParams( new String[]{"collection","key","query"} );
	}
	
  public String[] getParamInfo(){
		return new String[]{
			"the name of the collection",
			"the unique identifier for this document.  If a document already exists, it will be removed and replaced with this one.  If 'query' present, this is the column where the key is found",
			"the query representing all the rows to add to this index."
		};
  }
	
	
	public java.util.Map getInfo(){
		return makeInfo(
				"search", 
				"Deletes the given key from the collection.   If a query is specified then the key parameter is the column where the unique identifier is.  The query is then looped over and all the values deleted from the index", 
				ReturnType.BOOLEAN );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		
		// Validate the collection
		String col = getNamedStringParam(argStruct, "collection", null);
		if ( col == null )
			throwException(_session,"missing the collection parameter");
		
		Collection collection	= CollectionFactory.getCollection(col);
		if ( collection == null )
			throwException(_session,"invalid collection (" + col + ") was not found");
		
		
		// Let us determine if they have passed in a query object or not
		cfData	tmpdata	= getNamedParam(argStruct, "query", null );
		if  ( tmpdata != null && tmpdata.getDataType() != cfData.CFQUERYRESULTDATA ){
			throwException(_session, "the query parameter was not a proper type");
		}
		cfQueryResultData query	= (cfQueryResultData)tmpdata;

		checkParam(_session, argStruct, "key", 		query, true );
		
		
		// We have now have all our required parameters
		if ( query != null )
			return deleteQuery( collection, query, _session, getNamedStringParam(argStruct, "key", null) );
		else
			return deleteFlat( collection, _session, getNamedStringParam(argStruct, "key", null) );
	}

	
	
	/**
	 * Runs around the query look for the data to delete
	 * 
	 * @param collection
	 * @param query
	 * @param _session
	 * @param key
	 * @return
	 * @throws cfmRunTimeException
	 */
	private cfData deleteQuery(Collection collection, cfQueryResultData query, cfSession _session, String key) throws cfmRunTimeException {

		try{
			query.reset();
			
			while (query.nextRow()) {
				DocumentWrap	doc	= new DocumentWrap();
				doc.setId( key );
				collection.deleteDocument(doc, false);
			}
			
		}catch(Exception e){
			throwException(_session, e.getMessage() );
		}finally{
			try {
				collection.closeWriter();
			} catch (Exception e) {}
		}
		
		return cfBooleanData.TRUE;
	}
	
	
	

	/**
	 * Designed for only deleting a single document
	 * @param collection
	 * @param _session
	 * @param key
	 * @return
	 * @throws cfmRunTimeException
	 */
	private cfData deleteFlat(Collection collection, cfSession _session, String key ) throws cfmRunTimeException {

		DocumentWrap doc	= new DocumentWrap();
		doc.setId( key );
		try {
			collection.deleteDocument(doc, true);
		} catch (IOException e) {
			throwException(_session, e.getMessage() );
		}

		return cfBooleanData.TRUE;
	}
	
}
