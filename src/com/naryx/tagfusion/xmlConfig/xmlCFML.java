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

package com.naryx.tagfusion.xmlConfig;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.nary.security.encrypter;
import com.nary.util.Localization;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class xmlCFML {

	private static final String ENCRYPT_KEY = "c";

	private Hashtable<String, Object> data;

	public xmlCFML() {
		data = new Hashtable<String, Object>();
	}

	public xmlCFML(cfStructData admin) throws cfmRunTimeException {
		data = new Hashtable<String, Object>();
		Object[] keys = admin.keys();

		for (int i = 0; i < keys.length; i++) {
			String key = (String) keys[i];
			cfData DD = admin.getData(key);

			if (DD.getDataType() == cfData.CFSTRUCTDATA) {
				// Only add the structure if it isn't empty
				if (!((cfStructData) DD).isEmpty())
					data.put(key, new xmlCFML((cfStructData) DD));
			} else if (DD.getDataType() == cfData.CFSTRINGDATA) {
				data.put(key, DD.getString());
			} else if (DD.getDataType() == cfData.CFARRAYDATA) {
				cfArrayData AD = (cfArrayData) DD;
				for (int x = 0; x < AD.size(); x++) {
					DD = (cfStructData) AD.getData(new cfNumberData(x + 1));
					data.put(key + "[" + DD.getData("name").getString() + "]", new xmlCFML((cfStructData) DD));
				}
			}
		}
	}

	public void setData(String Key, String value) {
		int c1 = getDotIndex(Key);
		xmlCFML container;
		String subKey;
		if (c1 == -1) {
			container = this;
			subKey = Key;
		} else {
			container = getData(Key.substring(0, c1), false);
			subKey = Key.substring(c1 + 1);
		}

		// If this is a datasource password then we need to decrypt it
		if (Key.startsWith("server.cfquery.datasource") && subKey.equals("password"))
			container.set(subKey, encrypter.decryptDBpassword(value, ENCRYPT_KEY));
		else
			container.set(subKey, value);
	}

	public void removeData(String Key) {
		int c1 = getDotIndex(Key);
		xmlCFML container;
		String subKey;
		if (c1 == -1) {
			container = this;
			subKey = Key;
		} else {
			container = getData(Key.substring(0, c1), true);

			// If the key isn't present then just return
			if (container == null)
				return;

			subKey = Key.substring(c1 + 1);
		}

		container.remove(subKey);
	}

	/*
	 * getKeys
	 * 
	 * This method returns a vector of keys or null.
	 */
	public Vector<String> getKeys(String Key) {
		boolean exists = true;

		Vector<String> keys = new Vector<String>();

		int c1 = getDotIndex(Key);
		xmlCFML container;
		String subKey;
		if (c1 == -1) {
			container = this;
			subKey = Key;
		} else {
			container = getData(Key.substring(0, c1), true);
			if (container == null) {
				exists = false;
			}
			subKey = Key.substring(c1 + 1);
		}

		// If we found the key then add its entries to the keys vector
		if (exists) {
			String prefix = Key.substring(0, c1) + ".";
			subKey = subKey.substring(0, subKey.indexOf("["));

			Enumeration<String> E = container.data.keys();
			while (E.hasMoreElements()) {
				String t = E.nextElement();

				// If this element is an array then remove the index portion
				String subt;
				int pos = t.indexOf('[');
				if (pos == -1)
					subt = t;
				else
					subt = t.substring(0, pos);

				// If there's a match then add it to the keys vector
				if (subt.equals(subKey))
					keys.addElement(prefix + t);
			}
		}

		return (exists ? keys : null);
	}

	public String getString(String Key) {
		return getValue(Key);
	}

	public String getString(String Key, String _default) {
		String s = getString(Key);
		if (s != null) {
			return s;
		}
		return _default;
	}

	public long getLong(String Key, long _default) {
		String value = getValue(Key);
		if (value != null) {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException E) {
			}
		}
		return _default;
	}

	public int getInt(String Key, int _default) {
		String value = getValue(Key);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException E) {
			}
		}
		return _default;
	}

	public boolean getBoolean(String Key, boolean _default) {
		String value = getValue(Key);
		if (value != null) {
			return Boolean.valueOf(value).booleanValue();
		}
		return _default;
	}

	private void set(String Key, xmlCFML value) {
		data.put(Key.toLowerCase().trim(), value);
	}

	private void set(String Key, String value) {
		data.put(Key.toLowerCase().trim(), value.trim());
	}

	private void remove(String Key) {
		data.remove(Key.toLowerCase().trim());
	}

	// -------------------------------------

	/*
	 * dump
	 * 
	 * This method is for debugging.
	 */
	public void dump(int _tab) {
		try {
			Enumeration<String> keys = data.keys();
			while (keys.hasMoreElements()) {
				Object next = keys.nextElement();
				Object nextVal = data.get(next);

				if (nextVal instanceof xmlCFML) {
					cfEngine.log(padOut(_tab) + next);
					((xmlCFML) nextVal).dump(_tab + 2);
				} else {
					cfEngine.log(padOut(_tab) + next + " : " + nextVal);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private xmlCFML getData(String _key, boolean returnNull) {

		xmlCFML currentPtr = this, newOne;

		int dotIndex = getDotIndex(_key);
		if (dotIndex == -1) {
			newOne = (xmlCFML) data.get(_key);
			if (newOne == null) {
				if (returnNull) {
					return null;
				} else {
					newOne = new xmlCFML();
					currentPtr.set(_key, newOne);
				}
			}
			return newOne;
		}

		CustomStringTokenizer ST = new CustomStringTokenizer(_key, '.');

		while (ST.hasMoreTokens()) {
			String subkey = ST.nextToken();
			newOne = currentPtr.getData(subkey, returnNull);
			if (newOne == null) {
				if (returnNull) {
					return null;
				} else {
					newOne = new xmlCFML();
					currentPtr.set(subkey, newOne);
				}
			}

			currentPtr = newOne;
		}

		return currentPtr;
	}

	// -------------------------------------

	/*
	 * getValue
	 * 
	 * This method tries to retrieve the key value, returning null if not found.
	 */
	private String getValue(String Key) {
		String value = null;

		int c1 = getDotIndex(Key);
		xmlCFML container;
		String subKey;
		if (c1 == -1) {
			container = this;
			subKey = Key;
		} else {
			container = getData(Key.substring(0, c1), true);
			subKey = Key.substring(c1 + 1);
		}

		// If we found the key then get the value
		if (container != null)
			value = (String) container.data.get(subKey);

		return value;
	}

	// --------------------------------

	/**
	 * getCFMLData
	 * 
	 * Returns the data in the form of a cfStructData.
	 */
	public cfStructData getCFMLData() throws cfmRunTimeException {
		cfStructData topStruct = new cfStructData();

		Enumeration<String> E = data.keys();
		while (E.hasMoreElements()) {
			String key = E.nextElement();
			Object D = data.get(key);

			// If the data is a string then just add it to the structure
			if (D instanceof String) {
				topStruct.setData(key, new cfStringData((String) D));
			} else {
				// If the data isn't an array then add it to the structure as a
				// structure
				int c1 = key.indexOf("[");
				if (c1 == -1) {
					topStruct.setData(key, ((xmlCFML) D).getCFMLData());
				} else {
					// It's an array element so add it to the array
					String subKey = key.substring(0, c1);
					String arrayName = key.substring(c1 + 1, key.length() - 1);
					cfArrayData arrayData = (cfArrayData) topStruct.getData(subKey);
					if (arrayData == null) {
						arrayData = cfArrayData.createArray(1);
						topStruct.setData(subKey, arrayData);
					}

					cfStructData tmp = ((xmlCFML) D).getCFMLData();
					tmp.setData("name", new cfStringData(arrayName));
					arrayData.addElement(tmp);
				}
			}
		}
		return topStruct;
	}

	// --------------------------------

	public void printXML(String name, PrintWriter Out) {
		printXML(name, -2, Out);
	}

	public void printXML(String name, int tabSize, PrintWriter Out) {
		String keyN = name;

		if (name.length() > 0) {
			int c1 = name.indexOf("[");
			if (c1 == -1) {
				Out.println(padOut(tabSize) + "<" + name + ">");
				keyN = name;
			} else {
				keyN = name.substring(0, c1);
				Out.println(padOut(tabSize) + "<" + keyN + " name=\"" + xmlEscape(name.substring(c1 + 1, name.length() - 1)) + "\">");
			}
		}
		tabSize += 2;

		Enumeration<String> E = data.keys();
		String key;
		while (E.hasMoreElements()) {
			key = E.nextElement();
			Object D = data.get(key);
			if (D instanceof String) {
				// If this is a datasource password then we need to encrypt it
				if (keyN.equals("datasource") && key.equals("password"))
					Out.println(padOut(tabSize) + "<" + key + ">" + xmlEscape(encrypter.encryptDBpassword((String) D, ENCRYPT_KEY)) + "</" + key + ">");
				else
					Out.println(padOut(tabSize) + "<" + key + ">" + xmlEscape((String) D) + "</" + key + ">");
			} else {
				((xmlCFML) D).printXML(key, tabSize, Out);
			}
		}

		tabSize -= 2;

		if (keyN.length() > 0)
			Out.println(padOut(tabSize) + "</" + keyN + ">");
	}

	private static String padOut(int number) {
		String tmp = "";
		for (int x = 0; x < number; x++)
			tmp += " ";
		return tmp;
	}

	private String xmlEscape(String _value) {
		return com.nary.util.string.escapeHtml(_value);
	}

	// this returns the last index of '.' ignoring '.'s that are within [ ]
	private static int getDotIndex(String _str) {
		int rBindex = _str.lastIndexOf(']');
		int lBindex = _str.lastIndexOf('[');
		int dotIndex = _str.lastIndexOf('.');

		if (dotIndex == -1) {
			return -1;
		} else if (dotIndex > rBindex) {
			return dotIndex;
		} else {
			dotIndex = _str.lastIndexOf('.', lBindex);
			return dotIndex;
		}
	}

	public void writeTo(File _dest) throws IOException {
		writeTo("", _dest);
	}

	public void writeTo(String name, File _dest) throws IOException {
		OutputStream fOut = cfEngine.thisPlatform.getFileIO().getFileOutputStream(_dest);
		OutputStreamWriter outW = new OutputStreamWriter(fOut, Localization.convertCharSetToCharEncoding("UTF-8"));
		PrintWriter outStream = new PrintWriter(outW);
		outStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		printXML(name, outStream);
		outStream.flush();
		outStream.close();
	}

	// --------------------------------

	/**
	 * This is a string tokenizer with the difference that the occurrence of the
	 * delimiter between [ ]'s is ignored.
	 */

	private class CustomStringTokenizer {

		List<String> tokens;

		int current;

		int noTokens;

		CustomStringTokenizer(String _s, char _sep) {
			tokens = new ArrayList<String>();
			current = 0;

			int start = 0;
			boolean escape = false;
			char nextCh;
			for (int i = 0; i < _s.length(); i++) {
				nextCh = _s.charAt(i);
				if (nextCh == _sep && !escape) {
					tokens.add(_s.substring(start, i));
					start = i + 1;
				} else if (nextCh == '[' && !escape) {
					escape = true;
				} else if (nextCh == ']' && escape) {
					escape = false;
				}
			}

			if (start > 0) {
				tokens.add(_s.substring(start));
			}

			noTokens = tokens.size();
		}

		boolean hasMoreTokens() {
			return current < noTokens;
		}

		String nextToken() {
			return tokens.get(current++);
		}

	}

}
