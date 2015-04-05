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
 *  $Id: $
 */

package com.naryx.tagfusion.cfm.cache;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStructData;

public interface CacheInterface {
	
	/**
	 * Methods for setting the object
	 * 
	 * @param key
	 * @param data
	 * @param ageMs - minus value means to cache forever effectively
	 * @param idleTime - how long before this query is removed from memory if not used
	 */
	public void set( String id, cfData data, long ageMs, long idleTime );
	public cfData	get( String id );
	
	
	/**
	 * Methods for deleting the object
	 * @param id
	 */
	public void delete( String id, boolean exact );
	public void deleteAll();
		
	public String getName();

	public cfArrayData	getAllIds();
	
	/**
	 * Statistics for the engine 
	 * @return
	 */
	public cfStructData	getStats();
	
	public cfStructData	getProperties();
	public void setProperties( String region, cfStructData props ) throws Exception ;
	
	public void shutdown();

}
