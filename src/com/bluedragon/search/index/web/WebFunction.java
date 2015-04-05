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
 *  $Id: WebFunction.java 1638 2011-07-31 16:08:50Z alan $
 */

package com.bluedragon.search.index.web;

import java.net.URL;

import com.bluedragon.search.collection.Collection;
import com.bluedragon.search.collection.CollectionFactory;
import com.bluedragon.search.index.DocumentWriter;
import com.bluedragon.search.index.crawl.WebCrawlFactory;
import com.bluedragon.search.index.custom.CustomFunction;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;


public class WebFunction extends CustomFunction {
	private static final long	serialVersionUID	= 1L;

	public WebFunction(){
		String[] p = new String[]{"collection","key","category","categorytree","urlpath","custommap"};
		min = 2;
		max = p.length;
		setNamedParams( p );
	}
	
	public String[] getParamInfo(){
			return new String[]{
				"the name of the collection",
				"the full website URL to index.  If a document already exists, it will be removed and replaced with this one.  If 'query' present, this is the column where the file is found",
				"one or more categores, separated by a comma separated list.   If 'query' present this is a column name",
				"the categorytree for this particular document.   If 'query' present this is a column name",
				"the urlpath of this document.   If 'query' present this is a column name",
				"a structure of custom atttributes that will be added to the document and indexed.  The key of the structure element will be the field name and the value will be indexed.  You can specify as many custom attributes as required.  Each one is stored in the index as well.  If 'query' then this is used for column names"
		};
	}
	
	
	public java.util.Map getInfo(){
		return makeInfo(
				"search", 
				"Inserts/Updates a webpage into the collection.  The URL is the unique key for each document.  Each field in the document can be searched against.  " +
				"The index can still be searched while an update is happening, however the new documents will not be available in the search until this operation has completed. " +
				"Note that all fields are treated as strings and will be indexed accordingly.  The webpage will be automatically crawled for links and only internal links will be followed.  A maximum of 100 URLs will be indexed at once.  Each URL indexed will be logged to the main application log file.", 
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
		
		// Validate the body
		checkParam(_session, argStruct, "key", 	null, true );
		
		// Validate the custom map
		cfData tmpdata	= getNamedParam(argStruct, "custommap", null );
		if ( tmpdata != null && tmpdata.getDataType() != cfData.CFSTRUCTDATA ){
			throwException(_session, "the custommap parameter was not a proper structure/map");
		}
		cfStructData	custommap	= (cfStructData)tmpdata;

		// We have now have all our required parameters
		return indexWeb( collection, _session, custommap, argStruct );
	}

	
	/**
	 * Indexes a single file
	 * 
	 * @param collection
	 * @param _session
	 * @param custommap
	 * @param argStruct
	 * @return
	 * @throws cfmRunTimeException
	 */
	private cfData indexWeb(Collection collection, cfSession _session, cfStructData custommap, cfArgStructData argStruct) throws cfmRunTimeException  {
		URL weburl=null;
		String w = getNamedStringParam(argStruct, "key", "");
		try{
			weburl	= new URL( w );
		} catch (Exception e){
			throwException(_session, "invalid url: " + w);
		}
		
		String categoryTree = getNamedStringParam(argStruct, "categorytree", null );
		String category[]		= getNamedStringParam(argStruct, "category", "" ).split(",");
				
		WebCrawlFactory	crawlfactory = null;
		try{
			DocumentWriter	docWriter	= collection.getDocumentWriter();
			crawlfactory	= new WebCrawlFactory( collection.bStoreBody(), weburl, docWriter, categoryTree, category, custommap );
			return crawlfactory.crawl();
		}catch(Exception e){
			throwException(_session, e.getMessage());
		} finally {
			if ( crawlfactory != null ) crawlfactory.close();
		}
		return null;
	}

}