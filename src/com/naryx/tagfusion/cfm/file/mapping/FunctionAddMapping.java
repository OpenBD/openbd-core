/* 
 *  Copyright (C) 2012 TagServlet Ltd
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
 *  $Id: FunctionAddMapping.java 2151 2012-07-04 13:46:43Z alan $
 */
package com.naryx.tagfusion.cfm.file.mapping;

import java.io.File;
import java.io.IOException;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfmlFileCache;
import com.naryx.tagfusion.expression.function.functionBase;

public class FunctionAddMapping extends functionBase {
	private static final long serialVersionUID = 1L;
	
	
	public FunctionAddMapping(){
		min = 2; max = 3;
		setNamedParams( new String[] { "logicalpath", "directory", "archive", "scope" } );
	}
	

	public String[] getParamInfo(){
		return new String[]{
			"logical path of the mapping",
			"the directory (real path) to which the mapping will go.  Exclusive with archive",
			"the OpenBD archive file to use for the mapping. You can create a new mapping using MappingCreateArchive() function",
			"scope of the mapping; 'global' to add this mapping to the global list, or 'request' for only this page request"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"engine", 
				"Creates a new mapping for CFML/CFC resources.  You must specify either directory or archive, but not both", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		
		// Get the logical path
		String	logicalpath	= getNamedStringParam(argStruct, "logicalpath", null );
		if ( logicalpath == null )
			throwException(_session, "missing the 'logicalpath' parameter");

		if (!logicalpath.startsWith("/"))
			logicalpath = "/" + logicalpath;
		

		// Directory/Archive
		String	directory	= getNamedStringParam(argStruct, "directory", null );
		String	archive		= getNamedStringParam(argStruct, "archive", null );
		if ( directory == null && archive == null )
			throwException(_session, "specify either the 'directory' or 'archive' parameter");
		else if ( directory != null && archive != null )
			throwException(_session, "specify either the 'directory' or 'archive' parameter, not both");
			

		if ( directory != null && !new File(directory).isDirectory() )
			throwException(_session, "invalid directory: " + directory );
		else if ( archive != null && !new File(archive).isFile() )
			throwException(_session, "invalid archive: " + archive );
		
		
		// Get the scope
		String scope	= getNamedStringParam(argStruct, "scope", "request").toLowerCase();
		if ( !scope.equals("request") && !scope.equals("global") )
			throwException(_session, "invalid scope: " + scope + "; must be only 'global' or 'request'" );

		
		// If archive then map it to the OpenBD protocol
		if ( archive != null ){
			try {
				directory	= "openbd://" + new File(archive).getCanonicalPath() + "@";
			} catch (IOException e) {
				throwException(_session, "invalid archive: " + archive );
			}
		}
		
		
		// Add the mapping
		if ( scope.equals("request") ){
			_session.setCFMapping( logicalpath, directory );
		}else{
			cfmlFileCache.getCFMappings().put(logicalpath, directory);
		}

		return cfBooleanData.TRUE;
	}
}