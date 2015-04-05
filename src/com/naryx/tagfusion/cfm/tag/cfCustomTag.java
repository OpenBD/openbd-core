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
 *  $Id: cfCustomTag.java 2334 2013-03-03 16:41:52Z alan $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.file.cfmlFileCache;
import com.naryx.tagfusion.cfm.file.cfmlURI;

public class cfCustomTag extends cfMODULE implements cfOptionalBodyTag, Serializable {
	static final long serialVersionUID = 1;

	private transient cfmlURI[] directoryMapping;

	private String[] directories = null; // used for CFIMPORTed tags

	private boolean imported = false;

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		tagName = getTagName(_tag).trim();
		customTagName = tagName.toUpperCase();
		parseTagHeader(_tag);

		// Need to get the file mapping
		setDirectoryMappings();
		setCustomTagKey(tagName);
	}

	private void setDirectoryMappings() throws cfmBadFileException {
		List<String> directory = cfmlFileCache.getCustomDirMapping(tagName.toUpperCase());
		if (directory == null) {
			throw invalidAttributeException("cfmodule.missingMapping", new String[] { tagName });
		}
		String filename = tagName.substring(tagName.indexOf("_") + 1) + ".cfm";
		directoryMapping = new cfmlURI[directory.size()];
		for (int i = 0; i < directory.size(); i++) {
			directoryMapping[i] = new cfmlURI(directory.get(i), filename);
		}
	}

	// used by cfimported custom tags
	protected void defaultParameters(String _tag, String _dir) throws cfmBadFileException {
		tagName = getTagName(_tag, false).trim();
		// doesn't matter what the prefix of the cfimport tag is, the custom tag name begins with "CF_"
		customTagName = "CF_" + tagName.substring(tagName.indexOf(':') + 1).toUpperCase();
		imported = true;
		parseTagHeader(_tag);

		directories = com.nary.util.string.convertToList(_dir, ',');
		setCustomTagKey(tagName);
	}

	private static String getTagName(String tag) {
		return getTagName(tag, true);
	}

	private static String getTagName(String tag, boolean _convert) {
		// find first whitespace character
		int c1 = -1;

		for (int i = 1; (i < tag.length()) && (c1 == -1); i++) {
			if (Character.isWhitespace(tag.charAt(i)))
				c1 = i;
		}

		String tagname;
		if (c1 == -1) {
			if (tag.endsWith("/>"))
				tagname = tag.substring(1, tag.length() - 2);
			else
				tagname = tag.substring(1, tag.length() - 1);
		} else
			tagname = tag.substring(1, c1);

		if (_convert) {
			return tagname.replace(':', '_');
		}
		return tagname;
	}

	/**
	 * Have to override the cfMODULE implementation as it excludes the possibility of NAME, TEMPLATE and ATTRIBUTECOLLECTION named attributes (e.g. in <cf_mycustomtag name="foo">, the name attribute would not get passed to the attributes scope of this tag.
	 */
	protected cfStructData packageUpAttributes(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributeValues = super.packageUpAttributes(_Session);
		if (properties.containsKey("TEMPLATE")) {
			attributeValues.setData("template", getDynamic(_Session, "TEMPLATE"));
		}

		if (properties.containsKey("NAME")) {
			attributeValues.setData("name", getDynamic(_Session, "NAME"));
		}

		return attributeValues;
	}

	// -----------------------------------------------------------------

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfFile svrFile = null;

		if (imported) {
			resolveImportedTagMappings(_Session);
			// shallow search of custom tag directories (does not search local directory)
			svrFile = getCustomTagFile(_Session, tagName.substring(tagName.indexOf(":") + 1), directoryMapping, false, false, false);
		} else {
			String tagFileName = tagName.substring(tagName.indexOf("_") + 1);

			if (directoryMapping == null)
				setDirectoryMappings();

			// Do we have a custom tag situation?
			cfmlURI[] dirs = getCombinedCustomTagDirectories(_Session, tagFileName + ".cfm", directoryMapping);

			// deep search of custom tag directories (including local directory)
			svrFile = getCustomTagFile(_Session, tagFileName, dirs, true, true);
		}

		// Check to see we have an instance
		if (svrFile == null)
			throw newRunTimeException(catchDataFactory.missingCustomTagException(tagName));

		// Render the custom file
		// return customRender(_Session, svrFile);
		return realCustomRender(_Session, svrFile);
	}

	// ----------------------------------------------------------

	// if this tag was imported then it resolves the directoryMapping field
	private void resolveImportedTagMappings(cfSession _Session) {

		// if this custom tag was created as an imported tag
		// (i.e. the mapping was CFIMPORTed to enable this custom tag)
		if (directories != null) {
			String dir;
			List<cfmlURI> mappings = new ArrayList<cfmlURI>();
			for (int i = 0; i < directories.length; i++) {
				dir = directories[i];

				// resolve the dir to include the file name
				String filename = tagName.substring(tagName.indexOf(":") + 1) + ".cfm";
				if (!dir.endsWith("/")) {
					dir = dir + '/';
				}
				dir = dir + filename;

				// if the path given is relative to the current directory
				if (!dir.startsWith("/")) {
					String path = _Session.getPresentFilePath();
					if (path == null) {
						path = _Session.getPresentURIPath();
					}
					mappings.add(new cfmlURI(path, dir));
				} else {
					// the path is relative to the web root so work out the
					// dir remains the same but cf mappings also need to be searched
					cfmlURI resolvedPath = getMappedCfmlURI(_Session, dir);

					if (resolvedPath != null) {
						mappings.add(new cfmlURI(_Session.REQ, dir));
						mappings.add(resolvedPath);
					} else {
						mappings.add(new cfmlURI(_Session.REQ, dir));
					}

				}
			}

			directoryMapping = new cfmlURI[mappings.size()];
			for (int j = 0; j < mappings.size(); j++) {
				directoryMapping[j] = (cfmlURI) mappings.get(j);
			}
		}
	}
}
