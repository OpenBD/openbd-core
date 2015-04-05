/*
 *  Copyright (C) 2000 - 2009 TagServlet Ltd
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

package com.naryx.tagfusion.expression.function;

import java.io.File;
import java.util.List;

import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfINCLUDE;

public class expandPath extends functionBase {

	private static final long serialVersionUID = 1;

	public expandPath() {
		min = max = 1;
	}

  public String[] getParamInfo(){
		return new String[]{
			"path"
		};
	}

	public java.util.Map getInfo(){
		return makeInfo(
				"file",
				"Translates the given web path to a real file system path",
				ReturnType.STRING );
	}

	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		return new cfStringData(expand(_session, parameters.get(0).getString()));
	}

	/*
	 * We pull this out into a static as we wish to use this from other areas
	 * within the engine
	 */
	public static String expand(cfSession _session, String data) {
		// normalize the path removing occurrences of "./", resolving as many "../"
		// as possible
		// and normalizing the separator to '/'
		data = normalizePath(data, false);

		// paths ending with '/' should end with a '/' in the output,
		// those not ending with a slash shouldn't end with a slash in the output
		boolean endSlash = data.endsWith("/");

		// --[ Check to see if the path is indeed relative
		boolean absolutePath = (data.length() > 0 && data.charAt(0) == '/');

		String baseUri = null;
		if (!absolutePath) {
			// Relative paths are to be "rooted" at the path of "base"/"first"
			// template,
			// not the current template.
			baseUri = _session.getBaseTemplatePath();
			int c1 = baseUri.lastIndexOf("/");
			if (c1 > -1)
				baseUri = baseUri.substring(0, c1) + File.separatorChar;

			// no further processing is necessary for relative paths except to
			// concatenate, normalize, and return (mappings are not applicable).
			return normalizePath((baseUri + data), true).replace('/', File.separatorChar);

		} else {

			// check if it's a mapped path first
			String mappedPath = cfINCLUDE.getMappedPath(_session, data, false);
			if (mappedPath != null) {
				if (mappedPath.length() > 0) {
					mappedPath = mappedPath.replace('/', File.separatorChar);
					if (mappedPath.charAt(0) == '$') {
						//here is part of the fix for OpenBD #204
						mappedPath = FileUtils.getCanonicalPath(mappedPath.substring(1));
						return mappedPath;

						// relative path
					} else if (mappedPath.charAt(0) == File.separatorChar) {
						// baseUri = mappedPath;
						data = mappedPath;
					} else {
						return mappedPath;
					}
				}
			}

			if (baseUri == null) {
				baseUri = "/";
				data = data.substring(1);
			}
		}

		// Note that we call getRealPath() on the base uri only. This keeps it
		// simple since the
		// path contained in 'data' may contain '..' elements which may not be
		// honoured
		// if it makes the path outside of the www root.
		String baseFilepath = FileUtils.getRealPath(_session.REQ, baseUri).replace('\\', '/');
		String realPath = baseFilepath;

		// If the real path doesn't end with a forward slash then we need to append
		// a file separator
		if (!realPath.endsWith("/")) {
			realPath = realPath + "/";
		}

		// now append the normalized path passed in to the real path and clean it
		// up, replacing all forward slashes with file separators
		realPath = realPath + data;
		realPath = normalizePath(realPath, true).replace('/', File.separatorChar);

		if (!endSlash && (realPath.endsWith("/") || realPath.endsWith("\\"))) {
			realPath = realPath.substring(0, realPath.length() - 1);
		}

		return realPath;
	}

	private static String normalizePath(String _path, boolean _removeExcess) {
		StringBuilder path = new StringBuilder(_path);

		int i = 0;
		int dirCount = 0;
		while (i < path.length()) {
			switch (path.charAt(i)) {
			case '\\':
				path.setCharAt(i, '/');
			case '/':
				// remove sequences of '/' e.g. '//' '///'
				if (i > 0 && path.charAt(i - 1) == '/') {
					path = path.deleteCharAt(i);
					break; // don't wish to increment i since we've just deleted a char
				}

				// remove "./"
				if (i > 1 && path.charAt(i - 1) == '.' && path.charAt(i - 2) == '/') {
					path = path.deleteCharAt(i);
					path = path.deleteCharAt(i - 1);
					i--; // deleted 2 chars so adjust i and don't increment
					break;
				}

				// handle "../"
				if (i >= 2 && path.charAt(i - 1) == '.' && path.charAt(i - 2) == '.' && ((i - 3 == -1) || path.charAt(i - 3) == '/')) {
					if (dirCount > 0) {
						// remove the "../" and the previous directory
						path = path.delete(i - 2, i + 1);
						int j = i - 4;
						while (j > 0 && path.charAt(j) != '/') {
							j--;
						}
						path = path.delete((j == 0 && path.charAt(j) != '/') ? 0 : j + 1, i - 2);
						i = j + 1;
						dirCount--;
						break;
					} else if (_removeExcess) {
						// just remove the "../"
						path = path.delete(i - 2, i + 1);
						break;
					}
				} else if (i != 0 && !(i == 2 && path.charAt(1) == ':')) { // only
																																		// increment
																																		// if it's
																																		// not the
																																		// drive
																																		// letter
					dirCount++;
				}
				i++;
				break;

			case '.':
				if (i == (path.length() - 1)) {
					if (i > 1 && path.charAt(i - 1) == '/') {
						path = path.deleteCharAt(i);
						path = path.deleteCharAt(i - 1);
						i--; // deleted 2 chars so adjust i and don't increment
						break;
					}

					if (i > 2 && path.charAt(i - 1) == '.' && path.charAt(i - 2) == '/' && dirCount > 0) {
						path = path.delete(i - 2, i + 1);
						int j = i - 4;
						while (j > 0 && path.charAt(j) != '/') {
							j--;
						}
						path = path.delete(j == 0 ? 0 : j + 1, i - 2);
						i = j + 1;
						dirCount--;
						break;
					}

				}

			default:
				i++;
			}
		}

		return path.toString();
	}
}
