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
 *  $Id: cfZIPPARAM.java 1538 2011-04-14 07:26:08Z alan $
 */

package com.naryx.tagfusion.cfm.tag.io;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.filePatternFilter;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfZIPPARAM extends cfTag implements Serializable {
	static final long	serialVersionUID	= 1;

	
	@SuppressWarnings("unchecked")
	public java.util.Map getInfo() {
		return createInfo("file", "To be used inside the CFZIP tag.  This tag lets you add files to your zip, optionally changing their path within the ZIP file");
	}

	@SuppressWarnings("unchecked")
	public java.util.Map[] getAttInfo() {
		return new java.util.Map[] { 
				createAttInfo("SOURCE", "The source of the file or directory.  This is a real path", "", true),
				createAttInfo("PREFIX", "The prefix path for this file(s) inside the ZIP archive", "", false), 
				createAttInfo("RECURSE", "If SOURCE is a directory then controls where all the subdirectories will be added", "true", false), 
				createAttInfo("FILTER", "If SOURCE is a directory then this is an optional filter that can applied to the files lists", "", false), 
				createAttInfo("NEWPATH", "Use this path inside the ZIP archive instead of the relative path of the SOURCE", "", false), 
		};
	}



	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute("PREFIX", "");
		defaultAttribute("RECURSE", "true");
		parseTagHeader(_tag);

		if (!containsAttribute("SOURCE")) {
			throw newBadFileException("Missing Attribute", "This tag requires a SOURCE attribute");
		}
	}



	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		List<cfZipItem> zipData = (List<cfZipItem>) _Session.getDataBin(cfZIP.DATA_BIN_KEY);

		if (zipData == null) {
			throw newRunTimeException("The CFZIPITEM tag must be nested inside a CFZIP tag.");
		}

		String path = getDynamic(_Session, "SOURCE").getString(); // source or (directory and // file
		String prefix = getDynamic(_Session, "PREFIX").getString();

		File source = null;
		try {
			// lets make sure we have a clean file path now to make things easier later
			source = new File(new File(path).getCanonicalPath());
			if (!source.exists()) { // error checking
				throw newRunTimeException("The SOURCE specified does not exist or cannot be read [" + path + "].");
			}
		} catch (java.io.IOException e) {
			throw newRunTimeException("The SOURCE specified does not exist or cannot be read [" + path + "]: " + e.getClass().getName() + "[" + e.getMessage() + "]");
		}

		cfZipItem newItem = null;
		if (!containsAttribute("NEWPATH")) {
			newItem = new cfZipItem(source, prefix);
		} else {
			String newpath = getDynamic(_Session, "NEWPATH").getString();
			if (newpath.length() == 0) {
				throw newRunTimeException("NEWPATH specified is an empty string. It not a valid path.");
			}
			newItem = new cfZipItem(source, prefix, newpath);
		}

		if (containsAttribute("FILTER")) {
			newItem.setFilter(new filePatternFilter(getDynamic(_Session, "FILTER").getString(), true));
		}

		newItem.setRecurse(getDynamic(_Session, "RECURSE").getBoolean());
		zipData.add(newItem);

		return cfTagReturnType.NORMAL;
	}

}
