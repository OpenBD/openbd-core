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
 *  $Id: ConfigurableFileFilter.java 1638 2011-07-31 16:08:50Z alan $
 */

package com.bluedragon.search.index.crawl;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;


public class ConfigurableFileFilter implements FileFilter {
	private Set<String> acceptExt = null;

	private boolean recurse = false;
	private boolean acceptAllExt = false;
	private boolean acceptNoExt = false;

	public ConfigurableFileFilter(Set<String> exts, boolean recurse) {
		this.recurse = recurse;
		this.acceptExt = exts;
		this.acceptAllExt = false;
		this.acceptNoExt = false;
		
		if (acceptExt == null) {
			acceptAllExt = true;
		} else {
			if (acceptExt.contains(".*") || acceptExt.contains("*.*"))
				acceptAllExt = true;
			if (acceptExt.contains("*."))
				acceptNoExt = true;
		}
	}

	public boolean accept(File f) {
		if (f.isDirectory()) {
			return recurse;
		} else {
			if (acceptAllExt)
				return true;

			boolean hasNoExt = false;
			String ext = null;
			int ndx = f.getName().lastIndexOf(".");
			if (ndx == -1 || ndx + 1 == f.getName().length())
				hasNoExt = true;
			else
				ext = f.getName().substring(ndx).toLowerCase();

			if (hasNoExt && acceptNoExt)
				return true;
			else
				return (acceptExt.contains(ext));
		}
	}
}