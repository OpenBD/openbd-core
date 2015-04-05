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
 *  $Id: CustomFunction.java 2203 2012-07-23 13:30:28Z andy $
 */
package com.bluedragon.search.index.custom;

import java.util.Iterator;

import com.bluedragon.search.DocumentWrap;
import com.bluedragon.search.collection.Collection;
import com.bluedragon.search.collection.CollectionFactory;
import com.bluedragon.search.index.DocumentWriter;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.expression.function.functionBase;


public class CustomFunction extends functionBase {
	private static final long	serialVersionUID	= 1L;

	public CustomFunction(){
		String[] p = new String[]{"collection","key","body","title","summary","author","category","categorytree","urlpath","custommap","query"};
		min = 4;
		max = p.length;
		setNamedParams( p );
	}
	
  public String[] getParamInfo(){
		return new String[]{
			"the name of the collection",
			"the unique identifier for this document.  If a document already exists, it will be removed and replaced with this one.  If 'query' present, this is the column where the key is found",
			"the main content for this document.  Depending on whether you have flagged the collection to store the body determines whether or not this is presented back in the results.  It is advised not to store large quantities with the index.  If 'query' present, this is the column where the content is found.  You can specify a comma separated list of column names for multiple columns",
			"title for the document.   If 'query' present this is a column name",
			"summary for the document.   If 'query' present this is a column name.  This column is not indexed, merely stored as a reference",
			"author for the document.   If 'query' present this is a column name",
			"one or more categores, separated by a comma separated list.   If 'query' present this is a column name",
			"the categorytree for this particular document.   If 'query' present this is a column name",
			"the urlpath of this document.   If 'query' present this is a column name",
			"a structure of custom atttributes that will be added to the document and indexed.  The key of the structure element will be the field name and the value will be indexed.  You can specify as many custom attributes as required.  Each one is stored in the index as well.  If 'query' then this is used for column names",
			"the query representing all the rows to add to this index."
		};
  }
	
	
	public java.util.Map getInfo(){
		return makeInfo(
				"search", 
				"Inserts/Updates a document into the collection.  The key is the unique identifier for each document.  Each field in the document can be searched against.  " +
				"If a query is presented then the fields represent columns into the query.  If the column does not exist then an exception is thrown. " +
				"The index can still be searched while an update is happening, however the new documents will not be available in the search until this operation has completed. " +
				"Note that all fields are treated as strings and will be indexed accordingly.", 
				ReturnType.STRUCTURE );
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
		cfQueryResultData query = null;
		
		if ( tmpdata != null ){
			if ( tmpdata.getDataType() == cfData.CFSTRINGDATA )
				tmpdata	= runTime.runExpression(_session, tmpdata.getString());
			
			if ( tmpdata.getDataType() != cfData.CFQUERYRESULTDATA )
				throwException(_session, "the query parameter was not a proper query object");
			
			query	= (cfQueryResultData)tmpdata;
		}

		// Validate the body
		checkParam(_session, argStruct, "key", 		query, true );
		checkParam(_session, argStruct, "body", 	query, true );
		checkParam(_session, argStruct, "title", 	query, true );
		
		checkParam(_session, argStruct, "summary", query, false );
		checkParam(_session, argStruct, "author", query, false );
		checkParam(_session, argStruct, "category", query, false );
		checkParam(_session, argStruct, "categorytree", query, false );
		checkParam(_session, argStruct, "urlpath", query, false );
		
		// Validate the custom map
		tmpdata	= getNamedParam(argStruct, "custommap", null );
		if ( tmpdata != null && tmpdata.getDataType() != cfData.CFSTRUCTDATA ){
			throwException(_session, "the custommap parameter was not a proper structure/map");
		}
		cfStructData	custommap	= (cfStructData)tmpdata;
	
	
		if ( custommap != null && query != null ){
			Iterator<String> it = custommap.keySet().iterator();
			while ( it.hasNext() ){
				String k = it.next();
				String v = custommap.getData(k).getString();
				
				if ( !isColumn(query, v) )
					throwException(_session, "custommap key: " + k + ", is mapped to: " + v + ", but this column was not found in the query");
			}
		}


		// We have now have all our required parameters
		if ( query != null )
			return indexQuery( collection, query, _session, custommap, argStruct );
		else
			return indexFlat( collection, _session, custommap, argStruct );
	}

	
	/**
	 * This one does the basic record to which the session is created
	 * 
	 * @param collection
	 * @param query
	 * @param _session
	 * @param custommap
	 * @param argStruct
	 * @return
	 * @throws cfmRunTimeException
	 */
	private cfData indexFlat(Collection collection, cfSession _session, cfStructData custommap, cfArgStructData argStruct) throws cfmRunTimeException  {
		DocumentWrap	doc	= new DocumentWrap();
		
		// Set the minimum values
		doc.setName( getNamedStringParam(argStruct, "title", "") );
		doc.setId( getNamedStringParam(argStruct, "key", "") );
		doc.setContent( getNamedStringParam(argStruct, "body", ""), collection.bStoreBody() );
		
		// Set the optional fields
		doc.setSummary( getNamedStringParam(argStruct, "summary", null ) );
		doc.setAuthor( getNamedStringParam(argStruct, "author", null ) );
		doc.setCategoryTree( getNamedStringParam(argStruct, "categorytree", null ) );
		doc.setURL( getNamedStringParam(argStruct, "urlpath", null ) );

		// Set the custom attributes
		if ( custommap != null ){
			Iterator<String> it = custommap.keySet().iterator();
			while ( it.hasNext() ){
				String k = it.next();
				String v = custommap.getData(k).getString();
				doc.setAttribute(k, v);
			}
		}
		
		// Set the categories
		doc.setCategories( getNamedStringParam(argStruct, "category", "" ).split(",") );

		// Write the document
		try {
			DocumentWriter	docWriter	= collection.getDocumentWriter();
			docWriter.add(doc);
			docWriter.commit();
		} catch (Exception e) {
			throwException(_session, e.getMessage());
		}
		
		// Runtime struct
		cfStructData	sd	= new cfStructData();
		sd.setData("inserted", new cfNumberData(1) );
		return sd;
	}



