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

package com.naryx.tagfusion.cfm.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Used to hold name/value pairs that can be sorted by name. Used (primarily) by
 * the cfINOVKE classes.
 */
public class cfWSParameters {
	private String[] reservedWords;

	private cfWSParameters reserved;

	private List<String> names = null;

	private List<Object> values = null;

	private List<Boolean> omitted = null;

	/**
	 * Default constructor. Takes an array of reserved words (i.e. parameter names
	 * that must be kept separate so they can be returned in the reserved
	 * parameters OrderParameters instance).
	 * 
	 * @param reservedWords
	 *          parameter names to segregate
	 */
	public cfWSParameters(String[] reservedWords) {
		this();
		this.reserved = new cfWSParameters();
		this.reservedWords = reservedWords;
	}

	/**
	 * Internal constructor used to create the cfWSParameters instance that holds
	 * the reserved parameters.
	 * 
	 */
	private cfWSParameters() {
		this.names = new ArrayList<String>(4);
		this.values = new ArrayList<Object>(4);
		this.omitted = new ArrayList<Boolean>(4);
	}

	/**
	 * Adds a parameter name and value pair to this cfWSParameters collection. If
	 * filter is true, the parameter may be put into the reserved parameters
	 * cfWSParameters instance if the name is a reserved word.
	 * 
	 * @param name
	 *          name of the parameter
	 * @param val
	 *          value of the parameter
	 * @param filter
	 *          if true, the parameter may be added to the reserved parameters if
	 *          the name is a reserved word; if false, no filtering is done
	 * @param omit
	 *          if true, the parameter should be omitted from being sent
	 */
	public void add(String name, Object val, boolean filter, boolean omit) {
		// See if it's a reserved word
		if (filter && reservedWords != null && reserved != null) {
			for (int i = 0; i < this.reservedWords.length; i++) {
				if (this.reservedWords[i].equalsIgnoreCase(name)) {
					reserved.add(name, val, false, omit);
					return;
				}
			}
		}

		// OK, see about adding it then
		int ndx = -1;
		Iterator<String> itr = names.iterator();
		for (int i = 0; itr.hasNext(); i++) {
			if (itr.next().equalsIgnoreCase(name)) {
				ndx = i;
				break;
			}
		}

		if (ndx == -1) {
			names.add(name);
			values.add(val);
			omitted.add(new Boolean(omit));
		} else {
			values.set(ndx, val);
			omitted.set(ndx, new Boolean(omit));
		}
	}

	public String[] getNamesArray() {
		return names.toArray(new String[names.size()]);
	}

	public List<String> getNames() {
		return names;
	}

	public Object[] getValuesArray() {
		return values.toArray(new Object[values.size()]);
	}

	public List<Object> getValues() {
		return values;
	}

	public Boolean[] getOmittedArray() {
		return omitted.toArray(new Boolean[omitted.size()]);
	}

	public List<Boolean> getOmitted() {
		return omitted;
	}

	public cfWSParameters getReservedParameters() {
		return reserved;
	}

}
