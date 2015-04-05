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
 *  $Id: NullCacheImpl.java 2131 2012-06-27 19:02:26Z alan $
 */

package com.naryx.tagfusion.cfm.cache.impl;

import com.naryx.tagfusion.cfm.cache.CacheInterface;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStructData;

/*
 * The purpose of this class is to provide an empty implementation
 * that makes the logic of any using class always assume a cache
 * exists, but since this will never cache anything it can be safe
 * to use. 
 */
public class NullCacheImpl implements CacheInterface {

	@Override
	public void set(String id, cfData data, long ageMs, long idleTime) {
	}

	@Override
	public cfData get(String id) {
		return null;
	}

	@Override
	public void delete(String id, boolean exact ) {
	}

	@Override
	public void deleteAll() {
	}

	@Override
	public String getName() {
		return "NoCache";
	}

	@Override
	public cfArrayData getAllIds() {
		return null;
	}

	@Override
	public cfStructData getStats() {
		return null;
	}

	@Override
	public cfStructData getProperties() {
		return null;
	}
	
	public void setProperties( String region, cfStructData props ) throws Exception {
	}

	@Override
	public void shutdown() {
	}

}
