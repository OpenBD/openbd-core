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
 *  http://openbd.org/
 *  $Id: DirectoryList.java 2366 2013-05-18 20:47:20Z andy $
 */


package com.naryx.tagfusion.expression.function.file;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;

import com.nary.io.FileUtils;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.vfs.cfVFSData;
import com.naryx.tagfusion.expression.function.functionBase;

public class DirectoryList extends functionBase {
  private static final long serialVersionUID = 1L;

  public DirectoryList(){ 
  	min = 1; max = 5;
  	setNamedParams( new String[]{ "path", "recurse", "listinfo", "filter", "sort", "type" } );
  }
  
	public String[] getParamInfo(){
		return new String[]{
			"the path of the directory to list",
			"whether it should list all the sub-directories",
			"Values: name, path, query.  If 'name' returns a array of file names.  'path' full path of each one in an array, 'query' returns a query object",
			"File extension to filter on",
			"A comma separated list the query columns to sort on and in which direction e.g. 'name asc, size desc'",
			"The type of listing to perform; valid values: file, dir or all"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"file", 
				"For the given directory get the file contents {supports Amazon S3}", 
				ReturnType.ARRAY );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
  	String inFile	= getNamedStringParam(argStruct, "path", null );
  	if ( inFile == null )
  		throwException(_session, "invalid 'directory' attribute");
  	

  	cfVFSData vfsData = null;
  	try{
  		vfsData	= new cfVFSData( inFile );
  		cfArrayData a = cfArrayData.createArray(1);
  		
  		if ( !vfsData.isNative() ){
  			
  			FileObject[] list = vfsData.getFileObject().getChildren();
    		for ( int x=0; x < list.length; x++ ){
    			a.addElement( new cfStringData( list[x].getName().getBaseName() ) );
    		}
    		return a;
    		
  		}else{
  			
  			boolean recurse	= getNamedBooleanParam(argStruct, "recurse", false );
  			String listinfo	= getNamedStringParam(argStruct, 	"listinfo", "path" );
  			String filter		= getNamedStringParam(argStruct, 	"filter", null );
  			String sort			= getNamedStringParam(argStruct, 	"sort", null );
  			String type			= getNamedStringParam(argStruct, 	"type", null );
  			
  			return listDirectory( vfsData.getFile(), recurse, listinfo, filter, sort, type );
  		}

  	}catch(Exception e){
  		throwException( _session, "File [" + inFile + "] caused an error (" + e.getMessage() + ")" );
  		return cfBooleanData.FALSE;
  	}finally{
  		
  		if ( vfsData != null ){
  			try{
  				vfsData.close();
  			}catch(Exception e){}
  		}
  	}
  }
	
	
	
	public static cfData	listDirectory( File directory, boolean recurse, String listinfo, String filter, String sort, String type ) throws Exception{
		
		if ( !directory.exists() || !directory.isDirectory() )
			throw new Exception("invalid directory: " + directory );
		
		// Determine the List type
		int listType;
		if ( type == null )
			listType = FileUtils.LIST_TYPE_ALL;
		else if ( type.equalsIgnoreCase("file") )
			listType = FileUtils.LIST_TYPE_FILE;
		else if ( type.equalsIgnoreCase("dir") )
			listType = FileUtils.LIST_TYPE_DIR;
		else if ( type.equalsIgnoreCase("all") )
			listType = FileUtils.LIST_TYPE_ALL;
		else
			throw new Exception("Invalid TYPE specified: " + type + ". Expected ALL/FILE/DIR");	
		
		// Determine the return type
		int listInfo;
		boolean bReturnQuery = false;
		if ( listinfo == null )
			listInfo = FileUtils.LIST_INFO_ALL;
		else if ( listinfo.equalsIgnoreCase("all") ){
			listInfo = FileUtils.LIST_INFO_ALL;
			bReturnQuery = true;
		}else if ( listinfo.equalsIgnoreCase("name") )
			listInfo = FileUtils.LIST_INFO_NAME;
		else if ( listinfo.equalsIgnoreCase("path") )
			listInfo = FileUtils.LIST_INFO_ALL;
		else if ( listinfo.equalsIgnoreCase("query") ){
			listInfo = FileUtils.LIST_INFO_ALL;
			bReturnQuery = true;
		}else
			throw new Exception("Invalid LISTINFO specified: " + listinfo + ". Expected ALL/NANE");	
		
		
		// Create the actual file listing
		List<Map<String, cfData>> vResults;
		if ((filter != null) && (filter.length() > 0)) {
			vResults = FileUtils.createFileVector(directory, filter, recurse, listType, listInfo);
		} else {
			vResults = FileUtils.createFileVector(directory, recurse, listType, listInfo);
		}

		if (vResults == null)
			throw new Exception("Error getting directory list");
		
		
		if ( sort != null && sort.trim().length() != 0 )
			sortResults(vResults, sort);
		
		
		if ( bReturnQuery ){
			cfQueryResultData queryResult = new cfQueryResultData(new String[] { "name", "attributes", "datelastmodified", "directory", "mode",  "size", "type" }, "CFDIRECTORY");
			queryResult.populateQuery(vResults);
			return queryResult;
		}else{
			cfArrayData	arr	= cfArrayData.createArray(1);

			Iterator<Map<String, cfData>>	it	= vResults.iterator();
			while ( it.hasNext() ){
				Map<String, cfData> m = it.next();
				
				if ( listInfo == FileUtils.LIST_INFO_ALL  )
					arr.addElement( new cfStringData( m.get("directory").getString() + File.separator + m.get("name").getString() ) );	
				else
					arr.addElement( m.get("name") );
			}

			return arr;
		}
	}
	
	
	private static void sortResults(List<Map<String, cfData>> results, String sortString) {

		List<String> tokenList = string.split(sortString.toLowerCase(), ",");
		String sortCol;
		boolean bAscending;

		String[] tokens = new String[tokenList.size()];

		for (int i = 0; i < tokenList.size(); i++) {
			tokens[i] = tokenList.get(i).trim();
		}

		Comparator<Map<String, cfData>> comparator = null;
		for (int i = tokens.length - 1; i >= 0; i--) {
			String subSort = tokens[i];
			bAscending = true;

			// --[ Find out the order
			int c1 = subSort.indexOf(" ");
			if (c1 == -1)
				sortCol = subSort;
			else {
				sortCol = subSort.substring(0, c1).trim();
				String orderby = subSort.substring(c1 + 1);
				if (!orderby.equals("asc"))
					bAscending = false;
			}

			comparator = new dirComparator(sortCol, bAscending, comparator);
		}

		// -- Perform the sort
		Collections.sort(results, comparator);
	}
	
}