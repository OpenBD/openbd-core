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
 *  $Id: IndexTag.java 2039 2012-04-30 20:23:39Z alan $
 */

package com.bluedragon.search.index;

import java.io.Serializable;

import com.bluedragon.search.index.custom.CustomDeleteFunction;
import com.bluedragon.search.index.custom.CustomFunction;
import com.bluedragon.search.index.file.FileFunction;
import com.bluedragon.search.index.path.PathFunction;
import com.bluedragon.search.index.web.WebFunction;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.expression.function.functionBase;


public class IndexTag extends cfTag implements Serializable  {
	private static final long	serialVersionUID	= 1L;
	
	
	public java.util.Map getInfo(){
		return createInfo("search", 
				"Used to manage update content inside the collections.  This is the tag version of all the CollectionIndexXXX() functions.");
	}

	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
			createAttInfo( "ACTION", 			"Type of action to take: update|delete|purge|refresh", 	"", true ),
			createAttInfo( "TYPE", 				"If ACTION=update|refresh, the type of update to take: custom|file|path|website", 	"custom", true ),
			createAttInfo( "COLLECTION", 	"name of the collection", "", true ),
			createAttInfo( "STATUS", 			"The name of the variable to store the resulting data.   Used for ACTION=update|refresh", "", false ),
			
			createAttInfo( "KEY", 				"the unique identifier for this document.  If a document already exists, it will be removed and replaced with this one.  If 'query' present, this is the column where the key is found", 	"", false ),
			createAttInfo( "BODY", 				"the main content for this document.  Depending on whether you have flagged the collection to store the body determines whether or not this is presented back in the results.  It is advised not to store large quantities with the index.  If 'query' present, this is the column where the content is found.  You can specify a comma separated list of column names for multiple columns", 	"", false ),
			createAttInfo( "TITLE", 			"title for the document.   If 'query' present this is a column name", 	"", false ),
			createAttInfo( "SUMMARY", 		"summary for the document.   If 'query' present this is a column name.  This column is not indexed, merely stored as a reference", 	"", false ),
			createAttInfo( "AUTHOR", 			"author for the document.   If 'query' present this is a column name", 	"", false ),
			createAttInfo( "CATEGORY", 		"one or more categores, separated by a comma separated list.   If 'query' present this is a column name", 	"", false ),
			createAttInfo( "CATEGORYTREE","the categorytree for this particular document.   If 'query' present this is a column name", 	"", false ),
			createAttInfo( "URLPATH", 		"the urlpath of this document.   If 'query' present this is a column name", 	"", false ),
			createAttInfo( "CUSTOMMAP", 	"a structure of custom atttributes that will be added to the document and indexed.  The key of the structure element will be the field name and the value will be indexed.  You can specify as many custom attributes as required.  Each one is stored in the index as well.  If 'query' then this is used for column names", 	"", false ),
			createAttInfo( "QUERY", 			"the query representing all the rows to add to this index.", 	"", false ),

