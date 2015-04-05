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
 */

package com.naryx.tagfusion.cfm.registry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.nary.util.FastMap;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.xmlConfig.xmlCFML;
import com.newatlanta.jni.registry.NoSuchValueException;
import com.newatlanta.jni.registry.RegDWordValue;
import com.newatlanta.jni.registry.RegStringValue;
import com.newatlanta.jni.registry.Registry;
import com.newatlanta.jni.registry.RegistryException;
import com.newatlanta.jni.registry.RegistryKey;
import com.newatlanta.jni.registry.RegistryValue;

public class cfREGISTRY extends cfTag implements Serializable {

	static final long serialVersionUID = 1;

	public static final int STRING_TYPE = 0;

	public static final int DWORD_TYPE = 1;

	public static final int KEY_TYPE = 2;

	public static final int ANY_TYPE = 3;

	/*
	 * init
	 */
	public static void init(xmlCFML configFile) {
		try {
			String nativeLibPath = cfEngine.getNativeLibDirectory();
			if (nativeLibPath != null) {
				nativeLibPath = nativeLibPath + "cfregistry.dll";
				System.load(com.nary.io.FileUtils.resolveNativeLibPath(nativeLibPath));
				cfEngine.log("-] Loaded cfregistry.dll >> [" + nativeLibPath + "]");
			} else {
				cfEngine.log("-] ERROR - failed to initialize CFREGISTRY because nativeLibPath is null.");
			}
		} catch (Throwable t) {
			cfEngine.log("-] ERROR - failed to initialize CFREGISTRY.");
		}
	}

	/*
	 * defaultParameters
	 */
	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute("TYPE", "STRING");
		defaultAttribute("VALUE", "");

		parseTagHeader(_tag);

