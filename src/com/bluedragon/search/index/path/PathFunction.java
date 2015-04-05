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
 *  $Id: PathFunction.java 1638 2011-07-31 16:08:50Z alan $
 */

package com.bluedragon.search.index.path;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import com.bluedragon.search.DocumentWrap;
import com.bluedragon.search.collection.Collection;
import com.bluedragon.search.collection.CollectionFactory;
import com.bluedragon.search.index.DocumentWriter;
import com.bluedragon.search.index.crawl.CrawlFactory;
import com.bluedragon.search.index.custom.CustomFunction;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;


public class PathFunction extends CustomFunction {
	private static final long	serialVersionUID	= 1L;

	public PathFunction(){
		String[] p = new String[]{"collection","key","title","summary","author","category","categorytree","urlpath","custommap","extensions","recurse","query"};
		min = 2;
		max = p.length;
		setNamedParams( p );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"the name of the collection",
			"the full path of the file to index.  If a document already exists, it will be removed and replaced with this one.  If 'query' present, this is the column where the file is found",
			"title for the document.   If 'query' present this is a column name",
			"summary for the document.   If 'query' present this is a column name.  This column is not indexed, merely stored as a reference",
			"author for the document.   If 'query' present this is a column name",
			"one or more categores, separated by a comma separated list.   If 'query' present this is a column name",
			"the categorytree for this particular document.   If 'query' present this is a column name",
			"the urlpath of this document.   If 'query' present this is a column name",
			"a structure of custom atttributes that will be added to the document and indexed.  The key of the structure element will be the field name and the value will be indexed.  You can specify as many custom attributes as required.  Each one is stored in the index as well.  If 'query' then this is used for column names",
			"a list of extensions to include in this crawl.  defaults to '.cfm, .cfml, .htm, .html, .dbm, .dbml'.  '.*'/'*.*' handles all files",
			"a flag to determine whether or not the path is recursed for sub-directories",
			"the query representing all the rows to add to this index."
	};
}


