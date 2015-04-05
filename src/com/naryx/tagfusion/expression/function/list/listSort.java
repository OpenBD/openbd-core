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

package com.naryx.tagfusion.expression.function.list;

/**
 * This class implements the listSort function
 */

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.expression.function.functionBase;

public class listSort extends functionBase {

	private static final long serialVersionUID = 1L;

	public listSort() {
		min = 2;
		max = 4;
		setNamedParams( new String[]{ "list","type","order","delimiter"} );
	}

	public String[] getParamInfo(){
		return new String[]{
			"list",
			"type - ('text', 'textnocase' or 'numeric')",
			"order - ('asc' or 'desc')",
			"delimiter - default comma (,)"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"list", 
				"Sorts the array using the criteria given returning the new sorted list", 
				ReturnType.STRING );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String list = getNamedStringParam( argStruct, "list" , "" );
		String delimiter = getNamedStringParam( argStruct, "delimiter" , "," );
		String type = getNamedStringParam( argStruct, "type" ,"TEXT" ).toUpperCase();
		String order = getNamedStringParam( argStruct, "order" , "ASC" ).toUpperCase();

		validateOrder(order);
		validateType(type);

		// if list is empty don't do anything with it
		if (list.length() == 0) {
			return new cfStringData(list);
		} else {
			return new cfStringData(sortList(list, type, order, delimiter));
		}

	}// execute()

	private static void validateOrder(String _order) throws cfmRunTimeException {
		if (_order.equals("DESC") || _order.equals("ASC")) {
			return;
		}

		throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.expressionError", "listsort.invalidOrder", null));
	}// validateOrder()

	private static void validateType(String _type) throws cfmRunTimeException {
		if (_type.equals("TEXT") || _type.equals("TEXTNOCASE") || _type.equals("NUMERIC")) {
			return;
		}

		throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.expressionError", "listsort.invalidType", null));
	}// validateType()

	public String sortList(String _listdata, String _type, String _order, String _delimiter) throws dataNotSupportedException {

		// create list from string
		List listElements = getListElements(_listdata, _type, _delimiter);

		// create a suitable comparator
		Comparator chosenComparator = null;
		if (_type.equals("TEXTNOCASE")) {
			chosenComparator = new caseInsensitiveStringComparator(_order.equals("ASC"));
		} else if (_type.equals("NUMERIC")) {
			chosenComparator = new numericComparator(_order.equals("ASC"));
		} else if (_order.equals("DESC")) {
			// if desc && text || numeric
			chosenComparator = Collections.reverseOrder();
		}

		// do the sort
		if (chosenComparator != null) {
			Collections.sort(listElements, chosenComparator);
		} else {
			// allow it to sort naturally. If the list elements are Doubles then the
			// list
			// will sort NUMERIC, ASC; TEXT, ASC if the elements are Strings.
			Collections.sort(listElements);
		}

		Iterator iter = listElements.iterator();
		String result = "";

		while (iter.hasNext()) {
			result += iter.next().toString() + _delimiter;
		}

		result = result.substring(0, result.lastIndexOf(_delimiter));
		return result;
	}

	/*
	 * Returns the given list as a Vector containing all the individual elements
	 * contained in that list, delimited by _delimiter
	 */

	private static List getListElements(String _list, String _type, String _delimiter) throws dataNotSupportedException {
		List list = string.split(_list, _delimiter);
		cfStringData elementAsString = null;
		
		if (_type.equals("NUMERIC")) {
			for (int i = 0; i < list.size(); i++) {
				String nextItem = (String) list.get(i);
				try {
					elementAsString = new cfStringData(nextItem);
					if (elementAsString.isNumberConvertible()) {
						list.set(i, elementAsString.getNumber());
					} else if (elementAsString.isDateConvertible()) {
						list.set(i, elementAsString.getDateData());
					} else {
						throw new dataNotSupportedException("Cannot convert to number (" + nextItem + ").");
					}
				} catch (Exception e) {
					throw new dataNotSupportedException("Cannot convert to number (" + nextItem + ").");
				}
			}
		}
		return list;
	}// getListElements

	public class caseInsensitiveStringComparator implements Comparator<String> {

		boolean isAsc;

		caseInsensitiveStringComparator(boolean _isAsc) {
			isAsc = _isAsc;
		}

		public int compare(String _obj1, String _obj2) {
			if (isAsc) {
				return _obj1.compareToIgnoreCase(_obj2);
			} else {
				return _obj2.compareToIgnoreCase(_obj1);
			}
		}// compare()

	}// caseInsensitiveStringComparator()

	class numericComparator implements Comparator<cfData> {
		
		boolean isAsc;

		numericComparator(boolean _isAsc) {
			isAsc = _isAsc;
		}
		
		public int compare(cfData _o1, cfData _o2) {
			try {
				if (isAsc) {
					return compareTo(_o1.getDouble(), _o2.getDouble());
				} else {
					return compareTo(_o2.getDouble(), _o1.getDouble());
				}
			} catch (dataNotSupportedException E) {
				return 0;
			}
		}// compare()
		
		private int compareTo(Double d1, Double d2) {
			if (d1 == d2) {
				return 0;
			} else if (d1 < d2) {
				return -1;
			} else {
				return 1;
			}
		}
	}// numericComparator

}// listSort
