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

import java.util.Map;

import com.nary.util.FastMap;
import com.nary.util.SequencedHashMap;

/**
 * This cfStructData subclass is intended for use in storing actual arguments
 * for UDF function calls, and has the following additional characteristics:
 * 
 * 1. It supports numeric array indexing (in addition to associative array
 * indexing supported by cfStructData).
 * 
 * 2. It maintains the order that elements are inserted, to support numeric
 * array indexing.
 * 
 * Unlike cfStructData, the underlying SequencedHashMap is not synchronized.
 * This is OK, since a new cfArgStructData instance is created for every UDF
 * call.
 * 
 * We keep a note as to whether the primary calling function used named-based
 * parameter calling or index calling.  This is a flag for the named-based functions
 * to be able to pick up the right parameters
 */
public class cfArgStructData extends cfStructData implements java.io.Serializable {

	static final long serialVersionUID = 1;
	private boolean bModeNamedBased = false;

	public cfArgStructData() {
		super(new SequencedHashMap(FastMap.CASE_INSENSITIVE));
	}
	
	public cfArgStructData(boolean bNameSpaced) {
		super(new SequencedHashMap(FastMap.CASE_INSENSITIVE));
		bModeNamedBased = bNameSpaced;
	}

	// create a shallow copy
	protected Map cloneHashdata() {
		return new SequencedHashMap(getHashData(), FastMap.CASE_INSENSITIVE);
	}

	public boolean isCaseSensitive() {
		return false;
	}

	// this method is 1-based; the first element is at index 1
	public cfData getData(cfData arrayIndex) throws cfmRunTimeException {
		if (arrayIndex.getDataType() == cfData.CFNUMBERDATA) {
			return getData(arrayIndex.getInt() - 1);
		}

		// let cfStructData handle it as an associative array index
		return super.getData(arrayIndex);
	}

	// this method is 0-based; the first element is at index 0
	public cfData getData(int arrayIndex) {
		try {
			return (cfData) ((SequencedHashMap) getHashData()).getValue(arrayIndex);
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		} catch (ClassCastException e) { // should never happen
			com.nary.Debug.printStackTrace(e);
			return null;
		}
	}

	public String getKey(int arrayIndex) {
		try {
			return ((SequencedHashMap) getHashData()).get(arrayIndex).toString();
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	// this method is 0-based; the first element is at index 0
	public void setData(int arrayIndex, cfData _data) throws cfmRunTimeException {
		setData(new cfNumberData(arrayIndex), _data);
	}
	
	public void setNamedBasedMode(){
		bModeNamedBased = true;
	}
	
	public boolean isNamedBased(){
		return bModeNamedBased;
	}
}