		if (!containsAttribute("ACTION"))
			throw newBadFileException("Missing ACTION", "You need to provide a ACTION");
		if (!containsAttribute("BRANCH"))
			throw newBadFileException("Missing BRANCH", "You need to provide a BRANCH");
	}

	/*
	 * render
	 */
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		String ACTION = getDynamic(_Session, "ACTION").getString().toLowerCase();
		String BRANCH = getDynamic(_Session, "BRANCH").getString().toUpperCase();

		if (ACTION.equals("getall"))
			regGetAll(_Session, BRANCH);
		else if (ACTION.equals("get"))
			regGet(_Session, BRANCH);
		else if (ACTION.equals("set"))
			regSet(_Session, BRANCH);
		else if (ACTION.equals("delete"))
			regDelete(_Session, BRANCH);
		else
			throw newRunTimeException("Invalid ACTION attribute:" + ACTION);

		return cfTagReturnType.NORMAL;
	}

	// -------------------------------------------------------

	/*
	 * getType
	 */
	private int getType(cfSession _Session) throws cfmRunTimeException {
		String TYPE_STR = getDynamic(_Session, "TYPE").getString().toUpperCase();

		if (TYPE_STR.equals("STRING"))
			return STRING_TYPE;
		if (TYPE_STR.equals("DWORD"))
			return DWORD_TYPE;
		if (TYPE_STR.equals("KEY"))
			return KEY_TYPE;
		if (TYPE_STR.equals("ANY"))
			return ANY_TYPE;

		throw newRunTimeException("the specified type is not supported - " + TYPE_STR);
	}

	/*
	 * regGetAll
	 */
	private void regGetAll(cfSession _Session, String BRANCH) throws cfmRunTimeException {

		if (!containsAttribute("NAME"))
			throw newRunTimeException("Missing NAME attribute");

		String NAME = getDynamic(_Session, "NAME").getString();

		// If a SORT attribute was specified then get the sort comparator and check
		// the
		// SORT attribute for valid values
		regComparator sortComparator = null;
		if (containsAttribute("SORT"))
			sortComparator = getSortComparator(getDynamic(_Session, "SORT").getString());

		// Get the registry entries
		List<Map<String, cfData>> results = regGetAll(this, BRANCH, getType(_Session), NAME);
		if (results == null)
			return;

		// If a SORT attribute was specified then sort the results
		if (sortComparator != null)
			Collections.sort(results, sortComparator);

		cfQueryResultData qT = new cfQueryResultData(new String[] { "entry", "type", "value" }, "CFREGISTRY");
		qT.populateQuery(results);
		_Session.setData(NAME, qT);
	}

	/*
	 * regGet
	 */
	private void regGet(cfSession _Session, String BRANCH) throws cfmRunTimeException {
		if (!containsAttribute("ENTRY"))
			throw newRunTimeException("Missing ENTRY attribute");
		if (!containsAttribute("VARIABLE"))
			throw newRunTimeException("Missing VARIABLE attribute");

		int TYPE = getType(_Session);
		if (TYPE == ANY_TYPE)
			throw newRunTimeException("A type of any is not supported with action get.");

		cfData data = regGet(this, BRANCH, getDynamic(_Session, "ENTRY").getString(), TYPE);
		if (data == null)
			return;

		_Session.setData(getDynamic(_Session, "VARIABLE").getString(), data);
	}

	/*
	 * regSet
	 */
	private void regSet(cfSession _Session, String BRANCH) throws cfmRunTimeException {
		if (!containsAttribute("ENTRY"))
			throw newRunTimeException("Missing ENTRY attribute");

		// String ENTRY = getDynamic( _Session, "ENTRY" ).getString().toUpperCase();
		String VALUE = getDynamic(_Session, "VALUE").getString();
		int TYPE = getType(_Session);
		if (TYPE == ANY_TYPE)
			throw newRunTimeException("A type of any is not supported with action set.");

		regSet(this, BRANCH, getDynamic(_Session, "ENTRY").getString(), TYPE, VALUE);
	}

	/*
	 * regDelete
	 */
	private void regDelete(cfSession _Session, String BRANCH) throws cfmRunTimeException {

		if (containsAttribute("ENTRY"))
			regDelete(this, BRANCH, getDynamic(_Session, "ENTRY").getString());
		else
			regDelete(this, BRANCH, null);
	}

	/*
	 * getSortComparator
	 */
	private regComparator getSortComparator(String sortString) throws cfmRunTimeException {
		String sortColumns[] = new String[3];
		boolean bAscending[] = new boolean[3];
		List<String> tokens = string.split(sortString.toLowerCase(), ",");

		for (int i = 0; i < tokens.size(); i++) {
			String sortCol;
			String sortOrd;
			String subSort = tokens.get(i).toString().trim();

			// --[ Find out the order
			int c1 = subSort.indexOf(" ");
			if (c1 == -1) {
				sortCol = subSort;
				sortOrd = "asc";
			} else {
				sortCol = subSort.substring(0, c1).trim();
				sortOrd = subSort.substring(c1 + 1);
			}

			if (!sortCol.equals("entry") && !sortCol.equals("type") && !sortCol.equals("value"))
				throw newRunTimeException("The sort attribute can only be used for columns Entry, Type and Value.  The column '" + sortCol + "' is not valid.");

			boolean bAsc;
			if (sortOrd.equals("asc"))
				bAsc = true;
			else if (sortOrd.equals("desc"))
				bAsc = false;
			else
				throw newRunTimeException("The sort attribute can only sort a column as 'asc' or 'desc'.  The sort order '" + sortOrd + "' is not valid.");

			sortColumns[i] = sortCol;
			bAscending[i] = bAsc;
		}

		return new regComparator(sortColumns, bAscending);
	}

	/*
	 * regComparator
	 */
	class regComparator implements Comparator<Map<String, cfData>>, Serializable {
		private static final long serialVersionUID = 1L;

		String sortColumns[];

		boolean bAscending[];

		public regComparator(String[] _sortColumns, boolean[] _bAscending) {
			sortColumns = _sortColumns;
			bAscending = _bAscending;
		}

		public int compare(Map<String, cfData> o1, Map<String, cfData> o2) {
			return compare(o1, o2, 0);
		}

		public int compare(Map<String, cfData> o1, Map<String, cfData> o2, int index) {
			int result;
			try {
				String sortColumn = sortColumns[index];

				cfData data1 = o1.get(sortColumn);
				cfData data2 = o2.get(sortColumn);
				String TYPE1 = o1.get("type").toString();
				String TYPE2 = o2.get("type").toString();

				if (sortColumn.equals("value") && TYPE1.equalsIgnoreCase("DWORD") && TYPE2.equalsIgnoreCase("DWORD")) {
					// If they are equal then set result to 0
					if (data1.getDouble() == data2.getDouble())
						result = 0;
					else if (bAscending[index])
						result = (data1.getDouble() > data2.getDouble()) ? 1 : -1;
					else
						result = (data1.getDouble() < data2.getDouble()) ? 1 : -1;
				} else {
					if (bAscending[index])
						result = data1.getString().toLowerCase().compareTo(data2.getString().toLowerCase());
					else
						result = data2.getString().toLowerCase().compareTo(data1.getString().toLowerCase());
				}

				// If they are equal and there are more sort columns specified then try
				// sorting
				// using the next sort column
				if ((result == 0) && (index + 1 < sortColumns.length) && (sortColumns[index + 1] != null)) {
					result = compare(o1, o2, index + 1);
				}
			} catch (Exception E) {
				result = 0;
			}

			return result;
		}
	}

	private static List<Map<String, cfData>> regGetAll(cfTag tag, String BRANCH, int TYPE, String NAME) throws cfmRunTimeException {
		String topKeyName;
		String keyName;

		int pos = BRANCH.indexOf("\\");
		if (pos == -1) {
			// It's a top level key (ex. HKEY_LOCAL_MACHINE)
			topKeyName = BRANCH;
			keyName = null;
		} else {
			// It's a sub-level key (ex. HKEY_LOCAL_MACHINE\SOFTWARE)
			topKeyName = BRANCH.substring(0, pos);
			keyName = BRANCH.substring(pos + 1);
		}

		RegistryKey topKey = Registry.getTopLevelKey(topKeyName);
		if (topKey == null)
			throw new cfmRunTimeException(catchDataFactory.runtimeException(tag, "the value for BRANCH is invalid - " + BRANCH));

		List<Map<String, cfData>> results = new ArrayList<Map<String, cfData>>();
		RegistryKey key = null;
		try {
			if (keyName == null) {
				key = topKey;
			} else {
				// The openSubkey() method will return null if the key doesn't exist
				key = Registry.openSubkey(topKey, keyName, RegistryKey.ACCESS_READ);
				if (key == null)
					throw new cfmRunTimeException(catchDataFactory.runtimeException(tag, "the registry entry cannot be opened - " + BRANCH));
			}
			int numValues = key.getNumberValues();
			int numSubKeys = key.getNumberSubkeys();

			// Add the subkeys first
			if ((TYPE == cfREGISTRY.KEY_TYPE) || (TYPE == cfREGISTRY.ANY_TYPE)) {
				for (int i = 0; i < numSubKeys; i++) {
					String name = key.regEnumKey(i);
					Map<String, cfData> rowHT = new FastMap<String, cfData>();
					rowHT.put("entry", new cfStringData(name));
					rowHT.put("type", new cfStringData("KEY"));
					rowHT.put("value", new cfStringData(""));
					results.add(rowHT);
				}
			}

			// Add the values next
			if (TYPE != cfREGISTRY.KEY_TYPE) {
				for (int i = 0; i < numValues; i++) {
					String name = key.regEnumValue(i);
					try {
						RegistryValue regValue = key.getValue(name);
						if (((TYPE == cfREGISTRY.ANY_TYPE) || (TYPE == cfREGISTRY.STRING_TYPE)) && (regValue.getType() == RegistryValue.REG_SZ)) {
							Map<String, cfData> rowHT = new FastMap<String, cfData>();
							rowHT.put("entry", new cfStringData(name));
							rowHT.put("type", new cfStringData("STRING"));
							rowHT.put("value", new cfStringData(((RegStringValue) regValue).getData()));
							results.add(rowHT);
						} else if (((TYPE == cfREGISTRY.ANY_TYPE) || (TYPE == cfREGISTRY.DWORD_TYPE)) && (regValue.getType() == RegistryValue.REG_DWORD)) {
							Map<String, cfData> rowHT = new FastMap<String, cfData>();
							rowHT.put("entry", new cfStringData(name));
							rowHT.put("type", new cfStringData("DWORD"));
							rowHT.put("value", new cfNumberData(((RegDWordValue) regValue).getData()));
							results.add(rowHT);
						}
					} catch (RegistryException re) {
						String msg = re.getMessage();
						if (msg.indexOf("REG_QWORD is not supported") == -1)
							throw re;
					}
				}
			}
		} catch (RegistryException re) {
			throw new cfmRunTimeException(catchDataFactory.runtimeException(tag, "error retrieving registry information - " + re.getMessage()));
		} finally {
			// If we opened a key then make sure we close it
			closeKey(key);
		}

		return results;
	}

	private static cfData regGet(cfTag tag, String BRANCH, String ENTRY, int TYPE) throws cfmRunTimeException {
		String topKeyName;
		String keyName;

		int pos = BRANCH.indexOf("\\");
		if (pos == -1) {
			// It's a top level key (ex. HKEY_LOCAL_MACHINE)
			topKeyName = BRANCH;
			keyName = null;
		} else {
			// It's a sub-level key (ex. HKEY_LOCAL_MACHINE\SOFTWARE)
			topKeyName = BRANCH.substring(0, pos);
			keyName = BRANCH.substring(pos + 1);
		}

		RegistryKey topKey = Registry.getTopLevelKey(topKeyName);
		if (topKey == null)
			return null;

		// If we fail to get the key's default value then try to return it as a
		// value
		// to match the behaviour of CFMX 7.0.
		if (TYPE == cfREGISTRY.KEY_TYPE) {
			cfData defaultValue = getKeysDefaultValue(topKey, keyName, ENTRY);
			if (defaultValue != null)
				return defaultValue;
		}

		RegistryKey key = null;
		try {
			if (keyName == null) {
				key = topKey;
			} else {
				// The openSubkey() method will return null if the key doesn't exist
				key = Registry.openSubkey(topKey, keyName, RegistryKey.ACCESS_READ);
				if (key == null)
					return null;
			}

			// The getValue() method will throw a NoSuchValueException if the value
			// doesn't exist
			RegistryValue regValue = key.getValue(ENTRY);

			// CFMX 7.0 seems to ignore the TYPE attribute so we will too.
			if (regValue.getType() == RegistryValue.REG_SZ)
				return new cfStringData(((RegStringValue) regValue).getData());
			else
				return new cfNumberData(((RegDWordValue) regValue).getData());
		} catch (NoSuchValueException nsve) {
			return null;
		} catch (RegistryException re) {
			throw new cfmRunTimeException(catchDataFactory.runtimeException(tag, "error retrieving registry information - " + re.getMessage()));
		} finally {
			// If we opened a key then make sure we close it
			closeKey(key);
		}
	}

	private static cfData getKeysDefaultValue(RegistryKey topKey, String keyName, String ENTRY) {
		RegistryKey key = null;
		try {
			// The openSubkey() method will return null if the key doesn't exist
			key = Registry.openSubkey(topKey, keyName + "\\" + ENTRY, RegistryKey.ACCESS_READ);
			if (key == null)
				return null;

			return new cfStringData(key.getDefaultValue());
		} catch (RegistryException re) {
			return null;
		} finally {
			// If we opened a key then make sure we close it
			closeKey(key);
		}
	}

	public static void regSet(cfTag tag, String BRANCH, String ENTRY, int TYPE, String VALUE) throws cfmRunTimeException {
		String topKeyName;
		String keyName;

		int pos = BRANCH.indexOf("\\");
		if (pos == -1) {
			// It's a top level key (ex. HKEY_LOCAL_MACHINE)
			topKeyName = BRANCH;
			keyName = null;

			// NOTE: this has been tested and works with HKEY_CLASSES_ROOT,
			// HKEY_CURRENT_USER and HKEY_CURRENT_CONFIG.
			// NOTE: using REGEDIT to add a key directly to HKEY_LOCAL_MACHINE or
			// HKEY_USERS fails so this appears to
			// be a Windows limitation.
			if ((TYPE == cfREGISTRY.KEY_TYPE) && (BRANCH.equals("HKEY_LOCAL_MACHINE") || BRANCH.equals("HKEY_USERS")))
				throw new cfmRunTimeException(catchDataFactory.runtimeException(tag, "adding a key directly to HKEY_LOCAL_MACHINE or HKEY_USERS is not supported"));
		} else {
			// It's a sub-level key (ex. HKEY_LOCAL_MACHINE\SOFTWARE)
			topKeyName = BRANCH.substring(0, pos);
			keyName = BRANCH.substring(pos + 1);
		}

		RegistryKey topKey = Registry.getTopLevelKey(topKeyName);
		if (topKey == null)
			throw new cfmRunTimeException(catchDataFactory.runtimeException(tag, "the value for BRANCH is invalid - " + BRANCH));

		RegistryKey key = null;
		try {
			if (keyName == null) {
				key = topKey;
			} else {
				// The openSubkey() method will return null if the key doesn't exist
				key = Registry.openSubkey(topKey, keyName, RegistryKey.ACCESS_WRITE);
				if (key == null) {
					// If the key doesn't exist then create it. Fix for bug NA#2890.
					key = topKey.createSubKey(keyName, "", RegistryKey.ACCESS_WRITE);
				}
			}

			switch (TYPE) {
			case cfREGISTRY.KEY_TYPE:
				RegistryKey subkey = key.createSubKey(ENTRY, "", RegistryKey.ACCESS_WRITE);
				try {
					subkey.closeKey();
				} catch (RegistryException re) {
					cfEngine.log("-] ERROR - failed to close registry key: " + keyName + "\\" + ENTRY);
				}
				break;
			case cfREGISTRY.STRING_TYPE:
				RegStringValue strValue = new RegStringValue(key, ENTRY, VALUE);
				key.setValue(strValue);
				key.flushKey();
				break;
			case cfREGISTRY.DWORD_TYPE:
				int value;
				if (VALUE.equals(""))
					value = 0;
				else
					value = com.nary.util.string.convertToInteger(VALUE, 0);
				RegDWordValue dwordValue = new RegDWordValue(key, ENTRY, RegistryValue.REG_DWORD, value);
				key.setValue(dwordValue);
				key.flushKey();
				break;
			}
		} catch (RegistryException re) {
			throw new cfmRunTimeException(catchDataFactory.runtimeException(tag, "error setting registry entry - " + re.getMessage()));
		} finally {
			// If we opened a key then make sure we close it
			closeKey(key);
		}
	}

	private static void regDelete(cfTag tag, String BRANCH, String ENTRY) throws cfmRunTimeException {
		String topKeyName;
		String keyName;

		int pos = BRANCH.indexOf("\\");
		if (pos == -1) {
			// It's a top level key (ex. HKEY_LOCAL_MACHINE)
			topKeyName = BRANCH;
			keyName = null;
		} else {
			// It's a sub-level key (ex. HKEY_LOCAL_MACHINE\SOFTWARE)
			topKeyName = BRANCH.substring(0, pos);
			keyName = BRANCH.substring(pos + 1);
		}

		RegistryKey topKey = Registry.getTopLevelKey(topKeyName);
		if (topKey == null)
			throw new cfmRunTimeException(catchDataFactory.runtimeException(tag, "the value for BRANCH is invalid - " + BRANCH));

		RegistryKey key = null;
		try {
			if (ENTRY == null) {
				if (keyName == null)
					throw new cfmRunTimeException(catchDataFactory.runtimeException(tag, "cannot delete a top level key - " + BRANCH));

				// We're deleting a key
				deleteSubKeyTree(tag, BRANCH, topKey, keyName);
			} else {
				// We're deleting a value

				if (keyName == null) {
					key = topKey;
				} else {
					// The openSubkey() method will return null if the key doesn't exist
					key = Registry.openSubkey(topKey, keyName, RegistryKey.ACCESS_WRITE);
					if (key == null)
						throw new cfmRunTimeException(catchDataFactory.runtimeException(tag, "error deleting registry entry. The entry does not exist - " + BRANCH));
				}

				key.deleteValue(ENTRY);
			}
		} catch (RegistryException re) {
			throw new cfmRunTimeException(catchDataFactory.runtimeException(tag, "error deleting registry entry - " + re.getMessage()));
		} finally {
			// If we opened a key then make sure we close it
			closeKey(key);
		}
	}

	private static void deleteSubKeyTree(cfTag tag, String BRANCH, RegistryKey topKey, String keyName) throws cfmRunTimeException {
		RegistryKey subkey = null;
		try {
			// First open the subkey to see if it contains any keys
			// NOTE: The openSubkey() method will return null if the key doesn't exist
			subkey = Registry.openSubkey(topKey, keyName, RegistryKey.ACCESS_READ);
			if (subkey == null)
				throw new cfmRunTimeException(catchDataFactory.runtimeException(tag, "error deleting registry entry. The entry does not exist - " + BRANCH));

			// We need to delete all of the keys contained by the subkey before it can
			// be deleted
			// NOTE: since we are deleting keys we need to start with the last key to
			// avoid a no
			// more items exception.
			int numSubKeys = subkey.getNumberSubkeys();
			for (int i = numSubKeys - 1; i >= 0; i--) {
				String name = subkey.regEnumKey(i);
				deleteSubKeyTree(tag, BRANCH, topKey, keyName + "\\" + name);
			}

			// Now we can delete the subkey. Close it, set it to null so we don't try
			// to close it again in the finally block and delete it.
			closeKey(subkey);
			subkey = null;
			deleteSubKey(tag, BRANCH, topKey, keyName);
		} catch (RegistryException re) {
			throw new cfmRunTimeException(catchDataFactory.runtimeException(tag, "error deleting registry entry - " + re.getMessage()));
		} finally {
			// If we opened a subkey then make sure we close it
			closeKey(subkey);
		}
	}

	private static void deleteSubKey(cfTag tag, String BRANCH, RegistryKey topKey, String keyName) throws cfmRunTimeException {
		RegistryKey key = null;
		try {
			String subKeyName;
			int pos = keyName.lastIndexOf("\\");
			if (pos == -1) {
				key = topKey;
				subKeyName = keyName;
			} else {
				subKeyName = keyName.substring(pos + 1);
				keyName = keyName.substring(0, pos);

				// The openSubkey() method will return null if the key doesn't exist
				key = Registry.openSubkey(topKey, keyName, RegistryKey.ACCESS_WRITE);
				if (key == null)
					throw new cfmRunTimeException(catchDataFactory.runtimeException(tag, "error deleting registry entry. The entry does not exist - " + BRANCH));
			}
			key.deleteSubKey(subKeyName);
		} catch (RegistryException re) {
			throw new cfmRunTimeException(catchDataFactory.runtimeException(tag, "error deleting registry entry - " + re.getMessage()));
		} finally {
			// If we opened a key then make sure we close it
			closeKey(key);
		}
	}

	private static void closeKey(RegistryKey key) {
		if (key != null) {
			try {
				key.closeKey();
			} catch (RegistryException re) {
				cfEngine.log("-] ERROR - failed to close registry key: " + re.getMessage());
			}
		}
	}
}
