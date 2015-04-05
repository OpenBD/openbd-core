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
 *  $Id: cfDIRECTORY.java 2366 2013-05-18 20:47:20Z andy $
 */

/**
 * Implements CFDIRECTORY tag
 */

package com.naryx.tagfusion.cfm.tag.io;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.expression.function.file.DirectoryDelete;
import com.naryx.tagfusion.expression.function.file.DirectoryList;

public class cfDIRECTORY extends cfTag implements Serializable {
	static final long serialVersionUID = 1;


  @SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
  	return createInfo("file", "Performs a series of actions on directories");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
   			createAttInfo( "ACTION", "The operation to be performed", 	"list", true ),

   			createAttInfo( "ACTION=LIST", 	"List the given directory", 	"", false ),
   			createAttInfo( "ACTION=DELETE", "Delete the given directory", 	"", false ),
   			createAttInfo( "ACTION=CREATE", "Create the given directory", 	"", false ),
   			createAttInfo( "ACTION=RENAME", "Rename the given directory", 	"", false ),

   			createAttInfo( "RECURSE", "For ACTION=LIST,DELETE flag to control if it recurses down the subdirectories", 	"false", false ),
   			createAttInfo( "DIRECTORY", "The real path of the directory", 	"", false ),
   			createAttInfo( "NAME", "The name of the variable to receive the directory listing query", 	"", false ),
   			createAttInfo( "NEWDIRECTORY", "For ACTION=RENAME is the name of the new directory", 	"", false ),
   			createAttInfo( "MODE", "The mode of the new directory ACTION=CREATE", 	"", false ),
   			createAttInfo( "TYPE", "The type of listing to perform; valid values: file, dir or all", 	"all", false ),
   			createAttInfo( "LISTINFO", "How much detail do you want returned on the listing; valid values: all or name.  Much faster for just name", 	"all", false ),
   			createAttInfo( "FILTER", "The regex to filter on", 	"", false ),
   			createAttInfo( "SORT", "If listing a directory, a comma separated list the query columns to sort on and in which direction e.g. 'name asc, size desc'", 	"", false ),
  	};
  }
	
	
	
	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute("ACTION", 			"LIST");
		defaultAttribute("URIDIRECTORY", "NO");

		parseTagHeader(_tag);
		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;
    
		if (!containsAttribute("DIRECTORY"))
			throw newBadFileException("Missing DIRECTORY", "You need to specify a DIRECTORY");
		else if (getConstant("ACTION").equalsIgnoreCase("list") && !containsAttribute("NAME"))
			throw newBadFileException("Invalid ACTION", "You must have a NAME if ACTION=LIST");
		else if (getConstant("ACTION").equalsIgnoreCase("rename") && !containsAttribute("NEWDIRECTORY"))
			throw newBadFileException("Invalid RENAME", "You must have a NEWDIRECTORY if ACTION=RENAME");
	}

  
	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if (!containsAttribute(attributes,"DIRECTORY"))
			throw newBadFileException("Missing DIRECTORY", "You need to specify a DIRECTORY");
		else if (getDynamic(attributes,_Session,"ACTION").getString().equalsIgnoreCase("list") && !containsAttribute(attributes,"NAME"))
			throw newBadFileException("Invalid ACTION", "You must have a NAME if ACTION=LIST");
		else if (getDynamic(attributes,_Session,"ACTION").getString().equalsIgnoreCase("rename") && !containsAttribute(attributes,"NEWDIRECTORY"))
			throw newBadFileException("Invalid RENAME", "You must have a NEWDIRECTORY if ACTION=RENAME");

		return	attributes;
	}

	
	
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);
  	
		try {
			String ACTION = getDynamic(attributes,_Session,"ACTION").getString().toLowerCase();
			boolean bURI = false;

			if (getDynamic(attributes,_Session, "URIDIRECTORY").getBoolean())
				bURI = true;

			if (ACTION.equals("delete"))
				deleteDirectory(attributes,_Session, bURI);
			else if (ACTION.equals("rename"))
				renameDirectory(attributes,_Session, bURI);
			else if (ACTION.equals("create"))
				createDirectory(attributes,_Session, bURI);
			else
				listDirectory(attributes,_Session, bURI);

			return cfTagReturnType.NORMAL;
		} catch (SecurityException secExc) {
			throw newRunTimeException("CFDIRECTORY is not supported if it does not have FileIOPermission to the specified directory.");
		}
	}

	// ------------------------------------------------------------

	private void deleteDirectory(cfStructData attributes, cfSession _Session, boolean bURI) throws cfmRunTimeException {
		File thisDir;
		if (bURI)
			thisDir = FileUtils.getRealFile(_Session.REQ, getDynamic(attributes,_Session, "DIRECTORY").getString());
		else
			thisDir = new File(getDynamic(attributes,_Session, "DIRECTORY").getString());

		boolean recurse = false;
		if (containsAttribute(attributes,"RECURSE")) {
			recurse = getDynamic(attributes,_Session, "RECURSE").getBoolean();
		}

		try {
			DirectoryDelete.deleteDirectory(thisDir, recurse);
		} catch (Exception e) {
			throw newRunTimeException(e.getMessage());
		}
	}



	// ------------------------------------------------------------

	private void renameDirectory(cfStructData attributes, cfSession _Session, boolean bURI) throws cfmRunTimeException {
		File oldDirectory;
		if (bURI)
			oldDirectory = FileUtils.getRealFile(_Session.REQ, getDynamic(attributes,_Session, "DIRECTORY").getString());
		else
			oldDirectory = new File(getDynamic(attributes,_Session, "DIRECTORY").getString());

		if (!oldDirectory.isDirectory())
			throw newRunTimeException("RENAME.is not a directory: " + oldDirectory);

		File newDirectory;
		String newDirString = getDynamic(attributes,_Session, "NEWDIRECTORY").getString();
		if (bURI)
			newDirectory = FileUtils.getRealFile(_Session.REQ, newDirString);
		else {
			newDirectory = new File(newDirString);
			if (!newDirectory.isAbsolute()) {
				newDirectory = new File(oldDirectory.getParentFile(), newDirString);
			}
		}

		if (newDirectory.exists())
			throw newRunTimeException("RENAME.The new directory name already exist: " + newDirectory);

		oldDirectory.renameTo(newDirectory);
	}

	// ------------------------------------------------------------

	private void createDirectory(cfStructData attributes, cfSession _Session, boolean bURI) throws cfmRunTimeException {

		File newDirectory;
		if (bURI)
			newDirectory = FileUtils.getRealFile(_Session.REQ, getDynamic(attributes,_Session, "DIRECTORY").getString());
		else
			newDirectory = new File(getDynamic(attributes,_Session, "DIRECTORY").getString());

		if (newDirectory.exists()) {
			throw newRunTimeException("The directory, " + newDirectory + ", already exists");
		}
		
		// we know that the directory doesn't exist at this point so mkdirs() will only return
		// false if it couldn't create the directory
		if (!newDirectory.mkdirs() && !newDirectory.isDirectory()) {
			throw newRunTimeException("The directory, " + newDirectory + ", could not be created due to possible permissions issues.");
		}

		// if MODE specified and running on UNIX/Linux/Mac OS X
		if (containsAttribute(attributes,"MODE") && !cfEngine.WINDOWS) {
			String mode = getDynamic(attributes,_Session, "MODE").getString();
			if (mode.length() != 3) {
				throw newRunTimeException("Invalid MODE specified: " + mode + ". Expected a three digit octal value e.g. 755.");
			}
			try {
				Runtime.getRuntime().exec("chmod " + mode + " " + newDirectory.getAbsolutePath()).waitFor();
			} catch (IOException ioe) {
				throw newRunTimeException("Failed to modify file attributes. " + ioe.getMessage());
			} catch (InterruptedException ignored) {
			}
		}
	}

	// ------------------------------------------------------------
	// ------------------------------------------------------------
	// ------------------------------------------------------------

	private void listDirectory(cfStructData attributes, cfSession _Session, boolean bURI) throws cfmRunTimeException {
		File directory;

		if (bURI) {
			directory = FileUtils.getRealFile(_Session.REQ, getDynamic(attributes,_Session, "DIRECTORY").getString());
		} else {
			directory = new File(getDynamic(attributes,_Session, "DIRECTORY").getString());
		}

		// Gather up the parameters
		boolean recurse = false;
		if (containsAttribute(attributes,"RECURSE")) {
			recurse = getDynamic(attributes,_Session, "RECURSE").getBoolean();
		}
		
		String type = null;
		if ( containsAttribute(attributes,"TYPE") )
			type = getDynamic(attributes,_Session, "TYPE").getString();
		
		String listinfo = "all";
		if ( containsAttribute(attributes,"LISTINFO") )
			listinfo = getDynamic(attributes,_Session, "LISTINFO").getString();

		String filter = null;
		if ( containsAttribute(attributes,"FILTER") )
			filter = getDynamic(attributes,_Session, "FILTER").getString();

		String sort = null;
		if ( containsAttribute(attributes,"SORT") )
			sort = getDynamic(attributes,_Session, "SORT").getString();
		
		cfQueryResultData result;
		try {
			result = (cfQueryResultData)DirectoryList.listDirectory(directory, recurse, "all", filter, sort, type);
		
			if ( listinfo.equalsIgnoreCase("name") ){
				result.deleteColumn("size");
				result.deleteColumn("type");
				result.deleteColumn("directory");
				result.deleteColumn("datelastmodified");
				result.deleteColumn("mode");
				result.deleteColumn("attributes");
			}
			
			String name = getDynamic(attributes,_Session, "NAME").getString();
			cfData d = runTime.runExpression(_Session, name, false);
			if (d instanceof com.naryx.tagfusion.cfm.parser.cfLData) {
				((com.naryx.tagfusion.cfm.parser.cfLData) d).Set(result, _Session.getCFContext());
			}
			
		} catch (Exception e) {
			throw newRunTimeException( e.getMessage() );
		}
	}
}
