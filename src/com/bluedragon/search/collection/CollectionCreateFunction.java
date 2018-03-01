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
 *  $Id: CollectionCreateFunction.java 1733 2011-10-10 20:24:57Z alan $
 */


package com.bluedragon.search.collection;

import java.io.File;
import java.io.IOException;

import com.bluedragon.search.AnalyzerFactory;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class CollectionCreateFunction extends functionBase {
	private static final long	serialVersionUID	= 1L;

	public CollectionCreateFunction(){
		min = 1;
		max = 6;
		setNamedParams( new String[]{ "collection", "storebody", "language", "path", "relative", "onexists" } );
	}
	
	
  public String[] getParamInfo(){
		return new String[]{
			"the name of the collection",
			"a flag to determine if the body is stored as a whole in the index.  For large collections this can take up a lot of space. defaults to false",
			"language this collection is using. defaults to 'english'. Valid: english, german, russian, brazilian, korean, chinese, japanese, czech, greek, french, dutch, danish, finnish, italian, norwegian, portuguese, spanish, swedish",
			"path to where the collection will be created.  If omitted then it will be created in the working directory under the 'cfcollection' directory",
			"a flag to determine if the 'path' attribute, if presented, is relative to the web path",
			"define what to do if the collection already exists or the path contains an existing index. Valid options are ERROR (default) and SKIP"
		};
  }
	
  
	public java.util.Map getInfo(){
		return makeInfo(
				"search", 
				"Creates a brand new collection.  If it already exists then this function will throw an exception", 
				ReturnType.BOOLEAN );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		String name	= getNamedStringParam(argStruct, "collection", null );
		if ( name == null )
			throwException(_session, "please specifiy the 'collection' attribute" );
		
		String onExists =  getNamedStringParam(argStruct, "onexists", "ERROR" ).toUpperCase();
		boolean errorOnExists = onExists.equals("ERROR");
		
		if ( CollectionFactory.isCollection(name) ) {
			if ( errorOnExists ) {
				throwException(_session, "collection, " + name + ", already exists");
			}else {
				return cfBooleanData.TRUE;
			}
		}
		
		boolean bStoreBody	= getNamedBooleanParam(argStruct, "storebody", false );
		String language			= getNamedStringParam(argStruct, "language", "english" );
		if ( !AnalyzerFactory.isValid(language) )
			throwException(_session, "specified language, " + language + ", not supported" );
		
		boolean bRelative		= getNamedBooleanParam(argStruct, "relative", false );
		String pathIn				= getNamedStringParam(argStruct, "path", null );		
		String path 				= getPath( name, pathIn,  bRelative );
		if ( path == null )
			throwException(_session, "problem creating path" );
		
		Collection	col	= new Collection();
		try {
			// Make the call to actually create
			col.setName( name );
			col.setStoreBody(bStoreBody);
			col.setLanguage(language);
			
			col.setDirectory(path);

			col.create( errorOnExists );
			CollectionFactory.addCollection(col);

			// update the xmlconfig
      String collectionkey = "server.cfcollection.collection[" + name.toLowerCase() + "]";
      cfEngine.getConfig().setData(collectionkey + ".name", 			name);
      cfEngine.getConfig().setData(collectionkey + ".language", 	language );
      cfEngine.getConfig().setData(collectionkey + ".storebody", 	bStoreBody ? "true" : "false" );
      cfEngine.getConfig().setData(collectionkey + ".relative", 	bRelative ? "true" : "false" );
      
      if ( bRelative && pathIn != null ){
      	cfEngine.getConfig().setData(collectionkey + ".path", pathIn );
      }else{
      	cfEngine.getConfig().setData(collectionkey + ".path", path );
      }
      cfEngine.writeXmlFile(cfEngine.getConfig());
			
		} catch (Exception e) {
			throwException( _session, "Failed to create the collection: " + e.getMessage() );
		}

		return cfBooleanData.TRUE;
	}
	
	
	private String getPath( String name, String path, boolean bRelative ){
		
		try{
			File filePath;
			
			if ( path == null ){
				filePath	= new File(cfEngine.thisPlatform.getFileIO().getWorkingDirectory() + File.separator + "cfcollection", name);
			}else{
				if ( bRelative ){
					filePath	= cfEngine.getResolvedFile(path);
				}else{
					filePath	= new File( path );
				}
			}
			
			if ( filePath.exists() && filePath.isDirectory() )
				return filePath.getCanonicalPath();
			else if ( !filePath.exists() && filePath.mkdirs() )
				return filePath.getCanonicalPath();
			
		}catch(IOException ioe){
			cfEngine.log( "CollectionCreateFunction:" + ioe.getMessage() );
		}
		
		return null;
	}
}
