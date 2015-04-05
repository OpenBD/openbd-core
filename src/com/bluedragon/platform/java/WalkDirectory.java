/* 
 *  Copyright (C) 2000 - 2015 aw2.0Ltd
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
 *  README.txt @ http://openbd.org/license/README.txt
 *  
 *  http://openbd.org/
 *  $Id: WalkDirectory.java 2497 2015-02-02 01:53:48Z alan $
 */
package com.bluedragon.platform.java;

import java.io.File;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * Simple class to recursively loop through a directory and return them all 
 */
public class WalkDirectory {

	private final File directory;

	public WalkDirectory(File directory) {
		this.directory	= directory;
	}
	
	
	/**
	 * Performs a walk of the size and pulls back the total number of files
	 * @return
	 */
	public long getTotalFileSize(){
		return walkForSize(directory);
	}

	private long walkForSize(File directory) {
		File[] list = directory.listFiles();

		if (list == null)
			return 0;

		long size = 0;
		
		for (File f : list) {
			if (f.isDirectory()) {
				size += walkForSize(f);
			} else {
				size += f.length();
			}
		}
		
		return size;
	}

	
	private void walk(List<File> allFiles, File directory) {
		File[] list = directory.listFiles();

		if (list == null)
			return;

		for (File f : list) {
			if (f.isDirectory()) {
				walk(allFiles, f);
			} else {
				allFiles.add(f);
			}
		}
	}

	
	/**
	 * Walks through the directory pulling all the files, in an iterator
	 * @return
	 */
	public Iterator<File> iteratorAllFiles() {
		List<File> allFiles = new LinkedList<File>();
		walk(allFiles, directory);
		
		java.util.Collections.sort( allFiles, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				if ( o1.lastModified() == o2.lastModified() )
					return 0;
				else
					return (o1.lastModified() > o2.lastModified()) ? -1 : 1;
			}
		});
		
		return allFiles.iterator();
	}

}
