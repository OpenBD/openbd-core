/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  $Id: cfMODULE.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nary.cache.cfmlURICache;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.application.cfAPPLICATION;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmAbortException;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.engineListener;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.file.cfmlFileCache;
import com.naryx.tagfusion.cfm.file.cfmlURI;

/**
 * Inherits the TEMPLATE processing from CFINCLUDE tag.
 */

public class cfMODULE extends cfINCLUDE implements cfOptionalBodyTag, Serializable, Cloneable {
	static final long serialVersionUID = 1;

	public static final String THISTAG_SCOPE = "thistag";
	public static final String EXECUTION_MODE = "executionmode";
	public static final cfStringData START_MODE = new cfStringData("start");
	public static final cfStringData END_MODE = new cfStringData("end");
	public static final cfStringData INACTIVE_MODE = new cfStringData("inactive");
	public static final String GENERATED_CONTENT = "generatedcontent";
	public static final String HAS_END_TAG = "hasendtag";

	private static cfmlURICache locationCache = new cfmlURICache(10 * 60);

	private String endMarker = null;
	private static transient List<String> directoryList;
	private transient boolean isRendering = false;

	protected String customTagName; // the name of the tag deduced from it's attributes
	protected String customTagKey;
	
	static {
		cfEngine.registerEngineListener(new EngineListener());
	}

	public cfMODULE() {
		// allows definition of subclasses
	}

	public String getEndMarker() {
		return endMarker;
	}

	public boolean isRendering() {
		return isRendering;
	}

	public void setRendering(boolean rendering) {
		isRendering = rendering;
	}

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		tagName = "CFMODULE";
		parseTagHeader(_tag);

		if (containsAttribute("TEMPLATE") && containsAttribute("NAME"))
			throw invalidAttributeException("cfmodule.tooManyAttributes", null);

