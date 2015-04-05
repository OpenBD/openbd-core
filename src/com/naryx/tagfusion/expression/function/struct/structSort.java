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

package com.naryx.tagfusion.expression.function.struct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.nary.util.stringtokenizer;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.expression.function.functionBase;

public class structSort extends functionBase {

	private static final long serialVersionUID = 1L;

	public structSort() {
		min = 1;
		max = 4;
		setNamedParams( new String[]{ "struct", "type", "direction", "keypath" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"struct1",
			"type - ('numeric', 'text' or 'textnocase')",
			"direction - ('desc' or 'asc')",
			"top level key path"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"structure", 
				"Returns an array of all the keys, ordered by the sort criteria of the values in the structure", 
				ReturnType.ARRAY  );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		boolean asc = true;
		boolean sortText = true;
		boolean casesensitive = true;
		String pathToSubelements = "";
		cfStructData baseStruct = null;

		// key path argument
		if (argStruct.containsKey("keypath")) {
			pathToSubelements = getNamedStringParam(argStruct, "keypath", "");
			if (pathToSubelements.length() > 0 && pathToSubelements.charAt(0) == '.') {
				pathToSubelements = pathToSubelements.substring(1);
			}			
		}
		// determine sort order
		String sortOrder = getNamedStringParam(argStruct, "direction", "asc").toLowerCase();
		if (sortOrder.equalsIgnoreCase("desc")) {
			asc = false;
		}
		// determine sort type
		String sortType = getNamedStringParam(argStruct, "type", "text").toLowerCase();
		if (sortType.equals("numeric")) {
			sortText = false;
		} else if (sortType.equals("textnocase")) {
			casesensitive = false;
		}
		cfData data = getNamedParam(argStruct, "struct");
		if (!data.isStruct()) {
			throwException(_session, "Invalid argument. The first argument provided was not a struct.");
		}
		baseStruct = (cfStructData) data;

		// lets get structsorting now that the arguments are settled

		// if there's a path to subelements specified then we need to divide it into
		// sub keys
		List<String> subelements = getPathToSubelements(pathToSubelements);

		// get the struct keys
		Object[] keys = baseStruct.keys();
		String nextKey;
		cfData rawValue = null;
		List<Comparable> theElements = new ArrayList<Comparable>(keys.length);

		// loop thru the keys, creating a list to be sorted
		for (int i = 0; i < keys.length; i++) {
			nextKey = (String) keys[i];
			rawValue = baseStruct.getData(nextKey);
			for (int j = 0; j < subelements.size(); j++) {
				rawValue = rawValue.getData(subelements.get(j));
			}

			try {

				if (sortText) {
					if (casesensitive) {
						theElements.add(new stringSortElement(nextKey, rawValue.getString(), asc));						
					} else {
						theElements.add(new stringSortElement(nextKey, rawValue.getString().toLowerCase(), asc));
					}
				} else {
					theElements.add(new numericSortElement(nextKey, rawValue.getDouble(), asc));
				}
			} catch (dataNotSupportedException d) {
				throwException(_session, "The specified value \"" + nextKey + "\" is not a simple type.");
			}

		}

		// get the value using the subelement path if necessary
		Collections.sort(theElements);
		cfArrayData returnArray = cfArrayData.createArray(1);
		Iterator<Comparable> keysIterator = theElements.iterator();
		while (keysIterator.hasNext()) {
			returnArray.addElement(new cfStringData(keysIterator.next().toString()));
		}

		return returnArray;
	}

	private static List<String> getPathToSubelements(String _path) {
		List<String> returnList = null;
		if (_path != "") {
			// divide
			stringtokenizer st = new stringtokenizer(_path, ".");
			returnList = new ArrayList<String>(4);
			while (st.hasMoreTokens()) {
				returnList.add(st.nextToken());
			}
		} else {
			returnList = new ArrayList<String>(0);
		}

		return returnList;
	}

	class stringSortElement implements Comparable<stringSortElement> {
		String value;
		String key;
		boolean asc;

		public stringSortElement(String _key, String _val, boolean _asc) {
			value = _val;
			key = _key;
			asc = _asc;
		}

		public int compareTo(stringSortElement o) {
			String otherVal = o.value;
			int result = value.compareTo(otherVal);
			if (asc) {
				return result;
			} else {
				return -1 * result;
			}
		}

		// this returns the key to this element
		public String toString() {
			return key;
		}
	}

	class numericSortElement implements Comparable<numericSortElement> {
		double value;
		String key;
		boolean asc;

		public numericSortElement(String _key, double _val, boolean _asc) {
			value = _val;
			key = _key;
			asc = _asc;
		}

		public int compareTo(numericSortElement o) {
			double otherVal = o.value;
			int result = new Double(value).compareTo(new Double(otherVal));
			if (asc) {
				return result;
			} else {
				return -1 * result;
			}
		}

		// this returns the key to this element
		public String toString() {
			return key;
		}

	}

}
