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
package org.alanwilliamson.lang.java.inline;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.naryx.tagfusion.cfm.engine.cfArrayListData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;

public class cfArray extends Object implements List {

	private cfArrayListData cfdata;
	
	public cfArray( cfArrayListData data ){
		this.cfdata	= data;
	}
	
	public String getName() {
		return "cfArray";
	}
	
	public Object get(int index){
		try {
			return ContextImpl.getForJava( cfdata.getElement( index+1 ) );
		} catch (Exception e) {
			return null;
		}
	}
	
	public boolean add(Object value) {
		try {
			cfdata.addElement( tagUtils.convertToCfData( value ) );
		} catch (cfmRunTimeException e) {}
		return true;
	}

	public void add(int index, Object value) {
		try {
			cfdata.addElementAt( tagUtils.convertToCfData( value ), index + 1 );
		} catch (cfmRunTimeException e) {}
	}

	public boolean addAll(Collection col) {
		Iterator it = col.iterator();
		while ( it.hasNext() ){
			add( tagUtils.convertToCfData( it.next() ) );
		}
		
		return true;
	}

	public boolean addAll(int index, Collection col) {
		Iterator it = col.iterator();
		while ( it.hasNext() ){
			add( index, tagUtils.convertToCfData( it.next() ) );
		}
		return true;
	}

	public void clear() {
		try {
			cfdata.removeAllElements();
		} catch (cfmRunTimeException e) {
		}
	}

	@Override
	public boolean contains(Object arg0) {
		return cfdata.contains(arg0);
	}

	public boolean containsAll(Collection arg0) {
		return cfdata.containsAll(arg0);
	}

	public int indexOf(Object arg0) {
		return cfdata.indexOf(arg0);
	}

	public boolean isEmpty() {
		return cfdata.isEmpty();
	}

	public Iterator iterator() {
		return cfdata.iterator();
	}

	public int lastIndexOf(Object arg0) {
		return cfdata.lastIndexOf(arg0);
	}

	public ListIterator listIterator() {
		return cfdata.listIterator();
	}

	public ListIterator listIterator(int arg0) {
		return cfdata.listIterator(arg0);
	}

	public boolean remove(Object arg0) {
		return cfdata.remove(arg0);
	}

	public Object remove(int index) {
		try {
			Object oldValue = cfdata.getData(index+1);
			cfdata.removeElementAt(index+1);
			return oldValue;
		} catch (cfmRunTimeException e) {}
		return null;
	}

	public boolean removeAll(Collection arg0) {
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection arg0) {
		throw new UnsupportedOperationException();
	}

	public Object set(int index, Object value) {
		try {
			cfdata.setData(index+1, tagUtils.convertToCfData( value ) );
		} catch (cfmRunTimeException e) {}
		return value;
	}

	public int size() {
		return cfdata.size();
	}

	public List subList(int arg0, int arg1) {
		return cfdata.subList(arg0,arg1);
	}

	public Object[] toArray() {
		return cfdata.toArray();
	}

	public Object[] toArray(Object[] arg0) {
		return cfdata.toArray(arg0);
	}
}