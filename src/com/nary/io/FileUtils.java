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
 *  http://openbd.org/
 *  $Id: FileUtils.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.nary.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.vfs.FileObject;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.aw20.io.StreamUtil;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.file.cfmlFileCache;
import com.naryx.tagfusion.cfm.file.cfmlURI;

/**
 * Utility class to handle file manipulation.
 */
public class FileUtils extends Object {
	private static AtomicInteger counter = new AtomicInteger(new Random().nextInt() & 0xffff);

	public static int LIST_TYPE_ALL = 0;

	public static int LIST_TYPE_DIR = 1;

	public static int LIST_TYPE_FILE = 2;

	public static int LIST_INFO_ALL = 0;

	public static int LIST_INFO_NAME = 1;

	/**
	 * Delete all files in the directory whose names match the specified pattern.
	 * @throws MalformedPatternException 
	 */
	public static void deleteFiles(String _dir, String _pattern) throws IOException, MalformedPatternException {
		org.apache.oro.text.regex.Perl5Compiler perl = new org.apache.oro.text.regex.Perl5Compiler();
		Pattern pattern = perl.compile( escapeFilter( _pattern ), Perl5Compiler.CASE_INSENSITIVE_MASK );

		try {
			File[] filesToDelete = new File(_dir).listFiles((FileFilter) new CustomFileFilter(pattern, false));
			for (int i = 0; i < filesToDelete.length; i++) {
				filesToDelete[i].delete();
			}
		} catch (Throwable t) {
			throw new IOException(t.getMessage());
		}
	}

	
	/**
	 * These methods are for use by the CFDIRECTORY tag.
	 * @throws MalformedPatternException 
	 */
	public static List<Map<String, cfData>> createFileVector(File dir, String _pattern, boolean _recurse, int listType, int listInfo) throws MalformedPatternException {
		Pattern pattern = null;
		if ( _pattern != null ){
			org.apache.oro.text.regex.Perl5Compiler perl = new org.apache.oro.text.regex.Perl5Compiler();
			pattern = perl.compile( escapeFilter( _pattern ), Perl5Compiler.CASE_INSENSITIVE_MASK );
		}
		
		if (listInfo == LIST_INFO_NAME) {
			return createFilenameVector(listFilenames(dir, "", pattern, _recurse, listType));
		} else {
			return createFileVector(listFiles(dir, pattern, _recurse, listType), dir, _recurse);
		}
	}

	public static List<Map<String, cfData>> createFileVector(File dir, boolean _recurse, int listType, int listInfo) {
		if (listInfo == LIST_INFO_NAME) {
			return createFilenameVector(listFilenames(dir, "", null, _recurse, listType));
		} else {
			return createFileVector(listFiles(dir, null, _recurse, listType), dir, _recurse);
		}
	}

