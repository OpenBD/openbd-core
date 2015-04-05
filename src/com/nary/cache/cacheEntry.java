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

/*
 * This is the element that is used for the LRUCache and it is this
 * class that is serialised out to the persisted cache; be it JDBC or
 * disk. 
 */
package com.nary.cache;

import java.io.Serializable;
import java.util.Set;

public class cacheEntry implements Serializable {
	
	static final long serialVersionUID = -5309402182669148293L;
	
	transient Set<String>	group;
	
	Object 	data;
	String	key, groupkey;
	long		dateCreated = 0;
	boolean wasInGroup = false, dataPersisted=false;

	public cacheEntry(String _key, Object _data ){
		this(_key,null,_data);
	}
	
	public cacheEntry(String _key, String _groupKey, Object _data ){
		key					= _key;
		groupkey		= _groupKey;
		group				= null;
		data				= _data;
		dateCreated	= System.currentTimeMillis();
	}
	
	public void setGroup(Set<String> _group){
		group				= _group;
		wasInGroup 	= group.contains(key);
		if (!wasInGroup){ 
			synchronized(group){group.add(key);}
		}
	}
	
	public void setPersistedState(boolean bPersisted){
		dataPersisted = bPersisted;
	}
	
	public final Object getData(){ return data; }
	public final long		getCreationDate(){ return dateCreated; }
	
	public final boolean expired(long age){
		return (age == 0) || (System.currentTimeMillis()-dateCreated) > age;
	}
}
