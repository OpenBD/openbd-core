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
 *  
 *  $Id: dirComparator.java 1758 2011-11-02 22:46:33Z alan $
 */

package com.naryx.tagfusion.expression.function.file;

import java.util.Comparator;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

public class dirComparator implements Comparator<Map<String, cfData>> {
	String sortColumn;
	boolean order;

	Comparator<Map<String, cfData>> subCompare;

	public dirComparator(String _sortColumn, boolean _order, Comparator<Map<String, cfData>> _subcomparator) {
		sortColumn = _sortColumn;
		order = _order;
		subCompare = _subcomparator;
	}

	public int compare(Map<String, cfData> o1, Map<String, cfData> o2) {
		try {
			if (sortColumn.length() == 0)
				return 0;

			cfData data1 = o1.get(sortColumn);
			cfData data2 = o2.get(sortColumn);

			if ((data1 == null) || (data2 == null))
				return 0;

			if (sortColumn.equals("size")) {
				double d1 = data1.getDouble();
				double d2 = data2.getDouble();

				if (d1 > d2) {
					return (order ? 1 : -1);
				} else if (d1 < d2) {
					return (order ? -1 : 1);
				} else {
					return (subCompare != null ? subCompare.compare(o1, o2) : 0);
				}
			} else if (sortColumn.equals("datelastmodified")) {
				long l1 = data1.getLong();
				long l2 = data2.getLong();

				if (l1 > l2) {
					return (order ? 1 : -1);
				} else if (l1 < l2) {
					return (order ? -1 : 1);
				} else {
					return (subCompare != null ? subCompare.compare(o1, o2) : 0);
				}

			} else {
				String s1 = data1.getString().toLowerCase();
				String s2 = data2.getString().toLowerCase();
				int compareResult = s1.compareTo(s2);

				if (compareResult == 0) {
					return (subCompare != null ? subCompare.compare(o1, o2) : 0);
				} else {
					return (order ? 1 : -1) * compareResult;
				}
			}

		} catch (dataNotSupportedException e) {
			throw new RuntimeException(e.toString());
		}
	}

}
