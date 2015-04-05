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

package com.nary.util;

import java.util.Vector;

/**
 * This class implements simple sorting algorithms, for various classes etc.
 */
public class sort extends java.lang.Object {

	/**
	 * <P>
	 * Sorts an array of Strings into asending order
	 * <P>
	 * 
	 * @param _data
	 *          The array that is to be sorted.
	 * 
	 */
	public static void ascending(String _data[]) {

		for (int i = _data.length; --i >= 0;) {
			boolean swapped = false;
			for (int j = 0; j < i; j++) {
				if (_data[j].compareTo(_data[j + 1]) > 0) {
					String T = _data[j];
					_data[j] = _data[j + 1];
					_data[j + 1] = T;
					swapped = true;
				}
			}
			if (!swapped)
				return;
		}
	}

	/**
	 * Sorts a vector of Strings into asending order
	 * 
	 * @param _data
	 *          The vector that is to be sorted.
	 */
	public static void ascending(Vector _data) {

		for (int i = _data.size(); --i >= 0;) {
			boolean swapped = false;
			for (int j = 0; j < i; j++) {
				String f = (String) (_data.elementAt(j));
				String n = (String) (_data.elementAt(j + 1));
				if (f.compareTo(n) > 0) {
					String T = f;
					_data.setElementAt(n, j);
					_data.setElementAt(T, j + 1);
					swapped = true;
				}
			}
			if (!swapped)
				return;
		}

	}

}
