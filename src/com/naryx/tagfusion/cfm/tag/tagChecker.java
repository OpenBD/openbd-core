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
 *  $Id: tagChecker.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.util.TagElement;
import com.naryx.tagfusion.util.xmlSAXParser;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

/**
 * This class handles all the registering and lookup of tags.
 */

public class tagChecker extends Object {
	private Map<String, TagElement> tagElements;

	public tagChecker() throws Exception {

		// Load in the main XML file
		tagElements = new HashMap<String, TagElement>();
		registerTags();

		// Load in the Custom Tags
		cfEngine.log("Core Tags loaded: " + tagElements.size() );
	}

	public int getTotalTags(){
		return tagElements.size();
	}
	
	public void loadCustomTags(InputStream customXmlFile) throws Exception {
		try {
			xmlSAXParser saxParser = new xmlSAXParser(customXmlFile);
			cfEngine.log("tagChecker CustomTags=" + mergeCustomTags(saxParser.getTags()) + " tags");
		} catch (Exception E) {
			cfEngine.log("tagChecker encountered an error with custom tags: " + customXmlFile);
		}
	}

	private int mergeCustomTags(Hashtable<String, TagElement> cfxHashtable) {
		Enumeration<String> E = cfxHashtable.keys();
		String key;
		while (E.hasMoreElements()) {
			key = E.nextElement();
			tagElements.put(key, cfxHashtable.get(key));
		}

		return cfxHashtable.size();
	}

	public List<String> getSupportedTags() {
		List<String> V = new ArrayList<String>( tagElements.size() );

		Iterator<TagElement> it	= tagElements.values().iterator();
		while ( it.hasNext() ) {
			TagElement TE = it.next();
			if (TE.getSupported())
				V.add(TE.getName());
		}
		return V;
	}

	public boolean isTagAvailable(String tagName) {
		return tagElements.containsKey(tagName);
	}

	public void removeTag(String tagName) {
		tagElements.remove(tagName);
	}

	public void setTagSupported(String tagName, String supported) {
		TagElement te = tagElements.get(tagName);
		if (te != null)
			te.setSupported(supported);
	}

	public boolean isTagSupported(String tagName) {
		TagElement tagE = tagElements.get(tagName);
		if (tagE == null)
			return false;
		else
			return tagE.getSupported();
	}

	public boolean hasEndTag(String tagName) {
		TagElement tagE = tagElements.get(tagName);
		if (tagE == null)
			return false;
		else
			return tagE.hasEndTag();
	}

	public String getClass(String tagName) {
		TagElement tagE = tagElements.get(tagName);
		if (tagE != null && tagE.getSupported())
			return tagE.getTagClass();
		else
			return null;
	}

	public String getErrorMessage(String tagName) {
		TagElement tagE = tagElements.get(tagName);
		if (tagE != null && tagE.getSupported())
			return tagE.getErrorMessage();
		else
			return null;
	}

	public void initialiseTags(xmlCFML configFile) {

		// Initialize the built-in tags by calling their static methods
		com.naryx.tagfusion.cfm.sql.cfQUERY.init(configFile);

		cfEngine.thisPlatform.initialiseTagSystem(configFile);
		
		com.naryx.tagfusion.cfm.application.cfAPPLICATION.init(configFile);
		com.naryx.tagfusion.cfm.tag.ext.cfCACHECONTENT.init(configFile);
		com.naryx.tagfusion.cfm.cfform.cfFORM.init(configFile);
		com.naryx.tagfusion.cfm.tag.cfLOG.init(configFile);
    com.naryx.tagfusion.cfm.tag.cfSCRIPT.init(configFile);
    
		// Initialize the plugins using reflection
		initialisePlugins(configFile);
	}

	public void initialisePlugins(xmlCFML configFile) {
		TagElement tagE;
		Class<?> C;
		Method methodList[];

		Object args[] = new Object[1];
		args[0] = configFile;

		Iterator<TagElement> it	= tagElements.values().iterator();
		while (it.hasNext()) {
			tagE = it.next();

			// If it's not supported or not a plugin then skip it
			if (!tagE.getSupported() || !tagE.isPlugin())
				continue;

			try {
				C = Class.forName(tagE.getTagClass());
				methodList = C.getDeclaredMethods();

				for (int x = 0; x < methodList.length; x++) {
					if (methodList[x].getName().equals("init")) {
						try {
							methodList[x].invoke(null, args);
						} catch (InvocationTargetException ite) {
							cfEngine.log(tagE.getTagClass());

							// If there's a wrapped exception then print it's stacktrace
							Throwable t = ite.getTargetException();
							if (t == null)
								com.nary.Debug.printStackTrace(ite);
							else
								com.nary.Debug.printStackTrace(t);
						} catch (Exception ignoreEE) {
							cfEngine.log(tagE.getTagClass());
						}
					}
				}
			} catch (Error EEE) {
			} catch (Exception EE) {
			}
		}
	}

