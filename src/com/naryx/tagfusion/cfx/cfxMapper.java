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

package com.naryx.tagfusion.cfx;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.engineListener;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class cfxMapper implements engineListener, Serializable {

	private static final long serialVersionUID = 1L;

	private class cfxTag {
		// common attributes to Java and C++ tags
		private String name;

		// only for Java tags
		private String tagClass;

		// only for C++ tags
		private String module;

		private String path;

		private String function;

		private boolean keepLoaded;

		// generic contructor
		private cfxTag(String name, String desc, String tagClass, String module, String function, boolean keepLoaded) {
			this.name = name;
			this.tagClass = tagClass;
			this.module = module;
			this.path = null;
			this.function = function;
			this.keepLoaded = keepLoaded;
		}

		// Java constructor
		private cfxTag(String name, String desc, String tagClass) {
			this(name, desc, tagClass, null, null, false);
		}

		// C++ constructor
		private cfxTag(String name, String desc, String module, String function, boolean keepLoaded) {
			this(name, desc, null, module, function, keepLoaded);
		}
	}

	private Hashtable<String, cfxTag> cfxTags; // contains cfxTag instances

	public cfxMapper(xmlCFML config) {
		cfxTags = new Hashtable<String, cfxTag>();
		cfEngine.registerEngineListener(this);
		engineAdminUpdate(config);
	}

	public void engineShutdown() {
	}

	public void engineAdminUpdate(xmlCFML config) {

		try {
			Vector elements = config.getKeys("server.javacustomtags.mapping[]");
			if (elements != null) {
				Enumeration E = elements.elements();
				while (E.hasMoreElements()) {
					String key = (String) E.nextElement();
					String name = key.substring(key.indexOf("[") + 1, key.lastIndexOf("]")).toLowerCase().trim();

					cfxTag tag = new cfxTag(config.getString(key + ".displayname"), config.getString(key + ".description"), config.getString(key + ".class"));

					cfxTags.put(name, tag);

					cfEngine.log("-] Java CFX Mapping created [" + tag.name + "] >> [" + tag.tagClass + "]");
				}
			}
		} catch (Exception E) {
			cfEngine.log("-] Java CFX Mapping problem occurred. Please check the XML file.");
		}

		Vector elements = config.getKeys("server.nativecustomtags.mapping[]");
		if (elements != null) {
			Enumeration E = elements.elements();
			while (E.hasMoreElements()) {
				String key = (String) E.nextElement();
				String name = key.substring(key.indexOf("[") + 1, key.lastIndexOf("]")).toLowerCase().trim();

				String dispName = config.getString(key + ".displayname");
				String module = config.getString(key + ".module");
				String function = config.getString(key + ".function");

				if (dispName != null && module != null && function != null) {
					cfxTag tag = new cfxTag(dispName, config.getString(key + ".description", ""), module, function, config.getBoolean(key + ".keeploaded", true));
					cfxTags.put(name, tag);

					cfEngine.log("-] C++ CFX Mapping created [" + tag.name + "] >> [" + tag.module + "]");
				} else {
					cfEngine.log("-] C++ CFX Mapping problem occurred. Attributes are missing for the " + key + " CFX. Check the XML file");
				}
			}
		}
	}

	public boolean isNativeCFX(String name) {
		String key = name.toLowerCase();
		if (cfxTags.containsKey(key))
			return (cfxTags.get(key).module != null);

		return false;
	}

	public String getJavaTagClass(String name) {
		String key = name.toLowerCase();
		if (cfxTags.containsKey(key))
			return cfxTags.get(key).tagClass;

		// if not configured, return tag name as class name
		return name.substring("cfx_".length());
	}

	public String getNativeTagModuleName(String name) {
		String key = name.toLowerCase();
		if (cfxTags.containsKey(key))
			return cfxTags.get(key).module;

		return null;
	}

	public String getNativeTagModulePath(String name) {
		String key = name.toLowerCase();
		if (cfxTags.containsKey(key))
			return cfxTags.get(key).path;

		return null;
	}

	public void setNativeTagModulePath(String name, String path) {
		String key = name.toLowerCase();
		if (cfxTags.containsKey(key))
			cfxTags.get(key).path = path;
	}

	public String getNativeTagFunction(String name) {
		String key = name.toLowerCase();
		if (cfxTags.containsKey(key))
			return cfxTags.get(key).function;

		return null;
	}

	public String getTagDisplayName(String name) {
		String key = name.toLowerCase();
		if (cfxTags.containsKey(key))
			return cfxTags.get(key).name;

		return null;
	}

	public boolean keepLoaded(String name) {
		String key = name.toLowerCase();
		if (cfxTags.containsKey(key))
			return cfxTags.get(key).keepLoaded;

		return false;
	}
}
