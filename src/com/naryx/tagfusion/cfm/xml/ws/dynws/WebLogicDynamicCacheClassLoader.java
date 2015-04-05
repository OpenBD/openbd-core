/* 
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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

package com.naryx.tagfusion.cfm.xml.ws.dynws;

/**
 * Loads dynamically generated class definitions from the filesystem to be used
 * by Axis for web service purposes. This subclass is specific to BEA WebLogic
 * such that it uses the ClassLoader that has access to the
 * javax.xml.rpc.Service class instead of the context ClassLoader. This
 * overcomes a linkage problem only found with BEA WebLogic.
 */
public class WebLogicDynamicCacheClassLoader extends DynamicCacheClassLoader {
	/**
	 * Constructs a new DynamicCacheClassLoader instance with the specified
	 * directory as the directory it can load class definitions from and with the
	 * specified category (either STUB_CLASSES or SKEL_CLASSES).
	 * 
	 * NOTE: as this constructor alters the static list of STUB and SKEL
	 * DynamicCacheClassLoaders, it should be called from within a block that's
	 * synchronized on either the DynamicCacheClassLoader.STUB_MUTEX or the
	 * DynamicCacheClassLoader.SKEL_MUTEX.
	 * 
	 * @param dir
	 * @param cat
	 */
	public WebLogicDynamicCacheClassLoader(String dir, int cat) {
		super(javax.xml.rpc.Service.class.getClassLoader(), dir, cat);
		// super(Thread.currentThread().getContextClassLoader());
	}
}