			createAttInfo( "RECURSE", 		"a flag to determine whether or not the path is recursed for sub-directories. TYPE=PATH", 	"false", false ),
			createAttInfo( "EXTENSIONS", 	"a list of extensions to include in this crawl.  defaults to '.cfm, .cfml, .htm, .html, .dbm, .dbml'.  '.*'/'*.*' handles all files. TYPE=PATH", 	"false", false ),
		};
	}

	
	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseTagHeader(_tag);
	}
	
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData	attributes = setAttributeCollection(_Session);
		
		if ( !containsAttribute(attributes, "ACTION") )
			throw newRunTimeException("Missing ACTION attribute; valid: update, delete, purge, refresh");

		String action	= getDynamic(attributes, _Session, "ACTION").getString().toLowerCase();

		if ( action.equals("delete") ){

   		functionBase	func	= new CustomDeleteFunction();
   		cfArgStructData functionArgs = getFunctionArgsFromAttributes( _Session, func.getFormals(), attributes );
   		func.execute(_Session, functionArgs);
   		return cfTagReturnType.NORMAL;

		} else if ( action.equals("purge") ){

   		functionBase	func	= new PurgeFunction();
   		cfArgStructData functionArgs = getFunctionArgsFromAttributes( _Session, func.getFormals(), attributes );
   		func.execute(_Session, functionArgs);
   		return cfTagReturnType.NORMAL;

		} else if ( action.equals("refresh") ){

   		functionBase	func	= new PurgeFunction();
   		cfArgStructData functionArgs = getFunctionArgsFromAttributes( _Session, func.getFormals(), attributes );
   		func.execute(_Session, functionArgs);
			action	= "update";	// set this to drop into the next IF

		} 
		
		
		if ( action.equals("update") ){
			
			String	type = "custom";
			if ( containsAttribute(attributes, "TYPE") )
				type = getDynamic(_Session, "TYPE").getString().toLowerCase();
			
			String status	 = null;
			if ( containsAttribute(attributes, "STATUS") )
				status = getDynamic(_Session, "STATUS").getString().toLowerCase();
			
			attributes = provideSupportForCustom1234( _Session, attributes );
			
			cfData returnStatus = null;
			
			if ( type.equals("custom") ){
			
				functionBase	func	= new CustomFunction();
	   		cfArgStructData functionArgs = getFunctionArgsFromAttributes( _Session, func.getFormals(), attributes );
	   		fixQueryParam( _Session, functionArgs );
	   		returnStatus = func.execute(_Session, functionArgs);
				
			}else if ( type.equals("file") ){

				functionBase	func	= new FileFunction();
	   		cfArgStructData functionArgs = getFunctionArgsFromAttributes( _Session, func.getFormals(), attributes );
	   		fixQueryParam( _Session, functionArgs );
	   		returnStatus = func.execute(_Session, functionArgs);
				
			}else if ( type.equals("path") ){

				functionBase	func	= new PathFunction();
	   		cfArgStructData functionArgs = getFunctionArgsFromAttributes( _Session, func.getFormals(), attributes );
	   		fixQueryParam( _Session, functionArgs );
	   		returnStatus = func.execute(_Session, functionArgs);
				
			}else if ( type.equals("website") ){

				functionBase	func	= new WebFunction();
	   		cfArgStructData functionArgs = getFunctionArgsFromAttributes( _Session, func.getFormals(), attributes );
	   		returnStatus = func.execute(_Session, functionArgs);
				
			}else
				throw newRunTimeException("invalid TYPE attribute; valid: custom, file, path, website");
			
			// Set the status
			if ( returnStatus != null && status != null )
				_Session.setData( status, returnStatus );
			
		} else
			throw newRunTimeException("invalid ACTION attribute; valid: update, delete, purge, refresh");
		
   	return cfTagReturnType.NORMAL;
	}

	
	
	private void fixQueryParam(cfSession _Session, cfArgStructData functionArgs) throws dataNotSupportedException {
		cfData	queryData	= functionArgs.getData("QUERY");
		
		if ( queryData == null || queryData.getDataType() == cfData.CFQUERYRESULTDATA )
			return;
		
		cfData	queryObj	= _Session.getData( queryData.getString() );
		functionArgs.setData( "QUERY", queryObj );
		
	}

	/**
	 * This provides backward support for the CUSTOM1..4 fields by looking for them and wrapping them into
	 * the custom map scope
	 * 
	 * @param _Session
	 * @param attributes
	 * @throws cfmRunTimeException
	 */
	private cfStructData provideSupportForCustom1234(cfSession _Session, cfStructData attributes) throws cfmRunTimeException {
		
		cfStructData	customMap;
		if ( containsAttribute(attributes, "CUSTOMMAP") ){
			customMap	= (cfStructData)getDynamic(attributes, _Session, "CUSTOMMAP");
		}else{
			customMap	= new cfStructData();
		}
		
		if ( containsAttribute(attributes, "CUSTOM1") )
			customMap.put("CUSTOM1", getDynamic(attributes,_Session,"CUSTOM1") );
		if ( containsAttribute(attributes, "CUSTOM2") )
			customMap.put("CUSTOM2", getDynamic(attributes,_Session,"CUSTOM2") );
		if ( containsAttribute(attributes, "CUSTOM3") )
			customMap.put("CUSTOM3", getDynamic(attributes,_Session,"CUSTOM3") );
		if ( containsAttribute(attributes, "CUSTOM4") )
			customMap.put("CUSTOM4", getDynamic(attributes,_Session,"CUSTOM4") );
		
		if ( customMap.size() > 0 ){
			if ( attributes == null )
				attributes	= new cfStructData();

			attributes.put("CUSTOMMAP", customMap );
		}
		
		return attributes;
	}
}