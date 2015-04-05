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
 *  $Id: cfmlFileCache.java 2220 2012-07-30 01:11:52Z alan $
 */

package com.naryx.tagfusion.cfm.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.nary.cache.FileCache;
import com.nary.io.FileUtils;
import com.nary.util.FastMap;
import com.nary.util.stringtokenizer;
import com.naryx.tagfusion.cfm.engine.ComponentFactory;
import com.naryx.tagfusion.cfm.engine.ComponentScriptFactory;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.engineListener;
import com.naryx.tagfusion.cfm.file.mapping.OpenBDArchive;
import com.naryx.tagfusion.cfm.tag.cfINCLUDE;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

/**
 * This class handles the caching and creation of the actual class files that handle the CFML page.
 */
public class cfmlFileCache extends Object implements engineListener {

	public static final String DEFAULT_TRUST_CACHE = "false";
	public static final String DEFAULT_MAX_FILES = "1000";
	private static cfmlFileCache thisObject;
	
	private int maxFiles = 1000;
	private static boolean bTrustCache = false;

	private FileCache loadedFiles; // the actual file cache
	private Map<String, List<String>> customTags; // custom tag paths
	private Map<String, String> cfMappings; // mapped paths


	public static void init(ServletContext context, xmlCFML _iniFile) {
		// This routine is called from the cfEngine class at startup
		thisObject = new cfmlFileCache(context, _iniFile);
	}

	public static boolean isCustomTag(String _tag) {
		return thisObject._isCustomTag(_tag);
	}

	// removes non-"cf" custom tag mappings
	public static void removeAllNonCFMappings() {
		thisObject._removeAllNonCFMappings();
	}

	public static Map<String, String> getCFMappings() {
		return thisObject.cfMappings;
	}

	public static List<String> getCustomDirMapping(String tagname) {
		return thisObject._getCustomDirMapping(tagname);
	}

	public static cfFile getCfmlFile(ServletContext context, cfmlURI uri, HttpServletRequest REQ) throws cfmBadFileException {
		return thisObject._getCfmlFile(context, uri, REQ);
	}

	public static void removeCfmlFile(cfmlURI uri, HttpServletRequest REQ) {
		thisObject._removeCfmlFile(uri, REQ);
	}

	public static void insertCfmlFile(cfFile _cfmlFile, cfmlURI uri, HttpServletRequest REQ) {
		thisObject._insertCfmlFile(_cfmlFile, uri, REQ);
	}

	public static void flushFile(String _file) {
		thisObject.loadedFiles._flushFile(_file);
	}

	public static boolean isTrustCache() {
		return bTrustCache;
	}

	// note: not i18n safe
	public static InputStream getInputStream(ServletContext context, String _bdDirectory) throws IOException {
		// This method takes in the directory description for BD and returns an InputStream to that
		// resource. This is mainly a helper method for configuration files.
		if (_bdDirectory.length() == 0)
			return null;

		if (_bdDirectory.length() > 0 && _bdDirectory.charAt(0) == '/') {
			// Relative path
			return new FileInputStream(new File(FileUtils.getRealPath(_bdDirectory)));
		} else if (_bdDirectory.length() > 0 && _bdDirectory.charAt(0) == '$') {
			// Real path (UNIX/Linux)
			return new FileInputStream(new File(_bdDirectory.substring(1)));
		} else {
			// Real path (Windows)
			return new FileInputStream(new File(_bdDirectory));
		}
	}

	// this method is only invoked by cfmRunTimeException.printSourceCode()
	@SuppressWarnings("deprecation")
	public static BufferedReader getReader(HttpServletRequest req, ServletContext context, cfmlURI uri) throws Exception {
		if (!uri.isRealFile()) {
			try {
				// The resource is a normal URI request This method is called at request time so it must use
				// request.getRealPath() instead of context.getRealPath().
				File fin = FileUtils.getRealFile(req, uri.getURI());
				cfFileEncoding fileEncoding = new cfFileEncoding(fin);
				return fileEncoding.getReader(fin);
			} catch (cfmBadFileException e) {
				if (e.isPageEncodingException()) {
					return new BufferedReader(new FileReader(req.getRealPath(uri.getURI())));
				} else {
					throw e;
				}
			}
		} else {
			try {
				// --[ The URI object does not represent a URL but a real path
				cfFileEncoding fileEncoding = new cfFileEncoding(uri.getFile());
				return fileEncoding.getReader(uri.getFile());
			} catch (cfmBadFileException e) {
				if (e.isPageEncodingException()) {
					return new BufferedReader(new FileReader(uri.getRealPath()));
				} else {
					throw e;
				}
			}
		}
	}