		if (containsAttribute("NAME")) {
			// Need to get the file mapping
			directoryList = cfmlFileCache.getCustomDirMapping("CF_XYZ");
			if (directoryList == null)
				throw invalidAttributeException("cfmodule.missingMapping", new String[] { "CF_" });
		}
	}

	public void setEndTag() {
		// This is called once from the cfParseTag class. its to handle <CFMODULE/> which is to trigger double execution
		endMarker = "";
	}

	public void lookAheadForEndTag(tagReader inFile) {
		endMarker = new tagLocator(tagName, inFile).findEndMarker();
	}

	/**
	 * We have to clone this tag's instance because this tag allows us to have a <cfmodule name="#abc#">
	 */
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		try {
			cfMODULE clone = (cfMODULE) this.clone();
			clone.customTagName = clone.getRealTagName(_Session);

			_Session.replaceTag(clone);
			return clone.realCustomRender(_Session, clone.getCustomTagFile(_Session));

		} catch (CloneNotSupportedException e) {
			return cfTagReturnType.NORMAL;
		}
	}

	protected cfTagReturnType realCustomRender(cfSession _Session, cfFile customTag) throws cfmRunTimeException {

		// Setup the paramters
		cfStructData thisTag = new cfStructData();
		thisTag.setData(GENERATED_CONTENT, new cfStringData(""));
		if (endMarker == null)
			thisTag.setData(HAS_END_TAG, cfBooleanData.FALSE);
		else
			thisTag.setData(HAS_END_TAG, cfBooleanData.TRUE);

		cfStructData attributes = packageUpAttributes(_Session);
		_Session.enterCustomTag(thisTag, attributes, customTagName);
		isRendering = true;

		thisTag.setData(EXECUTION_MODE, START_MODE);

		// Render the file
		boolean suppressWhitespace = _Session.setSuppressWhiteSpace(cfEngine.getSuppressWhiteSpaceDefault());
		try {
			cfTagReturnType rt = renderCustomTagStart(_Session, customTag); // render the start tag
			if (rt.isExit()) {
				if (rt.getMethod().equals(cfEXIT.METHOD_EXITTAG)) {
					_Session.clearCustomTag();
					isRendering = false;
					return cfTagReturnType.NORMAL;
				} else if (rt.getMethod().equals(cfEXIT.METHOD_LOOP)) {
					throw newRunTimeException("CFEXIT of type METHOD=LOOP is not valid in START mode");
				}
				// keep going for cfEXIT.METHOD_EXITTEMPLATE
			}
		} finally {
			_Session.setSuppressWhiteSpace(suppressWhitespace);
		}

		// Check to see if the tag has an end tag
		if (endMarker != null) {
			boolean processingCfOutput = _Session.setProcessingCfOutput(true);
			suppressWhitespace = _Session.setSuppressWhiteSpace(false);
			thisTag.setData(EXECUTION_MODE, INACTIVE_MODE);

			while (true) {
				// render the body between the start/end tags
				cfStructData varScope = _Session.leaveCustomTag();
				isRendering = false;
				int renderOptions = cfTag.DEFAULT_OPTIONS;

				if (!processingCfOutput)
					renderOptions = renderOptions | cfTag.CF_OUTPUT_ONLY;

				if (suppressWhitespace)
					renderOptions = renderOptions | cfTag.SUPPRESS_WHITESPACE;

				thisTag.setData(GENERATED_CONTENT, new cfStringData(renderToString(_Session, renderOptions).getOutput()));

				// if any attributes have been added via cfASSOCIATE in child tags
				addAssociatedAttributes(_Session, this, thisTag);

				_Session.enterCustomTag(thisTag, attributes, customTagName, varScope, false);
				isRendering = true;

				try {
					thisTag.setData(EXECUTION_MODE, END_MODE);

					// render the end tag
					cfTagReturnType rt = renderCustomTagEnd(_Session, customTag, cfEngine.getSuppressWhiteSpaceDefault());

					// The content buffer may have been reset at this point (e.g. by cfcontent reset="yes") so don't
					// write out the generated content if it has
					if (!_Session.hasBufferReset()) {
						_Session.write(thisTag.getData(GENERATED_CONTENT).getString());
					}
					_Session.write(rt.getOutput());
					if (rt.isExit() && rt.getMethod().equals(cfEXIT.METHOD_LOOP)) {
						continue;
					}
					break; // if no CFEXIT METHOD=LOOP then we're done
				} catch (cfmAbortException ae) {
					if (ae.flushOutput()) {
						_Session.write(thisTag.getData(GENERATED_CONTENT).getString());
						_Session.write(ae.getOutput());
					}
					throw ae;
				}
			}
			_Session.setSuppressWhiteSpace(suppressWhitespace);
			_Session.setProcessingCfOutput(processingCfOutput);
		}

		_Session.clearCustomTag();
		isRendering = false;

		return cfTagReturnType.NORMAL;
	}

	// ----------------------------------------------------

	public static cfTagReturnType renderCustomTagStart(cfSession _Session, cfFile customTag) throws cfmRunTimeException {
		boolean isFiltered = pushAndSuspend(_Session, customTag);
		cfTagReturnType rt = customTag.render(_Session);
		if (!rt.isExit() || !rt.getMethod().equals(cfEXIT.METHOD_LOOP)) {
			// cfmExitExceptions are normal if a CFEXIT tag gets processed, except that
			// METHOD=LOOP is an error in start mode (this method is always in start mode)
			unsuspendAndPop(_Session, isFiltered);
		}
		return rt;
	}

	public static cfTagReturnType renderCustomTagEnd(cfSession _Session, cfFile customTag, boolean suppressWhiteSpace) throws cfmRunTimeException {
		boolean isFiltered = pushAndSuspend(_Session, customTag);
		int renderOptions = cfTag.CF_OUTPUT_ONLY | cfTag.SUPPRESS_OUTPUT_AFTER_ABORT | (suppressWhiteSpace ? cfTag.SUPPRESS_WHITESPACE : 0);
		cfTagReturnType rt = customTag.renderToString(_Session, renderOptions);
		unsuspendAndPop(_Session, isFiltered);
		return rt;
	}

	public static void addAssociatedAttributes(cfSession _Session, cfMODULE customTag, cfStructData _thisTag) {
		String basetag = customTag.getCustomTagName();

		// TODO: where is the _Session.putDataBin() that goes with this? we need to very the type cast
		Map<cfMODULE, cfStructData> associateVars = (Map<cfMODULE, cfStructData>) _Session.getDataBin(cfASSOCIATE.DATA_BIN_KEY);
		if (associateVars == null) {
			return;
		}

		// --[ get the datacollections associated with this basetag
		cfStructData dataColls = associateVars.get(customTag);

		// if it doesn't exist, create it
		if (dataColls == null) {
			return;
		}

		Object[] keys = dataColls.keys();
		for (int i = 0; i < keys.length; i++) {
			String nextKey = (String) keys[i];
			_thisTag.setData(nextKey, dataColls.getData(nextKey));
		}

		// now remove these associated attributes
		associateVars.remove(basetag);
	}

	private static boolean pushAndSuspend(cfSession _Session, cfFile customTag) {
		_Session.pushActiveFile(customTag);

		if (_Session.isFiltered()) {
			_Session.suspendFilter();
			return true;
		}

		return false;
	}

	private static void unsuspendAndPop(cfSession _Session, boolean isFiltered) {
		if (isFiltered)
			_Session.unsuspendFilter();

		_Session.popActiveFile();
	}

	// ----------------------------------------------------

	// returns the tagName
	public String getCustomTagName() {
		return customTagName;
	}

	// ----------------------------------------------------

	protected cfStructData packageUpAttributes(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributeValues = new cfStructData();
		String key;

		// Look to see if the ATTRIBUTECOLLECTION has been passed through
		// Note that the ATTRIBUTECOLLECTION is handled first so that specified
		// parameters can override those also supplied in the ATTRIBUTECOLLECTION
		if (properties.containsKey("ATTRIBUTECOLLECTION")) {

			cfData attributeCollection = getDynamic(_Session, "ATTRIBUTECOLLECTION");
			if (attributeCollection.getDataType() == cfData.CFSTRUCTDATA) {
				Object[] keys = ((cfStructData) attributeCollection).keys();
				for (int i = 0; i < keys.length; i++) {
					key = (String) keys[i];
					attributeValues.setData(key, ((cfStructData) attributeCollection).getData(key));
				}
			}
		}

		Iterator<String> iter = properties.keySet().iterator();
		while (iter.hasNext()) {
			key = iter.next();
			if (!key.equalsIgnoreCase("TEMPLATE") && !key.equalsIgnoreCase("NAME") && !key.equalsIgnoreCase("ATTRIBUTECOLLECTION")) {
				attributeValues.setData(key, getDynamic(_Session, key));
			}
		}

		return attributeValues;
	}

	// ----------------------------------------------------

	private cfFile getCustomTagFile(cfSession _Session) throws cfmRunTimeException {

		// If the custom tag is a TEMPLATE directive
		if (containsAttribute("TEMPLATE"))
			return loadTemplate(this, _Session, getDynamic(_Session, "TEMPLATE").getString());

		// --[ Using the NAME attribute
		String name = getDynamic(_Session, "NAME").getString();
		setCustomTagKey(name);

		return getCustomTagFile(_Session, name, getCustomTagDirectories(_Session, name + ".cfm"), false, true);
	}

	/**
	 * Searches for a custom tag file:
	 * 
	 * 1. If "trust cache" is true, return the file if we already have it. 2. Search the custom tag directories.
	 */
	protected cfFile getCustomTagFile(cfSession _Session, String _name, cfmlURI[] _directory, boolean searchCurrentDir, boolean deepFind) throws cfmRunTimeException {
		return getCustomTagFile(_Session, _name, _directory, searchCurrentDir, deepFind, true);
	}

	protected cfFile getCustomTagFile(cfSession _Session, String _name, cfmlURI[] _directory, boolean searchCurrentDir, boolean deepFind, boolean _checkCache) throws cfmRunTimeException {
		cfFile cfTagFile = null;

		if (cfmlFileCache.isTrustCache()) {
			cfmlURI customTagURI = (cfmlURI) locationCache.getFromCache(customTagKey);
			if (customTagURI != null)
				cfTagFile = _Session.getFile(customTagURI);

			if (cfTagFile != null)
				return cfTagFile;
		}

		// search the custom tag directories
		cfTagFile = getCustomTagFile(_Session, _name, "cfm", customTagKey, _directory, searchCurrentDir, deepFind, _checkCache).setURI(_name.replace('.', '/'));
		return cfTagFile;
	}

	/**
	 * This version of getCustomTagFile is a convenience method that could be used by a JSP custom tag library.
	 */
	public static cfFile getCustomTagFile(cfSession _Session, String name) throws cfmRunTimeException {
		return getCustomTagFile(_Session, name, "cfm", name.toLowerCase(), getCustomTagDirectories(_Session, name + ".cfm"), true, true, true);
	}

	/**
	 * First check the locationCache for the tag file URI; if it's not there, then search for the custom tag file and then add it to the location cache.
	 */
	protected static cfFile getCustomTagFile(cfSession _Session, String _name, String fileExt, String customTagKey, cfmlURI[] _directory, boolean searchCurrentDir, boolean deepFind, boolean _checkLocCache) throws cfmRunTimeException {
		// Try the directory that was in the cache
		if (_checkLocCache) {
			cfmlURI customTagUri = (cfmlURI) locationCache.getFromCache(customTagKey);
			if (customTagUri != null) {
				try {
					return _Session.getFile(customTagUri);
				} catch (cfmBadFileException BFEE) {
					locationCache.flushEntry(customTagKey);
					if (!BFEE.fileNotFound()) {
						cfCatchData catchData = catchDataFactory.summarizeBadFileException(_Session.activeTag(), "Badly formatted template", BFEE);
						catchData.setSession(_Session);
						throw new cfmRunTimeException(catchData, BFEE);
					}
				}
			}
		}

		cfFile cfTagFile = null;

		if (searchCurrentDir) {
			// look in the current (local) directory; return null if not found
			cfTagFile = getLocalCustomTagFile(_Session, _name + "." + fileExt);
		}

		// now search for the tag file
		if (cfTagFile == null) {
			// throw exception if not found
			cfTagFile = getCustomTagFile(_Session, _name, fileExt, _directory, deepFind);
			locationCache.setInCache(customTagKey, cfTagFile.getCfmlURI().copy());
		}

		// don't put in locationCache if found in local directory
		return cfTagFile;
	}

	/**
	 * Search for the custom tag file in the specified directories. Do a shallow or deep directory search.
	 */
	public static cfFile getCustomTagFile(cfSession _Session, String _name, String fileExt, cfmlURI[] _directory, boolean deepFind) throws cfmRunTimeException {
		cfFile cfTagFile;
		if (_name.indexOf(".") == -1) {
			// This tag is a simple <CFMODULE NAME='mycustom'> or <CF_mycustomtag>
			if (deepFind) {
				cfTagFile = deepFindTagFile(_Session, fileExt, _directory);
			} else {
				cfTagFile = shallowFindTagFile(_Session, _directory);
			}
			if (cfTagFile == null) {
				throw newRunTimeException(catchDataFactory.missingCustomTagException(_name));
			}
		} else {
			// This tag is a <CFMODULE NAME='directory.directory2.mycustom'>
			cfTagFile = huntDirectories(_Session, _name, fileExt);
		}
		return cfTagFile;
	}

	// Methods for locating the custom tags

	/**
	 * Look for the custom tag file in the current directory.
	 */
	public static cfFile getLocalCustomTagFile(cfSession _Session, String tagFileName) throws cfmRunTimeException {
		cfFile svrFile = null;

		String presentFilePath = _Session.getPresentFilePath();
		if (presentFilePath == null) { // running in packed WAR
			try {
				svrFile = _Session.getUriFile(tagFileName);
			} catch (cfmBadFileException bfe) {
				handleBadFileException(_Session, bfe);
			}
			return svrFile;
		}

		try {
			// first try lowercase for UNIX/Linux
			svrFile = _Session.getFile(new cfmlURI(presentFilePath, tagFileName.toLowerCase()));
		} catch (cfmBadFileException bfe) {
			handleBadFileException(_Session, bfe);
		}

		if (svrFile == null) {
			try {
				// try exact-case match
				svrFile = _Session.getFile(new cfmlURI(presentFilePath, tagFileName));
			} catch (cfmBadFileException bfe) {
				handleBadFileException(_Session, bfe);
			}
		}
		return svrFile;
	}

	private static cfFile shallowFindTagFile(cfSession _Session, cfmlURI[] directoryMappings) throws cfmRunTimeException {
		cfFile svrFile = null;

		for (int i = 0; i < directoryMappings.length; i++) {
			try {
				svrFile = cfmlFileCache.getCfmlFile(_Session.CTX, directoryMappings[i], _Session.REQ);
				if (svrFile != null) {
					return svrFile;
				}
			} catch (cfmBadFileException bfe) {
				handleBadFileException(_Session, bfe);
			}
		}

		return svrFile;
	}

	private static cfFile deepFindTagFile(cfSession _Session, String fileExt, cfmlURI[] directoryMappings) throws cfmRunTimeException {
		cfFile svrFile = null;
		cfmlURI activeURI = null;

		try {
			for (int i = 0; i < directoryMappings.length; i++) {
				activeURI = cfmlFileCache.deepFindFile(_Session, directoryMappings[i], fileExt);
				if (activeURI != null) {
					break;
				}
			}
			if (activeURI != null)
				svrFile = _Session.getFile(activeURI);

		} catch (cfmBadFileException bfe) {
			handleBadFileException(_Session, bfe);
		}

		return svrFile;
	}

	private static void handleBadFileException(cfSession _Session, cfmBadFileException bfe) throws cfmRunTimeException {
		if (!bfe.fileNotFound()) {
			throw bfe;
		}
	}

	private static cfFile huntDirectories(cfSession _Session, String namePath, String fileExt) throws cfmRunTimeException {

		// need to perform a case-insenstive search of the directories
		cfmlURI[] directoryMappings = getCustomTagDirectories(_Session, "");

		// Put the names into an array for easy access
		List<String> tokens = string.split(namePath, ".");
		String[] dirList = new String[tokens.size()];
		int x = 0;
		for (int i = 0; i < tokens.size(); i++)
			dirList[x++] = tokens.get(i).trim(); // preserve case here

		Set<cfmlURI> dirSet = new HashSet<cfmlURI>();
		for (int i = 0; i < directoryMappings.length; i++) {
			dirSet.addAll(cfmlFileCache.listFiles(_Session, directoryMappings[i]));
		}

		cfmlURI fileToFind = null;
		while (fileToFind == null) {
			// there may be duplicate directory names in the custom tag paths, so search them all
			cfmlURI dir = findMatch(dirSet, dirList[0]);
			if (dir == null) {
				// searched all custom tag directories, need to throw a bad file exception
				throw newRunTimeException(catchDataFactory.missingCustomTagException(namePath));
			}
			dirSet.remove(dir); // for the next loop iteration

			// search the sub-directories of the current custom tag directory
			Set<cfmlURI> subdirSet = cfmlFileCache.listFiles(_Session, dir);
			boolean foundSubdir = true;

			for (int i = 1; i < dirList.length - 1; i++) {
				cfmlURI subdir = findMatch(subdirSet, dirList[i]);
				if (subdir == null) {
					foundSubdir = false;
					break;
				}
				subdirSet = cfmlFileCache.listFiles(_Session, subdir);
			}

			// if we found the sub-directory, see if the file is there
			if (foundSubdir) {
				fileToFind = findMatch(subdirSet, dirList[dirList.length - 1] + "." + fileExt);
			}
		}

		try {
			return _Session.getFile(fileToFind);
		} catch (cfmBadFileException BFEE) {
			throw (!BFEE.fileNotFound() ? BFEE : newRunTimeException(catchDataFactory.missingCustomTagException(namePath)));
		}
	}

	public static cfmlURI[] getCustomTagDirectories(cfSession session, String filename) throws cfmRunTimeException {
		if (directoryList == null) {
			directoryList = cfmlFileCache.getCustomDirMapping("CF_XYZ");
			if (directoryList == null) {
				throw newRunTimeException(catchDataFactory.missingCustomTagException("CFMODULE"));
			}
		}

		cfmlURI[] directories = new cfmlURI[directoryList.size()];
		for (int i = 0; i < directoryList.size(); i++)
			directories[i] = new cfmlURI(directoryList.get(i), filename);

		// See if this has any custom mappings for this directory
		return getCombinedCustomTagDirectories(session, filename, directories);
	}

	public static cfmlURI[] getCombinedCustomTagDirectories(cfSession session, String filename, cfmlURI[] directories) {
		String customTagPaths = (String) session.getDataBin(cfAPPLICATION.CUSTOMTAGPATHS);
		if (customTagPaths != null) {
			String[] paths = customTagPaths.split(",");
			cfmlURI[] customDirectories = new cfmlURI[paths.length];

			for (int x = 0; x < paths.length; x++)
				customDirectories[x] = new cfmlURI(paths[x], filename);

			cfmlURI[] combinedDirs = new cfmlURI[directories.length + customDirectories.length];

			int c = 0;

			for (int x = 0; x < customDirectories.length; x++)
				combinedDirs[c++] = customDirectories[x];

			for (int x = 0; x < directories.length; x++)
				combinedDirs[c++] = directories[x];

			return combinedDirs;
		} else
			return directories;
	}

	private static cfmlURI findMatch(Set<cfmlURI> dirList, String directory) {
		// Runs through the directories seeing if there is a match
		Iterator<cfmlURI> IT = dirList.iterator();
		while (IT.hasNext()) {
			cfmlURI thisDir = IT.next();

			// Remove a trailing slash from the URLs if its present
			String dirToTest = thisDir.getURI().replace('\\', '/');
			if (dirToTest.endsWith("/"))
				dirToTest = dirToTest.substring(0, dirToTest.length() - 1);
			dirToTest = dirToTest.substring(dirToTest.lastIndexOf('/') + 1);
			if (dirToTest.equalsIgnoreCase(directory))
				return thisDir;
		}
		return null;
	}

	protected void setCustomTagKey(String key) {
		customTagKey = key.toLowerCase();
	}

	/**
	 * Gets the custom tag name for this CFMODULE. This is inferred from the specified NAME/TEMPLATE attribute. It is used in functions such as GetBaseTagList() and in matching tags with CFASSOCIATE.
	 */
	protected String getRealTagName(cfSession _session) {
		try {
			String template = null;
			String name = null;
			if (containsAttribute("TEMPLATE")) {
				template = getDynamic(_session, "TEMPLATE").getString();
			} else {
				name = getDynamic(_session, "NAME").getString();
			}
			return getRealTagName(template, name);
		} catch (cfmRunTimeException ignored) {
			// should never happen
		}
		return "CFMODULE"; // won't get here
	}

	public static String getRealTagName(String template, String name) {
		if (template != null) {
			// note if lastIndexOf() returns -1, slashIndex = 0 which is what we want
			int slashIndex = template.lastIndexOf('/') + 1;
			int dotIndex = template.lastIndexOf('.');
			if (dotIndex == -1)
				dotIndex = template.length();

			return "CF_" + template.substring(slashIndex, dotIndex).toUpperCase();
		} else if (name != null) {
			return "CF_" + name.toUpperCase();
		}
		return "CFMODULE"; // won't get here
	}

	private static class EngineListener implements engineListener {
		public void engineAdminUpdate(com.naryx.tagfusion.xmlConfig.xmlCFML config) {
			directoryList = null;
			locationCache.flushAll();
		}

		public void engineShutdown() { // do nothing
		}
	}
}