	/**
	 * Loops around the query pulling in all the necessary data
	 * 
	 * @param collection
	 * @param query
	 * @param _session
	 * @param custommap
	 * @param argStruct
	 * @return
	 * @throws cfmRunTimeException
	 */
	private cfData indexQuery(Collection collection, cfQueryResultData query, cfSession _session, cfStructData custommap, cfArgStructData argStruct) throws cfmRunTimeException  {
		cfStructData	sd	= new cfStructData();
		int totalDocs = 0;
		DocumentWriter	docWriter = null;
		String[]	contentKeys	= getNamedStringParam(argStruct, "body", "").split(",");

		try{
			docWriter	= collection.getDocumentWriter();
			
			query.reset();
			while (query.nextRow()) {
				DocumentWrap	doc	= new DocumentWrap();
				
				// Set the minimum values
				doc.setName( query.getData(getNamedStringParam(argStruct, "title", "")).getString() );
				doc.setId( query.getData(getNamedStringParam(argStruct, "key", "")).getString() );
				
				StringBuilder	sb = new StringBuilder();
				for ( int x=0; x < contentKeys.length; x++ ){
					sb.append( query.getData(contentKeys[x]).getString() );
					if ( sb.length() > 0 ) {
						sb.append( " " );
					}
				}

				doc.setContent( sb.toString(), collection.bStoreBody() );
				
				// Set the optional fields
				doc.setSummary( getQueryParam(argStruct, query, "summary" ) );
				doc.setAuthor( getQueryParam(argStruct, query, "author" ) );
				doc.setCategoryTree( getQueryParam(argStruct, query, "categorytree" ) );
				doc.setURL( getQueryParam(argStruct, query, "urlpath" ) );

				// Set the custom attributes
				if ( custommap != null ){
					Iterator<String> it = custommap.keySet().iterator();
					while ( it.hasNext() ){
						String k = it.next();
						String v = query.getData( custommap.getData(k).getString() ).getString();
						doc.setAttribute(k, v);
					}
				}

				String category	= getQueryParam(argStruct, query, "category" );
				if ( category != null )
					doc.setCategories( category.split(",") );

				docWriter.add( doc );
				totalDocs++;
			}

		} catch (Exception e) {
			throwException(_session, e.getMessage());
		}finally{
			try {
				if ( docWriter != null )
					docWriter.commit();
			} catch (Exception e) {
				throwException(_session, e.getMessage());
			}
		}
		
		sd.setData("inserted", new cfNumberData(totalDocs) );

		return sd;
	}

	
	protected String getQueryParam(cfArgStructData argStruct, cfQueryResultData query, String name) throws cfmRunTimeException{
		String col = getNamedStringParam(argStruct, name, null );
		if ( col == null )
			return null;
		
		return query.getData( col ).getString();
	}
	
	protected void checkParam( cfSession _session, cfArgStructData argStruct, String param, cfQueryResultData query, boolean required ) throws cfmRunTimeException{
		String tmp = getNamedStringParam(argStruct, param, null );
		if ( tmp == null && required )
			throwException(_session, "missing " + param + " parameter");

		if ( tmp == null && !required )
			return;
		
		if ( query != null && !isColumn(query, tmp) )
			throwException(_session, param + " value, when using query, must be a valid column in the query");
	}
	
	
	/**
	 * Determines if the column we passed in is indeed valid
	 * @param query
	 * @param column
	 * @return
	 */
	protected boolean isColumn(cfQueryResultData query, String column){
		if ( column.trim().length() == 0 )
			return false;
		
		String[] columnKeys	= column.split(",");
		
		for ( int c = 0; c < columnKeys.length; c++ ){
			if ( !_isColumn(query, columnKeys[c].trim() ))
				return false;
		}
		
		return true;
	}
	
	
	private boolean _isColumn(cfQueryResultData query, String column){
		String[] columns	= query.getColumnList();
		
		for ( int c = 0; c < columns.length; c++ ){
			if ( columns[c].equalsIgnoreCase(column) )
				return true;
		}
		
		return false;
	}
	
}