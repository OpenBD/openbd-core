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
 *  $Id: SearchFunction.java 2404 2013-09-22 21:51:40Z alan $
 */


package com.bluedragon.search.search;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class SearchFunction extends functionBase {
	private static final long	serialVersionUID	= 1L;

	public SearchFunction(){
		min = 2;
		max = 14;
		setNamedParams( new String[]{ "collection", "criteria", "type", "minscore", "maxrows", "startrow", "category", "categorytree", "contextpassages", "contextbytes", "contexthighlightstart", "contexthighlightend", "contents", "uniquecolumn" } );
	}
	
  public String[] getParamInfo(){
		return new String[]{
			"the name of one or more collections to perform the search.  Multiple collections are delimited by a comma",
			"the search to perform",
			"the type of search; 'simple' or 'explicit'.  The simple search will OR all the tokens in the crteria where as the explicit will use the criteria as a Lucene search string. Defaults to 'simple'",
			"minimum score of the document to be included in the result query.  A value between 0.0 and 10.0 is required.  Defaults to 0 (all results that match)",
			"maximum number of rows to return back in this result.  Defaults to all",
			"the start of the row in the search results to start from.  Useful for paging through results.  Defaults to 1",
			"the list of categories to which the search is matched against",
			"the hierachy to which to search against.  Usually in the form of 'a/b/c/', which would search all results that where in the 'a/b/c/' start prefix",
			
			"the number of contextual passages that will be returned.  Defaults to 0 which disables the feature",
			"the maximum size of the contextual passage",
			"the start marker for the contextual piece",
			"the end marker for the contextual piece",
			
			"flag to determine if the CONTENT column is return;  If you are storing the full body, this lets you return that full body or not in the query. defaults to true",
			"name of the column that will be checked to see if it is unique before adding to the result query"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"search", 
				"Performs searches against the registered collection using the Apache Lucene internal engine", 
				ReturnType.QUERY );
	}
		
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		QueryAttributes queryAttributes	= new QueryAttributes();
		
		// Set the collections up
		if ( !queryAttributes.setCollection( getNamedStringParam(argStruct, "collection", null) ) )
			throwException(_session, "must specify the collection");
		
		// Set the criteria		
		try {
			if ( !queryAttributes.setCriteria( getNamedStringParam(argStruct, "criteria", null), getNamedStringParam(argStruct, "type", null) ) )
				throwException(_session, "must specify the criteria");

			queryAttributes.setCategory( getNamedStringParam(argStruct, "category", null) );
			queryAttributes.setCategoryTree( getNamedStringParam(argStruct, "categorytree", null) );
		} catch (org.apache.lucene.queryparser.classic.ParseException e) {
			throwException(_session, "invalid criteria:" + e.getMessage());
		}

		// Set the context
		queryAttributes.setContextPassages(  getNamedIntParam(argStruct, "contextpassages", 0 ) );
		queryAttributes.setContextBytes(  getNamedIntParam(argStruct, "contextbytes", 300 ) );
		queryAttributes.setContext( getNamedStringParam(argStruct, "contexthighlightstart", null ), getNamedStringParam(argStruct, "contexthighlightend", null ) );
		
		queryAttributes.setMinScore( (float)getNamedDoubleParam(argStruct, "minscore", 0) );
		queryAttributes.setMaxRows( getNamedIntParam(argStruct, "maxrows", Integer.MAX_VALUE ) );
		queryAttributes.setStartRow( getNamedIntParam(argStruct, "startrow", 1 ) );
		queryAttributes.setContentFlag( getNamedBooleanParam( argStruct, "contents", true ) );
		queryAttributes.setUniqueColumn( getNamedStringParam(argStruct, "uniquecolumn", null ) );

		// Run the query now
		QueryRun	query	= new QueryRun(queryAttributes);
		try {
			query.run();
			return query.getQueryResultData();
		} catch (Exception e) {
			throwException(_session, e.getMessage());
		}
		
		return null;
	}
	
}
