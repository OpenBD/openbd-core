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
 */

package com.naryx.tagfusion.cfm.application;

import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStructData;

public class cfStructExpireData extends cfStructData implements java.io.Serializable {
	
	static final long serialVersionUID = 1;

	private long lastUsed;
	
	public cfStructExpireData(){
		super();
		lastUsed = System.currentTimeMillis();
	}
	
	/**
	 * 	This constructor is for subclasses that want to override the private
	 * 	hashdata attribute to use an alternate data store (see the comment
	 * 	labeled "SUBCLASSES" in cfStructData).
	 */
	protected cfStructExpireData( Map<String, cfData> hashdata ) {
		super( hashdata );
		lastUsed = System.currentTimeMillis();
	}
	
	public void setLastUsed(){
		lastUsed = System.currentTimeMillis();
	}
	
	public long getLastUsed(){
		return lastUsed;
	}
}