	private void registerTags() {
		tagElements.put("CFQUERY", new TagElement("CFQUERY", true, "com.naryx.tagfusion.cfm.sql.cfQUERY", true));
		tagElements.put("CFQUERYPARAM", new TagElement("CFQUERYPARAM", true, "com.naryx.tagfusion.cfm.sql.cfQUERYPARAM"));

		tagElements.put("CFEXIT", new TagElement("CFEXIT", true, "com.naryx.tagfusion.cfm.tag.cfEXIT"));
		tagElements.put("CFPARAM", new TagElement("CFPARAM", true, "com.naryx.tagfusion.cfm.tag.cfPARAM"));
		tagElements.put("CFTABLE", new TagElement("CFTABLE", true, "com.naryx.tagfusion.cfm.tag.cfTABLE"));
		tagElements.put("CFSETTING", new TagElement("CFSETTING", true, "com.naryx.tagfusion.cfm.tag.cfSETTING"));
		tagElements.put("CFOBJECT", new TagElement("CFOBJECT", true, "com.naryx.tagfusion.cfm.tag.cfOBJECT"));
		tagElements.put("CFPAUSE", new TagElement("CFPAUSE", true, "com.naryx.tagfusion.cfm.tag.cfPAUSE"));

		tagElements.put("CFCATCH", new TagElement("CFCATCH", true, "com.naryx.tagfusion.cfm.tag.cfCATCH"));
		tagElements.put("CFBASE", new TagElement("CFBASE", true, "com.naryx.tagfusion.cfm.tag.cfBASE"));

		tagElements.put("CFTRANSACTION", new TagElement("CFTRANSACTION", true, "com.naryx.tagfusion.cfm.sql.cfTRANSACTION"));

		tagElements.put("CFTHROW", new TagElement("CFTHROW", true, "com.naryx.tagfusion.cfm.tag.cfTHROW"));
		tagElements.put("CFCASE", new TagElement("CFCASE", true, "com.naryx.tagfusion.cfm.tag.cfCASE"));

		tagElements.put("CFSELECT", new TagElement("CFSELECT", true, "com.naryx.tagfusion.cfm.cfform.cfSELECT"));
		tagElements.put("CFABORT", new TagElement("CFABORT", true, "com.naryx.tagfusion.cfm.tag.cfABORT"));
		tagElements.put("CFFLUSH", new TagElement("CFFLUSH", true, "com.naryx.tagfusion.cfm.tag.cfFLUSH"));

		tagElements.put("CFAPPLICATION", new TagElement("CFAPPLICATION", true, "com.naryx.tagfusion.cfm.application.cfAPPLICATION"));
		tagElements.put("CFERROR", new TagElement("CFERROR", true, "com.naryx.tagfusion.cfm.tag.cfERROR"));
		tagElements.put("CFMODULE", new TagElement("CFMODULE", true, "com.naryx.tagfusion.cfm.tag.cfMODULE"));

		tagElements.put("CFHTMLHEAD", new TagElement("CFHTMLHEAD", true, "com.naryx.tagfusion.cfm.tag.cfHTMLHEAD"));
		tagElements.put("CFHTMLBODY", new TagElement("CFHTMLBODY", true, "com.naryx.tagfusion.cfm.tag.cfHTMLBODY"));
		tagElements.put("CFIF", new TagElement("CFIF", true, "com.naryx.tagfusion.cfm.tag.cfIF"));
		

		tagElements.put("CFSWITCH", new TagElement("CFSWITCH", true, "com.naryx.tagfusion.cfm.tag.cfSWITCH"));
		tagElements.put("CFRETHROW", new TagElement("CFRETHROW", true, "com.naryx.tagfusion.cfm.tag.cfRETHROW"));

		tagElements.put("CFSTOREDPROC", new TagElement("CFSTOREDPROC", true, "com.naryx.tagfusion.cfm.sql.cfSTOREDPROC"));
		tagElements.put("CFLOCATION", new TagElement("CFLOCATION", true, "com.naryx.tagfusion.cfm.tag.cfLOCATION"));
		
		tagElements.put("CFDUMP", new TagElement("CFDUMP", true, "com.naryx.tagfusion.cfm.tag.cfDUMP"));
		tagElements.put("CFSCRIPT", new TagElement("CFSCRIPT", true, "com.naryx.tagfusion.cfm.tag.cfSCRIPT"));
		tagElements.put("CFMP3", new TagElement("CFMP3", true, "com.naryx.tagfusion.cfm.tag.cfMP3"));
		tagElements.put("CFOUTPUT", new TagElement("CFOUTPUT", true, "com.naryx.tagfusion.cfm.tag.cfOUTPUT"));
		tagElements.put("CFINPUT", new TagElement("CFINPUT", true, "com.naryx.tagfusion.cfm.cfform.cfINPUT"));

		tagElements.put("CFINCLUDE", new TagElement("CFINCLUDE", true, "com.naryx.tagfusion.cfm.tag.cfINCLUDE"));
		tagElements.put("CFLOCK", new TagElement("CFLOCK", true, "com.naryx.tagfusion.cfm.tag.cfLOCK"));
		tagElements.put("CFBREAK", new TagElement("CFBREAK", true, "com.naryx.tagfusion.cfm.tag.cfBREAK"));
		tagElements.put("CFCONTINUE", new TagElement("CFCONTINUE", true, "com.naryx.tagfusion.cfm.tag.cfCONTINUE"));
		tagElements.put("CFLOOP", new TagElement("CFLOOP", true, "com.naryx.tagfusion.cfm.tag.cfLOOP"));

		tagElements.put("CFLOG", new TagElement("CFLOG", true, "com.naryx.tagfusion.cfm.tag.cfLOG"));
		tagElements.put("CFCONTENT", new TagElement("CFCONTENT", true, "com.naryx.tagfusion.cfm.tag.cfCONTENT"));
		tagElements.put("CFSAVECONTENT", new TagElement("CFSAVECONTENT", true, "com.naryx.tagfusion.cfm.tag.cfSAVECONTENT"));
		tagElements.put("CFPROCPARAM", new TagElement("CFPROCPARAM", true, "com.naryx.tagfusion.cfm.sql.cfPROCPARAM"));
		tagElements.put("CFDUMPSESSION", new TagElement("CFDUMPSESSION", true, "com.naryx.tagfusion.cfm.tag.cfDUMP"));
		tagElements.put("CFSILENT", new TagElement("CFSILENT", true, "com.naryx.tagfusion.cfm.tag.cfSILENT"));
		tagElements.put("CFUPDATE", new TagElement("CFUPDATE", true, "com.naryx.tagfusion.cfm.sql.cfUPDATE"));
		tagElements.put("CFINSERT", new TagElement("CFINSERT", true, "com.naryx.tagfusion.cfm.sql.cfINSERT"));
		tagElements.put("CFOBJECTCACHE", new TagElement("CFOBJECTCACHE", true, "com.naryx.tagfusion.cfm.sql.cfOBJECTCACHE"));

		tagElements.put("CFDEFAULTCASE", new TagElement("CFDEFAULTCASE", true, "com.naryx.tagfusion.cfm.tag.cfDEFAULTCASE"));
		tagElements.put("CFPROCESSINGDIRECTIVE", new TagElement("CFPROCESSINGDIRECTIVE", true, "com.naryx.tagfusion.cfm.tag.cfPROCESSINGDIRECTIVE"));
		tagElements.put("CFTRY", new TagElement("CFTRY", true, "com.naryx.tagfusion.cfm.tag.cfTRY"));
		tagElements.put("CFPROCRESULT", new TagElement("CFPROCRESULT", true, "com.naryx.tagfusion.cfm.sql.cfPROCRESULT"));

		tagElements.put("CFCOOKIE", new TagElement("CFCOOKIE", true, "com.naryx.tagfusion.cfm.cookie.cfCOOKIE"));
		tagElements.put("CFADMIN", new TagElement("CFADMIN", true, "com.naryx.tagfusion.cfm.tag.cfADMIN"));

		tagElements.put("CFCOL", new TagElement("CFCOL", true, "com.naryx.tagfusion.cfm.tag.cfCOL"));

		tagElements.put("CFELSE", new TagElement("CFELSE", true, "com.naryx.tagfusion.cfm.tag.cfELSE"));
		tagElements.put("CFCACHE", new TagElement("CFCACHE", true, "com.naryx.tagfusion.cfm.tag.cfCACHE"));
		tagElements.put("CFHEADER", new TagElement("CFHEADER", true, "com.naryx.tagfusion.cfm.tag.cfHEADER"));
		tagElements.put("CFSET", new TagElement("CFSET", true, "com.naryx.tagfusion.cfm.tag.cfSET"));
		tagElements.put("CFFORM", new TagElement("CFFORM", true, "com.naryx.tagfusion.cfm.cfform.cfFORM"));

		tagElements.put("CFWDDX", new TagElement("CFWDDX", true, "com.naryx.tagfusion.cfm.wddx.cfWDDX"));

		tagElements.put("CFTEXTINPUT", new TagElement("CFTEXTINPUT", true, "com.naryx.tagfusion.cfm.cfform.cfTEXTINPUT"));
		tagElements.put("CFELSEIF", new TagElement("CFELSEIF", true, "com.naryx.tagfusion.cfm.tag.cfELSEIF"));

		tagElements.put("CFFORWARD", new TagElement("CFFORWARD", true, "com.naryx.tagfusion.cfm.tag.cfFORWARD"));
		tagElements.put("CFASSOCIATE", new TagElement("CFASSOCIATE", true, "com.naryx.tagfusion.cfm.tag.cfASSOCIATE"));
		tagElements.put("CFASSERT", new TagElement("CFASSERT", true, "com.naryx.tagfusion.cfm.tag.cfASSERT"));
		tagElements.put("CFIMPORT", new TagElement("CFIMPORT", true, "com.naryx.tagfusion.cfm.tag.cfIMPORT"));

		tagElements.put("CFXML", new TagElement("CFXML", true, "com.naryx.tagfusion.cfm.xml.cfXML"));

		tagElements.put("CFFUNCTION", new TagElement("CFFUNCTION", true, "com.naryx.tagfusion.cfm.tag.cfFUNCTION"));
		tagElements.put("CFARGUMENT", new TagElement("CFARGUMENT", true, "com.naryx.tagfusion.cfm.tag.cfARGUMENT"));
		tagElements.put("CFRETURN", new TagElement("CFRETURN", true, "com.naryx.tagfusion.cfm.tag.cfRETURN"));
		tagElements.put("CFPROPERTY", new TagElement("CFPROPERTY", true, "com.naryx.tagfusion.cfm.tag.cfPROPERTY"));
		tagElements.put("CFCOMPONENT", new TagElement("CFCOMPONENT", true, "com.naryx.tagfusion.cfm.tag.cfCOMPONENT"));
		tagElements.put("CFINTERFACE", new TagElement("CFINTERFACE", true, "com.naryx.tagfusion.cfm.tag.cfINTERFACE"));
		tagElements.put("CFINVOKE", new TagElement("CFINVOKE", true, "com.naryx.tagfusion.cfm.tag.cfINVOKE"));
		tagElements.put("CFINVOKEARGUMENT", new TagElement("CFINVOKEARGUMENT", true, "com.naryx.tagfusion.cfm.tag.cfINVOKEARGUMENT"));

		tagElements.put("CFTRACE", new TagElement("CFTRACE", true, "com.naryx.tagfusion.cfm.tag.cfTRACE"));
		tagElements.put("CFLOGIN", new TagElement("CFLOGIN", true, "com.naryx.tagfusion.cfm.tag.cfLOGIN"));
		tagElements.put("CFLOGINUSER", new TagElement("CFLOGINUSER", true, "com.naryx.tagfusion.cfm.tag.cfLOGINUSER"));
		tagElements.put("CFLOGOUT", new TagElement("CFLOGOUT", true, "com.naryx.tagfusion.cfm.tag.cfLOGOUT"));


		tagElements.put("CFTIMER", new TagElement("CFTIMER", true, "com.naryx.tagfusion.cfm.tag.cfTIMER"));

		// Unsupported CF5 Tags
		tagElements.put("CFIMPERSONATE", 	new TagElement("CFIMPERSONATE", false, ""));
		tagElements.put("CFGRAPH", 				new TagElement("CFGRAPH", false, ""));
		tagElements.put("CFGRAPHDATA", 		new TagElement("CFGRAPHDATA", false, ""));
		tagElements.put("CFGRID", 				new TagElement("CFGRID", false, ""));
		tagElements.put("CFGRIDROW", 			new TagElement("CFGRIDROW", false, ""));
		tagElements.put("CFREPORT", 			new TagElement("CFREPORT", false, ""));
		tagElements.put("CFGRIDUPDATE", 	new TagElement("CFGRIDUPDATE", false, ""));
		tagElements.put("CFGRIDCOLUMN", 	new TagElement("CFGRIDCOLUMN", false, ""));
		tagElements.put("CFAUTHENTICATE", new TagElement("CFAUTHENTICATE", false, ""));
		tagElements.put("CFAPPLET", 			new TagElement("CFAPPLET", false, ""));

		// BlueDragon Only tags
		tagElements.put("CFCACHECONTENT", new TagElement("CFCACHECONTENT", true, "com.naryx.tagfusion.cfm.tag.ext.cfCACHECONTENT"));
		tagElements.put("CFTHROTTLE", 	new TagElement("CFTHROTTLE", true, "com.naryx.tagfusion.cfm.tag.ext.cfTHROTTLE"));
		tagElements.put("CFMAPPING", 		new TagElement("CFMAPPING", true, "com.naryx.tagfusion.cfm.tag.ext.cfMAPPING"));
		tagElements.put("CFTHREAD", 		new TagElement("CFTHREAD", true, "com.naryx.tagfusion.cfm.tag.ext.thread.cfTHREAD"));
		tagElements.put("CFJOIN", 			new TagElement("CFJOIN", true, "com.naryx.tagfusion.cfm.tag.ext.thread.cfJOIN"));
		tagElements.put("CFINTERRUPT", 	new TagElement("CFINTERRUPT", true, "com.naryx.tagfusion.cfm.tag.ext.thread.cfINTERRUPT"));
		tagElements.put("CFZIP", 				new TagElement("CFZIP", true, "com.naryx.tagfusion.cfm.tag.io.cfZIP"));
		tagElements.put("CFZIPPARAM", 	new TagElement("CFZIPPARAM", true, "com.naryx.tagfusion.cfm.tag.io.cfZIPPARAM"));
		tagElements.put("CFDEBUGGER", 	new TagElement("CFDEBUGGER", true, "com.naryx.tagfusion.cfm.tag.cfDEBUGGER"));
		
		tagElements.put("CFFEED", 			new TagElement("CFFEED", true, "com.naryx.tagfusion.cfm.tag.cffeed.cfFEED"));
		
		tagElements.put("CFVIDEOPLAYER",	new TagElement("CFVIDEOPLAYER", true, "com.naryx.tagfusion.cfm.tag.ext.video.cfVIDEOPLAYER"));
		
		tagElements.put("CFJAVASCRIPT",	new TagElement("CFJAVASCRIPT", true, "com.naryx.tagfusion.cfm.tag.ext.cfJAVASCRIPT"));
		tagElements.put("CFSTYLESHEET",	new TagElement("CFSTYLESHEET", true, "com.naryx.tagfusion.cfm.tag.ext.cfSTYLESHEET"));
		
		tagElements.put("CFAJAXPROXY", 	new TagElement("CFAJAXPROXY", true, "com.naryx.tagfusion.cfm.tag.cfajaxproxy.cfAJAXPROXY"));
		tagElements.put("CFFINALLY", 		new TagElement("CFFINALLY", true, "com.naryx.tagfusion.cfm.tag.cfFINALLY"));
		

		// io
		tagElements.put("CFDIRECTORY", 	new TagElement("CFDIRECTORY", true, "com.naryx.tagfusion.cfm.tag.io.cfDIRECTORY"));
		tagElements.put("CFFILE", 			new TagElement("CFFILE", true, "com.naryx.tagfusion.cfm.tag.io.cfFILETAG"));
		
		tagElements.put("CFHTTP", 			new TagElement("CFHTTP", true, "com.naryx.tagfusion.cfm.http.cfHTTP") );
		tagElements.put("CFHTTPPARAM", 	new TagElement("CFHTTPPARAM", true, "com.naryx.tagfusion.cfm.http.cfHTTPPARAM") );

		
		cfEngine.thisPlatform.registerTags(tagElements);
	}

	public void addTag(String tagName, String tagClass) {
		tagElements.put(tagName.toUpperCase(), new TagElement(tagName.toUpperCase(), true, tagClass, false, true));
	}
	
	public void replaceTag(String tagName, String tagClass) {
		tagElements.put(tagName.toUpperCase(), new TagElement(tagName.toUpperCase(), true, tagClass));
	}
}