public java.util.Map getInfo(){
	return makeInfo(
			"search", 
			"Inserts/Updates a path into the collection.  The key is the unique path for each directory that all files inside will be handled.  Each field in the document can be searched against.  " +
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
		if  ( tmpdata != null && tmpdata.getDataType() != cfData.CFQUERYRESULTDATA ){
			throwException(_session, "the query parameter was not a proper type");
		}
		cfQueryResultData query	= (cfQueryResultData)tmpdata;

		// Validate the body
		checkParam(_session, argStruct, "key", 		query, true );
		checkParam(_session, argStruct, "title", 	query, false );
		
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
			return indexPathQuery( collection, query, _session, custommap, argStruct );
		else
			return indexPath( collection, _session, custommap, argStruct );
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
	private cfData indexPath(Collection collection, cfSession _session, cfStructData custommap, cfArgStructData argStruct) throws cfmRunTimeException  {
		String filename = getNamedStringParam(argStruct, "key", "");
		
		File originalfile	= new File( filename );
		if ( !originalfile.isDirectory() )
			throwException(_session, "Path not found: " + originalfile.toString() );
		
		CrawlFactory	crawlfactory	= new CrawlFactory( collection.bStoreBody() );
		int totalFiles				= 0;
		cfArrayData	badFiles	= cfArrayData.createArray(1);
		String urlpath				= getNamedStringParam(argStruct, "urlpath", null );
		String	exts					= getNamedStringParam(argStruct, "extensions", null );
		boolean bRcurse				= getNamedBooleanParam(argStruct, "recurse", false );

		
		try{
			String originalFileSt	= originalfile.getCanonicalPath();
			DocumentWriter	docWriter	= collection.getDocumentWriter();
			
			Set<String>	filesToCrawlSet	= crawlfactory.getFilesToCrawl(originalfile, exts, bRcurse);
			Iterator<String>	fit	= filesToCrawlSet.iterator();

			while ( fit.hasNext() ){
				String fileToCrawl	= fit.next();
				
				DocumentWrap	doc	= crawlfactory.crawlFile( null, new File(fileToCrawl) );
				if ( doc == null ){
					badFiles.addElement( new cfStringData(fileToCrawl) );
					continue;
				}
				
				// Set the URL
				if ( urlpath != null ){
					doc.setURL( urlpath + fileToCrawl.replace('\\', '/').substring( originalFileSt.length()+1 ) );
				}
				
				// Set the optional fields
				doc.setName( getNamedStringParam(argStruct, "title", null ) );
				doc.setSummary( getNamedStringParam(argStruct, "summary", null ) );
				doc.setAuthor( getNamedStringParam(argStruct, "author", null ) );
				doc.setCategoryTree( getNamedStringParam(argStruct, "categorytree", null ) );
				doc.setCategories( getNamedStringParam(argStruct, "category", "" ).split(",") );
				
				// Set the custom attributes
				if ( custommap != null ){
					Iterator<String> it = custommap.keySet().iterator();
					while ( it.hasNext() ){
						String k = it.next();
						String v = custommap.getData(k).getString();
						doc.setAttribute(k, v);
					}
				}

				// Add the document to the index
				docWriter.add(doc);
				totalFiles++;
			}
 
			docWriter.commit();
			
		}catch(Exception e){
			throwException(_session, e.getMessage());
		} finally {
			crawlfactory.close();
		}
		
		cfStructData	sd	= new cfStructData();
		sd.setData("inserted", new cfNumberData(totalFiles) );
		sd.setData("invalid", new cfNumberData(badFiles.size()) );
		sd.setData("badkeys", badFiles );
		return sd;
	}

	
	
	private cfData indexPathQuery(Collection collection, cfQueryResultData query, cfSession _session, cfStructData custommap, cfArgStructData argStruct) throws cfmRunTimeException {
		cfStructData	sd	= new cfStructData();
		int totalDocs = 0;
		cfArrayData	badFiles	= cfArrayData.createArray(1);
		DocumentWriter	docWriter = null;
		
		String	exts					= getNamedStringParam(argStruct, "extensions", null );
		boolean bRcurse				= getNamedBooleanParam(argStruct, "recurse", false );
		CrawlFactory	crawlfactory	= new CrawlFactory( collection.bStoreBody() );

		try{
			docWriter	= collection.getDocumentWriter();
			
			query.reset();
			while (query.nextRow()) {
				
				String filename = getQueryParam(argStruct, query, "key" );
				File originalfile	= new File( filename );
				if ( !originalfile.isDirectory() ){
					badFiles.addElement( new cfStringData(filename) );
					continue;
				}

				String originalFileSt	= originalfile.getCanonicalPath();
				String urlpath	= getQueryParam(argStruct, query, "urlpath" );
				
				Set<String>	filesToCrawlSet	= crawlfactory.getFilesToCrawl(originalfile, exts, bRcurse);
				Iterator<String>	fit	= filesToCrawlSet.iterator();

				while ( fit.hasNext() ){
					String fileToCrawl	= fit.next();
					
					DocumentWrap	doc	= crawlfactory.crawlFile( null, new File(fileToCrawl) );
					if ( doc == null ){
						badFiles.addElement( new cfStringData(fileToCrawl) );
						continue;
					}
					
					// Set the URL
					if ( urlpath != null ){
						doc.setURL( urlpath + fileToCrawl.replace('\\', '/').substring( originalFileSt.length()+1 ) );
					}
					
					// Set the optional fields
					doc.setName( getQueryParam(argStruct, query, "title" ) );
					doc.setSummary( getQueryParam(argStruct, query, "summary" ) );
					doc.setAuthor( getQueryParam(argStruct, query, "author" ) );
					doc.setCategoryTree( getQueryParam(argStruct, query, "categorytree" ) );

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
			
			crawlfactory.close();
		}


		// Set the status
		sd.setData("inserted", new cfNumberData(totalDocs) );
		sd.setData("invalid", new cfNumberData(badFiles.size()) );
		sd.setData("badfiles", badFiles );
		return sd;
	}
	
	
	
}