	private static List<File> listFiles(File dir, Pattern _pattern, boolean _recurse, int listType) {
		File[] files = (_pattern == null ? dir.listFiles() : listFiles(dir, _pattern, listType));
		List<File> filesList = new ArrayList<File>();
		Perl5Matcher matcher = ( _pattern == null ? null : new Perl5Matcher() );

		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				boolean isDir = files[i].isDirectory();

				if (isDir && _recurse) {
					filesList.addAll(listFiles(files[i], _pattern, _recurse, listType));
				}

				if ((listType == LIST_TYPE_DIR && !isDir) || (listType == LIST_TYPE_FILE && isDir))
					continue;

				if ( _pattern == null || matcher.matches( files[i].getName(), _pattern ) ){
					filesList.add(files[i]);
				}
			}
		}
		return filesList;
	}

	private static List<String> listFilenames(File dir, String _parentDir, Pattern _pattern, boolean _recurse, int listType) {
		File[] files = (_pattern == null ? dir.listFiles() : listFiles(dir, _pattern, listType));
		List<String> filesList = new ArrayList<String>();

		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				boolean isDir = files[i].isDirectory();

				if (isDir && _recurse) {
					filesList.addAll(listFilenames(files[i], _parentDir + files[i].getName() + "/", _pattern, _recurse, listType));
				}

				if ((listType == LIST_TYPE_DIR && !isDir) || (listType == LIST_TYPE_FILE && isDir))
					continue;

				
				if (_pattern == null || listType == LIST_TYPE_ALL ) {
					filesList.add(_parentDir + files[i].getName());
				}
			}
		}
		return filesList;
	}

	private static List<Map<String, cfData>> createFilenameVector(List<String> files) {
		if (files == null)
			return null;

		List<Map<String, cfData>> resultVector = new ArrayList<Map<String, cfData>>();

		for (int i = 0; i < files.size(); i++) {
			Map<String, cfData> hm = new FastMap<String, cfData>();
			hm.put("name", new cfStringData(files.get(i)));

			hm.put("size", new cfNumberData(-1));
			hm.put("directory", new cfStringData(""));
			hm.put("type", new cfStringData(""));
			hm.put("datelastmodified", new cfNumberData(-1));
			hm.put("attributes", new cfStringData(""));
			hm.put("mode", new cfStringData(""));

			resultVector.add(hm);
		}

		return resultVector;
	}

	private static List<Map<String, cfData>> createFileVector(List<File> files, File rootdir, boolean recurse) {
		if (files == null)
			return null;

		String rootDirString = rootdir.getAbsolutePath();
		List<Map<String, cfData>> resultVector = new ArrayList<Map<String, cfData>>();
		int rootprefix	= 1 + rootDirString.length();

		for (int i = 0; i < files.size(); i++) {
			File f = files.get(i);
			Map<String, cfData> hm = new FastMap<String, cfData>();

			if ( recurse ){
				// Make this a relative path
				hm.put("name", new cfStringData(f.getAbsolutePath().substring(rootprefix)));
				hm.put("directory", new cfStringData(rootDirString));
			}else{
				hm.put("name", new cfStringData(f.getName()));
				hm.put("directory", new cfStringData(f.getParent()));
			}
			
			hm.put("size", new cfNumberData(f.length()));

			if (f.isDirectory()) {
				hm.put("type", new cfStringData("Dir"));
			} else {
				hm.put("type", new cfStringData("File"));
			}

			hm.put("datelastmodified", new cfDateData(f.lastModified()));

			StringBuilder attrs = new StringBuilder();

			if (!f.canWrite())
				attrs.append('R');

			if (f.isHidden())
				attrs.append('H');

			hm.put("attributes", new cfStringData(attrs.toString()));
			hm.put("mode", new cfStringData(""));

			resultVector.add(hm);
		}

		return resultVector;
	}

	/**
	 * The "pattern" string recognizes two wildcards characters:
	 * 
	 * * - matches 0 or more characters ? - matches exactly one character
	 * 
	 * Characters that are not wildcards are taken to be literal.
	 */
	private static File[] listFiles(File dir, Pattern _pattern, int listType) {
		return dir.listFiles((FilenameFilter) new CustomFileFilter( _pattern, true));
	}

	private static class CustomFileFilter implements java.io.FilenameFilter {

		private boolean includeDirs = true;
		private Pattern pattern;
		private Perl5Matcher matcher;
		
		public CustomFileFilter( Pattern _pattern, boolean _includeDirs) {
			this.includeDirs = _includeDirs;
			pattern = _pattern;
			matcher = new Perl5Matcher();
		}

		public boolean accept(File _path) {
			if (_path.isDirectory()) {
				return this.includeDirs;
			} else {
				boolean match = matcher.matches( _path.getName(), pattern );
				return match;
			}
		}

		@Override
		public boolean accept(File _path, String _filename) {
			return accept(new File(_path, _filename));
		}

	}

	private static String escapeFilter(String _filter) {
		String filter = _filter;
		filter = com.nary.util.string.replaceString(filter, "?", "\\?");
		filter = com.nary.util.string.replaceString(filter, "+", "\\+");
		filter = com.nary.util.string.replaceString(filter, ".", "\\.");
		filter = com.nary.util.string.replaceString(filter, "$", "\\$");
		filter = com.nary.util.string.replaceString(filter, "^", "\\^");
		filter = com.nary.util.string.replaceString(filter, "\\?", ".");
		filter = com.nary.util.string.replaceString(filter, "(", "\\(");
		filter = com.nary.util.string.replaceString(filter, ")", "\\)");
		filter = com.nary.util.string.replaceString(filter, "[", "\\[");
		filter = com.nary.util.string.replaceString(filter, "]", "\\]");
		filter = com.nary.util.string.replaceString(filter, "{", "\\{");
		filter = com.nary.util.string.replaceString(filter, "}", "\\}");
		return com.nary.util.string.replaceString(filter, "*", ".*");
	}

	/**
	 * resolveNativeLibPath
	 */
	public static String resolveNativeLibPath(String nativeLibPath) throws IOException {
		if (new cfmlURI(nativeLibPath).isRealFile()) {
			File nativeLib = cfEngine.getResolvedFile(nativeLibPath);
			if (!nativeLib.exists()) {
				throw new IOException("Native library does not exist: " + nativeLibPath);
			}
			return nativeLib.getCanonicalPath();
		}

		// copy native lib to temporary file, and return path to temp file
		InputStream is = cfEngine.thisServletContext.getResourceAsStream(nativeLibPath);
		if (is == null) {
			throw new IOException("Could not load native library: " + nativeLibPath);
		}
		File tempFile = File.createTempFile("LIB", (cfEngine.WINDOWS ? ".dll" : ".so"), cfEngine.thisPlatform.getFileIO().getTempDirectory());
		OutputStream fos = cfEngine.thisPlatform.getFileIO().getFileOutputStream(tempFile);

		StreamUtil.copyTo(is, fos);

		return tempFile.getCanonicalPath();
	}

	/**
	 * The following methods were provided by Montara for use by the CFSEARCH classes.
	 */
	public static void recursiveDelete(File file, boolean dirToo) throws IOException {
		File[] files = file.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory())
					recursiveDelete(files[i], true);
				else
					files[i].delete();
			}
		}

		if (dirToo) {
			file.delete();
		}
	}

	public static File getRealFile(HttpServletRequest request, String path) {
		String realPath = getRealPath(request, path);
		if (realPath == null) {
			return null;
		}
		return new File(realPath);
	}

	/**
	 * This method should be used instead of HttpServletRequest.getRealPath()
	 * 
	 * If we can't get the path, then lets drop to use the ServletContext as a final resort
	 */
	@SuppressWarnings("deprecation")
	public static String getRealPath(HttpServletRequest request, String path) {
		path = path.replace('\\', '/');
		String realPath = request.getRealPath(path);
		if (realPath == null) { // WebLogic packed WAR
			realPath = getWebLogicRealPath(path);
			if (realPath == null) {
				realPath = cfEngine.thisServletContext.getRealPath(path);
			}
		}
		return realPath;
	}

	/**
	 * This method should be used instead of ServletContext.getRealPath()
	 */
	public static String getRealPath(String path) {
		path = path.replace('\\', '/');
		String realPath = cfEngine.thisServletContext.getRealPath(path);
		if (realPath == null) { // WebLogic packed WAR
			realPath = getWebLogicRealPath(path);
		}
		return realPath;
	}

	/**
	 * This only works on WLS 9.x, which unpacks WARs to a temp directory, but still returns null from context.getRealPath() and request.getRealPath(). This method returns null if the resource does not exist, unlike context.getRealPath() and request.getRealPath()
	 */
	private static String getWebLogicRealPath(String path) {
		try {
			java.net.URL url = cfEngine.thisServletContext.getResource(path);
			if ((url != null) && (url.getProtocol().equalsIgnoreCase("file"))) {
				return url.getPath();
			}
		} catch (java.net.MalformedURLException ignore) {
		}
		return null;
	}

	public static String getRealPath(File root, File file) throws IOException {
		String rtn = null;
		String rootAbs = root.getCanonicalPath();
		String abs = file.getCanonicalPath();
		if (abs.startsWith(rootAbs)) {
			if (abs.equals(rootAbs))
				return "";
			abs = abs.substring(rootAbs.length());
			if (abs.startsWith(File.separator)) {
				abs = abs.substring(File.separator.length());
			}
			rtn = abs;
		}
		return rtn;
	}

	public static long getLastModified(String realPath) {
		if (realPath != null) {
			return new File(realPath).lastModified();
		}
		return 0;
	}

	/**
	 * Given a directory and filename, combine them and return the full canonical path.
	 */
	public static String getCanonicalPath(String directory, String fileName) {
		File f = new File(directory, fileName);
		try {
			return f.getCanonicalPath();
		} catch (IOException e) {
			return f.getPath();
		}
	}

	public static String getCanonicalPath(String path) {
		if (path != null) {
			try {
				return new File(path).getCanonicalPath();
			} catch (java.io.IOException ignore) {
			}
		}
		return path;
	}

	public static String combine(String path1, String path2) {
		return new File(path1, path2).getPath();
	}

	public static boolean exists(String path) {
		return new File(path).exists();
	}

	/**
	 * Replacement for File.exists() method designed to provide an accurate and efficient check that a file really exists. This method is necessary because of an error in the Windows JavaVM in which file names beginning with the same name as a MS-DOS device (e.g. con.cfm, or com1.cfm, etc) will be reported as existing by File.exists(). [ This causes the current thread to hang while attempting to
	 * read from the DOS device thus [ preventing further requests to be processed; thus a "denial of service" vulnerability. This alternative method is designed to return quickly for the typical case where the URL is supported with a real file. Note however that empty real files cannot be confirmed without completing all the validation steps required to absolutely confirm the MS-DOS device file
	 * condition. This should be a rare case. Note: - getCanonicalPath() provides inconsistent results for non-existing file dependent upon whether a previous call was made with an existant file! (yes, I tested this!) - DOS device names cannot be used as file names on Windows XP nor 2003; i.e. aux.cfm or con.cfm cannot be created. - getCanonicalPath() will sometimes return the pathname with the
	 * ".cfm" extension truncated for these "DOS device" files.
	 */
	public static boolean exists(File _thefile, cfmlURI _uri) {
		if (_thefile.length() != 0) { // all DOS devices have zero length...
			return true; // therefore most valid files will satisfy these tests and return here.
		}

		if (_thefile.lastModified() == 0) { // all valid files have a non-zero value, but DOS devices may also ...
			return false;
		}

		// if we make it here then _thefile.length() == 0, and _thefile.lastModified() != 0,
		// so now we need to make sure it's a valid empty file and not a DOS device name
		try {
			// _thefile.getCanonicalPath() has three possible results:
			// 1. throw an IOException, in which case the file doesn't exist
			// 2. returns a path including the file extension, in which case the file exists
			// 3. returns a path that doesn't have the file extension, in which case the file doesn't exist
			String filename = _thefile.getCanonicalPath();

			// Check for the case that filename has had the ".cfm" extension truncated by comparing against the original URI
			String uri = _uri.getURI();
			if (filename.endsWith(uri.substring(uri.lastIndexOf('.')))) {
				return true;
			}
			return false;
		} catch (IOException e) {
			return false; // Exception guarantees that file does not exist.
		}
	}

	/**
	 * Starting with the current directory of the requested template, look for Application.cfc and Application.cfm, searching parent directories until one is found or we reach the root of the file system.
	 * 
	 * The "requestPath" parameter is expected to be a full physical path that includes the name of the requested template.
	 */
	public static String findApplicationFile(String requestPath) {
		File f = null;
		String parentPath = requestPath; // start in the current directory

		try {

			do {
				parentPath = new File(parentPath).getParent();
				if ((parentPath == null) || (parentPath.length() == 0)) {
					return null; // reached file system root
				}

				// look for Application.cfc first, then Application.cfm
				f = new File(parentPath, cfSession.APPLICATION_CFC);
				if (f.exists()) {
					return f.getPath();
				}
				f = new File(parentPath, cfSession.APPLICATION_CFM);
			} while (!f.exists());

			return f.getPath();

		} catch (Exception e) { // GoogleAppEngine throws java.security.AccessControlException if we go outside the file root
			return null;
		}

	}

	public static String getOnRequestEndCfm(String path) {
		File f = new File(new File(path).getParent(), cfSession.ON_REQUEST_END_CFM);
		if (f.exists()) {
			return f.getPath();
		}
		return null;
	}

	public static String getExtension(String fileName) {
		return ("." + org.apache.commons.io.FilenameUtils.getExtension(fileName));
	}

	public static void copy(String sourceFile, String destFile) throws IOException {
		org.apache.commons.io.FileUtils.copyFile(new File(sourceFile), new File(destFile));
	}

	/**
	 * This method takes a root directory and a directory name, and will then create a series of nested directories to ensure maximum spread throughout a directory structure for files that may have a large number of files. The maximum size a subdirectory will be is 2 characters.
	 * 
	 * @param root
	 * @param dirname
	 * @return
	 */
	public static File deepMakedirs(File root, String dirname) {
		File newdir;

		// get the end piece
		if (dirname.length() == 1)
			newdir = new File(root, dirname);
		else
			newdir = new File(root, dirname.substring(0, 2));

		// Create the directory
		if (!newdir.isDirectory()) {
			newdir.mkdir();
		}

		if (dirname.length() == 1)
			return newdir;

		dirname = dirname.substring(2);
		if (dirname.length() == 0) {
			return newdir;
		} else
			return deepMakedirs(newdir, dirname);
	}

	/**
	 * Cleans up the path, removing any /./ and /../
	 * 
	 * @param pathIn
	 * @return
	 */
	public static String cleanPath(String pathIn) {
		return cleanPath(pathIn, '/');
	}

	public static String cleanPath(String pathIn, char fileseparator) {
		String tmp = pathIn;
		String slashdotslash = fileseparator + "." + fileseparator;
		String slashdotdotslash = fileseparator + ".." + fileseparator;

		// Sort out the /./ combinations
		int c1 = tmp.indexOf(slashdotslash);
		while (c1 != -1) {
			tmp = tmp.substring(0, c1) + fileseparator + tmp.substring(c1 + 3);
			c1 = tmp.indexOf(slashdotslash);
		}

		c1 = tmp.indexOf(slashdotdotslash);
		while (c1 != -1) {
			int c2 = tmp.lastIndexOf(fileseparator, c1 - 1);
			if (c2 != -1) {
				tmp = tmp.substring(0, c2 + 1) + tmp.substring(c1 + 4);
			} else
				break;

			c1 = tmp.indexOf(slashdotdotslash);
		}

		return tmp;
	}

	/*
	 * Helper Function to write content to a given file
	 */
	public static void writeFile(File outFile, String content) throws IOException {
		Writer outWriter = null;
		try {
			outWriter = cfEngine.thisPlatform.getFileIO().getFileWriter(outFile);
			outWriter.write(content);
			outWriter.flush();
		} finally {
			try {
				if (outWriter != null)
					outWriter.close();
			} catch (Exception ignoreCloseException) {
			}
		}
	}

	/*
	 * Reads a file into the given String Builder
	 */
	public static void readFile(File filePath, StringBuilder buffer) throws IOException {
		BufferedReader reader = null;
		Reader inreader = null;

		try {
			inreader = new FileReader(filePath);
			reader = new BufferedReader(inreader);

			char[] chars = new char[8096];
			int read;
			while ((read = reader.read(chars, 0, chars.length)) != -1) {
				buffer.append(chars, 0, read);
			}
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException ignored) {
				}
			if (inreader != null)
				try {
					inreader.close();
				} catch (IOException ignored) {
				}
		}
	}

	/*
	 * Checks to see that a given directory exists, and if it does, optional delete its contents
	 */
	public static File checkAndCreateDirectory(File rootDirectory, String subsdir, boolean deleteContents) throws Exception {
		File newDir = new File(rootDirectory, subsdir);

		if (!newDir.isDirectory()) {
			newDir.mkdirs();
			if (!newDir.isDirectory())
				throw new Exception("failed to create the directory: " + newDir);
		}

		if (deleteContents) {
			recursiveDelete(newDir, false);
		}

		return newDir;
	}

	/**
	 * Performs a MIME comparison against the file name
	 */
	public static boolean acceptContent(String _contentType, String _acceptable) {
		if (_contentType == null) {
			return false;
		}

		String[] acceptables = com.nary.util.string.convertToList(_acceptable, ',');

		String contentType = _contentType;
		int charsetSeparatorIndx = contentType.indexOf(';');
		if (charsetSeparatorIndx != -1) {
			contentType = contentType.substring(0, charsetSeparatorIndx);
		}

		int slashIndx = _contentType.indexOf('/');
		if (slashIndx != -1) { // check if we are fed a bad mimetype for whatever reason
			// breakdown the mime type into it's 2 parts
			String main = contentType.substring(0, slashIndx);
			String subtype = contentType.substring(slashIndx + 1);

			for (int i = 0; i < acceptables.length; i++) {
				String nextAcceptable = acceptables[i].trim();
				if (nextAcceptable.equals("*") || nextAcceptable.equals("*/*"))
					return true;

				int starIndx = nextAcceptable.indexOf('*');
				if (starIndx != -1) { // we're trying to match against mime types like text/* or */html
					slashIndx = nextAcceptable.indexOf('/');
					if (slashIndx != -1) {
						String acceptMain = nextAcceptable.substring(0, slashIndx);
						String acceptSubtype = nextAcceptable.substring(slashIndx + 1);

						if ((acceptMain.equalsIgnoreCase("*") && acceptSubtype.equalsIgnoreCase(subtype)) || (acceptMain.equalsIgnoreCase(main) && acceptSubtype.equalsIgnoreCase("*"))) {
							return true;
						}
					}

				} else if (nextAcceptable.equalsIgnoreCase(contentType)) {
					return true;
				}

			}
		}
		return false;
	}

	public static String getCleanFilePath(cfSession _Session, String _path, boolean _bURI) {
		String cleanPath = _path;

		if (File.separatorChar == '/') {
			cleanPath = cleanPath.replace('\\', '/');
		} else {
			cleanPath = cleanPath.replace('/', '\\');
		}

		if (_bURI)
			cleanPath = FileUtils.getRealPath(_Session.REQ, cleanPath);

		return cleanPath;
	}

	public static void removeFromFileCache(cfSession _Session, String _filepath) {
		String filename = _filepath;
		int filenameIndx = filename.lastIndexOf(File.separatorChar);
		filename = filename.substring(filenameIndx + 1);
		_Session.removeFromFileCache(filename);
		cfmlFileCache.flushFile(filename);
	}

	public static File getFile(cfSession _Session, String _path, boolean _bURI) {
		File file = new File(getCleanFilePath(_Session, _path, _bURI));
		if (!file.isAbsolute()) {
			file = new File(cfEngine.thisPlatform.getFileIO().getTempDirectory(), file.getPath());
		}
		return file;
	}

	public static FileObject createTempFile(String prefix, String suffix, FileObject directory) throws IOException {
		if (prefix == null) {
			throw new NullPointerException();
		}
		if (prefix.length() < 3) {
			throw new IllegalArgumentException("Prefix string too short");
		}

		return directory.resolveFile(prefix + Integer.toString(counter.getAndIncrement()) + (suffix == null ? ".tmp" : suffix));
	}

}
