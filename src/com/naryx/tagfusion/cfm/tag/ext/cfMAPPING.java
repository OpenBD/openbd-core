/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: cfMAPPING.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.tag.ext;

import java.io.File;
import java.io.Serializable;

import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfmlURI;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfMAPPING extends cfTag implements Serializable {

	static final long serialVersionUID = 1;

	private static final String LOGICAL_PATH 		= "LOGICALPATH";
	private static final String DIRECTORY_PATH 	= "DIRECTORYPATH";
	private static final String RELATIVE_PATH 	= "RELATIVEPATH";
	private static final String ARCHIVE 				= "ARCHIVE";

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseTagHeader(_tag);
		if (!containsAttribute(LOGICAL_PATH))
			throw newBadFileException("Missing " + LOGICAL_PATH, "You must specify the " + LOGICAL_PATH + " attribute");
		
		if ( !containsAttribute(DIRECTORY_PATH) 
				&& !containsAttribute(RELATIVE_PATH) 
				&& !containsAttribute(ARCHIVE) )
			throw newBadFileException("Missing", "Please provide one of the following attributes: DIRECTORYPATH, RELATIVEPATH or ARCHIVE");
	}

	public java.util.Map getInfo(){
		return createInfo("engine", "Creates a new mapping for CFML/CFC resources. Use only one at a time, DIRECTORYPATH, RELATIVEPATH or ARCHIVE.   You can create a new mapping using MappingCreateArchive() function.");
	}
  
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
				createAttInfo( LOGICAL_PATH, "logical path of the mapping", "", true ),
				createAttInfo( DIRECTORY_PATH, "the directory (real path) to which the mapping will go", "", false ),
				createAttInfo( RELATIVE_PATH, "the directory (relative) to which the mapping will go", "", false ),
				createAttInfo( ARCHIVE, "the OpenBD archive file to use for the mapping.  Full real path to the file", "", false ),
		};
	}

	
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		String logicalPath = getDynamic(_Session, LOGICAL_PATH).getString();
		if (!logicalPath.startsWith("/"))
			logicalPath = "/" + logicalPath;

		File directory = null;
		
		if (containsAttribute(DIRECTORY_PATH)) {
			
			String directoryPath = getDynamic(_Session, DIRECTORY_PATH).getString();
			directory = new File(directoryPath);
			if (!directory.isAbsolute())
				throw new cfmRunTimeException(catchDataFactory.runtimeException(this, "'" + directoryPath + "' is not an absolute path; DIRECTORYPATH must specify an absolute path"));
			
			setMapping(_Session, logicalPath, directory);
			
		} else if ( containsAttribute(RELATIVE_PATH) ) {
			String relativePath = getDynamic(_Session, RELATIVE_PATH).getString();
			if (relativePath.charAt(0) == '/') { // relative to application root
				directory = new File(FileUtils.getRealPath(_Session.REQ, relativePath));
			} else { // relative to current template
				directory = new File(new cfmlURI(_Session.getPresentFilePath(), relativePath).getRealPath(_Session.REQ));
			}
			
			setMapping(_Session, logicalPath, directory);
		} else if ( containsAttribute(ARCHIVE) ){
			
			String archivePath	= getDynamic( _Session, ARCHIVE ).getString();
			File	archiveFile	= new File( archivePath );
			if ( !archiveFile.isFile() )
				throw newRunTimeException( "ARCHIVE is not pointing to a valida file: " + archiveFile );
			
			_Session.setCFMapping(logicalPath, "openbd://" + archiveFile + "@" );
		}

		return cfTagReturnType.NORMAL;
	}

	
	
	private void setMapping(cfSession _Session, String logicalPath, File directory) throws cfmRunTimeException {
		if (!directory.exists() || !directory.isDirectory())
			throw new cfmRunTimeException(catchDataFactory.runtimeException(this, "The directory '" + directory.getAbsolutePath() + "' does not exist or is not a directory"));
		
		if (!directory.canRead())
			throw new cfmRunTimeException(catchDataFactory.runtimeException(this, "Read permission is not available for directory '" + directory.getAbsolutePath() + "'"));
		
		String physicalPath = directory.getAbsolutePath();
		if (physicalPath.startsWith("/")) { // for UNIX systems
			physicalPath = "$" + physicalPath;
		}
		
		_Session.setCFMapping(logicalPath, physicalPath);
	}
}
