/*
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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
 */

package com.naryx.tagfusion.cfm.engine;

import java.io.File;
import java.util.Calendar;

import javax.servlet.ServletConfig;

import com.nary.io.FileUtils;

public class cfWebServices extends Object {
	protected static final String CACHE_DIR = "ws";
	protected static cfWebServices instance = null;

	protected String javaCacheDir = null;
	protected String docRootDir = null;

	protected cfWebServices() {}

	public static void initialize(ServletConfig config) throws Exception {
		// This will be null when using the command-line tool for building admin.bda
		// [BlueDragonFileArchive.exe], so we guard against NullPointerExceptions in
		// that case.
		// Also protect against multiple initialization requests
		if (config != null && config.getServletContext() != null) {
			if (cfWebServices.instance == null) {
				synchronized (cfWebServices.class) {
					if (cfWebServices.instance == null) {
						cfWebServices.instance = new cfWebServices();
						cfWebServices.instance.internalInit(config);
					}
				}
			}
		}
	}
	

	protected void internalInit(ServletConfig config) throws Exception {
		String sessionKey = String.valueOf(Calendar.getInstance().getTime().getTime() % 999);

		// Setup the cache dir for all dynamic generated BD Java classes
		File dir	= FileUtils.checkAndCreateDirectory( cfEngine.thisPlatform.getFileIO().getTempDirectory(), CACHE_DIR, true );
		dir	= FileUtils.checkAndCreateDirectory( dir, sessionKey, true );
		
		this.javaCacheDir = dir.getAbsolutePath();
		
		// Capture the docroot so we can build a classpath when 
		// compiling dynamically created java files
		this.docRootDir = com.nary.io.FileUtils.getRealPath( "/" );
	}

	public static String getJavaCacheDir(){
		return cfWebServices.instance.javaCacheDir;
	}

	public static String getDocRootDir(){
		return cfWebServices.instance.docRootDir;
	}

}
