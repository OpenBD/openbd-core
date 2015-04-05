/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: cfINCLUDE.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmExitException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.file.cfmlFileCache;
import com.naryx.tagfusion.cfm.file.cfmlURI;
import com.naryx.tagfusion.servlet.jsp.CfmlPageContext;

public class cfINCLUDE extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

	private static boolean pageAttrDisabled = false;


  public java.util.Map getInfo(){
  	return createInfo("system", "Executes another template within the current template");
  }
  
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
 			createAttInfo("ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
 			createAttInfo("TEMPLATE", "The path to the template to execute", "", true ),
 			createAttInfo("PAGE", 		"If no TEMPLATE, then the PAGE can be used, which is the URL for include", "", false ),
 			createAttInfo("RUNONCE", 	"If this flag is set, then the template is only rendered once, even if included later on in the request, without this flag set", "", false ),
  	};
  }
	
	
	public static void disablePageAttr() {
		pageAttrDisabled = true;
	}

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseTagHeader(_tag);
		setFlushable(false);
		
		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;

		if (!containsAttribute("TEMPLATE") && (pageAttrDisabled || !containsAttribute("PAGE")))
			throw missingAttributeException("cfinclude.missingTemplatePage", null);
	}

	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if (!containsAttribute(attributes,"TEMPLATE") && (pageAttrDisabled || !containsAttribute(attributes,"PAGE")))
			throw missingAttributeException("cfinclude.missingTemplatePage", null);

		return	attributes;
	}

	
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);
		
		// If this is a PAGE attribute
		if (!pageAttrDisabled && containsAttribute(attributes,"PAGE")) {
			renderPage(attributes,_Session);
		} else {
			// Otherwise this is a normal CFINCLUDE
			boolean runOnce	= containsAttribute(attributes,"RUNONCE") ? getDynamic(attributes,_Session,"RUNONCE").getBoolean() : false;
			renderTemplate(this, _Session, getDynamic(attributes,_Session, "TEMPLATE").getString(), runOnce);
		}

		return cfTagReturnType.NORMAL;
	}

	/**
	 * This was refactored to be a static method so it can and potentially be
	 * invoked by a JSP taglib
	 */
	public static void renderTemplate(cfSession _Session, String template) throws cfmRunTimeException {
		renderTemplate(null, _Session, template, false );
	}

	private static void renderTemplate(cfTag tag, cfSession _Session, String template, boolean runOnce) throws cfmRunTimeException {
		cfFile svrFile = loadTemplate(tag, _Session, template);

		// Let us store we have this one
		if ( runOnce )
			_Session.setDataBin( String.valueOf(svrFile.hashCode()), true);
		else{
			Boolean runOnceChk	= (Boolean)_Session.getDataBin(String.valueOf(svrFile.hashCode()));
			if ( runOnceChk != null )
				return;
		}

		
		// the CFOUTPUT GROUP attribute is not inherited over CFINCLUDEs
		boolean groupedQuery = false;
		cfQueryResultData queryData = _Session.peekQuery();
		if ((queryData != null) && queryData.isGrouped()) {
			groupedQuery = true;
			queryData.setGroupedByEnabled(false);
		}

		_Session.pushActiveFile(svrFile);

		try {
			// if rendering CFINCLUDE within component function, "forget" about
			// the fact that we're within the component (see bug #67)
			_Session.pushComponentData(null, null);
			
			// Now render the file
			svrFile.render(_Session);
		} catch (cfmExitException ignored) {
			// a cfexit from an included file allows processing to continue unlike a cfabort
			if (tag != null)
				tag.restoreTagStack(_Session);
			
		} finally {
			_Session.popComponentData(); // restore component data
		}

		_Session.popActiveFile();

		if (groupedQuery) {
			queryData.setGroupedByEnabled(true);
		}
	}


	/**
	 * This version of loadTemplate() never throws cfmBadFileException (in case of
	 * file-not-found, for example), but throws cfmRunTimeException instead. Use
	 * this method when you want the user to see a CFML runtime error page in case
	 * of file-not-found or other cfmBadFileException.
	 */
	protected static cfFile loadTemplate(cfTag cftag, cfSession _Session, String _templatePath) throws cfmRunTimeException {
		try {
			return loadTemplate(_Session, _templatePath);
		} catch (cfmBadFileException bfe) {
			if (bfe.fileNotFound()) {
				cfCatchData catchData = catchDataFactory.missingFileException(cftag, _templatePath);
				catchData.setSession(_Session);
				throw new cfmRunTimeException(catchData);
			}

			cfCatchData catchData = catchDataFactory.summarizeBadFileException(cftag, "Badly formatted template: " + _templatePath, bfe);
			catchData.setSession(_Session);
			throw new cfmRunTimeException(catchData, bfe);
		}
	}
	

	/**
	 * This version of loadTemplate() throws cfmBadFileException (including
	 * file-not-found). Use this method when you intend to catch the
	 * cfmBadFileException and do some processing other than just report a CFML
	 * runtime error to the user.
	 */
	public static cfFile loadTemplate(cfSession _Session, String _templatePath) throws cfmBadFileException {
		String templatePath = _templatePath.replace('\\', '/');

		if (templatePath.charAt(0) == '/') {
			// look for mappings
			cfmlURI resolvedPath = getMappedCfmlURI(_Session, templatePath);
			if (resolvedPath != null)
				return _Session.getFile(resolvedPath).setURI(templatePath);
			else
				return _Session.getUriFile(templatePath);// if no mappings, look in the doc root
		}

		String presentFilePath = _Session.getPresentFilePath();
		if (presentFilePath != null)
			return _Session.getFile(new cfmlURI(presentFilePath, templatePath)).setURI(_Session.getPresentURIPath() + templatePath); // relative to the physical path of the calling template
		else
			return _Session.getUriFile(templatePath); // URI-relative
	}

	public static cfmlURI getMappedCfmlURI(cfSession _Session, String _path) {
		String path = getMappedPath(_Session, _path, true);
		return ( path != null ) ? new cfmlURI(path) : null;
	}

	public static String getMappedPath(cfSession _Session, String path, boolean _removeFile) {
		String mappedPath = null;
		
		// remove the file name from the path and convert to lowercase for case-insensitive comparisons
		String matchPath = _removeFile ? path.substring(0, path.lastIndexOf("/") + 1).toLowerCase() : path.toLowerCase();
		
		Map<String, String> mappings = new FastMap<String, String>(cfmlFileCache.getCFMappings()); // mappings configured in admin console
		
		if ( _Session != null )
			mappings.putAll(_Session.getCFMappings()); // mappings via CFMAPPING tag
		
		Iterator<String> iter = mappings.keySet().iterator();
		while (iter.hasNext()) {
			String nextPath = iter.next();
			if (matchPath.startsWith(nextPath.toLowerCase())) {
				
				// check the match is against a full section of the path (i.e. up to a '/' or '\')
				if (matchPath.length() == nextPath.length() 
						|| matchPath.charAt(nextPath.length()) == '/' 
						|| matchPath.charAt(nextPath.length()) == '\\') {
					// see if this match is better than a previous one
					if ((mappedPath == null) || (mappedPath.length() < nextPath.length())) {
						mappedPath = nextPath;
					}
				}
			}
		}

		if (mappedPath != null) {
			mappedPath = (String) mappings.get(mappedPath) + path.substring(mappedPath.length());
			return mappedPath;
		}

		return null;
	}

	// ------------------------------------------------------
	// Methods for the PAGE attribute processing
	// ------------------------------------------------------

	private void renderPage(cfStructData attributes, cfSession _Session) throws cfmRunTimeException {
		String page = getDynamic(attributes,_Session, "PAGE").getString();
		CfmlPageContext pageContext = new CfmlPageContext(_Session);
		pageContext.include(page);
	}
}
