/* 
 *  Copyright (C) 2012 TagServlet Ltd
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
 *  $Id: CacheUnit.java 2131 2012-06-27 19:02:26Z alan $
 */
package com.naryx.tagfusion.cfm.cache.impl;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfData;


public class CacheUnit implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String id;
	public cfData	val;
	public long	createdTime;
	public long	expireTime;

	
	public CacheUnit( String id, cfData val, long ageMS ){
		this.createdTime	= System.currentTimeMillis();
		
		if ( ageMS > 0 )
			this.expireTime	= this.createdTime + ageMS;
		else
			this.expireTime	= Long.MAX_VALUE; 

		this.id		= id;
		this.val	= val;
	}


	public boolean stillYoung() {
		return ( this.expireTime > System.currentTimeMillis() );
	}
	
}