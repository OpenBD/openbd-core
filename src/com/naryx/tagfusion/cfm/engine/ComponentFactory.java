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
 *  http://www.openbluedragon.org/
 *  $Id: ComponentFactory.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.engine;

import java.io.StringReader;
import java.util.List;

import com.nary.cache.cfmlURICache;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.file.cfmlURI;
import com.naryx.tagfusion.cfm.tag.cfCOMPONENT;
import com.naryx.tagfusion.cfm.tag.cfFUNCTION;
import com.naryx.tagfusion.cfm.tag.cfINCLUDE;
import com.naryx.tagfusion.cfm.tag.cfMODULE;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class ComponentFactory {
	// maps component names to cfmlURIs
	private static cfmlURICache nameCache = new cfmlURICache(10 * 60);

	public static final String GLOBAL_CFC_NAME = "WEB-INF.cftags.component";

	private static cfmlURI _globalComponentURI = null;

	public static void init(xmlCFML configFile) {
		// Determine the location of component.cfc
		String uri = configFile.getString("server.system.component-cfc");
		if ((uri == null) || (uri.trim().length() == 0)) {
			cfEngine.log("Unable to find component.cfc");
		} else {
			_globalComponentURI = new cfmlURI(uri).setComponentName(GLOBAL_CFC_NAME);
		}
	}

	
	// normalize component name to replace "/" with ".", and remove leading "/" or "."
	public static String normalizeComponentName(String componentName) {
		if ((componentName == null) || (componentName.length() == 0))
			throw new IllegalArgumentException("Component name cannot be null or empty string");
		
		componentName = componentName.replace('/', '.');
		if (componentName.charAt(0) == '.')
			componentName = componentName.substring(1);

		return componentName;
	}

	
	/**
	 * This method takes care of all caching related to component (.cfc) files.
	 * The basic concept of caching is that component names are mapped by this
	 * class to cfmlURIs, which in turn contain either the relative or physical
	 * path to the component file. The cfmlFileCache only understands cfmlURIs, so
	 * all interaction with the cfmlFileCache related to CFCs needs to be done
	 * through this class.
	 * 
	 * If a component is referenced via multiple names, there will be multiple
	 * entries in the name cache. However, these entries should map to the same
	 * cfmlURI value, so multiple names can be used to reference a component file
	 * in the cfmlFileCache.
	 * 
	 * Search for component files in this order:
	 * 
	 * 1. Local directory of the calling CFML page. 
	 * 2. Directories specified in the Mappings page of the admin console (essentially equivalent to CFINCLUDE lookup). 
	 * 3. Web root. 
	 * 4. Directories specified in the Custom Tag Paths of the admin console (essentially equivalent to CFMODULE NAME lookup).
	 */
	public static cfFile loadRawComponent(cfSession session, String componentName, List<String> importPaths ) throws cfmRunTimeException {
		if ((componentName == null) || (componentName.length() == 0)) {
			cfCatchData catchData = new cfCatchData();
			catchData.setMessage("Missing component name");
			throw new cfmRunTimeException(catchData);
		}

		componentName = normalizeComponentName(componentName);
		String rawComponentName = componentName;

		// check for the global component; that is, "WEB-INF.cftags.component"
		if (componentName.equals(GLOBAL_CFC_NAME)) {
			cfFile globalComponentFile = null;
			try {
				if (_globalComponentURI != null) {
					_globalComponentURI.getRealPath(session.REQ); // sets real path
					globalComponentFile = session.getFile(_globalComponentURI);
					if (emptyComponentFile(globalComponentFile)) {
						globalComponentFile = createGlobalComponentFile();
					} else {
						globalComponentFile.setComponentName(GLOBAL_CFC_NAME);
					}
				}
			} catch (cfmBadFileException e) {
				cfEngine.log("Unable to find component.cfc, configured path = " + _globalComponentURI.getURI() + ", real path = " + _globalComponentURI.getRealPath());
				_globalComponentURI = null; // don't take this path again
			}
			
			if (globalComponentFile != null){
				globalComponentFile.setRawComponentName(rawComponentName);
				return globalComponentFile;
			}else{
				return createGlobalComponentFile();
			}
			
		}

		String componentKey = componentName.toLowerCase();
		cfFile componentFile = null;
		cfmlURI componentURI = null;

		// check file based name cache first
		cfFile activeFile = session.activeFile();
		if ( activeFile != null ){
			componentURI = activeFile.getComponentPath( componentKey );
		}
		
		if ( componentURI == null ){
			// now check name cache
			componentURI = (cfmlURI) nameCache.getFromCache(componentKey);
		}
		
		if (componentURI != null) {
			try {
				componentFile = session.getFile(componentURI);
				if (componentFile.getComponentName() == null)
					componentFile.setComponentName(componentURI.getComponentName());
				
				if ( componentFile.getRawComponentName() == null )
					componentFile.setRawComponentName(rawComponentName);
				
				return componentFile;
			} catch (cfmBadFileException e) {
				nameCache.flushEntry(componentKey);
				if (!e.fileNotFound()) {
					cfCatchData catchData = catchDataFactory.summarizeBadFileException(session.activeTag(), "Badly formatted template", e);
					catchData.setSession(session);
					throw new cfmRunTimeException(catchData, e);
				}
			}
		}

		String componentUri = componentName.replace('.', '/') + ".cfc";

		// look in current directory
		if (componentName.indexOf(".") == -1) { // unqualified name
			componentFile = cfMODULE.getLocalCustomTagFile(session, componentUri);
		} else {
			try {
				// qualified name; these are case-sensitive on UNIX/Linux/MacOSX
				componentFile = cfINCLUDE.loadTemplate(session, componentUri);
			} catch (cfmBadFileException bfe) {
				handleBadFileException(bfe);
			}
		}

		if (componentFile != null) { // found file in current directory
			if (emptyComponentFile(componentFile)) {
				throw emptyComponentFileException(session, componentName);
			}
			componentName = getFullComponentName(session, componentName);
		} else {
			// search mapped directories (including web root)
			try {
				componentFile = cfINCLUDE.loadTemplate(session, "/" + componentUri);
			} catch (cfmBadFileException bfe) {
				handleBadFileException(bfe);
			}
		}

		boolean addToNameCache = false; // don't add to name cache if found in local or mapped directory, above
		cfmRunTimeException rte = null;
		
		// search custom tag directories
		if (componentFile == null) {
			try {
				// this method throws a cfmRunTimeException if the file is not found
				componentFile = cfMODULE.getCustomTagFile(session, componentName, "cfc", cfMODULE.getCustomTagDirectories(session, componentName + ".cfc"), true);
				addToNameCache = true;
			} catch (cfmRunTimeException e) {
				cfmBadFileException bfe = new cfmBadFileException(componentName);
				cfCatchData catchData = bfe.catchData;
				catchData.setType(cfCatchData.TYPE_APPLICATION);
				rte = new cfmRunTimeException(catchData);
			}
		}
		

		// if we haven't found the component yet and there are import paths defined
		if ( componentFile == null && importPaths != null ){
			componentFile = loadFromImportedPaths( session, componentName, importPaths );
			if ( componentFile != null ){ 
				activeFile.addComponentPath( componentKey, componentFile.getCfmlURI().copy() );
			}
		}
		
		if ( componentFile == null && rte != null ){
			throw rte;
		}
		
		if (emptyComponentFile(componentFile)) {
			componentFile	= ComponentScriptFactory.load( componentFile );
			if ( componentFile == null )
				throw emptyComponentFileException(session, componentName);
		}

		componentFile.setComponentName(componentName);
		componentFile.setRawComponentName(rawComponentName);
		
		if (addToNameCache)
			nameCache.setInCache(componentKey, componentFile.getCfmlURI().copy());

		return componentFile;
	}

	public static String getFullComponentName(cfSession session, String componentName) {
		String componentPath = null;

		cfTag activeTag = session.activeTag();
		if (activeTag instanceof cfFUNCTION)
			componentPath = ((cfFUNCTION) activeTag).getParentComponentPath();

		if (componentPath == null) {
			cfFUNCTION activeFunction = session.getActiveComponentTag();
			if (activeFunction != null)
				componentPath = activeFunction.getParentComponentPath();
		}
		
		if (componentPath == null) 
			componentPath = session.getActiveComponentPath();
		
		if ((componentPath != null) && (componentPath.length() > 0))
			componentName = componentPath + "." + componentName;
		else
			componentName = normalizeComponentName(session.getPresentURIPath() + componentName);
		
		return componentName;
	}


	private static cfFile createGlobalComponentFile() throws cfmBadFileException {
		cfFile f = new cfFile(new cfmlURI((String) null).setComponentName(GLOBAL_CFC_NAME), new StringReader("<cfcomponent></cfcomponent>"), "UTF-8");
		f.setRawComponentName(GLOBAL_CFC_NAME);
		return f;
	}

	public static boolean emptyComponentFile(cfFile componentFile) {
		return ((componentFile == null) 
				|| (componentFile.getFileBody().getTagList().length == 0) 
				|| !(componentFile.getFileBody().getTagList()[0] instanceof cfCOMPONENT));
	}

	private static cfmRunTimeException emptyComponentFileException(cfSession session, String componentName) {
		cfCatchData catchData = new cfCatchData(session);
		catchData.setType("Empty Source File");
		catchData.setDetail("Component Creation");
		catchData.setMessage("The component source file is empty or does not contain a CFCOMPONENT tag pair: " + componentName);
		return new cfmRunTimeException(catchData);
	}

	private static void handleBadFileException(cfmBadFileException _bfe) throws cfmRunTimeException {
		if (!_bfe.fileNotFound()) {
			cfCatchData catchData = _bfe.catchData;
			catchData.setType(cfCatchData.TYPE_APPLICATION);
			throw new cfmRunTimeException(catchData);
		}
	}
	
	private static cfFile loadFromImportedPaths( cfSession _session, String _componentName, List<String> _paths ) throws cfmRunTimeException{
		String componentKey = _componentName.toLowerCase();
		cfFile componentFile = null;

		String [] componentNames = new String[ _paths.size() ];
		String [] componentUri = new String[ _paths.size() ];
		
		//search off current directory
		for ( int i = 0; i < _paths.size(); i++ ){
			// on the first iteration, let initialize these arrays as we need them
			componentNames[i] = getNormalizedImportPath( _paths.get(i), componentKey );
			componentUri[i] = componentNames[i].replace('.', '/') + ".cfc";

			try {
				// qualified name; these are case-sensitive on UNIX/Linux/MacOSX
				componentFile = cfINCLUDE.loadTemplate( _session, componentUri[i] );
			} catch (cfmBadFileException bfe) {
				handleBadFileException(bfe);
			}
			
			if ( componentFile != null ){
				return componentFile;
			}
		}

		//search off custom tags dirs
		//INVARIANT: componentFile == null
		for ( int i = 0; i < _paths.size(); i++ ){
			try {
				// this method throws a cfmRunTimeException if the file is not found
				componentFile = cfMODULE.getCustomTagFile( _session, componentNames[i], "cfc", cfMODULE.getCustomTagDirectories( _session, componentNames[i] + ".cfc"), true );
			} catch (cfmRunTimeException e) {
				cfmBadFileException bfe = new cfmBadFileException( componentNames[i] );
				cfCatchData catchData = bfe.catchData;
				catchData.setType(cfCatchData.TYPE_APPLICATION);
				throw new cfmRunTimeException(catchData);
			}

			if ( componentFile != null ){
				return componentFile;
			}
		}

		// search mapped directories (including web root)
		//INVARIANT: componentFile == null
		for ( int i = 0; i < _paths.size(); i++ ){
			try {
				componentFile = cfINCLUDE.loadTemplate( _session, "/" + componentUri[i] );
			} catch (cfmBadFileException bfe) {
				handleBadFileException(bfe);
			}

			if ( componentFile != null )
				return componentFile;
		}
		
		// if we've reached here, then component wasn't found
		return null;
	}
	
	private static String getNormalizedImportPath(String _path, String _component) {
		if (_path.endsWith(".*")) {
			return _path.substring(0, _path.length() - 1) + _component;
		} else if (_path.toLowerCase().endsWith("." + _component.toLowerCase())) {
			return _path;
		} else {
			return null;
		}
	}
}
