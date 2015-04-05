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

package com.naryx.tagfusion.cfm.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletContext;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.tag.tagUtils;

/**
 * This is a wrapper class that stores application scope variables directly in the underlying J2EE application scope. It's used for unnamed applications.
 */
public class cfJ2EEApplicationData extends cfApplicationData {
	private static final long serialVersionUID = 1;

	private static boolean applicationStart = true;
	private static String applicationCfcPath;
	private static String webroot;

	private ServletContext applicationScope;

	public cfJ2EEApplicationData(cfSession session) {
		this(session.CTX);
		webroot = com.nary.io.FileUtils.getRealPath(session.REQ, "/");
	}

	public cfJ2EEApplicationData(ServletContext context) {
		super(null, cfAPPLICATION.UNNAMED_APPNAME); // null == overrides hashdata
		this.applicationScope = context;
	}


	/**
	 * Don't ever expire a J2EE application
	 */
	public synchronized boolean expire() {
		return false;
	}

	/**
	 * Returns true the first time it's invoked, false thereafter.
	 */
	public synchronized boolean isApplicationStart() {
		if (applicationStart) {
			applicationStart = false;
			return true;
		}
		return applicationStart;
	}

	protected void setApplicationCfcPath(String _applicationCfcPath) {
		applicationCfcPath = _applicationCfcPath;
	}

	public String getApplicationCfcPath() {
		return applicationCfcPath;
	}

	public String getWebroot() {
		return webroot;
	}

	public boolean isCaseSensitive() {
		return true;
	}

	/**
	 * Convert "natural" Java objects in the J2EE application scope to CFML variables; try exact case key first, then normalized (lowercase).
	 */
	public cfData getData(String _key) {
		Object attr = applicationScope.getAttribute(_key);
		if (attr == null) {
			attr = applicationScope.getAttribute(_key.toLowerCase());
		}
		cfData data = tagUtils.convertToCfData(attr);
		return (data.getDataType() == cfData.CFNULLDATA ? null : data);
	}

	/**
	 * does a get without normalizing to lowercase so that attributes placed into the application scope by a JSP page with mixed-case names will appear in the CFDUMP
	 */
	protected cfData getForDump(String key) {
		cfData data = tagUtils.convertToCfData(applicationScope.getAttribute(key));
		return (data.getDataType() == cfData.CFNULLDATA ? null : data);
	}

	/**
	 * Store objects in the J2EE application scope as "natural" Java objects; normalize key to lowercase. Put empty string instead of null.
	 */
	public void setData(String _key, cfData _data) {
		Object obj = tagUtils.getNatural(_data);
		applicationScope.setAttribute(_key.toLowerCase(), obj != null ? obj : "");
		_data.invalidateLoopIndex(); // invalidate cfLoopIndex
	}

	// normalize key to lowercase
	public void deleteData(String _key) {
		applicationScope.removeAttribute(_key.toLowerCase());
	}

	// normalize key to lowercase
	public boolean containsKey(String _key) {
		return (applicationScope.getAttribute(_key.toLowerCase()) != null);
	}

	public boolean containsValue(cfData _data) {
		return containsValue(tagUtils.getNatural(_data));
	}

	// NOTE: this method can be hit by enabling J2EE sessions and calling StructClear( application ).
	public void clear() {
		Enumeration enumer = applicationScope.getAttributeNames();

		// With WebLogic Server, removing an attribute while enumerating over the attribute
		// names will result in a ConcurrentModificationException so we need to copy the attribute
		// names to a Vector and loop over it instead.
		Vector<String> attrNames = new Vector<String>();
		while (enumer.hasMoreElements()) {
			attrNames.addElement(enumer.nextElement().toString());
		}

		for (int i = 0; i < attrNames.size(); i++) {
			applicationScope.removeAttribute(attrNames.elementAt(i));
		}
	}

	public Object[] keys() {
		Enumeration enumer = applicationScope.getAttributeNames();
		List<Object> keys = new ArrayList<Object>();

		while (enumer.hasMoreElements())
			keys.add(enumer.nextElement());

		return keys.toArray();
	}

	public int size() {
		int total = 0;
		Enumeration enumer = applicationScope.getAttributeNames();
		while (enumer.hasMoreElements()) {
			enumer.nextElement();
			total++;
		}
		return total;
	}

	public boolean isEmpty() {
		return (size() == 0);
	}

	public Set<String> keySet() {
		Set<String> hs = new HashSet<String>();
		Enumeration enumer = applicationScope.getAttributeNames();
		while (enumer.hasMoreElements()) {
			hs.add(enumer.nextElement().toString());
		}
		return hs;
	}

	public boolean equals(Object o) {
		if (o instanceof cfJ2EEApplicationData)
			return applicationScope.equals(((cfJ2EEApplicationData) o).applicationScope);

		return false;
	}

	public int hashCode() {
		return applicationScope.hashCode();
	}

	// Returns a collection view of the values contained in this map.
	public Collection values() {
		throw new UnsupportedOperationException();
	}

	// Returns a set view of the mappings contained in this map.
	public Set entrySet() {
		throw new UnsupportedOperationException();
	}

	// create a shallow copy
	protected Map<String, cfData> cloneHashdata() {
		Map<String, cfData> cloneData = new FastMap<String, cfData>();
		Enumeration<String> enumer = applicationScope.getAttributeNames();
		while (enumer.hasMoreElements()) {
			String key = enumer.nextElement();
			cloneData.put(key, tagUtils.convertToCfData(applicationScope.getAttribute(key)));
		}
		return cloneData;
	}

	/*******************************************************************
	 * The following java.util.Map interface methods operate on "natural" Java objects, not CFML variables. Note that keys are case-sensitive when using these methods.
	 *******************************************************************/

	public boolean containsValue(Object obj) {
		Enumeration enumer = applicationScope.getAttributeNames();
		while (enumer.hasMoreElements()) {
			if (obj.equals((get(enumer.nextElement()))))
				return true;
		}
		return false;
	}

	public Object get(Object key) {
		return applicationScope.getAttribute(key.toString());
	}

	// Associates the specified value with the specified key in this map.
	public Object put(String key, Object value) {
		Object oldValue = get(key);
		applicationScope.setAttribute(key, value);
		return oldValue;
	}
}
