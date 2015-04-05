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
 *  http://openbd.org/
 *  $Id: LinkedHashMap.java 1907 2011-12-30 22:44:39Z alan $
 */
package com.naryx.tagfusion.expression.function.struct;


public class LinkedHashMap<K,V> extends java.util.LinkedHashMap<K,V> {
	private static final long serialVersionUID = 1L;
	
	private final int maxItems;
	private cfStructLinkData	listener;
	
	public LinkedHashMap( int maxItems){
		super();
		
		this.listener	= null;
		this.maxItems	= maxItems;
	}
	
	public void registerListener(cfStructLinkData	_listener){
		listener = _listener;
	}
	
	@Override
	public synchronized V get(Object key) {
		return super.get(key);
	}
	
	@Override
	public synchronized V put( K key, V value ){
		return super.put( key, value );
	}
	
	@Override
	protected synchronized boolean removeEldestEntry(java.util.Map.Entry eldest) {
		if ( super.size() > maxItems ){
			if ( listener != null )
				listener.elementRemoved( eldest.getKey(), eldest.getValue() );

			return true;
		}else
			return false;
	}
}