	public static Set<cfmlURI> listFiles(cfSession _Session, cfmlURI uriDir) {
		if (uriDir.isRealFile())
			return getFileSet(uriDir.getFile());


		File realFile = FileUtils.getRealFile(_Session.REQ, uriDir.getURI());
		if (realFile != null)
			return getFileSet(realFile);

		
		// Now we must search the BDA / WAR files
		return thisObject.getResourcePaths(_Session.CTX, uriDir.getURI());
	}

	private static Set<cfmlURI> getFileSet(File dir) {
		File[] files = dir.listFiles();
		Set<cfmlURI> fileSet = new HashSet<cfmlURI>();

		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				fileSet.add(new cfmlURI(files[i]));
			}
		}
		return fileSet;
	}

	public static cfmlURI deepFindFile(cfSession _Session, cfmlURI uri, String fileExt) {

		if (uri.isRealFile()) {
			// If the uri is a real file then we need to get the files from disk
			try {
				// Build a list of the directories underneath the parent directory
				List<File> rawDirs = new com.nary.io.fileList(uri.getFile().getParent(), "*." + fileExt).list();
				Iterator<File> E = rawDirs.iterator();
				File possibleFile;

				while (E.hasNext()) {
					possibleFile = E.next();
					if (possibleFile.getName().equalsIgnoreCase(uri.getFile().getName()))
						return new cfmlURI(possibleFile);
				}
			} catch (FileNotFoundException EE) {
				return null;
			}

		} else {

			// The directory is defined as a URI and we must search the BDA or the URI space
			Set<cfmlURI> dirs = thisObject.getDeepResourcePaths(_Session.CTX, uri.getParentURI());
			Iterator<cfmlURI> EE = dirs.iterator();
			while (EE.hasNext()) {
				cfmlURI path = EE.next();

				if (path.getURI().indexOf(uri.getParentURI()) != 0)
					continue;

				if (path.getChildURI().equalsIgnoreCase(uri.getChildURI()))
					return path;
			}
		}

		return null;
	}

	public static void flushCache() {
		thisObject.flush();
	}

	public static int filesInCache() {
		return thisObject.loadedFiles.size();
	}

	public static long getStatsHits() {
		return thisObject.loadedFiles.getStatsHits();
	}

	public static long getStatsMisses() {
		return thisObject.loadedFiles.getStatsMisses();
	}

	public static cachedFile[] getCachedFiles() {
		return (cachedFile[]) thisObject.loadedFiles.getEntries();
	}

	public void engineAdminUpdate(xmlCFML config) {
		engineAdminUpdate(null, config);
	}

	public void engineAdminUpdate(ServletContext context, xmlCFML config) {
		maxFiles = config.getInt("server.file.maxfiles", Integer.parseInt(DEFAULT_MAX_FILES));
		bTrustCache = config.getBoolean("server.file.trustcache", Boolean.valueOf(DEFAULT_TRUST_CACHE).booleanValue());

		cfEngine.log("cfmlFileCache Configuration. Caching=" + maxFiles + " files; trusted cache=" + bTrustCache);

		// Reload/load the custom tag mappings
		loadCustomTagMappings(config);

		// Reload/load the cf mappings for cfinclude/cfmodule
		loadCFMappings(config);
	}

	public void engineShutdown() {
		flush();
		cfEngine.log("cfmlFileCache was shutdown");
	}

	private cfmlFileCache(ServletContext context, xmlCFML _iniFile) {
		// operations that iterate over loadedFiles must be explicitly synchronized
		loadedFiles = new com.nary.cache.FileCache(maxFiles);
		customTags = new FastMap<String, List<String>>();

		engineAdminUpdate(context, _iniFile);
		cfEngine.registerEngineListener(this);
	}

	public void flush() {
		loadedFiles.flushAll();
		cfEngine.log("cfmlFileCache was flushed");
	}

	private void _removeCfmlFile(cfmlURI uri, HttpServletRequest REQ) {
		loadedFiles.flushEntry(uri.getKey(REQ));
	}

	private cfFile _getCfmlFile(ServletContext context, cfmlURI uri, HttpServletRequest REQ) throws cfmBadFileException {
		// Check to see if this uri is in the current cache
		String cacheKey = uri.getKey(REQ);
		cachedFile fileInCache = (cachedFile) loadedFiles.getFromCache(cacheKey);
		if (fileInCache != null) {
			if (bTrustCache || !fileInCache.wasModified()) {
				return fileInCache.get();
			} else {
				loadedFiles.flushEntry(uri.getKey(REQ));
			}
		}

		try {
			// Get a lock just for this file
			synchronized (loadedFiles.getLock(cacheKey)) {
				// we need to check the loadedFiles again should the last thread have
				// loaded in the file
				fileInCache = (cachedFile) loadedFiles.getFromCache(cacheKey);
				if (fileInCache != null) {
					return fileInCache.get();
				}

				// At this point, the file was either not in the cache, or has expired
				fileInCache = new cachedFile();
				loadFile(context, uri, fileInCache, REQ);

				fileInCache.setURL(uri.getURI());
				fileInCache.file.setCfmlURI(uri);

				// Insert into the cache
				String fileDependency = null;
				if (!bTrustCache && uri.isRealFile())
					fileDependency = uri.getRealPath();
				
				loadedFiles.setInCache(cacheKey, fileInCache, fileDependency);

				// Quick check to delete the oldest files
				if (loadedFiles.size() > maxFiles) {
					loadedFiles.deleteOldestFile();
				}

				// Return the file
				return fileInCache.get();
			}
		} finally {
			// Be sure to remove the lock for this file
			loadedFiles.removeLock(cacheKey);
		}
	}

	private void loadFile(ServletContext context, cfmlURI cfmluri, cachedFile _fileInCache, HttpServletRequest REQ) throws cfmBadFileException {
		
		if ( cfmluri.isArchiveFile() ){

			try {
				Reader	reader	= OpenBDArchive.getReader( cfmluri.getArchiveFile(), cfmluri.getURI() );
				_fileInCache.file = new cfFile(cfmluri, reader, "UTF-8" );
			} catch (Exception e) {
				throw new cfmBadFileException( e.getMessage() );
			}

		}else{
			
			if (!cfmluri.isRealFile()) {
				// The resource is a normal URI request
				_fileInCache.setRealPath(cfmluri.getRealPath(REQ));
			} else {
				// The URI object does not represent a URL but a real path
				_fileInCache.setRealPath(cfmluri.getRealPath());
			}
	
			String realPath = _fileInCache.getRealPath();
			if (realPath == null)
				throw new cfmBadFileException(cfmluri.getURI());
						
			File theFile = new File(realPath);
			if (FileUtils.exists(theFile, cfmluri)){
				
				_fileInCache.file = new cfFile(cfmluri, theFile);
				
				if ( theFile.getName().endsWith(".cfc") && ComponentFactory.emptyComponentFile(_fileInCache.file) ){
					try {
						_fileInCache.file	= ComponentScriptFactory.load( _fileInCache.file );
						if ( _fileInCache.file == null )
							throw new cfmBadFileException(_fileInCache.getRealPath());
					} catch (cfmRunTimeException e) {
						e.getCatchData().setFileURI(cfmluri);
						throw new cfmBadFileException( e.getCatchData(), null );
					}
				}
				
			}else{
				
				if ( cfmluri.isMapSearch() && cfMappings.size() > 0 ){
					cfmlURI cfmluriTryFromArchive = cfINCLUDE.getMappedCfmlURI( null, cfmluri.getURI() );
					if ( cfmluriTryFromArchive != null )
						loadFile( context, cfmluriTryFromArchive, _fileInCache, REQ );
					else
						throw new cfmBadFileException(_fileInCache.getRealPath());
				}else
					throw new cfmBadFileException(_fileInCache.getRealPath());
			}

		}
	}

	private void _insertCfmlFile(cfFile _cfmlFile, cfmlURI uri, HttpServletRequest REQ) {
		String cacheKey = uri.getKey(REQ);
		try {
			// Get a lock just for this file
			synchronized (loadedFiles.getLock(cacheKey)) {
				cachedFile fW = new cachedFile(_cfmlFile, null, uri.getURI());
				fW.setNeverExpire();
				
				// Since the file is set to never expire, don't set a file dependency.
				String fileDependency = null;
				loadedFiles.setInCache(cacheKey, fW, fileDependency);
			}
		} finally {
			// Be sure to remove the lock for this file
			loadedFiles.removeLock(cacheKey);
		}
	}

	// ------------------------------------------------------
	// --[ BDA Support
	// ------------------------------------------------------

	private Set<cfmlURI> getDeepResourcePaths(ServletContext context, String path) {
		Set<cfmlURI> paths = getResourcePaths(context, path);
		Set<cfmlURI> subPaths = new HashSet<cfmlURI>();
		Iterator<cfmlURI> iter = paths.iterator();
		while (iter.hasNext()) {
			String nextPath = iter.next().getURI();
			if (nextPath.endsWith("/") && !nextPath.equals(path)) {
				subPaths.addAll(getDeepResourcePaths(context, nextPath));
			}
		}
		paths.addAll(subPaths);
		return paths;
	}

	private Set<cfmlURI> getResourcePaths(ServletContext context, String path) {

		// Check the repositories
		Set<String> resultSet = context.getResourcePaths(path);
		Set<cfmlURI> newResults = new HashSet<cfmlURI>();

		// Check for null because if the path passed to context.getResourcePaths()
		// doesn't contain any files then WLS will return null.
		if (resultSet != null) {
			// Transform the list of strings into a list of cfmlURI's
			Iterator<String> it = resultSet.iterator();
			while (it.hasNext()) {
				newResults.add(new cfmlURI(it.next(), ""));
			}
		}

		return newResults;
	}

	// ----------------------------------------------------------
	// ---] Custom Tag support
	// ----------------------------------------------------------

	private void loadCustomTagMappings(xmlCFML _properties) {
		// Load in the CFML Custom tags
		customTags = new FastMap<String, List<String>>();
		Vector elements = null;

		elements = _properties.getKeys("server.cfmlcustomtags.mapping[]");
		if (elements == null) {
			cfEngine.log("cfmlFileCache.loadCustomTagMappings: no custom tag mappings defined.");
			return;
		}

		Enumeration E = elements.elements();
		while (E.hasMoreElements()) {
			String key = (String) E.nextElement();
			String prefix = key.substring(key.indexOf("[") + 1, key.lastIndexOf("]"));
			if (prefix.indexOf("_") == -1 && prefix.indexOf(":") == -1)
				prefix += "_";
			prefix = prefix.toUpperCase().replace(':', '_').trim();

			// Read in the directories specified for this prefix
			List<String> directories = new ArrayList<String>();
			String dirString = _properties.getString(key + ".directory");
			if (dirString != null) {
				stringtokenizer dirTokenizer = new stringtokenizer(dirString, ";");
				while (dirTokenizer.hasMoreTokens())
					directories.add(dirTokenizer.nextToken());

				// Add the prefix and its directories to the customTags hashtable
				customTags.put(prefix, directories);

				cfEngine.log("cfmlFileCache.loadCustomTagMappings [" + prefix + "] >> [" + dirString + "]");
			} else {
				cfEngine.log("cfmlFileCache.loadCustomTagMappings: Failed to read custom tag Mappings. Invalid or missing data.");
				return;
			}
		}
	}

	public void loadCFMappings(xmlCFML _config) {
		cfMappings = new FastMap<String, String>();
		Vector elements = _config.getKeys("server.cfmappings.mapping[]");

		if (elements == null) {
			cfEngine.log("cfmlFileCache.loadCFMappings: no CF mappings defined.");
			return;
		}

		Enumeration E = elements.elements();
		while (E.hasMoreElements()) {
			String key = (String) E.nextElement();

			String logicalPath = _config.getString(key + ".name");
			String directory = _config.getString(key + ".directory");

			if (logicalPath != null && directory != null) {
				cfMappings.put(logicalPath, directory);
				cfEngine.log("cfmlFileCache.CFMapping [" + logicalPath + "] >> [" + directory + "]");
			} else {
				cfEngine.log("cfmlFileCache.CFMapping: Failed to read CF Mappings. Invalid or missing data.");
			}
		}
	}

	private void _removeAllNonCFMappings() {
		if (customTags != null) {
			Iterator<String> iter = customTags.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				if (key.indexOf("CF_") == -1) {
					cfEngine.log("cfmlFileCache.Removed CustomTagMapping [" + key + "]");
					customTags.remove(key);
				}
			}
		}
	}

	private List<String> _getCustomDirMapping(String tagname) {
		if (customTags != null) {
			Iterator<String> iter = customTags.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				if (tagname.indexOf(key) == 0) {
					return customTags.get(key);
				}
			}
		}
		return null;
	}

	private boolean _isCustomTag(String tag) {
		if (tag.toLowerCase().indexOf("cfx") == 0)
			return true;
		
		if (customTags != null) {
			tag = tag.replace(':', '_');
			Iterator<String> iter = customTags.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				if (tag.indexOf(key) == 0)
					return true;
			}
		}
		return false;
	}
}